package fr.enoe.loupgarou.listeners;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.core.GameState;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class MiningListener implements Listener {

    private final LoupGarouPlugin plugin;

    public MiningListener(LoupGarouPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (plugin.getGameManager().getState() != GameState.RUNNING) return;
        Material type = event.getBlock().getType();
        if (type == Material.DIAMOND_ORE || type == Material.DEEPSLATE_DIAMOND_ORE) {
            plugin.getGameManager().onDiamondMined(event.getPlayer());
        }
    }
}
