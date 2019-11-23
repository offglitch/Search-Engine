import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * The Class InvertedIndex stores words info to an index and creates search
 * results from queries
 */
public class InvertedIndex {

	/** The index */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> index;

	/** The word counts and their locations */
	private final TreeMap<String, Integer> wordCounts;

	/**
	 * Initializes this InvertedIndex.
	 */
	public InvertedIndex() {
		wordCounts = new TreeMap<>();
		index = new TreeMap<>();
	}

	/**
	 * Returns the number of times a word was found (i.e. the number of positions
	 * associated with a word in the index).
	 *
	 * @param word word to look for
	 * @return number of times the word was found
	 */
	public int count(String word) {
		TreeMap<String, TreeSet<Integer>> wordMap = index.get(word);
		if (wordMap == null) {
			return 0;
		} else {
			int count = 0;
			for (TreeSet<Integer> places : wordMap.values()) {
				count += places.size();
			}
			return count;
		}
	}

	/**
	 * Tests whether the index contains the specified word.
	 *
	 * @param word which is the word to look for
	 * 
	 * @return true if the word is stored in the index
	 */
	public boolean contains(String word) {
		return index.containsKey(word);
	}

	/**
	 * Tests whether the index contains the specified word in the specified
	 * location.
	 *
	 * @param word     word to look for, location the word is in
	 * @param location is location the word is in
	 * @return true if the word in location is stored in the index
	 */
	public boolean contains(String word, String location) {
		return index.containsKey(word) && index.get(word).containsKey(location);
	}

	/**
	 * Tests whether the index contains the specified word at the specified position
	 * and location.
	 * 
	 * @param word     word to look for
	 * @param location is location the word is in
	 * @param position to look for word
	 * @return true if the word is stored in the index
	 */
	public boolean contains(String word, String location, int position) {
		return contains(word, location) && index.get(word).get(location).contains(position);
	}

	/**
	 * Returns the locations stored in the index.
	 *
	 * @param word the word
	 * @return number of words
	 */
	public int locations(String word) {
		if (contains(word)) {
			return index.get(word).size();
		} else {
			return 0;
		}
	}

	/**
	 * Returns the positions in the index.
	 *
	 * @param word     the word
	 * @param location the location
	 * @return number of words
	 */
	public int positions(String word, String location) {
		if (contains(word, location)) {
			return index.get(location).size();
		} else {
			return 0;
		}
	}

	/**
	 * Adds words, their positions and files to index.
	 *
	 * @param word     the word
	 * @param location the location
	 * @param position the position
	 * @return word location position
	 */
	public void addWord(String word, String location, int position) {
		TreeMap<String, TreeSet<Integer>> wordMap = index.get(word);
		if (wordMap == null) {
			wordMap = new TreeMap<>();
			index.put(word, wordMap);
		}
		TreeSet<Integer> positions = wordMap.get(location);
		if (positions == null) {
			positions = new TreeSet<>();
			wordMap.put(location, positions);
		}
		wordCounts.merge(location, 1, Integer::sum);
		positions.add(position++);
	}

	/**
	 * Search helper method.
	 *
	 * @param word       the word
	 * @param searchMap  the search map
	 * @param resultList the result list
	 */
	private void searchHelper(String word, Map<String, SearchResult> searchMap, List<SearchResult> resultList) {

		for (String location : index.get(word).keySet()) {
			int count = index.get(word).get(location).size();
			int total = wordCounts.get(location);

			if (searchMap.containsKey(location)) {
				searchMap.get(location).updateMatches(count);

			} else {
				SearchResult searchResult = new SearchResult(location, count, total);
				searchMap.putIfAbsent(location, searchResult);
				resultList.add(searchResult);
			}
		}
	}

	/**
	 * Exact search.
	 *
	 * @param query the cleaned treeset of queries
	 * @return the list
	 */
	public List<SearchResult> exactSearch(TreeSet<String> query) {

		HashMap<String, SearchResult> searchMap = new HashMap<>();

		List<SearchResult> resultList = new ArrayList<>();

		for (String word : query) {
			if (index.containsKey(word)) {
				searchHelper(word, searchMap, resultList);
			}
		}

		Collections.sort(resultList);
		return resultList;
	}

	/**
	 * Partial search.
	 *
	 * @param query the cleaned TreeSet of queries
	 * @return the list
	 */
	public List<SearchResult> partialSearch(TreeSet<String> query) {
		HashMap<String, SearchResult> searchMap = new HashMap<>();
		List<SearchResult> resultList = new ArrayList<>();

		for (String words : query) {
			for (String word : index.tailMap(words).keySet()) {
				if (word.startsWith(words)) {
					searchHelper(word, searchMap, resultList);
				} else {
					break;
				}
			}
		}

		Collections.sort(resultList);
		return resultList;

	}

	/**
	 * Writing InvertedIndex to JSON.
	 *
	 * @param path the path
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void toJSON(Path path) throws IOException {
		JSONWriter.asInvertedIndex(index, path);
	}

	/**
	 * Writing locations to JSON.
	 *
	 * @param path the path
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void locJSON(Path path) throws IOException {
		JSONWriter.asObject(wordCounts, path);
	}

	/**
	 * Adds local inverted index to global inverted index
	 *
	 * @param currentIndex the current index
	 */
	public void addAll(InvertedIndex other) {
		for (String word : other.index.keySet()) {
			if (!this.index.containsKey(word)) {
				this.index.put(word, other.index.get(word));
			} else {
				for (String path : other.index.get(word).keySet()) {
					if (index.get(word).containsKey(path) == false) {
						this.index.get(word).put(path, other.index.get(word).get(path));
					} else {
						this.index.get(word).get(path).addAll(other.index.get(word).get(path));
					}
				}
			}
		}
		for (String word : other.wordCounts.keySet()) {
			if (!this.wordCounts.containsKey(word)) {
				this.wordCounts.put(word, other.wordCounts.get(word));
			} else {
				this.wordCounts.put(word, other.wordCounts.get(word) + this.wordCounts.get(word));
			}
		}
	}

}