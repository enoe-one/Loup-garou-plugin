package fr.enoe.loupgarou.roles.impl.hybride;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Chevaucheur de Cochon — rôle Hybride.
 *
 * - Marteau (hache en bois, dégâts épée diamant Tranchant 2, non enchantable).
 * - Lenteur si le Chasseur est à moins de 20 blocs.
 * - /lg hogrier (3×, cooldown 20 min) : cochon avec 2× PV d'un joueur, selle, no fall.
 *   Tenu uniquement par le CDC avec le Marteau en main. Dure 5 min.
 *   Dessus : Force I + Speed II + Résistance I.
 * - Carotte sur bâton donnée automatiquement pour contrôler le cochon.
 */
public class ChevaucheurDeCochon extends Role implements Listener {

    private static final String MARTEAU_NAME = "§6Marteau";
    private static final int MAX_HOGRIER = 3;
    private static final long COOLDOWN_TICKS = 20L * 60 * 20; // 20 min

    private int hogrierLeft = MAX_HOGRIER;
    private long lastHogrierTick = -9999L;
    private Pig activePig = null;
    private BukkitRunnable pigTimer = null;

    public ChevaucheurDeCochon(LoupGarouPlugin p, UUID u) { super(p, u); }

    @Override public String getDisplayName() { return "§6Chevaucheur de Cochon"; }
    @Override public String getDescription() {
        return "§7Marteau (dégâts épée diamant T2). /lg hogrier (3×, 20min cd) → cochon 2× PV. Dessus : Force+Speed+Résistance 5 min.";
    }
    @Override public RoleFamily getFamily() { return RoleFamily.HYBRIDE; }
    @Override public String getId()         { return "chevaucheur_cochon"; }

