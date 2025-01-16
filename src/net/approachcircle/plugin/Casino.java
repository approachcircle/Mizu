package net.approachcircle.plugin;

import io.sentry.Sentry;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;
import java.util.logging.Logger;

public class Casino {
    private static final List<UUID> ineligible = new ArrayList<UUID>();
    private static final Integer cooldownSeconds = 2;
    private static final Integer cooldown = cooldownSeconds * 20;
    private static final int entitySearchRadius = 8;
    public static final List<UUID> gambledLife = new ArrayList<>();
    private static final Logger logger = PluginLogger.getPluginLogger();
    public static void gamble(Player player, GambleType type, int amount, Object[] args) {
        if (checkCooldown(player)) {
            return;
        }
        if (amount <= 0) {
            player.sendMessage(String.format("%s%scannot gamble less than or equal to zero items", Utils.prefix, ChatColor.RED));
            return;
        }
        if ((type != GambleType.Experience && type != GambleType.Health) && args.length == 0) {
            logger.severe("args required for gamble types which are not Health or Experience");
            return;
        }
        startCooldown(player);
        boolean winner = roll(50.0f);
        switch (type) {
            case Experience -> {
                if (amount > player.getLevel()) {
                    player.sendMessage(String.format("%s%syou don't have that many levels to gamble", Utils.prefix, ChatColor.RED));
                    return;
                }
                if (winner) {
                    Bukkit.broadcast(Component.text(String.format("%s%s%s won double or nothing! they won an extra %s levels!",
                            Utils.prefix,
                            ChatColor.GREEN,
                            player.getName(),
                            amount
                    )));
                    player.giveExpLevels(amount);
                } else {
                    Bukkit.broadcast(Component.text(String.format("%s%s%s lost double or nothing! they lost %s levels!",
                            Utils.prefix,
                            ChatColor.RED,
                            player.getName(),
                            amount
                    )));
                    player.setLevel(Math.max(player.getLevel() - amount, 0));
                }
            }
            case Items -> {
                if (!(args[0] instanceof Material material)) {
                    logger.severe("material parsed correctly, but cannot be casted here (args in wrong place?)");
                    return;
                }
                logger.info(material.name());
                PlayerInventory inv = player.getInventory();
                if (!canGambleTheseItems(player, material, amount)) {
                    return;
                }
                if (winner) {
                    inv.addItem(new ItemStack(material, amount));
                    Bukkit.broadcast(Component.text(String.format("%s%s%s won double or nothing! they won an extra %s '%s'!",
                            Utils.prefix,
                            ChatColor.GREEN,
                            player.getName(),
                            amount,
                            material.toString().toLowerCase()
                    )));
                } else {
                    removeItems(player, material, amount);
                    Bukkit.broadcast(Component.text(String.format("%s%s%s lost double or nothing! they lost %s '%s'!",
                            Utils.prefix,
                            ChatColor.RED,
                            player.getName(),
                            amount,
                            material.toString().toLowerCase()
                    )));
                }
            }
            case Entities -> {
                if (!(args[0] instanceof EntityType entity)) {
                    logger.severe("entity type parsed correctly, but cannot be casted here (args in wrong place?)");
                    return;
                }
                logger.info(entity.name());
                Location playerLoc = player.getLocation();
                Collection<LivingEntity> ents = playerLoc.getNearbyLivingEntities(entitySearchRadius);
                List<LivingEntity> targets = new ArrayList<LivingEntity>();
                for (LivingEntity ent : ents) {
                    if (ent.getType() == entity) {
                        targets.add(ent);
                    }
                }
                if (amount > targets.size()) {
                    player.sendMessage(String.format("%s%snot enough '%s' nearby. only found '%s'", Utils.prefix, ChatColor.RED, entity.toString(), targets.size()));
                    Sentry.addBreadcrumb(String.format("couldn't find enough '%s' near '%s'. only found '%s'", entity.toString(), player.getName(), targets.size()));
                    return;
                }
                if (winner) {
                    Bukkit.broadcast(Component.text(String.format("%s%s%s won double or nothing! they spawned an extra %s '%s'!",
                            Utils.prefix,
                            ChatColor.GREEN,
                            player.getName(),
                            amount,
                            entity.toString().toLowerCase()
                    )));
                    for (int i = 0; i < amount; i++) {
                        playerLoc.getWorld().spawnEntity(playerLoc, entity);
                    }
                } else {
                    Bukkit.broadcast(Component.text(String.format("%s%s%s lost double or nothing! they lost %s '%s'!",
                            Utils.prefix,
                            ChatColor.RED,
                            player.getName(),
                            amount,
                            entity.toString().toLowerCase()
                    )));
                    for (int i = 0; i < amount; i++) {
                        targets.get(i).remove();
                    }
                }
            }
            case Health -> {
                int health = (int) Math.round(player.getHealth());
                if (amount > health) {
                    player.sendMessage(Component.text(String.format("%s%syou only have %d health, cannot gamble %d (1 heart = 2, half heart = 1)",
                            Utils.prefix,
                            ChatColor.RED,
                            health,
                            amount
                    )));
                    return;
                }
                if (winner) {
                    Bukkit.broadcast(Component.text(String.format("%s%s%s won double or nothing! they won an extra %s health!",
                            Utils.prefix,
                            ChatColor.GREEN,
                            player.getName(),
                            amount
                    )));
                    player.setHealth(Math.min(health + amount, 20));
                } else {
                    Bukkit.broadcast(Component.text(String.format("%s%s%s lost double or nothing! they lost %s health!",
                            Utils.prefix,
                            ChatColor.RED,
                            player.getName(),
                            amount
                    )));
                    double result = Math.max(health - amount, 0);
                    if (result == 0) {
                        gambledLife.add(player.getUniqueId());
                    }
                    player.setHealth(result);
                }
            }
            default -> {
                logger.severe(String.format("enum constant not present, type = %s, constant = %s", GambleType.class, type.name()));
                return;
            }
        }
    }

