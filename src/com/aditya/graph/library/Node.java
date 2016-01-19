package com.aditya.graph.library;

import java.util.ArrayList;

/**
 * Class to hold data required to maintain and handle a Node of a graph.
 */
public class Node
{
    public int idx;

    public ArrayList<Edge> neighbors;

    /**
     * Constructor for creating a node object
     * @param idx Index of the node
     */
    public Node(int idx)
    {
        this.idx = idx;
        this.neighbors = new ArrayList<>();
    }

    /**
     * Add an unweighted edge to the node
     * @param dest Destination of the edge
     */
    public void AddEdge(int dest)
    {
        this.neighbors.add(new Edge(this.idx, dest));
    }

    /**
     * Add a weighted edge to the node
     * @param dest Destination of the edge
     * @param weight Weight of the edge
     */
    public void AddEdge(int dest, int weight)
    {
        this.neighbors.add((new Edge(this.idx, dest, weight)));
    }
}
