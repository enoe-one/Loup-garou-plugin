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

    // Rôles villageois avec déjà un effet propre → pas de résistance de base
    private static final java.util.Set<String> ROLES_WITH_OWN_EFFECT = java.util.Set.of(
        "salvateur",     // résistance propre
        "chevalier",     // force propre
        "chasseur",      // force propre
        "grand_mechant_loup" // rage propre
    );

    public NightListener(LoupGarouPlugin plugin) {
        this.plugin = plugin;
        startNightTick();
    }

    private void startNightTick() {
        nightTask = new org.bukkit.scheduler.BukkitRunnable() {
            private boolean lastNight = false;

            @Override
            public void run() {
                if (plugin.getGameManager().getState() != GameState.RUNNING) return;

                World world = Bukkit.getWorlds().get(0);
                long time   = world.getTime();
                boolean isNight = (time >= 13000 && time <= 23000);

                // Début de nuit : ouvrir le canal loup (1 fois par nuit)
                if (isNight && !lastNight) {
                    plugin.getChatManager().tryOpenWolfChat();
                }
                lastNight = isNight;

                for (var uuid : plugin.getGameManager().getAlivePlayers()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p == null) continue;
                    var role = plugin.getRoleManager().getRole(uuid);
                    if (role == null) continue;

                    // ── Résistance 20% de base pour les villageois ────────
                    // (Résistance I = amp 0 ≈ 20%)
                    // Sauf : loups, solitaires, et rôles ayant déjà leur propre effet
                    if (plugin.getRoleManager().isVillager(uuid)
                            && !ROLES_WITH_OWN_EFFECT.contains(role.getId())) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 0, false, false, false));
                    }

                    // ── Force 30% pour TOUS les loups la nuit (amp 0, non visible) ──
                    if (plugin.getRoleManager().isWolf(uuid)) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 0, false, false, false));
                    }

                    // ── Tick de nuit du rôle ──────────────────────────────
                    role.onNightTick(p);

                    // ── Loup Perfide : invisible sans armure ───────────────
                    if (role.getId().equals("loup_perfide") && hasNoArmor(p)) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0, false, false, false));
                    }

                    // ── Feu Follet : invisible sans armure ─────────────────
                    if (role.getId().equals("feu_follet") && hasNoArmor(p)) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0, false, false, false));
                    }

                    // ── Jour : retirer la résistance de base villageois ────
                    if (!isNight && plugin.getRoleManager().isVillager(uuid)
                            && !ROLES_WITH_OWN_EFFECT.contains(role.getId())) {
                        // Ne retirer que si c'est l'effet de base (amp 0, ambiant false)
                        var existing = p.getPotionEffect(PotionEffectType.RESISTANCE);
                        if (existing != null && existing.getAmplifier() == 0 && !existing.isAmbient()) {
                            p.removePotionEffect(PotionEffectType.RESISTANCE);
                        }
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
