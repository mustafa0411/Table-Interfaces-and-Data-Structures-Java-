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

	private Row[] tableArray; // Field 1
	private String name; // Field 2
	private List<String> columns; // Field 3
	private int degree; // Field 4
	private int size; // Field 5
	private int capacity; // Field 6
	private int fingerprint;
	private static final int INITIAL_CAPACITY = 16;

	public SearchTable(String name, List<String> columns) {
		this.name = name;
		this.columns = List.copyOf(columns);
		this.degree = columns.size();
		clear();
	}

	@Override
	public void clear() {
		capacity = INITIAL_CAPACITY;
		tableArray = new Row[capacity];
		size = 0;
		fingerprint = 0;
	}

	@Override
	public List<Object> put(String key, List<Object> fields) {
		if(fields.size() != degree - 1) {
			throw new IllegalArgumentException("Number of fields doesn't match the degree of the table.");
		}

		Row newRow = new Row(key, fields);
		int newRowHashCode = newRow.hashCode(); // Calculate the hash code of the new row

		for (int i = 0; i < size; i++) {
			if (tableArray[i].getKey().equals(key)) {
				List<Object> oldFields = tableArray[i].getFields();
				int oldRowHashCode = tableArray[i].hashCode(); // Calculate the hash code of the old row
				tableArray[i] = newRow;
				fingerprint += (newRowHashCode - oldRowHashCode); // Update the fingerprint
				return oldFields;
			}
		}

		if(size == capacity) {
			capacity *= 2;
			tableArray = Arrays.copyOf(tableArray, capacity);
		}

		tableArray[size] = newRow;
		size++;
		fingerprint += newRowHashCode; // Update the fingerprint
		return null;
	}

	@Override
	public List<Object> get(String key) {
		for(int i = 0; i < size; i++) {
			if (tableArray[i].getKey().equals(key)) {
				return tableArray[i].getFields(); // Found the key, return its fields
			}
		}
		return null;
	}

	@Override
	public List<Object> remove(String key) {
		for (int i = 0; i < size; i++) {
			if(tableArray[i].getKey().equals(key)) {
				List<Object> oldFields = tableArray[i].getFields();
				int removedRowHashCode = tableArray[i].hashCode(); // Calculate the hash code of the removed row
				tableArray[i] = tableArray[size - 1];
				tableArray[size - 1] = null;
				size--;
				fingerprint -= removedRowHashCode; // Update the fingerprint
				return oldFields;
			}
		}
		return null;
	}

	@Override
	public int degree() {
		return degree;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public int capacity() {
		return capacity;
	}

	@Override
	public int hashCode() {
		return fingerprint;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BoundedTable) {
			BoundedTable otherTable = (BoundedTable) obj;
			return this.fingerprint == otherTable.hashCode();
		}
		return false;
	}

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

	@Override
	public String name() {
		return name;
	}

	@Override
	public List<String> columns() {
		return columns;
	}

	@Override
	public String toString() {
		return Arrays.toString(tableArray);
	}
}