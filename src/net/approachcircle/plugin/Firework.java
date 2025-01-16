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
            target.sendMessage(Utils.prefix + String.format("%syour firework cooldown hasn't expired yet", ChatColor.RED));
            logger.info(String.format("%s tried to get fireworks before the cooldown expired", target.getName()));
            Sentry.addBreadcrumb(String.format("%s tried to get fireworks before the cooldown expired", target.getName()));
        } else {
            giveFireworks(target);
        }
    }

    private static void giveFireworks(Player target) {
        ItemStack fireworks = new ItemStack(Material.FIREWORK_ROCKET, 64);
        logger.info(String.format("%s just got %s fireworks, giving %s minute cooldown", target.getName(), fireworks.getAmount(), cooldownMinutes));
        Sentry.addBreadcrumb(String.format("%s just got %s fireworks, giving %s minute cooldown", target.getName(), fireworks.getAmount(), cooldownMinutes));
        target.getInventory().addItem(fireworks);
        ineligible.add(target.getUniqueId());
        target.sendMessage(String.format(Utils.prefix + "%syou have been given %s fireworks, you now have a %s minute cooldown", ChatColor.GREEN, fireworks.getAmount(), cooldownMinutes));
        startTimer(target);
    }

    private static void startTimer(Player target) {
        Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                ineligible.remove(target.getUniqueId());
                target.sendMessage(Utils.prefix + String.format("%syour firework cooldown has expired", ChatColor.GREEN));
                logger.info(String.format("%s's firework cooldown has expired", target.getName()));
                Sentry.addBreadcrumb(String.format("%s's firework cooldown has expired", target.getName()));
            }
        }, cooldown);
    }
}
