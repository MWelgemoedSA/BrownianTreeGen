package brownian_tree;

public class Coordinate {
	public int x;
	public int y;

	Coordinate() {
		x = 0;
		y = 0;
	}

	Coordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof Coordinate)) {
			return false;
		}

		Coordinate other = (Coordinate) obj;
		return x == other.x && y == other.y;
	}

	public int hashCode() {
		return x * 1000 + y;
	}

	public void wrapAround(int maxX, int maxY) {
		if (x < 0) {
			x += maxX;
		} else if(x >= maxX) {
			x -= maxX;
		}
		
		if (y < 0) {
			y += maxY;
		} else if (y >= maxY) {
			y -= maxY;
		}
	}
}
