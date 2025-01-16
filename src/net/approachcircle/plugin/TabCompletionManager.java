package net.approachcircle.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TabCompletionManager {
    private static TabCompletionManager instance;
    
    private TabCompletionManager() {}
    
    public static TabCompletionManager getInstance() {
        if (instance == null) {
            instance = new TabCompletionManager();
        }
        return instance;
    }
    
    public void initTabCompletions() {
        Mizu mzInstance = Mizu.getInstance();
        mzInstance.getCommand("die").setTabCompleter(this::getNoCompletions);
        mzInstance.getCommand("tpa").setTabCompleter(this::getPlayerCompletions);
        mzInstance.getCommand("tpy").setTabCompleter(this::getNoCompletions);
        mzInstance.getCommand("tpn").setTabCompleter(this::getNoCompletions);
        mzInstance.getCommand("mock").setTabCompleter(this::getPlayerCompletions);
        mzInstance.getCommand("maintenance").setTabCompleter(this::getNoCompletions);
        mzInstance.getCommand("spawnon").setTabCompleter((sender, command, label, args) -> {
            List<String> completions = new ArrayList<>();
            List<String> options = new ArrayList<>();
            if (args.length == 1) {
                return getPlayerCompletions(sender, command, label, args);
            } else if (args.length == 2) {
                for (EntityType type : EntityType.values()) {
                    options.add(type.name().toLowerCase());
                }
            }
            StringUtil.copyPartialMatches(args[1], options, completions);
            return completions;
        });
        mzInstance.getCommand("firework").setTabCompleter(this::getNoCompletions);
        mzInstance.getCommand("gamble").setTabCompleter((sender, command, label, args) -> {
            List<String> completions = new ArrayList<>();
            List<String> options = new ArrayList<>();
            if (args.length == 1) {
                return List.of("<number>");
                // return numbersTo(sender, command, label, args, 64);
            } else if (args.length == 2) {
                // specifically not going to sort this so that xp and hp are pushed to the top
                options.add("xp");
                options.add("levels");
                options.add("hp");
                options.add("health");
                for (Material type : Material.values()) {
                    options.add(type.name().toLowerCase());
                }
                for (EntityType type : EntityType.values()) {
                    // could fail, unsure if we need type.name() or type.toString() here
                    if (CommandDispatcher.EntityBlacklist.contains(type.name().toLowerCase())) {
                        continue;
                    }
                    options.add(type.name().toLowerCase());
                }
            }
            StringUtil.copyPartialMatches(args[1], options, completions);
            return completions;
        });
        mzInstance.getCommand("spawn-chunk-now").setTabCompleter((sender, command, label, args) -> {
            List<String> completions = new ArrayList<>();
            List<String> options = new ArrayList<>();
            for (Material type : Material.values()) {
                options.add(type.name().toLowerCase());
            }
            StringUtil.copyPartialMatches(args[0], options, completions);
            return completions;
        });
        mzInstance.getCommand("lc").setTabCompleter(this::getNoCompletions);
        mzInstance.getCommand("fuck-you").setTabCompleter(this::getPlayerCompletions);
        mzInstance.getCommand("warp").setTabCompleter((sender, command, label, args) -> {
            List<String> completions = new ArrayList<>();
            List<String> options = new ArrayList<>();
            if (args.length == 1) {
                options.add("add");
                options.add("remove");
                options.add("list");
                for (String line : WarpManager.getInstance().getWarpFileLines()) {
                    options.add(line.split(" ")[0]);
                }
            } else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
                for (String line : WarpManager.getInstance().getWarpFileLines()) {
                    options.add(line.split(" ")[0]);
                }
            }
            StringUtil.copyPartialMatches(args[0], options, completions);
            return completions;
        });
        mzInstance.getCommand("stats").setTabCompleter(this::getNoCompletions);
    }
    
    private List<String> getPlayerCompletions(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        StringUtil.copyPartialMatches(args[0], Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), completions);
        completions.sort(String::compareToIgnoreCase);
        return completions;
    }
    
    private List<String> getNoCompletions(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
    
    //TODO: make impl nicer later (cannot be fucked)
    private List<String> numbersTo(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, int stop) {
        List<String> completions = new ArrayList<>();
        List<String> numbers = new ArrayList<>();
        for (int i = 0; i <= stop; i++) {
            numbers.add(String.valueOf(i));
        }
        StringUtil.copyPartialMatches(args[0], numbers, completions);
        if (numbers.contains(args[0])) {
            completions.add(args[0]);
        }
        return completions;
    }
}
