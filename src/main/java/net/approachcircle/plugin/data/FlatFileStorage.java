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
        // size 0 -> add -> size 1 -> remove -> size 0
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

    /**
     * Performs a perfect in-lieu replacement on the target entry, overwriting it with the new entry provided. This is
     * (obviously) a destructive operation.
     * @param predicate The predicate to test against all entries, replacing the entry that evaluates to true
     * @param newEntry The new entry to replace the entry that matches the predicate
     * @throws IOException If an IO error occurs reading OR writing to the disk
     * @return true if the predicate matches an entry, or false if nothing changed
     */
    public boolean replace(Predicate<T> predicate, T newEntry) throws IOException {
        // to make this "replace" operation TRULY a replacement, i feel like removing then adding isn't good enough.
        // we want to actually replace the object exactly in lieu, that is, overwriting the line containing the old
        // entry on the exact line it sat before.
        List<String> lines = rawLines();
        boolean replacement = false;
        for (int i = 0; i < lines.size(); i++) {
            // test the predicate against each entry
            if (predicate.test(deserialize(lines.get(i)))) {
                // if it's a match, then we've found the entry to replace, and we will now forcefully overwrite
                // that exact line with our new entry
                lines.set(i, serialize(newEntry));
                replacement = true;
            }
        }
        if (replacement) {
            writeLines(lines);
        }
        return replacement;
    }

    /**
     * Lazily replaces an entry in storage by simply calling remove() then add(). This method will not perform
     * an in-lieu replacement of the entry, but will instead just delete the old one and add a new entry immediately
     * after.
     * @param predicate The predicate to test against all entries, replacing the entry that evaluates to true
     * @param newEntry The new entry to replace the entry that matches the predicate
     * @throws IOException If an IO error occurs reading OR writing to the disk
     */
    public void replaceLazy(Predicate<T> predicate, T newEntry) throws IOException {
        remove(predicate);
        add(newEntry);
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
