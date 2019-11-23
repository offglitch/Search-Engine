import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * The Class SearchBuilder creates a map of queries and writes them
 */
public class SearchBuilder implements SearchBuilderInterface {

	/** The results. */
	private final TreeMap<String, List<SearchResult>> results;

	/** The inverted index. */
	private final InvertedIndex index;

	/**
	 * Instantiates a new partial search builder.
	 */
	public SearchBuilder(InvertedIndex index) {
		super();
		results = new TreeMap<>();
		this.index = index;

	}

	/**
	 * Checks whether the line is empty in order to produce search results
	 *
	 * @param line  the line
	 * @param index the inverted index
	 */
	@Override
	public void queryLine(String line, boolean exact) {

		Stemmer stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
		TreeSet<String> container = new TreeSet<>();
		TextFileStemmer.stemLine(line, stemmer, container);
		String joined = String.join(" ", container);

		if (!container.isEmpty() && !results.containsKey(joined)) {
			List<SearchResult> searchResults;
			if (exact) {
				searchResults = index.exactSearch(container);
			} else {
				searchResults = index.partialSearch(container);
			}
			results.put(joined, searchResults);
		}
	}

	/**
	 * As query to JSON format.
	 *
	 * @param output the output
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public void toJSON(Path output) throws IOException {
		JSONWriter.asQuery(results, output);
	}

}