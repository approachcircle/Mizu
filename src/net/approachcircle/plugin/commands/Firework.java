package net.approachcircle.plugin.commands;

import net.approachcircle.plugin.KeyArgPair;
import net.approachcircle.plugin.Localisation;
import net.approachcircle.plugin.Mizu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class Firework extends MizuCommand {
    private static final List<UUID> ineligible = new ArrayList<UUID>();
    private static final Plugin plugin = Mizu.getInstance();
    private static final Logger logger = plugin.getLogger();
    private static final int cooldownMinutes = 5;
    private static final int cooldownSeconds = cooldownMinutes * 60;
    private static final int cooldown = cooldownSeconds * 20;
    private static final int fireworkAmount = 64;

    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (sender instanceof Player player) {
            if (ineligible.contains((player).getUniqueId())) {
                Localisation.sendMessage(player, new KeyArgPair("firework_cooldown_still_present"));
                return true;
            }
            ItemStack fireworks = new ItemStack(Material.FIREWORK_ROCKET);
            fireworks.setAmount(fireworkAmount);
            player.getInventory().addItem(fireworks);
            logger.info(String.format("%s just got fireworks", player.getName()));
            Localisation.sendMessage(player, new KeyArgPair("been_given_fireworks", fireworkAmount, cooldownMinutes));
            ineligible.add(player.getUniqueId());
            // start timer
            Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                ineligible.remove(player.getUniqueId());
                Localisation.sendMessage(player, new KeyArgPair("firework_cooldown_expired"));
                logger.info(String.format("%s's firework cooldown has expired", player.getName()));
            }, cooldown);
        }
        return true;
    }
}
