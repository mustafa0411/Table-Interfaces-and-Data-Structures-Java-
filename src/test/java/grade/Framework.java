package grade;

import static grade.Configuration.*;
import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringJoiner;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.parallel.ResourceLock;

import models.BoundedTable;
import models.Row;
import models.Table;

enum CapacityProperty {
	POWER_OF_2,
	ODD_PRIME,
	MODULAR_PRIME
}

abstract class AbstractModule {
	int battery, volume;
	int graded, earned;

	@BeforeAll
	void startGrade() {
		graded = 0;
		earned = 0;
	}

	@AfterAll
	void reportGrade(TestReporter reporter) {
		var module = this.getClass().getSimpleName();
		var tag = "%s%s".formatted(module.charAt(0), module.charAt(module.length() - 1));
		var pct = (int) Math.ceil(earned / (double) graded * 100);

		System.out.printf("\n~~~ %s %d%% PASS RATE ~~~\n\n", tag, pct);

		reporter.publishEntry("moduleTag", tag);
		reporter.publishEntry("passedUnitTests", String.valueOf(pct));

		System.out.println();
	}

	@TestInstance(Lifecycle.PER_CLASS)
	abstract class AbstractTableContainer {
		String name;
		List<String> columns;

		Table subject;
		ControlTable control;
		Set<String> keyCache;
		int hits, misses;
		int passed;

		Random RNG;
		PrintStream log;

		@BeforeAll
		void startStats() {
			hits = 0;
			misses = 0;
			passed = 0;
		}

		@AfterAll
		@ResourceLock(value = "graded")
		@ResourceLock(value = "earned")
		void accrueGrade() {
			graded += battery;
			earned += passed;
		}

		@BeforeAll
		void defineRNG() {
			if (RANDOM_SEED != null)
				RNG = new Random(RANDOM_SEED);
			else
				RNG = new Random();
		}

		Table testConstructor(String which, List<Class<?>> types, List<Object> params, List<String> exempt) {
			try {
				var table = assertTimeoutPreemptively(ofMillis(TIMEOUT_MILLIS * 100), () -> {
					try {
						var clazz = Class.forName(which);
						var ctor = clazz.getConstructor(types.toArray(new Class<?>[0]));
						var instance = ctor.newInstance(params.toArray(new Object[0]));
						return (Table) instance;
					}
					catch (ClassNotFoundException e) {
						fail("Missing class %s".formatted(which));
						return null;
					}
					catch (NoSuchMethodException e) {
						fail("Missing %d-ary constructor with (%s) parameters".formatted(
							types.size(),
							encode(types, false, true)
						));
						return null;
					}
					catch (InvocationTargetException e) {
						throw e.getCause();
					}
				}, "Timeout in constructor (infinite loop/recursion likely)");
				logConstructor(name, table.getClass().getSimpleName(), params);

				thenTestForbiddenClasses(table, exempt);

				keyCache = new LinkedHashSet<>();
				return table;
			}
			catch (AssertionError e) {
				throw e;
			}
		}

		void thenTestForbiddenClasses(Table table, List<String> exempt) {
			var forbidden = new LinkedHashSet<Class<?>>();

			Class<?> c = table.getClass();
			while (c != null)  {
				var fields = new HashSet<Field>();
				Collections.addAll(fields, c.getFields());
				Collections.addAll(fields, c.getDeclaredFields());

				outer:
				for (Field f: fields) {
					try {
						f.setAccessible(true);

						var obj = f.get(table);
						if (obj != null) {
							var type = obj.getClass();

							while (type.isArray())
								type = type.getComponentType();

							if (type.isPrimitive() || type.isEnum())
								continue;

							if (exempt.contains(type.getTypeName()))
								continue;

							if (exempt.contains(type.getPackage().getName()))
								continue;

							if (type.getEnclosingClass() != null)
								if (exempt.contains(type.getEnclosingClass().getName()))
									continue;

							for (var iface: type.getInterfaces()) {
								if (exempt.contains(iface.getName()))
									continue outer;
							}

							forbidden.add(type);
						}
					}
					catch (Exception e) {
						continue;
					}
					finally {
						f.setAccessible(false);
					}
				}

				c = c.getSuperclass();
			}

			if (forbidden.size() == 1)
				fail("Forbidden class %s in table field".formatted(forbidden));
			else if (forbidden.size() > 1)
				fail("Forbidden classes %s in table fields".formatted(forbidden));
		}

