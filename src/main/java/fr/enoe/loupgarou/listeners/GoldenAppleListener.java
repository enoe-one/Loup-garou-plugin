package fr.enoe.loupgarou.listeners;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.core.GameState;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.impl.special.Napoleon;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GoldenAppleListener implements Listener {

    private final LoupGarouPlugin plugin;

    public GoldenAppleListener(LoupGarouPlugin plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGH)
    public void onConsume(PlayerItemConsumeEvent event) {
        if (plugin.getGameManager().getState() != GameState.RUNNING) return;

        Material mat = event.getItem().getType();
        if (mat != Material.GOLDEN_APPLE && mat != Material.ENCHANTED_GOLDEN_APPLE) return;

        Player player = event.getPlayer();

        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {

            // Retirer l'absorption vanilla
            player.removePotionEffect(PotionEffectType.ABSORPTION);

            Role role = plugin.getRoleManager().getRole(player.getUniqueId());

            // ── CAS NAPOLÉON : logique entièrement remplacée ──────────────────
            if (role instanceof Napoleon napoleon) {
                napoleon.applyNapoleonAppleBonus(player);
                return; // on court-circuite la logique standard
            }

            // ── CAS STANDARD : +2 cœurs de vie + 2 cœurs d'absorption ────────
            double maxHp = 20.0;
            var attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attr != null) maxHp = attr.getBaseValue();

            double newHp = Math.min(maxHp, player.getHealth() + 4.0);
            player.setHealth(newHp);

            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0, false, false, true));
            player.sendMessage("§6[Pomme] §a+2♥ §7| §6+2♥ absorption");

        }, 1L);
    }
}
