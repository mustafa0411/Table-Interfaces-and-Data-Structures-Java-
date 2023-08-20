package models;

import java.io.Flushable;

public interface StoredTable extends Table, Flushable, AutoCloseable {
	public String getDisplayName();

	@Override
	public default void flush() {

	}

	@Override
	public default void close() {
		flush();
	}
}
