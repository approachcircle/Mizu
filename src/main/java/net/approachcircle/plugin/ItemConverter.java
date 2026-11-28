package net.approachcircle.plugin;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;

import java.util.HashMap;
import java.util.Map;

public class ItemConverter {
    HashMap<ItemStack, ItemStack> directConversions = (HashMap<ItemStack, ItemStack>) Map.ofEntries(
            Map.entry(new ItemStack(Material.DIAMOND, 64), new ItemStack(Material.NETHERITE_INGOT, 1))
            
    );
    
    HashMap<String, String> generalConversions = (HashMap<String, String>) Map.ofEntries(
            Map.entry("%s_log", "stripped_%s_log"),
            Map.entry("%s_stem", "stripped_%s_stem")
    );
}
