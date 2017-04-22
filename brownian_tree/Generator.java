package brownian_tree;

public class Generator {
	public static void main(String... args) {
		final int argCount = args.length;
		
		//Defaults overridden by command line arguments
		int xSize = 500;
		int ySize = 500;
		int pixelCount = 30000;
		
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
		
		Generator instance = new Generator(xSize, ySize);
		instance.run(pixelCount);
	}

	private final World world;
	private final String outputName = "out.png";
	
	private Generator(int xsize, int ysize) {
		world = new World(xsize, ysize);
	}
	
	private void run(int totalPixels) {
		world.placeCenterPixel();		
		for(int i = 0; i != totalPixels; i++) {
			placePixel();
			System.out.println("Placed " + (i+1) + "/" + totalPixels);
		}
		world.saveToFile(outputName);
	}

	private void placePixel() {
		final int maxX = world.getXSize();
		final int maxY = world.getYSize();
		
		//Find an empty spot to start on
		Coordinate c = new Coordinate(0, 0);
		do {
			c.randomize(maxX, maxY);
		} while (world.hasPixel(c));
		
		//Now randomly jiggle our pixel around until it hits another one
		//Once it hits a second pixel, the position it had before the hit is taken
		Coordinate newC = new Coordinate(c.x, c.y);
		do { 
			c.x = newC.x;
			c.y = newC.y;
			
			newC.takeRandomStep(maxX, maxY);
		} while (!world.hasPixel(newC));
		
		world.place(c);
	}
}