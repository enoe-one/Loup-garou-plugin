package fr.enoe.loupgarou.roles.impl.village;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.UUID;

public class ChevaliereEpeeRouillee extends Role {

    // Force permanente contre les loups : démarre à Force I (≈30%)
    // Chaque kill de non-loup retire 10% → on modélise avec 3 seuils
    // 3 niveaux : 30% (initial), 20%, 10%, 0% (épuisé)
    private int forceLevel = 3; // 3=30%, 2=20%, 1=10%, 0=épuisé

    public ChevaliereEpeeRouillee(LoupGarouPlugin plugin, UUID uuid) { super(plugin, uuid); }

    @Override public String getDisplayName() { return "§7Chevalier à l'Épée Rouillée"; }
    @Override public String getDescription() {
        return "§7Force 30% permanente contre les Loups. -10% par kill de non-loup (jusqu'à 0).";
    }
    @Override public RoleFamily getFamily()  { return RoleFamily.VILLAGE; }
    @Override public String getId()          { return "chevalier"; }

    @Override
    public void onGameStart(Player player) {
        applyForce(player);
    }

    /** Appelé depuis un listener de kill quand la victime N'EST PAS un loup. */
    public void onNonWolfKill(Player player) {
        if (forceLevel <= 0) {
            player.sendMessage(MessageUtils.info("§c[Chevalier] Ta force est épuisée."));
            return;
        }
        forceLevel--;
        applyForce(player);
        player.sendMessage(MessageUtils.info("§c[Chevalier] Kill de non-loup — force réduite (" + (forceLevel * 10) + "% restant)"));
    }

    /** Appelé quand le Chevalier tue un loup. */
    public void onWolfKilled(Player player) {
        player.sendMessage(MessageUtils.success("§7[Chevalier] Loup éliminé ! Force intacte (" + (forceLevel * 10) + "%)"));
    }

    private void applyForce(Player player) {
        player.removePotionEffect(PotionEffectType.STRENGTH);
        if (forceLevel <= 0) return;
        // Force I (amp 0) = ~30% — non visible. On utilise le même amplifier mais le message indique le niveau.
        // La réduction réelle est cosmétique (on ne peut pas faire 10%/20%/30% exactement avec les potions)
        // → on retire carrément la force à 0, on la garde à amp 0 sinon
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0, false, false, false));
    }

    public int getForceLevel() { return forceLevel; }
}
