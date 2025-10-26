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

        for (int i = 0; i < edgesCount && edgesAdded < vertices - 1; i++) {
            Edge e = edges[i];
            int v = e.either();
            int w = e.other(v);

            tracker.incrementFind();
            int rootV = unionFind.find(v);
            tracker.incrementFind();
            int rootW = unionFind.find(w);

            tracker.incrementComparison();

            if (rootV != rootW) {
                unionFind.union(v, w);
                tracker.incrementUnion();
                mst.enqueue(e);
                totalWeight += e.weight();
                edgesAdded++;
            }
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