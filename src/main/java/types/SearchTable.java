package types;

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
		throw new UnsupportedOperationException();
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