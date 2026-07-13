package net.approachcircle.plugin;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class BackupManager {
    private final static long backupFrequency = 5 * 20 * 60; // this might have to be more infrequent
    private static boolean backingUp = false;

    public static void start() {
        if (backingUp) { return; }
        backingUp = true;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Mizu.getInstance(), BackupManager::backupAll, 0, backupFrequency);
    }

    public static void backupAll() {
        for (World world : Bukkit.getWorlds()) {
            backup(world.getName());
        }
    }

    public static void backup(String world) {

    }
}
