package net.approachcircle.plugin;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Die {
    public static List<UUID> suicided = new ArrayList<UUID>();

    public static void suicide(Player player) {
        suicided.add(player.getUniqueId());
        player.setHealth(0);
    }
}
