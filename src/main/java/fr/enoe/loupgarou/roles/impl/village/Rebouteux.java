package fr.enoe.loupgarou.roles.impl.village;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import java.util.*;

/**
 * Rebouteux (doc UHCWorld).
 * - /lg guerir <pseudo> : applique une protection à un joueur (max 3 fois, ciblé non averti).
 *   Quand ce joueur passe sous 4♥ → soigné automatiquement de 3♥.
 * - Lui-même : mange une pomme en or → +3♥ au lieu de +2♥.
 */
public class Rebouteux extends Role implements Listener {

    private static final int MAX_GUERISONS = 3;

    // UUID protégé → nombre de charges restantes (1 guérison par application)
    private final Map<UUID, Integer> proteges = new HashMap<>();
    private int guerisonsRestantes = MAX_GUERISONS;

    public Rebouteux(LoupGarouPlugin p, UUID u) { super(p, u); }

    @Override public String getDisplayName() { return "§aRebouteux"; }
    @Override public String getDescription() {
        return "§7/lg guerir <pseudo> (3×) → auto-soin 3♥ sous 4♥. Pomme en or → +3♥ au lieu de 2♥.";
    }
    @Override public RoleFamily getFamily() { return RoleFamily.VILLAGE; }
    @Override public String getId()         { return "rebouteux"; }

    @Override
    public void onGameStart(Player player) {
        // Enregistrer le listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void onNightTick(Player player) {
        // Vérifier si un protégé est sous le seuil
        checkProteges();
    }

    private void checkProteges() {
        Iterator<Map.Entry<UUID, Integer>> it = proteges.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> entry = it.next();
            Player target = Bukkit.getPlayer(entry.getKey());
            if (target == null || !plugin.getGameManager().isAlive(entry.getKey())) {
                it.remove(); continue;
            }
            if (target.getHealth() < 8.0) { // < 4♥ = < 8 hp
                double newHp = Math.min(target.getHealth() + 6.0,
                    target.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());
                target.setHealth(newHp);
                // Pas de message au ciblé selon la doc
                int charges = entry.getValue() - 1;
                if (charges <= 0) it.remove();
                else entry.setValue(charges);
            }
        }
    }

    /** Détection des pommes en or mangées par le Rebouteux */
    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        if (!event.getPlayer().getUniqueId().equals(playerUUID)) return;
        Material mat = event.getItem().getType();
        if (mat != Material.GOLDEN_APPLE) return;
        if (!plugin.getGameManager().isAlive(playerUUID)) return;

        // La pomme vanilla donne +4hp (+2♥) — on annule et donne +6hp (+3♥)
        // On laisse la pomme se manger normalement, puis on ajoute 1♥ supplémentaire
        Player p = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            double newHp = Math.min(p.getHealth() + 2.0,
                p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());
            p.setHealth(newHp);
            p.sendMessage(MessageUtils.success("Pomme en or — +3♥ (bonus Rebouteux) !"));
        }, 1L);
    }

    @Override
    public boolean onPowerCommand(Player player, String[] args) {
        if (!args[0].equalsIgnoreCase("guerir")) return false;
        if (args.length < 2) {
            player.sendMessage(MessageUtils.error("Usage : /lg guerir <pseudo>")); return true;
        }
        if (guerisonsRestantes <= 0) {
            player.sendMessage(MessageUtils.error("Tu as déjà utilisé tes 3 guérisons !")); return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(MessageUtils.error("Joueur introuvable ou hors ligne.")); return true;
        }
        if (target.getUniqueId().equals(playerUUID)) {
            player.sendMessage(MessageUtils.error("Tu ne peux pas te guérir toi-même.")); return true;
        }
        if (!plugin.getGameManager().isAlive(target.getUniqueId())) {
            player.sendMessage(MessageUtils.error(target.getName() + " est mort.")); return true;
        }

        proteges.put(target.getUniqueId(), proteges.getOrDefault(target.getUniqueId(), 0) + 1);
        guerisonsRestantes--;
        // Le ciblé n'est PAS averti (doc)
        player.sendMessage(MessageUtils.success(
            "Protection accordée à §e" + target.getName() + "§a. (" + guerisonsRestantes + " guérison(s) restante(s))"));
        return true;
    }
}
