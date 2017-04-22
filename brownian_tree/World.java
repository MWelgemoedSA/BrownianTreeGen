package brownian_tree;

import java.util.concurrent.ConcurrentHashMap;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class World {
	ConcurrentHashMap<Coordinate, Integer> placedPoints;
	private final int xSize;
	private final int ySize;
	private final boolean useColouredPixels = true;

	public World(int xSize, int ySize) {
		this.xSize = xSize;
		this.ySize = ySize;

		placedPoints = new ConcurrentHashMap<>();
	}

	public void placeCenterPixel() {
		place(xSize/2, ySize/2);
	}

	public int getXSize() {return xSize;}
	public int getYSize() {return ySize;}

	public boolean hasPixel(Coordinate c) {
		return placedPoints.containsKey(c);
	}
	
	//TODO
	//Our world is torriodal, pixels can wrap around the edges as they move
	//This function does not consider that
	public boolean hasPixelWithinCircle(Coordinate toCheck, double radius) {
		System.out.println("Checking " + toCheck + " " + radius);

		for (Coordinate placed: placedPoints.keySet()) {
			double distance = distanceBetweenCoordinates(toCheck, placed);
			if (distance - radius < 1) {
				System.out.println("Collision " + placed + " " + distance + " <= " + radius);
				return true;
			} else {
				System.out.println("No collision " + placed + " " + distance + " > " + radius);
			}
		}
		
		System.out.println("No collisions found");
		return false;
	}
	
	public double getDistanceToNearestPixel(Coordinate toCheck) {
		assert this.getPixelCount() > 0;
		
		double distance = Double.MAX_VALUE;
		for (Coordinate placed: placedPoints.keySet()) {
			double distanceToThisPixel = distanceBetweenCoordinates(toCheck, placed);
			//System.out.println("Distance from " + toCheck + " " + placed + " " + distanceToThisPixel);
			distance = Math.min(distance, distanceToThisPixel);
		}
		return distance;
	}
	
	private double distanceBetweenCoordinates(Coordinate c1, Coordinate c2) {
		return Math.sqrt(Math.pow(c1.x - c2.x, 2) + Math.pow(c1.y - c2.y, 2));
	}
	
	//Returns 0 if the pixel isn't in the HashMap
	public int getPixelValue(Coordinate coordinate) {
		if (placedPoints.containsKey(coordinate)) {
			return placedPoints.get(coordinate);
		}
		return 0;
	}
	
	public int getPixelCount() {
		return placedPoints.size();
	}
	
	public void place(int x, int y) {
		Coordinate c = new Coordinate(x, y);
		place(c);
	}

	public synchronized void place(Coordinate c) { //Synchronized, otherwise the counter is subject to a race condition
		place(c, getPixelCount()+1);
	}
	
	public void place(Coordinate c, int val) {
		placedPoints.put(c, val);
	}
	
	public void print() {
		for(int y = 0; y != ySize; y++) {
			for(int x = 0; x != xSize; x++) {
				Coordinate here = new Coordinate(x, y); 
				char toPrint = placedPoints.containsKey(here) ? 'X' : '.';
				System.out.print(toPrint);
			}
			System.out.println();
		}
		System.out.print("\n\n\n");
	}
	
	public void saveToFile(String filename) {
		int biggestValue = 0;
		for (Integer value : placedPoints.values()) {
			if (value > biggestValue) {
				biggestValue = value;
			}
		}
		saveToFile(filename, biggestValue);
	}
	
	public void saveToFile(String filename, int maxPixelValue) {
		BufferedImage image = new BufferedImage(xSize, ySize, BufferedImage.TYPE_INT_RGB);
		
		int factorForRGB = (int)Math.pow(2, 24) / maxPixelValue; //RGB as a 24 bit int is spread out evenly across the pixels, which creates a nice wavy pattern from dark to light
		Coordinate coordinate = new Coordinate();
		for (int x = 0; x < xSize; x++) {
			coordinate.x = x;
			for (int y = 0; y < ySize; y++) {
				coordinate.y = y;
				int rgb = 0xffffff;
				
				int pixelValue = getPixelValue(coordinate);
				if (pixelValue > 0) {
					if (useColouredPixels) {
						rgb = pixelValue * factorForRGB;
					} else {
						rgb = 0;	
					}					
				}

		        	image.setRGB(x, y, rgb);
			}
		}
		File outputFile = new File(filename);
		try {
			ImageIO.write(image, "png", outputFile);
		} catch (IOException e) {
			System.err.println("Error writing image: " + outputFile);
		}
	}
}

