package apps;

import java.util.List;

import types.HashTable;

public class Sandbox {
	public static void main(String[] args) {
		// Table 1: Employee Information
		HashTable table1 = new HashTable("Companies", List.of("Table #", "Name", "Position"));

		table1.put("162", List.of("Leidos", "CS Intern"));
		table1.put("201", List.of("Terrasim", "GIS Consultant"));
		table1.put("144", List.of("M&S", "IT Technician"));

		System.out.println("Employer Information Table:");
		System.out.println(table1.toTabularView(false));

		// Create partitions based on filter targets
		HashTable leidosEmployees = (HashTable) table1.filter("Leidos"); // Employees at Leidos
		HashTable consultants = (HashTable) table1.filter("Terrasim"); // Consultants in all companies
		HashTable tableMoreThan140 = (HashTable) table1.filter("M&S"); // Non-existent company

		System.out.println("\nPartition 1: Employers at Leidos");
		System.out.println(leidosEmployees.toTabularView(false));

		System.out.println("\nPartition 2: Consultants in All Companies");
		System.out.println(consultants.toTabularView(false));

		System.out.println("\nPartition 3: Table# larger than 140");
		System.out.println(tableMoreThan140.toTabularView(false));

		// Table 2: Product Catalog
		HashTable table2 = new HashTable("ProductCatalog", List.of("ProductID", "Name", "Price"));

		table2.put("GPU", List.of("RTX 3060", 400.0));
		table2.put("CPU", List.of("Ryzen 5", 250.0));
		table2.put("Case", List.of("NZXT H510", 90.0));

		System.out.println("\nProduct Catalog Table:");
		System.out.println(table2.toTabularView(false));

		// Create partitions based on filter targets
		HashTable highPriceProducts = (HashTable) table2.filter(400.0); // Products with a price above $300
		HashTable ryzenProducts = (HashTable) table2.filter("CPU"); // Products with "Ryzen" in the name
		HashTable lowPriceProducts = (HashTable) table2.filter(90.0); // Products with a price less than $100

		System.out.println("\nPartition 4: Products with Price Above $350");
		System.out.println(highPriceProducts.toTabularView(false));

		System.out.println("\nPartition 5: Products that are CPUs");
		System.out.println(ryzenProducts.toTabularView(false));

		System.out.println("\nPartition 6: Products with Price below $100");
		System.out.println(lowPriceProducts.toTabularView(false));

		// Table 3: Factions
		HashTable table3 = new HashTable("Factions", List.of("Moral", "Name", "Game"));

		table3.put("Evil", List.of("Institute", "Fallout 4"));
		table3.put("Mixed", List.of("BOS", "Fallout 3"));
		table3.put("Good", List.of("NCR", "Fallout NV"));

		System.out.println("\nFactions Table:");
		System.out.println(table3.toTabularView(false));

		// Create partitions based on filter targets
		HashTable evilFactions = (HashTable) table3.filter("Evil"); // Factions with moral alignment "Evil"
		HashTable fallout4 = (HashTable) table3.filter("Fallout 3"); // Factions associated with Fallout games
		HashTable caliAcronym = (HashTable) table3.filter("NCR"); // Factions that are called "New California Republic"

		System.out.println("\nPartition 7: Factions with Moral Alignment 'Evil'");
		System.out.println(evilFactions.toTabularView(false));

		System.out.println("\nPartition 8: Factions Associated with 'Fallout 3' Games");
		System.out.println(fallout4.toTabularView(false));

		System.out.println("\nPartition 9: Factions named the 'New California Republic'");
		System.out.println(caliAcronym.toTabularView(false));

		// Union Example
		HashTable unionResult = (HashTable) leidosEmployees.union(consultants);
		System.out.println("\nUnion of Leidos Employees and Consultants:");
		System.out.println(unionResult.toTabularView(false));

		// Intersect Example
		HashTable intersectResult = (HashTable) leidosEmployees.intersect(consultants);
		System.out.println("\nIntersection of Leidos Employees and Consultants:");
		System.out.println(intersectResult.toTabularView(false));

		// Minus Example
		HashTable minusResult = (HashTable) leidosEmployees.minus(consultants);
		System.out.println("\nDifference (Minus) between Leidos Employees and Consultants:");
		System.out.println(minusResult.toTabularView(false));

		// Keep Example
		HashTable keepResult = (HashTable) table1.keep("201");
		System.out.println("\nKeep Only Row with Table # '201':");
		System.out.println(keepResult.toTabularView(false));

		// Drop Example
		HashTable dropResult = (HashTable) table1.drop("162");
		System.out.println("\nDrop Row with Table # '162':");
		System.out.println(dropResult.toTabularView(false));
	}
}