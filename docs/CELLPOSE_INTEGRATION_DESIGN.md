# Cellpose Integration Architecture for VTEA 2.0

## Overview
This document details the architecture for integrating Cellpose (state-of-the-art deep learning segmentation) into VTEA 2.0 with support for chunked processing of large Zarr volumes.

## Architecture Components

### 1. Python-Java Bridge (Py4J)

**Choice Rationale:**
- Cellpose is Python-based (PyTorch)
- Py4J provides bidirectional Java-Python communication
- Supports both callback and blocking operations
- Lightweight compared to alternatives (JNA, JNI)

**Components:**
```
Java Side                    Python Side
---------                    -----------
CellposeInterface    <--->   CellposePythonServer
    |                            |
    | Py4J Gateway               | Cellpose API
    |                            |
ChunkedCellpose              models.Cellpose
Segmentation
```

### 2. Java Components

#### 2.1 CellposeInterface.java
Core interface for Python communication.

**Responsibilities:**
- Start/stop Python server
- Send image data to Python
- Receive segmentation masks
- Handle errors and timeouts
- Manage process lifecycle

**Key Methods:**
```java
public class CellposeInterface implements AutoCloseable {
    // Lifecycle
    public void startServer() throws IOException
    public void stopServer()
    public boolean isServerRunning()

    // Segmentation
    public int[][] segment2D(ImageProcessor ip, CellposeParams params)
    public int[][][] segment3D(ImageStack stack, CellposeParams params)
    public int[][][] segmentChunk(ImageStack chunk, CellposeParams params)

    // Configuration
    public void setModel(CellposeModel model)
    public void setDevice(String device) // "cpu", "cuda", "mps"

    // Health checks
    public boolean testConnection()
    public String getPythonVersion()
    public String getCellposeVersion()
}
```

**Data Transfer Format:**
- Images: Send as byte[] or float[] arrays with dimensions
- Results: Receive as int[][] or int[][][] label masks
- Parameters: Send as JSON strings via Py4J

#### 2.2 CellposeParams.java
Parameter container for Cellpose configuration.

```java
public class CellposeParams implements Serializable {
    private CellposeModel model = CellposeModel.CYTO2;
    private int diameter = 30;
    private double cellprobThreshold = 0.0;
    private double flowThreshold = 0.4;
    private boolean useGPU = true;
    private int[] channels = new int[]{0, 0}; // [cyto, nucleus]
    private boolean do3D = false;
    private boolean stitch3D = true;
    private boolean normalize = true;

    // Builder pattern
    public static class Builder { ... }
}
```

**Cellpose Models:**
```java
public enum CellposeModel {
    CYTO("cyto"),           // Generalized cytoplasm
    CYTO2("cyto2"),         // Updated cytoplasm model
    NUCLEI("nuclei"),       // Nuclear segmentation
    TISSUENET("tissuenet"), // Tissue sections
    LIVECELL("livecell"),   // Live cell imaging
    CUSTOM("custom");       // User-trained model

    private final String pythonName;
}
```

#### 2.3 ChunkedCellposeSegmentation.java
Main segmentation class extending AbstractChunkedSegmentation.

**Processing Strategy:**
1. **For each chunk:**
   - Send chunk to Python server
   - Receive label mask
   - Convert labels to MicroObjects
   - Transform to global coordinates
   - Tag boundary objects

2. **Stitching:**
   - Use ObjectStitcher with higher distance threshold
   - Enable intensity correlation (Cellpose provides consistent intensities)
   - Special handling for touching objects at boundaries

**Key Methods:**
```java
@Plugin(type = Segmentation.class)
public class ChunkedCellposeSegmentation extends AbstractChunkedSegmentation {

    @Override
    protected List<MicroObject> processChunk(Chunk chunk, List details, boolean calculate) {
        // 1. Extract chunk data
        // 2. Send to Cellpose via CellposeInterface
        // 3. Receive label mask
        // 4. Convert labels to MicroObjects
        // 5. Transform coordinates
        // 6. Return objects
    }

    @Override
    protected List<MicroObject> stitchChunks(...) {
        // Use ObjectStitcher with Cellpose-specific parameters
        // - Higher distance threshold (Cellpose can segment touching cells)
        // - Enable intensity correlation
        // - Consider label consistency across boundaries
    }

    private List<MicroObject> labelsToObjects(int[][][] labels, ImageStack original) {
        // Convert Cellpose integer labels to MicroObject instances
        // Preserve intensity measurements from original image
    }
}
```

### 3. Python Components

#### 3.1 cellpose_server.py
Python server that receives requests from Java.

