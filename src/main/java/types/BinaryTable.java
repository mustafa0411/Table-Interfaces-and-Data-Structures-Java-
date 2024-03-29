package types;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import models.Row;
import models.StoredTable;
import models.Table;

public class BinaryTable implements StoredTable {

	/*
	 * Required fields for the Module
	 */

	private static final Path BASE_DIR = Path.of("db", "sub", "tables");
	private Path root, data, metadata, virtualRoot;
	private static final boolean CUSTOM_ENCODE = true;
	private static final boolean ZIP_ARCHIVE = true;
	private FileSystem zipFileSystem;


	/**
	 * Creates the base directories at the specified path.
	 *
	 * @param directory The path for which base directories are to be created.
	 */
	private void createBaseDirectories(Path directory) {
		try {
			Files.createDirectories(directory);
		} catch (FileAlreadyExistsException e) {
			// Directory already exists, do nothing
		} catch (IOException e) {
			throw new RuntimeException("Failed to create base directories.", e);
		}
	}


	/**
	 * Creates parent directories for the given path.
	 *
	 * @param path The path for which parent directories are to be created.
	 */
	private static void createParentDirectories(Path path) {
		try {
			Files.createDirectories(path.getParent());
		} catch (IOException e) {
			throw new RuntimeException("Failed to create parent directories.", e);
		}
	}


	/**
	 * Constructor for creating a BinaryTable with specified name and columns.
	 *
	 * @param name    The name of the BinaryTable.
	 * @param columns The list of columns for the BinaryTable.
	 */
	public BinaryTable(String name, List<String> columns) {
		try {
			if (ZIP_ARCHIVE) {
				this.root = BASE_DIR.resolve(name +".zip");
				createParentDirectories(root);

				this.zipFileSystem = FileSystems.newFileSystem(root, Map.of("create", "true"));
				this.virtualRoot = zipFileSystem.getPath("/");

				this.data = virtualRoot.resolve("data");
				createBaseDirectories(data);

				this.metadata = virtualRoot.resolve("metadata");
				createBaseDirectories(metadata);

				Path columnsFile = metadata.resolve("columns.txt");
				Files.write(columnsFile, columns);

			} else {
				this.root = BASE_DIR.resolve(name);
				createBaseDirectories(root);

				this.data = root.resolve("data");
				createBaseDirectories(data);

				this.metadata = root.resolve("metadata");
				createBaseDirectories(metadata);

				Path columnsFile = metadata.resolve("columns.txt");
				Files.write(columnsFile, columns);
			}

		} catch (IOException e) {
			throw new RuntimeException("Failed to create base directories.");
		}
	}


	/**
	 * Constructor for creating a BinaryTable with a specified name.
	 *
	 * @param name The name of the BinaryTable.
	 */
	public BinaryTable(String name) {
		try {
			// Initialize the root directory based on the ZIP_ARCHIVE flag
			this.root = ZIP_ARCHIVE ? BASE_DIR.resolve(name +".zip") : BASE_DIR.resolve(name);

			// Check if the root directory exists and is a directory
			if(!Files.exists(root) || !Files.isDirectory(root)) {
				throw new IllegalArgumentException("Table root directory does not exist: " + root);
			}

			// Zip archive flag
			// If ZIP_ARCHIVE is true, create a ZIP file system
			if (ZIP_ARCHIVE) {

				URI zipUri = URI.create("zip.file:" + root.toUri().getPath());

				this.zipFileSystem = FileSystems.newFileSystem(zipUri, Map.of());
				this.virtualRoot = zipFileSystem.getPath("/");

				this.data = virtualRoot.resolve("data");
				this.metadata = virtualRoot.resolve("metadata");

			} else {
				// If ZIP_ARCHIVE is false, use the original code to initialize directories
				this.data = root.resolve("data");
				this.metadata = root.resolve("metadata");
			}

		} catch (IOException e) {
			throw new RuntimeException("Failed to create base directories.");

		}
	}


