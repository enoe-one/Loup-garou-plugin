package fr.enoe.loupgarou.roles.impl.village;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * Bienfaiteur (doc UHCWorld).
 * - Possède 2 livres Protection II dans l'inventaire au démarrage.
 * - /lg conferer <pseudo> : donne +1♥ permanent à un joueur différent (max 3 joueurs, 1×/5min).
 *   Le don est appliqué 3 minutes après la commande. Le ciblé n'est pas averti immédiatement.
 * - Après 3 dons consommés → Régénération lente (1♥/min = Regeneration I amp 0 toutes les 50s).
 */
public class Bienfaiteur extends Role {

    private static final int MAX_DONS = 3;
    private static final long COOLDOWN_TICKS = 20L * 60 * 5;  // 5 min
    private static final long DELAI_TICKS    = 20L * 60 * 3;  // 3 min

    private final Set<UUID> dejaCibles = new HashSet<>();
    private int donsRestants = MAX_DONS;
    private long lastDonTick = -9999L;
    private boolean regenActive = false;

    public Bienfaiteur(LoupGarouPlugin p, UUID u) { super(p, u); }

    @Override public String getDisplayName() { return "§aBienfaiteur"; }
    @Override public String getDescription() {
        return "§72 livres Prot II. /lg conferer <pseudo> → +1♥ permanent (3 joueurs, 1×/5min, délai 3min). Après 3 dons : Régén lente.";
    }
    @Override public RoleFamily getFamily() { return RoleFamily.VILLAGE; }
    @Override public String getId()         { return "bienfaiteur"; }

    @Override
    public void onGameStart(Player player) {
        // Donner 2 livres Protection II
        for (int i = 0; i < 2; i++) {
            ItemStack livre = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta meta = (BookMeta) livre.getItemMeta();
            if (meta != null) {
                meta.setTitle("§9Protection II");
                meta.setAuthor("Bienfaiteur");
                meta.addPage("§7Ce livre contient le savoir\nde la §9Protection II§7.\n\n§8(Objet décoratif — la protection est donnée via /lg conferer)");
                livre.setItemMeta(meta);
            }
            player.getInventory().addItem(livre);
        }
    }

    @Override
    public void onNightTick(Player player) {
        if (regenActive) {
            // Régénération lente ~1♥/min : Regeneration I avec très long uptime
            // Regen I = 1♥ toutes les 2.5s à 100% uptime → trop rapide
            // On applique 2 secondes sur 50 secondes → ~1♥/min approximatif
            long currentTick = plugin.getGameManager().getElapsedSeconds() * 20L;
            if (currentTick % 1000 < 40) { // ~2 secondes toutes les 50 secondes
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 0, false, false, false));
            }
        }
    }

    @Override
    public boolean onPowerCommand(Player player, String[] args) {
        if (!args[0].equalsIgnoreCase("conferer")) return false;
        if (args.length < 2) {
            player.sendMessage(MessageUtils.error("Usage : /lg conferer <pseudo>")); return true;
        }
        if (donsRestants <= 0) {
            player.sendMessage(MessageUtils.error("Tu as déjà utilisé tes 3 dons !")); return true;
        }

        long nowTick = Bukkit.getCurrentTick();
        if (nowTick - lastDonTick < COOLDOWN_TICKS) {
            long remaining = (COOLDOWN_TICKS - (nowTick - lastDonTick)) / 20;
            player.sendMessage(MessageUtils.error("Pouvoir en recharge — " + remaining + "s restantes.")); return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(MessageUtils.error("Joueur introuvable ou hors ligne.")); return true;
        }
        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(MessageUtils.error("Tu ne peux pas te conférer à toi-même.")); return true;
        }
        if (dejaCibles.contains(target.getUniqueId())) {
            player.sendMessage(MessageUtils.error("Tu as déjà béni " + target.getName() + ".")); return true;
        }
        if (!plugin.getGameManager().isAlive(target.getUniqueId())) {
            player.sendMessage(MessageUtils.error(target.getName() + " est mort.")); return true;
        }

        UUID targetUUID = target.getUniqueId();
        dejaCibles.add(targetUUID);
        donsRestants--;
        lastDonTick = nowTick;

        player.sendMessage(MessageUtils.success(
            "Don accordé à §e" + target.getName() + "§a — il recevra +1♥ dans 3 minutes. (" + donsRestants + " don(s) restant(s))"));

        // Appliquer le don 3 minutes plus tard
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player t = Bukkit.getPlayer(targetUUID);
            if (t != null && plugin.getGameManager().isAlive(targetUUID)) {
                double newMax = t.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue() + 2.0;
                t.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(newMax);
                t.setHealth(Math.min(t.getHealth() + 2.0, newMax));
                // Pas de message au ciblé selon la doc
            }
        }, DELAI_TICKS);

        // Activer la regen si tous les dons sont consommés
        if (donsRestants <= 0) {
            regenActive = true;
            player.sendMessage(MessageUtils.success("Tous tes dons sont distribués — tu bénéficies maintenant d'une Régénération lente."));
        }
        return true;
    }
}
