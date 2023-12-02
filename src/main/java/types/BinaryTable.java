package types;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import models.Row;
import models.StoredTable;

public class BinaryTable implements StoredTable {
	/*
	 * TODO: For Module 6, finish this stub.
	 */



	private static final Path BASE_DIR = Path.of("db", "sub", "tables");
	private final Path tableRoot;
	private final Path dataDirectory;
	private final Path metadataDirectory;


	/**
	 * Creates necessary directories for the database files.
	 */

	private void createBaseDirectories() {
		try {
			Files.createDirectories(BASE_DIR);
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to create base directories.");
		}
	}

	public BinaryTable(String name, List<String> columns) {
		throw new UnsupportedOperationException();
	}

	public BinaryTable(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	private static void writeInt(Path path, int i) {
		throw new UnsupportedOperationException();
	}

	private static int readInt(Path path) {
		throw new UnsupportedOperationException();
	}

	private static void writeRow(Path path, Row row) {
		throw new UnsupportedOperationException();
	}

	private static Row readRow(Path path) {
		throw new UnsupportedOperationException();
	}

	private static void deleteRow(Path path) {
		throw new UnsupportedOperationException();
	}

	private String digestFunction(Object key) {
		throw new UnsupportedOperationException();
	}

	private Path pathOf(String digest) {
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
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> columns() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		throw new UnsupportedOperationException();
	}
}