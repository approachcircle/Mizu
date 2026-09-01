package net.approachcircle.plugin;

import io.sentry.Sentry;
import net.approachcircle.plugin.command.*;
import net.approachcircle.plugin.effect.MizuEffectRegistry;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class Mizu extends JavaPlugin {
    private final Logger logger = getLogger();
    private final PluginManager pluginManager = getServer().getPluginManager();
    // TODO: NOT THAT SENSITIVE BUT HIDE STILL
    private final String sentryDsn = "https://80903f313fc7478284aadb5982eb248e@o4504855501602816.ingest.sentry.io/4504855620747264";
    private static Mizu instance = null;
    private final DatadogReporter reporter = DatadogReporter.getInstance();

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
        pluginManager.registerEvents(new MizuListener(), this);
        logger.info("events registered!");
        registerCommands();
        logger.info("commands registered!");
        MizuEffectRegistry.registerEffects();
        logger.info("effects registered!");
        reporter.startReportingMSPT();
        reporter.startReportingTPS();
        logger.info("started monitoring lag");
        saveDefaultConfig();
        // will throw runtime if something goes wrong
        Localisation.loadStrings();
        Localisation.loadExplicitLocaleSettings();
        logger.info("localisation initialised!");
        if (getConfig().getBoolean("lucky-chunk")) {
            logger.info("lucky chunk is enabled");
            LuckyChunk.start();
        } else {
            logger.warning("lucky chunk is NOT enabled");
        }
        if (getConfig().getBoolean("notify-chunk")) {
            logger.info("lucky chunk is notifying on chunk found");
        } else {
            logger.warning("lucky chunk is NOT notifying on chunk found");
        }
    }

    @Override
    public void onDisable() {
        saveConfig();
        reporter.close();
        Sentry.addBreadcrumb("datadog reporter closed");
        logger.info("datadog reporter closed");
        if (LuckyChunk.isChunkSpawned() && getConfig().getBoolean("lucky-chunk")) {
            logger.info("undoing last lucky chunk...");
            reporter.suppressReporting();
            Utils.commandAsConsole("/undo");
            reporter.returnReporting();
            logger.info("done!");
            return;
        }
        HandlerList.unregisterAll();
        Sentry.addBreadcrumb("events unregistered");
        logger.info("events unregistered");
        logger.info("goodbye!");
    }

    // suppressing because IntelliJ doesn't care about the try/catch for some reason
    @SuppressWarnings("DataFlowIssue")
    private void registerCommands() {
        try {
            getCommand("die").setExecutor(new Die());

            getCommand("tpa").setExecutor(new TeleportAsk());
            getCommand("tpy").setExecutor(new TeleportAsk());
            getCommand("tpn").setExecutor(new TeleportAsk());

            getCommand("mock").setExecutor(new Mock());
            getCommand("spawnon").setExecutor(new SpawnOn());
            getCommand("maintenance").setExecutor(new Maintenance());
            getCommand("firework").setExecutor(new Firework());

            getCommand("test-sentry").setExecutor(new Test());
            getCommand("test-notifications").setExecutor(new Test());
            getCommand("test-error-func").setExecutor(new Test());

            getCommand("gamble").setExecutor(new Casino());

            getCommand("spawn-chunk-now").setExecutor(new LuckyChunkManagement());
            getCommand("remove-chunk-now").setExecutor(new LuckyChunkManagement());
            getCommand("lctp").setExecutor(new LuckyChunkTeleport());
            getCommand("lc").setExecutor(new LuckyChunkStatus());

            getCommand("mizu-reload-config").setExecutor(new ReloadConfig());
            getCommand("fuck-you").setExecutor(new FuckYou());
            getCommand("warp").setExecutor(new Warp());
            getCommand("stats").setExecutor(new Stats());
            getCommand("vault").setExecutor(new Vault());
            getCommand("killswitch").setExecutor(new Killswitch());

            getCommand("mizu-effect").setExecutor(new MizuEffectCommand());
        } catch (NullPointerException e) {
            logger.severe("we're missing an entry for a command in the plugin.yml file! not good!");
            // rethrow so that the server automatically disables the plugin
            throw e;
        }
    }
}
