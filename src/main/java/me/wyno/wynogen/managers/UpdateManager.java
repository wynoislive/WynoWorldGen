package me.wyno.wynogen.managers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.wyno.wynogen.WynoGen;
import org.bukkit.Bukkit;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;

public class UpdateManager {

    private final WynoGen plugin;
    private final String repo = "wynoislive/WynoWorldGen";
    private final String currentVersion;
    private String latestVersion;
    private String downloadUrl;

    public UpdateManager(WynoGen plugin) {
        this.plugin = plugin;
        this.currentVersion = plugin.getDescription().getVersion();
    }

    public CompletableFuture<Boolean> checkForUpdates() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) URI.create("https://api.github.com/repos/" + repo + "/releases/latest").toURL().openConnection();
                connection.setRequestProperty("User-Agent", "WynoGen-Updater");
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() != 200) {
                    return false;
                }

                try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    this.latestVersion = json.get("tag_name").getAsString().replace("v", "");
                    
                    // Find the JAR asset
                    json.getAsJsonArray("assets").forEach(element -> {
                        JsonObject asset = element.getAsJsonObject();
                        if (asset.get("name").getAsString().endsWith(".jar")) {
                            this.downloadUrl = asset.get("browser_download_url").getAsString();
                        }
                    });

                    return isVersionNewer(currentVersion, latestVersion);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to check for updates: " + e.getMessage());
                return false;
            }
        });
    }

    private boolean isVersionNewer(String current, String latest) {
        String[] currentParts = current.split("\\.");
        String[] latestParts = latest.split("\\.");
        
        int length = Math.max(currentParts.length, latestParts.length);
        for (int i = 0; i < length; i++) {
            int curr = i < currentParts.length ? Integer.parseInt(currentParts[i].replaceAll("[^0-9]", "")) : 0;
            int lat = i < latestParts.length ? Integer.parseInt(latestParts[i].replaceAll("[^0-9]", "")) : 0;
            if (lat > curr) return true;
            if (curr > lat) return false;
        }
        return false;
    }

    public CompletableFuture<Boolean> downloadAndInstall() {
        return CompletableFuture.supplyAsync(() -> {
            if (downloadUrl == null) return false;

            try {
                // 1. Backup current JAR
                File currentJar = new File(WynoGen.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                File backupDir = new File(plugin.getDataFolder(), "backups");
                if (!backupDir.exists()) backupDir.mkdirs();
                
                File backupFile = new File(backupDir, "WynoGen-" + currentVersion + ".jar.bak");
                Files.copy(currentJar.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // 2. Download new JAR to a temporary file
                URL url = URI.create(downloadUrl).toURL();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "WynoGen-Updater");

                File tempJar = new File(plugin.getDataFolder(), "update-temp.jar");
                try (InputStream in = connection.getInputStream()) {
                    Files.copy(in, tempJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                // 3. Move temp JAR to active JAR (Safe swap)
                try {
                    Files.move(tempJar.toPath(), currentJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    plugin.getLogger().warning("Could not overwrite active JAR (likely locked). Preparing update in /update folder.");
                    File updateFolder = new File(Bukkit.getWorldContainer(), "plugins/update");
                    if (!updateFolder.exists()) updateFolder.mkdirs();
                    Files.move(tempJar.toPath(), new File(updateFolder, currentJar.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                return true;
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to install update: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }

    public boolean rollback() {
        try {
            File backupDir = new File(plugin.getDataFolder(), "backups");
            File[] backups = backupDir.listFiles((dir, name) -> name.endsWith(".bak"));
            
            if (backups == null || backups.length == 0) return false;

            // Get most recent backup
            File latestBackup = backups[0];
            for (File b : backups) {
                if (b.lastModified() > latestBackup.lastModified()) {
                    latestBackup = b;
                }
            }

            File currentJar = new File(WynoGen.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Files.move(latestBackup.toPath(), currentJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Rollback failed: " + e.getMessage());
            return false;
        }
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }
}
