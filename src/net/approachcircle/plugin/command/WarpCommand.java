package net.approachcircle.plugin.command;

import net.approachcircle.plugin.*;
import net.approachcircle.plugin.data.Storage;
import net.approachcircle.plugin.data.Warp;
import net.approachcircle.plugin.data.WarpStorage;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WarpCommand extends MizuCommand {
    public static String[] reservedCommands = {
            "add",
            "list",
            "remove",
            "update",
            "rename"
    };

    // this should be safe because WarpStorage should definitely exist by the time this command class is instantiated
    // within the onEnable function
    private final WarpStorage warpStorage = Storage.of(WarpStorage.class);

    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            return false;
        }
        switch (args[0]) {
            case "add" -> {
                if (args.length < 2) return false;
                // any other cases are dealt with by the function below.
                // any reasons why a warp can't be added or modified are sent to the user separately so that we
                // can construct a clean update execution path
                switch (tryAddWarp(sender, args[1])) {
                    case ADD_FAILED -> {
                        Localisation.sendMessage(sender, new KeyArgPair("failed_to_add_warp", args[1]));
                    }
                    case ADD_SUCCESS -> {
                        Localisation.sendMessage(sender, new KeyArgPair("added_warp", args[1]));
                    }
                }
            }
            case "remove" -> {
                switch (tryRemoveWarp(sender, args[1])) {
                    case REMOVE_FAILED -> {
                        Localisation.sendMessage(sender, new KeyArgPair("failed_to_remove_warp", args[1]));
                    }
                    case REMOVE_SUCCESS -> {
                        Localisation.sendMessage(sender, new KeyArgPair("removed_warp", args[1]));
                    }
                }
            }
            case "update" -> {
                // remove then add with the same name
                if (args.length < 2) return false;
                if (tryRemoveWarp(sender, args[1]) == WarpOperationResult.REMOVE_FAILED ||
                        tryAddWarp(sender, args[1]) == WarpOperationResult.ADD_FAILED) {
                    Localisation.sendMessage(sender, new KeyArgPair("failed_to_update_warp", args[1]));
                    return true;
                }
                Localisation.sendMessage(sender, new KeyArgPair("updated_warp_position", args[1]));
            }
            case "list" -> {
                List<Warp> warps;
                try {
                    warps = warpStorage.list();
                } catch (IOException e) {
                    sender.sendMessage(Localisation.rawPrefixComponent(new ColouredString("failed to list warps", NamedTextColor.RED)));
                    Utils.error(e, "failed to list warps");
                    return true;
                }
                Localisation.sendMessage(sender, new KeyArgPair("found_number_warps", warps.size()));
                warps.forEach(warp -> {
                    // owner name could be null, but i'm fine printing that if it is
                    Localisation.sendMessage(sender, new KeyArgPair("warp_owned_by_player", warp.warpName, warp.ownerName()));
                });
            }
            case "rename" -> {
                if (args.length < 3) {
                    return false;
                }
                switch (tryRenameWarp(sender, args[1], args[2])) {
                    case RENAME_FAILED -> {
                        Localisation.sendMessage(sender, new KeyArgPair("failed_to_rename", args[1]));
                    }
                    case RENAME_SUCCESS -> {
                        Localisation.sendMessage(sender, new KeyArgPair("renamed_warp", args[1], args[2]));
                    }
                }
            }
            default -> {
                if (sender instanceof Player player) {
                    Location warpTarget;
                    try {
                        // TODO: null safety
                        warpTarget = warpStorage.getWarp(args[0]).getLocation();
                    } catch (IOException e) {
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

    // ehhhhh i'm coming around to this idea, don't like it MUCH but i mean it works and it's pretty clean
    enum WarpOperationResult {
        ADD_FAILED,
        ADD_SUCCESS,
        REMOVE_FAILED,
        REMOVE_SUCCESS,
        RENAME_FAILED,
        RENAME_SUCCESS,
        NOOP
    }

    private WarpOperationResult tryAddWarp(CommandSender sender, String name) {
        if (!(sender instanceof Player player)) {
            return WarpOperationResult.NOOP;
        }
        if (Arrays.stream(reservedCommands).anyMatch((reserved) -> reserved.equalsIgnoreCase(name))) {
            Localisation.sendMessage(sender, new KeyArgPair("must_call_warp_something_else"));
            return WarpOperationResult.NOOP;
        }
        try {
            warpStorage.add(Warp.build(name, player));
        } catch (IOException e) {
            Utils.error(e, "failed to add a warp");
            return WarpOperationResult.ADD_FAILED;
        }
        return WarpOperationResult.ADD_SUCCESS;
    }

    private WarpOperationResult tryRemoveWarp(CommandSender sender, String name) {
        try {
            Warp warp = warpStorage.getWarp(name);
            if (warp.canBeModifiedBy(sender)) {
                warpStorage.remove((w) -> w.warpName.equalsIgnoreCase(name));
            }
        } catch (IOException e) {
            Utils.error(e, "failed to remove a warp");
            return WarpOperationResult.REMOVE_FAILED;
        }
        return WarpOperationResult.REMOVE_SUCCESS;
    }

    private WarpOperationResult tryRenameWarp(CommandSender sender, String oldName, String newName) {
        try {
            Warp warp = warpStorage.getWarp(oldName);
            if (warp.canBeModifiedBy(sender)) {
                if (tryRemoveWarp(sender, oldName) == WarpOperationResult.REMOVE_FAILED) {
                    return WarpOperationResult.RENAME_FAILED;
                }
                warp.warpName = newName;
                warpStorage.add(warp);
            }
        } catch (IOException e) {
            Utils.error(e, "failed to rename warp");
            return WarpOperationResult.RENAME_FAILED;
        }
        return WarpOperationResult.RENAME_SUCCESS;
    }

    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> options = new ArrayList<>();
        try {
            if (args.length == 1) {
                options.addAll(Arrays.asList(reservedCommands));
                    warpStorage.list().forEach(warp -> {
                        options.add(warp.warpName);
                    });
                return copyMatchesAndSort(args[0], options);
            } else if (args.length == 2 && (!args[0].equalsIgnoreCase("add"))) { // if it's not "add" then it's a modification
                warpStorage.list().forEach(warp -> {
                    if (sender instanceof Player player && !player.isOp() && !warp.belongsTo(player)) {
                        return;
                    }
                    // must either be console (not an instance of player), an operator, or the warp owner
                    options.add(warp.warpName);
                });
                return copyMatchesAndSort(args[1], options);
            }
        } catch (IOException e) {
            Utils.error(e, "failed to enumerate warps for tab completion");
        }
        return List.of();
    }
}