**Structure:**
```python
from py4j.java_gateway import JavaGateway, CallbackServerParameters
from cellpose import models, io
import numpy as np
import logging

class CellposeServer:
    def __init__(self, model_type='cyto2', gpu=True):
        """Initialize Cellpose models"""
        self.model = models.Cellpose(model_type=model_type, gpu=gpu)
        self.logger = self._setup_logging()

    def segment_2d(self, image_bytes, width, height, params_json):
        """Segment 2D image"""
        # 1. Deserialize image from bytes
        # 2. Parse parameters from JSON
        # 3. Run Cellpose segmentation
        # 4. Return label mask as serialized array

    def segment_3d(self, image_bytes, width, height, depth, params_json):
        """Segment 3D stack"""
        # 1. Deserialize 3D image
        # 2. Run Cellpose 3D or slice-by-slice
        # 3. Return 3D label mask

    def get_version(self):
        """Return Cellpose version"""
        return cellpose.__version__

    def health_check(self):
        """Simple health check"""
        return "OK"

    def shutdown(self):
        """Clean shutdown"""
        logging.info("Cellpose server shutting down")

def start_server(port=25333, model='cyto2', gpu=True):
    """Start Py4J gateway server"""
    server = CellposeServer(model_type=model, gpu=gpu)
    gateway = JavaGateway(
        callback_server_parameters=CallbackServerParameters(port=port),
        python_server_entry_point=server
    )
    logging.info(f"Cellpose server started on port {port}")
    return gateway

if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--port", type=int, default=25333)
    parser.add_argument("--model", default="cyto2")
    parser.add_argument("--gpu", action="store_true", default=True)
    parser.add_argument("--cpu", action="store_false", dest="gpu")
    args = parser.parse_args()

    start_server(port=args.port, model=args.model, gpu=args.gpu)
```

#### 3.2 Installation Script
Python environment setup script.

**cellpose_setup.py:**
```python
#!/usr/bin/env python3
"""
Setup script for Cellpose integration with VTEA 2.0
Installs required Python packages and tests the installation
"""
import subprocess
import sys

REQUIRED_PACKAGES = [
    "cellpose>=2.2",
    "torch>=2.0",
    "numpy>=1.23",
    "py4j>=0.10.9.7",
]

def install_packages():
    """Install required packages"""
    for package in REQUIRED_PACKAGES:
        print(f"Installing {package}...")
        subprocess.check_call([sys.executable, "-m", "pip", "install", package])

def test_installation():
    """Test that all packages are importable"""
    try:
        import cellpose
        import torch
        import py4j
        import numpy
        print("✓ All packages installed successfully")
        print(f"  Cellpose version: {cellpose.__version__}")
        print(f"  PyTorch version: {torch.__version__}")
        print(f"  CUDA available: {torch.cuda.is_available()}")
        return True
    except ImportError as e:
        print(f"✗ Import error: {e}")
        return False

if __name__ == "__main__":
    print("Setting up Cellpose environment for VTEA 2.0...")
    install_packages()
    if test_installation():
        print("\nSetup complete! Run cellpose_server.py to start the server.")
    else:
        print("\nSetup failed. Please check error messages above.")
        sys.exit(1)
```

### 4. Integration Flow

#### 4.1 Startup Sequence
```
1. User launches VTEA
2. VTEA checks for Python/Cellpose availability
3. If available, CellposeInterface.startServer() is called
4. Java launches Python subprocess: python cellpose_server.py
5. Python server initializes Cellpose models
6. Py4J gateway established on port 25333
7. Java testConnection() verifies communication
8. Status: "Cellpose Ready" displayed in UI
```

#### 4.2 Segmentation Sequence (Chunked)
```
1. User selects ChunkedCellposeSegmentation
2. User configures parameters (model, diameter, etc.)
3. VTEA loads Zarr volume as ChunkedVolumeDataset
4. For each chunk:
   a. Load chunk data (ImageStack)
   b. Convert to byte[] array
   c. Send to Python via CellposeInterface
   d. Python runs Cellpose segmentation
   e. Python returns int[][][] label mask
   f. Java converts labels to MicroObjects
   g. Transform coordinates to global space
   h. Tag boundary objects
5. ObjectStitcher merges boundary objects
6. Results displayed/saved
```

#### 4.3 Error Handling
```
- Python server crash: Auto-restart with exponential backoff
- GPU out of memory: Fallback to CPU automatically
- Timeout: Configurable timeout (default 300s per chunk)
- Network errors: Retry up to 3 times
- Model loading failure: Display clear error message with fix suggestions
```

### 5. Configuration

