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

public class Salvateur extends Role {

    // Toutes les personnes déjà protégées au moins une fois — ne peut pas répéter
    private final Set<UUID> alreadyProtected = new HashSet<>();
    private UUID currentProtected = null;

    public Salvateur(LoupGarouPlugin plugin, UUID uuid) { super(plugin, uuid); }

    @Override public String getDisplayName() { return "§aSalvateur"; }
    @Override public String getDescription() {
        return "§7/lg proteger <joueur> — protège une personne par tour. Ne peut pas protéger deux fois la même personne.";
    }
    @Override public RoleFamily getFamily()  { return RoleFamily.VILLAGE; }
    @Override public String getId()          { return "salvateur"; }

    @Override
    public void onGameStart(Player player) {
        // Résistance I de base (villageois avec effet propre → pas de rési double)
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 0, false, false, false));
    }

    @Override
    public boolean onPowerCommand(Player player, String[] args) {
        if (!args[0].equalsIgnoreCase("proteger")) return false;

        if (powerUsedThisEpisode) {
            player.sendMessage(MessageUtils.error("Tu as déjà protégé quelqu'un ce tour."));
            return true;
        }
        if (args.length < 2) {
            player.sendMessage(MessageUtils.error("Usage: /lg proteger <joueur>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(MessageUtils.error("Joueur introuvable."));
            return true;
        }

        UUID targetUUID = target.getUniqueId();

        // Ne peut pas protéger deux fois la même personne (toute la partie)
        if (alreadyProtected.contains(targetUUID)) {
            player.sendMessage(MessageUtils.error("Tu ne peux pas protéger §e" + target.getName() + " §cune deuxième fois !"));
            return true;
        }

        // Enregistrer la protection
        alreadyProtected.add(targetUUID);
        currentProtected = targetUUID;
        plugin.getRoleManager().setSalvateurProtected(targetUUID);

        // Résistance II (amp 1 ≈ 40%) pendant 1 épisode (1200 ticks = 1 min)
        target.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 1200, 1, false, false, false));

        player.sendMessage(MessageUtils.success("§e" + target.getName() + " §aest protégé ce tour. (Ne pourra plus être reprotégé)"));
        powerUsedThisEpisode = true;
        return true;
    }

    @Override
    public void onEpisodeEnd(int ep) {
        super.onEpisodeEnd(ep);
        currentProtected = null;
        // alreadyProtected se conserve sur toute la partie
    }
}
