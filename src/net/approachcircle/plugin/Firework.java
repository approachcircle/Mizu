package net.approachcircle.plugin;

import io.sentry.Sentry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class Firework {
    private static final List<UUID> ineligible = new ArrayList<UUID>();
    private static final Plugin plugin = Mizu.getInstance();
    private static final Logger logger = plugin.getLogger();
    private static final Integer cooldownMinutes = 5;
    private static final Integer cooldownSeconds = cooldownMinutes * 60;
    private static final Integer cooldown = cooldownSeconds * 20;

    public static void giveIfPossible(Player target) {
        Integer contains = 0;
        for (UUID id : ineligible) {
            if (id.equals(target.getUniqueId())) {
                contains++;
            }
        }
        if (contains > 0) {
            Localisation.sendMessage(target, new KeyArgPair("firework_cooldown_still_present"));
        } else {
            giveFireworks(target);
        }
    }

    private static void giveFireworks(Player target) {
        ItemStack fireworks = new ItemStack(Material.FIREWORK_ROCKET, 64);
        logger.info(String.format("%s just got fireworks", target.getName()));
        target.getInventory().addItem(fireworks);
        ineligible.add(target.getUniqueId());
        Localisation.sendMessage(target, new KeyArgPair("been_given_fireworks", 64, cooldownMinutes));
        startTimer(target);
    }

    private static void startTimer(Player target) {
        Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                ineligible.remove(target.getUniqueId());
                Localisation.sendMessage(target, new KeyArgPair("firework_cooldown_expired"));
                logger.info(String.format("%s's firework cooldown has expired", target.getName()));
            }
        }, cooldown);
    }
}
