package brownian_tree;

public class Generator {
	public static void main(String... args) {
		final int argCount = args.length;
		
		//Defaults overridden by command line arguments
		int xSize = 500;
		int ySize = 500;
		int pixelCount = 30000;
		int threadCount = 3;
		
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
			if (argCount >= 4) {
				threadCount = Integer.parseInt(args[3]);
			}
		} catch (NumberFormatException e) {
        		System.err.println("Command line arguments are xSize, ySize, pixelCount, threadcount, all must be integers");
			System.exit(1);
		}
		
		Generator instance = new Generator(xSize, ySize, threadCount);
		instance.run(pixelCount);
	}

	private final World world;
	private final String outputName = "out.png";
	private final int threadCount;
	
	private Generator(int xsize, int ysize, int threadCount) {
		this.threadCount = threadCount;
		world = new World(xsize, ysize);
	}
	
	private void run(int totalPixels) {
		try {
			world.placeCenterPixel();
			
			Thread[] threadList = new Thread[threadCount];
			for(int i = 0; i != threadCount; i++) {			
				threadList[i] = new GeneratorThread("Thread-" + (i+1), world, totalPixels);
				threadList[i].start();
			}

			for(int i = 0; i != threadCount; i++) {			
				threadList[i].join();
			}
			
			world.saveToFile(outputName);
		} catch (InterruptedException e) {
			System.err.println("Error, threads interrupted");
		}
	}
}