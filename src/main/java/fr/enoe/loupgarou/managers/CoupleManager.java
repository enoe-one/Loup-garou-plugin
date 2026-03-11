package fr.enoe.loupgarou.managers;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class CoupleManager {

    private final LoupGarouPlugin plugin;
    private UUID partner1 = null;
    private UUID partner2 = null;
    private UUID partner3 = null;
    private boolean announced = false;

    public CoupleManager(LoupGarouPlugin plugin) { this.plugin = plugin; }

    public void createCouple(UUID p1, UUID p2) {
        this.partner1 = p1;
        this.partner2 = p2;
        Player pl1 = Bukkit.getPlayer(p1), pl2 = Bukkit.getPlayer(p2);
        if (pl1 != null) pl1.sendMessage("§d§lTu es amoureux de §e" + (pl2 != null ? pl2.getName() : "?") + "§d§l ! Si il/elle meurt, tu mourras aussi.");
        if (pl2 != null) pl2.sendMessage("§d§lTu es amoureux de §e" + (pl1 != null ? pl1.getName() : "?") + "§d§l ! Si il/elle meurt, tu mourras aussi.");
    }

    public void createRandomCouple(List<UUID> alive) {
        if (alive.size() < 2) return;
        Collections.shuffle(alive);
        boolean trouple = plugin.getConfig().getBoolean("game.trouple", false) && alive.size() >= 9;
        createCouple(alive.get(0), alive.get(1));
        if (trouple) {
            partner3 = alive.get(2);
            Player pl3 = Bukkit.getPlayer(partner3);
            Player pl1 = Bukkit.getPlayer(partner1), pl2 = Bukkit.getPlayer(partner2);
            if (pl3 != null) pl3.sendMessage("§d§lTu fais partie d'un trouple avec §e"
                    + (pl1 != null ? pl1.getName() : "?") + " §det §e" + (pl2 != null ? pl2.getName() : "?"));
            if (pl1 != null) pl1.sendMessage("§d§lVotre trouple inclut maintenant §e" + pl3.getName());
            if (pl2 != null) pl2.sendMessage("§d§lVotre trouple inclut maintenant §e" + pl3.getName());
        }
    }

    public void announceCouple() {
        if (announced || partner1 == null) {
            boolean randomCouple = plugin.getConfig().getBoolean("game.random-couple", true);
            boolean hasCupidon   = plugin.getRoleManager().findRoleById("cupidon") != null;
            if (randomCouple && !hasCupidon) {
                createRandomCouple(new ArrayList<>(plugin.getGameManager().getAlivePlayers()));
            }
        }
        if (partner1 == null) return;
        announced = true;
        Player pl1 = Bukkit.getPlayer(partner1), pl2 = Bukkit.getPlayer(partner2);
        MessageUtils.broadcast("§d§l[Amour] §e" + (pl1 != null ? pl1.getName() : "?")
                + " §det §e" + (pl2 != null ? pl2.getName() : "?") + " §d§lsont amoureux !");
        if (partner3 != null) {
            Player pl3 = Bukkit.getPlayer(partner3);
            MessageUtils.broadcast("§d§l[Trouple] §e" + (pl3 != null ? pl3.getName() : "?")
                    + " §dfait également partie de ce couple !");
        }
    }

    public void onPartnerDeath(UUID dead) {
        if (dead.equals(partner1) && partner2 != null) killPartner(partner2);
        else if (dead.equals(partner2) && partner1 != null) killPartner(partner1);
        if (partner3 != null) {
            if (dead.equals(partner3)) { killPartner(partner1); killPartner(partner2); }
            else if (dead.equals(partner1) || dead.equals(partner2)) killPartner(partner3);
        }
    }

    private void killPartner(UUID uuid) {
        if (uuid == null || !plugin.getGameManager().isAlive(uuid)) return;
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) {
            p.sendMessage("§d[Amour] §cTon partenaire est mort... Tu meurs de chagrin.");
            plugin.getGameManager().handlePlayerDeath(p);
        }
    }

    public void reset() { partner1 = null; partner2 = null; partner3 = null; announced = false; }
}
