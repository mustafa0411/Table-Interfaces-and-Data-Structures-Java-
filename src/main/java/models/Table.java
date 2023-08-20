package models;

import java.util.Iterator;
import java.util.List;

public interface Table extends Iterable<Row> {
	public void clear();

	public List<Object> put(String key, List<Object> fields);
	public List<Object> get(String key);
	public List<Object> remove(String key);

	public int degree();
	public int size();

	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);

	@Override
	public Iterator<Row> iterator();

	@Override
	public String toString();

	public default boolean contains(String key) {
		throw new UnsupportedOperationException();
	}

	public default boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	public default String toTabularView(boolean sorted) {
		throw new UnsupportedOperationException();
	}
}
