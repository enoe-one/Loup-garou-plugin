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
import java.util.*;

public class Ange extends Role {

    private boolean isGuardian   = false;
    private boolean isFallen     = false;
    private boolean killedTarget = false;
    private UUID assignedTarget  = null;
    private UUID targetToKill    = null;

    public Ange(LoupGarouPlugin p, UUID u) { super(p, u); }

    @Override public String getDisplayName() {
        return isGuardian ? "§eAnge Gardien" : isFallen ? "§8Ange Déchu" : "§7Ange";
    }
    @Override public String getDescription() {
        return "§7/lg gardien ou /lg dechu. Déchu : Force 40% permanente après avoir éliminé sa cible assignée.";
    }
    @Override public RoleFamily getFamily()  { return RoleFamily.SOLITAIRE; }
    @Override public String getId()          { return "ange"; }

    @Override
    public boolean onPowerCommand(Player player, String[] args) {
        if (isGuardian || isFallen) {
            player.sendMessage(MessageUtils.error("Tu as déjà choisi !"));
            return true;
        }
        if (args[0].equalsIgnoreCase("gardien")) {
            isGuardian = true;
            List<UUID> alive = new ArrayList<>(plugin.getGameManager().getAlivePlayers());
            alive.remove(playerUUID);
            if (!alive.isEmpty()) assignedTarget = alive.get(new Random().nextInt(alive.size()));
            var attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attr != null) attr.setBaseValue(26); // +3 cœurs
            player.sendMessage(MessageUtils.success("Tu es Ange Gardien ! Protège §e"
                + (assignedTarget != null ? Bukkit.getOfflinePlayer(assignedTarget).getName() : "?")));
            return true;
        }
        if (args[0].equalsIgnoreCase("dechu")) {
            isFallen = true;
            List<UUID> alive = new ArrayList<>(plugin.getGameManager().getAlivePlayers());
            alive.remove(playerUUID);
            if (!alive.isEmpty()) targetToKill = alive.get(new Random().nextInt(alive.size()));
            var attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attr != null) attr.setBaseValue(26); // +3 cœurs
            player.sendMessage(MessageUtils.success("Tu es Ange Déchu ! Élimine §e"
                + (targetToKill != null ? Bukkit.getOfflinePlayer(targetToKill).getName() : "?")
                + " §apour obtenir Force 40% permanente."));
            return true;
        }
        return false;
    }

    /**
     * Appelé depuis GameManager quand quelqu'un meurt, pour vérifier si c'est la cible.
     * Force 40% = Force II (amp 1) — non visible.
     * Note : "80% c'est Force I" → Force I (amp 0) ≈ 80% en termes de formule Minecraft
     * (chaque niveau de Force ajoute 3 dégâts de base, donc +3/+6 sur une attaque à 1 dégât de base).
     * En pratique pour 40%, on applique Force II (amp 1) qui est la puissance souhaitée.
     */
    public void onPlayerKilled(UUID killed) {
        if (!isFallen || killedTarget) return;
        if (!killed.equals(targetToKill)) return;

        killedTarget = true;
        Player p = Bukkit.getPlayer(playerUUID);
        if (p == null) return;

        // Force II (amp 1) permanente — non visible
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1, false, false, false));
        p.sendMessage(MessageUtils.success("§8✦ Cible éliminée ! §cForce 40%§a permanente activée !"));
    }

    public boolean isGuardian()      { return isGuardian; }
    public UUID getAssignedTarget()  { return assignedTarget; }
}
