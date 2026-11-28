package net.approachcircle.plugin.data;

public class PlainTextStorage extends FlatFileStorage<String> {
    // super small storage type for basic line-by-line storage of singular words or phrases without special parsing
    // e.g. list of player UUIDs or a blacklist
    public PlainTextStorage(String filename) {
        super(filename);
    }

    @Override protected String serialize(String entry) { return entry; }

    @Override protected String deserialize(String line) { return line; }
}
