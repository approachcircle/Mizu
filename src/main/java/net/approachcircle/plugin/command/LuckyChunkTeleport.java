package net.approachcircle.plugin.command;

import net.approachcircle.plugin.LuckyChunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LuckyChunkTeleport extends MizuCommand {
    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (sender instanceof Player player) {
            LuckyChunk.teleportToChunk(player);
        }
        return true;
    }
}
