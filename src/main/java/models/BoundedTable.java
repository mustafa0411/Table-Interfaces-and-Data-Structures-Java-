package models;

/**
 * An interface representing a bounded table that extends the basic table functionality.
 */

public interface BoundedTable extends Table {

	/**
	 * Returns the maximum capacity of the bounded table.
	 *
	 * @return The maximum capacity of the table
	 */
	public int capacity();


	/**
	 * Checks if the bounded table is full.
	 *
	 * @return True if the table is full, false otherwise
	 */
	public default boolean isFull() {
		return size() == capacity();
	}


	/**
	 * Calculates and returns the load factor of the bounded table.
	 *
	 * @return The load factor of the table as a double
	 */
	public default double loadFactor() {
		return (double) size()/capacity();
	}
}
