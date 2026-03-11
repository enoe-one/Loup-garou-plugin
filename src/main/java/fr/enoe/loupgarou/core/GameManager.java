package fr.enoe.loupgarou.core;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.managers.CageManager;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
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
    private final Set<UUID> alivePlayers  = new HashSet<>();
    private final Set<UUID> deadPlayers   = new HashSet<>();

    // Timers
    private BukkitTask mainTimer    = null;
    private BukkitTask nightTimer   = null;
    private int elapsedSeconds      = 0;
    private int episodeNumber       = 0;

    // Nuit sanglante
    private boolean bloodyNight     = false;
    private final List<String> nightDeathMessages = new ArrayList<>();

    // Diamants minés par joueur
    private final Map<UUID, Integer> diamondsMined = new HashMap<>();

    public GameManager(LoupGarouPlugin plugin) {
        this.plugin = plugin;
    }

    // ─── CRÉATION DE ROOM ───────────────────────────────────────────────────────

    /**
     * Appelé par /lg create "nom". Le premier joueur qui rejoint devient owner.
     */
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

        // Permissions
        grantOwnerPerms(player);

        // Mettre tout le monde en mode aventure et désactiver le chat
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setGameMode(GameMode.ADVENTURE);
        }
        plugin.getChatManager().setGlobalChatEnabled(false);

        // Construire la cage
        plugin.getCageManager().buildCage();

        // TP tout le monde dans la cage
        Location cageCenter = new Location(Bukkit.getWorlds().get(0), 0, 221, 0);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.teleport(cageCenter);
            alivePlayers.add(p.getUniqueId());
        }

        MessageUtils.broadcast("§6[LoupGarou] §eRoom §b" + name + "§e créée par §a" + player.getName() + "§e !");
        MessageUtils.broadcast("§7En attente du lancement par l'owner...");
        return true;
    }

    // ─── DÉMARRAGE ──────────────────────────────────────────────────────────────

    public void startGame() {
        if (state != GameState.WAITING) return;
        if (alivePlayers.size() < 2) {
            MessageUtils.broadcastToAdmins(plugin, "§cImpossible de démarrer : pas assez de joueurs !");
            return;
        }

        state = GameState.STARTING;
        episodeNumber = 0;
        elapsedSeconds = 0;

        // Attribuer les rôles
        plugin.getRoleManager().assignRoles(new ArrayList<>(alivePlayers));

        // Annoncer les rôles aux joueurs (en privé)
        for (UUID uuid : alivePlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            Role role = plugin.getRoleManager().getRole(uuid);
            if (role != null) {
                p.sendMessage("§6═══════════════════════════");
                p.sendMessage("§eTon rôle : §b" + role.getDisplayName());
                p.sendMessage(role.getDescription());
                p.sendMessage("§6═══════════════════════════");
            }
        }

        // Désactiver le nether et l'end (WorldListener s'en charge)
        // Donner les effets de départ
        applyStartEffects();

        // Lancer le timer principal
        state = GameState.RUNNING;
        startMainTimer();

        MessageUtils.broadcast("§a§lLa partie commence ! §rBonne chance à tous !");
    }

    // ─── TIMER PRINCIPAL ────────────────────────────────────────────────────────

    private void startMainTimer() {
        mainTimer = new BukkitRunnable() {
            @Override
            public void run() {
                elapsedSeconds++;
                onTick();
            }
        }.runTaskTimer(plugin, 0L, 20L); // toutes les secondes
    }

    private void onTick() {
        // ── Épisode : toutes les 20 minutes
        if (elapsedSeconds % 1200 == 0) {
            episodeNumber++;
            onEpisodeEnd();
        }

        // ── 20 min : PVP activé + annonce des rôles
        if (elapsedSeconds == 1200) {
            plugin.getChatManager().setGlobalChatEnabled(true);
            enablePvp();
            announceRoles();
            MessageUtils.broadcast("§c§lLe PVP est maintenant activé !");
        }

        // ── 25 min : annonce du couple
        if (elapsedSeconds == 1500) {
            plugin.getCoupleManager().announceCouple();
        }

        // ── Astronome : toutes les 10 min
        if (elapsedSeconds % 600 == 0 && elapsedSeconds > 0) {
            plugin.getRoleManager().triggerAstronomeTick();
        }

        // ── Monteur d'ours : toutes les 20 min
        if (elapsedSeconds % 1200 == 0 && elapsedSeconds > 0) {
            plugin.getRoleManager().triggerBearTamerTick();
        }

        // ── Vérification fin de partie
        checkWinCondition();
    }

    private void onEpisodeEnd() {
        // Nuit sanglante : révéler les morts
        if (bloodyNight && !nightDeathMessages.isEmpty()) {
            MessageUtils.broadcast("§4§l[Nuit Sanglante] §rLes morts de la nuit :");
            for (String msg : nightDeathMessages) MessageUtils.broadcast(msg);
            nightDeathMessages.clear();
            bloodyNight = false;
        }

        // Appliquer les effets d'épisode (ex: maire speed)
        plugin.getRoleManager().onEpisodeEnd(episodeNumber);
    }

    // ─── MORT D'UN JOUEUR ───────────────────────────────────────────────────────

    /**
     * Appelé quand un joueur meurt. Gère le TP en cage, la conservation du stuff.
     */
    public void handlePlayerDeath(Player player) {
        UUID uuid = player.getUniqueId();
        if (!alivePlayers.contains(uuid)) return;

        Role role = plugin.getRoleManager().getRole(uuid);

        // Sorcière : peut-elle le ressusciter ?
        if (plugin.getRoleManager().tryWitchRevive(uuid)) return;

        // Infecté père des loups
        if (plugin.getRoleManager().tryInfectRevive(uuid)) return;

        // Chasseur : pouvoir si mort
        plugin.getRoleManager().triggerHunterDeath(player);

        // Ancien
        if (plugin.getRoleManager().tryElderRevive(uuid)) return;

        // Idiot du village
        if (plugin.getRoleManager().tryVillageIdiotRevive(uuid)) return;

        // Mort effective
        alivePlayers.remove(uuid);
        deadPlayers.add(uuid);

        // Couple : si l'un meurt, l'autre aussi
        plugin.getCoupleManager().onPartnerDeath(uuid);

        // Message de mort
        String deathMsg = "§c☠ §e" + player.getName() + " §ca été éliminé.";

        if (bloodyNight) {
            nightDeathMessages.add(deathMsg);
            // On révèle quand même en fin de nuit
        } else {
            // Assassin peut cacher
            if (!plugin.getRoleManager().tryAssassinHideDeath(uuid)) {
                MessageUtils.broadcast(deathMsg);
                // Révéler le rôle
                if (role != null) {
                    MessageUtils.broadcast("§7Son rôle était : §b" + role.getDisplayName());
                }
            }
        }

        // TP dans la cage avec inventaire (10s d'affichage du rôle)
        Location cageLoc = new Location(player.getWorld(), 0, 221, 0);
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(cageLoc);

        new BukkitRunnable() {
            @Override
            public void run() {
                // Après 10s le joueur reste spectateur mais peut se balader
            }
        }.runTaskLater(plugin, 200L);

        checkWinCondition();
    }

    // ─── CONDITIONS DE VICTOIRE ─────────────────────────────────────────────────

    private void checkWinCondition() {
        if (state != GameState.RUNNING) return;

        List<UUID> alive = new ArrayList<>(alivePlayers);
        if (alive.isEmpty()) {
            endGame("§7Personne ne gagne. Tout le monde est mort !");
            return;
        }

        // Loups gagnent si autant ou plus de loups que de villagois
        long wolves     = alive.stream().filter(u -> plugin.getRoleManager().isWolf(u)).count();
        long villagers  = alive.stream().filter(u -> plugin.getRoleManager().isVillager(u)).count();
        long solitaires = alive.stream().filter(u -> plugin.getRoleManager().isSolitary(u)).count();

        // Loup blanc solitaire
        plugin.getRoleManager().checkWhiteWolfWin(alive);

        // Joueur de flûte
        plugin.getRoleManager().checkFlutistWin(alive);

        if (wolves >= villagers + solitaires) {
            endGame("§c§lLes Loups-Garous gagnent !");
        } else if (wolves == 0 && solitaires == 0) {
            endGame("§a§lLe Village gagne !");
        }
    }

    public void endGame(String message) {
        state = GameState.ENDED;
        if (mainTimer != null) mainTimer.cancel();

        MessageUtils.broadcast("§6§l══════════════════════════");
        MessageUtils.broadcast(message);
        MessageUtils.broadcast("§6§l══════════════════════════");

        // Remettre tout le monde en survie et TP dans la cage
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
        elapsedSeconds = 0;
        episodeNumber  = 0;
        bloodyNight    = false;
        nightDeathMessages.clear();
        plugin.getRoleManager().reset();
        plugin.getVoteManager().reset();
        plugin.getCoupleManager().reset();
        state = GameState.WAITING;
    }

    // ─── UTILITAIRES ────────────────────────────────────────────────────────────

    private void applyStartEffects() {
        // Les effets de départ des rôles sont gérés dans RoleManager
        plugin.getRoleManager().applyStartEffects();
    }

    private void enablePvp() {
        // Géré par PvpListener via state
    }

    private void announceRoles() {
        if (!plugin.getConfig().getBoolean("game.composition-visible", false)) return;
        MessageUtils.broadcast("§e§lComposition de la partie :");
        plugin.getRoleManager().getRoleCompositionSummary().forEach(MessageUtils::broadcast);
    }

    private void grantOwnerPerms(Player player) {
        // On utilise les op temporairement pour l'owner — à remplacer par un système de permission dédié
        // Dans un vrai setup, utiliser LuckPerms ou le système interne
        plugin.getLogger().info("Owner défini : " + player.getName());
    }

    // Gestion diamants
    public void onDiamondMined(Player player) {
        UUID uuid = player.getUniqueId();
        int count = diamondsMined.getOrDefault(uuid, 0) + 1;
        diamondsMined.put(uuid, count);

        int max = plugin.getConfig().getInt("game.max-diamonds", 25);
        if (count > max) {
            // Transformer en or dans l'inventaire
            player.getInventory().remove(Material.DIAMOND);
            player.getInventory().addItem(new org.bukkit.inventory.ItemStack(Material.GOLD_INGOT, 1));
            player.sendMessage(MessageUtils.info("Surplus de diamants converti en lingots d'or !"));
        }
    }

    public void cleanup() {
        if (mainTimer != null) mainTimer.cancel();
        if (nightTimer != null) nightTimer.cancel();
    }

    // ─── GETTERS / SETTERS ──────────────────────────────────────────────────────

    public GameState getState()                     { return state; }
    public void setState(GameState s)               { this.state = s; }
    public UUID getOwnerUUID()                      { return ownerUUID; }
    public boolean isOwner(UUID uuid)               { return uuid.equals(ownerUUID); }
    public boolean isAdmin(UUID uuid)               { return admins.contains(uuid); }
    public void addAdmin(UUID uuid)                 { admins.add(uuid); }
    public void removeAdmin(UUID uuid)              { admins.remove(uuid); }
    public Set<UUID> getAlivePlayers()              { return Collections.unmodifiableSet(alivePlayers); }
    public Set<UUID> getDeadPlayers()               { return Collections.unmodifiableSet(deadPlayers); }
    public boolean isAlive(UUID uuid)               { return alivePlayers.contains(uuid); }
    public String getRoomName()                     { return roomName; }
    public int getElapsedSeconds()                  { return elapsedSeconds; }
    public int getEpisodeNumber()                   { return episodeNumber; }
    public boolean isBloodyNight()                  { return bloodyNight; }
    public void setBloodyNight(boolean b)           { this.bloodyNight = b; }
    public void addNightDeathMessage(String msg)    { nightDeathMessages.add(msg); }
    public Map<UUID, Integer> getDiamondsMined()    { return diamondsMined; }
}
