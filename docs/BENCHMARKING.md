# VTEA Performance Benchmarking Guide

## Overview

VTEA includes an automated performance benchmarking system to measure, track, and detect performance regressions.

## Quick Start

### Run Standard Benchmarks

```bash
# From command line
java -cp vtea.jar vtea.util.PerformanceBenchmark

# Or programmatically
PerformanceBenchmark benchmark = new PerformanceBenchmark();
benchmark.runStandardBenchmarks();
benchmark.saveResults("results.csv");
```

### Run Performance Tests

```bash
# Run all performance tests
mvn test -Dtest=VTEAPerformanceTests

# Run specific test
mvn test -Dtest=VTEAPerformanceTests#testBufferedImageCreation
```

## Benchmark System

### PerformanceBenchmark Class

The `PerformanceBenchmark` class provides:
- Automated timing of operations
- Statistical analysis (min/max/avg/stddev)
- Memory usage tracking
- CSV export for trend analysis

### Usage Example

```java
import vtea.util.PerformanceBenchmark;

PerformanceBenchmark benchmark = new PerformanceBenchmark();

// Run custom benchmark
BenchmarkResult result = benchmark.runBenchmark(
    "My Operation",
    100, // iterations
    () -> {
        // Your code to benchmark
        performOperation();
        return null;
    }
);

// Print results
System.out.println(result);

// Save to file
benchmark.saveResults("my_benchmark.csv");
```

## Standard Benchmarks

The standard benchmark suite includes:

1. **ArrayList Operations** - Add/remove 10k items
2. **Object Creation** - Create 1k objects
3. **String Concatenation** - Concatenate 1k strings
4. **StringBuilder** - StringBuilder with 1k strings

### Expected Results (Reference Hardware)

**Hardware:** Intel i7-9700K, 16GB RAM

| Benchmark | Average | Min | Max |
|-----------|---------|-----|-----|
| ArrayList Operations | <10ms | <5ms | <20ms |
| Object Creation | <5ms | <2ms | <10ms |
| String Concatenation | ~500ms | ~400ms | ~600ms |
| StringBuilder | <5ms | <2ms | <10ms |

## Performance Tests

### VTEAPerformanceTests

JUnit tests that verify performance doesn't regress:

```java
@Test
public void testArrayListPerformance() {
    // Ensures ArrayList operations stay under threshold
    assertTrue(result.average < 50);
}

@Test
public void testMemoryLeaks() {
    // Detects potential memory leaks
    assertTrue(memoryIncrease < 5MB);
}

@Test
public void testBufferedImageReuse() {
    // Verifies caching is faster than creation
    assertTrue(cachedTime < newTime);
}
```

### Running Tests

```bash
# Run all tests
mvn test

# Run only performance tests
mvn test -Dtest=VTEAPerformanceTests

# Run with verbose output
mvn test -Dtest=VTEAPerformanceTests -X

# Run and generate report
mvn test -Dtest=VTEAPerformanceTests surefire-report:report
```

## Continuous Integration

### GitHub Actions Example

```yaml
name: Performance Tests

on: [push, pull_request]

jobs:
  performance:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v2
        with:
          java-version: '11'
      - name: Run Performance Tests
        run: mvn test -Dtest=VTEAPerformanceTests
      - name: Upload Results
        uses: actions/upload-artifact@v2
        with:
          name: benchmark-results
          path: '*.csv'
```

## Tracking Performance Over Time

### CSV Output Format

```csv
Benchmark,Iterations,Average(ms),Min(ms),Max(ms),StdDev(ms),Memory(KB),Timestamp
ArrayList Operations,100,8.42,5,18,2.31,512,2025-10-26 10:30:00
```

### Analyzing Trends

```python
# Example Python script to analyze trends
import pandas as pd
import matplotlib.pyplot as plt

# Load historical data
df = pd.read_csv('benchmark_results.csv')
df['Timestamp'] = pd.to_datetime(df['Timestamp'])

# Plot trends
for benchmark in df['Benchmark'].unique():
    data = df[df['Benchmark'] == benchmark]
    plt.plot(data['Timestamp'], data['Average(ms)'], label=benchmark)

plt.xlabel('Date')
plt.ylabel('Average Time (ms)')
plt.title('Performance Trends Over Time')
plt.legend()
plt.show()
```

