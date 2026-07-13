package net.approachcircle.plugin.commands;

import net.approachcircle.plugin.KeyArgPair;
import net.approachcircle.plugin.Localisation;
import net.approachcircle.plugin.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class SpawnOn extends MizuCommand {
    private static final Logger logger = PluginLogger.getPluginLogger();

    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length < 2) return false;
        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            Localisation.sendMessage(sender, new KeyArgPair("player_not_found", args[0]));
            return true;
        }
        String mobName = args[1];
        EntityType entity;
        try {
            entity = Enum.valueOf(EntityType.class, mobName.toUpperCase());
        } catch (IllegalArgumentException e) {
            Localisation.sendMessage(sender, new KeyArgPair("no_entity_with_name", mobName));
            return true;
        }
        World world = player.getWorld();
        world.spawnEntity(player.getLocation(), entity);
        Localisation.sendMessage(sender, new KeyArgPair("entity_spawned_on_player", mobName, player.getName()));
        logger.info(String.format("a %s was spawned on %s", entity, player.getName()));
        return true;
    }
}
