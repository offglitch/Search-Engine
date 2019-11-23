import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Class TSInvertedIndexBuilder builds an inverted index from a thread safe
 * inverted index class.
 */
public class TSInvertedIndexBuilder {

	/** The Constant logger. */
	private static final Logger logger = LogManager.getLogger();

	/**
	 * Traverses a given directory and calls the private traverseDirectory method.
	 *
	 * @param root    the root file
	 * @param index   the thread safe index
	 * @param threads the number of threads
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void traverseDirectory(Path root, ThreadSafeInvertedIndex index, int threads) throws IOException {
		WorkQueue minions = new WorkQueue(threads);
		traverseDirectory(root, index, minions);
		minions.finish();
		minions.shutdown();

	}

	/**
	 * Traverses a given directory recursively and checks for proper file format
	 * then performs.
	 *
	 * @param root    the root
	 * @param index   the index
	 * @param minions the minions
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static void traverseDirectory(Path root, ThreadSafeInvertedIndex index, WorkQueue minions)
			throws IOException {
		if (Files.isDirectory(root)) {
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
				for (Path current : stream) {
					traverseDirectory(current, index, minions);
				}
			}
		} else if (InvertedIndexBuilder.isTextFile(root)) {
			minions.execute(new AddFilesMinion(root, index));
			logger.debug("execute");
		}
	}

	// TODO private
	private static class AddFilesMinion implements Runnable {

		/** The path. */
		private Path path;

		/** The index. */
		private ThreadSafeInvertedIndex index;

		/**
		 * Instantiates a new minion.
		 *
		 * @param path  the path
		 * @param index the index
		 */
		public AddFilesMinion(Path path, ThreadSafeInvertedIndex index) {
			logger.debug("Minion created for {}", path);
			this.path = path;
			this.index = index;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try {
				InvertedIndex local = new InvertedIndex();
				InvertedIndexBuilder.addFiles(path, local);
				index.addAll(local);
			} catch (IOException e) {
				logger.warn("Unable to build index from path {}", path);
				logger.catching(Level.DEBUG, e);
			}
			logger.debug("Minion done adding {}", path);
		}
	}
}