    @Override
    public void onGameStart(Player player) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        giveMarteau(player);
    }

    private void giveMarteau(Player player) {
        ItemStack marteau = new ItemStack(Material.WOODEN_AXE);
        ItemMeta meta = marteau.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MARTEAU_NAME);
            meta.setLore(List.of("§7Dégâts : épée en diamant Tranchant II", "§8Ne peut pas être enchanté davantage"));
            meta.addEnchant(Enchantment.SHARPNESS, 2, true);
            meta.setUnbreakable(true);
            marteau.setItemMeta(meta);
        }
        player.getInventory().addItem(marteau);
    }

    @Override
    public void onNightTick(Player player) {
        // Lenteur si près du Chasseur (<20 blocs)
        plugin.getGameManager().getAlivePlayers().stream()
            .filter(u -> { var r = plugin.getRoleManager().getRole(u); return r != null && r.getId().equals("chasseur"); })
            .map(Bukkit::getPlayer)
            .filter(Objects::nonNull)
            .filter(c -> c.getWorld().equals(player.getWorld()) && c.getLocation().distance(player.getLocation()) < 20)
            .findFirst()
            .ifPresent(c -> player.addPotionEffect(
                new PotionEffect(PotionEffectType.SLOWNESS, 60, 1, false, false, false)));
    }

    @Override
    public boolean onPowerCommand(Player player, String[] args) {
        if (!args[0].equalsIgnoreCase("hogrier")) return false;

        if (hogrierLeft <= 0) {
            player.sendMessage(MessageUtils.error("Plus de HOGRIER disponibles (3/3 utilisés).")); return true;
        }
        long now = Bukkit.getCurrentTick();
        if (lastHogrierTick > 0 && now - lastHogrierTick < COOLDOWN_TICKS) {
            long rem = (COOLDOWN_TICKS - (now - lastHogrierTick)) / 20;
            player.sendMessage(MessageUtils.error("Cooldown : " + (rem / 60) + "m " + (rem % 60) + "s.")); return true;
        }
        if (activePig != null && !activePig.isDead()) {
            player.sendMessage(MessageUtils.error("Un cochon est déjà actif !")); return true;
        }

        lastHogrierTick = now;
        hogrierLeft--;
        spawnPig(player);
        return true;
    }

    private void spawnPig(Player player) {
        Location loc = player.getLocation().add(player.getLocation().getDirection().multiply(2));
        activePig = (Pig) player.getWorld().spawnEntity(loc, EntityType.PIG);

        // 2× PV d'un joueur (joueur = 20 HP → cochon = 40 HP = 20♥)
        activePig.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40.0);
        activePig.setHealth(40.0);
        activePig.setSaddle(true); // nécessaire pour être monté
        activePig.setCustomName("§6§lCochon de " + player.getName() + " §7(300s)");
        activePig.setCustomNameVisible(true);
        activePig.setAI(true);

        // Donner carotte sur bâton pour contrôler le cochon
        ItemStack carotte = new ItemStack(Material.CARROT_ON_A_STICK);
        ItemMeta cm = carotte.getItemMeta();
        if (cm != null) { cm.setDisplayName("§6Rênes du cochon"); carotte.setItemMeta(cm); }
        player.getInventory().addItem(carotte);

        player.sendMessage(MessageUtils.success(
            "§6HOGRIER ! §eMonte sur le cochon avec le Marteau en main pour le contrôler. ("
            + hogrierLeft + " utilisation(s) restante(s))"));
        player.sendMessage("§7Une carotte sur bâton a été ajoutée à ton inventaire pour diriger le cochon.");

        // Timer 5 min
        if (pigTimer != null) pigTimer.cancel();
        pigTimer = new BukkitRunnable() {
            int remaining = 300;
            @Override public void run() {
                if (activePig == null || activePig.isDead()) { cancel(); activePig = null; return; }
                remaining--;
                activePig.setCustomName("§6§lCochon §7(" + remaining + "s)");
                if (remaining <= 0) {
                    activePig.remove();
                    activePig = null;
                    Player p = Bukkit.getPlayer(playerUUID);
                    if (p != null) {
                        p.sendMessage("§c[Cochon] §7Ton cochon est mort d'épuisement !");
                        // Retirer la carotte sur bâton
                        p.getInventory().remove(Material.CARROT_ON_A_STICK);
                    }
                    cancel();
                }
            }
        };
        pigTimer.runTaskTimer(plugin, 20L, 20L);
    }

    // ── Empêcher les autres de monter le cochon ───────────────────────────────
    @EventHandler(priority = EventPriority.HIGH)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Pig pig)) return;
        if (!pig.equals(activePig)) return;

        Player p = event.getPlayer();
        if (p.getUniqueId().equals(playerUUID)) {
            // CDC — vérifier qu'il a le Marteau en main
            ItemStack hand = p.getInventory().getItemInMainHand();
            if (!isMarteau(hand)) {
                event.setCancelled(true);
                p.sendMessage(MessageUtils.error("Tu dois tenir le §6Marteau§c pour monter le cochon !"));
                return;
            }
            // Appliquer les effets après montée
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (p.isInsideVehicle()) applyRidingEffects(p);
            }, 2L);
        } else {
            // Autre joueur — interdit
            event.setCancelled(true);
            p.sendMessage(MessageUtils.error("Ce cochon appartient à quelqu'un d'autre !"));
        }
    }

    // ── Retirer les effets quand le CDC descend ───────────────────────────────
    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        if (!(event.getExited() instanceof Player p)) return;
        if (!p.getUniqueId().equals(playerUUID)) return;
        p.removePotionEffect(PotionEffectType.STRENGTH);
        p.removePotionEffect(PotionEffectType.SPEED);
        p.removePotionEffect(PotionEffectType.RESISTANCE);
    }

    // ── No fall pour le cochon ────────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Pig pig)) return;
        if (!pig.equals(activePig)) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }

    private void applyRidingEffects(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,   6000, 0, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,      6000, 1, false, false, false)); // Speed II = ~1.5×
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 6000, 0, false, false, false));
    }

    private boolean isMarteau(ItemStack item) {
        if (item == null || item.getType() != Material.WOODEN_AXE) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && MARTEAU_NAME.equals(meta.getDisplayName());
    }
}
