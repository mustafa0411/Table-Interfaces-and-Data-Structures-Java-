package models;

import java.util.List;

public record Row(String key, List<Object> fields) {

	public Row setFields(List<Object> fields2) {
		// TODO Auto-generated method stub
		return new Row(key, fields2); // Create a new Row instance with updated fields
	}

}