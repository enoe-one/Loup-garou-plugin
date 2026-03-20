package fr.enoe.loupgarou.roles.impl.loup;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.UUID;

/**
 * Loup-Garou Hurleur (doc UHCWorld).
 * - Strength I la nuit.
 * - /lg hurler : 3× par partie (au lieu de 1× pour les autres loups).
 * - À chaque hurlement : lui + tous les loups online → Regeneration II pendant 7 secondes.
 * - Tue un joueur → Speed I + 2♥ absorption pendant 1 minute.
 */
public class LoupHurleur extends Role {

    private static final int MAX_HURLEMENTS = 3;
    private int hurlementCount = 0;

    public LoupHurleur(LoupGarouPlugin p, UUID u) { super(p, u); }

    @Override public String getDisplayName() { return "§cLoup-Garou Hurleur"; }
    @Override public String getDescription() {
        return "§7Force I la nuit. /lg hurler (3×/partie) → Régén II 7s à tous les loups. Tue → Speed I + 2♥ abs.";
    }
    @Override public RoleFamily getFamily() { return RoleFamily.LOUP; }
    @Override public String getId()         { return "loup_hurleur"; }

    @Override
    public void onNightTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 0, false, false, false));
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
        if (hurlementCount >= MAX_HURLEMENTS) {
            player.sendMessage(MessageUtils.error("Tu as utilisé tous tes hurlements (3/3).")); return true;
        }

        hurlementCount++;
        int zoneX = (int) Math.round(player.getLocation().getX() / 50) * 50;
        int zoneZ = (int) Math.round(player.getLocation().getZ() / 50) * 50;

        String msg = "§c[Hurlement] §7Un loup est autour de §eX:" + zoneX + " Z:" + zoneZ
                   + " §7(zone ~50 blocs) §8[" + hurlementCount + "/3]";
        plugin.getChatManager().sendToWolves(msg);
        plugin.getChatManager().tryOpenWolfChat();

        // Régénération II (7 secondes = 140 ticks) pour tous les loups + le hurleur
        PotionEffect regen = new PotionEffect(PotionEffectType.REGENERATION, 140, 1, false, false, false);
        for (UUID wolfUUID : plugin.getRoleManager().getWolfList()) {
            Player wolf = Bukkit.getPlayer(wolfUUID);
            if (wolf != null && plugin.getGameManager().isAlive(wolfUUID)) {
                wolf.addPotionEffect(regen);
                if (!wolf.getUniqueId().equals(player.getUniqueId())) {
                    wolf.sendMessage("§c[Hurlement] §7Un loup a hurlé — Régénération II pendant 7 secondes !");
                }
            }
        }
        // Aussi pour le hurleur lui-même s'il n'est pas dans la wolfList (sécurité)
        player.addPotionEffect(regen);

        player.sendMessage(MessageUtils.success(
            "Hurlement " + hurlementCount + "/3 — Régén II 7s accordée à tous les loups !"));
        return true;
    }
}
