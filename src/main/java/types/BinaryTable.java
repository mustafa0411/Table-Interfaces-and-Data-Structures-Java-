package types;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;

import models.Row;
import models.StoredTable;
import models.Table;

public class BinaryTable implements StoredTable {

	/*
	 * Required fields for the Module
	 */

	private static final Path BASE_DIR = Path.of("db", "sub", "tables");
	private final Path root;
	private final Path data;
	private final Path metadata;


	private void createBaseDirectories(Path directory) {
		try {
			Files.createDirectories(directory);
		} catch (FileAlreadyExistsException e) {
			// Directory already exists, do nothing
		} catch (IOException e) {
			throw new RuntimeException("Failed to create base directories.", e);
		}
	}

	private static void createParentDirectories(Path path) {
		try {
			Files.createDirectories(path.getParent());
		} catch (IOException e) {
			throw new RuntimeException("Failed to create parent directories.", e);
		}
	}

	public BinaryTable(String name, List<String> columns) {
		try {
			this.root = BASE_DIR.resolve(name);
			createBaseDirectories(root);

			this.data = root.resolve("data");
			createBaseDirectories(data);

			this.metadata = root.resolve("metadata");
			createBaseDirectories(metadata);

			Path columnsFile = metadata.resolve("columns.txt");
			Files.write(columnsFile, columns);

		} catch (IOException e) {
			throw new RuntimeException("Failed to create base directories.");
		}
	}

	public BinaryTable(String name) {
		this.root = BASE_DIR.resolve(name);

		if(!Files.exists(root) || !Files.isDirectory(root)) {
			throw new IllegalArgumentException("Table root directory does not exist: " + root);
		}

		this.data = root.resolve("data");
		this.metadata = root.resolve("metadata");
	}

	@Override
	public void clear() {
		try {

			Files.walk(data)
			.skip(1)
			.sorted(Comparator.reverseOrder())
			.forEach(path -> {
				try {
					Files.delete(path);
				}
				catch (IOException e) {
					throw new IllegalStateException(e);
				}
			});

			writeInt(metadata.resolve("size"), 0);
			writeInt(metadata.resolve("fingerprint"), 0);


		} catch (IOException e) {
			throw new IllegalStateException("Failed to clear file: " + e);
		}
	}

	private static void writeInt(Path path, int i)  {
		try(ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(path))) {
			oos.writeObject(i);

		} catch (IOException e) {
			throw new IllegalStateException("Failed to write integer to file: " + path, e);
		}
	}

	private static int readInt(Path path) {
		try(ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
			return ois.readInt();

		} catch (IOException e) {
			throw new IllegalStateException("Failed to read integer from file: " + path, e);
		}
	}

	private static void writeRow(Path path, Row row) {
		createParentDirectories(path);

		try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(path))) {
			oos.writeObject(row);

		} catch (IOException e) {
			throw new IllegalStateException("Failed to write row to file: " + path, e);
		}
	}

	private static Row readRow(Path path) {
		try(ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
			return (Row) ois.readObject();

		} catch (IOException | ClassNotFoundException e) {
			throw new IllegalStateException("Failed to read row from file: " + path, e);
		}
	}

	private static void deleteRow(Path path) {
		try {
			Files.delete(path);
			Path parentDir = path.getParent();
			if (Files.list(parentDir).count() == 0) {
				Files.delete(parentDir);
			}

		} catch (IOException e) {
			throw new IllegalStateException("Failed to delete row file: " + path, e);
		}
	}

	private String digestFunction(Object key) {
		try {
			var sha1 = MessageDigest.getInstance("SHA-1");

			String saltedKey = "salt" + key;
			sha1.update(saltedKey.getBytes());
			var digest = sha1.digest();

			// Corrected method names: lowercase() and toString()
			var hex = HexFormat.of().withLowerCase();
			var hexString = hex.toString();

			return hexString;

		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	private Path pathOf(String digest) {
		String prefix = digest.substring(0, 2);
		String suffix = digest.substring(2);

		Path resolvedPath = Paths.get("data", prefix, suffix);

		return resolvedPath;
	}

	@Override
	public List<Object> put(String key, List<Object> fields) {
		if (degree() != fields.size() + 1) {
			throw new IllegalArgumentException("Degree mismatch.");
		}

		String digest = digestFunction(key);
		Path rowPath = pathOf(digest);

		if (Files.exists(rowPath)) {
			Row oldRow = readRow(rowPath);
			writeRow(rowPath, new Row(key, fields));
			writeInt(metadata.resolve("fingerprint"), hashCode());
			return oldRow.fields();
		} else {
			writeRow(rowPath, new Row(key, fields));
			writeInt(metadata.resolve("size"), size() + 1);
			writeInt(metadata.resolve("fingerprint"), hashCode());
			return null;
		}

	}

	@Override
	public List<Object> get(String key) {
		String digest = digestFunction(key);
		Path rowPath = pathOf(digest);

		if (Files.exists(rowPath)) {
			Row row = readRow(rowPath);
			return row.fields();
		} else {
			return null;
		}

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
		return readInt(metadata.resolve("size"));
	}

	@Override
	public int hashCode() {
		return readInt(metadata.resolve("fingerprint"));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true; // It's the same object
		}

		if (obj == null || getClass() != obj.getClass()) {
			return false; // It's a different class or null
		}

		Table otherTable = (Table) obj;

		// Compare the hash codes of the tables
		return this.hashCode() == otherTable.hashCode();
	}

	@Override
	public Iterator<Row> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String name() {
		return root.getFileName().toString();
	}

	@Override
	public List<String> columns() {
		try {
			return Files.readAllLines(metadata.resolve("columns.txt"));
		} catch (IOException e) {
			throw new IllegalStateException("Failed to create columns: " + e);
		}
	}

	@Override
	public String toString() {
		return toTabularView(false);
	}
}