## Best Practices

### Writing Benchmarks

1. **Warm-up**: Always include warm-up runs
   ```java
   // Warm-up
   for (int i = 0; i < 10; i++) {
       operation();
   }

   // Actual benchmark
   for (int i = 0; i < 100; i++) {
       benchmark.runBenchmark("Test", 1, () -> {
           operation();
           return null;
       });
   }
   ```

2. **GC Management**: Force GC between iterations
   ```java
   System.gc();
   Thread.sleep(50); // Let GC complete
   ```

3. **Statistical Significance**: Run enough iterations
   ```java
   // Too few - unreliable
   runBenchmark("Test", 5, test);

   // Good - statistically significant
   runBenchmark("Test", 100, test);
   ```

4. **Isolate What You're Testing**
   ```java
   // BAD - tests both array creation AND operations
   runBenchmark("Test", 100, () -> {
       ArrayList<Integer> list = new ArrayList<>();
       list.add(1);
       return null;
   });

   // GOOD - only tests operations
   ArrayList<Integer> list = new ArrayList<>();
   runBenchmark("Test", 100, () -> {
       list.add(1);
       return null;
   });
   ```

### Interpreting Results

**High Standard Deviation** (>20% of average)
- Indicates inconsistent performance
- May need more iterations
- Could indicate GC interference
- System load variations

**High Memory Usage**
- Check for object creation in loop
- Look for memory leaks
- Consider object pooling

**Slower Than Expected**
- Compare with baseline
- Check for algorithm changes
- Look for blocking operations
- Verify no debug code

## Regression Detection

### Automated Thresholds

```java
@Test
public void testNoRegression() {
    BenchmarkResult result = benchmark.runBenchmark("Test", 100, test);

    // Load historical baseline
    double baseline = loadBaseline("Test");

    // Assert no more than 10% regression
    assertTrue("Performance regression detected",
        result.average < baseline * 1.1);
}
```

### Manual Review

1. Run benchmarks before changes
2. Make changes
3. Run benchmarks after changes
4. Compare results
5. Investigate any regression >10%

## Troubleshooting

### Benchmark Too Variable

```java
// Increase iterations
runBenchmark("Test", 1000, test); // Was 100

// Ensure GC between runs
System.gc();
Thread.sleep(100);

// Check system load
Runtime.getRuntime().availableProcessors();
```

### Benchmark Too Slow

```java
// Reduce problem size
for (int i = 0; i < 100; i++) { // Was 10000
    // ...
}

// Or reduce iterations
runBenchmark("Test", 10, test); // Was 100
```

### Memory Measurement Inaccurate

```java
// Force GC before and after
System.gc();
Thread.sleep(100);
long before = getUsedMemory();
operation();
System.gc();
Thread.sleep(100);
long after = getUsedMemory();
```

## Advanced Topics

### Custom Metrics

```java
public class CustomBenchmark extends PerformanceBenchmark {
    public void runWithCustomMetrics() {
        long cpuBefore = getCpuTime();
        runBenchmark("Test", 100, test);
        long cpuAfter = getCpuTime();

        System.out.println("CPU Time: " + (cpuAfter - cpuBefore));
    }
}
```

### Profiler Integration

```bash
# Run with Java Flight Recorder
java -XX:+UnlockCommercialFeatures \
     -XX:+FlightRecorder \
     -XX:StartFlightRecording=duration=60s,filename=recording.jfr \
     -cp vtea.jar vtea.util.PerformanceBenchmark
```

### Comparing Different Implementations

```java
BenchmarkResult implementation1 = benchmark.runBenchmark(
    "Implementation 1", 100, () -> { method1(); return null; }
);

BenchmarkResult implementation2 = benchmark.runBenchmark(
    "Implementation 2", 100, () -> { method2(); return null; }
);

if (implementation2.average < implementation1.average * 0.9) {
    System.out.println("Implementation 2 is >10% faster!");
}
```

## References

- [Java Microbenchmark Harness (JMH)](https://openjdk.java.net/projects/code-tools/jmh/)
- [Performance Testing Best Practices](https://martinfowler.com/articles/performance-testing.html)
- [Java Performance Tuning](https://www.oracle.com/technical-resources/articles/java/performance-tuning.html)

---

*Last Updated: 2025-10-26*
*Author: Claude Code*
