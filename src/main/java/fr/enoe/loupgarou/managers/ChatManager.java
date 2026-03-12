package fr.enoe.loupgarou.managers;

import fr.enoe.loupgarou.LoupGarouPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class ChatManager {

    private final LoupGarouPlugin plugin;
    private boolean globalChatEnabled = true;

    // Chat loups : une seule fenêtre par nuit, 1 minute
    private boolean wolfChatOpen       = false;  // fenêtre actuellement ouverte
    private boolean wolfChatUsedTonight = false; // déjà utilisé cette nuit
    private BukkitTask wolfChatTimer   = null;

    // Numéro de nuit (pour détecter le changement jour/nuit)
    private boolean wasNight = false;

    public ChatManager(LoupGarouPlugin plugin) {
        this.plugin = plugin;
        startNightWatcher();
    }

    public void setGlobalChatEnabled(boolean enabled) { this.globalChatEnabled = enabled; }
    public boolean isGlobalChatEnabled()              { return globalChatEnabled; }
    public boolean isWolfChatOpen()                   { return wolfChatOpen; }

    // ── Surveille la transition jour→nuit pour réinitialiser le chat ─────────
    private void startNightWatcher() {
        new BukkitRunnable() {
            @Override public void run() {
                if (plugin.getGameManager() == null) return;
                if (plugin.getGameManager().getState() != fr.enoe.loupgarou.core.GameState.RUNNING) return;

                World w = Bukkit.getWorlds().get(0);
                long time = w.getTime();
                boolean isNight = time >= 13000 && time <= 23000;

                // Transition jour→nuit : réinitialiser
                if (isNight && !wasNight) {
                    wolfChatUsedTonight = false;
                    wasNight = true;
                }
                if (!isNight && wasNight) {
                    // Fermer la fenêtre si encore ouverte au matin
                    if (wolfChatOpen) closeWolfChat();
                    wasNight = false;
                }
            }
        }.runTaskTimer(plugin, 0L, 40L); // toutes les 2 secondes
    }

    /**
     * Tente d'ouvrir la fenêtre de chat loups.
     * Déclenché par /lg chat ou automatiquement par NightListener au début de nuit.
     * Une seule ouverture par nuit, dure 1 minute.
     */
    public boolean tryOpenWolfChat() {
        if (wolfChatUsedTonight) return false; // déjà utilisé cette nuit
        if (wolfChatOpen) return false;        // déjà ouvert

        wolfChatOpen        = true;
        wolfChatUsedTonight = true;

        // Annoncer aux loups (et petite fille en espion) — pseudos cachés
        broadcastToWolves("§c§l[Chat Loups] §7La communication secrète s'ouvre pour §e1 minute§7. Les pseudos sont cachés.");

        // Timer 1 minute = 1200 ticks
        wolfChatTimer = new BukkitRunnable() {
            @Override public void run() { closeWolfChat(); }
        }.runTaskLater(plugin, 1200L);

        return true;
    }

    private void closeWolfChat() {
        wolfChatOpen = false;
        if (wolfChatTimer != null) { wolfChatTimer.cancel(); wolfChatTimer = null; }
        broadcastToWolves("§c[Chat Loups] §7La fenêtre de communication est fermée.");
    }

    /**
     * Envoie un message dans le canal loup.
     * Appelé depuis ChatListener quand wolfChatOpen == true.
     * Le pseudo est remplacé par "§8[Loup ???]" pour cacher l'identité.
     */
    public void sendWolfMessage(Player sender, String message) {
        if (!wolfChatOpen) return;
        // Pseudo caché
        String display = "§c[Loup §8???§c]§7 " + message;
        sendToWolves(display);
    }

    /** Envoie un message système aux loups + petite fille en espion. */
    public void sendToWolves(String message) {
        plugin.getRoleManager().getWolfList().forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.sendMessage(message);
        });
        // Petite fille voit aussi — avec préfixe espion
        plugin.getGameManager().getAlivePlayers().forEach(uuid -> {
            var role = plugin.getRoleManager().getRole(uuid);
            if (role != null && role.getId().equals("petite_fille")) {
                Player pf = Bukkit.getPlayer(uuid);
                if (pf != null) pf.sendMessage("§8[Espion] " + message);
            }
        });
    }

    private void broadcastToWolves(String message) {
        sendToWolves(message);
    }
}
