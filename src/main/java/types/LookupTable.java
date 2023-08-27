package types;

import java.util.ArrayList;
import java.util.Arrays;
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
		this.degree = degree; //initialize the degree field
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
		}else if(c >= 'A' && c <= 'Z') {
			return c - 'A';
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
		if(array[i] != null) { //check for null before accessing
			return array[i].fields();
		}
		return null;
	}

	// TODO: This method has 1 assignment error.
	@Override
	public List<Object> remove(String key) {
		int i = indexOf(key);

		Row here = array[i];

		if (here != null) {
			array[i] = null;
			return here.fields();
		}

		return null;
	}

	// TODO: This method has 1 result error.
	@Override
	public int degree() {
		return degree; //return the initialized degree
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
		if(this == obj ) {
			return true;
		}
		if(!(obj instanceof LookupTable)) {
			return false;
		}
		LookupTable other = (LookupTable) obj;
		return Arrays.equals(array, other.array);
	}

	@Override
	public Iterator<Row> iterator() {
		List<Row> rows = new ArrayList<>();
		for(Row row : array) {
			if(row != null) {
				rows.add(row);
			}
		}
		return rows.iterator();
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