		DynamicTest testClear() {
			var call = "clear()";
			logCall(name, call);

			return dynamicTest(call, () -> {
				control.clear();
				keyCache.clear();

				assertTimeoutPreemptively(ofMillis(TIMEOUT_MILLIS*10), () -> {
					subject.clear();
		        }, "Timeout in clear (infinite loop/recursion likely)");

				thenTestSize("clear");
				thenTestFingerprint("clear");

				passed++;
			});
		}

		DynamicTest testPut(boolean hitting, CapacityProperty property) {
			var key = key(hitting);
			var fields = fields();
			var call = "put(%s, %s)".formatted(encode(key), encode(fields));

			if (1 + fields.size() == columns.size()) {
				logCall(name, call);
				return dynamicTest(title(call, key), () -> {
					var expected = control.put(key, fields);
					var hit = expected != null;
					if (hit) hits++;
					else misses++;

					if (hit)
						keyCache.remove(key);
					keyCache.add(key);

					var actual = assertTimeoutPreemptively(ofMillis(TIMEOUT_MILLIS), () -> {
				    	return subject.put(key, fields);
				    }, "Timeout in put (infinite loop/recursion likely)");

					if (hit) {
						assertNotNull(actual, "Should hit for key %s but missed".formatted(key));

						assertEquals(
							expected,
							actual,
							"Mismatched fields for key %s on hit".formatted(key)
						);
					}
					else {
						assertNull(actual, "Should miss for key %s but hit".formatted(key));
					}

					thenTestSize("put");
					thenTestFingerprint("put");
					if (property != null)
						thenTestCapacityProperty("put", property);

					passed++;
				});
			}
			else {
				logCommentedCall(name, call);
				return dynamicTest(title(call, key), () -> {
					assertThrows(IllegalArgumentException.class, () -> {
						assertTimeoutPreemptively(ofMillis(TIMEOUT_MILLIS), () -> {
					    	return subject.put(key, fields);
					    }, "Timeout in put (infinite loop/recursion likely)");
					}, "Missing exception for fields with size %d (guard condition error likely)".formatted(fields.size()));

					passed++;
				});
			}
		}

		DynamicTest testGet(boolean hitting) {
			var key = key(hitting);
			var call = "get(%s)".formatted(encode(key));
			logCall(name, call);

			return dynamicTest(title(call, key), () -> {
				var expected = control.get(key);
				var hit = expected != null;
				if (hit) hits++;
				else misses++;

				var actual = assertTimeoutPreemptively(ofMillis(TIMEOUT_MILLIS), () -> {
		        	return subject.get(key);
		        }, "Timeout in get (infinite loop/recursion likely)");

				if (hit) {
					assertNotNull(actual, "Should hit for key %s but missed".formatted(key));

					assertEquals(
						expected,
						actual,
						"Mismatched fields for key %s on hit".formatted(key)
					);
				}
				else {
					assertNull(actual, "Should miss for key %s but hit".formatted(key));
				}

				if (control.size() > 0)
					thenTestDegree("get");

				passed++;
			});
		}

		DynamicTest testRemove(boolean hitting, CapacityProperty property) {
			var key = key(hitting);
			var call = "remove(%s)".formatted(encode(key));
			logCall(name, call);

			return dynamicTest(title(call, key), () -> {
				var expected = control.remove(key);
				var hit = expected != null;
				if (hit) hits++;
				else misses++;

				if (hit)
					keyCache.remove(key);

				var actual = assertTimeoutPreemptively(ofMillis(TIMEOUT_MILLIS), () -> {
		        	return subject.remove(key);
		        }, "Timeout in remove (infinite loop/recursion likely)");

				if (hit) {
					assertNotNull(actual, "Should hit for key %s but missed".formatted(key));

					assertEquals(
						expected,
						actual,
						"Mismatched fields for key %s on hit".formatted(key)
					);
				}
				else {
					assertNull(actual, "Should miss for key %s but hit".formatted(key));
				}

				thenTestSize("remove");
				thenTestFingerprint("remove");
				if (property != null)
					thenTestCapacityProperty("remove", property);

				passed++;
			});
		}

		// Untested: contains

