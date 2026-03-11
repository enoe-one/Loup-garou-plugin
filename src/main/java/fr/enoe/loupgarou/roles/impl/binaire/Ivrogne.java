package fr.enoe.loupgarou.roles.impl.binaire;
import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import org.bukkit.entity.Player;
import java.util.UUID;
public class Ivrogne extends Role {
    private Role fakeRole=null;
    public Ivrogne(LoupGarouPlugin p, UUID u) { super(p, u); }
    @Override public String getDisplayName() { return "§6Ivrogne"; }
    @Override public String getDescription()  { return "§7Reçoit un faux rôle. Gagne avec le village."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.VILLAGE; }
    @Override public String getId()           { return "ivrogne"; }
    @Override public boolean isBinary()       { return true; }
    @Override public void onGameStart(Player player) {
        fakeRole = plugin.getRoleManager().getRandomRoleForDisplay();
        player.sendMessage("§6[Ivrogne] §7Faux rôle : §b"+(fakeRole!=null?fakeRole.getDisplayName():"?"));
        player.sendMessage("§7Vrai objectif : §aGagner avec le Village !");
    }
    public Role getFakeRole() { return fakeRole; }
}
