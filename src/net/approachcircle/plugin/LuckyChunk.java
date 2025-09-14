package net.approachcircle.plugin;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LuckyChunk {
    private static final Plugin plugin = Mizu.getInstance();
    private static final Logger logger = PluginLogger.getPluginLogger();
    private static final int spawnRadius = 500;
    private static int progressTask = -1;
    private static int removeTask = -1;
    private static int spawnTask = -1;
    // private static final int nextSpawnDelay = 36000; // 30m
    private static final int nextSpawnDelay = 24000; // 20m
    private static int nextSpawnProgress = nextSpawnDelay;
    private static int nextSpawnProgressTask = -1;
    private static int chunkFoundTask = -1;
    private static boolean chunkFound = false;
    private static final World chunkSpawnWorld = Bukkit.getWorld("world");
    private static final int chunkFoundRadius = 50;
    private static final int duration = 6000; // 5m
    // private static final int duration = 20 * 30;
    // private static final int nextSpawnDelay = 20 * 15;
    private static int progress = duration;
    private static final int progressStep = 20;
    private static final int height = 200;
    private static Material material;
    private static Material lastMaterial;
    private static boolean chunkDue = false; // used in the case when the server is empty
    private static List<Material> spawnableBlocks = List.of(new Material[] {
            Material.DIAMOND_ORE,
            Material.REDSTONE_ORE,
            Material.COAL_BLOCK,
            Material.SLIME_BLOCK,
            Material.NETHER_QUARTZ_ORE
    }); //unused
    private static List<String> valuables = List.of(
            "iron",
            "diamond",
            "gold",
            "netherite",
            "ancient",
            "redstone",
            "coal",
            "beacon",
            "core",
            "head",
            "shulker",
            "emerald",
            "hopper",
            "dragon"
    );
    private static List<String> blacklist;

    private static final BossBar bar = BossBar.bossBar(Component.text("Lucky Chunk"), 1.0f, BossBar.Color.GREEN, BossBar.Overlay.PROGRESS);
    private static boolean notifiedMinute = false;
    private static boolean notifiedListen = false;
    private static double x;
    private static final double y = 60;
    private static double z;
    private static boolean firstChunk = true;
    private static DatadogReporter reporter = DatadogReporter.getInstance();

    public static void start() {
        if (chunkSpawnWorld == null) {
            throw new RuntimeException("lucky chunk spawn world is null (misspelled or non-existent?)");
        }
        readBlacklist();
        // Utils.deferredRepeatWithDelay(LuckyChunk::spawnChunk, nextSpawnDelay, 0, 1);
        scheduleNewChunk();
    }

    public static void spawnChunk() {
        spawnChunk(getRandomConstrainedMaterial());
    }

    public static void spawnChunk(Material material) {
        if (material == null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isOp()) {
                    player.sendMessage("material to spawn chunk is null");
                    logger.warning("material to spawn chunk is null");
                }
            }
            return;
        }
        LuckyChunk.material = material;
        if (isChunkSpawned()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isOp()) {
                    player.sendMessage(Utils.prefix + ChatColor.RED + "cannot spawn a new chunk while one already exists. remove it first");
                }
            }
            return;
        }
        progress = duration;
        nextSpawnProgress = nextSpawnDelay;
        removeChunk(false);
        if (spawnTask != -1) {
            Bukkit.getScheduler().cancelTask(spawnTask);
        }
        if (nextSpawnProgressTask != -1) {
            Bukkit.getScheduler().cancelTask(nextSpawnProgressTask);
        }
        if (!Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
            logger.warning("lucky chunk is unavailable without worldedit");
            return;
        }
        double spawnX = chunkSpawnWorld.getSpawnLocation().getX();
        double spawnZ = chunkSpawnWorld.getSpawnLocation().getZ();
        Random rng = new Random();
        // 50/50 to choose if we should flip either axis
        if (rng.nextBoolean()) {
            x = rng.nextDouble(spawnX, spawnX + spawnRadius);
        } else {
            if (spawnX - spawnRadius < spawnX) {
                x = rng.nextDouble(spawnX - spawnRadius, spawnX);
            } else {
                x = rng.nextDouble(spawnX, spawnX - spawnRadius);
            }
        }
        if (rng.nextBoolean()) {
            z = rng.nextDouble(spawnZ, spawnZ + spawnRadius);
        } else {
            if (spawnZ - spawnRadius < spawnZ) {
                z = rng.nextDouble(spawnZ - spawnRadius, spawnZ);
            } else {
                z = rng.nextDouble(spawnZ, spawnZ - spawnRadius);
            }
        }
        // chunk = 16x16
        Utils.commandAsConsole(String.format("/world %s", chunkSpawnWorld.getName()));
        Utils.commandAsConsole(String.format("/pos1 %d,%d,%d", Math.round(x), Math.round(y), Math.round(z)));
        Utils.commandAsConsole(String.format("/pos2 %d,%d,%d", Math.round(x + 15), Math.round(y + height), Math.round(z + 15)));
        reporter.suppressReporting();
        Utils.commandAsConsole(String.format("/set %s", material.name().toLowerCase()));
        reporter.returnReporting();
        removeTask = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            removeChunk(true);
        }, duration);
        double finalX = x;
        double finalZ = z;
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showBossBar(bar);
            player.sendMessage(Utils.prefix + ChatColor.GREEN + "lucky chunk has spawned!");
            if (player.isOp()) {
                sendTeleportMessage(player);
            }
            if (player.getWorld().getName().equals(chunkSpawnWorld.getName())) {
                player.playSound(Sound.sound(Key.key("minecraft:block.note_block.chime"), Sound.Source.BLOCK, 999, 1), finalX, y / 2, finalZ);
            }
        }
        progressTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> { // every second
            progress -= progressStep;
            if (progress < 0) {
                progress = 0;
            }
            bar.progress(progress * ((float) 1 / duration));
            if (progress <= 20 * 60) { // 1 minute left
                bar.color(BossBar.Color.YELLOW);
            }
            if (progress <= 20 * 30) { // 30 seconds
                bar.color(BossBar.Color.RED);
            }
            if (progress < 20 * 3) { // 3 seconds
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getWorld().getName().equals(chunkSpawnWorld.getName())) {
                        player.playSound(Sound.sound(Key.key("minecraft:block.note_block.chime"), Sound.Source.PLAYER, 999, 0.1f), finalX, y, finalZ);
                    }
                }
            }
            if (progress % (20 * 60) == 0 && progress != duration) { // every minute, excluding when first spawned (a sound already plays)
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getWorld().getName().equals(chunkSpawnWorld.getName())) {
                        player.playSound(Sound.sound(Key.key("minecraft:block.note_block.chime"), Sound.Source.BLOCK, 999, 1), finalX, y / 2, finalZ);
                    }
                }
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.showBossBar(bar); // in case a player joins during a chunk, they must be shown the bar
            }
        }, 0, progressStep);
        if (progressTask == -1) {
            throw new RuntimeException("scheduling failed for lucky chunk progress update task");
        }
        chunkFoundTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Location loc = player.getLocation();
                Location from = new Location(chunkSpawnWorld, finalX - chunkFoundRadius, y, finalZ - chunkFoundRadius);
                Location to = new Location(chunkSpawnWorld, finalX + chunkFoundRadius, y + (height * 2), finalZ + chunkFoundRadius);
                if ((from.x() < loc.x() && to.x() > loc.x()) && (from.y() < loc.y() && to.y() > loc.y()) && (from.z() < loc.z() && to.z() > loc.z())) {
                    Bukkit.broadcast(Component.text(Utils.prefix + ChatColor.GOLD + String.format("%s found the chunk!", player.getName())));
                    Bukkit.broadcast(Component.text(Utils.prefix + ChatColor.GOLD + String.format("it's %s", material.name().toLowerCase().replace("_", " "))));
                    String messageBuffer = String.format("%s found the chunk, it's %s", player.getName(), material.name().toLowerCase().replace("_", " "));
                    for (String valuable : valuables) {
                        if (material.name().toLowerCase().contains(valuable)) {
                            messageBuffer += " @everyone";
                            break;
                        }
                    }
                    NotificationClient.send(messageBuffer);
                    if (chunkFoundTask != -1) {
                        Bukkit.getScheduler().cancelTask(chunkFoundTask);
                    }
                    sendTeleportMessage(); // no player specified so will broadcast
                    chunkFound = true;
                }
            }
        }, 0, 1);
    }

    public static void removeChunk(boolean scheduleNew) {
        if (!isChunkSpawned()) {
            return;
        }
        reporter.suppressReporting();
        Utils.commandAsConsole("/undo");
        reporter.returnReporting();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.hideBossBar(bar);
            player.sendMessage(Utils.prefix + ChatColor.RED + "lucky chunk has disappeared!");
            if (!chunkFound) {
                player.sendMessage(Utils.prefix + ChatColor.GOLD + "nobody found the chunk!");
                player.sendMessage(Utils.prefix + ChatColor.GOLD + String.format("it was %s", material.name().toLowerCase().replace("_", " ")));
            }
        }
        resetState();
        if (scheduleNew) {
            //TODO: fix shit not work, new chunk not scheduling!"!¬!!!!!!!!!!!!!!!!!!!!!!!!!!
            //TODO: is working now??????????
            scheduleNewChunk();
        }
    }

    public static void removeChunk() {
        removeChunk(true);
    }

    private static void resetState() {
        Bukkit.getScheduler().cancelTask(progressTask);
        Bukkit.getScheduler().cancelTask(removeTask);
        Bukkit.getScheduler().cancelTask(chunkFoundTask);
        progress = duration;
        nextSpawnProgress = nextSpawnDelay;
        notifiedMinute = false;
        notifiedListen = false;
        chunkFound = false;
        lastMaterial = material;
        bar.color(BossBar.Color.GREEN);
        bar.progress(1.0f);
    }


    public static void scheduleNewChunk() {
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            setChunkDue(true);
            logger.info("marked chunk due as there are no players present");
            return;
        }
        if (isChunkDue()) {
            logger.info("scheduling due chunk");
            setChunkDue(false);
        }
        logger.info("called chunk schedule");
        spawnTask = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, LuckyChunk::spawnChunk, nextSpawnDelay); //30m
        nextSpawnProgressTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            nextSpawnProgress -= progressStep;
            if (nextSpawnProgress <= 20 * 60) {
                if (!notifiedMinute) {
                    Bukkit.broadcast(Utils.cPrefix.append(Component.text("lucky chunk in one minute!", NamedTextColor.GOLD)));
                    notifiedMinute = true;
                }
            }
            if (nextSpawnProgress <= 20 * 5) {
                if (!notifiedListen) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getWorld().getName().equalsIgnoreCase(chunkSpawnWorld.getName())) {
                            // players must be in the same world as the chunk otherwise they aren't going to be
                            // listening for much
                            player.sendMessage(Utils.cPrefix.append(Component.text("listen closely...", NamedTextColor.GOLD)));
                        }
                    }
                    notifiedListen = true;
                }
            }
        }, 0, progressStep);
        logger.info("chunk scheduled");
    }

    private static Material getRandomMaterial() {
        Material[] materials = Material.values();
        int idx = new Random().nextInt(0, materials.length);
        return materials[idx];
    }

    private static Material getRandomConstrainedMaterial() {
        Material material = null;
        boolean reroll = true;
        while (reroll) {
            material = getRandomMaterial();
            if (!material.isAir() && material.isBlock()) {
                reroll = false;
            } else {
                logger.info(String.format("skipping lucky chunk block '%s' (does not meet block criteria)", material.name().toLowerCase()));
            }
            if (Bukkit.getPluginManager().isPluginEnabled("Timber") && material.name().contains("log")) {
                logger.info(String.format("skipping lucky chunk block '%s' (timber plugin is enabled)", material.name().toLowerCase()));
                reroll = true;
                continue;
            }
            if (blacklist == null) {
                logger.warning("no blacklist found");
                return material;
            }
            for (Object item : blacklist) {
                String itemString = String.valueOf(item);
                if (material.name().toLowerCase().contains(itemString)) {
                    logger.info(String.format("skipping lucky chunk block '%s' (blacklisted)", material.name().toLowerCase()));
                    reroll = true;
                    break;
                }
            }
        }
        return material;
    }

    public static boolean isChunkSpawned() {
        return progress < duration;
    }
    
    public static boolean isChunkDue() {
        return chunkDue;
    }
    
    public static void setChunkDue(boolean chunkDue) {
        LuckyChunk.chunkDue = chunkDue;
    }

    public static int getTicksUntilSpawn() {
        return nextSpawnProgress;
    }
    public static int getTicksUntilRemoval() {
        return progress;
    }

    public static void readBlacklist() {
        blacklist = plugin.getConfig().getStringList("blacklist");
        if (blacklist.isEmpty()) {
            logger.warning("blacklist empty");
        } else {
            logger.info("blacklist contains: " + String.join(", ", blacklist));
        }
    }

    public static Material getLastMaterial() {
        return lastMaterial;
    }


    public static void sendTeleportMessage() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendTeleportMessage(player);
        }
    }

    // works for anybody now
    public static void sendTeleportMessage(Player player) {
        TextComponent text = Component.text(Utils.prefix + ChatColor.GOLD + "click to teleport");
        text = text.hoverEvent(HoverEvent.showText(Component.text(String.format("%d %d %d", Math.round(x), Math.round(y) + height, Math.round(z)))));
        text = text.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/lctp"));
        player.sendMessage(text);
    }

    public static void teleportToChunk(Player player) {
        if (isChunkSpawned()) {
            if (chunkFound || player.isOp()) {
                player.teleport(new Location(chunkSpawnWorld, (double) Math.round(x) + 7.5, Math.round(y) + height + 1,  Math.round(z) + 7.5));
            } else {
                player.sendMessage(Utils.prefix + ChatColor.RED + "chunk has not been found yet!");
            }
        } else {
            player.sendMessage(Utils.prefix + ChatColor.RED + "chunk not currently spawned!");
        }
    }

    public static boolean isChunkFound() {
        return chunkFound;
    }
}
