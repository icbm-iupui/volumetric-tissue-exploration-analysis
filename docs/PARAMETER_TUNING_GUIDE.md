# VTEA 2.0 Parameter Tuning Guide

Complete guide to optimizing segmentation parameters for high-quality results.

---

## Table of Contents

1. [Overview](#overview)
2. [Method Selection Flowchart](#method-selection-flowchart)
3. [Universal Parameters](#universal-parameters)
4. [Method-Specific Tuning](#method-specific-tuning)
5. [Chunked Processing Parameters](#chunked-processing-parameters)
6. [Quality Assessment](#quality-assessment)
7. [Common Issues & Solutions](#common-issues--solutions)
8. [Best Practices by Use Case](#best-practices-by-use-case)

---

## Overview

### The Tuning Process

```
1. Select Method → 2. Set Threshold → 3. Filter by Size → 4. Refine → 5. Validate
```

**Typical tuning session**: 15-30 minutes
**Goal**: Balance precision (no false positives) and recall (no missed objects)

### Quick Reference

| Parameter | Typical Range | Effect |
|-----------|---------------|--------|
| Threshold | 50-200 (8-bit) | Lower = more objects |
| Min Size | 10-100 voxels | Filter noise |
| Max Size | 1000-100000 voxels | Filter artifacts |
| Overlap | 10-20% | Chunk stitching quality |
| Distance | 5-15 pixels | Merge distance |

---

## Method Selection Flowchart

```
START: What type of objects do you have?

├─ Well-separated cells/nuclei?
│  ├─ YES → ChunkedLayerCake3D (fast, simple)
│  └─ NO → Continue
│
├─ Touching/clustered cells?
│  ├─ YES → ChunkedFloodFill3D (with watershed)
│  └─ NO → Continue
│
├─ Need highest accuracy?
│  ├─ YES → ChunkedCellpose (deep learning)
│  └─ NO → Continue
│
├─ Complex shapes, high density?
│  ├─ YES → ChunkedLayerCake3DkDTree (spatial indexing)
│  └─ NO → Continue
│
└─ Just need threshold region?
   └─ YES → ChunkedSingleThreshold (baseline)
```

### Method Comparison Table

| Method | Speed | Accuracy | Best For | Avoids |
|--------|-------|----------|----------|--------|
| ChunkedLayerCake3D | ⚡⚡⚡⚡ | ⭐⭐⭐ | Well-separated | Touching cells |
| ChunkedLayerCake3DkDTree | ⚡⚡⚡ | ⭐⭐⭐⭐ | Complex shapes | Simple cases |
| ChunkedFloodFill3D | ⚡⚡⚡ | ⭐⭐⭐⭐ | Dense/touching | Over-merging |
| ChunkedCellpose | ⚡⚡ (GPU) | ⭐⭐⭐⭐⭐ | High accuracy | Speed-critical |
| ChunkedSingleThreshold | ⚡⚡⚡⚡⚡ | ⭐ | Continuous regions | Individual objects |

---

## Universal Parameters

These parameters apply to all segmentation methods.

### Threshold

**Purpose**: Separate foreground (objects) from background

**How to set**:
1. **Visual inspection**: Look at image histogram
2. **Start mid-range**: 127 for 8-bit, 2048 for 12-bit
3. **Adjust iteratively**: Lower to find more, higher to filter noise

**Image bit-depth guide**:
```
8-bit (0-255):        Start at 127
12-bit (0-4095):      Start at 2048
16-bit (0-65535):     Start at 32768
```

**Examples**:

```
DAPI nuclei (8-bit):       threshold = 80-120
GFP cells (8-bit):         threshold = 60-100
Brightfield (8-bit):       threshold = 100-150
High-contrast (16-bit):    threshold = 10000-20000
Low-contrast (16-bit):     threshold = 5000-10000
```

**Visual guide**:
- **Too low**: Background noise becomes objects
- **Too high**: Dim objects disappear
- **Just right**: Clear objects, minimal noise

### Min Size (Minimum Object Size)

**Purpose**: Filter out noise and debris

**How to set**:
1. **Estimate smallest real object**: Count voxels in smallest cell
2. **Add safety margin**: Set 50-70% of smallest object
3. **Check results**: Adjust if real objects filtered

**Typical values**:
```
Noise/debris filtering:    minSize = 5-10 voxels
Small objects (bacteria):  minSize = 10-20 voxels
Medium objects (nuclei):   minSize = 50-100 voxels
Large objects (cells):     minSize = 200-500 voxels
```

**Example calculation**:
```
Smallest nucleus: ~15 pixels diameter
Volume = (4/3)π(7.5)³ ≈ 1767 voxels
Set minSize = 1000 (safety margin)
```

### Max Size (Maximum Object Size)

**Purpose**: Filter out imaging artifacts and merged clumps

**How to set**:
1. **Estimate largest real object**: Measure biggest expected cell
2. **Add buffer**: 2-3x largest expected
3. **Filter artifacts**: Catches debris aggregates

**Typical values**:
```
Nuclei:           maxSize = 5000-10000 voxels
Cells:            maxSize = 50000-100000 voxels
Large structures: maxSize = 500000-1000000 voxels
No limit:         maxSize = Integer.MAX_VALUE
```

**Warning signs**:
- **Too low**: Real objects rejected
- **Too high**: Artifacts included
- **Check**: Count of rejected objects

---

## Method-Specific Tuning

### ChunkedLayerCake3D

**Algorithm**: Layer-by-layer 2D segmentation with simple distance-based 3D connection.

**Key Parameters**:
```java
threshold = 127          // Intensity cutoff
minSize = 50            // Minimum voxels
maxSize = 50000         // Maximum voxels
is2D = false            // 3D connectivity
```

**Tuning Strategy**:

1. **2D vs. 3D mode**:
   - `is2D = false`: Connect across Z-slices (3D objects)
   - `is2D = true`: Each slice independent (2D objects)
   - **Use 3D** for: Cells, nuclei, continuous structures
   - **Use 2D** for: Monolayers, flat tissues, QC

2. **Separation issues**:
   - **Under-segmentation** (cells merged):
     - Increase threshold slightly
     - Ensure good Z-resolution
     - Try FloodFill with watershed instead

   - **Over-segmentation** (cells split):
     - Decrease threshold
     - Check if chunks are stitching poorly
     - Increase chunk overlap to 15-20%

**Example use cases**:
```
Well-separated fibroblasts:
  threshold = 90, minSize = 500, maxSize = 100000, is2D = false

DAPI nuclei, sparse:
  threshold = 100, minSize = 200, maxSize = 10000, is2D = false

2D monolayer QC:
  threshold = 80, minSize = 100, maxSize = 50000, is2D = true
```

### ChunkedLayerCake3DkDTree

**Algorithm**: Layer-by-layer with kDTree spatial indexing for sophisticated region connection.

**Key Parameters**:
```java
threshold = 127
minSize = 50
maxSize = 50000
is2D = false
// kDTree parameters configured in ObjectStitcher
```

**When to use**:
- Complex, irregular shapes
- Variable cell sizes
- High cell density
- Need maximum accuracy

**Tuning Strategy**:

1. **Distance threshold** (in ObjectStitcher):
   - Controls how far apart objects can be to connect
   - Typical: 5-15 pixels
   - **Smaller** for dense packing
   - **Larger** for diffuse staining

2. **Overlap threshold**:
   - Minimum overlap required to merge
   - Typical: 0.05-0.15 (5-15%)
   - **Higher** for conservative merging
   - **Lower** for aggressive merging

3. **Intensity correlation**:
   - Checks if intensities match before merging
   - **Enable** for consistent staining
   - **Disable** for variable expression

**Example use cases**:
```
Neurons with processes:
  threshold = 70, minSize = 1000, distance = 15, overlap = 0.05

Clustered cells:
  threshold = 110, minSize = 300, distance = 8, overlap = 0.10

Variable-size objects:
  threshold = 90, minSize = 100, maxSize = 500000, distance = 10
```

### ChunkedFloodFill3D

**Algorithm**: Recursive 26-connectivity flood fill with optional watershed.

**Key Parameters**:
```java
minThreshold = 127
minObjectSize = 50
maxObjectSize = 50000
useWatershed = true
```

**Unique feature**: **Watershed separation**

**Tuning Strategy**:

1. **Watershed on/off**:
   - `useWatershed = true`: Separates touching objects
   - `useWatershed = false`: Merges touching regions
   - **Enable** for: Clustered cells, nuclei
   - **Disable** for: Connected structures, vessels

2. **Threshold with watershed**:
   - **Lower threshold** = more aggressive separation
   - **Higher threshold** = more conservative
   - Start at 100-120 for watershed mode

3. **Connectivity considerations**:
   - 26-connectivity = diagonal connections
   - Good for isotropic data
   - May over-connect in anisotropic (different XY vs. Z resolution)

**Example use cases**:
```
Clustered nuclei:
  minThreshold = 110, minObjectSize = 200, watershed = true

Touching cells:
  minThreshold = 90, minObjectSize = 500, watershed = true

Connected network (vessels):
  minThreshold = 80, minObjectSize = 100, watershed = false

Dense tissue:
  minThreshold = 130, minObjectSize = 300, watershed = true
```

### ChunkedCellpose (Deep Learning)

**Algorithm**: Deep neural network trained on diverse cell types.

**Key Parameters**:
```java
model = CellposeModel.CYTO2
diameter = 30
cellprobThreshold = 0.0
flowThreshold = 0.4
do3D = false
useGPU = true
```

**Tuning Strategy**:

1. **Model selection** (most important):
   ```
   CYTO2:     General cytoplasm, whole cells (RECOMMENDED)
   NUCLEI:    Nuclear segmentation, DAPI/Hoechst
   TISSUENET: Tissue sections, H&E
   LIVECELL:  Phase contrast, label-free
   ```

2. **Diameter** (critical parameter):
   - Measure typical cell diameter in pixels
   - **0 = auto-detect** (slower but automatic)
   - **Fixed value** (faster, more consistent)

   **How to measure**:
   ```
   1. Draw line across typical cell
   2. Analyze → Measure
   3. Note pixel length
   4. Use this as diameter
   ```

   **Typical diameters**:
   ```
   Bacteria:        5-10 pixels
   Nuclei:          15-20 pixels
   Small cells:     20-30 pixels
   Typical cells:   30-40 pixels
   Large cells:     50-70 pixels
   ```

3. **Cell Probability Threshold** (-6 to 6):
   - Default: 0.0 (neutral)
   - **Lower (negative)**: Find more objects (⚠️ more false positives)
   - **Higher (positive)**: Stricter (⚠️ might miss dim cells)

   **Adjustment guide**:
   ```
   Missing obvious cells:  Try -1.0 to -3.0
   Too many false positives: Try +0.5 to +2.0
   Default good:           Keep at 0.0
   ```

4. **Flow Threshold** (0.0 to 1.0):
   - Default: 0.4
   - Controls boundary strictness
   - **Lower (0.2-0.3)**: More permissive merging
   - **Higher (0.5-0.7)**: Stricter boundaries

   **When to adjust**:
   ```
   Cells over-segmented:  Decrease to 0.3
   Cells under-segmented: Increase to 0.5-0.6
   Default works:         Keep at 0.4
   ```

5. **2D vs. 3D processing**:
   - **2D mode (stitching)**: Fast, good for thin objects
   - **3D mode**: Slower, better for spherical objects
   - **Recommendation**: Start with 2D, use 3D if quality insufficient

6. **GPU vs. CPU**:
   - **GPU**: 5-10x faster (strongly recommended)
   - **CPU**: Fallback, slower but works
   - **Quality**: Identical between GPU/CPU

**Example use cases**:
```
HeLa cells (cytoplasm):
  model = CYTO2, diameter = 32, cellprob = 0.0, flow = 0.4, 2D mode

DAPI nuclei:
  model = NUCLEI, diameter = 17, cellprob = 0.0, flow = 0.4, 2D mode

Tissue section:
  model = TISSUENET, diameter = 0 (auto), cellprob = -1.0, 3D mode

Phase contrast:
  model = LIVECELL, diameter = 28, cellprob = 0.0, flow = 0.4, 2D mode
```

**Troubleshooting Cellpose**:
```
Problem: No cells detected
Solution: Lower cellprobThreshold to -2.0

Problem: Everything merged
Solution: Increase flowThreshold to 0.6, check diameter

Problem: Each cell split into pieces
Solution: Decrease flowThreshold to 0.3, increase diameter

Problem: Background detected as cells
Solution: Increase cellprobThreshold to +1.0
```

### ChunkedSingleThreshold

**Algorithm**: Simple threshold - all pixels above threshold become one object.

**Key Parameters**:
```java
lowThreshold = 127
```

**Use cases**:
- Continuous regions (not individual cells)
- Baseline/reference
- Very simple masking
- Performance testing

**Note**: Does NOT separate individual objects.

---

## Chunked Processing Parameters

When working with large Zarr volumes, these parameters control chunk-based processing.

### Chunk Size

**Purpose**: Divide volume into manageable pieces

**How to set**:
```java
partitioner.setChunkSize(512, 512, 32);  // X, Y, Z
```

**Guidelines**:
```
Small RAM (8GB):     256 × 256 × 16
Medium RAM (16GB):   512 × 512 × 32
Large RAM (32GB+):   512 × 512 × 64
GPU processing:      Smaller chunks (256×256×16)
```

**Trade-offs**:
- **Larger chunks**: Fewer stitching artifacts, more memory
- **Smaller chunks**: Less memory, more stitching needed

### Overlap Percentage

**Purpose**: Provide context at chunk boundaries for stitching

**How to set**:
```java
partitioner.setOverlapPercent(0.15);  // 15%
```

**Guidelines**:
```
Well-separated objects:  10% (0.10)
Touching objects:        15% (0.15)
Complex shapes:          20% (0.20)
```

**Effects**:
- **Too small**: Objects split at boundaries
- **Too large**: Wasted computation
- **Typical**: 10-15%

### ObjectStitcher Parameters

**Purpose**: Merge objects that cross chunk boundaries

**Configuration**:
```java
ObjectStitcher stitcher = new ObjectStitcher();
stitcher.setDistanceThreshold(10.0);       // Max merge distance
stitcher.setOverlapThreshold(0.10);         // Min overlap to merge
stitcher.setUseIntensityCorrelation(true);  // Check intensity match
```

**Distance Threshold**:
- **Purpose**: Maximum distance between centroids to consider merging
- **Typical**: 5-15 pixels
- **Lower**: Conservative merging
- **Higher**: Aggressive merging

**Overlap Threshold**:
- **Purpose**: Minimum overlap fraction required
- **Typical**: 0.05-0.20 (5-20%)
- **Lower**: Merge even slight overlaps
- **Higher**: Require substantial overlap

**Intensity Correlation**:
- **Enable**: Check if objects have similar intensities
- **Disable**: Merge based only on geometry
- **Use when**: Consistent staining

**Method-specific stitcher settings**:
```java
// LayerCake3D (simple, fast)
stitcher.setDistanceThreshold(5.0);
stitcher.setOverlapThreshold(0.10);
stitcher.setUseIntensityCorrelation(false);

// LayerCake3DkDTree (sophisticated)
stitcher.setDistanceThreshold(10.0);
stitcher.setOverlapThreshold(0.05);
stitcher.setUseIntensityCorrelation(true);

// FloodFill3D (dense objects)
stitcher.setDistanceThreshold(8.0);
stitcher.setOverlapThreshold(0.15);
stitcher.setUseIntensityCorrelation(false);

// Cellpose (deep learning)
stitcher.setDistanceThreshold(10.0);
stitcher.setOverlapThreshold(0.20);
stitcher.setUseIntensityCorrelation(true);

// SingleThreshold (merge everything)
stitcher.setDistanceThreshold(Double.MAX_VALUE);
stitcher.setOverlapThreshold(0.0);
stitcher.setUseIntensityCorrelation(false);
```

---

## Quality Assessment

### Visual Inspection

**Quick checks**:
1. **Overlay**: View segmentation overlay on original
2. **3D rendering**: Rotate and inspect in 3D
3. **Representative slices**: Check beginning, middle, end

**What to look for**:
- ✅ All real objects detected
- ✅ No background noise detected
- ✅ Objects not split inappropriately
- ✅ Touching objects separated (if desired)
- ✅ Boundaries match visual inspection

### Quantitative Metrics

**Object count**:
```
Expected: ~1000 cells
Detected: 987 objects
→ Good (within 5%)
```

**Size distribution**:
```
Plot histogram of object volumes
Check for bimodal distribution (might indicate merged objects)
Very small objects = noise
Very large objects = artifacts or merges
```

**Validation against manual count**:
```
1. Manually count objects in 3 representative regions
2. Compare to automatic count
3. Precision = True Positives / (True Positives + False Positives)
4. Recall = True Positives / (True Positives + False Negatives)
```

**Stitching quality**:
```
Check logs for stitching statistics:
"Merged 1250 chunk objects into 987 final objects"
Merge rate = (1250-987)/1250 = 21%

Typical merge rates:
5-10%: Good overlap, minimal splitting
15-25%: Some boundary objects merged
>30%: Check overlap and stitching parameters
```

---

## Common Issues & Solutions

### Issue: Objects Split into Pieces

**Symptoms**:
- One cell shows as 2-3 objects
- Fragmented segmentations
- Count much higher than expected

**Solutions**:
1. **Lower threshold**: Try threshold = current - 20
2. **Increase chunk overlap**: Set to 20%
3. **Increase distance threshold**: stitcher.setDistanceThreshold(15.0)
4. **Decrease overlap requirement**: stitcher.setOverlapThreshold(0.05)
5. **Try different method**: FloodFill or Cellpose

### Issue: Objects Merged Together

**Symptoms**:
- Multiple cells show as one object
- Count lower than expected
- Large, irregular objects

**Solutions**:
1. **Increase threshold**: Try threshold = current + 20
2. **Enable watershed**: useWatershed = true (FloodFill)
3. **Use stricter method**: LayerCake instead of FloodFill
4. **Cellpose**: Increase flowThreshold to 0.6
5. **Check image quality**: Low contrast? Poor staining?

### Issue: Too Much Noise

**Symptoms**:
- Many tiny objects
- Background detected as objects
- Count much higher than expected

**Solutions**:
1. **Increase threshold**: Start with +30
2. **Increase min size**: minSize = 100 or higher
3. **Pre-processing**: Gaussian blur before segmentation
4. **Check image**: Is SNR too low?

### Issue: Missing Dim Objects

**Symptoms**:
- Obviously visible cells not detected
- Lower count than expected
- Only bright cells detected

**Solutions**:
1. **Lower threshold**: Try threshold = current - 30
2. **Cellpose**: Lower cellprobThreshold to -2.0
3. **Normalize intensity**: Adjust image contrast
4. **Check bit depth**: 16-bit but values only 0-1000?

### Issue: Boundary Artifacts at Chunk Edges

**Symptoms**:
- Grid pattern in results
- Objects missing at chunk boundaries
- Stitching errors

**Solutions**:
1. **Increase overlap**: Set to 20%
2. **Larger distance threshold**: stitcher.setDistanceThreshold(15.0)
3. **Lower overlap requirement**: stitcher.setOverlapThreshold(0.05)
4. **Different chunk size**: Try 512×512×64 instead of 256×256×16

---

## Best Practices by Use Case

### Nuclear Segmentation (DAPI/Hoechst)

**Recommended method**: ChunkedCellpose (nuclei model) or ChunkedLayerCake3DkDTree

**Parameters**:
```
Method: ChunkedCellpose
Model: nuclei
Diameter: 15-20 pixels
Threshold (if manual): 100-120 (8-bit)
MinSize: 200 voxels
MaxSize: 5000 voxels
```

**Tips**:
- Nuclei usually well-separated → LayerCake works well
- Cellpose nuclei model is excellent
- If clustered, enable watershed in FloodFill

### Cytoplasmic Segmentation (GFP, RFP)

**Recommended method**: ChunkedCellpose (cyto2) or ChunkedFloodFill3D

**Parameters**:
```
Method: ChunkedCellpose
Model: cyto2
Diameter: 30-40 pixels
Threshold (if manual): 80-100 (8-bit)
MinSize: 500 voxels
MaxSize: 100000 voxels
```

**Tips**:
- Cytoplasm often irregular → Cellpose handles well
- If touching, use FloodFill with watershed
- Check for dim cells at edges

### Tissue Sections (H&E, Immunofluorescence)

**Recommended method**: ChunkedCellpose (tissuenet)

**Parameters**:
```
Method: ChunkedCellpose
Model: tissuenet
Diameter: 0 (auto-detect)
CellprobThreshold: -1.0 (find more cells)
3D mode: checked
```

**Tips**:
- Tissue sections complex → deep learning best
- Variable cell sizes → use auto diameter
- May need manual curation

### Live Cell Imaging (Phase Contrast)

**Recommended method**: ChunkedCellpose (livecell)

**Parameters**:
```
Method: ChunkedCellpose
Model: livecell
Diameter: 25-30 pixels
FlowThreshold: 0.4
```

**Tips**:
- Phase contrast challenging → use specialized model
- Label-free → Cellpose trained for this
- Adjust flow threshold if boundaries poor

### High-Throughput Screening

**Recommended method**: ChunkedLayerCake3D (speed) or ChunkedFloodFill3D (quality)

**Parameters**:
```
Method: ChunkedLayerCake3D
Threshold: 100
MinSize: 50
MaxSize: 50000
Chunk size: 512×512×32 (larger for speed)
```

**Tips**:
- Speed critical → avoid deep learning
- Consistent imaging → simple methods work
- Batch process overnight
- Save parameters for reproducibility

### Rare Event Detection

**Recommended method**: ChunkedCellpose or sensitive traditional method

**Parameters**:
```
Threshold: Lower than usual (find more)
CellprobThreshold: -2.0 (Cellpose)
MinSize: Lower (don't filter out small events)
MaxSize: Higher (include large events)
```

**Tips**:
- Bias towards false positives, manually curate
- Use Cellpose for maximum sensitivity
- Visual inspection critical

---

## Summary

### Parameter Tuning Workflow

```
1. Choose appropriate method (flowchart)
   ↓
2. Start with defaults
   ↓
3. Visual inspection
   ↓
4. Adjust threshold (most important)
   ↓
5. Set size filters
   ↓
6. Refine method-specific parameters
   ↓
7. Optimize chunking (if needed)
   ↓
8. Validate quality
   ↓
9. Save parameters!
```

### Quick Troubleshooting

| Problem | First Thing to Try |
|---------|-------------------|
| No objects | Lower threshold by 30 |
| Too many objects | Increase threshold by 30 |
| Objects split | Increase overlap to 20% |
| Objects merged | Increase threshold, try watershed |
| Slow processing | Use LayerCake, larger chunks, GPU |
| Poor boundaries | Try Cellpose, adjust flow threshold |

### Recommended Starting Points

**Conservative** (few false positives):
```
threshold = median intensity + 1 std dev
minSize = 50% of expected size
maxSize = 3x expected size
overlap = 15%
```

**Sensitive** (find all objects):
```
threshold = median intensity
minSize = 25% of expected size
maxSize = 10x expected size
overlap = 20%
```

**Balanced** (recommended):
```
threshold = median intensity + 0.5 std dev
minSize = 40% of expected size
maxSize = 5x expected size
overlap = 15%
```

---

**Version**: 2.0.0
**Last Updated**: 2025-10-28
**Authors**: Seth Winfree, VTEA Team
**License**: GPL v2
