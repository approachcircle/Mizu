package net.approachcircle.plugin.data;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Party {
    public String name;
    public UUID leader;
    public NamedTextColor color;
    public List<UUID> members = new ArrayList<>();
    public Team underlyingTeam;

    public static Party build(Player leader, String name, NamedTextColor color) {
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
}
