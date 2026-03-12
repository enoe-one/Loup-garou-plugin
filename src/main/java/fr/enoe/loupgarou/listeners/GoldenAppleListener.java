package fr.enoe.loupgarou.listeners;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.core.GameState;
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

        // Annuler les effets vanilla de la pomme, on applique les nôtres
        // On utilise un délai d'1 tick pour passer après la consommation vanilla
        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {

            // Retirer l'absorption vanilla (elle est trop haute sur les pommes enchantées)
            player.removePotionEffect(PotionEffectType.ABSORPTION);

            double maxHp = 20.0;
            var attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attr != null) maxHp = attr.getBaseValue();

            // +2 cœurs de vie (= +4 HP), plafonné au max
            double newHp = Math.min(maxHp, player.getHealth() + 4.0);
            player.setHealth(newHp);

            // +2 cœurs d'absorption (= 4 HP absorption) — visible car c'est le but
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0, false, false, true));

            player.sendMessage("§6[Pomme] §a+2 cœurs §7| §6+2 cœurs absorption");

        }, 1L);
    }
}
