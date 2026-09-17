package net.approachcircle.plugin;

import io.sentry.Sentry;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.permissions.ServerOperator;
import org.jetbrains.annotations.Nullable;

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
        boolean noOperatorsOnline = Bukkit.getOnlinePlayers().stream().noneMatch(ServerOperator::isOp);
        if (e != null) {
            e.printStackTrace(System.err);
            if (noOperatorsOnline) {
                // if no operators are online to relay the exception to, then capture it with sentry so that we get
                // an email
                Sentry.captureException(e);
                // this could be a funny easter egg if shit ever breaks while i'm offline
                Bukkit.broadcast(Localisation.rawPrefixComponent(new ColouredString("can somebody ping oscar and tell him that his plugin just shit the bed so he can fix it (lazy ahh prolly sleepin)", NamedTextColor.RED)));
            }
        }
        if (message != null) {
            PluginLogger.getPluginLogger().severe(message);
        }
        if (!noOperatorsOnline) {
            Bukkit.getOnlinePlayers().stream().filter(ServerOperator::isOp).forEach(p -> {
                p.sendMessage(Localisation.rawPrefixComponent(new ColouredString("something very bad has happened. check the console for details", NamedTextColor.RED)));
            });
        }
    }

    public static void error(String message) {
        error(null, message);
    }

    public static void error(Exception e) {
        error(e, null);
    }
}
