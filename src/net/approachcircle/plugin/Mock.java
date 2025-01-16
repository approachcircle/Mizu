package net.approachcircle.plugin;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

public class Mock {
    public static void mockPlayer(String target, String[] message) {
        String joinedMessage = "";
        for (String word : message) {
            if (word.equalsIgnoreCase(target)) {
                continue;
            }
            joinedMessage += word + " ";
        }
        Bukkit.broadcast(Component.text("<" + target + "> " + joinedMessage));
    }
}
