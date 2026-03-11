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

public class MonteurDOurs extends Role {
    private long lastWolfPower = 0;
    public MonteurDOurs(LoupGarouPlugin plugin, UUID uuid) { super(plugin, uuid); }
    @Override public String getDisplayName() { return "§6Monteur d'Ours"; }
    @Override public String getDescription()  { return "§7Grogne le nb de LG proches. /lg loup : Speed 0.5 (recharge 40 min)."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.VILLAGE; }
    @Override public String getId()           { return "monteur_ours"; }

    @Override
    public void onEpisodeEnd(int episode) {
        super.onEpisodeEnd(episode);
        Player p = Bukkit.getPlayer(playerUUID);
        if (p == null) return;
        long wolves = p.getNearbyEntities(25, 25, 25).stream()
                .filter(e -> e instanceof Player)
                .filter(e -> plugin.getRoleManager().isWolf(((Player) e).getUniqueId()))
                .count();
        p.sendMessage("§6[Ours] §7" + "Grrr!".repeat((int) Math.max(1, wolves)) + " (" + wolves + " loup(s) proche(s))");
    }

    @Override
    public boolean onPowerCommand(Player player, String[] args) {
        if (!args[0].equalsIgnoreCase("loup")) return false;
        long now = plugin.getGameManager().getElapsedSeconds();
        if (now - lastWolfPower < 2400) { player.sendMessage(MessageUtils.error("Pouvoir en recharge !")); return true; }
        lastWolfPower = now;
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 6000, 0, false, false, true));
        player.sendMessage(MessageUtils.success("Speed activé pendant 5 minutes !"));
        return true;
    }
}
