package fr.enoe.loupgarou.roles.impl.loup;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.UUID;

public class GrandMechantLoup extends Role {

    // Rage utilisable 2 fois dans toute la partie
    private int rageCount = 0;
    private static final int MAX_RAGE = 2;
    // Recharge : 30 minutes entre chaque utilisation
    private long lastRage = -9999;

    public GrandMechantLoup(LoupGarouPlugin p, UUID u) { super(p, u); }

    @Override public String getDisplayName() { return "§4Grand Méchant Loup"; }
    @Override public String getDescription() {
        return "§7Vote double. /lg rage : Force 30% + Speed 30% pendant 5 min (2× par partie, recharge 30 min).";
    }
    @Override public RoleFamily getFamily()  { return RoleFamily.LOUP; }
    @Override public String getId()          { return "grand_mechant_loup"; }

    @Override
    public boolean onPowerCommand(Player player, String[] args) {
        if (!args[0].equalsIgnoreCase("rage")) return false;

        if (rageCount >= MAX_RAGE) {
            player.sendMessage(MessageUtils.error("Tu as déjà utilisé ta Rage 2 fois dans cette partie !"));
            return true;
        }
        long now = plugin.getGameManager().getElapsedSeconds();
        long recharge = 1800; // 30 min
        if (now - lastRage < recharge) {
            long remaining = recharge - (now - lastRage);
            player.sendMessage(MessageUtils.error("Rage en recharge ! (" + (remaining / 60) + " min " + (remaining % 60) + " sec)"));
            return true;
        }

        lastRage = now;
        rageCount++;

        // Force I (amp 0) ≈ 30% — durée 5 min = 6000 ticks
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 6000, 0, false, false, false));
        // Speed I (amp 0) ≈ 20%, Speed II (amp 1) ≈ 40% → on prend amp 0 pour ~30%
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,    6000, 0, false, false, false));

        player.sendMessage(MessageUtils.success(
            "RAGE activée ! (" + rageCount + "/" + MAX_RAGE + ") — Force 30% + Speed pendant 5 minutes !"));
        return true;
    }
}
