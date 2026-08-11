package net.approachcircle.plugin;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class Utils {
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

    public static void error(@Nullable Exception e, @Nullable String message) {
        if (e != null) {
            e.printStackTrace(System.err);
        }
        if (message != null) {
            PluginLogger.getPluginLogger().severe(message);
        }
        StackTraceElement stack;
        stack = Thread.currentThread().getStackTrace()[2];
        String source = stack.getClassName() + "." + stack.getMethodName();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) {
                player.sendMessage(Localisation.unlocalisedPrefixComponent(new ColouredString(String.format("something very bad has happened in '%s'. check the console for details", source), NamedTextColor.RED)));
            }
        }
    }

    public static void error(String message) {
        error(null, message);
    }

    public static void error(Exception e) {
        error(e, null);
    }
}
