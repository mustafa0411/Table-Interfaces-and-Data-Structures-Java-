package types;

import java.util.Iterator;
import java.util.List;

import models.BoundedTable;
import models.Row;

public class HashTable implements BoundedTable {
	private Row[] table;
	private String name;
	private List<String> columns;
	private int degree;
	private int size;
	private int capacity;
	private int fingerprint;
	private final static int INITIAL_CAPACITY = 997;
	/*
	 * TODO: For Modules 2 & 3, finish this stub.
	 */

	public HashTable(String name, List<String> columns) {
		this.name = name;
		this.columns = List.copyOf(columns);
		this.degree = columns.size();
		clear();
	}

	@Override
	public void clear() {
		capacity = INITIAL_CAPACITY;
		table = new Row[capacity];
		size = 0;
		fingerprint = 0;
	}

	private int hashFunction(String key) {
		String saltedKey = "salt" + key;

		int hash = 0;
		for(int i = 0; i < saltedKey.length(); i++) {
			char c = saltedKey.charAt(i);

			hash = (hash * 31 + c) % capacity;
		}
		return Math.floorMod(hash, capacity);
	}

	@Override
	public List<Object> put(String key, List<Object> fields) {
		if (fields.size() != degree - 1) {
			throw new IllegalArgumentException("Number of fields doesn't match the degree of the table.");
		}

		Row newRow = new Row(key, List.copyOf(fields));

		int index = hashFunction(key);
		int startIndex = index;

		while(table[index] != null) {
			if(table[index].key().equals(key)) {
				Row oldRow = table[index];
				table[index] = newRow;
				fingerprint += newRow.hashCode() - oldRow.hashCode();
				return oldRow.fields();
			}
			index = (index - 1) % capacity;

			if(index == startIndex) {
				throw new IllegalStateException("Array is Full.");
			}
		}
		table[index] = newRow;
		size++;
		fingerprint += newRow.hashCode();
		return null;
	}

	@Override
	public List<Object> get(String key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Object> remove(String key) {
		throw new UnsupportedOperationException();
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
		if (this == obj) {
			return true;
		}
		if(!(obj instanceof HashTable)) {
			return false;
		}
		HashTable otherTable = (HashTable) obj;
		return this.fingerprint == otherTable.fingerprint;
	}

	@Override
	public Iterator<Row> iterator() {
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
	}
}
