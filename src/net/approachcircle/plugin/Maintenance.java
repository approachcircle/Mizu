package net.approachcircle.plugin;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

public class Maintenance {
    private static final Plugin plugin = Mizu.getInstance();
    private static final Logger logger = plugin.getLogger();
    private static Boolean maintenanceMode = false;

    public static void toggleMaintenance(CommandSender sender) {
        sender.sendMessage("Mizu> " + ChatColor.GREEN + "maintenance mode: " + maintenanceMode + " -> " + !maintenanceMode);
        maintenanceMode = !maintenanceMode;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.isOp()) {
                turnAwayPlayer(player, false);
            }
        }
    }

    public static void turnAwayPlayer(Player player, Boolean triedToJoin) {
        player.kick(Component.text(
                ChatColor.DARK_GREEN + "==--Mizu--==\n" + ChatColor.RESET +
                        ChatColor.MAGIC + "a" + ChatColor.RESET +
                        ChatColor.RED + "the server is currently in maintenance mode." +
                        ChatColor.RESET + ChatColor.MAGIC + "a" +
                        ChatColor.RESET + "\n" + ChatColor.DARK_AQUA +
                        "this means that i am currently performing maintenance on " +
                        "the server, so it is unstable." + ChatColor.BLUE +
                        "\neither join back later, or message me on discord."
        ));

        if (triedToJoin) {
            for (Player curPlayer : Bukkit.getOnlinePlayers()) {
                if (curPlayer.isOp()) {
                    curPlayer.sendMessage("Mizu> " + ChatColor.RED + player.getName() + " just tried to join");
                }
            }
            logger.info(player.getName() + " just tried to join");
        }
    }

    public static Boolean getState() {
        return maintenanceMode;
    }
}
