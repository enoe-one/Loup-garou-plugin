package fr.enoe.loupgarou.roles.impl.village;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.UUID;

public class Maire extends Role {
    private double currentSpeed = 0.1;
    public Maire(LoupGarouPlugin plugin, UUID uuid) { super(plugin, uuid); }
    @Override public String getDisplayName() { return "§6Maire"; }
    @Override public String getDescription()  { return "§7Vote décisif en égalité, compte double. Speed +0.1/épisode."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.VILLAGE; }
    @Override public String getId()           { return "maire"; }
    @Override public void onGameStart(Player player) { applySpeed(player, currentSpeed); }
    @Override public void onEpisodeEnd(int ep) {
        super.onEpisodeEnd(ep);
        Player p = org.bukkit.Bukkit.getPlayer(playerUUID);
        if (p == null) return;
        currentSpeed = Math.min(0.5, currentSpeed + 0.1);
        applySpeed(p, currentSpeed);
    }
    private void applySpeed(Player p, double s) {
        int amp = (int) Math.round(s / 0.1) - 1;
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, Math.max(0,amp), false, false, true));
    }
}
