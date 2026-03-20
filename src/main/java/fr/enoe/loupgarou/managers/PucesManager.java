package fr.enoe.loupgarou.managers;

import fr.enoe.loupgarou.LoupGarouPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * PucesManager — système de puces pour les Loups-Garous.
 *
 * Chaque loup reçoit des puces à un moment aléatoire entre 50 et 150 min.
 * S'il passe >5 min cumulées à <15 blocs d'un non-loup → ce joueur est averti.
 * Le loup peut se débarrasser des puces en plongeant dans l'eau sans armure.
 */
public class PucesManager {

    private final LoupGarouPlugin plugin;

    // UUID loup infecté → temps cumulé (en secondes) passé près de chaque non-loup
    private final Map<UUID, Map<UUID, Integer>> proximity = new HashMap<>();
    // UUID loup infecté → true si ses puces ont déjà été transmises à quelqu'un
    private final Set<UUID> notifiedTargets = new HashSet<>(); // UUID non-loups déjà avertis
    // UUID loups guéris
    private final Set<UUID> cured = new HashSet<>();

    private static final int PROX_RANGE    = 15;   // blocs
    private static final int TRANSMIT_SECS = 300;  // 5 min cumulées
    private static final int MIN_MINUTES   = 50;
    private static final int MAX_MINUTES   = 150;

    public PucesManager(LoupGarouPlugin plugin) {
        this.plugin = plugin;
    }

    /** Appelé par GameManager au démarrage — programme les puces pour chaque loup. */
    public void schedulePuces() {
        Random rng = new Random();
        for (UUID wolfUUID : plugin.getRoleManager().getWolfList()) {
            int delayMinutes = MIN_MINUTES + rng.nextInt(MAX_MINUTES - MIN_MINUTES + 1);
            long delayTicks  = delayMinutes * 60L * 20L;
            Bukkit.getScheduler().runTaskLater(plugin, () -> infectWolf(wolfUUID), delayTicks);
        }
        startProximityTicker();
    }

    private void infectWolf(UUID wolfUUID) {
        if (!plugin.getGameManager().isAlive(wolfUUID)) return;
        if (cured.contains(wolfUUID)) return;
        Player wolf = Bukkit.getPlayer(wolfUUID);
        if (wolf == null) return;
        wolf.sendMessage("§6[Puces] §7Des puces ont sauté sur toi... Tu les transmettras aux non-Loups proches de toi (>5 min cumulées à <15 blocs).");
        wolf.sendMessage("§7Pour t'en débarrasser : §eplonge entièrement dans l'eau sans armure§7.");
        proximity.put(wolfUUID, new HashMap<>());
    }

    private void startProximityTicker() {
        new BukkitRunnable() {
            @Override public void run() {
                if (!plugin.getGameManager().getState().name().equals("RUNNING")) return;
                checkWaterCure();
                for (UUID wolfUUID : new HashSet<>(proximity.keySet())) {
                    if (cured.contains(wolfUUID)) { proximity.remove(wolfUUID); continue; }
                    Player wolf = Bukkit.getPlayer(wolfUUID);
                    if (wolf == null || !plugin.getGameManager().isAlive(wolfUUID)) continue;

                    Map<UUID, Integer> cumul = proximity.get(wolfUUID);
                    for (UUID aliveUUID : plugin.getGameManager().getAlivePlayers()) {
                        if (plugin.getRoleManager().isWolf(aliveUUID)) continue;
                        Player target = Bukkit.getPlayer(aliveUUID);
                        if (target == null) continue;
                        if (!target.getWorld().equals(wolf.getWorld())) continue;
                        if (target.getLocation().distance(wolf.getLocation()) > PROX_RANGE) continue;

                        int secs = cumul.getOrDefault(aliveUUID, 0) + 1;
                        cumul.put(aliveUUID, secs);

                        if (secs >= TRANSMIT_SECS && !notifiedTargets.contains(aliveUUID)) {
                            notifiedTargets.add(aliveUUID);
                            target.sendMessage("§c[Puces] §7Tu grouilles ! Un Loup-Garou a passé trop de temps près de toi. Il y a un loup dans les environs !");
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // toutes les secondes
    }

    private void checkWaterCure() {
        for (UUID wolfUUID : new HashSet<>(proximity.keySet())) {
            if (cured.contains(wolfUUID)) continue;
            Player wolf = Bukkit.getPlayer(wolfUUID);
            if (wolf == null) continue;
            // Vérifier : entièrement dans l'eau (head block = WATER) + pas d'armure
            if (wolf.getEyeLocation().getBlock().getType() == Material.WATER && !hasArmor(wolf)) {
                cured.add(wolfUUID);
                proximity.remove(wolfUUID);
                wolf.sendMessage("§a[Puces] §7Tu t'es débarrassé de tes puces en plongeant dans l'eau !");
            }
        }
    }

    private boolean hasArmor(Player p) {
        for (var item : p.getInventory().getArmorContents())
            if (item != null && item.getType() != Material.AIR) return true;
        return false;
    }

    public boolean isInfected(UUID wolfUUID) { return proximity.containsKey(wolfUUID) && !cured.contains(wolfUUID); }

    public void reset() { proximity.clear(); notifiedTargets.clear(); cured.clear(); }
}
