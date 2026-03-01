package domes_1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.io.RandomAccessFile;

public class IndexPage {
	public static final int DATA_PAGE_LENGTH = 256;
	private int numPairsPerPage;
	private ByteBuffer buf;
	private byte[] arr;

	public IndexPage() {
		this.numPairsPerPage = DATA_PAGE_LENGTH / ((2 * Integer.SIZE / 8));
		this.buf = ByteBuffer.allocate(DATA_PAGE_LENGTH);
		this.arr = new byte[DATA_PAGE_LENGTH];
	}

	public int getNumPairsPerPage() {
		return numPairsPerPage;
	}

	public Pair[] readFromFile(RandomAccessFile f, int pageNumber) throws IOException {
		f.seek(pageNumber * DATA_PAGE_LENGTH);
		f.read(arr);

		ByteBuffer arrBuf = ByteBuffer.wrap(arr);
		Pair[] pairs = new Pair[numPairsPerPage];
		for (int i = 0; i < numPairsPerPage; i++) {
			int key = arrBuf.getInt();
			if (key < 0)
				break;
			int dataPageNumber = arrBuf.getInt();
			pairs[i] = new Pair(key, dataPageNumber);
		}

		return pairs;
	}

	public void writeToFile(RandomAccessFile f, Pair[] pairs, int pageNumber) throws IOException {
		buf.clear();

		for (int i = 0; i < numPairsPerPage; i++) {
			Pair p = pairs[i];
			if (p != null) {
				buf.putInt(p.getKey());
				buf.putInt(p.getPageNumber());
			} else {
				// mark end of pairs
				buf.putInt(-1);
			}
		}

		f.seek(pageNumber * DATA_PAGE_LENGTH);
		f.write(buf.array());
	}

}