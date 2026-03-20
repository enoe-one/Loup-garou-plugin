package fr.enoe.loupgarou.core;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.managers.CageManager;
import fr.enoe.loupgarou.managers.MessageManager;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.impl.loup.LoupEndormi;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class GameManager {

    private final LoupGarouPlugin plugin;
    private GameState state = GameState.WAITING;

    private UUID ownerUUID = null;
    private final Set<UUID> admins = new HashSet<>();
    private String roomName = null;

    // Joueurs en vie / morts
    private final Set<UUID> alivePlayers = new HashSet<>();
    private final Set<UUID> deadPlayers  = new HashSet<>();

    // Timers
    private BukkitTask mainTimer  = null;
    private BukkitTask nightTimer = null;
    private int elapsedSeconds    = 0;
    private int episodeNumber     = 0;

    // Nuit sanglante
    private boolean bloodyNight = false;
    private final List<String> nightDeathMessages = new ArrayList<>();

    // Diamants minés par joueur
    private final Map<UUID, Integer> diamondsMined = new HashMap<>();

    // ── Résurrections en attente (cage 10 secondes) ──────────────────────────
    // uuid -> données (stuff + loc de mort)
    private final Map<UUID, ResurrectionData> pendingResurrection = new HashMap<>();

    public GameManager(LoupGarouPlugin plugin) {
        this.plugin = plugin;
    }

    // ─── CRÉATION DE ROOM ───────────────────────────────────────────────────

    public boolean createRoom(Player player, String name) {
        if (state != GameState.WAITING) {
            player.sendMessage(MessageUtils.error("Une partie est déjà en cours ou créée."));
            return false;
        }
        if (ownerUUID != null) {
            player.sendMessage(MessageUtils.error("Une room existe déjà : " + roomName));
            return false;
        }

        ownerUUID = player.getUniqueId();
        roomName  = name;
        admins.add(ownerUUID);

        grantOwnerPerms(player);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setGameMode(GameMode.ADVENTURE);
        }
        plugin.getChatManager().setGlobalChatEnabled(false);
        plugin.getCageManager().buildCage();

        Location cageCenter = new Location(Bukkit.getWorlds().get(0), 0, 221, 0);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.teleport(cageCenter);
            alivePlayers.add(p.getUniqueId());
        }

        MessageUtils.broadcast("§6[LoupGarou] §eRoom §b" + name + "§e créée par §a" + player.getName() + "§e !");
        MessageUtils.broadcast("§7En attente du lancement par l'owner...");
        return true;
    }

    // ─── DÉMARRAGE ──────────────────────────────────────────────────────────

    public void startGame() {
        if (state != GameState.WAITING) return;
        if (alivePlayers.size() < 2 && !plugin.getTestManager().isTestMode()) {
            MessageUtils.broadcastToAdmins(plugin, "§cImpossible de démarrer : pas assez de joueurs !");
            return;
        }

        state = GameState.STARTING;
        episodeNumber  = 0;
        elapsedSeconds = 0;

        plugin.getRoleManager().assignRoles(new ArrayList<>(alivePlayers));

        // Les rôles sont assignés mais PAS révélés maintenant.
        // Ils seront révélés à 20 minutes via announceRoles().

        applyStartEffects();

        plugin.getMessageManager().startCountdown(() -> {
            state = GameState.RUNNING;
            scatterPlayers();
            giveStartKit();
            if (plugin.getConfig().getBoolean("events.puces", false))
                plugin.getPucesManager().schedulePuces();
            if (plugin.getConfig().getBoolean("events.skyssofrenie", false))
                plugin.getSkysofrenieManager().schedule();
            startMainTimer();
        });
    }

    // ─── TIMER PRINCIPAL ────────────────────────────────────────────────────

    private void startMainTimer() {
        mainTimer = new BukkitRunnable() {
            @Override
            public void run() {
                elapsedSeconds++;
                onTick();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void onTick() {
        // Épisode toutes les 20 min
        if (elapsedSeconds % 1200 == 0) {
            episodeNumber++;
            onEpisodeEnd();
        }

        // 20 min : PVP + annonce rôles
        if (elapsedSeconds == plugin.getConfig().getInt("game.pvp-delay", 1200)) {
            plugin.getChatManager().setGlobalChatEnabled(true);
            enablePvp();
            announceRoles();
            plugin.getMessageManager().announceRoleReveal();
        }

        // 25 min : Cupidon informe le couple en privé (pas d'annonce publique)
        if (elapsedSeconds == 1500) {
            // Créer le faux couple pour l'Ivrogne (si présent) avant la notification
            for (UUID uuid : alivePlayers) {
                var role = plugin.getRoleManager().getRole(uuid);
                if (role != null && role.getId().equals("ivrogne")) {
                    plugin.getCoupleManager().createFakeCouple(uuid, new ArrayList<>(alivePlayers));
                    break;
                }
            }
            // Notifier le vrai couple ET le faux couple Ivrogne en PRIVÉ — pas d'annonce publique
            plugin.getCoupleManager().notifyCouplePrivately();
        }

        // Loup Endormi : à 1h, il devient "awake" automatiquement via isAwake()
        // (LoupEndormi.isAwake() retourne true quand elapsedSeconds >= 3600)
        // On envoie juste un message d'annonce discret au loup concerné
        if (elapsedSeconds == 3600) {
            for (UUID uuid : alivePlayers) {
                Player p = Bukkit.getPlayer(uuid);
                if (p == null) continue;
                if (plugin.getRoleManager().getRole(uuid) instanceof fr.enoe.loupgarou.roles.impl.loup.LoupEndormi) {
                    p.sendMessage("§c§l[LG] Tu t'éveilles... Tu es un Loup-Garou !");
                    p.sendMessage("§7Tes alliés loups :");
                    for (UUID wolf : plugin.getRoleManager().getWolfList()) {
                        Player wp = Bukkit.getPlayer(wolf);
                        if (wp != null && !wolf.equals(uuid))
                            p.sendMessage("§c  - " + wp.getName());
                    }
                }
            }
        }

        // Astronome : toutes les 10 min
        if (elapsedSeconds % 600 == 0 && elapsedSeconds > 0) {
            plugin.getRoleManager().triggerAstronomeTick();
        }

        // Monteur d'ours : toutes les 20 min
        if (elapsedSeconds % 1200 == 0 && elapsedSeconds > 0) {
            plugin.getRoleManager().triggerBearTamerTick();
        }

        // ── 1h30 (5400s) : réduction de la bordure de map ─────────────────
        if (elapsedSeconds == 5400) {
            startBorderShrink();
        }

        checkWinCondition();
    }

    // ─── RÉDUCTION DE LA BORDURE ────────────────────────────────────────────

    /**
     * À 1h30 : la WorldBorder commence à se réduire de 1000→100 blocs sur 30 minutes.
     */
    private void startBorderShrink() {
        World world = Bukkit.getWorlds().get(0);
        WorldBorder border = world.getWorldBorder();
        border.setCenter(0, 0);
        // Réduire de 1000 à 100 blocs sur 1800 secondes (30 min)
        border.setSize(100, 1800);
        MessageUtils.broadcast("§c§l[LG] ⚠ La zone de jeu commence à se réduire ! ⚠");
        MessageUtils.broadcast("§7La bordure passera de §e1000§7 à §c100 blocs§7 en 30 minutes.");

        // Avertissement à 1h45 (zone déjà réduite de moitié)
        Bukkit.getScheduler().runTaskLater(plugin, () ->
            MessageUtils.broadcast("§c[LG] ⚠ La zone est à moitié réduite — restez au centre !"),
        18000L); // 15 min après = 1h45
    }

    private void onEpisodeEnd() {
        if (bloodyNight && !nightDeathMessages.isEmpty()) {
            MessageUtils.broadcast("§4§l[Nuit Sanglante] §rLes morts de la nuit :");
            for (String msg : nightDeathMessages) MessageUtils.broadcast(msg);
            nightDeathMessages.clear();
            bloodyNight = false;
        }
        plugin.getRoleManager().onEpisodeEnd(episodeNumber);
    }

    // ─── SCATTER ALÉATOIRE ──────────────────────────────────────────────────

    /**
     * Téléporte chaque joueur à une position aléatoire dans la map (rayon 50–450 blocs),
     * espacés d'au moins 30 blocs les uns des autres.
     * Cherche un sol solide en descendant depuis y=120.
     */
    /**
     * Donne le kit de départ UHC à chaque joueur vivant.
     * - 10 pommes dorées
     * - 10 livres vierges
     * - 64 steaks cuits
     */
    private void giveStartKit() {
        ItemStack goldenApples = new ItemStack(Material.GOLDEN_APPLE, 10);
        ItemStack books        = new ItemStack(Material.BOOK, 10);
        ItemStack steak        = new ItemStack(Material.COOKED_BEEF, 64);

        for (UUID uuid : alivePlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            p.getInventory().addItem(goldenApples.clone());
            p.getInventory().addItem(books.clone());
            p.getInventory().addItem(steak.clone());
            p.sendMessage("§6[Kit UHC] §710 pommes dorées §7| §610 livres §7| §a64 steaks cuits");
        }
        plugin.getLogger().info("[LG] Kit de départ distribué à " + alivePlayers.size() + " joueurs.");
    }

    private void scatterPlayers() {
        World world = Bukkit.getWorlds().get(0);
        java.util.Random rng = new java.util.Random();
        List<Location> usedSpots = new ArrayList<>();

        for (UUID uuid : new ArrayList<>(alivePlayers)) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;

            Location spot = findScatterSpot(world, rng, usedSpots);
            usedSpots.add(spot);
            p.teleport(spot);
            p.sendMessage("§6[LG] §7Tu as été téléporté aléatoirement sur la map !");
        }
        plugin.getLogger().info("[LG] Scatter effectué — " + alivePlayers.size() + " joueurs téléportés.");
    }

    private Location findScatterSpot(World world, java.util.Random rng, List<Location> used) {
        int minR = 50, maxR = 450;
        for (int attempt = 0; attempt < 100; attempt++) {
            double angle = rng.nextDouble() * 2 * Math.PI;
            int r = minR + rng.nextInt(maxR - minR);
            int x = (int)(Math.cos(angle) * r);
            int z = (int)(Math.sin(angle) * r);

            // Descendre depuis y=120 pour trouver le sol
            Location loc = new Location(world, x + 0.5, 120, z + 0.5);
            loc = findSurface(world, loc);
            if (loc == null) continue;

            // Vérifier espacement minimal (30 blocs)
            boolean tooClose = false;
            for (Location u : used) {
                if (u.distanceSquared(loc) < 30 * 30) { tooClose = true; break; }
            }
            if (!tooClose) return loc;
        }
        // Fallback : position basique si on ne trouve pas
        int x = minR + rng.nextInt(maxR - minR);
        Location fallback = new Location(world, x + 0.5, 120, 0.5);
        Location surf = findSurface(world, fallback);
        return surf != null ? surf : fallback;
    }

    private Location findSurface(World world, Location from) {
        int x = from.getBlockX(), z = from.getBlockZ();
        for (int y = 115; y > 40; y--) {
            org.bukkit.block.Block b     = world.getBlockAt(x, y, z);
            org.bukkit.block.Block above = world.getBlockAt(x, y + 1, z);
            org.bukkit.block.Block above2 = world.getBlockAt(x, y + 2, z);
            if (!b.isPassable() && above.isPassable() && above2.isPassable()) {
                return new Location(world, x + 0.5, y + 1, z + 0.5);
            }
        }
        return null;
    }

    // ─── MORT D'UN JOUEUR ───────────────────────────────────────────────────

    /**
     * Appelé depuis PlayerDeathListener quand un joueur meurt en partie.
     * v3 : TP en cage avec stuff → 10 sec pour ressusciter → sinon drop du stuff.
     */
    public void handlePlayerDeath(Player player) {
        UUID uuid = player.getUniqueId();
        if (!alivePlayers.contains(uuid)) return;

        // Pouvoirs de résurrection instantanée (Ancien, Idiot, etc.)
        if (plugin.getRoleManager().tryElderRevive(uuid))       return;
        if (plugin.getRoleManager().tryVillageIdiotRevive(uuid)) return;

        // Chasseur : pouvoir à la mort
        plugin.getRoleManager().triggerHunterDeath(player);

        // Mort effective
        alivePlayers.remove(uuid);
        deadPlayers.add(uuid);

        // Notifier GrandMechantLoup + Braconnier si c'est un loup qui meurt
        if (plugin.getRoleManager().isWolf(uuid)) {
            plugin.getRoleManager().onWolfDeath(uuid, player.getLocation().clone());
        }

        // Couple : si l'un meurt, l'autre aussi
        plugin.getCoupleManager().onPartnerDeath(uuid);

        // Message de mort
        UUID killerUUID = null;
        if (player.getLastDamageCause() instanceof org.bukkit.event.entity.EntityDamageByEntityEvent dmg
                && dmg.getDamager() instanceof Player killer) {
            killerUUID = killer.getUniqueId();
        }
        // Notifier le tueur (callback rôle)
        if (killerUUID != null) {
            Role killerRole = plugin.getRoleManager().getRole(killerUUID);
            Player killerPlayer = Bukkit.getPlayer(killerUUID);
            if (killerRole != null && killerPlayer != null) {
                killerRole.onPlayerKill(killerPlayer, uuid);
            }
        }
        String deathMsg = plugin.getMessageManager().buildDeathMessage(uuid, killerUUID, false);

        if (bloodyNight) {
            nightDeathMessages.add(deathMsg);
        } else {
            if (!plugin.getRoleManager().tryAssassinHideDeath(uuid)) {
                MessageUtils.broadcast(deathMsg);
            }
        }

        // ── v3 : toujours TP en cage avec stuff, 10 sec pour résurrection ─
        Location deathLoc = player.getLocation().clone();
        List<ItemStack> stuff = collectInventory(player);

        // Vider l'inventaire (on le conserve dans pendingResurrection)
        player.getInventory().clear();
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(new Location(player.getWorld(), 0, 221, 0)); // cage bedrock

        boolean canRevive = elapsedSeconds < 1800 || plugin.getTestManager().isTestMode();

        if (canRevive) {
            // Stocker les données pour la résurrection
            pendingResurrection.put(uuid, new ResurrectionData(stuff, deathLoc));
            player.sendMessage("§6[LG] §7Tu as §e10 secondes§7 pour être ressuscité !");
            player.sendMessage("§7La Sorcière ou l'Infecté peuvent te ramener à la vie.");

            // Timer 10 secondes : si pas de résurrection → drop du stuff à l'endroit de mort
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (pendingResurrection.containsKey(uuid)) {
                    ResurrectionData data = pendingResurrection.remove(uuid);
                    // Drop le stuff à l'endroit de la mort
                    for (ItemStack item : data.getItems()) {
                        if (item != null && item.getType() != Material.AIR) {
                            deathLoc.getWorld().dropItemNaturally(deathLoc, item);
                        }
                    }
                    player.sendMessage("§c[LG] Délai expiré — ton stuff a été déposé à ta position de mort.");
                }
            }, 200L); // 200 ticks = 10 secondes
        } else {
            // Après 30 min : mort définitive, drop immédiat du stuff
            for (ItemStack item : stuff) {
                if (item != null && item.getType() != Material.AIR) {
                    deathLoc.getWorld().dropItemNaturally(deathLoc, item);
                }
            }
            player.sendMessage("§c[LG] Mort définitive — ton stuff a été déposé à ta position de mort.");
        }

        checkWinCondition();
    }

    /**
     * Ressuscite un joueur encore en attente (Sorcière ou Infecté).
     * Retourne true si la résurrection a réussi.
     */
    public boolean resurrectPlayer(UUID uuid, Player resurrectedBy) {
        ResurrectionData data = pendingResurrection.remove(uuid);
        if (data == null) return false; // délai dépassé ou pas de joueur en attente

        Player p = Bukkit.getPlayer(uuid);
        if (p == null) return false;

        // Retirer de la liste des morts, remettre vivant
        deadPlayers.remove(uuid);
        alivePlayers.add(uuid);

        // Remettre en mode survie + TP à la position de mort
        p.setGameMode(GameMode.SURVIVAL);
        p.teleport(data.getDeathLocation());

        // Redonner le stuff
        for (ItemStack item : data.getItems()) {
            if (item != null && item.getType() != Material.AIR) {
                p.getInventory().addItem(item);
            }
        }

        // 2 cœurs à la résurrection
        p.setHealth(Math.min(4.0, p.getMaxHealth()));

        String who = resurrectedBy != null ? resurrectedBy.getName() : "un pouvoir";
        MessageUtils.broadcast("§a[LG] §e" + p.getName() + "§a a été ressuscité par §e" + who + "§a !");
        p.sendMessage("§a[LG] Tu as été ressuscité !");

        return true;
    }

    /**
     * Vérifie si un joueur est actuellement en attente de résurrection (dans les 10 sec).
     */
    public boolean isPendingResurrection(UUID uuid) {
        return pendingResurrection.containsKey(uuid);
    }

    /**
     * Collecte tout l'inventaire d'un joueur (armure + main + contenu).
     */
    private List<ItemStack> collectInventory(Player player) {
        List<ItemStack> items = new ArrayList<>();
        // Inventaire principal
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) items.add(item.clone());
        }
        // Armure
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && item.getType() != Material.AIR) items.add(item.clone());
        }
        return items;
    }

    // ─── CONDITIONS DE VICTOIRE ─────────────────────────────────────────────
    //
    //  v3 :
    //  - Village gagne : plus aucun Loup ET plus aucun Solitaire
    //  - Loups gagnent : plus aucun Villageois ET plus aucun Solitaire
    //  - Solitaire gagne : il est le SEUL joueur encore en vie
    //  - Joueur de Flûte : 100% des vivants charmés (condition propre)
    //
    private void checkWinCondition() {
        if (state != GameState.RUNNING) return;

        // ── Mode test : pas de condition de victoire automatique ─────────────
        if (plugin.getTestManager().isTestMode()) return;

        List<UUID> alive = new ArrayList<>(alivePlayers);
        if (alive.isEmpty()) {
            endGame("§7Personne ne gagne. Tout le monde est mort !");
            return;
        }

        long wolves    = alive.stream().filter(u -> plugin.getRoleManager().isWolf(u)).count();
        long villagers = alive.stream().filter(u -> plugin.getRoleManager().isVillager(u)).count();
        long solos     = alive.stream().filter(u -> plugin.getRoleManager().isSolitary(u)).count();

        // ── Solitaire gagne seul : 1 seul joueur vivant et c'est un solitaire ─
        if (alive.size() == 1 && solos == 1) {
            Player winner = Bukkit.getPlayer(alive.get(0));
            String name = winner != null ? winner.getName() : "Inconnu";
            endGame("§d§l" + name + " gagne seul ! (Solitaire)");
            return;
        }

        // ── Village gagne : zéro Loup ET zéro Solitaire restants ──────────
        if (wolves == 0 && solos == 0 && villagers > 0) {
            endGame("§a§lLe Village gagne !");
            return;
        }

        // ── Loups gagnent : zéro Villageois ET zéro Solitaire restants ────
        if (villagers == 0 && solos == 0 && wolves > 0) {
            endGame("§c§lLes Loups-Garous gagnent !");
            return;
        }

        // Note : le JdF gagne via la condition solitaire standard (dernier survivant)
    }

    public void endGame(String message) {
        state = GameState.ENDED;
        if (mainTimer != null) mainTimer.cancel();
        plugin.getMessageManager().announceGameEnd(message);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setGameMode(GameMode.SURVIVAL);
            p.teleport(new Location(Bukkit.getWorlds().get(0), 0, 221, 0));
        }
        plugin.getChatManager().setGlobalChatEnabled(true);
        resetGame();
    }

    private void resetGame() {
        ownerUUID = null;
        roomName  = null;
        admins.clear();
        alivePlayers.clear();
        deadPlayers.clear();
        diamondsMined.clear();
        pendingResurrection.clear();
        elapsedSeconds = 0;
        episodeNumber  = 0;
        bloodyNight    = false;
        nightDeathMessages.clear();
        plugin.getRoleManager().reset();
        plugin.getVoteManager().reset();
        plugin.getCoupleManager().reset();
        plugin.getColorManager().reset();
        plugin.getScoreboardManager().clearAll();
        plugin.getPucesManager().reset();
        // Remettre la bordure à 1000 blocs
        World world = Bukkit.getWorlds().get(0);
        if (world != null) {
            world.getWorldBorder().setSize(1000);
            world.getWorldBorder().setCenter(0, 0);
        }
        state = GameState.WAITING;
    }

    private void applyStartEffects() {
        plugin.getRoleManager().applyStartEffects();
    }

    private void enablePvp() { /* géré par PvpListener via state */ }

    private void announceRoles() {
        // Révéler le rôle à chaque joueur en message privé
        for (UUID uuid : alivePlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            Role role = plugin.getRoleManager().getRole(uuid);
            if (role == null) continue;
            p.sendMessage("§6§l╔══════════════════════════╗");
            p.sendMessage("§6§l║  §eTon rôle est révélé !  §6§l║");
            p.sendMessage("§6§l╚══════════════════════════╝");
            p.sendMessage("§eTon rôle : §b" + role.getDisplayName());
            p.sendMessage(role.getDescription());
        }
        // Composition publique si configuré
        if (plugin.getConfig().getBoolean("game.composition-visible", false)) {
            MessageUtils.broadcast("§e§lComposition de la partie :");
            plugin.getRoleManager().getRoleCompositionSummary().forEach(MessageUtils::broadcast);
        }
    }

    private void grantOwnerPerms(Player player) {
        plugin.getLogger().info("Owner défini : " + player.getName());
    }

    // Gestion diamants
    public void onDiamondMined(Player player) {
        UUID uuid = player.getUniqueId();
        int count = diamondsMined.getOrDefault(uuid, 0) + 1;
        diamondsMined.put(uuid, count);

        int max = plugin.getConfig().getInt("game.max-diamonds", 25);
        if (count > max) {
            player.getInventory().remove(Material.DIAMOND);
            player.getInventory().addItem(new org.bukkit.inventory.ItemStack(Material.GOLD_INGOT, 1));
            player.sendMessage(MessageUtils.info("Surplus de diamants converti en lingots d'or !"));
        }
    }

    public void cleanup() {
        if (mainTimer  != null) mainTimer.cancel();
        if (nightTimer != null) nightTimer.cancel();
        plugin.getSkysofrenieManager().reset();
    }

    // ─── GETTERS / SETTERS ──────────────────────────────────────────────────

    public GameState getState()                      { return state; }
    public void setState(GameState s)                { this.state = s; }
    public UUID getOwnerUUID()                       { return ownerUUID; }
    public boolean isOwner(UUID uuid)                { return uuid.equals(ownerUUID); }
    public boolean isAdmin(UUID uuid)                { return admins.contains(uuid); }
    public void addAdmin(UUID uuid)                  { admins.add(uuid); }
    public void removeAdmin(UUID uuid)               { admins.remove(uuid); }
    public Set<UUID> getAlivePlayers()               { return alivePlayers; } // mutable pour TestManager
    public Set<UUID> getDeadPlayers()                { return Collections.unmodifiableSet(deadPlayers); }
    public boolean isAlive(UUID uuid)                { return alivePlayers.contains(uuid); }
    public String getRoomName()                      { return roomName; }
    public int getElapsedSeconds()                   { return elapsedSeconds; }
    public int getEpisodeNumber()                    { return episodeNumber; }
    public boolean isBloodyNight()                   { return bloodyNight; }
    public void setBloodyNight(boolean b)            { this.bloodyNight = b; }
    public void addNightDeathMessage(String msg)     { nightDeathMessages.add(msg); }
    public Map<UUID, Integer> getDiamondsMined()     { return diamondsMined; }

    // ─── MÉTHODES TEST MODE ──────────────────────────────────────────────────

    /** Avance le temps interne sans déclencher les ticks (les événements sont gérés par TestManager). */
    public void forceElapsedSeconds(int seconds) {
        this.elapsedSeconds = seconds;
    }

    /** Force la fin d'épisode (reset pouvoirs, Conteuse, etc.). */
    public void forceEpisodeEnd() {
        episodeNumber++;
        onEpisodeEnd();
    }

    /** Active le PVP de force (sans attendre 20 min). */
    public void forcePvpEnable() {
        plugin.getChatManager().setGlobalChatEnabled(true);
        enablePvp();
    }

    /** Révèle les rôles à tous. */
    public void forceAnnounceRoles() {
        announceRoles();
        plugin.getMessageManager().announceRoleReveal();
    }

    /** Déclenche la réduction de bordure. */
    public void forceBorderShrink() {
        startBorderShrink();
    }

    // ─── CLASSE INTERNE : données de résurrection ───────────────────────────

    public static class ResurrectionData {
        private final List<ItemStack> items;
        private final Location deathLocation;

        public ResurrectionData(List<ItemStack> items, Location deathLocation) {
            this.items         = items;
            this.deathLocation = deathLocation;
        }

        public List<ItemStack> getItems()    { return items; }
        public Location getDeathLocation()   { return deathLocation; }
    }
}
