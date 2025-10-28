# VTEA 2.0.0 - Phase 2 Plan: Comprehensive Segmentation Adaptation

## Overview

Phase 2 focuses on adapting ALL existing segmentation methods to support chunked processing and adding new deep learning integrations (Cellpose, DeepImageJ) for modern ML-based segmentation.

**Goal:** Enable every segmentation method in VTEA to process large volumes that don't fit in RAM.

---

## Existing Segmentation Methods Found: 11

### Traditional Methods (6)
1. ✅ **SingleThreshold** - Basic 3D intensity thresholding
2. ✅ **Region2DSingleThreshold** - 2D slice-by-slice with watershed
3. ✅ **LayerCake3DSingleThreshold** - Connected components (2D/3D hybrid)
4. ✅ **LayerCake3DSingleThresholdkDTree** - With kDTree spatial indexing (PRIORITY)
5. ✅ **LayerCake3DLargeScaleSingleThreshold** - Already has tiling support!
6. ✅ **FloodFill3DSingleThreshold** - 3D flood fill algorithm

### Library-Based Methods (2)
7. ✅ **MorphoLibJConnectedComponents** - MorphoLibJ watershed + CC
8. ✅ **Imglib2ConnectedComponents** - ImgLib2 native CC

### Import/External Methods (3)
9. ✅ **ImageJROIBased** - Import ImageJ ROIs
10. ✅ **PreLabelled** - Import label maps
11. ✅ **Points** - Point-based segmentation from coordinates

### New Methods to Add (2)
12. 🆕 **Cellpose Integration** - Deep learning cell/nuclei segmentation
13. 🆕 **DeepImageJ Integration** - Generic deep learning model inference

---

## Phase 2 Sub-Phases

### Phase 2A: High-Priority Adaptations (Week 1-2)

#### 2A.1: LayerCake3DSingleThresholdkDTree → ChunkedLayerCake3DkDTree
**Priority:** ⭐⭐⭐⭐⭐ (HIGHEST - Most advanced, uses kDTree already)

**Why First:**
- Already uses kDTree spatial indexing (perfect for boundary stitching)
- Most sophisticated existing method
- Template for other LayerCake variants
- Recent improvements (2.5D segmentation, boundary merging)

**Implementation Strategy:**
```java
public class ChunkedLayerCake3DkDTree extends AbstractChunkedSegmentation {
    // Inherits chunking framework

    @Override
    protected List<MicroObject> processChunk(Chunk chunk, List details, boolean calculate) {
        // Process single chunk using existing LayerCake3D algorithm
        ImageStack chunkStack = chunk.getData();
        LayerCake3D layerCake = new LayerCake3D(chunkStack, minConstants, true);
        return layerCake.getVolumes();
    }

    @Override
    protected List<MicroObject> stitchChunks(List<MicroObject> chunkObjects,
                                            ChunkedVolumeDataset dataset) {
        // Use ObjectStitcher with existing kDTree logic
        ObjectStitcher stitcher = new ObjectStitcher();
        stitcher.setUseIntensityCorrelation(true);
        return stitcher.stitchObjects(chunkObjects);
    }

    @Override
    protected boolean processTraditional(ImagePlus imp, List details, boolean calculate) {
        // Delegate to existing LayerCake3DSingleThresholdkDTree
        // Zero code duplication!
    }
}
```

**Stitching Notes:**
- Leverage existing kDTree implementation in ObjectStitcher
- Reuse nuclei merging logic from original implementation
- Maintain 2.5D segmentation option for chunked mode

**Estimated Effort:** 2-3 days
**Files to Create:** 1 new class
**Testing:** Compare chunked vs non-chunked results on test volumes

---

#### 2A.2: LayerCake3DSingleThreshold → ChunkedLayerCake3D
**Priority:** ⭐⭐⭐⭐ (HIGH - Core method, simpler than kDTree version)

**Why Second:**
- Simpler than kDTree version
- Template can reuse kDTree work
- Widely used for basic connected components

