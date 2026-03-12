package fr.enoe.loupgarou.managers;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;
import java.util.UUID;

public class MessageManager {

    private final LoupGarouPlugin plugin;
    private static final Random RNG = new Random();

    // Probabilité de révéler le rôle selon la famille
    private static final double REVEAL_VILLAGE   = 0.60;
    private static final double REVEAL_LOUP      = 0.40;
    private static final double REVEAL_SOLITAIRE = 1.00;
    private static final double REVEAL_BINAIRE   = 1.00; // avant transformation

    public MessageManager(LoupGarouPlugin plugin) { this.plugin = plugin; }

    // ── BIENVENUE ──────────────────────────────────────────────────────────────

    public void sendWelcome(Player player) {
        player.sendMessage("");
        player.sendMessage("§6§l╔══════════════════════════════════╗");
        player.sendMessage("§6§l║   §e§lLOUP-GAROU UHC §6§l— §fPaper 1.21   §6§l║");
        player.sendMessage("§6§l╠══════════════════════════════════╣");
        player.sendMessage("§6§l║  §7Bienvenue, §e" + padRight(player.getName(), 22) + "§6§l║");
        player.sendMessage("§6§l║  §7Attends le lancement de l'Owner.  §6§l║");
        player.sendMessage("§6§l║  §7La vie §cne se régénère PAS§7 en UHC. §6§l║");
        player.sendMessage("§6§l║  §7PVP actif après §c20 minutes§7.        §6§l║");
        player.sendMessage("§6§l╚══════════════════════════════════╝");
        player.sendMessage("");
    }

    // ── COUNTDOWN LANCEMENT ───────────────────────────────────────────────────

