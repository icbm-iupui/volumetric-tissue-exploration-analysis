# Cellpose Setup Guide for VTEA 2.0

Complete guide to setting up Cellpose deep learning integration for state-of-the-art cell and tissue segmentation.

**Estimated Time**: 30-60 minutes (depending on GPU drivers)

---

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Installation](#installation)
4. [GPU Configuration](#gpu-configuration)
5. [Testing Installation](#testing-installation)
6. [Starting the Server](#starting-the-server)
7. [Using Cellpose in VTEA](#using-cellpose-in-vtea)
8. [Model Selection](#model-selection)
9. [Performance Optimization](#performance-optimization)
10. [Troubleshooting](#troubleshooting)

---

## Overview

### What is Cellpose?

Cellpose is a state-of-the-art deep learning model for cell and nucleus segmentation developed by the Stringer Lab. It provides:

✅ **High Accuracy**: Trained on diverse cell types and imaging conditions
✅ **Generalization**: Works without retraining on new data
✅ **3D Support**: Native 3D segmentation or 2D stitching
✅ **Multiple Models**: Specialized for cytoplasm, nuclei, tissues

### VTEA Integration

VTEA 2.0 integrates Cellpose via a Python-Java bridge (Py4J):
- Python server runs Cellpose models
- Java client (VTEA) sends images and receives segmentations
- Supports chunked processing for large Zarr volumes
- GPU acceleration for fast processing

---

## Prerequisites

### System Requirements

**Minimum**:
- Python 3.8 or higher
- 8GB RAM
- 5GB disk space (for models)

**Recommended**:
- Python 3.9-3.11
- NVIDIA GPU with 4GB+ VRAM
- CUDA 11.0 or higher
- 16GB+ system RAM

### Check Your System

```bash
# Python version
python --version
# Should show: Python 3.8.x or higher

# Check if GPU is available (NVIDIA only)
nvidia-smi
# Should show your GPU info if NVIDIA card present
```

---

## Installation

### Step 1: Create Python Environment (Recommended)

Using a virtual environment keeps dependencies isolated:

**Option A: Using venv**:
```bash
# Create environment
python -m venv vtea-cellpose-env

# Activate (Linux/Mac)
source vtea-cellpose-env/bin/activate

# Activate (Windows)
vtea-cellpose-env\Scripts\activate
```

**Option B: Using conda**:
```bash
# Create environment
conda create -n vtea-cellpose python=3.10

# Activate
conda activate vtea-cellpose
```

### Step 2: Install Dependencies

**Automatic Installation** (Recommended):
```bash
# Navigate to VTEA directory
cd volumetric-tissue-exploration-analysis/python

# Run setup script
python cellpose_setup.py
```

The setup script will:
1. Install cellpose, torch, numpy, py4j
2. Test all imports
3. Check GPU availability
4. Download a test model
5. Verify everything works

**Manual Installation**:
```bash
# Install PyTorch (choose appropriate command from pytorch.org)
# For CUDA 11.8:
pip install torch torchvision --index-url https://download.pytorch.org/whl/cu118

# For CPU only:
pip install torch torchvision

# Install Cellpose
pip install cellpose

# Install Py4J
pip install py4j

# Verify
python -c "import cellpose; print(cellpose.__version__)"
```

### Step 3: Verify Installation

```bash
# Run setup script
python cellpose_setup.py
```

**Expected Output**:
```
Setting up Cellpose environment for VTEA 2.0...

======================================================================
  Installing Python packages for Cellpose
======================================================================

Installing cellpose>=2.2...
✓ cellpose>=2.2 installed successfully
...

======================================================================
  Testing Installation
======================================================================

✓ Cellpose version: 2.2.3
✓ PyTorch version: 2.0.1
  CUDA available: True
  CUDA version: 11.8
  GPU device: NVIDIA GeForce RTX 3080
✓ NumPy version: 1.24.3
✓ Py4J version: 0.10.9.7

======================================================================
  Testing Cellpose Model Loading
======================================================================

Loading cyto2 model...
✓ Model loaded successfully
Running test segmentation on dummy image...
✓ Test segmentation complete (found 12 objects)

======================================================================
  Setup Complete!
======================================================================

Next steps:
1. Start the Cellpose server:
   python cellpose_server.py

2. In VTEA, select: Segmentation > Deep Learning > Cellpose

3. For GPU support, ensure CUDA is properly installed
   Check: https://pytorch.org/get-started/locally/
```

---

## GPU Configuration

### NVIDIA GPU Setup

**Step 1: Check CUDA Compatibility**

```bash
# Check NVIDIA driver
nvidia-smi

# Check CUDA version
nvcc --version  # Or check nvidia-smi output
```

**Step 2: Install Matching PyTorch**

Visit https://pytorch.org/get-started/locally/ and select:
- PyTorch Build: Stable
- Your OS
- Package: Pip
- Language: Python
- Compute Platform: Your CUDA version

Example for CUDA 11.8:
```bash
pip3 install torch torchvision --index-url https://download.pytorch.org/whl/cu118
```

**Step 3: Verify GPU**

```python
import torch
print("CUDA available:", torch.cuda.is_available())
print("CUDA version:", torch.version.cuda)
print("Device count:", torch.cuda.device_count())
print("Device name:", torch.cuda.get_device_name(0))
```

Expected:
```
CUDA available: True
CUDA version: 11.8
Device count: 1
Device name: NVIDIA GeForce RTX 3080
```

### AMD GPU (Experimental)

AMD GPU support via ROCm is experimental:

```bash
# Install PyTorch with ROCm
pip install torch torchvision --index-url https://download.pytorch.org/whl/rocm5.4.2

# Cellpose may work but not officially supported
```

**Recommendation**: Use CPU mode if AMD GPU issues arise.

### Mac M1/M2 (MPS)

Apple Silicon Macs can use Metal Performance Shaders:

```bash
# Install PyTorch with MPS support
pip install torch torchvision

# In cellpose_server.py, use:
python cellpose_server.py --device mps
```

### CPU-Only Mode

If no GPU available:

```bash
# Force CPU mode
python cellpose_server.py --cpu
```

**Performance**: 5-10x slower than GPU, but still works.

---

## Testing Installation

### Quick Test

```bash
# Test Cellpose directly
python -c "from cellpose import models; model = models.Cellpose(model_type='cyto2', gpu=True); print('Success!')"
```

### Full Test

```bash
# Run comprehensive test
cd volumetric-tissue-exploration-analysis/python
python -c "
from cellpose import models
import numpy as np

# Create dummy image
img = np.random.randint(0, 255, (100, 100), dtype=np.uint8)

# Load model
model = models.Cellpose(model_type='cyto2', gpu=True)

# Segment
masks, flows, styles, diams = model.eval(img, diameter=30, channels=[0,0])

print(f'Found {masks.max()} objects')
print('GPU Test: PASSED')
"
```

---

## Starting the Server

### Basic Startup

```bash
cd volumetric-tissue-exploration-analysis
python python/cellpose_server.py
```

**Expected Output**:
```
Starting Cellpose server on port 25333
Model: cyto2, GPU: True
[2025-10-28 10:30:15] INFO: Loading Cellpose model: cyto2 (GPU=True)
[2025-10-28 10:30:18] INFO: Model loaded successfully
Cellpose server started successfully
Press Ctrl+C to stop
```

### Advanced Options

```bash
# Specify port
python cellpose_server.py --port 25333

# Specify model
python cellpose_server.py --model nuclei

# Force CPU mode
python cellpose_server.py --cpu

# All options
python cellpose_server.py --help
```

**Full Options**:
```
usage: cellpose_server.py [-h] [--port PORT] [--model MODEL] [--gpu] [--cpu]

Cellpose server for VTEA 2.0

optional arguments:
  -h, --help     show this help message
  --port PORT    Py4J port (default: 25333)
  --model MODEL  Cellpose model (default: cyto2)
  --gpu          Use GPU (default: True)
  --cpu          Force CPU mode
```

### Running as Background Service

**Linux/Mac**:
```bash
# Using nohup
nohup python python/cellpose_server.py > cellpose.log 2>&1 &

# Check if running
ps aux | grep cellpose_server

# Stop
pkill -f cellpose_server.py
```

**Windows**:
```bash
# Using start
start /B python python/cellpose_server.py

# Or use Task Scheduler for automatic startup
```

### Server Health Check

While server is running:
```bash
# In another terminal/Python session
from py4j.java_gateway import JavaGateway
gateway = JavaGateway()
python_server = gateway.entry_point

# Test connection
result = python_server.health_check()
print(f"Health: {result}")  # Should print "OK"

# Get version
version = python_server.get_version()
print(f"Cellpose version: {version}")
```

---

## Using Cellpose in VTEA

### Step 1: Verify Connection

1. Start Cellpose server (as shown above)
2. Open VTEA
3. Select **"Chunked Cellpose (Deep Learning)"** method
4. Click **"Test Connection"** button
5. Status should show: **"Status: Connected (Cellpose X.X.X)"**

### Step 2: Configure Parameters

#### Model Selection

| Model | Use Case | Diameter |
|-------|----------|----------|
| cyto2 | General cytoplasm (recommended) | 30 |
| cyto | Original cytoplasm model | 30 |
| nuclei | Nuclear segmentation | 17 |
| tissuenet | Tissue sections | Variable |
| livecell | Live cell imaging | 25 |
| custom | Your trained model | Variable |

#### Parameter Guide

**Diameter** (pixels):
- Set to expected cell/nucleus diameter
- **0 = auto-detect** (slower but automatic)
- Too small: over-segmentation
- Too large: under-segmentation

**Cell Probability Threshold** (-6 to 6):
- Default: 0.0
- Lower (negative): Find more cells (more false positives)
- Higher (positive): Stricter (more false negatives)

**Flow Threshold** (0.0 to 1.0):
- Default: 0.4
- Higher: Stricter cell boundaries
- Lower: More permissive merging

**Processing Mode**:
- **2D (stitching)**: Fast, process slices then stitch
- **3D**: True 3D, slower but better for spherical objects
- **2.5D (pseudo-3D)**: Good compromise

**Hardware**:
- **Use GPU**: Recommended if available
- **CPU mode**: Fallback, 5-10x slower

### Step 3: Run Segmentation

1. Load your image/volume
2. Configure parameters (above)
3. Click **"Segment"**
4. Monitor progress in console
5. Results appear when complete

**Console Output**:
```
[Cellpose] Segmenting 3D volume: 512x512x64
[Cellpose] Found 1,234 objects
Processing chunk 1/8...
Cellpose: Found 156 objects in chunk
...
Stitching 1,234 chunk objects...
Merged to 987 final objects
```

---

## Model Selection

### Pre-trained Models

#### cyto2 (Recommended)
- **Training**: Diverse cell types, fluorescence, brightfield
- **Best for**: General purpose, cytoplasm, whole cells
- **Diameter**: ~30 pixels
- **Examples**: HeLa cells, fibroblasts, neurons

#### nuclei
- **Training**: Nuclear images, DAPI, Hoechst
- **Best for**: Nuclear segmentation
- **Diameter**: ~17 pixels
- **Examples**: DAPI-stained nuclei, H&E nuclei

#### tissuenet
- **Training**: Tissue sections, H&E, immunofluorescence
- **Best for**: Tissue organization, cell types in context
- **Diameter**: Variable
- **Examples**: Tumor sections, organs

#### livecell
- **Training**: Phase contrast live cell imaging
- **Best for**: Label-free live cell imaging
- **Diameter**: ~25 pixels
- **Examples**: Phase contrast, DIC microscopy

### Custom Models

If you have trained your own Cellpose model:

1. **Save model** to a known location
2. **Configure in VTEA**:
   ```
   Model: Custom
   Custom Model Path: /path/to/your/model
   ```

3. **Start server with custom model**:
   ```bash
   python cellpose_server.py --model /path/to/your/model
   ```

### Model Performance Comparison

| Model | Speed (GPU) | Speed (CPU) | Accuracy |
|-------|-------------|-------------|----------|
| cyto2 | ⚡⚡⚡ Fast | 🐢 Slow | ⭐⭐⭐⭐⭐ |
| nuclei | ⚡⚡⚡ Fast | 🐢 Slow | ⭐⭐⭐⭐⭐ |
| tissuenet | ⚡⚡ Medium | 🐢🐢 Very Slow | ⭐⭐⭐⭐ |
| custom | Varies | Varies | Depends |

---

## Performance Optimization

### GPU Memory Management

**If you get "CUDA out of memory" errors**:

1. **Reduce chunk size**:
   ```java
   partitioner.setChunkSize(256, 256, 16);  // Smaller chunks
   ```

2. **Use 2D mode instead of 3D**:
   ```
   Use 3D: unchecked
   ```

3. **Clear GPU cache** (in Python):
   ```python
   import torch
   torch.cuda.empty_cache()
   ```

### Speed Optimization

**For faster processing**:

1. **Use GPU**: 5-10x faster than CPU

2. **Larger chunks**: Fewer stitching operations
   ```java
   partitioner.setChunkSize(512, 512, 64);  // Bigger chunks
   ```

3. **2D mode**: Faster than true 3D
   ```
   Use 3D: unchecked (use 2D stitching)
   ```

4. **Disable augmentation**: Test-time augmentation slower
   ```python
   # In server code, set augment=False
   ```

### Quality vs. Speed Trade-offs

| Setting | Speed | Quality |
|---------|-------|---------|
| 2D mode, GPU, diameter=auto | ⚡⚡⚡ | ⭐⭐⭐ |
| 2D mode, GPU, diameter=fixed | ⚡⚡⚡⚡ | ⭐⭐⭐⭐ |
| 3D mode, GPU, diameter=auto | ⚡⚡ | ⭐⭐⭐⭐⭐ |
| 2D mode, CPU, diameter=fixed | ⚡ | ⭐⭐⭐⭐ |
| 3D mode, CPU, diameter=auto | 🐢 | ⭐⭐⭐⭐⭐ |

**Recommendation**: Start with 2D GPU with fixed diameter for speed, switch to 3D if quality insufficient.

---

## Troubleshooting

### Server Won't Start

#### Error: "ModuleNotFoundError: No module named 'cellpose'"

**Solution**:
```bash
pip install cellpose
# Or if in conda environment:
conda install -c conda-forge cellpose
```

#### Error: "Address already in use"

**Solution**:
```bash
# Kill existing server
pkill -f cellpose_server.py

# Or use different port
python cellpose_server.py --port 25334
```

#### Error: "CUDA not available"

**Solutions**:
1. **Check NVIDIA driver**:
   ```bash
   nvidia-smi
   ```

2. **Reinstall PyTorch with CUDA**:
   ```bash
   pip uninstall torch
   pip install torch --index-url https://download.pytorch.org/whl/cu118
   ```

3. **Use CPU mode**:
   ```bash
   python cellpose_server.py --cpu
   ```

### Connection Issues

#### VTEA shows "Status: Not connected"

**Solutions**:
1. **Check server is running**:
   ```bash
   ps aux | grep cellpose_server  # Linux/Mac
   tasklist | findstr python      # Windows
   ```

2. **Check port**:
   ```bash
   netstat -an | grep 25333  # Should show LISTENING
   ```

3. **Test connection manually**:
   ```python
   from py4j.java_gateway import JavaGateway
   gateway = JavaGateway()
   print(gateway.entry_point.health_check())
   ```

4. **Check firewall**: Ensure port 25333 not blocked

### Segmentation Errors

#### Error: "CUDA out of memory"

**Solutions**:
1. Reduce chunk size (256×256×16)
2. Use 2D mode instead of 3D
3. Switch to CPU mode
4. Close other GPU applications

#### Error: "Model not found"

**Solutions**:
1. **Download model manually**:
   ```python
   from cellpose import models
   model = models.Cellpose(model_type='cyto2')  # Downloads automatically
   ```

2. **Check internet connection**: Models download on first use

3. **Check model cache**:
   ```bash
   # Models stored in:
   # Linux/Mac: ~/.cellpose/models/
   # Windows: C:\Users\<username>\.cellpose\models\
   ```

#### Poor Segmentation Quality

**Solutions**:
1. **Adjust diameter**: Try different values (10-50)
2. **Try different model**: nuclei vs. cyto2 vs. tissuenet
3. **Adjust thresholds**:
   - Lower cell_prob for more cells
   - Raise flow_threshold for stricter boundaries
4. **Use 3D mode**: Better for spherical objects
5. **Check image quality**: Cellpose needs reasonable contrast

### Performance Issues

#### Segmentation very slow

**Solutions**:
1. **Enable GPU**:
   ```bash
   python cellpose_server.py --gpu
   # Verify with nvidia-smi showing python process
   ```

2. **Update PyTorch**:
   ```bash
   pip install --upgrade torch
   ```

3. **Use fixed diameter** (not auto-detect)

4. **Increase chunk size**: 512×512×32 instead of 256×256×16

---

## Advanced Configuration

### Custom Server Configuration

Edit `python/cellpose_server.py`:

```python
# Change default port
DEFAULT_PORT = 25333

# Adjust logging level
logging.basicConfig(level=logging.DEBUG)  # More verbose

# Pre-load multiple models
models_cache = {
    'cyto2': models.Cellpose(model_type='cyto2', gpu=True),
    'nuclei': models.Cellpose(model_type='nuclei', gpu=True),
}
```

### Environment Variables

```bash
# Set Cellpose cache directory
export CELLPOSE_LOCAL_MODELS_PATH=/path/to/models

# Set PyTorch CUDA device
export CUDA_VISIBLE_DEVICES=0  # Use first GPU

# Limit GPU memory
export PYTORCH_CUDA_ALLOC_CONF=max_split_size_mb:512
```

### Model Training

To train custom Cellpose models, see:
- https://cellpose.readthedocs.io/en/latest/train.html
- Cellpose GUI for manual annotation
- Use your trained model with VTEA custom model option

---

## Support

### Documentation
- **Cellpose Docs**: https://cellpose.readthedocs.io/
- **VTEA Quick Start**: [QUICK_START_GUIDE.md](QUICK_START_GUIDE.md)
- **Parameter Tuning**: [PARAMETER_TUNING_GUIDE.md](PARAMETER_TUNING_GUIDE.md)

### Community
- **Cellpose Forum**: https://github.com/MouseLand/cellpose/discussions
- **VTEA Issues**: https://github.com/your-org/vtea/issues
- **Image.sc Forum**: https://forum.image.sc/

### Reporting Issues

When reporting Cellpose-related issues, include:
1. Cellpose version (`python -c "import cellpose; print(cellpose.__version__)"`)
2. PyTorch version (`python -c "import torch; print(torch.__version__)"`)
3. CUDA available? (`python -c "import torch; print(torch.cuda.is_available())"`)
4. Server output (console log)
5. VTEA console output
6. Sample image (if possible)

---

## Summary

After completing this guide, you should have:
- ✅ Python environment with Cellpose installed
- ✅ GPU configured (if available)
- ✅ Cellpose server running
- ✅ VTEA connected to Cellpose
- ✅ Ability to run deep learning segmentation

**Next Steps**:
1. Try different models on your data
2. Tune parameters for optimal results
3. Compare with traditional methods
4. Process large volumes with chunked mode

**Enjoy state-of-the-art deep learning segmentation in VTEA!** 🎉

---

**Version**: 2.0.0
**Last Updated**: 2025-10-28
**Authors**: Seth Winfree, VTEA Team
**License**: GPL v2
