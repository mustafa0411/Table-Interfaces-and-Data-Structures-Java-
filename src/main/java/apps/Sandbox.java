package apps;

import java.util.List;

import types.JSONTable;
import types.XMLTable;

public class Sandbox {
	public static void main(String[] args) {
		// Create a new table using the 1-ary constructor
		List<String> factionColumns = List.of("Moral", "Name", "Game");
		JSONTable factionTable = new JSONTable("Factions", factionColumns);

		// Add data to the factionCatalog table
		factionTable.put("Evil", List.of("Institute", "Fallout 4"));
		factionTable.put("Mixed", List.of("BOS", "Fallout 4"));
		factionTable.put("Good", List.of("NCR", "Fallout NV"));

		// Display factionCatalog Table
		System.out.println("\nfactionCatalog Table:");
		System.out.println(factionTable.toTabularView(false));

		// Create Employee Information Table
		List<String> employerColumns = List.of("Table #", "Name", "Position");
		JSONTable employerTable = new JSONTable("Companies");
		employerTable.put("162", List.of("Leidos", "CS Intern"));
		employerTable.put("201", List.of("Terrasim", "GIS Consultant"));
		employerTable.put("144", List.of("M&S", "IT Technician"));

		// Display Employee Information Table
		System.out.println("\nEmployee Information Table:");
		System.out.println(employerTable.toTabularView(false));

		// Create Product Catalog Table using the 2-ary constructor
		List<String> productColumns = List.of("Product ID", "Name", "Price");
		XMLTable productCatalogTable = new XMLTable("ProductCatalog", productColumns);
		productCatalogTable.put("GPU", List.of("RTX 3060", "400.0"));
		productCatalogTable.put("CPU", List.of("Ryzen 5", "250.0"));
		productCatalogTable.put("Case", List.of("NZXT H510", "90.0"));

		// Display Product Catalog Table
		System.out.println("\nProduct Information Table:");
		System.out.println(productCatalogTable.toTabularView(false));

		// Create SoulsLike Protagonist Table using the 2-ary constructor
		List<String> soulsColumns = List.of("Game", "Theme", "Protagonist");
		XMLTable soulsProtagInfo = new XMLTable("SoulsLike");
		soulsProtagInfo.put("Bloodborne", List.of("Victorian", "Hunter"));
		soulsProtagInfo.put("Elden Ring", List.of("Medieval", "Tarnished"));
		soulsProtagInfo.put("Sekiro", List.of("Japanese", "Wolf"));

		// Display SoulsLike Protagonist Table
		System.out.println("\nSoulsLike Protagonist Table:");
		System.out.println(soulsProtagInfo.toTabularView(false));
	}
}
