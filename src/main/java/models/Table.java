package models;

import java.util.Iterator;
import java.util.List;

/**
 * An interface representing a table that stores key-value pairs.
 */
public interface Table extends Iterable<Row> {

	/**
	 * Clears all entries in the table.
	 */
	public void clear();

	/**
	 * Puts a key-value pair into the table.
	 *
	 * @param key    The key for the pair
	 * @param fields The values associated with the key
	 * @return The previous values associated with the key, or null if the key was
	 *         not present
	 */
	public List<Object> put(String key, List<Object> fields);

	/**
	 * Retrieves the values associated with a given key.
	 *
	 * @param key The key to search for
	 * @return The values associated with the key, or null if the key was not found
	 */
	public List<Object> get(String key);

	/**
	 * Removes the key-value pair associated with a given key.
	 *
	 * @param key The key to remove
	 * @return The values associated with the removed key, or null if the key was
	 *         not found
	 */
	public List<Object> remove(String key);

	/**
	 * Checks if the table contains a specific key.
	 *
	 * @param key The key to search for
	 * @return True if the key is present in the table, false otherwise
	 */
	public default boolean contains(String key) {
		return get(key) != null;
	}

	/**
	 * Returns the degree of the table.
	 *
	 * @return The degree of the table
	 */
	public int degree();

	/**
	 * Returns the number of key-value pairs in the table.
	 *
	 * @return The size of the table
	 */
	public int size();

	/**
	 * Checks if the table is empty.
	 *
	 * @return True if the table is empty, false otherwise
	 */
	public default boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * Computes the hash code for the table.
	 *
	 * @return The hash code of the table
	 */
	@Override
	public int hashCode();

	/**
	 * Checks if the table is equal to another object.
	 *
	 * @param obj The object to compare to
	 * @return True if the table is equal to the object, false otherwise
	 */
	@Override
	public boolean equals(Object obj);

	/**
	 * Returns an iterator for the rows in the table.
	 *
	 * @return An iterator for the rows
	 */
	@Override
	public Iterator<Row> iterator();

	/**
	 * Returns the name of the table.
	 *
	 * @return The name of the table
	 */
	public String name();

	/**
	 * Returns the columns of the table.
	 *
	 * @return The list of column names
	 */
	public List<String> columns();

	/**
	 * Returns a string representation of the table.
	 *
	 * @return A string representation of the table
	 */
	@Override
	public String toString();

	/**
	 * Generates a tabular view of the table.
	 *
	 * @param sorted Flag indicating whether to sort rows in the view
	 * @return A formatted tabular view of the table
	 */
	public default String toTabularView(boolean sorted) {
		StringBuilder view = new StringBuilder();

		Iterator<Row> rowIterator = sorted ? sortedIterator() : iterator();

		// Implement formatting logic here

		return view.toString();
	}

	/**
	 * Returns a sorted iterator for the rows in the table.
	 *
	 * @return A sorted iterator for the rows
	 */
	default Iterator<Row> sortedIterator() {
		return null; // Placeholder for sorted iterator implementation
	}
}
