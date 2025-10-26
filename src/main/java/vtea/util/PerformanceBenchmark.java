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
import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Automated performance benchmarking system for VTEA.
 * Provides repeatable tests to measure and track performance over time.
 *
 * Usage:
 * <pre>
 * PerformanceBenchmark benchmark = new PerformanceBenchmark();
 * benchmark.runBenchmarks();
 * benchmark.saveResults("benchmark_results.csv");
 * </pre>
 *
 * @author Claude Code
 */
public class PerformanceBenchmark {

    private List<BenchmarkResult> results = new ArrayList<>();
    private boolean verbose = true;

    public PerformanceBenchmark() {
        this(true);
    }

    public PerformanceBenchmark(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Run a single benchmark test.
     *
     * @param name Name of the test
     * @param iterations Number of times to run the test
     * @param test The test to execute
     * @return BenchmarkResult containing statistics
     */
    public BenchmarkResult runBenchmark(String name, int iterations, Callable<Void> test) {
        if (verbose) {
            IJ.log("Running benchmark: " + name + " (" + iterations + " iterations)");
        }

        List<Long> times = new ArrayList<>();
        long totalMemoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        // Warm-up run
        try {
            test.call();
        } catch (Exception e) {
            IJ.log("ERROR during warm-up: " + e.getMessage());
        }

        // Actual benchmark runs
        for (int i = 0; i < iterations; i++) {
            // Force GC before each iteration for consistent results
            System.gc();
            try {
                Thread.sleep(50); // Let GC complete
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            long start = System.nanoTime();
            try {
                test.call();
            } catch (Exception e) {
                IJ.log("ERROR during iteration " + i + ": " + e.getMessage());
                continue;
            }
            long duration = (System.nanoTime() - start) / 1_000_000; // Convert to ms
            times.add(duration);

            if (verbose && i % 10 == 0) {
                IJ.log("  Iteration " + i + ": " + duration + "ms");
            }
        }

        long totalMemoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long memoryDelta = totalMemoryAfter - totalMemoryBefore;

        BenchmarkResult result = new BenchmarkResult(name, times, memoryDelta);
        results.add(result);

        if (verbose) {
            IJ.log(result.toString());
        }

        return result;
    }

    /**
     * Run standard VTEA benchmarks.
     */
    public void runStandardBenchmarks() {
        IJ.log("=== VTEA STANDARD BENCHMARKS ===");
        IJ.log("Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        IJ.log("Java Version: " + System.getProperty("java.version"));
        IJ.log("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        IJ.log("Available Processors: " + Runtime.getRuntime().availableProcessors());
        IJ.log("Max Memory: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + " MB");
        IJ.log("");

        // Benchmark 1: ArrayList operations
        runBenchmark("ArrayList Add/Remove (10k items)", 50, () -> {
            ArrayList<Integer> list = new ArrayList<>();
            for (int i = 0; i < 10000; i++) {
                list.add(i);
            }
            for (int i = 0; i < 5000; i++) {
                list.remove(0);
            }
            return null;
        });

        // Benchmark 2: Object creation
        runBenchmark("Object Creation (1k objects)", 50, () -> {
            for (int i = 0; i < 1000; i++) {
                new Object();
            }
            return null;
        });

        // Benchmark 3: String concatenation
        runBenchmark("String Concatenation (1k strings)", 50, () -> {
            String result = "";
            for (int i = 0; i < 1000; i++) {
                result += i;
            }
            return null;
        });

        // Benchmark 4: StringBuilder (better practice)
        runBenchmark("StringBuilder (1k strings)", 50, () -> {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                sb.append(i);
            }
            String result = sb.toString();
            return null;
        });

        IJ.log("=== BENCHMARKS COMPLETE ===");
        IJ.log("");
        printSummary();
    }

    /**
     * Print summary of all benchmark results.
     */
    public void printSummary() {
        IJ.log("=== BENCHMARK SUMMARY ===");
        IJ.log(String.format("%-40s %10s %10s %10s %10s %15s",
                "Benchmark", "Avg(ms)", "Min(ms)", "Max(ms)", "StdDev", "Memory(KB)"));
        IJ.log(String.format("%-40s %10s %10s %10s %10s %15s",
                "----------", "-------", "-------", "-------", "------", "----------"));

        for (BenchmarkResult result : results) {
            IJ.log(String.format("%-40s %10.2f %10d %10d %10.2f %15d",
                    result.name.length() > 40 ? result.name.substring(0, 37) + "..." : result.name,
                    result.average,
                    result.min,
                    result.max,
                    result.stdDev,
                    result.memoryDelta / 1024));
        }

        IJ.log("=== END SUMMARY ===");
    }

    /**
     * Save benchmark results to CSV file.
     *
     * @param filename Output file path
     */
    public void saveResults(String filename) {
        try (PrintWriter writer = new PrintWriter(new File(filename))) {
            // Header
            writer.println("Benchmark,Iterations,Average(ms),Min(ms),Max(ms),StdDev(ms),Memory(KB),Timestamp");

            // Data
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            for (BenchmarkResult result : results) {
                writer.printf("%s,%d,%.2f,%d,%d,%.2f,%d,%s%n",
                        result.name,
                        result.iterations,
                        result.average,
                        result.min,
                        result.max,
                        result.stdDev,
                        result.memoryDelta / 1024,
                        timestamp);
            }

            IJ.log("Benchmark results saved to: " + filename);
        } catch (Exception e) {
            IJ.log("ERROR saving results: " + e.getMessage());
        }
    }

    /**
     * Clear all benchmark results.
     */
    public void clearResults() {
        results.clear();
    }

    /**
     * Get all benchmark results.
     */
    public List<BenchmarkResult> getResults() {
        return new ArrayList<>(results);
    }

    /**
     * Result of a single benchmark test.
     */
    public static class BenchmarkResult {
        public final String name;
        public final int iterations;
        public final long min;
        public final long max;
        public final double average;
        public final double stdDev;
        public final long memoryDelta;

        public BenchmarkResult(String name, List<Long> times, long memoryDelta) {
            this.name = name;
            this.iterations = times.size();
            this.memoryDelta = memoryDelta;

            if (times.isEmpty()) {
                this.min = 0;
                this.max = 0;
                this.average = 0;
                this.stdDev = 0;
                return;
            }

            // Calculate min, max, average
            long sum = 0;
            long min = Long.MAX_VALUE;
            long max = Long.MIN_VALUE;

            for (long time : times) {
                sum += time;
                min = Math.min(min, time);
                max = Math.max(max, time);
            }

            this.min = min;
            this.max = max;
            this.average = (double) sum / times.size();

            // Calculate standard deviation
            double sumSquaredDiff = 0;
            for (long time : times) {
                double diff = time - average;
                sumSquaredDiff += diff * diff;
            }
            this.stdDev = Math.sqrt(sumSquaredDiff / times.size());
        }

        @Override
        public String toString() {
            return String.format("%s: avg=%.2fms, min=%dms, max=%dms, stdDev=%.2fms, memory=%dKB (%d iterations)",
                    name, average, min, max, stdDev, memoryDelta / 1024, iterations);
        }
    }

    /**
     * Main method for standalone benchmarking.
     */
    public static void main(String[] args) {
        PerformanceBenchmark benchmark = new PerformanceBenchmark();
        benchmark.runStandardBenchmarks();

        // Save results
        String outputFile = "vtea_benchmark_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv";
        benchmark.saveResults(outputFile);
    }
}
