package fr.enoe.loupgarou.roles.impl.solitaire;
import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;
public class Assassin extends Role {
    private final Set<UUID> hiddenDeaths = new HashSet<>();
    public Assassin(LoupGarouPlugin p, UUID u) { super(p, u); }
    @Override public String getDisplayName() { return "§8Assassin"; }
    @Override public String getDescription()  { return "§7Peut cacher les morts. Force+Résistance permanents."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.SOLITAIRE; }
    @Override public String getId()           { return "assassin"; }
    @Override public void onGameStart(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,   Integer.MAX_VALUE, 0, false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 0, false, false, true));
        player.giveExp(10);
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
        meta.addStoredEnchant(Enchantment.PROTECTION, 4, true);
        meta.addStoredEnchant(Enchantment.SHARPNESS,  4, true);
        meta.addStoredEnchant(Enchantment.POWER,      4, true);
        book.setItemMeta(meta);
        player.getInventory().addItem(book);
    }
    public boolean tryHideDeath(UUID dead) { return hiddenDeaths.contains(dead); }
    @Override public boolean onPowerCommand(Player player, String[] args) {
        if (!args[0].equalsIgnoreCase("cacher")) return false;
        if (args.length < 2) { player.sendMessage(MessageUtils.error("Usage: /lg cacher <joueur>")); return true; }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) { player.sendMessage(MessageUtils.error("Joueur introuvable.")); return true; }
        hiddenDeaths.add(target.getUniqueId());
        player.sendMessage(MessageUtils.success("La mort de §e" + target.getName() + " §asera cachée."));
        return true;
    }
}
