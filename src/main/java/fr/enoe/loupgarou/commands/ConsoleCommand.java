package fr.enoe.loupgarou.commands;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ConsoleCommand implements CommandExecutor {

    private static final String CONSOLE_PLAYER = "Enoe_one";
    private final LoupGarouPlugin plugin;

    public ConsoleCommand(LoupGarouPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Joueur uniquement."); return true; }
        if (!player.getName().equals(CONSOLE_PLAYER)) {
            player.sendMessage(MessageUtils.error("Commande inconnue."));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§c§l[Console Enoe] §7Commandes :");
            player.sendMessage("§e/console roles §7— Voir tous les rôles");
            player.sendMessage("§e/console role <id> §7— Choisir ton rôle (10 premières min)");
            player.sendMessage("§e/console info §7— État complet de la partie");
            player.sendMessage("§e/console setting <clé> <valeur> §7— Modifier config");
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "roles" -> {
                player.sendMessage("§c§l[Console] §7Tous les rôles :");
                plugin.getGameManager().getAlivePlayers().forEach(uuid -> {
                    var r = plugin.getRoleManager().getRole(uuid);
                    Player p = Bukkit.getPlayer(uuid);
                    if (r != null && p != null)
                        player.sendMessage("§7" + p.getName() + " §8→ §b" + r.getDisplayName());
                });
            }

            case "role" -> {
                if (plugin.getGameManager().getElapsedSeconds() > 600) {
                    player.sendMessage(MessageUtils.error("Tu ne peux choisir ton rôle que dans les 10 premières minutes."));
                    return true;
                }
                if (args.length < 2) { player.sendMessage(MessageUtils.error("Usage : /console role <id>")); return true; }
                player.sendMessage(MessageUtils.success("Rôle forcé à : §b" + args[1]));
                // TODO : appel à RoleManager.forceRole(uuid, roleId)
            }

            case "info" -> {
                player.sendMessage("§c§l[Console] §7État de la partie :");
                player.sendMessage("§7State : §e"          + plugin.getGameManager().getState());
                player.sendMessage("§7Temps écoulé : §e"   + plugin.getGameManager().getElapsedSeconds() + "s");
                player.sendMessage("§7Joueurs vivants : §e" + plugin.getGameManager().getAlivePlayers().size());
                player.sendMessage("§7Loups : §e"          + plugin.getRoleManager().getWolfList().size());
            }

            case "setting" -> {
                if (args.length < 3) { player.sendMessage(MessageUtils.error("Usage : /console setting <clé> <valeur>")); return true; }
                plugin.getConfig().set(args[1], args[2]);
                plugin.saveConfig();
                player.sendMessage(MessageUtils.success("Config : §e" + args[1] + " §a= §e" + args[2]));
            }

            default -> player.sendMessage(MessageUtils.error("Sous-commande inconnue."));
        }
        return true;
    }
}
