package fr.enoe.loupgarou.gui;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.managers.MessageManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;

import java.util.*;

public class SettingsGUI implements Listener {

    private final LoupGarouPlugin plugin;

    // Pages
    public enum Page { MAIN, ROLES_VILLAGE, ROLES_LOUP, ROLES_SOLI, ROLES_BINAIRE, EVENTS, PARAMS }

    private final Map<UUID, Page> openPages = new HashMap<>();

    public SettingsGUI(LoupGarouPlugin plugin) {
        this.plugin = plugin;
    }

    // ─── OUVERTURE ──────────────────────────────────────────────────────────────

    public void open(Player player) {
        if (!plugin.getGameManager().isAdmin(player.getUniqueId())) {
            player.sendMessage("§cAccès refusé.");
            return;
        }
        openPages.put(player.getUniqueId(), Page.MAIN);
        player.openInventory(buildMain());
    }

    // ─── MENU PRINCIPAL ─────────────────────────────────────────────────────────

    private Inventory buildMain() {
        Inventory inv = Bukkit.createInventory(null, 54, "§6⚙ Paramètres — Loup-Garou");
        fill(inv, 0, 53, glass(Material.BLACK_STAINED_GLASS_PANE, " "));

        // Titre décoratif
        inv.setItem(4, named(Material.NETHER_STAR, "§e§lConfiguration de la partie",
                "§7Clique sur une catégorie pour la configurer.", ""));

        // Rôles Village
        inv.setItem(19, head("§a§lRôles Village",
                List.of("§7Activer/désactiver les rôles villageois.", "§7" + countEnabled("village") + " actifs"),
                "VILLAGE_BANNER", Material.GREEN_BANNER));

        // Rôles Loups
        inv.setItem(21, head("§c§lRôles Loups-Garous",
                List.of("§7Activer/désactiver les loups.", "§7" + countEnabled("loup") + " actifs"),
                "WOLF_BANNER", Material.RED_BANNER));

        // Rôles Solitaires
        inv.setItem(23, head("§6§lRôles Solitaires",
                List.of("§7Activer/désactiver les solitaires.", "§7" + countEnabled("soli") + " actifs"),
                "SOLI_BANNER", Material.ORANGE_BANNER));

        // Rôles Binaires
        inv.setItem(25, head("§5§lRôles Binaires",
                List.of("§7Activer/désactiver les binaires.", "§7" + countEnabled("binaire") + " actifs"),
                "BIN_BANNER", Material.PURPLE_BANNER));

        // Événements
        inv.setItem(38, named(Material.LIGHTNING_ROD, "§b§lÉvénements spéciaux",
                "§7Activer les événements (Nuit Sanglante,", "§7Trublionage, Élection, Exposé)"));

        // Paramètres généraux
        inv.setItem(42, named(Material.COMPARATOR, "§e§lParamètres généraux",
                "§7Nombre de loups, couple, PVP,", "§7composition visible, etc."));

        // Lancer la partie (si admin/owner)
        inv.setItem(49, named(Material.FIREWORK_ROCKET, "§a§l▶ Lancer la partie",
                "§7Lance la partie avec la configuration actuelle."));

        return inv;
    }

    // ─── PAGE RÔLES ─────────────────────────────────────────────────────────────

    private Inventory buildRolesPage(String family) {
        String title = switch (family) {
            case "village" -> "§a⚔ Rôles Village";
            case "loup"    -> "§c🐺 Rôles Loups-Garous";
            case "soli"    -> "§6✦ Rôles Solitaires";
            case "binaire" -> "§5⚖ Rôles Binaires";
            default        -> "§fRôles";
        };
        Inventory inv = Bukkit.createInventory(null, 54, title);
        fill(inv, 0, 53, glass(Material.BLACK_STAINED_GLASS_PANE, " "));

        List<RoleEntry> entries = getRoleEntries(family);
        int[] slots = {10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34};
        for (int i = 0; i < entries.size() && i < slots.length; i++) {
            RoleEntry e = entries.get(i);
            boolean enabled = plugin.getConfig().getBoolean("roles." + e.id + ".enabled", true);
            Material mat = enabled ? e.onMat : Material.BARRIER;
            String status = enabled ? "§a✔ Activé" : "§c✘ Désactivé";
            String color  = enabled ? "§a" : "§c";
            inv.setItem(slots[i], named(mat, color + e.name,
                    "§7" + e.desc, "", status, "§8Clic pour changer"));
        }

        // Bouton retour
        inv.setItem(49, named(Material.ARROW, "§f◀ Retour", "§7Retour au menu principal"));
        return inv;
    }

