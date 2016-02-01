package com.aditya.graph.library;

import com.aditya.general.utilities.Point2D;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

public class ShiftPlanarDrawingStrategy implements IPlanarDrawingMethods
{
    /**
     * In this algorithm we first compute an ordering of vertices, namely canonical ordering, which makes sure that
     * every sub-graph containing first k vertices is 2-connected and is internally triangulated. Next, we keep
     * embedding vertices in the same order
     *
     * @param triangulatedPlanarGraph Fully triangulated graph built by the triangulate function.
     * @return List of 2D points that represent positions of nodes in embedded graph.
     */
    @Override
    public ArrayList<Point2D> DrawOnPlane(Graph triangulatedPlanarGraph)
    {
        int[] canonicalOrder = getCanonicalOrder(triangulatedPlanarGraph);

        printOrdering(canonicalOrder);

        ArrayList<Point2D> nodePositions = new ArrayList<>();
        ArrayList<HashSet<Integer>> dependentVertices = new ArrayList<>();
        LinkedList<Integer> currentCycle = new LinkedList<>();
        boolean[] isEmbedded = new boolean[canonicalOrder.length];

        // initialize each position
        for (int i = 0; i < triangulatedPlanarGraph.nodesCount; i++)
        {
            nodePositions.add(new Point2D());
            dependentVertices.add(new HashSet<>());
            dependentVertices.get(i).add(i); // default set
        }

        // fix positions of first 3 nodes
        setPosition(nodePositions.get(canonicalOrder[0]), 0, 0);
        setPosition(nodePositions.get(canonicalOrder[1]), 2, 0);
        setPosition(nodePositions.get(canonicalOrder[2]), 1, 1);

        // first 3 are embedded already
        isEmbedded[canonicalOrder[0]] = true;
        isEmbedded[canonicalOrder[1]] = true;
        isEmbedded[canonicalOrder[2]] = true;

        // initializing the outer most cycle
        currentCycle.add(canonicalOrder[0]);
        currentCycle.add(canonicalOrder[2]);
        currentCycle.add(canonicalOrder[1]);

        // keep embedding new nodes and adjusting positions of existing nodes
        for (int i = 3; i < canonicalOrder.length; i++)
        {
            // build list of already embedded neighbors
            LinkedList<Integer> neighborsOnCycle = new LinkedList<>();
            HashSet<Integer> neighborsOfNode = new HashSet<>();
            for (Edge edge : triangulatedPlanarGraph.nodes.get(canonicalOrder[i]).neighbors)
            {
                neighborsOfNode.add(edge.dest);
            }

            for (Integer node : currentCycle)
            {
                if (neighborsOfNode.contains(node) && isEmbedded[node])
                {
                    neighborsOnCycle.add(node);
                }
            }

            // finding nodes to move left and right
            LinkedList<Integer> toMoveLeft = new LinkedList<>();
            LinkedList<Integer> toMoveRight = new LinkedList<>();
            HashSet<Integer> dependentOnCurrent = new HashSet<>();
            boolean hasSeenLeft = false, hasSeenRight = false;

            for (Integer curNode : currentCycle)
            {
                // need to move the right most vertex too
                if (curNode == neighborsOnCycle.getLast())
                {
                    hasSeenRight = true;
                }
                if (!hasSeenLeft)
                {
                    toMoveLeft.add(curNode);
                }
                if (hasSeenRight)
                {
                    toMoveRight.add(curNode);
                }
                if (hasSeenLeft && !hasSeenRight)
                {
                    dependentOnCurrent.addAll(dependentVertices.get(curNode));
                }
                // need to move the left most vertex too
                if (curNode == neighborsOnCycle.getFirst())
                {
                    hasSeenLeft = true;
                }
            }

            // move nodes appropriately
            moveNodes(toMoveLeft, -1, dependentVertices, nodePositions);
            moveNodes(toMoveRight, 1, dependentVertices, nodePositions);

            // initialize dependent nodes of node we are embedding
            dependentVertices.set(canonicalOrder[i], dependentOnCurrent);

            // create position for the new node
            nodePositions.set(canonicalOrder[i],
                    findNewNodePosition(
                            nodePositions.get(neighborsOnCycle.getFirst()),
                            nodePositions.get(neighborsOnCycle.getLast())
                    ));
            isEmbedded[canonicalOrder[i]] = true;

            currentCycle.clear();
            currentCycle.addAll(toMoveLeft);

            // remove first and last ones on the cycle. they are already in lists we want to move
            currentCycle.add(canonicalOrder[i]);

            currentCycle.addAll(toMoveRight);
        }

        return nodePositions;
    }

