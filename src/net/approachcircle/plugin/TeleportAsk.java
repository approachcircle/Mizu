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
    // not a fan of this, we should use a hashmap
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
            Localisation.sendMessage(commandSender, new KeyArgPair("self_teleport_error"));
            return;
        }
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            Localisation.sendMessage(commandSender, new KeyArgPair("invalid_teleport_target", targetName));
            return;
        }
        if (requesters.contains(commandSender) && targets.contains(target)) {
            Localisation.sendMessage(commandSender, new KeyArgPair("already_sent_tpa_request", targetName));
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
                requester.teleport(senderLocation);
                Localisation.sendMessage(commandSender, new KeyArgPair("teleport_request_accepted", target.getName()));
                logger.info(String.format("%s accepted a teleport request from %s", commandSender.getName(), requester.getName()));
                success = true;
                break;
            }
            index++;
        }
        if (success) {
            Localisation.sendMessage(commandSender, new KeyArgPair("teleport_accepted"));
            currentTask.cancel();
            // incorrect if multiple requests pending
            requesters.remove(0);
            targets.remove(0);
        } else {
            Localisation.sendMessage(commandSender, new KeyArgPair("no_teleport_to_accept"));
        }
    }

    public static void denyTeleport(Player commandSender) {
        String senderUUID = commandSender.getUniqueId().toString();
        int index = 0;
        boolean success = false;
        for (Player target : targets) {
            String targetUUID = target.getUniqueId().toString();
            if (senderUUID.equalsIgnoreCase(targetUUID)) {
                Localisation.sendMessage( requesters.get(index), new KeyArgPair("teleport_request_denied", commandSender.getName()));
                logger.info(String.format("%s denied a teleport request from %s", commandSender.getName(), requesters.get(index)));
                success = true;
                break;
            }
            index++;
        }
        if (success) {
            Localisation.sendMessage(commandSender, new KeyArgPair("teleport_denied"));
            currentTask.cancel();
            requesters.remove(0);
            targets.remove(0);
        } else {
            Localisation.sendMessage(commandSender, new KeyArgPair("no_teleport_to_deny"));
        }
    }

    private static void sendTeleportRequest(Player requester, Player target) {
        Localisation.sendMessage(requester, new KeyArgPair("sending_teleport_request", target.getName()));
        Localisation.sendMessage(requester, new KeyArgPair("teleport_expiry", timeToLive));
        requesters.add(requester);
        targets.add(target);
        currentTask = beginTimer(requester, target);
        Localisation.sendMessage(target, new KeyArgPair("teleport_request", requester.getName()));
        Localisation.sendMessage(target, new KeyArgPair("tpy_to_accept"));
        Localisation.sendMessage(target, new KeyArgPair("tpn_to_deny"));
        Localisation.sendMessage(target, new KeyArgPair("teleport_expiry", timeToLive));
        Localisation.sendMessage(requester, new KeyArgPair("request_sent"));
        logger.info(String.format("%s just sent a teleport request to %s", requester.getName(), target.getName()));
    }

    private static BukkitTask beginTimer(Player requester, Player target) {
        Plugin plugin = Mizu.getInstance();
        BukkitTask task = Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                Localisation.sendMessage(requester, new KeyArgPair("teleport_request_to_player_expired", target.getName()));
                Localisation.sendMessage(target, new KeyArgPair("teleport_request_from_player_expired", requester.getName()));
                requesters.remove(0);
                targets.remove(0);
                logger.info(String.format("%s's teleport request to %s has expired", requester.getName(), target.getName()));
            }
        }, 20L * timeToLive);
        return task;
    }
}
