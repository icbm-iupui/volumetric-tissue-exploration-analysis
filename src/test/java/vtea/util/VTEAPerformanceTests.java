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
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Performance tests for VTEA components.
 * These tests establish performance baselines and detect regressions.
 *
 * Run with: mvn test -Dtest=VTEAPerformanceTests
 *
 * @author Claude Code
 */
public class VTEAPerformanceTests {

    private PerformanceBenchmark benchmark;

    @Before
    public void setUp() {
        benchmark = new PerformanceBenchmark(false); // Silent mode for tests
    }

    @After
    public void tearDown() {
        benchmark.clearResults();
    }

    @Test
    public void testArrayListPerformance() {
        PerformanceBenchmark.BenchmarkResult result = benchmark.runBenchmark(
                "ArrayList Operations",
                100,
                () -> {
                    ArrayList<Integer> list = new ArrayList<>();
                    for (int i = 0; i < 1000; i++) {
                        list.add(i);
                    }
                    for (int i = 0; i < 500; i++) {
                        list.remove(0);
                    }
                    return null;
                }
        );

        // Assert reasonable performance (adjust thresholds as needed)
        assertTrue("ArrayList operations too slow: " + result.average + "ms",
                result.average < 50);
        assertTrue("Memory usage too high: " + result.memoryDelta / 1024 + "KB",
                result.memoryDelta < 1024 * 1024); // Less than 1MB
    }

    @Test
    public void testHashMapPerformance() {
        PerformanceBenchmark.BenchmarkResult result = benchmark.runBenchmark(
                "HashMap Operations",
                100,
                () -> {
                    HashMap<String, Object> map = new HashMap<>();
                    for (int i = 0; i < 1000; i++) {
                        map.put("key" + i, new Object());
                    }
                    for (int i = 0; i < 1000; i++) {
                        map.get("key" + i);
                    }
                    return null;
                }
        );

        assertTrue("HashMap operations too slow: " + result.average + "ms",
                result.average < 50);
    }

    @Test
    public void testBufferedImageCreation() {
        PerformanceBenchmark.BenchmarkResult result = benchmark.runBenchmark(
                "BufferedImage Creation",
                50,
                () -> {
                    BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
                    java.awt.Graphics2D g2 = img.createGraphics();
                    g2.setColor(Color.RED);
                    g2.fillRect(0, 0, 800, 600);
                    g2.dispose();
                    return null;
                }
        );

        // BufferedImage creation should be fast
        assertTrue("BufferedImage creation too slow: " + result.average + "ms",
                result.average < 100);
    }

    @Test
    public void testBufferedImageReuse() {
        // Test that reusing images is faster than creating new ones
        BufferedImage cachedImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);

        PerformanceBenchmark.BenchmarkResult reusedResult = benchmark.runBenchmark(
                "BufferedImage Reuse",
                100,
                () -> {
                    java.awt.Graphics2D g2 = cachedImage.createGraphics();
                    g2.setBackground(new Color(0, 0, 0, 0));
                    g2.clearRect(0, 0, 800, 600);
                    g2.setColor(Color.RED);
                    g2.fillRect(0, 0, 800, 600);
                    g2.dispose();
                    return null;
                }
        );

        PerformanceBenchmark.BenchmarkResult newResult = benchmark.runBenchmark(
                "BufferedImage New",
                100,
                () -> {
                    BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
                    java.awt.Graphics2D g2 = img.createGraphics();
                    g2.setColor(Color.RED);
                    g2.fillRect(0, 0, 800, 600);
                    g2.dispose();
                    return null;
                }
        );

        // Reusing should be faster
        assertTrue("Image reuse not faster than creation",
                reusedResult.average < newResult.average);
    }

    @Test
    public void testQueryCachePerformance() {
        // Simulate database query caching
        HashMap<String, ArrayList<Integer>> cache = new HashMap<>();

        PerformanceBenchmark.BenchmarkResult cachedResult = benchmark.runBenchmark(
                "Cached Query",
                100,
                () -> {
                    String cacheKey = "query_1";
                    ArrayList<Integer> result;
                    if (cache.containsKey(cacheKey)) {
                        result = cache.get(cacheKey);
                    } else {
                        result = new ArrayList<>();
                        for (int i = 0; i < 100; i++) {
                            result.add(i);
                        }
                        cache.put(cacheKey, result);
                    }
                    return null;
                }
        );

        cache.clear();

        PerformanceBenchmark.BenchmarkResult uncachedResult = benchmark.runBenchmark(
                "Uncached Query",
                100,
                () -> {
                    ArrayList<Integer> result = new ArrayList<>();
                    for (int i = 0; i < 100; i++) {
                        result.add(i);
                    }
                    return null;
                }
        );

        // Cached should be faster (after first iteration)
        assertTrue("Cache not providing speedup",
                cachedResult.average < uncachedResult.average * 0.8);
    }

    @Test
    public void testStringBuilderVsConcat() {
        PerformanceBenchmark.BenchmarkResult sbResult = benchmark.runBenchmark(
                "StringBuilder",
                100,
                () -> {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 100; i++) {
                        sb.append(i);
                    }
                    String result = sb.toString();
                    return null;
                }
        );

        PerformanceBenchmark.BenchmarkResult concatResult = benchmark.runBenchmark(
                "String Concatenation",
                100,
                () -> {
                    String result = "";
                    for (int i = 0; i < 100; i++) {
                        result += i;
                    }
                    return null;
                }
        );

        // StringBuilder should be much faster
        assertTrue("StringBuilder not faster than concatenation",
                sbResult.average < concatResult.average * 0.1);
    }

    @Test
    public void testPerformanceRegression() {
        // Run standard benchmarks
        benchmark.runStandardBenchmarks();

        // Verify no test took excessively long
        for (PerformanceBenchmark.BenchmarkResult result : benchmark.getResults()) {
            assertTrue("Benchmark '" + result.name + "' exceeded threshold: " + result.average + "ms",
                    result.average < 500);
        }
    }

    @Test
    public void testMemoryLeaks() {
        long initialMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        // Perform operations that should not leak
        for (int i = 0; i < 10; i++) {
            ArrayList<Object> temp = new ArrayList<>();
            for (int j = 0; j < 1000; j++) {
                temp.add(new Object());
            }
            temp.clear(); // Explicitly clear
            temp = null; // Nullify reference
        }

        // Force GC
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long finalMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long memoryIncrease = finalMemory - initialMemory;

        // Memory should not have increased significantly
        assertTrue("Possible memory leak detected: " + (memoryIncrease / 1024) + "KB increase",
                memoryIncrease < 5 * 1024 * 1024); // Less than 5MB increase
    }

    /**
     * Run all performance tests and generate report.
     */
    public static void main(String[] args) {
        VTEAPerformanceTests tests = new VTEAPerformanceTests();
        tests.setUp();

        try {
            IJ.log("=== VTEA COMPONENT PERFORMANCE TESTS ===");
            tests.testArrayListPerformance();
            tests.testHashMapPerformance();
            tests.testBufferedImageCreation();
            tests.testBufferedImageReuse();
            tests.testQueryCachePerformance();
            tests.testStringBuilderVsConcat();

            // Save results
            tests.benchmark.saveResults("vtea_performance_tests.csv");
            IJ.log("=== ALL TESTS PASSED ===");

        } catch (AssertionError e) {
            IJ.log("TEST FAILED: " + e.getMessage());
        } finally {
            tests.tearDown();
        }
    }
}
