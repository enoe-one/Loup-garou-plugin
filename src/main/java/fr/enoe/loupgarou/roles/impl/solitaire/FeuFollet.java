package fr.enoe.loupgarou.roles.impl.solitaire;
import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.UUID;
public class FeuFollet extends Role {
    private long lastIncendie=-9999, lastPlume=-9999;
    public FeuFollet(LoupGarouPlugin p, UUID u) { super(p, u); }
    @Override public String getDisplayName() { return "§6Feu Follet"; }
    @Override public String getDescription()  { return "§7/lg incendie (20min). /lg plume (8min, TP 25 blocs)."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.SOLITAIRE; }
    @Override public String getId()           { return "feu_follet"; }
    @Override public boolean onPowerCommand(Player player, String[] args) {
        long now = plugin.getGameManager().getElapsedSeconds();
        if (args[0].equalsIgnoreCase("incendie")) {
            if (now - lastIncendie < 1200) { player.sendMessage(MessageUtils.error("Recharge en cours.")); return true; }
            lastIncendie = now;
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 60, 0, false, false, false));
            player.sendMessage(MessageUtils.success("Folie incendiaire activée !"));
            return true;
        }
        if (args[0].equalsIgnoreCase("plume")) {
            if (now - lastPlume < 480) { player.sendMessage(MessageUtils.error("Recharge en cours.")); return true; }
            lastPlume = now;
            org.bukkit.util.Vector dir = player.getLocation().getDirection().normalize().multiply(25);
            Location dest = player.getLocation().add(dir);
            dest.setY(dest.getWorld().getHighestBlockYAt(dest.getBlockX(), dest.getBlockZ()) + 1);
            player.teleport(dest);
            player.sendMessage(MessageUtils.success("Plume activée !"));
            return true;
        }
        return false;
    }
}
