package net.approachcircle.plugin;

import org.bukkit.Bukkit;

import java.util.Random;

public class RandomEventOrchestrator {
    private static final Mizu mizu = Mizu.getInstance();
    private static int eventTask = -1; // until next event
    private static int progressTask = -1; // every second, how long left of event
    private static int endEventTask = -1; // until event ends
    private static final int waitPeriod = 36000; //30m
    private static int progress = waitPeriod;

    public static void start() {
        scheduleNewEvent();
    }

    private static int chooseEvent() {
        return new Random().nextInt(1, 4);
    }

    private static void scheduleNewEvent() {
        eventTask = Bukkit.getScheduler().scheduleSyncDelayedTask(mizu, () -> {
            int eventIndex = chooseEvent();
            if (eventIndex == 1) {
                LuckyChunk.spawnChunk();
            }
        }, waitPeriod);
    }
}
