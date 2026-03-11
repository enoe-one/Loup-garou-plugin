package fr.enoe.loupgarou.roles.impl.village;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import java.util.UUID;

public class IdiotDuVillage extends Role {
    private boolean usedRevive = false;
    public IdiotDuVillage(LoupGarouPlugin plugin, UUID uuid) { super(plugin, uuid); }
    @Override public String getDisplayName() { return "§eIdiot du Village"; }
    @Override public String getDescription()  { return "§7Si éliminé par le village, ressuscite une première fois."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.VILLAGE; }
    @Override public String getId()           { return "idiot"; }

    public boolean tryRevive(boolean killedByVillage) {
        if (killedByVillage && !usedRevive) { usedRevive = true; return true; }
        return false;
    }
}
