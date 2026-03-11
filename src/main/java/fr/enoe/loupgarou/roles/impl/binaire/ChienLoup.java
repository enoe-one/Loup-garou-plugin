package fr.enoe.loupgarou.roles.impl.binaire;
import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.UUID;
public class ChienLoup extends Role {
    private boolean choseLoup=false, chose=false;
    public ChienLoup(LoupGarouPlugin p, UUID u) { super(p, u); }
    @Override public String getDisplayName() { return chose?(choseLoup?"§cChien-Loup (Loup)":"§aChien-Loup (Village)"):"§7Chien-Loup"; }
    @Override public String getDescription()  { return "§7/lg loup ou /lg village au premier tour."; }
    @Override public RoleFamily getFamily()   { return !chose ? RoleFamily.BINAIRE : choseLoup ? RoleFamily.LOUP : RoleFamily.VILLAGE; }
    @Override public String getId()           { return "chien_loup"; }
    @Override public boolean isBinary()       { return true; }
    @Override public boolean onPowerCommand(Player player, String[] args) {
        if (chose) { player.sendMessage(MessageUtils.error("Tu as déjà choisi !")); return true; }
        if (args[0].equalsIgnoreCase("loup")) {
            chose=true; choseLoup=true;
            plugin.getRoleManager().addToWolfTeam(playerUUID);
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,Integer.MAX_VALUE,0,false,false,true));
            player.sendMessage(MessageUtils.success("Tu rejoins les Loups-Garous !"));
            return true;
        }
        if (args[0].equalsIgnoreCase("village")) {
            chose=true; player.sendMessage(MessageUtils.success("Tu restes au Village."));
            return true;
        }
        return false;
    }
}
