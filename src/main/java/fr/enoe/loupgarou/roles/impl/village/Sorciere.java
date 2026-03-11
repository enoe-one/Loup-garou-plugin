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

public class Sorciere extends Role {
    private boolean lifePotion  = true;
    private boolean deathPotion = true;

    public Sorciere(LoupGarouPlugin plugin, UUID uuid) { super(plugin, uuid); }
    @Override public String getDisplayName() { return "§5Sorcière"; }
    @Override public String getDescription()  { return "§7/lg vie — ressuscite. /lg mort <joueur> — retire 3 cœurs."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.VILLAGE; }
    @Override public String getId()           { return "sorciere"; }

    @Override
    public boolean onPowerCommand(Player player, String[] args) {
        if (args[0].equalsIgnoreCase("vie")) {
            if (!lifePotion) { player.sendMessage(MessageUtils.error("Potion de vie déjà utilisée !")); return true; }
            UUID lastDead = plugin.getRoleManager().getLastNightDead();
            if (lastDead == null) { player.sendMessage(MessageUtils.error("Personne n'est mort cette nuit.")); return true; }
            plugin.getRoleManager().revivePlayer(lastDead, player);
            lifePotion = false;
            player.sendMessage(MessageUtils.success("Joueur ressuscité !"));
            return true;
        }
        if (args[0].equalsIgnoreCase("mort")) {
            if (!deathPotion) { player.sendMessage(MessageUtils.error("Potion de mort déjà utilisée !")); return true; }
            if (args.length < 2) { player.sendMessage(MessageUtils.error("Usage: /lg mort <joueur>")); return true; }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null || !plugin.getGameManager().isAlive(target.getUniqueId())) { player.sendMessage(MessageUtils.error("Joueur introuvable.")); return true; }
            target.setHealth(Math.max(1.0, target.getHealth() - 6.0));
            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 6000, 0, false, false, true));
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,  6000, 0, false, false, true));
            deathPotion = false;
            player.sendMessage(MessageUtils.success("Potion de mort lancée sur §e" + target.getName()));
            return true;
        }
        return false;
    }

    public boolean hasLifePotion()  { return lifePotion; }
    public boolean hasDeathPotion() { return deathPotion; }
}
