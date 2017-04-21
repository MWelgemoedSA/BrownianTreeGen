package brownian_tree;

import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class World {
	HashMap<Coordinate, Integer> placedPoints;
	private final int xSize;
	private final int ySize;
	private final boolean useColouredPixels = true;

	World(int xSize, int ySize) {
		this.xSize = xSize;
		this.ySize = ySize;

		placedPoints = new HashMap<>();
	}

	public void placeCenterPixel() {
		place(xSize/2, ySize/2);
	}

	public int getXSize() {return xSize;}
	public int getYSize() {return ySize;}

	public boolean hasPixel(Coordinate c) {
		return placedPoints.containsKey(c);
	}

	public int pixelCount() {
		return placedPoints.size();
	}
	
	public void place(int x, int y) {
		Coordinate c = new Coordinate(x, y);
		place(c);
	}

	public void place(Coordinate c) {
		placedPoints.put(c, placedPoints.size()+1);
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
		BufferedImage image = new BufferedImage(xSize, ySize, BufferedImage.TYPE_INT_RGB);
		
		int factorForRGB = (int)Math.pow(2, 24) / placedPoints.size(); //RGB as a 24 bit int is spread out evenly across the pixels, which creates a nice wavy pattern from dark to light
		Coordinate coordinate = new Coordinate();
		for (int x = 0; x < xSize; x++) {
			coordinate.x = x;
			for (int y = 0; y < ySize; y++) {
				coordinate.y = y;
				int rgb = 0xffffff;
				if (placedPoints.containsKey(coordinate)) {
					if (useColouredPixels) {
						rgb = placedPoints.get(coordinate) * factorForRGB;
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
			System.out.println("Error writing image: " + outputFile);
		}
	}
}

