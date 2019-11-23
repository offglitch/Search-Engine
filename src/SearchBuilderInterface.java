import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The Interface SearchBuilderInterface.
 */
public interface SearchBuilderInterface {

	/**
	 * Query file.
	 *
	 * @param root  the root file
	 * @param exact the exact boolean for searching
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public default void queryFile(Path root, boolean exact) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(root, StandardCharsets.UTF_8)) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				queryLine(line, exact);
			}
		}
	}

	/**
	 * Query line.
	 *
	 * @param root  the root file
	 * @param exact the exact boolean for searching
	 */
	public void queryLine(String line, boolean exact);

	/**
	 * To JSON format.
	 *
	 * @param output the output
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void toJSON(Path output) throws IOException;

}
