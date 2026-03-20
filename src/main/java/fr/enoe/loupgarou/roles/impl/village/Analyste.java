package fr.enoe.loupgarou.roles.impl.village;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * Analyste (dès 50 min).
 * /lg observer <pseudo> (5× par partie, cooldown 5 min) — sait si la cible a Strength/Resistance/Weakness/Speed/Invisibility/Absorption.
 * /lg analyser — 1× par partie — révèle les effets précis de la dernière observation. Le ciblé est averti, et s'il n'est pas Villageois il connaît l'identité de l'Analyste.
 */
public class Analyste extends Role {

    private int observationsLeft = 5;
    private long lastObservTick  = -9999L;
    private boolean analyseUsed  = false;

    // Dernière observation : cible + effets détectés
    private UUID lastObservedUUID = null;
    private final List<String> lastObservedEffects = new ArrayList<>();

    private static final List<PotionEffectType> WATCHED = List.of(
        PotionEffectType.STRENGTH, PotionEffectType.RESISTANCE, PotionEffectType.WEAKNESS,
        PotionEffectType.SPEED, PotionEffectType.INVISIBILITY, PotionEffectType.ABSORPTION
    );

    public Analyste(LoupGarouPlugin p, UUID u) { super(p, u); }

    @Override public String getDisplayName() { return "§bAnalyste"; }
    @Override public String getDescription() {
        return "§7Dès 50 min : /lg observer <pseudo> (5×, 5min cd) → effets. /lg analyser (1×) → détails précis.";
    }
    @Override public RoleFamily getFamily() { return RoleFamily.VILLAGE; }
    @Override public String getId()         { return "analyste"; }

    @Override
    public boolean onPowerCommand(Player player, String[] args) {
        if (args[0].equalsIgnoreCase("observer")) {
            return doObserver(player, args);
        } else if (args[0].equalsIgnoreCase("analyser")) {
            return doAnalyse(player);
        }
        return false;
    }

    private boolean doObserver(Player player, String[] args) {
        if (plugin.getGameManager().getElapsedSeconds() < 3000) { // 50 min
            player.sendMessage(MessageUtils.error("Le pouvoir d'observation n'est disponible qu'après 50 minutes.")); return true;
        }
        if (observationsLeft <= 0) {
            player.sendMessage(MessageUtils.error("Tu n'as plus d'observations disponibles (5/5 utilisées).")); return true;
        }
        long now = Bukkit.getCurrentTick();
        if (now - lastObservTick < 20L * 60 * 5) {
            long rem = (20L * 60 * 5 - (now - lastObservTick)) / 20;
            player.sendMessage(MessageUtils.error("Cooldown : " + rem + "s restantes.")); return true;
        }
        if (args.length < 2) { player.sendMessage(MessageUtils.error("Usage : /lg observer <pseudo>")); return true; }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null || !plugin.getGameManager().isAlive(target.getUniqueId())) {
            player.sendMessage(MessageUtils.error("Joueur introuvable ou mort.")); return true;
        }

        // Observer
        lastObservTick = now;
        observationsLeft--;
        lastObservedUUID = target.getUniqueId();
        lastObservedEffects.clear();

        boolean hasAny = false;
        for (PotionEffectType type : WATCHED) {
            if (target.hasPotionEffect(type)) {
                hasAny = true;
                lastObservedEffects.add(type.getName());
            }
        }

        if (hasAny) {
            player.sendMessage(MessageUtils.success("§e" + target.getName()
                + " §apossède au moins un effet actif parmi : Strength, Resistance, Weakness, Speed, Invisibility, Absorption."));
        } else {
            player.sendMessage(MessageUtils.success("§e" + target.getName() + " §an'a aucun des effets surveillés."));
        }
        player.sendMessage("§7(" + observationsLeft + " observation(s) restante(s) · /lg analyser pour les détails)");
        return true;
    }

    private boolean doAnalyse(Player player) {
        if (analyseUsed) { player.sendMessage(MessageUtils.error("Tu as déjà utilisé ton analyse.")); return true; }
        if (lastObservedUUID == null) { player.sendMessage(MessageUtils.error("Observe d'abord un joueur avec /lg observer.")); return true; }

        analyseUsed = true;
        Player target = Bukkit.getPlayer(lastObservedUUID);
        String tName = target != null ? target.getName() : "le joueur observé";

        if (lastObservedEffects.isEmpty()) {
            player.sendMessage(MessageUtils.success("§e" + tName + " §an'avait aucun effet actif lors de ton observation."));
        } else {
            player.sendMessage(MessageUtils.success("Effets de §e" + tName + " §alors de l'observation : §b"
                + String.join(", ", lastObservedEffects)));
        }

        // Notifier la cible si elle n'est pas Villageois
        if (target != null && plugin.getGameManager().isAlive(lastObservedUUID)) {
            var role = plugin.getRoleManager().getRole(lastObservedUUID);
            if (role != null && role.getFamily() != RoleFamily.VILLAGE) {
                target.sendMessage("§c[Analyste] §7L'Analyste §e" + player.getName()
                    + " §7a analysé tes effets — il connaît maintenant tes potions !");
            }
        }
        return true;
    }
}
