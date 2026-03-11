package fr.enoe.loupgarou.roles.impl.solitaire;
import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import java.util.UUID;
public class LoupBlanc extends Role {
    public LoupBlanc(LoupGarouPlugin p, UUID u) { super(p, u); }
    @Override public String getDisplayName()         { return "§fLoup Blanc"; }
    @Override public String getDescription()          { return "§7Doit gagner seul. Camouflé parmi les loups. +4 cœurs."; }
    @Override public RoleFamily getFamily()           { return RoleFamily.SOLITAIRE; }
    @Override public RoleFamily getApparentFamily()   { return RoleFamily.LOUP; }
    @Override public String getId()                   { return "loup_blanc"; }
    @Override public void onGameStart(Player player) {
        var attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr != null) attr.setBaseValue(attr.getValue() + 8);
    }
}
