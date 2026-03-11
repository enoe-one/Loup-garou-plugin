package fr.enoe.loupgarou.listeners;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.core.GameState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private final LoupGarouPlugin plugin;

    public PlayerDeathListener(LoupGarouPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (plugin.getGameManager().getState() != GameState.RUNNING) return;
        if (!plugin.getGameManager().isAlive(player.getUniqueId())) return;

        // Supprimer le message et les drops vanilla —
        // c'est GameManager.handlePlayerDeath() qui gère le stuff (cage 10s)
        event.setKeepInventory(true);
        event.getDrops().clear();
        event.setDeathMessage(null);

        // Déléguer entièrement à GameManager
        plugin.getGameManager().handlePlayerDeath(player);
    }
}
