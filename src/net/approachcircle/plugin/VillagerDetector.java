package net.approachcircle.plugin;

import io.sentry.Sentry;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;

public class VillagerDetector {
    public static boolean checkEntityType(EntityDeathEvent event) {
        if (event.getEntityType() == EntityType.VILLAGER) {
            villagerHasDied(event);
            return true;
        }
        return false;
    }

    private static void villagerHasDied(EntityDeathEvent event) {
        LivingEntity villagerEntity = event.getEntity();
        Location villagerLocation = villagerEntity.getLocation();
        Player villagerKiller = villagerEntity.getKiller();
        if (villagerKiller != null) {
            EntityType villagerKillerEntity = villagerKiller.getType();
            if (villagerKillerEntity == EntityType.PLAYER) {
                announceDeath(villagerKiller, villagerLocation);
            }
        } else {
            announceDeath(villagerLocation);
        }
    }

    private static void announceDeath(Player villagerKiller, Location villagerLocation) {
        String villagerx = Integer.valueOf((int) villagerLocation.getX()).toString();
        String villagery = Integer.valueOf((int) villagerLocation.getY()).toString();
        String villagerz = Integer.valueOf((int) villagerLocation.getZ()).toString();
        String worldName = villagerLocation.getWorld().getName();
        String message = Utils.prefix + String.format("%sa villager at %s %s %s in world '%s' "
                        + "was killed by %s", ChatColor.RED, villagerx, villagery, villagerz,
                worldName, villagerKiller.getName());
        Bukkit.broadcast(Component.text(message));
        Sentry.addBreadcrumb(message);
    }

    private static void announceDeath(Location villagerLocation) {
        String villagerx = Integer.valueOf((int) villagerLocation.getX()).toString();
        String villagery = Integer.valueOf((int) villagerLocation.getY()).toString();
        String villagerz = Integer.valueOf((int) villagerLocation.getZ()).toString();
        String worldName = villagerLocation.getWorld().getName();
        String message = Utils.prefix + String.format("%sa villager at %s %s %s in world '%s' "
                        + "has died", ChatColor.YELLOW, villagerx, villagery, villagerz,
                worldName);
        Bukkit.broadcast(Component.text(message));
        Sentry.addBreadcrumb(message);
    }
}
