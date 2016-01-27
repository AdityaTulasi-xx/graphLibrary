package com.aditya.graph.library;

public interface IPlanarEmbeddingMethods
{
    /**
     * This function takes a graph as input, checks if it is planar and builds a planarEmbeddedGraph with correct
     * edge order
     *
     * @param graph               Graph which should be checked for planarity
     * @param planarEmbeddedGraph Graph with edges in correct order for planar drawing. Initialize an empty graph and
     *                            pass it as input
     * @return Boolean value indicating planarity of input graph
     */
    boolean isPlanar(Graph graph, Graph planarEmbeddedGraph);

    /**
     * Takes a planar embedded graph as input and returns a fully triangulated graph. This step also ensures that the
     * order in which neighbors of nodes appear is consistent across different nodes.
     *
     * @param graph             Graph object returned by isPlanar function.
     * @param triangulatedGraph Fully triangulated version of the input graph. Initialize an empty graph object and
     *                          pass it as input
     */
    void triangulate(Graph graph, Graph triangulatedGraph);
}
