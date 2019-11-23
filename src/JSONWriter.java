import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * The Class JSONWriter writes to files in proper JSON format.
 */
public class JSONWriter {

	/**
	 * Writes several tab <code>\t</code> symbols using the provided {@link Writer}.
	 *
	 * @param times  the number of times to write the tab symbol
	 * @param writer the writer to use
	 * @throws IOException if the writer encounters any issues
	 */
	public static void indent(int times, Writer writer) throws IOException {
		for (int i = 0; i < times; i++) {
			writer.write('\t');
		}
	}

	/**
	 * Writes the element surrounded by quotes using the provided {@link Writer}.
	 *
	 * @param index  the index
	 * @param writer the writer to use
	 * @throws IOException if the writer encounters any issues
	 */
	public static void quote(String index, Writer writer) throws IOException {
		writer.write('"');
		writer.write(index);
		writer.write('"');
	}

	/**
	 * Writes the set of elements formatted as a JSON array of numbers using the
	 * provided {@link Writer} and indentation level.
	 *
	 * @param elements the elements
	 * @param writer   the writer
	 * @param level    the level
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void asArray(TreeSet<Integer> elements, Writer writer, int level) throws IOException {
		if (elements.isEmpty()) {
			writer.write(System.lineSeparator());
			indent(level, writer);
		} else {
			writer.write(System.lineSeparator());
			for (Integer element : elements.headSet(elements.last(), false)) {
				indent(level + 1, writer);
				writer.write(element.toString());
				writer.write(',');
				writer.write(System.lineSeparator());
			}
			indent(level + 1, writer);
			writer.write(elements.last().toString());
			writer.write(System.lineSeparator());
			indent(level, writer);
		}
	}

	/**
	 * Writes the map of elements as a JSON object using the provided {@link Writer}
	 * and indentation level.
	 *
	 * @param elements the elements
	 * @param writer   the writer
	 * @param level    the level
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void asObject(TreeMap<String, Integer> elements, Writer writer, int level) throws IOException {

		if (elements.isEmpty()) {

			writer.write("{" + System.lineSeparator() + "}");
		} else {
			int count = 0;
			writer.write('{');
			writer.write(System.lineSeparator());
			for (Entry<String, Integer> element : elements.entrySet()) {
				String key = element.getKey();
				Integer value = element.getValue();
				indent(level + 1, writer);
				quote(key.toString(), writer);
				writer.write(": ");
				writer.write(value.toString());
				if (count < elements.size() - 1) {
					writer.write(',');
				}
				writer.write(System.lineSeparator());
				count++;
			}
			writer.write("}");
		}
	}

	/**
	 * Helper method as a nested object
	 *
	 * @param elements the elements
	 * @param writer   the writer
	 * @param level    the level
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void asNestedObject(Entry<String, TreeSet<Integer>> elements, Writer writer, int level)
			throws IOException {
		if (elements.getValue().isEmpty()) {
			writer.write(System.lineSeparator());
		} else {
			writer.write(System.lineSeparator());
			TreeSet<Integer> values = elements.getValue();
			indent(level + 1, writer);
			quote(elements.getKey(), writer);
			writer.write(": ");
			writer.write("[");
			asArray(values, writer, level + 1);
		}
		writer.write("]");
	}

	/**
	 * Helper method as a double nested object
	 *
	 * @param elements the elements
	 * @param writer   the writer
	 * @param level    the level
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void asDoubleNestedObject(Entry<String, TreeMap<String, TreeSet<Integer>>> elements, Writer writer,
			int level) throws IOException {

		TreeMap<String, TreeSet<Integer>> subEntry = elements.getValue();
		writer.write(System.lineSeparator());
		indent(level, writer);
		quote(elements.getKey(), writer);
		writer.write(": ");
		writer.write("{");
		if (!subEntry.isEmpty()) {
			Entry<String, TreeSet<Integer>> elemFirst = subEntry.firstEntry();
			asNestedObject(elemFirst, writer, level + 1);
			for (Entry<String, TreeSet<Integer>> head : subEntry.tailMap(elemFirst.getKey(), false).entrySet()) {
				writer.write(",");
				asNestedObject(head, writer, level + 1);
			}
		}
		writer.write(System.lineSeparator());
		indent(level + 1, writer);
		writer.write("}");
	}

	/**
	 * Writes the InvertedIndex of elements as a nested JSON object using the
	 * provided {@link Writer} and indentation level.
	 *
	 * @param elements the elements
	 * @param writer   the writer
	 * @param level    the level
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void asInvertedIndex(TreeMap<String, TreeMap<String, TreeSet<Integer>>> elements, Writer writer,
			int level) throws IOException {

		if (elements.isEmpty()) {
			writer.write("{");
			writer.write(System.lineSeparator());
			writer.write("}");
		} else {
			writer.write("{");
			Entry<String, TreeMap<String, TreeSet<Integer>>> elemFirst = elements.firstEntry();
			asDoubleNestedObject(elemFirst, writer, 1);
			for (Entry<String, TreeMap<String, TreeSet<Integer>>> head : elements.tailMap(elemFirst.getKey(), false)
					.entrySet()) {
				writer.write(",");
				asDoubleNestedObject(head, writer, 1);
			}
			writer.write(System.lineSeparator());
			writer.write("}");
		}
	}

	/**
	 * As query result helper to write results in JSON format
	 *
	 * @param result the result
	 * @param writer the writer
	 * @param level  the level
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void asQueryResult(SearchResult result, Writer writer, int level) throws IOException {
		DecimalFormat FORMATTER = new DecimalFormat("0.000000");
		DecimalFormat form = new DecimalFormat("0");
		writer.write(System.lineSeparator());
		indent(level + 2, writer);
		writer.write("{");
		writer.write(System.lineSeparator());
		indent(level + 3, writer);
		quote("where", writer);
		writer.write(": ");
		quote(result.getLocations(), writer);
		writer.write(",");
		writer.write(System.lineSeparator());
		indent(level + 3, writer);
		quote("count", writer);
		writer.write(": ");
		writer.write(form.format(result.getCount()));
		writer.write(",");
		writer.write(System.lineSeparator());
		indent(level + 3, writer);
		quote("score", writer);
		writer.write(": ");
		writer.write(FORMATTER.format(result.getScore()));
		writer.write(System.lineSeparator());
		indent(level + 2, writer);
		writer.write("}");
	}

	/**
	 * As query result format writes query results in JSON Format
	 *
	 * 
	 * @param searchResults the search results
	 * @param writer        the writer
	 * @param level         the level
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void asQueryResultFormat(List<SearchResult> searchResults, Writer writer, int level)
			throws IOException {
		if (!searchResults.isEmpty()) {
			Iterator<SearchResult> results = searchResults.iterator();
			SearchResult firstResult = results.next();
			asQueryResult(firstResult, writer, level);
			while (results.hasNext()) {
				SearchResult nextResult = results.next();
				writer.write(",");
				asQueryResult(nextResult, writer, level);
			}
		}
	}

	/**
	 * As query word helper to write the query key in JSON Format
	 *
	 * @param result the result
	 * @param key    the key
	 * @param writer the writer
	 * @param level  the level
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void asQueryWord(TreeMap<String, List<SearchResult>> result, String key, Writer writer, int level)
			throws IOException {

		indent(level, writer);
		writer.write("{");
		writer.write(System.lineSeparator());
		indent(level + 1, writer);
		quote("queries", writer);
		writer.write(": ");
		quote(key, writer);
		writer.write(",");
		writer.write(System.lineSeparator());
		indent(level + 1, writer);
		quote("results", writer);
		writer.write(": [");
		List<SearchResult> results = result.get(key);
		asQueryResultFormat(results, writer, 1);
		writer.write(System.lineSeparator());
		indent(level + 1, writer);
		writer.write("]");
		writer.write(System.lineSeparator());
		indent(level, writer);
		writer.write("}");

	}

	/**
	 * As query writes a map of queries in JSON format
	 *
	 * @param queryResult the query result
	 * @param writer      the writer
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void asQuery(TreeMap<String, List<SearchResult>> queryResult, Writer writer) throws IOException {

		writer.write("[");
		writer.write(System.lineSeparator());
		if (!queryResult.keySet().isEmpty()) {
			Iterator<String> key = queryResult.keySet().iterator();
			String first = key.next();
			asQueryWord(queryResult, first, writer, 1);
			while (key.hasNext()) {
				String next = key.next();
				writer.write(",");
				writer.write(System.lineSeparator());
				asQueryWord(queryResult, next, writer, 1);
			}
		}
		writer.write(System.lineSeparator());
		writer.write("]");
	}

	/**
	 * Returns the set of elements formatted as a pretty JSON array of numbers.
	 *
	 * @param elements the elements to convert to JSON
	 * @return {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asArray(TreeSet, Writer, int)
	 */
	public static String asArray(TreeSet<Integer> elements) {
		try {
			StringWriter writer = new StringWriter();
			asArray(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the set of elements formatted as a pretty JSON array of numbers to the
	 * specified file.
	 *
	 * @param elements the elements to convert to JSON
	 * @param path     the path to the file write to output
	 * @throws IOException if the writer encounters any issues
	 */
	public static void asArray(TreeSet<Integer> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the double nested map of elements formatted as a nested pretty JSON
	 * object.
	 *
	 * @param elements the elements to convert to JSON
	 * @return {@link String} containing the elements in pretty JSON format
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String asInvertedIndex(TreeMap<String, TreeMap<String, TreeSet<Integer>>> elements)
			throws IOException {
		StringWriter writer = new StringWriter();
		asInvertedIndex(elements, writer, 0);
		return writer.toString();
	}

	/**
	 * Writes the double nested map of elements formatted as a nested pretty JSON
	 * object to the specified file.
	 *
	 * @param elements the elements to convert to JSON
	 * @param path     the path to the file write to output
	 * @throws IOException if the writer encounters any issues
	 *
	 */
	public static void asInvertedIndex(TreeMap<String, TreeMap<String, TreeSet<Integer>>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);) {
			asInvertedIndex(elements, writer, 0);
		}
	}

	/**
	 * Returns the query map formatted
	 *
	 * @param queryResult the query result
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String asQuery(TreeMap<String, List<SearchResult>> queryResult) throws IOException {
		StringWriter writer = new StringWriter();
		asQuery(queryResult, writer);
		return writer.toString();
	}

	/**
	 * Writes the query map formatted
	 *
	 * @param queryResult the query result
	 * @param path        the path
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void asQuery(TreeMap<String, List<SearchResult>> queryResult, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);) {
			asQuery(queryResult, writer);
		}
	}

	/**
	 * Returns as an object
	 *
	 * @param elements the elements
	 * @param path     the path
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void asObject(TreeMap<String, Integer> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asObject(elements, writer, 0);
		}
	}

	/**
	 * Writes as an object
	 *
	 * @param elements the elements
	 * @return the string
	 */
	public static String asObject(TreeMap<String, Integer> elements) {
		try {
			StringWriter writer = new StringWriter();
			asObject(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}
}