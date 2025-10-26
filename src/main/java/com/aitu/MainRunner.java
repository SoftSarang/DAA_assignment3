package com.aitu;

import org.json.JSONArray;
import org.json.JSONObject;
import com.aitu.Dependencies.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainRunner {
    public static void main(String[] args) {
        try {
            warmupJVM();

            JSONObject input = readInputJson();
            JSONArray graphs = input.getJSONArray("graphs");
            JSONArray results = new JSONArray();
            String csvFilePath = "data/output.csv";

            Metrics.writeCsv(csvFilePath, new String[][]{}, false);

            for (int i = 0; i < graphs.length(); i++) {
                JSONObject graphJson = graphs.getJSONObject(i);
                int id = graphJson.getInt("id");
                JSONArray nodes = graphJson.getJSONArray("nodes");
                Map<String, Integer> nodeMap = new HashMap<>();
                for (int j = 0; j < nodes.length(); j++) {
                    nodeMap.put(nodes.getString(j), j);
                }
                EdgeWeightedGraph graph = parseGraph(graphJson, nodeMap);
                int vertexCount = graph.V();
                int edgeCount = graph.E();

                System.gc();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                Metrics primMetrics = new Metrics();
                PrimMST primMST = new PrimMST(graph, primMetrics);
                double primWeight = primMST.weight();
                JSONArray primMstEdges = getMstEdges(primMST.edges(), nodeMap);
                JSONObject primResult = createAlgorithmResult("prim", primWeight, primMstEdges, primMetrics);

                System.gc();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                Metrics kruskalMetrics = new Metrics();
                KruskalMST kruskalMST = new KruskalMST(graph, kruskalMetrics);
                double kruskalWeight = kruskalMST.weight();
                JSONArray kruskalMstEdges = getMstEdges(kruskalMST.edges(), nodeMap);
                JSONObject kruskalResult = createAlgorithmResult("kruskal", kruskalWeight, kruskalMstEdges, kruskalMetrics);

                JSONObject result = new JSONObject();
                result.put("graph_id", id);
                result.put("input_stats", new JSONObject()
                        .put("vertices", vertexCount)
                        .put("edges", edgeCount));
                result.put("prim", primResult);
                result.put("kruskal", kruskalResult);
                results.put(result);

                String[][] csvData = {
                        {String.valueOf(id), String.valueOf(vertexCount), String.valueOf(edgeCount), "Prim", String.format("%.2f", primWeight),
                                String.valueOf(primMetrics.getTotalOperations()), String.format("%.2f", primMetrics.getExecutionTimeMs())},
                        {String.valueOf(id), String.valueOf(vertexCount), String.valueOf(edgeCount), "Kruskal", String.format("%.2f", kruskalWeight),
                                String.valueOf(kruskalMetrics.getTotalOperations()), String.format("%.2f", kruskalMetrics.getExecutionTimeMs())}
                };
                Metrics.writeCsv(csvFilePath, csvData, true);

                System.gc();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            writeOutputJson(results);

            System.out.println("✅ Processing completed. Check data/output.json and data/output.csv");
        } catch (IOException e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    private static JSONObject readInputJson() throws IOException {
        File file = new File("data/input.json");
        StringBuilder content = new StringBuilder();
        try (java.util.Scanner scanner = new java.util.Scanner(file)) {
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine());
            }
        }
        return new JSONObject(content.toString());
    }

    private static EdgeWeightedGraph parseGraph(JSONObject graphJson, Map<String, Integer> nodeMap) {
        int vertexCount = nodeMap.size();
        if (vertexCount == 0) {
            throw new IllegalArgumentException("Graph has no vertices");
        }
        EdgeWeightedGraph graph = new EdgeWeightedGraph(vertexCount);
        JSONArray edges = graphJson.getJSONArray("edges");
        for (int i = 0; i < edges.length(); i++) {
            JSONObject edge = edges.getJSONObject(i);
            int source = nodeMap.get(edge.getString("from"));
            int target = nodeMap.get(edge.getString("to"));
            double weight = edge.getDouble("weight");

            if (weight < 0) {
                throw new IllegalArgumentException(
                        String.format("Negative edge weight detected: %.2f (from %s to %s)",
                                weight, edge.getString("from"), edge.getString("to"))
                );
            }

            if (source == target) {
                throw new IllegalArgumentException(
                        String.format("Self-loop detected: %s -> %s",
                                edge.getString("from"), edge.getString("to"))
                );
            }
            graph.addEdge(new Edge(source, target, weight));
        }
        return graph;
    }

    private static void warmupJVM() {
        EdgeWeightedGraph warmupGraph = new EdgeWeightedGraph(20);
        for (int i = 0; i < 19; i++) {
            warmupGraph.addEdge(new Edge(i, i + 1, Math.random() * 10));
        }

        for (int i = 0; i < 10; i++) {
            Metrics dummyMetrics = new Metrics();
            new PrimMST(warmupGraph, dummyMetrics);
            new KruskalMST(warmupGraph, dummyMetrics);
        }
    }
    private static JSONArray getMstEdges(Iterable<Edge> edges, Map<String, Integer> nodeMap) {
        JSONArray mstEdges = new JSONArray();
        for (Edge edge : edges) {
            JSONObject edgeJson = new JSONObject();
            int v = edge.either();
            int w = edge.other(v);
            edgeJson.put("from", getKeyByValue(nodeMap, v));
            edgeJson.put("to", getKeyByValue(nodeMap, w));
            edgeJson.put("weight", edge.weight());
            mstEdges.put(edgeJson);
        }
        return mstEdges;
    }

    private static <K, V> K getKeyByValue(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private static JSONObject createAlgorithmResult(String algorithm, double weight, JSONArray mstEdges, Metrics metrics) {
        JSONObject result = new JSONObject();
        result.put("mst_edges", mstEdges);
        result.put("total_cost", weight);
        if (metrics != null) {
            result.put("operations_count", metrics.getTotalOperations());
            result.put("execution_time_ms", metrics.getExecutionTimeMs());
        } else {
            result.put("operations_count", 0);
            result.put("execution_time_ms", 0.0);
        }
        return result;
    }

    private static void writeOutputJson(JSONArray results) throws IOException {
        JSONObject output = new JSONObject();
        output.put("results", results);
        File dir = new File("data");
        if (!dir.exists()) dir.mkdirs();
        try (FileWriter file = new FileWriter("data/output.json")) {
            file.write(output.toString(4));
        }
    }
}