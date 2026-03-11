package fr.enoe.loupgarou.managers;

import fr.enoe.loupgarou.LoupGarouPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ChatManager {

    private final LoupGarouPlugin plugin;
    private boolean globalChatEnabled = true;

    public ChatManager(LoupGarouPlugin plugin) { this.plugin = plugin; }

    public void setGlobalChatEnabled(boolean enabled) { this.globalChatEnabled = enabled; }
    public boolean isGlobalChatEnabled()              { return globalChatEnabled; }

    /** Envoie un message uniquement aux loups (+ petite fille en espion) */
    public void sendToWolves(String message) {
        plugin.getRoleManager().getWolfList().forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.sendMessage(message);
        });
        // Petite fille voit aussi le chat des loups
        plugin.getGameManager().getAlivePlayers().forEach(uuid -> {
            var role = plugin.getRoleManager().getRole(uuid);
            if (role != null && role.getId().equals("petite_fille")) {
                Player pf = Bukkit.getPlayer(uuid);
                if (pf != null) pf.sendMessage("§8[Espion] " + message);
            }
        });
    }
}
