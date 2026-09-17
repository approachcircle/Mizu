package net.approachcircle.plugin.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

public class JSONStorage<T> extends Storage<T> {
    private final ObjectMapper mapper = new ObjectMapper();

    public JSONStorage(String filename) {
        super(filename, "[]");
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void add(T object) throws IOException {
        List<T> objects = list();
        objects.add(object);
        // could also make use of the mapper's built-in write method, but i'd rather use our own write which is guarded
        // against failures and bad states blah blah
        write(mapper.writeValueAsString(objects), false);
    }

    @Override
    public boolean remove(Predicate<T> predicate) throws IOException {
        List<T> objects = list();
        boolean removalOccurred = objects.removeIf(predicate);
        write(mapper.writeValueAsString(objects), false);
        return removalOccurred;
    }

    @Override
    public void clear() throws IOException {
        write(initialFileContents, false);
    }

    @Override
    public List<T> list() throws IOException {
        return mapper.readValue(underlyingFile, new TypeReference<>(){});
    }
}
