package fr.enoe.loupgarou.roles.impl.village;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import java.util.UUID;

public class IdiotDuVillage extends Role {

    private boolean usedRevive = false;

    public IdiotDuVillage(LoupGarouPlugin plugin, UUID uuid) { super(plugin, uuid); }

    @Override public String getDisplayName() { return "§eIdiot du Village"; }
    @Override public String getDescription() {
        return "§7Si tué par un villageois : ressuscite une fois, perd 2 cœurs permanents.";
    }
    @Override public RoleFamily getFamily()  { return RoleFamily.VILLAGE; }
    @Override public String getId()          { return "idiot"; }

    /**
     * Appelé depuis RoleManager.tryVillageIdiotRevive().
     * killedByVillage = true si la mort vient d'un vote ou d'un joueur villageois.
     * Retourne true pour annuler la mort et ressusciter le joueur.
     */
    public boolean tryRevive(boolean killedByVillage) {
        if (!killedByVillage || usedRevive) return false;

        usedRevive = true;
        Player p = Bukkit.getPlayer(playerUUID);
        if (p == null) return false;

        // Ressusciter : remettre en mode survie et soigner
        p.setGameMode(GameMode.SURVIVAL);
        p.setHealth(Math.min(p.getMaxHealth(), 10.0)); // 5 cœurs au retour

        // Réduire le max HP de 4 (= 2 cœurs) de façon permanente
        var attr = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr != null) {
            double newMax = Math.max(4.0, attr.getBaseValue() - 4.0);
            attr.setBaseValue(newMax);
            if (p.getHealth() > newMax) p.setHealth(newMax);
        }

        p.sendMessage("§e[Idiot] §7Le village t'a tué... mais tu es trop idiot pour mourir !");
        p.sendMessage("§c§lTu perds 2 cœurs permanents. (Vie max réduite)");
        return true;
    }
}
