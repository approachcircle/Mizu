package net.approachcircle.plugin;

import io.papermc.paper.event.player.PlayerDeepSleepEvent;
import io.sentry.Sentry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.logging.Logger;

public class MainListener implements Listener {
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Logger logger = PluginLogger.getPluginLogger();

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        VillagerDetector.checkEntityType(event);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Location deathLocation = event.getPlayer().getLocation();
        Player player = event.getPlayer();
        if (Die.suicided.contains(player.getUniqueId())) { // suicide
            event.deathMessage(Component.text(String.format("%s%s committed suicide", ChatColor.LIGHT_PURPLE, player.getName())));
            deathLocation.getWorld().playSound(deathLocation, Sound.BLOCK_NOTE_BLOCK_PLING, 10.0f, 0.5f);
            Die.suicided.remove(player.getUniqueId());
        } else if (Casino.gambledLife.contains(player.getUniqueId())) { // gambling
            event.deathMessage(Component.text(String.format("%s%s gambled their life away", ChatColor.LIGHT_PURPLE, player.getName())));
            deathLocation.getWorld().playSound(deathLocation, Sound.ENTITY_GUARDIAN_DEATH_LAND, 10.0f, 0.5f);
            Casino.gambledLife.remove(player.getUniqueId());
        } else if (player.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.VOID) { // void
            deathLocation.getWorld().playSound(deathLocation, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 10.0f, 1.0f);
            event.deathMessage(Component.text(ChatColor.LIGHT_PURPLE + String.format("%s sacrificed themselves to the void", player.getName())));
        } else { // other
            deathLocation.getWorld().playSound(deathLocation, Sound.BLOCK_NOTE_BLOCK_PLING, 10.0f, 1.0f);
        }
        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();
        player.sendMessage(String.format("%syou died at %d %d %d", Utils.prefix, x, y, z));
        logger.info(String.format("%s died at %d %d %d in world '%s'", player.getName(), x, y, z, player.getWorld().getName()));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String brandName = player.getClientBrandName();
        if (!Objects.equals(brandName, "vanilla") && !Objects.equals(brandName, "optifine")) {
            if (brandName != null) {
                if (brandName.contains("lunarclient")) {
                    brandName = "lunar client";
                }
                player.sendMessage(String.format("%s%soh, you're using %s... that's nice", Utils.prefix, ChatColor.YELLOW, brandName));
                logger.warning(String.format("%s is using %s", player.getName(), brandName));
            } else {
                player.sendMessage(String.format("%s%si have no idea what client you're using", Utils.prefix, ChatColor.YELLOW));
                logger.warning(String.format("%s did not send their client brand name", player.getName()));
            }

        }
        if (Maintenance.getState()) {
            if (player.isOp()) {
                player.sendMessage(Utils.prefix + ChatColor.YELLOW + "warning: the server is currently in maintenance mode");
            } else {
                Maintenance.turnAwayPlayer(player, true);
                return;
            }
        }
        Sentry.addBreadcrumb(String.format("%s joined the game", player.getName()));
        LocalTime time;
        player.sendMessage(Utils.prefix + ChatColor.LIGHT_PURPLE + "welcome " + player.getName());
        time = LocalTime.now();
        player.sendMessage(Utils.prefix + ChatColor.LIGHT_PURPLE + "the server time is " + dtf.format(time));
        player.sendMessage(Utils.prefix + ChatColor.LIGHT_PURPLE + String.format("%d other players online", Bukkit.getOnlinePlayers().size() - 1));
        boolean lcState = Mizu.getInstance().getConfig().getBoolean("lucky-chunk");
        String lcMsg;
        if (lcState) {
            lcMsg = ChatColor.GREEN + "lucky chunk is enabled";
        } else {
            lcMsg = ChatColor.RED + "lucky chunk is not enabled";
        }
        player.sendMessage(Utils.prefix + lcMsg);
        if (LuckyChunk.isChunkSpawned()) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.isOp() || LuckyChunk.isChunkFound()) {
                    LuckyChunk.sendTeleportMessage(p);
                }
            }

        }
        if (LuckyChunk.isChunkDue()) {
            LuckyChunk.scheduleNewChunk();
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Sentry.addBreadcrumb(String.format("%s left the game", player.getName()));
        Location location = player.getLocation();
        logger.info(String.format("player %s logged out at %s %s %s in world '%s'", player.getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName()));
    }
    
    @EventHandler
    public void onPlayerSleep(PlayerDeepSleepEvent event) {
        if (event.getPlayer().getWorld().getGameRuleValue(GameRule.PLAYERS_SLEEPING_PERCENTAGE) <= 1 / Bukkit.getOnlinePlayers().size() * 100) {
            Bukkit.broadcast(Component.text(Utils.prefix + ChatColor.GRAY + "night skipped" + ChatColor.RESET));
        }
    }
    
    @EventHandler
    public void onPlayerKicked(PlayerKickEvent event) {
        Bukkit.broadcast(Component.text(Utils.prefix + String.format("%s%s was kicked: %s%s", ChatColor.RED, event.getPlayer().getName(), event.getCause().toString().toLowerCase(), ChatColor.RESET)));
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("mizu.magicwand"))
            return;
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && player.getInventory().getItemInMainHand().getType() == Material.CLOCK) {
            Block targetBlock = event.getClickedBlock();
            MagicWand.affectBlock(targetBlock);
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && player.getInventory().getItemInMainHand().getType() == Material.CLOCK) {
            Block targetBlock = event.getClickedBlock();
            MagicWand.resetBlock(targetBlock);
        }
    }

    @EventHandler
    public void onDimensionChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        Sentry.addBreadcrumb(String.format(ChatColor.GRAY + "%s travelled from %s to %s", player.getName(), event.getFrom().getName(), player.getWorld().getName()));
        Bukkit.broadcast(Component.text(String.format(ChatColor.GRAY + "%s: %s -> %s", player.getName(), event.getFrom().getName(), player.getWorld().getName())));
        if (!player.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY)) {
            player.sendMessage(Component.text(Utils.prefix + ChatColor.RED + "keep inventory is not enabled in this dimension!!!"));
        }
    }

    @EventHandler
    public void onPlayerSendCommand(PlayerCommandPreprocessEvent event) {
        if (event.getPlayer().hasPermission("minecraft.command.gamerule") && event.getMessage().startsWith("/gamerule")) {
            String[] commandParts = event.getMessage().split(" ");
            Bukkit.broadcast(Component.text(Utils.prefix + ChatColor.GRAY + "gamerule " + commandParts[1] + " changed to: " + commandParts[2]));
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!((TextComponent)event.getView().title()).content().equalsIgnoreCase(VaultManager.viewName)) {
            return;
        }
        VaultManager.getInstance().saveVault((Player) event.getPlayer(), event.getInventory());
        event.getPlayer().sendMessage(Utils.prefix + ChatColor.GREEN + "vault saved!");
    }
}
