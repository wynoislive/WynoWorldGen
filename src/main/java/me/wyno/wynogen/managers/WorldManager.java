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

    // -------------------------------------------------------------------------
    // WorldData — stores metadata for each parent Featured World
    // -------------------------------------------------------------------------
    public static class WorldData {
        public String difficultyStr;
        public boolean tightBiomes;
        /** Name of the companion Nether world, or null if not generated. */
        public String netherWorldName;
        /** Name of the companion End world, or null if not generated. */
        public String endWorldName;

        public WorldData(String difficultyStr, boolean tightBiomes,
                         String netherWorldName, String endWorldName) {
            this.difficultyStr = difficultyStr;
            this.tightBiomes = tightBiomes;
            this.netherWorldName = netherWorldName;
            this.endWorldName = endWorldName;
        }
    }

    /** Map of parentWorldName → WorldData for every tracked Featured World. */
    private final Map<String, WorldData> featuredWorlds = new HashMap<>();

    /**
     * Reverse-lookup: companionWorldName → parentWorldName.
     * Populated whenever a companion world is registered.
     */
    private final Map<String, String> companionToParent = new HashMap<>();

    public WorldManager(WynoGen plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "worlds.yml");
        loadConfig();
    }

    // -------------------------------------------------------------------------
    // Config persistence
    // -------------------------------------------------------------------------

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

        if (!config.contains("worlds")) return;

        boolean dirty = false;

        for (String key : config.getConfigurationSection("worlds").getKeys(false)) {
            // --- Backwards compatibility: old format was worlds.<name> = "EASY" ---
            if (config.isString("worlds." + key)) {
                String diff = config.getString("worlds." + key);
                config.set("worlds." + key, null);
                config.set("worlds." + key + ".difficulty", diff);
                config.set("worlds." + key + ".tight_biomes", false);
                featuredWorlds.put(key, new WorldData(diff, false, null, null));
                dirty = true;
                continue;
            }

            String diff          = config.getString("worlds." + key + ".difficulty", "NORMAL");
            boolean tight        = config.getBoolean("worlds." + key + ".tight_biomes", false);
            String netherName    = config.getString("worlds." + key + ".nether_world", null);
            String endName       = config.getString("worlds." + key + ".end_world", null);

            featuredWorlds.put(key, new WorldData(diff, tight, netherName, endName));

            // Register companions in reverse-lookup
            if (netherName != null) companionToParent.put(netherName, key);
            if (endName    != null) companionToParent.put(endName,    key);
        }

        if (dirty) saveConfig();
    }

    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------------------------
    // Startup: load all tracked worlds (overworld + companions)
    // -------------------------------------------------------------------------

    public void loadAllWorlds() {
        if (featuredWorlds.isEmpty()) {
            plugin.getLogger().info("No featured worlds to load.");
            return;
        }
        plugin.getLogger().info("Loading " + featuredWorlds.size() + " featured world(s)...");
        for (Map.Entry<String, WorldData> entry : featuredWorlds.entrySet()) {
            String parentName = entry.getKey();
            WorldData data    = entry.getValue();

            // Load overworld
            loadWorldByName(parentName, World.Environment.NORMAL, data.tightBiomes);

            // Load companions if they were previously created
            if (data.netherWorldName != null) {
                loadWorldByName(data.netherWorldName, World.Environment.NETHER, false);
            }
            if (data.endWorldName != null) {
                loadWorldByName(data.endWorldName, World.Environment.THE_END, false);
            }
        }
    }

    /**
     * Loads (or skips if already loaded) a world by name + environment.
     * Does NOT track it as a new featured world — only for startup loading.
     */
    private void loadWorldByName(String name, World.Environment environment, boolean tightBiomes) {
        if (Bukkit.getWorld(name) != null) {
            plugin.getLogger().info("World '" + name + "' is already loaded. Skipping.");
            return;
        }
        WorldCreator creator = new WorldCreator(name).environment(environment);
        if (tightBiomes && environment == World.Environment.NORMAL) {
            creator.biomeProvider(new WynoBiomeProvider());
        }
        try {
            World world = creator.createWorld();
            if (world != null) {
                plugin.getLogger().info("Loaded world: " + name + " [" + environment.name() + "]");
            } else {
                plugin.getLogger().warning("Failed to load world '" + name + "' — createWorld() returned null.");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error loading world '" + name + "': " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Create: parent + companions
    // -------------------------------------------------------------------------

    /**
     * Creates a new Featured World and its companion Nether/End worlds
     * based on config toggles.
     *
     * @param name          The parent world name (e.g. "EasyWorld")
     * @param difficultyStr "EASY", "MEDIUM", or "HARD"
     * @param tightBiomes   Whether to apply the WynoBiomeProvider
     * @param isNew         true when called from a player command, false on startup reload
     * @return true if the parent world was created successfully
     */
    public boolean createWorld(String name, String difficultyStr, boolean tightBiomes, boolean isNew) {
        if (Bukkit.getWorld(name) != null) {
            plugin.getLogger().warning("World '" + name + "' is already loaded. Aborting create.");
            return false;
        }

        plugin.getLogger().info("Initiating creation of world: " + name
                + " (Difficulty: " + difficultyStr
                + ", Tight Biomes: " + tightBiomes + ")");

        // --- Create the overworld ---
        WorldCreator creator = new WorldCreator(name).environment(World.Environment.NORMAL);
        if (tightBiomes) {
            plugin.getLogger().info("Applying WynoBiomeProvider to world: " + name);
            creator.biomeProvider(new WynoBiomeProvider());
        }

        World world;
        try {
            world = creator.createWorld();
        } catch (Exception e) {
            plugin.getLogger().severe("CRITICAL ERROR creating world '" + name + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        if (world == null) {
            plugin.getLogger().severe("createWorld() returned null for '" + name + "'.");
            return false;
        }

        Difficulty difficulty = Difficulty.valueOf(
                difficultyStr.toUpperCase().replace("MEDIUM", "NORMAL"));
        world.setDifficulty(difficulty);
        plugin.getLogger().info("World '" + name + "' created successfully.");

        // --- Determine companion names ---
        boolean wantNether = plugin.getConfig().getBoolean("options.generate_nether", true);
        boolean wantEnd    = plugin.getConfig().getBoolean("options.generate_end",    true);

        String netherName = wantNether ? name + "-nether" : null;
        String endName    = wantEnd    ? name + "-end"    : null;

        // --- Create Nether companion ---
        if (wantNether) {
            boolean netherOk = createCompanionWorld(netherName, World.Environment.NETHER, difficulty);
            if (!netherOk) {
                plugin.getLogger().warning("Could not create Nether companion for '" + name + "'.");
                netherName = null; // Don't track if creation failed
            }
        }

        // --- Create End companion ---
        if (wantEnd) {
            boolean endOk = createCompanionWorld(endName, World.Environment.THE_END, difficulty);
            if (!endOk) {
                plugin.getLogger().warning("Could not create End companion for '" + name + "'.");
                endName = null; // Don't track if creation failed
            }
        }

        // --- Persist ---
        if (isNew) {
            WorldData data = new WorldData(difficultyStr.toUpperCase(), tightBiomes, netherName, endName);
            featuredWorlds.put(name, data);

            config.set("worlds." + name + ".difficulty",   difficultyStr.toUpperCase());
            config.set("worlds." + name + ".tight_biomes", tightBiomes);
            config.set("worlds." + name + ".nether_world", netherName);
            config.set("worlds." + name + ".end_world",    endName);
            saveConfig();

            if (netherName != null) companionToParent.put(netherName, name);
            if (endName    != null) companionToParent.put(endName,    name);

            plugin.getLogger().info("World '" + name + "' and companions registered in worlds.yml.");
        }

        return true;
    }

    /**
     * Creates a companion world (Nether or End) for an existing Featured World.
     *
     * @param name        Full world name (e.g. "EasyWorld-nether")
     * @param environment NETHER or THE_END
     * @param difficulty  Bukkit Difficulty to set
     * @return true if successful
     */
    private boolean createCompanionWorld(String name, World.Environment environment, Difficulty difficulty) {
        if (Bukkit.getWorld(name) != null) {
            plugin.getLogger().info("Companion world '" + name + "' already exists. Skipping.");
            return true;
        }
        plugin.getLogger().info("Creating companion world: " + name + " [" + environment.name() + "]");
        try {
            WorldCreator creator = new WorldCreator(name).environment(environment);
            World w = creator.createWorld();
            if (w != null) {
                w.setDifficulty(difficulty);
                plugin.getLogger().info("Companion world '" + name + "' created successfully.");
                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error creating companion world '" + name + "': " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Delete: parent + companions
    // -------------------------------------------------------------------------

    /**
     * Deletes a Featured World and all its companion worlds.
     * Teleports any players in the companion worlds to the main server world first.
     *
     * @param name Parent world name
     * @return true if the parent world was found and removed
     */
    public boolean deleteWorld(String name) {
        if (!featuredWorlds.containsKey(name)) return false;

        WorldData data = featuredWorlds.get(name);
        World defaultWorld = Bukkit.getWorlds().get(0);

        // 1. Evict players from ALL related worlds (parent + companions)
        evictPlayersFromWorld(name, defaultWorld, name);
        if (data.netherWorldName != null) evictPlayersFromWorld(data.netherWorldName, defaultWorld, name);
        if (data.endWorldName    != null) evictPlayersFromWorld(data.endWorldName,    defaultWorld, name);

        // 2. Unload + delete companions
        if (data.netherWorldName != null) {
            unloadAndDeleteWorld(data.netherWorldName);
            companionToParent.remove(data.netherWorldName);
        }
        if (data.endWorldName != null) {
            unloadAndDeleteWorld(data.endWorldName);
            companionToParent.remove(data.endWorldName);
        }

        // 3. Unload + delete parent
        unloadAndDeleteWorld(name);

        // 4. Remove from tracking
        featuredWorlds.remove(name);
        config.set("worlds." + name, null);
        saveConfig();

        plugin.getLogger().info("World '" + name + "' and companions deleted.");
        return true;
    }

    private void evictPlayersFromWorld(String worldName, World destination, String parentName) {
        World w = Bukkit.getWorld(worldName);
        if (w == null) return;
        for (org.bukkit.entity.Player player : w.getPlayers()) {
            player.teleport(destination.getSpawnLocation());
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.delete.success")
                    .replace("{name}", parentName));
        }
    }

    private void unloadAndDeleteWorld(String worldName) {
        World w = Bukkit.getWorld(worldName);
        if (w != null) {
            Bukkit.unloadWorld(w, false);
        }
        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        try {
            deleteDirectory(worldFolder.toPath());
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to delete world folder: " + worldName);
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------------------------
    // Query helpers
    // -------------------------------------------------------------------------

    /** Returns true if {@code name} is a tracked parent Featured World. */
    public boolean isFeaturedWorld(String name) {
        return featuredWorlds.containsKey(name);
    }

    /**
     * Returns true if {@code name} is a companion world (-nether or -end)
     * that belongs to a tracked Featured World.
     */
    public boolean isCompanionWorld(String name) {
        return companionToParent.containsKey(name);
    }

    /**
     * Returns true if the world is managed by this plugin (parent OR companion).
     * Use this for data-save/load decisions.
     */
    public boolean isManagedWorld(String name) {
        return isFeaturedWorld(name) || isCompanionWorld(name);
    }

    /**
     * Given a companion world name, returns the parent world name.
     * Returns null if {@code worldName} is not a companion.
     */
    public String getParentWorldName(String worldName) {
        return companionToParent.get(worldName);
    }

    /**
     * Returns the companion Nether world for the given parent world name,
     * or null if none is registered.
     */
    public World getNetherWorld(String parentName) {
        WorldData data = featuredWorlds.get(parentName);
        if (data == null || data.netherWorldName == null) return null;
        return Bukkit.getWorld(data.netherWorldName);
    }

    /**
     * Returns the companion End world for the given parent world name,
     * or null if none is registered.
     */
    public World getEndWorld(String parentName) {
        WorldData data = featuredWorlds.get(parentName);
        if (data == null || data.endWorldName == null) return null;
        return Bukkit.getWorld(data.endWorldName);
    }

    /**
     * Returns the configured Nether world name for the given parent (may be null).
     */
    public String getNetherWorldName(String parentName) {
        WorldData data = featuredWorlds.get(parentName);
        return data != null ? data.netherWorldName : null;
    }

    /**
     * Returns the configured End world name for the given parent (may be null).
     */
    public String getEndWorldName(String parentName) {
        WorldData data = featuredWorlds.get(parentName);
        return data != null ? data.endWorldName : null;
    }

    /** Returns the list of all parent Featured World names (companions are excluded). */
    public List<String> getFeaturedWorldNames() {
        return new ArrayList<>(featuredWorlds.keySet());
    }

    /** Returns the WorldData for the given parent world, or null. */
    public WorldData getWorldData(String name) {
        return featuredWorlds.get(name);
    }

    // -------------------------------------------------------------------------
    // File utilities
    // -------------------------------------------------------------------------

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
