package com.aitu;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Metrics {
    private long comparisons = 0;
    private long unions = 0;
    private long pqOperations = 0;
    private long startTime;
    private double executionTimeMs = 0.0;

    public void startTimer() {
        startTime = System.nanoTime();
    }

    public void stopTimer() {
        long endTime = System.nanoTime();
        executionTimeMs = (endTime - startTime) / 1_000_000.0;
    }

    public void incrementComparison() {
        comparisons++;
    }

    public void incrementUnion() {
        unions++;
    }

    public void incrementPQOperation() {
        pqOperations++;
    }

    public long getComparisons() {
        return comparisons;
    }

    public long getUnions() {
        return unions;
    }

    public long getPQOperations() {
        return pqOperations;
    }

    public long getTotalOperations() {
        return comparisons + unions + pqOperations;
    }

    public double getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void reset() {
        comparisons = 0;
        unions = 0;
        pqOperations = 0;
        executionTimeMs = 0.0;
    }

    @Override
    public String toString() {
        return "Metrics{comparisons=" + comparisons + ", unions=" + unions +
                ", pqOperations=" + pqOperations + ", totalOps=" + getTotalOperations() +
                ", timeMs=" + executionTimeMs + "}";
    }

    public static void writeCsv(String filePath, String[][] data, boolean append) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, append))) {
            if (!append) {
                writer.write("graph_id,vertices,edges,algorithm,total_cost,operations_count,execution_time_ms\n");
            }
            for (String[] row : data) {
                writer.write(String.join(",", row) + "\n");
            }
        }
    }

}
