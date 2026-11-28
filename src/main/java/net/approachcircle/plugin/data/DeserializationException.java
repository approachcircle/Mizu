package net.approachcircle.plugin.data;

/**
 * This exception, when thrown, indicates that an error occurred deserializing and constructing an object from data
 * on the disk. This might mean that, for example, the data on the disk is corrupted, or that something required
 * for deserialization is in an invalid state or is otherwise not working correctly.
 */
public class DeserializationException extends RuntimeException {
    public DeserializationException(String message) {
        super(message);
    }
}
