package fr.enoe.loupgarou.managers;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.stream.Collectors;

public class VoteManager {

    private final LoupGarouPlugin plugin;

    /** UUID votant → UUID cible */
    private final Map<UUID, UUID> votes = new HashMap<>();
    /** Dernier vote de chaque joueur (conservé après fermeture) */
    private final Map<UUID, UUID> lastVotes = new HashMap<>();
    private boolean voteOpen = false;

    public VoteManager(LoupGarouPlugin plugin) {
        this.plugin = plugin;
    }

    public void openVote() {
        votes.clear();
        voteOpen = true;
        plugin.getMessageManager().broadcastVoteStart();
        // ── Exposed : s'active avec les votes uniquement si l'événement est activé ──
        if (plugin.getConfig().getBoolean("events.expose", false)) {
            if (new java.util.Random().nextBoolean()) {
                plugin.getEventManager().triggerExpose();
            } else {
                plugin.getEventManager().triggerExposeInverse();
            }
        }
        int duration = plugin.getConfig().getInt("game.village-vote-time", 300);
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override public void run() { closeVote(); }
        }.runTaskLater(plugin, duration * 20L);
    }

    public boolean castVote(Player voter, Player target) {
        if (!voteOpen) { voter.sendMessage(MessageUtils.error("Le vote n'est pas ouvert.")); return false; }
        if (!plugin.getGameManager().isAlive(voter.getUniqueId())) { voter.sendMessage(MessageUtils.error("Les morts ne votent pas.")); return false; }
        Role r = plugin.getRoleManager().getRole(voter.getUniqueId());
        if (r != null && r.getId().equals("inconnu")) { voter.sendMessage(MessageUtils.error("Tu ne peux pas voter.")); return false; }
        votes.put(voter.getUniqueId(), target.getUniqueId());
        lastVotes.put(voter.getUniqueId(), target.getUniqueId());

        // ── Vérifier si ce vote est trafiqué par un LoupMesquin ──────────
        boolean trafique = plugin.getRoleManager().getAllRoles().stream()
            .filter(role -> role instanceof fr.enoe.loupgarou.roles.impl.loup.LoupMesquin)
            .map(role -> (fr.enoe.loupgarou.roles.impl.loup.LoupMesquin) role)
            .anyMatch(lm -> lm.isTrafique(voter.getUniqueId()));
        if (trafique) {
            // Vote enregistré mais ignoré au décompte — on retire silencieusement
            votes.remove(voter.getUniqueId());
            voter.sendMessage("§7Ton vote a été enregistré."); // message neutre, pas d'indice
            plugin.getLogger().info("[LG-TRAFIQUE] Vote de " + voter.getName() + " annulé par LoupMesquin.");
            return true;
        }
        // Notifier le Citoyen de son vote (pour enquête)
        var roleVoter = plugin.getRoleManager().getRole(voter.getUniqueId());
        if (roleVoter instanceof fr.enoe.loupgarou.roles.impl.village.Citoyen c) {
            c.setLastVotedFor(target.getUniqueId());
        }
        plugin.getMessageManager().broadcastVoteCast(voter.getName(), target.getName());
        return true;
    }

    public void closeVote() {
        if (!voteOpen) return;
        voteOpen = false;

        // Comptage avec poids
        Map<UUID, Integer> scores = new HashMap<>();
        for (Map.Entry<UUID, UUID> e : votes.entrySet()) {
            UUID voter = e.getKey();
            UUID target = e.getValue();
            int weight = getVoteWeight(voter, false);
            scores.merge(target, weight, Integer::sum);
        }

        if (scores.isEmpty()) {
            MessageUtils.broadcast("§7Aucun vote. Personne n'est éliminé.");
            return;
        }
        // Trouver le max
        int max = Collections.max(scores.values());
        List<UUID> top = scores.entrySet().stream()
                .filter(e -> e.getValue() == max)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        UUID eliminated;
        if (top.size() == 1) {
            eliminated = top.get(0);
        } else {
            // Égalité → le Maire décide
            UUID maireUUID = getMaireUUID();
            if (maireUUID != null && top.contains(votes.get(maireUUID))) {
                eliminated = votes.get(maireUUID);
            } else {
                // Pas de maire ou pas de vote → personne n'est éliminé
                MessageUtils.broadcast("§7Égalité ! Personne n'est éliminé.");
                return;
            }
        }

        Player victim = Bukkit.getPlayer(eliminated);
        if (victim == null) return;

        // Pénalités vote
        applyVotePenalties(eliminated);

        Role victimRole = plugin.getRoleManager().getRole(eliminated);
        String roleName = victimRole != null ? victimRole.getDisplayName() : "?";
        plugin.getMessageManager().broadcastVoteResult(victim.getName(), max, true, roleName);

        // Message de mort avec tag "vote"
        String deathMsg = plugin.getMessageManager().buildDeathMessage(eliminated, null, true);
        MessageUtils.broadcast(deathMsg);

        plugin.getGameManager().handlePlayerDeath(victim);
        votes.clear();
    }

    private void applyVotePenalties(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        if (p == null) return;

        int penaltyHearts = plugin.getConfig().getInt("game.vote-penalty-hearts", 3);
        int weakDuration  = plugin.getConfig().getInt("game.vote-weakness-duration", 300);

        // Perd 3 cœurs
        p.setHealth(Math.max(1.0, p.getHealth() - (penaltyHearts * 2.0)));
        // Faiblesse 0.5 pendant 5 min
        p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, weakDuration * 20, 0, false, false, true));
        // Révéler le rôle
        Role role = plugin.getRoleManager().getRole(uuid);
        if (role != null) MessageUtils.broadcast("§7Son rôle : §b" + role.getDisplayName());
    }

    private int getVoteWeight(UUID voter, boolean tieBreaker) {
        Role r = plugin.getRoleManager().getRole(voter);
        if (r == null) return 1;
        return switch (r.getId()) {
            case "maire"   -> tieBreaker ? 2 : 2;
            case "citoyen" -> tieBreaker ? 1 : 2;
            default        -> 1;
        };
    }

    private UUID getMaireUUID() {
        return plugin.getRoleManager().getWolfList().stream()
                .filter(u -> {
                    Role r = plugin.getRoleManager().getRole(u);
                    return r != null && r.getId().equals("maire");
                }).findFirst().orElse(null);
        // Note : on cherche dans tous les joueurs, pas que les loups
    }

    /** Retourne le dernier joueur pour lequel cet UUID a voté. */
    public java.util.UUID getLastVoteOf(UUID voter) { return lastVotes.get(voter); }

    public void reset() {
        votes.clear();
        voteOpen = false;
    }

    public boolean isVoteOpen() { return voteOpen; }
}
