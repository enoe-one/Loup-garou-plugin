package fr.enoe.loupgarou.roles;

import fr.enoe.loupgarou.LoupGarouPlugin;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Classe de base pour tous les rôles du jeu.
 */
public abstract class Role {

    protected final LoupGarouPlugin plugin;
    protected final UUID playerUUID;
    protected boolean powerUsedThisEpisode = false;

    public Role(LoupGarouPlugin plugin, UUID playerUUID) {
        this.plugin = plugin;
        this.playerUUID = playerUUID;
    }

    /** Nom affiché du rôle */
    public abstract String getDisplayName();

    /** Description courte du rôle (envoyée au joueur au début) */
    public abstract String getDescription();

    /** Famille du rôle */
    public abstract RoleFamily getFamily();

    /** ID technique unique (ex: "loup_simple", "voyant") */
    public abstract String getId();

    /** Appelé à chaque fin d'épisode */
    public void onEpisodeEnd(int episodeNumber) {
        powerUsedThisEpisode = false;
    }

    /** Appelé quand la partie démarre */
    public void onGameStart(Player player) {}

    /** Appelé quand ce joueur meurt */
    public void onDeath(Player player) {}

    /** Appelé à chaque tick de nuit */
    public void onNightTick(Player player) {}

    /** Appelé quand ce joueur tue quelqu'un (killerPlayer = lui, victimUUID = la victime) */
    public void onPlayerKill(Player killer, UUID victimUUID) {}

    /** Commande /lg <action> pour ce rôle */
    public boolean onPowerCommand(Player player, String[] args) { return false; }

    /** Le rôle est-il visible par les rôles à information ? */
    public RoleFamily getApparentFamily() { return getFamily(); }

    /** Pour les rôles binary : peut changer de camp */
    public boolean isBinary() { return false; }

    public UUID getPlayerUUID()                         { return playerUUID; }
    public boolean isPowerUsedThisEpisode()             { return powerUsedThisEpisode; }
    public void setPowerUsedThisEpisode(boolean used)   { this.powerUsedThisEpisode = used; }
}
