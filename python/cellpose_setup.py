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

def print_header(text):
    """Print formatted header"""
    print("\n" + "=" * 70)
    print(f"  {text}")
    print("=" * 70)

def install_packages():
    """Install required packages"""
    print_header("Installing Python packages for Cellpose")

    for package in REQUIRED_PACKAGES:
        print(f"\nInstalling {package}...")
        try:
            subprocess.check_call([
                sys.executable, "-m", "pip", "install", package
            ])
            print(f"✓ {package} installed successfully")
        except subprocess.CalledProcessError as e:
            print(f"✗ Failed to install {package}: {e}")
            return False

    return True

def test_installation():
    """Test that all packages are importable"""
    print_header("Testing Installation")

    all_ok = True

    # Test cellpose
    try:
        import cellpose
        print(f"✓ Cellpose version: {cellpose.__version__}")
    except ImportError as e:
        print(f"✗ Cellpose import failed: {e}")
        all_ok = False

    # Test PyTorch
    try:
        import torch
        print(f"✓ PyTorch version: {torch.__version__}")
        print(f"  CUDA available: {torch.cuda.is_available()}")
        if torch.cuda.is_available():
            print(f"  CUDA version: {torch.version.cuda}")
            print(f"  GPU device: {torch.cuda.get_device_name(0)}")
    except ImportError as e:
        print(f"✗ PyTorch import failed: {e}")
        all_ok = False

    # Test numpy
    try:
        import numpy
        print(f"✓ NumPy version: {numpy.__version__}")
    except ImportError as e:
        print(f"✗ NumPy import failed: {e}")
        all_ok = False

    # Test py4j
    try:
        import py4j
        print(f"✓ Py4J version: {py4j.version.__version__}")
    except ImportError as e:
        print(f"✗ Py4J import failed: {e}")
        all_ok = False

    return all_ok

def test_cellpose_model():
    """Test loading a Cellpose model"""
    print_header("Testing Cellpose Model Loading")

    try:
        from cellpose import models
        import numpy as np

        print("Loading cyto2 model...")
        model = models.Cellpose(model_type='cyto2', gpu=False)
        print("✓ Model loaded successfully")

        # Test with dummy image
        print("Running test segmentation on dummy image...")
        dummy_image = np.random.randint(0, 255, (100, 100), dtype=np.uint8)
        masks, flows, styles, diams = model.eval(
            dummy_image,
            diameter=30,
            channels=[0, 0]
        )
        print(f"✓ Test segmentation complete (found {masks.max()} objects)")

        return True

    except Exception as e:
        print(f"✗ Model test failed: {e}")
        return False

def main():
    """Main setup function"""
    print_header("VTEA 2.0 Cellpose Setup")
    print("This script will install and test Cellpose integration")

    # Install packages
    if not install_packages():
        print("\n✗ Installation failed. Please check error messages above.")
        sys.exit(1)

    # Test installation
    if not test_installation():
        print("\n✗ Installation test failed. Please check error messages above.")
        sys.exit(1)

    # Test Cellpose model
    if not test_cellpose_model():
        print("\n⚠ Model test failed, but packages are installed.")
        print("   This might be due to network issues downloading models.")
        print("   Try running cellpose_server.py manually to download models.")

    print_header("Setup Complete!")
    print("\nNext steps:")
    print("1. Start the Cellpose server:")
    print("   python cellpose_server.py")
    print("\n2. In VTEA, select: Segmentation > Deep Learning > Cellpose")
    print("\n3. For GPU support, ensure CUDA is properly installed")
    print("   Check: https://pytorch.org/get-started/locally/")

    return 0

if __name__ == "__main__":
    sys.exit(main())
