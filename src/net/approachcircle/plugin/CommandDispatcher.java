package net.approachcircle.plugin;

import io.sentry.Sentry;
import net.approachcircle.plugin.government.Party;
import net.approachcircle.plugin.government.PartyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

public class CommandDispatcher {
    private static final Component noPerms = Localisation.unlocalisedPrefixComponent(new ColouredString("sorry, you do not have the permission to run this command", NamedTextColor.RED));
    private static final Logger logger = PluginLogger.getPluginLogger();
    private static final List<String> experienceAliases = List.of(new String[] {"exp", "xp", "levels", "experience", "level", "eckspee"});
    private static final List<String> healthAliases = List.of(new String[] {"health", "hp", "life", "hearts"});
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
            "horse",
            "parrot",
            "cat"
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
                        TeleportAsk.answerTeleport(player, TeleportResponse.Accept);
                    } else {
                        insufficientPermissions(sender);
                    }
                }
                break;
            case "tpn":
                if (sender instanceof Player player) {
                    if (sender.hasPermission("mizu.command.tpa")) {
                        TeleportAsk.answerTeleport(player, TeleportResponse.Deny);
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
                            Localisation.sendMessage(player, new KeyArgPair("invalid_gamble_number"));
                            return true;
                        }
                        if (experienceAliases.contains(args[1].toLowerCase())) {
                            // Casino.doubleOrNothingLevels(player, amount);
                            Casino.gamble(player, GambleType.Experience, amount);
                            return true;
                        }
                        List<String> entListNames = Arrays.stream(EntityType.values()).map(Enum::name).toList();
                        // add check to account for entities that are also materials
                        // favour materials
                        if (entListNames.contains(args[1].toUpperCase()) && Material.matchMaterial(args[1].toUpperCase()) == null) {
                            if (EntityBlacklist.contains(args[1].toLowerCase())) {
                                Localisation.sendMessage(player, new KeyArgPair("cannot_gamble_entity", args[1]));
                                logger.warning(String.format("%s tried gambling a disallowed entity: %s", player.getName(), args[1]));
                                return true;
                            }
                            EntityType entity;
                            try {
                                entity = Enum.valueOf(EntityType.class, args[1].toUpperCase());
                            } catch (IllegalArgumentException e) {
                                Localisation.sendMessage(player, new KeyArgPair("entity_does_not_exist", args[1]));
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
                            Localisation.sendMessage(player, new KeyArgPair("material_does_not_exist", args[1]));
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
                            Localisation.sendMessage(sender, new KeyArgPair("material_does_not_exist", args[1]));
                            return false;
                        }
                        LuckyChunk.spawnChunk(material);
                        Localisation.sendMessage(sender, new KeyArgPair("custom_lucky_chunk_material_warning_1"));
                        Localisation.sendMessage(sender, new KeyArgPair("custom_lucky_chunk_material_warning_2"));
                        Localisation.sendMessage(sender, new KeyArgPair("custom_lucky_chunk_material_warning_3"));
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
                    Localisation.sendMessage(sender, new KeyArgPair("lucky_chunk_not_enabled"));
                    return true;
                }
                if (LuckyChunk.getLastMaterial() == null) {
                    Localisation.sendMessage(sender, new KeyArgPair("no_previous_chunk"));
                } else {
                    String lastMat = LuckyChunk.getLastMaterial().name().toLowerCase().replace("_", " ");
                    Localisation.sendMessage(sender, new KeyArgPair("last_chunk_was", lastMat));
                }
                if (sender.hasPermission("mizu.command.lc")) {
                    if (LuckyChunk.isChunkSpawned()) {
                        Localisation.sendMessage(sender, new KeyArgPair("chunk_currently_spawned"));
                        int minutes = (LuckyChunk.getTicksUntilRemoval() / 20) / 60;
                        int seconds = (LuckyChunk.getTicksUntilRemoval() / 20) % 60;
                        Localisation.sendMessage(sender, new KeyArgPair("time_until_lucky_chunk_removal", minutes, seconds));
                        return true;
                    }
                    int minutes = (LuckyChunk.getTicksUntilSpawn() / 20) / 60;
                    int seconds = (LuckyChunk.getTicksUntilSpawn() / 20) % 60;
                    Localisation.sendMessage(sender, new KeyArgPair("time_until_lucky_chunk_spawn", minutes, seconds));
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
                            target.sendTitlePart(TitlePart.TITLE, Component.text("fuck you", NamedTextColor.RED));
                        } else {
                            Localisation.sendMessage(sender, new KeyArgPair("player_not_found", args[0]));
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
                                if (Arrays.stream(WarpManager.reservedCommands).anyMatch((reserved) -> reserved.equalsIgnoreCase(args[1]))) {
                                    Localisation.sendMessage(sender, new KeyArgPair("must_call_warp_something_else"));
                                    return true;
                                }
                                if (WarpManager.getInstance().addNewWarp(args[1], player)) {
                                    Localisation.sendMessage(sender, new KeyArgPair("added_warp", args[1]));
                                } else {
                                    Localisation.sendMessage(sender, new KeyArgPair("failed_to_add_warp", args[1]));
                                }
                            }
                        } else if (Objects.equals(args[0], "list")) {
                            List<String> warpLines = WarpManager.getInstance().listWarps(sender);
                            if (warpLines != null) {
                                Localisation.sendMessage(sender, new KeyArgPair("found_number_warps", warpLines.size()));
                                for (String warp : warpLines) {
                                    Localisation.sendMessage(sender, new KeyArgPair("warp_owned_by_player", warp.split(" ")[0], warp.split(" ")[5]));
                                }
                            }
                        } else if (Objects.equals(args[0], "remove")) {
                            if (WarpManager.getInstance().removeWarp(args[1], sender)) {
                                Localisation.sendMessage(sender, new KeyArgPair("removed_warp", args[1]));
                            } else {
                                Localisation.sendMessage(sender, new KeyArgPair("failed_to_remove_warp", args[1]));
                            }
                        } else if (Objects.equals(args[0], "update")) {
                            // update just does the equivalent of remove then add with the same name
                            if (sender instanceof Player player) {
                                if (args.length < 2) {
                                    insufficientArguments(new String[]{"warp name to update"}, player);
                                    //TODO: for QoL, can make this update the closest warp to the player within 1/2 blocks?
                                    // might be lowkey kinda useless but y'know
                                    return true;
                                }
                                String owner = WarpManager.getInstance().getOwner(args[1]);
                                if (owner == null) {
                                    Localisation.sendMessage(sender, new KeyArgPair("failed_to_get_warp_owner"));
                                    return true;
                                }
                                // use boolean AND so that the condition short circuits if either remove or add fails
                                if (WarpManager.getInstance().removeWarp(args[1], sender) &&
                                        WarpManager.getInstance().addNewWarp(args[1], player, owner)) {
                                    Localisation.sendMessage(sender, new KeyArgPair("updated_warp_position", args[1]));
                                } else {
                                    Localisation.sendMessage(sender, new KeyArgPair("failed_to_update_warp", args[1]));
                                }
                            }
                        } else if (Objects.equals(args[0], "rename")) {
                            if (args.length < 3) {
                                insufficientArguments(new String[]{"old warp name and new warp name"}, sender);
                                return true;
                            }
                            String owner = WarpManager.getInstance().getOwner(args[1]);
                            if (owner == null) {
                                Localisation.sendMessage(sender, new KeyArgPair("failed_to_rename_owner_not_found"));
                                return true;
                            }
                            String[] oldWarpTokens = WarpManager.getInstance().getWarpTokens(args[1]);
                            if (oldWarpTokens == null) {
                                Localisation.sendMessage(sender, new KeyArgPair("failed_to_rename_parsing_failed"));
                                return true;
                            }
                            Location reusedLocation = null;
                            try {
                                reusedLocation = new Location(
                                        Bukkit.getWorld(oldWarpTokens[4]),
                                        Double.parseDouble(oldWarpTokens[1]),
                                        Double.parseDouble(oldWarpTokens[2]),
                                        Double.parseDouble(oldWarpTokens[3]),
                                        Float.parseFloat(oldWarpTokens[6]),
                                        0
                                );
                            } catch (NumberFormatException e) {
                                logger.severe("failed to parse a coordinate for warp: " + args[1] + "(it may be corrupted?)");
                                Localisation.sendMessage(sender, new KeyArgPair("failed_to_parse_warp_coords", args[1]));
                                return true;
                            } catch (ArrayIndexOutOfBoundsException e) {
                                logger.severe("failed to find coordinates for warp: " + args[1]);
                                Localisation.sendMessage(sender, new KeyArgPair("failed_to_find_warp_coords", args[1]));
                            }
                            if (WarpManager.getInstance().getWarpTokens(args[2]) != null) {
                                Localisation.sendMessage(sender, new KeyArgPair("warp_name_taken", args[2]));
                                return true;
                            }
                            if (WarpManager.getInstance().removeWarp(args[1], sender) &&
                                    WarpManager.getInstance().addNewWarpRaw(args[2], reusedLocation, owner)) {
                                Localisation.sendMessage(sender, new KeyArgPair("renamed_warp", args[1], args[2]));
                            } else {
                                Localisation.sendMessage(sender, new KeyArgPair("failed_to_rename", args[1]));
                            }
                        } else {
                            if (sender instanceof Player player) {
                                Location warpTarget = WarpManager.getInstance().getWarpLocation(args[0], player);
                                if (warpTarget == null) {
                                    Localisation.sendMessage(sender, new KeyArgPair("warp_failed"));
                                    return true;
                                }
                                player.teleport(warpTarget);
                                Localisation.sendMessage(sender, new KeyArgPair("warped_success", args[0]));
                            }
                        }
                    } else {
                        insufficientArguments(new String[]{"action (list/add/remove/update/rename/warp name)", "warp name (for add)"}, sender);
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
                    Localisation.sendMessage(sender, new KeyArgPair("most_used_command_personally",
                             mostUsedCommand.getKey(),
                             mostUsedCommand.getValue())
                    );
                }
                StatisticManager.getInstance().sendMostUsedCommandGlobally(sender);
                break;
            case "lctp":
                if (!sender.hasPermission("mizu.command.teleport-chunk")) {
                    insufficientPermissions(sender);
                    return true;
                }
                if (sender instanceof Player player) {
                    LuckyChunk.teleportToChunk(player);
                }
                break;
            case "vault":
                if (!sender.hasPermission("mizu.command.vault")) {
                    insufficientPermissions(sender);
                    return true;
                }
                if (sender instanceof Player player) {
                    Inventory vault = VaultManager.getInstance().getVault(player);
                    if (vault != null) {
                        player.openInventory(vault);
                        VaultManager.currentlyViewingVault.add(player.getUniqueId());
                    } else {
                        Localisation.sendMessage(sender, new KeyArgPair("failed_to_open_vault"));
                    }
                }
                break;
            case "party":
                if (!sender.hasPermission("mizu.command.party")) {
                    insufficientPermissions(sender);
                    return true;
                }
                if (sender instanceof Player player) {
                    if (args.length < 1) {
                        insufficientArguments(new String[]{"create", "view", "add", "remove", "delete", "rename", "changecolour"}, sender);
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("create")) {
                        if (args.length < 3) {
                            insufficientArguments(new String[]{"name", "colour"}, sender);
                            return true;
                        }
                        List<Party> parties = PartyManager.getInstance().getParties();
                        if (parties == null) {
                            Localisation.sendMessage(player, "error_occurred_getting_parties");
                            return true;
                        }
                        for (Party party : parties) {
                            if (party.name.equalsIgnoreCase(args[1])) {
                                Localisation.sendMessage(player, new KeyArgPair("party_name_taken", args[1]));
                                return true;
                            }
                        }
                        NamedTextColor teamColor = NamedTextColor.NAMES.value(args[2]);
                        if (teamColor == null) {
                            Localisation.sendMessage(player, new KeyArgPair("not_a_valid_colour", args[2]));
                            return true;
                        }
                        if (!PartyManager.getInstance().createParty(args[1], args[2], player.getUniqueId())) {
                            Localisation.sendMessage(player, new KeyArgPair("error_occurred_creating_party", args[1]));
                            return true;
                        }
                        Localisation.sendMessage(player, new KeyArgPair("party_created", args[1]));
                        Localisation.sendMessage(player, "auto_appointed_leader");
                        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
                        Team team = scoreboard.registerNewTeam(args[1]);
                        team.color(teamColor);
                        team.addEntry(player.getName());
                    }
                }
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
                new PotionEffect(PotionEffectType.MINING_FATIGUE, duration,255),
                // 255 is too much
                // 30 kills with max feather falling
                new PotionEffect(PotionEffectType.LEVITATION, duration, 30)
        );
    }

    private static void insufficientArguments(String[] missingArgs, CommandSender sender) {
        String message = "not enough arguments: <command> ";
        StringBuilder missing = new StringBuilder();
        for (String arg : missingArgs) {
            missing.append("{");
            missing.append(arg);
            missing.append("} ");
        }
        message += missing;
        sender.sendMessage(Localisation.unlocalisedPrefixComponent(new ColouredString(message, NamedTextColor.RED)));
    }

    private static void insufficientPermissions(CommandSender sender) {
        sender.sendMessage(noPerms);
    }
}
 