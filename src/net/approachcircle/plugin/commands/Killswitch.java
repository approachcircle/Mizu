package net.approachcircle.plugin.commands;

import net.approachcircle.plugin.ColouredString;
import net.approachcircle.plugin.Localisation;
import net.approachcircle.plugin.Mizu;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Killswitch extends MizuCommand {
    @Override
    protected boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length < 2) return false;
        String state = args[0]; // on/off
        String commandName = args[1];
        if (commandName.equalsIgnoreCase("killswitch")) {
            sender.sendMessage(Localisation.unlocalisedPrefixComponent(new ColouredString("you cannot killswitch the killswitch bro...", NamedTextColor.RED)));
            return true;
        }
        // /killswitch off will set the command to true in the config (command is enabled), and vice versa
        Mizu.getInstance().getConfig().set(commandName, Objects.equals(state, "off"));
        ColouredString cs = new ColouredString("", NamedTextColor.GREEN);
        StringBuilder sb = new StringBuilder("killswitch ");
        if (state.equalsIgnoreCase("off")) {
            sb.append("disabled");
            cs.color = NamedTextColor.GREEN;
        } else {
            sb.append("enabled");
            cs.color = NamedTextColor.RED;
        }
        sb.append(" for command ").append(commandName);
        cs.string = sb.toString();
        sender.sendMessage(Localisation.unlocalisedPrefixComponent(cs));
        Mizu.getInstance().saveConfig();
        return true;
    }
}
