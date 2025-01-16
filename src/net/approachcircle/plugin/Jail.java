package net.approachcircle.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Jail {
    public void jail(Player player) {
        Location initialLoc = player.getLocation();
        player.teleport(new Location(initialLoc.getWorld(), initialLoc.getX(), initialLoc.getY() + 20, initialLoc.getZ()));
        Location finalLoc = player.getLocation();
        //TODO: finish
    }
}
