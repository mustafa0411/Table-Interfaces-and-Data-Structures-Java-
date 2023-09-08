package models;

public interface BoundedTable extends Table {
	public int capacity();

	public default boolean isFull() {
		return size() == capacity();
	}

	public default double loadFactor() {
		return (double) size()/capacity();
	}
}
