package fr.enoe.loupgarou.roles.impl.special;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.UUID;

/**
 * Napoléon Iᵉʳ — transformation révolutionnaire de l'Inconnu.
 *
 * Effets permanents : Speed I, Force II, Résistance I, Chance 255
 * Épée Netherite Tranchant IV
 * /lg Marengo : invoque le destrier gris Speed II, taille x1.5, usage exclusif
 * Pommes dorées : +3 cœurs de vie ET +3 cœurs d'absorption
 */
public class Napoleon extends Role {

    private UUID marengoUUID = null;

    public Napoleon(LoupGarouPlugin plugin, UUID playerUUID) {
        super(plugin, playerUUID);
    }

    @Override public String getDisplayName() { return "§bNapoléon Iᵉʳ"; }
    @Override public String getDescription()  { return "L'Empereur de France — invincible, rapide, éternel."; }
    @Override public RoleFamily getFamily()   { return RoleFamily.VILLAGE; }
    @Override public String getId()           { return "napoleon"; }

    // ─── TRANSFORMATION ──────────────────────────────────────────────────────

    public void applyTransformation(Player player) {
        // Effets permanents
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,             Integer.MAX_VALUE, 0,   false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,   Integer.MAX_VALUE, 1,   false, false)); // Force II
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 0,   false, false)); // Résistance I
        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK,              Integer.MAX_VALUE, 254, false, false)); // Chance 255

        // Épée Netherite Tranchant IV
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = sword.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§b§lÉpée d'Austerlitz");
            meta.setLore(List.of(
                "§8« Impossible n'est pas français. »",
                "§7Tranchant IV — forgée dans le feu de la victoire."
            ));
            meta.addEnchant(Enchantment.SHARPNESS, 4, true);
            sword.setItemMeta(meta);
        }
        player.getInventory().addItem(sword);

        // ── Le Poème de l'Avènement ───────────────────────────────────────
        player.sendMessage("");
        player.sendMessage("§6§m                                                  ");
        player.sendMessage("§e§l         ✦ L'EMPEREUR EST DE RETOUR ✦");
        player.sendMessage("§6§m                                                  ");
        player.sendMessage("");
        player.sendMessage("§fDu fond de l'exil, à travers la tempête,");
        player.sendMessage("§fLe soleil d'Austerlitz rallume sa conquête.");
        player.sendMessage("§fL'aigle a repris son vol, les canons ont tonné —");
        player.sendMessage("§fNapoléon est vivant, et rien n'est terminé.");
        player.sendMessage("");
        player.sendMessage("§7Les empires chancellent, les rois fuient, tremblants,");
        player.sendMessage("§7Car l'Homme au bicorne marche à nouveau dedans.");
        player.sendMessage("§7La France dans le cœur, la victoire dans la main,");
        player.sendMessage("§7Il n'est pas de muraille qui résiste à son destin.");
        player.sendMessage("");
        player.sendMessage("§b§l✦ Effets  : §r§bSpeed I · Force II · Résistance I · Chance 255");
        player.sendMessage("§b§l✦ Arme    : §r§bÉpée d'Austerlitz §7(Tranchant IV)");
        player.sendMessage("§b§l✦ Monture : §r§b/lg marengo §7— ton destrier gris t'attend.");
        player.sendMessage("§b§l✦ Pommes  : §r§b+3♥ de vie et +3♥ d'absorption.");
        player.sendMessage("");
        player.sendMessage("§6§o« La mort n'est rien, mais vivre vaincu et sans gloire, c'est mourir tous les jours. »");
        player.sendMessage("");

        // Titre à l'écran
        player.sendTitle(
            "§b§l⚔ NAPOLÉON Iᵉʳ ⚔",
            "§eL'Aigle reprend son envol.",
            10, 140, 30
        );

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 0.8f);
    }

    // ─── COMMANDE /lg marengo ────────────────────────────────────────────────

    public boolean summonMarengo(Player player) {
        // Supprimer l'ancien Marengo
        if (marengoUUID != null) {
            Entity old = Bukkit.getEntity(marengoUUID);
            if (old != null && old.isValid()) old.remove();
            marengoUUID = null;
        }

        Horse horse = (Horse) player.getWorld().spawnEntity(player.getLocation(), EntityType.HORSE);

        // ── Robe grise, pas d'invisibilité ──────────────────────────────────
        horse.setColor(Horse.Color.GRAY);
        horse.setStyle(Horse.Style.NONE);
        horse.setCustomName("§f§lMarengo");
        horse.setCustomNameVisible(true);

        // ── Statistiques ────────────────────────────────────────────────────
        horse.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(40.0);
        horse.setHealth(40.0);
        horse.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.45); // Speed II

        // ── Taille x1.5 (API 1.20.5+, ignorée si non disponible) ────────────
        try {
            var scaleAttr = horse.getAttribute(org.bukkit.attribute.Attribute.GENERIC_SCALE);
            if (scaleAttr != null) scaleAttr.setBaseValue(1.5);
        } catch (Exception ignored) {}

        // ── Selle ────────────────────────────────────────────────────────────
        horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));

        marengoUUID = horse.getUniqueId();

        player.sendMessage("");
        player.sendMessage("§f§l          ✦ Marengo ✦");
        player.sendMessage("§7  Il piaffe et hennit, fidèle à son maître —");
        player.sendMessage("§7  Le destrier gris que nulle mort ne peut abattre.");
        player.sendMessage("§7  Monte, Empereur. Le champ de bataille t'attend.");
        player.sendMessage("");
        player.playSound(player.getLocation(), Sound.ENTITY_HORSE_AMBIENT, 1f, 0.8f);
        return true;
    }

    public boolean isMarengo(UUID entityUUID) {
        return marengoUUID != null && entityUUID.equals(marengoUUID);
    }

    // ─── POMME DORÉE — +3 vie ET +3 absorption ──────────────────────────────

    /**
     * Remplace ENTIÈREMENT la logique standard de la pomme dorée pour Napoléon.
     * +3 cœurs de vie (6 HP) et +3 cœurs d'absorption (6 HP).
     * Appelé depuis GoldenAppleListener après avoir annulé la logique vanilla.
     */
    public void applyNapoleonAppleBonus(Player player) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // +3 cœurs de vie = +6 HP
            double maxHp = 20.0;
            var attr = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
            if (attr != null) maxHp = attr.getBaseValue();
            double newHp = Math.min(maxHp, player.getHealth() + 6.0);
            player.setHealth(newHp);

            // +3 cœurs d'absorption = 6 HP
            // setAbsorptionAmount disponible depuis Spigot 1.15+
            player.removePotionEffect(PotionEffectType.ABSORPTION);
            try {
                // Donner l'effet absorption pour la durée, puis forcer le montant
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 120, 0, false, false), true);
                player.setAbsorptionAmount(6.0); // 6 HP = 3 cœurs
            } catch (Exception ignored) {
                // Fallback : amplifier 0 donne 4 HP (2 cœurs) — acceptable
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 120, 0, false, false), true);
            }

            player.sendMessage("§6[Pomme Impériale] §f+3♥ de vie §7| §6+3♥ d'absorption");
            player.sendMessage("§8§o         « L'Empereur ne faiblit pas. »");
        }, 1L);
    }

    @Override
    public boolean onPowerCommand(Player player, String[] args) {
        if (args[0].equalsIgnoreCase("marengo")) {
            return summonMarengo(player);
        }
        return false;
    }

    public UUID getMarengoUUID() { return marengoUUID; }
}
