package net.approachcircle.plugin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Localisation {
    private static List<HashMap<String, String>> english;
    private static List<HashMap<String, String>> portuguese;
    private static final TextComponent mizuPrefix = Component.text("Mizu", NamedTextColor.DARK_AQUA)
            .append(Component.text("> ", NamedTextColor.WHITE));
    private static final String localeSettingsFilename = "locale-definitions.txt";
    private static File localeSettingsFile;

    private static InputStream getLanguageResource(String name) {
        return Mizu.getPlugin(Mizu.class).getResource(name + ".json");
    }

    public static void loadStrings() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            english = mapper.readValue(getLanguageResource("en"), new TypeReference<>() {});
            portuguese = mapper.readValue(getLanguageResource("pt"), new TypeReference<>() {});
        } catch (IOException e) {
            PluginLogger.getPluginLogger().severe("failed to load language files");
            throw new RuntimeException(e);
        }
        PluginLogger.getPluginLogger().info("loaded language files successfully");
        PluginLogger.getPluginLogger().info(english.size() + " english entries");
        Set<String> portugueseKeys = getKeysForLocale("pt");
        for (HashMap<String, String> englishEntry : english) {
            for (String key : englishEntry.keySet()) {
                if (!key.equals("color")) {
                    if (!portugueseKeys.contains(key)) {
                        PluginLogger.getPluginLogger().warning(String.format("missing portuguese translation for key: %s", key));
                    }
                }
            }
        }
    }

    public static Set<String> getKeysForLocale(String locale) {
        Set<String> keys = new HashSet<>();
        if (locale.equalsIgnoreCase("en")) {
            for (HashMap<String, String> englishEntry : english) {
                keys.addAll(englishEntry.keySet());
            }
        } else if (locale.equalsIgnoreCase("pt")) {
            for (HashMap<String, String> portugueseEntry : portuguese) {
                keys.addAll(portugueseEntry.keySet());
            }
        }
        return keys;
    }

    public static void loadExplicitLocaleSettings() {
        localeSettingsFile = new File(Mizu.getInstance().getDataFolder().getAbsolutePath() + "\\" + localeSettingsFilename);
        if (!localeSettingsFile.exists()) {
            try {
                localeSettingsFile.createNewFile();
            } catch (IOException e) {
                PluginLogger.getPluginLogger().severe("failed to create locale settings file");
                throw new RuntimeException(e);
            }
        }
    }

    public static void addExplicitLocale(Player player, String locale) {
        try (FileWriter writer = new FileWriter(localeSettingsFile, true)) {
            writer.append(String.format("%s %s%n", player.getName(), locale));
        } catch (IOException e) {
            PluginLogger.getPluginLogger().severe("failed to add locale to locale settings file");
        }
    }

    private static List<String> getExplicitLocaleLines() {
        List<String> lines = new ArrayList<>();
        try (Scanner scanner = new Scanner(localeSettingsFile)) {
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
        } catch (IOException e) {
            PluginLogger.getPluginLogger().severe("something went wrong grabbing explicit locales from file");
            e.printStackTrace(System.err);
            return null;
        }
        return lines;
    }

    public static void removeExplicitLocale(Player player) {
        List<String> lines = getExplicitLocaleLines();
        assert lines != null;
        lines.removeIf(line -> line.startsWith(player.getName()));
        try (FileWriter writer = new FileWriter(localeSettingsFile)) {
            for (String line : lines) {
                writer.append(line).append("\n");
            }
        } catch (IOException e) {
            PluginLogger.getPluginLogger().severe("failed to restore explicit locales");
            e.printStackTrace(System.err);
        }
    }

    public static String getClientLocaleOrExplicit(Player player) {
        List<String> lines = getExplicitLocaleLines();
        if (lines != null && lines.stream().anyMatch(line -> line.startsWith(player.getName()))) {
            // weird LINQ-like wizardry going on here, looks good to me though?
            return lines.stream().filter(line -> line.startsWith(player.getName())).findFirst().orElseThrow().split(" ")[1];
        }
        return player.locale().getLanguage();
    }

    /**
     * Returns a localised string based on the key within the KeyArgPair, and the provided region.
     * @param keyArgPair an object so
     * @param region
     * @return
     */
    public static ColouredString getLocalisedString(KeyArgPair keyArgPair, String region) {
        List<HashMap<String, String>> selected = english;
        if (region.equalsIgnoreCase("pt")) {
            selected = portuguese;
        }
        // returns first key found, if there are duplicates then this will be an issue
        for (HashMap<String, String> map : selected) {
            for (String k : map.keySet()) {
                if (k.equalsIgnoreCase(keyArgPair.key)) {
                    return new ColouredString(String.format(map.get(k), keyArgPair.args), NamedTextColor.NAMES.value(map.get("color")));
                }
            }
        }
        // no key present in this language, try english then return an error if not in english either
        if (selected != english) {
            PluginLogger.getPluginLogger().warning(String.format("key %s not found in %s.json file, returning english", keyArgPair.key, region));
            return getLocalisedString(keyArgPair, "en");
        } else {
            String message = "%s";
            if (region.equalsIgnoreCase("pt")) {
                message += " (par de chaves em falta)";
            } else if (region.equalsIgnoreCase("en")) {
                message += " (missing key pair)";
            }
            return new ColouredString(String.format(message, keyArgPair.key), NamedTextColor.WHITE);
        }
    }

    public static TextComponent component(KeyArgPair keyArgPair, String region) {
        ColouredString colouredString = getLocalisedString(keyArgPair, region);
        return Component.text(colouredString.string, colouredString.color);
    }

    public static TextComponent prefixComponent(KeyArgPair keyArgPair, String region) {
        return mizuPrefix.append(component(keyArgPair, region));
    }

    public static TextComponent unlocalisedPrefixComponent(ColouredString message) {
        return mizuPrefix.append(Component.text(message.string, message.color));
    }

    public static void sendMessage(Player player, KeyArgPair keyArgPair) {
        player.sendMessage(Localisation.prefixComponent(keyArgPair, getClientLocaleOrExplicit(player)));
    }

    public static void sendMessage(Player player, String key) {
        sendMessage(player, new KeyArgPair(key));
    }

    public static void sendMessage(CommandSender sender, KeyArgPair keyArgPair) {
        if (sender instanceof Player) {
            sendMessage((Player) sender, keyArgPair);
        } else {
            sender.sendMessage(getLocalisedString(keyArgPair, "en").string);
        }
    }

    public static void sendMessage(CommandSender sender, String key) {
        sendMessage(sender, new KeyArgPair(key));
    }

    public static void broadcast(KeyArgPair keyArgPair, boolean prefix) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // sendMessage(player, keyArgPair);
            TextComponent text;
            if (prefix) {
                text = Localisation.prefixComponent(keyArgPair, getClientLocaleOrExplicit(player));
            } else {
                text = Localisation.component(keyArgPair, getClientLocaleOrExplicit(player));
            }
            player.sendMessage(text);
        }
        Mizu.getInstance().getServer().getConsoleSender().sendMessage(getLocalisedString(keyArgPair, "en").string);
    }

    public static void broadcast(KeyArgPair keyArgPair) {
        broadcast(keyArgPair, true);
    }

    public static boolean playersWithLocalePresent(String locale) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (getClientLocaleOrExplicit(player).equalsIgnoreCase(locale)) {
                return true;
            }
        }
        return false;
    }
}
