package fr.enoe.loupgarou.core;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

/**
 * TestManager — mode test pour le debug en solo ou avec peu de joueurs.
 *
 * Fonctionnalités :
 *  - /lg test           : lance la partie même avec 1 seul joueur, bypasse les vérifications.
 *  - /lg add <minutes>  : avance le temps interne de N minutes (événements déclenchés en cascade).
 *
 * Tout est loggé en console avec le préfixe [LG-TEST].
 */
public class TestManager {

    private final LoupGarouPlugin plugin;
    private final Logger log;
    private boolean testMode = false;

    public TestManager(LoupGarouPlugin plugin) {
        this.plugin = plugin;
        this.log = plugin.getLogger();
    }

    public boolean isTestMode() { return testMode; }

    public void stopTest(Player caller) {
        if (!testMode) {
            caller.sendMessage(MessageUtils.error("Le mode test n'est pas actif."));
            return;
        }
        testMode = false;
        log("[LG-TEST] Mode test arrêté par " + caller.getName());
        log("[LG-TEST] La partie se termine normalement.");
        plugin.getGameManager().endGame("§c§l[TEST] Partie test terminée manuellement.");
        caller.sendMessage(MessageUtils.success("Mode test arrêté. Partie terminée."));
    }

    // ─── /lg test ───────────────────────────────────────────────────────────

    /**
     * Lance la partie en mode test :
     * - Bypasse le minimum de 2 joueurs
     * - Assigne un rôle même à 1 joueur
     * - Active le mode test (logs enrichis, /lg add disponible)
     */
    public void startTest(Player caller) {
        GameManager gm = plugin.getGameManager();

        if (gm.getState() == GameState.RUNNING) {
            caller.sendMessage(MessageUtils.error("Une partie est déjà en cours. /lg add pour avancer le temps."));
            return;
        }

        // Créer la room si elle n'existe pas encore
        if (gm.getOwnerUUID() == null) {
            gm.createRoom(caller, "§c[TEST]");
            log("[LG-TEST] Room créée automatiquement par " + caller.getName());
        }

        testMode = true;
        log("[LG-TEST] ════════════════════════════════════════");
        log("[LG-TEST] Mode TEST activé par " + caller.getName());
        log("[LG-TEST] Joueurs en partie : " + gm.getAlivePlayers().size());
        log("[LG-TEST] Bypass minimum joueurs : OUI");
        log("[LG-TEST] Commandes disponibles :");
        log("[LG-TEST]   /lg add <minutes>  — Avancer le temps");
        log("[LG-TEST]   /lg add nuit       — Passer en nuit immédiatement");
        log("[LG-TEST]   /lg add jour       — Passer en jour immédiatement");
        log("[LG-TEST]   /lg add episode    — Forcer fin d'épisode");
        log("[LG-TEST]   /lg add pvp        — Activer PVP maintenant");
        log("[LG-TEST]   /lg add roles      — Révéler les rôles maintenant");
        log("[LG-TEST]   /lg add bordure    — Déclencher la réduction de bordure");
        log("[LG-TEST] ════════════════════════════════════════");

        caller.sendMessage("§c§l[TEST] §eLancement de la partie en mode test...");
        caller.sendMessage("§7Les logs détaillés sont visibles en console.");

        // Forcer le démarrage même avec 1 joueur
        forceStartGame(caller);
    }

    private void forceStartGame(Player caller) {
        GameManager gm = plugin.getGameManager();

        if (gm.getState() != GameState.WAITING) {
            caller.sendMessage(MessageUtils.error("La partie a déjà démarré."));
            return;
        }

        // Injecter au moins ce joueur dans les vivants si la liste est vide
        if (gm.getAlivePlayers().isEmpty()) {
            gm.getAlivePlayers().add(caller.getUniqueId());
            log("[LG-TEST] " + caller.getName() + " ajouté manuellement aux joueurs vivants.");
        }

        // Appel direct à startGame() — qui avait la vérification >= 2 joueurs
        // On bypasse via TestManager.isTestMode()
        gm.startGame();

        log("[LG-TEST] startGame() appelé — état : " + gm.getState());
        logRoleAssignments();
    }

    // ─── /lg add ────────────────────────────────────────────────────────────

