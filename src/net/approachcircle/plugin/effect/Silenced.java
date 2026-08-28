package net.approachcircle.plugin.effect;

import net.kyori.adventure.text.format.NamedTextColor;

public class Silenced extends MizuEffect {
    public Silenced() {
        super(MizuEffectType.SILENCED, "unsilenced", "you cannot speak", NamedTextColor.DARK_PURPLE);
    }
}
