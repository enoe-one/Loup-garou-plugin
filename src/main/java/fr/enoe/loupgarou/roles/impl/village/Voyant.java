package fr.enoe.loupgarou.roles.impl.village;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.UUID;

public class Voyant extends Role {
    public Voyant(LoupGarouPlugin plugin, UUID uuid) { super(plugin, uuid); }
    @Override public String getDisplayName() { return "§bVoyant"; }
    @Override public String getDescription()  { return "§7/lg voir <joueur> — une fois par épisode."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.VILLAGE; }
    @Override public String getId()           { return "voyant"; }
    @Override public boolean onPowerCommand(Player player, String[] args) {
        if (!args[0].equalsIgnoreCase("voir")) return false;
        if (powerUsedThisEpisode) { player.sendMessage(MessageUtils.error("Déjà utilisé.")); return true; }
        if (args.length < 2) { player.sendMessage(MessageUtils.error("Usage: /lg voir <joueur>")); return true; }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !plugin.getGameManager().isAlive(target.getUniqueId())) { player.sendMessage(MessageUtils.error("Joueur introuvable.")); return true; }
        Role role = plugin.getRoleManager().getRole(target.getUniqueId());
        if (role == null) return true;
        player.sendMessage("§b[Voyant] §e" + target.getName() + " §7→ §b" + role.getDisplayName());
        powerUsedThisEpisode = true;
        if (plugin.getConfig().getBoolean("game.roles.voyante-bavarde", false))
            MessageUtils.broadcast("§b[Voyant] §e" + target.getName() + " §7→ §b" + role.getDisplayName());
        return true;
    }
}
