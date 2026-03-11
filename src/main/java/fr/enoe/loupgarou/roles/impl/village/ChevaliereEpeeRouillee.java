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
    private boolean powerActive = false;
    public ChevaliereEpeeRouillee(LoupGarouPlugin plugin, UUID uuid) { super(plugin, uuid); }
    @Override public String getDisplayName() { return "§7Chevalier à l'Épée Rouillée"; }
    @Override public String getDescription()  { return "§7/lg arme — Force 2 vs LG jusqu'à en tuer un."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.VILLAGE; }
    @Override public String getId()           { return "chevalier"; }

    @Override
    public boolean onPowerCommand(Player player, String[] args) {
        if (!args[0].equalsIgnoreCase("arme")) return false;
        if (powerActive) { player.sendMessage(MessageUtils.error("Pouvoir déjà actif !")); return true; }
        powerActive = true;
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 3, false, false, true));
        player.sendMessage(MessageUtils.success("Force 2 activée contre les Loups-Garous !"));
        return true;
    }

    public void onWolfKilled(Player p) {
        if (!powerActive) return;
        p.removePotionEffect(PotionEffectType.STRENGTH);
        p.sendMessage(MessageUtils.info("Tu as tué un loup. Ton bonus est terminé."));
        powerActive = false;
    }
}
