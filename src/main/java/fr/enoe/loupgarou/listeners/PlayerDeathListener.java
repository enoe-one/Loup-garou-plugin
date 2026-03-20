package fr.enoe.loupgarou.listeners;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.core.GameState;
import fr.enoe.loupgarou.roles.impl.village.ChevaliereEpeeRouillee;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private final LoupGarouPlugin plugin;

    public PlayerDeathListener(LoupGarouPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        Player dead = event.getEntity();
        if (plugin.getGameManager().getState() != GameState.RUNNING) return;
        if (!plugin.getGameManager().isAlive(dead.getUniqueId())) return;

        // Supprimer message et drops vanilla
        event.setKeepInventory(true);
        event.getDrops().clear();
        event.setDeathMessage(null);

        // ── Chevalier : pénalité si le tueur est un Chevalier qui tue un non-loup ──
        Player killer = dead.getKiller();
        if (killer != null) {
            var killerRole = plugin.getRoleManager().getRole(killer.getUniqueId());
            if (killerRole instanceof ChevaliereEpeeRouillee chevalier) {
                boolean deadIsWolf = plugin.getRoleManager().isWolf(dead.getUniqueId());
                if (deadIsWolf) {
                    chevalier.onWolfKilled(killer);
                } else {
                    chevalier.onNonWolfKill(killer);
                }
            }
        }

        // Déléguer à GameManager
        plugin.getGameManager().handlePlayerDeath(dead);
    }
}

