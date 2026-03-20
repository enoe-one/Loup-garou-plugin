package fr.enoe.loupgarou.commands;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.core.GameState;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LgCommand implements CommandExecutor {

    private final LoupGarouPlugin plugin;

    public LgCommand(LoupGarouPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessageUtils.error("Joueur uniquement."));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§e/lg create <nom> §7— Créer une room");
            player.sendMessage("§e/lg start §7— Démarrer la partie");
            player.sendMessage("§e/lg test §7— Démarrer en mode test (1 joueur, logs console)");player.sendMessage("§e/lg test stop §7— Arrêter la partie test");
            player.sendMessage("§e/lg add <min|nuit|jour|episode|pvp|roles|bordure> §7— Avancer le temps (mode test)");
            player.sendMessage("§e/lg color §7— Choisir la couleur des pseudos (vue personnelle)");
            player.sendMessage("§e/lg roles §7— Voir la composition (si visible)");
            player.sendMessage("§e/lg <pouvoir> §7— Utiliser ton pouvoir de rôle");
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "create" -> {
                if (plugin.getGameManager().getOwnerUUID() != null) {
                    player.sendMessage(MessageUtils.error("Une room existe déjà."));
                    return true;
                }
                String name = args.length > 1 ? args[1] : "Partie";
                plugin.getGameManager().createRoom(player, name);
            }

            case "start" -> {
                if (!plugin.getGameManager().isOwner(player.getUniqueId())) {
                    player.sendMessage(MessageUtils.error("Seul l'owner peut démarrer."));
                    return true;
                }
                plugin.getGameManager().startGame();
            }

            case "test" -> {
                if (!plugin.getGameManager().isAdmin(player.getUniqueId())
                        && !player.hasPermission("loupgarou.admin")) {
                    player.sendMessage(MessageUtils.error("Réservé aux admins."));
                    return true;
                }
                // /lg test stop → arrêter le mode test
                if (args.length >= 2 && args[1].equalsIgnoreCase("stop")) {
                    plugin.getTestManager().stopTest(player);
                } else {
                    plugin.getTestManager().startTest(player);
                }
            }

            case "add" -> {
                if (!plugin.getGameManager().isAdmin(player.getUniqueId())
                        && !player.hasPermission("loupgarou.admin")) {
                    player.sendMessage(MessageUtils.error("Réservé aux admins."));
                    return true;
                }
                plugin.getTestManager().addTime(player, args);
            }

            case "voter" -> {
                if (args.length < 2) { player.sendMessage(MessageUtils.error("Usage : /lg voter <joueur>")); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { player.sendMessage(MessageUtils.error("Joueur introuvable.")); return true; }
                plugin.getVoteManager().castVote(player, target);
            }

            case "color", "couleur" -> {
                if (plugin.getGameManager().getState() != GameState.RUNNING) {
                    player.sendMessage(MessageUtils.error("La partie n'est pas en cours."));
                    return true;
                }
                plugin.getColorGui().openPlayerList(player);
            }

            case "chat" -> {
                if (plugin.getGameManager().getState() != GameState.RUNNING) { player.sendMessage(MessageUtils.error("La partie n'est pas en cours.")); return true; }
                if (!plugin.getRoleManager().isWolf(player.getUniqueId())) { player.sendMessage(MessageUtils.error("Réservé aux loups.")); return true; }
                boolean opened = plugin.getChatManager().tryOpenWolfChat();
                if (!opened) player.sendMessage(MessageUtils.error("Le canal loup a déjà été utilisé cette nuit."));
            }

            case "roles" -> {
                boolean visible = plugin.getConfig().getBoolean("game.composition-visible", false);
                boolean isAdmin = plugin.getGameManager().isAdmin(player.getUniqueId());
                if (!visible && !isAdmin) {
                    player.sendMessage(MessageUtils.error("La composition est cachée."));
                    return true;
                }
                plugin.getRoleManager().getRoleCompositionSummary().forEach(player::sendMessage);
            }

            default -> {
                if (plugin.getGameManager().getState() != GameState.RUNNING) {
                    player.sendMessage(MessageUtils.error("La partie n'est pas en cours."));
                    return true;
                }
                var role = plugin.getRoleManager().getRole(player.getUniqueId());
                if (role == null) { player.sendMessage(MessageUtils.error("Tu n'as pas de rôle.")); return true; }
                if (!role.onPowerCommand(player, args)) {
                    player.sendMessage(MessageUtils.error("Commande de rôle inconnue : /lg " + args[0]));
                }
            }
        }
        return true;
    }
}
