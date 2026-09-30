package net.approachcircle.plugin.command;

import net.approachcircle.plugin.ColouredString;
import net.approachcircle.plugin.Localisation;
import net.approachcircle.plugin.Mizu;
import net.approachcircle.plugin.Utils;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Killswitch extends MizuCommand {
    @Override
    protected boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length < 2) return false;
        String state = args[0]; // on/off
        String commandName = args[1];
        if (commandName.equalsIgnoreCase("killswitch")) {
            sender.sendMessage(Localisation.rawPrefixComponent(new ColouredString("you cannot killswitch the killswitch bro...", NamedTextColor.RED)));
            return true;
        }
        if (!Utils.getCommandsInConfig().contains(commandName)) {
            sender.sendMessage(Localisation.rawPrefixComponent(new ColouredString(String.format("the command '%s' was not found in my config file! did you spell it wrong?", commandName), NamedTextColor.RED)));
            return true;
        }
        // /killswitch off will set the command to true in the config (command is enabled), and vice versa
        ColouredString cs = new ColouredString("", NamedTextColor.GREEN);
        StringBuilder sb = new StringBuilder("killswitch ");
        if (state.equalsIgnoreCase("off")) {
            if (!commandIsKillswitched(Objects.requireNonNull(Mizu.getInstance().getCommand(commandName)))) {
                sender.sendMessage(Localisation.rawPrefixComponent(new ColouredString("this command is not killswitched in the first place!", NamedTextColor.YELLOW)));
                return true;
            }
            sb.append("disabled");
            cs.color = NamedTextColor.RED;
        } else {
            if (commandIsKillswitched(Objects.requireNonNull(Mizu.getInstance().getCommand(commandName)))) {
                sender.sendMessage(Localisation.rawPrefixComponent(new ColouredString("this command is already killswitched!", NamedTextColor.YELLOW)));
                return true;
            }
            sb.append("enabled");
            cs.color = NamedTextColor.GREEN;
        }
        sb.append(" for command ").append(commandName);
        cs.string = sb.toString();
        Mizu.getInstance().getConfig().set(commandName, Objects.equals(state, "off"));
        sender.sendMessage(Localisation.rawPrefixComponent(cs));
        Mizu.getInstance().saveConfig();
        return true;
    }

    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            // already sorted + there's only two options, so we can use an immutable List.of here
            return copyMatches(args[0], List.of("off", "on"));
        } else if (args.length == 2) {
            return copyMatchesAndSort(args[1], new ArrayList<>(Utils.getCommandsInConfig()));
        }
        return List.of();
    }

    public static boolean commandIsKillswitched(Command command) {
        return !Mizu.getInstance().getConfig().getBoolean(command.getName(), true);
    }
}
