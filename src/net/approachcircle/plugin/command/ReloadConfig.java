package net.approachcircle.plugin.command;

import net.approachcircle.plugin.ColouredString;
import net.approachcircle.plugin.Localisation;
import net.approachcircle.plugin.LuckyChunk;
import net.approachcircle.plugin.Mizu;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadConfig extends MizuCommand {
    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        sender.sendMessage(Localisation.rawPrefixComponent(new ColouredString("config reloading...", NamedTextColor.YELLOW)));
        Mizu.getInstance().reloadConfig();
        sender.sendMessage(Localisation.rawPrefixComponent(new ColouredString("done!", NamedTextColor.GREEN)));
        LuckyChunk.readBlacklist();
        return true;
    }
}
