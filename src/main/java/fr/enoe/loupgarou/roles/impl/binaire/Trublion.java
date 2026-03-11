package fr.enoe.loupgarou.roles.impl.binaire;
import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.UUID;
public class Trublion extends Role {
    private boolean used=false;
    private RoleFamily family;
    public Trublion(LoupGarouPlugin p, UUID u) { super(p, u); }
    @Override public String getDisplayName() { return "§5Trublion"; }
    @Override public String getDescription()  { return "§7Entre 20-25 min: /lg echanger <j1> <j2>."; }
    @Override public RoleFamily getFamily()   { return family; }
    @Override public String getId()           { return "trublion"; }
    @Override public boolean isBinary()       { return true; }
    @Override public void onGameStart(Player player) { family = plugin.getConfig().getBoolean("game.trublion-solitaire",false) ? RoleFamily.SOLITAIRE : RoleFamily.VILLAGE; }
    @Override public boolean onPowerCommand(Player player, String[] args) {
        if (!args[0].equalsIgnoreCase("echanger")) return false;
        int elapsed = plugin.getGameManager().getElapsedSeconds();
        if (elapsed<1200||elapsed>1500) { player.sendMessage(MessageUtils.error("Seulement entre 20 et 25 minutes !")); return true; }
        if (used) { player.sendMessage(MessageUtils.error("Déjà utilisé !")); return true; }
        if (args.length<3) { player.sendMessage(MessageUtils.error("Usage: /lg echanger <j1> <j2>")); return true; }
        Player p1=Bukkit.getPlayer(args[1]), p2=Bukkit.getPlayer(args[2]);
        if (p1==null||p2==null) { player.sendMessage(MessageUtils.error("Joueur(s) introuvable(s).")); return true; }
        plugin.getRoleManager().swapRoles(p1.getUniqueId(), p2.getUniqueId());
        used=true;
        player.sendMessage(MessageUtils.success("Rôles échangés !"));
        return true;
    }
}
