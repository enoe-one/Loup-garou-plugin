package fr.enoe.loupgarou.roles.impl.solitaire;
import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import java.util.UUID;
public class Inconnu extends Role {
    public Inconnu(LoupGarouPlugin p, UUID u) { super(p, u); }
    @Override public String getDisplayName()         { return "§7Inconnu"; }
    @Override public String getDescription()          { return "§7Vu comme village par les rôles info, comme loup par les loups. Ne vote pas."; }
    @Override public RoleFamily getFamily()           { return RoleFamily.SOLITAIRE; }
    @Override public RoleFamily getApparentFamily()   { return RoleFamily.VILLAGE; }
    @Override public String getId()                   { return "inconnu"; }
    @Override public void onNightTick(Player player) {
        for (UUID alive : plugin.getGameManager().getAlivePlayers()) {
            if (alive.equals(playerUUID)) continue;
            Player target = Bukkit.getPlayer(alive);
            if (target == null) continue;
            Role role = plugin.getRoleManager().getRole(alive);
            if (role == null) continue;
            Color color = switch (role.getFamily()) {
                case VILLAGE   -> Color.GREEN;
                case LOUP      -> Color.RED;
                case SOLITAIRE -> Color.YELLOW;
                default        -> Color.WHITE;
            };
            target.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0,2,0), 5, 0.3,0.3,0.3, 0, new Particle.DustOptions(color, 1.5f));
        }
    }
}
