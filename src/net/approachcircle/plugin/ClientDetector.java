package net.approachcircle.plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Bukkit;

import java.util.logging.Logger;

public class ClientDetector {
    private static final ProtocolManager pManager = ProtocolLibrary.getProtocolManager();
    private static final Logger logger = PluginLogger.getPluginLogger();

    public static void StartListening() {
        pManager.addPacketListener(new PacketAdapter(
                Mizu.getInstance(),
                ListenerPriority.NORMAL,
                PacketType.Play.Client.CUSTOM_PAYLOAD
        ) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Boolean vanilla = !packet.getBooleans().readSafely(0);
                if (vanilla) {
                    logger.info("client is vanilla, and did not understand the plugin channel request");
                } else {
                    int msgID = packet.getIntegers().readSafely(0);
                    logger.info("client is not vanilla, and understood the plugin channel request");
                    logger.info("client responded with message ID of: " + msgID);
                }
            }
        });
        logger.info("started listening for modded client packets");
    }
}
