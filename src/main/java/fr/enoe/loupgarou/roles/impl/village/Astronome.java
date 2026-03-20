package fr.enoe.loupgarou.roles.impl.village;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class Astronome extends Role {
    public Astronome(LoupGarouPlugin plugin, UUID uuid) { super(plugin, uuid); }
    @Override public String getDisplayName() { return "§9Astronome"; }
    @Override public String getDescription()  { return "§7Toutes les 10 min, faisceau coloré vers un joueur aléatoire."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.VILLAGE; }
    @Override public String getId()           { return "astronome"; }

    public void triggerTick() {
        Player self = Bukkit.getPlayer(playerUUID);
        if (self == null) return;
        List<UUID> alive = new ArrayList<>(plugin.getGameManager().getAlivePlayers());
        alive.remove(playerUUID);
        if (alive.isEmpty()) return;
        UUID targetUUID = alive.get(new Random().nextInt(alive.size()));
        Player target = Bukkit.getPlayer(targetUUID);
        if (target == null) return;
        Role role = plugin.getRoleManager().getRole(targetUUID);
        if (role == null) return;
        Particle.DustOptions dust;
        int durationTicks;
        switch (role.getFamily()) {
            case LOUP      -> { dust = new Particle.DustOptions(Color.RED,    2f); durationTicks = 1200; }
            case SOLITAIRE -> { dust = new Particle.DustOptions(Color.YELLOW, 2f); durationTicks = 600;  }
            default        -> { dust = new Particle.DustOptions(Color.GREEN,  2f); durationTicks = 600;  }
        }
        Location from = self.getLocation().add(0, 1, 0);
        Location to   = target.getLocation().add(0, 1, 0);
        final Particle.DustOptions finalDust = dust;
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (ticks >= durationTicks) { cancel(); return; }
                double dist = from.distance(to);
                for (double d = 0; d <= dist; d += 0.5) {
                    double r = d / dist;
                    from.getWorld().spawnParticle(Particle.DUST,
                        from.getX() + (to.getX()-from.getX())*r,
                        from.getY() + (to.getY()-from.getY())*r,
                        from.getZ() + (to.getZ()-from.getZ())*r,
                        1, 0, 0, 0, 0, finalDust);
                }
                ticks += 5;
            }
        }.runTaskTimer(plugin, 0L, 5L);
        self.sendMessage("§9[Astronome] §7Faisceau vers §e" + target.getName());
    }
}
