package net.approachcircle.plugin.data;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

public abstract class FlatFileStorage<T> extends Storage<T> {
    public FlatFileStorage(String filename) {
        super(filename, "");
    }

    protected abstract String serialize(T entry);
    protected abstract T deserialize(String line);

    private List<String> rawLines() throws IOException {
        String data = read();
        if (data.isEmpty())
            return new ArrayList<>();
        else
            return new ArrayList<>(List.of(data.split(System.lineSeparator())));
    }

    private void writeLines(List<String> lines) throws IOException {
        write(String.join(System.lineSeparator(), lines) + System.lineSeparator(), false);
    }

    @Override
    public void add(T object) throws IOException {
        write(serialize(object) + System.lineSeparator(), true);
    }

    @Override
    public boolean remove(Predicate<T> predicate) throws IOException {
        List<String> lines = rawLines();
        boolean removalOccurred = lines.removeIf(line -> predicate.test(deserialize(line)));
        // not a huge fan of this particularly
        // the only reason we do this is that if we do something like:
        // add -> remove
        // and the resulting remove operation leaves no entries left, then any children storages extending this will
        // throw an exception trying to deserialize the empty file because there is a leftover newline character
        // which appears to be an entry, so deserializing the newline character throws an exception while attempting to
        // parse its tokens
        if (lines.isEmpty()) {
            clear();
        } else {
            writeLines(lines);
        }
        return removalOccurred;
    }

    @Override
    public void clear() throws IOException {
        write(initialFileContents, false);
    }

    @Override
    public List<T> list() throws IOException {
        String data = read();
        if (data.isEmpty())
            return List.of();
        else
            return data.lines().map(this::deserialize).toList();
    }
}
