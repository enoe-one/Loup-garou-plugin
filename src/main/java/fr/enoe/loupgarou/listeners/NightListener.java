package fr.enoe.loupgarou.listeners;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.core.GameState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class NightListener implements Listener {

    private final LoupGarouPlugin plugin;
    private BukkitTask nightTask;

    public NightListener(LoupGarouPlugin plugin) {
        this.plugin = plugin;
        startNightTick();
    }

    private void startNightTick() {
        nightTask = new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                if (plugin.getGameManager().getState() != GameState.RUNNING) return;

                World world = Bukkit.getWorlds().get(0);
                long time = world.getTime();
                boolean isNight = time >= 13000 && time <= 23000;
                if (!isNight) return;

                for (var uuid : plugin.getGameManager().getAlivePlayers()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p == null) continue;
                    var role = plugin.getRoleManager().getRole(uuid);
                    if (role == null) continue;

                    // Tick de nuit du rôle
                    role.onNightTick(p);

                    // Loup perfide : invisible sans armure la nuit
                    if (role.getId().equals("loup_perfide") && hasNoArmor(p)) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0, false, false, false));
                    }

                    // Feu follet : invisible sans armure
                    if (role.getId().equals("feu_follet") && hasNoArmor(p)) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0, false, false, false));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private boolean hasNoArmor(Player p) {
        for (ItemStack item : p.getInventory().getArmorContents()) {
            if (item != null && item.getType() != Material.AIR) return false;
        }
        return true;
    }
}
