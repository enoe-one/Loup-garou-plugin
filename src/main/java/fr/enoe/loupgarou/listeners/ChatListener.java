package fr.enoe.loupgarou.listeners;

import fr.enoe.loupgarou.LoupGarouPlugin;
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
        if (!plugin.getChatManager().isGlobalChatEnabled()) {
            if (!plugin.getGameManager().isAdmin(player.getUniqueId())) {
                event.setCancelled(true);
                player.sendMessage(MessageUtils.error("Le chat est désactivé pendant la partie."));
            }
        }
    }
}
