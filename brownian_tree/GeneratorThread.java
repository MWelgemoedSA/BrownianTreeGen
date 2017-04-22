package brownian_tree;

import java.util.Random;

class GeneratorThread extends Thread {
	final private int maxPixelCount;
	final private World world;
	final private Random randomGen;
	final private String threadName; //Mainly for debugging
	
	public GeneratorThread(String threadName, World world, int maxPixelCount) {
		this.threadName = threadName;
		this.maxPixelCount = maxPixelCount;
		this.world = world;
		this.randomGen = new Random();
	}
	
	@Override
	public void run() {
		System.out.println("Launching thread " + threadName);
		while (world.getPixelCount() < maxPixelCount) {
			placePixel();
		}		
	}

	private void placePixel() {
		final int maxX = world.getXSize();
		final int maxY = world.getYSize();
		
		//Find an empty spot to start on
		Coordinate c = new Coordinate(0, 0);
		c.setRandom(randomGen);
		do {
			c.randomize(maxX, maxY);
		} while (world.hasPixel(c));
		
		//Now randomly jiggle our pixel around until it hits another one
		//Once it hits a second pixel, the position it had before the hit is taken
		Coordinate newC = new Coordinate(c.x, c.y);
		newC.setRandom(randomGen);
		do { 
			c.x = newC.x;
			c.y = newC.y;
			
			newC.takeRandomStep(maxX, maxY);
		} while (!world.hasPixel(newC));
		
		System.out.println("Thread " + threadName + " placed " + world.getPixelCount());
		world.place(c);
	}
}
