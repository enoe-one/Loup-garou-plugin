package fr.enoe.loupgarou.roles.impl.village;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.UUID;

public class Soeur extends Role {
    private UUID sisterUUID = null;
    public Soeur(LoupGarouPlugin plugin, UUID uuid) { super(plugin, uuid); }
    @Override public String getDisplayName() { return "§bSœur"; }
    @Override public String getDescription()  { return "§7Vous vous connaissez mutuellement. Résistance 1 si proches (<50 blocs)."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.VILLAGE; }
    @Override public String getId()           { return "soeur"; }

    public void setSister(UUID sisterUUID) { this.sisterUUID = sisterUUID; }
    public UUID getSisterUUID() { return sisterUUID; }

    @Override
    public void onNightTick(Player player) {
        if (sisterUUID == null) return;
        Player sister = Bukkit.getPlayer(sisterUUID);
        if (sister == null) return;
        if (player.getLocation().distance(sister.getLocation()) <= 50) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 0, false, false, false));
        } else {
            player.removePotionEffect(PotionEffectType.RESISTANCE);
        }
    }
}
