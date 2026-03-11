package fr.enoe.loupgarou.roles.impl.loup;
import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.UUID;
public class GrandMechantLoup extends Role {
    private long lastRage = -9999;
    public GrandMechantLoup(LoupGarouPlugin p, UUID u) { super(p, u); }
    @Override public String getDisplayName() { return "§4Grand Méchant Loup"; }
    @Override public String getDescription()  { return "§7Vote double. /lg rage : Speed+Résistance 10 min (recharge 30 min)."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.LOUP; }
    @Override public String getId()           { return "grand_mechant_loup"; }
    @Override public boolean onPowerCommand(Player player, String[] args) {
        if (!args[0].equalsIgnoreCase("rage")) return false;
        long now = plugin.getGameManager().getElapsedSeconds();
        if (now - lastRage < 1800) { player.sendMessage(MessageUtils.error("Rage en recharge !")); return true; }
        lastRage = now;
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12000, 0, false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 12000, 0, false, false, true));
        player.sendMessage(MessageUtils.success("RAGE activée !"));
        return true;
    }
}
