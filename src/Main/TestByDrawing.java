package Main;

import com.aditya.general.utilities.Point2D;
import com.aditya.graph.library.*;
import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableUndirectedGraph;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class TestByDrawing extends JApplet
{
    private static final Dimension DEFAULT_SIZE = new Dimension(1000, 600);
    private static final Color DEFAULT_BG_COLOR = Color.decode("#FAFBFF");

    public static void main(String[] args)
    {
        Graph graphToTest = Main.readFileToGraph(
                "C:\\Users\\adity\\IdeaProjects\\GraphLibrary\\inputFiles\\simplePlanar.txt");

        IPlanarEmbeddingMethods embedder = PlanarEmbeddingFactory
                .GetPlanarEmbeddingStrategy(PlanarEmbeddingStrategies.DMP);

        Graph embeddedGraph = new Graph(false);
        boolean isPlanar = embedder.isPlanar(graphToTest, embeddedGraph);

        if (!isPlanar)
        {
            return;
        }

        Graph triangulatedGraph = new Graph(embeddedGraph.isDirected);
        embedder.triangulate(embeddedGraph, triangulatedGraph);

        IPlanarDrawingMethods drawer = PlanarDrawingFactory
                .GetPlanarDrawingStrategy(PlanarDrawingStrategies.SCHNYDER);

        ArrayList<Point2D> positions = drawer.DrawOnPlane(triangulatedGraph);
        for (int i = 0; i < positions.size(); i++)
        {
            System.out.println(i + " - " + positions.get(i));
        }

        TestByDrawing graphDrawingFrame = new TestByDrawing();
        graphDrawingFrame.init(graphToTest, positions);

        JFrame frame = new JFrame();
        frame.getContentPane().add(graphDrawingFrame);
        frame.setTitle("Planar embedding of a graph");
        frame.pack();
        frame.setVisible(true);
    }

    private void adjustDisplaySettings(JGraph jg)
    {
        jg.setPreferredSize(DEFAULT_SIZE);

        Color c = DEFAULT_BG_COLOR;
        String colorStr = null;

        try
        {
            colorStr = getParameter("bgcolor");
        }
        catch (Exception e)
        {
        }

        if (colorStr != null)
        {
            c = Color.decode(colorStr);
        }

        jg.setBackground(c);
    }

    public void init(Graph embeddedGraph, List<Point2D> positions)
    {
        ListenableGraph<String, DefaultEdge> graph = new ListenableUndirectedGraph<>(DefaultEdge.class);
        JGraphModelAdapter<String, DefaultEdge> adapter = new JGraphModelAdapter<>(graph);

        JGraph jGraph = new JGraph(adapter);
        getContentPane().add(jGraph);
        adjustDisplaySettings(jGraph);
        resize(DEFAULT_SIZE);

        // add nodes
        for (int i = 0; i < embeddedGraph.nodesCount; i++)
        {
            graph.addVertex(getNodeLabel(i));
        }

        // add edges
        for (Edge edge : embeddedGraph.getEdges())
        {
            graph.addEdge(getNodeLabel(edge.src), getNodeLabel(edge.dest));
        }

        double minX = embeddedGraph.nodesCount * 2, maxX = -1;
        double minY = embeddedGraph.nodesCount * 2, maxY = -1;
        for (Point2D point : positions)
        {
            if (point.x < minX)
            {
                minX = point.x;
            }
            if (point.x > maxX)
            {
                maxX = point.x;
            }
            if (point.y < minY)
            {
                minY = point.y;
            }
            if (point.y > maxY)
            {
                maxY = point.y;
            }
        }

        for (int i = 0; i < embeddedGraph.nodesCount; i++)
        {
            positionNode(
                    adapter,
                    i,
                    getPosition(positions.get(i).x, minX, maxX, DEFAULT_SIZE.getWidth()),
                    getPosition(positions.get(i).y, minY, maxY, DEFAULT_SIZE.getHeight()));
        }
    }

    private double getPosition(double relative, double min, double max, double fullSize)
    {
        return ((relative - min) / (max - min)) * fullSize;
    }

    private void positionNode(
            JGraphModelAdapter<String, DefaultEdge> adapter,
            int node,
            double x,
            double y)
    {
        DefaultGraphCell cell = adapter.getVertexCell(getNodeLabel(node));
        AttributeMap attrMap = cell.getAttributes();
        Rectangle2D bounds = GraphConstants.getBounds(attrMap);

        Rectangle2D newBounds =
                new Rectangle2D.Double(
                        x,
                        y,
                        bounds.getWidth(),
                        bounds.getHeight());

        GraphConstants.setBounds(attrMap, newBounds);
        AttributeMap cellAttr = new AttributeMap();
        cellAttr.put(cell, attrMap);
        adapter.edit(cellAttr, null, null, null);
    }

    private String getNodeLabel(int node)
    {
        return (node + 1) + "";
    }
}
