package net.approachcircle.plugin.effect;

import net.approachcircle.plugin.ColouredString;
import net.approachcircle.plugin.Localisation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public abstract class MizuEffect {
    public MizuEffectType type;
    public String opposite;
    public String effectAppliedResult;
    public NamedTextColor color;

    public MizuEffect(MizuEffectType type, String opposite, String effectAppliedResult, NamedTextColor color) {
        this.type = type;
        this.opposite = opposite;
        this.effectAppliedResult = effectAppliedResult;
        this.color = color;
    }

    public final void applyEffect(Player player) {
        if (playerHasEffect(player)) return;
        if (!MizuEffectRegistry.playerEffectMap.containsKey(player.getUniqueId())) {
            MizuEffectRegistry.playerEffectMap.put(player.getUniqueId(), new ArrayList<>());
        }
        MizuEffectRegistry.playerEffectMap.get(player.getUniqueId()).add(this);
        player.playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1f, 1f);
        player.sendTitlePart(TitlePart.TITLE, Component.text(""));
        player.sendTitlePart(TitlePart.SUBTITLE, Component.text(type.name()).color(color));
        player.sendMessage(Localisation.rawPrefixComponent(new ColouredString(String.format("you have been %s, this means that %s", type.name().toLowerCase(), effectAppliedResult), NamedTextColor.RED)));
    }

    public final boolean playerHasEffect(Player player) {
        if (MizuEffectRegistry.playerEffectMap.containsKey(player.getUniqueId())) {
            return MizuEffectRegistry.playerEffectMap.get(player.getUniqueId()).contains(this);
        }
        return false;
    }

    public final void clearEffect(Player player) {
        if (playerHasEffect(player)) {
            if (MizuEffectRegistry.playerEffectMap.get(player.getUniqueId()).removeIf((effect) -> effect.equals(this))) {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                player.sendTitlePart(TitlePart.TITLE, Component.text(""));
                player.sendTitlePart(TitlePart.SUBTITLE, Component.text(opposite.toUpperCase()).color(NamedTextColor.GREEN));
                player.sendMessage(Localisation.rawPrefixComponent(new ColouredString(String.format("you are no longer %s", type.name().toLowerCase()), NamedTextColor.GREEN)));
            }
        }
    }

    public final void applyToAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) continue;
            applyEffect(player);
        }
    }
}
