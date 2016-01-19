package com.aditya.graph.library;

import java.util.LinkedList;

/**
 * This class represents face of a planar graph. It contains sequence of nodes in the same order as they should appear
 * on the embedding of the face. All the nodes appear in clockwise sequence.
 */
public class Face
{
    public int numberOfNodes;

    public LinkedList<Integer> nodeSeq;

    public Face()
    {
        this.numberOfNodes = 0;
        this.nodeSeq = new LinkedList<>();
    }

    /**
     * This function adds a new node to the clockwise sequence of nodes of the face next to the specified node.
     * @param newNode Index of new node to be added to the face.
     * @param nextTo Index of the node next to which the new node has to be added.
     */
    public void insert(int newNode, int nextTo)
    {
        if (this.nodeSeq.size() == 0)
        {
            this.nodeSeq.add(newNode);
            return;
        }

        int idxToInsertAt = 0;
        for (int node: nodeSeq)
        {
            if (node == nextTo)
            {
                break;
            }
            idxToInsertAt++;
        }

        this.nodeSeq.add(idxToInsertAt, newNode);
    }
}
