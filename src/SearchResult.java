/**
 * The Class SearchResult stores a single search result and implements the
 * Comparable interface
 */
public class SearchResult implements Comparable<SearchResult> {

	/** The locations. */
	private String locations;

	/** The count. */
	private int count;

	/** The score. */
	private double score;

	/** The total. */
	private int total;

	/**
	 * Instantiates a new search result.
	 *
	 * @param locations the locations
	 * @param count     the count
	 * @param total     the total
	 */
	public SearchResult(String locations, int count, int total) {
		super();
		this.locations = locations; // where
		this.count = count; // match
		this.total = total; // total
		this.score = (double) count / total; // score
	}

	/**
	 * Gets the count which is the total matches.
	 *
	 * @return the count
	 */
	public int getCount() {
		return this.count;
	}

	/**
	 * Gets the locations where the matches where found.
	 *
	 * @return the locations
	 */
	public String getLocations() {
		return this.locations;
	}

	/**
	 * Gets the score.
	 *
	 * @return the score
	 */
	public double getScore() {
		return this.score;
	}

	/**
	 * Update the matches matches and recalculate the score.
	 *
	 * @param found the matches found
	 */
	public void updateMatches(int found) {

		this.count += found;

		this.score = (double) count / total;
	}

	@Override
	public int compareTo(SearchResult o) {
		if (this.score != o.score) {
			return Double.compare(o.score, this.score);
		} else {
			if (this.count != o.getCount()) {
				return Integer.compare(o.count, this.count);
			} else {
				return this.locations.compareToIgnoreCase(o.locations);
			}
		}
	}
}