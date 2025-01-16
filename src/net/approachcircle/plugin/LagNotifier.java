package net.approachcircle.plugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

public class LagNotifier {
    private static final Plugin plugin = Mizu.getInstance();
    private static final Logger logger = PluginLogger.getPluginLogger();
    private static Boolean reported = true;
    private static final Double TPSthreshold = 13D;
    private static final Double MSPTthreshold = 55D;
    private static final Long queryInterval = 20L;
    private static boolean suppressed = false;

    public static void startObserving() {
        assert plugin != null;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            double currentTPS = Bukkit.getTPS()[0];
            double currentMSPT = Bukkit.getAverageTickTime();
            boolean lagging = currentTPS <= TPSthreshold && currentMSPT >= MSPTthreshold;
            if (lagging) {
                if (!reported) {
                    String message = String.format("minecraft server '%s' is not performing well, TPS: %s | MSPT: %s", System.getenv("COMPUTERNAME"), Math.round(currentTPS), Math.round(currentMSPT));
                    logger.warning(String.format(message));
                    if (!suppressed) {
                        return; // unused
                    }
                    reported = true;
                }
            } else {
                reported = false;
            }
        }, 0L, queryInterval);
    }

    public static void suppressNotifications() {
        suppressed = true;
        logger.info("lag: notifications suppressed");
    }

    public static void returnNotifications() {
        suppressed = false;
        logger.info("lag: notifications returned");
    }
}
