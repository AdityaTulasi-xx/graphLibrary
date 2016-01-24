package com.aditya.graph.library;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

public class DMPPlanarEmbeddingStrategy implements IPlanarEmbeddingMethods
{
    /**
     * In this algorithm we use primitive strategy to check planarity.
     * We first take a random cycle from the graph, create 2 faces of the partially embedded graph.
     * Keep finding components in the remaining graph. If one of the components can fit in only one face,
     * then we choose that component. Otherwise we choose any random component. Within that component we choose
     * any path that runs between two already embedded nodes. We have to note that such a path exists because
     * we are dealing with bi-connected components. We will handle generic graphs later.
     *
     * @param graph               Graph which should be checked for planarity
     * @param planarEmbeddedGraph Graph with edges in correct order for planar drawing. Initialize an empty graph and
     *                            pass it as input
     * @return Boolean value indicating planarity of the graph
     */
    @Override
    public boolean isPlanar(Graph graph, Graph planarEmbeddedGraph)
    {
        boolean isPlanar = true;
        boolean[] isNodeEmbedded = new boolean[graph.nodesCount];
        Graph yetToEmbed = graph.cloneGraph();

        // in each face the sequence of nodes is maintained in such a way that as we keep walking beside the face
        // we should see each node on our left side in the same sequence. whenever we update a face we make sure
        // that this assumption holds true.
        ArrayList<LinkedList<Integer>> faces = new ArrayList<>();

        // get a random cycle and begin the algorithm
        LinkedList<Integer> currentPath = Helpers.findSomeCycle(graph);
        currentPath.removeLast(); // because the first vertex is repeated in the helper function

        removeAllNodesInPath(yetToEmbed, currentPath);
        embedNewPath(faces, currentPath, -1, isNodeEmbedded, planarEmbeddedGraph);

        while (isPlanar && yetToEmbed.edgesCount > 0)
        {
            ArrayList<ArrayList<Integer>> components = getNonEmptyComponents(yetToEmbed);
            ArrayList<ArrayList<Integer>> embeddableFaces = new ArrayList<>();
            int componentWithOneFace = -1;
            int curComponent = 0;
            for (ArrayList<Integer> component : components)
            {
                embeddableFaces.add(allowedFacesForEmbedding(faces, component, isNodeEmbedded));
                int noOfFaces = embeddableFaces.get(embeddableFaces.size() - 1).size();
                if (noOfFaces == 1)
                {
                    componentWithOneFace = curComponent;
                }
                else if (noOfFaces == 0)
                {
                    isPlanar = false;
                    break;
                }

                curComponent++;
            }

            if (componentWithOneFace == -1)
            {
                // we can choose any component in this case
                componentWithOneFace = 0;
            }

            currentPath = Helpers.findPathBetweenAnyTwo(
                    yetToEmbed,
                    getEmbeddedNodesInComponent(components.get(componentWithOneFace), isNodeEmbedded));

            embedNewPath(
                    faces,
                    currentPath,
                    embeddableFaces.get(componentWithOneFace).get(0),
                    isNodeEmbedded, planarEmbeddedGraph);

            removeAllNodesInPath(yetToEmbed, currentPath);
        }

        return isPlanar;
    }

    private ArrayList<ArrayList<Integer>> getNonEmptyComponents(Graph graph)
    {
        int[] components = Helpers.findComponents(graph);
        int componentCount = -1;
        ArrayList<ArrayList<Integer>> nonEmptyComponents = new ArrayList<>();

        for (int i = 0; i < components.length; i++)
        {
            if (components[i] > componentCount)
            {
                componentCount = components[i];
            }
        }

        for (int i = 1; i < componentCount; i++)
        {
            ArrayList<Integer> component = new ArrayList<>();
            for (int j = 0; j < components.length; j++)
            {
                if (components[j] == i)
                {
                    component.add(j);
                }
            }
            if (component.size() > 1)
            {
                nonEmptyComponents.add(component);
            }
        }

        return nonEmptyComponents;
    }

    private void removeAllNodesInPath(Graph graph, LinkedList<Integer> path)
    {
        if (path.size() == 1)
        {
            return;
        }

        int lastNode = -1;
        for (Integer node : path)
        {
            if (lastNode == -1)
            {
                lastNode = node;
            }
            else
            {
                graph.removeEdge(lastNode, node);
            }
        }
    }

