package com.aitu;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class InputJsonGenerator {
    private final JSONArray graphs;
    private final Random random;

    public InputJsonGenerator() {
        this.graphs = new JSONArray();
        this.random = new Random();
    }

    private String[] generateNodeLabels(int count) {
        String[] labels = new String[count];
        for (int i = 0; i < count; i++) {
            labels[i] = "V" + (i + 1);
        }
        return labels;
    }

    private JSONObject generateRandomGraph(int id, int nodeCount, int edgesPerVertex) {
        JSONArray edges = new JSONArray();
        String[] nodes = generateNodeLabels(nodeCount);
        boolean[][] edgeMatrix = new boolean[nodeCount][nodeCount];

        for (int i = 1; i < nodeCount; i++) {
            int u = random.nextInt(i);
            int v = i;
            double weight = random.nextDouble() * 10;
            edges.put(new JSONObject()
                    .put("from", nodes[u])
                    .put("to", nodes[v])
                    .put("weight", weight));
            edgeMatrix[u][v] = edgeMatrix[v][u] = true;
        }

        int maxPossibleEdges = nodeCount * (nodeCount - 1) / 2;
        int totalEdges = Math.min(nodeCount * edgesPerVertex, maxPossibleEdges);

        while (edges.length() < totalEdges) {
            int u = random.nextInt(nodeCount);
            int v = random.nextInt(nodeCount);
            if (u != v && !edgeMatrix[u][v]) {
                double weight = 1 + random.nextDouble() * 9;
                edges.put(new JSONObject()
                        .put("from", nodes[u])
                        .put("to", nodes[v])
                        .put("weight", weight));
                edgeMatrix[u][v] = edgeMatrix[v][u] = true;
            }
        }

        JSONObject graph = new JSONObject();
        graph.put("id", id);
        graph.put("nodes", new JSONArray(nodes));
        graph.put("edges", edges);
        return graph;
    }

    public void generateAndSave() throws IOException {
        int idCounter = 1;

        int[] smallNodes = {5, 10, 15, 20, 25};
        for (int nodes : smallNodes) {
            graphs.put(generateRandomGraph(idCounter++, nodes, 3));
        }

        int[] mediumNodes = {30, 60, 90, 120, 150, 180, 210, 240, 270, 300};
        for (int nodes : mediumNodes) {
            graphs.put(generateRandomGraph(idCounter++, nodes, 3));
        }

        int[] largeNodes = {350, 400, 500, 600, 700, 800, 850, 900, 950, 1000};
        for (int nodes : largeNodes) {
            graphs.put(generateRandomGraph(idCounter++, nodes, 4));
        }

        int[] extraLargeNodes = {1300, 1600, 2000};
        for (int nodes : extraLargeNodes) {
            graphs.put(generateRandomGraph(idCounter++, nodes, 4));
        }

        JSONObject output = new JSONObject();
        output.put("graphs", graphs);

        File dir = new File("data");
        if (!dir.exists()) dir.mkdirs();

        try (FileWriter file = new FileWriter("data/input.json")) {
            file.write(output.toString(4));
        }

        System.out.println("✅ Saved " + graphs.length() + " graphs to data/input.json");
    }

    public static void main(String[] args) {
        InputJsonGenerator generator = new InputJsonGenerator();
        try {
            generator.generateAndSave();
        } catch (IOException e) {
            System.err.println("❌ Failed: " + e.getMessage());
        }
    }
}
