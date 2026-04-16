package me.wyno.wynogen.listeners;

import me.wyno.wynogen.WynoGen;
import org.bukkit.Bukkit;
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPortal(PlayerPortalEvent event) {
        if (!plugin.getConfig().getBoolean("options.disable_portals", true)) return;
        
        String worldName = event.getFrom().getWorld().getName();
        if (plugin.getWorldManager().isFeaturedWorld(worldName)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getLanguageManager().getMessage("world.portals-disabled"));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        String worldName = event.getPlayer().getWorld().getName();
        if (plugin.getWorldManager().isFeaturedWorld(worldName)) {
            // INSTANT mask to prevent "inventory flicker" while async DB loads
            event.getPlayer().getInventory().clear();
            event.getPlayer().getEnderChest().clear();
            
            // Load their featured world data on login
            plugin.getDataManager().loadPlayerData(event.getPlayer(), worldName);
            applySafety(event.getPlayer());
        }

        // --- Update Notification ---
        if (event.getPlayer().hasPermission("wynogen.admin") && plugin.getConfig().getBoolean("options.updater.notify_admins", true)) {
            plugin.getUpdateManager().checkForUpdates().thenAccept(available -> {
                if (available) {
                    event.getPlayer().sendMessage(plugin.getLanguageManager().getMessage("update.available")
                            .replace("{version}", plugin.getUpdateManager().getLatestVersion()));
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        String fromWorld = event.getPlayer().getWorld().getName();
        boolean fromFeatured = plugin.getWorldManager().isFeaturedWorld(fromWorld);
        
        // --- Logic: Stay in Featured World on Death ---
        if (fromFeatured && plugin.getConfig().getBoolean("options.respawn_in_same_world", true)) {
            org.bukkit.World currentWorld = event.getPlayer().getWorld();
            org.bukkit.Location bed = event.getPlayer().getBedSpawnLocation();
            
            // If they have a bed IN THE SAME world, use it. Otherwise, force world spawn.
            if (bed != null && bed.getWorld().getName().equals(fromWorld)) {
                event.setRespawnLocation(bed);
            } else {
                event.setRespawnLocation(currentWorld.getSpawnLocation());
            }
        }

        // --- Data Sync Management ---
        String toWorld = event.getRespawnLocation().getWorld().getName();
        boolean toFeatured = plugin.getWorldManager().isFeaturedWorld(toWorld);

        if (fromFeatured != toFeatured) {
            String targetId = toFeatured ? toWorld : "default";
            plugin.getDataManager().loadPlayerData(event.getPlayer(), targetId);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        String worldName = event.getPlayer().getWorld().getName();
        String id = plugin.getWorldManager().isFeaturedWorld(worldName) ? worldName : "default";
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
