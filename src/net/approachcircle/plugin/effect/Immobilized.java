package net.approachcircle.plugin.effect;

import net.kyori.adventure.text.format.NamedTextColor;

public class Immobilized extends MizuEffect {
    public Immobilized() {
        super(MizuEffectType.IMMOBILIZED, "remobilized", "you cannot move", NamedTextColor.YELLOW);
    }
}
