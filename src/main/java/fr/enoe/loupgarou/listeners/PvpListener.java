package fr.enoe.loupgarou.listeners;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.core.GameState;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PvpListener implements Listener {

    private final LoupGarouPlugin plugin;

    public PvpListener(LoupGarouPlugin plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim))   return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (plugin.getGameManager().getState() != GameState.RUNNING) return;

        // PVP désactivé avant 20 minutes
        if (plugin.getGameManager().getElapsedSeconds() < 1200) {
            event.setCancelled(true);
            attacker.sendMessage(MessageUtils.error("Le PVP sera activé dans §e"
                    + ((1200 - plugin.getGameManager().getElapsedSeconds()) / 60) + " §cminute(s) !"));
        }
    }
}
