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
        // Bloquer tout enchantement supplémentaire sur le Marteau du CDC
        if (item.getType() == org.bukkit.Material.WOODEN_AXE) {
            var meta = item.getItemMeta();
            if (meta != null && "§6Marteau".equals(meta.getDisplayName())) return; // ne toucher à rien
        }
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

        // ── ÉPÉES : autorisés ────────────────────────────────────────────
        if (ench.equals(Enchantment.SHARPNESS))         return plugin.getConfig().getInt("game.enchants.sharpness", 3);
        if (ench.equals(Enchantment.UNBREAKING))        return -1; // Solidité : autorisé

        // ── ÉPÉES : interdits ────────────────────────────────────────────
        if (ench.equals(Enchantment.FIRE_ASPECT))       return 0; // Feu : interdit
        if (ench.equals(Enchantment.BANE_OF_ARTHROPODS)) return 0; // Fléau des arthropodes : interdit
        if (ench.equals(Enchantment.KNOCKBACK))         return 0; // Recul : interdit
        if (ench.equals(Enchantment.SMITE))             return 0; // Châtiment : interdit (bonus contre morts-vivants = déséquilibre)
        if (ench.equals(Enchantment.SWEEPING_EDGE))     return 0; // Tranchant : interdit

        // ── ARMURES : autorisés ──────────────────────────────────────────
        if (ench.equals(Enchantment.PROTECTION))        return plugin.getConfig().getInt("game.enchants.protection", 3);
        if (ench.equals(Enchantment.MENDING))           return plugin.getConfig().getBoolean("game.enchants.mending", false) ? 1 : 0;

        // ── ARMURES : interdits ──────────────────────────────────────────
        if (ench.equals(Enchantment.FIRE_PROTECTION))   return 0; // Protection Feu : interdite
        if (ench.equals(Enchantment.PROJECTILE_PROTECTION)) return 0; // Protection Projectiles : interdite
        if (ench.equals(Enchantment.BLAST_PROTECTION))  return 0; // Protection Explosion (cristal) : interdite
        if (ench.equals(Enchantment.THORNS))            return 0; // Épines : interdites

        // ── ARC : autorisés ──────────────────────────────────────────────
        if (ench.equals(Enchantment.POWER))             return plugin.getConfig().getInt("game.enchants.power", 3);
        if (ench.equals(Enchantment.PUNCH))             return plugin.getConfig().getInt("game.enchants.punch", 0); // Interdit par défaut
        if (ench.equals(Enchantment.INFINITY))          return 0; // Infinité : interdite (trop fort en UHC)
        if (ench.equals(Enchantment.FLAME))             return 0; // Flèches enflammées : interdites

        // ── OUTILS — appliqués par CraftListener ─────────────────────────
        if (ench.equals(Enchantment.EFFICIENCY))        return -1; // Autorisé
        if (ench.equals(Enchantment.FORTUNE))           return plugin.getConfig().getInt("game.enchants.fortune", 0); // Bloqué par défaut (diamants limités)
        if (ench.equals(Enchantment.SILK_TOUCH))        return -1; // Autorisé
        if (ench.equals(Enchantment.LOOTING))           return -1; // Butin sur épée : autorisé

        // Tous les autres : niveau max vanilla sans restriction
        return ench.getMaxLevel();
    }
}
