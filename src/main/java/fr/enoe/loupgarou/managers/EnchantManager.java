package fr.enoe.loupgarou.managers;

import fr.enoe.loupgarou.LoupGarouPlugin;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class EnchantManager {

    private final LoupGarouPlugin plugin;

    public EnchantManager(LoupGarouPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Vérifie et rétrograde les enchantements interdits sur un item.
     * v3 : Efficacité et Solidité sont toujours autorisés (appliqués par CraftListener).
     */
    public void checkAndDowngrade(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        Map<Enchantment, Integer> enchants = Map.copyOf(item.getEnchantments());
        for (Map.Entry<Enchantment, Integer> e : enchants.entrySet()) {
            int allowed = getAllowedLevel(e.getKey());
            if (allowed < 0) continue; // -1 = toujours autorisé, ne pas toucher
            if (allowed == 0) {
                item.removeEnchantment(e.getKey());
            } else if (e.getValue() > allowed) {
                item.removeEnchantment(e.getKey());
                item.addUnsafeEnchantment(e.getKey(), allowed);
            }
        }
    }

    /**
     * Retourne le niveau max autorisé pour un enchantement.
     *  -1 = toujours autorisé sans limite (ne pas modifier)
     *   0 = interdit (retirer)
     *  >0 = niveau max autorisé
     */
    private int getAllowedLevel(Enchantment ench) {
        // ── Enchantements COMBAT ────────────────────────────────────────
        if (ench.equals(Enchantment.SHARPNESS))    return plugin.getConfig().getInt("game.enchants.sharpness",   3);
        if (ench.equals(Enchantment.PROTECTION))   return plugin.getConfig().getInt("game.enchants.protection",  3);
        if (ench.equals(Enchantment.POWER))        return plugin.getConfig().getInt("game.enchants.power",       3);
        if (ench.equals(Enchantment.KNOCKBACK))    return plugin.getConfig().getInt("game.enchants.knockback",   0); // Interdit
        if (ench.equals(Enchantment.PUNCH))        return plugin.getConfig().getInt("game.enchants.punch",       0); // Interdit
        if (ench.equals(Enchantment.MENDING))      return plugin.getConfig().getBoolean("game.enchants.mending", false) ? 1 : 0; // Interdit par défaut

        // ── Enchantements OUTILS — appliqués par CraftListener ──────────
        // Efficacité 3 et Solidité 2 : toujours autorisés, ne jamais rétrograder
        if (ench.equals(Enchantment.EFFICIENCY))   return -1; // Autorisé sans limite
        if (ench.equals(Enchantment.UNBREAKING))   return -1; // Autorisé sans limite

        // Tous les autres enchantements : niveau max vanilla
        return ench.getMaxLevel();
    }
}
