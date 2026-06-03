package net.approachcircle.plugin;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
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
    public void onMessageSent(AsyncChatEvent event) {
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        // effectively consume every chat message and broadcast it ourselves...
        event.setCancelled(true);
        String originalMessage = String.format("<%s> %s", event.getPlayer().getName(), message);
        Bukkit.broadcast(Component.text(originalMessage));
        String messageLanguage = TranslationClient.detect(message);
        if (messageLanguage == null) {
            logger.severe("failed to detect message language");
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            String playerLocale = Localisation.getClientLocaleOrExplicit(player);
            if (!Localisation.getClientLocaleOrExplicit(player).equalsIgnoreCase(messageLanguage)) {
                String translatedMessage = TranslationClient.translate(message, messageLanguage, playerLocale);
                String translatedChatMessage = String.format("<%s> %s", event.getPlayer().getName(), translatedMessage);
                logger.info(translatedChatMessage);
                player.sendMessage(Component.text(translatedChatMessage, NamedTextColor.GRAY, TextDecoration.ITALIC));
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntityType() == EntityType.VILLAGER) {
            VillagerDetector.villagerDied(event);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Location deathLocation = event.getPlayer().getLocation();
        Player player = event.getPlayer();
        if (Die.suicided.contains(player.getUniqueId())) { // suicide
            // set actual death message as null, and just broadcast a fake localised one to the correct players
            event.deathMessage(null);
            Localisation.broadcast(new KeyArgPair("player_suicided", player.getName()), false);
            deathLocation.getWorld().playSound(deathLocation, Sound.BLOCK_NOTE_BLOCK_PLING, 10.0f, 0.5f);
            Die.suicided.remove(player.getUniqueId());
        } else if (Casino.gambledLife.contains(player.getUniqueId())) { // gambling
            event.deathMessage(null);
            Localisation.broadcast(new KeyArgPair("player_gambled_life", player.getName()), false);
            deathLocation.getWorld().playSound(deathLocation, Sound.ENTITY_GUARDIAN_DEATH_LAND, 10.0f, 0.5f);
            Casino.gambledLife.remove(player.getUniqueId());
        } else if (player.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.VOID) { // void
            deathLocation.getWorld().playSound(deathLocation, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 10.0f, 1.0f);
            event.deathMessage(null);
            Localisation.broadcast(new KeyArgPair("player_fell_into_void", player.getName()), false);
        } else { // other
            deathLocation.getWorld().playSound(deathLocation, Sound.BLOCK_NOTE_BLOCK_PLING, 10.0f, 1.0f);
        }
        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();
        Localisation.sendMessage(player, new KeyArgPair("died_location", x, y, z));
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
                Localisation.sendMessage(player, new KeyArgPair("using_client_creepy_message", brandName));
                logger.warning(String.format("%s is using %s", player.getName(), brandName));
            } else {
                Localisation.sendMessage(player, new KeyArgPair("unidentified_client_message"));
                logger.warning(String.format("%s did not send their client brand name", player.getName()));
            }

        }
        if (Maintenance.getState()) {
            if (player.isOp()) {
                Localisation.sendMessage(player, new KeyArgPair("maintenance_mode_warning"));
            } else {
                Maintenance.turnAwayPlayer(player, true);
            }
        }
        Localisation.sendMessage(player, new KeyArgPair("welcome_message", player.getName()));
        Localisation.sendMessage(player, new KeyArgPair("server_time_message", dtf.format(LocalTime.now())));
        Localisation.sendMessage(player, new KeyArgPair("other_players_online", Bukkit.getOnlinePlayers().size() - 1));
        boolean lcState = Mizu.getInstance().getConfig().getBoolean("lucky-chunk");
        String enabledKey = lcState ? "lucky_chunk_enabled" : "lucky_chunk_not_enabled";
        player.sendMessage(Localisation.prefixComponent(new KeyArgPair(enabledKey), Localisation.getClientLocaleOrExplicit(player)));
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
        Location location = player.getLocation();
        logger.info(String.format("player %s logged out at %s %s %s in world '%s'", player.getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName()));
    }
    
//    @EventHandler
//    public void onPlayerSleep(PlayerDeepSleepEvent event) {
//        if (event.getPlayer().getWorld().getGameRuleValue(GameRule.PLAYERS_SLEEPING_PERCENTAGE) <= 1 / Bukkit.getOnlinePlayers().size() * 100) {
//            Bukkit.broadcast(Component.text(Utils.prefix + ChatColor.GRAY + "night skipped" + ChatColor.RESET));
//        }
//    }
    
    @EventHandler
    public void onPlayerKicked(PlayerKickEvent event) {
        Localisation.broadcast(new KeyArgPair("player_kicked_reason", event.getPlayer().getName(), event.getCause().toString().toLowerCase()));
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
        Bukkit.broadcast(Component.text(String.format("%s: %s -> %s", player.getName(), event.getFrom().getName(), player.getWorld().getName()), NamedTextColor.GRAY));
        if (!player.getWorld().getGameRuleValue(GameRules.KEEP_INVENTORY)) {
            Localisation.sendMessage(player, new KeyArgPair("keep_inventory_off_in_dimension"));
        }
    }

    @EventHandler
    public void onPlayerSendCommand(PlayerCommandPreprocessEvent event) {
        if (event.getPlayer().hasPermission("minecraft.command.gamerule") && event.getMessage().startsWith("/gamerule")) {
            String[] commandParts = event.getMessage().split(" ");
            Localisation.broadcast(new KeyArgPair("gamerule_changed_message", commandParts[1], commandParts[2]));
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!VaultManager.currentlyViewingVault.contains(event.getPlayer().getUniqueId())) {
            return;
        }
        VaultManager.currentlyViewingVault.remove(event.getPlayer().getUniqueId());
        VaultManager.getInstance().saveVault((Player) event.getPlayer(), event.getInventory());
        Localisation.sendMessage(event.getPlayer(), new KeyArgPair("vault_saved"));
        logger.info(String.format("%s closed their vault", event.getPlayer().getName()));
    }
}
