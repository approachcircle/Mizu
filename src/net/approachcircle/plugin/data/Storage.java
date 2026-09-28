package net.approachcircle.plugin.data;

import net.approachcircle.plugin.Mizu;
import net.approachcircle.plugin.Utils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public abstract class Storage<T> {
    protected final String filename;
    protected File underlyingFile;
    protected final String initialFileContents;
    protected boolean healthy;
    protected IOException initialisationFailedException;
    private static final Map<Class<?>, Storage<?>> storageMap = new HashMap<>();

    protected Storage(@NotNull String filename, @NotNull String initialFileContents) {
        this.filename = filename;
        this.initialFileContents = initialFileContents;
        this.healthy = tryInitialise();
    }

    private boolean tryInitialise() {
        try {
            underlyingFile = new File(Mizu.getInstance().getDataFolder().getAbsolutePath() + "\\" + filename);
            if (!underlyingFile.exists()) {
                underlyingFile.createNewFile();
                Files.writeString(underlyingFile.toPath(), initialFileContents);
            }
            return true;
        } catch (IOException e) {
            Utils.error(e, "an error occurred initialising storage for '%s' of type '%s'");
            initialisationFailedException = e;
        }
        return false;
    }

    // i'm so fucking scared
    @SuppressWarnings("unchecked")
    public static <S extends Storage<?>> S of(Class<S> clazz) {
        return (S) storageMap.computeIfAbsent(clazz, k -> {
            try {
                Constructor<S> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
            } catch (ReflectiveOperationException e) {
                Utils.error(e, String.format("failed to grab a storage instance of type '%s'", clazz.getSimpleName()));
                // this can only be caused by a programming error on my part, so it can't just randomly happen.
                // in this case, throw runtime
                throw new RuntimeException(e);
            }
        });
    }

    public abstract void add(T object) throws IOException;

    public abstract boolean remove(Predicate<T> predicate) throws IOException;

    public abstract void clear() throws IOException;

    public abstract List<T> list() throws IOException;

    public final void write(Object data, boolean append) throws IOException {
        if (!healthy && !(healthy = tryInitialise())) {
            throw initialisationFailedException;
        }
        writeRaw(data, append);
    }

    private void writeRaw(Object data, boolean append) throws IOException {
        try (FileWriter writer = new FileWriter(underlyingFile, append)) {
            writer.write(data.toString());
        }
    }

    public final String read() throws IOException {
        if (!healthy && !(healthy = tryInitialise())) {
            throw initialisationFailedException;
        }
        return readRaw();
    }

    private String readRaw() throws IOException {
        return Files.readString(underlyingFile.toPath());
    }
}
