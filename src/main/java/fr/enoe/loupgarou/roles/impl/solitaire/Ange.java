package fr.enoe.loupgarou.roles.impl.solitaire;
import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;
public class Ange extends Role {
    private boolean isGuardian=false, isFallen=false, killedTarget=false;
    private UUID assignedTarget=null, targetToKill=null;
    public Ange(LoupGarouPlugin p, UUID u) { super(p, u); }
    @Override public String getDisplayName() { return isGuardian ? "§eAnge Gardien" : "§8Ange Déchu"; }
    @Override public String getDescription()  { return "§7/lg gardien ou /lg dechu au début."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.SOLITAIRE; }
    @Override public String getId()           { return "ange"; }
    @Override public boolean onPowerCommand(Player player, String[] args) {
        if (isGuardian || isFallen) { player.sendMessage(MessageUtils.error("Déjà choisi !")); return true; }
        if (args[0].equalsIgnoreCase("gardien")) {
            isGuardian = true;
            List<UUID> alive = new ArrayList<>(plugin.getGameManager().getAlivePlayers());
            alive.remove(playerUUID);
            if (!alive.isEmpty()) assignedTarget = alive.get(new Random().nextInt(alive.size()));
            var attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attr != null) attr.setBaseValue(26);
            player.sendMessage(MessageUtils.success("Tu es Ange Gardien ! Protège §e" + (assignedTarget!=null ? Bukkit.getOfflinePlayer(assignedTarget).getName() : "?")));
            return true;
        }
        if (args[0].equalsIgnoreCase("dechu")) {
            isFallen = true;
            List<UUID> alive = new ArrayList<>(plugin.getGameManager().getAlivePlayers());
            alive.remove(playerUUID);
            if (!alive.isEmpty()) targetToKill = alive.get(new Random().nextInt(alive.size()));
            var attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attr != null) attr.setBaseValue(26);
            player.sendMessage(MessageUtils.success("Tu es Ange Déchu ! Élimine §e" + (targetToKill!=null ? Bukkit.getOfflinePlayer(targetToKill).getName() : "?")));
            return true;
        }
        return false;
    }
    public void onPlayerKilled(UUID killed) {
        if (isFallen && !killedTarget && killed.equals(targetToKill)) {
            killedTarget = true;
            Player p = Bukkit.getPlayer(playerUUID);
            if (p != null) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0, false, false, true));
                p.sendMessage(MessageUtils.success("Cible éliminée ! Force permanente !"));
            }
        }
    }
}
