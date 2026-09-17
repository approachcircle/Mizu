package net.approachcircle.plugin.data;

import net.approachcircle.plugin.KeyArgPair;
import net.approachcircle.plugin.Localisation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Warp {
    public String warpName;
    public Double x;
    public Double y;
    public Double z;
    public World world;
    public @Nullable UUID owner;
    public float yaw;

    public static Warp build(String name, Player player) {
        Warp warp = new Warp();
        warp.warpName = name;
        warp.x = player.getLocation().getX();
        warp.y = player.getLocation().getY();
        warp.z = player.getLocation().getZ();
        warp.world = player.getWorld();
        warp.owner = player.getUniqueId();
        warp.yaw = player.getLocation().getYaw();
        return warp;
    }

    public Location getLocation() {
        return new Location(world, x, y, z, yaw, 0);
    }

    public boolean belongsTo(Player player) {
        return player.getUniqueId() == owner;
    }

    public boolean canBeModifiedBy(CommandSender sender) {
        if (sender.isOp()) return true;
        if (sender instanceof Player player) {
            if (ownerName() == null) {
                Localisation.sendMessage(sender, new KeyArgPair("warp_no_owner_no_modify"));
                return false;
            }
            if (!belongsTo(player)) {
                Localisation.sendMessage(player, new KeyArgPair("warp_not_yours", ownerName()));
                return false;
            }
            return true;
        }
        // a sender that isn't an operator but also isn't a player? would be strange but whoever it is, get fucked
        return false;
    }

    public @Nullable String ownerName() {
        if (owner == null) return null;
        return Bukkit.getOfflinePlayer(owner).getName();
    }
}
