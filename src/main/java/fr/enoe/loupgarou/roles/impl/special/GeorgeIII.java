package fr.enoe.loupgarou.roles.impl.special;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.UUID;

/**
 * George III — transformation révolutionnaire du Grand Méchant Loup.
 * Couronne qui lui octroie /lg hurler + /lg charge (Speed I à tous les loups alentour).
 */
public class GeorgeIII extends Role {

    // Nombre de fois que George III peut encore hurler (même quota que GrandMechantLoup)
    private int howlsRemaining = 3;

    public GeorgeIII(LoupGarouPlugin plugin, UUID playerUUID) {
        super(plugin, playerUUID);
    }

    @Override public String getDisplayName() { return "§4George III"; }
    @Override public String getDescription()  { return "Le roi d'Angleterre — hurle ses ordres et charge ses troupes."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.LOUP; }
    @Override public String getId()           { return "george_iii"; }

    public void applyTransformation(Player player) {
        // Donner la couronne (casque en or avec enchantements décoratifs)
        ItemStack crown = new ItemStack(Material.GOLDEN_HELMET);
        ItemMeta meta = crown.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§4§lCouronne de George III");
            meta.setLore(List.of(
                "§7Symbole du pouvoir royal.",
                "§7Commandes : §e/lg hurler §7et §e/lg charge"
            ));
            crown.setItemMeta(meta);
        }
        player.getInventory().setHelmet(crown);

        player.sendMessage("");
        player.sendMessage("§4§m                                          ");
        player.sendMessage("§4§l      ✦ GEORGE III ✦");
        player.sendMessage("§4§m                                          ");
        player.sendMessage("");
        player.sendMessage("§fTu portes la couronne d'un empire qui tremble.");
        player.sendMessage("§fL'Angleterre tient — mais Paris brûle.");
        player.sendMessage("§7La couronne royale est sur ta tête.");
        player.sendMessage("§7Tu disposes de §e" + howlsRemaining + " §7hurlements royaux.");
        player.sendMessage("§7Commandes : §e/lg hurler §7| §e/lg charge §7(Speed I aux loups proches, 30s)");
        player.sendMessage("");
    }

    /** /lg hurler — révèle position de Napoléon à tous les loups */
    public boolean useHurl(Player player) {
        if (howlsRemaining <= 0) {
            player.sendMessage(MessageUtils.error("Tu n'as plus de hurlements disponibles !"));
            return false;
        }
        // Trouver Napoléon et révéler sa position aux loups
        for (UUID wolfUUID : plugin.getRoleManager().getWolfList()) {
            Player wolf = Bukkit.getPlayer(wolfUUID);
            if (wolf == null) continue;
            // Chercher Napoléon parmi les joueurs vivants
            plugin.getGameManager().getAlivePlayers().forEach(uuid -> {
                Role r = plugin.getRoleManager().getRole(uuid);
                if (r != null && r.getId().equals("napoleon")) {
                    Player napoleon = Bukkit.getPlayer(uuid);
                    if (napoleon != null) {
                        wolf.sendMessage("§4[George III - Hurlement] §7Napoléon est en §e"
                            + napoleon.getLocation().getBlockX() + ", "
                            + napoleon.getLocation().getBlockY() + ", "
                            + napoleon.getLocation().getBlockZ());
                    }
                }
            });
        }
        howlsRemaining--;
        player.sendMessage("§4[Hurlement] §7Position révélée aux loups. Hurlements restants : §e" + howlsRemaining);
        return true;
    }

    /** /lg charge — Speed I à tous les loups dans un rayon de 50 blocs */
    public boolean useCharge(Player player) {
        int radius = 50;
        int count = 0;
        for (UUID wolfUUID : plugin.getRoleManager().getWolfList()) {
            Player wolf = Bukkit.getPlayer(wolfUUID);
            if (wolf == null) continue;
            if (!wolf.getWorld().equals(player.getWorld())) continue;
            if (wolf.getLocation().distance(player.getLocation()) <= radius) {
                wolf.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 30, 0, false, false)); // 30s
                wolf.sendMessage("§4[George III] §7La charge est lancée ! Speed I pour 30 secondes !");
                count++;
            }
        }
        player.sendMessage("§4[Charge] §7" + count + " loup(s) boosté(s) dans un rayon de " + radius + " blocs.");
        return true;
    }

    @Override
    public boolean onPowerCommand(Player player, String[] args) {
        return switch (args[0].toLowerCase()) {
            case "hurler" -> useHurl(player);
            case "charge" -> useCharge(player);
            default -> false;
        };
    }

    public int getHowlsRemaining() { return howlsRemaining; }
}
