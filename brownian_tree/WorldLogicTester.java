package brownian_tree;

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
		Coordinate c = new Coordinate(400, 400);
		assert world.hasPixelWithinCircle(c, 200);
		assert !world.hasPixelWithinCircle(c, 50);
		c.x = 500;
		assert world.hasPixelWithinCircle(c, 100); //Edge test
		assert !world.hasPixelWithinCircle(c, 99); //Second edge test
	}
}