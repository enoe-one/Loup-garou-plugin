package fr.enoe.loupgarou.managers;

import fr.enoe.loupgarou.LoupGarouPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * CoupleManager — Coup de Foudre.
 *
 * Seul Cupidon est informé du couple à 25 min.
 * Les amoureux ne découvrent leur lien que lorsqu'ils passent à <40 blocs l'un de l'autre.
 * Si l'un meurt AVANT d'avoir rencontré l'autre → l'autre ne meurt PAS.
 */
public class CoupleManager {

    private final LoupGarouPlugin plugin;

    private UUID partner1 = null;
    private UUID partner2 = null;
    private UUID partner3 = null; // trouple
    private UUID cupidUUID = null;

    // Si le joueur a déjà rencontré son partenaire (<40 blocs)
    private final Set<UUID> hasMetPartner = new HashSet<>();

    // Faux couple Ivrogne
    private UUID fakeCoupleMember  = null;
    private UUID fakeCouplePartner = null;

    private boolean notified = false;

    public CoupleManager(LoupGarouPlugin plugin) { this.plugin = plugin; }

    // ── Création du couple ──────────────────────────────────────────────────

    public void setCouple(UUID p1, UUID p2) { partner1 = p1; partner2 = p2; }
    public void createCouple(UUID p1, UUID p2) { setCouple(p1, p2); } // alias Cupidon
    public void setTrouple(UUID p1, UUID p2, UUID p3) { partner1 = p1; partner2 = p2; partner3 = p3; }
    public void setCupid(UUID cupid) { this.cupidUUID = cupid; }

    public void createRandomCouple(List<UUID> alive) {
        if (alive.size() < 2) return;
        Collections.shuffle(alive);
        partner1 = alive.get(0); partner2 = alive.get(1);
    }

    public void createFakeCouple(UUID ivrogneUUID, List<UUID> alive) {
        fakeCoupleMember = ivrogneUUID;
        List<UUID> others = new ArrayList<>(alive);
        others.remove(ivrogneUUID);
        if (others.isEmpty()) return;
        Collections.shuffle(others);
        fakeCouplePartner = others.get(0);
    }

    // ── Notification à 25 min ───────────────────────────────────────────────

    public void notifyCouplePrivately() {
        if (partner1 == null) {
            boolean randomCouple = plugin.getConfig().getBoolean("game.random-couple", true);
            boolean hasCupidon   = plugin.getRoleManager().findRoleById("cupidon") != null;
            if (randomCouple && !hasCupidon)
                createRandomCouple(new ArrayList<>(plugin.getGameManager().getAlivePlayers()));
        }
        if (partner1 == null || notified) return;
        notified = true;

        // Informer Cupidon uniquement
        if (cupidUUID != null) {
            Player cupid = Bukkit.getPlayer(cupidUUID);
            Player pl1   = Bukkit.getPlayer(partner1);
            Player pl2   = Bukkit.getPlayer(partner2);
            if (cupid != null) {
                cupid.sendMessage("§d§l[Cupidon] §7Ton flèche a touché §e"
                    + (pl1 != null ? pl1.getName() : "?") + " §7et §e"
                    + (pl2 != null ? pl2.getName() : "?") + "§7. Ils ne savent pas encore qu'ils s'aiment.");
            }
        }

        // Ivrogne : faux couple (message trompeur)
        if (fakeCoupleMember != null && fakeCouplePartner != null) {
            Player iv = Bukkit.getPlayer(fakeCoupleMember);
            Player fp = Bukkit.getPlayer(fakeCouplePartner);
            if (iv != null)
                iv.sendMessage("§d§l[Amour] §7Tu ressens quelque chose pour §e"
                    + (fp != null ? fp.getName() : "?") + "§7...");
        }

        // Démarrer le ticker de proximité seulement si l'événement Coup de Foudre est activé
        if (plugin.getConfig().getBoolean("events.coup_de_foudre", false)) {
            startProximityTicker();
        } else {
            // Comportement classique : informer les deux directement
            Player pl1 = Bukkit.getPlayer(partner1);
            Player pl2 = Bukkit.getPlayer(partner2);
            hasMetPartner.add(partner1);
            hasMetPartner.add(partner2);
            if (pl1 != null && pl2 != null) {
                pl1.sendMessage("§d§l[Amour] §7Tu es amoureux de §e" + pl2.getName()
                    + "§7. Si il/elle meurt, tu mourras aussi.");
                pl2.sendMessage("§d§l[Amour] §7Tu es amoureux de §e" + pl1.getName()
                    + "§7. Si il/elle meurt, tu mourras aussi.");
            }
        }
    }

