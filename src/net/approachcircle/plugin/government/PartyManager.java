package net.approachcircle.plugin.government;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.approachcircle.plugin.Mizu;
import net.approachcircle.plugin.PluginLogger;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class PartyManager {
    private final File partiesFile;
    private final Logger logger = PluginLogger.getPluginLogger();
    private static PartyManager instance = null;

    private PartyManager() {
        String partiesFileName = "parties.json";
        partiesFile = new File(Mizu.getInstance().getDataFolder().getAbsolutePath() + "\\" + partiesFileName);
        if (!partiesFile.exists()) {
            try {
                partiesFile.createNewFile();
                try (FileWriter writer = new FileWriter(partiesFile)) {
                    writer.write("[]");
                } catch (IOException e) {
                    logger.severe("failed to initialise parties file");
                    e.printStackTrace(System.err);
                }
            } catch (IOException e) {
                logger.severe("failed to create parties file");
                e.printStackTrace(System.err);
            }
        }
    }

    public static PartyManager getInstance() {
        if (instance == null) {
            instance = new PartyManager();
        }
        return instance;
    }

    @Nullable
    public List<Party> getParties() {
        List<Party> parties;
        ObjectMapper mapper = new ObjectMapper();
        try {
            parties = mapper.readValue(partiesFile, new TypeReference<>() {});
        } catch (IOException e) {
            logger.severe("failed to read parties file");
            e.printStackTrace(System.err);
            return null;
        }
        return parties;
    }

    public boolean createParty(String name, String color, UUID leader) {
        List<Party> parties = getParties();
        if (parties == null) {
            return false;
        }
        // parties.add(new Party(name, color, leader, List.of(leader)));
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(partiesFile, parties);
        } catch (IOException e) {
            logger.severe("failed to write parties file");
            e.printStackTrace(System.err);
            return false;
        }
        return true;
    }

    @Nullable
    public Party getPartyForPlayer(UUID player) {
        List<Party> parties = getParties();
        if (parties == null) {
            return null;
        }
//        for (Party party : parties) {
//            for (UUID member : party.members) {
//                if (member.equals(player)) {
//                    return party;
//                }
//            }
//        }
        return null;
    }
}
