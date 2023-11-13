package types;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import models.Row;
import models.StoredTable;

public class JSONTable implements StoredTable {

	//Required private fields

	private static final Path BASE_DIR = Path.of("db", "sub", "tables");
	private final Path path;
	private final ObjectNode tree;
	private static final JsonMapper mapper = JsonMapper.builder().build();


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


	/**
	 * Constructs a JSONTable with the given name and columns.
	 *
	 * @param name     The name of the table.
	 * @param columns  List of column names.
	 */
	@SuppressWarnings("deprecation")
	public JSONTable(String name, List<String> columns) {
		createBaseDirectories();
		this.path = BASE_DIR.resolve(name + ".json");
		File file = this.path.toFile();

		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		this.tree = mapper.createObjectNode();
		ObjectNode metadata = tree.putObject("metadata");
		ObjectNode data = tree.putObject("data");
		metadata.put("columns", mapper.valueToTree(columns));
		flush();

	}


	/**
	 * Constructs a JSONTable with the given name.
	 *
	 * @param name  The name of the table.
	 */
	public JSONTable(String name) {
		createBaseDirectories();
		this.path = BASE_DIR.resolve(name + ".json");
		File file = this.path.toFile();


		if (!file.exists()) {
			throw new IllegalArgumentException("Table does not exist.");
		}

		try {
			this.tree = (ObjectNode) mapper.readTree(file);
		} catch (IOException e) {
			throw new IllegalStateException("Invalid JSON data in the file.", e);
		}
	}


	/**
	 * Clears all data in the table.
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void clear() {
		tree.with("data").removeAll();
		flush();
	}


	/**
	 * Writes the current state of the table to a JSON file.
	 */
	@Override
	public void flush() {
		try {
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(path.toString()), tree);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Inserts or updates a row in the table.
	 *
	 * @param key     The identifier of the row.
	 * @param fields  List of field values.
	 * @return        Previous values if the row was updated, else null.
	 */
	@Override
	public List<Object> put(String key, List<Object> fields) {
		List<String> columns = columns();
		if (columns.size() != fields.size() + 1) {
			throw new IllegalArgumentException("Degree mismatch.");
		}

		ObjectNode dataNode = tree.with("data");

		if(dataNode.has(key)) {
			ObjectNode oldRow = (ObjectNode) dataNode.get(key);
			List<Object> oldFields = mapper.convertValue(oldRow.get("fields"), List.class);

			dataNode.remove(key);

			ObjectNode newRow = mapper.createObjectNode();
			newRow.put("key", key);
			newRow.set("fields", mapper.valueToTree(fields));
			dataNode.set(key, newRow);
			flush();
			return oldFields;
		} else {
			ObjectNode newRow = mapper.createObjectNode();
			newRow.put("key", key);
			newRow.set("fields", mapper.valueToTree(fields));
			dataNode.set(key, newRow);
			flush();
			return null;
		}
	}


	/**
	 * Retrieves the fields of a row using its key.
	 *
	 * @param key  The identifier of the row.
	 * @return     List of field values, or null if the row doesn't exist.
	 */
	@Override
	public List<Object> get(String key) {
		ObjectNode dataNode = tree.with("data");

		if (dataNode.has(key)) {
			ObjectNode row = ((ObjectNode) dataNode.get(key));
			return mapper.convertValue(row.get("fields"), List.class);
		} else {
			return null;
		}
	}


	/**
	 * Removes a row from the table using its key.
	 *
	 * @param key  The identifier of the row to be removed.
	 * @return     List of removed field values, or null if the row doesn't exist.
	 */
	@Override
	public List<Object> remove(String key) {
		ObjectNode dataNode = tree.with("data");

		if (dataNode.has(key)) {
			ObjectNode oldRow = (ObjectNode) dataNode.get(key);
			List<Object> oldFields = mapper.convertValue(oldRow.get("fields"), List.class);

			dataNode.remove(key);
			flush();

			return oldFields;
		} else {
			return null;
		}
	}


	/**
	 * Returns the number of columns in the table.
	 *
	 * @return The number of columns.
	 */
	@Override
	public int degree() {
		return columns().size();
	}


	/**
	 * Returns the number of rows in the table.
	 *
	 * @return The number of rows.
	 */
	@Override
	public int size() {
		if (tree.has("data")){
			return tree.get("data").size();
		} else {
			return 0;
		}
	}


	/**
	 * Generates a hash code for the table.
	 *
	 * @return The hash code for the table.
	 */
	@Override
	public int hashCode() {
		int hash = 0;

		if (tree.has("data") && tree.get("data").isObject()) {
			ObjectNode data = (ObjectNode) tree.get("data");
			Iterator<String> fieldNames = data.fieldNames();

			while (fieldNames.hasNext()) {
				String key = fieldNames.next();
				ObjectNode row = (ObjectNode) data.get(key);
				hash += hashRow(key, row);
			}
		}
		return hash;
	}


	/**
	 * Generates a hash code for a row.
	 *
	 * @param key  The identifier of the row.
	 * @param row  The row object.
	 * @return     The hash code for the row.
	 */
	private int hashRow(String key, ObjectNode row) {
		int result = (key != null) ? key.hashCode() : 0;

		if (row.has("fields")) {
			List<Object> fields = mapper.convertValue(row.get("fields"), List.class);
			result = 31 * result + ((fields != null) ? fields.hashCode() : 0);
		}
		return result;
	}


	/**
	 * Compares two tables for equality.
	 *
	 * @param obj  The object to compare.
	 * @return     True if the tables are equal, otherwise false.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true; // It's the same object
		}

		if (obj == null || getClass() != obj.getClass()) {
			return false; // It's a different class or null
		}

		JSONTable otherTable = (JSONTable) obj;

		// Compare the hash codes of the tables
		return this.hashCode() == otherTable.hashCode();
	}


	/**
	 * Provides an iterator for the table rows.
	 *
	 * @return An iterator for the table rows.
	 */
	@Override
	public Iterator<Row> iterator() {
		List<Row> rowList = new ArrayList<>();

		if (tree.has("data") && tree.get("data").isObject()) {
			ObjectNode data = (ObjectNode) tree.get("data");
			Iterator<String> fieldNames = data.fieldNames();

			while (fieldNames.hasNext()) {
				String key = fieldNames.next();
				ObjectNode property = (ObjectNode) data.get(key);
				List<Object> fields = mapper.convertValue(property.get("fields"), List.class);
				Row row = new Row(key, fields);
				rowList.add(row);
			}

		}
		return rowList.iterator();
	}


	/**
	 * Retrieves the name of the table.
	 *
	 * @return The name of the table.
	 */
	@Override
	public String name() {
		File file = this.path.toFile();
		return file.getName().replace(".json", "");
	}


	/**
	 * Retrieves the column names of the table.
	 *
	 * @return List of column names.
	 */
	@Override
	public List<String> columns() {
		if (tree.has("metadata") && tree.get("metadata").has("columns")) {
			return mapper.convertValue(tree.get("metadata").get("columns"), List.class);
		} else {
			return new ArrayList<>();
		}
	}


	/**
	 * Generates a tabular representation of the table.
	 *
	 * @return Tabular view of the table.
	 */
	@Override
	public String toString() {
		return toTabularView(false);
	}
}
