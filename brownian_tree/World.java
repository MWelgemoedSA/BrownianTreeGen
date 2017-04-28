package brownian_tree;

import datastructure.KDTree;
import datastructure.XYHolder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

class World {
    private final int xSize;
    private final int ySize;
    private final KDTree placedPointsTree; //Contains points, used for efficient nearest neighbour search

    private String imageFileName = null;
    private String pointFileName = null;
    private int targetPixelCount = 0; //If given, used for the colouring

    private World(int xSize, int ySize, KDTree treeToUse) {
        this.xSize = xSize;
        this.ySize = ySize;
        this.placedPointsTree = treeToUse;
    }

    World(int xSize, int ySize) {
        this.xSize = xSize;
        this.ySize = ySize;
        this.placedPointsTree = new KDTree();
    }

    static World createFromFile(String fileToLoad) {
        assert fileToLoad != null;

        int xSize = 0;
        int ySize = 0;
        KDTree placedPointsTree = null;

        try {
            try (
                    BufferedReader reader = new BufferedReader(
                            new FileReader(fileToLoad))
            ) {
                String firstLine = reader.readLine();
                if (firstLine == null) {
                    System.err.println("File " + fileToLoad + " is blank");
                    System.exit(1);
                }

                String[] stringParts = firstLine.split(";");
                xSize = Integer.parseInt(stringParts[0]);
                ySize = Integer.parseInt(stringParts[1]);

                String pointLine;
                ArrayList<XYHolder> pointList = new ArrayList<>();
                while ((pointLine = reader.readLine()) != null)
                    pointList.add(new Coordinate(pointLine));

                placedPointsTree = new KDTree(pointList);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Unable to find file " + fileToLoad);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Unable to read file " + fileToLoad);
            System.exit(1);
        }

        return new World(xSize, ySize, placedPointsTree);
    }

    void placeCenterPixel() {
        Coordinate c = new Coordinate(xSize / 2, ySize / 2);
        c.fillExtraFields(0, "Init");
        place(c);
    }

    int getXSize() {
        return xSize;
    }

    int getYSize() {
        return ySize;
    }

    boolean hasPixel(Coordinate c) {
        return placedPointsTree.contains(c);
    }

    double getDistanceToNearestPixel(Coordinate toCheck) {
        assert this.getPixelCount() > 0;

        XYHolder nearestPoint = placedPointsTree.nearestNeighbour(toCheck);

        return distanceBetweenCoordinates(toCheck, nearestPoint);
    }

    private double distanceBetweenCoordinates(XYHolder c1, XYHolder c2) {
        return Math.sqrt(Math.pow(c1.getX() - c2.getX(), 2) + Math.pow(c1.getY() - c2.getY(), 2));
    }

    int getPixelCount() {
        return placedPointsTree.size();
    }

    void place(Coordinate c) {
        placedPointsTree.insert(c);
        //if (getPixelCount() % 10_000 == 0) {
        //	long start = System.currentTimeMillis();
        //	placedPointsTree.rebalance();
        //	System.out.println("Tree rebalance finished in " + (System.currentTimeMillis()-start) + "ms");
        //}
    }

    void saveToFiles() {
        this.saveToFiles(this.imageFileName, this.pointFileName);
    }

    void saveToIntermediateFiles() {
        String count = String.format("%010d", getPixelCount());

        String prefix = "pixels-" + count + "-";
        saveToFiles(prefix + imageFileName, prefix + pointFileName);
    }

    private void saveToFiles(String imageFileName, String pointFileName) {
        BufferedImage image = new BufferedImage(xSize, ySize, BufferedImage.TYPE_INT_RGB);

        ArrayList<XYHolder> pointList = new ArrayList<>();
        placedPointsTree.getAllPoints(pointList);

        //Background
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                image.setRGB(x, y, 0xffffff); //White
            }
        }

        try {
            FileWriter pointWriter = new FileWriter(pointFileName);
            pointWriter.write(xSize + ";" + ySize + "\n");
            for (XYHolder xy : pointList) {
                pointWriter.write(xy.toString());
                pointWriter.write("\n");
            }
            pointWriter.close();
        } catch (IOException e) {
            System.err.println("Error outputting points: " + pointFileName);
        }

        //Draw the tree
        int maxPixelValue = targetPixelCount;
        if (maxPixelValue == 0) {
            maxPixelValue = placedPointsTree.size();
        }

        int factorForRGB = (int) Math.pow(2, 24) / maxPixelValue; //RGB as a 24 bit int is spread out evenly across the pixels, which creates a nice wavy pattern from dark to light
        for (XYHolder xy : pointList) {
            Coordinate c = (Coordinate) xy;
            int rgb = c.getPixelNumber() * factorForRGB;

            image.setRGB((int) c.getX(), (int) c.getY(), rgb);
        }

        File imageOutputFile = new File(imageFileName);
        try {
            ImageIO.write(image, "png", imageOutputFile);
        } catch (IOException e) {
            System.err.println("Error outputting image: " + imageOutputFile);
        }
    }

    void setExportFileName(String imageFileName, String pointFileName) {
        assert imageFileName != null;
        assert pointFileName != null;
        this.imageFileName = imageFileName;
        this.pointFileName = pointFileName;
    }

    void setTargetPixelCount(int targetPixelCount) {
        this.targetPixelCount = targetPixelCount;
    }
}
