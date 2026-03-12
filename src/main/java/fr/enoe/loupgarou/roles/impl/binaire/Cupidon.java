package fr.enoe.loupgarou.roles.impl.binaire;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.UUID;

public class Cupidon extends Role {

    private boolean used   = false;
    private RoleFamily family = RoleFamily.BINAIRE;

    public Cupidon(LoupGarouPlugin p, UUID u) { super(p, u); }

    @Override public String getDisplayName() { return "§dCupidon"; }
    @Override public String getDescription() {
        return "§7/lg lier <j1> <j2> — lie deux joueurs en secret. Le couple sera informé à 25 min sans savoir qui les a liés.";
    }
    @Override public RoleFamily getFamily()  { return family; }
    @Override public String getId()          { return "cupidon"; }
    @Override public boolean isBinary()      { return true; }

    @Override
    public void onGameStart(Player player) {
        // Arc avec Frappe I (PUNCH I) — seul rôle autorisé à avoir cet enchantement
        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta meta = bow.getItemMeta();
        meta.setDisplayName("§d§lFlèche de Cupidon");
        // Frappe I = PUNCH enchantment level 1
        meta.addEnchant(Enchantment.PUNCH, 1, true);
        bow.setItemMeta(meta);
        player.getInventory().addItem(bow);
    }

    @Override
    public boolean onPowerCommand(Player player, String[] args) {
        if (!args[0].equalsIgnoreCase("lier")) return false;

        if (used) {
            player.sendMessage(MessageUtils.error("Tu as déjà lié un couple !"));
            return true;
        }
        if (args.length < 3) {
            player.sendMessage(MessageUtils.error("Usage: /lg lier <joueur1> <joueur2>"));
            return true;
        }

        Player p1 = Bukkit.getPlayer(args[1]);
        Player p2 = Bukkit.getPlayer(args[2]);
        if (p1 == null || p2 == null) {
            player.sendMessage(MessageUtils.error("Joueur(s) introuvable(s)."));
            return true;
        }
        if (p1.getUniqueId().equals(p2.getUniqueId())) {
            player.sendMessage(MessageUtils.error("Tu ne peux pas lier un joueur avec lui-même."));
            return true;
        }

        plugin.getCoupleManager().createCouple(p1.getUniqueId(), p2.getUniqueId());
        used = true;

        // Cupidon sait qui il a lié, mais le couple ne saura pas que c'est lui
        player.sendMessage(MessageUtils.success("§e" + p1.getName() + " §aet §e" + p2.getName()
            + " §asont liés ! Ils seront informés à 25 minutes."));
        return true;
    }

    public void onPartnerDied() {
        family = RoleFamily.VILLAGE;
        Player p = Bukkit.getPlayer(playerUUID);
        if (p != null) p.sendMessage(MessageUtils.info("Ton couple est mort. Tu rejoins le village."));
    }
}
