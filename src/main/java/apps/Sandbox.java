package apps;

import java.util.Arrays;

public class Sandbox {
	public static void main(String[] args) {
		//		// Table 1: Employee Information
		//		HashTable table1 = new HashTable("Companies", List.of("Table #", "Name", "Position"));
		//
		//		table1.put("162", List.of("Leidos", "CS Intern"));
		//		table1.put("201", List.of("Terrasim", "GIS Consultant"));
		//		table1.put("144", List.of("M&S", "IT Technician"));
		//
		//		System.out.println("Employer Information Table:");
		//		System.out.println(table1.toTabularView(false));
		//
		//		// Table 2: Product Catalog
		//		HashTable table2 = new HashTable("ProductCatalog", List.of("ProductID", "Name", "Price"));
		//
		//		table2.put("GPU", List.of("RTX 3060", 400.0));
		//		table2.put("CPU", List.of("Ryzen 5", 250.0));
		//		table2.put("Case", List.of("NZXT H510", 90.0));
		//
		//		System.out.println("\nProduct Catalog Table:");
		//		System.out.println(table2.toTabularView(false));
		//
		//		// Table 3: Factions
		//		HashTable table3 = new HashTable("Factions", List.of("Moral", "Name", "Game"));
		//
		//		table3.put("Evil", List.of("Institute", "Fallout 4"));
		//		table3.put("Mixed", List.of("BOS", "Fallout 3"));
		//		table3.put("Good", List.of("NCR", "Fallout NV"));
		//
		//		System.out.println("\nFactions Table:");
		//		System.out.println(table3.toTabularView(false));
		//
		//		// Union Example
		//		HashTable unionResult = (HashTable) table1.union(table2);
		//		System.out.println("\nUnion of Employee Information and Product Catalog:");
		//		System.out.println(unionResult.toTabularView(false));
		//
		//		// Intersect Example
		//		HashTable intersectResult = (HashTable) table1.intersect(table3);
		//		System.out.println("\nIntersection of Employee Information and Factions:");
		//		System.out.println(intersectResult.toTabularView(false));
		//
		//		// Minus Example
		//		HashTable minusResult = (HashTable) table2.minus(table3);
		//		System.out.println("\nDifference (Minus) between Product Catalog and Factions:");
		//		System.out.println(minusResult.toTabularView(false));
		//
		//		// Keep Example
		//		HashTable keepResult = (HashTable) table1.keep("201");
		//		System.out.println("\nKeep Only Row with Table # '201' in Employee Information:");
		//		System.out.println(keepResult.toTabularView(false));
		//
		//		// Drop Example
		//		HashTable dropResult = (HashTable) table1.drop("162");
		//		System.out.println("\nDrop Row with Table # '162' in Employee Information:");
		//		System.out.println(dropResult.toTabularView(false));


		var s = "A | B | C | D | E";
		System.out.println(s);
		var l = Arrays.asList(s.split("\\|" ));
		System.out.println(l);


	}
}
