package fr.enoe.loupgarou.roles.impl.village;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.UUID;

public class Maire extends Role {

    private double currentSpeed = 0.1;

    public Maire(LoupGarouPlugin plugin, UUID uuid) { super(plugin, uuid); }

    @Override public String getDisplayName() { return "§6Maire"; }
    @Override public String getDescription() {
        return "§7Vote décisif en égalité, compte double. Speed +0.1/épisode. +Résistance 10% si 4 villageois proches.";
    }
    @Override public RoleFamily getFamily()  { return RoleFamily.VILLAGE; }
    @Override public String getId()          { return "maire"; }

    @Override
    public void onGameStart(Player player) {
        applySpeed(player, currentSpeed);
    }

    @Override
    public void onEpisodeEnd(int ep) {
        super.onEpisodeEnd(ep);
        Player p = Bukkit.getPlayer(playerUUID);
        if (p == null) return;
        currentSpeed = Math.min(0.5, currentSpeed + 0.1);
        applySpeed(p, currentSpeed);
    }

    @Override
    public void onNightTick(Player player) {
        // Compter les villageois vivants dans un rayon de 20 blocs
        long nearbyVillagers = player.getNearbyEntities(20, 20, 20).stream()
            .filter(e -> e instanceof Player)
            .map(e -> (Player) e)
            .filter(p -> plugin.getGameManager().isAlive(p.getUniqueId()))
            .filter(p -> plugin.getRoleManager().isVillager(p.getUniqueId()))
            .count();

        if (nearbyVillagers >= 4) {
            // Résistance I (amp 0) ≈ 20% — on l'applique en plus du speed
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 0, false, false, false));
        } else {
            player.removePotionEffect(PotionEffectType.RESISTANCE);
        }
    }

    private void applySpeed(Player p, double s) {
        int amp = (int) Math.round(s / 0.1) - 1;
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, Math.max(0, amp), false, false, false));
    }
}
