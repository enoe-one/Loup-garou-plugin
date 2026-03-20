package fr.enoe.loupgarou.commands;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OwnerCommand implements CommandExecutor {

    private final LoupGarouPlugin plugin;

    public OwnerCommand(LoupGarouPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Joueur uniquement."); return true; }
        if (!plugin.getGameManager().isOwner(player.getUniqueId())) {
            player.sendMessage(MessageUtils.error("Seul l'owner peut utiliser cette commande."));
            return true;
        }
        if (args.length < 2) {
            player.sendMessage("§e/lgowner admin <joueur> §7— Ajouter un admin");
            player.sendMessage("§e/lgowner deadmin <joueur> §7— Retirer un admin");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "admin" -> {
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { player.sendMessage(MessageUtils.error("Joueur introuvable.")); return true; }
                plugin.getGameManager().addAdmin(target.getUniqueId());
                player.sendMessage(MessageUtils.success(target.getName() + " est maintenant admin."));
                target.sendMessage(MessageUtils.info("Tu es maintenant admin de la partie."));
            }
            case "deadmin" -> {
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { player.sendMessage(MessageUtils.error("Joueur introuvable.")); return true; }
                plugin.getGameManager().removeAdmin(target.getUniqueId());
                player.sendMessage(MessageUtils.success(target.getName() + " n'est plus admin."));
            }
            default -> player.sendMessage(MessageUtils.error("Commande owner inconnue."));
        }
        return true;
    }
}
