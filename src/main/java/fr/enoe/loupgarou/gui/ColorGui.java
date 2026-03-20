package fr.enoe.loupgarou.gui;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.managers.ColorManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * ColorGui — interface inventaire pour /lg color.
 *
 * Page 1 : Liste des joueurs en vie (cliquer → ouvre la page couleurs).
 * Page 2 : Palette de laines (16 couleurs) → cliquer pour assigner.
 *
 * Chaque joueur a sa propre vue — les autres ne voient pas ces couleurs.
 */
public class ColorGui implements Listener {

    private final LoupGarouPlugin plugin;

    // UUID viewer → UUID cible sélectionnée (pour la page 2)
    private final Map<UUID, UUID> pendingTarget = new HashMap<>();
    // Inventaires ouverts → pour identifier nos GUI
    private final Set<UUID> openInventories = new HashSet<>();

    // Mapping couleur Minecraft → laine + ChatColor
    private static final List<ColorEntry> COLORS = List.of(
        new ColorEntry(DyeColor.WHITE,      Material.WHITE_WOOL,       ChatColor.WHITE,         "§fBlanc"),
        new ColorEntry(DyeColor.LIGHT_GRAY, Material.LIGHT_GRAY_WOOL,  ChatColor.GRAY,          "§7Gris clair"),
        new ColorEntry(DyeColor.GRAY,       Material.GRAY_WOOL,        ChatColor.DARK_GRAY,     "§8Gris foncé"),
        new ColorEntry(DyeColor.BLACK,      Material.BLACK_WOOL,       ChatColor.BLACK,         "§0Noir"),
        new ColorEntry(DyeColor.RED,        Material.RED_WOOL,         ChatColor.RED,           "§cRouge"),
        new ColorEntry(DyeColor.ORANGE,     Material.ORANGE_WOOL,      ChatColor.GOLD,          "§6Orange"),
        new ColorEntry(DyeColor.YELLOW,     Material.YELLOW_WOOL,      ChatColor.YELLOW,        "§eJaune"),
        new ColorEntry(DyeColor.LIME,       Material.LIME_WOOL,        ChatColor.GREEN,         "§aVert clair"),
        new ColorEntry(DyeColor.GREEN,      Material.GREEN_WOOL,       ChatColor.DARK_GREEN,    "§2Vert foncé"),
        new ColorEntry(DyeColor.CYAN,       Material.CYAN_WOOL,        ChatColor.DARK_AQUA,     "§3Cyan"),
        new ColorEntry(DyeColor.LIGHT_BLUE, Material.LIGHT_BLUE_WOOL,  ChatColor.AQUA,          "§bBleu clair"),
        new ColorEntry(DyeColor.BLUE,       Material.BLUE_WOOL,        ChatColor.DARK_BLUE,     "§1Bleu foncé"),
        new ColorEntry(DyeColor.PURPLE,     Material.PURPLE_WOOL,      ChatColor.DARK_PURPLE,   "§5Violet"),
        new ColorEntry(DyeColor.MAGENTA,    Material.MAGENTA_WOOL,     ChatColor.LIGHT_PURPLE,  "§dRose"),
        new ColorEntry(DyeColor.PINK,       Material.PINK_WOOL,        ChatColor.LIGHT_PURPLE,  "§dRose clair"),
        new ColorEntry(DyeColor.BROWN,      Material.BROWN_WOOL,       ChatColor.DARK_RED,      "§4Marron")
    );

