package fr.enoe.loupgarou.world;

import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * BiomeProvider custom pour forcer :
 * - Forêt sombre (DARK_FOREST) dans un rayon de 500 blocs autour de (0,0)
 * - Plaines autour (PLAINS) pour les zones de combat ouvert
 * - Pas de biomes souterrains spéciaux (lush caves, dripstone caves désactivés)
 * - Pas de swamp/mangrove (qui génèrent des villages ou structures)
 *
 * Zone :    0–500 blocs = DARK_FOREST
 *         500–750 blocs = FOREST (transition)
 *         750+    blocs = PLAINS
 */
public class LGBiomeProvider extends BiomeProvider {

    private static final double DARK_FOREST_RADIUS  = 500.0;
    private static final double FOREST_RADIUS       = 750.0;

    @Override
    public @NotNull Biome getBiome(@NotNull WorldInfo worldInfo, int x, int y, int z) {
        double dist = Math.sqrt((double) x * x + (double) z * z);

        if (dist <= DARK_FOREST_RADIUS) {
            // Cœur : forêt sombre garantie
            return Biome.DARK_FOREST;
        } else if (dist <= FOREST_RADIUS) {
            // Transition douce
            return Biome.FOREST;
        } else {
            // Zone ouverte extérieure : plaines
            return Biome.PLAINS;
        }
    }

    @Override
    public @NotNull List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
        // Déclarer seulement les biomes utilisés
        // → Paper/Spigot n'essaiera pas de générer d'autres biomes
        return Arrays.asList(Biome.DARK_FOREST, Biome.FOREST, Biome.PLAINS);
    }
}
