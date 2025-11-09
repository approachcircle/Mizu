package net.approachcircle.plugin;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class VaultManager {
    private static VaultManager instance;
    private final File vaultFolder;
    private final Logger logger = PluginLogger.getPluginLogger();
    public static final String viewName = "Mizu Vault";
    public static final List<UUID> currentlyViewingVault = new ArrayList<>();

    private VaultManager() {
        vaultFolder = new File(Mizu.getInstance().getDataFolder().getAbsolutePath() + "\\vaults");
        if (!vaultFolder.exists()) {
            if (!vaultFolder.mkdirs()) {
                logger.severe("failed to create vaults folder");
            }
        }
    }

    public static VaultManager getInstance() {
        if (instance == null) {
            instance = new VaultManager();
        }
        return instance;
    }

    public Inventory getVault(Player player) {
        Inventory vault = Bukkit.createInventory(player, 27, Component.text(viewName));
        File vaultFile = new File(vaultFolder, player.getUniqueId() + ".yml");
        if (!vaultFile.exists()) {
            try {
                vaultFile.createNewFile();
            } catch (IOException e) {
                logger.severe(Utils.prefix + ChatColor.RED + "failed to create vault file for " + player.getName());
                e.printStackTrace(System.err);
                return null;
            }
            return vault;
        }
        List<ItemStack> items = parseVault(vaultFile);
        if (items == null) {
            return vault;
        }
        for (ItemStack item : items) {
            if (item == null) {
                continue;
            }
            vault.addItem(item);
        }
        return vault;
    }

    private List<ItemStack> parseVault(File vault) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(vault);
        return (List<ItemStack>) config.getList("contents");
    }

    public void saveVault(Player player, Inventory inventory) {
        File vaultFile = new File(vaultFolder, player.getUniqueId() + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(vaultFile);
        config.set("contents", Arrays.asList(inventory.getContents()));
        try {
            config.save(vaultFile);
        } catch (IOException e) {
            logger.severe(Utils.prefix + ChatColor.RED + "failed to save vault for " + player.getName());
            e.printStackTrace(System.err);
        }
    }
}
