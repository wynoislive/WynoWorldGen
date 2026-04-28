package me.wyno.wynogen.listeners;

import me.wyno.wynogen.WynoGen;
import me.wyno.wynogen.managers.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class PlayerListener implements Listener {

    private final WynoGen plugin;

    public PlayerListener(WynoGen plugin) {
        this.plugin = plugin;
    }

    // =========================================================================
    // Portal Handling — simplified for shared dimension profiles
    // =========================================================================

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPortal(PlayerPortalEvent event) {
        String fromWorldName = event.getFrom().getWorld().getName();
        WorldManager wm = plugin.getWorldManager();

        boolean fromParent    = wm.isFeaturedWorld(fromWorldName);
        boolean fromCompanion = wm.isCompanionWorld(fromWorldName);

        if (!fromParent && !fromCompanion) return;

        // Legacy / blanket disable_portals toggle
        if (plugin.getConfig().getBoolean("options.disable_portals", false)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getLanguageManager().getMessage("world.portals-disabled"));
            return;
        }

        String parentName = fromParent ? fromWorldName : wm.getParentWorldName(fromWorldName);
        World.Environment targetEnv = getTargetEnvironment(event);

        if (targetEnv == World.Environment.NETHER) {
            handleNetherPortal(event, parentName, fromParent);
        } else if (targetEnv == World.Environment.THE_END) {
            handleEndPortal(event, parentName, fromParent);
        }
    }

    private void handleNetherPortal(PlayerPortalEvent event, String parentName, boolean fromParent) {
        if (plugin.getConfig().getBoolean("options.disable_nether_portals", false)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getLanguageManager().getMessage("world.nether-portals-disabled"));
            return;
        }

        WorldManager wm = plugin.getWorldManager();
        if (fromParent) {
            World netherWorld = wm.getNetherWorld(parentName);
            if (netherWorld == null) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(plugin.getLanguageManager().getMessage("world.nether-portals-disabled"));
                return;
            }
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getLanguageManager().getMessage("world.teleporting-nether"));
            // DIRECT Teleport — dimensions share profile, no sync needed
            event.getPlayer().teleport(netherWorld.getSpawnLocation());
            applySafety(event.getPlayer());
        } else {
            World parentWorld = Bukkit.getWorld(parentName);
            if (parentWorld == null) { event.setCancelled(true); return; }
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getLanguageManager().getMessage("world.teleporting-overworld"));
            event.getPlayer().teleport(parentWorld.getSpawnLocation());
            applySafety(event.getPlayer());
        }
    }

    private void handleEndPortal(PlayerPortalEvent event, String parentName, boolean fromParent) {
        if (plugin.getConfig().getBoolean("options.disable_end_portals", false)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getLanguageManager().getMessage("world.end-portals-disabled"));
            return;
        }

        WorldManager wm = plugin.getWorldManager();
        if (fromParent) {
            World endWorld = wm.getEndWorld(parentName);
            if (endWorld == null) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(plugin.getLanguageManager().getMessage("world.end-portals-disabled"));
                return;
            }
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getLanguageManager().getMessage("world.teleporting-end"));
            event.getPlayer().teleport(endWorld.getSpawnLocation());
            applySafety(event.getPlayer());
        } else {
            World parentWorld = Bukkit.getWorld(parentName);
            if (parentWorld == null) { event.setCancelled(true); return; }
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getLanguageManager().getMessage("world.teleporting-overworld"));
            event.getPlayer().teleport(parentWorld.getSpawnLocation());
            applySafety(event.getPlayer());
        }
    }

    private World.Environment getTargetEnvironment(PlayerPortalEvent event) {
        String cause = event.getCause().name();
        if (cause.contains("NETHER")) return World.Environment.NETHER;
        if (cause.contains("END"))    return World.Environment.THE_END;
        World.Environment fromEnv = event.getFrom().getWorld().getEnvironment();
        if (fromEnv == World.Environment.NETHER) return World.Environment.NORMAL;
        return World.Environment.NETHER;
    }

    // =========================================================================
    // Respawn — refined for parent world routing
    // =========================================================================

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        String fromWorldName = event.getPlayer().getWorld().getName();
        WorldManager wm = plugin.getWorldManager();

        if (!wm.isManagedWorld(fromWorldName)) return;

        // Logic: Always respawn in the PARENT Overworld
        String parentName = wm.isFeaturedWorld(fromWorldName) ? fromWorldName : wm.getParentWorldName(fromWorldName);
        World parentWorld = Bukkit.getWorld(parentName);

        if (parentWorld != null) {
            Location bed = event.getPlayer().getBedSpawnLocation();
            // Use bed only if it is in the SAME parent overworld
            if (bed != null && bed.getWorld().getName().equals(parentName)) {
                event.setRespawnLocation(bed);
            } else {
                event.setRespawnLocation(parentWorld.getSpawnLocation());
            }
        }
    }

    // =========================================================================
    // Join / Quit / Damage (Standard Logic)
    // =========================================================================

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        String worldName = event.getPlayer().getWorld().getName();
        if (plugin.getWorldManager().isManagedWorld(worldName)) {
            event.getPlayer().getInventory().clear();
            event.getPlayer().getEnderChest().clear();
            plugin.getDataManager().loadPlayerData(event.getPlayer(), worldName);
            applySafety(event.getPlayer());
        }

        if (event.getPlayer().hasPermission("wynogen.admin") && plugin.getConfig().getBoolean("options.updater.notify_admins", true)) {
            plugin.getUpdateManager().checkForUpdates().thenAccept(available -> {
                if (available) {
                    event.getPlayer().sendMessage(plugin.getLanguageManager().getMessage("update.available")
                            .replace("{version}", plugin.getUpdateManager().getLatestVersion()));
                }
            });
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        String worldName = event.getPlayer().getWorld().getName();
        String id = plugin.getWorldManager().isManagedWorld(worldName) ? worldName : "default";
        plugin.getDataManager().savePlayerData(event.getPlayer(), id);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity().hasMetadata("wynogen_safety")) {
            event.setCancelled(true);
        }
    }

    private void applySafety(org.bukkit.entity.Player player) {
        long duration = plugin.getConfig().getLong("options.safety_buffer_ticks", 60L);
        player.setMetadata("wynogen_safety", new FixedMetadataValue(plugin, true));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.removeMetadata("wynogen_safety", plugin);
            }
        }, duration);
    }
}
