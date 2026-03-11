package fr.enoe.loupgarou.utils;

import fr.enoe.loupgarou.LoupGarouPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class MessageUtils {

    private static final String PREFIX = "§6[LG] ";

    private MessageUtils() {}

    public static String error(String msg)   { return "§c✗ " + msg; }
    public static String success(String msg) { return "§a✔ " + msg; }
    public static String info(String msg)    { return PREFIX + "§7" + msg; }

    public static void broadcast(String msg) {
        Bukkit.broadcastMessage(msg);
    }

    public static void broadcastToAdmins(LoupGarouPlugin plugin, String msg) {
        plugin.getGameManager().getAlivePlayers().forEach(uuid -> {
            if (plugin.getGameManager().isAdmin(uuid)) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) p.sendMessage(msg);
            }
        });
        // Aussi aux morts admins
        plugin.getGameManager().getDeadPlayers().forEach(uuid -> {
            if (plugin.getGameManager().isAdmin(uuid)) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) p.sendMessage(msg);
            }
        });
    }
}
