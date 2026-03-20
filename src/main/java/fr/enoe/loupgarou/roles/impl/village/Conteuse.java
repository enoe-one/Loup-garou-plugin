package fr.enoe.loupgarou.roles.impl.village;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.UUID;

/**
 * Conteuse — reçoit les pseudos des joueurs ayant agi la nuit.
 * Nouveau : /lg histoire <joueur> — invente une "histoire" sur ce joueur.
 * En réalité, révèle si ce joueur a AGI cette nuit (oui/non).
 * Utilisable 1× par épisode.
 */
public class Conteuse extends Role {

    public Conteuse(LoupGarouPlugin plugin, UUID uuid) { super(plugin, uuid); }

    @Override public String getDisplayName() { return "§dConteuse"; }
    @Override public String getDescription() {
        return "§7Reçoit les acteurs de la nuit. /lg histoire <joueur> — a-t-il agi cette nuit ? (1× par épisode)";
    }
    @Override public RoleFamily getFamily()  { return RoleFamily.VILLAGE; }
    @Override public String getId()          { return "conteuse"; }

    @Override
    public boolean onPowerCommand(Player player, String[] args) {
        if (!args[0].equalsIgnoreCase("histoire")) return false;
        if (powerUsedThisEpisode) {
            player.sendMessage(MessageUtils.error("Déjà utilisé ce tour.")); return true;
        }
        if (args.length < 2) {
            player.sendMessage(MessageUtils.error("Usage: /lg histoire <joueur>")); return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(MessageUtils.error("Joueur introuvable.")); return true;
        }

        boolean acted = plugin.getRoleManager().hasActedThisNight(target.getUniqueId());
        powerUsedThisEpisode = true;

        player.sendMessage("§d[Conteuse] §7Ton histoire sur §e" + target.getName() + " §7:");
        if (acted) {
            player.sendMessage("§7§o\"Cette nuit... §e" + target.getName() + "§7§o a fait quelque chose.\"");
            player.sendMessage("§8(Ce joueur a utilisé une action cette nuit)");
        } else {
            player.sendMessage("§7§o\"" + target.getName() + "§7§o a dormi paisiblement.\"");
            player.sendMessage("§8(Ce joueur n'a pas utilisé d'action cette nuit)");
        }
        return true;
    }
}
