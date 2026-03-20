package fr.enoe.loupgarou.managers;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.impl.special.*;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * SkysofrenieManager — Gère l'événement spécial "La Révolution Française".
 *
 * Timeline :
 *  - 2h10 : compte à rebours commence (son de tambour, messages)
 *  - 2h10 : l'Inconnu reçoit "Tu commences à te remémorer qui tu es..." + Weakness I + Lenteur I
 *  - 2h14 : message d'ambiance final
 *  - 2h15 : transformation de tous les rôles concernés + annonce épique
 */
public class SkysofrenieManager {

    private final LoupGarouPlugin plugin;

    /** Tâches schedulées pour pouvoir les annuler si la partie s'arrête */
    private final List<BukkitTask> scheduledTasks = new ArrayList<>();

    /** true si l'événement a déjà été déclenché cette partie */
    private boolean triggered = false;

    /** Mapping rôle original → rôle transformé (pour la sauvegarde) */
    private final Map<UUID, String> originalRoles = new HashMap<>();

    public SkysofrenieManager(LoupGarouPlugin plugin) {
        this.plugin = plugin;
    }

    // ─── ACTIVATION ────────────────────────────────────────────────────────

    /**
     * Lance les timers de l'événement dès le démarrage de la partie.
     * À appeler dans GameManager.startGame() si events.skyssofrenie = true.
     */
    public void schedule() {
        if (!plugin.getConfig().getBoolean("events.skyssofrenie", false)) return;
        if (triggered) return;

        int secondsTo2h10 = (2 * 60 + 10) * 60;  // 7800s
        int secondsTo2h14 = (2 * 60 + 14) * 60;  // 8040s
        int secondsTo2h15 = (2 * 60 + 15) * 60;  // 8100s

        // ── 2h00 — Premier avertissement ───────────────────────────────────
        scheduledTasks.add(new BukkitRunnable() {
            @Override public void run() {
                Bukkit.getOnlinePlayers().forEach(p -> {
                    p.sendMessage("");
                    p.sendMessage("§8§o  ... Des noms oubliés murmurent dans le vent ...");
                    p.sendMessage("");
                });
            }
        }.runTaskLater(plugin, (long) (2 * 60 * 60) * 20));

        // ── 2h10 — Tambour + avertissement inconu ──────────────────────────
        scheduledTasks.add(new BukkitRunnable() {
            @Override public void run() {
                // Son de tambour sur tout le serveur
                Bukkit.getOnlinePlayers().forEach(p -> {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1f, 0.5f);
                    p.sendMessage("§8§m                                              ");
                    p.sendMessage("§7§o  La terre tremble sous des pas que l'Histoire connaît.");
                    p.sendMessage("§7§o  Dans cinq minutes, le monde ne sera plus le même.");
                    p.sendMessage("§8§m                                              ");
                });

                // Avertissement spécial à l'Inconnu
                warnInconnu();
            }
        }.runTaskLater(plugin, (long) secondsTo2h10 * 20));

