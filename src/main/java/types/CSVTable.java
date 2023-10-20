package types;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

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
		return fields;

	}

	@Override
	public List<Object> get(String key) {
		return null;

	}

	@Override
	public List<Object> remove(String key) {
		return null;

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
		int hashCodeSum = 0;
		try {
			List<String> records = Files.readAllLines(path);
			for (int i = 1; i < records.size(); i++) {
				Row row = decodeRow(records.get(i));
				hashCodeSum += row.hashCode();
			}
		} catch(IOException e){
			throw new IllegalArgumentException("Failed to read records for hashCode calculation");
		}
		return hashCodeSum;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StoredTable) {
			StoredTable other = (StoredTable) obj;
			return this.name().equals(other.name()) && this.columns().equals(other.columns());
		}
		return false;
	}

	private static String encodeField(Object obj) {
		if (obj == null) {
			return "null";
		} else if (obj instanceof String) {
			return "\"" + obj.toString() + "\"";
		} else if (obj instanceof Boolean || obj instanceof Integer || obj instanceof Double) {
			return obj.toString();
		} else {
			throw new IllegalArgumentException("Unsupported field type: " + obj.getClass().getName());
		}
	}

	private static Object decodeField(String field) {
		if (field.equalsIgnoreCase("null")) {
			return null;
		} else if (field.startsWith("\"") && field.endsWith("\"")) {
			return field.substring(1, field.length() - 1);
		} else if (field.equalsIgnoreCase("true") || field.equalsIgnoreCase("false")) {
			return Boolean.parseBoolean(field);
		} else {
			try {
				if(field.contains(".")) {
					return Double.parseDouble(field);
				} else {
					return Integer.parseInt(field);
				}
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Unrecognized field: " + field);
			}
		}
	}

	private static String encodeRow(Row row) {
		StringJoiner joiner = new StringJoiner(",");
		joiner.add(encodeField(row.key()));
		for(Object field : row.fields()) {
			joiner.add(encodeField(field));
		}
		return joiner.toString();
	}

	private static Row decodeRow(String record) {
		String[] fields = record.split(",");
		String key = (String) decodeField(fields[0]);
		List<Object> rowFields = new ArrayList<>();
		for(int i = 1; i < fields.length; i++) {
			rowFields.add(decodeField(fields[i]));
		}
		return  new Row(key, rowFields);
	}

	@Override
	public Iterator<Row> iterator() {
		// Create a list of rows decoded from all records in the flat file, excluding the header.
		List<Row> rows = new ArrayList<>();
		try {
			List<String> records = Files.readAllLines(path);
			for (int i = 1; i < records.size(); i++) {
				rows.add(decodeRow(records.get(i)));
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to read records for iterator");
		}
		// Return the iterator for the list of rows
		return rows.iterator();
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
		return toTabularView(false);
	}

	public static CSVTable fromText(String name, String text) {
		// Create the base directories, if needed.
		try {
			Files.createDirectories(BASE_DIR);
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to create base directories.");
		}

		// Create a path to a new file by resolving the given name plus the .csv extension relative to the base directories.
		Path filePath = BASE_DIR.resolve(name + ".csv");

		// If the new file doesn't exist at the resolved path, create it.
		if (!Files.exists(filePath)) {
			try {
				Files.createFile(filePath);
				Files.write(filePath, text.getBytes());
			} catch (IOException e) {
				throw new IllegalArgumentException("Failed to create the table file.");
			}
		}

		// Return a new CSV table by calling the 1-ary constructor and passing it the given name.
		return new CSVTable(name);
	}

}
