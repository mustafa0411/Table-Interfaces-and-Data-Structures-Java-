package apps;

import java.util.List;

import types.SearchTable;

public class Sandbox {
	public static void main(String[] args) {
		/*
		 * TODO: Modify as needed to debug
		 * or demonstrate arbitrary code.
		 */

		// Table 1: Employee Information
		SearchTable table1 = new SearchTable("Companies", List.of("Table #", "Name", "Position"));

		table1.put("162", List.of("Leidos", "CS Intern"));
		table1.put("201", List.of("Terrasim", "GIS Consultant"));
		table1.put("144", List.of("M&S", "IT Technician"));

		System.out.println(table1);

		// Table 2: Product Catalog
		SearchTable table2 = new SearchTable("ProductCatalog", List.of("ProductID", "Name", "Price"));

		table2.put("P001", List.of("RTX 3060", 400.0));
		table2.put("P002", List.of("Ryzen 5", 250.0));
		table2.put("P003", List.of("NZXT H510", 90.0));

		System.out.println(table2);

		// Table 3: Customer Data
		SearchTable table3 = new SearchTable("Factions", List.of("Moral", "Name", "Game"));

		table3.put("Evil", List.of("Institute", "Fallout 4"));
		table3.put("Mixed", List.of("BOS", "Fallout 3, NV, 4"));
		table3.put("Good", List.of("NCR", "Fallout 1, 2, NV"));

		System.out.println(table3);
	}
}
