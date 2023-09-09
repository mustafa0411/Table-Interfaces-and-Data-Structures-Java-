package models;

import java.util.Iterator;
import java.util.List;

public interface Table extends Iterable<Row> {
	public void clear();

	public List<Object> put(String key, List<Object> fields);

	public List<Object> get(String key);

	public List<Object> remove(String key);

	public default boolean contains(String key) {
		return get(key) != null;
	}

	public int degree();

	public int size();

	public default boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);

	@Override
	public Iterator<Row> iterator();

	public String name();

	public List<String> columns();

	@Override
	public String toString();

	public default String toTabularView(boolean sorted) {
		// Implement the toTabularView method as described in the prompt
		// You can use a StringBuilder to build the tabular view
		StringBuilder view = new StringBuilder();

		// Check the 'sorted' flag and iterate through rows accordingly
		Iterator<Row> rowIterator = sorted ? sortedIterator() : iterator();

		// ... Implement the tabular view formatting logic here ...

		return view.toString();
	}
	default Iterator<Row> sortedIterator() {
		// Implement a sorted iterator based on your sorting criteria
		// This method should return an iterator that traverses rows in a sorted order
		// You can use a sorted collection or custom sorting logic here
		// ...

		return null; // Replace with your sorted iterator implementation
	}
}
