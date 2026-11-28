package net.approachcircle.plugin;

import com.timgroup.statsd.NonBlockingStatsDClientBuilder;
import com.timgroup.statsd.StatsDClient;
import org.bukkit.Bukkit;

import java.io.Closeable;

public class DatadogReporter implements Closeable {
    private StatsDClient client;
    private int tpsTask;
    private int msptTask;
    private boolean suppressed = false;
    private static DatadogReporter instance;

    private DatadogReporter() {
        client = new NonBlockingStatsDClientBuilder().prefix("MC").hostname("127.0.0.1").port(8125).build();
        tpsTask = -1;
        msptTask = -1;
    }

    public static DatadogReporter getInstance() {
        if (instance == null) {
            instance = new DatadogReporter();
        }
        return instance;
    }

    public void startReportingTPS() {
        tpsTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(Mizu.getInstance(), () -> {
            double currentTPS = Bukkit.getTPS()[0];
            if (!suppressed) {
                client.gauge("TPS.gauge", currentTPS);
            }
        }, 0, 20);
    }

    public void startReportingMSPT() {
        msptTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(Mizu.getInstance(), () -> {
            double currentMSPT = Bukkit.getAverageTickTime();
            if (!suppressed) {
                client.gauge("MSPT.gauge", currentMSPT);
            }
        }, 0, 20);
    }

    public void suppressReporting() {
        suppressed = true;
    }

    public void returnReporting() {
        suppressed = false;
    }

    @Override
    public void close() {
        if (tpsTask != -1 && msptTask != -1) {
            Bukkit.getScheduler().cancelTask(tpsTask);
            Bukkit.getScheduler().cancelTask(msptTask);
        }
        client.close();
    }
}
