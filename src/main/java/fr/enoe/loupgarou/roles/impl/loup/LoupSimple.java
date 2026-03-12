package fr.enoe.loupgarou.roles.impl.loup;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.UUID;

public class LoupSimple extends Role {
    public LoupSimple(LoupGarouPlugin p, UUID u) { super(p, u); }
    @Override public String getDisplayName() { return "§cLoup-Garou"; }
    @Override public String getDescription() { return "§7Force 30% la nuit. +2 cœurs absorption si tu tues."; }
    @Override public RoleFamily getFamily()  { return RoleFamily.LOUP; }
    @Override public String getId()          { return "loup_simple"; }

    @Override
    public void onNightTick(Player player) {
        // Force I (amp 0) ≈ 30% de dégâts bonus
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 0, false, false, false));
    }
}
