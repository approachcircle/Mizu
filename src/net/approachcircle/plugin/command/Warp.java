package net.approachcircle.plugin.command;

import net.approachcircle.plugin.KeyArgPair;
import net.approachcircle.plugin.Localisation;
import net.approachcircle.plugin.PluginLogger;
import net.approachcircle.plugin.WarpManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class Warp extends MizuCommand {
    private final Logger logger = PluginLogger.getPluginLogger();

    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            return false;
        }
        switch (args[0]) {
            case "add" -> {
                if (args.length < 2) {
                    return false;
                }
                if (sender instanceof Player player) {
                    if (Arrays.stream(WarpManager.reservedCommands).anyMatch((reserved) -> reserved.equalsIgnoreCase(args[1]))) {
                        Localisation.sendMessage(sender, new KeyArgPair("must_call_warp_something_else"));
                        return true;
                    }
                    if (WarpManager.getInstance().addNewWarp(args[1], player)) {
                        Localisation.sendMessage(sender, new KeyArgPair("added_warp", args[1]));
                    } else {
                        Localisation.sendMessage(sender, new KeyArgPair("failed_to_add_warp", args[1]));
                    }
                }
            }
            case "list" -> {
                List<String> warpLines = WarpManager.getInstance().listWarps(sender);
                if (warpLines != null) {
                    Localisation.sendMessage(sender, new KeyArgPair("found_number_warps", warpLines.size()));
                    for (String warp : warpLines) {
                        Localisation.sendMessage(sender, new KeyArgPair("warp_owned_by_player", warp.split(" ")[0], warp.split(" ")[5]));
                    }
                }
            }
            case "remove" -> {
                if (WarpManager.getInstance().removeWarp(args[1], sender)) {
                    Localisation.sendMessage(sender, new KeyArgPair("removed_warp", args[1]));
                } else {
                    Localisation.sendMessage(sender, new KeyArgPair("failed_to_remove_warp", args[1]));
                }
            }
            case "update" -> {
                // update just does the equivalent of remove then add with the same name
                if (sender instanceof Player player) {
                    if (args.length < 2) {
                        return false;
                    }
                    String owner = WarpManager.getInstance().getOwner(args[1]);
                    if (owner == null) {
                        Localisation.sendMessage(sender, new KeyArgPair("failed_to_get_warp_owner"));
                        return true;
                    }
                    // use boolean AND so that the condition short circuits if either remove or add fails
                    if (WarpManager.getInstance().removeWarp(args[1], sender) &&
                            WarpManager.getInstance().addNewWarp(args[1], player, owner)) {
                        Localisation.sendMessage(sender, new KeyArgPair("updated_warp_position", args[1]));
                    } else {
                        Localisation.sendMessage(sender, new KeyArgPair("failed_to_update_warp", args[1]));
                    }
                }
            }
            case "rename" -> {
                if (args.length < 3) {
                    return false;
                }
                String owner = WarpManager.getInstance().getOwner(args[1]);
                if (owner == null) {
                    Localisation.sendMessage(sender, new KeyArgPair("failed_to_rename_owner_not_found"));
                    return true;
                }
                String[] oldWarpTokens = WarpManager.getInstance().getWarpTokens(args[1]);
                if (oldWarpTokens == null) {
                    Localisation.sendMessage(sender, new KeyArgPair("failed_to_rename_parsing_failed"));
                    return true;
                }
                Location reusedLocation = null;
                try {
                    reusedLocation = new Location(
                            Bukkit.getWorld(oldWarpTokens[4]),
                            Double.parseDouble(oldWarpTokens[1]),
                            Double.parseDouble(oldWarpTokens[2]),
                            Double.parseDouble(oldWarpTokens[3]),
                            Float.parseFloat(oldWarpTokens[6]),
                            0
                    );
                } catch (NumberFormatException e) {
                    logger.severe("failed to parse a coordinate for warp: " + args[1] + "(it may be corrupted?)");
                    Localisation.sendMessage(sender, new KeyArgPair("failed_to_parse_warp_coords", args[1]));
                    return true;
                } catch (ArrayIndexOutOfBoundsException e) {
                    logger.severe("failed to find coordinates for warp: " + args[1]);
                    Localisation.sendMessage(sender, new KeyArgPair("failed_to_find_warp_coords", args[1]));
                }
                if (WarpManager.getInstance().getWarpTokens(args[2]) != null) {
                    Localisation.sendMessage(sender, new KeyArgPair("warp_name_taken", args[2]));
                    return true;
                }
                if (WarpManager.getInstance().removeWarp(args[1], sender) &&
                        WarpManager.getInstance().addNewWarpRaw(args[2], reusedLocation, owner)) {
                    Localisation.sendMessage(sender, new KeyArgPair("renamed_warp", args[1], args[2]));
                } else {
                    Localisation.sendMessage(sender, new KeyArgPair("failed_to_rename", args[1]));
                }
            }
            default -> {
                if (sender instanceof Player player) {
                    Location warpTarget = WarpManager.getInstance().getWarpLocation(args[0], player);
                    if (warpTarget == null) {
                        Localisation.sendMessage(sender, new KeyArgPair("warp_failed"));
                        return true;
                    }
                    player.teleport(warpTarget);
                    Localisation.sendMessage(sender, new KeyArgPair("warped_success", args[0]));
                }
            }
        }
        return true;
    }
}
