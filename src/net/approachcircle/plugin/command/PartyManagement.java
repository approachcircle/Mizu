package net.approachcircle.plugin.command;

import net.approachcircle.plugin.ColouredString;
import net.approachcircle.plugin.Localisation;
import net.approachcircle.plugin.data.Party;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PartyManagement extends MizuCommand {
    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (sender instanceof Player player) {
            if (args.length < 1) {
                return false;
            }
            switch (args[0]) {
                case "create" -> {
                    // need name and colour so 3 including "create"
                    if (args.length < 3) {
                        return false;
                    }
                    String name = args[1];
                    NamedTextColor color;
                    try {
                        color = NamedTextColor.NAMES.valueOrThrow(args[2]);
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage(Localisation.rawPrefixComponent(new ColouredString(String.format("invalid colour: '%s'", args[2]), NamedTextColor.RED)));
                        return false;
                    }
                    Party party = Party.build(player, name, color);
                }
            }
        }
        return true;
    }
}
