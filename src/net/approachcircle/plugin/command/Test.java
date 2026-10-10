package net.approachcircle.plugin.command;

import io.sentry.Sentry;
import net.approachcircle.plugin.WebhookClient;
import net.approachcircle.plugin.Utils;
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
            WebhookClient.send(String.format("test notification from minecraft server '%s'", System.getenv("COMPUTERNAME")));
        } else if (label.equalsIgnoreCase("test-error-func")) {
            try {
                throw new Exception("test exception");
            } catch (Exception e) {
                Utils.error(e, "test exception thrown");
            }
        }
        return true;
    }
}
