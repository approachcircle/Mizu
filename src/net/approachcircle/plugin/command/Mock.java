package net.approachcircle.plugin.command;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class Mock extends MizuCommand {
    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length < 2) return false;
        StringBuilder joinedMessage = new StringBuilder();
        for (String word : args[0].split(" ")) {
            if (word.equalsIgnoreCase(args[1])) {
                continue;
            }
            joinedMessage.append(word).append(" ");
        }
        Bukkit.broadcast(Component.text("<" + args[1] + "> " + joinedMessage));
        return true;
    }
}
