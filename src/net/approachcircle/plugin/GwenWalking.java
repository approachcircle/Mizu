package net.approachcircle.plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GwenWalking {
    public static List<UUID> activated = new ArrayList<UUID>();
    private static final Integer startID = 10001;
    private static Integer currentID = startID;
    private static final Integer endID = 9800;
    private static final List<Integer> GuardianIds = new ArrayList<Integer>();
    private static final EntityType GuardianType = EntityType.GUARDIAN;
    private static Boolean overflowing = false;

    public static void Activate(Player player) {
        if (!Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
            player.sendMessage(Utils.prefix + ChatColor.RED + "protocollib is not enabled");
            return;
        }
        activated.add(player.getUniqueId());
    }

    public static void Deactivate(Player player) {
        if (!Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
            player.sendMessage(Utils.prefix + ChatColor.RED + "protocollib is not enabled");
            return;
        }
        activated.remove(player.getUniqueId());

        for (Player p : Bukkit.getOnlinePlayers()) {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);

            // let's just take a moment to appreciate how truly fucking abominable it
            // is to simply convert a List<Integer> to an int[]. you can't even cast an
            // Integer[] to an int[]. why the fuck not?
            int[] guardianIdsArray = GuardianIds.stream().mapToInt(i -> i).toArray();

            packet.getIntegers().writeSafely(0, guardianIdsArray.length - 1); // set number of entities to destroy
            packet.getIntegerArrays().writeSafely(0, guardianIdsArray); // set entity Ids to be destroyed
            ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet);
        }

        currentID = startID;
        GuardianIds.clear();
        overflowing = false;
    }

    //TODO: make work again (surely can do it right??????????????)
    public static void Walk(Player player) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);

            currentID--;

            if (currentID <= endID) {
                currentID = startID;
                overflowing = true;
            }

            packet.getIntegers().writeSafely(0, currentID); // set ID
            GuardianIds.add(currentID);
            if (overflowing) {
                GuardianIds.removeLast();
            }

            packet.getEntityTypeModifier().writeSafely(0, GuardianType); // set type

            packet.getUUIDs().writeSafely(0, UUID.randomUUID()); // set UUID

            packet.getDoubles()
                    .writeSafely(0, player.getLocation().getX())
                    .writeSafely(1, player.getLocation().getY())
                    .writeSafely(2, player.getLocation().getZ()); // set location

            ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet);
        }
    }
}
