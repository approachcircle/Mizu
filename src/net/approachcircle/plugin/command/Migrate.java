package net.approachcircle.plugin.command;

import net.approachcircle.plugin.*;
import net.approachcircle.plugin.data.Storage;
import net.approachcircle.plugin.data.ToBeMigratedFile;
import net.approachcircle.plugin.data.WarpStorage;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class Migrate extends MizuCommand {
    private static final String migrationNotification =
            """
            since you last joined the server, we have "migrated" to a new area and started again. this means everyone's
            inventories have been reset, along with all warps. you have been teleported to the new area.""";

    private static final String commandWarning =
            """
            are you ABSOLUTELY SURE you want to run this command? this will reset ALL PLAYERS' INVENTORIES, as well
            as warps, advancements and other things to essentially RESET THE SERVER ENTIRELY... if you're sure that's
            what you want, type "/migrate confirm".""";

    @Override
    protected boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (sender instanceof Player player) {
            if (args.length == 0) {
                sender.sendMessage(Localisation.rawPrefixComponent(new ColouredString(commandWarning, NamedTextColor.DARK_RED)));
                return true;
            } else if (args.length == 1) {
                if (!args[0].equals("confirm"))
                {
                    sender.sendMessage(Localisation.rawPrefixComponent(new ColouredString("bro how did you spell it wrong", NamedTextColor.RED)));
                    return true;
                }
            }
            if (!player.getWorld().getName().equalsIgnoreCase("world")) {
                player.sendMessage(Localisation.rawPrefixComponent(new ColouredString("yeah let's not do that (only run the migration command in the overworld)", NamedTextColor.RED)));
                return true;
            }
            player.sendMessage(Localisation.rawPrefixComponent(new ColouredString("migration in progress!", NamedTextColor.YELLOW)));
            if (!markOfflinePlayersToBeMigrated()) {
                player.sendMessage(Localisation.rawPrefixComponent(new ColouredString("migration cancelled!", NamedTextColor.RED)));
                return true;
            }
            clearWarps();
            clearStatistics();
            Bukkit.getWorlds().getFirst().setSpawnLocation(player.getLocation());
            for (Player p : Bukkit.getOnlinePlayers()) {
                migratePlayer(p);
            }
            // reset the world's time back to the beginning
            Bukkit.getWorlds().getFirst().setFullTime(0L);
            player.sendMessage(Localisation.rawPrefixComponent(new ColouredString("migration complete!", NamedTextColor.GREEN)));
        }
        return true;
    }

    public static void migratePlayer(Player player) {
        Bukkit.getOnlinePlayers().forEach(p -> {
            if (p.isOp())
                p.sendMessage(Localisation.rawPrefixComponent(new ColouredString(String.format("migrating %s...", player.getName()), NamedTextColor.GREEN)));
        });
        player.getInventory().clear();
        clearAdvancements(player);
        player.teleport(Bukkit.getWorlds().getFirst().getSpawnLocation());
        // forgot that there are actual Minecraft statistics, so let's also reset those.
        // this is disgustingly expensive but i can't think of a better way, and who said migrations were supposed
        // to be efficient anyway type shi
        for (Statistic statistic : Statistic.values()) {
            if (statistic.getType() == Statistic.Type.BLOCK) {
                for (Material material : Material.values()) {
                    if (!material.isBlock()) continue;
                    player.setStatistic(statistic, material, 0);
                }
            } else if (statistic.getType() == Statistic.Type.ITEM) {
                for (Material material : Material.values()) {
                    if (!material.isItem()) continue;
                    player.setStatistic(statistic, material, 0);
                }
            } else if (statistic.getType() == Statistic.Type.ENTITY) {
                for (EntityType entityType : EntityType.values()) {
                    if (entityType == EntityType.UNKNOWN) continue;
                    player.setStatistic(statistic, entityType, 0);
                }
            } else if (statistic.getType() == Statistic.Type.UNTYPED) {
                player.setStatistic(statistic, 0);
            }
        }
        // force the player to now spawn here even if their spawn point was a bed in the old area
        player.setRespawnLocation(Bukkit.getWorlds().getFirst().getSpawnLocation(), true);
        Localisation.sendMessage(player, "new_home");
        Bukkit.getOnlinePlayers().forEach(p -> {
            if (p.isOp())
                p.sendMessage(Localisation.rawPrefixComponent(new ColouredString(String.format("fully migrated %s!", player.getName()), NamedTextColor.GREEN)));
        });
    }

    private static void clearAdvancements(Player player) {
        Iterator<Advancement> iterator = Bukkit.getServer().advancementIterator();
        while (iterator.hasNext())
        {
            AdvancementProgress progress = player.getAdvancementProgress(iterator.next());
            for (String criteria : progress.getAwardedCriteria())
                progress.revokeCriteria(criteria);
        }
    }

    private boolean markOfflinePlayersToBeMigrated() {
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (!offlinePlayer.isOnline()) {
                try {
                    Storage.of(ToBeMigratedFile.class).add(offlinePlayer.getUniqueId().toString());
                } catch (IOException e) {
                    Utils.error(e, String.format("failed to write %s to the 'to be migrated' file", offlinePlayer.getName()));
                    // if one fails then we can short circuit and stop the migration
                    return false;
                }
            }
        }
        return true;
    }

    private void clearWarps() {
        try {
            Storage.of(WarpStorage.class).clear();
        } catch (IOException e) {
            Utils.error(e, "failed to clear warps");
            return;
        }
        PluginLogger.getPluginLogger().info("warps cleared!");
    }

    private void clearStatistics() {
        if (!StatisticManager.getInstance().clearStatistics()) {
            PluginLogger.getPluginLogger().warning("statistics will have to be cleared manually");
        } else {
            PluginLogger.getPluginLogger().info("statistics cleared!");
        }
    }

    public static void sendMigrationNotification(Player player) {
        player.sendMessage(Localisation.rawPrefixComponent(new ColouredString(migrationNotification, NamedTextColor.DARK_AQUA)));
    }

    // we DO NOT want to write a tab completion for this with the "confirm" argument, because we don't want that to
    // be accidentally tabbed in and have someone execute the command without realising
    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
