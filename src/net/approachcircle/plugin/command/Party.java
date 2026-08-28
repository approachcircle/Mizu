package net.approachcircle.plugin.command;

import net.approachcircle.plugin.KeyArgPair;
import net.approachcircle.plugin.Localisation;
import net.approachcircle.plugin.government.PartyManager;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Party extends MizuCommand {
    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (sender instanceof Player player) {
            if (args.length < 1) {
                return false;
            }
            if (args[0].equalsIgnoreCase("create")) {
                if (args.length < 3) {
                    return false;
                }
                List<net.approachcircle.plugin.government.Party> parties = PartyManager.getInstance().getParties();
                if (parties == null) {
                    Localisation.sendMessage(player, "error_occurred_getting_parties");
                    return true;
                }
                for (net.approachcircle.plugin.government.Party party : parties) {
                    if (party.name.equalsIgnoreCase(args[1])) {
                        Localisation.sendMessage(player, new KeyArgPair("party_name_taken", args[1]));
                        return true;
                    }
                }
                NamedTextColor teamColor = NamedTextColor.NAMES.value(args[2]);
                if (teamColor == null) {
                    Localisation.sendMessage(player, new KeyArgPair("not_a_valid_colour", args[2]));
                    return true;
                }
                if (!PartyManager.getInstance().createParty(args[1], args[2], player.getUniqueId())) {
                    Localisation.sendMessage(player, new KeyArgPair("error_occurred_creating_party", args[1]));
                    return true;
                }
                Localisation.sendMessage(player, new KeyArgPair("party_created", args[1]));
                Localisation.sendMessage(player, "auto_appointed_leader");
                Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
                Team team = scoreboard.registerNewTeam(args[1]);
                team.color(teamColor);
                team.addEntry(player.getName());
            }
        }
        return true;
    }
}
