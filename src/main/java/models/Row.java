package models;

import java.util.List;

public record Row(String key, List<Object> fields) {
	@Override
	public String toString() {
		return key + ": " + fields;
	}

	public String getKey() {
		return key;
	}

	public List<Object> getFields(){
		return fields;
	}
}