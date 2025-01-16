package net.approachcircle.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class MagicWand {
    public static void affectBlock(Block targetBlock) {
        Location blockLocation = targetBlock.getLocation();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendBlockChange(blockLocation, Material.AIR.createBlockData());
        }
    }

    public static void resetBlock(Block targetBlock) {
        Location blockLocation = targetBlock.getLocation();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendBlockChange(blockLocation, targetBlock.getType().createBlockData());
        }
    }
}
