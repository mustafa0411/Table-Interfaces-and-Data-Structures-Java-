package models;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import types.HashTable;

/**
 * An interface representing a table that stores key-value pairs.
 */
public interface Table extends Iterable<Row> {

	default Table filter(Object target) {
		if (target == null) {
			throw new IllegalArgumentException("Target cannot be null");
		}

		Table partition = new HashTable(name() + "_parition",  columns());

		for (Row row : this) {
			boolean includeRow = false;

			if(row.key().equals(target) || row.key().toString().equals(target.toString())) {
				includeRow = true;
			}
			else {
				List<Object> rowFields = row.fields();
				for (Object field : rowFields) {
					if (field != null && (field.equals(target)) || field.toString().equals(target.toString())){
						includeRow = true;
						break;
					}
				}
			}
			if (includeRow) {
				partition.put(row.key(), row.fields());
			}
		}
		return partition;
	}

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

		// Retrieve table name and column names
		String tableName = name();
		List<String> columns = columns();

		// Calculate column widths based on column names and data
		int[] columnWidths = new int[columns.size()];
		Arrays.fill(columnWidths, 0); // Initialize to zero

		// Calculate column widths based on column names
		for (int i = 0; i < columns.size(); i++) {
			columnWidths[i] = Math.max(columnWidths[i], columns.get(i).length());
		}

		// Calculate column widths based on data
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			List<Object> rowFields = row.fields();

			for (int i = 0; i < rowFields.size() && i < columns.size(); i++) {
				Object field = rowFields.get(i);
				if (field != null) {
					columnWidths[i] = Math.max(columnWidths[i], field.toString().length());
				}
			}
		}

		// Create a format string for column alignment
		StringBuilder format = new StringBuilder("|");
		for (int width : columnWidths) {
			format.append(" %-" + (width + 2) + "s |"); // +2 for extra spacing
		}

		// Define separator, table top, and table bottom for formatting
		String separator = "+";
		String tableTop = "+";
		String tableBottom = "+";

		// Build separator, table top, and table bottom based on column widths
		for (int width : columnWidths) {
			separator += "+" + "-".repeat(width + 2); // +2 for extra spacing
			tableTop += "+" + "-".repeat(width + 2); // +2 for extra spacing
			tableBottom += "+" + "-".repeat(width + 2); // +2 for extra spacing
		}
		separator += "+";
		tableTop += "+";
		tableBottom += "+";

		// Table name
		view.append(tableName).append(":\n");

		// Add table top
		view.append(tableTop).append("\n");

		// Add column headers
		view.append(String.format(format.toString(), (Object[]) columns.toArray(new String[0]))).append("\n");

		// Add separator
		view.append(separator).append("\n");

		// Rows
		rowIterator = sorted ? sortedIterator() : iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();

			// Iterate through row fields and format them
			List<Object> rowFields = row.fields();
			for (int i = 0; i < rowFields.size() && i < columns.size(); i++) {
				Object field = rowFields.get(i);
				String formattedField = field == null ? "" : field.toString();
				view.append(String.format(" %-" + (columnWidths[i] + 2) + "s |", formattedField));
			}

			// End the row
			view.append("\n");

			// Add separator between rows
			if (rowIterator.hasNext()) {
				view.append(separator).append("\n");
			}
		}

		// Add table bottom
		view.append(tableBottom).append("\n");

		// Remove the last line separator
		view.deleteCharAt(view.length() - 1);

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
