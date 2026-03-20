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
 * Grand Méchant Loup (doc UHCWorld — sans aura/honneur).
 * - Strength I en PERMANENCE (jour ET nuit).
 * - Dès qu'un loup meurt : perd la Force le jour, la garde seulement la nuit.
 * - /lg hurler : 1× par partie.
 * - Tue un joueur → Speed I + 2♥ absorption pendant 1 minute.
 */
public class GrandMechantLoup extends Role {

    private boolean hurlementUsed = false;
    private boolean wolfHasDied    = false; // un loup est mort → mode nuit seulement

    public GrandMechantLoup(LoupGarouPlugin p, UUID u) { super(p, u); }

    @Override public String getDisplayName() { return "§4Grand Méchant Loup"; }
    @Override public String getDescription() {
        return "§7Force I permanente (nuit seule si un loup meurt). /lg hurler (1×/partie). Tue → Speed I + 2♥ abs.";
    }
    @Override public RoleFamily getFamily() { return RoleFamily.LOUP; }
    @Override public String getId()         { return "grand_mechant_loup"; }

    /** Appelé par RoleManager quand un loup meurt */
    public void onWolfDeath() {
        wolfHasDied = true;
    }

    @Override
    public void onNightTick(Player player) {
        // Toujours actif la nuit
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 0, false, false, false));
    }

    /** Appelé chaque tick de jour par NightListener (qui doit appeler onDayTick si on l'ajoute) */
    public void onDayTick(Player player) {
        if (!wolfHasDied) {
            // Pas encore de loup mort → Force permanente même le jour
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 0, false, false, false));
        }
        // Si un loup est mort → pas de Force le jour (on ne remet pas l'effet)
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
        String msg = "§c[Hurlement] §7Un loup est autour de §eX:" + zoneX + " Z:" + zoneZ + " §7(zone ~50 blocs)";
        plugin.getChatManager().sendToWolves(msg);
        plugin.getChatManager().tryOpenWolfChat();
        hurlementUsed = true;
        player.sendMessage(MessageUtils.success("Tu as hurlé — les loups savent où tu es."));
        return true;
    }
}

