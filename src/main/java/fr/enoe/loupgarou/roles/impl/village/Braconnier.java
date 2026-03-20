package fr.enoe.loupgarou.roles.impl.village;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * Braconnier — effets par fourrure :
 *  Fourrure 1 : +1♥ permanent
 *  Fourrure 2 : Résistance I (100% uptime, amp 0)
 *  Fourrure 3 : +1♥ permanent
 *  Fourrure 4 : +1♥ permanent
 *  Max : Résistance I uniquement (pas de Résistance II+)
 */
public class Braconnier extends Role {

    private final Map<UUID, WolfCorpse> wolfCorpses = new HashMap<>();
    private final Map<UUID, Long> proximityStart    = new HashMap<>();
    private int fourrures = 0;
    private boolean hasResi = false; // Résistance I accordée à la 2ème fourrure

    public Braconnier(LoupGarouPlugin p, UUID u) { super(p, u); }

    @Override public String getDisplayName() { return "§6Braconnier"; }
    @Override public String getDescription() {
        return "§7Fourrure 1 : +1♥ | Fourrure 2 : Rési I | Fourrure 3 : +1♥ | Fourrure 4 : +1♥. Max Rési I.";
    }
    @Override public RoleFamily getFamily() { return RoleFamily.VILLAGE; }
    @Override public String getId()         { return "braconnier"; }

    @Override
    public RoleFamily getApparentFamily() {
        return fourrures > 0 ? RoleFamily.LOUP : RoleFamily.VILLAGE;
    }

    public void registerWolfCorpse(UUID wolfUUID, Location loc) {
        wolfCorpses.put(wolfUUID, new WolfCorpse(loc, Bukkit.getCurrentTick()));
        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            if (!wolfCorpses.containsKey(wolfUUID)) { task.cancel(); return; }
            long age = Bukkit.getCurrentTick() - wolfCorpses.get(wolfUUID).birthTick;
            if (age > 18000) { wolfCorpses.remove(wolfUUID); task.cancel(); return; }
            loc.getWorld().spawnParticle(Particle.DUST, loc.clone().add(0, 1, 0), 5,
                new Particle.DustOptions(org.bukkit.Color.RED, 1.5f));
        }, 0L, 20L);
    }

    @Override public void onNightTick(Player player) { applyResi(player); checkProximity(player); }
    public void onTick(Player player)                { applyResi(player); checkProximity(player); }

    private void applyResi(Player player) {
        if (!hasResi) return;
        // Résistance I (amp 0) uniquement — pas d'escalade
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 0, false, false, false));
    }

    private void checkProximity(Player player) {
        Iterator<Map.Entry<UUID, WolfCorpse>> it = wolfCorpses.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            UUID wolfUUID = entry.getKey();
            Location corpse = entry.getValue().location;
            if (!corpse.getWorld().equals(player.getWorld())) continue;
            if (player.getLocation().distance(corpse) <= 10.0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 40, 0, false, false, false));
                long now = Bukkit.getCurrentTick();
                proximityStart.putIfAbsent(wolfUUID, now);
                if (now - proximityStart.get(wolfUUID) >= 200L) {
                    collectFourrure(player, wolfUUID);
                    it.remove();
                }
            } else {
                proximityStart.remove(wolfUUID);
            }
        }
    }

    private void collectFourrure(Player player, UUID wolfUUID) {
        proximityStart.remove(wolfUUID);
        fourrures++;

        switch (fourrures) {
            case 1 -> {
                // +1♥ permanent
                addMaxHealth(player, 2.0);
                player.sendMessage(MessageUtils.success("§6[Braconnier] §eFourrure 1 : §a+1♥ permanent !"));
                player.sendMessage("§7§oTu apparaîtras comme un Loup aux rôles d'information.");
            }
            case 2 -> {
                // Résistance I permanente
                hasResi = true;
                player.sendMessage(MessageUtils.success("§6[Braconnier] §eFourrure 2 : §aRésistance I permanente !"));
            }
            case 3 -> {
                addMaxHealth(player, 2.0);
                player.sendMessage(MessageUtils.success("§6[Braconnier] §eFourrure 3 : §a+1♥ permanent !"));
            }
            case 4 -> {
                addMaxHealth(player, 2.0);
                player.sendMessage(MessageUtils.success("§6[Braconnier] §eFourrure 4 : §a+1♥ permanent ! (maximum atteint)"));
            }
            default -> player.sendMessage("§7§o[Braconnier] Fourrure supplémentaire — aucun effet additionnel.");
        }
    }

    private void addMaxHealth(Player player, double amount) {
        var attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr == null) return;
        double newMax = Math.min(attr.getValue() + amount, 40.0); // plafond 20♥
        attr.setBaseValue(newMax);
        player.setHealth(Math.min(player.getHealth() + amount, newMax));
    }

    private static class WolfCorpse {
        final Location location; final long birthTick;
        WolfCorpse(Location l, long t) { location = l; birthTick = t; }
    }
}
