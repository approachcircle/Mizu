package net.approachcircle.plugin.effect;

import net.kyori.adventure.text.format.NamedTextColor;

public class Suppressed extends MizuEffect {
    public Suppressed() {
        super(MizuEffectType.SUPPRESSED, "unsuppressed", "you cannot execute Mizu commands", NamedTextColor.DARK_BLUE);
    }
}
