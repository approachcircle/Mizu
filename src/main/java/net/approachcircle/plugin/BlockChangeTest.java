package net.approachcircle.plugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

public class BlockChangeTest {
    public static void placePhantomBlockAt(Location location, Player target, Material block) {
        BlockData data = block.createBlockData();
        target.sendBlockChange(location, data);
    }
}
