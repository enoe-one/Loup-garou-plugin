package fr.enoe.loupgarou.roles.impl.loup;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

/**
 * Loup-Garou Mesquin.
 * Strength I la nuit. Hurlement 1×/partie. Kill → Speed I + 2♥ abs 1 min.
 * /lg trafiquer (2× par partie) : marque l'urne de vote — les Villageois qui votent dedans ne gagnent pas d'Honneur.
 * (Note : l'Honneur n'étant pas implémenté, le traficage invalide le vote du joueur au prochain round.)
 */
public class LoupMesquin extends Role {

    private boolean hurlementUsed = false;
    private int trafiquerLeft = 2;
    // UUID des votants dont le prochain vote est annulé (traficage)
    private final java.util.Set<UUID> trafiquedVoters = new java.util.HashSet<>();

    public LoupMesquin(LoupGarouPlugin p, UUID u) { super(p, u); }

    @Override public String getDisplayName() { return "§cLoup-Garou Mesquin"; }
    @Override public String getDescription() {
        return "§7Force I la nuit. /lg trafiquer (2×) → invalide les votes d'une urne. /lg hurler (1×/partie). Tue → Speed+abs.";
    }
    @Override public RoleFamily getFamily() { return RoleFamily.LOUP; }
    @Override public String getId()         { return "loup_mesquin"; }

    @Override
    public void onNightTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 0, false, false, false));
    }

    @Override
    public void onPlayerKill(Player killer, UUID victimUUID) {
        killer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,      1200, 0, false, false, false));
        killer.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,  1200, 0, false, false, true));
        killer.sendMessage(MessageUtils.success("Tu as tué — Speed I + 2♥ absorption pour 1 minute !"));
    }

    @Override
    public boolean onPowerCommand(Player player, String[] args) {
        switch (args[0].toLowerCase()) {
            case "hurler" -> {
                if (hurlementUsed) { player.sendMessage(MessageUtils.error("Déjà hurlé.")); return true; }
                int zoneX = (int) Math.round(player.getLocation().getX() / 50) * 50;
                int zoneZ = (int) Math.round(player.getLocation().getZ() / 50) * 50;
                plugin.getChatManager().sendToWolves("§c[Hurlement] §7Un loup autour de §eX:" + zoneX + " Z:" + zoneZ);
                plugin.getChatManager().tryOpenWolfChat();
                hurlementUsed = true;
                player.sendMessage(MessageUtils.success("Tu as hurlé."));
                return true;
            }
            case "trafiquer" -> {
                if (trafiquerLeft <= 0) {
                    player.sendMessage(MessageUtils.error("Plus de traficages disponibles (2/2 utilisés).")); return true;
                }
                if (!plugin.getVoteManager().isVoteOpen()) {
                    player.sendMessage(MessageUtils.error("Aucun vote n'est en cours.")); return true;
                }
                // Marquer tous les votants actuels comme "trafiqués" → leur vote sera ignoré
                plugin.getGameManager().getAlivePlayers().stream()
                    .filter(u -> plugin.getRoleManager().isVillager(u))
                    .forEach(trafiquedVoters::add);
                trafiquerLeft--;
                player.sendMessage(MessageUtils.success(
                    "Urne trafiquée ! Les votes villageois de ce tour sont annulés. (" + trafiquerLeft + " restant(s))"));
                plugin.getLogger().info("[LG] LoupMesquin " + player.getName() + " a trafiqué l'urne.");
                return true;
            }
        }
        return false;
    }

    /** Vérifie si ce votant est trafiqué (appelé par VoteManager). */
    public boolean isTrafique(UUID voterUUID) {
        return trafiquedVoters.remove(voterUUID); // consomme le traficage
    }
}
