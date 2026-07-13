package net.approachcircle.plugin.commands;

import net.approachcircle.plugin.KeyArgPair;
import net.approachcircle.plugin.Localisation;
import net.approachcircle.plugin.VaultManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class Vault extends MizuCommand {
    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (sender instanceof Player player) {
            Inventory vault = VaultManager.getInstance().getVault(player);
            if (vault != null) {
                player.openInventory(vault);
                VaultManager.currentlyViewingVault.add(player.getUniqueId());
            } else {
                Localisation.sendMessage(sender, new KeyArgPair("failed_to_open_vault"));
            }
        }
        return true;
    }
}
