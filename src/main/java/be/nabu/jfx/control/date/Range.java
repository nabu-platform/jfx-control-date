package be.nabu.jfx.control.date;

public class Range<T extends Number> {
	
	private T start, end;

	public Range(T start, T end) {
		this.start = start;
		this.end = end;
	}

	public T getStart() {
		return start;
	}

	public void setStart(T start) {
		this.start = start;
	}

	public T getEnd() {
		return end;
	}

	public void setEnd(T end) {
		this.end = end;
	}
	
	@Override
	public String toString() {
		return "[" + start + ", " + end + "]";
	}
}
