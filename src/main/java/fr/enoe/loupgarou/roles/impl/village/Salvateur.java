package fr.enoe.loupgarou.roles.impl.village;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.UUID;

public class Salvateur extends Role {
    private UUID lastProtected = null;
    public Salvateur(LoupGarouPlugin plugin, UUID uuid) { super(plugin, uuid); }
    @Override public String getDisplayName() { return "§aSalvateur"; }
    @Override public String getDescription()  { return "§7/lg proteger <joueur> — protège une personne par tour."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.VILLAGE; }
    @Override public String getId()           { return "salvateur"; }

    @Override
    public void onGameStart(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 0, false, false, true));
    }

    @Override
    public boolean onPowerCommand(Player player, String[] args) {
        if (!args[0].equalsIgnoreCase("proteger")) return false;
        if (powerUsedThisEpisode) { player.sendMessage(MessageUtils.error("Déjà utilisé ce tour.")); return true; }
        if (args.length < 2) { player.sendMessage(MessageUtils.error("Usage: /lg proteger <joueur>")); return true; }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) { player.sendMessage(MessageUtils.error("Joueur introuvable.")); return true; }
        if (target.getUniqueId().equals(lastProtected)) { player.sendMessage(MessageUtils.error("Ne peut pas protéger 2x la même personne !")); return true; }
        lastProtected = target.getUniqueId();
        plugin.getRoleManager().setSalvateurProtected(target.getUniqueId());
        target.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 1200, 1, false, false, false));
        target.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,   1200, 0, false, false, false));
        player.sendMessage(MessageUtils.success("§e" + target.getName() + " §aest protégé cette nuit."));
        powerUsedThisEpisode = true;
        return true;
    }
}
