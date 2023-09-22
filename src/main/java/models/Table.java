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
	default String toTabularView(boolean sorted) {
		// Create a StringBuilder to build the tabular view
		StringBuilder view = new StringBuilder();

		// Get an iterator for the rows, optionally sorted
		Iterator<Row> rowIterator = sorted ? sortedIterator() : iterator();

		// Define separators and header separator for formatting
		String separator = "+------------------+------------------+------------------+------------------+";
		String headerSeparator = "+------------------+------------------+------------------+------------------+";

		// Table name
		view.append("Table: ").append(name()).append("\n");

		// Header
		view.append(headerSeparator).append("\n");

		// Check the table name and set appropriate headers
		if (name().equals("Companies")) {
			view.append("| Key              | Name             | Position                            |");
		} else if (name().equals("ProductCatalog")) {
			view.append("| Product ID       | Name             | Price                               |");
		} else if (name().equals("Factions")) {
			view.append("| Moral            | Name             | Game                                |");
		}

		view.append("\n");
		view.append(headerSeparator).append("\n");

		// Rows
		while (rowIterator.hasNext()) {
			// Get the current row
			Row row = rowIterator.next();

			// Append the key with formatting
			view.append("| ").append(String.format("%-16s", row.key()));

			// Iterate through row fields and format them
			List<Object> rowFields = row.fields();
			for (Object field : rowFields) {
				// Format the field with appropriate spacing
				// If the field is too long, truncate and add ellipsis
				String formattedField = field == null ? "                  " : String.format("%-18s", field.toString());
				formattedField = formattedField.length() > 20 ? formattedField.substring(0, 15) + "..." : formattedField;
				// Append the formatted field
				view.append("| ").append(formattedField);
			}
			// End the row
			view.append("|").append("\n");
			// Add separator if there are more rows
			if (rowIterator.hasNext()) {
				view.append(separator).append("\n"); // Add separator if there are more rows
			}
		}
		// Add the final header separator
		view.append(headerSeparator).append("\n");
		// Return the formatted tabular view as a string
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
