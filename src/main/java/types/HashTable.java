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
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Object> put(String key, List<Object> fields) {
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
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
