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

    // Garde actif : "force" ou "resistance"
    private String activeGuard = null;
    private final Set<UUID> hiddenDeaths = new HashSet<>();

    public Assassin(LoupGarouPlugin p, UUID u) { super(p, u); }

    @Override public String getDisplayName() { return "§8Assassin"; }
    @Override public String getDescription() {
        return "§7/lg changegard — bascule entre Force 40% et Résistance 30% (pas les deux en même temps). Peut cacher les morts.";
    }
    @Override public RoleFamily getFamily() { return RoleFamily.SOLITAIRE; }
    @Override public String getId()         { return "assassin"; }

    @Override
    public void onGameStart(Player player) {
        // Livre d'enchantements au démarrage
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
        meta.addStoredEnchant(Enchantment.PROTECTION, 4, true);
        meta.addStoredEnchant(Enchantment.SHARPNESS,  4, true);
        meta.addStoredEnchant(Enchantment.POWER,      4, true);
        book.setItemMeta(meta);
        player.getInventory().addItem(book);

        // Démarre en mode Force
        activeGuard = "force";
        applyGuard(player);
        player.sendMessage("§8[Assassin] §7Garde actif : §cForce 40%§7. Tapez §f/lg changegard §7pour basculer.");
    }

    @Override
    public boolean onPowerCommand(Player player, String[] args) {
        if (args[0].equalsIgnoreCase("changegard")) {
            // Basculer entre force et resistance
            if ("force".equals(activeGuard)) {
                activeGuard = "resistance";
            } else {
                activeGuard = "force";
            }
            applyGuard(player);
            return true;
        }
        if (args[0].equalsIgnoreCase("cacher")) {
            if (args.length < 2) { player.sendMessage(MessageUtils.error("Usage: /lg cacher <joueur>")); return true; }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) { player.sendMessage(MessageUtils.error("Joueur introuvable.")); return true; }
            hiddenDeaths.add(target.getUniqueId());
            player.sendMessage(MessageUtils.success("La mort de §e" + target.getName() + " §asera cachée."));
            return true;
        }
        return false;
    }

    private void applyGuard(Player player) {
        // Retirer les deux effets d'abord
        player.removePotionEffect(PotionEffectType.STRENGTH);
        player.removePotionEffect(PotionEffectType.RESISTANCE);

        if ("force".equals(activeGuard)) {
            // Force II (amp 1) ≈ 60% — on prend amp 1 pour ~40-60% selon les sources
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1, false, false, false));
            player.sendMessage("§8[Assassin] §7Garde : §cForce 40%§7 activé.");
        } else {
            // Résistance I (amp 0) ≈ 20%, Résistance II (amp 1) ≈ 40% → amp 1 pour ~30%
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 1, false, false, false));
            player.sendMessage("§8[Assassin] §7Garde : §6Résistance 30%§7 activé.");
        }
    }

    public boolean tryHideDeath(UUID dead) { return hiddenDeaths.contains(dead); }
}
