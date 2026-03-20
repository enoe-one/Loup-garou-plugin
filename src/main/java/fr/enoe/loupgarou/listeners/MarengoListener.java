package fr.enoe.loupgarou.listeners;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.core.GameState;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.impl.special.Napoleon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

/**
 * Empêche quiconque sauf Napoléon de monter ou d'attaquer Marengo.
 */
public class MarengoListener implements Listener {

    private final LoupGarouPlugin plugin;

    public MarengoListener(LoupGarouPlugin plugin) { this.plugin = plugin; }

    /** Empêche de monter Marengo si ce n'est pas Napoléon */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (plugin.getGameManager().getState() != GameState.RUNNING) return;
        Entity entity = event.getRightClicked();
        if (!(entity instanceof Horse)) return;

        Player player = event.getPlayer();
        Role role = plugin.getRoleManager().getRole(player.getUniqueId());

        if (!(role instanceof Napoleon napoleon)) {
            // Ce n'est pas Napoléon — vérifier si c'est Marengo
            if (isAnyMarengo(entity)) {
                event.setCancelled(true);
                player.sendMessage("§c[Marengo] §7Ce cheval n'obéit qu'à l'Empereur !");
            }
            return;
        }

        // C'est Napoléon — vérifier que c'est bien son Marengo
        if (!napoleon.isMarengo(entity.getUniqueId())) {
            // Autre cheval, pas Marengo — laisser faire
        }
    }

    /** Empêche d'attaquer Marengo */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (plugin.getGameManager().getState() != GameState.RUNNING) return;
        if (!(event.getEntity() instanceof Horse)) return;
        if (!(event.getDamager() instanceof Player player)) return;

        Role role = plugin.getRoleManager().getRole(player.getUniqueId());

        // Si c'est Napoléon qui frappe son propre cheval — autoriser (pour le désmonter)
        if (role instanceof Napoleon napoleon && napoleon.isMarengo(event.getEntity().getUniqueId())) return;

        // Sinon, si c'est Marengo, annuler
        if (isAnyMarengo(event.getEntity())) {
            event.setCancelled(true);
            player.sendMessage("§c[Marengo] §7Tu ne peux pas blesser le destrier de l'Empereur !");
        }
    }

    /** Vérifie si l'entité est le Marengo d'un Napoléon en jeu */
    private boolean isAnyMarengo(Entity entity) {
        return plugin.getGameManager().getAlivePlayers().stream().anyMatch(uuid -> {
            Role r = plugin.getRoleManager().getRole(uuid);
            return r instanceof Napoleon napoleon && napoleon.isMarengo(entity.getUniqueId());
        });
    }
}
