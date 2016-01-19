package com.aditya.graph.library;

import com.aditya.general.utilities.Point2D;

import java.util.ArrayList;

/**
 * Created by adity on 1/19/2016.
 */
public interface IGraphAlgorithms
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
     * Takes a planar embedded graph as input and returns a fully triangulated graph.
     *
     * @param graph             Graph object returned by isPlanar function.
     * @param triangulatedGraph Fully triangulated version of the input graph. Initialize an empty graph object and
     *                          pass it as input
     */
    void triangulate(Graph graph, Graph triangulatedGraph);

    /**
     * This function takes a triangulated planar graph and returns list of coordinates for each vertex. These
     * can be used to draw the graph on a 2D plane
     *
     * @param triangulatedPlanarGraph Fully triangulated graph built by the triangulate function.
     * @return List of 2D points that represent positions of the points on a 2D plane
     */
    ArrayList<Point2D> DrawOnPlane(Graph triangulatedPlanarGraph);
}
