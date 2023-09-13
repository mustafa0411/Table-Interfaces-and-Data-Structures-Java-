package types;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import models.BoundedTable;
import models.Row;

public class SearchTable implements BoundedTable {
	/*
	 * TODO: For Module 1, finish this stub.
	 */

	// all private fields
	private Row[] tableArray; // Field 1
	private String name; // Field 2
	private List<String> columns; // Field 3
	private int degree; // Field 4
	private int size; // Field 5
	private int capacity; // Field 6
	private int fingerprint; // Field 7
	private static final int INITIAL_CAPACITY = 16; // Capacity constant

	public SearchTable(String name, List<String> columns) {
		this.name = name;
		this.columns = List.copyOf(columns);
		this.degree = columns.size();
		clear();
	}

	/**
	 * Clears the table by resetting capacity, size, and fingerprint.
	 */
	@Override
	public void clear() {
		capacity = INITIAL_CAPACITY;
		tableArray = new Row[capacity];
		size = 0;
		// Initialize fingerprint field to 0
		fingerprint = 0;
	}

	/**
	 * Puts a key-value pair into the table.
	 *
	 * @param key    The key for the pair
	 * @param fields The values associated with the key
	 * @return The previous values associated with the key, or null if the key was
	 *         not present
	 */
	@Override
	public List<Object> put(String key, List<Object> fields) {

		// Degree guard condition to make sure the degrees are the correct size, throws
		// exception other wise.
		if (fields.size() != degree - 1) {
			throw new IllegalArgumentException("Number of fields doesn't match the degree of the table.");
		}

		Row newRow = new Row(key, fields);
		int newRowHashCode = newRow.hashCode(); // Calculate the hash code of the new row

		// linear search the array
		for (int i = 0; i < size; i++) {
			if (tableArray[i].key().equals(key)) {
				List<Object> oldFields = tableArray[i].fields();
				int oldRowHashCode = tableArray[i].hashCode(); // Calculate the hash code of the old row
				tableArray[i] = newRow;
				fingerprint += (newRowHashCode - oldRowHashCode); // Update the fingerprint
				return oldFields;
			}
		}
		// condition to expan array
		if (size == capacity) {
			capacity *= 2;
			tableArray = Arrays.copyOf(tableArray, capacity);
		}

		tableArray[size] = newRow;
		size++;
		fingerprint += newRowHashCode; // Update the fingerprint
		return null;
	}

	/**
	 * Gets the values associated with a key.
	 *
	 * @param key The key to look up
	 * @return The values associated with the key, or null if the key is not present
	 */
	@Override
	public List<Object> get(String key) {
		// linear search the array
		for (int i = 0; i < size; i++) {
			// hit
			if (tableArray[i].key().equals(key)) {
				return tableArray[i].fields(); // Found the key, return its fields
			}
		}
		// miss
		return null;
	}

	/**
	 * Removes a key-value pair from the table.
	 *
	 * @param key The key to remove
	 * @return The values associated with the key before removal, or null if the key
	 *         is not present
	 */
	@Override
	public List<Object> remove(String key) {
		// linear search
		for (int i = 0; i < size; i++) {
			// condition for hit
			if (tableArray[i].key().equals(key)) {
				List<Object> oldFields = tableArray[i].fields();
				int removedRowHashCode = tableArray[i].hashCode(); // Calculate the hash code of the removed row
				tableArray[i] = tableArray[size - 1];
				tableArray[size - 1] = null;
				size--;
				fingerprint -= removedRowHashCode; // Update the fingerprint
				return oldFields;
			}
		}
		// otherwise return null for miss
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
	 * Returns the current size of the table.
	 *
	 * @return The current size of the table
	 */
	@Override
	public int size() {
		return size;
	}

	/**
	 * Returns the current capacity of the table.
	 *
	 * @return The current capacity of the table
	 */
	@Override
	public int capacity() {
		return capacity;
	}

	/**
	 * Returns the fingerprint value.
	 *
	 * @return The fingerprint value
	 */
	@Override
	public int hashCode() {
		return fingerprint;
	}

	/**
	 * Checks if the current table is equal to another BoundedTable based on the
	 * fingerprint.
	 *
	 * @param obj The object to compare to
	 * @return True if the tables are equal, false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		//if another table has the same fingerprint as this table then return true
		if (obj instanceof BoundedTable) {
			BoundedTable otherTable = (BoundedTable) obj;
			return this.fingerprint == otherTable.hashCode();
		}
		//unequal to table
		return false;
	}

	/**
	 * Returns an iterator for iterating over the rows in the table.
	 *
	 * @return An iterator for the table
	 */
	@Override
	public Iterator<Row> iterator() {
		return new Iterator<Row>() {
			private int currentIndex = 0;

			@Override
			public boolean hasNext() {
				return currentIndex < size;
			}

			@Override
			public Row next() {
				if (!hasNext()) {
					throw new java.util.NoSuchElementException();
				}
				return tableArray[currentIndex++];
			}
		};
	}

	/**
	 * Returns the name of the table.
	 *
	 * @return The name of the table
	 */
	@Override
	public String name() {
		return name;
	}

	/**
	 * Returns the list of column names in the table.
	 *
	 * @return The list of column names
	 */
	@Override
	public List<String> columns() {
		return columns;
	}

	/**
	 * Returns a string representation of the table.
	 *
	 * @return A string representation of the table
	 */
	@Override
	public String toString() {
		return Arrays.toString(tableArray);
	}
}