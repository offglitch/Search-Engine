
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ThreadSafeInvertedIndex extends InvertedIndex {

	private final ReadWriteLock lock;

	public static final Logger logger = LogManager.getLogger();

	public ThreadSafeInvertedIndex() {
		super();
		lock = new ReadWriteLock();
	}

	@Override
	public int count(String word) {
		lock.lockReadOnly();
		try {
			return super.count(word);
		} finally {
			lock.unlockReadOnly();
		}
	}

	@Override
	public boolean contains(String word, String location, int position) {
		lock.lockReadOnly();
		try {
			return super.contains(word, location, position);
		} finally {
			lock.unlockReadOnly();
		}
	}

	@Override
	public boolean contains(String word) {
		lock.lockReadOnly();
		try {
			return super.contains(word);
		} finally {
			lock.unlockReadOnly();
		}
	}

	@Override
	public boolean contains(String word, String location) {
		lock.lockReadOnly();
		try {
			return super.contains(word, location);
		} finally {
			lock.unlockReadOnly();
		}
	}

	@Override
	public int locations(String word) {
		lock.lockReadOnly();
		try {
			return super.locations(word);
		} finally {
			lock.unlockReadOnly();
		}
	}

	@Override
	public int positions(String word, String location) {
		lock.lockReadOnly();
		try {
			return super.positions(word, location);
		} finally {
			lock.unlockReadOnly();
		}
	}

	@Override
	public void addWord(String word, String location, int position) {
		lock.lockReadWrite();
		try {
			super.addWord(word, location, position);
			logger.debug("done adding");
		} finally {
			lock.unlockReadWrite();
		}
	}

	@Override
	public List<SearchResult> exactSearch(TreeSet<String> query) {
		lock.lockReadOnly();
		try {
			return super.exactSearch(query);
		} finally {
			lock.unlockReadOnly();
		}
	}

	@Override
	public List<SearchResult> partialSearch(TreeSet<String> query) {
		lock.lockReadOnly();
		try {
			return super.partialSearch(query);
		} finally {
			lock.unlockReadOnly();
		}
	}

	@Override
	public void toJSON(Path path) throws IOException {
		lock.lockReadOnly();
		try {
			super.toJSON(path);
		} finally {
			lock.unlockReadOnly();
		}
	}

	@Override
	public void locJSON(Path path) throws IOException {
		lock.lockReadOnly();
		try {
			super.locJSON(path);
		} finally {
			lock.unlockReadOnly();
		}
	}

	@Override
	public void addAll(InvertedIndex other) {
		lock.lockReadWrite();
		try {
			super.addAll(other);
		} finally {
			lock.unlockReadWrite();
		}
	}

}