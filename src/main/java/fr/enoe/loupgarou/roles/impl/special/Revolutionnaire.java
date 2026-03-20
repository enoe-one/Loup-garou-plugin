package fr.enoe.loupgarou.roles.impl.special;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

/**
 * Révolutionnaire — transformation révolutionnaire du Simple Villageois.
 * Gagne Force I mais perd sa Résistance (si elle existait).
 */
public class Revolutionnaire extends Role {

    public Revolutionnaire(LoupGarouPlugin plugin, UUID playerUUID) {
        super(plugin, playerUUID);
    }

    @Override public String getDisplayName() { return "§cRévolutionnaire"; }
    @Override public String getDescription()  { return "Le peuple en armes — Force I mais Résistance retirée."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.VILLAGE; }
    @Override public String getId()           { return "revolutionnaire"; }

    public void applyTransformation(Player player) {
        // Retirer la résistance si présente
        player.removePotionEffect(PotionEffectType.RESISTANCE);

        // Ajouter Force I
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0, false, false));

        player.sendMessage("");
        player.sendMessage("§c§m                                          ");
        player.sendMessage("§c§l      ✦ RÉVOLUTIONNAIRE ✦");
        player.sendMessage("§c§m                                          ");
        player.sendMessage("");
        player.sendMessage("§fTu n'as plus de chaînes — tu n'as plus rien à perdre.");
        player.sendMessage("§fLa peur est morte ce soir. La Force est ton héritage.");
        player.sendMessage("§fLa résistance s'est envolée — mais ta rage, elle, demeure.");
        player.sendMessage("§7Force I. Résistance retirée. En avant, citoyen.");
        player.sendMessage("");
    }
}
