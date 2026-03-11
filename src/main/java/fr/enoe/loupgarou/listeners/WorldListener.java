package fr.enoe.loupgarou.listeners;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.core.GameState;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerPortalEvent;

public class WorldListener implements Listener {

    private final LoupGarouPlugin plugin;

    public WorldListener(LoupGarouPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        if (plugin.getGameManager().getState() != GameState.RUNNING) return;
        if (event.getTo() == null) return;
        World.Environment env = event.getTo().getWorld().getEnvironment();
        if (env == World.Environment.NETHER || env == World.Environment.THE_END) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(MessageUtils.error("Le Nether et l'End sont désactivés !"));
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (plugin.getGameManager().getState() != GameState.RUNNING) return;
        // Bloquer les PNJ villageois
        if (event.getEntityType() == EntityType.VILLAGER) {
            event.setCancelled(true);
        }
    }
}
