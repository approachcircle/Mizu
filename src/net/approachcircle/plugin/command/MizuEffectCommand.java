package net.approachcircle.plugin.command;

import net.approachcircle.plugin.ColouredString;
import net.approachcircle.plugin.Localisation;
import net.approachcircle.plugin.effect.MizuEffect;
import net.approachcircle.plugin.effect.MizuEffectRegistry;
import net.approachcircle.plugin.effect.MizuEffectType;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MizuEffectCommand extends MizuCommand {
    @Override
    protected boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length < 3) return false;
        List<MizuEffect> effects = new ArrayList<>();
        if (args[1].equalsIgnoreCase("all")) {
            for (MizuEffectType effectType : MizuEffectType.values()) {
                effects.add(MizuEffectRegistry.getEffect(effectType));
            }
        } else {
            MizuEffect effect;
            try {
                effect = MizuEffectRegistry.getEffect(args[1]);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(Localisation.unlocalisedPrefixComponent(new ColouredString(String.format("effect '%s' not found", args[1]), NamedTextColor.RED)));
                return false;
            }
            effects.add(effect);
        }
        List<Player> players = new ArrayList<>();
        // normally i hate using lists of handles to actual player objects, but all this happens so quickly that we don't
        // need to worry about players rejoining and losing the handles, so i can let this slide
        if (args[2].equalsIgnoreCase("all")) {
            players.addAll(Bukkit.getOnlinePlayers());
        } else {
            Player player = Bukkit.getPlayer(args[2]);
            if (player == null) {
                Localisation.sendMessage(sender, "player_not_found");
                return false;
            }
            players.add(player);
        }
        switch (args[0]) {
            case "give" -> {
                for (Player player : players) {
                    for (MizuEffect effect : effects) {
                        effect.applyEffect(player);
                    }
                }
            }
            case "clear" -> {
                for (Player player : players) {
                    for (MizuEffect effect : effects) {
                        effect.clearEffect(player);
                    }
                }
            }
        }
        // actual frankenstein shit
        // sunk cost fallacy...
        String message = (args[0].equalsIgnoreCase("give") ? "applied" : "cleared") +
                (effects.size() == 1 ? String.format(" '%s'", effects.getFirst().type.name().toLowerCase()) : " all effects") +
                (args[0].equalsIgnoreCase("give") ? " to" : " from") +
                (players.size() == 1 ? String.format(" '%s'", players.getFirst().getName()) : String.format(" %d players", players.size()));
        sender.sendMessage(Localisation.unlocalisedPrefixComponent(new ColouredString(message, NamedTextColor.GREEN)));
        return true;
    }

    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> completions;
        if (args.length == 1) {
            return List.of("clear", "give");
        } else if (args.length == 2) {
            completions = new ArrayList<>();
            for (MizuEffectType effectType : MizuEffectType.values()) {
                completions.add(effectType.name().toLowerCase());
            }
        } else if (args.length == 3) {
            completions = getPlayerNames();
        } else {
            return List.of();
        }
        completions.add("all");
        sort(completions);
        return completions;
    }
}
