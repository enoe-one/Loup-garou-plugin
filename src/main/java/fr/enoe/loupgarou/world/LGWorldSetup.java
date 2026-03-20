package fr.enoe.loupgarou.world;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;

/**
 * Utilitaire pour créer / configurer le monde de jeu Loup-Garou.
 *
 * Utilisation dans server.properties :
 *   generator=LoupGarou:fr.enoe.loupgarou.world.LGWorldGenerator
 *   level-name=lgworld
 *
 * OU via WorldCreator en code (pour un monde secondaire) :
 *   LGWorldSetup.createOrLoad("lgworld");
 *
 * Règles appliquées :
 *   - Pas de régénération naturelle (UHC)
 *   - Pas d'annonces de mort à la première mort
 *   - doImmediateRespawn = false
 *   - Villagers : bloqués via WorldListener (EntitySpawnEvent)
 *   - Deepslate : remplacé dans le generator
 *   - Grottes simples sans biomes spéciaux : géré par LGBiomeProvider
 */
public class LGWorldSetup {

    public static World createOrLoad(String worldName) {
        // Vérifier si le monde existe déjà
        World existing = Bukkit.getWorld(worldName);
        if (existing != null) {
            applyGameRules(existing);
            return existing;
        }

        // Créer le monde avec notre générateur + biome provider
        WorldCreator creator = new WorldCreator(worldName)
            .generator(new LGWorldGenerator())
            .biomeProvider(new LGBiomeProvider());

        World world = creator.createWorld();
        if (world == null) return null;

        applyGameRules(world);
        return world;
    }

    private static void applyGameRules(World world) {
        // UHC : pas de regen naturelle
        world.setGameRule(GameRule.NATURAL_REGENERATION, false);

        // Pas d'annonces de mort à la 1ère mort
        world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);

        // Météo désactivée (perturbation visuelle)
        world.setStorm(false);
        world.setThundering(false);
        world.setWeatherDuration(Integer.MAX_VALUE);

        // Pas de structures vanilla (villages, temples, etc.)
        // Note : cela s'applique à la génération future des chunks
        // world.setGameRule(GameRule.SPAWN_RADIUS, 0); // spawn au centre

        // Garder les monstres normaux (zombies, araignées) mais pas les villageois
        // → géré dans WorldListener via EntitySpawnEvent
    }
}
