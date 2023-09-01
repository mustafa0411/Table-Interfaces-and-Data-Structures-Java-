package types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

import models.Row;
import models.Table;

/**
 * Represents a LookupTable implementation that stores key-value pairs. This
 * implementation uses an array to store rows of data.
 *
 * @author Mustafa Al-Shebeeb
 *
 * @version 1.0
 * @since 09-1-2023
 */

public class LookupTable implements Table {
	// initialize variables
	private Row[] array; // An array to store rows
	private int degree; // The degree of the table

	/**
	 * Constructs a LookupTable with the given degree.
	 *
	 * @param degree The degree of the table
	 */
	public LookupTable(int degree) {
		this.degree = degree; // initialize the degree field
		clear(); // Initialize the array
	}

	/**
	 * Clears the table by creating a new array of rows.
	 */
	@Override
	public void clear() {
		array = new Row[54]; // Changed the value from 26 to 54 to account for upper and lower case letters.
	}

	/**
	 * Helper method to find the index for a given key (a single character).
	 *
	 * @param key The key to find the index for
	 * @return The index corresponding to the key
	 */
	private int indexOf(String key) {
		if (key.length() != 1) {
			throw new IllegalArgumentException("Key must be length 1");
		}
		// lower case letters
		char c = key.charAt(0);
		if (c >= 'a' && c <= 'z') {
			return c - 'a';
		} else if (c >= 'A' && c <= 'Z') { // upper case letters
			return c - 'A' + 26; // shift 26 to reach upper case
		} else {
			throw new IllegalArgumentException("Key must be a lowercase or uppercase letter");
		}
	}

	/**
	 * Puts a key-value pair into the table.
	 *
	 * @param key    The key for the pair (must be a single character)
	 * @param fields The values associated with the key
	 * @return The previous values associated with the key, or null if the key was
	 *         not present
	 */
	@Override
	public List<Object> put(String key, List<Object> fields) {
		if (key.length() != 1) {// check the length of the keys
			throw new IllegalArgumentException("Key must be length 1");
		}

		if (fields.size() != degree - 1) { // Check the degree of the fields
			throw new IllegalArgumentException("Wrong number of fields. Expected degree - 1 fields.");
		}

		int i = indexOf(key);

		Row here = array[i];
		Row make = new Row(key, fields);

		if (here != null) {
			array[i] = make;
			return here.fields();
		}

		array[i] = make;
		return null;
	}

	/**
	 * Gets the values associated with a key.
	 *
	 * @param key The key to retrieve values for
	 * @return The values associated with the key, or null if the key is not present
	 */
	@Override
	public List<Object> get(String key) {
		int i = indexOf(key);
		if (array[i] != null) { // check for null before accessing
			return array[i].fields();
		}
		return null;
	}

	/**
	 * Removes a key and its associated values from the table.
	 *
	 * @param key The key to remove
	 * @return The values associated with the removed key, or null if the key was
	 *         not present
	 */
	@Override
	public List<Object> remove(String key) {
		int i = indexOf(key);

		Row here = array[i];

		if (here != null) {
			array[i] = null;
			return here.fields();
		}

		return null;
	}

	/**
	 * Returns the degree of the table.
	 *
	 * @return The degree of the table
	 */
	@Override
	public int degree() {
		return degree;
	}

	/**
	 * Returns the number of non-null rows in the table.
	 *
	 * @return The number of non-null rows in the table
	 */
	@Override
	public int size() {
		int size = 0;
		for (Row row : array) {
			if (row != null) {
				size++;
			}
		}
		return size;
	}

	/**
	 * Computes the hash code for the table.
	 *
	 * @return The hash code for the table
	 */
	@Override
	public int hashCode() {
		int fingerprint = 0;
		for (Row row : array) {
			if (row != null) {
				fingerprint += row.hashCode();
			}
		}
		return fingerprint;
	}

	/**
	 * Checks if two LookupTables are equal by comparing their arrays of rows.
	 *
	 * @param obj The object to compare to this table
	 * @return True if the tables are equal, false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof LookupTable)) {
			return false;
		}
		LookupTable other = (LookupTable) obj;
		return Arrays.equals(array, other.array);
	}

	/**
	 * Returns an iterator over the non-null rows in the table.
	 *
	 * @return An iterator over the non-null rows in the table
	 */
	@Override
	public Iterator<Row> iterator() {
		List<Row> rows = new ArrayList<>();
		for (Row row : array) {
			if (row != null) {
				rows.add(row);
			}
		}
		return rows.iterator();
	}

	/**
	 * Returns a string representation of the LookupTable.
	 *
	 * @return A string representation of the LookupTable
	 */
	@Override
	public String toString() {
		StringJoiner sj = new StringJoiner(", ", "LookupTable[", "]");
		for (Row row : array) {
			if (row != null) {
				sj.add(row.key() + "=" + row.fields());
			}
		}
		return sj.toString();
	}
}