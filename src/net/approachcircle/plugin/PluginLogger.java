package net.approachcircle.plugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

public class PluginLogger {
    public static Logger getPluginLogger() {
        Plugin plugin = Mizu.getInstance();
        return plugin.getLogger();
    }
}
