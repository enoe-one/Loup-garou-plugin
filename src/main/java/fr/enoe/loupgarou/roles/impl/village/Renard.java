package fr.enoe.loupgarou.roles.impl.village;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Renard extends Role {
    private boolean enrhume = false;
    public Renard(LoupGarouPlugin plugin, UUID uuid) { super(plugin, uuid); }
    @Override public String getDisplayName() { return "§6Renard"; }
    @Override public String getDescription()  { return "§7/lg renard <joueur> — détecte si un loup est proche."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.VILLAGE; }
    @Override public String getId()           { return "renard"; }

    @Override
    public boolean onPowerCommand(Player player, String[] args) {
        if (!args[0].equalsIgnoreCase("renard")) return false;
        if (enrhume) { player.sendMessage(MessageUtils.error("Tu es enrhumé, ton flair est perdu !")); return true; }
        if (powerUsedThisEpisode) { player.sendMessage(MessageUtils.error("Déjà utilisé ce tour.")); return true; }
        if (args.length < 2) { player.sendMessage(MessageUtils.error("Usage: /lg renard <joueur>")); return true; }
        Player target = org.bukkit.Bukkit.getPlayer(args[1]);
        if (target == null) { player.sendMessage(MessageUtils.error("Joueur introuvable.")); return true; }
        List<UUID> alive = new ArrayList<>(plugin.getGameManager().getAlivePlayers());
        int idx = alive.indexOf(target.getUniqueId());
        List<UUID> toCheck = new ArrayList<>();
        toCheck.add(alive.get(idx));
        if (idx > 0) toCheck.add(alive.get(idx - 1));
        if (idx < alive.size() - 1) toCheck.add(alive.get(idx + 1));
        boolean hasLoup = toCheck.stream().anyMatch(u -> plugin.getRoleManager().isWolf(u));
        if (hasLoup) {
            player.sendMessage("§6[Renard] §eTu sens un loup parmi eux !");
        } else {
            player.sendMessage("§6[Renard] §7Aucun loup... Tu perds ton flair !");
            if (plugin.getConfig().getBoolean("game.fox-cold-permanent", true)) enrhume = true;
        }
        powerUsedThisEpisode = true;
        return true;
    }
}
