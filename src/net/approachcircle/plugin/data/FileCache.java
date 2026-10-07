package net.approachcircle.plugin.data;

import net.approachcircle.plugin.Mizu;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class FileCache {
    private String contents = null;
    private boolean isValid = false;
    private final IOSupplier<String> contentsSupplier;
    private final int cacheExpirySeconds = 7;

    public FileCache(@NotNull IOSupplier<String> contentsSupplier) {
        this.contentsSupplier = contentsSupplier;
        // invalidate cache automatically every amount of seconds specified by the constant
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Mizu.getInstance(), this::invalidate, 0, cacheExpirySeconds * 20);
    }

    public void invalidate() {
        isValid = false;
    }

    public String get() throws IOException {
        if (!isValid || contents == null) {
            contents = contentsSupplier.get();
            isValid = true;
        }
        return contents;
    }

    public String getUncached() throws IOException {
        return contentsSupplier.get();
    }
}
