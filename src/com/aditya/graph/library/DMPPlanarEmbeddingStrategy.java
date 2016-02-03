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
        Graph subGraphYetToEmbed = graph.cloneGraph();

        // create embedded graph with no edges
        // we'll keep edges in sequence as and when they are embedded
        for (int i = 0; i < graph.nodesCount; i++)
        {
            planarEmbeddedGraph.addNode();
        }

        // in each face the sequence of nodes is maintained in such a way that as we keep walking beside the face
        // we should see each node on our left side in the same sequence. whenever we update a face we make sure
        // that this assumption holds true.
        ArrayList<LinkedList<Integer>> faces = new ArrayList<>();

        // get a random cycle and begin the algorithm
        LinkedList<Integer> pathToEmbed = Helpers.findSomeCycle(graph);

        System.out.println(pathToEmbed);

        // adding first node at the ending so that the cyclic edge gets removed
        pathToEmbed.addLast(pathToEmbed.getFirst());
        removeAllNodesInPathFromGraph(subGraphYetToEmbed, pathToEmbed);
        pathToEmbed.removeLast();

        embedNewPath(faces, pathToEmbed, -1, isNodeEmbedded, planarEmbeddedGraph);

        while (isPlanar && subGraphYetToEmbed.edgesCount > 0)
        {
            ArrayList<ArrayList<Integer>> components =
                    Helpers.findNonEmbeddedComponents(subGraphYetToEmbed, isNodeEmbedded);
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
                    System.out.println("Failed for this component");
                    System.out.println(component);
                    isPlanar = false;
                    break;
                }

                curComponent++;
            }

            if (!isPlanar)
            {
                continue;
            }

            if (componentWithOneFace == -1)
            {
                // we can choose any component in this case
                componentWithOneFace = 0;
            }

            pathToEmbed = Helpers.findPathBetweenAnyTwo(
                    subGraphYetToEmbed,
                    getEmbeddedNodesInComponent(components.get(componentWithOneFace), isNodeEmbedded),
                    components.get(componentWithOneFace));

            System.out.println(pathToEmbed);

            embedNewPath(
                    faces,
                    pathToEmbed,
                    embeddableFaces.get(componentWithOneFace).get(0),
                    isNodeEmbedded,
                    planarEmbeddedGraph);

            removeAllNodesInPathFromGraph(subGraphYetToEmbed, pathToEmbed);
        }

        System.out.println("Faces:\n");
        for (LinkedList<Integer> face : faces)
        {
            System.out.println(face);
        }

        if (isPlanar)
        {
            planarEmbeddedGraph.faces = faces;
            planarEmbeddedGraph.edgesCount = graph.edgesCount;
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

        for (int i = 1; i <= componentCount; i++)
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

    private void removeAllNodesInPathFromGraph(Graph graph, LinkedList<Integer> path)
    {
        if (path.size() == 1)
        {
            return;
        }

        int lastNode = -1;
        for (Integer node : path)
        {
            if (lastNode != -1)
            {
                graph.removeEdge(lastNode, node);
            }
            lastNode = node;
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
        return canEmbedFaces;
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
        for (Integer node : pathToEmbed)
        {
            isEmbedded[node] = true;
        }

        if (faces.size() == 0)
        {
            // create two new faces and add them to faces list. for this case we know that the
            // path we get as input is a cycle. no validations on that.
            LinkedList<Integer> internalFace = new LinkedList<>();
            LinkedList<Integer> externalFace = new LinkedList<>();

            int prevNode = -1;
            for (Integer node : pathToEmbed)
            {
                internalFace.add(node);
                externalFace.addFirst(node);
                if (prevNode != -1)
                {
                    // adding embedded edges to the new graph
                    addFreshEdge(planarEmbeddedGraph, prevNode, node, false);
                    addFreshEdge(planarEmbeddedGraph, node, prevNode, false);
                }
                prevNode = node;
            }
            // adding the edge between start node and end node
            addFreshEdge(planarEmbeddedGraph, pathToEmbed.getFirst(), pathToEmbed.getLast(), false);
            addFreshEdge(planarEmbeddedGraph, pathToEmbed.getLast(), pathToEmbed.getFirst(), false);

            faces.add(internalFace);
            faces.add(externalFace);
        }
        else
        {
            // we get the face in which current path has to be embedded and we split that face into two.
            // add all nodes of the path except start and end in the same order to original face.
            // create a new list with nodes that we delete from original face. add all nodes from the path
            // in reverse order.
            // similar handling is done in case
            int startNode = pathToEmbed.getFirst();
            int endNode = pathToEmbed.getLast();
            int startNodeIdx = -1;
            int endNodeIdx = -1;

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
            LinkedList<Integer> newFace = new LinkedList<>();
            for (Integer node : curFace)
            {
                if (node == startNode)
                {
                    shouldAdd = true;
                }
                else if (shouldAdd)
                {
                    if (node == endNode)
                    {
                        break;
                    }
                    newFace.add(node);
                }
            }

            int prevNode = -1;
            // adding reverse of the actual path
            for (Integer node : pathToEmbed)
            {
                if (prevNode != -1)
                {
                    // start and end nodes are handled separately below
                    if (prevNode != startNode)
                    {
                        addFreshEdge(planarEmbeddedGraph, prevNode, node, false);
                    }
                    if (node != endNode)
                    {
                        addFreshEdge(planarEmbeddedGraph, node, prevNode, false);
                    }
                }
                newFace.addFirst(node);
                prevNode = node;
            }
            // new face built completely
            faces.add(newFace);

            handleStartAndEndEdgeAdditions(planarEmbeddedGraph, curFace, pathToEmbed, startNode, endNode);

            // rebuilding original face if required
            LinkedList<Integer> newCurFace = new LinkedList<>();
            // add all nodes till start (included)
            newCurFace.addAll(curFace.subList(0, startNodeIdx + 1));

            if (pathToEmbed.size() > 1)
            {
                // add other nodes in path we are embedding
                newCurFace.addAll(pathToEmbed.subList(1, pathToEmbed.size() - 1));
            }

            // add all nodes from end node to last of original face
            newCurFace.addAll(curFace.subList(endNodeIdx, curFace.size()));

            curFace.clear();
            curFace.addAll(newCurFace);
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

        // in this function we are sure that the last node will never be start node. we can ignore that corner case
        for (Integer node : curFace)
        {
            secLastNode = lastNode;
            lastNode = curNode;
            curNode = node;

            if (lastNode == startNode)
            {
                addEdgeBetweenNodes(planarEmbeddedGraph, startNode, secLastNode, curNode, pathToEmbed.get(1), false);
            }
            // this particular case might occur if end node is the last node of the face. we will reach this point
            // again when we reach end node normally. we have start node added after this.
            else if (lastNode == endNode && secLastNode != -1)
            {
                addEdgeBetweenNodes(
                        planarEmbeddedGraph,
                        endNode,
                        secLastNode,
                        curNode,
                        pathToEmbed.get(pathLength - 2), false);
            }
        }
        // undo the operation done above
        curFace.removeLast();
    }

    private void addFreshEdge(
            Graph graph,
            int nodeIdx,
            int newNeighbor,
            boolean isTemporary)
    {
        graph.nodes.get(nodeIdx).neighbors.add(new Edge(nodeIdx, newNeighbor, isTemporary));
    }

    private void addEdgeBetweenNodes(
            Graph graph,
            int nodeIdx,
            int prevNode,
            int nextNode,
            int newNeighbor,
            boolean isTemporary)
    {
        // find a point where both nodes are neighbors and insert the node between them
        ArrayList<Edge> neighbors = graph.nodes.get(nodeIdx).neighbors;
        int neighborsCount = neighbors.size();
        if ((prevNode == neighbors.get(0).dest && nextNode == neighbors.get(neighborsCount - 1).dest) ||
                (nextNode == neighbors.get(0).dest && prevNode == neighbors.get(neighborsCount - 1).dest))
        {
            neighbors.add(new Edge(nodeIdx, newNeighbor, isTemporary));
        }
        else
        {
            for (int i = 0; i < neighborsCount - 1; i++)
            {
                if ((prevNode == neighbors.get(i).dest && nextNode == neighbors.get(i + 1).dest) ||
                        (nextNode == neighbors.get(i).dest && prevNode == neighbors.get(i + 1).dest))
                {
                    neighbors.add(i + 1, new Edge(nodeIdx, newNeighbor, isTemporary));
                    break;
                }
            }
        }
    }

    /**
     * This uses a bruteforce algorithm that goes over all the faces, removes any triangular faces, adds edges to
     * faces that aren't triangular until we exhaust all faces. This tampers the list of faces and at the end it just
     * becomes null because we don't need it anymore.
     *
     * @param graph             Graph object returned by isPlanar function.
     * @param triangulatedGraph Fully triangulated version of the input graph. Initialize an empty graph object and
     */
    @Override
    public void triangulate(Graph graph, Graph triangulatedGraph)
    {
        graph.cloneGraph(triangulatedGraph);
        ArrayList<LinkedList<Integer>> faces = triangulatedGraph.faces;
        HashSet<String> edgesInGraph = new HashSet<>();

        // go through all faces and add edges.
        for (int i = 0; i < faces.size(); i++)
        {
            if (faces.get(i).size() == 3)
            {
                faces.remove(i);
                i--;
            }
        }
        for (Edge edge : triangulatedGraph.getEdges())
        {
            edgesInGraph.add(Helpers.getStringForEdge(edge));
        }

        while (faces.size() > 0)
        {
            // choose a random face. try to add edges to it. remove updated faces if necessary.
            triangulateOneFace(triangulatedGraph, faces.get(0), edgesInGraph);
            faces.remove(0);
        }

        // rotate neighbors of each node so that all of them follow the same sequence
        orientNeighborsOfTriangulatedGraph(triangulatedGraph);
    }

    // this function just adds edges that are missing to the graph.
    private void triangulateOneFace(Graph triangulatedGraph, LinkedList<Integer> face, HashSet<String> setOfEdges)
    {
        if (face.size() <= 3)
        {
            return;
        }

        ArrayList<Integer> nodesInFace = new ArrayList<>(face);
        int nodesCountInFace = nodesInFace.size();
        int i;

        // let's find the node that is not connected to all nodes other than neighbors
        for (i = 0; i < nodesInFace.size(); i++)
        {
            boolean isConnectedToSomeNode = false;
            for (int j = 0; j < nodesCountInFace; j++)
            {
                if (j != i && Math.abs(i - j) != 1 && Math.abs(i - j) != (nodesCountInFace - 1))
                {
                    if (setOfEdges.contains(Helpers.getStringForEdge(
                            new Edge(nodesInFace.get(i), nodesInFace.get(j), false))))
                    {
                        isConnectedToSomeNode = true;
                        break;
                    }
                }
            }
            if (!isConnectedToSomeNode)
            {
                // i is the node we have to connect with every other node in the face
                break;
            }
        }

        if (i == nodesInFace.size())
        {
            // hack to throw some exception
            triangulatedGraph.nodesCount = -1;
            return;
        }
        else
        {
            int masterNode = nodesInFace.get(i);
            while (masterNode != nodesInFace.get(0))
            {
                int tempNode = nodesInFace.remove(nodesInFace.size() - 1);
                nodesInFace.add(0, tempNode);
            }
            i = 0;
        }

        // We have to maintain the pair of nodes between which the new node has to be inserted. We keep copying
        // the current node into this variable and use it accordingly.
        // We don't face this issue with "slave" nodes which are getting connected to "master" node.
        int nextNeighbor = -1;
        for (int j = 0; j < nodesInFace.size(); j++)
        {
            if (i != j && Math.abs(i - j) != 1 && Math.abs(i - j) != (nodesCountInFace - 1))
            {
                setOfEdges.add(Helpers.getStringForEdge(new Edge(nodesInFace.get(i), nodesInFace.get(j), true)));
                triangulatedGraph.edgesCount++;

                if (nextNeighbor == -1)
                {
                    nextNeighbor = nodesInFace.get((i + 1) % nodesCountInFace);
                }
                addEdgeBetweenNodes(
                        triangulatedGraph,
                        nodesInFace.get(i),
                        nodesInFace.get((i + nodesCountInFace - 1) % nodesCountInFace),
                        nextNeighbor,
                        nodesInFace.get(j),
                        true);

                addEdgeBetweenNodes(
                        triangulatedGraph,
                        nodesInFace.get(j),
                        nodesInFace.get((j + nodesCountInFace - 1) % nodesCountInFace),
                        nodesInFace.get((j + 1) % nodesCountInFace),
                        nodesInFace.get(i),
                        true);
                nextNeighbor = nodesInFace.get(j);
            }
        }
    }

    // This function takes a triangulated graph, orders neighbors of nodes in such a way that all of them are
    // following the same order when compared to clockwise order.
    //
    // Algorithm: Once ordering of a node is fixed, we can iterate through its children, make sure that next neighbor
    // and parent are appearning in the same order. We can keep adding all nodes to list, use them to find new
    // un-ordered neighbors and order the entire graph
    private void orientNeighborsOfTriangulatedGraph(Graph graph)
    {
        Node curNode;
        int prevNeighbor;
        boolean[] hasOrdered = new boolean[graph.nodesCount];

        LinkedList<Integer> orderedNodes = new LinkedList<>();
        orderedNodes.add(0);
        hasOrdered[0] = true; // we consider order of 0th node as the correct order

        while (!orderedNodes.isEmpty())
        {
            curNode = graph.nodes.get(orderedNodes.removeFirst());
            prevNeighbor = curNode.neighbors.get(curNode.neighbors.size() - 1).dest;

            for (Edge edge : curNode.neighbors)
            {
                if (!hasOrdered[edge.dest])
                {
                    orderNeighbors(graph.nodes.get(prevNeighbor), edge.dest, curNode.idx);
                    orderedNodes.addLast(edge.dest);
                    hasOrdered[edge.dest] = true;
                }
                prevNeighbor = edge.dest;
            }
        }
    }

    private void orderNeighbors(
            Node curNode,
            int firstNode,
            int nextNode)
    {
        System.out.println("Ordering: " + curNode.idx + " with first as: " + firstNode + " and second as: " + nextNode);

        int prevNode = curNode.neighbors.get(curNode.neighbors.size() - 1).dest;
        for (Edge edge : curNode.neighbors)
        {
            if (firstNode == prevNode && nextNode == edge.dest)
            {
                return;
            }
            else if (firstNode == edge.dest && nextNode == prevNode)
            {
                // need to revert
                break;
            }
            prevNode = edge.dest;
        }
        LinkedList<Edge> reversedList = new LinkedList<>();
        for (Edge edge : curNode.neighbors)
        {
            reversedList.addFirst(new Edge(edge.src, edge.dest, edge.isTemporary));
        }
        curNode.neighbors.clear();
        curNode.neighbors.addAll(reversedList);
    }
}
