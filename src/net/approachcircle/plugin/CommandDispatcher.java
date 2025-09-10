package net.approachcircle.plugin;

import io.sentry.Sentry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

public class CommandDispatcher {
    private static final String noPerms = Utils.prefix + ChatColor.RED + "sorry, you do not have the permission to run this command";
    private static final Logger logger = PluginLogger.getPluginLogger();
    private static final List<String> experienceAliases = List.of(new String[] {"exp", "xp", "levels", "experience", "level", "eckspee"});
    private static final List<String> healthAliases = List.of(new String[] {"health", "hp", "life"});
//    private static final List<String> gambleableEntities = List.of(
//            "pig",
//            "sheep",
//            "cow",
//            "chicken",
//            "creeper",
//            "zombie",
//            "skeleton",
//            "squid",
//            "axolotl",
//            "glow_squid",
//            "tropical_fish",
//            "cod",
//            "spider",
//            "enderman",
//            "wither",
//            "warden",
//            "blaze",
//            "wither_skeleton",
//            "guardian",
//            "elder_guardian",
//            "pufferfish",
//            "allay",
//            "stray",
//            "husk",
//            "phantom",
//            "bat",
//            "silverfish",
//            "ocelot",
//            "shulker",
//            "hoglin",
//            "piglin",
//            "zombified_piglin",
//            "strider",
//            "zombified_hoglin",
//            "witch",
//            "armadillo",
//            "bee"
//    );

    public static final List<String> EntityBlacklist = List.of(
            "ender_dragon",
            "wither",
            "villager",
            "wolf",
            "player",
            "horse"
    ); // TODO: need to make these entity types at some point

    private static Plugin plugin = Mizu.getInstance();

