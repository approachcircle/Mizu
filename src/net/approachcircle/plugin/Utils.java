package net.approachcircle.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.concurrent.atomic.AtomicInteger;

public class Utils {
    /**
     * legacy prefix using ChatColor
     */
    public static String prefix = ChatColor.ITALIC + "" + ChatColor.DARK_AQUA + "Mizu" + ChatColor.RESET + "> ";
    /**
     * new prefix using components
     */
    public static TextComponent cPrefix = Component.text("Mizu", NamedTextColor.LIGHT_PURPLE).append(Component.text("> ", NamedTextColor.WHITE));
    private static int repeatImmediatelyTask = -1;
    private static int repeatDeferredTask = -1;
    
    public static int immediateRepeatWithDelay(Runnable runnable, int delay, int frequency) {
        AtomicInteger currentFreq = new AtomicInteger();
        repeatImmediatelyTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(Mizu.getInstance(), () -> {
            if (currentFreq.get() >= frequency) {
                Bukkit.getScheduler().cancelTask(repeatImmediatelyTask);
                return;
            }
            runnable.run();
            currentFreq.getAndIncrement();
        }, 0, delay);
        return repeatImmediatelyTask;
    }

    public static int deferredRepeatWithDelay(Runnable runnable, int deference, int delay, int frequency) {
        AtomicInteger currentFreq = new AtomicInteger();
        repeatDeferredTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(Mizu.getInstance(), () -> {
            if (currentFreq.get() >= frequency) {
                Bukkit.getScheduler().cancelTask(repeatDeferredTask);
                return;
            }
            runnable.run();
            currentFreq.getAndIncrement();
        }, deference, delay);
        return repeatDeferredTask;
    }

    public static void commandAsConsole(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        PluginLogger.getPluginLogger().info("as console: " + command);
    }
}
