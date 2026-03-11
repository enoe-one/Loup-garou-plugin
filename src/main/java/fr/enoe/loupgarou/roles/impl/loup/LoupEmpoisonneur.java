package fr.enoe.loupgarou.roles.impl.loup;
import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.UUID;
public class LoupEmpoisonneur extends Role {
    private int poisonsLeft = 2;
    public LoupEmpoisonneur(LoupGarouPlugin p, UUID u) { super(p, u); }
    @Override public String getDisplayName() { return "§2Loup Empoisonneur"; }
    @Override public String getDescription()  { return "§72x dans la partie: /lg empoisonner <joueur> — fausse ses infos."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.LOUP; }
    @Override public String getId()           { return "loup_empoisonneur"; }
    @Override public boolean onPowerCommand(Player player, String[] args) {
        if (!args[0].equalsIgnoreCase("empoisonner")) return false;
        if (poisonsLeft <= 0) { player.sendMessage(MessageUtils.error("Plus de poison !")); return true; }
        if (args.length < 2)  { player.sendMessage(MessageUtils.error("Usage: /lg empoisonner <joueur>")); return true; }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null)   { player.sendMessage(MessageUtils.error("Joueur introuvable.")); return true; }
        plugin.getRoleManager().setPoisoned(target.getUniqueId(), true);
        poisonsLeft--;
        player.sendMessage(MessageUtils.success(target.getName() + " empoisonné !"));
        return true;
    }
}