	/**
	 * Clears the data and metadata directories associated with the BinaryTable.
	 */
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


	/**
	 * Writes an integer value to the specified path.
	 *
	 * @param path The path to write the integer value.
	 * @param i    The integer value to be written.
	 */
	private static void writeInt(Path path, int i)  {
		try {
			if (CUSTOM_ENCODE) {
				ByteBuffer buffer = ByteBuffer.allocate(4);
				buffer.putInt(i);
				var bytes = buffer.array();
				Files.write(path, bytes);

			} else {
				try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(path))){
					out.writeInt(i);
					out.flush();
					out.close();
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException("Failed to write integer to file: " + path, e);
		}
	}


	/**
	 * Reads an integer value from the specified path.
	 *
	 * @param path The path from which to read the integer value.
	 * @return The integer value read from the file.
	 */
	private static int readInt(Path path) {
		try {
			if (CUSTOM_ENCODE) {
				byte[] bytes = Files.readAllBytes(path);
				ByteBuffer buffer = ByteBuffer.wrap(bytes);
				return buffer.getInt();
			} else {
				try(ObjectInputStream in = new ObjectInputStream(Files.newInputStream(path))){
					return in.readInt();
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException("Failed to read integer from file: " + path, e);
		}
	}


	/**
	 * Writes a Row object to the specified path.
	 *
	 * @param path The path to write the Row object.
	 * @param row  The Row object to be written.
	 */
	private static void writeRow(Path path, Row row) {
		createParentDirectories(path);

		try {
			if (CUSTOM_ENCODE) {
				byte[] rowBytes = row.getBytes();
				Files.write(path, rowBytes);

			} else {
				try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(path))){
					out.writeObject(row);
					out.flush();
					out.close();
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException("Failed to write row to file: " + path, e);
		}
	}


	/**
	 * Reads a Row object from the specified path.
	 *
	 * @param path The path from which to read the Row object.
	 * @return The Row object read from the file.
	 */
	private static Row readRow(Path path) {
		try {
			if (CUSTOM_ENCODE) {
				byte[] bytes = Files.readAllBytes(path);
				return Row.fromBytes(bytes);

			} else {
				try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(path))) {
					return (Row) in.readObject();
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			throw new IllegalStateException("Failed to read row from file: " + path, e);
		}
	}


	/**
	 * Deletes a Row object file specified by the path.
	 *
	 * @param path The path of the Row object file to be deleted.
	 */
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


	/**
	 * Computes the digest of the given key using the SHA-1 algorithm.
	 *
	 * @param key The key for which to compute the digest.
	 * @return The hexadecimal digest of the key.
	 */
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


	/**
	 * Resolves the path based on the provided digest.
	 *
	 * @param digest The digest used to construct the path.
	 * @return The resolved path.
	 */
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


	/**
	 * Flushes the BinaryTable, ensuring data consistency after modifications.
	 */
	@Override
	// needed to set it to public instead of private because of Stored Table interface
	public void flush () {
		//Step 1:
		if (ZIP_ARCHIVE) {
			// Substep i: If Zip archive flag is true, close the Zip file system
			// Needed to add this block because of a consistent "FileSystemClosed" exception in the failure trace
			try {
				if (zipFileSystem != null && zipFileSystem.isOpen()) {
					zipFileSystem.close();
				}
			} catch (IOException e) {
				throw new RuntimeException("Failed to close the Zip file system: " + e.getMessage());
			}
			try {
				// Substep ii: Reinitialize the Zip file system at the corresponding path
				// Used "jar" instead of "zip" for compatibility with certain virtual file system libraries
				// Needed to use a jar file instead of zip because of a persistent Iterator failure
				URI zipUri = URI.create("jar:file:" + root.toUri().getPath());
				zipFileSystem = FileSystems.newFileSystem(zipUri, Map.of());
			} catch (FileSystemAlreadyExistsException e) {
				// Logs the error using System.err.println instead of throwing the exception
				System.err.println("File system already exists: " + e.getMessage());
				// This is for an already existing file system, same thing as the try block
				// A consistent "FileSystemAlreadyExists" exception kept getting thrown in the failure trace
				// Created a ZipFileSystem by obtaining the file system for a JAR file located at the given 'root' path.
				zipFileSystem = FileSystems.getFileSystem(URI.create("jar:file:" + root.toUri().getPath()));
			} catch (IOException e) {
				throw new RuntimeException("Failed to reinitialize the Zip file system.", e);
			}

			// Substep iii: Reinitializes the virtual root at / under the Zip file system
			virtualRoot = zipFileSystem.getPath("/");

			// Reinitialize the data and metadata directories under the virtual root
			data = virtualRoot.resolve("data");
			metadata = virtualRoot.resolve("metadata");
		}
	}


	/**
	 * Inserts or updates a row with the specified key and fields.
	 *
	 * @param key    The key of the row.
	 * @param fields The list of fields for the row.
	 * @return The list of fields from the previous row with the same key, or null if the key was not present.
	 */
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


	/**
	 * Retrieves the fields of the row with the specified key.
	 *
	 * @param key The key of the row to retrieve.
	 * @return The list of fields of the specified row, or null if the key was not present.
	 */
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


	/**
	 * Removes the row with the specified key from the BinaryTable.
	 *
	 * @param key The key of the row to be removed.
	 * @return The list of fields from the removed row, or null if the key was not present.
	 */
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


	/**
	 * Returns the degree of the BinaryTable, i.e., the number of columns.
	 *
	 * @return The degree of the BinaryTable.
	 */
	@Override
	public int degree() {
		return columns().size();
	}


	/**
	 * Returns the size of the BinaryTable, i.e., the number of rows.
	 *
	 * @return The size of the BinaryTable.
	 */
	@Override
	public int size() {
		return readInt(metadata.resolve("size"));
	}


	/**
	 * Returns the hash code of the BinaryTable, representing its fingerprint.
	 *
	 * @return The hash code of the BinaryTable.
	 */
	@Override
	public int hashCode() {
		return readInt(metadata.resolve("fingerprint"));
	}


	/**
	 * Compares the BinaryTable with another object for equality.
	 *
	 * @param obj The object to compare with the BinaryTable.
	 * @return True if the objects are equal, false otherwise.
	 */
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


	/**
	 * Returns an iterator over the rows of the BinaryTable.
	 *
	 * @return An iterator over the rows of the BinaryTable.
	 */
	@Override
	public Iterator<Row> iterator() {
		try {
			// If the Zip archive flag is true, call the flush method.
			if (ZIP_ARCHIVE) {
				flush();
			}

			return Files.walk(data)
					.filter(path -> Files.isRegularFile(path)) // Filter only regular files, not directories
					.map(path -> readRow(path))
					.iterator();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}


	/**
	 * Returns the name of the BinaryTable.
	 *
	 * @return The name of the BinaryTable.
	 */
	@Override
	public String name() {
		if (ZIP_ARCHIVE) {
			// If ZIP_ARCHIVE is true, return the name without the ".zip" extension
			return root.getFileName().toString().replace(".zip", "");
		} else {
			// If ZIP_ARCHIVE is false, return the name as usual
			return root.getFileName().toString();
		}
	}


	/**
	 * Returns the list of columns in the BinaryTable.
	 *
	 * @return The list of columns in the BinaryTable.
	 */
	@Override
	public List<String> columns() {
		try {
			return Files.readAllLines(metadata.resolve("columns.txt"));
		} catch (IOException e) {
			throw new IllegalStateException("Failed to create columns: " + e);
		}
	}


	/**
	 * Returns a string representation of the BinaryTable in tabular view.
	 *
	 * @return A string representation of the BinaryTable.
	 */
	@Override
	public String toString() {
		return toTabularView(false);
	}
}