    /**
     * Avance le temps interne de N minutes et déclenche tous les événements
     * qui auraient dû se produire entre l'heure actuelle et l'heure cible.
     */
    public void addTime(Player caller, String[] args) {
        if (!testMode) {
            caller.sendMessage(MessageUtils.error("Le mode test n'est pas actif. Fais d'abord /lg test."));
            return;
        }

        GameManager gm = plugin.getGameManager();
        if (gm.getState() != GameState.RUNNING) {
            caller.sendMessage(MessageUtils.error("La partie n'est pas en cours."));
            return;
        }

        if (args.length < 2) {
            caller.sendMessage("§e/lg add <minutes|nuit|jour|episode|pvp|roles|bordure>");
            return;
        }

        String param = args[1].toLowerCase();

        switch (param) {
            case "nuit" -> {
                // Forcer l'heure du monde en nuit
                Bukkit.getWorlds().get(0).setTime(13000);
                logAndBroadcast(caller, "Heure forcée en nuit (13000).");
            }
            case "jour" -> {
                Bukkit.getWorlds().get(0).setTime(1000);
                logAndBroadcast(caller, "Heure forcée en jour (1000).");
            }
            case "episode" -> {
                gm.forceEpisodeEnd();
                logAndBroadcast(caller, "Fin d'épisode forcée. Épisode actuel : " + gm.getEpisodeNumber());
            }
            case "pvp" -> {
                gm.forceElapsedSeconds(1200); // 20 min
                gm.forcePvpEnable();
                logAndBroadcast(caller, "PVP activé de force (temps mis à 20 min).");
            }
            case "roles" -> {
                gm.forceElapsedSeconds(1200);
                gm.forceAnnounceRoles();
                logAndBroadcast(caller, "Rôles révélés de force.");
                logRoleAssignments();
            }
            case "bordure" -> {
                gm.forceBorderShrink();
                logAndBroadcast(caller, "Réduction de bordure déclenchée.");
            }
            default -> {
                // Nombre de minutes
                try {
                    int minutes = Integer.parseInt(param);
                    if (minutes <= 0 || minutes > 120) {
                        caller.sendMessage(MessageUtils.error("Entre 1 et 120 minutes."));
                        return;
                    }
                    addMinutes(caller, gm, minutes);
                } catch (NumberFormatException e) {
                    caller.sendMessage(MessageUtils.error("Paramètre inconnu : " + param));
                }
            }
        }
    }

    private void addMinutes(Player caller, GameManager gm, int minutes) {
        int from = gm.getElapsedSeconds();
        int to   = from + (minutes * 60);

        log("[LG-TEST] Avance du temps : " + formatTime(from) + " → " + formatTime(to));
        caller.sendMessage("§c§l[TEST] §eAvance de §b" + minutes + " min§e (de " + formatTime(from) + " à " + formatTime(to) + ")");

        // Déclencher tous les événements entre from et to
        checkAndFireEvents(caller, gm, from, to);

        // Mettre à jour le temps interne
        gm.forceElapsedSeconds(to);

        log("[LG-TEST] Temps interne mis à jour : " + formatTime(gm.getElapsedSeconds()));
        caller.sendMessage("§a[TEST] Temps actuel : §e" + formatTime(gm.getElapsedSeconds()));
    }

    private void checkAndFireEvents(Player caller, GameManager gm, int from, int to) {
        // PVP + Révélation rôles à 20 min (1200s)
        if (from < 1200 && to >= 1200) {
            gm.forceElapsedSeconds(1200);
            gm.forcePvpEnable();
            gm.forceAnnounceRoles();
            logAndBroadcast(caller, "§b[20 min] §ePVP activé + rôles révélés !");
            logRoleAssignments();
        }

        // Couple notifié à 25 min (1500s)
        if (from < 1500 && to >= 1500) {
            plugin.getCoupleManager().notifyCouplePrivately();
            logAndBroadcast(caller, "§b[25 min] §eCouple notifié en privé.");
        }

        // Loup Endormi s'éveille à 60 min (3600s)
        if (from < 3600 && to >= 3600) {
            for (java.util.UUID uuid : gm.getAlivePlayers()) {
                org.bukkit.entity.Player p = Bukkit.getPlayer(uuid);
                if (p == null) continue;
                if (plugin.getRoleManager().getRole(uuid)
                        instanceof fr.enoe.loupgarou.roles.impl.loup.LoupEndormi) {
                    p.sendMessage("§c§l[LG] Tu t'éveilles... Tu es un Loup-Garou !");
                    logAndBroadcast(caller, "§b[60 min] §eLoup Endormi éveillé : " + p.getName());
                }
            }
        }

        // Bordure à 90 min (5400s)
        if (from < 5400 && to >= 5400) {
            gm.forceBorderShrink();
            logAndBroadcast(caller, "§b[90 min] §eBordure commence à rétrécir !");
        }

        // Épisodes : tous les 20 min
        int episodeFrom = from / 1200;
        int episodeTo   = to   / 1200;
        for (int ep = episodeFrom + 1; ep <= episodeTo; ep++) {
            gm.forceEpisodeEnd();
            log("[LG-TEST] Fin épisode " + gm.getEpisodeNumber() + " déclenchée.");
        }
    }

    // ─── LOGS ────────────────────────────────────────────────────────────────

    private void logRoleAssignments() {
        log("[LG-TEST] ── Rôles assignés ─────────────────────");
        for (java.util.UUID uuid : plugin.getGameManager().getAlivePlayers()) {
            org.bukkit.entity.Player p = Bukkit.getPlayer(uuid);
            var role = plugin.getRoleManager().getRole(uuid);
            String name  = p != null ? p.getName() : uuid.toString().substring(0, 8);
            String rName = role != null ? role.getDisplayName() : "§7(aucun)";
            // Strip couleur pour le log console
            log("[LG-TEST]   " + name + " → " + stripColor(rName));
        }
        log("[LG-TEST] ─────────────────────────────────────────");
    }

    private void logAndBroadcast(Player caller, String msg) {
        log("[LG-TEST] " + stripColor(msg));
        caller.sendMessage("§c§l[TEST] §r" + msg);
        // Broadcast en console aussi pour les admins
        MessageUtils.broadcastToAdmins(plugin, "§c[TEST] §r" + msg);
    }

    private void log(String msg) {
        log.info(msg);
    }

    private String formatTime(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    private String stripColor(String s) {
        return s.replaceAll("§[0-9a-fk-or]", "");
    }
}
