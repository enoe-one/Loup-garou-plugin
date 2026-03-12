package fr.enoe.loupgarou.listeners;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.core.GameState;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class PvpListener implements Listener {

    private final LoupGarouPlugin plugin;

    public PvpListener(LoupGarouPlugin plugin) { this.plugin = plugin; }

    /**
     * PVP style 1.8 : vitesse d'attaque maximale (pas de cooldown).
     * Paper 1.21 : Attribute.GENERIC_ATTACK_SPEED (l'ancien ATTACK_SPEED a été renommé).
     * setAttackCooldown() a été supprimé — on passe uniquement par l'attribut.
     */
    public static void setAttackSpeed(Player p) {
        // Paper 1.21 : utiliser GENERIC_ATTACK_SPEED
        AttributeInstance attr = p.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (attr != null) attr.setBaseValue(1024.0);
    }

    /** Applique la vitesse d'attaque 1.8 dès qu'un joueur rejoint. */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        setAttackSpeed(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim))    return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (plugin.getGameManager().getState() != GameState.RUNNING) return;

        // PVP désactivé avant 20 minutes
        if (plugin.getGameManager().getElapsedSeconds() < 1200) {
            event.setCancelled(true);
            attacker.sendMessage(MessageUtils.error("Le PVP sera activé dans §e"
                    + ((1200 - plugin.getGameManager().getElapsedSeconds()) / 60) + " §cminute(s) !"));
            return;
        }

        // PVP style 1.8 : s'assurer que la vitesse reste maximale
        // (setAttackCooldown() supprimé en 1.21 — l'attribut suffit)
        setAttackSpeed(attacker);
    }
}
