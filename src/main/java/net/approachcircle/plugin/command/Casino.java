package net.approachcircle.plugin.command;

import io.sentry.Sentry;
import net.approachcircle.plugin.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Logger;

public class Casino extends MizuCommand {
    private static final List<UUID> ineligible = new ArrayList<>();
    private static final Integer cooldownSeconds = 2;
    private static final Integer cooldown = cooldownSeconds * 20;
    private static final int entitySearchRadius = 15;
    public static final List<UUID> gambledLife = new ArrayList<>();
    private static final Logger logger = PluginLogger.getPluginLogger();
    private static final List<String> experienceAliases = List.of(new String[] {"exp", "xp", "levels", "experience", "level", "eckspee"});
    private static final List<String> healthAliases = List.of(new String[] {"health", "hp", "life", "hearts"});
    public static final List<String> EntityBlacklist = List.of(
            "ender_dragon",
            "wither",
            "villager",
            "wolf",
            "player",
            "horse",
            "parrot",
            "cat"
    ); // could be entity types, don't mind it here though

    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (sender instanceof Player player) {
            if (args.length <= 1) {
                return false;
            }
            int amount;
            try {
                amount = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                Sentry.addBreadcrumb(player.getName() + " supplied an invalid amount of items to gamble");
                Localisation.sendMessage(player, new KeyArgPair("invalid_gamble_number"));
                return true;
            }
            if (experienceAliases.contains(args[1].toLowerCase())) {
                Casino.gamble(player, GambleType.Experience, amount);
                return true;
            }
            List<String> entListNames = Arrays.stream(EntityType.values()).map(Enum::name).toList();
            // add check to account for entities that are also materials
            // favour materials
            if (entListNames.contains(args[1].toUpperCase()) && Material.matchMaterial(args[1].toUpperCase()) == null) {
                if (EntityBlacklist.contains(args[1].toLowerCase())) {
                    Localisation.sendMessage(player, new KeyArgPair("cannot_gamble_entity", args[1]));
                    logger.warning(String.format("%s tried gambling a disallowed entity: %s", player.getName(), args[1]));
                    return true;
                }
                EntityType entity;
                try {
                    entity = Enum.valueOf(EntityType.class, args[1].toUpperCase());
                } catch (IllegalArgumentException e) {
                    Localisation.sendMessage(player, new KeyArgPair("entity_does_not_exist", args[1]));
                    return true;
                }
                Casino.gamble(player, GambleType.Entities, amount, new Object[] {entity});
                return true;
            }
            if (healthAliases.contains(args[1].toLowerCase())) {
                Casino.gamble(player, GambleType.Health, amount);
                return true;
            }
            Material material;
            try {
                material = Enum.valueOf(Material.class, args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                Localisation.sendMessage(player, new KeyArgPair("material_does_not_exist", args[1]));
                return true;
            }
            gamble(player, GambleType.Items, amount, new Object[] {material});
        }
        return true;
    }

    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            return List.of("<number>");
        } else if (args.length == 2) {
            options.add("xp");
            options.add("levels");
            options.add("hp");
            options.add("health");
            options.addAll(getMaterialNames());
            for (EntityType type : EntityType.values()) {
                if (Casino.EntityBlacklist.contains(type.name().toLowerCase())) {
                    continue;
                }
                options.add(type.name().toLowerCase());
            }
        }
        return copyMatches(args[1], options);
    }

    public static void gamble(Player player, GambleType type, int amount, Object[] args) {
        if (checkCooldown(player)) {
            return;
        }
        if (amount <= 0) {
            Localisation.sendMessage(player, new KeyArgPair("cannot_gamble_zero_items"));
            return;
        }
        if ((type != GambleType.Experience && type != GambleType.Health) && args.length == 0) {
            Utils.error("args required for gamble types which are not Health or Experience");
            return;
        }
        startCooldown(player);
        boolean winner = rollDouble();
        switch (type) {
            case Experience -> {
                if (amount > player.getLevel()) {
                    Localisation.sendMessage(player, new KeyArgPair("not_enough_levels_to_gamble"));
                    return;
                }
                if (winner) {
                    Localisation.broadcast(new KeyArgPair("won_levels_gamble", player.getName(), amount));
                    player.giveExpLevels(amount);
                } else {
                    Localisation.broadcast(new KeyArgPair("lost_levels_gamble", player.getName(), amount));
                    player.setLevel(Math.max(player.getLevel() - amount, 0));
                }
            }
            // add ability to gamble durability-having items
            // AND to gamble actual durability numbers
            case Items -> {
                if (!(args[0] instanceof Material material)) {
                    Utils.error("material parsed correctly, but cannot be casted here (args in wrong place?)");
                    return;
                }
                logger.info(material.name());
                PlayerInventory inv = player.getInventory();
                if (!canGambleTheseItems(player, material, amount)) {
                    return;
                }
                if (winner) {
                    Localisation.broadcast(new KeyArgPair("won_items_gamble", player.getName(), amount, material.toString().toLowerCase()));
                    inv.addItem(new ItemStack(material, amount));
                } else {
                    Localisation.broadcast(new KeyArgPair("lost_items_gamble", player.getName(), amount, material.toString().toLowerCase()));
                    removeItems(player, material, amount);
                }
            }
            case Entities -> {
                if (!(args[0] instanceof EntityType entity)) {
                    Utils.error("entity type parsed correctly, but cannot be casted here (args in wrong place?)");
                    return;
                }
                logger.info(entity.name());
                Location playerLoc = player.getLocation();
                Collection<LivingEntity> ents = playerLoc.getNearbyLivingEntities(entitySearchRadius);
                List<LivingEntity> targets = new ArrayList<>();
                for (LivingEntity ent : ents) {
                    if (ent.getType() == entity) {
                        targets.add(ent);
                    }
                }
                if (amount > targets.size()) {
                    Localisation.sendMessage(player, new KeyArgPair("not_enough_mobs_to_gamble", entity.toString(), targets.size()));
                    return;
                }
                if (winner) {
                    Localisation.broadcast(new KeyArgPair("won_mobs_gamble", player.getName(), amount, entity.toString().toLowerCase()));
                    for (int i = 0; i < amount; i++) {
                        playerLoc.getWorld().spawnEntity(playerLoc, entity);
                    }
                } else {
                    Localisation.broadcast(new KeyArgPair("lost_mobs_gamble", player.getName(), amount, entity.toString().toLowerCase()));
                    for (int i = 0; i < amount; i++) {
                        targets.get(i).remove();
                    }
                }
            }
            case Health -> {
                int health = (int) Math.round(player.getHealth());
                if (amount > health) {
                    Localisation.sendMessage(player, new KeyArgPair("not_enough_health_to_gamble", health, amount));
                    return;
                }
                if (winner) {
                    Localisation.broadcast(new KeyArgPair("won_health_gamble", player.getName(), amount));
                    player.setHealth(Math.min(health + amount, 20));
                } else {
                    double result = Math.max(health - amount, 0);
                    if (result == 0) {
                        gambledLife.add(player.getUniqueId());
                    }
                    Localisation.broadcast(new KeyArgPair("lost_health_gamble", player.getName(), amount));
                    player.setHealth(result);
                }
            }
            default -> {
                Utils.error(String.format("enum constant not present, type = %s, constant = %s", GambleType.class, type.name()));
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
            Localisation.sendMessage(player, new KeyArgPair("not_enough_items_to_gamble", material.toString().toLowerCase()));
            return false;
        }
        boolean success = false;
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null)
                continue;
            if (inv.getItem(i).getType() == material) {
                if (inv.getItem(i).getAmount() >= amount) {
                    success = true;
                    break;
                }
            }
        }
        if (!success) {
            Localisation.sendMessage(player, new KeyArgPair("ensure_large_enough_stack_to_gamble", material.toString().toLowerCase(), amount));
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
            Localisation.sendMessage(player, new KeyArgPair("gamble_cooldown"));
            return true;
        }
        return false;
    }

    private static void startCooldown(Player player) {
        ineligible.add(player.getUniqueId());
        // doesn't need to be asynchronous
        Bukkit.getServer().getScheduler().runTaskLater(Mizu.getInstance(), () -> {
            ineligible.remove(player.getUniqueId());
            Localisation.sendMessage(player, new KeyArgPair("can_gamble_again"));
        }, cooldown);
    }

    private static Boolean rollDouble() {
        return new Random().nextBoolean();
    }
}
