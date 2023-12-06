package models;

import java.io.DataOutputStream;
import java.io.IOException;
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

	public byte[] getBytes() {
		// build the list of key followed by row values:
		// build a copy of the row (just the row) using a list constructor
		// then prepend the key to that copy of the row
		// the resulting list is the objects to be predicted/encoded

		// predict the # bytes to allocate for the buffer
		// for each field that will be in the record
		//			add the number of bytes needed to a running total
		// when done, the running total = # bytes to allocate

		//use a buffer to create the bytes array
		// allocate a byte buffer with the prediceted # bytes
		//for each field in the list of objects:
		//			check the type
		//			per type, determine the tag and remaining bytes, (if any)
		//			adding all bytes for tag/remainder to the buffer

		//return the array of the byte buffer

	}

	public static Row fromBytes(byte[] bytes) {
		// wrap a buffer around the given bytes array
		// build a new list of objects to be filled up with friends

		//while there are bytes remaining in the fiel:
		//		decode the tag, identify the type
		//		add the corresponding decoded data to the objects list

		//return a new pair composed of key/row

	}

	private static void encodeObject(Object obj, DataOutputStream dataStream) throws IOException {
		if (obj instanceof String) {
			byte[] stringBytes = ((String) obj).getBytes();
			dataStream.writeByte(stringBytes.length);
			dataStream.write(stringBytes);
		}else if (obj instanceof Integer) {
			dataStream.writeByte(-1); // unique tag for Integer
			dataStream.writeInt((Integer) obj);
		} else if (obj instanceof Double) {
			dataStream.writeByte(-2); // unique tag for Double/float
			dataStream.writeDouble((Double) obj);
		} else if (obj instanceof Boolean) {
			dataStream.writeByte((Boolean) obj ? -3 : -4); // unique tag for boolean -3/-4 -> true/false
		} else if (obj == null) {
			dataStream.writeByte(-5); // unique tag for null values
		}
	}


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