package brownian_tree;

import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import datastructure.KDTree;
import datastructure.XYHolder;
import java.util.ArrayList;

public class World {	
	private final int xSize;
	private final int ySize;
	private final boolean useColouredPixels = true;

	private final KDTree placedPointsTree; //Used for nearest neighbour search

	public World(int xSize, int ySize) {
		this.xSize = xSize;
		this.ySize = ySize;

		placedPointsTree = new KDTree();
	}

	public void placeCenterPixel() {
		place(xSize/2, ySize/2);
	}

	public int getXSize() {return xSize;}
	public int getYSize() {return ySize;}

	public boolean hasPixel(Coordinate c) {
		boolean hasPixel = placedPointsTree.contains(c);
		//assert hasPixel == placedPoints.containsKey(c);
		return hasPixel;
	}
		
	public double getDistanceToNearestPixel(Coordinate toCheck) {
		assert this.getPixelCount() > 0;
		
		XYHolder nearestPoint = placedPointsTree.nearestNeighbour(toCheck);
		
		return distanceBetweenCoordinates(toCheck, nearestPoint);
	}
	
	private double distanceBetweenCoordinates(XYHolder c1, XYHolder c2) {
		return Math.sqrt(Math.pow(c1.getX() - c2.getX(), 2) + Math.pow(c1.getY() - c2.getY(), 2));
	}
	
	public int getPixelCount() {
		int size = (int)placedPointsTree.size();
		//assert size == placedPoints.size();
		return size;
	}
	
	public void place(int x, int y) {
		Coordinate c = new Coordinate(x, y);
		c.fillExtraFields(0, "Init");
		place(c);
	}

	public void place(Coordinate c) {
		placedPointsTree.insert(c);
		//if (getPixelCount() % 10_000 == 0) {
		//	long start = System.currentTimeMillis();
		//	placedPointsTree.rebalance();
		//	System.out.println("Tree rebalance finished in " + (System.currentTimeMillis()-start) + "ms");
		//}
	}
	
	public void saveToFile(String filename) {
		BufferedImage image = new BufferedImage(xSize, ySize, BufferedImage.TYPE_INT_RGB);
		
		ArrayList<XYHolder> pointList = new ArrayList<>();
		placedPointsTree.getAllPoints(pointList);
		
		//Background
		for (int x = 0; x < xSize; x++) {
			for (int y = 0; y < ySize; y++) {
		        	image.setRGB(x, y, 0xffffff); //White
			}
		}

		//Draw the tree
		int maxPixelValue = (int)placedPointsTree.size();
		int factorForRGB = (int)Math.pow(2, 24) / maxPixelValue; //RGB as a 24 bit int is spread out evenly across the pixels, which creates a nice wavy pattern from dark to light
		for (XYHolder xy : pointList) {
			Coordinate c = (Coordinate)xy;
			int rgb = 0;
			if (useColouredPixels) {
				rgb = c.getPixelNumber() * factorForRGB;
			}			
			image.setRGB((int)c.getX(), (int)c.getY(), rgb);
		}
		
		File outputFile = new File(filename);
		try {
			ImageIO.write(image, "png", outputFile);
		} catch (IOException e) {
			System.err.println("Error writing image: " + outputFile);
		}
	}
}

