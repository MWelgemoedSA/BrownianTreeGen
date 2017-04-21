package brownian_tree;

public class MotionTest {
	public static void main(String... args) {
		final int maxX = 100;
		final int maxY = 100;
		
		World world = new World(maxX, maxY);
		int pixelsToTest = 10_000_000;
		for(int i = 0; i != pixelsToTest; i++) {
			if (i % (pixelsToTest/100) == 0) {
				System.out.println((i * 100 / pixelsToTest) + "%");
			}
			Coordinate c = new Coordinate(maxX/2, maxY/2);
			for(int j = 0; j != 50; j++) {
				c.takeRandomStep(maxX, maxY);
			}
			int old = world.getPixelValue(c);
			world.place(c, old+1);
		}
		world.saveToFile("motionTest.png");
	}
}
