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

public class DataPage {

	public static final int DATA_PAGE_LENGTH = 256;
	private int dataSize; // size of data field of our class
	private int numRecordsPerPage;
	private ByteBuffer buf;
	private byte[] arr;

	public DataPage(int dataSize) {
		this.dataSize = dataSize;
		this.numRecordsPerPage = DATA_PAGE_LENGTH / (dataSize + (Integer.SIZE / 8));
		this.buf = ByteBuffer.allocate(DATA_PAGE_LENGTH);
		this.arr = new byte[DATA_PAGE_LENGTH];
	}

	int getNumRecordsPerPage() {
		return this.numRecordsPerPage;
	}

	public DataClass[] readFromFile(RandomAccessFile f, int pageNumber) throws IOException {
		f.seek(pageNumber * DATA_PAGE_LENGTH);
		f.read(arr);

		ByteBuffer arrBuf = ByteBuffer.wrap(arr);
		DataClass[] records = new DataClass[numRecordsPerPage];
		byte[] data = new byte[dataSize];
		for (int i = 0; i < numRecordsPerPage; i++) {
			int key = arrBuf.getInt();
			if (key < 0)
				break;

			arrBuf.get(data);
			records[i] = new DataClass(key, new String(data, StandardCharsets.US_ASCII));
		}

		return records;
	}

	public void writeToFile(RandomAccessFile f, DataClass[] records, int pageNumber) throws IOException {
		buf.clear();

		for (int i = 0; i < numRecordsPerPage; i++) {
			DataClass d = records[i];
			if (d != null) {
				buf.putInt(d.getKey());
				byte[] data = d.getData().getBytes(StandardCharsets.US_ASCII);
				buf.put(data);
			} else {
				// mark end of records
				buf.putInt(-1);
			}
		}

		f.seek(pageNumber * DATA_PAGE_LENGTH);
		f.write(buf.array());

	}

}