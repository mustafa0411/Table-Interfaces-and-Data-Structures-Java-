package types;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import models.Row;
import models.StoredTable;

public class BinaryTable implements StoredTable {

	/*
	 * Required fields for the Module
	 */

	private static final Path BASE_DIR = Path.of("db", "sub", "tables");
	private final Path root;
	private final Path data;
	private final Path metadata;


	private void createBaseDirectories(Path directory) {
		try {
			Files.createDirectories(directory);
		} catch (FileAlreadyExistsException e) {
			// Directory already exists, do nothing
		} catch (IOException e) {
			e.printStackTrace(); // Handle the exception as appropriate
		}
	}

	public BinaryTable(String name, List<String> columns) {
		try {
			this.root = BASE_DIR.resolve(name);
			createBaseDirectories(root);

			this.data = root.resolve("data");
			createBaseDirectories(data);

			this.metadata = root.resolve("metadata");
			createBaseDirectories(metadata);

			Path columnsFile = metadata.resolve("columns.txt");
			Files.write(columnsFile, columns);

		} catch (IOException e) {
			throw new RuntimeException("Failed to create base directories.");
		}
	}

	public BinaryTable(String name) {
		this.root = BASE_DIR.resolve(name);

		if(!Files.exists(root) || !Files.isDirectory(root)) {
			throw new IllegalArgumentException("Table root directory does not exist: " + root);
		}

		this.data = root.resolve("data");
		this.metadata = root.resolve("metadata");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	private static void writeInt(Path path, int i)  {
		try(ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(path))){
			oos.writeObject(i);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static int readInt(Path path) {
		try(ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))){
			return ois.readInt();

		} catch (IOException e) {
			e.printStackTrace(); // Handle the exception as appropriate
			return 0; // Return default value if reading fails
		}
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