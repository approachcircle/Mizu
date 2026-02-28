package net.approachcircle.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public class SpawnOn {
    private static final Logger logger = PluginLogger.getPluginLogger();

    public static void spawnOn(CommandSender sender, String mobName, String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        EntityType entity = EntityType.UNKNOWN;
        try {
            entity = Enum.valueOf(EntityType.class, mobName.toUpperCase());
        } catch (IllegalArgumentException e) {
            Localisation.sendMessage(sender, new KeyArgPair("no_entity_with_name", mobName));
            return;
        }
        World world = player.getWorld();
        world.spawnEntity(player.getLocation(), entity);
        Localisation.sendMessage(sender, new KeyArgPair("entity_spawned_on_player", mobName, player.getName()));
        logger.info("a " + entity + " was just spawned on " + player.getName());
    }
}
