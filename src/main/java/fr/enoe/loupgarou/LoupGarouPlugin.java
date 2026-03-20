package fr.enoe.loupgarou;

import fr.enoe.loupgarou.commands.*;
import fr.enoe.loupgarou.core.GameManager;
import fr.enoe.loupgarou.core.TestManager;
import fr.enoe.loupgarou.gui.ColorGui;
import fr.enoe.loupgarou.gui.SettingsGUI;
import fr.enoe.loupgarou.listeners.*;
import fr.enoe.loupgarou.managers.*;
import fr.enoe.loupgarou.managers.SkysofrenieManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.plugin.java.JavaPlugin;

public class LoupGarouPlugin extends JavaPlugin {

    private static LoupGarouPlugin instance;

    private GameManager          gameManager;
    private RoleManager          roleManager;
    private VoteManager          voteManager;
    private CoupleManager        coupleManager;
    private EnchantManager       enchantManager;
    private ChatManager          chatManager;
    private CageManager          cageManager;
    private EventManager         eventManager;
    private MessageManager       messageManager;
    private TestManager          testManager;
    private ColorManager         colorManager;
    private GameScoreboardManager scoreboardManager;
    private PucesManager         pucesManager;
    private SkysofrenieManager   skysofrenieManager;
    private SettingsGUI          settingsGUI;
    private ColorGui             colorGui;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // ── Compatibilité serveur crack (offline-mode: false) ──────────────
        // Le serveur doit avoir online-mode=false dans server.properties.
        // Le plugin utilise les UUIDs Bukkit natifs qui sont déterministes
        // en mode offline (generés depuis le nom du joueur).
        // Aucune vérification de session Mojang n'est faite ici.
        if (!getServer().getOnlineMode()) {
            getLogger().info("[LoupGarou] Mode OFFLINE détecté — compatibilité crack activée.");
        }

        // Managers (ordre important : MessageManager en premier)
        this.messageManager    = new MessageManager(this);
        this.roleManager       = new RoleManager(this);
        this.voteManager       = new VoteManager(this);
        this.coupleManager     = new CoupleManager(this);
        this.enchantManager    = new EnchantManager(this);
        this.chatManager       = new ChatManager(this);
        this.cageManager       = new CageManager(this);
        this.eventManager      = new EventManager(this);
        this.gameManager       = new GameManager(this);
        this.testManager       = new TestManager(this);
        this.colorManager      = new ColorManager(this);
        this.scoreboardManager = new GameScoreboardManager(this);
        this.pucesManager      = new PucesManager(this);
        this.skysofrenieManager = new SkysofrenieManager(this);
        this.settingsGUI       = new SettingsGUI(this);
        this.colorGui          = new ColorGui(this);

        // Démarrer le ticker du scoreboard
        this.scoreboardManager.startTicker();

        // ── Initialiser la WorldBorder à 1000×1000 au démarrage ───────────
        World world = Bukkit.getWorlds().get(0);
        WorldBorder border = world.getWorldBorder();
        border.setCenter(0, 0);
        border.setSize(1000);          // -500 à +500
        border.setWarningDistance(50);
        border.setWarningTime(15);
        border.setDamageAmount(1.0);
        border.setDamageBuffer(0.0);
        getLogger().info("[LoupGarou] WorldBorder initialisée : 1000×1000 (centre 0,0).");

        // Commandes
        getCommand("lg").setExecutor(new LgCommand(this));
        getCommand("lgadmin").setExecutor(new AdminCommand(this));
        getCommand("lgowner").setExecutor(new OwnerCommand(this));
        getCommand("console").setExecutor(new ConsoleCommand(this));
        getCommand("lgsetting").setExecutor((sender, cmd, label, args) -> {
            if (sender instanceof org.bukkit.entity.Player p) settingsGUI.open(p);
            return true;
        });

        // Listeners existants
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this),  this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this),        this);
        getServer().getPluginManager().registerEvents(new EnchantListener(this),     this);
        getServer().getPluginManager().registerEvents(new MiningListener(this),      this);
        getServer().getPluginManager().registerEvents(new PvpListener(this),         this);
        getServer().getPluginManager().registerEvents(new WorldListener(this),       this);
        getServer().getPluginManager().registerEvents(new NightListener(this),       this);
        getServer().getPluginManager().registerEvents(new OreBoostListener(this),    this);
        getServer().getPluginManager().registerEvents(new GoldenAppleListener(this), this);

        // ── v3 : CraftListener — auto-enchant Efficacité 3 + Solidité 2 ──
        getServer().getPluginManager().registerEvents(new CraftListener(this), this);

        // ── Révolution : Marengo listener ─────────────────────────────────
        getServer().getPluginManager().registerEvents(new MarengoListener(this), this);

        getServer().getPluginManager().registerEvents(settingsGUI, this);

        getLogger().info("§a[LoupGarou] Plugin v3 activé !");
    }

    @Override
    public void onDisable() {
        if (gameManager != null) gameManager.cleanup();
        getLogger().info("§c[LoupGarou] Plugin désactivé.");
    }

    // ── Utilitaire offline : résoudre un joueur par nom (crack-compatible) ──
    /**
     * Cherche un joueur en ligne par nom exact.
     * Compatible online-mode ET offline-mode (crack).
     */
    public org.bukkit.entity.Player getPlayerByName(String name) {
        return Bukkit.getPlayerExact(name);
    }

    public static LoupGarouPlugin getInstance()          { return instance; }
    public GameManager           getGameManager()        { return gameManager; }
    public RoleManager           getRoleManager()        { return roleManager; }
    public TestManager           getTestManager()        { return testManager; }
    public ColorManager          getColorManager()       { return colorManager; }
    public GameScoreboardManager getScoreboardManager()  { return scoreboardManager; }
    public PucesManager          getPucesManager()       { return pucesManager; }
    public SkysofrenieManager    getSkysofrenieManager() { return skysofrenieManager; }
    public ColorGui              getColorGui()           { return colorGui; }
    public VoteManager           getVoteManager()        { return voteManager; }
    public CoupleManager         getCoupleManager()      { return coupleManager; }
    public EnchantManager        getEnchantManager()     { return enchantManager; }
    public ChatManager           getChatManager()        { return chatManager; }
    public CageManager           getCageManager()        { return cageManager; }
    public EventManager          getEventManager()       { return eventManager; }
    public MessageManager        getMessageManager()     { return messageManager; }
    public SettingsGUI           getSettingsGUI()        { return settingsGUI; }
}
