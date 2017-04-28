package brownian_tree;

import datastructure.XYHolder;

import java.util.Random;

public class Coordinate implements XYHolder{
    int x; //x and y are explicitly package private for speed
    int y;
    private Random randomGen;

    //Mainly for debugging, but also interesting to know
    private long creationTime = System.currentTimeMillis();
    private long placedTime; //Value of currentTimeMillis()
    private int teleportCount = 0;
    private int randomStepCount = 0;
    private int timesReset = 0;

    private String findingThread;

    //Used to colour the png pixel
    private int pixelNumber = -1;

    Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    Coordinate(String pointLine) {
        String[] parts = pointLine.split(";");
        assert parts.length == 9;

        this.x = Integer.parseInt(parts[0]);
        this.y = Integer.parseInt(parts[1]);
        this.pixelNumber = Integer.parseInt(parts[2]);
        this.creationTime = Long.parseLong(parts[3]);
        this.placedTime = Long.parseLong(parts[4]);
        this.timesReset = Integer.parseInt(parts[5]);
        this.teleportCount = Integer.parseInt(parts[6]);
        this.randomStepCount = Integer.parseInt(parts[7]);
        this.findingThread = parts[8];
    }

    void resetCounts() {
        this.teleportCount = 0;
        this.randomStepCount = 0;
        this.timesReset++;
    }

    void fillExtraFields(int pixelNumber, String findingThread) {
        this.placedTime = System.currentTimeMillis();
        this.pixelNumber = pixelNumber;
        this.findingThread = findingThread;
    }

    int getPixelNumber() {
        return pixelNumber;
    }

    void setRandom(Random randomGen) {
        this.randomGen = randomGen;
    }

    @Override
    public long getX() {
        return x;
    }

    @Override
    public long getY() {
        return y;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Coordinate)) {
            return false;
        }

        Coordinate other = (Coordinate) obj;
        return x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
        return x * 1000 + y;
    }

    @Override
    public String toString() {
        return 
                x + ";" +
                y + ";" +
                pixelNumber + ";" +
                creationTime + ";" +
                placedTime + ";" +
                timesReset + ";" +
                teleportCount + ";" +
                randomStepCount + ";" +
                findingThread;
    }

    void takeRandomStep(int maxX, int maxY) {
        this.randomStepCount++;

        //Calculate the step to take
        int xStep = 0;
        int yStep = 0;
        while(xStep == 0 && yStep == 0) { //0 step not allowed
            xStep = randomGen.nextInt(3) - 1;
            yStep = randomGen.nextInt(3) - 1;
        }

        this.x += xStep;
        this.y += yStep;

        this.wrapAround(maxX, maxY); //Wrap around the edge of the world
    }

    void randomize(int maxX, int maxY) {
        this.x = randomGen.nextInt(maxX);
        this.y = randomGen.nextInt(maxY);
    }

    void teleportToCircleEdge(double radius) {
        double angle = randomGen.nextDouble() * Math.PI * 2; //Random 360 degree angle in radians
        //System.out.println("Teleporting to circle edge from " + this + " radius " + radius + " angle " + angle);
        double xStep = Math.cos(angle) * radius;
        double yStep = Math.sin(angle) * radius;

        this.x += Math.round(xStep);
        this.y += Math.round(yStep);

        this.teleportCount++;
        //System.out.println("New position " + xStep + " " + yStep + " " + this);
    }

    private void wrapAround(int maxX, int maxY) {
        if (x < 0) {
            x += maxX;
        } else if(x >= maxX) {
            x -= maxX;
        }

        if (y < 0) {
            y += maxY;
        } else if (y >= maxY) {
            y -= maxY;
        }
    }

    void setCounts(Coordinate counterSource) {
        this.timesReset = counterSource.timesReset;
        this.teleportCount = counterSource.teleportCount;
        this.randomStepCount = counterSource.randomStepCount;
    }
}
