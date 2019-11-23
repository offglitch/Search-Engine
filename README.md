# Search Engine

A multi-threaded search engine that utilizes a web crawler which crawls through a given link and parses out the HTML. It searches and stores all the text into an inverted index data structure that keeps track of each word's position, frequency, and page number.

* Inverted Index: Processes all text files in a directory and its subdirectories, cleans and parses the text into word stems, and builds an in-memory inverted index to store the mapping from word stems to the documents and position within those documents where those word stems were found.

* Partial Search: Support for exact search and partial search. Tracks the total number of words found in each text file, parses and stems a query file, generates a sorted list of search results from the inverted index, and supports writing those results to a JSON file.

* Multithreading: Thread-safety was created to use a work queue to build and search an inverted index using multiple threads.

* Web Crawler: A multithreaded web crawler using a work queue to build the index from a seed URL.

* Web Interface: An interface using embedded Jetty and servlets to search an index. A webpage with a text box is displayed where users may enter a multi-word search query with a button that submits the query to the search engine. The search engine then performs a partial search from an inverted index generated by the web crawler, and returns an HTML page with sorted and clickable links to the search results. The interface has session tracking and cookies to store per-user information. 

