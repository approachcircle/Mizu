package net.approachcircle.plugin.command;

import net.approachcircle.plugin.ColouredString;
import net.approachcircle.plugin.Localisation;
import net.approachcircle.plugin.data.Party;
import net.approachcircle.plugin.data.PartyStorage;
import net.approachcircle.plugin.data.Storage;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class PartyManagement extends MizuCommand {
    private PartyStorage partyStorage = Storage.of(PartyStorage.class);

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
                    try {
                        Party currentPartyForPlayer = Party.getPartyFor(player);
                        if (currentPartyForPlayer != null) {
                            sender.sendMessage(Localisation.rawPrefixComponent(new ColouredString(String.format("leave or disband your current party (%s) before making a new one", currentPartyForPlayer.name), NamedTextColor.RED)));
                            return true;
                        }
                        if (partyStorage.getOrElseNull((p) -> p.name.equalsIgnoreCase(name)) != null) {
                            sender.sendMessage(Localisation.rawPrefixComponent(new ColouredString(String.format("a party with the name '%s' already exists!", name), NamedTextColor.RED)));
                            return true;
                        }
                    } catch (IOException e) {
                        sender.sendMessage(Localisation.rawPrefixComponent(new ColouredString("failed to verify if you can create a new party", NamedTextColor.RED)));
                        return true;
                    }
                    NamedTextColor color;
                    try {
                        color = NamedTextColor.NAMES.valueOrThrow(args[2]);
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage(Localisation.rawPrefixComponent(new ColouredString(String.format("invalid colour: '%s'", args[2]), NamedTextColor.RED)));
                        return false;
                    }
                    Party party = Party.register(player, name, color);
                    try {
                        partyStorage.add(party);
                    } catch (IOException e) {
                        player.sendMessage(Localisation.rawPrefixComponent(new ColouredString(String.format("failed to save party: '%s' to the filesystem", party.name), NamedTextColor.RED)));
                        // try to disband the party if saving fails, if even THAT fails then meh what can you do
                        try {
                            party.disband();
                        } catch (IOException ignored) {}
                        return true;
                    }
                    player.sendMessage(Localisation.rawPrefixComponent(new ColouredString(String.format("created the party '%s'!", party.name), NamedTextColor.GREEN)));
                }
                case "disband" -> {
                    // only name is required
                    if (args.length < 2) {
                        return false;
                    }
                    String name = args[1];
                    Party party;
                    try {
                        party = partyStorage.getOrElseNull((p) -> p.name.equalsIgnoreCase(name));
                    } catch (IOException e) {
                        player.sendMessage(Localisation.rawPrefixComponent(new ColouredString(String.format("failed to retrieve party '%s'", name), NamedTextColor.RED)));
                        return true;
                    }
                    if (party == null) {
                        player.sendMessage(Localisation.rawPrefixComponent(new ColouredString(String.format("a party with the name '%s' does not exist!", name), NamedTextColor.RED)));
                        return true;
                    }
                    if (!party.leader.equals(player.getUniqueId())) {
                        player.sendMessage(Localisation.rawPrefixComponent(new ColouredString(String.format("only the party leader (%s) can disband the party!", Bukkit.getOfflinePlayer(party.leader).getName()), NamedTextColor.RED)));
                        return true;
                    }
                    try {
                        party.disband();
                    } catch (IOException e) {
                        player.sendMessage(Localisation.rawPrefixComponent(new ColouredString("failed to disband the party", NamedTextColor.RED)));
                        return true;
                    }
                    player.sendMessage(Localisation.rawPrefixComponent(new ColouredString("party disbanded!", NamedTextColor.RED)));
                }

            }
        }
        return true;
    }
}
