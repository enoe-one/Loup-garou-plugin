package fr.enoe.loupgarou.roles.impl.solitaire;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.UUID;

/**
 * Loup Blanc — solitaire camouflé parmi les loups.
 * Nouveau : /lg trahir <joueur> — révèle le rôle d'un loup à tout le monde
 * (trahison d'un allié apparent). Utilisable 1× par partie. Le Loup Blanc
 * ne meurt pas si les loups perdent — il doit gagner SEUL.
 */
public class LoupBlanc extends Role {

    private boolean trahisonUsed = false;

    public LoupBlanc(LoupGarouPlugin p, UUID u) { super(p, u); }

    @Override public String getDisplayName()       { return "§fLoup Blanc"; }
    @Override public String getDescription()       {
        return "§7Solitaire camouflé parmi les loups. +4 cœurs. /lg trahir <joueur> — révèle son rôle à tous. (1× par partie)";
    }
    @Override public RoleFamily getFamily()        { return RoleFamily.SOLITAIRE; }
    @Override public RoleFamily getApparentFamily(){ return RoleFamily.LOUP; }
    @Override public String getId()                { return "loup_blanc"; }

    @Override
    public void onGameStart(Player player) {
        var attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr != null) attr.setBaseValue(attr.getValue() + 8); // +4 cœurs
    }

    @Override
    public boolean onPowerCommand(Player player, String[] args) {
        if (!args[0].equalsIgnoreCase("trahir")) return false;
        if (trahisonUsed) {
            player.sendMessage(MessageUtils.error("Trahison déjà utilisée !")); return true;
        }
        if (args.length < 2) {
            player.sendMessage(MessageUtils.error("Usage: /lg trahir <joueur>")); return true;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(MessageUtils.error("Joueur introuvable.")); return true;
        }

        var role = plugin.getRoleManager().getRole(target.getUniqueId());
        if (role == null) {
            player.sendMessage(MessageUtils.error("Ce joueur n'a pas de rôle.")); return true;
        }

        trahisonUsed = true;
        // Broadcast à tous — révélation publique
        plugin.getGameManager().getAlivePlayers().forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.sendMessage("§f§l[TRAHISON] §7Le rôle de §e" + target.getName()
                + " §7est révélé : " + role.getDisplayName());
        });
        player.sendMessage(MessageUtils.success("Tu as trahi §e" + target.getName() + " §a!"));
        return true;
    }
}
