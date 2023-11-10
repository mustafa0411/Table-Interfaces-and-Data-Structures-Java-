package types;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import models.Row;
import models.StoredTable;

public class XMLTable implements StoredTable {

	private static final Path BASE_DIR = FileSystems.getDefault().getPath("db", "sub", "tables");
	private final Path path;
	private Document document;

	public void createBaseDirectories(){
		File baseDirFile = BASE_DIR.toFile();
		if(!baseDirFile.exists()) {
			baseDirFile.mkdirs();
		}
	}

	public XMLTable(String name, List<String> columns) {
		createBaseDirectories();
		this.path = BASE_DIR.resolve(name + ".xml");

		if (!path.toFile().exists()) {
			try {
				path.toFile().createNewFile();
			} catch (IOException e) {
				throw new IllegalStateException("Failed to create the XML file.");
			}
		}

		this.document = DocumentHelper.createDocument();
		Element rootElement = document.addElement("table");
		Element columnsElement = document.addElement("columns");

		for (String column : columns) {
			columnsElement.addElement("column").setText(column);
		}

		rootElement.addElement("rows");

		flush();
	}

	public XMLTable(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void flush() {
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
