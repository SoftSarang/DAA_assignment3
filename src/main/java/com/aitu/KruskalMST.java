package com.aitu;

import com.aitu.Dependencies.*;

import java.util.Arrays;

public class KruskalMST {
    private final int vertices;
    private final int edgesCount;
    private Queue<Edge> mst;
    private double totalWeight;
    private Metrics tracker;

    public KruskalMST(EdgeWeightedGraph graph, Metrics tracker) {
        this.vertices = graph.V();
        this.edgesCount = graph.E();
        this.tracker = tracker;
        this.mst = new Queue<>();
        tracker.reset();
        tracker.startTimer();

        Edge[] edges = new Edge[edgesCount];
        int index = 0;
        for (Edge e : graph.edges()) {
            edges[index++] = e;
        }

        Arrays.sort(edges);

        UF unionFind = new UF(vertices);
        int edgesAdded = 0;

        for (Edge e : edges) {
            int v = e.either();
            int w = e.other(v);

            if (v == w) {
                continue;
            }
            if (e.weight() < 0) {
                throw new IllegalArgumentException("Negative edge weight detected: " + e.weight());
            }

            tracker.incrementComparison();
            int rootV = unionFind.find(v);
            tracker.incrementComparison();
            int rootW = unionFind.find(w);

            if (rootV != rootW) {
                tracker.incrementUnion();
                unionFind.union(v, w);
                mst.enqueue(e);
                totalWeight += e.weight();
                edgesAdded++;
            }

            if (edgesAdded == vertices - 1) break;
        }

        if (edgesAdded != vertices - 1) {
            throw new IllegalStateException("Graph is not connected, no MST possible");
        }

        tracker.stopTimer();
    }

    public Iterable<Edge> edges() {
        return mst;
    }

    public double weight() {
        return totalWeight;
    }

    public Metrics getMetrics() {
        return tracker;
    }

    public int getVertices() {
        return vertices;
    }

    public int getEdgesCount() {
        return edgesCount;
    }
}