    private void moveNodes(
            LinkedList<Integer> parents,
            int amount,
            ArrayList<HashSet<Integer>> dependentNodes,
            ArrayList<Point2D> positions)
    {
        HashSet<Integer> allNodesToMove = new HashSet<>();
        for (Integer node : parents)
        {
            allNodesToMove.addAll(dependentNodes.get(node));
        }

        for (Integer node : allNodesToMove)
        {
            positions.get(node).x = positions.get(node).x + amount;
        }
    }

    private Point2D findNewNodePosition(Point2D left, Point2D right)
    {
        Point2D newPoint = new Point2D();
        newPoint.x = (left.x + right.x + right.y - left.y) / 2;
        newPoint.y = (left.y + right.y + right.x - left.x) / 2;
        return newPoint;
    }

    private void setPosition(Point2D point, int x, int y)
    {
        point.x = x;
        point.y = y;
    }

    private void printOrdering(int[] canonicalOrder)
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < canonicalOrder.length; i++)
        {
            builder.append(canonicalOrder[i] + " ");
        }
        System.out.println(builder.toString());
    }

    private int[] getCanonicalOrder(Graph triangulatedGraph)
    {
        int[] ordering = new int[triangulatedGraph.nodesCount];
        int firstNodeDegree = triangulatedGraph.nodes.get(0).neighbors.size();

        // choose any two nodes as base nodes
        ordering[0] = 0;
        ordering[triangulatedGraph.nodes.get(0).neighbors.get(0).dest] = 1;

        boolean[] isMarked = new boolean[triangulatedGraph.nodesCount];
        boolean[] isOuterNode = new boolean[triangulatedGraph.nodesCount];
        int[] chordCount = new int[triangulatedGraph.nodesCount];

        for (int i = 0; i < triangulatedGraph.nodesCount; i++)
        {
            isMarked[i] = isOuterNode[i] = false;
            chordCount[i] = 0;
        }
        isOuterNode[0] = true;
        isOuterNode[triangulatedGraph.nodes.get(0).neighbors.get(0).dest] = true;
        isOuterNode[triangulatedGraph.nodes.get(0).neighbors.get(firstNodeDegree - 1).dest] = true;

        int j;
        for (int i = triangulatedGraph.nodesCount - 1; i > 1; i--)
        {
            // choose an unmarked outer node that doesn't have any chords
            for (j = 1; j < triangulatedGraph.nodesCount; j++)
            {
                if (!isMarked[j] && isOuterNode[j] && chordCount[j] == 0 && 1 != ordering[j])
                {
                    break;
                }
            }
            // mark this node and assign it order number of i
            isMarked[j] = true;
            isOuterNode[j] = false;
            ordering[j] = i;

            // let us update outer nodes and chords
            updateChordCounts(j, isMarked, isOuterNode, chordCount, triangulatedGraph);
        }

        int[] orderedNodes = new int[triangulatedGraph.nodesCount];
        for (int i = 0; i < ordering.length; i++)
        {
            orderedNodes[ordering[i]] = i;
        }
        return orderedNodes;
    }

    private void updateChordCounts(int curNode, boolean[] isMarked, boolean[] isOuter, int[] chordCount, Graph graph)
    {
        // go through all outer neighbors. if count is just 2, then reduce count of chords for both of them.
        // otherwise iterate neighbors of each node and increase counts appropriately
        ArrayList<Integer> outerSequence = new ArrayList<>();
        for (Edge edge : graph.nodes.get(curNode).neighbors)
        {
            if (!isMarked[edge.dest])
            {
                outerSequence.add(edge.dest);
                isOuter[edge.dest] = true;
            }
        }

        if (outerSequence.size() == 2)
        {
            chordCount[outerSequence.get(0)]--;
            chordCount[outerSequence.get(1)]--;
        }
        else
        {
            // go through all nodes that were not outer in previous iteration
            // increase chord count of each
            for (int i = 1; i < outerSequence.size() - 1; i++)
            {
                for (Edge edge : graph.nodes.get(outerSequence.get(i)).neighbors)
                {
                    if (isOuter[edge.dest] &&
                            edge.dest != outerSequence.get(i - 1) &&
                            edge.dest != outerSequence.get(i + 1))
                    {
                        chordCount[outerSequence.get(i)]++;
                        chordCount[edge.dest]++;
                    }
                }
            }
        }
    }
}
