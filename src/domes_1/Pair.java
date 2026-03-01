package domes_1;

public class Pair implements Comparable<Pair> {

	private int key;
	private int pageNumber;

	public Pair(int key, int pageNumber) {

		this.key = key;
		this.pageNumber = pageNumber;
	}

	public int compareTo(Pair other) {
		return key - other.key;
	}

	public int getKey() {
		return key;
	}

	public int getPageNumber() {
		return pageNumber;
	}
}


