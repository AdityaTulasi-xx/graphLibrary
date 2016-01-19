package com.aditya.graph.library;

import com.aditya.general.utilities.Point2D;

import java.util.ArrayList;

public interface IPlanarDrawingMethods
{
    /**
     * This function takes a triangulated planar graph and returns list of coordinates for each vertex. These
     * can be used to draw the graph on a 2D plane
     *
     * @param triangulatedPlanarGraph Fully triangulated graph built by the triangulate function.
     * @return List of 2D points that represent positions of the points on a 2D plane
     */
    ArrayList<Point2D> DrawOnPlane(Graph triangulatedPlanarGraph);
}
