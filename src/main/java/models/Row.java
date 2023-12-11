package models;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A record representing a row in a table with a key and a list of fields.
 */
public record Row(String key, List<Object> fields) implements Serializable{


	/**
	 * Creates and returns a new Row object with unmodifiable fields.
	 *
	 */
	public Row{
		if(fields != null) {
			fields = Collections.unmodifiableList(new ArrayList<>(fields));
		}
	}


	/**
	 * Converts the key and fields of a Row into a byte array using a simple encoding scheme.
	 *
	 * @return The byte array representation of the Row.
	 */
	public byte[] getBytes() {
		// Create a list of objects with the key followed by fields
		List <Object> objectsToEncode = new ArrayList<>();
		objectsToEncode.add(key);
		objectsToEncode.addAll(fields);

		// Predict the total number of bytes needed
		int totalBytes = predictTotalBytes(objectsToEncode);

		// Allocate a byte buffer with the predicted number of bytes
		ByteBuffer buffer = ByteBuffer.allocate(totalBytes);

		// Encode each object and put the bytes into the buffer
		for (Object obj : objectsToEncode) {
			encodeObject(obj, buffer);
		}

		// Return the byte array from the buffer
		return buffer.array();
	}


	/**
	 * Decodes a byte array into a new Row object, extracting the key and fields.
	 *
	 * @param bytes The byte array to decode into a Row.
	 * @return A new Row object with the key and fields.
	 */
	public static Row fromBytes(byte[] bytes) {
		// Create a list of objects to be filled with decoded values
		List<Object> decodedObjects = new ArrayList<>();

		// Wrap a buffer around the given byte array
		ByteBuffer buffer = ByteBuffer.wrap(bytes);

		// While there are remaining bytes in the buffer, decode objects and add them to the list
		while (buffer.hasRemaining()) {
			decodedObjects.add(decodeObject(buffer));
		}

		// Return a new Row with the first object as the key and the rest as fields
		return new Row((String) decodedObjects.get(0), decodedObjects.subList(1, decodedObjects.size()));
	}


	/**
	 * Predicts the total number of bytes needed to encode the given list of objects.
	 *
	 * @param objects The list of objects to be encoded.
	 * @return The predicted total number of bytes.
	 */
	private int predictTotalBytes (List<Object> objects) {
		// Predict the total number of bytes needed to encode the list of objects
		int totalBytes = 0;
		for (Object obj : objects) {
			totalBytes += predictObjectBytes(obj);
		}
		return  totalBytes;
	}


	/**
	 * Predicts the number of bytes needed to encode a specific object.
	 *
	 * @param obj The object to be encoded.
	 * @return The predicted number of bytes needed.
	 */
	private int predictObjectBytes (Object obj) {
		// Predict the number of bytes needed to encode an object
		if (obj instanceof String) {
			return ((String) obj).getBytes().length + 1; // +1 for the tag byte
		} else if (obj instanceof Integer) {
			return Integer.BYTES + 1; // +1 for the tag byte
		} else if (obj instanceof Double) {
			return Double.BYTES + 1; // +1 for the tag byte
		} else if (obj instanceof Boolean || obj == null) {
			return 1; // Only a tag byte is needed for boolean and null values
		} else {
			throw new IllegalArgumentException("Unsupported object type: " + obj.getClass());
		}
	}


	/**
	 * Encodes a specific object and puts the bytes into the given ByteBuffer.
	 *
	 * @param obj    The object to be encoded.
	 * @param buffer The ByteBuffer to store the encoded bytes.
	 */
	private static void encodeObject(Object obj, ByteBuffer buffer) {
		if (obj instanceof String) {
			byte[] stringBytes = ((String) obj).getBytes();
			buffer.put((byte) stringBytes.length);
			buffer.put(stringBytes);
		} else if (obj instanceof Integer) {
			buffer.put((byte) -1); // unique tag for Integer
			buffer.putInt((Integer) obj);
		} else if (obj instanceof Double) {
			buffer.put((byte) -2); // unique tag for Double/float
			buffer.putDouble((Double) obj);
		} else if (obj instanceof Boolean) {
			buffer.put((Boolean) obj ? (byte) -3 : (byte) -4); // unique tag for boolean -3/-4 -> true/false
		} else if (obj == null) {
			buffer.put((byte) -5); // unique tag for null values
		}
	}


	/**
	 * Decodes an object from the given ByteBuffer.
	 *
	 * @param buffer The ByteBuffer containing the encoded bytes.
	 * @return The decoded object.
	 */
	private static Object decodeObject(ByteBuffer buffer) {
		byte tag = buffer.get();

		switch(tag) {
		case  -1: // unique tag for integer
			return buffer.getInt();
		case -2: // unique tag for double
			return buffer.getDouble();
		case -3: // unique tag for true
			return true;
		case -4: // unique tag for false
			return false;
		case -5: // unique tag for null
			return null;
		default: // default case for strings, step G* in sub step 3ii.
			byte[] stringBytes = new byte[tag];
			buffer.get(stringBytes);
			return new String(stringBytes);
		}
	}


	/**
	 * Returns a string representation of the row.
	 *
	 * @return A string representing the row in the format "key: fields"
	 */
	@Override
	public String toString() {
		return key + ": " + fields;
	}

	/**
	 * Computes the hash code for the row.
	 *
	 * @return The hash code of the row
	 */
	@Override
	public int hashCode() {
		int result = (key != null) ? key.hashCode() : 0;
		result = 31 * result + ((fields != null) ? fields.hashCode() : 0);
		return result;
		//have this original method call the static one

	}


	public int compareTo(Row other) {
		return this.key.compareTo(other.key);
	}

}