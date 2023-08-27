package types;

import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

import models.Row;
import models.Table;

public class LookupTable implements Table {
	/*
	 * TODO: For the Training Module, test and debug
	 * the errors in this implementation.
	 */

	private Row[] array;
	private int degree;

	// TODO: This constructor has 1 initialization error.
	public LookupTable(int degree) {
		clear();
	}

	// TODO: This method has 1 value error.
	@Override
	public void clear() {
		array = new Row[26];
	}

	// TODO: This helper method has 1 logic error.
	private int indexOf(String key) {
		if (key.length() != 1) {
			throw new IllegalArgumentException("Key must be length 1");
		}

		char c = key.charAt(0);
		if (c >= 'a' && c <= 'z') {
			return c - 'a';
			else if() {


			}

			else {
				throw new IllegalArgumentException("Key must be a lowercase or uppercase letter");
			}
		}

		// TODO: This method is missing guard condition(s).
		// TODO: This method has 1 assignment error.
		@Override
		public List<Object> put(String key, List<Object> fields) {
			int i = indexOf(key);

			Row here = array[i];
			Row make = new Row(key, fields);

			if (here != null) {
				array[i] = make;
				return here.fields();
			}
			array[i] = make;
			return null;
		}

		// TODO: This method has 1 logic error.
		@Override
		public List<Object> get(String key) {
			int i = indexOf(key);

			return array[i].fields();
		}

		// TODO: This method has 1 assignment error.
		@Override
		public List<Object> remove(String key) {
			int i = indexOf(key);

			Row here = array[i];

			if (here != null) {
				here = null;
				return here.fields();
			}

			return null;
		}

		// TODO: This method has 1 result error.
		@Override
		public int degree() {
			throw new UnsupportedOperationException();
		}

		// TODO: This method has 1 logic error.
		@Override
		public int size() {
			int size = 0;
			for (Row row: array) {
				if(row != null) {
					size++;
				}
			}
			return size;
		}

		// TODO: This method has 1 assignment error.
		@Override
		public int hashCode() {
			int fingerprint = 0;
			for (Row row: array) {
				if (row != null) {
					fingerprint += row.hashCode();
				}
			}
			return fingerprint;
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
		public String toString() {
			StringJoiner sj = new StringJoiner(", ", "LookupTable[", "]");
			for (Row row: array) {
				if (row != null) {
					sj.add(row.key() + "=" + row.fields());
				}
			}
			return sj.toString();
		}
	}