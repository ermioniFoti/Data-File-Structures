package domes_1;

public class SearchStats {

	long t0, t1;
	int pageReadCount;

	public SearchStats(long t0) {
		this.t0 = t0;
		this.pageReadCount = 0;
	}

	public void incrPageReadCount() {
		this.pageReadCount++;
	}

	public long getStartTime() {
		return t0;
	}

	public int getPageReadCount() {
		return pageReadCount;
	}

	public void setEndTime(long t1) {
		this.t1 = t1;
	}

	public long getTimeElapsed() {
		return t1 - t0;
	}
}