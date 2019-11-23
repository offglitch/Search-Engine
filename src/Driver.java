import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Driver {

	/**
	 * Parses the command-line arguments to build and use an in-memory search engine
	 * from files or the web.
	 *
	 * @param args the command-line arguments to parse
	 * @return 0 if everything went well
	 */
	static long start = System.nanoTime();

	public static final int DEFAULT = 5;

	public static void main(String[] args) {

		Logger logger = LogManager.getLogger();
		ArgumentMap parse = new ArgumentMap(args);
		int threads = DEFAULT;
		InvertedIndex index = null;
		SearchBuilderInterface searchBuilder = null;
//		WorkQueue queue = null;

		if (parse.hasFlag("-threads") && parse.hasValue("-threads")) {
			if (parse.hasFlag("-threads")) {
				threads = parse.getValue("-threads", threads);
				if (threads <= 0) {
					threads = DEFAULT;
				}
			}
			ThreadSafeInvertedIndex threadSafe = new ThreadSafeInvertedIndex();
			index = threadSafe;
			searchBuilder = new TSSearchBuilder(threadSafe, threads);

			if (parse.hasFlag("-path")) {
				if (parse.hasValue("-path")) {
					try {
						Path inputPath = parse.getPath("-path");
						TSInvertedIndexBuilder.traverseDirectory(inputPath, threadSafe, threads);
						logger.debug("Done with traverseDirectory");
					} catch (Exception e) {
						System.out.println("Unable to build search from path: " + parse.getPath("-path"));
					}
				}
			}

		} else {

			index = new InvertedIndex();
			searchBuilder = new SearchBuilder(index);

			if (parse.hasFlag("-path")) {
				if (parse.hasValue("-path")) {
					try {
						Path input = parse.getPath("-path");
						InvertedIndexBuilder.traverseDirectory(input, index);
					} catch (IOException e) {
						System.out.println("Unable to build index from path: " + parse.getPath("-path"));
					}
				} else {
					System.out.println("Seems to be something wrong with the path!");
				}
			}
		}

		if (parse.hasFlag("-index")) {
			Path output = parse.getPath("-index", Paths.get("index.json"));
			try {
				index.toJSON(output);
			} catch (IOException e) {
				System.out.println("Unable to build index from path: " + parse.getPath("-index"));
			}
		}

		if (parse.hasFlag("-search")) {
			Path search = parse.getPath("-search");
			try {
				searchBuilder.queryFile(search, parse.hasFlag("-exact"));
			} catch (IOException e) {
				System.out.println("Unable to build results from path: " + parse.getPath("-search"));
			}
		}

		if (parse.hasFlag("-results")) {
			Path results = parse.getPath("-results", Paths.get("results.json"));
			try {
				searchBuilder.toJSON(results);
			} catch (IOException e) {
				System.out.println("Could not write results file");
			}
		}

		if (parse.hasFlag("-locations")) {
			Path locations = parse.getPath("-locations", Paths.get("locations.json"));
			try {
				index.locJSON(locations);
			} catch (IOException e) {
				System.out.println("Unable to build locations from path: " + parse.getPath("-locations"));
			}
		}

		long elapsed = System.nanoTime() - start;
		System.out.println("Seconds: " + (elapsed / 1000000000.0));
	}
}
