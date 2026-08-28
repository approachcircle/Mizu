package net.approachcircle.plugin.command;

import net.approachcircle.plugin.ColouredString;
import net.approachcircle.plugin.KeyArgPair;
import net.approachcircle.plugin.Localisation;
import net.approachcircle.plugin.effect.MizuEffect;
import net.approachcircle.plugin.effect.MizuEffectRegistry;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MizuEffectCommand extends MizuCommand {
    @Override
    protected boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length < 1) return false;

        switch (args[0]) {
            // if /give + effect, effect is given to everyone
            // if /give + effect + player, effect is given to one player
            case "give":
                // add a scope here because code within these case statements are treated as one scope even with
                // break statements (why)
                {
                    if (args.length < 2) return false;
                    MizuEffect effect;
                    try {
                        effect = MizuEffectRegistry.getEffect(args[1]);
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage(Localisation.unlocalisedPrefixComponent(new ColouredString(String.format("effect '%s' not found", args[1]), NamedTextColor.RED)));
                        return false;
                    }
                    if (args.length == 2) {
                        effect.applyToAllPlayers();
                        sender.sendMessage(Localisation.unlocalisedPrefixComponent(new ColouredString(String.format("applied '%s' to everyone", effect.type.name()), NamedTextColor.GREEN)));
                    } else if (args.length == 3) {
                        Player player = Bukkit.getPlayer(args[2]);
                        if (player == null) {
                            Localisation.sendMessage(sender, new KeyArgPair("player_not_found", args[2]));
                            return false;
                        }
                        effect.applyEffect(player);
                        sender.sendMessage(Localisation.unlocalisedPrefixComponent(new ColouredString(String.format("applied '%s' to %s", effect.type.name(), player.getName()), NamedTextColor.GREEN)));
                    }
                }
                break;
            // if /clear, ALL effects are cleared from everyone
            // if /clear + person, ALL effects are cleared from one player
            // if /clear + effect, effect is removed from everyone
            // if /clear + effect + person, effect is removed from one player
            case "clear":
                if (args.length == 1) {
                    MizuEffectRegistry.clearAllEffects();
                    sender.sendMessage(Localisation.unlocalisedPrefixComponent(new ColouredString("cleared all effects from everyone", NamedTextColor.GREEN)));
                } else if (args.length == 2) {
                    Player player = Bukkit.getPlayer(args[1]);
                    if (player == null) {
                        MizuEffect effect;
                        try {
                            effect = MizuEffectRegistry.getEffect(args[1]);
                        } catch (IllegalArgumentException e) {
                            sender.sendMessage(Localisation.unlocalisedPrefixComponent(new ColouredString(String.format("player or effect '%s' not found", args[1]), NamedTextColor.RED)));
                            return false;
                        }
                        // remove this effect from everyone
                        Bukkit.getOnlinePlayers().forEach(effect::clearEffect);
                        sender.sendMessage(Localisation.unlocalisedPrefixComponent(new ColouredString(String.format("cleared '%s' from everyone", effect.type.name()), NamedTextColor.GREEN)));
                        return true;
                    }
                    // remove every effect from a player
                    MizuEffectRegistry.clearEffectsOnPlayer(player);
                    sender.sendMessage(Localisation.unlocalisedPrefixComponent(new ColouredString(String.format("cleared all affects from %s", player.getName()), NamedTextColor.GREEN)));
                } else if (args.length == 3) {
                    MizuEffect effect;
                    try {
                        effect = MizuEffectRegistry.getEffect(args[1]);
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage(Localisation.unlocalisedPrefixComponent(new ColouredString(String.format("effect '%s' not found", args[1]), NamedTextColor.RED)));
                        return false;
                    }
                    Player player = Bukkit.getPlayer(args[2]);
                    if (player == null) {
                        Localisation.sendMessage(sender, new KeyArgPair("player_not_found", args[2]));
                        return false;
                    }
                    effect.clearEffect(player);
                    sender.sendMessage(Localisation.unlocalisedPrefixComponent(new ColouredString(String.format("cleared '%s' from %s", effect.type.name(), player.getName()), NamedTextColor.GREEN)));
                }
        }
        return true;
    }
}