		void thenTestDegree(String after) {
			var expected = columns.size();

			var actual = assertTimeoutPreemptively(ofMillis(TIMEOUT_MILLIS), () -> {
	        	return subject.degree();
	        }, "After %s, timeout in degree (infinite loop/recursion likely)".formatted(after));

			assertEquals(
				expected,
				actual,
				"After %s, degree off by %+d (calculation error likely)".formatted(after, actual - expected)
			);
		}

		void thenTestSize(String after) {
			var expected = control.size();

			var actual = assertTimeoutPreemptively(ofMillis(TIMEOUT_MILLIS), () -> {
	        	return subject.size();
	        }, "After %s, timeout in size (infinite loop/recursion likely)".formatted(after));

			assertEquals(
				expected,
				actual,
				"After %s, size off by %+d (calculation error likely)".formatted(after, actual - expected)
			);
		}

		// Untested: isEmpty

		// Untested: isFull

		void thenTestCapacityProperty(String after, CapacityProperty property) {
			if (subject instanceof BoundedTable bounded) {
				var result = assertTimeoutPreemptively(ofMillis(TIMEOUT_MILLIS), () -> {
		        	return bounded.capacity();
		        }, "After %s, timeout in capacity (infinite loop/recursion likely)".formatted(after));

				switch (property) {
					case POWER_OF_2 -> assertTrue(
						 (result & (result - 1)) == 0 || result < 1,
						"After %s, capacity %d not power of 2".formatted(after, result)
					);
					case ODD_PRIME -> assertTrue(
						result % 2 != 0 && BigInteger.valueOf(result).isProbablePrime(3) || result < 3,
						"After %s, capacity %d not odd prime".formatted(after, result)
					);
					case MODULAR_PRIME -> assertEquals(
						result % 4 == 3 && BigInteger.valueOf(result).isProbablePrime(3) || result < 3,
						"After %s, capacity %d not prime congruent to 3 modulo 4".formatted(after, result)
					);
				}
			}
			else {
				fail("Table not bounded (inheritance error likely)");
			}
		}

		// Untested: loadFactor

		void thenTestFingerprint(String after) {
			var expected = control.hashCode();

			var actual = assertTimeoutPreemptively(ofMillis(TIMEOUT_MILLIS), () -> {
	        	return subject.hashCode();
	        }, "After %s, timeout in fingerprint (infinite loop/recursion likely)".formatted(after));

			assertEquals(
				expected,
				actual,
				"After %s, fingerprint off by %+d (corrupted rows likely)".formatted(after, actual - expected)
			);
		}

		// Untested: equals

		DynamicTest testIterator() {
			var call = "iterator traversal";

			return dynamicTest(title(call), () -> {
				var expected = control.size();

				var actual = assertTimeoutPreemptively(ofMillis(TIMEOUT_MILLIS*10), () -> {
					var iter = subject.iterator();

					assertNotNull(iter, "Null iterator");

					var i = 0;
					while (iter.hasNext()) {
						var row = iter.next();

						if (i < expected) {
							assertNotNull(
								row,
								"Null row on iteration %d (hasNext/next errors likely)".formatted(i)
							);

							if (!control.contains(row.key()))
								fail("Row with unknown key %s on iteration %d (hasNext/next errors likely)".formatted(row.key(), i));

							assertEquals(
								control.get(row.key()),
								row.fields(),
								"Row with mismatched fields for key %s on iteration %d (hasNext/next errors likely)".formatted(row.key(), i)
							);
						}

						i++;
					}
					return i;
		        }, "Timeout in iterator (infinite loop/recursion likely)");

				assertEquals(
					expected,
					actual,
					"Iterations off by %+d (hasNext/next errors likely)".formatted(actual - expected)
				);

				passed++;
			});
		}

		DynamicTest testName() {
			var call = "name()";
			logCall(name, call);

			return dynamicTest(call, () -> {
				var expected = name;

				var actual = assertTimeoutPreemptively(ofMillis(TIMEOUT_MILLIS), () -> {
		        	return subject.name();
		        }, "Timeout in name (infinite loop/recursion likely)");

				assertEquals(
					expected,
					actual,
					"Mismatched name (assignment error likely)"
				);

				passed++;
			});
		}

