package apps;

import java.util.List;

import types.CSVTable;

public class Sandbox {
	public static void main(String[] args) {
		// Create Employee Information Table using 2-ary constructor
		List<String> employerColumns = List.of("Table #", "Name", "Position");
		CSVTable employerTable = new CSVTable("EmployerInformation", employerColumns);
		employerTable.put("162", List.of("Leidos", "CS Intern"));
		employerTable.put("201", List.of("Terrasim", "GIS Consultant"));
		employerTable.put("144", List.of("M&S", "IT Technician"));

		//		 Create Product Catalog CSVTable from Text using fromText method
		String productCatalogText = "Product,Name,Price\n" +
				"GPU,RTX 3060,400.0\n" +
				"CPU,Ryzen 5,250.0\n" +
				"Case,NZXT H510,90.0";
		CSVTable productCatalogTable = CSVTable.fromText("ProductCatalog", productCatalogText);

		// Create a new table using the 1-ary constructor
		List<String> factionColumns = List.of("Moral", "Name", "Game");
		CSVTable factionTable = new CSVTable("factionCatalog");

		// Add data to the factionCatalog table
		factionTable.put("Evil", List.of("Institute", "Fallout 4"));
		factionTable.put("Mixed", List.of("BOS", "Fallout 4"));
		factionTable.put("Good", List.of("NCR", "Fallout NV"));

		// Display Employee Information Table
		System.out.println("Employee Information Table:");
		System.out.println(employerTable.toTabularView(false));

		// Display Product Catalog Table
		System.out.println("\nProduct Catalog Table:");
		System.out.println(productCatalogTable.toTabularView(false));

		// Display factionCatalog Table
		System.out.println("\nfactionCatalog Table:");
		System.out.println(factionTable.toTabularView(false));

		// You can continue to add more tables and perform operations as needed.


		//		List<String> employerColumns = List.of("Table #", "Name", "Position");
		//		JSONTable employerTable = new JSONTable("EmployerInformation", employerColumns);
		//		employerTable.put("162", List.of("Leidos", "CS Intern"));
		//		employerTable.put("201", List.of("Terrasim", "GIS Consultant"));
		//		employerTable.put("144", List.of("M&S", "IT Technician"));
		//
		//
		//// Display Employee Information Table
		//		System.out.println("Employee Information Table:");
		//		System.out.println(employerTable.toTabularView(false));
	}
}