#### 5.1 Java Configuration (cellpose.properties)
```properties
# Cellpose Server Configuration
cellpose.server.port=25333
cellpose.server.startup.timeout=60000
cellpose.server.request.timeout=300000

# Python Configuration
cellpose.python.executable=python3
cellpose.python.script=cellpose_server.py

# Model Configuration
cellpose.model.default=cyto2
cellpose.model.cache.dir=${user.home}/.vtea/cellpose_models

# GPU Configuration
cellpose.gpu.enabled=true
cellpose.gpu.memory.limit=8G

# Logging
cellpose.log.level=INFO
cellpose.log.file=${user.home}/.vtea/logs/cellpose.log
```

#### 5.2 UI Integration
```
New menu option: Segmentation > Deep Learning > Cellpose
Parameter panel:
  - Model dropdown: [Cyto2, Nuclei, TissueNet, Custom]
  - Diameter: [auto-detect] or slider (5-100)
  - Cell probability threshold: slider (0.0-1.0)
  - Flow threshold: slider (0.0-1.0)
  - Channels: [Cyto channel, Nucleus channel]
  - Processing mode: [2D, 3D, 2.5D (pseudo-3D)]
  - Device: [Auto, CPU, GPU]
  - [Test Connection] button
  - Status indicator: [●] Ready / [○] Not Connected
```

### 6. Performance Considerations

#### 6.1 Chunk Size Recommendations
- **2D mode**: Process entire Z-slices (no Z chunking needed)
- **3D mode**: Larger chunks (512x512x64) for context
- **GPU memory**: Auto-adjust chunk size based on available VRAM
- **Overlap**: 20% overlap for 3D to ensure stitching quality

#### 6.2 Optimization Strategies
1. **Batch Processing**: Send multiple 2D slices in one call
2. **Model Caching**: Keep Cellpose model loaded between chunks
3. **GPU Warmup**: Run dummy prediction on startup
4. **Data Transfer**: Use shared memory for large volumes (future)
5. **Parallel Processing**: Process multiple chunks if multiple GPUs available

### 7. Testing Strategy

#### 7.1 Unit Tests
- CellposeInterface connection/disconnection
- Parameter serialization/deserialization
- Label to MicroObject conversion
- Coordinate transformation

#### 7.2 Integration Tests
- Full chunked segmentation pipeline
- Stitching quality verification
- GPU vs CPU comparison
- Error recovery scenarios

#### 7.3 Test Data
- Small synthetic dataset (256x256x32)
- Known cell count for validation
- Boundary test cases (cells crossing chunk boundaries)

### 8. Documentation

#### 8.1 User Documentation
- Installation guide (Python environment setup)
- Parameter tuning guide
- Model selection guide
- Troubleshooting common issues

#### 8.2 Developer Documentation
- API reference for CellposeInterface
- Extending with custom models
- Adding new deep learning methods using this pattern

### 9. Future Enhancements

1. **Model Training Integration**: Allow users to train custom Cellpose models
2. **Model Zoo**: Download pre-trained models from Cellpose model zoo
3. **Real-time Preview**: Show segmentation on sample chunk before full run
4. **Batch Mode**: Process multiple datasets overnight
5. **Cloud Integration**: Run Cellpose on cloud GPUs for very large datasets
6. **Multi-GPU**: Distribute chunks across multiple GPUs

### 10. Dependencies

#### 10.1 Maven Dependencies (pom.xml)
```xml
<!-- Py4J for Python-Java bridge -->
<dependency>
    <groupId>net.sf.py4j</groupId>
    <artifactId>py4j</artifactId>
    <version>0.10.9.7</version>
</dependency>

<!-- JSON parsing for parameter passing -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

#### 10.2 Python Requirements (requirements.txt)
```
cellpose>=2.2
torch>=2.0
numpy>=1.23
py4j>=0.10.9.7
```

### 11. File Structure
```
src/main/java/vtea/
├── objects/Segmentation/
│   └── ChunkedCellposeSegmentation.java
├── deeplearning/
│   ├── CellposeInterface.java
│   ├── CellposeParams.java
│   ├── CellposeModel.java (enum)
│   └── DeepLearningException.java
└── utilities/conversion/
    └── LabelMaskConverter.java (labels to MicroObjects)

python/
├── cellpose_server.py
├── cellpose_setup.py
└── requirements.txt

resources/
├── cellpose.properties
└── icons/
    └── cellpose-icon.png
```

## Summary

This architecture provides a robust, extensible integration of Cellpose into VTEA 2.0 with full support for chunked processing of large Zarr volumes. The design prioritizes:

1. **Reliability**: Error handling, auto-recovery, health checks
2. **Performance**: GPU acceleration, efficient data transfer, chunk optimization
3. **Usability**: Clear UI, sensible defaults, good error messages
4. **Extensibility**: Easy to add more deep learning methods using this pattern

The Py4J bridge provides a clean separation between Java and Python while maintaining good performance for typical segmentation workloads.
