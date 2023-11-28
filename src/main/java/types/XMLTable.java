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
import models.Table;

public class XMLTable implements StoredTable {

	private static final Path BASE_DIR = FileSystems.getDefault().getPath("db", "sub", "tables");
	private final Path path;
	private Document document;


	/**
	 * Creates the base directories for storing XML files if they don't exist.
	 */
	public void createBaseDirectories(){
		File baseDirFile = BASE_DIR.toFile();
		if(!baseDirFile.exists()) {
			baseDirFile.mkdirs();
		}
	}


	/**
	 * Constructor for creating a new XMLTable with specified name and columns.
	 *
	 * @param name    The name of the table.
	 * @param columns The list of column names.
	 */
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


	/**
	 * Constructor for loading an existing XMLTable with the specified name.
	 *
	 * @param name The name of the table to load.
	 */
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


	/**
	 * Clears all rows in the table.
	 */
	@Override
	public void clear() {
		Element rowsElement = document.getRootElement().element("rows");
		rowsElement.elements("row").forEach(Element::detach);
		flush();
	}


	/**
	 * Flushes the document content to the XML file.
	 */
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


	/**
	 * Encodes a field to its string representation.
	 *
	 * @param field The field to encode.
	 * @return The string representation of the field.
	 */
	public static String encodeField(Object field) {
		if (field instanceof Integer || field instanceof Boolean) {
			return field.toString();
		} else {
			return (String) field;
		}

	}


	/**
	 * Decodes a field from its string representation.
	 *
	 * @param type  The type of the field.
	 * @param value The string representation of the field.
	 * @return The decoded field.
	 */
	public static Object decodeField(String type, String value) {
		// Simple decoding: based on the type, convert the string value back to the original type
		// Used switch cases due to simpler implementation

		//Used an if condition for checking nulls due to switch statements not detecting them.
		if(type.equals("null")) {
			return null;
		}

		switch (type) {
		case "String":
			return value;
		case "Integer":
			return Integer.parseInt(value);
		case "Double":
			return Double.parseDouble(value);
		case "Boolean":
			return Boolean.parseBoolean(value);
		default:
			throw new IllegalArgumentException("unssuported field type: " + type);
		}

	}


	/**
	 * Converts a key and list of fields to an XML element.
	 *
	 * @param key      The key of the row.
	 * @param fields   The list of fields.
	 * @param document The XML document.
	 * @return The XML element representing the row.
	 */
	public Element toElement(String key, List<Object> fields, Document document) {
		// Use DocumentHelper to create a "row" element
		Element rowElement = DocumentHelper.createElement("row");

		// Add the corresponding "key" attribute to the "row" element
		rowElement.addAttribute("key", key);

		// For each field in the list of fields:
		for (Object field : fields) {
			// Add a child element to the "row" element
			Element fieldElement = rowElement.addElement("field");

			if (field != null) {
				// Serialize the field type and value and set them as attributes
				fieldElement.addAttribute("type", field.getClass().getSimpleName());
				fieldElement.addAttribute("value", encodeField(field));
			} else {
				// Handle null fields gracefully
				fieldElement.addAttribute("type", "null");
				fieldElement.addAttribute("value", "null");
			}
		}

		return rowElement;
	}


	/**
	 * Retrieves the key attribute value from an XML element.
	 *
	 * @param elem The XML element.
	 * @return The value of the "key" attribute.
	 */
	public String keyOf(Element elem) {
		// Return the value of the "key" attribute
		return elem.attributeValue("key");
	}


	/**
	 * Retrieves the list of fields from an XML element.
	 *
	 * @param elem The XML element.
	 * @return The list of fields.
	 */
	public List<Object> fieldsOf(Element elem) {
		List<Object> fields = new ArrayList<>();

		// For each child element under the given elem:
		for (Element fieldElement : elem.elements("field")) {
			// Deserialize the field type and value
			String type = fieldElement.attributeValue("type");
			String value = fieldElement.attributeValue("value");
			// Add a corresponding field to the list of fields
			fields.add(decodeField(type, value));
		}

		return fields;
	}


