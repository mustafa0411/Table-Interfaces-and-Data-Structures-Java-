package types;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
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
	private Path root, data, metadata;


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
		try(ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(path))) {
			out.writeInt(i);
			out.flush();
			out.close();

		} catch (IOException e) {
			throw new IllegalStateException("Failed to write integer to file: " + path, e);
		}
	}

	private static int readInt(Path path) {
		try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(path))) {
			return in.readInt();
		} catch (EOFException e) {
			// Handle the EOFException gracefully, possibly returning a default value
			return 0;
		} catch (IOException e) {
			throw new IllegalStateException("Failed to read integer from file: " + path, e);
		}
	}


	private static void writeRow(Path path, Row row) {
		createParentDirectories(path);

		try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(path))) {
			out.writeObject(row);
			out.flush();
			out.close();

		} catch (IOException e) {
			throw new IllegalStateException("Failed to write row to file: " + path, e);
		}
	}

	private static Row readRow(Path path) {
		try(ObjectInputStream in = new ObjectInputStream(Files.newInputStream(path))) {
			return (Row) in.readObject();

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

	private String digestFunction(String key) {
		try {
			var sha1 = MessageDigest.getInstance("SHA-1");

			sha1.update("salt-".getBytes());
			sha1.update(key.getBytes());

			var digest = sha1.digest();
			String hex = HexFormat.of().withLowerCase().formatHex(digest);

			return hex;

		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	private Path pathOf(String digest) {
		// Check if the digest is a valid hexadecimal string
		if (!digest.matches("[0-9a-fA-F]{40}")) {
			throw new IllegalArgumentException("Invalid digest format: " + digest);
		}

		// Extract the 2-character prefix and the 38-character suffix
		String prefix = digest.substring(0, 2);
		String suffix = digest.substring(2);

		// Resolve the path under the data directory
		return data.resolve(prefix).resolve(suffix);
	}



	@Override
	public List<Object> put(String key, List<Object> fields) {
		if (degree() != fields.size() + 1) {
			throw new IllegalArgumentException("Degree mismatch.");
		}

		String digest = digestFunction(key);
		Path rowPath = pathOf(digest);
		Row newRow = new Row(key, fields);


		if (Files.exists(rowPath)) {
			Row oldRow = readRow(rowPath);
			writeRow(rowPath, newRow);
			writeInt(metadata.resolve("fingerprint"), hashCode() - oldRow.hashCode() + newRow.hashCode());
			return oldRow.fields();
		} else {
			writeRow(rowPath, newRow);
			writeInt(metadata.resolve("size"), size() + 1);
			writeInt(metadata.resolve("fingerprint"), hashCode() + newRow.hashCode());
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
		String digest = digestFunction(key);
		Path rowPath = pathOf(digest);

		if (Files.exists(rowPath)) {
			Row oldRow = readRow(rowPath);
			deleteRow(rowPath);
			writeInt(metadata.resolve("size"), size() - 1);
			writeInt(metadata.resolve("fingerprint"), hashCode() -oldRow.hashCode());
			return oldRow.fields();
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
		try {
			return Files.walk(data)
					.filter(path -> Files.isRegularFile(path)) // Filter only regular files, not directories
					.map(path -> readRow(path))
					.iterator();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
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