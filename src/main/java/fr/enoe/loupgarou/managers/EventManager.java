package fr.enoe.loupgarou.managers;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class EventManager {

    private final LoupGarouPlugin plugin;

    public EventManager(LoupGarouPlugin plugin) { this.plugin = plugin; }

    public void triggerElection() {
        var maire = plugin.getRoleManager().findRoleById("maire");
        if (maire == null) return;
        Player mairePlayer = Bukkit.getPlayer(maire.getPlayerUUID());
        if (mairePlayer == null) return;
        MessageUtils.broadcast("§6[Élection] §eLe Maire est : §b" + mairePlayer.getName());
        plugin.getGameManager().getAlivePlayers().stream()
                .filter(u -> !u.equals(maire.getPlayerUUID()))
                .filter(u -> plugin.getRoleManager().isVillager(u))
                .findFirst()
                .ifPresent(u -> {
                    Player vp = Bukkit.getPlayer(u);
                    if (vp != null)
                        mairePlayer.sendMessage("§6[Élection] §7Un villageois révélé : §e" + vp.getName());
                });
    }

    public void triggerExpose() {
        List<UUID> alive = new ArrayList<>(plugin.getGameManager().getAlivePlayers());
        if (alive.size() < 4) return;
        Collections.shuffle(alive);
        List<UUID> suspects = alive.subList(0, Math.min(4, alive.size()));
        UUID withRole = suspects.get(new Random().nextInt(suspects.size()));
        var role = plugin.getRoleManager().getRole(withRole);
        if (role == null) return;
        String names = suspects.stream()
                .map(u -> { Player p = Bukkit.getPlayer(u); return p != null ? p.getName() : "?"; })
                .reduce((a, b) -> a + ", " + b).orElse("?");
        MessageUtils.broadcast("§e[Exposé] §7Parmi §b" + names + "§7, il y a un(e) §b" + role.getDisplayName() + "§7 !");
    }
}
