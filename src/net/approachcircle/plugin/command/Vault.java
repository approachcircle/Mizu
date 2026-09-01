package net.approachcircle.plugin.command;

import net.approachcircle.plugin.ColouredString;
import net.approachcircle.plugin.KeyArgPair;
import net.approachcircle.plugin.Localisation;
import net.approachcircle.plugin.VaultManager;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class Vault extends MizuCommand {
    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (sender instanceof Player player) {
            // grab the UUID of the player opening the vault themselves
            UUID target = player.getUniqueId();
            if (args.length > 0) {
                if (!player.isOp()) {
                    player.sendMessage(Localisation.unlocalisedPrefixComponent(new ColouredString("you do not have permission to open other another player's vault!", NamedTextColor.RED)));
                    return true;
                }
                // if we're given a player's name, grab THEIR UUID, even if they're offline
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
                if (!offlinePlayer.hasPlayedBefore()) {
                    player.sendMessage(Localisation.unlocalisedPrefixComponent(new ColouredString("this player has never joined the server before!", NamedTextColor.RED)));
                    return true;
                }
                target = offlinePlayer.getUniqueId();

            }
            Inventory vault = VaultManager.getInstance().getVault(player, target);
            if (vault != null) {
                player.openInventory(vault);
                if (player.getUniqueId().equals(target)) {
                    player.sendMessage(Localisation.unlocalisedPrefixComponent(new ColouredString("now viewing your vault!", NamedTextColor.GREEN)));
                } else {
                    player.sendMessage(Localisation.unlocalisedPrefixComponent(new ColouredString(String.format("viewing the vault of: %s", Bukkit.getOfflinePlayer(args[0]).getName()), NamedTextColor.YELLOW)));
                }
                // viewer : owner
                VaultManager.getInstance().vaultsOpen.put(player.getUniqueId(), target);
            } else {
                Localisation.sendMessage(sender, new KeyArgPair("failed_to_open_vault"));
            }
        }
        return true;
    }

    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (sender.isOp()) {
            if (args.length == 1) {
                return copyMatchesAndSort(args[0], getPlayerNames());
            }
        }
        return List.of();
    }
}
