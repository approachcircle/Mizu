package net.approachcircle.plugin.effect;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MizuEffectRegistry {
    public final static HashMap<UUID, List<MizuEffect>> playerEffectMap = new HashMap<>();

    private final static List<MizuEffect> effectRegistry = new ArrayList<>();

    public static void registerEffects() {
        effectRegistry.add(new Immobilized());
        effectRegistry.add(new Silenced());
        effectRegistry.add(new Suppressed());
        effectRegistry.add(new Disarmed());
    }

    public static MizuEffect getEffect(MizuEffectType type) {
        for (MizuEffect effect : effectRegistry) {
            if (effect.type == type) {
                return effect;
            }
        }
        throw new IllegalArgumentException(String.format("effect of type '%s' is not in the registry! not good!", type.name()));
    }

    public static MizuEffect getEffect(String type) throws IllegalArgumentException {
        return MizuEffectRegistry.getEffect(Enum.valueOf(MizuEffectType.class, type.toUpperCase()));
    }

    public static void clearAllEffects() {
        for (MizuEffect effect : effectRegistry) {
            Bukkit.getOnlinePlayers().forEach(effect::clearEffect);
        }
        playerEffectMap.clear();
    }

    public static void clearEffectsOnPlayer(Player player) {
        for (MizuEffect effect : effectRegistry) {
            effect.clearEffect(player);
        }
    }
}
