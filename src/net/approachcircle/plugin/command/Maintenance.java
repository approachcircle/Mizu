package net.approachcircle.plugin.command;

import net.approachcircle.plugin.KeyArgPair;
import net.approachcircle.plugin.Localisation;
import net.approachcircle.plugin.Mizu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class Maintenance extends MizuCommand {
    private static final Plugin plugin = Mizu.getInstance();
    private static final Logger logger = plugin.getLogger();
    private static Boolean maintenanceMode = false;

    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        Localisation.sendMessage(sender, new KeyArgPair("maintenance_status_update", maintenanceMode, !maintenanceMode));
        maintenanceMode = !maintenanceMode;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.isOp()) {
                turnAwayPlayer(player, false);
            }
        }
        return true;
    }

    public static void turnAwayPlayer(Player player, Boolean triedToJoin) {
        player.kick(
                Component.text("==--Mizu--==", NamedTextColor.DARK_GREEN)
                .appendNewline()
                .append(Localisation.component(new KeyArgPair("server_currently_in_maintenance"), Localisation.getClientLocaleOrExplicit(player)))
                .appendNewline()
                .append(Localisation.component(new KeyArgPair("server_unavailable_for_players"), Localisation.getClientLocaleOrExplicit(player)))
                .appendNewline()
                .append(Localisation.component(new KeyArgPair("join_back_later"), Localisation.getClientLocaleOrExplicit(player)))
        );

        if (triedToJoin) {
            for (Player curPlayer : Bukkit.getOnlinePlayers()) {
                if (curPlayer.isOp()) {
                    Localisation.sendMessage(curPlayer, new KeyArgPair("player_tried_to_join_during_maintenance", player.getName()));
                }
            }
            logger.info(player.getName() + " just tried to join");
        }
    }

    public static Boolean getState() {
        return maintenanceMode;
    }
}
