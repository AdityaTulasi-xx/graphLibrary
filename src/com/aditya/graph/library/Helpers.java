package com.aditya.graph.library;

import java.util.ArrayList;
import java.util.LinkedList;

public final class Helpers
{
    /**
     * Finds components of the graph and returns an array of numbers which indicates the component index of
     * each node of the graph.
     *
     * @param graph Graph in which components have to be found
     * @return Array indicating components of the graph
     */
    public static int[] findComponents(Graph graph)
    {
        int[] components = new int[graph.nodesCount];
        int curComponent = 0;

        for (int i = 0; i < graph.nodesCount; i++)
        {
            components[i] = -1;
        }

        for (int i = 0; i < graph.nodesCount; i++)
        {
            if (components[i] == -1)
            {
                curComponent++;
                findComponentsInternal(graph, i, components, curComponent);
            }
        }

        return components;
    }

    private static void findComponentsInternal(Graph graph, int curIdx, int[] components, int curComponent)
    {
        if (components[curIdx] == -1)
        {
            return;
        }

        components[curIdx] = curComponent;
        for (Edge edge : graph.nodes.get(curIdx).neighbors)
        {
            if (components[edge.dest] == -1)
            {
                findComponentsInternal(graph, edge.dest, components, curComponent);
            }
        }
    }

    /**
     * Takes any graph, checks if there is a cycle anywhere and returns a list of nodes if there is one.
     * Returns null otherwise.
     *
     * @param graph Graph in which a cycle has to be found
     * @return List of nodes in the cycle OR null (for empty graph too).
     */
    public static LinkedList<Integer> findSomeCycle(Graph graph)
    {
        LinkedList<Integer> cycle = null;
        boolean[] hasVisited = new boolean[graph.nodesCount];

        for (int i = 0; i < graph.nodesCount; i++)
        {
            hasVisited[i] = false;
        }

        for (int i = 0; i < graph.nodesCount; i++)
        {
            // iterate through entire graph and make sure every component is tested
            cycle = new LinkedList<Integer>();
            if (!hasVisited[i])
            {
                if (findSomeCycle(graph, i, -1, cycle, hasVisited))
                {
                    break;
                }
            }
        }

        return cycle;
    }

    // helper recursive function that finds cycle in a single component
    private static boolean findSomeCycle(
            Graph graph,
            int curNode,
            int parentNode,
            LinkedList<Integer> pathSoFar,
            boolean[] hasVisited)
    {
        pathSoFar.add(curNode);

        if (hasVisited[curNode])
        {
            // remove any path before cycle began
            while (!pathSoFar.isEmpty())
            {
                if (pathSoFar.get(0) != curNode)
                {
                    pathSoFar.remove(0);
                }
                else
                {
                    break;
                }
            }
            return true;
        }

        hasVisited[curNode] = true;

        for (Edge edge : graph.nodes.get(curNode).neighbors)
        {
            // preventing calling parent node of dfs
            if (parentNode == -1 || parentNode != edge.dest)
            {
                if (findSomeCycle(graph, edge.dest, curNode, pathSoFar, hasVisited))
                {
                    return true;
                }
            }
        }

        pathSoFar.removeLast();
        return false;
    }

    /**
     * This function finds path between any two of the acceptable nodes, if such a path exists.
     *
     * @param graph           Graph in which the path has to be found.
     * @param acceptableNodes List of nodes between which a path is acceptable.
     * @return List of nodes that constitute the path.
     */
    public static LinkedList<Integer> findPathBetweenAnyTwo(Graph graph, ArrayList<Integer> acceptableNodes)
    {
        LinkedList<Integer> somePath = null;
        boolean[] hasVisited = new boolean[graph.nodesCount];
        boolean[] isAcceptableNode = new boolean[graph.nodesCount];

        for (int i = 0; i < graph.nodesCount; i++)
        {
            isAcceptableNode[i] = false;
            hasVisited[i] = false;
        }

        for (Integer acceptableNode : acceptableNodes)
        {
            isAcceptableNode[acceptableNode] = true;
        }

        for (Integer acceptableNode : acceptableNodes)
        {
            somePath = new LinkedList<>();
            if (findPathBetweenAnyTwo(graph, acceptableNode, somePath, isAcceptableNode, hasVisited))
            {
                break;
            }
        }

        return somePath;
    }

    // helper recursive function that finds a path
    private static boolean findPathBetweenAnyTwo(
            Graph graph,
            int curNode,
            LinkedList<Integer> pathSoFar,
            boolean[] isAcceptableNode,
            boolean[] hasVisited)
    {
        // have to check for a non-empty path because we are starting with first node and it has to be an
        // acceptable node
        if (isAcceptableNode[curNode] && !pathSoFar.isEmpty())
        {
            pathSoFar.add(curNode);
            return true;
        }

        hasVisited[curNode] = true;
        pathSoFar.add(curNode);

        for (Edge edge : graph.nodes.get(curNode).neighbors)
        {
            if (!hasVisited[edge.dest])
            {
                if (findPathBetweenAnyTwo(graph, edge.dest, pathSoFar, isAcceptableNode, hasVisited))
                {
                    return true;
                }
            }
        }

        return false;
    }
}
