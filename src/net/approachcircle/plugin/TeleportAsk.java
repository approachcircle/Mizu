package net.approachcircle.plugin;

import io.sentry.Sentry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class TeleportAsk {
    private static final List<Player> requesters = new ArrayList<Player>();
    private static final List<Player> targets = new ArrayList<Player>();
    private static BukkitTask currentTask;
    private static final Logger logger = PluginLogger.getPluginLogger();
    private static final Integer timeToLive = 15;


    public static void requestTeleport(Player commandSender, String targetName) {
        if (targetName.equalsIgnoreCase("yes")) {
            acceptTeleport(commandSender);
            return;
        }
        if (targetName.equalsIgnoreCase("no")) {
            denyTeleport(commandSender);
            return;
        }
        if (targetName.equalsIgnoreCase(commandSender.getName())) {
            commandSender.sendMessage(String.format("%s%syou tryna teleport to yourself fool????", Utils.prefix, ChatColor.RED));
            return;
        }
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            commandSender.sendMessage(Utils.prefix + String.format("%s%s is not a valid player to teleport to", ChatColor.RED, targetName));
            return;
        }
        if (requesters.contains(commandSender) && targets.contains(target)) {
            commandSender.sendMessage(Utils.prefix + String.format("%syou've already sent a teleport request to %s", ChatColor.RED, targetName));
            return;
        }
        sendTeleportRequest(commandSender, target);
    }

    public static void acceptTeleport(Player commandSender) {
        String senderUUID = commandSender.getUniqueId().toString();
        int index = 0;
        boolean success = false;
        for (Player target : targets) {
            String targetUUID = target.getUniqueId().toString();
            if (senderUUID.equalsIgnoreCase(targetUUID)) {
                Location senderLocation = commandSender.getLocation();
                Player requester = requesters.get(index);
                Sentry.addBreadcrumb(String.format("%s accepted a teleport request from %s", commandSender.getName(), requester.getName()));
                requester.teleport(senderLocation);
                requester.sendMessage(Utils.prefix + String.format("%syour teleport to %s has been accepted", ChatColor.GREEN, target.getName()));
                logger.info(String.format("%s accepted a teleport request from %s", commandSender.getName(), requester.getName()));
                success = true;
                break;
            }
            index++;
        }
        if (success) {
            commandSender.sendMessage(Utils.prefix + ChatColor.GREEN + "okay, teleported accepted");
            currentTask.cancel();
            requesters.remove(0);
            targets.remove(0);
        } else {
            commandSender.sendMessage(Utils.prefix + ChatColor.RED + "you don't have a teleport request to accept");
        }
    }

    public static void denyTeleport(Player commandSender) {
        String senderUUID = commandSender.getUniqueId().toString();
        int index = 0;
        boolean success = false;
        for (Player target : targets) {
            String targetUUID = target.getUniqueId().toString();
            if (senderUUID.equalsIgnoreCase(targetUUID)) {
                Sentry.addBreadcrumb(String.format("%s denied a teleport request from %s", commandSender.getName(), requesters.get(index)));
                requesters.get(index).sendMessage(Utils.prefix + ChatColor.RED + "your teleport request to " + target.getName() + " was denied");
                logger.info(String.format("%s denied a teleport request from %s", commandSender.getName(), requesters.get(index)));
                success = true;
                break;
            }
            index++;
        }
        if (success) {
            commandSender.sendMessage(Utils.prefix + ChatColor.YELLOW + "okay, teleport denied");
            currentTask.cancel();
            requesters.remove(0);
            targets.remove(0);
        } else {
            commandSender.sendMessage(Utils.prefix + ChatColor.RED + "you don't have a teleport request to deny");
        }


    }

    private static void sendTeleportRequest(Player requester, Player target) {
        Sentry.addBreadcrumb(String.format("%s just sent a teleport request to %s", requester.getName(), target.getName()));
        requester.sendMessage(Utils.prefix + String.format("%ssending teleport request to %s", ChatColor.YELLOW, target.getName()));
        requester.sendMessage(Utils.prefix + String.format("%s(expires in %s seconds)", ChatColor.YELLOW, timeToLive));
        requesters.add(requester);
        targets.add(target);
        currentTask = beginTimer(requester, target);
        target.sendMessage(Utils.prefix + String.format("%s%s would like to teleport to you", ChatColor.YELLOW, requester.getName()));
        target.sendMessage(Utils.prefix + String.format("%s'/tpy' to allow this teleport", ChatColor.DARK_GREEN));
        target.sendMessage(Utils.prefix + String.format("%s'/tpn' to deny this teleport", ChatColor.RED));
        target.sendMessage(Utils.prefix + String.format("%s(expires in %s seconds)", ChatColor.YELLOW, timeToLive));
        requester.sendMessage(Utils.prefix + String.format("%srequest sent", ChatColor.YELLOW));
        logger.info(String.format("%s just sent a teleport request to %s", requester.getName(), target.getName()));
    }

    private static BukkitTask beginTimer(Player requester, Player target) {
        Plugin plugin = Mizu.getInstance();
        BukkitTask task = Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                Sentry.addBreadcrumb(String.format("%s's teleport request to %s expired", requester.getName(), target.getName()));
                requester.sendMessage(Utils.prefix + String.format("%syour teleport request to %s expired", ChatColor.RED, target.getName()));
                target.sendMessage(Utils.prefix + String.format("%sthe teleport request from %s expired", ChatColor.RED, requester.getName()));
                requesters.remove(0);
                targets.remove(0);
                logger.info(String.format("%s's teleport request to %s has expired", requester.getName(), target.getName()));
            }
        }, 20L * timeToLive);
        return task;
    }
}
