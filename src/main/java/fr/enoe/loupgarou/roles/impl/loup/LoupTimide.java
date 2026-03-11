package fr.enoe.loupgarou.roles.impl.loup;
import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.UUID;
public class LoupTimide extends Role {
    public LoupTimide(LoupGarouPlugin p, UUID u) { super(p, u); }
    @Override public String getDisplayName() { return "§cLoup Timide"; }
    @Override public String getDescription()  { return "§7Résistance+Speed sans loup proche. Faiblesse si loup <20 blocs."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.LOUP; }
    @Override public String getId()           { return "loup_timide"; }
    @Override public void onNightTick(Player player) {
        boolean near = player.getNearbyEntities(20,20,20).stream()
                .filter(e -> e instanceof Player)
                .anyMatch(e -> plugin.getRoleManager().isWolf(((Player)e).getUniqueId()));
        if (near) {
            player.removePotionEffect(PotionEffectType.RESISTANCE);
            player.removePotionEffect(PotionEffectType.SPEED);
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0, false, false, false));
        } else {
            player.removePotionEffect(PotionEffectType.WEAKNESS);
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 0, false, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0, false, false, false));
        }
    }
}
