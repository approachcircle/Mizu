package net.approachcircle.plugin.data;

import org.bukkit.entity.Player;

import java.io.IOException;

public class ToBeMigratedFile extends PlainTextStorage {
    public ToBeMigratedFile() {
        super("migration.txt");
    }

    public boolean hasBeenMigrated(Player player) throws IOException {
        return list().stream().noneMatch(uuid -> uuid.equalsIgnoreCase(player.getUniqueId().toString()));
    }

    public boolean setMigrated(Player player) throws IOException {
        return remove(uuid -> uuid.equalsIgnoreCase(player.getUniqueId().toString()));
    }
}
