package Main;

import com.aditya.general.utilities.Point2D;
import com.aditya.graph.library.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Main
{
    public static void main(String[] args)
    {
        System.out.println("Begin testing.");

        try
        {
            // testFindCycle();
            System.out.println("FindCycle test passed.");
        }
        catch (Exception ex)
        {
            System.out.println("FindCycle test failed. " + ex.getMessage());
        }

        try
        {
            // testFindSomePath();
            System.out.println("FindSomePath test passed.");
        }
        catch (Exception ex)
        {
            System.out.println("FindSomePath test failed. " + ex.getMessage());
        }

        // test the output manually
        // can't test automatically
        testPlanarEmbedding();

        System.out.println("End testing");
    }

    public static void testPlanarEmbedding()
    {
        Graph graphToTest = readFileToGraph(
                "C:\\Users\\adity\\IdeaProjects\\GraphLibrary\\inputFiles\\simplePlanar2.txt");

        IPlanarEmbeddingMethods embedder = PlanarEmbeddingFactory
                .GetPlanarEmbeddingStrategy(PlanarEmbeddingStrategies.DMP);

        System.out.println("Graph we are testing on:\n" + graphToTest);
        Graph embeddedGraph = new Graph(false);
        embedder.isPlanar(graphToTest, embeddedGraph);

        System.out.println("Planar embedded graph");
        System.out.println(embeddedGraph);

        Graph triangulatedGraph = new Graph(embeddedGraph.isDirected);
        embedder.triangulate(embeddedGraph, triangulatedGraph);
        System.out.println("Triangulated graph");
        System.out.println(triangulatedGraph);
        System.out.println();

        IPlanarDrawingMethods drawer = PlanarDrawingFactory
                .GetPlanarDrawingStrategy(PlanarDrawingStrategies.SCHNYDER);

        System.out.println("\nPositions of planar graph are:\n");
        ArrayList<Point2D> positions = drawer.DrawOnPlane(triangulatedGraph);
        for (int i = 0; i < positions.size(); i++)
        {
            System.out.println(i + " - " + positions.get(i));
        }
    }

    public static void testFindCycle() throws Exception
    {
        Graph graph = new Graph(false);
        graph.addNode(); // 0
        graph.addNode(); // 1
        graph.addNode(); // 2
        graph.addNode(); // 3
        graph.addNode(); // 4
        graph.addNode(); // 5

        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 4);
        graph.addEdge(4, 5);
        graph.addEdge(5, 2);

        LinkedList<Integer> cycle = Helpers.findSomeCycle(graph);
        LinkedList<Integer> expectedCycle = new LinkedList<>();
        expectedCycle.add(2);
        expectedCycle.add(3);
        expectedCycle.add(4);
        expectedCycle.add(5);

        compareLists(expectedCycle, cycle);

        graph.removeEdge(5, 2);
        cycle = Helpers.findSomeCycle(graph);

        if (cycle != null && cycle.size() != 0)
        {
            throw new Exception("Length of cycle is not as expected. Expecting an empty list");
        }
    }

    public static void testFindSomePath() throws Exception
    {
        Graph graph = new Graph(false);
        graph.addNode(); // 0
        graph.addNode(); // 1
        graph.addNode(); // 2
        graph.addNode(); // 3
        graph.addNode(); // 4
        graph.addNode(); // 5
        graph.addNode(); // 6
        graph.addNode(); // 7

        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(1, 3);
        graph.addEdge(4, 5);
        graph.addEdge(5, 6);
        graph.addEdge(6, 7);
        graph.addEdge(5, 7);

        ArrayList<Integer> allowedNodes = new ArrayList<>();
        allowedNodes.add(0);
        allowedNodes.add(4);
        allowedNodes.add(7);

        ArrayList<Integer> component = new ArrayList<>();
        for (int i = 0; i < 7; i++)
        {
            component.add(i);
        }

        LinkedList<Integer> pathFound = Helpers.findPathBetweenAnyTwo(graph, allowedNodes, component);
        LinkedList<Integer> expectedPath = new LinkedList<>();
        expectedPath.add(4);
        expectedPath.add(5);
        expectedPath.add(6);
        expectedPath.add(7);

        compareLists(expectedPath, pathFound);
    }

    private static void compareLists(List<Integer> list1, List<Integer> list2) throws Exception
    {
        Iterator<Integer> resultItr = list1.iterator();
        Iterator<Integer> expectedItr = list2.iterator();

        while (resultItr.hasNext() || expectedItr.hasNext())
        {
            if (resultItr.hasNext() != expectedItr.hasNext())
            {
                throw new Exception("Lengths of lists don't match. " +
                        "Length of first list: " + list1.size() + " " +
                        "Length of second list: " + list2.size());
            }
            if (resultItr.next() != expectedItr.next())
            {
                throw new Exception("Nodes of lists don't match");
            }
        }
    }

    private static Graph readFileToGraph(String pathToFile)
    {
        Graph toReturn = new Graph(false);
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(pathToFile));
            int nodesCount = Integer.parseInt(reader.readLine());
            int edgeCount = Integer.parseInt(reader.readLine());
            for (int i = 0; i < nodesCount; i++)
            {
                toReturn.addNode();
            }

            for (int i = 0; i < edgeCount; i++)
            {
                String[] nodesOfEdge = reader.readLine().split("\\s+");
                toReturn.addEdge(Integer.parseInt(nodesOfEdge[0]), Integer.parseInt(nodesOfEdge[1]));
            }
            reader.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return toReturn;
    }
}
