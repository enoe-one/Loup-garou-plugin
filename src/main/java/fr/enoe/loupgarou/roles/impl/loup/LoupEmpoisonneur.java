package fr.enoe.loupgarou.roles.impl.loup;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.UUID;

public class LoupEmpoisonneur extends Role {
    public LoupEmpoisonneur(LoupGarouPlugin p, UUID u) { super(p, u); }
    @Override public String getDisplayName() { return "§2Loup Empoisonneur"; }
    @Override public String getDescription() { return "§7Force 30% la nuit. /lg empoisonner <joueur> — empoisonne une cible."; }
    @Override public RoleFamily getFamily()  { return RoleFamily.LOUP; }
    @Override public String getId()          { return "loup_empoisonneur"; }

    @Override
    public void onNightTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 0, false, false, false));
    }

    @Override
    public boolean onPowerCommand(Player player, String[] args) {
        if (!args[0].equalsIgnoreCase("empoisonner")) return false;
        if (powerUsedThisEpisode) { player.sendMessage(MessageUtils.error("Déjà utilisé ce tour.")); return true; }
        if (args.length < 2) { player.sendMessage(MessageUtils.error("Usage: /lg empoisonner <joueur>")); return true; }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) { player.sendMessage(MessageUtils.error("Joueur introuvable.")); return true; }
        plugin.getRoleManager().setPoisoned(target.getUniqueId(), true);
        target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 6000, 0, false, false, false));
        player.sendMessage(MessageUtils.success("§e" + target.getName() + " §aest empoisonné !"));
        powerUsedThisEpisode = true;
        return true;
    }
}
