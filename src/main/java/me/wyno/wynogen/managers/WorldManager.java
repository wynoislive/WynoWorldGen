package me.wyno.wynogen.managers;

import me.wyno.wynogen.WynoGen;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldManager {

    private final WynoGen plugin;
    private final File configFile;
    private YamlConfiguration config;
    
    public static class WorldData {
        public String difficultyStr;
        public boolean tightBiomes;
        public WorldData(String difficultyStr, boolean tightBiomes) {
            this.difficultyStr = difficultyStr;
            this.tightBiomes = tightBiomes;
        }
    }

    private final Map<String, WorldData> featuredWorlds = new HashMap<>();

    public WorldManager(WynoGen plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "worlds.yml");
        loadConfig();
    }

    private void loadConfig() {
        if (!configFile.exists()) {
            try {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        if (config.contains("worlds")) {
            for (String key : config.getConfigurationSection("worlds").getKeys(false)) {
                if (config.isString("worlds." + key)) {
                    // Backwards compatibility for v1
                    String diff = config.getString("worlds." + key);
                    config.set("worlds." + key, null); // Clear old scalar
                    config.set("worlds." + key + ".difficulty", diff);
                    config.set("worlds." + key + ".tight_biomes", false);
                    featuredWorlds.put(key, new WorldData(diff, false));
                } else {
                    String diff = config.getString("worlds." + key + ".difficulty");
                    boolean tight = config.getBoolean("worlds." + key + ".tight_biomes", false);
                    featuredWorlds.put(key, new WorldData(diff, tight));
                }
            }
            saveConfig();
        }
    }

    public void loadAllWorlds() {
        if (featuredWorlds.isEmpty()) {
            plugin.getLogger().info("No featured worlds to load.");
            return;
        }
        plugin.getLogger().info("Loading " + featuredWorlds.size() + " featured worlds...");
        for (Map.Entry<String, WorldData> entry : featuredWorlds.entrySet()) {
            createWorld(entry.getKey(), entry.getValue().difficultyStr, entry.getValue().tightBiomes, false);
        }
    }

    public boolean createWorld(String name, String difficultyStr, boolean tightBiomes, boolean isNew) {
        if (Bukkit.getWorld(name) != null) return false;

        plugin.getLogger().info("Initiating creation of world: " + name + " (Difficulty: " + difficultyStr + ", Tight Biomes: " + tightBiomes + ")");
        
        WorldCreator creator = new WorldCreator(name);
        
        // Inject custom BiomeProvider if tight rotation is enabled
        if (tightBiomes) {
            plugin.getLogger().info("Applying WynoBiomeProvider to world: " + name);
            creator.biomeProvider(new WynoBiomeProvider());
        }

        try {
            World world = creator.createWorld();
            if (world != null) {
                Difficulty difficulty = Difficulty.valueOf(difficultyStr.toUpperCase().replace("MEDIUM", "NORMAL"));
                world.setDifficulty(difficulty);
                plugin.getLogger().info("World '" + name + "' created/loaded successfully.");

                if (isNew) {
                    featuredWorlds.put(name, new WorldData(difficultyStr.toUpperCase(), tightBiomes));
                    config.set("worlds." + name + ".difficulty", difficultyStr.toUpperCase());
                    config.set("worlds." + name + ".tight_biomes", tightBiomes);
                    saveConfig();
                    plugin.getLogger().info("World '" + name + "' added to tracked worlds.");
                }
                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().severe("CRITICAL ERROR during creation of world '" + name + "': " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteWorld(String name) {
        if (!featuredWorlds.containsKey(name)) return false;

        World world = Bukkit.getWorld(name);
        if (world != null) {
            for (org.bukkit.entity.Player player : world.getPlayers()) {
                player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                player.sendMessage(plugin.getLanguageManager().getMessage("commands.delete.success").replace("{name}", name));
            }
            Bukkit.unloadWorld(world, false);
        }

        featuredWorlds.remove(name);
        config.set("worlds." + name, null);
        saveConfig();

        File worldFolder = new File(Bukkit.getWorldContainer(), name);
        try {
            deleteDirectory(worldFolder.toPath());
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to delete world folder: " + name);
            e.printStackTrace();
            return false;
        }
    }

    public boolean isFeaturedWorld(String name) {
        return featuredWorlds.containsKey(name);
    }

    public List<String> getFeaturedWorldNames() {
        return new ArrayList<>(featuredWorlds.keySet());
    }

    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteDirectory(Path path) throws IOException {
        if (!Files.exists(path)) return;
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) throw exc;
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