    public void startCountdown(Runnable onFinish) {
        // Blindness pendant le compte à rebours
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.BLINDNESS, 120, 0, false, false, false));
        }

        broadcast("");
        broadcast("§e§l╔═══════════════════════════════╗");
        broadcast("§e§l║       §6§lLA PARTIE COMMENCE !      §e§l║");
        broadcast("§e§l╚═══════════════════════════════╝");
        broadcast("");

        new BukkitRunnable() {
            int count = 5;
            @Override public void run() {
                if (count > 0) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle(
                            "§c§l" + count,
                            count == 5 ? "§7Préparez-vous..." : count == 1 ? "§aCOURSE !" : "",
                            5, 15, 5
                        );
                    }
                    broadcastCountdown(count);
                    count--;
                } else {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle("§a§lGO !", "§7Bonne chance à tous !", 5, 30, 10);
                        p.removePotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS);
                    }
                    broadcast("§a§l▶ §r§aLa partie est lancée ! Explorez, survivez, déduisez !");
                    broadcast("§7PVP dans §c20 minutes §7— Rôles révélés à §620 minutes§7.");
                    onFinish.run();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void broadcastCountdown(int n) {
        String bar = "§8[" + "§a█".repeat(n) + "§7" + "░".repeat(5 - n) + "§8]";
        broadcast("§e§lLancement dans : §c§l" + n + " §r" + bar);
    }

    // ── MORT D'UN JOUEUR ──────────────────────────────────────────────────────

    /**
     * Génère le message de mort avec révélation de rôle selon les probabilités :
     * Village = 60%, Loup = 40%, Solitaire = 100%, Binaire = 100% (avant transfo)
     */
    public String buildDeathMessage(UUID dead, UUID killer, boolean killedByVote) {
        Player deadPlayer = Bukkit.getPlayer(dead);
        String deadName   = deadPlayer != null ? deadPlayer.getName()
                : Bukkit.getOfflinePlayer(dead).getName();

        Role role        = plugin.getRoleManager().getRole(dead);
        String roleReveal = buildRoleReveal(role);

        if (killedByVote) {
            return "§8§l☠ §e" + deadName + " §7a été éliminé par le §6vote du village§7. " + roleReveal;
        }

        if (killer != null) {
            Player killerPlayer = Bukkit.getPlayer(killer);
            String killerName   = killerPlayer != null ? killerPlayer.getName()
                    : Bukkit.getOfflinePlayer(killer).getName();
            return "§4§l☠ §e" + deadName + " §7a été tué par §c" + killerName + "§7. " + roleReveal;
        }

        // Mort par autre cause (environnement, loup, etc.)
        return "§4§l☠ §e" + deadName + " §7est mort. " + roleReveal;
    }

    private String buildRoleReveal(Role role) {
        if (role == null) return "";

        double threshold = switch (role.getFamily()) {
            case VILLAGE   -> REVEAL_VILLAGE;
            case LOUP      -> REVEAL_LOUP;
            case SOLITAIRE -> REVEAL_SOLITAIRE;
            case BINAIRE   -> REVEAL_BINAIRE;
        };

        if (RNG.nextDouble() < threshold) {
            return "§7(Rôle : §b" + role.getDisplayName() + "§7)";
        }
        return "§7(Rôle : §8inconnu§7)";
    }

    // ── VOTE ─────────────────────────────────────────────────────────────────

    public void broadcastVoteStart() {
        broadcast("");
        broadcast("§6§l╔══════════════════════════════╗");
        broadcast("§6§l║     §e§l⚖ VOTE DU VILLAGE ⚖     §6§l║");
        broadcast("§6§l║  §7/lg voter <joueur> pour voter §6§l║");
        broadcast("§6§l╚══════════════════════════════╝");
        broadcast("");
    }

    public void broadcastVoteResult(String targetName, int votes, boolean revealed, String roleName) {
        broadcast("");
        broadcast("§6§l═══ Résultat du vote ═══");
        broadcast("§e" + targetName + " §7a reçu §c" + votes + " vote(s)§7.");
        broadcast("§c-3 cœurs §7+ §6Faiblesse 5 min§7 appliqués.");
        if (revealed) {
            broadcast("§7Rôle révélé : §b" + roleName);
        }
        broadcast("§6§l════════════════════════");
        broadcast("");
    }

    public void broadcastVoteCast(String voterName, String targetName) {
        broadcast("§7" + voterName + " §6vote contre §e" + targetName + "§6.");
    }

    // ── ANNONCE DES RÔLES ─────────────────────────────────────────────────────

    public void announceRoleReveal() {
        broadcast("");
        broadcast("§6§l╔══════════════════════════════════╗");
        broadcast("§6§l║     §e§l🌙 20 MINUTES ÉCOULÉES ! 🌙     §6§l║");
        broadcast("§6§l║  §7Les rôles ont été révélés en privé. §6§l║");
        broadcast("§6§l║  §c§lLE PVP EST MAINTENANT ACTIF !     §6§l║");
        broadcast("§6§l╚══════════════════════════════════╝");
        broadcast("");
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle("§c§lPVP ACTIVÉ !", "§7Les rôles ont été révélés !", 10, 40, 10);
        }
    }

    // ── COUPLE ────────────────────────────────────────────────────────────────

    // Le couple n'est JAMAIS annoncé publiquement — notification privée uniquement via CoupleManager
    @Deprecated
    public void announceCouple(String name1, String name2, boolean isTrouple, String name3) {
        // Intentionnellement vide — le couple reste secret
    }

    // ── FIN DE PARTIE ─────────────────────────────────────────────────────────

    public void announceGameEnd(String winnerMsg) {
        broadcast("");
        broadcast("§6§l╔══════════════════════════════════╗");
        broadcast("§6§l║          §e§l⚑ FIN DE PARTIE ⚑         §6§l║");
        broadcast("§6§l╠══════════════════════════════════╣");
        broadcast("§6§l║   " + padCenter(winnerMsg, 34) + "   §6§l║");
        broadcast("§6§l╚══════════════════════════════════╝");
        broadcast("");
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle(winnerMsg, "§7Partie terminée !", 10, 80, 20);
        }
    }

    // ── LOUP ENDORMI ─────────────────────────────────────────────────────────

    public void notifyWolfListReveal(Player player, java.util.List<String> wolfNames) {
        player.sendMessage("");
        player.sendMessage("§4§l[Loup Endormi] §r§71 heure écoulée — tes alliés :");
        wolfNames.forEach(n -> player.sendMessage("  §c• §e" + n));
        player.sendMessage("");
    }

    // ── HELPER ────────────────────────────────────────────────────────────────

    private void broadcast(String msg) { Bukkit.broadcastMessage(msg); }

    private String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }

    private String padCenter(String s, int width) {
        if (s.length() >= width) return s;
        int pad = (width - s.length()) / 2;
        return " ".repeat(pad) + s + " ".repeat(pad);
    }
}
