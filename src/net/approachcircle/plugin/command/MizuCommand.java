package net.approachcircle.plugin.command;

import net.approachcircle.plugin.*;
import net.approachcircle.plugin.effect.MizuEffectRegistry;
import net.approachcircle.plugin.effect.MizuEffectType;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class MizuCommand implements CommandExecutor {
    @Override
    public final boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        // fetch the killswitch status if it exists, if not, default to true.
        // true = command is on, not killswitch is on
        // false = commands is off, not killswitch is off
        if (!Mizu.getInstance().getConfig().getBoolean(command.getName(), true)) {
            sender.sendMessage(Localisation.unlocalisedPrefixComponent(new ColouredString("sorry, this command is currently killswitched", NamedTextColor.RED)));
            return true;
        }
        if (sender instanceof Player player && MizuEffectRegistry.getEffect(MizuEffectType.SUPPRESSED).playerHasEffect(player)) {
            if (label.equalsIgnoreCase("die")) {
                // feels the aura
                sender.sendMessage(Localisation.unlocalisedPrefixComponent(new ColouredString("death is not an escape...", NamedTextColor.RED)));
                return true;
            }
            sender.sendMessage(Localisation.unlocalisedPrefixComponent(new ColouredString("you are currently suppressed!", NamedTextColor.RED)));
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
            sender.sendMessage(Localisation.unlocalisedPrefixComponent(new ColouredString("an unexpected error occurred executing this command!", NamedTextColor.RED)));
            Utils.error(e, "uncaught exception during command execution!");
        }
        return true;
    }

    protected abstract boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args);
}
