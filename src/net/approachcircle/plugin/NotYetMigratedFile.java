package net.approachcircle.plugin;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class NotYetMigratedFile {
    private final String fileName = "migration.txt";
    private final File notYetMigratedFile;
    private static NotYetMigratedFile instance = null;

    private NotYetMigratedFile() {
        notYetMigratedFile = new File(Mizu.getInstance().getDataFolder().getAbsolutePath() + "\\" + fileName);
    }

    public static NotYetMigratedFile getInstance() {
        if (instance == null) {
            instance = new NotYetMigratedFile();
        }
        return instance;
    }

    public void init() throws IOException {
        if (!notYetMigratedFile.exists()) {
            notYetMigratedFile.createNewFile();
        }
    }

    public void writePlayers(OfflinePlayer[] offlinePlayers) throws IOException {
        init();
        for (OfflinePlayer offlinePlayer : offlinePlayers) {
            try (FileWriter writer = new FileWriter(notYetMigratedFile, true)) {
                writer.write(offlinePlayer.getUniqueId().toString());
            }
        }
    }

    private List<String> getFileLines() throws IOException {
        init();
        List<String> lines = new ArrayList<>();
        try (Scanner scanner = new Scanner(notYetMigratedFile)) {
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
        }
        return lines;
    }

    public boolean hasBeenMigrated(Player player) throws IOException {
        return !getFileLines().contains(player.getUniqueId().toString());
    }

    public void setMigrated(Player player) throws IOException {
        List<String> lines = getFileLines();
        Iterator<String> it = lines.iterator();
        while (it.hasNext()) {
            String line = it.next();
            if (line.equalsIgnoreCase(player.getUniqueId().toString())) {
                it.remove();
                break;
            }
        }
        try (FileWriter writer = new FileWriter(notYetMigratedFile)) {
            for (String line : lines) {
                writer.append(line).append("\n");
            }
        }
    }
}
