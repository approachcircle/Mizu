package net.approachcircle.plugin.data;


import net.approachcircle.plugin.PluginLogger;
import net.approachcircle.plugin.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import java.util.UUID;

public class WarpStorage extends FlatFileStorage<Warp> {
    protected WarpStorage() {
        super("warps.txt");
    }

    @Override
    protected String serialize(Warp entry) {
        return String.format("%s %f %f %f %s %s %f",
                // not nullable
                entry.warpName,
                // throws exception if parsing fails
                entry.x,
                entry.y,
                entry.z,
                // not nullable
                entry.world.getName(),
                // just be like C# bro
                entry.owner != null ? entry.owner.toString() : "null",
                entry.yaw
        );
    }

    @Override
    protected Warp deserialize(String line) {
        Warp warp = new Warp();
        String[] tokens = line.split(" ");
        warp.warpName = tokens[0];
        try {
            warp.x = Double.parseDouble(tokens[1]);
            warp.y = Double.parseDouble(tokens[2]);
            warp.z = Double.parseDouble(tokens[3]);
        } catch (NumberFormatException e) {
            // if the warp's location is unintelligible, then that's pretty world ending
            throw new DeserializationException("an error occurred parsing coordinates for this warp: '" + warp.warpName + "'");
        }
        World world = Bukkit.getWorld(tokens[4]);
        if (world == null) {
            // we also do kinda need the world, because while we could just hope/guess that the warp is in the overworld,
            // that could also end pretty badly if it was supposed to be in the end or the nether
            throw new DeserializationException("an error occurred parsing the world for this warp: '" + warp.warpName + "'");
        }
        warp.world = world;
        try {
            warp.owner = UUID.fromString(tokens[5]);
        } catch (IllegalArgumentException ex) {
            // if we fail parsing the UUID here, we're probably looking at a player name from the old format
            // so we'll try that.
            // we're gonna check only the cache for this player because we do NOT have the time to wait for a blocking
            // web request to try and resolve this player, ESPECIALLY if they have changed their name.
            // just want to make a point that saving anything belonging to anyone under their name instead of UUID is a
            // terrible idea... never again.
            OfflinePlayer owner = Bukkit.getOfflinePlayerIfCached(tokens[5]);
            if (owner != null && owner.hasPlayedBefore()) {
                warp.owner = owner.getUniqueId();
            } else {
                // either the player with this name is not cached, or they don't exist (maybe malformed/corrupted), in
                // any case we don't have a valid owner for this warp any more...
                warp.owner = null;
            }
        }
        try {
            warp.yaw = Float.parseFloat(tokens[6]);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            Utils.error(e, String.format("warp %s has no yaw or is malformed", warp.warpName));
            // set it to 0 for now so it's something valid, but send an alert
            warp.yaw = 0;
        }
        return warp;
    }

    public Location getLocation(Warp warp) {
        // we don't store pitch cause we don't really care
        return new Location(warp.world, warp.x, warp.y, warp.z, warp.yaw, 0);
    }
}
