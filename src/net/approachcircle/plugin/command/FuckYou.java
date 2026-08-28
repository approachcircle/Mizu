package net.approachcircle.plugin.command;

import net.approachcircle.plugin.KeyArgPair;
import net.approachcircle.plugin.Localisation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FuckYou extends MizuCommand {
    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            return false;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target != null) {
            List<PotionEffect> effects = getPotionEffects();
            for (PotionEffect effect : effects) {
                target.addPotionEffect(effect);
            }
            target.sendTitlePart(TitlePart.TITLE, Component.text("fuck you", NamedTextColor.RED));
        } else {
            Localisation.sendMessage(sender, new KeyArgPair("player_not_found", args[0]));
        }
        return true;
    }

    private static @NotNull List<PotionEffect> getPotionEffects() {
        int duration = 20*5;
        // nausea, blindness, slowness, weakness, hunger, poison
        return List.of(
                new PotionEffect(PotionEffectType.NAUSEA, duration, 255),
                new PotionEffect(PotionEffectType.BLINDNESS, duration, 255),
                new PotionEffect(PotionEffectType.SLOWNESS, duration, 255),
                new PotionEffect(PotionEffectType.WEAKNESS, duration, 255),
                new PotionEffect(PotionEffectType.HUNGER, duration, 255),
                new PotionEffect(PotionEffectType.POISON, duration, 255),
                new PotionEffect(PotionEffectType.DARKNESS, duration, 255),
                new PotionEffect(PotionEffectType.GLOWING, duration, 255),
                new PotionEffect(PotionEffectType.INFESTED, duration, 255),
                new PotionEffect(PotionEffectType.WIND_CHARGED, duration, 255),
                new PotionEffect(PotionEffectType.OOZING, duration, 255),
                new PotionEffect(PotionEffectType.MINING_FATIGUE, duration,255),
                // 255 is too much
                // 30 kills with max feather falling
                new PotionEffect(PotionEffectType.LEVITATION, duration, 30)
        );
    }
}
