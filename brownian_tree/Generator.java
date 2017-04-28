package brownian_tree;

import java.util.concurrent.locks.ReentrantLock;

class Generator {
    private final World world;
    private final String imageFileName;
    private final String pointsFileName;
    private final int threadCount;

    private Generator(int xSize, int ySize, int threadCount, String imageFileName, String pointsFileName) {
        this.threadCount = threadCount;
        this.imageFileName = imageFileName;
        this.pointsFileName = pointsFileName;
        world = new World(xSize, ySize);
    }

    private Generator(String fileToLoad, int threadCount, String imageFileName, String pointsFileName) {
        this.threadCount = threadCount;
        this.imageFileName = imageFileName;
        this.pointsFileName = pointsFileName;
        world = World.createFromFile(fileToLoad);
    }

    public static void main(String... args) {
        final int argCount = args.length;

        //Defaults overridden by command line arguments
        int xSize = 500;
        int ySize = 500;
        int pixelCount = 30000;
        int threadCount = 3;
        String imageFileName = "image.png";
        String pointsFileName = "points.csv";
        String fileToLoad = null;

        try {
            if (argCount == 0) {
                System.err.println("Running with default parameters " + xSize + " " + ySize + " " + pixelCount + " " + threadCount + " " + imageFileName + " " + pointsFileName);
                System.err.println("Command line arguments are xSize, ySize, pixelCount, threadCount, all must be integers.");
                System.err.println("Can be followed by image filename and points csv filename");
                System.err.println("Alternatively the first command line may be the string 'load', a csv to load and a maximum number of pixels to place before stopping. Optionally threadCount, image filename and a new csv filename.");
            }

            if (argCount >= 1 && args[0].equals("load")) {
                if (argCount < 3) {
                    System.err.println("Must give at least three arguments, load, csv filename, number of pixels.");
                    System.exit(1);
                }

                fileToLoad = args[1];
                pixelCount = Integer.parseInt(args[2]);
                if (argCount >= 4) {
                    threadCount = Integer.parseInt(args[3]);
                }

                if (argCount >= 5) {
                    imageFileName = args[4];
                }
                if (argCount >= 6) {
                    pointsFileName = args[5];
                }
            } else {
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
                if (argCount >= 5) {
                    imageFileName = args[4];
                }
                if (argCount >= 6) {
                    pointsFileName = args[5];
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("Unable to parse command line arguments " + e);
            System.exit(1);
        }

        Generator instance;
        if (fileToLoad == null) {
            instance = new Generator(xSize, ySize, threadCount, imageFileName, pointsFileName);
        } else {
            instance = new Generator(fileToLoad, threadCount, imageFileName, pointsFileName);
        }
        instance.run(pixelCount);
    }

    private void run(int totalPixels) {
        try {
            world.setExportFileName(imageFileName, pointsFileName);
            world.setTargetPixelCount(totalPixels);
            world.placeCenterPixel();

            ReentrantLock placeLock = new ReentrantLock();
            Thread[] threadList = new Thread[threadCount];
            for (int i = 0; i != threadCount; i++) {
                threadList[i] = new GeneratorThread("Thread-" + (i + 1), world, totalPixels, placeLock);
                threadList[i].start();
            }

            for (int i = 0; i != threadCount; i++) {
                threadList[i].join();
            }

            //Save with the the file names given
            world.saveToFiles();
        } catch (InterruptedException e) {
            System.err.println("Error, threads interrupted");
        }
    }
}