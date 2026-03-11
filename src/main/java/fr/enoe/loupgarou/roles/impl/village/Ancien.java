package fr.enoe.loupgarou.roles.impl.village;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.entity.Player;
import java.util.UUID;

public class Ancien extends Role {
    private int resurrectionsLeft;

    public Ancien(LoupGarouPlugin plugin, UUID uuid) { super(plugin, uuid); }
    @Override public String getDisplayName() { return "§2Ancien"; }
    @Override public String getDescription()  { return "§7Ressuscite si tué par les loups (N fois). Si tué par le village, le tueur perd 2 cœurs."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.VILLAGE; }
    @Override public String getId()           { return "ancien"; }

    @Override
    public void onGameStart(Player player) {
        resurrectionsLeft = plugin.getConfig().getInt("game.elder-resurrections", 2);
    }

    public boolean tryResurrect(boolean killedByWolves, Player killer) {
        if (killedByWolves && resurrectionsLeft > 0) {
            resurrectionsLeft--;
            return true;
        }
        if (!killedByWolves && killer != null) {
            killer.setHealth(Math.max(1.0, killer.getHealth() - 4.0));
            killer.sendMessage(MessageUtils.error("Tu as tué l'Ancien ! -2 cœurs !"));
        }
        return false;
    }
}
