/*
 * Copyright (C) 2020 Indiana University
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package vtea.util;

import ij.IJ;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight performance profiling utility for identifying performance bottlenecks.
 * This class provides methods to time operations and log warnings when they exceed thresholds.
 *
 * Usage:
 * <pre>
 * PerformanceProfiler.profileOperation("LoadImage", () -> {
 *     // Long running operation
 * });
 * </pre>
 *
 * @author Claude Code
 */
public class PerformanceProfiler {

    private static final boolean PROFILING_ENABLED = Boolean.getBoolean("vtea.profile");
    private static final long WARNING_THRESHOLD_MS = Long.getLong("vtea.profile.threshold", 100);

    // Track operation statistics
    private static final Map<String, OperationStats> stats = new ConcurrentHashMap<>();

    /**
     * Profile a runnable operation and log if it exceeds threshold.
     *
     * @param operationName The name of the operation being profiled
     * @param operation The operation to execute
     */
    public static void profileOperation(String operationName, Runnable operation) {
        if (!PROFILING_ENABLED) {
            operation.run();
            return;
        }

        long start = System.nanoTime();
        try {
            operation.run();
        } finally {
            long duration = (System.nanoTime() - start) / 1_000_000; // Convert to ms
            recordOperation(operationName, duration);

            if (duration > WARNING_THRESHOLD_MS) {
                IJ.log("PERFORMANCE: " + operationName + " took " + duration + "ms (threshold: " + WARNING_THRESHOLD_MS + "ms)");
            }
        }
    }

    /**
     * Profile an operation with exception handling.
     *
     * @param operationName The name of the operation being profiled
     * @param operation The operation to execute
     * @throws Exception if the operation throws an exception
     */
    public static void profileOperationWithException(String operationName, RunnableWithException operation) throws Exception {
        if (!PROFILING_ENABLED) {
            operation.run();
            return;
        }

        long start = System.nanoTime();
        try {
            operation.run();
        } finally {
            long duration = (System.nanoTime() - start) / 1_000_000;
            recordOperation(operationName, duration);

            if (duration > WARNING_THRESHOLD_MS) {
                IJ.log("PERFORMANCE: " + operationName + " took " + duration + "ms (threshold: " + WARNING_THRESHOLD_MS + "ms)");
            }
        }
    }

    /**
     * Start timing an operation. Must be paired with {@link #endTiming(String)}.
     *
     * @param operationName The name of the operation
     * @return The start time in nanoseconds
     */
    public static long startTiming(String operationName) {
        return System.nanoTime();
    }

    /**
     * End timing an operation started with {@link #startTiming(String)}.
     *
     * @param operationName The name of the operation
     * @param startTime The start time from startTiming
     */
    public static void endTiming(String operationName, long startTime) {
        if (!PROFILING_ENABLED) {
            return;
        }

        long duration = (System.nanoTime() - startTime) / 1_000_000;
        recordOperation(operationName, duration);

        if (duration > WARNING_THRESHOLD_MS) {
            IJ.log("PERFORMANCE: " + operationName + " took " + duration + "ms (threshold: " + WARNING_THRESHOLD_MS + "ms)");
        }
    }

    /**
     * Record an operation's execution time.
     */
    private static void recordOperation(String operationName, long durationMs) {
        stats.computeIfAbsent(operationName, k -> new OperationStats())
             .record(durationMs);
    }

    /**
     * Print performance statistics to ImageJ log.
     */
    public static void printStatistics() {
        if (stats.isEmpty()) {
            IJ.log("PERFORMANCE: No operations profiled yet.");
            return;
        }

        IJ.log("=== PERFORMANCE STATISTICS ===");
        IJ.log(String.format("%-40s %8s %8s %8s %8s", "Operation", "Count", "Avg(ms)", "Min(ms)", "Max(ms)"));
        IJ.log(String.format("%-40s %8s %8s %8s %8s", "----------", "-----", "-------", "-------", "-------"));

        stats.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue().totalTime, a.getValue().totalTime))
            .forEach(entry -> {
                String name = entry.getKey();
                OperationStats s = entry.getValue();
                IJ.log(String.format("%-40s %8d %8.2f %8d %8d",
                    name.length() > 40 ? name.substring(0, 37) + "..." : name,
                    s.count,
                    s.count > 0 ? (double) s.totalTime / s.count : 0,
                    s.minTime,
                    s.maxTime));
            });

        IJ.log("=== END STATISTICS ===");
    }

    /**
     * Clear all recorded statistics.
     */
    public static void clearStatistics() {
        stats.clear();
    }

    /**
     * Check if profiling is enabled.
     */
    public static boolean isProfilingEnabled() {
        return PROFILING_ENABLED;
    }

    /**
     * Statistics for a single operation type.
     */
    private static class OperationStats {
        long count = 0;
        long totalTime = 0;
        long minTime = Long.MAX_VALUE;
        long maxTime = 0;

        synchronized void record(long durationMs) {
            count++;
            totalTime += durationMs;
            minTime = Math.min(minTime, durationMs);
            maxTime = Math.max(maxTime, durationMs);
        }
    }

    /**
     * Functional interface for operations that can throw exceptions.
     */
    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws Exception;
    }
}
