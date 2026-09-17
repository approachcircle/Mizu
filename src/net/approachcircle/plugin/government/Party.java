package net.approachcircle.plugin.government;


import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Party {
    public String name;
    public String color;
    private UUID leader;
    private List<UUID> members = new ArrayList<>();
    private Team underlyingTeam;

    private Party() {}


}
