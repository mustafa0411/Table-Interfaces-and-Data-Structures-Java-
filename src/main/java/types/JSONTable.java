package types;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import models.Row;
import models.StoredTable;

public class JSONTable implements StoredTable {
	/*
	 * TODO: For Module 5, finish this stub.
	 */

	private static final Path BASE_DIR = Path.of("db", "sub", "tables");
	private final Path path;
	private final ObjectNode tree;
	private static final JsonMapper mapper = JsonMapper.builder().build();

	private void createBaseDirectories() {
		try {
			Files.createDirectories(BASE_DIR);
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to create base directories.");
		}
	}


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

	@SuppressWarnings("deprecation")
	@Override
	public void clear() {
		tree.with("data").removeAll();
		flush();
	}

	@Override
	public void flush() {
		try {
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(path.toString()), tree);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

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

	@Override
	public int degree() {
		return columns().size();
	}

	@Override
	public int size() {
		if (tree.has("data")){
			return tree.get("data").size();
		} else {
			return 0;
		}
	}

	@Override
	public int hashCode() {
		int hash = 0; //hashcode only works for fields, needs to check keys and make the rows out of the keys and fields
		if (tree.has("data")) {
			ObjectNode data = (ObjectNode) tree.get("data");
			Iterator<String> fieldNames = data.fieldNames();
			while (fieldNames.hasNext()) {
				String fieldName = fieldNames.next();
				String keyAndValue = fieldName + ":" + data.get(fieldName).asText();
				hash += keyAndValue.hashCode();
			}
		}
		return hash;
	}

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



	@Override
	public Iterator<Row> iterator() {
		List<Row> rowList = new ArrayList<>();

		if (tree.has("data")) {
			ArrayNode data = (ArrayNode) tree.get("data");
			for (int i = 0; i < data.size(); i++) {
				ObjectNode property = (ObjectNode) data.get(i);
				String key = property.get("key").asText();
				List<Object> fields = mapper.convertValue(property.get("fields"), List.class);
				Row row = new Row(key, fields);
				rowList.add(row);
			}
		}
		return rowList.iterator();
	}

	@Override
	public String name() {
		File file = this.path.toFile();
		return file.getName().replace(".json", "");
	}

	@Override
	public List<String> columns() {
		if (tree.has("metadata") && tree.get("metadata").has("columns")) {
			return mapper.convertValue(tree.get("metadata").get("columns"), List.class);
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public String toString() {
		return toTabularView(false);
	}
}
