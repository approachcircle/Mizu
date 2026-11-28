package net.approachcircle.plugin.command;

import net.approachcircle.plugin.KeyArgPair;
import net.approachcircle.plugin.Localisation;
import net.approachcircle.plugin.LuckyChunk;
import net.approachcircle.plugin.Mizu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class LuckyChunkStatus extends MizuCommand {
    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!Mizu.getInstance().getConfig().getBoolean("lucky-chunk")) {
            Localisation.sendMessage(sender, new KeyArgPair("lucky_chunk_not_enabled"));
            return true;
        }
        if (LuckyChunk.getLastMaterial() == null) {
            Localisation.sendMessage(sender, new KeyArgPair("no_previous_chunk"));
        } else {
            String lastMat = LuckyChunk.getLastMaterial().name().toLowerCase().replace("_", " ");
            Localisation.sendMessage(sender, new KeyArgPair("last_chunk_was", lastMat));
        }
        if (LuckyChunk.isChunkSpawned()) {
            Localisation.sendMessage(sender, new KeyArgPair("chunk_currently_spawned"));
            int minutes = (LuckyChunk.getTicksUntilRemoval() / 20) / 60;
            int seconds = (LuckyChunk.getTicksUntilRemoval() / 20) % 60;
            Localisation.sendMessage(sender, new KeyArgPair("time_until_lucky_chunk_removal", minutes, seconds));
            return true;
        }
        int minutes = (LuckyChunk.getTicksUntilSpawn() / 20) / 60;
        int seconds = (LuckyChunk.getTicksUntilSpawn() / 20) % 60;
        Localisation.sendMessage(sender, new KeyArgPair("time_until_lucky_chunk_spawn", minutes, seconds));
        return true;
    }
}
