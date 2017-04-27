package datastructure;

import java.util.AbstractList;
import java.util.Comparator;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.ArrayList;

public class KDTree {
    private KDNode root = null;
    private int count;
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public KDTree() {
        count = 0;
    }

    public KDTree(AbstractList<XYHolder> xyList) {
        root = new KDNode(xyList, 0);
        count = xyList.size();
    }

    public void rebalance() {
        readWriteLock.writeLock().lock();
        ArrayList<XYHolder> allPoints = new ArrayList<>();
        getAllPoints(allPoints);
        root = new KDNode(allPoints, 0);
        readWriteLock.writeLock().unlock();
    }

    public void insert(XYHolder xy) {
        readWriteLock.writeLock().lock();
        if (root == null) {
            root = new KDNode(xy, 0);
        } else {
            KDNode toInsertAt = findNodeFor(root, xy);
            if (toInsertAt.pointEqualsPointAtNode(xy)) {
                readWriteLock.writeLock().unlock();
                return; //Already in the tree
            }
            toInsertAt.addChild(xy);
        }
        count++;
        readWriteLock.writeLock().unlock();
    }

    private KDNode findNodeFor(KDNode root, XYHolder xyToFind) {
        KDNode subNode = root.getSubNode(xyToFind); //To the left or right or the node itself
        if (subNode == null) { //Closest node to the point found
            return root;
        }

        return findNodeFor(subNode, xyToFind);
    }

    public int size() {
        return count;
    }

    public void getAllPoints(AbstractList<XYHolder> list) {
        readWriteLock.readLock().lock();
        getAllPoints(root, list);
        readWriteLock.readLock().unlock();
    }

    private void getAllPoints(KDNode node, AbstractList<XYHolder> list) {
        if (node == null) {
            return;
        }

        list.add(node.getXYObject());
        getAllPoints(node.getLeft(), list);
        getAllPoints(node.getRight(), list);
    }

    public boolean contains(XYHolder xy) {
        if (root == null) {
            return false;
        }

        readWriteLock.readLock().lock();
        KDNode closestPointNode = findNodeFor(root, xy);
        readWriteLock.readLock().unlock();
        return closestPointNode.pointEqualsPointAtNode(xy);
    }

    public XYHolder nearestNeighbour(XYHolder xy) {
        //System.out.println("Starting nearest neighbour search " + xy);
        if (root == null) {
            return null;
        }

        readWriteLock.readLock().lock();
        KDNode closestNode = nearestNeighbour(root, xy, new FindResult()).node;
        readWriteLock.readLock().unlock();

        if (closestNode == null) return null;

        //System.out.println("Found " + closestNode);
        return closestNode.getXYObject();
    }

    private FindResult nearestNeighbour(KDNode nodeToSearch, XYHolder xyToFind, FindResult bestSoFar) {
        if (nodeToSearch == null) return bestSoFar;

        //System.out.println("Searching " + nodeToSearch);
        //System.out.println("Best so far " + bestSoFar.node + " with " + bestSoFar.distance);

        //Check point at the node
        double distToNode = nodeToSearch.distanceTo(xyToFind);
        if (distToNode < bestSoFar.distance) {
            bestSoFar.distance = distToNode;
            bestSoFar.node = nodeToSearch;
        }

        //Now check it's children, if they exist
        if (nodeToSearch.pointIsLeftOf(xyToFind)) { //Search left first
            bestSoFar = nearestNeighbour(nodeToSearch.getLeft(), xyToFind, bestSoFar);
            if (nodeToSearch.getSplitValueOf(xyToFind) + bestSoFar.distance >= nodeToSearch.getSplitValue()) { //Can't exclude it based on distance
                bestSoFar = nearestNeighbour(nodeToSearch.getRight(), xyToFind, bestSoFar);
            }
        } else { //Search right first
            bestSoFar = nearestNeighbour(nodeToSearch.getRight(), xyToFind, bestSoFar);
            if (nodeToSearch.getSplitValueOf(xyToFind) - bestSoFar.distance <= nodeToSearch.getSplitValue()) { //Can't exclude it based on distance
                bestSoFar = nearestNeighbour(nodeToSearch.getLeft(), xyToFind, bestSoFar);
            }
        }

        return bestSoFar;
    }

    private class FindResult {
        double distance = Double.MAX_VALUE;
        KDNode node = null;
    }
}

class KDNode {
    private int depth; //0 is X, 1 is Y
    private XYHolder pointAtNode;
    private KDNode left;
    private KDNode right;

    KDNode(XYHolder pointAtNode, int depth) {
        this.pointAtNode = pointAtNode;
        this.depth = depth;
    }

    KDNode(AbstractList<XYHolder> xylist, int depth) {
        this.depth = depth;

        xylist.sort(Comparator.comparingLong(this::getSplitValueOf));

        int median = xylist.size() / 2;
        //System.out.println("Median " + median + " " + xylist);
        this.pointAtNode = xylist.get(median);

        AbstractList<XYHolder> leftPoints = new ArrayList<>(xylist.subList(0, median));
        if (!leftPoints.isEmpty()) {
            this.left = new KDNode(leftPoints, depth + 1);
        }

        AbstractList<XYHolder> rightPoints = new ArrayList<>(xylist.subList(median + 1, xylist.size()));
        if (!rightPoints.isEmpty()) {
            this.right = new KDNode(rightPoints, depth + 1);
        }
    }

    @Override
    public String toString() {
        return "KDNode{" +
                "depth=" + depth +
                ", pointAtNode=" + pointAtNode +
                '}';
    }

    XYHolder getXYObject() {
        return pointAtNode;
    }

    KDNode getLeft() {
        return left;
    }

    KDNode getRight() {
        return right;
    }

    private long getX() {
        return pointAtNode.getX();
    }

    private long getY() {
        return pointAtNode.getY();
    }

    //This node's split value
    long getSplitValue() {
        return getSplitValueOf(pointAtNode);
    }

    //Takes the dimension this current node splits points based on out of the XYHolder
    long getSplitValueOf(XYHolder xy) {
        if (depth % 2 == 0) {
            return xy.getX();
        }
        return xy.getY();
    }

    boolean hasChildren() {
        return left != null && right != null;
    }

    void addChild(XYHolder xy) {
        if (pointIsLeftOf(xy)) {
            left = new KDNode(xy, depth + 1);
        } else {
            right = new KDNode(xy, depth + 1);
        }
    }

    boolean pointIsLeftOf(XYHolder xy) {
        return getSplitValueOf(xy) < getSplitValue();
    }

    boolean pointEqualsPointAtNode(XYHolder toCheck) {
        return toCheck.getX() == pointAtNode.getX() && toCheck.getY() == pointAtNode.getY();
    }

    KDNode getSubNode(XYHolder xy) {
        if (pointIsLeftOf(xy)) {
            return left;
        }
        return right;
    }

    double distanceTo(XYHolder xyToFind) {
        return Math.sqrt(Math.pow(getX() - xyToFind.getX(), 2) +
                Math.pow(getY() - xyToFind.getY(), 2)
        );
    }
}