    public static boolean dispatch(String label, CommandSender sender, String[] args) {
        logger.info(String.format("dispatching command '/%s' through Mizu ", label));
        Sentry.addBreadcrumb(String.format("dispatching command '/%s'", label));
        if (sender instanceof Player) {
            // increments command usage stat even if args were wrong, or it was invalid, but still counts i guess  
            // should be fine i think
            StatisticManager.getInstance().commandUsed((Player) sender, label.toLowerCase());
        }
        switch (label.toLowerCase()) {
            case "die":
                if (sender instanceof Player player) {
                    if (sender.hasPermission("mizu.command.die")) {
                        Die.suicide(player);
                    } else {
                        insufficientPermissions(sender);
                    }
                }
                break;
            case "tpa":
                if (sender instanceof Player player) {
                    if (args.length <= 0) {
                        String[] missing = {"playerName"};
                        insufficientArguments(missing, sender);
                        return true;
                    }
                    if (sender.hasPermission("mizu.command.tpa")) {
                        TeleportAsk.requestTeleport(player, args[0]);
                    } else {
                        insufficientPermissions(sender);
                    }
                }
                break;
            case "tpy":
                if (sender instanceof Player player) {
                    if (sender.hasPermission("mizu.command.tpa")) {
                        TeleportAsk.acceptTeleport(player);
                    } else {
                        insufficientPermissions(sender);
                    }
                }
                break;
            case "tpn":
                if (sender instanceof Player player) {
                    if (sender.hasPermission("mizu.command.tpa")) {
                        TeleportAsk.denyTeleport(player);
                    } else {
                        insufficientPermissions(sender);
                    }
                }
                break;
            case "mock":
                if (sender.hasPermission("mizu.command.mock")) {
                    if (args.length <= 0) {
                        String[] missing = {"playerName", "message"};
                        insufficientArguments(missing, sender);
                        return true;
                    }
                    Mock.mockPlayer(args[0], args);
                } else {
                    insufficientPermissions(sender);
                }
                break;
            case "maintenance":
                if (sender.hasPermission("mizu.command.maintenance"))
                    Maintenance.toggleMaintenance(sender);
                else
                    insufficientPermissions(sender);
                break;
            case "spawnon":
                if (sender.hasPermission("mizu.command.spawnon"))
                    SpawnOn.spawnOn(sender, args[1], args[0]);
                else
                    insufficientPermissions(sender);
                break;
            case "firework":
                if (sender.hasPermission("mizu.command.firework")) {
                    if (sender instanceof Player)
                        Firework.giveIfPossible((Player) sender);
                } else {
                    insufficientPermissions(sender);
                }
                break;
            case "test-sentry":
                if (!sender.hasPermission("mizu.command.test-sentry")) {
                    insufficientPermissions(sender);
                    break;
                }
                try {
                    throw new Exception("test exception");
                } catch (Exception e) {
                    Sentry.captureException(e);
                }
                break;
            case "test-notifications":
                if (!sender.hasPermission("mizu.command.test-notifications")) {
                    insufficientPermissions(sender);
                    break;
                }
                NotificationClient.send(String.format("test notification from minecraft server '%s'", System.getenv("COMPUTERNAME")));
                break;
            case "gwen-walk":
                if (sender instanceof Player player) {
                    if (player.hasPermission("mizu.command.gwen-walk")) {
                        if (!GwenWalking.activated.contains(player.getUniqueId())) {
                            GwenWalking.Activate(player);
                        } else {
                            GwenWalking.Deactivate(player);
                        }
                    } else {
                        insufficientPermissions(sender);
                    }
                }
                break;
            case "gamble":
                if (sender instanceof Player player) {
                    if (player.hasPermission("mizu.command.gamble")) {
                        if (args.length <= 1) {
                            String[] missing = {"amountOfMaterial", "(material)/(entity)/levels/health"};
                            insufficientArguments(missing, sender);
                            return true;
                        }
                        int amount;
                        try {
                            amount = Integer.parseInt(args[0]);
                        } catch (NumberFormatException e) {
                            Sentry.addBreadcrumb(player.getName() + " supplied an invalid amount of items to gamble");
                            player.sendMessage(String.format("%s%ssupply a valid amount of items to gamble", Utils.prefix, ChatColor.RED));
                            return true;
                        }
                        if (experienceAliases.contains(args[1].toLowerCase())) {
                            // Casino.doubleOrNothingLevels(player, amount);
                            Casino.gamble(player, GambleType.Experience, amount);
                            return true;
                        }
                        List<String> entListNames = Arrays.stream(EntityType.values()).map(Enum::name).toList();
                        if (entListNames.contains(args[1].toUpperCase())) {
                            if (EntityBlacklist.contains(args[1].toLowerCase())) {
                                player.sendMessage(String.format("%s%sgambling the entity '%s' is disallowed", Utils.prefix, ChatColor.RED, args[1]));
                                logger.warning(String.format("%s tried gambling a disallowed entity: %s", player.getName(), args[1]));
                                return true;
                            }
                            EntityType entity;
                            try {
                                entity = Enum.valueOf(EntityType.class, args[1].toUpperCase());
                            } catch (IllegalArgumentException e) {
                                player.sendMessage(String.format("%s%san entity with the name '%s' doesn't exist", Utils.prefix, ChatColor.RED, args[1]));
                                Sentry.addBreadcrumb(String.format("%s tried to gamble entity '%s' which doesn't exist", player.getName(), args[1]));
                                return true;
                            }
                            // Casino.doubleOrNothingEntity(player, amount, entity);
                            Casino.gamble(player, GambleType.Entities, amount, new Object[] {entity});
                            return true;
                        }
                        if (healthAliases.contains(args[1].toLowerCase())) {
                            Casino.gamble(player, GambleType.Health, amount);
                            return true;
                        }
                        Material material;
                        try {
                            material = Enum.valueOf(Material.class, args[1].toUpperCase());
                        } catch (IllegalArgumentException e) {
                            player.sendMessage(String.format("%s%sa material with the name '%s' doesn't exist", Utils.prefix, ChatColor.RED, args[1]));
                            Sentry.addBreadcrumb(String.format("%s tried to gamble material '%s' which doesn't exist", player.getName(), args[1]));
                            return true;
                        }
                        // Casino.doubleOrNothing(player, amount, material);
                        Casino.gamble(player, GambleType.Items, amount, new Object[] {material});
                    } else {
                        insufficientPermissions(sender);
                    }
                }
                break;
            case "spawn-chunk-now":
                if (sender.hasPermission("mizu.spawn-chunk")) {
                    if (args.length > 0) {
                        Material material = Material.getMaterial(args[0].toUpperCase());
                        if (material == null) {
                            sender.sendMessage(Utils.prefix + ChatColor.RED + String.format("a material with the name '%s' does not exist", args[0]));
                            return false;
                        }
                        LuckyChunk.spawnChunk(material);
                        sender.sendMessage(String.format("%s%spassing in a custom material will bypass all constraints", Utils.prefix, ChatColor.YELLOW));
                        sender.sendMessage(String.format("%s%sthis includes any blacklists, block type constraints, and log constraints if the timber plugin is present", Utils.prefix, ChatColor.YELLOW));
                        sender.sendMessage(String.format("%s%sso use with caution. or don't, it's not that deep", Utils.prefix, ChatColor.YELLOW));
                    } else {
                        LuckyChunk.spawnChunk();
                    }
                } else {
                    insufficientPermissions(sender);
                }
                break;
            case "remove-chunk-now":
                if (sender.hasPermission("mizu.remove-chunk")) {
                    LuckyChunk.removeChunk();
                } else {
                    insufficientPermissions(sender);
                }
                break;
            case "lc":
                if (!Mizu.getInstance().getConfig().getBoolean("lucky-chunk")) {
                    sender.sendMessage(Component.text(Utils.prefix + ChatColor.RED + "lucky chunk is not enabled! (either its broken or someone whined about it being on)"));
                    return true;
                }
                if (LuckyChunk.getLastMaterial() == null) {
                    sender.sendMessage(Utils.prefix + ChatColor.GOLD + "no previous chunk");
                } else {
                    String lastMat = LuckyChunk.getLastMaterial().name().toLowerCase().replace("_", " ");
                    sender.sendMessage(Utils.prefix + ChatColor.GOLD + String.format("last chunk was %s", lastMat));
                }
                if (sender.hasPermission("mizu.command.lc")) {
                    if (LuckyChunk.isChunkSpawned()) {
                        sender.sendMessage(Utils.prefix + ChatColor.GOLD + "chunk is currently spawned!");
                        int minutes = (LuckyChunk.getTicksUntilRemoval() / 20) / 60;
                        int seconds = (LuckyChunk.getTicksUntilRemoval() / 20) % 60;
                        sender.sendMessage(Utils.prefix + ChatColor.RED + String.format("%dm %ds until removal", minutes, seconds));
                        return true;
                    }
                    int minutes = (LuckyChunk.getTicksUntilSpawn() / 20) / 60;
                    int seconds = (LuckyChunk.getTicksUntilSpawn() / 20) % 60;
                    sender.sendMessage(Utils.prefix + String.format("%dm %ds until next lucky chunk", minutes, seconds));
                } else {
                    insufficientPermissions(sender);
                }
                break;
            case "mizu-reload-config":
                if (sender.hasPermission("mizu.reload-config")) {
                    sender.sendMessage(Utils.prefix + "config reloading...");
                    plugin.reloadConfig();
                    sender.sendMessage(Utils.prefix + "done!");
                    LuckyChunk.readBlacklist();
                } else {
                    insufficientPermissions(sender);
                }
                break;
            case "fuck-you":
                if (sender.hasPermission("mizu.command.fuck-you")) {
                    if (args.length > 0) {
                        Player target = Bukkit.getPlayer(args[0]);
                        if (target != null) {
                            List<PotionEffect> effects = getPotionEffects();
                            for (PotionEffect effect : effects) {
                                target.addPotionEffect(effect);
                            }
                            target.sendTitlePart(TitlePart.TITLE, Component.text(ChatColor.RED + "fuck you"));
                        } else {
                            sender.sendMessage(String.format("%s%splayer not found '%s'", Utils.prefix, ChatColor.RED, args[0]));
                        }
                    } else {
                        insufficientArguments(new String[]{"playerName"}, sender);
                    }
                }
                break;
            case "warp":
                if (sender.hasPermission("mizu.warp")) {
                    if (args.length > 0) {
                        if (Objects.equals(args[0], "add")) {
                            if (args.length < 2) {
                                insufficientArguments(new String[]{"warp name"}, sender);
                                return true;
                            }
                            if (sender instanceof Player player) {
                                if (Objects.equals(args[1], "add") || Objects.equals(args[1], "list") || Objects.equals(args[1], "remove")) {
                                    player.sendMessage(Utils.prefix + ChatColor.RED + "these are reserved command words, call the warp something else");
                                    return true;
                                }
                                if (WarpManager.getInstance().addNewWarp(args[1], player)) {
                                    player.sendMessage(Utils.prefix + ChatColor.GREEN + "added warp " + args[1]);
                                } else {
                                    player.sendMessage(Utils.prefix + ChatColor.RED + "failed to add warp " + args[1]);
                                }
                            }
                        } else if (Objects.equals(args[0], "list")) {
                            List<String> warpLines = WarpManager.getInstance().listWarps(sender);
                            if (warpLines != null) {
                                sender.sendMessage(Utils.prefix + ChatColor.GREEN + "found " + warpLines.size() + " warps:");
                                for (String warp : warpLines) {
                                    sender.sendMessage(Utils.prefix + ChatColor.GOLD + warp.split(" ")[0] + " owned by " + warp.split(" ")[5]);
                                }
                            }
                        } else if (Objects.equals(args[0], "remove")) {
                            if (WarpManager.getInstance().removeWarp(args[1], sender)) {
                                sender.sendMessage(Utils.prefix + ChatColor.GREEN + "removed warp " + args[1]);
                            } else {
                                sender.sendMessage(Utils.prefix + ChatColor.RED + "failed to remove warp " + args[1]);
                            }
                        } else if (Objects.equals(args[0], "update")) {
                            // update just does the equivalent of remove then add with the same name
                            if (sender instanceof Player player) {
                                // use boolean AND so that the condition short circuits if either remove or add fails
                                if (WarpManager.getInstance().removeWarp(args[1], sender) &&
                                        WarpManager.getInstance().addNewWarp(args[1], player)) {
                                    player.sendMessage(Utils.prefix + ChatColor.GREEN + "updated warp " + args[1] + " to current position");
                                } else {
                                    player.sendMessage(Utils.prefix + ChatColor.RED + "failed to update warp " + args[1]);
                                }
                            }
                        } else {
                            if (sender instanceof Player player) {
                                Location warpTarget = WarpManager.getInstance().getWarpLocation(args[0]);
                                if (warpTarget == null) {
                                    player.sendMessage(Utils.prefix + ChatColor.RED + "warp failed (did you spell the name right?)");
                                    return true;
                                }
                                player.teleport(warpTarget);
                                player.sendMessage(Utils.prefix + ChatColor.GREEN + "warped to " + args[0] + "!");
                            }
                        }
                    } else {
                        insufficientArguments(new String[]{"action (list/add/warp name)", "warp name (for add)"}, sender);
                    }
                } else {
                    insufficientPermissions(sender);
                }
                break;
            case "stats":
                if (!sender.hasPermission("mizu.command.stats")) {
                    insufficientPermissions(sender);
                    return true;
                }
                if (sender instanceof Player player) {
                    Map.Entry<String, Integer> mostUsedCommand = StatisticManager.getInstance().getMostUsedCommandForPlayer(player);
                    player.sendMessage(String.format("%s%smost used command by you: /%s %d times!",
                            Utils.prefix,
                            ChatColor.LIGHT_PURPLE,
                            mostUsedCommand.getKey(),
                            mostUsedCommand.getValue()
                    ));
                }
                StatisticManager.getInstance().sendMostUsedCommandGlobally(sender);
                break;
        }
        return true;
    }

