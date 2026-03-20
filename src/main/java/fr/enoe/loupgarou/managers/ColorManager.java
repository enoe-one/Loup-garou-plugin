package fr.enoe.loupgarou.managers;

import fr.enoe.loupgarou.LoupGarouPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ColorManager — chaque joueur peut choisir une couleur pour afficher
 * le pseudo des autres joueurs dans le chat et le scoreboard.
 * La couleur est purement LOCALE : les autres joueurs ne la voient pas.
 */
public class ColorManager {

    private final LoupGarouPlugin plugin;

    // UUID du joueur qui configure → (UUID cible → couleur choisie)
    private final Map<UUID, Map<UUID, ChatColor>> playerColors = new HashMap<>();

    // Couleur par défaut pour les joueurs non colorés
    public static final ChatColor DEFAULT_COLOR = ChatColor.GRAY;

    public ColorManager(LoupGarouPlugin plugin) {
        this.plugin = plugin;
    }

    /** Retourne la couleur qu'un joueur a choisie pour afficher un autre joueur. */
    public ChatColor getColor(UUID viewer, UUID target) {
        Map<UUID, ChatColor> map = playerColors.get(viewer);
        if (map == null) return DEFAULT_COLOR;
        return map.getOrDefault(target, DEFAULT_COLOR);
    }

    /** Définit la couleur qu'un joueur voit pour un autre. */
    public void setColor(UUID viewer, UUID target, ChatColor color) {
        playerColors.computeIfAbsent(viewer, k -> new HashMap<>()).put(target, color);
    }

    /** Formate un pseudo pour un viewer donné. */
    public String formatName(UUID viewer, Player target) {
        ChatColor c = getColor(viewer, target.getUniqueId());
        return c + target.getName() + ChatColor.RESET;
    }

    /** Réinitialise toutes les couleurs d'un joueur. */
    public void resetColors(UUID viewer) {
        playerColors.remove(viewer);
    }

    /** Réinitialise tout (fin de partie). */
    public void reset() {
        playerColors.clear();
    }
}
