package grade;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;

@DisplayName("M3 Hash Table")
@TestInstance(Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
@TestClassOrder(ClassOrderer.ClassName.class)
final class Module3 extends AbstractModule {
	@BeforeAll
	void defineModule() {
		battery = 1000;
		volume = 500;
	}

	@Nested
	@DisplayName("m3_table1 [degree 3]")
	class Table1 extends HashTableContainer {
		@BeforeAll
		void defineTable() {
			name = "m3_table1";
			columns = List.of("k1", "f1a", "f1b");
		}
	}

	@Nested
	@DisplayName("m3_table2 [degree 4]")
	class Table2 extends HashTableContainer {
		@BeforeAll
		void defineTable() {
			name = "m3_table2";
			columns = List.of("k2", "f2a", "f2b", "f2c");
		}
	}

	@Nested
	@DisplayName("m3_table3 [degree 6]")
	class Table3 extends HashTableContainer {
		@BeforeAll
		void defineTable() {
			name = "m3_table3";
			columns = List.of("k3", "f3a", "f3b", "f3c", "f3d", "f3e");
		}
	}

	abstract class HashTableContainer extends AbstractTableContainer {
		static final List<String> exempt = List.of(
    		"models",
			"types",
			"java.lang.String",
			"java.lang.Number",
        	"java.lang.Boolean",
			"java.util.ImmutableCollections$AbstractImmutableCollection"
		);

		@TestFactory
		@DisplayName("New Hash Table")
		@Execution(ExecutionMode.SAME_THREAD)
		Stream<DynamicTest> testNewTable() {
			logStart("new");

			subject = testConstructor(
				"types.HashTable",
				List.of(String.class, List.class),
				List.of(name, columns),
				exempt
			);

			control = new ControlTable();

			return IntStream.range(0, battery).mapToObj(i -> {
				if (i == 0)
					return testName();
				else if (i == 1)
					return testColumns();
				else if (i == 2 || i == battery-1)
					return testClear();
				else if (i % 20 == 0 || i == battery-2)
					return testIterator();
				else {
					if (control.size() < volume * .99)
						return testPut(false, CapacityProperty.ODD_PRIME);
					else if (control.size() > volume * 1.01)
						return testRemove(true, null);
					else if (RNG.nextBoolean())
						return testGet(RNG.nextBoolean());
					else if (RNG.nextBoolean())
						return testPut(RNG.nextBoolean(), CapacityProperty.ODD_PRIME);
					else
						return testRemove(RNG.nextBoolean(), null);
				}
			});
		}

		@Override
		@AfterAll
		@ResourceLock(value = "graded")
		@ResourceLock(value = "earned")
		void accrueGrade() {
			graded += battery;
			earned += passed;
		}
	}
}