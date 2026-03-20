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

/**
 * Gère les effets nocturnes de tous les joueurs.
 *
 * RÉSISTANCE DE BASE VILLAGEOIS : ~10%
 *   Minecraft n'a pas de Résistance 10% native.
 *   Formule : Résistance I (amp 0) = 20%. Pour obtenir ~10% net,
 *   on applique Résistance I pendant 10 ticks (0.5s) sur un cycle de 20 ticks (1s)
 *   → uptime 50% × 20% = ~10% de réduction effective en moyenne.
 *
 * HIÉRARCHIE DES EFFETS (aucun rôle villageois ne dépasse le plafond solitaire) :
 *   Villageois base nuit : ~10% résistance
 *   Salvateur cible     : Résistance I (20%) uptime 100% ≈ "30%" (le plus proche atteignable)
 *   Chevalier/Chasseur  : Force I (30%) — propre à leur rôle
 *   Solitaires          : jusqu'à Force II (60%) / Résistance II (40%)
 */
public class NightListener implements Listener {

    private final LoupGarouPlugin plugin;
    private BukkitTask nightTask;

    // Rôles villageois avec défense propre → pas de résistance de base
    private static final java.util.Set<String> ROLES_WITH_OWN_DEFENSE = java.util.Set.of(
        "salvateur", "chevalier"
    );

    private int tick = 0;

    public NightListener(LoupGarouPlugin plugin) {
        this.plugin = plugin;
        startNightTick();
    }

    private void startNightTick() {
        nightTask = new org.bukkit.scheduler.BukkitRunnable() {
            private boolean lastNight = false;

            @Override public void run() {
                if (plugin.getGameManager().getState() != GameState.RUNNING) return;

                World world = Bukkit.getWorlds().get(0);
                long time = world.getTime();
                boolean isNight = (time >= 13000 && time <= 23000);
                tick++;

                // Début de nuit → ouvre le chat loups (1x/nuit)
                if (isNight && !lastNight) {
                    plugin.getChatManager().tryOpenWolfChat();
                }
                lastNight = isNight;

                for (var uuid : plugin.getGameManager().getAlivePlayers()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p == null) continue;
                    var role = plugin.getRoleManager().getRole(uuid);
                    if (role == null) continue;

                    if (isNight) {
                        // ── Résistance ~10% base villageois ─────────────────────
                        // Technique : Résistance I (20%) pendant 10 ticks sur 20 = 50% uptime → ~10% net
                        if (plugin.getRoleManager().isVillager(uuid)
                                && !ROLES_WITH_OWN_DEFENSE.contains(role.getId())) {
                            if (tick % 2 == 0) {
                                // ticks pairs : appliquer pour 10 ticks (0.5s)
                                p.addPotionEffect(new PotionEffect(
                                    PotionEffectType.RESISTANCE, 10, 0, false, false, false));
                            }
                            // ticks impairs : laisser expirer → ~10% effectif
                        }

                        // ── Tick de nuit propre au rôle ─────────────────────────
                        role.onNightTick(p);

                        // ── Loup Perfide / Feu Follet : invisible sans armure ───
                        if ((role.getId().equals("loup_perfide") || role.getId().equals("feu_follet"))
                                && hasNoArmor(p)) {
                            p.addPotionEffect(new PotionEffect(
                                PotionEffectType.INVISIBILITY, 60, 0, false, false, false));
                        }

                    } else {
                        // ── Jour : supprimer la résistance de base ───────────────
                        if (plugin.getRoleManager().isVillager(uuid)
                                && !ROLES_WITH_OWN_DEFENSE.contains(role.getId())) {
                            var ex = p.getPotionEffect(PotionEffectType.RESISTANCE);
                            if (ex != null && ex.getAmplifier() == 0 && !ex.isAmbient())
                                p.removePotionEffect(PotionEffectType.RESISTANCE);
                        }
                        // ── Grand Méchant Loup : Force I le jour si aucun loup mort ──
                        if (role instanceof fr.enoe.loupgarou.roles.impl.loup.GrandMechantLoup gml) {
                            gml.onDayTick(p);
                        }
                    }

                    // ── Braconnier : tick permanent (jour et nuit) ────────────────
                    if (role instanceof fr.enoe.loupgarou.roles.impl.village.Braconnier braconnier) {
                        braconnier.onTick(p);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private boolean hasNoArmor(Player p) {
        for (ItemStack i : p.getInventory().getArmorContents())
            if (i != null && i.getType() != Material.AIR) return false;
        return true;
    }
}
