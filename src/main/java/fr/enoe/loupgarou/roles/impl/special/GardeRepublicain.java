package fr.enoe.loupgarou.roles.impl.special;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

/**
 * Garde Républicain — transformation révolutionnaire du Citoyen.
 * Speed I permanent + Livre Tranchant IV ajouté à l'inventaire.
 */
public class GardeRepublicain extends Role {

    public GardeRepublicain(LoupGarouPlugin plugin, UUID playerUUID) {
        super(plugin, playerUUID);
    }

    @Override public String getDisplayName() { return "§aGarde Républicain"; }
    @Override public String getDescription()  { return "Défenseur de la République — Speed I permanent et lame affûtée."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.VILLAGE; }
    @Override public String getId()           { return "garde_republicain"; }

    /** Applique les effets de transformation (appelé par SkysofrenieManager). */
    public void applyTransformation(Player player) {
        // Speed I permanent
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));

        // Livre Tranchant IV
        ItemStack sword = new ItemStack(Material.BOOK);
        ItemMeta meta = sword.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aSabre Républicain");
            meta.addEnchant(Enchantment.SHARPNESS, 4, true); // Tranchant IV
            sword.setItemMeta(meta);
        }
        player.getInventory().addItem(sword);

        player.sendMessage("");
        player.sendMessage("§a§m                                          ");
        player.sendMessage("§a§l      ✦ GARDE RÉPUBLICAIN ✦");
        player.sendMessage("§a§m                                          ");
        player.sendMessage("");
        player.sendMessage("§fLa République t'appelle — tu réponds présent.");
        player.sendMessage("§fSous les pavés, la gloire. Dans tes mains, la lame.");
        player.sendMessage("§7Speed I permanent + Sabre Tranchant IV dans l'inventaire.");
        player.sendMessage("");
    }
}
