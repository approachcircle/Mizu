package net.approachcircle.plugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Die extends MizuCommand {
    public static List<UUID> suicided = new ArrayList<>();

    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (sender instanceof Player player) {
            suicided.add(player.getUniqueId());
            player.setHealth(0);
        }
        return true;
    }
}
