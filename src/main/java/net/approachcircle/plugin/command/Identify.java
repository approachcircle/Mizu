package net.approachcircle.plugin.command;

import net.approachcircle.plugin.ColouredString;
import net.approachcircle.plugin.Localisation;
import net.approachcircle.plugin.Mizu;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;

public class Identify extends MizuCommand {
    @Override
    protected boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        sender.sendMessage(Localisation.rawPrefixComponent(new ColouredString(String.format("i am '%s'", getInformalServerName()))));
        String offlinePlayerNames = String.join(", ", Arrays
                .stream(Bukkit.getOfflinePlayers())
                .map(OfflinePlayer::getName)
                .toArray(String[]::new));
        sender.sendMessage(Localisation.rawPrefixComponent(new ColouredString("offline players: " + offlinePlayerNames)));
        return true;
    }

    public static String getInformalServerName() {
        return Arrays.stream(new File(Mizu.getInstance().getDataFolder().getAbsolutePath()).getParentFile().getParentFile().getAbsolutePath().split("\\\\")).toList().getLast();
    }
}
