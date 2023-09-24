package models;

import java.util.List;

/**
 * A record representing a row in a table with a key and a list of fields.
 */
public record Row(String key, List<Object> fields) {

	public static Row unmodFields(String key, List<Object> fields) {
		return new Row(key, List.copyOf(fields));
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
		int result = key.hashCode();
		result = 31 * result + fields.hashCode();
		return result;
	}

	public int compareTo(Row other) {
		return this.key.compareTo(other.key);
	}

}