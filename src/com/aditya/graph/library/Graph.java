package com.aditya.graph.library;

import java.util.ArrayList;

/**
 * Class to hold details of a Graph
 */
public class Graph
{
    private int _graphPrintMaxLength = 100000;

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
        nodesCount = 0;
        edgesCount = 0;
        nodes = new ArrayList<Node>();
    }

    /**
     * Adds a new node and returns its index.
     *
     * @return Index of the newly created node.
     */
    public int addNode()
    {
        nodes.add(new Node(nodesCount++));
        return nodesCount - 1;
    }

    /**
     * Adds an unweighted edge to the graph
     *
     * @param src  Source vertex of edge
     * @param dest Destination vertex of edge
     * @throws Exception Throws an exception if indices of the vertices are non-existent in the graph
     */
    public void addEdge(int src, int dest) throws Exception
    {
        addEdge(src, dest, 1);
    }

    /**
     * Adds a weighted edge to the graph. This function doesn't take care of duplicate edges.
     *
     * @param src  Source vertex of edge
     * @param dest Destination vertex of edge
     * @throws Exception Throws an exception if indices of the vertices are non-existent in the graph
     */
    public void addEdge(int src, int dest, int weight) throws Exception
    {
        validateSrcDest(src, dest);

        nodes.get(src).neighbors.add(new Edge(src, dest, weight));
        if (!isDirected)
        {
            nodes.get(dest).neighbors.add(new Edge(dest, src, weight));
        }
        ++edgesCount;
    }

    /**
     * Removes an edge from the graph. Doesn't do anything if the edge doesn't exist.
     *
     * @param src  Source of edge to be removed
     * @param dest Destination of edge to be removed
     * @throws Exception Throws exception if there is an issue with the indices of nodes
     */
    public void removeEdge(int src, int dest)
    {
        try
        {
            validateSrcDest(src, dest);
        }
        catch (Exception ex)
        {
            System.out.println("Failed to validate src and dest. Ignoring removal.");
            return;
        }

        for (int i = 0; i < nodes.get(src).neighbors.size(); i++)
        {
            if (nodes.get(src).neighbors.get(i).dest == dest)
            {
                nodes.get(src).neighbors.remove(i);
                break;
            }
        }

        if (!isDirected)
        {
            for (int i = 0; i < nodes.get(dest).neighbors.size(); i++)
            {
                if (nodes.get(dest).neighbors.get(i).dest == src)
                {
                    nodes.get(dest).neighbors.remove(i);
                    break;
                }
            }
        }
        --edgesCount;
    }

    private void validateSrcDest(int src, int dest) throws Exception
    {
        if (nodesCount < src || nodesCount < dest)
        {
            throw new Exception("Invalid src or dest value. " + "Number of nodes in graph: " + nodesCount + ". "
                    + "Src: " + src + " Dest: " + dest + ".");
        }
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

        for (Node node : nodes)
        {
            for (Edge edge : node.neighbors)
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

    /**
     * Clones graph object and creates a new replica.
     *
     * @return Cloned object
     */
    public Graph cloneGraph()
    {
        Graph clone = new Graph(this.isDirected);

        try
        {
            // add all the nodes
            for (int i = 0; i < this.nodesCount; i++)
            {
                clone.addNode();
            }

            // add all edges
            for (int i = 0; i < this.nodesCount; i++)
            {
                for (Edge edge : this.nodes.get(i).neighbors)
                {
                    if (this.isDirected)
                    {
                        clone.addEdge(edge.src, edge.dest, edge.weight);
                    }
                    else if (edge.src < edge.dest)
                    {
                        clone.addEdge(edge.src, edge.dest, edge.weight);
                    }
                }
            }
        }
        catch (Exception ex)
        {
            System.out.println("Some unexpected exception occurred while cloning the graph. \n\n" + ex);
            clone = null;
        }

        return clone;
    }

    /**
     * Prints the graph node by node. For each node, the edges are printed in the order they are present
     * in the neighbors list of node.
     *
     * @return A formatted string that represents the graph's structure.
     */
    @Override
    public String toString()
    {
        StringBuilder printedGraph = new StringBuilder();
        printedGraph.append("Is directed: " + this.isDirected + ".\n\n");
        printedGraph.append("Number of nodes: " + this.nodesCount + ".\n\n");
        printedGraph.append("Number of edges: " + this.edgesCount + ".\n\n");

        for (int i = 0; i < this.nodesCount; i++)
        {
            printedGraph.append("Node #" + i + ":");
            for (Edge edge : this.nodes.get(i).neighbors)
            {
                if (!this.isDirected && edge.src > edge.dest)
                {
                    continue;
                }
                printedGraph.append(" " + edge.dest);
            }
            printedGraph.append(".\n");
        }

        return printedGraph.toString();
    }
}
