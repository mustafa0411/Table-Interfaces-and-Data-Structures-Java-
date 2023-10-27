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

	//Required Private Fields
	private static final Path BASE_DIR = Path.of("db", "sub", "tables");
	private Path path;

	/**
	 * Creates the base directories for storing CSV tables if they don't exist.
	 */
	private void createBaseDirectories() {
		try {
			Files.createDirectories(BASE_DIR);
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to create base directories.");
		}
	}
	/**
	 * Creates a new CSVTable with the specified name and column names.
	 *
	 * @param name     The name of the table.
	 * @param columns  A list of column names.
	 */
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

	/**
	 * Creates a new CSVTable with the specified name.
	 *
	 * @param name The name of the table.
	 */
	public CSVTable(String name) {
		createBaseDirectories();
		this.path = BASE_DIR.resolve(name + ".csv");

		if (!Files.exists(path)) {
			throw new IllegalArgumentException("Table file does not exist");
		}
	}
	/**
	 * Clears all data in the CSV table, leaving only the header row.
	 */
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

	/**
	 * Adds or updates a row in the CSV table with the specified key and fields.
	 *
	 * @param key    The key for the row.
	 * @param fields A list of field values for the row.
	 * @return The previous field values associated with the key, or null if it's a new row.
	 */
	@Override
	public List<Object> put(String key, List<Object> fields) {
		// Read all records (lines) from the flat file into a list of records.
		List<String> records;
		try {
			records = Files.readAllLines(path);
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to read records for put operation");
		}
		// Check if the degree of the new row matches the number of columns in the header.
		List<String> headerFields = List.of(records.get(0).split(","));
		if (headerFields.size() != fields.size() + 1) {
			throw new IllegalArgumentException("Degree of the new row does not match the header");
		}
		// Encode the new row composed of the key and fields.
		String newRecord = encodeRow(new Row(key, fields));

		// Linear Search for an old row with the same key (skip the header).
		for(int i = 1; i < records.size(); i++) {
			Row oldRow = decodeRow(records.get(i));
			if (oldRow.key().equals(key)) {
				// On a hit, remove the old record and prepend the new record.
				records.remove(i);
				records.add(1, newRecord);
				// Write the modified list of records to the flat file.
				try {
					Files.write(path, records);
				} catch (IOException e) {
					throw new IllegalArgumentException("Failed to write records after put operation");
				}
				// Return the old row.
				return oldRow.fields();
			}
		}
		// On a miss, prepend the new record to the list of records (still after the header).
		records.add(1, newRecord);
		// Write the modified list of records to the flat file.
		try {
			Files.write(path, records);
		} catch(IOException e) {
			throw new IllegalArgumentException("Failed to write records after put operation");
		}
		// Return null since there was no old row with the same key.
		return null;
	}
	/**
	 * Retrieves the field values associated with the given key.
	 *
	 * @param key The key for the row.
	 * @return The field values associated with the key, or null if the key is not found.
	 */
	@Override
	public List<Object> get(String key) {
		// Read all records (lines) from the flat file into a list of records.
		List<String> records;
		try {
			records = Files.readAllLines(path);
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to read records for get operation");
		}

		// Linear Search for an old row with the given key (skip the header).
		for(int i = 1; i < records.size(); i++) {
			Row oldRow = decodeRow(records.get(i));
			if (oldRow.key().equals(key)) {
				// On a hit, remove the old record and prepend it.
				records.remove(i);
				records.add(1, encodeRow(oldRow));
				// Write the modified list of records to the flat file.
				try {
					Files.write(path, records);
				} catch (IOException e) {
					throw new IllegalArgumentException("Failed to write records after put operation");
				}
				// Return the old row.
				return oldRow.fields();
			}
		}
		// On a miss, return null.
		return null;
	}
	/**
	 * Removes a row from the CSV table with the specified key.
	 *
	 * @param key The key for the row to be removed.
	 * @return The field values of the removed row, or null if the key is not found.
	 */
	@Override
	public List<Object> remove(String key) {
		// Read all records (lines) from the flat file into a list of records.
		List<String> records;
		try {
			records = Files.readAllLines(path);
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to read records for get operation");
		}

		// Linear Search for an old row with the given key (skip the header).
		for(int i = 1; i < records.size(); i++) {
			Row oldRow = decodeRow(records.get(i));
			if (oldRow.key().equals(key)) {
				// On a hit, remove the old record.
				records.remove(i);

				// Write the modified list of records to the flat file.
				try {
					Files.write(path, records);
				} catch (IOException e) {
					throw new IllegalArgumentException("Failed to write records after put operation");
				}
				// Return the old row.
				return oldRow.fields();
			}
		}
		return null;
	}
	/**
	 * Returns the degree (number of columns) of the CSV table.
	 *
	 * @return The degree of the CSV table.
	 */
	@Override
	public int degree() {
		return columns().size();
	}
	/**
	 * Returns the number of rows in the CSV table (excluding the header row).
	 *
	 * @return The number of rows in the table.
	 */
	@Override
	public int size() {
		try {
			List<String> records = Files.readAllLines(path);
			return records.size() - 1;
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to read the size");
		}
	}
	/**
	 * Computes the hash code for the CSV table.
	 *
	 * @return The hash code for the CSV table.
	 */
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

	/**
	 * Checks if this CSV table is equal to another object.
	 *
	 * @param obj The object to compare with.
	 * @return true if the objects are equal, false otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StoredTable) {
			StoredTable other = (StoredTable) obj;
			return this.name().equals(other.name()) && this.columns().equals(other.columns());
		}
		return false;
	}
	/**
	 * Encodes a field value to a string for writing to the CSV file.
	 *
	 * @param obj The field value to encode.
	 * @return The encoded string.
	 */
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
	/**
	 * Decodes a field value from a string read from the CSV file.
	 *
	 * @param field The string to decode into a field value.
	 * @return The decoded field value.
	 */
	private static Object decodeField(String field) {
		if (field.equalsIgnoreCase("null")) {
			return null;
		} else if (field.startsWith("\"") && field.endsWith("\"")) {
			// If the field starts and ends with quotation marks, return it as a string without the quotes.
			return field.substring(1, field.length() - 1);
		} else if (field.equalsIgnoreCase("true") || field.equalsIgnoreCase("false")) {
			return Boolean.parseBoolean(field);
		} else {
			try {
				if (field.contains(".")) {
					return Double.parseDouble(field);
				} else {
					return Integer.parseInt(field);
				}
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Unrecognized field: " + field);
			}
		}
	}

	/**
	 * Encodes a row into a CSV record string.
	 *
	 * @param row The row to encode.
	 * @return The encoded CSV record.
	 */
	private static String encodeRow(Row row) {
		StringJoiner joiner = new StringJoiner(",");
		joiner.add(encodeField(row.key()));
		for(Object field : row.fields()) {
			joiner.add(encodeField(field));
		}
		return joiner.toString();
	}
	/**
	 * Decodes a CSV record string into a row.
	 *
	 * @param record The CSV record string to decode.
	 * @return The decoded row.
	 */
	private static Row decodeRow(String record) {
		String[] fields = record.split(",");
		String key = (String) decodeField(fields[0]);
		List<Object> rowFields = new ArrayList<>();
		for (int i = 1; i < fields.length; i++) {
			rowFields.add(decodeField(fields[i]));
		}
		return new Row(key, rowFields);
	}

	/**
	 * Creates an iterator for the CSV table, allowing iteration through the rows.
	 *
	 * @return An iterator for the rows of the CSV table.
	 */
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

	/**
	 * Returns the name of the CSV table.
	 *
	 * @return The name of the table.
	 */
	@Override
	public String name() {
		return path.getFileName().toString().replace(".csv", "");
	}
	/**
	 * Returns the list of column names in the CSV table.
	 *
	 * @return The list of column names.
	 */
	@Override
	public List<String> columns() {
		try {
			List<String> header = Files.readAllLines(path);
			return List.of(header.get(0).split(","));
		} catch(IOException e) {
			throw new IllegalArgumentException("Failed to read columns");
		}
	}
	/**
	 * Returns a tabular view of the CSV table as a string.
	 *
	 * @return A string representing the tabular view of the CSV table.
	 */
	@Override
	public String toString() {
		return toTabularView(false);
	}
	/**
	 * Creates a new CSV table from the given name and text content.
	 *
	 * @param name The name of the table.
	 * @param text The text content representing the table.
	 * @return A new CSV table.
	 */
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
