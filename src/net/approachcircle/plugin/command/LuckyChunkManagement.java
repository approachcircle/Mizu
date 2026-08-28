package net.approachcircle.plugin.command;

import net.approachcircle.plugin.KeyArgPair;
import net.approachcircle.plugin.Localisation;
import net.approachcircle.plugin.LuckyChunk;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class LuckyChunkManagement extends MizuCommand {
    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (label.equalsIgnoreCase("spawn-chunk-now")) {
            if (args.length > 0) {
                Material material = Material.getMaterial(args[0].toUpperCase());
                if (material == null) {
                    Localisation.sendMessage(sender, new KeyArgPair("material_does_not_exist", args[1]));
                    return false;
                }
                LuckyChunk.spawnChunk(material);
                Localisation.sendMessage(sender, new KeyArgPair("custom_lucky_chunk_material_warning_1"));
                Localisation.sendMessage(sender, new KeyArgPair("custom_lucky_chunk_material_warning_2"));
                Localisation.sendMessage(sender, new KeyArgPair("custom_lucky_chunk_material_warning_3"));
            } else {
                LuckyChunk.spawnChunk();
            }
        } else if (label.equalsIgnoreCase("remove-chunk-now")) {
            LuckyChunk.removeChunk();
        }
        return true;
    }
}
