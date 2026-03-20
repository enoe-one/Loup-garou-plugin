package fr.enoe.loupgarou.managers;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class EventManager {

    private final LoupGarouPlugin plugin;

    public EventManager(LoupGarouPlugin plugin) { this.plugin = plugin; }

    public void triggerElection() {
        var maire = plugin.getRoleManager().findRoleById("maire");
        if (maire == null) return;
        Player mairePlayer = Bukkit.getPlayer(maire.getPlayerUUID());
        if (mairePlayer == null) return;
        MessageUtils.broadcast("§6[Élection] §eLe Maire est : §b" + mairePlayer.getName());
    }

    /**
     * EXPOSED — 1 joueur voit son pseudo affiché à côté de 4 rôles :
     * - Son vrai rôle
     * - 1 rôle d'un autre camp
     * - 2 rôles aléatoires
     * Au moins 2 rôles Villageois dans l'Exposed.
     * S'active automatiquement avec les votes.
     */
    public void triggerExpose() {
        List<UUID> alive = new ArrayList<>(plugin.getGameManager().getAlivePlayers());
        if (alive.size() < 2) return;

        // Choisir 1 joueur cible
        UUID targetUUID = alive.get(new Random().nextInt(alive.size()));
        Player target = Bukkit.getPlayer(targetUUID);
        if (target == null) return;

        Role trueRole = plugin.getRoleManager().getRole(targetUUID);
        if (trueRole == null) return;

        // Construire la liste de 4 rôles (dont le vrai)
        List<String> roleNames = buildExposedRoles(targetUUID, trueRole, alive);

        // Mélanger
        Collections.shuffle(roleNames);

        String rolesStr = String.join(" §7| ", roleNames.stream()
            .map(r -> "§b" + r).collect(Collectors.toList()));

        MessageUtils.broadcast("§e§l[EXPOSED] §r§e" + target.getName()
            + " §7— Parmi ces rôles, l'un est le sien : " + rolesStr);
    }

    private List<String> buildExposedRoles(UUID targetUUID, Role trueRole, List<UUID> alive) {
        List<String> result = new ArrayList<>();
        result.add(trueRole.getDisplayName());

        // 1 rôle d'un autre camp
        RoleFamily trueFamily = trueRole.getFamily();
        List<Role> otherCamp = alive.stream()
            .filter(u -> !u.equals(targetUUID))
            .map(u -> plugin.getRoleManager().getRole(u))
            .filter(r -> r != null && r.getFamily() != trueFamily)
            .collect(Collectors.toList());
        if (!otherCamp.isEmpty()) {
            Collections.shuffle(otherCamp);
            result.add(otherCamp.get(0).getDisplayName());
        } else {
            result.add("§fSimple Villageois");
        }

        // 2 rôles aléatoires (en s'assurant qu'il y a au moins 2 Villageois au total)
        List<Role> pool = alive.stream()
            .filter(u -> !u.equals(targetUUID))
            .map(u -> plugin.getRoleManager().getRole(u))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        Collections.shuffle(pool);
        for (Role r : pool) {
            if (result.size() >= 4) break;
            if (!result.contains(r.getDisplayName())) result.add(r.getDisplayName());
        }
        // Compléter si pas assez
        while (result.size() < 4) result.add("§fSimple Villageois");

        // Garantir au moins 2 Villageois
        long villageCount = result.stream()
            .filter(n -> !n.contains("§c") && !n.contains("§4") && !n.contains("§d"))
            .count();
        if (villageCount < 2) result.set(result.size() - 1, "§fSimple Villageois");

        return result;
    }

    /**
     * EXPOSED INVERSÉ — 5 joueurs voient leurs pseudos à côté d'un rôle.
     * Un seul parmi les 5 est réellement ce rôle.
     */
    public void triggerExposeInverse() {
        List<UUID> alive = new ArrayList<>(plugin.getGameManager().getAlivePlayers());
        if (alive.size() < 5) { triggerExpose(); return; } // fallback si pas assez de joueurs

        Collections.shuffle(alive);
        List<UUID> suspects = alive.subList(0, 5);

        // Choisir le "vrai" parmi les 5
        UUID realUUID = suspects.get(new Random().nextInt(5));
        Role realRole = plugin.getRoleManager().getRole(realUUID);
        if (realRole == null) return;

        String names = suspects.stream()
            .map(u -> { Player p = Bukkit.getPlayer(u); return p != null ? "§e" + p.getName() + "§7" : "§e?§7"; })
            .collect(Collectors.joining(", "));

        MessageUtils.broadcast("§e§l[EXPOSED INVERSÉ] §r§7Parmi " + names
            + " §7, l'un d'eux est §b" + realRole.getDisplayName() + "§7. Mais lequel ?");
    }

    /**
     * Déclenche un événement aléatoire parmi les disponibles.
     * Appelé par GameManager lors des votes ou events aléatoires.
     */
    public void triggerRandomEvent() {
        List<Runnable> events = new ArrayList<>();
        events.add(this::triggerExpose);
        events.add(this::triggerExposeInverse);
        // On peut ajouter d'autres events ici

        events.get(new Random().nextInt(events.size())).run();
    }
}
