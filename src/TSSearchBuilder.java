import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * The Class TSSearchBuilder builds a multithreaded search result and adds it to
 * the inverted index.
 */
public class TSSearchBuilder implements SearchBuilderInterface {

	/** The results. */
	private final TreeMap<String, List<SearchResult>> results;

	/** The Constant logger. */
	private static final Logger logger = LogManager.getLogger();

	/** The threads. */
	private final int threads;

	/** The index. */
	private final ThreadSafeInvertedIndex index;

	/**
	 * Instantiates a new thread safe multithreaded search builder.
	 *
	 * @param minions the minions
	 * @param index   the index
	 */
	public TSSearchBuilder(ThreadSafeInvertedIndex index, int threads) {
		results = new TreeMap<>();
		this.threads = threads;
		this.index = index;
	}

	/*
	 * 
	 * 
	 * @see SearchBuilderInterface#queryFile(java.nio.file.Path, boolean)
	 */

	public void queryFile(Path root, boolean exact) throws IOException {
		WorkQueue minions = new WorkQueue(threads);
		try (BufferedReader reader = Files.newBufferedReader(root, StandardCharsets.UTF_8)) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				minions.execute(new SearchMinion(line, exact));
			}
		}
		minions.finish();
		minions.shutdown();
	}

	/*
	 * 
	 * 
	 * @see SearchBuilderInterface#queryLine(java.lang.String, boolean)
	 */

	public void queryLine(String line, boolean exact) {
		Stemmer stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
		TreeSet<String> container = new TreeSet<>();
		TextFileStemmer.stemLine(line, stemmer, container);
		String joined = String.join(" ", container);

		if (container.isEmpty() || results.containsKey(joined)) {
			return;
		}

		if (!container.isEmpty() && !results.containsKey(joined)) {
			List<SearchResult> searchResults;
			if (exact) {
				searchResults = index.exactSearch(container);
			} else {
				searchResults = index.partialSearch(container);
			}
			synchronized (results) {
				results.put(joined, searchResults);
			}
		}

	}

	/*
	 * 
	 * 
	 * @see SearchBuilderInterface#toJSON(java.nio.file.Path)
	 */

	public void toJSON(Path output) throws IOException {
		synchronized (results) {
			JSONWriter.asQuery(results, output);
		}
	}

	private class SearchMinion implements Runnable {

		private String line;

		private boolean exact;

		/**
		 * Instantiates a new search minion.
		 *
		 * @param line  the line
		 * @param exact the exact
		 */
		public SearchMinion(String line, boolean exact) {
			logger.debug("Minion created for {}", line);
			this.line = line;
			this.exact = exact;
		}

		/*
		 * 
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {

			logger.debug("Minion running for {}", line);
			queryLine(line, exact);
			logger.debug("Minion finished for {}", line);
		}

	}

}