**Implementation Strategy:**
- Copy pattern from ChunkedLayerCake3DkDTree
- Simpler stitching (no intensity correlation needed)
- Reuse LayerCake3D helper class

**Estimated Effort:** 1-2 days
**Files to Create:** 1 new class

---

#### 2A.3: FloodFill3DSingleThreshold → ChunkedFloodFill3D
**Priority:** ⭐⭐⭐⭐ (HIGH - Different algorithm paradigm)

**Why Third:**
- Different algorithmic approach (flood fill vs connected components)
- Tests chunking framework with different paradigm
- May need special boundary handling for flood fill

**Challenges:**
- Flood fill across chunk boundaries needs careful handling
- May need to "continue" flood fill in adjacent chunks
- Requires testing for leakage across boundaries

**Implementation Strategy:**
```java
public class ChunkedFloodFill3D extends AbstractChunkedSegmentation {
    @Override
    protected List<MicroObject> processChunk(Chunk chunk, List details, boolean calculate) {
        // Use existing FloodFill3D on chunk
        // Mark objects touching boundary
    }

    @Override
    protected List<MicroObject> stitchChunks(List<MicroObject> chunkObjects,
                                            ChunkedVolumeDataset dataset) {
        // Special logic: flood fill may "leak" across boundaries
        // Need to merge objects that are actually same object split by boundary
        ObjectStitcher stitcher = new ObjectStitcher();
        stitcher.setOverlapThreshold(0.01); // More aggressive merging for flood fill
        return stitcher.stitchObjects(chunkObjects);
    }
}
```

**Estimated Effort:** 2-3 days
**Files to Create:** 1 new class

---

### Phase 2B: Medium-Priority Adaptations (Week 3)

#### 2B.1: LayerCake3DLargeScaleSingleThreshold → Enhanced with Zarr
**Priority:** ⭐⭐⭐ (MEDIUM - Already has tiling, needs Zarr integration)

**Why Special:**
- Already implements tile-based processing!
- Has chunk size and overlap parameters
- Main work: integrate with VolumeDataset instead of ImagePlus

**Implementation Strategy:**
```java
public class ChunkedLayerCake3DLargeScale extends AbstractChunkedSegmentation {
    // Leverage existing tiling logic
    // Replace manual tiling with VolumePartitioner
    // Reuse overlap merging logic
}
```

**Key Insight:** This class already does chunking manually - we need to:
1. Replace manual chunking with VolumePartitioner
2. Use VolumeDataset for I/O instead of ImagePlus
3. Integrate with Zarr backend
4. Unify with AbstractChunkedSegmentation framework

**Estimated Effort:** 2-3 days (refactoring existing code)
**Files to Modify:** 1 existing class

---

#### 2B.2: Region2DSingleThreshold → ChunkedRegion2D
**Priority:** ⭐⭐⭐ (MEDIUM - 2D, simpler)

