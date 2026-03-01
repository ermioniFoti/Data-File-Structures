package domes_1;

import java.io.IOException;
import java.util.Random;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class Main {

	static final int[] NUM_OF_RECORDS = { 50, 100, 200, 500, 800, 1000, 2000, 5000, 10000, 50000, 100000, 200000 };

	static final int[] DATA_SIZES = { 27, 55 };

	static final Random RAND = new Random();

	static final int NUM_SEARCHES = 1000;

	public static void main(String[] args) throws IOException {
		for (final int n : NUM_OF_RECORDS) {
			// generate n rand keys
			int[] keys = RAND.ints(1, 2 * n + 1).limit(n).toArray();
			for (final int dataSize : DATA_SIZES) {
				// Generate n instances of our DataClass
				DataClass[] items = new DataClass[n];
				for (int i = 0; i < n; i++) {
					items[i] = new DataClass(keys[i], generateAlphaNumericString(dataSize));
				}
				// Open data file and index files
				RandomAccessFile f = new RandomAccessFile("data", "rw");
				RandomAccessFile indexB = new RandomAccessFile("index-B", "rw");
				RandomAccessFile indexC = new RandomAccessFile("index-C", "rw");
				f.setLength(0L); // discard existing data
				indexB.setLength(0L);
				indexC.setLength(0L);

				Pair[] pairs = buildFile(items, dataSize, f);
				int[] searchKeys = createKeys(n);
				// Test search
				System.out.println("Number of Records: " + n);
				System.out.println("Data Size: " + dataSize);
				System.out.println(
						"--------------------------------------------------------------------------------------------");
				SearchStats statsA = searchA(f, dataSize, searchKeys);
				System.out.println("searchA:");
				printStats(statsA, NUM_SEARCHES);
				// Test search B
				buildIndexFile(pairs, indexB);
				SearchStats statsB = searchB(f, indexB, dataSize, searchKeys);
				System.out.println("searchB:");
				printStats(statsB, searchKeys.length);
				// Test search C
				Arrays.sort(pairs);
				buildIndexFile(pairs, indexC);
				SearchStats statsC = searchC(f, indexC, dataSize, searchKeys);
				System.out.println("SearchC:");
				printStats(statsC, NUM_SEARCHES);
				// Cleanup
				f.close();
				indexB.close();
				indexC.close();
			}
		}
	}

	static int[] createKeys(int numOfRecords) {
		int[] searchKeys;
		if (numOfRecords <= 1000) {
			searchKeys = RAND.ints(1, 2 * numOfRecords + 1).limit(NUM_SEARCHES).toArray();
			return searchKeys;

		} else {
			searchKeys = RAND.ints(1, 2 * numOfRecords + 1).distinct().limit(NUM_SEARCHES).toArray();
			return searchKeys;
		}
	}

	static SearchStats searchA(RandomAccessFile f, int dataSize, int[] searchKeys) throws IOException {

		DataPage p = new DataPage(dataSize);
		// Find how many pages exist
		int numPages = (int) (f.length() / DataPage.DATA_PAGE_LENGTH);
		// System.out.printf("number-of-pages=%d page-size=%d num-record-per-page=%d\n",
		// numPages, DataPage.DATA_PAGE_LENGTH, p.getNumRecordsPerPage());

		SearchStats stats = new SearchStats(System.nanoTime());
		for (int searchKey : searchKeys) {
			// Fetch all pages and search into array of records
			boolean found = false;
			for (int pageNumber = 0; pageNumber < numPages && !found; pageNumber++) {
				DataClass[] records = p.readFromFile(f, pageNumber);
				stats.incrPageReadCount();
				for (int i = 0; i < records.length; i++) {
					if (records[i] == null) {
						break;
					}
					if (records[i].getKey() == searchKey) {
						// System.out.printf("Found key at page %d: %d\n", pageNumber, searchKey);
						found = true;
						break;
					}
				}
			}
		}
		stats.setEndTime(System.nanoTime());
		return stats;
	}

	static SearchStats searchB(RandomAccessFile f, RandomAccessFile indexB, int dataSize, int[] searchKeys)
			throws IOException {

		IndexPage iPage = new IndexPage();
		DataPage p = new DataPage(dataSize);
		// Find how many pages exist
		int numPages = (int) (indexB.length() / IndexPage.DATA_PAGE_LENGTH);
		SearchStats stats = new SearchStats(System.nanoTime());

		for (int searchKey : searchKeys) {
			// Fetch all pages and search into array of records
			boolean found = false;
			for (int i = 0; i < numPages && !found; i++) {
				Pair[] pairs = iPage.readFromFile(indexB, i);
				stats.incrPageReadCount();
				for (int j = 0; j < pairs.length; j++) {
					if (pairs[j] == null)
						break;
					if (pairs[j].getKey() == searchKey) {
						DataClass[] records = p.readFromFile(f, pairs[j].getPageNumber());
						stats.incrPageReadCount();
						for (int k = 0; k < p.getNumRecordsPerPage() && !found; k++) {
							if (records[k] == null) {
								break;
							}
							if (records[k].getKey() == searchKey) {
								found = true;
								break;
							}
						}
					}

				}
			}
		}
		stats.setEndTime(System.nanoTime());
		return stats;

	}

	static SearchStats searchC(RandomAccessFile f, RandomAccessFile indexC, int dataSize, int[] searchKeys)
			throws IOException {
		IndexPage iPage = new IndexPage();
		DataPage p = new DataPage(dataSize);
		// Find how many pages exist
		int numPages = (int) (indexC.length() / IndexPage.DATA_PAGE_LENGTH);
		SearchStats stats = new SearchStats(System.nanoTime());

		boolean found = false;
		for (int searchKey : searchKeys) {
			// Fetch all pages and search into array of records

			Pair searchPair = binarySearch(indexC, iPage, 0, numPages, searchKey, stats);
			if (searchPair == null)
				continue;
			DataClass[] records = p.readFromFile(f, searchPair.getPageNumber());
			stats.incrPageReadCount();
			for (int k = 0; k < p.getNumRecordsPerPage() && !found; k++) {
				if (records[k] == null) {
					break;
				}
				if (records[k].getKey() == searchKey) {
					found = true;
					break;
				}
			}
		}
		stats.setEndTime(System.nanoTime());
		return stats;

	}

	static Pair binarySearch(RandomAccessFile indexC, IndexPage iPage, int leftIndex, int rightIndex, int searchKey,
			SearchStats stats) throws IOException {

		if (rightIndex >= leftIndex) {
			int mid = leftIndex + (rightIndex - leftIndex) / 2;
			stats.incrPageReadCount();
			Pair[] pairs = iPage.readFromFile(indexC, mid);

			if (pairs[0].getKey() > searchKey)
				return binarySearch(indexC, iPage, leftIndex, mid - 1, searchKey, stats);

			int lastKey = 0;
			for (int i = 0; i < iPage.getNumPairsPerPage(); i++) {
				// If the element is present at the
				// middle itself
				if (pairs[i] == null)
					break;

				lastKey = pairs[i].getKey();

				if (lastKey == searchKey)
					return pairs[i];

				else if (lastKey > searchKey)
					break;

			}

			if (lastKey < searchKey) {
				return binarySearch(indexC, iPage, mid + 1, rightIndex, searchKey, stats);
			}
		}
		// We reach here when element is not present in array.
		// We return Integer.MIN_VALUE in this case, so the data array can not contain
		// this value!
		return null;
	}

	static Pair[] buildFile(DataClass[] records, int dataSize, RandomAccessFile f) throws IOException {
		DataPage p = new DataPage(dataSize);
		int numRecordsPerPage = p.getNumRecordsPerPage();
		Pair[] pairs = new Pair[records.length];
		for (int i = 0, pageNumber = 0; i < records.length; i += numRecordsPerPage, pageNumber++) {
			DataClass[] recordsOfPage = Arrays.copyOfRange(records, i, i + numRecordsPerPage);
			p.writeToFile(f, recordsOfPage, pageNumber);
			// Create pairs of (key, pagenumber) for each record that belongs to current
			// page
			int j1 = i + numRecordsPerPage;
			if (j1 > records.length)
				j1 = records.length;
			for (int j = i; j < j1; j++) {
				pairs[j] = new Pair(records[j].getKey(), pageNumber);
			}
		}
		return pairs;
	}

	static void buildIndexFile(Pair[] pairs, RandomAccessFile f) throws IOException {
		IndexPage p = new IndexPage();
		int numPairsPerPage = p.getNumPairsPerPage();

		for (int i = 0, pageNumber = 0; i < pairs.length; i += numPairsPerPage, pageNumber++) {
			Pair[] pairsOfPage = Arrays.copyOfRange(pairs, i, i + numPairsPerPage);
			p.writeToFile(f, pairsOfPage, pageNumber);
		}
	}

	static void printStats(SearchStats stats, int numSearches) {
		// Print results
		long elapsed = stats.getTimeElapsed(); // nanosecond
		int pageReadCount = stats.getPageReadCount();
		System.out.printf("time=%d ns/search\npage-reads=%.1f/search\n", elapsed / numSearches,
				((double) pageReadCount) / numSearches);
		System.out.println(
				"--------------------------------------------------------------------------------------------");
	}

	// function to generate a random string of length n
	static String generateAlphaNumericString(int n) {

		// choose a Character random from this String
		String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789" + "abcdefghijklmnopqrstuvxyz";

		// create StringBuffer size of AlphaNumericString
		StringBuilder sb = new StringBuilder(n);

		for (int i = 0; i < n; i++) {
			// generate a random number between
			// 0 to AlphaNumericString variable length
			int index = (int) (AlphaNumericString.length() * Math.random());

			// add Character one by one in end of sb
			sb.append(AlphaNumericString.charAt(index));
		}

		return sb.toString();
	}

}