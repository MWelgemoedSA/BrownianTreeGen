package brownian_tree;

import datastructure.XYHolder;
import java.util.Random;

public class Coordinate implements XYHolder{
	public int x;
	public int y;
	private Random randomGen;
	
	//Mainly for debugging, but also interesting to know
	private final long creationTime = System.currentTimeMillis();
	private long placedTime; //Value of currentTimeMillis()
	private long teleportCount = 0;
	private long randomStepCount = 0;
	private String findingThread;

	//Used to colour the png pixel
	private int pixelNumber = -1;

	public Coordinate() {
		x = 0;
		y = 0;
	}

	public Coordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public void fillExtraFields(int pixelNumber, String findingThread) {
		this.placedTime = System.currentTimeMillis();
		this.pixelNumber = pixelNumber;
		this.findingThread = findingThread;
	}
	
	public int getPixelNumber() {
		return pixelNumber;
	}
	
	public void setRandom(Random randomGen) {
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
		return "(" + x + ", " + y + ")";
	}
	
	public void takeRandomStep(int maxX, int maxY) {
		//Calculate the step to take
		int xstep = 0;
		int ystep = 0;
		while(xstep == 0 && ystep == 0) { //0 step not allowed
			xstep = randomGen.nextInt(3) - 1;
			ystep = randomGen.nextInt(3) - 1;
		}
		
		this.x += xstep;
		this.y += ystep;
		
		this.wrapAround(maxX, maxY); //Wrap around the edge of the world

	}
	
	public void randomize(int maxX, int maxY) {
		this.x = randomGen.nextInt(maxX);
		this.y = randomGen.nextInt(maxY);
	}
	
	public void teleportToCircleEdge(double radius) {
		double angle = randomGen.nextDouble() * Math.PI * 2; //Random 360 degree angle in radians
		//System.out.println("Teleporting to circle edge from " + this + " radius " + radius + " angle " + angle);
		double xStep = Math.cos(angle) * radius;
		double yStep = Math.sin(angle) * radius;
		
		this.x += Math.round(xStep);
		this.y += Math.round(yStep);
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
}