		DynamicTest testColumns() {
			var call = "columns()";
			logCall(name, call);

			return dynamicTest(call, () -> {
				var expected = columns;

				var actual = assertTimeoutPreemptively(ofMillis(TIMEOUT_MILLIS), () -> {
		        	return subject.columns();
		        }, "Timeout in columns (infinite loop/recursion likely)");

				assertEquals(
					expected,
					actual,
					"Mismatched columns (assignment error likely)"
				);

				passed++;
			});
		}

		// Untested: toString

//		double hitRate() {
//			return (double) hits / (hits + misses);
//		}
//
//		@AfterAll
//		void auditHitRate() {
//			System.err.println(hitRate());
//		}

		String title(String call) {
			try {
				if (subject instanceof BoundedTable bounded) {
					return assertTimeoutPreemptively(ofMillis(TIMEOUT_MILLIS), () -> {
						return "%s when lf=%d/%d=%.3f".formatted(
							call,
							bounded.size(),
							bounded.capacity(),
							bounded.loadFactor()
						);
			        }, "Before %s, timeout in size/capacity/loadFactor (infinite loop/recursion likely)".formatted(call));
				}
				else {
					return assertTimeoutPreemptively(ofMillis(TIMEOUT_MILLIS), () -> {
						return "%s when n=%d".formatted(
							call,
							subject.size()
						);
			        }, "Before %s, timeout in size (infinite loop/recursion likely)".formatted(call));
				}
			}
			catch (AssertionError e) {
				return "%s".formatted(call);
			}
		}

		String title(String call, String key) {
			var hit = control.contains(key);
			try {
				if (subject instanceof BoundedTable bounded) {
					return assertTimeoutPreemptively(ofMillis(TIMEOUT_MILLIS), () -> {
						return "%s %s %s when lf=%d/%d=%.3f".formatted(
							call,
							hit ? "hits" : "misses",
							encode(key),
							bounded.size(),
							bounded.capacity(),
							bounded.loadFactor()
						);
			        }, "Before %s, timeout in size/capacity/loadFactor (infinite loop/recursion likely)".formatted(call));
				}
				else {
					return assertTimeoutPreemptively(ofMillis(TIMEOUT_MILLIS), () -> {
						return "%s %s %s when size=%d".formatted(
							call,
							hit ? "hits" : "misses",
							encode(key),
							subject.size()
						);
			        }, "Before %s, timeout in size (infinite loop/recursion likely)".formatted(call));
				}
			}
			catch (AssertionError e) {
				return "%s %s %s".formatted(
					call,
					hit ? "hits" : "misses",
					encode(key)
				);
			}
		}

		String encode(Object obj) {
			if (obj == null)
				return "null";
			else if (obj instanceof String)
				return "\"" + obj + "\"";
			else
				return obj.toString();
		}

		String encode(List<Object> fields) {
			return encode(fields, true, false);
		}

		String encode(List<?> fields, boolean checkNulls, boolean raw) {
			StringJoiner sj;
			if (checkNulls && fields.contains(null))
				sj = new StringJoiner(", ", "Arrays.asList(", ")");
			else if (!raw)
				sj = new StringJoiner(", ", "List.of(", ")");
			else
				sj = new StringJoiner(", ");
			for (var field: fields) {
				if (field instanceof List<?> flist)
					sj.add(encode(flist, false, false));
				else
					sj.add(encode(field));
			}
			return sj.toString();
		}

		String key(boolean hitting) {
			if (hitting && keyCache.size() > 10) {
				var iter = keyCache.iterator();
				var key = iter.next();
				iter.remove();
				keyCache.add(key);
				return key;
			}
			else {
				return key();
			}
		}

		String key() {
			return skey();
		}

		String skey() {
			var s = s();
			while (RNG.nextDouble() < .10)
				s = s + "_" + s();
			while (keyCache.contains(s))
				s = s + "_" + Math.abs(i());
			return s;
		}

		String ckey() {
			String c;
			do {
				c = c();
			} while (keyCache.contains(c));
			return c;
		}

		List<Object> fields() {
			var fields = new LinkedList<>();
			var d = columns.size();
			if (RNG.nextDouble() < .01) {
				if (RNG.nextBoolean())
					d++;
				else
					d--;
			}
			for (var i = 0; i < d - 1; i++) {
				var r = RNG.nextDouble();
				if (r < .60)
					fields.add(s());
				else if (r < .90)
					fields.add(i());
				else if (r < .99)
					fields.add(b());
				else
					fields.add(null);
			}
			return fields;
		}

