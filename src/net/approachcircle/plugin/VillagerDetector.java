package net.approachcircle.plugin;

import io.sentry.Sentry;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.EntityDeathEvent;

public class VillagerDetector {
    public static void villagerDied(EntityDeathEvent event) {
        Villager villager = (Villager) event.getEntity();
        Villager.Type villagerType = villager.getVillagerType();
        Player villagerKiller = villager.getKiller();
        if (villagerKiller != null) {
            Localisation.broadcast(new KeyArgPair("villager_killed_player",
                    villagerType,
                    villager.getLocation().getX(),
                    villager.getLocation().getY(),
                    villager.getLocation().getZ(),
                    villager.getLocation().getWorld().getName(),
                    villagerKiller.getName()));
        } else {
            Localisation.broadcast(new KeyArgPair("villager_died",
                    villagerType,
                    villager.getLocation().getX(),
                    villager.getLocation().getY(),
                    villager.getLocation().getZ(),
                    villager.getLocation().getWorld().getName()));
        }
    }
}
