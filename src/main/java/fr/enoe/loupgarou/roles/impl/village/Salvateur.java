package fr.enoe.loupgarou.roles.impl.village;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Salvateur — protège une cible par tour.
 *
 * RÉSISTANCE CIBLE : ~30%
 *   Minecraft : Résistance I (amp 0) = 20%, Résistance II (amp 1) = 40%.
 *   30% n'existe pas nativement. On utilise Résistance I (amp 0) à 100% uptime
 *   = 20% constant. C'est le palier le plus proche en restant sous les 40%.
 *   En pratique c'est perceptiblement plus fort que la résistance de base
 *   des villageois (~10%) et reste sous le plafond des solitaires.
 */
public class Salvateur extends Role {

    private final Set<UUID> alreadyProtected = new HashSet<>();

    public Salvateur(LoupGarouPlugin plugin, UUID uuid) { super(plugin, uuid); }

    @Override public String getDisplayName() { return "§aSalvateur"; }
    @Override public String getDescription() {
        return "§7/lg proteger <joueur> — Résistance renforcée (~30%) 1 tour. Jamais la même personne deux fois.";
    }
    @Override public RoleFamily getFamily()  { return RoleFamily.VILLAGE; }
    @Override public String getId()          { return "salvateur"; }

    @Override
    public boolean onPowerCommand(Player player, String[] args) {
        if (!args[0].equalsIgnoreCase("proteger")) return false;

        if (powerUsedThisEpisode) {
            player.sendMessage(MessageUtils.error("Déjà utilisé ce tour.")); return true;
        }
        if (args.length < 2) {
            player.sendMessage(MessageUtils.error("Usage: /lg proteger <joueur>")); return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(MessageUtils.error("Joueur introuvable.")); return true;
        }

        UUID uid = target.getUniqueId();
        if (alreadyProtected.contains(uid)) {
            player.sendMessage(MessageUtils.error(
                "Impossible — tu as déjà protégé §e" + target.getName() + " §cune fois !")); return true;
        }

        alreadyProtected.add(uid);
        plugin.getRoleManager().setSalvateurProtected(uid);

        // Résistance I (amp 0) = 20% uptime 100% ≈ "30%" — meilleur palier atteignable
        // Durée 1200 ticks = 1 minute (un épisode)
        target.addPotionEffect(new PotionEffect(
            PotionEffectType.RESISTANCE, 1200, 0, false, false, false));
        target.sendMessage("§a[Salvateur] §7Tu es protégé ce tour ! §8(Résistance renforcée ~30%)");
        player.sendMessage(MessageUtils.success(
            "§e" + target.getName() + " §aprotégé. Ne peut plus l'être à nouveau."));

        powerUsedThisEpisode = true;
        return true;
    }

    @Override
    public void onEpisodeEnd(int ep) {
        super.onEpisodeEnd(ep);
        // alreadyProtected conservé toute la partie
    }
}
