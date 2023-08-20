package grade;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

@DisplayName("M0 Lookup Table")
@TestInstance(Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
@TestClassOrder(ClassOrderer.ClassName.class)
final class Module0 extends AbstractModule {
	@BeforeAll
	void defineModule() {
		battery = 100;
		volume = 40;
	}

	@Nested
	@DisplayName("m0_table1 [degree 3]")
	class Table1 extends LookupTableContainer {
		@BeforeAll
		void defineTable() {
			name = "m0_table1";
			degree = 3;
		}
	}

	@Nested
	@DisplayName("m0_table2 [degree 4]")
	class Table2 extends LookupTableContainer {
		@BeforeAll
		void defineTable() {
			name = "m0_table2";
			degree = 4;
		}
	}

	@Nested
	@DisplayName("m0_table3 [degree 6]")
	class Table3 extends LookupTableContainer {
		@BeforeAll
		void defineTable() {
			name = "m0_table3";
			degree = 6;
		}
	}

	abstract class LookupTableContainer extends AbstractTableContainer {
		static final List<String> exempt = List.of(
    		"models",
			"types",
			"java.lang"
		);

		@Override
		String key() {
			return ckey();
		}

		@TestFactory
		@DisplayName("New Lookup Table")
		@Execution(ExecutionMode.SAME_THREAD)
		Stream<DynamicTest> testNewTable() {
			logStart("new");

			subject = testConstructor(
				"types.LookupTable",
				List.of(int.class),
				List.of(degree),
				exempt
			);

			control = new ControlTable();

			return IntStream.range(0, battery).mapToObj(i -> {
				if (i == 0 || i == battery-1)
					return testClear();
				else {
					if (control.size() < volume * .99)
						return testPut(false, null);
					else if (control.size() > volume * 1.01)
						return testRemove(true, null);
					else if (RNG.nextBoolean())
						return testGet(RNG.nextBoolean());
					else if (RNG.nextBoolean())
						return testPut(RNG.nextBoolean(), null);
					else
						return testRemove(RNG.nextBoolean(), null);
				}
			});
		}
	}
}