        // ── 2h12 — Deuxième battement ──────────────────────────────────────
        scheduledTasks.add(new BukkitRunnable() {
            @Override public void run() {
                Bukkit.getOnlinePlayers().forEach(p -> {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1f, 0.6f);
                    p.sendMessage("§8§o  ... Les canons grondent. Plus que trois minutes avant l'Histoire ...");
                });
            }
        }.runTaskLater(plugin, (long) ((2 * 60 + 12) * 60) * 20));

        // ── 2h14 — Message dramatique final ────────────────────────────────
        scheduledTasks.add(new BukkitRunnable() {
            @Override public void run() {
                Bukkit.getOnlinePlayers().forEach(p -> {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1f, 0.8f);
                    p.sendMessage("§e§o  « Du haut de ces pyramides, quarante siècles vous contemplent. »");
                    p.sendMessage("§8§o  ... Plus qu'une minute avant la Révolution ...");
                });
            }
        }.runTaskLater(plugin, (long) secondsTo2h14 * 20));

        // ── 2h15 — DÉCLENCHEMENT ───────────────────────────────────────────
        scheduledTasks.add(new BukkitRunnable() {
            @Override public void run() {
                triggerRevolution();
            }
        }.runTaskLater(plugin, (long) secondsTo2h15 * 20));
    }

    // ─── AVERTISSEMENT INCONNU ─────────────────────────────────────────────

    private void warnInconnu() {
        plugin.getGameManager().getAlivePlayers().forEach(uuid -> {
            Role r = plugin.getRoleManager().getRole(uuid);
            if (r != null && r.getId().equals("inconnu")) {
                Player inconnu = Bukkit.getPlayer(uuid);
                if (inconnu != null) {
                    inconnu.sendMessage("");
                    inconnu.sendMessage("§d§l[???] §r§dDes images surgissent... des batailles, des couronnes, un aigle.");
                    inconnu.sendMessage("§7§o  Un nom résonne dans ta tête comme un tambour de guerre.");
                    inconnu.sendMessage("§7§o  Qui es-tu, toi qui portes l'Histoire sans le savoir ?");
                    inconnu.sendMessage("");

                    // Weakness I + Lenteur I pendant 5 minutes
                    inconnu.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,   20 * 60 * 5, 0, false, false));
                    inconnu.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,        20 * 60 * 5, 0, false, false));

                    // Titre à l'écran
                    inconnu.sendTitle("§d§l~ ? ~", "§7... tu te souviens ...", 20, 120, 30);
                }
            }
        });
    }

    // ─── DÉCLENCHEMENT PRINCIPAL ───────────────────────────────────────────

    private void triggerRevolution() {
        triggered = true;

        // ── Annonce épique ─────────────────────────────────────────────────
        String msg = "§c§l  ⚔  Le peuple prend les Armes — l'Empereur arrive.  ⚔  ";
        Bukkit.getOnlinePlayers().forEach(p -> {
            p.sendMessage("");
            p.sendMessage("§6§m                                                            ");
            p.sendMessage(msg);
            p.sendMessage("§f  Les peuples combattront, jamais ne s'étendront.");
            p.sendMessage("§7§o  La liberté ou la mort — il n'est pas d'autre chemin.");
            p.sendMessage("§6§m                                                            ");
            p.sendMessage("");

            // Titre à l'écran
            p.sendTitle(
                "§c§l⚔ LA RÉVOLUTION ⚔",
                "§eLe peuple prend les armes !",
                10, 100, 20
            );

            // Son dramatique
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 0.7f);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM,   1f, 1.0f);
        });

        // ── Transformations ────────────────────────────────────────────────
        // Petit délai pour laisser l'annonce s'afficher
        new BukkitRunnable() {
            @Override public void run() {
                transformPlayers();
            }
        }.runTaskLater(plugin, 40L); // 2 secondes après l'annonce
    }

    // ─── TRANSFORMATIONS ───────────────────────────────────────────────────

    private void transformPlayers() {
        Set<UUID> alive = plugin.getGameManager().getAlivePlayers();

        for (UUID uuid : alive) {
            Role current = plugin.getRoleManager().getRole(uuid);
            if (current == null) continue;

            Player player = Bukkit.getPlayer(uuid);

            switch (current.getId()) {

                case "citoyen" -> {
                    originalRoles.put(uuid, current.getId());
                    GardeRepublicain newRole = new GardeRepublicain(plugin, uuid);
                    plugin.getRoleManager().forceSetRole(uuid, newRole);
                    if (player != null) newRole.applyTransformation(player);
                }

                case "maire" -> {
                    originalRoles.put(uuid, current.getId());
                    PetitBourgois newRole = new PetitBourgois(plugin, uuid);
                    plugin.getRoleManager().forceSetRole(uuid, newRole);
                    if (player != null) newRole.applyTransformation(player);
                }

                case "simple_villagois" -> {
                    originalRoles.put(uuid, current.getId());
                    Revolutionnaire newRole = new Revolutionnaire(plugin, uuid);
                    plugin.getRoleManager().forceSetRole(uuid, newRole);
                    if (player != null) newRole.applyTransformation(player);
                }

                case "grand_mechant_loup" -> {
                    originalRoles.put(uuid, current.getId());
                    GeorgeIII newRole = new GeorgeIII(plugin, uuid);
                    plugin.getRoleManager().forceSetRole(uuid, newRole);
                    if (player != null) newRole.applyTransformation(player);
                }

                case "loup_endormi" -> {
                    // Se rendort + Résistance I permanente
                    originalRoles.put(uuid, current.getId());
                    if (player != null) {
                        player.addPotionEffect(new PotionEffect(
                            PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 0, false, false
                        ));
                        player.sendMessage("§8§l[Révolution] §r§8La guerre fait rage — et toi, tu dors, indifférent aux empires.");
                        player.sendMessage("§7§o  Le silence t'arme mieux que n'importe quelle lame.");
                        player.sendTitle("§8§l~ Endormi ~", "§7Résistance I permanente", 20, 80, 20);
                    }
                }

                case "loup_simple" -> {
                    originalRoles.put(uuid, current.getId());
                    GardeAnglais newRole = new GardeAnglais(plugin, uuid);
                    plugin.getRoleManager().forceSetRole(uuid, newRole);
                    if (player != null) newRole.applyTransformation(player);
                }

                case "inconnu" -> {
                    originalRoles.put(uuid, current.getId());
                    // Retirer les effets négatifs de la période de transition
                    if (player != null) {
                        player.removePotionEffect(PotionEffectType.WEAKNESS);
                        player.removePotionEffect(PotionEffectType.SLOWNESS);
                    }
                    Napoleon newRole = new Napoleon(plugin, uuid);
                    plugin.getRoleManager().forceSetRole(uuid, newRole);
                    if (player != null) newRole.applyTransformation(player);
                }

                default -> {
                    // Joueurs non transformés — juste le message d'annonce
                    if (player != null) {
                        player.sendMessage("§7§o  [Révolution] La Révolution gronde — tu en es le témoin silencieux.");
                    }
                }
            }
        }

        // ── Donner à tous un livre "Parchemin de la Révolution" ────────────
        giveScrollsToAll(alive);

        // Log console
        plugin.getLogger().info("[LG-SKYSOFRENIE] Révolution déclenchée — " + originalRoles.size() + " rôles transformés.");
    }

    // ─── PARCHEMIN DE LA RÉVOLUTION ────────────────────────────────────────

    private void giveScrollsToAll(Set<UUID> alive) {
        for (UUID uuid : alive) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            Role r = plugin.getRoleManager().getRole(uuid);
            if (r == null) continue;

            org.bukkit.inventory.ItemStack scroll = new org.bukkit.inventory.ItemStack(Material.WRITTEN_BOOK);
            org.bukkit.inventory.meta.BookMeta meta = (org.bukkit.inventory.meta.BookMeta) scroll.getItemMeta();
            if (meta == null) continue;

            meta.setTitle("§6Parchemin de la Révolution");
            meta.setAuthor("L'Histoire");
            meta.setDisplayName("§6Parchemin de la Révolution");

            String roleName = r.getDisplayName();
            String desc = r.getDescription();

            meta.addPage(
                "§l§nRévolution Française\n\n"
                + "§rTu es désormais :\n"
                + roleName + "\n\n"
                + "§7" + desc + "\n\n"
                + "§8§o— Que l'Histoire te juge.\n"
                + "§8§o  Que la République se souvienne.\n"
                + "§8§o    Que la France éternelle te guide."
            );

            scroll.setItemMeta(meta);
            p.getInventory().addItem(scroll);
        }
    }

    // ─── UTILITAIRES ───────────────────────────────────────────────────────

    public boolean isTriggered() { return triggered; }

    public void reset() {
        triggered = false;
        originalRoles.clear();
        scheduledTasks.forEach(t -> { if (!t.isCancelled()) t.cancel(); });
        scheduledTasks.clear();
    }

    public Map<UUID, String> getOriginalRoles() { return Collections.unmodifiableMap(originalRoles); }
}
