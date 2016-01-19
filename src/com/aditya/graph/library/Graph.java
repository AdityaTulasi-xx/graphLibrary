package com.aditya.graph.library;

import java.util.ArrayList;

/**
 * Class to hold details of a Graph
 */
public class Graph
{
    public int nodesCount;

    public int edgesCount;

    public boolean isDirected;

    public ArrayList<Node> nodes;

    /**
     * Create a graph object for a directed or undirected graph.
     * NOTE - This graph supports nodes with continuous and integer indices only. It decides the index of node. If
     * you need to use random indices then maintain a mapping of your own.
     *
     * @param isDirected Parameter that indicates if the graph is directed
     */
    public Graph(boolean isDirected)
    {
        this.isDirected = isDirected;
        this.nodesCount = 0;
        this.edgesCount = 0;
        this.nodes = new ArrayList<Node>();
    }

    /**
     * Adds a new node and returns its index.
     * @return Index of the newly created node.
     */
    public int addNode()
    {
        nodes.add(new Node(nodesCount--));
        return nodesCount - 1;
    }

    /**
     * Adds an unweighted edge to the graph
     * @param src Source vertex of edge
     * @param dest Destination vertex of edge
     * @throws Exception Throws an exception if indices of the vertices are non-existent in the graph
     */
    public void addEdge(int src, int dest) throws Exception
    {
        this.addEdge(src, dest, 1);
    }

    /**
     * Adds a weighted edge to the graph
     * @param src Source vertex of edge
     * @param dest Destination vertex of edge
     * @throws Exception Throws an exception if indices of the vertices are non-existent in the graph
     */
    public void addEdge(int src, int dest, int weight) throws Exception
    {
        if (nodesCount < src || nodesCount < dest)
        {
            throw new Exception("Invalid src or dest value. " + "Number of nodes in graph: " + nodesCount + ". "
            + "Src: " + src + " Dest: " + dest + ".");
        }

        nodes.get(src).neighbors.add(new Edge(src, dest, weight));
        if (!isDirected)
        {
            nodes.get(dest).neighbors.add(new Edge(dest, src, weight));
        }
        ++edgesCount;
    }

    /**
     * Creates an array with all edges in the graph so far. If it's an undirected graph, then this creates edges
     * that go from vertex with smaller index to greater.
     *
     * @return An array of edges in the graph.
     */
    public Edge[] getEdges()
    {
        Edge[] edges = new Edge[edgesCount];
        int count = 0;

        for (Node node: this.nodes)
        {
            for (Edge edge: node.neighbors)
            {
                if (isDirected)
                {
                    edges[count++] = edge;
                }
                else if (edge.src < edge.dest)
                {
                    edges[count++] = edge;
                }
            }
        }

        return edges;
    }
}
