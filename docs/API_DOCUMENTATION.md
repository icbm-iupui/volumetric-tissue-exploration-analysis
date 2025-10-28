# VTEA 2.0 API Documentation

Developer guide for extending VTEA and integrating volumetric analysis into custom workflows.

**Audience**: Java developers, plugin creators, researchers with programming experience

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Core Interfaces](#core-interfaces)
3. [Creating Custom Segmentation Methods](#creating-custom-segmentation-methods)
4. [Working with VolumeDataset](#working-with-volumedataset)
5. [Chunk Processing](#chunk-processing)
6. [Object Stitching](#object-stitching)
7. [Deep Learning Integration](#deep-learning-integration)
8. [Examples](#examples)
9. [Best Practices](#best-practices)

---

## Architecture Overview

### Package Structure

```
vtea/
├── dataset/
│   └── volume/                    # Volume data abstractions
│       ├── VolumeDataset          # Base interface
│       ├── ChunkedVolumeDataset   # Chunked processing interface
│       ├── ImagePlusVolumeDataset # Traditional ImagePlus wrapper
│       └── ZarrVolumeDataset      # Zarr format implementation
│
├── partition/                     # Volume partitioning system
│   ├── Chunk                      # Chunk metadata and data
│   ├── VolumePartitioner          # Chunking strategies
│   ├── ChunkIterator              # Sequential chunk access
│   └── ObjectStitcher             # Boundary object merging
│
├── objects/
│   └── Segmentation/              # Segmentation methods
│       ├── Segmentation           # Plugin interface
│       ├── AbstractSegmentation   # Base implementation
│       ├── AbstractChunkedSegmentation  # Chunked base class
│       └── Chunked*               # Specific implementations
│
├── deeplearning/                  # Deep learning integration
│   ├── CellposeInterface          # Python-Java bridge
│   ├── CellposeParams             # Configuration
│   ├── CellposeModel              # Model enumeration
│   └── DeepLearningException      # Error handling
│
└── io/
    └── zarr/                      # Zarr I/O
        ├── ZarrReader             # Read Zarr volumes
        └── ZarrWriter             # Write Zarr volumes
```

### Key Concepts

1. **VolumeDataset**: Abstraction for image data (ImagePlus or Zarr)
2. **Chunking**: Divide large volumes into manageable pieces
3. **Processing**: Apply algorithms to chunks independently
4. **Stitching**: Merge objects across chunk boundaries
5. **Plugin System**: SciJava annotations for extensibility

---

## Core Interfaces

### VolumeDataset

Base interface for all volume data sources.

```java
public interface VolumeDataset {
    // Dimension information
    long[] getDimensions();
    boolean isChunked();
    int[] getChunkDimensions();

    // Data access
    double getVoxel(long x, long y, long z, int channel);
    ImageStack getSubVolume(long xStart, long yStart, long zStart,
                           long width, long height, long depth, int channel);
    ImagePlus getImagePlus();
    <T extends RealType<T> & NativeType<T>> RandomAccessibleInterval<T> getImgLib2();

    // Memory management
    boolean fitsInMemory();
    void close();
}
```

**Usage Example**:
```java
// Open a volume
VolumeDataset dataset = new ZarrVolumeDataset("/path/to/volume.zarr", "data");

// Check dimensions
long[] dims = dataset.getDimensions();
System.out.println("Volume size: " + dims[0] + "x" + dims[1] + "x" + dims[2]);

// Check if chunking needed
if (!dataset.fitsInMemory()) {
    System.out.println("Using chunked processing");
}

// Clean up
dataset.close();
```

### ChunkedVolumeDataset

Extended interface for partitioned datasets.

```java
public interface ChunkedVolumeDataset extends VolumeDataset {
    // Chunk information
    int[] getNumChunks();
    int getTotalChunkCount();

    // Chunk access
    Chunk getChunk(int chunkIndex, int channel);
    Iterator<Chunk> getChunkIterator(int channel);
    List<Chunk> getIntersectingChunks(long xStart, long yStart, long zStart,
                                     long width, long height, long depth, int channel);

    // Cache management
    void clearCache();
    long getCacheSize();
}
```

**Usage Example**:
```java
ChunkedVolumeDataset zarr = new ZarrVolumeDataset(path, dataset, cacheSize);

// Iterate through chunks
Iterator<Chunk> it = zarr.getChunkIterator(0);  // Channel 0
while (it.hasNext()) {
    Chunk chunk = it.next();
    processChunk(chunk);
    chunk.clearData();  // Free memory
}
```

### Segmentation (Plugin Interface)

```java
@Plugin(type = Segmentation.class)
public interface Segmentation {
    // Processing
    boolean process(ImageStack[] stacks, List details, boolean calculate);
    boolean process(ImagePlus imp, List details, boolean calculate);

    // Results
    ArrayList<MicroObject> getObjects();
    ImagePlus getSegmentation();

    // Metadata
    String getNAME();
    String getKEY();
    String getVERSION();

    // UI
    Component getUI();
    ArrayList getSegmentationToolOptions();
}
```

---

## Creating Custom Segmentation Methods

### Option 1: Traditional (Non-Chunked) Method

Extend `AbstractSegmentation` for simple, in-memory algorithms.

```java
@Plugin(type = Segmentation.class)
public class MySimpleSegmentation extends AbstractSegmentation {

    private ArrayList<MicroObject> results;

    public MySimpleSegmentation() {
        NAME = "My Simple Segmentation";
        KEY = "MYSIMPLESEGMENTATION";
        VERSION = "1.0";
        AUTHOR = "Your Name";
        COMMENT = "Description of your method";

        // Define UI components
        protocol = new ArrayList();
        protocol.add(new JLabel("Threshold:"));
        protocol.add(new JTextField("127", 5));
    }

    @Override
    public boolean process(ImageStack[] stacks, List details, boolean calculate) {
        results = new ArrayList<>();

        // Extract parameters
        int channel = (Integer) details.get(2);
        ArrayList params = (ArrayList) details.get(3);
        int threshold = Integer.parseInt(((JTextField) params.get(1)).getText());

        // Your segmentation algorithm here
        ImageStack stack = stacks[channel];
        // ... process stack ...
        // ... create MicroObjects ...
        // results.add(microObject);

        return true;
    }

    @Override
    public ArrayList<MicroObject> getObjects() {
        return results;
    }

    // Implement other required methods...
}
```

### Option 2: Chunked Method (Recommended for Large Volumes)

Extend `AbstractChunkedSegmentation` for automatic chunking support.

```java
@Plugin(type = Segmentation.class)
public class MyChunkedSegmentation extends AbstractChunkedSegmentation<Component, Object> {

    private int threshold = 127;

    public MyChunkedSegmentation() {
        super();
        NAME = "My Chunked Segmentation";
        KEY = "MYCHUNKEDSEGMENTATION";
        VERSION = "1.0";
    }

    @Override
    protected List<MicroObject> processChunk(Chunk chunk, List details, boolean calculate) {
        List<MicroObject> chunkObjects = new ArrayList<>();

        // Get chunk data
        ImageStack chunkStack = chunk.getData();
        if (chunkStack == null) return chunkObjects;

        // Process chunk (your algorithm here)
        // ... segment chunkStack ...

        // Transform coordinates to global space
        transformToGlobalCoordinates(chunkObjects, chunk);

        // Tag boundary objects
        tagBoundaryObjects(chunkObjects, chunk);

        return chunkObjects;
    }

    @Override
    protected List<MicroObject> stitchChunks(List<MicroObject> chunkObjects,
                                            ChunkedVolumeDataset dataset) {
        // Configure stitcher for your method
        ObjectStitcher stitcher = new ObjectStitcher();
        stitcher.setDistanceThreshold(10.0);
        stitcher.setOverlapThreshold(0.10);
        stitcher.setUseIntensityCorrelation(true);

        return stitcher.stitchObjects(chunkObjects);
    }

    @Override
    protected boolean processTraditional(ImageStack[] stacks, List details, boolean calculate) {
        // Fallback for non-chunked data
        // Process entire stack at once
        return true;
    }

    // Helper methods
    private void transformToGlobalCoordinates(List<MicroObject> objects, Chunk chunk) {
        long[] globalStart = chunk.getGlobalStart();
        for (MicroObject obj : objects) {
            int[] pixelsX = obj.getPixelsX();
            for (int i = 0; i < pixelsX.length; i++) {
                pixelsX[i] += (int) globalStart[0];
                // Repeat for Y and Z
            }
        }
    }

    private void tagBoundaryObjects(List<MicroObject> objects, Chunk chunk) {
        for (MicroObject obj : objects) {
            boolean isBoundary = isNearChunkBoundary(obj, chunk);
            obj.setProperty("chunkBoundary", isBoundary);
        }
    }
}
```

### Required Method Implementations

```java
// Get UI components
@Override
public Component getUI() {
    JPanel panel = new JPanel();
    // Add your UI components
    return panel;
}

// Progress reporting
@Override
public String getProgressDetail() {
    if (useChunkedProcessing) {
        return getChunkProcessingStats();
    }
    return "Processing...";
}

// Parameter setting
@Override
public void setParameters(List<Object> parameters) {
    // Extract and store parameters
}
```

---

## Working with VolumeDataset

### Opening Volumes

```java
// ImagePlus (traditional)
ImagePlus imp = IJ.openImage("/path/to/image.tif");
VolumeDataset dataset = new ImagePlusVolumeDataset(imp);

// Zarr
VolumeDataset dataset = new ZarrVolumeDataset(
    "/path/to/volume.zarr",
    "data",                      // Dataset name
    2L * 1024 * 1024 * 1024      // 2GB cache
);

// Check if chunking will be used
if (dataset.isChunked()) {
    ChunkedVolumeDataset chunked = (ChunkedVolumeDataset) dataset;
    System.out.println("Chunks: " + chunked.getTotalChunkCount());
}
```

### Reading Data

```java
// Single voxel
double value = dataset.getVoxel(100, 200, 50, 0);  // x, y, z, channel

// Sub-volume
ImageStack subVolume = dataset.getSubVolume(
    0, 0, 0,          // Start position
    512, 512, 32,     // Size
    0                 // Channel
);

// Full volume (if fits in memory)
if (dataset.fitsInMemory()) {
    ImagePlus imp = dataset.getImagePlus();
}
```

### Converting TIFF to Zarr

```java
import vtea.utilities.conversion.TiffToZarrConverter;

TiffToZarrConverter converter = new TiffToZarrConverter();
boolean success = converter.convert(
    "/path/to/input.tif",
    "/path/to/output.zarr",
    "volume",                    // Dataset name
    new int[]{512, 512, 32},    // Chunk size
    "blosc"                      // Compression: blosc, gzip, or none
);
```

---

## Chunk Processing

### Chunk Structure

```java
public class Chunk {
    int getChunkId();
    int[] getGridPosition();        // [chunkX, chunkY, chunkZ]
    long[] getGlobalStart();        // Global coordinates
    long[] getGlobalEnd();
    int[] getOverlapStart();        // Overlap region
    int[] getOverlapEnd();
    ImageStack getData();
    void setData(ImageStack data);
    void clearData();               // Free memory
    boolean containsInCore(long x, long y, long z);  // Check if in core region
}
```

### Using VolumePartitioner

```java
VolumePartitioner partitioner = new VolumePartitioner();

// Configure chunking
partitioner.setChunkSize(512, 512, 32);           // X, Y, Z
partitioner.setOverlapPercent(0.15);               // 15% overlap
partitioner.setStrategy(PartitioningStrategy.FIXED_SIZE);

// Calculate chunks for a volume
long[] dimensions = new long[]{2048, 2048, 128};
List<Chunk> chunks = partitioner.partition(dimensions, 0);  // Channel 0

// Generate specific chunk
Chunk chunk = partitioner.generateChunk(0, 0, 0, 0, chunkId);
```

### Chunk Iteration

```java
ChunkedVolumeDataset dataset = new ZarrVolumeDataset(path, "data");

// Method 1: Iterator
Iterator<Chunk> iterator = dataset.getChunkIterator(0);
while (iterator.hasNext()) {
    Chunk chunk = iterator.next();

    // Process chunk
    List<MicroObject> objects = processChunk(chunk);

    // Clean up
    chunk.clearData();
}

// Method 2: Manual indexing
int totalChunks = dataset.getTotalChunkCount();
for (int i = 0; i < totalChunks; i++) {
    Chunk chunk = dataset.getChunk(i, 0);
    processChunk(chunk);
    chunk.clearData();
}

// Method 3: Parallel processing
ForkJoinPool pool = new ForkJoinPool();
pool.submit(() -> {
    IntStream.range(0, totalChunks).parallel().forEach(i -> {
        Chunk chunk = dataset.getChunk(i, 0);
        processChunk(chunk);
        chunk.clearData();
    });
}).join();
```

---

## Object Stitching

### ObjectStitcher Configuration

```java
ObjectStitcher stitcher = new ObjectStitcher();

// Distance-based merging
stitcher.setDistanceThreshold(10.0);  // Maximum centroid distance

// Overlap-based merging
stitcher.setOverlapThreshold(0.10);   // Minimum 10% overlap

// Intensity correlation
stitcher.setUseIntensityCorrelation(true);  // Check intensity similarity

// Perform stitching
List<MicroObject> stitched = stitcher.stitchObjects(chunkObjects);

// Get statistics
String stats = stitcher.getStitchingStats(
    chunkObjects.size(),
    stitched.size()
);
System.out.println(stats);
```

### Custom Stitching Logic

```java
public class MyCustomStitcher {

    public List<MicroObject> stitchObjects(List<MicroObject> objects) {
        // Build spatial index
        KDTree<MicroObject> kdTree = buildKDTree(objects);

        // Find merge candidates
        Set<Set<MicroObject>> mergeGroups = new HashSet<>();
        for (MicroObject obj1 : objects) {
            if (!isBoundaryObject(obj1)) continue;

            // Find nearby objects
            double[] centroid = obj1.getCentroidXYZ_AsDbl();
            Neighbor<double[], MicroObject>[] neighbors =
                kdTree.range(centroid, distanceThreshold);

            // Determine which to merge
            Set<MicroObject> group = new HashSet<>();
            group.add(obj1);
            for (Neighbor<double[], MicroObject> n : neighbors) {
                if (shouldMerge(obj1, n.value)) {
                    group.add(n.value);
                }
            }
            if (group.size() > 1) {
                mergeGroups.add(group);
            }
        }

        // Perform merging
        return mergeObjectGroups(mergeGroups);
    }

    private boolean shouldMerge(MicroObject obj1, MicroObject obj2) {
        // Check overlap
        double overlap = calculateOverlap(obj1, obj2);
        if (overlap < overlapThreshold) return false;

        // Check intensity correlation
        if (useIntensityCorrelation) {
            double correlation = calculateIntensityCorrelation(obj1, obj2);
            if (correlation < 0.7) return false;
        }

        return true;
    }
}
```

---

## Deep Learning Integration

### Py4J Bridge Pattern

```java
public class MyDeepLearningInterface implements AutoCloseable {

    private Process pythonProcess;
    private GatewayServer gatewayServer;
    private Object pythonEntryPoint;

    public void startServer() throws Exception {
        // Start Python process
        ProcessBuilder pb = new ProcessBuilder(
            "python3",
            "my_server.py",
            "--port", "25333"
        );
        pythonProcess = pb.start();

        // Wait for ready
        Thread.sleep(2000);

        // Connect Py4J gateway
        gatewayServer = new GatewayServer(null, 25333);
        gatewayServer.start();
        pythonEntryPoint = gatewayServer.getPythonServerEntryPoint(new Class[0]);
    }

    public int[][] segment2D(ImageProcessor ip, String paramsJSON) throws Exception {
        // Convert image to bytes
        byte[] imageBytes = imageToBytes(ip);

        // Call Python method
        Method method = pythonEntryPoint.getClass().getMethod(
            "segment_2d",
            byte[].class, int.class, int.class, String.class
        );

        byte[] resultBytes = (byte[]) method.invoke(
            pythonEntryPoint,
            imageBytes, ip.getWidth(), ip.getHeight(), paramsJSON
        );

        // Convert back to int array
        return bytesToIntArray(resultBytes, ip.getWidth(), ip.getHeight());
    }

    @Override
    public void close() {
        if (gatewayServer != null) {
            gatewayServer.shutdown();
        }
        if (pythonProcess != null) {
            pythonProcess.destroy();
        }
    }
}
```

### Python Server Example

```python
# my_server.py
from py4j.java_gateway import JavaGateway, GatewayParameters
import numpy as np

class MyModelServer:
    def __init__(self):
        self.model = self.load_model()

    def segment_2d(self, image_bytes, width, height, params_json):
        # Deserialize image
        image = np.frombuffer(image_bytes, dtype=np.float32)
        image = image.reshape((height, width))

        # Run model
        result = self.model.predict(image)

        # Serialize result
        return result.astype(np.int32).tobytes()

    def health_check(self):
        return "OK"

if __name__ == "__main__":
    server = MyModelServer()
    gateway = JavaGateway(
        gateway_parameters=GatewayParameters(port=25333),
        python_server_entry_point=server
    )
    print("Server started")
```

---

## Examples

### Example 1: Simple Threshold Segmentation

```java
@Plugin(type = Segmentation.class)
public class SimpleThresholdExample extends AbstractChunkedSegmentation<Component, Object> {

    private int threshold = 127;

    @Override
    protected List<MicroObject> processChunk(Chunk chunk, List details, boolean calculate) {
        List<MicroObject> objects = new ArrayList<>();
        ImageStack stack = chunk.getData();

        // Collect pixels above threshold
        List<int[]> pixels = new ArrayList<>();
        for (int z = 0; z < stack.getSize(); z++) {
            for (int y = 0; y < stack.getHeight(); y++) {
                for (int x = 0; x < stack.getWidth(); x++) {
                    if (stack.getVoxel(x, y, z) >= threshold) {
                        pixels.add(new int[]{x, y, z});
                    }
                }
            }
        }

        // Create MicroObject
        if (!pixels.isEmpty()) {
            int[][] coords = pixelsToArrays(pixels);
            MicroObject obj = new MicroObject(
                coords[0], coords[1], coords[2],
                0, new ImageStack[]{stack}, chunk.getChunkId()
            );
            transformToGlobalCoordinates(Collections.singletonList(obj), chunk);
            objects.add(obj);
        }

        return objects;
    }

    @Override
    protected List<MicroObject> stitchChunks(List<MicroObject> chunkObjects,
                                            ChunkedVolumeDataset dataset) {
        ObjectStitcher stitcher = new ObjectStitcher();
        stitcher.setDistanceThreshold(Double.MAX_VALUE);  // Merge all
        return stitcher.stitchObjects(chunkObjects);
    }
}
```

### Example 2: Connected Components

```java
public class ConnectedComponentsExample {

    public List<MicroObject> findConnectedComponents(ImageStack stack, int threshold) {
        List<MicroObject> objects = new ArrayList<>();

        // Create binary mask
        boolean[][][] visited = new boolean[stack.getWidth()][stack.getHeight()][stack.getSize()];

        int objectId = 0;
        for (int z = 0; z < stack.getSize(); z++) {
            for (int y = 0; y < stack.getHeight(); y++) {
                for (int x = 0; x < stack.getWidth(); x++) {
                    if (stack.getVoxel(x, y, z) >= threshold && !visited[x][y][z]) {
                        // Found new object, flood fill
                        List<int[]> pixels = new ArrayList<>();
                        floodFill3D(stack, visited, x, y, z, threshold, pixels);

                        if (pixels.size() >= minSize) {
                            int[][] coords = pixelsToArrays(pixels);
                            MicroObject obj = new MicroObject(
                                coords[0], coords[1], coords[2],
                                0, new ImageStack[]{stack}, objectId++
                            );
                            objects.add(obj);
                        }
                    }
                }
            }
        }

        return objects;
    }

    private void floodFill3D(ImageStack stack, boolean[][][] visited,
                            int x, int y, int z, int threshold,
                            List<int[]> pixels) {
        // Bounds check
        if (x < 0 || y < 0 || z < 0 ||
            x >= stack.getWidth() || y >= stack.getHeight() || z >= stack.getSize()) {
            return;
        }

        // Already visited or below threshold
        if (visited[x][y][z] || stack.getVoxel(x, y, z) < threshold) {
            return;
        }

        // Mark as visited
        visited[x][y][z] = true;
        pixels.add(new int[]{x, y, z});

        // Recurse to neighbors (6-connectivity)
        floodFill3D(stack, visited, x+1, y, z, threshold, pixels);
        floodFill3D(stack, visited, x-1, y, z, threshold, pixels);
        floodFill3D(stack, visited, x, y+1, z, threshold, pixels);
        floodFill3D(stack, visited, x, y-1, z, threshold, pixels);
        floodFill3D(stack, visited, x, y, z+1, threshold, pixels);
        floodFill3D(stack, visited, x, y, z-1, threshold, pixels);
    }
}
```

### Example 3: Batch Processing

```java
public class BatchProcessor {

    public void processBatch(List<String> zarrPaths, Segmentation method) {
        for (String path : zarrPaths) {
            System.out.println("Processing: " + path);

            try (ZarrVolumeDataset dataset = new ZarrVolumeDataset(path, "data")) {
                // Run segmentation
                ImageStack[] stacks = new ImageStack[]{dataset.getImagePlus().getStack()};
                List details = createDefaultDetails();
                method.process(stacks, details, true);

                // Get results
                ArrayList<MicroObject> objects = method.getObjects();

                // Save results
                saveResults(path, objects);

            } catch (Exception e) {
                System.err.println("Failed: " + path);
                e.printStackTrace();
            }
        }
    }

    private void saveResults(String volumePath, List<MicroObject> objects) throws IOException {
        String csvPath = volumePath.replace(".zarr", "_results.csv");
        try (PrintWriter writer = new PrintWriter(csvPath)) {
            writer.println("ObjectID,Volume,CentroidX,CentroidY,CentroidZ,MeanIntensity");
            for (MicroObject obj : objects) {
                writer.printf("%d,%d,%.2f,%.2f,%.2f,%.2f%n",
                    obj.getSerialID(),
                    obj.getVoxelCount(),
                    obj.getCentroidXYZ_AsDbl()[0],
                    obj.getCentroidXYZ_AsDbl()[1],
                    obj.getCentroidXYZ_AsDbl()[2],
                    obj.getMeanIntensity()
                );
            }
        }
    }
}
```

---

## Best Practices

### Memory Management

```java
// Always close resources
try (ZarrVolumeDataset dataset = new ZarrVolumeDataset(path, "data")) {
    // Use dataset
} // Automatically closed

// Clear chunk data after processing
chunk.clearData();

// Clear cache periodically
if (dataset instanceof ChunkedVolumeDataset) {
    ((ChunkedVolumeDataset) dataset).clearCache();
}
```

### Error Handling

```java
@Override
protected List<MicroObject> processChunk(Chunk chunk, List details, boolean calculate) {
    try {
        ImageStack stack = chunk.getData();
        if (stack == null) {
            System.err.println("Chunk " + chunk.getChunkId() + " has no data");
            return new ArrayList<>();
        }

        // Process...

    } catch (OutOfMemoryError e) {
        System.err.println("Out of memory on chunk " + chunk.getChunkId());
        System.gc();
        return new ArrayList<>();
    } catch (Exception e) {
        System.err.println("Error processing chunk " + chunk.getChunkId() + ": " + e.getMessage());
        e.printStackTrace();
        return new ArrayList<>();
    }
}
```

### Progress Reporting

```java
@Override
public String getProgressDetail() {
    if (useChunkedProcessing) {
        int processed = chunksProcessed.get();
        int total = totalChunks;
        int percent = (int) ((processed / (double) total) * 100);
        return String.format("Processing chunks: %d/%d (%d%%)", processed, total, percent);
    }
    return "Processing...";
}
```

### Parameter Validation

```java
private void validateParameters() throws IllegalArgumentException {
    if (threshold < 0 || threshold > 65535) {
        throw new IllegalArgumentException("Threshold must be 0-65535");
    }
    if (minSize < 1) {
        throw new IllegalArgumentException("Min size must be > 0");
    }
    if (maxSize < minSize) {
        throw new IllegalArgumentException("Max size must be >= min size");
    }
}
```

### Thread Safety

```java
// Use thread-safe collections for concurrent processing
private List<MicroObject> results = Collections.synchronizedList(new ArrayList<>());

// Or use concurrent collections
private ConcurrentHashMap<Integer, List<MicroObject>> chunkResults = new ConcurrentHashMap<>();

// Protect shared state
private final Object lock = new Object();
public void addResult(MicroObject obj) {
    synchronized(lock) {
        results.add(obj);
    }
}
```

---

## Summary

### Quick Start Checklist

For creating a new segmentation method:

- [ ] Extend `AbstractChunkedSegmentation`
- [ ] Implement `processChunk()`
- [ ] Implement `stitchChunks()`
- [ ] Add `@Plugin(type = Segmentation.class)` annotation
- [ ] Set NAME, KEY, VERSION fields
- [ ] Implement `getUI()` for parameters
- [ ] Handle coordinate transformation
- [ ] Tag boundary objects
- [ ] Test with small and large volumes

### Common Patterns

**Chunked Processing**:
```java
processChunk() → transform coordinates → tag boundaries
stitchChunks() → configure ObjectStitcher → merge
```

**Traditional Processing**:
```java
process() → analyze full stack → create MicroObjects
```

**Deep Learning**:
```java
startServer() → send data → receive results → convert to MicroObjects
```

### Resources

- **Source Code**: Browse existing implementations in `vtea/objects/Segmentation/`
- **Examples**: See `Chunked*` classes for patterns
- **SciJava Docs**: https://javadoc.scijava.org/
- **ImageJ API**: https://javadoc.scijava.org/ImageJ1/

---

**Version**: 2.0.0
**Last Updated**: 2025-10-28
**Authors**: Seth Winfree, VTEA Development Team
**License**: GPL v2

For questions: https://github.com/your-org/vtea/issues