    // ─── PAGE ÉVÉNEMENTS ────────────────────────────────────────────────────────

    private Inventory buildEventsPage() {
        Inventory inv = Bukkit.createInventory(null, 54, "§b⚡ Événements spéciaux");
        fill(inv, 0, 53, glass(Material.BLACK_STAINED_GLASS_PANE, " "));

        record Evt(String id, String name, String desc, Material mat) {}
        List<Evt> events = List.of(
            new Evt("bloody_night",   "Nuit Sanglante",  "Morts révélées seulement en fin de nuit.",  Material.REDSTONE_TORCH),
            new Evt("trublionage",    "Trublionage",     "Échange 2 rôles aléatoires au 2e tour.",    Material.REPEATER),
            new Evt("election",       "Élection",        "Le Maire est révélé publiquement.",         Material.GOLDEN_HELMET),
            new Evt("expose",         "Exposé",          "Un rôle précis caché parmi 3-4 joueurs.",   Material.SPYGLASS)
        );
        int[] slots = {20, 22, 24, 31};
        for (int i = 0; i < events.size(); i++) {
            Evt e = events.get(i);
            boolean on = plugin.getConfig().getBoolean("events." + e.id, false);
            Material mat = on ? e.mat : Material.BARRIER;
            String status = on ? "§a✔ Activé" : "§c✘ Désactivé";
            inv.setItem(slots[i], named(mat, (on ? "§a" : "§c") + e.name,
                    "§7" + e.desc, "", status, "§8Clic pour changer"));
        }
        inv.setItem(49, named(Material.ARROW, "§f◀ Retour", ""));
        return inv;
    }

    // ─── PAGE PARAMÈTRES GÉNÉRAUX ───────────────────────────────────────────────

    private Inventory buildParamsPage() {
        Inventory inv = Bukkit.createInventory(null, 54, "§e⚙ Paramètres généraux");
        fill(inv, 0, 53, glass(Material.BLACK_STAINED_GLASS_PANE, " "));

        int wolves = plugin.getConfig().getInt("game.wolves", 2);
        boolean couple       = plugin.getConfig().getBoolean("game.couple", true);
        boolean trouple      = plugin.getConfig().getBoolean("game.trouple", false);
        boolean compVisible  = plugin.getConfig().getBoolean("game.composition-visible", false);
        boolean mystery      = plugin.getConfig().getBoolean("events.mystery", false);
        boolean oreBoost     = plugin.getConfig().getBoolean("game.ore-boost", true);
        int maxDiamonds      = plugin.getConfig().getInt("game.max-diamonds", 25);

        // Nombre de loups
        inv.setItem(11, named(Material.SKELETON_SKULL, "§c§lNombre de Loups : §f" + wolves,
                "§7Clic gauche : +1    Clic droit : -1",
                "§7Actuel : §c" + wolves + " loup(s)"));

        // Couple
        inv.setItem(13, toggleItem(Material.PINK_DYE, "Couple aléatoire", couple,
                "Active le couple aléatoire à 25 min", "si Cupidon n'a pas lié de joueurs."));

        // Trouple
        inv.setItem(15, toggleItem(Material.MAGENTA_DYE, "Trouple (3 amoureux)", trouple,
                "Active le trouple si ≥9 joueurs."));

        // Composition visible
        inv.setItem(20, toggleItem(Material.BOOKSHELF, "Composition visible", compVisible,
                "Tous les joueurs voient les rôles présents."));

        // Événement mystère
        inv.setItem(22, toggleItem(Material.ENDER_EYE, "Événement mystère", mystery,
                "Active un événement aléatoire en cours de partie."));

        // Boost minerais
        inv.setItem(24, toggleItem(Material.DIAMOND_PICKAXE, "Boost minerais ×1.8", oreBoost,
                "Drop bonus sur tous les minerais (×1.8)."));

        // Max diamants
        inv.setItem(29, named(Material.DIAMOND, "§b§lMax diamants : §f" + maxDiamonds,
                "§7Clic gauche : +5    Clic droit : -5",
                "§7Actuel : §b" + maxDiamonds + " diamants max"));

        // PVP delay
        int pvpDelay = plugin.getConfig().getInt("game.pvp-delay", 1200) / 60;
        inv.setItem(31, named(Material.IRON_SWORD, "§e§lPVP activé après : §f" + pvpDelay + " min",
                "§7Clic gauche : +5 min    Clic droit : -5 min",
                "§7Actuel : §e" + pvpDelay + " minutes"));

        // Revive avant X min
        int reviveLimit = plugin.getConfig().getInt("game.revive-limit", 1800) / 60;
        inv.setItem(33, named(Material.TOTEM_OF_UNDYING, "§a§lRevive avant : §f" + reviveLimit + " min",
                "§7Clic gauche : +5 min    Clic droit : -5 min",
                "§7Actuel : §a" + reviveLimit + " minutes"));

        inv.setItem(49, named(Material.ARROW, "§f◀ Retour", ""));
        return inv;
    }