    private ArrayList<Integer> getEmbeddedNodesInComponent(ArrayList<Integer> component, boolean[] isEmbedded)
    {
        ArrayList<Integer> embeddedNodes = new ArrayList<>();
        for (Integer node : component)
        {
            if (isEmbedded[node])
            {
                embeddedNodes.add(node);
            }
        }

        return embeddedNodes;
    }

    private ArrayList<Integer> allowedFacesForEmbedding(
            ArrayList<LinkedList<Integer>> faces,
            ArrayList<Integer> component,
            boolean[] isNodeEmbedded)
    {
        // build a list of nodes in the component that are already in some face
        // check which face has all the nodes
        ArrayList<Integer> nodesAlreadyEmbedded = getEmbeddedNodesInComponent(component, isNodeEmbedded);
        ArrayList<Integer> canEmbedFaces = new ArrayList<>();
        for (int i = 0; i < faces.size(); i++)
        {
            if (canEmbedInFace(nodesAlreadyEmbedded, faces.get(i)))
            {
                canEmbedFaces.add(i);
            }
        }
        return null;
    }

    private boolean canEmbedInFace(ArrayList<Integer> vertices, LinkedList<Integer> face)
    {
        // using hashset hoping that complexity will be better than comparing two lists
        HashSet<Integer> nodesInFace = new HashSet<>();
        nodesInFace.addAll(face);
        return nodesInFace.containsAll(vertices);
    }

    private void embedNewPath(
            ArrayList<LinkedList<Integer>> faces,
            LinkedList<Integer> pathToEmbed,
            int faceIdx,
            boolean[] isEmbedded,
            Graph planarEmbeddedGraph)
    {
        // create embedded graph with no edges
        // we'll keep edges in sequence as and when they are embedded
        for (int i = 0; i < isEmbedded.length; i++)
        {
            planarEmbeddedGraph.addNode();
        }

        for (Integer node : pathToEmbed)
        {
            isEmbedded[node] = true;
        }

        if (faces.size() == 0)
        {
            // create two new faces and add them to faces list. for this case we know that the
            // path we get as input is a cycle. no validations on that.
            LinkedList<Integer> internalFace = new LinkedList<>(pathToEmbed);
            LinkedList<Integer> externalFace = new LinkedList<>();

            int prevNode = -1;
            for (Integer node : pathToEmbed)
            {
                externalFace.addFirst(node);
                if (prevNode == -1)
                {
                    prevNode = node;
                }
                else
                {
                    // adding embedded edges to the new graph
                    addFreshEdge(planarEmbeddedGraph, prevNode, node);
                    addFreshEdge(planarEmbeddedGraph, node, prevNode);
                }
            }

            faces.add(internalFace);
            faces.add(externalFace);
        }
        else
        {
            // we get the face in which current path has to be embedded and we split that face into two
            // add all nodes of the path except start and end in the same order
            // create a new list with nodes that we delete from original face. add all nodes from the path
            // in reverse order.
            // similar handling is done in case
            int startNode = pathToEmbed.getFirst();
            int endNode = pathToEmbed.getLast();
            int startNodeIdx = -1, endNodeIdx = -1;
            LinkedList<Integer> curFace = faces.get(faceIdx);

            int curIdx = 0;
            for (Integer node : curFace)
            {
                if (node == startNode)
                {
                    startNodeIdx = curIdx;
                }
                else if (node == endNode)
                {
                    endNodeIdx = curIdx;
                }
                curIdx++;
            }

            if (startNodeIdx > endNodeIdx)
            {
                // let's just rotate the face until we get start before end
                int curNode = -1;

                do
                {
                    curNode = curFace.removeLast();
                    curFace.addFirst(curNode);
                    endNodeIdx++;
                }
                while (curNode != startNode);

                startNodeIdx = 0;
            }

            // let's build the new face first
            boolean shouldAdd = false;
            int prevNode = curFace.getLast();
            LinkedList<Integer> newFace = new LinkedList<>();
            for (Integer node : curFace)
            {
                if (node == startNode)
                {
                    shouldAdd = true;
                }
                else if (shouldAdd)
                {
                    // whenever we add a new node, we can add edges freshly. no need to check for neighbors and sequence
                    if (node != endNode)
                    {
                        // end node has to be handled separately
                        addFreshEdge(planarEmbeddedGraph, node, prevNode);
                    }
                    if (prevNode != startNode)
                    {
                        // start node has to be handled separately
                        addFreshEdge(planarEmbeddedGraph, prevNode, node);
                    }
                    if (node == endNode)
                    {
                        break;
                    }
                    else
                    {
                        newFace.add(node);
                    }
                }
                prevNode = node;
            }
            // adding reverse of the actual path
            for (Integer node : pathToEmbed)
            {
                newFace.addFirst(node);
            }
            // new face built completely
            faces.add(newFace);

            handleStartAndEndEdgeAdditions(planarEmbeddedGraph, curFace, pathToEmbed, startNode, endNode);

            // rebuilding original face if required
            if (pathToEmbed.size() > 1)
            {
                LinkedList<Integer> newCurFace = new LinkedList<>();
                // add all nodes till start
                newCurFace.addAll(curFace.subList(0, startNodeIdx));
                // add other nodes in path we are embedding
                newCurFace.addAll(pathToEmbed.subList(1, pathToEmbed.size() - 1));
                // add all nodes from end node to last of original face
                curFace.addAll(curFace.subList(endNodeIdx, curFace.size()));
                curFace.clear();
                curFace.addAll(newCurFace);
            }
        }
    }

