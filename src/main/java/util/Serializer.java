package main.java.util;

import org.jetbrains.annotations.NotNull;

import java.io.*;

/**
 * Utility class for serialization. In general, use the serializers and deserializer inside the content classes instead.
 */
public class Serializer {
    /**
     * @return the deserialized content
     * @throws SerializationException this is thrown if there is an issue with deserialization
     */
    public static <R extends Serializable> R deserialize(byte[] object) throws SerializationException {
        try {
            ByteArrayInputStream bi = new ByteArrayInputStream(object);
            ObjectInputStream oi = new ObjectInputStream(bi);

            //noinspection unchecked
            return (R) oi.readObject(); //Technically an unsafe cast. We catch the class cast exception though
        } catch (ClassCastException | IOException | ClassNotFoundException e) {
            throw new SerializationException("Could not deserialize: " + new String(object));
        }
    }

    /**
     * Requires that the input object is not null.
     *
     * @return the serialized content
     * @throws SerializationException if this is thrown, there was an issue with serialization
     */
    public static byte[] serialize(@NotNull Object object) throws SerializationException {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(object);
            oo.flush();
            return bo.toByteArray();
        } catch (IOException e) {
            throw new SerializationException("Could not serialize");
        }
    }

    public static class SerializationException extends Exception {
        public SerializationException(String message) {
            super(message);
        }
    }
}
