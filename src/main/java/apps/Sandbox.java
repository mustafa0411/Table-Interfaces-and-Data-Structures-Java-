package apps;

import java.util.Arrays;
import java.util.List;

import models.Table;
import types.LookupTable;

public class Sandbox {
	public static void main(String[] args) {
		/*
		 * TODO: Modify as needed to debug
		 * or demonstrate arbitrary code.
		 */

		Table table1 = new LookupTable("example", List.of("K", "F1", "F2", "F3"));

		table1.put("a", List.of("Abc", 1, true));
		table1.put("b", List.of("Uvw", 2, false));
		table1.put("c", Arrays.asList("Xyz", 3, null));

		System.out.println(table1);
	}
}
