package fr.enoe.loupgarou.listeners;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.core.GameState;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.StructureGrowEvent;

public class WorldListener implements Listener {

    private final LoupGarouPlugin plugin;

    public WorldListener(LoupGarouPlugin plugin) { this.plugin = plugin; }

    // ── Portails Nether / End bloqués ──────────────────────────────────────
    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        if (event.getTo() == null) return;
        World.Environment env = event.getTo().getWorld().getEnvironment();
        if (env == World.Environment.NETHER || env == World.Environment.THE_END) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(MessageUtils.error("Le Nether et l'End sont désactivés !"));
        }
    }

    // ── Bloquer les villageois et mobs indésirables ─────────────────────────
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntitySpawn(EntitySpawnEvent event) {
        EntityType type = event.getEntityType();
        switch (type) {
            case VILLAGER,
                 WANDERING_TRADER,
                 TRADER_LLAMA,
                 PILLAGER,
                 RAVAGER,
                 VINDICATOR,
                 EVOKER,
                 VEX,
                 WITCH,    // sorcière vanilla (différente de la nôtre — pnj)
                 ILLUSIONER,
                 IRON_GOLEM -> event.setCancelled(true);
            default -> { /* laisser spawner */ }
        }
    }

    // ── Remplacer le deepslate généré naturellement par de la stone ─────────
    // Note : le ChunkGenerator remplace déjà dans les chunks générés,
    // mais des chunks vanilla peuvent avoir du deepslate → on le remplace à chargement.
    // ATTENTION : très coûteux sur de grandes maps — désactiver si lag.
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!event.isNewChunk()) return; // seulement les nouveaux chunks
        // Remplacer deepslate dans ce chunk (Y de -64 à 8)
        replaceDeepslateInChunk(event.getChunk());
    }

    private void replaceDeepslateInChunk(org.bukkit.Chunk chunk) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = -64; y <= 8; y++) {
                    Block b = chunk.getBlock(x, y, z);
                    switch (b.getType()) {
                        case DEEPSLATE          -> b.setType(Material.STONE,    false);
                        case DEEPSLATE_COAL_ORE -> b.setType(Material.COAL_ORE, false);
                        case DEEPSLATE_IRON_ORE -> b.setType(Material.IRON_ORE, false);
                        case DEEPSLATE_GOLD_ORE -> b.setType(Material.GOLD_ORE, false);
                        case DEEPSLATE_DIAMOND_ORE -> b.setType(Material.DIAMOND_ORE, false);
                        case DEEPSLATE_EMERALD_ORE -> b.setType(Material.EMERALD_ORE, false);
                        case DEEPSLATE_LAPIS_ORE   -> b.setType(Material.LAPIS_ORE,   false);
                        case DEEPSLATE_COPPER_ORE  -> b.setType(Material.COPPER_ORE,  false);
                        case DEEPSLATE_REDSTONE_ORE -> b.setType(Material.REDSTONE_ORE, false);
                        case COBBLED_DEEPSLATE,
                             DEEPSLATE_BRICKS,
                             DEEPSLATE_TILES,
                             CHISELED_DEEPSLATE,
                             POLISHED_DEEPSLATE,
                             CRACKED_DEEPSLATE_TILES,
                             CRACKED_DEEPSLATE_BRICKS -> b.setType(Material.COBBLESTONE, false);
                        default -> { /* laisser */ }
                    }
                }
            }
        }
    }
}
