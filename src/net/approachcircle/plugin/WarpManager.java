package net.approachcircle.plugin;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

public class WarpManager {
    private final String warpFileName = "warps.txt";
    private final File warpFile;
    private final Logger logger = PluginLogger.getPluginLogger();
    private static WarpManager instance = null;
    // warp_name x y z world
    // ^^^^ this format CANNOT change im so serious (shit will break heavily)
    
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
    
    public boolean addNewWarp(String name, Location location) {
        try (FileWriter writer = new FileWriter(warpFile, true)) {
            writer.append(String.format("%s %f %f %f %s%n", name, location.getX(), location.getY(), location.getZ(), location.getWorld().getName()));
        } catch (IOException e) {
            logger.severe("failed to add warp to warp file");
            e.printStackTrace(System.err);
            return false;
        }
        return true;
    }
    
    public boolean removeWarp(String name) {
        List<String> lines = getWarpFileLines();
        if (lines == null) {
            return false;
        }
        Iterator<String> it = lines.iterator();
        while (it.hasNext()) {
            String line = it.next();
            String[] tokens = line.split(" ");
            if (tokens[0].equals(name)) {
                it.remove();
            }
        }
        for (String line : lines) {
            try (FileWriter writer = new FileWriter(warpFile)) {
                writer.append(line).append("\n");
            } catch (IOException e) {
                logger.severe("failed to restore warp file");
                e.printStackTrace(System.err);
                return false;
            }
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

    public void listWarps(CommandSender sender) {
        sender.sendMessage(Utils.prefix + "warps:");
        List<String> warpLines = getWarpFileLines();
        if (warpLines == null) {
            sender.sendMessage(Utils.prefix + ChatColor.RED + "something went wrong grabbing warps");
            return;
        }
        if (warpLines.isEmpty()) {
            sender.sendMessage(Utils.prefix + ChatColor.RED + "no warps right now");
            return;
        }
        for (String line : warpLines) {
            sender.sendMessage(Utils.prefix + line.split(" ")[0]);
        }
    }
}
