package types;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import models.Row;
import models.StoredTable;

public class JSONTable implements StoredTable {
	/*
	 * TODO: For Module 5, finish this stub.
	 */

	private static final String BASE_DIR = "db-sub-tables";
	private final String path;
	private final ObjectNode tree;
	private static final ObjectMapper mapper = new ObjectMapper();

	public JSONTable(String name, List<String> columns) {
		File baseDir = new File(BASE_DIR);
		if (!baseDir.exists()) {
			baseDir.mkdirs();
		}

		this.path = BASE_DIR + File.separator + name + ".json";

		File file = new File(this.path);
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
		this.path = BASE_DIR + File.separator + name + ".json";

		File file = new File(this.path);
		if (!file.exists()) {
			throw new IllegalArgumentException("Table does not exist.");
		}

		try {
			this.tree = (ObjectNode) mapper.readTree(file);
		} catch (IOException e) {
			throw new IllegalStateException("Invalid JSON data in the file.", e);
		}
	}

	@Override
	public void clear() {
		tree.with("data").removeAll();
		flush();
	}

	@Override
	public void flush() {
		try {
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(path), tree);
		} catch (IOException e) {
			e.printStackTrace();
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
