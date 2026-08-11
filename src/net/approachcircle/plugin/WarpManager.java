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
    public static String[] reservedCommands = {
        "add",
        "list",
        "remove",
        "update",
        "rename"
    };
    // warp_name x y z world owner yaw
    // ^^^^ ensure that logic is updated fully if this format changes
    
    private WarpManager() {
        warpFile = new File(Mizu.getInstance().getDataFolder().getAbsolutePath() + "\\" + warpFileName);
        if (!warpFile.exists()) {
            try {
                warpFile.createNewFile();
            } catch (IOException e) {
                Utils.error(e, "failed to create warp file");
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
        return addNewWarp(name, player, player.getName());
    }

    public boolean addNewWarp(String name, Player player, String owner) {
        if (getWarpTokens(name) != null) {
            Localisation.sendMessage(player, new KeyArgPair("warp_name_taken", name));
            return false;
        }
        try (FileWriter writer = new FileWriter(warpFile, true)) {
            writer.append(String.format("%s %f %f %f %s %s %f%n",
                    name,
                    player.getLocation().getX(),
                    player.getLocation().getY(),
                    player.getLocation().getZ(),
                    player.getLocation().getWorld().getName(),
                    owner,
                    player.getYaw()
            ));
        } catch (IOException e) {
            Utils.error(e, "failed to add warp to warp file");
            return false;
        }
        return true;
    }

    public boolean addNewWarpRaw(String name, Location location, String owner) {
        if (getWarpTokens(name) != null) {
            return false;
        }
        try (FileWriter writer = new FileWriter(warpFile, true)) {
            writer.append(String.format("%s %f %f %f %s %s %f%n",
                    name,
                    location.getX(),
                    location.getY(),
                    location.getZ(),
                    location.getWorld().getName(),
                    owner,
                    location.getYaw()
            ));
        } catch (IOException e) {
            Utils.error(e, "failed to add warp to warp file");
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
                        Localisation.sendMessage(player, new KeyArgPair("warp_no_owner_no_delete"));
                        return false;
                    }
                    if (tokens[5].equals(player.getName()) || sender.isOp()) {
                        it.remove();
                    } else {
                        Localisation.sendMessage(player, new KeyArgPair("warp_not_yours", tokens[5]));
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
                return true;
            } catch (IOException e) {
                Utils.error(e, "failed to clear warp file");
                return false;
            }
        }
        try (FileWriter writer = new FileWriter(warpFile)) {
            for (String line : lines) {
                writer.append(line).append("\n");
            }
        } catch (IOException e) {
            Utils.error(e, "failed to restore warp file");
            return false;
        }
        return true;
    }

    public Location getWarpLocation(String name, Player player) {
        String[] warpTokens = getWarpTokens(name);
        if (warpTokens == null) {
            return null;
        }
        World world = Bukkit.getWorld(warpTokens[4]);
        if (world == null) {
            Utils.error("got null while parsing world for warp: " + name);
            return null;
        }
        float yaw = 0;
        try {
            yaw = Float.parseFloat(warpTokens[6]);
        } catch (ArrayIndexOutOfBoundsException e) {
            PluginLogger.getPluginLogger().warning(String.format("warp %s is missing yaw, defaulting to 0", name));
            if (warpTokens[5].equals(player.getName())) {
                Localisation.sendMessage(player, new KeyArgPair("warp_no_yaw_1"));
                Localisation.sendMessage(player, new KeyArgPair("warp_no_yaw_2"));
            }
        }
        try {
            // pitch is 0 since we don't care
            return new Location(world, Double.parseDouble(warpTokens[1]), Double.parseDouble(warpTokens[2]), Double.parseDouble(warpTokens[3]), yaw, 0);
        } catch (NumberFormatException e) {
            Utils.error(e, "failed to parse a coordinate for warp: " + name + "(it may be corrupted?)");
            return null;
        }
    }
    
    public List<String> getWarpFileLines() {
        List<String> lines = new ArrayList<>();
        try (Scanner scanner = new Scanner(warpFile)) {
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
        } catch (IOException e) {
            Utils.error(e, "an error occurred parsing warps");
            return null;
        }
        return lines;
    }

    public String[] getWarpTokens(String name) {
        List<String> lines = getWarpFileLines();
        if (lines == null) {
            return null;
        }
        for (String line : lines) {
            String[] tokens = line.split(" ");
            if (tokens[0].equals(name)) {
                return tokens;
            }
        }
        return null;
    }

    public String getOwner(String name) {
        String[] tokens = getWarpTokens(name);
        if (tokens == null) {
            return null;
        }
        return tokens[5];
    }

    public List<String> listWarps(CommandSender sender) {
        List<String> warpLines = getWarpFileLines();
        if (warpLines == null) {
            Localisation.sendMessage(sender, new KeyArgPair("error_grabbing_warps"));
            return null;
        }
        if (warpLines.isEmpty()) {
            Localisation.sendMessage(sender, new KeyArgPair("no_warps"));
            return null;
        }
        return warpLines;
    }
}
