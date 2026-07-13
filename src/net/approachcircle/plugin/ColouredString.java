package net.approachcircle.plugin;

import net.kyori.adventure.text.format.NamedTextColor;

public class ColouredString {
    public String string;
    public NamedTextColor color;

    public ColouredString(String string, NamedTextColor color) {
        this.string = string;
        this.color = color;
    }
}
