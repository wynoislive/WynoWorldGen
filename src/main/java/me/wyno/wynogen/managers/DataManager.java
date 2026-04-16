package me.wyno.wynogen.managers;

import me.wyno.wynogen.WynoGen;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DataManager {

    private final WynoGen plugin;

    public DataManager(WynoGen plugin) {
        this.plugin = plugin;
    }

    /**
     * Asynchronously saves player data to MySQL.
     */
    public CompletableFuture<Void> savePlayerData(Player player, String identifier) {
        String uuid = player.getUniqueId().toString();
        
        // Capture data on the main thread
        String inv = serializeItems(player.getInventory().getContents());
        String armor = serializeItems(player.getInventory().getArmorContents());
        String ender = serializeItems(player.getEnderChest().getContents());
        String stats = serializeStats(player);
        String potions = serializePotions(player.getActivePotionEffects());
        String adv = serializeAdvancements(player);

        return CompletableFuture.runAsync(() -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                String sql = plugin.getDatabaseManager().getUpsertSQL();

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, uuid);
                    pstmt.setString(2, identifier);
                    pstmt.setString(3, inv);
                    pstmt.setString(4, armor);
                    pstmt.setString(5, ender);
                    pstmt.setString(6, stats);
                    pstmt.setString(7, potions);
                    pstmt.setString(8, adv);
                    pstmt.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Async MySQL Save Error for " + player.getName() + ": " + e.getMessage());
            }
        });
    }

    /**
     * Asynchronously loads player data from MySQL and applies it to the player on the main thread.
     */
    public CompletableFuture<Boolean> loadPlayerData(Player player, String identifier) {
        String uuid = player.getUniqueId().toString();

        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                String sql = "SELECT * FROM player_data WHERE uuid = ? AND world_id = ?;";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, uuid);
                    pstmt.setString(2, identifier);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            // Extract data while still async
                            PlayerDataSnapshot snapshot = new PlayerDataSnapshot(rs);
                            // Schedule application on main thread
                            Bukkit.getScheduler().runTask(plugin, () -> applySnapshot(player, snapshot));
                            return true;
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Async MySQL Load Error for " + player.getName() + ": " + e.getMessage());
            }
            return false;
        }).thenApply(found -> {
            if (!found && !identifier.equalsIgnoreCase("default")) {
                Bukkit.getScheduler().runTask(plugin, () -> resetPlayerState(player));
            }
            return found;
        });
    }

    private void applySnapshot(Player player, PlayerDataSnapshot snapshot) {
        player.getInventory().clear();
        player.getEnderChest().clear();
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        player.getInventory().setContents(deserializeItems(snapshot.inventory));
        player.getInventory().setArmorContents(deserializeItems(snapshot.armor));
        player.getEnderChest().setContents(deserializeItems(snapshot.enderChest));
        deserializeAndApplyStats(player, snapshot.stats);
        
        for (PotionEffect effect : deserializePotions(snapshot.potions)) {
            player.addPotionEffect(effect);
        }
        
        applyAdvancements(player, snapshot.advancements);
    }

    private static class PlayerDataSnapshot {
        final String inventory, armor, enderChest, stats, potions, advancements;
        PlayerDataSnapshot(ResultSet rs) throws SQLException {
            this.inventory = rs.getString("inventory");
            this.armor = rs.getString("armor");
            this.enderChest = rs.getString("ender_chest");
            this.stats = rs.getString("stats");
            this.potions = rs.getString("potions");
            this.advancements = rs.getString("advancements");
        }
    }

    // --- Serialization Helpers (Same as before but clarified for professional use) ---

    private String serializeItems(ItemStack[] items) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
            dataOutput.writeInt(items.length);
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) { return ""; }
    }

    private ItemStack[] deserializeItems(String data) {
        if (data == null || data.isEmpty()) return new ItemStack[0];
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            ItemStack[] items = new ItemStack[dataInput.readInt()];
            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }
            return items;
        } catch (Exception e) { return new ItemStack[0]; }
    }

    private String serializeStats(Player player) {
        return player.getHealth() + ";" +
               player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() + ";" +
               player.getFoodLevel() + ";" +
               player.getSaturation() + ";" +
               player.getExp() + ";" +
               player.getLevel() + ";" +
               player.getGameMode().toString();
    }

    private void deserializeAndApplyStats(Player player, String data) {
        if (data == null || data.isEmpty()) return;
        try {
            String[] parts = data.split(";");
            double maxHealth = Double.parseDouble(parts[1]);
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
            player.setHealth(Math.min(Double.parseDouble(parts[0]), maxHealth));
            player.setFoodLevel(Integer.parseInt(parts[2]));
            player.setSaturation(Float.parseFloat(parts[3]));
            player.setExp(Float.parseFloat(parts[4]));
            player.setLevel(Integer.parseInt(parts[5]));
            player.setGameMode(GameMode.valueOf(parts[6]));
        } catch (Exception e) {}
    }

    private String serializePotions(Collection<PotionEffect> effects) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
            dataOutput.writeInt(effects.size());
            for (PotionEffect effect : effects) {
                dataOutput.writeObject(effect);
            }
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) { return ""; }
    }

    private List<PotionEffect> deserializePotions(String data) {
        List<PotionEffect> effects = new ArrayList<>();
        if (data == null || data.isEmpty()) return effects;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            int size = dataInput.readInt();
            for (int i = 0; i < size; i++) {
                effects.add((PotionEffect) dataInput.readObject());
            }
        } catch (Exception e) {}
        return effects;
    }

    private String serializeAdvancements(Player player) {
        StringBuilder sb = new StringBuilder();
        Iterator<Advancement> it = Bukkit.advancementIterator();
        while (it.hasNext()) {
            Advancement adv = it.next();
            AdvancementProgress progress = player.getAdvancementProgress(adv);
            if (!progress.getAwardedCriteria().isEmpty()) {
                sb.append(adv.getKey().toString()).append(":").append(String.join(",", progress.getAwardedCriteria())).append(";");
            }
        }
        return sb.toString();
    }

    private void applyAdvancements(Player player, String data) {
        if (data == null || data.isEmpty()) return;
        resetAdvancements(player);
        String[] primaryParts = data.split(";");
        for (String part : primaryParts) {
            String[] kv = part.split(":");
            if (kv.length < 2) continue;
            NamespacedKey key = NamespacedKey.fromString(kv[0]);
            if (key == null) continue;
            Advancement adv = Bukkit.getAdvancement(key);
            if (adv == null) continue;
            AdvancementProgress progress = player.getAdvancementProgress(adv);
            for (String crit : kv[1].split(",")) {
                progress.awardCriteria(crit);
            }
        }
    }

    private void resetAdvancements(Player player) {
        Iterator<Advancement> it = Bukkit.advancementIterator();
        while (it.hasNext()) {
            Advancement adv = it.next();
            AdvancementProgress progress = player.getAdvancementProgress(adv);
            for (String crit : progress.getAwardedCriteria()) {
                progress.revokeCriteria(crit);
            }
        }
    }

    private void resetPlayerState(Player player) {
        player.getInventory().clear();
        player.getEnderChest().clear();
        player.setHealth(20.0);
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
        player.setFoodLevel(20);
        player.setSaturation(5.0f);
        player.setExp(0);
        player.setLevel(0);
        player.setGameMode(GameMode.SURVIVAL);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        resetAdvancements(player);
    }
}
