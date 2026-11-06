package net.shababsmp.core;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * One-time fresh lobby platform. Refuses to overwrite an existing structure:
 * every affected position at the build elevation must be air first.
 */
public final class HubBuilder {
    private static final int Y = 190;
    private static final int R = 19;
    private HubBuilder() {}

    public static void build(Player player, JavaPlugin plugin, NamespacedKey npcKey) {
        buildWorld(player.getWorld(), player, plugin, npcKey);
    }

    public static void buildWorld(World world, Player player, JavaPlugin plugin, NamespacedKey npcKey) {
        if (!plugin.getConfig().getString("server-name", "").equalsIgnoreCase("lobby")) {
            notify(player, plugin, "Hub construction is only available on the Lobby backend.");
            return;
        }
        NamespacedKey marker = new NamespacedKey(plugin, "generated_hub_v1");
        if (world.getPersistentDataContainer().has(marker, PersistentDataType.BYTE)) {
            notify(player, plugin, "The starter lobby already exists in this world.");
            return;
        }
        if (Y + 12 >= world.getMaxHeight() || Y - 2 < world.getMinHeight()) {
            notify(player, plugin, "This world\'s build height is incompatible.");
            return;
        }
        // Check ALL positions that may be changed, including NPC positions.
        for (int x = -R - 2; x <= R + 2; x++) {
            for (int z = -R - 2; z <= R + 2; z++) {
                if (x * x + z * z > (R + 2) * (R + 2)) continue;
                for (int y = Y - 2; y <= Y + 10; y++) {
                    if (!world.getBlockAt(x, y, z).getType().isAir()) {
                        notify(player, plugin, "Construction cancelled: blocks already exist near "
                            + x + ", " + y + ", " + z + ". No changes made.");
                        return;
                    }
                }
            }
        }
        // A circular, two-block-thick floating spawn island.
        for (int x = -R; x <= R; x++) {
            for (int z = -R; z <= R; z++) {
                int d2 = x*x + z*z;
                if (d2 > R*R) continue;
                Material floor = d2 < 36 ? Material.QUARTZ_BLOCK
                    : (d2 >= (R - 2)*(R - 2) ? Material.DARK_PRISMARINE
                    : Material.STONE_BRICKS);
                if (Math.abs(x) <= 2 || Math.abs(z) <= 2) {
                    floor = Material.POLISHED_ANDESITE;
                }
                block(world, x, Y-1, z, Material.DEEPSLATE_BRICKS);
                block(world, x, Y, z, floor);
                if (d2 >= (R - 1)*(R - 1) && d2 <= R*R && (x + z) % 3 == 0) {
                    block(world, x, Y+1, z, Material.SEA_LANTERN);
                }
            }
        }
        // A glowing beacon-themed spawn center (no functional beacon required).
        for (int x=-2; x<=2; x++) for (int z=-2; z<=2; z++) {
            block(world, x, Y, z, Material.QUARTZ_BLOCK);
        }
        block(world, 0, Y, 0, Material.SEA_LANTERN);
        // Four colored destinations: cardinal points around the main compass NPC.
        gateway(world, 12, 0, Material.LIME_STAINED_GLASS);
        gateway(world, -12, 0, Material.RED_STAINED_GLASS);
        gateway(world, 0, 12, Material.LIGHT_BLUE_STAINED_GLASS);
        gateway(world, 0, -12, Material.PURPLE_STAINED_GLASS);

        npc(world, 4.5, Y+1, 4.5, "games", npcKey, ChatColor.AQUA + "Choose a Game");
        npc(world, -4.5, Y+1, 4.5, "shop", npcKey, ChatColor.GREEN + "Starter Shop");
        world.setSpawnLocation(0, Y+1, 0);
        world.getPersistentDataContainer().set(marker, PersistentDataType.BYTE, (byte) 1);
        if (player != null) {
            player.teleport(new Location(world, 0.5, Y+1, 0.5));
            player.sendMessage(ChatColor.GREEN + "SHABAB SMP starter hub created at 0, " + Y + ", 0!");
            player.sendMessage(ChatColor.GRAY + "Green=Survival | Red=BedWars | Blue=SkyWars | Purple=Lobby.");
            player.sendMessage(ChatColor.GRAY + "Use /games or the Game Selector NPC to travel.");
        }
        plugin.getLogger().info("Created SHABAB SMP starter hub in " + world.getName());
    }

    private static void notify(Player player, JavaPlugin plugin, String message) {
        if (player != null) player.sendMessage(ChatColor.YELLOW + message);
        plugin.getLogger().warning(message);
    }

    private static void gateway(World world, int x, int z, Material color) {
        // Decorative arch; clicking the game selector NPC handles actual travel.
        for (int d=-2; d<=2; d+=4) {
            boolean eastWest = x != 0;
            int gx = eastWest ? x : d;
            int gz = eastWest ? d : z;
            for (int h=1; h<=4; h++) block(world, gx, Y+h, gz, color);
        }
        if (x != 0) {
            for (int d=-2; d<=2; d++) block(world, x, Y+5, d, color);
        } else {
            for (int d=-2; d<=2; d++) block(world, d, Y+5, z, color);
        }
    }

    private static void npc(World world, double x, double y, double z, String type,
                            NamespacedKey npcKey, String label) {
        world.spawn(new Location(world, x, y, z), Villager.class, villager -> {
            villager.setAI(false);
            villager.setInvulnerable(true);
            villager.setSilent(true);
            villager.setRemoveWhenFarAway(false);
            villager.setCustomName(label);
            villager.setCustomNameVisible(true);
            villager.getPersistentDataContainer().set(npcKey, PersistentDataType.STRING, type);
        });
    }

    private static void block(World world, int x, int y, int z, Material material) {
        Block block = world.getBlockAt(x, y, z);
        block.setType(material, false);
    }
}
