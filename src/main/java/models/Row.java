package models;

import java.util.List;

public record Row(String key, List<Object> fields) {
	@Override
	public String toString() {
		return key + ": " + fields;
	}
}