    // ─── GESTION DES CLICS ──────────────────────────────────────────────────────

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();
        if (!title.contains("Loup-Garou") && !title.contains("Rôles") &&
            !title.contains("Événements") && !title.contains("Paramètres") &&
            !title.contains("Village") && !title.contains("Loups") &&
            !title.contains("Solitaires") && !title.contains("Binaires")) return;

        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;
        if (item.getType() == Material.BLACK_STAINED_GLASS_PANE) return;

        Page page = openPages.getOrDefault(player.getUniqueId(), Page.MAIN);
        String itemName = item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                ? ChatColor.stripColor(item.getItemMeta().getDisplayName()) : "";

        // ── Menu principal ──
        if (page == Page.MAIN) {
            if (itemName.contains("Village"))    { openPages.put(player.getUniqueId(), Page.ROLES_VILLAGE); player.openInventory(buildRolesPage("village")); }
            else if (itemName.contains("Loup"))  { openPages.put(player.getUniqueId(), Page.ROLES_LOUP);    player.openInventory(buildRolesPage("loup")); }
            else if (itemName.contains("Solit")) { openPages.put(player.getUniqueId(), Page.ROLES_SOLI);    player.openInventory(buildRolesPage("soli")); }
            else if (itemName.contains("Binai")) { openPages.put(player.getUniqueId(), Page.ROLES_BINAIRE); player.openInventory(buildRolesPage("binaire")); }
            else if (itemName.contains("vén"))  { openPages.put(player.getUniqueId(), Page.EVENTS);        player.openInventory(buildEventsPage()); }
            else if (itemName.contains("nements")) { openPages.put(player.getUniqueId(), Page.EVENTS);     player.openInventory(buildEventsPage()); }
            else if (itemName.contains("amètre") || itemName.contains("aramt")) { openPages.put(player.getUniqueId(), Page.PARAMS); player.openInventory(buildParamsPage()); }
            else if (itemName.contains("Lancer")) { player.closeInventory(); plugin.getGameManager().startGame(); }
            return;
        }

        // ── Retour ──
        if (itemName.contains("Retour") || item.getType() == Material.ARROW) {
            openPages.put(player.getUniqueId(), Page.MAIN);
            player.openInventory(buildMain());
            return;
        }

        // ── Pages rôles ──
        if (page == Page.ROLES_VILLAGE || page == Page.ROLES_LOUP ||
            page == Page.ROLES_SOLI    || page == Page.ROLES_BINAIRE) {

            String family = switch (page) {
                case ROLES_VILLAGE -> "village";
                case ROLES_LOUP    -> "loup";
                case ROLES_SOLI    -> "soli";
                case ROLES_BINAIRE -> "binaire";
                default            -> "";
            };
            // Trouver le rôle correspondant à l'item cliqué
            String strippedName = itemName.trim();
            for (RoleEntry e : getRoleEntries(family)) {
                if (strippedName.equalsIgnoreCase(e.name)) {
                    String path = "roles." + e.id + ".enabled";
                    boolean current = plugin.getConfig().getBoolean(path, true);
                    plugin.getConfig().set(path, !current);
                    plugin.saveConfig();
                    player.sendMessage("§7Rôle §e" + e.name + " : " + (!current ? "§aActivé" : "§cDésactivé"));
                    player.openInventory(buildRolesPage(family));
                    return;
                }
            }
        }

        // ── Page événements ──
        if (page == Page.EVENTS) {
            Map<String, String> evMap = Map.of(
                "Nuit Sanglante",  "events.bloody_night",
                "Trublionage",     "events.trublionage",
                "lection",        "events.election",
                "Expos",          "events.expose"
            );
            for (Map.Entry<String, String> entry : evMap.entrySet()) {
                if (itemName.contains(entry.getKey())) {
                    boolean cur = plugin.getConfig().getBoolean(entry.getValue(), false);
                    plugin.getConfig().set(entry.getValue(), !cur);
                    plugin.saveConfig();
                    player.sendMessage("§7Événement §e" + itemName + " : " + (!cur ? "§aActivé" : "§cDésactivé"));
                    player.openInventory(buildEventsPage());
                    return;
                }
            }
        }