    public ColorGui(LoupGarouPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // ─── PAGE 1 : liste des joueurs ──────────────────────────────────────────

    public void openPlayerList(Player viewer) {
        List<UUID> alive = new ArrayList<>(plugin.getGameManager().getAlivePlayers());
        alive.remove(viewer.getUniqueId()); // pas soi-même

        int size = Math.max(9, ((alive.size() / 9) + 1) * 9);
        Inventory inv = Bukkit.createInventory(null, Math.min(size, 54),
            "§6🎨 Couleurs des joueurs");

        for (int i = 0; i < alive.size() && i < 54; i++) {
            UUID targetUUID = alive.get(i);
            Player target = Bukkit.getPlayer(targetUUID);
            if (target == null) continue;

            // Tête du joueur
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            org.bukkit.inventory.meta.SkullMeta meta =
                (org.bukkit.inventory.meta.SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(target);
                ChatColor current = plugin.getColorManager().getColor(viewer.getUniqueId(), targetUUID);
                meta.setDisplayName(current + target.getName());
                meta.setLore(List.of("§7Cliquer pour changer la couleur",
                    "§7Couleur actuelle : " + current + current.name()));
                head.setItemMeta(meta);
            }
            inv.setItem(i, head);
        }

        openInventories.add(viewer.getUniqueId());
        viewer.openInventory(inv);
    }

    // ─── PAGE 2 : palette de couleurs ────────────────────────────────────────

    private void openColorPalette(Player viewer, UUID targetUUID) {
        pendingTarget.put(viewer.getUniqueId(), targetUUID);
        Player target = Bukkit.getPlayer(targetUUID);
        String targetName = target != null ? target.getName() : "Joueur";

        Inventory inv = Bukkit.createInventory(null, 27,
            "§6🎨 Couleur pour §r" + targetName);

        for (int i = 0; i < COLORS.size(); i++) {
            ColorEntry entry = COLORS.get(i);
            ItemStack wool = new ItemStack(entry.material);
            ItemMeta meta = wool.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(entry.label);
                meta.setLore(List.of("§7Cliquer pour appliquer"));
                wool.setItemMeta(meta);
            }
            inv.setItem(i, wool);
        }

        // Bouton retour (slot 26)
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bm = back.getItemMeta();
        if (bm != null) { bm.setDisplayName("§7← Retour"); back.setItemMeta(bm); }
        inv.setItem(26, back);

        openInventories.add(viewer.getUniqueId());
        viewer.openInventory(inv);
    }

    // ─── ÉVÉNEMENTS ──────────────────────────────────────────────────────────

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player viewer)) return;
        if (!openInventories.contains(viewer.getUniqueId())) return;
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        String title = event.getView().getTitle();

        if (title.startsWith("§6🎨 Couleurs des joueurs")) {
            // Page 1 : clic sur une tête → ouvrir palette
            if (clicked.getType() == Material.PLAYER_HEAD) {
                org.bukkit.inventory.meta.SkullMeta sm =
                    (org.bukkit.inventory.meta.SkullMeta) clicked.getItemMeta();
                if (sm != null && sm.getOwningPlayer() != null) {
                    UUID targetUUID = sm.getOwningPlayer().getUniqueId();
                    Bukkit.getScheduler().runTask(plugin, () -> openColorPalette(viewer, targetUUID));
                }
            }
        } else if (title.startsWith("§6🎨 Couleur pour")) {
            // Page 2 : clic sur une laine → appliquer couleur
            if (clicked.getType() == Material.ARROW) {
                // Retour
                Bukkit.getScheduler().runTask(plugin, () -> openPlayerList(viewer));
                return;
            }
            UUID targetUUID = pendingTarget.get(viewer.getUniqueId());
            if (targetUUID == null) return;

            for (ColorEntry entry : COLORS) {
                if (clicked.getType() == entry.material) {
                    plugin.getColorManager().setColor(viewer.getUniqueId(), targetUUID, entry.chatColor);
                    Player target = Bukkit.getPlayer(targetUUID);
                    String tName = target != null ? target.getName() : "ce joueur";
                    viewer.sendMessage("§6[Couleur] §r" + entry.label + " §7appliqué à §r"
                        + entry.chatColor + tName);
                    // Mettre à jour le scoreboard
                    plugin.getScoreboardManager().update(viewer);
                    Bukkit.getScheduler().runTask(plugin, () -> openPlayerList(viewer));
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        openInventories.remove(event.getPlayer().getUniqueId());
    }

    // ─── CLASSE INTERNE ──────────────────────────────────────────────────────

    private record ColorEntry(DyeColor dye, Material material, ChatColor chatColor, String label) {}
}
