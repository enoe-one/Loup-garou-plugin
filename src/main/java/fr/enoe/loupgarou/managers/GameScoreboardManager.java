package fr.enoe.loupgarou.managers;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.core.GameState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.UUID;

/**
 * GameScoreboardManager — affiche en sidebar pour chaque joueur :
 *  ┌─────────────────┐
 *  │  § LOUP-GAROU   │
 *  ├─────────────────┤
 *  │ ⏱ 00:00         │
 *  │ 📖 Épisode 1    │
 *  │ 🌙 Nuit / ☀ Jour│
 *  └─────────────────┘
 *
 * Le scoreboard est individuel (chaque joueur a le sien) pour supporter
 * les couleurs de pseudo personnalisées.
 */
public class GameScoreboardManager {

    private final LoupGarouPlugin plugin;

    public GameScoreboardManager(LoupGarouPlugin plugin) {
        this.plugin = plugin;
    }

    /** Démarre le ticker (toutes les secondes). */
    public void startTicker() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (plugin.getGameManager().getState() == GameState.RUNNING) {
                for (UUID uuid : plugin.getGameManager().getAlivePlayers()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) update(p);
                }
                // Aussi les morts en spectateur
                for (UUID uuid : plugin.getGameManager().getDeadPlayers()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) update(p);
                }
            }
        }, 0L, 20L);
    }

    /** Met à jour le scoreboard d'un joueur. */
    public void update(Player player) {
        ScoreboardManager sbm = Bukkit.getScoreboardManager();
        Scoreboard sb = sbm.getNewScoreboard();

        Objective obj = sb.registerNewObjective("lg_info", Criteria.DUMMY,
            "§6§lLOUP-GAROU");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        int elapsed   = plugin.getGameManager().getElapsedSeconds();
        int episode   = plugin.getGameManager().getEpisodeNumber();
        long worldTime = Bukkit.getWorlds().get(0).getTime();
        boolean isNight = worldTime >= 13000 && worldTime <= 23000;

        String timeStr    = formatTime(elapsed);
        String episodeStr = "§bÉpisode §e" + Math.max(1, episode);
        String cycleStr   = isNight ? "§9§l🌙 Nuit" : "§e§l☀ Jour";

        // Lignes du scoreboard (score décroissant = ordre d'affichage de haut en bas)
        set(obj, sb, "§7▬▬▬▬▬▬▬▬▬▬▬", 7);
        set(obj, sb, "§f⏱ §e" + timeStr, 6);
        set(obj, sb, episodeStr, 5);
        set(obj, sb, cycleStr, 4);
        set(obj, sb, "§7▬▬▬▬▬▬▬▬▬▬", 3);

        // Joueurs vivants
        int alive = plugin.getGameManager().getAlivePlayers().size();
        set(obj, sb, "§f👥 §a" + alive + " §7joueur(s)", 2);
        set(obj, sb, "§7▬▬▬▬▬▬▬▬▬", 1);

        player.setScoreboard(sb);
    }

    /** Efface le scoreboard d'un joueur. */
    public void clear(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    /** Efface tous les scoreboards. */
    public void clearAll() {
        for (Player p : Bukkit.getOnlinePlayers()) clear(p);
    }

    private void set(Objective obj, Scoreboard sb, String entry, int score) {
        // Chaque entrée doit être unique → ajouter des espaces invisibles si doublon
        String safe = entry;
        while (sb.getEntries().contains(safe)) safe += "§r";
        obj.getScore(safe).setScore(score);
    }

    private String formatTime(int seconds) {
        int h = seconds / 3600;
        int m = (seconds % 3600) / 60;
        int s = seconds % 60;
        if (h > 0) return String.format("%dh%02d:%02d", h, m, s);
        return String.format("%02d:%02d", m, s);
    }
}
