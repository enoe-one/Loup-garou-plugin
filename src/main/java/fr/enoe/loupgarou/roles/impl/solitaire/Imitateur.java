package fr.enoe.loupgarou.roles.impl.solitaire;
import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.UUID;
public class Imitateur extends Role {
    public Imitateur(LoupGarouPlugin p, UUID u) { super(p, u); }
    @Override public String getDisplayName() { return "§3Imitateur"; }
    @Override public String getDescription()  { return "§7/lg imiter <joueur> — copie son rôle une nuit."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.SOLITAIRE; }
    @Override public String getId()           { return "imitateur"; }
    @Override public boolean onPowerCommand(Player player, String[] args) {
        if (!args[0].equalsIgnoreCase("imiter")) return false;
        if (powerUsedThisEpisode) { player.sendMessage(MessageUtils.error("Déjà utilisé.")); return true; }
        if (args.length < 2) { player.sendMessage(MessageUtils.error("Usage: /lg imiter <joueur>")); return true; }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) { player.sendMessage(MessageUtils.error("Joueur introuvable.")); return true; }
        Role role = plugin.getRoleManager().getRole(target.getUniqueId());
        if (role == null) return true;
        player.sendMessage("§3[Imitateur] §7Tu imites §e" + target.getName() + " §7(§b" + role.getDisplayName() + "§7)");
        if (role.getFamily() == RoleFamily.LOUP)
            plugin.getRoleManager().getWolfList().forEach(w -> { Player ww = Bukkit.getPlayer(w); if (ww!=null) player.sendMessage("§c[Loup] §e"+ww.getName()); });
        powerUsedThisEpisode = true;
        return true;
    }
}
