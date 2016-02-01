package com.aditya.graph.library;

/**
 * Class to hold data required to maintain and handle an edge
 */
public class Edge
{
    public int src;

    public int dest;

    // Weight of the edge. Used only when applicable
    public int weight;

    // indicates if the edge is temporary
    public boolean isTemporary;

    /**
     * Constructor for unweighted edges
     *
     * @param src  Source vertex of the edge
     * @param dest Destination vertex of the edge
     */
    public Edge(int src, int dest, boolean isTemporary)
    {
        this.src = src;
        this.dest = dest;
        this.isTemporary = isTemporary;
        weight = 1;
    }

    /**
     * Constructor for weighted edges
     *
     * @param src    Source vertex of the edge
     * @param dest   Destination vertex of the edge
     * @param weight Weight of the edge (if applicable)
     */
    public Edge(int src, int dest, boolean isTemporary, int weight)
    {
        this(src, dest, isTemporary);
        // overwrite the default weight
        this.weight = weight;
    }
}
