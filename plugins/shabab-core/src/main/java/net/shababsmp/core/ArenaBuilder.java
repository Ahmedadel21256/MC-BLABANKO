package net.shababsmp.core;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Small starter map layouts for isolated NEW BedWars and SkyWars backends.
 * Decorative/practice islands only, not match management.
 * Never deletes world data; aborts if intended construction volume is obstructed.
 */
public final class ArenaBuilder {
    private static final int Y = 225;
    private ArenaBuilder() {}

    public static void create(World world, JavaPlugin plugin, String mode) {
        if (!mode.equals("bedwars") && !mode.equals("skywars")) return;
        NamespacedKey tag = new NamespacedKey(plugin, "starter_arena_" + mode + "_v1");
        if (world.getPersistentDataContainer().has(tag, PersistentDataType.BYTE)) return;
        if (Y+12 >= world.getMaxHeight() || Y-3 <= world.getMinHeight()) {
            plugin.getLogger().warning("Starter arena skipped: incompatible world height");
            return;
        }
        int players = mode.equals("bedwars") ? 4 : 8;
        int radius = mode.equals("bedwars") ? 39 : 32;
        // Check exactly the bounding volume before any modification.
        for (int x = -radius-9; x <= radius+9; x++) {
            for (int z = -radius-9; z <= radius+9; z++) {
                // Check only regions that will actually get platforms.
                boolean relevant = x*x + z*z <= 12*12;
                for (int i=0; i<players && !relevant; i++) {
                    double angle = 2*Math.PI*i/players;
                    int cx=(int)Math.round(radius*Math.cos(angle));
                    int cz=(int)Math.round(radius*Math.sin(angle));
                    relevant = Math.abs(x-cx) <= 7 && Math.abs(z-cz) <= 7;
                }
                if (!relevant) continue;
                for (int y=Y-2; y<=Y+9; y++) {
                    if (!world.getBlockAt(x,y,z).getType().isAir()) {
                        plugin.getLogger().warning("Starter " + mode + " map NOT generated; existing blocks in build area at " + x + "," + y + "," + z);
                        return;
                    }
                }
            }
        }
        platform(world, 0, 0, 11, Material.DEEPSLATE_BRICKS, Material.POLISHED_ANDESITE);
        for (int i=0; i<players; i++) {
            double angle = 2*Math.PI*i/players;
            int cx=(int)Math.round(radius*Math.cos(angle));
            int cz=(int)Math.round(radius*Math.sin(angle));
            Material color = switch (i%4) {
                case 0 -> Material.RED_CONCRETE;
                case 1 -> Material.BLUE_CONCRETE;
                case 2 -> Material.LIME_CONCRETE;
                default -> Material.YELLOW_CONCRETE;
            };
            platform(world, cx, cz, mode.equals("bedwars") ? 6 : 5, Material.STONE_BRICKS, color);
            // Simple boundary pillars / spawn markers.
            for(int d=-2;d<=2;d+=4) {
                block(world,cx+d,Y+1,cz+d,Material.SEA_LANTERN);
            }
            if (mode.equals("bedwars")) {
                // Clearly visible colored team bed; actual BedWars logic requires its own plugin.
                Material bed = switch (i%4) {
                    case 0 -> Material.RED_WOOL;
                    case 1 -> Material.BLUE_WOOL;
                    case 2 -> Material.LIME_WOOL;
                    default -> Material.YELLOW_WOOL;
                };
                block(world,cx,Y+1,cz,bed);
                block(world,cx,Y+2,cz,Material.SEA_LANTERN);
            } else {
                Block chestBlock=world.getBlockAt(cx,Y+1,cz);
                chestBlock.setType(Material.CHEST,false);
                if (chestBlock.getState() instanceof Chest chest) {
                    Inventory inventory=chest.getBlockInventory();
                    inventory.addItem(new ItemStack(Material.IRON_SWORD),new ItemStack(Material.BREAD,8),
                        new ItemStack(Material.OAK_PLANKS,32));
                }
            }
        }
        world.setSpawnLocation(0,Y+1,0);
        world.getPersistentDataContainer().set(tag,PersistentDataType.BYTE,(byte)1);
        plugin.getLogger().info("Created " + mode + " starter practice islands in " + world.getName() +
            ". Arena registration and actual match logic still required.");
    }

    private static void platform(World world,int centerX,int centerZ,int radius,Material base,Material top) {
        for(int dx=-radius;dx<=radius;dx++) {
            for(int dz=-radius;dz<=radius;dz++) {
                if(dx*dx+dz*dz>radius*radius) continue;
                int x=centerX+dx,z=centerZ+dz;
                block(world,x,Y-1,z,base);
                block(world,x,Y,z,(dx*dx+dz*dz > (radius-2)*(radius-2)) ? base : top);
            }
        }
    }

    private static void block(World world,int x,int y,int z,Material material) {
        world.getBlockAt(x,y,z).setType(material,false);
    }
}
