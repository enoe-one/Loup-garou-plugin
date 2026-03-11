package fr.enoe.loupgarou;

import fr.enoe.loupgarou.commands.*;
import fr.enoe.loupgarou.core.GameManager;
import fr.enoe.loupgarou.listeners.*;
import fr.enoe.loupgarou.managers.*;
import org.bukkit.plugin.java.JavaPlugin;

public class LoupGarouPlugin extends JavaPlugin {

    private static LoupGarouPlugin instance;

    private GameManager gameManager;
    private RoleManager roleManager;
    private VoteManager voteManager;
    private CoupleManager coupleManager;
    private EnchantManager enchantManager;
    private ChatManager chatManager;
    private CageManager cageManager;
    private EventManager eventManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Managers
        this.roleManager    = new RoleManager(this);
        this.voteManager    = new VoteManager(this);
        this.coupleManager  = new CoupleManager(this);
        this.enchantManager = new EnchantManager(this);
        this.chatManager    = new ChatManager(this);
        this.cageManager    = new CageManager(this);
        this.eventManager   = new EventManager(this);
        this.gameManager    = new GameManager(this);

        // Commands
        getCommand("lg").setExecutor(new LgCommand(this));
        getCommand("lgadmin").setExecutor(new AdminCommand(this));
        getCommand("lgowner").setExecutor(new OwnerCommand(this));
        getCommand("console").setExecutor(new ConsoleCommand(this));

        // Listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new EnchantListener(this), this);
        getServer().getPluginManager().registerEvents(new MiningListener(this), this);
        getServer().getPluginManager().registerEvents(new PvpListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldListener(this), this);
        getServer().getPluginManager().registerEvents(new NightListener(this), this);

        getLogger().info("§a[LoupGarou] Plugin activé !");
    }

    @Override
    public void onDisable() {
        if (gameManager != null) gameManager.cleanup();
        getLogger().info("§c[LoupGarou] Plugin désactivé.");
    }

    public static LoupGarouPlugin getInstance() { return instance; }
    public GameManager getGameManager()         { return gameManager; }
    public RoleManager getRoleManager()         { return roleManager; }
    public VoteManager getVoteManager()         { return voteManager; }
    public CoupleManager getCoupleManager()     { return coupleManager; }
    public EnchantManager getEnchantManager()   { return enchantManager; }
    public ChatManager getChatManager()         { return chatManager; }
    public CageManager getCageManager()         { return cageManager; }
    public EventManager getEventManager()       { return eventManager; }
}
