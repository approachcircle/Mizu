package net.approachcircle.plugin;

import io.papermc.paper.event.player.PlayerDeepSleepEvent;
import io.sentry.Sentry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.examination.string.StringExaminer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.logging.Logger;

public class MainListener implements Listener {
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        VillagerDetector.checkEntityType(event);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Logger logger = PluginLogger.getPluginLogger();
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
                if (p.isOp()) {
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
        Logger logger = PluginLogger.getPluginLogger();
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
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (GwenWalking.activated.contains(player.getUniqueId())) {
            GwenWalking.Walk(player);
        }
    }
}
