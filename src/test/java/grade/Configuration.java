package grade;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Configuration {
	/**
	 * The seed used to randomize test cases.
	 * <p>
	 * When grading, use the original number.
	 * <p>
	 * When debugging, you can use
	 * any number for repeatable test cases
	 * or null for varying test cases.
	 */
	static final Integer RANDOM_SEED = 2023_08;

	/**
	 * The number of milliseconds a test case
	 * is allowed to run before a timeout.
	 * <p>
	 * When debugging on a slower device,
	 * you can use a higher number.
	 */
	static final int TIMEOUT_MILLIS = 100;

	/**
	 * The folder where logs are generated.
	 */
	static final Path LOGS_DIRECTORY = Paths.get("db", "logs");
}
