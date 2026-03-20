package fr.enoe.loupgarou.roles.impl.special;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

/**
 * Petit Bourgeois — transformation révolutionnaire du Maire.
 * Garde sa Résistance I existante + gagne Force I.
 */
public class PetitBourgois extends Role {

    public PetitBourgois(LoupGarouPlugin plugin, UUID playerUUID) {
        super(plugin, playerUUID);
    }

    @Override public String getDisplayName() { return "§6Petit Bourgeois"; }
    @Override public String getDescription()  { return "Noblesse en déclin — Force I ajoutée à sa résistance."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.VILLAGE; }
    @Override public String getId()           { return "petit_bourgois"; }

    public void applyTransformation(Player player) {
        // Force I (en plus de la résistance existante du Maire)
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0, false, false));

        player.sendMessage("");
        player.sendMessage("§6§m                                          ");
        player.sendMessage("§6§l      ✦ PETIT BOURGEOIS ✦");
        player.sendMessage("§6§m                                          ");
        player.sendMessage("");
        player.sendMessage("§fTu portais la couronne sans jamais la mériter.");
        player.sendMessage("§fLa Force t'est donnée — mais le peuple, lui, ne pardonne pas.");
        player.sendMessage("§7Force I ajoutée. La foule gronde à ta porte.");
        player.sendMessage("");
    }
}
