package fr.enoe.loupgarou.roles.impl.loup;
import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import java.util.UUID;
public class LoupEndormi extends Role {
    public LoupEndormi(LoupGarouPlugin p, UUID u) { super(p, u); }
    @Override public String getDisplayName() { return "§cLoup Endormi"; }
    @Override public String getDescription()  { return "§7Reçoit la liste des loups seulement après 1h de jeu."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.LOUP; }
    @Override public String getId()           { return "loup_endormi"; }
    public boolean isAwake() { return plugin.getGameManager().getElapsedSeconds() >= 3600; }
}
