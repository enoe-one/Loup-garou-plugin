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
    @Override public String getDescription() { return "§7Force 30% la nuit. Si un loup meurt : cible aléatoire + Speed (5 min)."; }
    @Override public RoleFamily getFamily()  { return RoleFamily.LOUP; }
    @Override public String getId()          { return "loup_vengeur"; }

    @Override
    public void onNightTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 0, false, false, false));
    }

    public void onWolfDied(Player watcher) {
        List<UUID> alive = new ArrayList<>(plugin.getGameManager().getAlivePlayers());
        if (alive.isEmpty()) return;
        UUID target = alive.get(new Random().nextInt(alive.size()));
        Player tp = Bukkit.getPlayer(target);
        fr.enoe.loupgarou.roles.Role role = plugin.getRoleManager().getRole(target);
        if (tp != null && role != null)
            watcher.sendMessage("§c[Vengeur] §eCible: §f" + tp.getName() + " §7(§b" + role.getDisplayName() + "§7)");
        // Speed 5 min — non visible
        watcher.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 6000, 0, false, false, false));
    }
}
