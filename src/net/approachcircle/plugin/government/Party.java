package net.approachcircle.plugin.government;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Party {
    public String name;
    public String color;
    public UUID leader;
    public List<UUID> members =  new ArrayList<>();

    public Party() {}

    public Party(String name, String color, UUID leader) {
        this.name = name;
        this.color = color;
        this.leader = leader;
    }

    public Party(String name, String color, UUID leader, List<UUID> members) {
        this.name = name;
        this.color = color;
        this.leader = leader;
        this.members = members;
    }
}
