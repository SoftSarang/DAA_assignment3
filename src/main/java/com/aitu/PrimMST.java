package com.aitu;

import com.aitu.Dependencies.*;

public class PrimMST {
    private final int vertices;
    private final int edgesCount;
    private Edge[] edgeTo;
    private double[] distTo;
    private boolean[] marked;
    private double totalWeight;
    private IndexMinPQ<Double> pq;
    private Metrics tracker;

    public PrimMST(EdgeWeightedGraph graph, Metrics tracker) {
        this.vertices = graph.V();
        this.edgesCount = graph.E();
        this.tracker = tracker;
        tracker.reset();
        tracker.startTimer();

        edgeTo = new Edge[vertices];
        distTo = new double[vertices];
        marked = new boolean[vertices];
        for (int v = 0; v < vertices; v++) {
            distTo[v] = Double.POSITIVE_INFINITY;
        }
        pq = new IndexMinPQ<>(vertices);


        int startVertex = 0;
        for (int v = 0; v < vertices; v++) {
            if (graph.degree(v) > 0) {
                startVertex = v;
                break;
            }
        }
        distTo[startVertex] = 0.0;
        pq.insert(startVertex, distTo[startVertex]);
        tracker.incrementPQOperation();

        while (!pq.isEmpty()) {
            int v = pq.delMin();
            tracker.incrementPQOperation();
            marked[v] = true;
            updateMinEdge(graph, v);
        }

        for (int v = 0; v < vertices; v++) {
            if (!marked[v]) {
                throw new IllegalStateException("Graph is not connected, no MST possible");
            }
        }

        tracker.stopTimer();
    }

    private void updateMinEdge(EdgeWeightedGraph graph, int v) {
        for (Edge e : graph.adj(v)) {
            int w = e.other(v);

            tracker.incrementComparison();
            if (!marked[w] && e.weight() < distTo[w]) {
                distTo[w] = e.weight();
                edgeTo[w] = e;
                if (pq.contains(w)) {
                    pq.decreaseKey(w, distTo[w]);
                    tracker.incrementPQOperation();
                } else {
                    pq.insert(w, distTo[w]);
                    tracker.incrementPQOperation();
                }
            }
        }
    }

    public Iterable<Edge> edges() {
        Queue<Edge> mstEdges = new Queue<>();
        for (int v = 0; v < vertices; v++) {
            if (edgeTo[v] != null) {
                mstEdges.enqueue(edgeTo[v]);
            }
        }
        return mstEdges;
    }

    public double weight() {
        totalWeight = 0.0;
        for (Edge e : edges()) {
            totalWeight += e.weight();
        }
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