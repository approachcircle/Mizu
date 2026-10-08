package net.approachcircle.plugin.data;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Party {
    public String name;
    public UUID leader;
    public NamedTextColor color;
    public List<UUID> members = new ArrayList<>();
    public Team underlyingTeam;

    /**
     * Registers a new party by creating and registering a new underlying Minecraft Team, and returning a new Party
     * object. This method does not commit the party to storage automatically, that must be done by accessing the
     * PartyStorage object.
     * @param leader the Player object who will be the leader of this party
     * @param name the name of the party
     * @param color the colour that represents this party
     * @return a Party object with all fields generated
     */
    public static Party register(Player leader, String name, NamedTextColor color) {
        Party party = new Party();
        party.name = name;
        party.leader = leader.getUniqueId();
        party.color = color;
        party.members.add(leader.getUniqueId());
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.registerNewTeam(party.name);
        team.color(color);
        team.displayName(Component.text(name));
        team.addPlayer(leader);
        return party;
    }

    /**
     * Disbands this party by unregistering the Minecraft Team, AND removing it from storage.
     * @throws IOException if an error occurs removing the party from storage.
     */
    public void disband() throws IOException {
        // the team must always be deregistered AFTER deserializing a party instance because a registered team with
        // the same name must exist for the deserialization to actually work......................................
        Storage.of(PartyStorage.class).remove((p) -> p.name.equals(name));
        underlyingTeam.unregister();
    }

    public static @Nullable Party getPartyFor(Player player) throws IOException {
        return Storage.of(PartyStorage.class).getOrElseNull(p -> p.members.contains(player.getUniqueId()));
    }
}
