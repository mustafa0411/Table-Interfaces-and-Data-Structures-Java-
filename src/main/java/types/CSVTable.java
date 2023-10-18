package types;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import models.Row;
import models.StoredTable;

public class CSVTable implements StoredTable {
	/*
	 * TODO: For Module 4, finish this stub.
	 */
	private static final Path BASE_DIR = Path.of("db", "sub", "tables");
	private Path path;


	private void createBaseDirectories() {
		try {
			Files.createDirectories(BASE_DIR);
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to create base directories.");
		}
	}

	public CSVTable(String name, List<String> columns) {
		createBaseDirectories();
		this.path = BASE_DIR.resolve(name + ".csv");

		if (!Files.exists(path)) {
			try {
				Files.createFile(path);
				List<String> header = new ArrayList<>();
				header.add(String.join(",", columns));
				Files.write(path, header);

			}catch(IOException e){
				throw new IllegalArgumentException("Failed to create the table file.");
			}
		}
	}



	public CSVTable(String name) {
		createBaseDirectories();
		this.path = BASE_DIR.resolve(name + ".csv");

		if (!Files.exists(path)) {
			throw new IllegalArgumentException("Table file does not exist");
		}
	}

	@Override
	public void clear() {
		try {
			List<String> records = Files.readAllLines(path);
			List<String> newRecords = new ArrayList<>();
			newRecords.add(records.get(0));
			Files.write(path, newRecords);
		}catch(IOException e){
			throw new IllegalArgumentException("Failed to clear the table");
		}
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
		return columns().size();
	}

	@Override
	public int size() {
		try {
			List<String> records = Files.readAllLines(path);
			return records.size() - 1;
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to read the size");
		}
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StoredTable) {
			StoredTable other = (StoredTable) obj;
			return this.name().equals(other.name()) && this.columns().equals(other.columns());
		}
		return false;
	}

	@Override
	public Iterator<Row> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String name() {
		return path.getFileName().toString().replace(".csv", "");
	}

	@Override
	public List<String> columns() {
		try {
			List<String> header = Files.readAllLines(path);
			return List.of(header.get(0).split(","));
		} catch(IOException e) {
			throw new IllegalArgumentException("Failed to read columns");
		}
	}

	@Override
	public String toString() {
		throw new UnsupportedOperationException();
	}
}
