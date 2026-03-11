package fr.enoe.loupgarou.commands;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class AdminCommand implements CommandExecutor {

    private final LoupGarouPlugin plugin;

    public AdminCommand(LoupGarouPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Joueur uniquement."); return true; }
        if (!plugin.getGameManager().isAdmin(player.getUniqueId())) {
            player.sendMessage(MessageUtils.error("Tu n'es pas admin de cette partie."));
            return true;
        }
        if (args.length == 0) {
            player.sendMessage("§e/lgadmin setting §7— Paramètres de jeu");
            player.sendMessage("§e/lgadmin reviv <joueur> §7— Ressusciter un joueur");
            player.sendMessage("§e/lgadmin info <message> §7— Message dans le chat");
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "setting" -> openSettings(player, args);

            case "reviv" -> {
                if (args.length < 2) { player.sendMessage(MessageUtils.error("Usage : /lgadmin reviv <joueur>")); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { player.sendMessage(MessageUtils.error("Joueur introuvable.")); return true; }
                plugin.getRoleManager().revivePlayer(target.getUniqueId(), player);
                player.sendMessage(MessageUtils.success(target.getName() + " ressuscité !"));
            }

            case "info" -> {
                if (args.length < 2) { player.sendMessage(MessageUtils.error("Usage : /lgadmin info <message>")); return true; }
                String msg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                MessageUtils.broadcast("§6[INFO] §f" + msg);
            }

            default -> player.sendMessage(MessageUtils.error("Commande admin inconnue."));
        }
        return true;
    }

    private void openSettings(Player player, String[] args) {
        if (args.length >= 3) {
            // /lgadmin setting <clé> <valeur>
            String key   = args[1];
            String value = args[2];
            switch (key.toLowerCase()) {
                case "wolves"      -> plugin.getConfig().set("roles.wolves", Integer.parseInt(value));
                case "cycle"       -> plugin.getConfig().set("game.day-night-cycle", Boolean.parseBoolean(value));
                case "composition" -> plugin.getConfig().set("game.composition-visible", Boolean.parseBoolean(value));
                case "trouple"     -> plugin.getConfig().set("game.trouple", Boolean.parseBoolean(value));
                case "mystery"     -> plugin.getConfig().set("game.mystery-event", Boolean.parseBoolean(value));
                case "couple"      -> plugin.getConfig().set("game.random-couple", Boolean.parseBoolean(value));
                default            -> {
                    player.sendMessage(MessageUtils.error("Paramètre inconnu : " + key));
                    return;
                }
            }
            plugin.saveConfig();
            player.sendMessage(MessageUtils.success("Paramètre §e" + key + " §adéfini à §e" + value));
            return;
        }

        if (args.length == 4 && args[1].equalsIgnoreCase("role")) {
            // /lgadmin setting role <id> <true/false>
            String roleId  = args[2];
            boolean enable = Boolean.parseBoolean(args[3]);
            java.util.List<String> enabled = new java.util.ArrayList<>(plugin.getConfig().getStringList("roles.enabled"));
            if (enable) { if (!enabled.contains(roleId)) enabled.add(roleId); }
            else         enabled.remove(roleId);
            plugin.getConfig().set("roles.enabled", enabled);
            plugin.saveConfig();
            player.sendMessage(MessageUtils.success("Rôle §e" + roleId + (enable ? " §aactivé." : " §cdésactivé.")));
            return;
        }

        // Afficher l'aide
        player.sendMessage("§6═══ Paramètres de Partie ═══");
        player.sendMessage("§e/lgadmin setting wolves <n> §7— Nombre de loups");
        player.sendMessage("§e/lgadmin setting cycle <true/false> §7— Cycle jour/nuit");
        player.sendMessage("§e/lgadmin setting composition <true/false> §7— Composition visible");
        player.sendMessage("§e/lgadmin setting trouple <true/false> §7— Trouple");
        player.sendMessage("§e/lgadmin setting mystery <true/false> §7— Événement mystère");
        player.sendMessage("§e/lgadmin setting couple <true/false> §7— Couple aléatoire");
        player.sendMessage("§e/lgadmin setting role <id> <true/false> §7— Activer/désactiver un rôle");
        player.sendMessage("§6════════════════════════════");
    }
}
