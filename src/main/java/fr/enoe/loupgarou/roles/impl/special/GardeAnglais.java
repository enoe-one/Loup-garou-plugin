package fr.enoe.loupgarou.roles.impl.special;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Garde Anglais — transformation révolutionnaire du LoupSimple.
 * Connaît la position ET l'identité de George III et Napoléon.
 */
public class GardeAnglais extends Role {

    public GardeAnglais(LoupGarouPlugin plugin, UUID playerUUID) {
        super(plugin, playerUUID);
    }

    @Override public String getDisplayName() { return "§4Garde Anglais"; }
    @Override public String getDescription()  { return "Protecteur du roi — connaît George III et Napoléon."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.LOUP; }
    @Override public String getId()           { return "garde_anglais"; }

    public void applyTransformation(Player player) {
        player.sendMessage("");
        player.sendMessage("§4§m                                          ");
        player.sendMessage("§4§l      ✦ GARDE ANGLAIS ✦");
        player.sendMessage("§4§m                                          ");
        player.sendMessage("");
        player.sendMessage("§fTu sers le roi. Tu connais ses ennemis.");
        player.sendMessage("§fL'ombre t'a tout révélé — use-en bien.");
        player.sendMessage("§7Tes alliés royaux :");

        // Révéler George III
        plugin.getGameManager().getAlivePlayers().forEach(uuid -> {
            Role r = plugin.getRoleManager().getRole(uuid);
            if (r == null) return;
            Player target = Bukkit.getPlayer(uuid);
            if (target == null) return;

            if (r.getId().equals("george_iii")) {
                player.sendMessage("§4👑 George III : §e" + target.getName()
                    + " §7(" + target.getLocation().getBlockX()
                    + ", " + target.getLocation().getBlockY()
                    + ", " + target.getLocation().getBlockZ() + ")");
            }
            if (r.getId().equals("napoleon")) {
                player.sendMessage("§b⚔ Napoléon : §e" + target.getName()
                    + " §7(" + target.getLocation().getBlockX()
                    + ", " + target.getLocation().getBlockY()
                    + ", " + target.getLocation().getBlockZ() + ")");
            }
        });
    }
}
