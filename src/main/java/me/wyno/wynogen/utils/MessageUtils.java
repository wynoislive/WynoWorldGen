package me.wyno.wynogen.utils;

import net.md_5.bungee.api.ChatColor;

public class MessageUtils {

    /**
     * Translates & color codes into Minecraft's internal color format.
     * @param message The message to colorize.
     * @return The colorized message.
     */
    public static String color(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
