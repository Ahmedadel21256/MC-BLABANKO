package net.shababsmp.core;

import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class ShababCore extends JavaPlugin implements Listener {
    private static final String GAMES_TITLE = ChatColor.DARK_AQUA + "SHABAB SMP | Games";
    private static final String SHOP_TITLE = ChatColor.DARK_GREEN + "SHABAB SMP | Shop";
    private static final Map<Integer, String> GAMES = Map.of(
        10, "lobby", 12, "survival", 14, "bedwars", 16, "skywars"
    );
    private static final Map<Integer, Offer> OFFERS = Map.of(
        10, new Offer(Material.BREAD, 16, 3),
        12, new Offer(Material.COOKED_BEEF, 16, 6),
        14, new Offer(Material.OAK_LOG, 32, 4),
        16, new Offer(Material.IRON_INGOT, 8, 10)
    );
    private NamespacedKey npcKey;
    private NamespacedKey selectorKey;

    private record Offer(Material material, int quantity, int emeralds) {}

    @Override
    public void onEnable() {
        saveDefaultConfig();
        npcKey = new NamespacedKey(this, "network_npc");
        selectorKey = new NamespacedKey(this, "games_selector");
        Bukkit.getPluginManager().registerEvents(this, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getLogger().info("ShababCore ready on " + getConfig().getString("server-name", "unknown"));
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Use this command in Minecraft.");
            return true;
        }
        String name = command.getName().toLowerCase(java.util.Locale.ROOT);
        if (name.equals("hub")) {
            if (!player.hasPermission("shabab.use")) return noPermission(player);
            connect(player, "lobby");
            return true;
        }
        if (name.equals("games")) {
            if (!player.hasPermission("shabab.use")) return noPermission(player);
            openGames(player);
            return true;
        }
        if (name.equals("shop")) {
            if (!player.hasPermission("shabab.shop")) return noPermission(player);
            openShop(player);
            return true;
        }
        if (name.equals("buildhub")) {
            if (!player.hasPermission("shabab.admin")) return noPermission(player);
            HubBuilder.build(player, this, npcKey);
            return true;
        }
        if (name.equals("shababnpc")) {
            if (!player.hasPermission("shabab.admin")) return noPermission(player);
            if (args.length != 1 || !Set.of("games", "shop").contains(args[0].toLowerCase(java.util.Locale.ROOT))) {
                player.sendMessage(ChatColor.YELLOW + "Usage: /shababnpc games|shop");
                return true;
            }
            spawnNpc(player, args[0].toLowerCase(java.util.Locale.ROOT));
            return true;
        }
        return false;
    }

    private boolean noPermission(Player player) {
        player.sendMessage(ChatColor.RED + "You do not have permission.");
        return true;
    }

    private ItemStack icon(Material material, String text) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + text);
        item.setItemMeta(meta);
        return item;
    }

    private void openGames(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, GAMES_TITLE);
        inv.setItem(10, icon(Material.COMPASS, "Lobby"));
        inv.setItem(12, icon(Material.GRASS_BLOCK, "Survival SMP"));
        inv.setItem(14, icon(Material.RED_BED, "BedWars"));
        inv.setItem(16, icon(Material.ENDER_EYE, "SkyWars"));
        player.openInventory(inv);
    }

    private void openShop(Player player) {
        if (!getConfig().getBoolean("shop-enabled", true)) {
            player.sendMessage(ChatColor.RED + "Shop is disabled on this server.");
            return;
        }
        Inventory inv = Bukkit.createInventory(null, 27, SHOP_TITLE);
        for (Map.Entry<Integer, Offer> entry : OFFERS.entrySet()) {
            Offer offer = entry.getValue();
            inv.setItem(entry.getKey(), icon(offer.material(),
                offer.quantity() + " " + offer.material().name().replace('_', ' ') + " | " + offer.emeralds() + " emeralds"));
        }
        player.openInventory(inv);
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!getConfig().getString("server-name", "").equalsIgnoreCase("lobby")) return;
        String welcomeName = getConfig().getString("featured-player", "I_luca1");
        player.sendMessage(ChatColor.AQUA + "Welcome to SHABAB SMP, " + player.getName() + "!");
        player.sendMessage(ChatColor.GRAY + "Network host: " + welcomeName
            + " | Use your compass or /games to select a server.");
        Bukkit.getScheduler().runTask(this, () -> {
            if (!player.isOnline()) return;
            if (hasSelector(player)) return;
            ItemStack compass = icon(Material.COMPASS, "SHABAB SMP | Game Selector");
            // Item meta must be set back after editing PersistentDataContainer.
            ItemMeta meta = compass.getItemMeta();
            meta.getPersistentDataContainer().set(selectorKey, PersistentDataType.BYTE, (byte) 1);
            compass.setItemMeta(meta);
            int slot = player.getInventory().firstEmpty();
            if (slot >= 0) player.getInventory().setItem(slot, compass);
        });
    }

    private boolean hasSelector(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isSelector(item)) return true;
        }
        return false;
    }

    private boolean isSelector(ItemStack item) {
        return item != null && item.getType() == Material.COMPASS
            && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer()
                .has(selectorKey, PersistentDataType.BYTE);
    }

    @EventHandler
    public void onSelectorUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!getConfig().getString("server-name", "").equalsIgnoreCase("lobby")) return;
        if (!isSelector(event.getItem())) return;
        event.setCancelled(true);
        openGames(event.getPlayer());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        boolean games = GAMES_TITLE.equals(title);
        boolean shop = SHOP_TITLE.equals(title);
        if (!games && !shop) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        // Ignore player inventory slots underneath the upper inventory.
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;
        int slot = event.getRawSlot();
        if (games && GAMES.containsKey(slot) && player.hasPermission("shabab.use")) {
            player.closeInventory();
            connect(player, GAMES.get(slot));
        } else if (shop && OFFERS.containsKey(slot) && player.hasPermission("shabab.shop")) {
            buy(player, OFFERS.get(slot));
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();
        if (GAMES_TITLE.equals(title) || SHOP_TITLE.equals(title)) event.setCancelled(true);
    }

    private void buy(Player player, Offer offer) {
        if (!getConfig().getBoolean("shop-enabled", true)) return;
        ItemStack currency = new ItemStack(Material.EMERALD);
        if (!player.getInventory().containsAtLeast(currency, offer.emeralds())) {
            player.sendMessage(ChatColor.RED + "You need " + offer.emeralds() + " emeralds.");
            return;
        }
        // Refuse before charging if the inventory cannot hold the full purchase.
        ItemStack reward = new ItemStack(offer.material(), offer.quantity());
        if (!fits(player, reward)) {
            player.sendMessage(ChatColor.RED + "Make space in your inventory first.");
            return;
        }
        int need = offer.emeralds();
        for (int slot = 0; slot < 36 && need > 0; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack == null || stack.getType() != Material.EMERALD) continue;
            int removed = Math.min(need, stack.getAmount());
            stack.setAmount(stack.getAmount() - removed);
            need -= removed;
        }
        player.getInventory().addItem(reward);
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);
        player.sendMessage(ChatColor.GREEN + "Purchased " + offer.quantity() + " " + offer.material().name());
    }

    private boolean fits(Player player, ItemStack reward) {
        int free = 0;
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack == null || stack.getType().isAir()) free += reward.getMaxStackSize();
            else if (stack.isSimilar(reward)) free += Math.max(0, stack.getMaxStackSize() - stack.getAmount());
            if (free >= reward.getAmount()) return true;
        }
        return false;
    }

    private void connect(Player player, String target) {
        String current = getConfig().getString("server-name", "lobby");
        if (current.equalsIgnoreCase(target)) {
            player.sendMessage(ChatColor.YELLOW + "You are already on " + target + ".");
            return;
        }
        try {
            java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
            java.io.DataOutputStream data = new java.io.DataOutputStream(bytes);
            data.writeUTF("Connect");
            data.writeUTF(target);
            player.sendPluginMessage(this, "BungeeCord", bytes.toByteArray());
            player.sendMessage(ChatColor.AQUA + "Connecting to " + target + "...");
        } catch (java.io.IOException e) {
            getLogger().warning("Could not send network request: " + e.getMessage());
            player.sendMessage(ChatColor.RED + "Network connection is unavailable.");
        }
    }

    @EventHandler
    public void onNpc(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (!(entity instanceof Villager)) return;
        String type = entity.getPersistentDataContainer().get(npcKey, PersistentDataType.STRING);
        if (type == null) return;
        event.setCancelled(true);
        if (type.equals("games")) openGames(event.getPlayer());
        if (type.equals("shop")) openShop(event.getPlayer());
    }

    private void spawnNpc(Player player, String type) {
        Villager npc = player.getWorld().spawn(player.getLocation(), Villager.class, villager -> {
            villager.setAI(false);
            villager.setInvulnerable(true);
            villager.setSilent(true);
            villager.setRemoveWhenFarAway(false);
            villager.setCustomName(type.equals("games") ? ChatColor.AQUA + "Select Game" : ChatColor.GREEN + "Shopkeeper");
            villager.setCustomNameVisible(true);
            villager.getPersistentDataContainer().set(npcKey, PersistentDataType.STRING, type);
        });
        player.sendMessage(ChatColor.GREEN + "NPC created: " + type + " (entity " + npc.getUniqueId() + ")");
    }
}
