package fr.enoe.loupgarou.roles.impl.village;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import java.util.UUID;

public class Conteuse extends Role {
    public Conteuse(LoupGarouPlugin plugin, UUID uuid) { super(plugin, uuid); }
    @Override public String getDisplayName() { return "§dConteuse"; }
    @Override public String getDescription()  { return "§7Reçoit les pseudos des joueurs ayant agi la nuit."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.VILLAGE; }
    @Override public String getId()           { return "conteuse"; }
}
