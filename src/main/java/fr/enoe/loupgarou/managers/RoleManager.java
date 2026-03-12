package fr.enoe.loupgarou.managers;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.roles.impl.village.*;
import fr.enoe.loupgarou.roles.impl.loup.*;
import fr.enoe.loupgarou.roles.impl.solitaire.*;
import fr.enoe.loupgarou.roles.impl.binaire.*;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class RoleManager {

    private final LoupGarouPlugin plugin;

    /** UUID → Rôle */
    private final Map<UUID, Role> roles = new HashMap<>();

    /** UUID protégé par le Salvateur ce tour */
    private UUID salvateurProtected = null;

    /** Dernier mort de nuit pour la Sorcière */
    private UUID lastNightDead = null;

    /** Loups empoisonnés (fausse info) */
    private final Set<UUID> poisoned = new HashSet<>();

    /** Liste des loups (pour communication entre loups) */
    private final Set<UUID> wolfTeam = new HashSet<>();

    /** Actions effectuées cette nuit (pour la Conteuse) */
    private final Set<UUID> nightActors = new HashSet<>();

    public RoleManager(LoupGarouPlugin plugin) {
        this.plugin = plugin;
    }

    // ─── ATTRIBUTION ────────────────────────────────────────────────────────────

    /**
     * Attribue les rôles selon la config à tous les joueurs en vie.
     */
    public void assignRoles(List<UUID> players) {
        Collections.shuffle(players);
        roles.clear();
        wolfTeam.clear();

        List<String> configRoles = plugin.getConfig().getStringList("roles.enabled");
        int wolfCount = Math.min(
                plugin.getConfig().getInt("roles.wolves", 1),
                (int) Math.floor(players.size() * 0.30)
        );

        List<String> roleQueue = new ArrayList<>();

        // Ajouter les loups
        for (int i = 0; i < wolfCount; i++) roleQueue.add("loup_simple");

        // Ajouter les rôles configurés (sans dépasser le nombre de joueurs)
        for (String r : configRoles) {
            if (!r.startsWith("loup_simple")) roleQueue.add(r);
        }

        // Compléter avec des simple_villagois
        while (roleQueue.size() < players.size()) roleQueue.add("simple_villagois");

        // Tronquer si trop
        while (roleQueue.size() > players.size()) roleQueue.remove(roleQueue.size() - 1);

        Collections.shuffle(roleQueue);

        for (int i = 0; i < players.size(); i++) {
            UUID uuid = players.get(i);
            String roleId = roleQueue.get(i);
            Role role = createRole(roleId, uuid);
            if (role != null) {
                roles.put(uuid, role);
                if (role.getFamily() == RoleFamily.LOUP) wolfTeam.add(uuid);
            }
        }

        // Cas Sœurs : lier entre elles
        linkSisters();

        // Informer les loups entre eux
        informWolves();
    }

    private Role createRole(String id, UUID uuid) {
        return switch (id) {
            // Village
            case "maire"            -> new Maire(plugin, uuid);
            case "citoyen"          -> new Citoyen(plugin, uuid);
            case "voyant"           -> new Voyant(plugin, uuid);
            case "renard"           -> new Renard(plugin, uuid);
            case "conteuse"         -> new Conteuse(plugin, uuid);
            case "salvateur"        -> new Salvateur(plugin, uuid);
            case "sorciere"         -> new Sorciere(plugin, uuid);
            case "chasseur"         -> new Chasseur(plugin, uuid);
            case "astronome"        -> new Astronome(plugin, uuid);
            case "chevalier"        -> new ChevaliereEpeeRouillee(plugin, uuid);
            case "ancien"           -> new Ancien(plugin, uuid);
            case "simple_villagois" -> new SimpleVillagois(plugin, uuid);
            case "idiot"            -> new IdiotDuVillage(plugin, uuid);
            case "soeur"            -> new Soeur(plugin, uuid);
            case "monteur_ours"     -> new MonteurDOurs(plugin, uuid);
            case "petite_fille"     -> new PetiteFille(plugin, uuid);
            // Loups
            case "loup_simple"      -> new LoupSimple(plugin, uuid);
            case "loup_perfide"     -> new LoupPerfide(plugin, uuid);
            case "loup_endormi"     -> new LoupEndormi(plugin, uuid);
            case "loup_vengeur"     -> new LoupVengeur(plugin, uuid);
            case "grand_mechant_loup" -> new GrandMechantLoup(plugin, uuid);
            case "infecte"          -> new InfectePereDesLoups(plugin, uuid);
            case "loup_timide"      -> new LoupTimide(plugin, uuid);
            case "loup_empoisonneur"-> new LoupEmpoisonneur(plugin, uuid);
            // Solitaires
            case "loup_blanc"       -> new LoupBlanc(plugin, uuid);
            case "flutiste"         -> new JoueurDeFlute(plugin, uuid);
            case "ange"             -> new Ange(plugin, uuid);
            case "feu_follet"       -> new FeuFollet(plugin, uuid);
            case "imitateur"        -> new Imitateur(plugin, uuid);
            case "assassin"         -> new Assassin(plugin, uuid);
            case "inconnu"          -> new Inconnu(plugin, uuid);
            // Binaires
            case "cupidon"          -> new Cupidon(plugin, uuid);
            case "enfant_sauvage"   -> new EnfantSauvage(plugin, uuid);
            case "trublion"         -> new Trublion(plugin, uuid);
            case "voleur"           -> new Voleur(plugin, uuid);
            case "chien_loup"       -> new ChienLoup(plugin, uuid);
            case "ivrogne"          -> new Ivrogne(plugin, uuid);
            default -> {
                plugin.getLogger().warning("Rôle inconnu : " + id);
                yield new SimpleVillagois(plugin, uuid);
            }
        };
    }

    private void linkSisters() {
        List<UUID> sisters = roles.entrySet().stream()
                .filter(e -> e.getValue().getId().equals("soeur"))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        if (sisters.size() >= 2) {
            ((Soeur) roles.get(sisters.get(0))).setSister(sisters.get(1));
            ((Soeur) roles.get(sisters.get(1))).setSister(sisters.get(0));
        }
    }

    private void informWolves() {
        // Envoyer la liste des loups à chaque loup (sauf endormi)
        List<String> wolfNames = wolfTeam.stream()
                .map(u -> {
                    Player p = Bukkit.getPlayer(u);
                    return p != null ? p.getName() : u.toString();
                })
                .collect(Collectors.toList());

        for (UUID wolfUUID : wolfTeam) {
            Role r = roles.get(wolfUUID);
            if (r instanceof LoupEndormi && !((LoupEndormi) r).isAwake()) continue;
            Player wolf = Bukkit.getPlayer(wolfUUID);
            if (wolf == null) continue;
            wolf.sendMessage("§c[Loups] §7Tes compagnons : §e" + String.join(", ", wolfNames));
        }
    }

    // ─── EFFETS DE DÉPART ───────────────────────────────────────────────────────

    public void applyStartEffects() {
        for (Map.Entry<UUID, Role> entry : roles.entrySet()) {
            Player p = Bukkit.getPlayer(entry.getKey());
            if (p != null) entry.getValue().onGameStart(p);
        }
    }

    // ─── ÉPISODE ────────────────────────────────────────────────────────────────

    public void onEpisodeEnd(int episode) {
        nightActors.clear();
        for (Map.Entry<UUID, Role> entry : roles.entrySet()) {
            entry.getValue().onEpisodeEnd(episode);
            // Conteuse
            if (entry.getValue().getId().equals("conteuse")) {
                Player conteuse = Bukkit.getPlayer(entry.getKey());
                if (conteuse != null && !nightActors.isEmpty()) {
                    conteuse.sendMessage("§d[Conteuse] §7Actions cette nuit : §e" +
                            nightActors.stream().map(u -> {
                                Player p = Bukkit.getPlayer(u);
                                return p != null ? p.getName() : "?";
                            }).collect(Collectors.joining(", ")));
                }
            }
        }
        salvateurProtected = null;
    }

    // ─── TICKS ──────────────────────────────────────────────────────────────────

    public void triggerAstronomeTick() {
        roles.entrySet().stream()
                .filter(e -> e.getValue().getId().equals("astronome"))
                .filter(e -> plugin.getGameManager().isAlive(e.getKey()))
                .forEach(e -> ((Astronome) e.getValue()).triggerTick());
    }

    public void triggerBearTamerTick() {
        // Géré dans onEpisodeEnd pour le Monteur d'ours
    }

    // ─── RÉSURRECTIONS ──────────────────────────────────────────────────────────

    public boolean tryWitchRevive(UUID dead) {
        return roles.values().stream()
                .filter(r -> r.getId().equals("sorciere") && plugin.getGameManager().isAlive(r.getPlayerUUID()))
                .anyMatch(r -> {
                    Sorciere s = (Sorciere) r;
                    if (!s.hasLifePotion()) return false;
                    setLastNightDead(dead);
                    Player witch = Bukkit.getPlayer(r.getPlayerUUID());
                    if (witch != null) witch.sendMessage("§5[Sorcière] §e" + Bukkit.getOfflinePlayer(dead).getName() + " §7vient de mourir. Utilise §e/lg vie §7pour le sauver !");
                    return false; // Ne ressuscite pas automatiquement, attend la commande
                });
    }

    public boolean tryInfectRevive(UUID dead) {
        return roles.values().stream()
                .filter(r -> r.getId().equals("infecte") && plugin.getGameManager().isAlive(r.getPlayerUUID()))
                .anyMatch(r -> ((InfectePereDesLoups) r).tryInfect(dead));
    }

    public boolean tryElderRevive(UUID dead) {
        Role r = roles.get(dead);
        if (!(r instanceof Ancien)) return false;
        // killedByWolves: à déterminer via contexte (simplifié : toujours vrai)
        return ((Ancien) r).tryResurrect(true, null);
    }

    public boolean tryVillageIdiotRevive(UUID dead) {
        Role r = roles.get(dead);
        if (!(r instanceof IdiotDuVillage)) return false;
        return ((IdiotDuVillage) r).tryRevive(true);
    }

    public boolean tryAssassinHideDeath(UUID dead) {
        return roles.values().stream()
                .filter(r -> r.getId().equals("assassin"))
                .anyMatch(r -> ((Assassin) r).tryHideDeath(dead));
    }

    public void revivePlayer(UUID uuid, Player reviver) {
        plugin.getGameManager().getAlivePlayers(); // juste pour ne pas compiler vide
        // Réintégrer dans la partie
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) {
            p.setHealth(p.getMaxHealth());
            MessageUtils.broadcast("§a[Résurrection] §e" + p.getName() + " §aest revenu à la vie !");
        }
    }

    public void infectPlayer(UUID uuid, Player infector) {
        wolfTeam.add(uuid);
        roles.put(uuid, new LoupSimple(plugin, uuid));
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) {
            p.sendMessage("§4[Infecté] §7Tu es maintenant un Loup-Garou !");
            p.setHealth(p.getMaxHealth());
            MessageUtils.broadcast("§a[Résurrection] §e" + p.getName() + " §arevient à la vie... mais quelque chose a changé.");
        }
    }

    // ─── CHASSEUR ───────────────────────────────────────────────────────────────

    public void triggerHunterDeath(Player player) {
        Role r = roles.get(player.getUniqueId());
        if (r instanceof Chasseur) r.onDeath(player);
    }

    // ─── LOUP BLANC ─────────────────────────────────────────────────────────────

    public void checkWhiteWolfWin(List<UUID> alive) {
        roles.entrySet().stream()
                .filter(e -> e.getValue().getId().equals("loup_blanc"))
                .filter(e -> alive.contains(e.getKey()))
                .forEach(e -> {
                    long others = alive.stream().filter(u -> !u.equals(e.getKey())).count();
                    if (others == 0) plugin.getGameManager().endGame("§f§lLe Loup Blanc gagne seul !");
                });
    }

    // Le Joueur de Flûte gagne en étant le dernier survivant (comme tous les solitaires)
    // Plus de victoire par charme — checkFlutistWin est conservé vide pour compatibilité
    public void checkFlutistWin(List<UUID> alive) {
        // Intentionnellement vide — victoire gérée par checkWinCondition dans GameManager
    }

    // ─── ÉCHANGE DE RÔLES (Trublion) ────────────────────────────────────────────

    public void swapRoles(UUID a, UUID b) {
        Role ra = roles.get(a);
        Role rb = roles.get(b);
        if (ra == null || rb == null) return;
        roles.put(a, rb);
        roles.put(b, ra);
        Player pa = Bukkit.getPlayer(a), pb = Bukkit.getPlayer(b);
        if (pa != null) pa.sendMessage("§5[Trublion] §7Ton nouveau rôle : §b" + rb.getDisplayName());
        if (pb != null) pb.sendMessage("§5[Trublion] §7Ton nouveau rôle : §b" + ra.getDisplayName());
        MessageUtils.broadcast("§5[Trublion] §7Deux rôles ont été échangés !");
    }

    // ─── UTILITAIRES ────────────────────────────────────────────────────────────

    public Role getRole(UUID uuid)          { return roles.get(uuid); }
    public boolean isWolf(UUID uuid)        { return wolfTeam.contains(uuid); }
    public boolean isVillager(UUID uuid)    {
        Role r = roles.get(uuid);
        return r != null && r.getFamily() == RoleFamily.VILLAGE;
    }
    public boolean isSolitary(UUID uuid)    {
        Role r = roles.get(uuid);
        return r != null && r.getFamily() == RoleFamily.SOLITAIRE;
    }

    public Set<UUID> getWolfList()          { return Collections.unmodifiableSet(wolfTeam); }
    public void addToWolfTeam(UUID uuid)    { wolfTeam.add(uuid); }

    public UUID getLastNightDead()          { return lastNightDead; }
    public void setLastNightDead(UUID u)    { this.lastNightDead = u; }

    public void setSalvateurProtected(UUID u) { this.salvateurProtected = u; }
    public UUID getSalvateurProtected()       { return salvateurProtected; }

    public void setPoisoned(UUID u, boolean p) {
        if (p) poisoned.add(u); else poisoned.remove(u);
    }
    public boolean isPoisoned(UUID u) { return poisoned.contains(u); }

    public void addNightActor(UUID u) { nightActors.add(u); }

    public Role findRoleById(String id) {
        return roles.values().stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null);
    }

    public Role getRandomRoleForDisplay() {
        List<Role> all = new ArrayList<>(roles.values());
        if (all.isEmpty()) return null;
        return all.get(new Random().nextInt(all.size()));
    }

    public List<String> getRoleCompositionSummary() {
        Map<String, Long> counts = roles.values().stream()
                .collect(Collectors.groupingBy(Role::getDisplayName, Collectors.counting()));
        return counts.entrySet().stream()
                .map(e -> "§7- " + e.getKey() + " §8x" + e.getValue())
                .collect(Collectors.toList());
    }

    public void reset() {
        roles.clear();
        wolfTeam.clear();
        poisoned.clear();
        nightActors.clear();
        salvateurProtected = null;
        lastNightDead = null;
    }
}