    public static void gamble(Player player, GambleType type, int amount) {
        gamble(player, type, amount, new Object[]{});
    }

    private static boolean canGambleTheseItems(Player player, Material material, int amount) {
        PlayerInventory inv = player.getInventory();
        boolean hasEnough = inv.containsAtLeast(new ItemStack(material), amount);
        if (!hasEnough) {
            player.sendMessage(String.format("%s%syou don't have that many '%s' to gamble (or that item is enchanted/not full durability)", Utils.prefix, ChatColor.RED, material.toString().toLowerCase()));
            Sentry.addBreadcrumb(player.getName() + " didn't have enough '" + material.toString().toLowerCase() + "' for how many they wanted to gamble");
            return false;
        }
        int amountLeft = 0;
        boolean success = false;
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null)
                continue;
            if (inv.getItem(i).getType() == material) {
                if (inv.getItem(i).getAmount() >= amount) {
                    amountLeft = inv.getItem(i).getAmount() - amount;
                    success = true;
                    break;
                }
            }
        }
        if (!success) {
            player.sendMessage(String.format("%s%smake sure you have a stack of '%s' at least %s big in your inventory", Utils.prefix, ChatColor.RED, material.toString().toLowerCase(), amount));
            return false;
        }
        return true;
    }

    private static void removeItems(Player player, Material material, int amount) {
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null)
                continue;
            if (inv.getItem(i).getType() == material) {
                if (inv.getItem(i).getAmount() >= amount) {
                    ItemStack resultingItems = new ItemStack(inv.getItem(i));
                    resultingItems.setAmount(inv.getItem(i).getAmount() - amount);
                    inv.setItem(inv.first(Objects.requireNonNull(inv.getItem(i))), resultingItems);
                    break;
                }
            }
        }
    }

    private static Boolean checkCooldown(Player player) {
        if (ineligible.contains(player.getUniqueId())) {
            player.sendMessage(String.format("%s%shold your fucking horses", Utils.prefix, ChatColor.RED));
            return true;
        }
        return false;
    }

    private static void startCooldown(Player player) {
        ineligible.add(player.getUniqueId());
        Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(Mizu.getInstance(), new Runnable() {
            @Override
            public void run() {
                ineligible.remove(player.getUniqueId());
                player.sendMessage(String.format("%s%syou can gamble again", Utils.prefix, ChatColor.GREEN));
            }
        }, cooldown);
    }

    private static Boolean roll(Float chancePercent) {
        Random rand = new Random();
        if (chancePercent == 50.0f) {
            return rand.nextBoolean();
        }
        int size = Math.round(100.0f / chancePercent);
        int result = rand.nextInt(size);
        return result == size - 1;
    }
}
