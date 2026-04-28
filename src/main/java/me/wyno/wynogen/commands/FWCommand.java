package me.wyno.wynogen.commands;

import me.wyno.wynogen.WynoGen;
import me.wyno.wynogen.managers.DataManager;
import me.wyno.wynogen.managers.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FWCommand implements CommandExecutor, TabCompleter {

    private final WynoGen plugin;

    public FWCommand(WynoGen plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "join" -> handleJoin(player, args);
            case "exit" -> handleExit(player);
            case "list" -> handleList(player);
            case "create" -> handleCreate(player, args);
            case "delete" -> handleDelete(player, args);
            case "reload" -> handleReload(player);
            case "update" -> handleUpdate(player);
            case "rollback" -> handleRollback(player);
            default -> sendHelp(player);
        }

        return true;
    }

    private void handleJoin(Player player, String[] args) {
        if (!player.hasPermission("wynogen.use")) {
            player.sendMessage(plugin.getLanguageManager().getMessage("general.no-permission"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.join.usage"));
            return;
        }

        String worldName = args[1];
        World targetWorld = Bukkit.getWorld(worldName);

        if (targetWorld == null || !plugin.getWorldManager().isFeaturedWorld(worldName)) {
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.join.not-found"));
            return;
        }

        // Save current data (default world data) before moving
        plugin.getDataManager().savePlayerData(player, player.getWorld().getName()).thenRun(() -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                // Load target world data
                plugin.getDataManager().loadPlayerData(player, worldName).thenAccept(snapshot -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Location lastLoc = (snapshot != null) ? snapshot.getBukkitLocation() : null;
                        
                        // If we have a last location in the target world (or its companions), use it!
                        if (lastLoc != null) {
                            player.teleport(lastLoc);
                        } else {
                            // Fallback to default spawn
                            player.teleport(targetWorld.getSpawnLocation());
                        }
                        
                        player.sendMessage(plugin.getLanguageManager().getMessage("commands.join.success")
                                .replace("{world}", worldName));
                    });
                });
            });
        });
    }

    private void handleExit(Player player) {
        if (!player.hasPermission("wynogen.use")) {
            player.sendMessage(plugin.getLanguageManager().getMessage("general.no-permission"));
            return;
        }

        String fromWorld = player.getWorld().getName();
        if (!plugin.getWorldManager().isManagedWorld(fromWorld)) {
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.exit.not-in-featured"));
            return;
        }

        World mainWorld = Bukkit.getWorlds().get(0);
        
        // Save featured world data (including current location)
        plugin.getDataManager().savePlayerData(player, fromWorld).thenRun(() -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                // Load default data
                plugin.getDataManager().loadPlayerData(player, "default").thenRun(() -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.teleport(mainWorld.getSpawnLocation());
                        player.sendMessage(plugin.getLanguageManager().getMessage("commands.exit.success"));
                    });
                });
            });
        });
    }

    private void handleList(Player player) {
        if (!player.hasPermission("wynogen.use")) {
            player.sendMessage(plugin.getLanguageManager().getMessage("general.no-permission"));
            return;
        }

        List<String> worlds = plugin.getWorldManager().getFeaturedWorldNames();
        if (worlds.isEmpty()) {
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.list.empty"));
            return;
        }

        player.sendMessage(plugin.getLanguageManager().getMessage("commands.list.header"));
        for (String w : worlds) {
            StringBuilder sb = new StringBuilder("§8 - §b" + w);
            if (plugin.getWorldManager().getNetherWorld(w) != null) sb.append(" §4[N]");
            if (plugin.getWorldManager().getEndWorld(w) != null) sb.append(" §d[E]");
            player.sendMessage(sb.toString());
        }
    }

    private void handleCreate(Player player, String[] args) {
        if (!player.hasPermission("wynogen.admin")) {
            player.sendMessage(plugin.getLanguageManager().getMessage("general.no-permission"));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.create.usage"));
            return;
        }

        String name = args[1];
        String difficulty = args[2].toUpperCase();
        boolean tight = args.length >= 4 && args[3].equalsIgnoreCase("tight");

        if (!Arrays.asList("EASY", "MEDIUM", "HARD").contains(difficulty)) {
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.create.invalid-difficulty"));
            return;
        }

        player.sendMessage(plugin.getLanguageManager().getMessage("commands.create.starting").replace("{world}", name));
        
        boolean success = plugin.getWorldManager().createWorld(name, difficulty, tight, true);
        if (success) {
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.create.success").replace("{world}", name));
            if (plugin.getConfig().getBoolean("options.generate_nether", true)) {
                player.sendMessage(plugin.getLanguageManager().getMessage("world.companion-created").replace("{type}", "Nether"));
            }
            if (plugin.getConfig().getBoolean("options.generate_end", true)) {
                player.sendMessage(plugin.getLanguageManager().getMessage("world.companion-created").replace("{type}", "End"));
            }
        } else {
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.create.error"));
        }
    }

    private void handleDelete(Player player, String[] args) {
        if (!player.hasPermission("wynogen.admin")) {
            player.sendMessage(plugin.getLanguageManager().getMessage("general.no-permission"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.delete.usage"));
            return;
        }

        String name = args[1];
        if (!plugin.getWorldManager().isFeaturedWorld(name)) {
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.delete.not-found"));
            return;
        }

        player.sendMessage(plugin.getLanguageManager().getMessage("commands.delete.starting").replace("{world}", name));
        
        if (plugin.getWorldManager().deleteWorld(name)) {
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.delete.success").replace("{world}", name));
            player.sendMessage(plugin.getLanguageManager().getMessage("world.companion-deleted"));
        } else {
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.delete.error"));
        }
    }

    private void handleReload(Player player) {
        if (!player.hasPermission("wynogen.admin")) {
            player.sendMessage(plugin.getLanguageManager().getMessage("general.no-permission"));
            return;
        }
        plugin.reloadConfig();
        plugin.getLanguageManager().reload();
        player.sendMessage(plugin.getLanguageManager().getMessage("commands.reload.success"));
    }

    private void handleUpdate(Player player) {
        if (!player.hasPermission("wynogen.admin")) return;
        player.sendMessage("§bChecking for updates...");
        plugin.getUpdateManager().checkForUpdates().thenAccept(available -> {
            if (available) {
                player.sendMessage("§eNew version found! Installing...");
                plugin.getUpdateManager().downloadAndInstall().thenAccept(success -> {
                    if (success) {
                        player.sendMessage("§aUpdate installed! Restart the server to apply.");
                    } else {
                        player.sendMessage("§cUpdate failed. Check console.");
                    }
                });
            } else {
                player.sendMessage("§aYou are on the latest version.");
            }
        });
    }

    private void handleRollback(Player player) {
        if (!player.hasPermission("wynogen.admin")) return;
        if (plugin.getUpdateManager().rollback()) {
            player.sendMessage("§aRollback successful! Restart to revert to previous version.");
        } else {
            player.sendMessage("§cNo backup found or rollback failed.");
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage("§8§m----------------------------------");
        player.sendMessage("§b§lWYNO WORLDGEN §7- §fCommands");
        player.sendMessage("§b/fw join <world> §7- Join a featured world");
        player.sendMessage("§b/fw exit §7- Leave featured world");
        player.sendMessage("§b/fw list §7- Show all worlds");
        if (player.hasPermission("wynogen.admin")) {
            player.sendMessage("§b/fw create <n> <diff> [tight] §7- Create world");
            player.sendMessage("§b/fw delete <name> §7- Delete world");
            player.sendMessage("§b/fw reload §7- Reload config");
            player.sendMessage("§b/fw update §7- Update plugin");
        }
        player.sendMessage("§8§m----------------------------------");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>(Arrays.asList("join", "exit", "list"));
            if (sender.hasPermission("wynogen.admin")) {
                list.addAll(Arrays.asList("create", "delete", "reload", "update", "rollback"));
            }
            return list.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("delete")) {
                return plugin.getWorldManager().getFeaturedWorldNames().stream().filter(s -> s.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
            }
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            return Arrays.asList("EASY", "MEDIUM", "HARD").stream().filter(s -> s.startsWith(args[2].toUpperCase())).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