    private void startProximityTicker() {
        new BukkitRunnable() {
            @Override public void run() {
                if (partner1 == null || partner2 == null) { cancel(); return; }
                checkMeeting(partner1, partner2);
                if (partner3 != null) { checkMeeting(partner1, partner3); checkMeeting(partner2, partner3); }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void checkMeeting(UUID a, UUID b) {
        if (hasMetPartner.contains(a) && hasMetPartner.contains(b)) return;
        Player pa = Bukkit.getPlayer(a);
        Player pb = Bukkit.getPlayer(b);
        if (pa == null || pb == null) return;
        if (!pa.getWorld().equals(pb.getWorld())) return;
        if (pa.getLocation().distance(pb.getLocation()) > 40) return;

        // Les deux se découvrent
        if (!hasMetPartner.contains(a)) {
            hasMetPartner.add(a);
            pa.sendMessage("§d§l[Coup de Foudre] §7Ton cœur s'emballe... Tu es amoureux de §e" + pb.getName()
                + "§7. Si il/elle meurt, tu mourras aussi. Tu ne sais pas qui vous a liés.");
        }
        if (!hasMetPartner.contains(b)) {
            hasMetPartner.add(b);
            pb.sendMessage("§d§l[Coup de Foudre] §7Ton cœur s'emballe... Tu es amoureux de §e" + pa.getName()
                + "§7. Si il/elle meurt, tu mourras aussi. Tu ne sais pas qui vous a liés.");
        }
    }

    // ── Mort d'un partenaire ─────────────────────────────────────────────────

    public void onPartnerDeath(UUID dead) {
        if (dead.equals(partner1) && partner2 != null) killPartnerIfMet(dead, partner2);
        else if (dead.equals(partner2) && partner1 != null) killPartnerIfMet(dead, partner1);
        if (partner3 != null) {
            if (dead.equals(partner1) || dead.equals(partner2)) killPartnerIfMet(dead, partner3);
            if (dead.equals(partner3)) { killPartnerIfMet(dead, partner1); killPartnerIfMet(dead, partner2); }
        }
    }

    private void killPartnerIfMet(UUID dead, UUID survivor) {
        // Si le survivant n'a jamais rencontré le mort → il ne meurt PAS
        if (!hasMetPartner.contains(survivor)) return;
        Player sp = Bukkit.getPlayer(survivor);
        if (sp == null || !plugin.getGameManager().isAlive(survivor)) return;
        sp.sendMessage("§c§l[Amour] §7Ton amour est mort... Tu ne peux pas survivre sans lui/elle.");
        Bukkit.getScheduler().runTaskLater(plugin, () ->
            plugin.getGameManager().handlePlayerDeath(sp), 40L);
    }

    // ── Getters / reset ──────────────────────────────────────────────────────

    public boolean isCouple(UUID a, UUID b) {
        return (a.equals(partner1) && b.equals(partner2)) || (a.equals(partner2) && b.equals(partner1));
    }

    public UUID getPartner(UUID uuid) {
        if (uuid.equals(partner1)) return partner2;
        if (uuid.equals(partner2)) return partner1;
        return null;
    }

    public void reset() {
        partner1 = null; partner2 = null; partner3 = null; cupidUUID = null;
        fakeCoupleMember = null; fakeCouplePartner = null;
        hasMetPartner.clear(); notified = false;
    }
}
