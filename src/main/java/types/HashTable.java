package types;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import models.BoundedTable;
import models.Row;

public class HashTable implements BoundedTable {
	private Row[] table;
	private String name;
	private List<String> columns;
	private int degree;
	private int size;
	private int capacity;
	private int fingerprint;
	private int contamination;
	private final static int INITIAL_CAPACITY = 997;
	private static final Row TOMBSTONE = new Row(null, null);
	private static final double LOAD_FACTOR_BOUND = 0.75;

	/**
	 * Constructor to initialize a new HashTable with a given name and columns.
	 *
	 * @param name    The name of the table.
	 * @param columns The list of column names.
	 */
	public HashTable(String name, List<String> columns) {
		this.name = name;
		this.columns = List.copyOf(columns);
		this.degree = columns.size();
		clear();
	}

	/**
	 * Clears the table, resetting it to its initial state.
	 */
	@Override
	public void clear() {
		capacity = INITIAL_CAPACITY;
		table = new Row[capacity];
		size = 0;
		fingerprint = 0;
		contamination = 0;
	}

	@Override
	public double loadFactor() {
		return (size + contamination) / (double) capacity;
	}

	private void rehash() {
		Row[] oldTable = table;
		int newCapacity = capacity * 2 + 1; // Double the capacity, add 1 so it's odd
		while (!isPrime(newCapacity)) {
			newCapacity += 2; // Keep adding 2 until it's prime again
		}

		table = new Row[newCapacity];
		size = 0;
		fingerprint = 0;
		contamination = 0;

		for (Row row : oldTable) {
			if (row != null && !row.equals(TOMBSTONE)) {
				put(row.key(), row.fields());
			}
		}
	}

