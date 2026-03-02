package net.approachcircle.plugin;

import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Arrays;

public class VillagerDetector {
    public static void villagerDied(EntityDeathEvent event) {
        Villager villager = (Villager) event.getEntity();
        // bit techy, and it may stop working, but the only other alternative is a deprecated method being removed
        // in 1.22, which is probably soon
        // entity.minecraft.villager.librarian -> librarian
        // entity.minecraft.villager.none -> none
        String villagerType = Arrays.stream(villager.getProfession().translationKey().split("\\.")).toList().getLast();
        Player villagerKiller = villager.getKiller();
        if (villagerKiller != null) {
            Localisation.broadcast(new KeyArgPair("villager_killed_player",
                    villagerType,
                    (int)villager.getLocation().getX(),
                    (int)villager.getLocation().getY(),
                    (int)villager.getLocation().getZ(),
                    villager.getLocation().getWorld().getName(),
                    villagerKiller.getName()));
        } else {
            Localisation.broadcast(new KeyArgPair("villager_died",
                    villagerType,
                    (int)villager.getLocation().getX(),
                    (int)villager.getLocation().getY(),
                    (int)villager.getLocation().getZ(),
                    villager.getLocation().getWorld().getName()));
        }
    }
}
