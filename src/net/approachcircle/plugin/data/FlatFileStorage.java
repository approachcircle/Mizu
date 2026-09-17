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
    public void remove(Predicate<T> predicate) throws IOException {
        List<String> lines = rawLines();
        for (String line : lines) {
            System.out.println(line);
        }
        lines.removeIf(line -> predicate.test(deserialize(line)));
        for (String line : lines) {
            System.out.println(line);
        }
        writeLines(lines);
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