	/**
	 * Adds a row with specified key and fields to the table.
	 *
	 * @param key    The key of the row.
	 * @param fields The list of fields.
	 * @return The list of fields from the replaced row if it existed, otherwise null.
	 */
	@Override
	public List<Object> put(String key, List<Object> fields) {
		List<String> columns = columns();
		if (columns.size() != fields.size() + 1) {
			throw new IllegalArgumentException("Degree mismatch.");
		}

		Element rowsElement = document.getRootElement().element("rows");

		for (Element rowElement : rowsElement.elements("row")) {
			if (keyOf(rowElement).equals(key)) {
				List<Object> oldFields = fieldsOf(rowElement);

				rowsElement.remove(rowElement);

				// Use encodeRow to construct the XML elements
				Element newRow = toElement(key, fields, document);
				rowsElement.add(newRow);

				flush();
				return oldFields;
			}
		}

		// Use encodeRow to construct the XML elements
		Element newRow = toElement(key, fields, document);
		rowsElement.add(newRow);

		flush();
		return null;
	}


	/**
	 * Retrieves the list of fields for the row with the specified key.
	 *
	 * @param key The key of the row.
	 * @return The list of fields, or null if the row does not exist.
	 */
	@Override
	public List<Object> get(String key) {
		Element rowsElement = document.getRootElement().element("rows");

		for (Element rowElement : rowsElement.elements("row")) {
			if (keyOf(rowElement).equals(key)) {
				return fieldsOf(rowElement);
			}
		}
		return null;
	}


	/**
	 * Removes the row with the specified key from the table.
	 *
	 * @param key The key of the row to remove.
	 * @return The list of fields from the removed row if it existed, otherwise null.
	 */
	@Override
	public List<Object> remove(String key) {
		Element rowsElement = document.getRootElement().element("rows");

		for (Element rowElement : rowsElement.elements("row")) {
			if (keyOf(rowElement).equals(key)) {
				List<Object> oldFields = fieldsOf(rowElement);

				rowsElement.remove(rowElement);
				flush();
				return oldFields;
			}
		}
		return null;
	}


	/**
	 * Retrieves the degree of the table (number of columns).
	 *
	 * @return The degree of the table.
	 */
	@Override
	public int degree() {
		return columns().size();
	}


	/**
	 * Retrieves the size of the table (number of rows).
	 *
	 * @return The size of the table.
	 */
	@Override
	public int size() {
		Element rowsElement = document.getRootElement().element("rows");
		return rowsElement.elements("row").size();  // Count the number of 'row' elements
	}


	/**
	 * Calculates the hash code for the table.
	 *
	 * @return The calculated hash code.
	 */
	@Override
	public int hashCode() {
		int hashCodeSum = 0;

		Element rowsElement = document.getRootElement().element("rows");

		for (Element rowElement : rowsElement.elements("row")) {
			List<Object> fields = fieldsOf(rowElement);
			Row decodedRow = new Row(keyOf(rowElement), fields);

			// Calculate the hash code using the decoded Row object
			hashCodeSum += decodedRow.hashCode();
		}

		return hashCodeSum;
	}


	/**
	 * Checks if this table is equal to another object.
	 *
	 * @param obj The object to compare.
	 * @return True if equal, false otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		Table otherTable = (Table) obj;

		return this.hashCode() == otherTable.hashCode();
	}


	/**
	 * Retrieves an iterator for the rows of the table.
	 *
	 * @return An iterator for the rows.
	 */
	@Override
	public Iterator<Row> iterator() {
		List<Row> rowList = new ArrayList<>();
		Element rowsElement = document.getRootElement().element("rows");

		for (Element rowElement : rowsElement.elements("row")) {
			String key = keyOf(rowElement);
			List<Object> fields = fieldsOf(rowElement);

			Row row = new Row(key, fields);
			rowList.add(row);
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
		return path.getFileName().toString().replace(".xml", "");
	}


	/**
	 * Retrieves the list of column names.
	 *
	 * @return The list of column names.
	 */
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


	/**
	 * Retrieves a tabular view of the table.
	 *
	 * @return A string representing the tabular view of the table.
	 */
	@Override
	public String toString() {
		return toTabularView(false);
	}
}