    private void handleStartAndEndEdgeAdditions(
            Graph planarEmbeddedGraph,
            LinkedList<Integer> curFace,
            LinkedList<Integer> pathToEmbed,
            int startNode,
            int endNode)
    {
        // handling addition of edges for start and end nodes of the path
        int secLastNode = -1;
        int lastNode = -1;
        int curNode = -1;
        int pathLength = pathToEmbed.size();
        curNode = curFace.getLast();

        // let's add first node to ending so that we handle the corner case of a node being last in the face's list
        curFace.add(curFace.getFirst());

        for (Integer node : curFace)
        {
            secLastNode = lastNode;
            lastNode = curNode;
            curNode = node;

            if (lastNode == startNode)
            {
                addEdgeBetweenNodes(planarEmbeddedGraph, startNode, secLastNode, curNode, pathToEmbed.get(1));
            }
            else if (lastNode == endNode)
            {
                addEdgeBetweenNodes(
                        planarEmbeddedGraph,
                        endNode,
                        secLastNode,
                        curNode,
                        pathToEmbed.get(pathLength - 2));
            }
        }
        // undo the operation done above
        curFace.removeLast();
    }

    private void addFreshEdge(
            Graph graph,
            int nodeIdx,
            int newNeighbor)
    {
        graph.nodes.get(nodeIdx).neighbors.add(new Edge(nodeIdx, newNeighbor));
    }

    private void addEdgeBetweenNodes(
            Graph graph,
            int nodeIdx,
            int prevNode,
            int nextNode,
            int newNeighbor)
    {
        // find a point where both nodes are neighbors and insert the node between them
        ArrayList<Edge> neighbors = graph.nodes.get(nodeIdx).neighbors;
        int neighborsCount = neighbors.size();
        if (prevNode == neighbors.get(0).dest && nextNode == neighbors.get(neighborsCount).dest)
        {
            neighbors.add(new Edge(nodeIdx, newNeighbor));
        }
        else if (nextNode == neighbors.get(0).dest && prevNode == neighbors.get(neighborsCount).dest)
        {
            neighbors.add(new Edge(nodeIdx, newNeighbor));
        }
        else
        {
            for (int i = 0; i < neighborsCount - 1; i++)
            {
                if (prevNode == neighbors.get(i).dest && nextNode == neighbors.get(i + 1).dest)
                {
                    neighbors.add(i + 1, new Edge(nodeIdx, newNeighbor));
                }
                else if (nextNode == neighbors.get(i).dest && prevNode == neighbors.get(i + 1).dest)
                {
                    neighbors.add(i + 1, new Edge(nodeIdx, newNeighbor));
                }
            }
        }
    }

    @Override
    public void triangulate(Graph graph, Graph triangulatedGraph)
    {

    }
}