        // ── Page paramètres ──
        if (page == Page.PARAMS) {
            boolean left = event.getClick() == ClickType.LEFT;
            if (itemName.contains("Loups")) {
                int v = plugin.getConfig().getInt("game.wolves", 2) + (left ? 1 : -1);
                plugin.getConfig().set("game.wolves", Math.max(1, v));
                plugin.saveConfig();
                player.openInventory(buildParamsPage());
            } else if (itemName.contains("Couple")) {
                toggle(player, "game.couple");
                player.openInventory(buildParamsPage());
            } else if (itemName.contains("Trouple")) {
                toggle(player, "game.trouple");
                player.openInventory(buildParamsPage());
            } else if (itemName.contains("Compos")) {
                toggle(player, "game.composition-visible");
                player.openInventory(buildParamsPage());
            } else if (itemName.contains("myst") || itemName.contains("nement myst")) {
                toggle(player, "events.mystery");
                player.openInventory(buildParamsPage());
            } else if (itemName.contains("Boost")) {
                toggle(player, "game.ore-boost");
                player.openInventory(buildParamsPage());
            } else if (itemName.contains("diamants")) {
                int v = plugin.getConfig().getInt("game.max-diamonds", 25) + (left ? 5 : -5);
                plugin.getConfig().set("game.max-diamonds", Math.max(5, v));
                plugin.saveConfig();
                player.openInventory(buildParamsPage());
            } else if (itemName.contains("PVP")) {
                int v = plugin.getConfig().getInt("game.pvp-delay", 1200) + (left ? 300 : -300);
                plugin.getConfig().set("game.pvp-delay", Math.max(300, v));
                plugin.saveConfig();
                player.openInventory(buildParamsPage());
            } else if (itemName.contains("Revive")) {
                int v = plugin.getConfig().getInt("game.revive-limit", 1800) + (left ? 300 : -300);
                plugin.getConfig().set("game.revive-limit", Math.max(300, v));
                plugin.saveConfig();
                player.openInventory(buildParamsPage());
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        openPages.remove(event.getPlayer().getUniqueId());
    }

    // ─── HELPERS ────────────────────────────────────────────────────────────────

    private void toggle(Player p, String path) {
        boolean v = plugin.getConfig().getBoolean(path, false);
        plugin.getConfig().set(path, !v);
        plugin.saveConfig();
        p.sendMessage("§7" + path + " : " + (!v ? "§aactivé" : "§cdésactivé"));
    }

    private ItemStack named(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack glass(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack toggleItem(Material mat, String label, boolean enabled, String... lore) {
        Material display = enabled ? mat : Material.BARRIER;
        String status = enabled ? "§a✔ Activé" : "§c✘ Désactivé";
        String[] fullLore = new String[lore.length + 2];
        for (int i = 0; i < lore.length; i++) fullLore[i] = "§7" + lore[i];
        fullLore[lore.length] = "";
        fullLore[lore.length + 1] = status + " §8(clic)";
        return named(display, (enabled ? "§a" : "§c") + label, fullLore);
    }

    private ItemStack head(String name, List<String> lore, String skullId, Material fallback) {
        // On utilise un bloc décoratif comme substitut de tête (les vraies PlayerSkulls
        // nécessitent une texture base64 spécifique)
        ItemStack item = new ItemStack(fallback);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private void fill(Inventory inv, int from, int to, ItemStack item) {
        for (int i = from; i <= to; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, item);
        }
    }

    private String countEnabled(String family) {
        int count = 0;
        for (RoleEntry e : getRoleEntries(family)) {
            if (plugin.getConfig().getBoolean("roles." + e.id + ".enabled", true)) count++;
        }
        return count + "/" + getRoleEntries(family).size();
    }

    // ─── LISTE DES RÔLES ────────────────────────────────────────────────────────

    private record RoleEntry(String id, String name, String desc, Material onMat) {}

    private List<RoleEntry> getRoleEntries(String family) {
        return switch (family) {
            case "village" -> List.of(
                new RoleEntry("maire",            "Maire",             "Vote double, décisif en égalité",         Material.GOLDEN_HELMET),
                new RoleEntry("citoyen",          "Citoyen",           "Vote double (sauf égalité)",              Material.IRON_HELMET),
                new RoleEntry("voyant",           "Voyant",            "Révèle le rôle d'un joueur 1x/épisode",   Material.ENDER_EYE),
                new RoleEntry("renard",           "Renard",            "Détecte un Loup parmi 3 voisins",         Material.ORANGE_DYE),
                new RoleEntry("conteuse",         "Conteuse",          "Voit qui a utilisé un pouvoir la nuit",   Material.BOOK),
                new RoleEntry("salvateur",        "Salvateur",         "Protège un joueur par nuit",              Material.SHIELD),
                new RoleEntry("sorciere",         "Sorcière",          "Potion vie + potion mort (1 chacune)",    Material.BREWING_STAND),
                new RoleEntry("chasseur",         "Chasseur",          "Tire sur quelqu'un en mourant",           Material.BOW),
                new RoleEntry("astronome",        "Astronome",         "Particules colorées toutes les 10 min",   Material.SPYGLASS),
                new RoleEntry("chevalier",        "Chevalier",         "Force 2 vs Loups jusqu'au 1er kill",      Material.IRON_SWORD),
                new RoleEntry("ancien",           "Ancien",            "Ressuscite si tué par les Loups",         Material.TOTEM_OF_UNDYING),
                new RoleEntry("simple_villagois", "Simple Villagois",  "Aucun pouvoir",                           Material.WHEAT),
                new RoleEntry("idiot",            "Idiot du Village",  "Ressuscite 1x si vote du village",        Material.PUMPKIN),
                new RoleEntry("soeur",            "Sœurs",             "Se connaissent, Résistance si proches",   Material.PINK_DYE),
                new RoleEntry("monteur_ours",     "Monteur d'Ours",    "Grogne le nb de loups proches",           Material.SALMON),
                new RoleEntry("petite_fille",     "Petite Fille",      "Voit le chat des Loups la nuit",          Material.LILY_OF_THE_VALLEY)
            );
            case "loup" -> List.of(
                new RoleEntry("loup_simple",       "Loup Simple",         "Loup de base",                             Material.BONE),
                new RoleEntry("loup_perfide",      "Loup Perfide",        "Invisible la nuit sans armure",            Material.GLASS),
                new RoleEntry("loup_endormi",      "Loup Endormi",        "Reçoit ses alliés après 1h",               Material.RED_BED),
                new RoleEntry("loup_vengeur",      "Loup Vengeur",        "Speed quand un loup meurt",                Material.FEATHER),
                new RoleEntry("grand_mechant_loup","Grand Méchant Loup",  "Vote double + RAGE",                       Material.IRON_AXE),
                new RoleEntry("infecte",           "Infecté Père Loups",  "Transforme un mort en Loup (1x)",          Material.ROTTEN_FLESH),
                new RoleEntry("loup_timide",       "Loup Timide",         "Fort seul, faible avec un autre loup",     Material.WHITE_DYE),
                new RoleEntry("loup_empoisonneur", "Loup Empoisonneur",   "Fausse les infos de 2 joueurs",            Material.FERMENTED_SPIDER_EYE)
            );
            case "soli" -> List.of(
                new RoleEntry("loup_blanc",  "Loup Blanc",        "Camouflé parmi les loups, gagne seul",  Material.SNOW_BLOCK),
                new RoleEntry("flutiste",    "Joueur de Flûte",   "Charme les joueurs par proximité",       Material.NOTE_BLOCK),
                new RoleEntry("ange",        "Ange",              "Gardien ou Déchu (condition unique)",    Material.FEATHER),
                new RoleEntry("feu_follet",  "Feu Follet",        "Invisible sans armure la nuit",          Material.TORCH),
                new RoleEntry("imitateur",   "Imitateur",         "Copie un rôle chaque nuit",              Material.CLOCK),
                new RoleEntry("assassin",    "Assassin",          "Force+Résistance perma, cache morts",    Material.NETHERITE_SWORD),
                new RoleEntry("inconnu",     "Inconnu",           "Vu Village par détection, Loup par les loups", Material.COMPASS)
            );
            case "binaire" -> List.of(
                new RoleEntry("cupidon",       "Cupidon",        "Lie 2 joueurs",                          Material.ARROW),
                new RoleEntry("enfant_sauvage","Enfant Sauvage", "Devient Loup si son mentor meurt",       Material.JUNGLE_SAPLING),
                new RoleEntry("trublion",      "Trublion",       "Échange 2 rôles entre 20-25 min",        Material.REPEATER),
                new RoleEntry("voleur",        "Voleur",         "Prend le rôle du 1er mort",              Material.DARK_OAK_TRAPDOOR),
                new RoleEntry("chien_loup",    "Chien-Loup",     "Choisit Village ou Loup",                Material.BONE_MEAL),
                new RoleEntry("ivrogne",       "Ivrogne",        "Reçoit un faux rôle",                    Material.HONEY_BOTTLE)
            );
            default -> List.of();
        };
    }
}
