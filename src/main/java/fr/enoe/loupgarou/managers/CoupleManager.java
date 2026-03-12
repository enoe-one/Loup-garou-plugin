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
    private boolean notified = false;

    // UUID de l'Ivrogne en "faux couple"
    private UUID fakeCoupleMember = null;
    private UUID fakeCouplePartner = null; // le "partenaire" fictif (un joueur aléatoire)

    public CoupleManager(LoupGarouPlugin plugin) { this.plugin = plugin; }

    // ── Créer le vrai couple (par Cupidon) ─────────────────────────────────
    public void createCouple(UUID p1, UUID p2) {
        this.partner1 = p1;
        this.partner2 = p2;
        // Les joueurs seront informés à 25 min via notifyCouplePrivately()
    }

    public void createRandomCouple(List<UUID> alive) {
        if (alive.size() < 2) return;
        Collections.shuffle(alive);
        boolean trouple = plugin.getConfig().getBoolean("game.trouple", false) && alive.size() >= 9;
        createCouple(alive.get(0), alive.get(1));
        if (trouple) partner3 = alive.get(2);
    }

    // ── Faux couple Ivrogne ─────────────────────────────────────────────────
    public void createFakeCouple(UUID ivrogneUUID, List<UUID> alive) {
        this.fakeCoupleMember = ivrogneUUID;
        // Choisir un partenaire fictif parmi les vivants (pas l'ivrogne lui-même)
        List<UUID> candidates = new ArrayList<>(alive);
        candidates.remove(ivrogneUUID);
        if (candidates.isEmpty()) return;
        Collections.shuffle(candidates);
        this.fakeCouplePartner = candidates.get(0);
    }

    // ── Notification privée à 25 min (appelée depuis GameManager.onTick) ───
    public void notifyCouplePrivately() {
        // Créer couple aléatoire si pas de Cupidon
        if (partner1 == null) {
            boolean randomCouple = plugin.getConfig().getBoolean("game.random-couple", true);
            boolean hasCupidon   = plugin.getRoleManager().findRoleById("cupidon") != null;
            if (randomCouple && !hasCupidon) {
                createRandomCouple(new ArrayList<>(plugin.getGameManager().getAlivePlayers()));
            }
        }
        if (partner1 == null) return;
        if (notified) return;
        notified = true;

        Player pl1 = Bukkit.getPlayer(partner1);
        Player pl2 = Bukkit.getPlayer(partner2);

        // Informer chaque partenaire en PRIVÉ — sans révéler qui est Cupidon
        if (pl1 != null)
            pl1.sendMessage("§d§l[Amour] §7Tu es amoureux de §e" + (pl2 != null ? pl2.getName() : "?")
                + "§7. Si il/elle meurt, tu mourras aussi. Tu ne sais pas qui vous a liés.");
        if (pl2 != null)
            pl2.sendMessage("§d§l[Amour] §7Tu es amoureux de §e" + (pl1 != null ? pl1.getName() : "?")
                + "§7. Si il/elle meurt, tu mourras aussi. Tu ne sais pas qui vous a liés.");

        // Trouple
        if (partner3 != null) {
            Player pl3 = Bukkit.getPlayer(partner3);
            if (pl3 != null) pl3.sendMessage("§d§l[Amour] §7Tu fais partie d'un trouple avec §e"
                + (pl1 != null ? pl1.getName() : "?") + " §7et §e" + (pl2 != null ? pl2.getName() : "?")
                + "§7. Tu mourras s'ils meurent.");
            if (pl1 != null) pl1.sendMessage("§d[Amour] §7Votre trouple inclut §e" + (pl3 != null ? pl3.getName() : "?"));
            if (pl2 != null) pl2.sendMessage("§d[Amour] §7Votre trouple inclut §e" + (pl3 != null ? pl3.getName() : "?"));
        }

        // Faux couple Ivrogne
        if (fakeCoupleMember != null && fakeCouplePartner != null) {
            Player iv = Bukkit.getPlayer(fakeCoupleMember);
            Player fp = Bukkit.getPlayer(fakeCouplePartner);
            if (iv != null)
                iv.sendMessage("§d§l[Amour] §7Tu es amoureux de §e" + (fp != null ? fp.getName() : "?")
                    + "§7. Si il/elle meurt... (tu le ressentiras profondément)");
        }
    }

    // ── Mort d'un partenaire ────────────────────────────────────────────────
    public void onPartnerDeath(UUID dead) {
        // Vrai couple
        if (dead.equals(partner1) && partner2 != null) killPartner(partner2);
        else if (dead.equals(partner2) && partner1 != null) killPartner(partner1);

        if (partner3 != null) {
            if (dead.equals(partner3)) { killPartner(partner1); killPartner(partner2); }
            else if (dead.equals(partner1) || dead.equals(partner2)) killPartner(partner3);
        }

        // Faux couple Ivrogne : le "partenaire fictif" meurt → l'ivrogne reçoit un message
        // mais NE meurt PAS réellement
        if (fakeCoupleMember != null && dead.equals(fakeCouplePartner)) {
            Player iv = Bukkit.getPlayer(fakeCoupleMember);
            if (iv != null && plugin.getGameManager().isAlive(fakeCoupleMember)) {
                iv.sendMessage("§d§l[Amour] §cTon partenaire est mort...");
                iv.sendMessage("§7§o(Tu ressens une douleur immense, mais tu tiens bon.)");
                // L'ivrogne ne meurt PAS — c'est le twist
            }
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

    // ── Getters ─────────────────────────────────────────────────────────────
    public UUID getPartner1()         { return partner1; }
    public UUID getPartner2()         { return partner2; }
    public boolean hasCouple()        { return partner1 != null; }

    public void reset() {
        partner1 = null; partner2 = null; partner3 = null;
        notified = false;
        fakeCoupleMember = null; fakeCouplePartner = null;
    }
}
