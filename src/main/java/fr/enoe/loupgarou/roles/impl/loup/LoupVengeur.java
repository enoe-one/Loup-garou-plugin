package fr.enoe.loupgarou.roles.impl.loup;
import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;
public class LoupVengeur extends Role {
    public LoupVengeur(LoupGarouPlugin p, UUID u) { super(p, u); }
    @Override public String getDisplayName() { return "§cLoup Vengeur"; }
    @Override public String getDescription()  { return "§7Si un loup meurt : reçoit cible+rôle aléatoire + Speed 1 (5 min)."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.LOUP; }
    @Override public String getId()           { return "loup_vengeur"; }
    public void onWolfDied(Player watcher) {
        List<UUID> alive = new ArrayList<>(plugin.getGameManager().getAlivePlayers());
        if (alive.isEmpty()) return;
        UUID target = alive.get(new Random().nextInt(alive.size()));
        Player tp = Bukkit.getPlayer(target);
        Role role = plugin.getRoleManager().getRole(target);
        if (tp != null && role != null) watcher.sendMessage("§c[Vengeur] §eCible: §f" + tp.getName() + " §7(§b" + role.getDisplayName() + "§7)");
        watcher.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 6000, 0, false, false, true));
    }
}
