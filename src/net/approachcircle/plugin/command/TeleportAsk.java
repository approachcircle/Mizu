package net.approachcircle.plugin.command;

import net.approachcircle.plugin.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

public class TeleportAsk extends MizuCommand {
    // requester : target
    private static final HashMap<UUID, UUID> teleportRequestMap = new HashMap<>();
    private static BukkitTask currentTask;
    private static final Logger logger = PluginLogger.getPluginLogger();
    private static final Integer timeToLive = 20;

    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (sender instanceof Player player) {
            if (label.equalsIgnoreCase("tpa")) {
                if (args.length == 0) {
                    return false;
                }
                requestTeleport(player, args[0]);
            } else if (label.equalsIgnoreCase("tpy")) {
                answerTeleport(player, TeleportResponse.Accept);
            } else if (label.equalsIgnoreCase("tpn")) {
                answerTeleport(player, TeleportResponse.Deny);
            }
        }
        return true;
    }

    private static void requestTeleport(Player commandSender, String targetName) {
        if (targetName.equalsIgnoreCase("yes")) {
            answerTeleport(commandSender, TeleportResponse.Accept);
            return;
        }
        if (targetName.equalsIgnoreCase("no")) {
            answerTeleport(commandSender, TeleportResponse.Deny);
            return;
        }
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            Localisation.sendMessage(commandSender, new KeyArgPair("invalid_teleport_target", targetName));
            return;
        }
        // getPlayer can find a partially typed name, which can bypass the old targetName == senderName check
        // so we compare the name of the actual returned player with the sender
        if (target.getName().equalsIgnoreCase(commandSender.getName())) {
            Localisation.sendMessage(commandSender, new KeyArgPair("self_teleport_error"));
            return;
        }
        // cancel (remove) the teleport that's already present for this player if one exists
        if (teleportRequestMap.containsKey(commandSender.getUniqueId())) {
            if (teleportRequestMap.get(commandSender.getUniqueId()).equals(target.getUniqueId())) {
                Localisation.sendMessage(commandSender, new KeyArgPair("already_sent_tpa_request", targetName));
                return;
            }
            Localisation.sendMessage(Bukkit.getPlayer(teleportRequestMap.get(commandSender.getUniqueId())), "tpa_requester_cancelled");
            teleportRequestMap.remove(commandSender.getUniqueId());
        }
        sendTeleportRequest(commandSender, target);
    }

    public static void answerTeleport(Player commandSender, TeleportResponse response) {
        UUID senderUUID = commandSender.getUniqueId();
        if (teleportRequestMap.containsValue(senderUUID)) {
            for (UUID id : teleportRequestMap.keySet()) {
                if (teleportRequestMap.get(id).equals(senderUUID)) {
                    Player requester = Bukkit.getPlayer(id);
                    // if the requester is null, i assume that means they have left the server.
                    // either that, or it's a fallback in case the UUID doesn't exist i'm not sure.
                    // whatever the case, we don't care
                    if (requester == null) {
                        return;
                    }
                    if (response == TeleportResponse.Accept) {
                        requester.teleport(commandSender);
                        Localisation.sendMessage(requester, new KeyArgPair("teleport_request_accepted", commandSender.getName()));
                        logger.info(String.format("%s accepted a teleport request from %s", commandSender.getName(), requester.getName()));
                    } else {
                        Localisation.sendMessage(requester, new KeyArgPair("teleport_request_denied", commandSender.getName()));
                        logger.info(String.format("%s denied a teleport request from %s", commandSender.getName(), requester.getName()));
                    }
                    teleportRequestMap.remove(id);
                    currentTask.cancel();
                    break;
                }
            }
        } else {
            // i mean we COULD just make this one message like "you don't have any teleport requests" but whatever
            if (response == TeleportResponse.Accept) {
                Localisation.sendMessage(commandSender, new KeyArgPair("no_teleport_to_accept"));
            } else {
                Localisation.sendMessage(commandSender, new KeyArgPair("no_teleport_to_deny"));
            }
        }
    }

    private static void sendTeleportRequest(Player requester, Player target) {
        Localisation.sendMessage(requester, new KeyArgPair("sending_teleport_request", target.getName()));
        Localisation.sendMessage(requester, new KeyArgPair("teleport_expiry", timeToLive));
        teleportRequestMap.put(requester.getUniqueId(), target.getUniqueId());
        currentTask = beginTimer(requester, target);
        Localisation.sendMessage(target, new KeyArgPair("teleport_request", requester.getName()));
        Localisation.sendMessage(target, new KeyArgPair("tpy_to_accept"));
        Localisation.sendMessage(target, new KeyArgPair("tpn_to_deny"));
        Localisation.sendMessage(target, new KeyArgPair("teleport_expiry", timeToLive));
        Localisation.sendMessage(requester, new KeyArgPair("request_sent"));
        logger.info(String.format("%s just sent a teleport request to %s", requester.getName(), target.getName()));
    }

    private static BukkitTask beginTimer(Player requester, Player target) {
        return Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(Mizu.getInstance(), () -> {
            Localisation.sendMessage(requester, new KeyArgPair("teleport_request_to_player_expired", target.getName()));
            Localisation.sendMessage(target, new KeyArgPair("teleport_request_from_player_expired", requester.getName()));
            teleportRequestMap.remove(requester.getUniqueId());
            logger.info(String.format("%s's teleport request to %s has expired", requester.getName(), target.getName()));
        }, 20L * timeToLive);
    }

}
