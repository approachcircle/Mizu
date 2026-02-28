package net.approachcircle.plugin;

import io.sentry.Sentry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

//TODO: add hit system (bounties can be placed on people for a reward)
//TODO: also lifelink as a random event (would be better with no keep inven, temporarily disable during?)
//TODO: random coordinate that must be reached
//TODO: add jail system (every tick will teleport someone inside a bedrock jail somewhere)
//TODO: add echest command for personal echest
public class Mizu extends JavaPlugin {
    private Logger logger = getLogger();
    private PluginManager pluginManager = getServer().getPluginManager();
    private String sentryDsn = "https://80903f313fc7478284aadb5982eb248e@o4504855501602816.ingest.sentry.io/4504855620747264";
    private static Mizu instance = null;
    private DatadogReporter reporter = DatadogReporter.getInstance();

    public static Mizu getInstance() {
        if (instance == null) {
            throw new RuntimeException("access before initialization is disallowed");
        }
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        Sentry.init(options -> {
            options.setDsn(sentryDsn);
            options.setSampleRate(1.0);
        });
        logger.info("plugin enabled");
        Sentry.addBreadcrumb("plugin enabled");
        pluginManager.registerEvents(new MainListener(), this);
        logger.info("all events have been registered");
        Sentry.addBreadcrumb("events registered");
        // LagNotifier.startObserving();
        reporter.startReportingMSPT();
        reporter.startReportingTPS();
        Sentry.addBreadcrumb("started monitoring lag");
        logger.info("started monitoring lag");
        saveDefaultConfig();
        // will throw runtime if something goes wrong
        Localisation.loadStrings();
        Localisation.loadExplicitLocaleSettings();
        if (getConfig().getBoolean("lucky-chunk")) {
            LuckyChunk.start();
        }
        if (getConfig().getBoolean("notify-chunk")) {
            logger.info("lucky chunk is notifying on chunk found");
        } else {
            logger.warning("lucky chunk is NOT notifying on chunk found");
        }
        TabCompletionManager.getInstance().initTabCompletions();
    }

    @Override
    public void onDisable() {
        reporter.close();
        Sentry.addBreadcrumb("datadog reporter closed");
        logger.info("datadog reporter closed");
        if (LuckyChunk.isChunkSpawned() && getConfig().getBoolean("lucky-chunk")) {
            logger.info("undoing last lucky chunk...");
            reporter.suppressReporting();
            // LagNotifier.suppressNotifications();
            Utils.commandAsConsole("/undo");
            reporter.returnReporting();
            //LagNotifier.returnNotifications();
            logger.info("done!");
            return;
        }
        HandlerList.unregisterAll();
        Sentry.addBreadcrumb("events unregistered");
        logger.info("events unregistered");
        logger.info("goodbye!");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        try {
            return CommandDispatcher.dispatch(label, sender, args);
        } catch (Exception e) {
            Sentry.captureException(e);
            return true;
        }
    }
}
