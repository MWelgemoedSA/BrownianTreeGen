package brownian_tree;

import java.util.Random;

public class WorldLogicTester {
	public static void main(String... args) {
		World world = new World(1000, 1000);
		
		//Basic sanity tests
		assert world.getPixelCount() == 0;

		world.placeCenterPixel();
		assert world.getPixelCount() == 1;
	
		world.place(100, 100);
		assert world.getPixelCount() == 2;
		
		//Now test the circle optimizations
		world = new World(1000, 1000);
		world.placeCenterPixel();
		world.place(900, 900);
		
		Coordinate c = new Coordinate(400, 400);
		assert world.hasPixelWithinCircle(c, 200);
		assert !world.hasPixelWithinCircle(c, 50);
		c.x = 500;
		assert world.hasPixelWithinCircle(c, 100); //Edge test
		assert !world.hasPixelWithinCircle(c, 99); //Second edge test
		
		//Circle binary search test
		assert world.getDistanceToNearestPixel(c) == 100;
		
		c.x = 400;
		assert (int)world.getDistanceToNearestPixel(c) == 141;
		
		world.place(500, 400);
		c.x = 500;
		c.y = 450;
		assert world.getDistanceToNearestPixel(c) == 50;
		
		//Edge tests
		world = new World(1000, 1000);
		world.place(500, 500);
		c = new Coordinate(499, 499);
		assert (int)world.getDistanceToNearestPixel(c) == 1;
		c = new Coordinate(501, 501);
		assert (int)world.getDistanceToNearestPixel(c) == 1;
		c = new Coordinate(503, 503);
		assert (int)world.getDistanceToNearestPixel(c) == 4;
		c = new Coordinate(500, 500);
		assert world.getDistanceToNearestPixel(c) == 0;
		
		//Nondeterminate test, but better than nothing
		for(int i = 0; i != 1000_000; i++) {
			c = new Coordinate();
			c.setRandom(new Random());
			double distance = 100;
			while (distance > 2 && distance < 2000) {
				distance  = world.getDistanceToNearestPixel(c);
				c.teleportToCircleEdge(distance-2);
				distance = world.getDistanceToNearestPixel(c);
				//System.out.println(i + ": New Distance " + distance + " " + c);
				assert distance >= 1;
			}
		}
	}
}