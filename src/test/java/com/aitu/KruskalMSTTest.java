package com.aitu;

import com.aitu.Dependencies.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class KruskalMSTTest {
    @Test
    void testConnectedGraph() {
        EdgeWeightedGraph G = new EdgeWeightedGraph(4);
        G.addEdge(new Edge(0, 1, 1));
        G.addEdge(new Edge(0, 2, 4));
        G.addEdge(new Edge(1, 2, 2));
        G.addEdge(new Edge(2, 3, 3));

        Metrics tracker = new Metrics();
        KruskalMST mst = new KruskalMST(G, tracker);

        assertEquals(6.0, mst.weight(), 0.001);
        int edgeCount = 0;
        for (Edge e : mst.edges()) {
            edgeCount++;
        }
        assertEquals(3, edgeCount);

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
        assertThrows(IllegalStateException.class, () -> new KruskalMST(G, tracker));
    }

    @Test
    void testSelfLoop() {
        EdgeWeightedGraph G = new EdgeWeightedGraph(2);
        G.addEdge(new Edge(0, 0, 1)); // Self-loop
        G.addEdge(new Edge(0, 1, 2));

        Metrics tracker = new Metrics();
        KruskalMST mst = new KruskalMST(G, tracker);
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
        assertThrows(IllegalArgumentException.class, () -> new KruskalMST(G, tracker));
    }

    @Test
    void testCyclePrevention() {
        EdgeWeightedGraph G = new EdgeWeightedGraph(3);
        G.addEdge(new Edge(0, 1, 1));
        G.addEdge(new Edge(1, 2, 2));
        G.addEdge(new Edge(0, 2, 3)); // Cycle possible

        Metrics tracker = new Metrics();
        KruskalMST mst = new KruskalMST(G, tracker);
        assertEquals(3.0, mst.weight(), 0.001); // Should take 1 + 2
        int count = 0;
        for (Edge e : mst.edges()) {
            count++;
        }
        assertEquals(2, count); // V-1 edges, no cycle

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
        KruskalMST mst1 = new KruskalMST(G, tracker1);
        double weight1 = mst1.weight();

        Metrics tracker2 = new Metrics();
        KruskalMST mst2 = new KruskalMST(G, tracker2);
        double weight2 = mst2.weight();

        assertEquals(weight1, weight2, 0.001);

        assertEquals(4, mst1.getVertices());
        assertEquals(4, mst1.getEdgesCount());
        assertEquals(4, mst2.getVertices());
        assertEquals(4, mst2.getEdgesCount());
    }
}