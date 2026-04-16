package me.wyno.wynogen.managers;

import me.wyno.wynogen.WynoGen;
import me.wyno.wynogen.utils.MessageUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Objects;

public class LanguageManager {

    private final WynoGen plugin;
    private File file;
    private FileConfiguration config;

    public LanguageManager(WynoGen plugin) {
        this.plugin = plugin;
        setup();
    }

    public void setup() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        this.file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public String getMessage(String path) {
        String msg = config.getString(path);
        if (msg == null) return "Missing message: " + path;
        
        // Auto-prepend prefix if it's not a general message or header
        if (!path.startsWith("prefix") && !path.contains("header") && !path.contains("footer") && !path.contains("help.")) {
            return getPrefix() + MessageUtils.color(msg);
        }
        
        return MessageUtils.color(msg);
    }

    public String getRawMessage(String path) {
        return MessageUtils.color(config.getString(path, ""));
    }

    public String getPrefix() {
        return MessageUtils.color(Objects.requireNonNull(config.getString("prefix", "&8[&bWYNO&fGEN&8] &7")));
    }

    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(file);
    }
}
