package brownian_tree;

public class MotionTest {
	public static void main(String... args) {
		final int max = 500;
		
		World world = new World(max, max);
		int pixelsToTest = 100_000_000;
		for(long i = 0; i != pixelsToTest; i++) {
			if (i % (pixelsToTest/100) == 0) {
				System.out.println((i * 100 / pixelsToTest) + "%");
			}
			Coordinate c = new Coordinate(max/2, max/2);
			for(int j = 0; j != max/2; j++) {
				c.takeRandomStep(max, max);
			}
			int old = world.getPixelValue(c);
			world.place(c, old+1);
		}
		world.saveToFile("motionTest.png");
	}
}
