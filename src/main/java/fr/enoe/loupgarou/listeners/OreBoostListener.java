package fr.enoe.loupgarou.listeners;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.core.GameState;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class OreBoostListener implements Listener {

    private final LoupGarouPlugin plugin;
    private static final Random RNG = new Random();

    // Boost ×1.8 : 80% de chance de drop supplémentaire (hors diamant)
    private static final double BOOST_CHANCE = 0.80;

    public OreBoostListener(LoupGarouPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onOreBreak(BlockBreakEvent event) {
        if (plugin.getGameManager().getState() != GameState.RUNNING) return;

        Block block   = event.getBlock();
        Player player = event.getPlayer();
        Material type = block.getType();

        // ─────────────────────────────────────────────────────────────────
        // CAS SPÉCIAL : Diamant → TOUJOURS exactement 1 seul diamant,
        // peu importe Fortune ou boost. On prend le contrôle complet du drop.
        // ─────────────────────────────────────────────────────────────────
        if (type == Material.DIAMOND_ORE || type == Material.DEEPSLATE_DIAMOND_ORE) {
            // Annuler tous les drops vanilla (y compris Fortune)
            event.setDropItems(false);
            event.setExpToDrop(0);

            // Dropper exactement 1 diamant
            Location loc = block.getLocation().add(0.5, 0.5, 0.5);
            block.getWorld().dropItemNaturally(loc, new ItemStack(Material.DIAMOND, 1));

            // Comptabiliser pour la limite des 25 diamants
            plugin.getGameManager().onDiamondMined(player);
            return; // Ne pas appliquer le boost ×1.8
        }

        // ─────────────────────────────────────────────────────────────────
        // Tous les autres minerais : boost ×1.8 (80% de drop bonus)
        // ─────────────────────────────────────────────────────────────────
        if (!plugin.getConfig().getBoolean("game.ore-boost", true)) return;

        ItemStack bonus = getBonusDrop(type);
        if (bonus == null) return;

        if (RNG.nextDouble() < BOOST_CHANCE) {
            // Délai d'1 tick pour laisser le drop vanilla se faire d'abord
            Location loc = block.getLocation().add(0.5, 0.5, 0.5);
            new org.bukkit.scheduler.BukkitRunnable() {
                @Override public void run() {
                    block.getWorld().dropItemNaturally(loc, bonus);
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    private ItemStack getBonusDrop(Material type) {
        return switch (type) {
            case IRON_ORE,      DEEPSLATE_IRON_ORE                      -> new ItemStack(Material.RAW_IRON, 1);
            case GOLD_ORE,      DEEPSLATE_GOLD_ORE, NETHER_GOLD_ORE    -> new ItemStack(Material.RAW_GOLD, 1);
            case COPPER_ORE,    DEEPSLATE_COPPER_ORE                    -> new ItemStack(Material.RAW_COPPER, 1);
            case COAL_ORE,      DEEPSLATE_COAL_ORE                      -> new ItemStack(Material.COAL, 1);
            case LAPIS_ORE,     DEEPSLATE_LAPIS_ORE                     -> new ItemStack(Material.LAPIS_LAZULI, 2);
            case REDSTONE_ORE,  DEEPSLATE_REDSTONE_ORE                  -> new ItemStack(Material.REDSTONE, 2);
            case EMERALD_ORE,   DEEPSLATE_EMERALD_ORE                   -> new ItemStack(Material.EMERALD, 1);
            case ANCIENT_DEBRIS                                          -> new ItemStack(Material.ANCIENT_DEBRIS, 1);
            // DIAMOND_ORE est géré séparément — on ne doit pas arriver ici
            default -> null;
        };
    }
}
