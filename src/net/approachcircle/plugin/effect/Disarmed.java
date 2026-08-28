package net.approachcircle.plugin.effect;

import net.kyori.adventure.text.format.NamedTextColor;

public class Disarmed extends MizuEffect {
    public Disarmed() {
        super(MizuEffectType.DISARMED, "rearmed", "you cannot attack", NamedTextColor.DARK_GRAY);
    }
}
