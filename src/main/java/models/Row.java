package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A record representing a row in a table with a key and a list of fields.
 */
public record Row(String key, List<Object> fields) {


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
	}

	public int compareTo(Row other) {
		return this.key.compareTo(other.key);
	}

}