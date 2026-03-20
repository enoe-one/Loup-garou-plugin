package fr.enoe.loupgarou.world;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Générateur de monde custom pour le Loup-Garou UHC.
 *
 * Objectifs :
 * - Forêt sombre garantie au spawn (0,0), rayon minimum 500 blocs
 * - Pas de deepslate (remplacé par stone en dessous de Y=8)
 * - Grottes "simples" (style pré-1.18) : pas de grandes cavernes amplifiées,
 *   pas de biomes souterrains (dripstone, lush caves) — bruit de perlin classique
 * - Pas de structures de villages (géré via bukkit.yml / spigot.yml)
 * - Bédrock en fond à Y=0
 *
 * IMPORTANT : Pour forcer la forêt sombre et supprimer les villages,
 * il faut également configurer :
 *   - bukkit.yml → settings.allow-end: false, allow-nether: false
 *   - spigot.yml → world-settings.default.mob-spawn-range → désactiver villager
 *   - level-type=FLAT ou via le plugin BiomeProvider
 *   - OU utiliser un monde pré-généré avec WorldEdit pour placer la forêt
 *
 * Ce générateur fournit la logique Java côté plugin.
 */
public class LGWorldGenerator extends ChunkGenerator {

    // Rayon en chunks de la forêt sombre centrale (500 blocs = 31 chunks)
    private static final int DARK_FOREST_RADIUS_CHUNKS = 32;

    @Override
    public void generateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random,
                              int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        // Hauteur de surface de base
        int baseHeight = 64;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // Coordonnées monde
                int worldX = chunkX * 16 + x;
                int worldZ = chunkZ * 16 + z;

                // Variation de hauteur via bruit simplifié
                double noise = simplexLike(worldX * 0.02, worldZ * 0.02);
                int surfaceY = baseHeight + (int)(noise * 8);

                // Bédrock
                chunkData.setBlock(x, worldInfo.getMinHeight(), z, Material.BEDROCK);

                // Pierre du fond jusqu'à la surface
                for (int y = worldInfo.getMinHeight() + 1; y < surfaceY - 3; y++) {
                    // Pas de deepslate : remplacer par stone sous Y=8 relatif au min
                    // (en 1.21 deepslate apparaît naturellement sous Y=0 dans la génération vanilla)
                    // Ici on force STONE partout
                    chunkData.setBlock(x, y, z, Material.STONE);
                }

                // Couche de terre
                for (int y = Math.max(worldInfo.getMinHeight() + 1, surfaceY - 3); y < surfaceY; y++) {
                    chunkData.setBlock(x, y, z, Material.DIRT);
                }

                // Surface : herbe ou mycélium (forêt sombre = podzol)
                if (isInDarkForestZone(chunkX, chunkZ)) {
                    chunkData.setBlock(x, surfaceY, z, Material.PODZOL);
                } else {
                    chunkData.setBlock(x, surfaceY, z, Material.GRASS_BLOCK);
                }

                // Grottes simples style pré-1.18 : tunnels étroits via bruit 3D
                generateSimpleCaves(chunkData, worldInfo, random, x, z, worldX, worldZ, surfaceY);
            }
        }
    }

    /**
     * Génère des grottes simples (tunnels) sans biomes souterrains.
     * Inspiré du style pré-1.18 : petites poches et tunnels,
     * pas de grandes cavernes amplifiées ni de lush caves / dripstone.
     */
    private void generateSimpleCaves(ChunkData data, WorldInfo info, Random rand,
                                     int x, int z, int wx, int wz, int surfaceY) {
        int minY = info.getMinHeight() + 5; // garder quelques blocs de bédrock
        int maxY = surfaceY - 5;            // ne pas crever la surface

        for (int y = minY; y < maxY; y++) {
            // Bruit 3D multi-octave simplifié → valeur entre -1 et 1
            double n1 = simplexLike(wx * 0.06, y * 0.06, wz * 0.06);
            double n2 = simplexLike(wx * 0.06 + 100, y * 0.06 + 100, wz * 0.06 + 100);

            // Creuser si les deux bruits convergent → tunnels étroits
            if (n1 * n1 + n2 * n2 < 0.025) { // seuil = taille des tunnels
                Material current = data.getType(x, y, z);
                if (current == Material.STONE || current == Material.DIRT) {
                    data.setBlock(x, y, z, Material.AIR);
                }
            }
        }
    }

    /** Vrai si ce chunk est dans la zone de forêt sombre (rayon 32 chunks = 512 blocs). */
    private boolean isInDarkForestZone(int cx, int cz) {
        return cx * cx + cz * cz < DARK_FOREST_RADIUS_CHUNKS * DARK_FOREST_RADIUS_CHUNKS;
    }

    /**
     * Bruit simplex-like 2D basé sur des sinus/cosinus imbriqués.
     * Pas de dépendance externe, résultat entre -1 et 1.
     */
    private double simplexLike(double x, double z) {
        return Math.sin(x * 1.7 + Math.cos(z * 1.3)) * Math.cos(z * 1.9 + Math.sin(x * 1.1)) * 0.5
             + Math.sin(x * 3.1 + z * 2.7) * 0.25
             + Math.sin(x * 5.9 - z * 4.3) * 0.125;
    }

    /** Bruit 3D simplex-like. */
    private double simplexLike(double x, double y, double z) {
        return Math.sin(x * 1.7 + Math.cos(y * 1.5)) * Math.cos(z * 1.9 + Math.sin(x * 1.1))
             * Math.sin(y * 2.3 + z * 1.7) * 0.5
             + Math.sin(x * 3.7 + y * 2.1 - z * 1.9) * 0.3
             + Math.sin(y * 5.1 + x * 3.3 + z * 4.7) * 0.2;
    }

    @Override
    public boolean canSpawn(@NotNull World world, int x, int z) {
        // Autoriser le spawn uniquement dans la zone de forêt sombre
        int cx = x >> 4, cz = z >> 4;
        return isInDarkForestZone(cx, cz);
    }
}
