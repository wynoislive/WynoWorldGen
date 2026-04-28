package me.wyno.wynogen.commands;

import me.wyno.wynogen.WynoGen;
import me.wyno.wynogen.managers.LanguageManager;
import me.wyno.wynogen.managers.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FWCommand implements CommandExecutor, TabCompleter {

    private final WynoGen plugin;
    private final LanguageManager lang;

    public FWCommand(WynoGen plugin) {
        this.plugin = plugin;
        this.lang   = plugin.getLanguageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(lang.getMessage("general.only-players"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create"   -> handleCreate(player, args);
            case "delete"   -> handleDelete(player, args);
            case "join"     -> handleJoin(player, args);
            case "exit"     -> handleExit(player);
            case "list"     -> handleList(player);
            case "reload"   -> handleReload(player);
            case "update"   -> handleUpdate(player);
            case "rollback" -> handleRollback(player);
            default         -> sendHelp(player);
        }

        return true;
    }

    // =========================================================================
    // /fw create <name> <difficulty> [tight]
    // =========================================================================

    private void handleCreate(Player player, String[] args) {
        if (!player.hasPermission("wynogen.admin")) {
            player.sendMessage(lang.getMessage("general.no-permission"));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(lang.getMessage("commands.create.usage"));
            return;
        }

        String name       = args[1];
        String difficulty = args[2].toUpperCase();
        boolean tight     = args.length >= 4 && args[3].equalsIgnoreCase("tight");

        if (!Arrays.asList("EASY", "MEDIUM", "HARD").contains(difficulty)) {
            player.sendMessage(lang.getMessage("commands.create.invalid-difficulty"));
            return;
        }

        String tightMsg = tight ? " §8[§dTight Biomes§8]" : "";
        player.sendMessage(lang.getMessage("commands.create.starting")
                .replace("{name}", name)
                .replace("{difficulty}", difficulty)
                .replace("{tight}", tightMsg));

        boolean success = plugin.getWorldManager().createWorld(name, difficulty, tight, true);

        if (success) {
            player.sendMessage(lang.getMessage("commands.create.success").replace("{name}", name));

            // --- Companion world feedback ---
            sendCompanionCreatedMessage(player, name);
        } else {
            player.sendMessage(lang.getMessage("commands.create.failed"));
        }
    }

    /**
     * Sends a message to the creator indicating which companion worlds were generated.
     */
    private void sendCompanionCreatedMessage(Player player, String parentName) {
        WorldManager wm = plugin.getWorldManager();
        WorldManager.WorldData data = wm.getWorldData(parentName);
        if (data == null) return;

        String netherName = data.netherWorldName;
        String endName    = data.endWorldName;

        if (netherName != null && endName != null) {
            player.sendMessage(lang.getMessage("world.companion-created")
                    .replace("{nether}", netherName)
                    .replace("{end}", endName));
        } else if (netherName != null) {
            player.sendMessage(lang.getMessage("world.companion-nether-only")
                    .replace("{nether}", netherName));
        } else if (endName != null) {
            player.sendMessage(lang.getMessage("world.companion-end-only")
                    .replace("{end}", endName));
        }
        // If both are null (both disabled in config), no companion message shown
    }

    // =========================================================================
    // /fw delete <name>
    // =========================================================================

    private void handleDelete(Player player, String[] args) {
        if (!player.hasPermission("wynogen.admin")) {
            player.sendMessage(lang.getMessage("general.no-permission"));
            return;
        }

        if (args.length < 2) {
            sendWorldList(player, lang.getRawMessage("commands.delete.usage"));
            return;
        }

        String name = args[1];
        if (plugin.getWorldManager().deleteWorld(name)) {
            player.sendMessage(lang.getMessage("commands.delete.success").replace("{name}", name));
            player.sendMessage(lang.getMessage("world.companion-deleted"));
        } else {
            player.sendMessage(lang.getMessage("commands.delete.failed").replace("{name}", name));
        }
    }

    // =========================================================================
    // /fw join <name>
    // =========================================================================

    private void handleJoin(Player player, String[] args) {
        if (!player.hasPermission("wynogen.use")) {
            player.sendMessage(lang.getMessage("general.no-permission"));
            return;
        }

        if (args.length < 2) {
            sendWorldList(player, lang.getRawMessage("commands.join.usage"));
            return;
        }

        String name        = args[1];
        World targetWorld  = Bukkit.getWorld(name);

        if (targetWorld == null || !plugin.getWorldManager().isFeaturedWorld(name)) {
            player.sendMessage(lang.getMessage("general.world-not-found"));
            return;
        }

        String currentWorld = player.getWorld().getName();
        String currentId    = plugin.getWorldManager().isManagedWorld(currentWorld)
                ? currentWorld : "default";

        player.sendMessage(lang.getMessage("commands.join.preparing"));

        // 1. Save current state → 2. Load target state → 3. Teleport
        plugin.getDataManager().savePlayerData(player, currentId).thenRun(() ->
                plugin.getDataManager().loadPlayerData(player, name).thenAccept(found ->
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            player.teleport(targetWorld.getSpawnLocation());
                            applySafety(player);
                            player.sendMessage(lang.getMessage("commands.join.success")
                                    .replace("{name}", name));
                        })
                )
        );
    }

    // =========================================================================
    // /fw exit
    // =========================================================================

    private void handleExit(Player player) {
        if (!player.hasPermission("wynogen.use")) {
            player.sendMessage(lang.getMessage("general.no-permission"));
            return;
        }

        String currentWorld = player.getWorld().getName();
        WorldManager wm     = plugin.getWorldManager();

        // Allow exit from any managed world (parent or companion)
        if (!wm.isManagedWorld(currentWorld)) {
            player.sendMessage(lang.getMessage("general.not-in-featured"));
            return;
        }

        player.sendMessage(lang.getMessage("commands.exit.preparing"));

        // Save current world data, then load default profile
        plugin.getDataManager().savePlayerData(player, currentWorld).thenRun(() ->
                plugin.getDataManager().loadPlayerData(player, "default").thenAccept(found ->
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            World defaultWorld = Bukkit.getWorlds().get(0);
                            org.bukkit.Location spawn = player.getBedSpawnLocation();
                            if (spawn == null || !spawn.getWorld().getName()
                                    .equals(defaultWorld.getName())) {
                                spawn = defaultWorld.getSpawnLocation();
                            }
                            player.teleport(spawn);
                            applySafety(player);
                            player.sendMessage(lang.getMessage("commands.exit.success"));
                        })
                )
        );
    }

    // =========================================================================
    // /fw list
    // =========================================================================

    private void handleList(Player player) {
        sendWorldList(player, null);
    }

    // =========================================================================
    // /fw reload
    // =========================================================================

    private void handleReload(Player player) {
        if (!player.hasPermission("wynogen.admin")) {
            player.sendMessage(lang.getMessage("general.no-permission"));
            return;
        }
        plugin.reloadPlugin();
        player.sendMessage(lang.getMessage("general.reloaded"));
    }

    // =========================================================================
    // /fw update
    // =========================================================================

    private void handleUpdate(Player player) {
        if (!player.hasPermission("wynogen.admin")) {
            player.sendMessage(lang.getMessage("general.no-permission"));
            return;
        }
        player.sendMessage(lang.getMessage("update.checking"));
        plugin.getUpdateManager().checkForUpdates().thenAccept(available -> {
            if (!available) {
                player.sendMessage(lang.getPrefix() + "&cNo updates found.");
                return;
            }
            player.sendMessage(lang.getMessage("update.downloading"));
            plugin.getUpdateManager().downloadAndInstall().thenAccept(success -> {
                if (success) {
                    player.sendMessage(lang.getMessage("update.success"));
                } else {
                    player.sendMessage(lang.getMessage("update.error")
                            .replace("{error}", "Download failed or JAR locked."));
                }
            });
        });
    }

    // =========================================================================
    // /fw rollback
    // =========================================================================

    private void handleRollback(Player player) {
        if (!player.hasPermission("wynogen.admin")) {
            player.sendMessage(lang.getMessage("general.no-permission"));
            return;
        }
        if (plugin.getUpdateManager().rollback()) {
            player.sendMessage(lang.getMessage("update.rollback-success"));
        } else {
            player.sendMessage(lang.getMessage("update.error")
                    .replace("{error}", "No backup found or restoration failed."));
        }
    }

    // =========================================================================
    // Shared UI helpers
    // =========================================================================

    private void sendWorldList(Player player, String subHeader) {
        List<String> worlds = plugin.getWorldManager().getFeaturedWorldNames();
        player.sendMessage(lang.getMessage("commands.list.header"));
        if (subHeader != null) player.sendMessage(subHeader);

        if (worlds.isEmpty()) {
            player.sendMessage(lang.getMessage("commands.list.no-worlds"));
        } else {
            for (String worldName : worlds) {
                WorldManager.WorldData data = plugin.getWorldManager().getWorldData(worldName);
                String diff = data != null ? data.difficultyStr : "UNKNOWN";

                // Build companion status suffix
                StringBuilder companions = new StringBuilder();
                if (data != null) {
                    if (data.netherWorldName != null) companions.append(" §8[§cN§8]");
                    if (data.endWorldName    != null) companions.append(" §8[§5E§8]");
                }

                player.sendMessage(lang.getRawMessage("commands.list.item")
                        .replace("{name}", worldName + companions)
                        .replace("{difficulty}", diff));
            }
        }
        player.sendMessage(lang.getMessage("commands.list.footer"));
    }

    private void sendHelp(Player player) {
        player.sendMessage(lang.getMessage("commands.help.header"));
        player.sendMessage(lang.getRawMessage("commands.help.join"));
        player.sendMessage(lang.getRawMessage("commands.help.list"));
        player.sendMessage(lang.getRawMessage("commands.help.exit"));
        if (player.hasPermission("wynogen.admin")) {
            player.sendMessage(lang.getRawMessage("commands.help.create"));
            player.sendMessage(lang.getRawMessage("commands.help.delete"));
            player.sendMessage(lang.getRawMessage("commands.help.reload"));
            player.sendMessage("§b/fw update §7- Install the latest update");
            player.sendMessage("§b/fw rollback §7- Revert to previous JAR");
        }
        player.sendMessage(lang.getMessage("commands.help.footer"));
    }

    private void applySafety(Player player) {
        long duration = plugin.getConfig().getLong("options.safety_buffer_ticks", 60L);
        player.setMetadata("wynogen_safety",
                new org.bukkit.metadata.FixedMetadataValue(plugin, true));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.removeMetadata("wynogen_safety", plugin);
            }
        }, duration);
    }

    // =========================================================================
    // Tab completion
    // =========================================================================

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>(Arrays.asList("join", "exit", "list"));
            if (sender.hasPermission("wynogen.admin")) {
                subs.addAll(Arrays.asList("create", "delete", "reload", "update", "rollback"));
            }
            return subs.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("join") || sub.equals("delete")) {
                return plugin.getWorldManager().getFeaturedWorldNames().stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            return Arrays.asList("EASY", "MEDIUM", "HARD").stream()
                    .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("create")) {
            return Arrays.asList("tight").stream()
                    .filter(s -> s.toLowerCase().startsWith(args[3].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
