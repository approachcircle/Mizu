package net.approachcircle.plugin.commands;

import net.approachcircle.plugin.KeyArgPair;
import net.approachcircle.plugin.Localisation;
import net.approachcircle.plugin.StatisticManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Stats extends MizuCommand {
    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (sender instanceof Player player) {
            Map.Entry<String, Integer> mostUsedCommand = StatisticManager.getInstance().getMostUsedCommandForPlayer(player);
            Localisation.sendMessage(sender, new KeyArgPair("most_used_command_personally",
                    mostUsedCommand.getKey(),
                    mostUsedCommand.getValue())
            );
        }
        StatisticManager.getInstance().sendMostUsedCommandGlobally(sender);
        return true;
    }
}
