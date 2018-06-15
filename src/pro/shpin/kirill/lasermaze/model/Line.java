package pro.shpin.kirill.lasermaze.model;

public class Line {

	public final double x1;
	public final double y1;
	public final double x2;
	public final double y2;

	public Line(double x1, double y1, double x2, double y2) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	@Override
	public String toString() {
		return "{(" + x1 + ", " + y1 + ")" + ", " + "(" + x2 + ", " + y2 + ")}";
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof Line) && (
				(((x1 == ((Line) o).x1) && y1 == ((Line) o).y1) && (x2 == ((Line) o).x2) && y2 == ((Line) o).y2) ||
				(((x1 == ((Line) o).x2) && y1 == ((Line) o).y2) && (x2 == ((Line) o).x1) && y2 == ((Line) o).y1)
		);
	}
}
