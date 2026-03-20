package fr.enoe.loupgarou.roles.impl.village;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.UUID;

public class Chasseur extends Role {
    private boolean canUseVengeance = false;

    public Chasseur(LoupGarouPlugin plugin, UUID uuid) { super(plugin, uuid); }
    @Override public String getDisplayName() { return "§cChasseur"; }
    @Override public String getDescription()  { return "§7Force 0.5 vs LG. En mourant: /lg tirer <joueur>."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.VILLAGE; }
    @Override public String getId()           { return "chasseur"; }

    @Override
    public void onDeath(Player player) {
        canUseVengeance = true;
        player.sendMessage("§c[Chasseur] §7Tu meurs ! Utilise §e/lg tirer <joueur> §7pour te venger !");
    }

    @Override
    public boolean onPowerCommand(Player player, String[] args) {
        if (!args[0].equalsIgnoreCase("tirer")) return false;
        if (!canUseVengeance) { player.sendMessage(MessageUtils.error("Pouvoir non disponible.")); return true; }
        if (args.length < 2) { player.sendMessage(MessageUtils.error("Usage: /lg tirer <joueur>")); return true; }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) { player.sendMessage(MessageUtils.error("Joueur introuvable.")); return true; }
        double hp = target.getHealth();
        target.setHealth(hp <= 10.0 ? 0.5 : hp - 10.0);
        MessageUtils.broadcast("§c[Chasseur] §e" + player.getName() + " §ctire sur §e" + target.getName() + " §cen mourant !");
        canUseVengeance = false;
        return true;
    }
}