		String s() {
			var i = RNG.nextInt(TestData.WORDS_LOOKUP.length);
			var s = TestData.WORDS_LOOKUP[i];
			if (s.length() > 0) {
				var r = RNG.nextDouble();
				if (r < .05)
					s = s.toUpperCase();
				else if (r < .20)
					s = Character.toTitleCase(s.charAt(0)) + s.substring(1);
			}
			return s;
		}

		String c() {
			if (RNG.nextBoolean())
				return String.valueOf((char) ('a' + RNG.nextInt(26)));
			else
				return String.valueOf((char) ('A' + RNG.nextInt(26)));
		}

		int i() {
			return (int) (RNG.nextGaussian() * 1000);
		}

		Boolean b() {
			return RNG.nextBoolean();
		}

		void logStart(String suffix) {
			try {
				var path = LOGS_DIRECTORY.resolve(suffix == null
					? "%s.java".formatted(name)
					: "%s_%s.java".formatted(name, suffix)
				);

				Files.createDirectories(path.getParent());
				log = new PrintStream(path.toFile());

				System.out.println("Logging: " + path);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		void logLine(String line) {
			if (log != null)
				log.println(line);
		}

		void logConstructor(String refName, String className, List<Object> params) {
			if (log== null) return;

			logLine("Table %s = new %s(%s);".formatted(
				refName,
				className,
				encode(params, false, true)
			));
		}

		void logCall(String refName, String call) {
			if (log== null) return;

			logLine("%s.%s;".formatted(refName, call));
		}

		void logCommentedCall(String refName, String call) {
			if (log== null) return;

			logLine("//%s.%s;".formatted(refName, call));
		}
	}
}

class ControlTable implements Table {
	Map<String, List<Object>> map;
	int fingerprint;

	ControlTable() {
		this.map = new HashMap<>(16);
	}

	@Override
	public void clear() {
		map.clear();
		fingerprint = 0;
	}

	@Override
	public List<Object> put(String key, List<Object> fields) {
		var put = map.put(key, fields);
		fingerprint += key.hashCode() * 31 + fields.hashCode();
		if (put != null) {
			fingerprint -= key.hashCode() * 31 + put.hashCode();
			return put;
		}
		return null;
	}

	@Override
	public List<Object> remove(String key) {
		var rem = map.remove(key);
		if (rem != null) {
			fingerprint -= key.hashCode() * 31 + rem.hashCode();
			return rem;
		}
		return null;
	}

	@Override
	public List<Object> get(String key) {
		return map.get(key);
	}

	@Override
	public boolean contains(String key) {
		return map.containsKey(key);
	}

	@Override
	public int degree() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public int hashCode() {
		return fingerprint;
	}

