import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

public class InvertedIndexBuilder {
	/**
	 * Recursively process the given path and add words/files to the inverted index.
	 * 
	 * @param root  the pathname to search. If the path is a directory, this method
	 *              will be called recursively for each directory/file in that path.
	 * 
	 * @param index the InvertedIndex into which all the words encountered will be
	 *              added after stemming.
	 * 
	 * @throws IOException
	 */
	public static void traverseDirectory(Path root, InvertedIndex index) throws IOException {
		if (Files.isDirectory(root)) {
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
				for (Path current : stream) {
					traverseDirectory(current, index);
				}
			}
		} else if (isTextFile(root)) {
			addFiles(root, index);
		}
	}

	/**
	 * Takes a path and filters out any files that aren't .txt or .text files
	 * 
	 * @param root the path to convert into a file
	 * 
	 * @returns returns normalized .txt and .text files
	 */
	public static boolean isTextFile(Path path) {
		String name = path.getFileName().toString().toLowerCase();
		return Files.isRegularFile(path) && (name.endsWith(".txt") || name.endsWith(".text"));
	}

	/**
	 * Stem the words/files by line singularly to the inverted index
	 * 
	 * @param root  the pathname of a single file that will be stemmed
	 * @param index the InvertedIndex which all words encountered will be stemmed
	 *              per file
	 * @throws IOException
	 */
	public static void addFiles(Path root, InvertedIndex index) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(root, StandardCharsets.UTF_8)) {
			String line;
			String fileName = root.toString();
			int count = 1;
			Stemmer stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
			while ((line = reader.readLine()) != null) {
				String[] words = TextParser.split(TextParser.clean(line));
				for (String word : words) {
					index.addWord(stemmer.stem(word).toString(), fileName, count++);
				}
			}
		}
	}

}