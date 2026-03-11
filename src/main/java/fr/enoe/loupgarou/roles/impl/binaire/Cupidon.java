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
    private boolean used = false;
    private RoleFamily family = RoleFamily.BINAIRE;
    public Cupidon(LoupGarouPlugin p, UUID u) { super(p, u); }
    @Override public String getDisplayName() { return "§dCupidon"; }
    @Override public String getDescription()  { return "§7/lg lier <j1> <j2> — lie deux joueurs."; }
    @Override public RoleFamily getFamily()   { return family; }
    @Override public String getId()           { return "cupidon"; }
    @Override public boolean isBinary()       { return true; }
    @Override public void onGameStart(Player player) {
        ItemStack bow = new ItemStack(Material.BOW);
        bow.addEnchantment(Enchantment.PUNCH, 1);
        ItemMeta m = bow.getItemMeta(); m.setDisplayName("§dFlèche de Cupidon"); bow.setItemMeta(m);
        player.getInventory().addItem(bow);
    }
    @Override public boolean onPowerCommand(Player player, String[] args) {
        if (!args[0].equalsIgnoreCase("lier")) return false;
        if (used) { player.sendMessage(MessageUtils.error("Déjà utilisé !")); return true; }
        if (args.length < 3) { player.sendMessage(MessageUtils.error("Usage: /lg lier <j1> <j2>")); return true; }
        Player p1 = Bukkit.getPlayer(args[1]), p2 = Bukkit.getPlayer(args[2]);
        if (p1==null||p2==null) { player.sendMessage(MessageUtils.error("Joueur(s) introuvable(s).")); return true; }
        plugin.getCoupleManager().createCouple(p1.getUniqueId(), p2.getUniqueId());
        used = true;
        player.sendMessage(MessageUtils.success(p1.getName() + " et " + p2.getName() + " sont liés !"));
        return true;
    }
    public void onPartnerDied() { family = RoleFamily.VILLAGE; Player p = Bukkit.getPlayer(playerUUID); if(p!=null) p.sendMessage(MessageUtils.info("Ton couple est mort. Tu rejoins le village.")); }
}
