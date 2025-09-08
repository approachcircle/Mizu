package net.approachcircle.plugin;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class WarpManager {
    private final String warpFileName = "warps.txt";
    private final File warpFile;
    private final Logger logger = PluginLogger.getPluginLogger();
    private static WarpManager instance = null;
    // warp_name x y z world owner
    // ^^^^ ensure that logic is updated fully if this format changes
    
    private WarpManager() {
        warpFile = new File(Mizu.getInstance().getDataFolder().getAbsolutePath() + "\\" + warpFileName);
        if (!warpFile.exists()) {
            try {
                warpFile.createNewFile();
            } catch (IOException e) {
                logger.severe("failed to create warp file");
                e.printStackTrace(System.err);
            }
        }
    }
    
    public static WarpManager getInstance() {
        if (instance == null) {
            instance = new WarpManager();
        }
        return instance;
    }
    
    public boolean addNewWarp(String name, Player player) {
        for (String warpLine : getWarpFileLines()) {
            if (name.equals(warpLine.split(" ")[0])) {
                player.sendMessage(Utils.prefix + ChatColor.RED + "there is a warp with the name '" + name + "' already");
                return false;
            }
        }
        try (FileWriter writer = new FileWriter(warpFile, true)) {
            writer.append(String.format("%s %f %f %f %s %s%n", name, player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getWorld().getName(), player.getName()));
        } catch (IOException e) {
            logger.severe("failed to add warp to warp file");
            e.printStackTrace(System.err);
            return false;
        }
        return true;
    }
    
    public boolean removeWarp(String name, CommandSender sender) {
        List<String> lines = getWarpFileLines();
        if (lines == null) {
            return false;
        }
        Iterator<String> it = lines.iterator();
        while (it.hasNext()) {
            String line = it.next();
            String[] tokens = line.split(" ");
            if (tokens[0].equals(name)) {
                if (sender instanceof Player player) {
                    if (tokens.length < 6) {
                        player.sendMessage(String.format("%s%sthis warp doesn't belong to anyone (pre-ownership format) so to be safe, you can't delete it", Utils.prefix, ChatColor.RED));
                        return false;
                    }
                    if (tokens[5].equals(player.getName()) || sender.isOp()) {
                        it.remove();
                    } else {
                        player.sendMessage(String.format("%s%sthis warp doesn't belong to you (the owner is %s)", Utils.prefix, ChatColor.RED, tokens[5]));
                        return false;
                    }
                } else {
                    // must be console if we're here
                    it.remove();
                }
            }
        }
        if (lines.isEmpty()) {
            try (FileWriter writer = new FileWriter(warpFile)) {
                writer.append("");
                sender.sendMessage("cleared warps");
                return true;
            } catch (IOException e) {
                logger.severe("failed to clear warp file");
                e.printStackTrace(System.err);
                return false;
            }
        }
        try (FileWriter writer = new FileWriter(warpFile)) {
            for (String line : lines) {
                writer.append(line).append("\n");
            }
        } catch (IOException e) {
            logger.severe("failed to restore warp file");
            e.printStackTrace(System.err);
            return false;
        }
        return true;
    }
    
    public Location getWarpLocation(String name) {
        List<String> lines = getWarpFileLines();
        if (lines == null) {
            return null;
        }
        for (String line : lines) {
            String[] tokens = line.split(" ");
            if (tokens[0].equals(name)) {
                World world = Bukkit.getWorld(tokens[4]);
                if (world == null) {
                    logger.severe("got null while parsing world for warp: " + name);
                    return null;
                }
                try {
                    return new Location(world, Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]), Double.parseDouble(tokens[3]));
                } catch (NumberFormatException e) {
                    logger.severe("failed to parse a coordinate for warp: " + name);
                    return null;
                }
            }
        }
        return null;
    }
    
    public List<String> getWarpFileLines() {
        List<String> lines = new ArrayList<>();
        try (Scanner scanner = new Scanner(warpFile)) {
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
        } catch (IOException e) {
            logger.severe("something went wrong grabbing warps");
            e.printStackTrace(System.err);
            return null;
        }
        return lines;
    }

    public List<String> listWarps(CommandSender sender) {
        List<String> warpLines = getWarpFileLines();
        if (warpLines == null) {
            sender.sendMessage(Utils.prefix + ChatColor.RED + "something went wrong grabbing warps");
            return null;
        }
        if (warpLines.isEmpty()) {
            sender.sendMessage(Utils.prefix + ChatColor.RED + "no warps right now");
            return null;
        }
        return warpLines;
    }
}
