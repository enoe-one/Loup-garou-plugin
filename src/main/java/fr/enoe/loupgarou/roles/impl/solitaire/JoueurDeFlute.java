package fr.enoe.loupgarou.roles.impl.solitaire;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;

public class JoueurDeFlute extends Role {

    private final Map<UUID, Double> charmPercent = new HashMap<>();

    public JoueurDeFlute(LoupGarouPlugin p, UUID u) { super(p, u); }

    @Override public String getDisplayName() { return "§dJoueur de Flûte"; }
    @Override public String getDescription() {
        return "§7Charme les joueurs proches la nuit. Gagne Force/Résistance/Cœurs selon les charmés. Gagne en étant le DERNIER SURVIVANT (pas en charmant tout le monde).";
    }
    @Override public RoleFamily getFamily()  { return RoleFamily.SOLITAIRE; }
    @Override public String getId()          { return "flutiste"; }

    @Override
    public void onNightTick(Player player) {
        // Charmer les joueurs proches
        for (UUID alive : plugin.getGameManager().getAlivePlayers()) {
            if (alive.equals(playerUUID)) continue;
            Player target = org.bukkit.Bukkit.getPlayer(alive);
            if (target == null) continue;
            double dist = player.getLocation().distance(target.getLocation());
            double rate = dist <= 5 ? 15 : dist <= 25 ? 3 : dist <= 50 ? 1.5 : 0;
            if (rate == 0) continue;
            charmPercent.merge(alive, rate, Double::sum);
        }
        applyBonuses(player);
    }

    private void applyBonuses(Player p) {
        long total   = plugin.getGameManager().getAlivePlayers().size() - 1;
        if (total <= 0) return;
        long charmed = charmPercent.values().stream().filter(v -> v >= 100.0).count();
        double pct   = total > 0 ? (double) charmed / total * 100.0 : 0;

        // +4 cœurs max (+2 à 25%, +2 de plus à 75%)
        double extraHp = 0;
        if (pct >= 25) extraHp += 4; // +2 cœurs
        if (pct >= 75) extraHp += 4; // +2 cœurs supplémentaires
        var attr = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr != null) attr.setBaseValue(20 + extraHp);

        // Force : max 60% (amp 1 = Force II) — non visible
        if (pct >= 50) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 1, false, false, false)); // ~60%
        } else if (pct >= 20) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 0, false, false, false)); // ~30%
        } else {
            p.removePotionEffect(PotionEffectType.STRENGTH);
        }

        // Résistance : max 40% (amp 1) — non visible
        if (pct >= 80) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 1, false, false, false)); // ~40%
        } else if (pct >= 40) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 0, false, false, false)); // ~20%
        } else {
            p.removePotionEffect(PotionEffectType.RESISTANCE);
        }
    }

    // Le JdF ne gagne PAS en charmant tout le monde — il doit être le DERNIER SURVIVANT
    // (condition gérée par checkWinCondition dans GameManager comme tout autre solitaire)

    public Map<UUID, Double> getCharmPercent() { return charmPercent; }
    public boolean isCharmed(UUID uuid) {
        return charmPercent.getOrDefault(uuid, 0.0) >= 100.0;
    }
}