	private boolean isPrime(int n) {
		if (n <= 1) {
			return false;
		}
		if (n <= 3) {
			return true;
		}
		if (n % 2 == 0 || n % 3 == 0) {
			return false;
		}
		for (int i = 5; i * i <= n; i += 6) {
			if (n % i == 0 || n % (i + 2) == 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Computes the hash value for a given key using the FNV-1a hash algorithm.
	 *
	 * @param key The key for which to compute the hash.
	 * @return The computed hash value.
	 */
	private int hashFunction2(String key) {
		String saltedKey = "salt" + key;

		int hash = fnvHash(saltedKey);

		return 1 + (hash % (capacity - 1));
	}

	/**
	 * Computes the FNV-1a hash value for a given string.
	 *
	 * @param str The input string.
	 * @return The computed hash value.
	 */
	private int fnvHash(String str) {
		final int fnvOffsetBasis = 0x811C9DC5;
		final int fnvPrime = 0x01000193;

		int hash = fnvOffsetBasis;
		for(int i =0; i < str.length(); i++) {
			char c = str.charAt(i);
			hash ^= c;
			hash *= fnvPrime;
		}
		return hash;
	}

	private int hashFunction1(String key) {

		String salt = "mySalt";

		String saltedKey = salt + key;

		int hashCode;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashBytes = digest.digest(saltedKey.getBytes(StandardCharsets.UTF_8));
			ByteBuffer buffer = ByteBuffer.wrap(hashBytes);
			hashCode = buffer.getInt();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-256 algorithm not available.");
		}
		return Math.floorMod(hashCode, capacity);
	}

	/**
	 * Inserts a new key-value pair into the table or updates an existing one.
	 *
	 * @param key    The key for the pair.
	 * @param fields The values associated with the key.
	 * @return The previous values associated with the key, or null if the key was not present.
	 * @throws IllegalArgumentException if the number of fields doesn't match the degree of the table.
	 */
	@Override
	public List<Object> put(String key, List<Object> fields) {
		if (fields == null || fields.size() != degree - 1) {
			throw new IllegalArgumentException("Number of fields doesn't match the degree of the table.");
		}

		if (loadFactor() > LOAD_FACTOR_BOUND) {
			rehash(); // Rehash if load factor exceeds the bound
		}

		Row newRow = new Row(key, fields);

		int index = hashFunction1(key);
		int startIndex = hashFunction2(key);
		int trackedTombstoneIndex = -1; // Track tombstone index

		while (table[index] != null && !table[index].equals(TOMBSTONE)) {
			if (table[index].key().equals(key)) {
				Row oldRow = table[index];
				table[index] = newRow;
				fingerprint += newRow.hashCode() - oldRow.hashCode();
				return oldRow.fields();
			}
			if (table[index].equals(TOMBSTONE) && trackedTombstoneIndex == -1) {
				trackedTombstoneIndex = index;
			}
			index = (index + startIndex) % capacity;

			if (index == startIndex) {
				throw new IllegalStateException("Array is Full.");
			}
		}

		if (trackedTombstoneIndex != -1) {
			table[trackedTombstoneIndex] = newRow;
			size++;
			contamination--;
			fingerprint += newRow.hashCode();
			return null;
		} else {
			table[index] = newRow;
			size++;
			fingerprint += newRow.hashCode();
			return null;
		}
	}


	/**
	 * Retrieves the values associated with a given key.
	 *
	 * @param key The key to search for.
	 * @return The values associated with the key, or null if the key was not found.
	 */
	@Override
	public List<Object> get(String key) {
		int index = hashFunction1(key);
		int startIndex = hashFunction2(key);
		while (table[index] != null && !table[index].equals(TOMBSTONE)) {
			if (table[index].key().equals(key)) {
				return table[index].fields();
			}
			index = (index + startIndex) % capacity;

			if (index == startIndex) {
				throw new IllegalStateException("Array is Full");
			}
		}
		return null;
	}

	@Override
	public List<Object> remove(String key) {
		int index = hashFunction1(key);
		int startIndex = hashFunction2(key);
		int trackedTombstoneIndex = -1; // Track tombstone index

		while (table[index] != null && !table[index].equals(TOMBSTONE)) {
			if (table[index].key().equals(key)) {
				Row oldRow = table[index];
				table[index] = TOMBSTONE;
				size--;
				contamination++;
				fingerprint += TOMBSTONE.hashCode() - oldRow.hashCode();
				return oldRow.fields();
			}
			if (table[index].equals(TOMBSTONE) && trackedTombstoneIndex == -1) {
				trackedTombstoneIndex = index;
			}
			index = (index + startIndex) % capacity;

			if (index == startIndex) {
				throw new IllegalStateException("Array is Full");
			}
		}

		if (trackedTombstoneIndex != -1) {
			table[trackedTombstoneIndex] = TOMBSTONE;
			size--;
			contamination++;
			fingerprint += TOMBSTONE.hashCode();
		}
		return null;
	}

	/**
	 * Returns the degree of the table.
	 *
	 * @return The degree of the table.
	 */
	@Override
	public int degree() {
		return degree;
	}

	/**
	 * Returns the size of the table.
	 *
	 * @return The size of the table.
	 */
	@Override
	public int size() {
		return size;
	}

	/**
	 * Returns the capacity of the table.
	 *
	 * @return The capacity of the table.
	 */
	@Override
	public int capacity() {
		return capacity;
	}

	/**
	 * Computes the hash code for the table.
	 *
	 * @return The hash code of the table.
	 */
	@Override
	public int hashCode() {
		return fingerprint;
	}

	/**
	 * Checks if the table is equal to another object.
	 *
	 * @param obj The object to compare to.
	 * @return True if the table is equal to the object, false otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if(!(obj instanceof HashTable)) {
			return false;
		}
		HashTable otherTable = (HashTable) obj;
		return this.fingerprint == otherTable.fingerprint;
	}

	/**
	 * Returns an iterator for the rows in the table.
	 *
	 * @return An iterator for the rows.
	 */
	@Override
	public Iterator<Row> iterator() {
		return new Iterator<Row>() {
			private int currentIndex = 0;

			@Override
			public boolean hasNext() {
				while (currentIndex < capacity) {
					if (table[currentIndex] != null && !table[currentIndex].equals(TOMBSTONE)) {
						return true;
					}
					currentIndex++;
				}
				return false;
			}

			@Override
			public Row next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				while (currentIndex < capacity && (table[currentIndex] == null || table[currentIndex].equals(TOMBSTONE))) {
					currentIndex++;
				}
				return table[currentIndex++];
			}
		};
	}

	/**
	 * Returns the name of the table.
	 *
	 * @return The name of the table.
	 */
	@Override
	public String name() {
		return name;
	}

	/**
	 * Returns the columns of the table.
	 *
	 * @return The list of column names.
	 */
	@Override
	public List<String> columns() {
		return columns;
	}

	/**
	 * Returns a string representation of the table.
	 *
	 * @return A string representation of the table.
	 */
	@Override
	public String toString() {
		return toTabularView(false);
	}
}