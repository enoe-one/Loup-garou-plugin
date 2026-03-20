package fr.enoe.loupgarou.listeners;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.core.GameState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

public class EnchantListener implements Listener {

    private final LoupGarouPlugin plugin;

    public EnchantListener(LoupGarouPlugin plugin) { this.plugin = plugin; }

    /**
     * Détecte quand un joueur clique dans une table d'enchantement pour prendre
     * l'item enchanté — on vérifie l'item résultant après 1 tick.
     */
    @EventHandler
    public void onEnchantClick(InventoryClickEvent event) {
        if (plugin.getGameManager().getState() != GameState.RUNNING) return;
        if (event.getInventory().getType() != InventoryType.ENCHANTING) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        // Slot 0 = l'item à enchanter, slot 1 = le lapis. On surveille le slot 0.
        if (event.getSlot() != 0) return;
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override public void run() {
                plugin.getEnchantManager().checkAndDowngrade(item);
            }
        }.runTaskLater(plugin, 1L);
    }

    /** Détecte les combinaisons à l'enclume */
    @EventHandler
    public void onAnvil(PrepareAnvilEvent event) {
        if (plugin.getGameManager().getState() != GameState.RUNNING) return;
        ItemStack result = event.getResult();
        if (result != null) plugin.getEnchantManager().checkAndDowngrade(result);
    }
}
