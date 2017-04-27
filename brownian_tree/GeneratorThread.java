package brownian_tree;

import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

class GeneratorThread extends Thread {
    final private int maxPixelCount;
    final private World world;
    final private Random randomGen;
    final private String threadName; //Mainly for debugging
    final private ReentrantLock placeLock; //This lock is used to ensure a single thread at a time places a pixel

    //These belong to the world, but query them once for speed
    //World is final, and these can't change once world is initialized
    private final int maxX;
    private final int maxY;

    public GeneratorThread(String threadName, World world, int maxPixelCount, ReentrantLock placeLock) {
        this.threadName = threadName;
        this.world = world;
        this.maxPixelCount = maxPixelCount;
        this.placeLock = placeLock;

        this.randomGen = new Random();

        maxX = world.getXSize();
        maxY = world.getYSize();
    }

    @Override
    public void run() {
        System.out.println("Launching thread " + threadName);
        while (world.getPixelCount() < maxPixelCount) {
            placePixel();
        }
    }

    private void getInitialPosition(Coordinate c) {
        do {
            c.randomize(maxX, maxY);
        } while (world.hasPixel(c));

        placeLock.lock();
        if (world.hasPixel(c)) { //Was placed by another thread while we were looping
            getInitialPosition(c); //Retry
        }
        placeLock.unlock();
    }

    private void placePixel() {
        //Find an empty spot to start on
        Coordinate c = new Coordinate(0, 0);
        c.setRandom(randomGen);

        getInitialPosition(c);
        teleportWalkPixel(c);

        placeLock.lock();
        if (world.getPixelCount() < maxPixelCount && !world.hasPixel(c)) { //Another thread may have a placed a pixel since we started looking, ensure we still can place one
            c.fillExtraFields(world.getPixelCount(), threadName);
            world.place(c);

            int pixelCount = world.getPixelCount();

            if (pixelCount % 1000 == 0) {
                System.out.println("Thread " + threadName + " placed " + world.getPixelCount());
            }

            if (pixelCount % 10_000 == 0) {
                world.saveToIntermediateFiles();
            }
        }
        placeLock.unlock();
    }

    //The motion of the pixel averages to a perfect circle
    //Taking the a random point on the edge of a circle is identical to random walking until it hits the edge of the circle
    //The circle must not contain any points otherwise this assumption is not correct
    //This repeats teleportation until it hits something
    //If it is right next to another pixel it takes a random step instead of teleporting
    private void teleportWalkPixel(Coordinate c) {
        Coordinate newC = new Coordinate(c.x, c.y);
        newC.setRandom(randomGen);
        do {
            c.x = newC.x;
            c.y = newC.y;

            int distance = (int) world.getDistanceToNearestPixel(c);
            //System.out.println(newC + " dist " + distance);
            if (distance > (maxX + maxY) * 3) { //Too far, kill the pixel and restart
                getInitialPosition(newC);
                newC.resetCounts();
            } else if (distance > 3) {
                newC.teleportToCircleEdge(distance - 2);
            } else {
                newC.takeRandomStep(maxX, maxY);
            }
        } while (!world.hasPixel(newC));
        c.setCounts(newC);
    }
}
