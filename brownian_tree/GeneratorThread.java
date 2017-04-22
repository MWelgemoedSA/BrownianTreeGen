package brownian_tree;

import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

class GeneratorThread extends Thread {
	final private int maxPixelCount;
	final private World world;
	final private Random randomGen;
	final private String threadName; //Mainly for debugging
	final private ReentrantLock placeLock; //This lock is used to ensure a single thread at a time places a pixel
	
	public GeneratorThread(String threadName, World world, int maxPixelCount, ReentrantLock placeLock) {
		this.threadName = threadName;
		this.world = world;
		this.maxPixelCount = maxPixelCount;
		this.placeLock = placeLock;

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
		
		placeLock.lock();
		if (world.getPixelCount() < maxPixelCount) { //Another thread may have a placed a pixel since we started looking, ensure we still can place on
			world.place(c);
			System.out.println("Thread " + threadName + " placed " + world.getPixelCount());
		}
		placeLock.unlock();
	}
}
