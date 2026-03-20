package fr.enoe.loupgarou.roles.impl.binaire;
import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.UUID;
public class Voleur extends Role {
    private Role stolenRole=null;
    public Voleur(LoupGarouPlugin p, UUID u) { super(p, u); }
    @Override public String getDisplayName() { return stolenRole!=null ? stolenRole.getDisplayName()+" §7(Voleur)" : "§7Voleur"; }
    @Override public String getDescription()  { return "§7Devient le rôle du premier joueur tué."; }
    @Override public RoleFamily getFamily()   { return stolenRole!=null ? stolenRole.getFamily() : RoleFamily.BINAIRE; }
    @Override public String getId()           { return "voleur"; }
    @Override public boolean isBinary()       { return true; }
    public void onKill(UUID victim) {
        if (stolenRole!=null) return;
        Role r = plugin.getRoleManager().getRole(victim);
        if (r==null||r.getId().equals("ancien")) return;
        stolenRole=r;
        Player p = Bukkit.getPlayer(playerUUID);
        if (p!=null) p.sendMessage("§7[Voleur] §7Tu es maintenant : §b"+stolenRole.getDisplayName());
    }
}