    private static @NotNull List<PotionEffect> getPotionEffects() {
        int duration = 20*5;
        // nausea, blindness, slowness, weakness, hunger, poison
        return List.of(
                new PotionEffect(PotionEffectType.NAUSEA, duration, 255),
                new PotionEffect(PotionEffectType.BLINDNESS, duration, 255),
                new PotionEffect(PotionEffectType.SLOWNESS, duration, 255),
                new PotionEffect(PotionEffectType.WEAKNESS, duration, 255),
                new PotionEffect(PotionEffectType.HUNGER, duration, 255),
                new PotionEffect(PotionEffectType.POISON, duration, 255),
                new PotionEffect(PotionEffectType.DARKNESS, duration, 255),
                new PotionEffect(PotionEffectType.GLOWING, duration, 255),
                new PotionEffect(PotionEffectType.INFESTED, duration, 255),
                new PotionEffect(PotionEffectType.WIND_CHARGED, duration, 255),
                new PotionEffect(PotionEffectType.OOZING, duration, 255),
                new PotionEffect(PotionEffectType.MINING_FATIGUE, duration ,255)
        );
    }

    private static void insufficientArguments(String[] missingArgs, CommandSender sender) {
        String message = "";
        message += "not enough arguments: <command> ";
        String missing = "";
        for (String arg : missingArgs) {
            missing += "{";
            missing += arg;
            missing += "} ";
        }
        message += missing;
        Sentry.addBreadcrumb(String.format("command sent with missing args: %s", missing));
        sender.sendMessage(Utils.prefix + ChatColor.RED + message);
    }

    private static void insufficientPermissions(CommandSender sender) {
        Sentry.addBreadcrumb("command sent with missing permission");
        sender.sendMessage(noPerms);
    }
}
 