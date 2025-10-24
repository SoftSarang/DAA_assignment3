package com.aitu;

import com.aitu.Dependencies.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PrimMSTTest {
    @Test
    void testMSTWeightComparisonWithKruskal() {
        EdgeWeightedGraph G = new EdgeWeightedGraph(4);
        G.addEdge(new Edge(0, 1, 1));
        G.addEdge(new Edge(0, 2, 4));
        G.addEdge(new Edge(1, 2, 2));
        G.addEdge(new Edge(2, 3, 3));

        Metrics primTracker = new Metrics();
        PrimMST primMST = new PrimMST(G, primTracker);
        double primWeight = primMST.weight();

        Metrics kruskalTracker = new Metrics();
        KruskalMST kruskalMST = new KruskalMST(G, kruskalTracker);
        double kruskalWeight = kruskalMST.weight();

        assertEquals(primWeight, kruskalWeight, 0.001, "MST weights should be identical for both algorithms");
    }

    @Test
    void testConnectedGraph() {
        EdgeWeightedGraph G = new EdgeWeightedGraph(4);
        G.addEdge(new Edge(0, 1, 1));
        G.addEdge(new Edge(0, 2, 4));
        G.addEdge(new Edge(1, 2, 2));
        G.addEdge(new Edge(2, 3, 3));

        Metrics tracker = new Metrics();
        PrimMST mst = new PrimMST(G, tracker);

        assertEquals(6.0, mst.weight(), 0.001);
        int count = 0;
        for (Edge e : mst.edges()) {
            count++;
        }
        assertEquals(3, count);

        assertEquals(4, mst.getVertices());
        assertEquals(4, mst.getEdgesCount());

        assertTrue(tracker.getTotalOperations() > 0);
        assertTrue(tracker.getExecutionTimeMs() > 0.0);
    }

    @Test
    void testDisconnectedGraph() {
        EdgeWeightedGraph G = new EdgeWeightedGraph(3);
        G.addEdge(new Edge(0, 1, 1));

        Metrics tracker = new Metrics();
        assertThrows(IllegalStateException.class, () -> new PrimMST(G, tracker));
    }

    @Test
    void testCyclePrevention() {
        EdgeWeightedGraph G = new EdgeWeightedGraph(3);
        G.addEdge(new Edge(0, 1, 1));
        G.addEdge(new Edge(1, 2, 2));
        G.addEdge(new Edge(0, 2, 3));

        Metrics tracker = new Metrics();
        PrimMST mst = new PrimMST(G, tracker);
        assertEquals(3.0, mst.weight(), 0.001);
        int count = 0;
        for (Edge e : mst.edges()) {
            count++;
        }
        assertEquals(2, count);

        assertEquals(3, mst.getVertices());
        assertEquals(3, mst.getEdgesCount());
    }

    @Test
    void testReproducibility() {
        EdgeWeightedGraph G = new EdgeWeightedGraph(4);
        G.addEdge(new Edge(0, 1, 1));
        G.addEdge(new Edge(0, 2, 4));
        G.addEdge(new Edge(1, 2, 2));
        G.addEdge(new Edge(2, 3, 3));

        Metrics tracker1 = new Metrics();
        PrimMST mst1 = new PrimMST(G, tracker1);
        double weight1 = mst1.weight();

        Metrics tracker2 = new Metrics();
        PrimMST mst2 = new PrimMST(G, tracker2);
        double weight2 = mst2.weight();

        assertEquals(weight1, weight2, 0.001);

        assertEquals(4, mst1.getVertices());
        assertEquals(4, mst1.getEdgesCount());
        assertEquals(4, mst2.getVertices());
        assertEquals(4, mst2.getEdgesCount());
    }

    @Test
    void testSelfLoop() {
        EdgeWeightedGraph G = new EdgeWeightedGraph(2);
        G.addEdge(new Edge(0, 0, 1));
        G.addEdge(new Edge(0, 1, 2));

        Metrics tracker = new Metrics();
        PrimMST mst = new PrimMST(G, tracker);
        assertEquals(2.0, mst.weight(), 0.001);
        int count = 0;
        for (Edge e : mst.edges()) {
            count++;
        }
        assertEquals(1, count);

        assertEquals(2, mst.getVertices());
        assertEquals(2, mst.getEdgesCount());
    }

    @Test
    void testNegativeWeight() {
        EdgeWeightedGraph G = new EdgeWeightedGraph(2);
        G.addEdge(new Edge(0, 1, -1));

        Metrics tracker = new Metrics();
        assertThrows(IllegalArgumentException.class, () -> new PrimMST(G, tracker));
    }
}