package types;

import java.io.File;
import java.io.FileOutputStream;
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
import org.dom4j.io.OutputFormat;
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

		try {
			if (path.toFile().length() == 0) {
				this.document = DocumentHelper.createDocument();
				Element rootElement = document.addElement("table");

				Element columnsElement = rootElement.addElement("columns"); // Add columns under root
				for (String column : columns) {
					columnsElement.addElement("column").setText(column);
				}

				rootElement.addElement("rows");

				flush();
			} else {
				this.document = new SAXReader().read(path.toFile());
			}
		} catch (DocumentException e) {
			throw new IllegalStateException("Invalid XML File", e);
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
		try (FileOutputStream fileOutputStream = new FileOutputStream(path.toFile())){
			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter writer = new XMLWriter(fileOutputStream, format);
			writer.write(document);
			writer.close();
		} catch (IOException e) {
			throw new IllegalStateException("Failed to write to the XML file.", e);
		}
	}

	public static String encodeField(Object field) {
		if (field instanceof Integer || field instanceof Boolean) {
			return field.toString();
		} else {
			return (String) field;
		}
	}


	private void encodeRow(Element rowElement, String key, List<Object> fields) {
		rowElement.addElement("key").setText(key);
		Element fieldsElement = rowElement.addElement("fields");

		// Updated to use encodeField to encode fields, handling null values
		for (Object field : fields) {
			if (field == null) {
				fieldsElement.addElement("field").setText("null");
			} else {
				fieldsElement.addElement("field").setText(encodeField(field));
			}
		}
	}

	private Row decodeRow(Element rowElement) {
		String key = rowElement.elementText("key");
		List<Object> fields = new ArrayList<>();

		for (Element fieldElement : rowElement.element("fields").elements("field")) {
			fields.add(decodeField(fieldElement.getText()));
		}

		return new Row(key, fields);
	}



	public static Object decodeField(String field) {
		// Assuming a simple case where the field can be a number, boolean, or string
		if (field.matches("-?\\d+")) {
			return Integer.parseInt(field);
		} else if (field.equalsIgnoreCase("true") || field.equalsIgnoreCase("false")) {
			return Boolean.parseBoolean(field);
		} else if (field.equalsIgnoreCase("null")) {
			return null;
		}
		else {
			return field;
		}
	}

	public Element toElement(String key, List<Object> fields) {
		// Use DocumentHelper to create a "row" element
		Element rowElement = DocumentHelper.createElement("row");

		// Add the corresponding "key" attribute to the "row" element
		rowElement.addAttribute("key", key);

		// For each field in the list of fields:
		for (Object field : fields) {
			// Add a child element to the "row" element
			Element fieldElement = rowElement.addElement("field");
			// Serialize the field type and value and set them as attributes
			fieldElement.addAttribute("type", field.getClass().getSimpleName());
			fieldElement.addAttribute("value", encodeField(field));
		}
		return rowElement;
	}


	public String keyOf(Element elem) {
		// Return the value of the "key" attribute
		return elem.attributeValue("key");
	}


	public List<Object> fieldsOf(Element elem){
		List<Object> fields = new ArrayList<>();

		// For each child element under the given elem:
		for (Element fieldElement : elem.elements("field")) {
			// Deserialize the field type and value
			String type = fieldElement.attributeValue("type");
			String value = fieldElement.attributeValue("value");

			fields.add(decodeField(type, value));

		}
		return fields;
	}



	@Override
	public List<Object> put(String key, List<Object> fields) {
		List<String> columns = columns();
		if (columns.size() != fields.size() + 1) {
			throw new IllegalArgumentException("Degree mismatch.");
		}

		Element rowsElement = document.getRootElement().element("rows");

		for (Element rowElement : rowsElement.elements("row")) {
			if (rowElement.elementText("key").equals(key)) {
				List<Object> oldFields = decodeRow(rowElement).fields();

				rowsElement.remove(rowElement);

				// Use encodeRow to construct the XML elements
				Element newRow = rowsElement.addElement("row");
				encodeRow(newRow, key, fields);
				flush();
				return oldFields;
			}
		}

		// Use encodeRow to construct the XML elements
		Element newRow = rowsElement.addElement("row");
		encodeRow(newRow, key, fields);
		flush();
		return null;
	}

	@Override
	public List<Object> get(String key) {
		Element rowsElement = document.getRootElement().element("rows");

		for (Element rowElement : rowsElement.elements("row")) {
			if (rowElement.elementText("key").equals(key)) {
				return decodeRow(rowElement).fields();
			}
		}
		return null;
	}

	@Override
	public List<Object> remove(String key) {
		Element rowsElement = document.getRootElement().element("rows");

		for (Element rowElement : rowsElement.elements("row")) {
			if (rowElement.elementText("key").equals(key)) {
				List<Object> oldFields = decodeRow(rowElement).fields();

				rowsElement.remove(rowElement);
				flush();
				return oldFields;
			}
		}
		return null;
	}


	@Override
	public int degree() {
		return columns().size();
	}

	@Override
	public int size() {
		Element rowsElement = document.getRootElement().element("rows");
		return rowsElement.elements("row").size();  // Count the number of 'row' elements
	}


	//use decode row hashcode instead of helper method for both JSON and XML.
	@Override
	public int hashCode() {
		int hashCodeSum = 0;

		Element rowsElement = document.getRootElement().element("rows");

		for (Element rowElement : rowsElement.elements("row")) {
			Row decodedRow = decodeRow(rowElement);

			// Calculate the hash code using the decoded Row object
			hashCodeSum += decodedRow.hashCode();
		}

		return hashCodeSum;
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
		List<Row> rowList = new ArrayList<>();
		Element rowsElement = document.getRootElement().element("rows");

		for (Element rowElement : rowsElement.elements("row")) {
			String key = rowElement.elementText("key");
			List<Object> fields = new ArrayList<>();

			for (Element fieldElement : rowElement.element("fields").elements("field")) {
				fields.add(decodeField(fieldElement.getText()));
			}

			Row row = new Row(key, fields);
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
		return toTabularView(false);
	}
}