	@Override
	public Iterator<Row> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String name() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> columns() {
		throw new UnsupportedOperationException();
	}
}

class TestData {
	/**
	 * Adapted from:
	 * https://simple.wikipedia.org/wiki/Wikipedia:List_of_1000_basic_words
	 */
	static final String[] WORDS_LOOKUP = {
		"", " ", "!", "?", "@", "#", "$", "%", "^", "&", "|", "*", "/", "+", "-", "=", "~", "<", ">",
		"a", "about", "above", "across", "act", "active", "activity", "add", "afraid", "after", "again", "age", "ago", "agree", "air", "all", "alone", "along", "already", "always", "am", "amount", "an", "and", "angry", "another", "answer", "any", "anyone", "anything", "anytime", "appear", "apple", "are", "area", "arm", "army", "around", "arrive", "art", "as", "ask", "at", "attack", "aunt", "autumn", "away",
		"baby", "back", "bad", "bag", "ball", "bank", "base", "basket", "bath", "be", "bean", "bear", "beautiful", "bed", "bedroom", "beer", "behave",  "before", "begin", "behind", "bell", "below", "besides", "best", "better", "between", "big", "bird", "birth", "birthday", "bit", "bite", "black", "bleed", "block", "blood", "blow", "blue", "board", "boat", "body", "boil", "bone", "book", "border", "born", "borrow", "both", "bottle", "bottom", "bowl", "box", "boy", "branch", "brave", "bread", "break", "breakfast", "breathe", "bridge", "bright", "bring", "brother", "brown", "brush", "build", "burn", "business", "bus", "busy", "but",  "buy", "by",
		"cake", "call", "can", "candle", "cap", "car", "card", "care", "careful", "careless", "carry", "case", "cat", "catch",  "central", "century", "certain", "chair", "chance", "change", "chase", "cheap",  "cheese", "chicken", "child", "children", "chocolate", "choice", "choose", "circle", "city", "class", "clever", "clean", "clear", "climb", "clock", "cloth", "clothes", "cloud", "cloudy", "close", "coffee", "coat", "coin", "cold", "collect", "colour", "comb", "comfortable", "common", "compare", "come", "complete", "computer", "condition", "continue", "control", "cook", "cool", "copper", "corn", "corner", "correct", "cost", "contain", "count", "country", "course", "cover", "crash", "cross", "cry", "cup", "cupboard", "cut",
		"dance", "dangerous", "dark", "daughter", "day", "dead", "decide", "decrease", "deep", "deer", "depend", "desk", "destroy", "develop", "die", "different", "difficult", "dinner", "direction", "dirty", "discover", "dish", "do", "dog", "door", "double", "down", "draw", "dream", "dress", "drink", "drive", "drop", "dry", "duck", "dust", "duty",
		"each", "ear", "early", "earn", "earth", "east", "easy", "eat", "education", "effect", "egg", "eight", "either", "electric", "elephant", "else", "empty", "end", "enemy", "enjoy", "enough", "enter", "equal", "entrance", "escape", "even", "evening", "event", "ever", "every", "everyone", "exact", "everybody", "examination", "example", "except", "excited", "exercise", "expect", "expensive", "explain", "extremely", "eye",
		"face", "fact", "fail", "fall", "false", "family", "famous", "far", "farm", "father", "fast", "fat", "fault", "fear", "feed", "feel", "female", "fever", "few", "fight", "fill", "film", "find", "fine", "finger", "finish", "fire", "first", "fish", "fit", "five", "fix", "flag", "flat", "float", "floor", "flour", "flower", "fly", "fold", "food", "fool", "foot", "football", "for", "force", "foreign", "forest", "forget", "forgive", "fork", "form", "fox", "four", "free", "freedom", "freeze", "fresh", "friend", "friendly", "from", "front", "fruit", "full", "fun", "funny", "furniture", "further", "future",
		"game", "garden", "gate", "general", "gentleman", "get", "gift", "give", "glad", "glass", "go", "goat", "god", "gold", "good", "goodbye", "grandfather", "grandmother", "grass", "grave", "great", "green", "gray", "ground", "group", "grow", "gun",
		"hair", "half", "hall", "hammer",  "hand", "happen", "happy", "hard", "hat", "hate", "have", "he", "head", "healthy", "hear", "heavy", "heart", "heaven", "height", "hello", "help", "hen", "her", "here", "hers", "hide", "high", "hill", "him", "his", "hit", "hobby", "hold", "hole", "holiday", "home", "hope", "horse", "hospital", "hot", "hotel", "house", "how", "hundred", "hungry", "hour", "hurry", "husband", "hurt",
		"I", "ice", "idea", "if", "important", "in", "increase", "inside", "into", "introduce", "invent", "iron", "invite", "is", "island", "it", "its",
		"jelly", "job", "join", "juice", "jump", "just",
		"keep", "key", "kill", "kind", "king", "kitchen", "knee", "knife", "knock", "know",
		"ladder", "lady", "lamp", "land", "large", "last", "late", "lately", "laugh", "lazy", "lead", "leaf", "learn", "leave", "leg", "left", "lend", "length", "less", "lesson", "let", "letter", "library", "lie", "life", "light", "like", "lion", "lip", "list", "listen", "little", "live", "lock", "lonely", "long", "look", "lose", "lot", "love", "low", "lower", "luck",
		"machine", "main", "make", "male", "man", "many", "map", "mark", "market", "marry", "matter", "may", "me", "meal", "mean", "measure", "meat", "medicine", "meet", "member", "mention", "method", "middle", "milk", "million", "mind", "minute", "miss", "mistake", "mix", "model", "modern", "moment", "money", "monkey", "month", "moon", "more", "morning", "most", "mother", "mountain", "mouth", "move", "much", "music", "must", "my",
		"name", "narrow", "nation", "nature", "near", "nearly", "neck", "need", "needle", "neighbour", "neither", "net", "never", "new", "news", "newspaper", "next", "nice", "night", "nine", "no", "noble", "noise", "none", "nor", "north", "nose", "not", "nothing", "notice", "now", "number",
		"obey", "object", "ocean", "of", "off", "offer", "office", "often", "oil", "old", "on", "one", "only", "open", "opposite", "or", "orange", "order", "other", "our", "out", "outside", "over", "own",
		"page", "pain", "paint", "pair", "pan", "paper", "parent", "park", "part", "partner", "party", "pass", "past", "path", "pay", "peace", "pen", "pencil", "people", "pepper", "per", "perfect", "period", "person", "petrol", "photograph", "piano", "pick", "picture", "piece", "pig", "pin", "pink", "place", "plane", "plant", "plastic", "plate", "play", "please", "pleased", "plenty", "pocket", "point", "poison", "police", "polite", "pool", "poor", "popular", "position", "possible", "potato", "pour", "power", "present", "press",  "pretty", "prevent", "price", "prince", "prison", "private", "prize", "probably", "problem", "produce", "promise", "proper", "protect", "provide", "public", "pull", "punish", "pupil", "push", "put",
		"queen", "question", "quick", "quiet", "quite",
		"radio", "rain", "rainy", "raise", "reach", "read", "ready", "real", "really", "receive", "record", "red", "remember", "remind", "remove", "rent", "repair", "repeat", "reply", "report", "rest", "restaurant", "result", "return", "rice", "rich", "ride", "right", "ring", "rise", "road", "rob", "rock", "room", "round", "rubber", "rude", "rule", "ruler", "run", "rush",
		"sad", "safe", "sail", "salt", "same", "sand", "save", "say", "school", "science", "scissors", "search", "seat", "second", "see", "seem", "sell", "send", "sentence", "serve", "seven", "several", "sex", "shade", "shadow", "shake", "shape", "share", "sharp", "she", "sheep", "sheet", "shelf", "shine", "ship", "shirt", "shoe", "shoot", "shop", "short", "should", "shoulder", "shout", "show", "sick", "side", "signal", "silence", "silly", "silver", "similar", "simple", "single", "since", "sing", "sink", "sister", "sit", "six", "size", "skill", "skin", "skirt", "sky", "sleep", "slip", "slow", "small", "smell", "smile", "smoke", "snow", "so", "soap", "sock", "soft", "some", "someone", "something", "sometimes", "son", "soon", "sorry", "sound", "soup", "south", "space", "speak", "special", "speed", "spell", "spend", "spoon", "sport", "spread", "spring", "square", "stamp", "stand", "star", "start", "station", "stay", "steal", "steam", "step", "still", "stomach", "stone", "stop", "store", "storm", "story", "strange", "street", "strong", "structure", "student", "study", "stupid", "subject", "substance", "successful", "such", "sudden", "sugar", "suitable", "summer", "sun", "sunny", "support", "sure", "surprise", "sweet", "swim", "sword",
		"table", "take", "talk", "tall", "taste", "taxi", "tea", "teach", "team", "tear", "telephone", "television", "tell", "ten", "tennis", "terrible", "test", "than", "that", "the", "their", "then", "there", "therefore", "these", "thick", "thin", "thing", "think", "third", "this", "though", "threat", "three", "tidy", "tie", "title", "to", "today", "toe", "together", "tomorrow", "tonight", "too", "tool", "tooth", "top", "total", "touch", "town", "train", "tram", "travel", "tree", "trouble", "true", "trust", "twice", "try", "turn", "type",
		"ugly", "uncle", "under", "understand", "unit", "until", "up", "use", "useful", "usual", "usually",
		"vegetable", "very", "village", "voice", "visit",
		"wait", "wake", "walk", "want", "warm", "was", "wash", "waste", "watch", "water", "way", "we", "weak", "wear", "weather", "wedding", "week", "weight", "welcome", "were", "well", "west", "wet", "what", "wheel", "when", "where", "which", "while", "white", "who", "why", "wide", "wife", "wild", "will", "win", "wind", "window", "wine", "winter", "wire", "wise", "wish", "with", "without", "woman", "wonder", "word", "work", "world", "worry",
		"yard", "yell", "yesterday", "yet", "you", "young", "your",
		"zero", "zoo",
	};
}