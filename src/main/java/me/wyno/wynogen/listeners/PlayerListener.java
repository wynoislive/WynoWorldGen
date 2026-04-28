package me.wyno.wynogen.listeners;

import me.wyno.wynogen.WynoGen;
import me.wyno.wynogen.managers.WorldManager;
import org.bukkit.Bukkit;
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
    // Portal Handling — smart routing to companion worlds
    // =========================================================================

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPortal(PlayerPortalEvent event) {
        String fromWorldName = event.getFrom().getWorld().getName();
        WorldManager wm = plugin.getWorldManager();

        // Only act on managed worlds (parent or companion)
        boolean fromParent    = wm.isFeaturedWorld(fromWorldName);
        boolean fromCompanion = wm.isCompanionWorld(fromWorldName);

        if (!fromParent && !fromCompanion) return; // Vanilla world — do nothing

        // ------------------------------------------------------------------
        // Legacy / blanket disable_portals toggle (backwards-compatible)
        // ------------------------------------------------------------------
        if (plugin.getConfig().getBoolean("options.disable_portals", false)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(
                    plugin.getLanguageManager().getMessage("world.portals-disabled"));
            return;
        }

        // Resolve the parent world name regardless of whether the player is
        // currently in the parent or a companion.
        String parentName = fromParent ? fromWorldName : wm.getParentWorldName(fromWorldName);

        // Determine portal type
        World.Environment targetEnv = getTargetEnvironment(event);

        if (targetEnv == World.Environment.NETHER) {
            handleNetherPortal(event, parentName, fromWorldName, fromParent);
        } else if (targetEnv == World.Environment.THE_END) {
            handleEndPortal(event, parentName, fromWorldName, fromParent);
        }
        // If neither (shouldn't happen), let vanilla handle it
    }

    /**
     * Handles a nether portal attempt inside a managed world.
     * Routes to <parent>-nether or back to the parent overworld.
     */
    private void handleNetherPortal(PlayerPortalEvent event,
                                    String parentName,
                                    String fromWorldName,
                                    boolean fromParent) {
        // Granular disable check
        if (plugin.getConfig().getBoolean("options.disable_nether_portals", false)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(
                    plugin.getLanguageManager().getMessage("world.nether-portals-disabled"));
            return;
        }

        WorldManager wm = plugin.getWorldManager();

        if (fromParent) {
            // Parent → Nether companion
            World netherWorld = wm.getNetherWorld(parentName);
            if (netherWorld == null) {
                // Companion not generated (feature disabled at creation time) → block
                event.setCancelled(true);
                event.getPlayer().sendMessage(
                        plugin.getLanguageManager().getMessage("world.nether-portals-disabled"));
                return;
            }
            event.setCancelled(true);
            event.getPlayer().sendMessage(
                    plugin.getLanguageManager().getMessage("world.teleporting-nether"));
            doDataSwap(event.getPlayer(), fromWorldName, netherWorld.getName(),
                    netherWorld.getSpawnLocation());

        } else {
            // Nether companion → Parent overworld
            World parentWorld = Bukkit.getWorld(parentName);
            if (parentWorld == null) {
                event.setCancelled(true);
                return;
            }
            event.setCancelled(true);
            event.getPlayer().sendMessage(
                    plugin.getLanguageManager().getMessage("world.teleporting-overworld"));
            doDataSwap(event.getPlayer(), fromWorldName, parentName,
                    parentWorld.getSpawnLocation());
        }
    }

    /**
     * Handles an end portal attempt inside a managed world.
     * Routes to <parent>-end or back to the parent overworld.
     */
    private void handleEndPortal(PlayerPortalEvent event,
                                 String parentName,
                                 String fromWorldName,
                                 boolean fromParent) {
        // Granular disable check
        if (plugin.getConfig().getBoolean("options.disable_end_portals", false)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(
                    plugin.getLanguageManager().getMessage("world.end-portals-disabled"));
            return;
        }

        WorldManager wm = plugin.getWorldManager();

        if (fromParent) {
            // Parent → End companion
            World endWorld = wm.getEndWorld(parentName);
            if (endWorld == null) {
                // Companion not generated → block
                event.setCancelled(true);
                event.getPlayer().sendMessage(
                        plugin.getLanguageManager().getMessage("world.end-portals-disabled"));
                return;
            }
            event.setCancelled(true);
            event.getPlayer().sendMessage(
                    plugin.getLanguageManager().getMessage("world.teleporting-end"));
            doDataSwap(event.getPlayer(), fromWorldName, endWorld.getName(),
                    endWorld.getSpawnLocation());

        } else {
            // End companion → Parent overworld
            World parentWorld = Bukkit.getWorld(parentName);
            if (parentWorld == null) {
                event.setCancelled(true);
                return;
            }
            event.setCancelled(true);
            event.getPlayer().sendMessage(
                    plugin.getLanguageManager().getMessage("world.teleporting-overworld"));
            doDataSwap(event.getPlayer(), fromWorldName, parentName,
                    parentWorld.getSpawnLocation());
        }
    }

    /**
     * Saves player data for {@code fromWorldId}, then loads data for
     * {@code toWorldId} and teleports the player to {@code destination}.
     * All heavy I/O is async; the teleport runs on the main thread.
     */
    private void doDataSwap(org.bukkit.entity.Player player,
                             String fromWorldId,
                             String toWorldId,
                             org.bukkit.Location destination) {
        plugin.getDataManager().savePlayerData(player, fromWorldId).thenRun(() ->
                plugin.getDataManager().loadPlayerData(player, toWorldId).thenAccept(found ->
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            player.teleport(destination);
                            applySafety(player);
                        })
                )
        );
    }

    /**
     * Derives the intended target environment from the portal event.
     * Works for both standard nether portals and end portals / gateways.
     */
    private World.Environment getTargetEnvironment(PlayerPortalEvent event) {
        // PlayerPortalEvent.getCause() returns TravelAgent cause in older API;
        // the safest cross-version approach is to check the from-world environment
        // and the portal cause string.
        String cause = event.getCause().name(); // e.g. "NETHER_PORTAL", "END_PORTAL", "END_GATEWAY"
        if (cause.contains("NETHER")) return World.Environment.NETHER;
        if (cause.contains("END"))    return World.Environment.THE_END;

        // Fallback: look at the current world environment and infer direction
        World.Environment fromEnv = event.getFrom().getWorld().getEnvironment();
        if (fromEnv == World.Environment.NETHER) return World.Environment.NORMAL;
        return World.Environment.NETHER; // Default assumption for NORMAL → NETHER
    }

    // =========================================================================
    // Join — load featured / companion world data
    // =========================================================================

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        String worldName = event.getPlayer().getWorld().getName();
        WorldManager wm  = plugin.getWorldManager();

        if (wm.isManagedWorld(worldName)) {
            // Instant inventory mask to prevent "inventory flicker"
            event.getPlayer().getInventory().clear();
            event.getPlayer().getEnderChest().clear();

            // Load the correct data profile for this world (uses full world name as key)
            plugin.getDataManager().loadPlayerData(event.getPlayer(), worldName);
            applySafety(event.getPlayer());
        }

        // --- Update notification for admins ---
        if (event.getPlayer().hasPermission("wynogen.admin")
                && plugin.getConfig().getBoolean("options.updater.notify_admins", true)) {
            plugin.getUpdateManager().checkForUpdates().thenAccept(available -> {
                if (available) {
                    event.getPlayer().sendMessage(
                            plugin.getLanguageManager().getMessage("update.available")
                                    .replace("{version}", plugin.getUpdateManager().getLatestVersion()));
                }
            });
        }
    }

    // =========================================================================
    // Respawn — keep player in the same managed world if configured
    // =========================================================================

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        String fromWorld   = event.getPlayer().getWorld().getName();
        WorldManager wm    = plugin.getWorldManager();
        boolean fromManaged = wm.isManagedWorld(fromWorld);

        // --- Stay-in-world logic ---
        if (fromManaged && plugin.getConfig().getBoolean("options.respawn_in_same_world", true)) {
            org.bukkit.World currentWorld = event.getPlayer().getWorld();
            org.bukkit.Location bed = event.getPlayer().getBedSpawnLocation();

            // Use bed spawn only if it is in the same managed world
            if (bed != null && bed.getWorld().getName().equals(fromWorld)) {
                event.setRespawnLocation(bed);
            } else {
                event.setRespawnLocation(currentWorld.getSpawnLocation());
            }
        }

        // --- Data sync when crossing managed ↔ unmanaged boundary ---
        String toWorld     = event.getRespawnLocation().getWorld().getName();
        boolean toManaged  = wm.isManagedWorld(toWorld);

        if (fromManaged != toManaged || !fromWorld.equals(toWorld)) {
            String targetId = toManaged ? toWorld : "default";
            plugin.getDataManager().loadPlayerData(event.getPlayer(), targetId);
        }
    }

    // =========================================================================
    // Quit — save player data
    // =========================================================================

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        String worldName = event.getPlayer().getWorld().getName();
        WorldManager wm  = plugin.getWorldManager();

        // Use the world name as the storage key for any managed world (parent or companion)
        String id = wm.isManagedWorld(worldName) ? worldName : "default";
        plugin.getDataManager().savePlayerData(event.getPlayer(), id);
    }

    // =========================================================================
    // Damage — teleport safety buffer
    // =========================================================================

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity().hasMetadata("wynogen_safety")) {
            event.setCancelled(true);
        }
    }

    // =========================================================================
    // Utility
    // =========================================================================

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
