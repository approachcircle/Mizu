package net.approachcircle.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class VaultManager {
    private static VaultManager instance;
    private final File vaultFolder;
    public final String viewName = "Mizu Vault";
    // had to change this because the person viewing the vault may not actually be its owner
    // viewer : owner
    public final HashMap<UUID, UUID> vaultsOpen = new HashMap<>();
    public final int vaultSize = 36;

    private VaultManager() {
        vaultFolder = new File(Mizu.getInstance().getDataFolder().getAbsolutePath() + "\\vaults");
        if (!vaultFolder.exists()) {
            if (!vaultFolder.mkdirs()) {
                Utils.error("failed to create vaults folder");
            }
        }
    }

    public static VaultManager getInstance() {
        if (instance == null) {
            instance = new VaultManager();
        }
        return instance;
    }

    public Inventory getVault(@Nullable Player sender, UUID uuid) {
        // the inventory being created belongs to the sender as they're the VIEWER, not because it's actually their vault
        Inventory vault = Bukkit.createInventory(sender, vaultSize, Component.text(viewName));
        File vaultFile = new File(vaultFolder, uuid + ".yml");
        if (!vaultFile.exists()) {
            try {
                vaultFile.createNewFile();
                YamlConfiguration config = YamlConfiguration.loadConfiguration(vaultFile);
                // initialise as empty list of ItemStacks
                config.set("contents", new ArrayList<ItemStack>());
            } catch (IOException e) {
                if (sender != null) {
                    sender.sendMessage(Localisation.rawPrefixComponent(new ColouredString("failed to create vault file", NamedTextColor.RED)));
                }
                Utils.error(e, "failed to create vault file for " + Bukkit.getOfflinePlayer(uuid).getName());
                return null;
            }
            return vault;
        }
        List<ItemStack> items = parseVault(vaultFile);
        if (items == null) {
            return null;
        }
        if (items.isEmpty()) {
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

    public void clearVault(UUID owner) {
        Inventory vault = getVault(null, owner);
        vault.clear();
        saveVault(owner, vault);
    }

    private List<ItemStack> parseVault(File vault) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(vault);
        List<?> items = config.getList("contents");
        if (items == null) {
            Utils.error("got null from contents of vault file");
            return null;
        }
        List<ItemStack> castedItems;
        try {
            // i'm sorry, child... this is the best i can do
            //noinspection unchecked
            castedItems = (List<ItemStack>) items;
        } catch (ClassCastException e) {
            Utils.error(e, "type of list in vault file is not List<ItemStack>");
            return null;
        }
        return castedItems;
    }

    public void saveVault(UUID player, Inventory inventory) {
        File vaultFile = new File(vaultFolder, player + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(vaultFile);
        config.set("contents", Arrays.asList(inventory.getContents()));
        try {
            config.save(vaultFile);
        } catch (IOException e) {
            Utils.error(e, "failed to save vault for " + Bukkit.getOfflinePlayer(player).getName());
        }
    }
}
