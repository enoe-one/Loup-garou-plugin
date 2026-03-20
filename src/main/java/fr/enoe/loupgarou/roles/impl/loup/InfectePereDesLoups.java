package fr.enoe.loupgarou.roles.impl.loup;
import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.UUID;
public class InfectePereDesLoups extends Role {
    private boolean usedPower = false;
    private UUID pendingRevive = null;
    public InfectePereDesLoups(LoupGarouPlugin p, UUID u) { super(p, u); }
    @Override public String getDisplayName() { return "§4Infecté Père des Loups"; }
    @Override public String getDescription()  { return "§7Une fois: ressuscite un mort dans les 10s — il devient loup."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.LOUP; }
    @Override public String getId()           { return "infecte"; }
    public boolean tryInfect(UUID deadUUID) {
        if (usedPower) return false;
        Player infect = Bukkit.getPlayer(playerUUID);
        if (infect == null) return false;
        pendingRevive = deadUUID;
        String name = Bukkit.getOfflinePlayer(deadUUID).getName();
        infect.sendMessage("§4[Infecté] §e" + name + " §cvient de mourir ! /lg infecter pour l'infecter (10s) !");
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override public void run() { pendingRevive = null; }
        }.runTaskLater(plugin, 200L);
        return false;
    }
    @Override public boolean onPowerCommand(Player player, String[] args) {
        if (!args[0].equalsIgnoreCase("infecter")) return false;
        if (usedPower)              { player.sendMessage(MessageUtils.error("Déjà utilisé !")); return true; }
        if (pendingRevive == null)  { player.sendMessage(MessageUtils.error("Aucun mort disponible.")); return true; }
        plugin.getRoleManager().infectPlayer(pendingRevive, player);
        usedPower = true; pendingRevive = null;
        return true;
    }
}
