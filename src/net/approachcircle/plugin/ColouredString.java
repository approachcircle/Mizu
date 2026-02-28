package net.approachcircle.plugin;

import net.kyori.adventure.text.format.NamedTextColor;

public class ColouredString {
    String string;
    NamedTextColor color;

    public ColouredString(String string, NamedTextColor color) {
        this.string = string;
        this.color = color;
    }
}
