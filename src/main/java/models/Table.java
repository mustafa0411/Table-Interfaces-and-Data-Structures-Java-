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
		throw new UnsupportedOperationException();
	}
}
