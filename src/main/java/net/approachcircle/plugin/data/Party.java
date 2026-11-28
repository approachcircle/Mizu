package net.approachcircle.plugin.data;


import net.approachcircle.plugin.ColouredString;
import net.approachcircle.plugin.Localisation;
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
import java.util.UUID;

public class Party {
    public String name;
    public UUID leader;
    public NamedTextColor color;
    public List<UUID> members = new ArrayList<>();
    public Team underlyingTeam;

    /**
     * Registers a new party by creating and registering a new underlying Minecraft Team, and returning a new Party
     * object. This method does NOT commit the party to storage automatically, that must be done by accessing the
     * PartyStorage object.
     * @param leader the Player object who will be the leader of this party
     * @param name the name of the party
     * @param color the colour that represents this party
     * @return a Party object with all fields generated
     */
    public static Party registerInMemory(UUID leader, String name, NamedTextColor color) {
        Party party = new Party();
        party.name = name;
        party.leader = leader;
        party.color = color;
        party.members.add(leader);
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.registerNewTeam(party.name);
        team.color(color);
        team.displayName(Component.text(name));
        team.displayName();
        team.addPlayer(Bukkit.getOfflinePlayer(leader));
        // TODO: the more i think about it, the less i like this only registering in memory rather than being committed
        // TODO: to storage... things will break if we disband this party or join members to it before committing it
        // TODO: to storage, so even if it feels weird that a party is registering itself, i think it's for the best
        // TODO: considering we're already being forced to use a weird decoupled paradigm anyway...
        return party;
    }

    /**
     * Disbands this party by unregistering the Minecraft Team, AND removing it from storage.
     * @throws IOException if an error occurs removing the party from storage.
     */
    public void disband(boolean silent) throws IOException {
        // the team must always be deregistered AFTER deserializing a party instance because a registered team with
        // the same name must exist for the deserialization to actually work......................................
        Storage.of(PartyStorage.class).remove((p) -> p.name.equals(name));
        underlyingTeam.unregister();
        if (silent) return;
        for (UUID memberUUID : members) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null) {
                if (memberUUID.equals(leader)) {
                    // member we're looking at is the leader
                    member.sendMessage(Localisation.rawPrefixComponent(new ColouredString("party disbanded!", NamedTextColor.RED)));
                } else {
                    // member is somebody else
                    member.sendMessage(Localisation.rawPrefixComponent(new ColouredString(String.format("your party has been disbanded either by the party leader %s or an operator (%s)", Bukkit.getOfflinePlayer(leader).getName(), name), NamedTextColor.RED)));
                }
            }
        }
    }

    public void disband() throws IOException {
        disband(false);
    }

    public void joinPlayer(UUID player) {
        members.add(player);
        underlyingTeam.addPlayer(Bukkit.getOfflinePlayer(player));
    }

    public void transferOwnership(UUID newOwner) {
        // TODO: the underlying team remains unchanged, but we do need to change the leader that's stored on the disk
        // TODO: it should be okay to just call replace() in that case, and not have to worry about changing anything
    }

    public static @Nullable Party getPartyFor(Player player) throws IOException {
        return Storage.of(PartyStorage.class).getOrElseNull(p -> p.members.contains(player.getUniqueId()));
    }
}
