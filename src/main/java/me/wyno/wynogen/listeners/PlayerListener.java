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
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
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

        // Blanket disable_portals toggle
        if (plugin.getConfig().getBoolean("options.disable_portals", false)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getLanguageManager().getMessage("world.portals-disabled"));
            return;
        }

        String parentName = fromParent ? fromWorldName : wm.getParentWorldName(fromWorldName);
        
        // Handle based on the portal type
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            handleNetherPortal(event, parentName);
        } else if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            handleEndPortal(event, parentName);
        }
    }

    private void handleNetherPortal(PlayerPortalEvent event, String parentName) {
        if (plugin.getConfig().getBoolean("options.disable_nether_portals", false)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getLanguageManager().getMessage("world.nether-portals-disabled"));
            return;
        }

        WorldManager wm = plugin.getWorldManager();
        World targetWorld;
        double scale;
        String messageKey;

        // Logic: If we are currently in the Nether, we go back to the parent Overworld.
        // Otherwise, we go to the companion Nether.
        if (event.getFrom().getWorld().getEnvironment() == World.Environment.NETHER) {
            targetWorld = Bukkit.getWorld(parentName);
            scale = 8.0;
            messageKey = "world.teleporting-overworld";
        } else {
            targetWorld = wm.getNetherWorld(parentName);
            scale = 0.125;
            messageKey = "world.teleporting-nether";
        }

        if (targetWorld == null) {
            event.setCancelled(true);
            return;
        }

        Location from = event.getFrom();
        Location to = new Location(targetWorld, from.getX() * scale, from.getY(), from.getZ() * scale, from.getYaw(), from.getPitch());
        
        // Use event methods to allow Bukkit to handle portal search and creation
        event.setTo(to);
        event.setCanCreatePortal(true);
        event.setSearchRadius(128);
        event.setCreationRadius(16);

        event.getPlayer().sendMessage(plugin.getLanguageManager().getMessage(messageKey));
        applySafety(event.getPlayer());
    }

    private void handleEndPortal(PlayerPortalEvent event, String parentName) {
        if (plugin.getConfig().getBoolean("options.disable_end_portals", false)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getLanguageManager().getMessage("world.end-portals-disabled"));
            return;
        }

        WorldManager wm = plugin.getWorldManager();
        World targetWorld;
        Location targetLoc;
        String messageKey;

        // Logic: If we are in the End, we go back to parent Overworld.
        // We use the player's bed if it's in the parent world, otherwise the spawn.
        if (event.getFrom().getWorld().getEnvironment() == World.Environment.THE_END) {
            targetWorld = Bukkit.getWorld(parentName);
            if (targetWorld == null) { event.setCancelled(true); return; }
            
            Location bed = event.getPlayer().getBedSpawnLocation();
            if (bed != null && bed.getWorld().getName().equals(parentName)) {
                targetLoc = bed;
            } else {
                targetLoc = targetWorld.getSpawnLocation();
            }
            messageKey = "world.teleporting-overworld";
        } else {
            // FROM Overworld -> TO End
            targetWorld = wm.getEndWorld(parentName);
            if (targetWorld == null) { event.setCancelled(true); return; }
            
            // Use the target world's configured spawn location (requested by user)
            // This replaces the fixed obsidian platform coordinates.
            targetLoc = targetWorld.getSpawnLocation();
            messageKey = "world.teleporting-end";
        }

        event.setTo(targetLoc);
        event.getPlayer().sendMessage(plugin.getLanguageManager().getMessage(messageKey));
        applySafety(event.getPlayer());
    }

    // =========================================================================
    // Cross-World Sync — catch teleports from other plugins
    // =========================================================================

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        String from = event.getFrom().getName();
        String to = player.getWorld().getName();

        // Resolve data buckets (parent worlds or "default")
        String fromId = plugin.getDataManager().resolveDataId(from);
        String toId   = plugin.getDataManager().resolveDataId(to);

        // If the data profile bucket changed, we must swap data.
        if (!fromId.equals(toId)) {
            plugin.getDataManager().savePlayerData(player, from).thenRun(() -> {
                plugin.getDataManager().loadPlayerData(player, to);
            });
            applySafety(player);
        }
    }

    // =========================================================================
    // Respawn — refined for parent world routing
    // =========================================================================

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        String fromWorldName = event.getPlayer().getWorld().getName();
        WorldManager wm = plugin.getWorldManager();

        if (!wm.isManagedWorld(fromWorldName)) return;

        // Respect config toggle for staying in the mode worlds
        if (!plugin.getConfig().getBoolean("options.respawn_in_same_world", true)) {
            return;
        }

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
            // DataManager.loadPlayerData handles clearing and loading
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
