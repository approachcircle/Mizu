package net.approachcircle.plugin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class StatisticManager {
    private final String statFileName = "stats.json";
    private final File statFile;
    private final Logger logger = PluginLogger.getPluginLogger();
    private static StatisticManager instance = null;

    private StatisticManager() {
        statFile = new File(Mizu.getInstance().getDataFolder().getAbsolutePath() + "\\" + statFileName);
        if (!statFile.exists()) {
            try {
                statFile.createNewFile();
                try (FileWriter writer = new FileWriter(statFile)) {
                    writer.write("[]");
                } catch (IOException e) {
                    logger.severe("failed to initialise statistics file");
                    e.printStackTrace(System.err);
                }
            } catch (IOException e) {
                logger.severe("failed to create statistics file");
                e.printStackTrace(System.err);
            }
        }
    }

    public static StatisticManager getInstance() {
        if (instance == null) {
            instance = new StatisticManager();
        }
        return instance;
    }
    
    public List<Statistic> readStatistics() {
        ObjectMapper mapper = new ObjectMapper();
        List<Statistic> result;
        try {
            result = mapper.readValue(statFile, new TypeReference<>(){});
        } catch (IOException e) {
            logger.severe("failed to read statistics file");
            e.printStackTrace(System.err);
            return null;
        }
        return result;
    }
    
    public void commandUsed(Player player, String command) {
        ObjectMapper mapper = new ObjectMapper();
        List<Statistic> current = readStatistics();
        if (current.isEmpty()) {
            Statistic stat = new Statistic();
            stat.player = player.getName();
            stat.commandUses = new HashMap<>();
            current.add(stat);
        }
        boolean found = false;
        for (Statistic stat : current) {
            if (stat.player.equals(player.getName())) {
                if (stat.commandUses.containsKey(command)) {
                    stat.commandUses.put(command, stat.commandUses.get(command) + 1);
                } else {
                    stat.commandUses.put(command, 1);
                }
                found = true;
                break;
            }
        }
        if (!found) {
            Statistic stat = new Statistic();
            stat.player = player.getName();
            stat.commandUses = new HashMap<>();
            stat.commandUses.put(command, 1);
            current.add(stat);
        }
        try {
            mapper.writeValue(statFile, current);
        } catch (IOException e) {
            logger.severe("failed to update statistics file for player: " + player.getName());
            e.printStackTrace(System.err);
        }
    }
    
    public Map.Entry<String, Integer> getMostUsedCommandForPlayer(Player player) {
        List<Statistic> statistics = StatisticManager.getInstance().readStatistics();
        Map.Entry<String, Integer> mostUsedCommand = null;
        for (Statistic stat : statistics) {
            if (stat.player.equals(player.getName())) {
                for (Map.Entry<String, Integer> command : stat.commandUses.entrySet()) {
                    if (mostUsedCommand == null) {
                        mostUsedCommand = command;
                    }
                    if (command.getValue() > mostUsedCommand.getValue()) {
                        mostUsedCommand = command;
                    }
                }
                return mostUsedCommand;
            }
        }
        return null;
    }
    
    public void sendMostUsedCommandGlobally(CommandSender sender) {
        List<Statistic> statistics = StatisticManager.getInstance().readStatistics();
        Map.Entry<String, Integer> mostUsedCommand = null;
        String player = null;
        for (Statistic stat : statistics) {
            for (Map.Entry<String, Integer> command : stat.commandUses.entrySet()) {
                if (mostUsedCommand == null) {
                    mostUsedCommand = command;
                }
                if (player == null) {
                    player = stat.player;
                }
                if (command.getValue() > mostUsedCommand.getValue()) {
                    mostUsedCommand = command;
                    player = stat.player;
                }
            }
        }
        Localisation.sendMessage(sender, new KeyArgPair("stats_for_everyone",
                mostUsedCommand.getKey(),
                mostUsedCommand.getValue(),
                player
        ));
    }
}
