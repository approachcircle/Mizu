package net.approachcircle.plugin.data;

import net.approachcircle.plugin.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.UUID;

public class PartyStorage extends FlatFileStorage<Party> {
    protected PartyStorage() {
        super("parties.txt");
    }

    @Override
    protected String serialize(Party entry) {
        // we only need to store the name and leader as everything else about the party is stored in the underlying
        // team anyway
        return String.format(
                "%s %s",
                entry.name,
                entry.leader.toString()
        );
    }

    @Override
    protected Party deserialize(String line) {
        Party party = new Party();
        String[] tokens = line.split(" ");
        party.name = tokens[0];
        try {
            party.leader = UUID.fromString(tokens[1]);
        } catch (IllegalArgumentException e) {
            throw new DeserializationException(String.format("invalid UUID for party leader on party: '%s'", party.name));
        }
        // bro actually fuck off, don't bug me to put this code into a separate function just because i wrote a long
        // multiline comment. actual brain-dead IDE
        //noinspection ExtractMethodRecommender
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team underlyingTeam = scoreboard.getTeam(party.name);
        if (underlyingTeam == null)
        {
            // it would be great if we could just create a new team here if it doesn't exist, but the point is that
            // we're gonna be grabbing all of the party metadata (members, colours, etc.) from the underlying team
            // so we can't reconstruct it without knowing that info beforehand, and i don't really want to store it
            // cause then we'd have to keep it synced with the underlying team's metadata.
            throw new DeserializationException(String.format("there is no underlying team for the party: '%s'", party.name));
        }
        party.color = (net.kyori.adventure.text.format.NamedTextColor) underlyingTeam.color();
        party.members = new ArrayList<>();
        for (String playerName : underlyingTeam.getEntries()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            if (offlinePlayer.hasPlayedBefore()) {
                party.members.add(offlinePlayer.getUniqueId());
            } else {
                PluginLogger.getPluginLogger().warning(
                        String.format(
                                "underlying team '%s' contains an entry of a player who has never played before: '%s'",
                                party.name,
                                playerName
                        )
                );
            }
        }
        party.underlyingTeam = underlyingTeam;
        return party;
    }
}
