package fr.enoe.loupgarou.listeners;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.core.GameState;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Set;

/**
 * CraftListener — Nouveau fichier (v3)
 *
 * Quand un joueur craft une hache, pioche, pelle, cisaille ou houe,
 * l'outil reçoit automatiquement Efficacité 3 + Solidité 2.
 *
 * Emplacement : src/main/java/fr/enoe/loupgarou/listeners/CraftListener.java
 */
public class CraftListener implements Listener {

    private final LoupGarouPlugin plugin;

    // Tous les outils concernés (toutes variantes de matériaux)
    private static final Set<Material> TOOLS = Set.of(
        // Haches
        Material.WOODEN_AXE,   Material.STONE_AXE,
        Material.IRON_AXE,     Material.GOLDEN_AXE,    Material.DIAMOND_AXE,
        Material.NETHERITE_AXE,
        // Pioches
        Material.WOODEN_PICKAXE, Material.STONE_PICKAXE,
        Material.IRON_PICKAXE,   Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE,
        Material.NETHERITE_PICKAXE,
        // Pelles
        Material.WOODEN_SHOVEL,  Material.STONE_SHOVEL,
        Material.IRON_SHOVEL,    Material.GOLDEN_SHOVEL,  Material.DIAMOND_SHOVEL,
        Material.NETHERITE_SHOVEL,
        // Cisailles
        Material.SHEARS,
        // Houes
        Material.WOODEN_HOE,   Material.STONE_HOE,
        Material.IRON_HOE,     Material.GOLDEN_HOE,     Material.DIAMOND_HOE,
        Material.NETHERITE_HOE
    );

    public CraftListener(LoupGarouPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        // Seulement en partie active
        if (plugin.getGameManager().getState() != GameState.RUNNING) return;

        ItemStack result = event.getRecipe().getResult().clone();
        if (!TOOLS.contains(result.getType())) return;

        // Appliquer Efficacité 3 + Solidité 2
        ItemMeta meta = result.getItemMeta();
        if (meta == null) return;

        // true = bypasse le niveau max vanilla (utile pour Cisailles qui ont max I)
        meta.addEnchant(Enchantment.EFFICIENCY, 3, true);
        meta.addEnchant(Enchantment.UNBREAKING,  2, true);
        result.setItemMeta(meta);

        // Remplacer le résultat par la version enchantée
        event.setCurrentItem(result);
    }
}
