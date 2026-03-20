package fr.enoe.loupgarou.roles.impl.loup;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.UUID;

/**
 * Loup Perfide — invisible la nuit sans armure + Force 30% la nuit.
 * Nouveau : /lg pieger <joueur> — pose un piège invisible sur un joueur.
 * La prochaine fois que ce joueur attaque quelqu'un, il reçoit Faiblesse 15s.
 * Utilisable 1× par épisode.
 */
public class LoupPerfide extends Role {

    private UUID trapTarget = null;

    public LoupPerfide(LoupGarouPlugin p, UUID u) { super(p, u); }

    @Override public String getDisplayName() { return "§cLoup Perfide"; }
    @Override public String getDescription() {
        return "§7Invisible la nuit (sans armure). Force 30% nuit. /lg pieger <joueur> — Faiblesse au prochain combat. (1×/épisode)";
    }
    @Override public RoleFamily getFamily()  { return RoleFamily.LOUP; }
    @Override public String getId()          { return "loup_perfide"; }

    @Override
    public void onNightTick(Player player) {
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.STRENGTH, 60, 0, false, false, false));
    }

    @Override
    public boolean onPowerCommand(Player player, String[] args) {
        if (!args[0].equalsIgnoreCase("pieger")) return false;
        if (powerUsedThisEpisode) {
            player.sendMessage(MessageUtils.error("Déjà utilisé ce tour.")); return true;
        }
        if (args.length < 2) {
            player.sendMessage(MessageUtils.error("Usage: /lg pieger <joueur>")); return true;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(MessageUtils.error("Joueur introuvable.")); return true;
        }

        trapTarget = target.getUniqueId();
        powerUsedThisEpisode = true;
        player.sendMessage(MessageUtils.success("Piège posé sur §e" + target.getName()
            + "§a. Son prochain coup lui donnera Faiblesse !"));
        return true;
    }

    /** Appelé depuis le listener de combat quand ce joueur frappe. */
    public boolean checkTrap(UUID attacker) {
        if (!attacker.equals(trapTarget)) return false;
        trapTarget = null;
        Player p = Bukkit.getPlayer(attacker);
        if (p != null) {
            p.addPotionEffect(new PotionEffect(
                PotionEffectType.WEAKNESS, 300, 0, false, false, false)); // 15s
            p.sendMessage("§c[Piège] §7Tu t'es pris dans un piège ! Faiblesse 15s.");
        }
        return true;
    }

    public UUID getTrapTarget() { return trapTarget; }
}
