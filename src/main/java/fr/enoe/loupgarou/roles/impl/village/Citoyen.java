package fr.enoe.loupgarou.roles.impl.village;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.UUID;

/**
 * Citoyen — vote double + /lg enquete <joueur> : révèle si la cible a voté le même joueur
 * que lui au dernier vote (info sociale, pas de rôle).
 * Utilisable 2 fois par partie.
 */
public class Citoyen extends Role {

    private int enquetesRestantes = 2;
    private UUID lastVotedFor = null; // mis à jour par VoteManager

    public Citoyen(LoupGarouPlugin plugin, UUID uuid) { super(plugin, uuid); }

    @Override public String getDisplayName() { return "§eCitoyen"; }
    @Override public String getDescription() {
        return "§7Vote double. /lg enquete <joueur> — sait si la cible a voté comme toi au dernier vote. (2× par partie)";
    }
    @Override public RoleFamily getFamily()   { return RoleFamily.VILLAGE; }
    @Override public String getId()           { return "citoyen"; }

    public void setLastVotedFor(UUID uuid) { this.lastVotedFor = uuid; }

    @Override
    public boolean onPowerCommand(Player player, String[] args) {
        if (!args[0].equalsIgnoreCase("enquete")) return false;
        if (enquetesRestantes <= 0) {
            player.sendMessage(MessageUtils.error("Plus d'enquêtes disponibles !")); return true;
        }
        if (args.length < 2) {
            player.sendMessage(MessageUtils.error("Usage: /lg enquete <joueur>")); return true;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(MessageUtils.error("Joueur introuvable.")); return true;
        }
        if (lastVotedFor == null) {
            player.sendMessage(MessageUtils.error("Tu n'as pas encore voté.")); return true;
        }

        // Vérifier si la cible a voté pour le même joueur
        UUID targetVote = plugin.getVoteManager().getLastVoteOf(target.getUniqueId());
        boolean sameVote = lastVotedFor.equals(targetVote);

        enquetesRestantes--;
        player.sendMessage("§e[Citoyen] §7" + target.getName() + " a voté "
            + (sameVote ? "§aCOMME toi" : "§cDIFFÉREMMENT de toi")
            + " §7au dernier vote. (§e" + enquetesRestantes + " enquête(s) restante(s))");
        return true;
    }
}
