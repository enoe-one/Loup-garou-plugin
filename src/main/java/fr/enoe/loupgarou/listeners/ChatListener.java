package fr.enoe.loupgarou.listeners;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.core.GameState;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final LoupGarouPlugin plugin;

    public ChatListener(LoupGarouPlugin plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // Chat global désactivé hors admins
        if (!plugin.getChatManager().isGlobalChatEnabled()) {
            if (plugin.getGameManager().isAdmin(player.getUniqueId())) return; // admins passent

            // Partie en cours : vérifier si c'est un loup pendant la fenêtre de chat
            if (plugin.getGameManager().getState() == GameState.RUNNING
                    && plugin.getChatManager().isWolfChatOpen()
                    && plugin.getRoleManager().isWolf(player.getUniqueId())) {

                // Intercepter et rediriger vers le canal loup (pseudo caché)
                event.setCancelled(true);
                plugin.getChatManager().sendWolfMessage(player, event.getMessage());
                return;
            }

            // Sinon : chat bloqué
            event.setCancelled(true);
            player.sendMessage(MessageUtils.error("Le chat est désactivé pendant la partie."));
        }
    }
}
