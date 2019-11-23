import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

public class TextFileStemmer {

	/**
	 * Returns a list of cleaned and stemmed words parsed from the provided line.
	 * Uses the English {@link SnowballStemmer.ALGORITHM} for stemming.
	 *
	 * @param line the line of words to clean, split, and stem
	 * @return list of cleaned and stemmed words
	 *
	 * @see SnowballStemmer
	 * @see SnowballStemmer.ALGORITHM#ENGLISH
	 * @see #stemLine(String, Stemmer)
	 */
	public static List<String> stemLine(String line) {
		// This is provided for you.
		return stemLine(line, new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH));
	}

	/**
	 * Returns a list of cleaned and stemmed words parsed from the provided line.
	 *
	 * @param line    the line of words to clean, split, and stem
	 * @param stemmer the stemmer to use
	 * @return list of cleaned and stemmed words
	 *
	 * @see Stemmer#stem(CharSequence)
	 * @see TextParser#parse(String)
	 */
	public static List<String> stemLine(String line, Stemmer stemmer) {
		ArrayList<String> list = new ArrayList<>();
		String[] words = TextParser.split(TextParser.clean(line));
		for (String word : words) {

			list.add(stemmer.stem(word).toString());
		}

		return list;
	}

	/**
	 * Performs cleaning and stemming of words parsed from the provided line into a
	 * container.
	 *
	 * @param line      the line of words to clean, split, and stem
	 * @param stemmer   the stemmer to use
	 * @param container the container to contain the words in
	 * @return list of cleaned and stemmed words
	 *
	 * @see Stemmer#stem(CharSequence)
	 * @see TextParser#parse(String)
	 */
	public static void stemLine(String line, Stemmer stemmer, Collection<String> container) {
		String[] words = TextParser.split(TextParser.clean(line));
		for (String word : words) {
			container.add(stemmer.stem(word).toString());
		}
	}

	/**
	 * Reads a file line by line, parses each line into cleaned and stemmed words,
	 * and then writes that line to a new file.
	 *
	 * @param inputFile  the input file to parse
	 * @param outputFile the output file to write the cleaned and stemmed words
	 * @throws IOException if unable to read or write to file
	 *
	 * @see #stemLine(String)
	 * @see TextParser#parse(String)
	 */
	public static void stemFile(Path inputFile, Path outputFile) throws IOException {
		try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile.toFile()));
				Scanner sc = new Scanner(inputFile.toFile())) {
			while (sc.hasNextLine()) {
				List<String> stems = stemLine(sc.nextLine());
				if (stems.isEmpty()) {
					writer.println();
				} else {
					writer.print(String.join(" ", stems));
					writer.println(" ");
				}

			}
			writer.close();
		}
	}
}