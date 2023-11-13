package types;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

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

		if (path.toFile().length() == 0) {


			this.document = DocumentHelper.createDocument();
			Element rootElement = document.addElement("table");
			Element columnsElement = document.addElement("columns");

			for (String column : columns) {
				columnsElement.addElement("column").setText(column);
			}

			rootElement.addElement("rows");

			flush();
		} else {
			try {
				this.document = new SAXReader().read(path.toFile());
			} catch (DocumentException e) {
				throw new IllegalStateException("Invalid XML File", e);
			}
		}

	}

	public XMLTable(String name) {
		createBaseDirectories();
		this.path = BASE_DIR.resolve(name + ".xml");

		if (!path.toFile().exists()) {
			throw new IllegalStateException("Failed to create the XML file.");
		}

		try {
			this.document = new SAXReader().read(path.toFile());
		} catch (DocumentException e) {
			throw new IllegalStateException("Invalid XML File", e);
		}

	}

	@Override
	public void clear() {
		Element rowsElement = document.getRootElement().element("rows");
		rowsElement.elements("row").forEach(Element::detach);
		flush();
	}

	@Override
	public void flush() {
		try (FileWriter fileWriter = new FileWriter(path.toFile())){
			XMLWriter xmlWriter = new XMLWriter(fileWriter);
			xmlWriter.write(document);
			xmlWriter.close();
		} catch (IOException e) {
			throw new IllegalStateException("Failed to write to the XML file.", e);
		}
	}

	@Override
	public List<Object> put(String key, List<Object> fields) {
		if (degree() != fields.size()) {
			throw new IllegalArgumentException("Degree mismatch");
		}

		Element rowsElement = document. getRootElement().element("rows");

		for (Element rowElement : rowsElement.elements("row")) {
			if (rowElement.elementText("key").equals(key)) {
				List<Object> oldFields = new ArrayList<>();

				for (Element fieldElement : rowElement.element("fields").elements("field")) {
					oldFields.add(fieldElement.getText());
				}
				rowsElement.remove(rowElement);

				Element newRow = rowsElement.addElement("row");
				newRow.addElement("key").setText(key);
				Element fieldsElement = newRow.addElement("fields");

				for (Object field : fields) {
					fieldsElement.addElement("field").setText(field.toString());
				}
				flush();
				return oldFields;
			}
		}

		Element newRow = rowsElement.addElement("row");
		newRow.addElement("key").setText(key);
		Element fieldsElement = newRow.addElement("fields");

		for (Object field : fields) {
			fieldsElement.addElement("field").setText(field.toString());
		}

		flush();
		return null;
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
		Element rowsElement = document.getRootElement().element("rows");
		return rowsElement.elements("rows").size();
	}

	@Override
	public int hashCode() {
		int hash = 0;
		Element rowsElement = document.getRootElement().element("rows");
		for (Element rowElement : rowsElement.elements("rows")) {
			hash += rowElement.hashCode();
		}
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		XMLTable otherTable = (XMLTable) obj;

		return this.hashCode() == otherTable.hashCode();
	}

	@Override
	public Iterator<Row> iterator() {
		List <Row> rowList = new ArrayList<>();
		Element rowsElement = document.getRootElement().element("rows");
		for (Element rowElement : rowsElement.elements("rows")) {
			String key = rowElement.elementText("key");
			List<Object> fields = new ArrayList<>();
			for (Element fieldElement : rowElement.element("fields").elements("field")) {
				fields.add(fieldElement.getText());
			}
			Row row = new Row (key, fields);
			rowList.add(row);
		}
		return rowList.iterator();
	}

	@Override
	public String name() {
		return path.getFileName().toString().replace(".xml", "");
	}

	@Override
	public List<String> columns() {
		List<String> columnList = new ArrayList<>();
		Element columnsElement = document.getRootElement().element("columns");

		// Assuming each column is represented as a "column" element
		for (Element columnElement : columnsElement.elements("column")) {
			columnList.add(columnElement.getText());
		}

		return columnList;
	}


	@Override
	public String toString() {
		throw new UnsupportedOperationException();
	}
}
