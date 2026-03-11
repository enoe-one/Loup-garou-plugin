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
    @Override public String getDescription()  { return "§7Charme les joueurs proches. Gagne quand tous sont charmés."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.SOLITAIRE; }
    @Override public String getId()           { return "flutiste"; }
    @Override public void onNightTick(Player player) {
        for (UUID alive : plugin.getGameManager().getAlivePlayers()) {
            if (alive.equals(playerUUID)) continue;
            Player target = org.bukkit.Bukkit.getPlayer(alive);
            if (target == null) continue;
            double dist = player.getLocation().distance(target.getLocation());
            double rate = dist<=5 ? 15 : dist<=25 ? 3 : dist<=50 ? 1.5 : 0;
            if (rate == 0) continue;
            charmPercent.merge(alive, rate, Double::sum);
        }
        applyBonuses(player);
    }
    private void applyBonuses(Player p) {
        long total = plugin.getGameManager().getAlivePlayers().size() - 1;
        if (total <= 0) return;
        long charmed = charmPercent.values().stream().filter(v -> v >= 100.0).count();
        double pct = (double) charmed / total * 100.0;
        double extraHp = 0;
        if (pct >= 20) extraHp += 2;
        if (pct >= 40) extraHp += 2;
        if (pct >= 80) extraHp += 2;
        var attr = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr != null) attr.setBaseValue(20 + extraHp);
        if (pct >= 20) p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 0, false, false, false));
        if (pct >= 80) p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 1, false, false, false));
    }
    public boolean hasWon() {
        long total = plugin.getGameManager().getAlivePlayers().size() - 1;
        if (total <= 0) return false;
        return charmPercent.values().stream().filter(v -> v >= 100.0).count() >= total;
    }
    public Map<UUID, Double> getCharmPercent() { return charmPercent; }
}
