package fr.enoe.loupgarou.roles.impl.loup;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.UUID;

/**
 * Vilain Petit Loup — légèrement plus faible que le Loup simple.
 * Strength I + Speed I la nuit à 100% uptime (sans clignotement).
 * Différenciation : le LoupSimple n'a que Strength I (pas Speed).
 */
public class VilainPetitLoup extends Role {

    private boolean hurlementUsed = false;

    public VilainPetitLoup(LoupGarouPlugin p, UUID u) { super(p, u); }

    @Override public String getDisplayName() { return "§cVilain Petit Loup"; }
    @Override public String getDescription() {
        return "§7Strength I + Speed I la nuit. /lg hurler (1×/partie). Tue → Speed I + 2♥ abs 1 min.";
    }
    @Override public RoleFamily getFamily() { return RoleFamily.LOUP; }
    @Override public String getId()         { return "vilain_petit_loup"; }

    @Override
    public void onNightTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 0, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,    60, 0, false, false, false));
    }

    @Override
    public void onPlayerKill(Player killer, UUID victimUUID) {
        killer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,      1200, 0, false, false, false));
        killer.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,  1200, 0, false, false, true));
        killer.sendMessage(MessageUtils.success("Tu as tué — Speed I + 2♥ absorption pour 1 minute !"));
    }

    @Override
    public boolean onPowerCommand(Player player, String[] args) {
        if (!args[0].equalsIgnoreCase("hurler")) return false;
        if (hurlementUsed) {
            player.sendMessage(MessageUtils.error("Tu as déjà hurlé cette partie.")); return true;
        }
        int zoneX = (int) Math.round(player.getLocation().getX() / 50) * 50;
        int zoneZ = (int) Math.round(player.getLocation().getZ() / 50) * 50;
        plugin.getChatManager().sendToWolves("§c[Hurlement] §7Un loup est autour de §eX:" + zoneX + " Z:" + zoneZ);
        plugin.getChatManager().tryOpenWolfChat();
        hurlementUsed = true;
        player.sendMessage(MessageUtils.success("Tu as hurlé."));
        return true;
    }
}
