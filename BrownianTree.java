import java.util.HashMap;
import java.util.Random;

public class BrownianTree {
	public static void main(String... args) {
		int argCount = args.length;
		
		//Defaults overridden by command line arguments
		int xSize = 100;
		int ySize = 100;
		int pixelCount = 2000;
		
		try {
			if (argCount >= 1) {
				xSize = Integer.parseInt(args[0]);
			}
			if (argCount >= 2) {
				ySize = Integer.parseInt(args[1]);
			}
			if (argCount >= 3) {
				pixelCount = Integer.parseInt(args[2]);
			}
		} catch (NumberFormatException e) {
        		System.err.println("Command line arguments are xSize, ySize, pixelCount, all must be integers");
			System.exit(1);
		}
		
		BrownianTree instance = new BrownianTree(xSize, ySize);
		instance.run(pixelCount);
	}

	World world;
	Random randomGen = new Random();

	BrownianTree(int xsize, int ysize) {
		world = new World(xsize, ysize);
	}

	public void run(int totalPixels) {
		world.placeCenterPixel();		
		for(int i = 0; i != totalPixels; i++) {
			placePixel();
		}
		world.print();
	}

	void placePixel() {
		final int maxX = world.getXSize();
		final int maxY = world.getYSize();
		
		//Find an empty spot to start on
		Coordinate c = new Coordinate(0, 0);
		do {
			c.x = randomGen.nextInt(maxX);
			c.y = randomGen.nextInt(maxY);
		} while (world.hasPixel(c));
		
		//Now randomly jiggle our pixel around until it hits another one
		//Once it hits a second pixel, the position it had before the hit is taken
		Coordinate newC = new Coordinate(c.x, c.y);
		do { 
			c.x = newC.x;
			c.y = newC.y;

			//Calculate the step to take
			int xstep = 0;
			int ystep = 0;
			while(xstep == 0 && ystep == 0) { //0 step not allowed
				xstep = randomGen.nextInt(3) - 1;
				ystep = randomGen.nextInt(3) - 1;
			}

			newC.x += xstep;
			newC.y += ystep;

			newC.wrapAround(maxX, maxY); //Wrap around the edge of the world
		} while (!world.hasPixel(newC));
		
		world.place(c);
	}
}

class Coordinate {
	public int x;
	public int y;

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

class World {
	HashMap<Coordinate, Integer> placedPoints;
	private int xSize;
	private int ySize;

	World(int xSize, int ySize) {
		this.xSize = xSize;
		this.ySize = ySize;

		placedPoints = new HashMap<>();
	}

	public void placeCenterPixel() {
		place(xSize/2, ySize/2);
	}

	public int getXSize() {return xSize;}
	public int getYSize() {return ySize;}

	public boolean hasPixel(Coordinate c) {
		return placedPoints.containsKey(c);
	}

	public int pixelCount() {
		return placedPoints.size();
	}
	
	public void place(int x, int y) {
		Coordinate c = new Coordinate(x, y);
		place(c);
	}

	public void place(Coordinate c) {
		placedPoints.put(c, placedPoints.size()+1);
	}
	
	public void print() {
		for(int y = 0; y != ySize; y++) {
			for(int x = 0; x != xSize; x++) {
				Coordinate here = new Coordinate(x, y); 
				char toPrint = placedPoints.containsKey(here) ? 'X' : '.';
				System.out.print(toPrint);
			}
			System.out.println();
		}
		System.out.print("\n\n\n");
	}
}
