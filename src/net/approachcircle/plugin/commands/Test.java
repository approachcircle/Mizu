package net.approachcircle.plugin.commands;

import io.sentry.Sentry;
import net.approachcircle.plugin.NotificationClient;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class Test extends MizuCommand {
    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (label.equalsIgnoreCase("test-sentry")) {
            try {
                throw new Exception("test exception");
            } catch (Exception e) {
                Sentry.captureException(e);
            }
        } else if (label.equalsIgnoreCase("test-notifications")) {
            NotificationClient.send(String.format("test notification from minecraft server '%s'", System.getenv("COMPUTERNAME")));
        }
        return true;
    }
}
