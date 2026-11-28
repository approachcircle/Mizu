package net.approachcircle.plugin.command;

import net.approachcircle.plugin.ColouredString;
import net.approachcircle.plugin.Localisation;
import net.approachcircle.plugin.data.Party;
import net.approachcircle.plugin.data.PartyStorage;
import net.approachcircle.plugin.data.Storage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class PartyManagement extends MizuCommand {
    private final PartyStorage partyStorage = Storage.of(PartyStorage.class);

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
                    } catch (NoSuchElementException e) {
                        sender.sendMessage(Localisation.rawPrefixComponent(new ColouredString(String.format("invalid colour: '%s'", args[2]), NamedTextColor.RED)));
                        return false;
                    }
                    Party party = Party.registerInMemory(player.getUniqueId(), name, color);
                    try {
                        partyStorage.add(party);
                    } catch (IOException e) {
                        player.sendMessage(Localisation.rawPrefixComponent(new ColouredString(String.format("failed to save party: '%s' to the filesystem", party.name), NamedTextColor.RED)));
                        // try to disband the party if saving fails, if even THAT fails then meh what can you do
                        try {
                            party.disband();
                            player.sendMessage(Localisation.rawPrefixComponent(new ColouredString("this party has been disbanded (deleted), please try creating one again", NamedTextColor.RED)));
                        } catch (IOException ignored) {} // etoooo..... blehhhhhhhhh >,..,<
                        return true;
                    }
                    player.sendMessage(Localisation.rawPrefixComponent(new ColouredString(String.format("created the party '%s'!", party.name), NamedTextColor.GREEN)));
                }
                case "disband" -> {
                    // only name is required
                    if (args.length < 2) {
                        return false;
                    }
                    // i mean we don't really need the name as a required argument here? like most of the time i think
                    // people will only need to disband their own party? but i guess for operators it might be useful
                    // to be able to disband other parties for management purposes
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
                    if (!party.leader.equals(player.getUniqueId()) && !player.isOp()) {
                        player.sendMessage(Localisation.rawPrefixComponent(new ColouredString(String.format("only the party leader (%s) can disband the party!", Bukkit.getOfflinePlayer(party.leader).getName()), NamedTextColor.RED)));
                        return true;
                    }
                    try {
                        // TODO: when disbanding a party, we need to ensure that that party is "removed from parliament"
                        // TODO: i.e. removed from the qualified parties/removed from power, etc etc.
                        // TODO: alternatively, we could make it so that after signing up, parties cannot disband immediately
                        // TODO: and they either have to resign if in power, disqualify themselves, or remove themselves
                        // TODO: from the signups list.
                        party.disband();
                    } catch (IOException e) {
                        player.sendMessage(Localisation.rawPrefixComponent(new ColouredString("failed to disband the party", NamedTextColor.RED)));
                        return true;
                    }
                }
                case "rename" -> {
                    player.sendMessage(Localisation.rawPrefixComponent(new ColouredString("if you want to rename your party, you'll have to disband it and create a new one... sorry blehhhhhhhhhh!", NamedTextColor.RED)));
                    return true;
                }
                case "list" -> {
                    List<Party> parties;
                    try {
                        parties = partyStorage.list();
                    } catch (IOException e) {
                        player.sendMessage(Localisation.rawPrefixComponent(new ColouredString("failed to list the political parties :(", NamedTextColor.RED)));
                        return true;
                    }
                    player.sendMessage(Localisation.rawPrefixComponent(new ColouredString("here is a list of the parties:")));
                    for (Party party : parties) {
                        TextComponent component = Localisation.mizuPrefix.append(Component.text(party.name).color(party.color))
                                .append(Component.text(" owned by ").color(NamedTextColor.WHITE))
                                .append(Component.text(Objects.requireNonNull(Bukkit.getOfflinePlayer(party.leader).getName())).color(party.color)
                                .append(Component.text(" with ").color(NamedTextColor.WHITE))
                                .append(Component.text(party.members.size()).color(NamedTextColor.GREEN))
                                .append(Component.text(" total member(s): ").color(NamedTextColor.WHITE))
                        );
                        for (int i = 0; i < party.members.size(); i++) {
                            component = component.append(Component.text(Objects.requireNonNull(Bukkit.getOfflinePlayer(party.members.get(i)).getName())).color(party.color));
                            if (i == party.members.size() - 1) {
                                component = component.append(Component.text(".").color(NamedTextColor.WHITE));
                            } else {
                                component = component.append(Component.text(", ").color(NamedTextColor.WHITE));
                            }
                        }
                        player.sendMessage(component);
                    }
                }
                case "transfer" -> {
                    // name of party, then player to transfer ownership to
                    if (args.length < 3) {
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
                    // TODO: yeah we can squish all this validation into a method at some point so i don't have to copy paste
                    if (party == null) {
                        player.sendMessage(Localisation.rawPrefixComponent(new ColouredString(String.format("a party with the name '%s' does not exist!", name), NamedTextColor.RED)));
                        return true;
                    }
                    if (!party.leader.equals(player.getUniqueId()) && !player.isOp()) {
                        player.sendMessage(Localisation.rawPrefixComponent(new ColouredString(String.format("only the party leader (%s) can transfer ownership!", Bukkit.getOfflinePlayer(party.leader).getName()), NamedTextColor.RED)));
                        return true;
                    }
                    // i think we just need to change party.leader
                    // then call replace()? that would update the party leader on the disk, which is the only place the
                    // leader is actually stored, and we don't even need to touch the team.

                    // for renaming parties, it'll have to be a bit different. we can't just call replace because then
                    // the team name and party name will be out of sync... maybe we should've used an internal ID...
                    // i don't actually see a way to call replace at all? because grabbing the party from the disk is
                    // guaranteed to fail if the team and party names are out of sync... fuck sake. maybe we'll have to
                    // just fully disband the party and make a new one which is the exact fucking thing i was trying to
                    // fucking fix by making the fucking replace method for FUCK FUCK FUCK FUCK FUCK SAKE FUCK YOU FUCK YOU FUCK YOU FUCK YOU

                    // TODO: add a command to display the team names (IDs) and their corresponding parties for management
                }
            }
        }
        return true;
    }
}
