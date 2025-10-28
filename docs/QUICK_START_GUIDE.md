# VTEA 2.0 Quick Start Guide

Welcome to VTEA 2.0! This guide will help you get started with volumetric tissue exploration and analysis, including support for large Zarr volumes.

**Estimated Time**: 15-30 minutes

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Installation](#installation)
3. [Your First Segmentation (Traditional)](#your-first-segmentation-traditional)
4. [Working with Large Zarr Volumes](#working-with-large-zarr-volumes)
5. [Using Deep Learning (Cellpose)](#using-deep-learning-cellpose)
6. [Understanding Results](#understanding-results)
7. [Next Steps](#next-steps)
8. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### System Requirements

**Minimum**:
- Java 8 or higher
- 8GB RAM
- 2GB free disk space

**Recommended for Large Volumes**:
- Java 11 or higher
- 16GB+ RAM
- SSD storage
- GPU (for Cellpose deep learning)

### Software Dependencies

- **Fiji/ImageJ** (recommended) or standalone VTEA
- **Maven** (for building from source)
- **Python 3.8+** (optional, for Cellpose)

---

## Installation

### Option 1: Using Fiji (Recommended)

1. **Download Fiji**:
   ```bash
   # Visit https://fiji.sc/ and download for your platform
   ```

2. **Install VTEA Plugin**:
   ```bash
   # Copy VTEA JAR to Fiji plugins folder
   cp vtea-2.0.0.jar /path/to/Fiji.app/plugins/
   ```

3. **Restart Fiji**:
   - VTEA should appear in Plugins menu

### Option 2: Building from Source

```bash
# Clone repository
git clone https://github.com/your-org/volumetric-tissue-exploration-analysis.git
cd volumetric-tissue-exploration-analysis

# Build with Maven
mvn clean package

# JAR will be in target/vtea-2.0.0.jar
```

### Verify Installation

1. Open Fiji/ImageJ
2. Look for **Plugins → VTEA** in the menu
3. You should see VTEA 2.0 options

---

## Your First Segmentation (Traditional)

Let's segment a simple 3D image using traditional methods.

### Step 1: Load Sample Data

```
File → Open Samples → Mitosis (5D stack)
```

Or use your own image:
```
File → Open → [select your TIFF/OME-TIFF file]
```

**Image Requirements**:
- Format: TIFF, OME-TIFF, or Zarr
- Type: 8-bit, 16-bit, or 32-bit
- Dimensions: Works with 2D, 3D, or higher

### Step 2: Launch VTEA

```
Plugins → VTEA → Start Analysis
```

### Step 3: Choose Segmentation Method

In the VTEA interface:

1. **Select Channel**: Choose the channel containing your objects
2. **Select Method**: Pick a segmentation method:

   **For Well-Separated Objects**:
   - `Chunked LayerCake 3D` - Fast, simple distance-based

   **For Touching Objects**:
   - `Chunked FloodFill 3D` - Good for dense regions

   **For High Quality**:
   - `Chunked LayerCake 3D kDTree` - Sophisticated spatial indexing

   **For Baseline/Testing**:
   - `Chunked Single Threshold 3D` - Creates one large region

### Step 4: Configure Parameters

Example for **Chunked LayerCake 3D**:

```
Threshold: 127           # Intensity threshold (0-255 for 8-bit)
Min Size: 10 voxels      # Filter small noise
Max Size: 10000 voxels   # Filter large artifacts
2D Mode: unchecked       # Use 3D connectivity
```

**Tips**:
- Start with default values
- Adjust threshold based on your image intensity
- Use 2D mode for slice-by-slice analysis

### Step 5: Run Segmentation

1. Click **"Segment"** button
2. Processing begins:
   - Progress bar shows completion
   - Console shows chunk processing (for large images)
   - Wait time depends on volume size

### Step 6: View Results

After segmentation completes:

1. **Segmentation Mask**: Binary mask of detected objects
2. **Object Overlay**: Color-coded objects on original image
3. **Results Table**: Object measurements (volume, centroid, intensity)

**Example Results Table**:
```
Object ID | Volume (voxels) | Centroid X | Centroid Y | Centroid Z | Mean Intensity
----------|-----------------|------------|------------|------------|---------------
1         | 1250            | 45.2       | 67.8       | 12.3       | 156.4
2         | 890             | 123.5      | 89.1       | 15.7       | 178.9
...
```

---

## Working with Large Zarr Volumes

VTEA 2.0 can process volumes too large to fit in RAM using the Zarr format.

### Step 1: Convert TIFF to Zarr (Optional)

If you have a large TIFF that won't fit in memory:

```java
// In VTEA or ImageJ macro:
import vtea.utilities.conversion.TiffToZarrConverter;

TiffToZarrConverter converter = new TiffToZarrConverter();
converter.convert(
    "/path/to/large_volume.tif",     // Input TIFF
    "/path/to/output.zarr",           // Output Zarr
    "volume",                         // Dataset name
    new int[]{512, 512, 32},         // Chunk size [X, Y, Z]
    "blosc"                          // Compression
);
```

**Recommended Chunk Sizes**:
- **Small volumes** (< 2GB): 256 × 256 × 16
- **Medium volumes** (2-10GB): 512 × 512 × 32
- **Large volumes** (> 10GB): 512 × 512 × 64

### Step 2: Load Zarr Volume

```java
import vtea.dataset.volume.ZarrVolumeDataset;

ZarrVolumeDataset dataset = new ZarrVolumeDataset(
    "/path/to/volume.zarr",
    "volume",              // Dataset name
    2 * 1024 * 1024 * 1024L  // 2GB cache size
);

// Check if chunked processing will be used
System.out.println("Chunked: " + dataset.isChunked());
System.out.println("Dimensions: " + Arrays.toString(dataset.getDimensions()));
```

### Step 3: Run Chunked Segmentation

The process is the same as traditional segmentation, but:

✅ **Automatic Chunk Detection**: VTEA detects if volume is Zarr and uses chunked processing
✅ **Memory Efficient**: Only loads one chunk at a time
✅ **Progress Tracking**: Shows chunk-by-chunk progress
✅ **Automatic Stitching**: Merges objects across chunk boundaries

**Console Output**:
```
Processing chunk 1/64 (position: [0, 0, 0])...
Found 45 objects in chunk
Processing chunk 2/64 (position: [512, 0, 0])...
Found 38 objects in chunk
...
Stitching 2,480 chunk objects...
Merged to 1,234 final objects
```

### Step 4: Adjust Chunk Processing Settings

For advanced users:

```java
// Configure partitioner
VolumePartitioner partitioner = new VolumePartitioner();
partitioner.setChunkSize(512, 512, 32);     // X, Y, Z
partitioner.setOverlapPercent(0.15);         // 15% overlap
partitioner.setStrategy(PartitioningStrategy.FIXED_SIZE);

// Configure stitcher (per method)
ObjectStitcher stitcher = new ObjectStitcher();
stitcher.setDistanceThreshold(10.0);         // Max distance to merge
stitcher.setOverlapThreshold(0.1);           // Min overlap (10%)
stitcher.setUseIntensityCorrelation(true);   // Check intensity similarity
```

---

## Using Deep Learning (Cellpose)

VTEA 2.0 includes state-of-the-art deep learning segmentation via Cellpose.

### Prerequisites

See [CELLPOSE_SETUP_GUIDE.md](CELLPOSE_SETUP_GUIDE.md) for detailed setup instructions.

**Quick Setup**:
```bash
# Install Python dependencies
pip install cellpose py4j torch

# Test installation
python python/cellpose_setup.py
```

### Step 1: Start Cellpose Server

**Option A: Automatic** (if configured):
- VTEA will auto-start Python server when needed

**Option B: Manual**:
```bash
cd volumetric-tissue-exploration-analysis
python python/cellpose_server.py --model cyto2 --gpu
```

You should see:
```
Cellpose server started successfully on port 25333
Model: cyto2, GPU: True
Press Ctrl+C to stop
```

### Step 2: Select Cellpose Method

In VTEA:
1. Choose **"Chunked Cellpose (Deep Learning)"**
2. Click **"Test Connection"** to verify Python server
3. Status should show: "Connected (Cellpose X.X.X)"

### Step 3: Configure Cellpose Parameters

```
Model: Cyto2                    # cyto2, nuclei, tissuenet, etc.
Diameter: 30 pixels             # Expected cell diameter (0 = auto)
Cell Probability: 0.0           # Lower = more cells (-6 to 6)
Flow Threshold: 0.4             # Higher = stricter (0.0 to 1.0)
Use 3D: checked                 # True 3D vs. 2D stitching
Use GPU: checked                # Use GPU acceleration
```

**Model Selection Guide**:
- **cyto2**: General cytoplasm (RECOMMENDED)
- **nuclei**: Nuclear segmentation
- **tissuenet**: Tissue sections
- **livecell**: Live cell imaging

### Step 4: Run Segmentation

1. Click **"Segment"**
2. Images sent to Python server
3. Cellpose processes each chunk
4. Results returned and stitched
5. Final objects displayed

**Performance**:
- GPU: ~1-5 seconds per chunk
- CPU: ~10-30 seconds per chunk
- Quality: State-of-the-art accuracy

---

## Understanding Results

### Object Table Columns

| Column | Description | Units |
|--------|-------------|-------|
| Object ID | Unique identifier | - |
| Volume | Number of voxels | voxels |
| Centroid X/Y/Z | Center of mass | pixels |
| Mean Intensity | Average intensity | intensity units |
| Min/Max Intensity | Intensity range | intensity units |
| Surface Area | Object surface | voxels |
| Sphericity | Shape measure (1 = perfect sphere) | 0-1 |

### Visualization Options

**3D Visualization**:
```
Plugins → VTEA → 3D Viewer
```
- Rotate, zoom, pan objects
- Color by property
- Toggle individual objects

**Overlay on Original**:
```
Plugins → VTEA → Show Overlay
```
- Objects outlined on original image
- Step through Z-slices
- Verify segmentation quality

### Exporting Results

**Export Object Table**:
```
File → Save As → CSV
```

**Export Segmentation Mask**:
```
File → Save As → TIFF
```
- Label mask (each object has unique integer ID)
- Compatible with other analysis tools

**Export to Zarr**:
```java
ZarrWriter writer = new ZarrWriter("/path/to/output.zarr");
writer.writeSegmentationMask(labelMask, "segmentation");
```

---

## Next Steps

### Learn More

- **[Parameter Tuning Guide](PARAMETER_TUNING_GUIDE.md)**: Optimize segmentation quality
- **[Cellpose Setup Guide](CELLPOSE_SETUP_GUIDE.md)**: Deep learning configuration
- **[API Documentation](API_DOCUMENTATION.md)**: Extend VTEA with custom methods

### Advanced Topics

1. **Batch Processing**: Segment multiple volumes
2. **Custom Segmentation**: Develop your own methods
3. **Integration**: Use VTEA in Python/MATLAB workflows
4. **Cloud Computing**: Process on AWS/GCP

### Example Workflows

**Workflow 1: High-Throughput Cell Counting**
```
1. Convert TIFF batch to Zarr
2. Use ChunkedLayerCake3D (fast)
3. Export count statistics
4. Batch process overnight
```

**Workflow 2: High-Accuracy Nuclear Segmentation**
```
1. Load Zarr volume
2. Use Cellpose (nuclei model)
3. 3D mode with GPU
4. Export for downstream tracking
```

**Workflow 3: Quality Control**
```
1. Run ChunkedFloodFill3D
2. Visual inspection in 3D viewer
3. Adjust parameters
4. Re-run until satisfied
```

---

## Troubleshooting

### Common Issues

#### "Out of Memory" Error

**Problem**: Java heap space exceeded

**Solutions**:
1. Increase Java memory:
   ```bash
   # In Fiji.app/ImageJ.cfg, set:
   -Xmx16G  # 16GB heap
   ```

2. Use Zarr format instead of TIFF
3. Reduce chunk size
4. Close other applications

#### No Objects Found

**Problem**: Segmentation returns 0 objects

**Solutions**:
1. **Lower threshold**: Try threshold = 50-100 instead of 127
2. **Check channel**: Verify correct channel selected
3. **Check image type**: Ensure 8-bit or 16-bit image
4. **Reduce min size**: Lower minimum object size filter

#### Cellpose Connection Failed

**Problem**: "Status: Not connected"

**Solutions**:
1. **Start server manually**:
   ```bash
   python python/cellpose_server.py
   ```

2. **Check Python**:
   ```bash
   python --version  # Should be 3.8+
   pip list | grep cellpose  # Should show cellpose>=2.2
   ```

3. **Check firewall**: Ensure port 25333 not blocked

4. **See full guide**: [CELLPOSE_SETUP_GUIDE.md](CELLPOSE_SETUP_GUIDE.md)

#### Segmentation Takes Forever

**Problem**: Processing very slow

**Solutions**:
1. **Use GPU** (for Cellpose): Enable GPU acceleration
2. **Increase chunk size**: Larger chunks = fewer stitching operations
3. **Use simpler method**: LayerCake3D faster than kDTree variant
4. **Reduce resolution**: Downsample if appropriate

#### Objects Split Across Chunks

**Problem**: One object becomes multiple

**Solutions**:
1. **Increase overlap**: Set overlap to 20%
2. **Increase distance threshold**: Allow larger merge distance
3. **Lower overlap threshold**: Require less overlap to merge
4. **Use larger chunks**: Fewer chunk boundaries

### Getting Help

**Documentation**:
- This guide: Quick start
- [PARAMETER_TUNING_GUIDE.md](PARAMETER_TUNING_GUIDE.md): Detailed tuning
- [CELLPOSE_SETUP_GUIDE.md](CELLPOSE_SETUP_GUIDE.md): Deep learning setup

**Community**:
- GitHub Issues: https://github.com/your-org/vtea/issues
- Forum: https://forum.image.sc/tag/vtea

**Reporting Bugs**:
Please include:
1. VTEA version (2.0.0)
2. Java version (`java -version`)
3. Operating system
4. Steps to reproduce
5. Error messages (from console)
6. Sample data (if possible)

---

## Summary Checklist

After completing this guide, you should be able to:

- [x] Install VTEA 2.0
- [x] Load images (TIFF or Zarr)
- [x] Run basic segmentation
- [x] Configure parameters
- [x] Process large Zarr volumes
- [x] Use Cellpose deep learning (if set up)
- [x] View and export results
- [x] Troubleshoot common issues

**Congratulations!** You're ready to use VTEA 2.0 for volumetric tissue analysis.

---

## Quick Reference Card

### Segmentation Method Selection

| Use Case | Recommended Method | Settings |
|----------|-------------------|----------|
| Well-separated cells | ChunkedLayerCake3D | threshold=127, minSize=10 |
| Touching/dense cells | ChunkedFloodFill3D | threshold=127, watershed=true |
| High accuracy | ChunkedCellpose | model=cyto2, diameter=30 |
| Fast baseline | ChunkedSingleThreshold | threshold=127 |
| Nuclei | ChunkedCellpose | model=nuclei, diameter=17 |

### Default Parameters

```
Threshold: 127 (8-bit), 2048 (12-bit), 32768 (16-bit)
Min Size: 10 voxels
Max Size: 100000 voxels
Overlap: 10-15%
Distance: 5-10 pixels
Chunk Size: 512 × 512 × 32
```

### Keyboard Shortcuts

```
Ctrl+O: Open image
Ctrl+S: Save results
Ctrl+R: Run segmentation
Ctrl+V: View 3D
Esc: Cancel processing
```

---

**Version**: 2.0.0
**Last Updated**: 2025-10-28
**Authors**: Seth Winfree, VTEA Team
**License**: GPL v2
