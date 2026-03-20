package fr.enoe.loupgarou.roles.impl.village;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import java.util.UUID;

public class SimpleVillagois extends Role {
    public SimpleVillagois(LoupGarouPlugin plugin, UUID uuid) { super(plugin, uuid); }
    @Override public String getDisplayName() { return "§fSimple Villageois"; }
    @Override public String getDescription() { return "§7Aucun pouvoir. Ton arme : le vote !"; }
    @Override public RoleFamily getFamily()  { return RoleFamily.VILLAGE; }
    @Override public String getId()          { return "simple_villagois"; }
}
