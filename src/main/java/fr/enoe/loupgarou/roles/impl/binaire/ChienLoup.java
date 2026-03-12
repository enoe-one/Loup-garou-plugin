package fr.enoe.loupgarou.roles.impl.binaire;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.UUID;

public class ChienLoup extends Role {

    private boolean choseLoup = false;
    private boolean chose      = false;

    public ChienLoup(LoupGarouPlugin p, UUID u) { super(p, u); }

    @Override public String getDisplayName() {
        return chose ? (choseLoup ? "§cChien-Loup (Loup)" : "§aChien-Loup (Village)") : "§7Chien-Loup";
    }
    @Override public String getDescription() {
        return "§7/lg loup : Force 20% la nuit. /lg village : Force 20% permanente.";
    }
    @Override public RoleFamily getFamily() {
        return !chose ? RoleFamily.BINAIRE : choseLoup ? RoleFamily.LOUP : RoleFamily.VILLAGE;
    }
    @Override public String getId()       { return "chien_loup"; }
    @Override public boolean isBinary()   { return true; }

    @Override
    public boolean onPowerCommand(Player player, String[] args) {
        if (chose) { player.sendMessage(MessageUtils.error("Tu as déjà choisi ton camp !")); return true; }

        if (args[0].equalsIgnoreCase("loup")) {
            chose     = true;
            choseLoup = true;
            plugin.getRoleManager().addToWolfTeam(playerUUID);
            // Loup : Force I (≈30%) seulement la nuit — géré dans onNightTick
            player.sendMessage(MessageUtils.success("Tu rejoins les Loups-Garous ! Force 20% la nuit."));
            return true;
        }

        if (args[0].equalsIgnoreCase("village")) {
            chose     = true;
            choseLoup = false;
            // Village : Force I permanente (≈30%)
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0, false, false, false));
            player.sendMessage(MessageUtils.success("Tu restes au Village ! Force 20% permanente."));
            return true;
        }

        return false;
    }

    @Override
    public void onNightTick(Player player) {
        if (!chose || !choseLoup) return;
        // Loup : Force I (amp 0 ≈ 30%) seulement la nuit
        boolean isNight = isNightTime(player);
        if (isNight) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 0, false, false, false));
        } else {
            player.removePotionEffect(PotionEffectType.STRENGTH);
        }
    }

    private boolean isNightTime(Player p) {
        long time = p.getWorld().getTime();
        return time >= 13000 && time <= 23000;
    }
}