**Implementation Strategy:**
- Process each Z-slice chunk independently
- Minimal stitching needed (2D regions in same slice don't cross boundaries)
- Only stitch objects that span Z-axis boundaries

**Estimated Effort:** 1-2 days
**Files to Create:** 1 new class

---

#### 2B.3: SingleThreshold → ChunkedSingleThreshold
**Priority:** ⭐⭐⭐ (MEDIUM - Simplest, but still useful)

**Implementation Strategy:**
- Simple threshold application per chunk
- Minimal stitching (just connected regions)

**Estimated Effort:** 1 day
**Files to Create:** 1 new class

---

### Phase 2C: Library-Based Adaptations (Week 4)

#### 2C.1: MorphoLibJConnectedComponents → ChunkedMorphoLibJCC
**Priority:** ⭐⭐ (LOWER - Experimental status)

**Challenges:**
- Watershed algorithm may behave differently on chunk boundaries
- Need to understand MorphoLibJ watershed internals
- Marked as "not working well" in original code

**Implementation Strategy:**
```java
public class ChunkedMorphoLibJCC extends AbstractChunkedSegmentation {
    @Override
    protected List<MicroObject> processChunk(Chunk chunk, List details, boolean calculate) {
        // Apply MorphoLibJ watershed to chunk
        // Extended watershed markers may propagate across boundaries
    }

    @Override
    protected List<MicroObject> stitchChunks(...) {
        // Aggressive stitching - watershed regions likely to connect
        // Use intensity correlation heavily
    }
}
```

**Estimated Effort:** 2-3 days (debugging watershed boundaries)
**Files to Create:** 1 new class

---

#### 2C.2: Imglib2ConnectedComponents → ChunkedImglib2CC
**Priority:** ⭐⭐ (LOWER - Experimental)

**Implementation Strategy:**
- Use ImgLib2's connected components on chunk sub-volumes
- ImgLib2 has good chunked data support via Views

**Estimated Effort:** 1-2 days
**Files to Create:** 1 new class

---

### Phase 2D: Import/External Methods (Week 4)

#### 2D.1: PreLabelled → ChunkedPreLabelled
**Priority:** ⭐⭐ (LOWER - Import method)

**Why Adapt:**
- Users may have pre-labeled Zarr volumes
- Need to load labels in chunks

**Implementation Strategy:**
```java
public class ChunkedPreLabelled extends AbstractChunkedSegmentation {
    @Override
    protected List<MicroObject> processChunk(Chunk chunk, List details, boolean calculate) {
        // Load label chunk from Zarr
        // Extract objects from label IDs
        // Tag boundary objects for stitching
    }

    @Override
    protected List<MicroObject> stitchChunks(...) {
        // Merge objects with same label ID across boundaries
        // Simple: match by label ID
    }
}
```

**Estimated Effort:** 1-2 days
**Files to Create:** 1 new class

---

#### 2D.2: Points → ChunkedPoints
**Priority:** ⭐ (LOW - Simple import method)

**Implementation Strategy:**
- Partition points by chunk
- Create spherical objects around each point
- No stitching needed (points are discrete)

**Estimated Effort:** 1 day
**Files to Create:** 1 new class

---

#### 2D.3: ImageJROIBased → ChunkedImageJROI
**Priority:** ⭐ (LOW - 2D ROI import)

**Implementation Strategy:**
- ROIs already 2D, map to chunks by Z-position
- Minimal adaptation needed

**Estimated Effort:** 1 day
**Files to Create:** 1 new class

---

### Phase 2E: Deep Learning Integration (Week 5-6) 🆕

#### 2E.1: Cellpose Integration
**Priority:** ⭐⭐⭐⭐⭐ (HIGHEST NEW FEATURE)

**Why Critical:**
- Cellpose is state-of-the-art for cell/nuclei segmentation
- Widely used in microscopy community
- GPU-accelerated
- Pre-trained models available

**Architecture:**

```
┌─────────────────────────────────────────┐
│   ChunkedCellposeSegmentation          │
│   extends AbstractChunkedSegmentation   │
└──────────────┬──────────────────────────┘
               │
               ├─► CellposeInterface
               │   └─► Python subprocess via py4j
               │       - Start Cellpose Python process
               │       - Send chunk data via shared memory
               │       - Receive segmentation masks
               │
               ├─► CellposeModelManager
               │   └─► Model selection and caching
               │       - cyto, cyto2, nuclei models
               │       - Custom trained models
               │       - Model download/cache
               │
               └─► CellposeBoundaryStitcher
                   └─► Specialized stitching for Cellpose
                       - Handles probability maps
                       - Uses flow fields if available
```

**Implementation Files:**

1. **CellposeInterface.java** - Python bridge
```java
public class CellposeInterface {
    private Process pythonProcess;
    private py4j.GatewayServer gateway;

    public ImageStack segment(ImageStack chunk, CellposeModel model,
                             double diameter, boolean useGPU) {
        // Send chunk to Python Cellpose
        // Receive label mask
        // Return as ImageStack
    }
}
```

2. **ChunkedCellposeSegmentation.java** - Main implementation
```java
@Plugin(type = Segmentation.class)
public class ChunkedCellposeSegmentation extends AbstractChunkedSegmentation {

    private CellposeInterface cellpose;
    private CellposeModel model = CellposeModel.CYTO2;
    private double diameter = 30.0;
    private boolean useGPU = true;

    @Override
    protected List<MicroObject> processChunk(Chunk chunk, List details, boolean calculate) {
        // Send chunk to Cellpose
        ImageStack labels = cellpose.segment(chunk.getData(), model, diameter, useGPU);

        // Convert labels to MicroObjects
        return labelsToObjects(labels, chunk);
    }

    @Override
    protected List<MicroObject> stitchChunks(...) {
        // Use ObjectStitcher with intensity correlation
        // Cellpose objects have consistent boundaries
        return new ObjectStitcher().stitchObjects(chunkObjects);
    }
}
```

3. **CellposeModelManager.java** - Model management
```java
public class CellposeModelManager {
    public enum CellposeModel {
        CYTO("cyto"),
        CYTO2("cyto2"),
        NUCLEI("nuclei"),
        CUSTOM("custom");
    }

    public File getModelPath(CellposeModel model);
    public void downloadModel(CellposeModel model);
    public List<String> listAvailableModels();
}
```

**Python Requirements:**
```python
# requirements.txt
cellpose>=2.0
numpy
scipy
```

**Py4J Integration:**
```xml
<dependency>
    <groupId>net.sf.py4j</groupId>
    <artifactId>py4j</artifactId>
    <version>0.10.9.7</version>
</dependency>
```

**Cellpose Python Script (cellpose_server.py):**
```python
from cellpose import models
from py4j.java_gateway import JavaGateway, CallbackServerParameters
import numpy as np

class CellposeServer:
    def __init__(self):
        self.model = models.Cellpose(model_type='cyto2')

    def segment(self, image_data, diameter=30.0, channels=[0,0]):
        masks, flows, styles, diams = self.model.eval(
            image_data,
            diameter=diameter,
            channels=channels
        )
        return masks

gateway = JavaGateway(
    callback_server_parameters=CallbackServerParameters()
)
cellpose_server = CellposeServer()
gateway.entry_point.setCellposeServer(cellpose_server)
```

**Configuration UI Panel:**
- Model selection dropdown (cyto, cyto2, nuclei, custom)
- Diameter slider (10-100 pixels)
- Use GPU checkbox
- Custom model file browser
- Advanced: flow threshold, cell probability threshold

**Estimated Effort:** 5-7 days
**Files to Create:**
- CellposeInterface.java
- ChunkedCellposeSegmentation.java
- CellposeModelManager.java
- CellposeConfigPanel.java (UI)
- cellpose_server.py (Python bridge)

---

#### 2E.2: DeepImageJ Integration
**Priority:** ⭐⭐⭐⭐ (HIGH NEW FEATURE)

**Why Important:**
- Generic deep learning inference framework
- Supports TensorFlow and PyTorch models
- ImageJ plugin already exists (easier integration)
- Users can use ANY trained model

**Architecture:**

```
┌─────────────────────────────────────────┐
│   ChunkedDeepImageJSegmentation        │
│   extends AbstractChunkedSegmentation   │
└──────────────┬──────────────────────────┘
               │
               ├─► DeepImageJRunner
               │   └─► Run inference via DeepImageJ plugin
               │       - Load model from model zoo
               │       - Pre/post processing
               │       - Patch-based inference
               │
               ├─► ModelZooManager
               │   └─► Browse and download models
               │       - BioImage Model Zoo integration
               │       - Local model cache
               │       - Model metadata
               │
               └─► TiledInference
                   └─► Handle models with small receptive fields
                       - Tile input chunk if needed
                       - Merge predictions with blending
```

**Implementation Files:**

1. **DeepImageJRunner.java** - DeepImageJ bridge
```java
public class DeepImageJRunner {
    private DeepImageJ deepImageJ;  // From DeepImageJ plugin

    public ImageStack runInference(ImageStack input, File modelFile,
                                   PreProcessing preprocessing,
                                   PostProcessing postprocessing) {
        // Run DeepImageJ inference
        // Handle pre/post processing
        // Return segmentation masks
    }
}
```

2. **ChunkedDeepImageJSegmentation.java** - Main implementation
```java
@Plugin(type = Segmentation.class)
public class ChunkedDeepImageJSegmentation extends AbstractChunkedSegmentation {

    private DeepImageJRunner runner;
    private File modelFile;
    private int tileSize = 256;  // For models with small receptive field

    @Override
    protected List<MicroObject> processChunk(Chunk chunk, List details, boolean calculate) {
        ImageStack chunkData = chunk.getData();

        // Check if chunk needs tiling (model receptive field < chunk size)
        if (needsTiling(chunkData)) {
            return processTiled(chunkData, chunk);
        } else {
            ImageStack labels = runner.runInference(chunkData, modelFile,
                                                    preprocessing, postprocessing);
            return labelsToObjects(labels, chunk);
        }
    }

    private List<MicroObject> processTiled(ImageStack data, Chunk chunk) {
        // Tile chunk into smaller patches
        // Run inference on each patch
        // Stitch patch predictions with blending
        // Convert to objects
    }
}
```

3. **ModelZooManager.java** - BioImage Model Zoo integration
```java
public class ModelZooManager {
    private static final String MODEL_ZOO_URL = "https://bioimage.io/";

    public List<ModelInfo> listModels(String task);
    public File downloadModel(String modelId);
    public ModelMetadata getModelMetadata(File modelFile);

    public static class ModelInfo {
        public String id;
        public String name;
        public String description;
        public String task;  // segmentation, detection, etc.
        public String framework;  // tensorflow, pytorch
    }
}
```

**DeepImageJ Dependency:**
```xml
<dependency>
    <groupId>io.github.deepimagej</groupId>
    <artifactId>deepimagej</artifactId>
    <version>3.0.0</version>
</dependency>
```

**Configuration UI Panel:**
- Model file browser (with Model Zoo browser)
- Model info display (inputs, outputs, resolution)
- Pre-processing selection (normalization, etc.)
- Post-processing selection (thresholding, etc.)
- Tile size for large models
- GPU selection

**Estimated Effort:** 5-7 days
**Files to Create:**
- DeepImageJRunner.java
- ChunkedDeepImageJSegmentation.java
- ModelZooManager.java
- TiledInference.java
- DeepImageJConfigPanel.java (UI)

---

#### 2E.3: StarDist Integration (Bonus)
**Priority:** ⭐⭐⭐ (MEDIUM - Popular alternative to Cellpose)

**Why Consider:**
- Star-convex shapes (nuclei, cells)
- Fast inference
- Pre-trained models available
- ImageJ plugin exists

**Implementation:** Similar pattern to Cellpose
**Estimated Effort:** 3-4 days
**Files to Create:** 3-4 new classes

---

## Phase 2 Implementation Priority Order

### Week 1-2: Core Traditional Methods
1. ✅ ChunkedLayerCake3DkDTree (3 days)
2. ✅ ChunkedLayerCake3D (2 days)
3. ✅ ChunkedFloodFill3D (3 days)

### Week 3: Medium Priority
4. ✅ ChunkedLayerCake3DLargeScale (3 days)
5. ✅ ChunkedRegion2D (2 days)
6. ✅ ChunkedSingleThreshold (1 day)

### Week 4: Library-Based + Import
7. ✅ ChunkedMorphoLibJCC (2 days)
8. ✅ ChunkedImglib2CC (2 days)
9. ✅ ChunkedPreLabelled (1 day)
10. ✅ ChunkedPoints (1 day)
11. ✅ ChunkedImageJROI (1 day)

### Week 5-6: Deep Learning (NEW!)
12. 🆕 Cellpose Integration (7 days)
13. 🆕 DeepImageJ Integration (7 days)
14. 🆕 (Optional) StarDist Integration (4 days)

---

## Common Stitching Strategies by Method Type

### Connected Components (LayerCake, CC methods)
- **Stitching:** ObjectStitcher with overlap + distance thresholds
- **Key Parameters:** 10% overlap, 10px distance
- **Challenge:** Objects split across boundaries

### Flood Fill
- **Stitching:** Aggressive merging (lower thresholds)
- **Key Parameters:** 1% overlap (any touch), 5px distance
- **Challenge:** Same object may flood into multiple chunks

### Watershed-Based (MorphoLibJ)
- **Stitching:** Use intensity correlation heavily
- **Key Parameters:** 15% overlap, intensity correlation > 0.7
- **Challenge:** Watershed basins may not align at boundaries

### Deep Learning (Cellpose, DeepImageJ)
- **Stitching:** Standard ObjectStitcher (DL output is clean)
- **Key Parameters:** 10% overlap, 10px distance
- **Challenge:** Model edge effects (solved with overlap)

### Import Methods (PreLabelled, Points, ROI)
- **Stitching:** Minimal (match by ID) or None
- **Challenge:** Data already segmented

---

## Testing Strategy

### Unit Tests
- Test each chunked method independently
- Verify chunk processing produces valid objects
- Test stitching with synthetic data

### Integration Tests
- **Gold Standard Comparison:**
  - Run same data through traditional AND chunked version
  - Compare object counts, centroids, measurements
  - Tolerance: <1% difference in measurements

- **Boundary Test Cases:**
  - Create synthetic volumes with objects at chunk boundaries
  - Verify stitching merges correctly
  - Test all overlap percentages (5%, 10%, 15%)

### Performance Tests
- Measure memory usage vs volume size
- Benchmark processing time (chunked vs traditional)
- Verify cache behavior

### Validation Datasets
Create test volumes:
1. **Small (512³)** - Fits in RAM, test correctness
2. **Medium (1024³)** - Tight fit, test memory management
3. **Large (2048³)** - Requires chunking, test scalability
4. **Synthetic** - Known ground truth, test accuracy

---

## Dependencies to Add

### For Cellpose:
```xml
<dependency>
    <groupId>net.sf.py4j</groupId>
    <artifactId>py4j</artifactId>
    <version>0.10.9.7</version>
</dependency>
```

### For DeepImageJ:
```xml
<dependency>
    <groupId>io.github.deepimagej</groupId>
    <artifactId>deepimagej</artifactId>
    <version>3.0.0</version>
</dependency>
<dependency>
    <groupId>org.tensorflow</groupId>
    <artifactId>tensorflow</artifactId>
    <version>1.15.0</version>
</dependency>
```

### For StarDist (optional):
```xml
<dependency>
    <groupId>de.csbdresden</groupId>
    <artifactId>stardist</artifactId>
    <version>0.3.0</version>
</dependency>
```

---

## Documentation Updates

For each adapted method, document:
1. **Usage example** with chunked data
2. **Stitching parameters** and recommendations
3. **Memory requirements** vs volume size
4. **Performance benchmarks**
5. **Known limitations**

---

## Success Criteria

✅ All 11 existing methods adapted to AbstractChunkedSegmentation
✅ Cellpose integration working with GPU acceleration
✅ DeepImageJ integration with Model Zoo
✅ <1% difference in results (chunked vs traditional)
✅ 10× larger volumes processable
✅ Comprehensive test coverage
✅ User documentation complete
✅ Performance benchmarks published

---

## Summary Statistics

**Total Methods to Implement:** 14
- Traditional adaptations: 11
- New DL integrations: 3

**Estimated Total Effort:** 6 weeks
- Week 1-2: Core traditional (3 methods)
- Week 3: Medium priority (3 methods)
- Week 4: Library + import (5 methods)
- Week 5-6: Deep learning (2-3 methods)

**Lines of Code Estimate:** ~4,000-5,000
**Test Code:** ~2,000-3,000 lines

---

**Next Step:** Begin implementation with ChunkedLayerCake3DkDTree as the template for all other adaptations.
