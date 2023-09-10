package models;

import java.util.List;

/**
 * A record representing a row in a table with a key and a list of fields.
 */
public record Row(String key, List<Object> fields) {

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
	 * Gets the key of the row.
	 *
	 * @return The key of the row
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Gets the list of fields in the row.
	 *
	 * @return The list of fields in the row
	 */
	public List<Object> getFields() {
		return fields;
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
}