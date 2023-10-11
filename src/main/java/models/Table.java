package models;

import java.util.Iterator;
import java.util.List;

import types.HashTable;

/**
 * An interface representing a table that stores key-value pairs.
 */
public interface Table extends Iterable<Row> {

	/**
	 * Filters rows in the table based on a target value.
	 *
	 * @param target The target value used for filtering.
	 * @return A new table containing rows that match the target value.
	 * @throws IllegalArgumentException if the target value is null.
	 */
	default Table filter(Object target) {
		// Check if the target value is null and throw an exception if it is.
		if (target == null) {
			throw new IllegalArgumentException("Target cannot be null");
		}
		// Create a new table (partition) to store filtered rows.
		Table partition = new HashTable(name() + "_parition",  columns());
		// Iterate over each row in the current table.
		for (Row row : this) {
			boolean includeRow = false;
			// Check if the key of the row matches the target, or their string representations match.
			if(row.key().equals(target) || row.key().toString().equals(target.toString())) {
				includeRow = true;
			}
			else {
				List<Object> rowFields = row.fields();
				for (Object field : rowFields) {
					if (field != null && (field.equals(target)) || field.toString().equals(target.toString())){
						includeRow = true;
						break;// Break the loop if a match is found in the fields.
					}
				}
			}
			if (includeRow) {
				partition.put(row.key(), row.fields());
			}
		}
		// Return the filtered partition table.
		return partition;
	}

	default Table union (Table thatTable) {
		if (this.degree() != thatTable.degree()) {
			throw new IllegalArgumentException("Tables have different degrees");
		}

		Table unionTable = new HashTable(name() + "_union", columns());

		for (Row row : this) {
			unionTable.put(row.key(), row.fields());
		}

		for (Row row : this) {
			unionTable.put(row.key(), row.fields());
		}

		return unionTable;

	}

	default Table intersect (Table thatTable) {
		if (this.degree() != thatTable.degree()) {
			throw new IllegalArgumentException("Tables have different degrees");
		}

		Table intersectionTable = new HashTable(name() + "_intersection", columns());

		for (Row row : this) {
			String key = row.key();
			List <Object> fields = row.fields();

			if(thatTable.contains(key)) {
				intersectionTable.put(key, fields);
			}
		}

		return intersectionTable;
	}

	default Table minus(Table thatTable) {
		if (this.degree() != thatTable.degree()) {
			throw new IllegalArgumentException("Tables have different degrees");
		}

		Table differenceTable = new HashTable(name() + "difference", columns());

		for (Row row : this) {
			String key = row.key();

			if(!thatTable.contains(key)) {
				differenceTable.put(key, row.fields());
			}
		}

		return differenceTable;
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

		// Define separator and table top/bottom for formatting
		String separator = "+-----------------+-------------------+-------------------+";
		String tableTop = "+-----------------+-------------------+-------------------+";
		String tableBottom = "+-----------------+-------------------+-------------------+";

		// Table name
		view.append("Table: ").append(name()).append("\n");

		// Add table top
		view.append(tableTop).append("\n");

		if (name().equals("Companies")) {
			view.append("| Table#          | Name              | Position          |");
		} else if (name().equals("ProductCatalog")) {
			view.append("| Product ID      | Name              | Price             |");

		} else if (name().equals("Factions")) {
			view.append("| Moral           | Name              | Game              |");
		}

		view.append("\n");
		view.append(separator).append("\n");


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

		// Add table bottom
		view.append(tableBottom).append("\n");

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
