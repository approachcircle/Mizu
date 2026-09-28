package net.approachcircle.plugin.command;

import net.approachcircle.plugin.*;
import net.approachcircle.plugin.effect.MizuEffectRegistry;
import net.approachcircle.plugin.effect.MizuEffectType;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class MizuCommand implements TabExecutor {
    // is any command ever really gonna have to override this itself?
    @Override
    public final @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return getTabCompletions(sender, command, label, args);
    }

    @Override
    public final boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        // fetch the killswitch status if it exists, if not, default to true.
        // true = command is on, not killswitch is on
        // false = commands is off, not killswitch is off
        if (Killswitch.commandIsKillswitched(command)) {
            sender.sendMessage(Localisation.rawPrefixComponent(new ColouredString("sorry, this command is currently killswitched", NamedTextColor.RED)));
            return true;
        }
        if (sender instanceof Player player && MizuEffectRegistry.getEffect(MizuEffectType.SUPPRESSED).playerHasEffect(player)) {
            if (label.equalsIgnoreCase("die")) {
                // feels the aura
                sender.sendMessage(Localisation.rawPrefixComponent(new ColouredString("death is not an escape...", NamedTextColor.RED)));
                return true;
            }
            sender.sendMessage(Localisation.rawPrefixComponent(new ColouredString("you are currently suppressed!", NamedTextColor.RED)));
            return true;
        }
        PluginLogger.getPluginLogger().info("executing mizu command: " + command.getName());
        try {
            boolean execResult = run(sender, command, label, args);
            // increment stat on successful execution only
            if (execResult && sender instanceof Player) {
                StatisticManager.getInstance().commandUsed((Player) sender, label.toLowerCase());
            }
            return execResult;
        } catch (Exception e) {
            sender.sendMessage(Localisation.rawPrefixComponent(new ColouredString("an unexpected error occurred executing this command!", NamedTextColor.RED)));
            Utils.error(e, "uncaught exception during command execution!");
        }
        return true;
    }

    protected abstract boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args);

    protected final List<String> getPlayerNames() {
        List<String> playerNames = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerNames.add(player.getName());
        }
        return playerNames;
    }

    protected final List<String> getEntityNames() {
        List<String> entities = new ArrayList<>();
        for (EntityType entityType : EntityType.values()) {
            entities.add(entityType.name().toLowerCase());
        }
        return entities;
    }

    protected final List<String> getMaterialNames() {
        List<String> materials = new ArrayList<>();
        for (Material materialType : Material.values()) {
            materials.add(materialType.name().toLowerCase());
        }
        return materials;
    }

    protected final List<String> numbersTo(int stop) {
        List<String> numbers = new ArrayList<>();
        for (int i = 0; i <= stop; i++) {
            numbers.add(String.valueOf(i));
        }
        return numbers;
    }

    protected final List<String> copyMatchesAndSort(String arg, List<String> options) {
        List<String> completions = copyMatches(arg, options);
        sort(completions);
        return completions;
    }

    protected final List<String> copyMatches(String arg, List<String> options) {
        List<String> completions = new ArrayList<>();
        StringUtil.copyPartialMatches(arg, options, completions);
        return completions;
    }

    @Contract(mutates = "param1")
    protected final void sort(List<String> completions) {
        completions.sort(String::compareToIgnoreCase);
    }

    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
