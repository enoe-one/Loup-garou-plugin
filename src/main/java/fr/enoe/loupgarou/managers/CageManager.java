package fr.enoe.loupgarou.managers;

import fr.enoe.loupgarou.LoupGarouPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;

public class CageManager {

    private final LoupGarouPlugin plugin;

    public CageManager(LoupGarouPlugin plugin) { this.plugin = plugin; }

    /** Construit une cage en bedrock 20×20 centrée en 0,220,0 */
    public void buildCage() {
        World w = Bukkit.getWorlds().get(0);
        int cx = 0, cy = 220, cz = 0, half = 10;

        for (int x = cx - half; x <= cx + half; x++) {
            for (int z = cz - half; z <= cz + half; z++) {
                for (int y = cy; y <= cy + 5; y++) {
                    boolean wall = x == cx - half || x == cx + half
                            || z == cz - half || z == cz + half
                            || y == cy || y == cy + 5;
                    if (wall) w.getBlockAt(x, y, z).setType(Material.BEDROCK);
                    else      w.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }
    }

    public void destroyCage() {
        World w = Bukkit.getWorlds().get(0);
        int cx = 0, cy = 220, cz = 0, half = 10;
        for (int x = cx - half; x <= cx + half; x++)
            for (int z = cz - half; z <= cz + half; z++)
                for (int y = cy; y <= cy + 5; y++)
                    if (w.getBlockAt(x, y, z).getType() == Material.BEDROCK)
                        w.getBlockAt(x, y, z).setType(Material.AIR);
    }
}
