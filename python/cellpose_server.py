#!/usr/bin/env python3
"""
Cellpose Server for VTEA 2.0
Python server that receives segmentation requests from Java via Py4J

Copyright (C) 2023 University of Nebraska
Licensed under GPLv2
"""

import sys
import json
import logging
import numpy as np
from py4j.java_gateway import JavaGateway, CallbackServerParameters, GatewayParameters

# Import Cellpose (will fail gracefully if not installed)
try:
    from cellpose import models, io
    CELLPOSE_AVAILABLE = True
except ImportError:
    CELLPOSE_AVAILABLE = False
    print("WARNING: Cellpose not installed. Run: pip install cellpose")


class CellposeServer:
    """
    Cellpose segmentation server for VTEA
    """

    def __init__(self, model_type='cyto2', gpu=True):
        """
        Initialize Cellpose server

        Args:
            model_type: Model to load ('cyto', 'cyto2', 'nuclei', etc.)
            gpu: Use GPU if available
        """
        self.logger = self._setup_logging()
        self.model = None
        self.model_type = model_type
        self.use_gpu = gpu

        if not CELLPOSE_AVAILABLE:
            self.logger.error("Cellpose is not installed!")
            raise ImportError("Cellpose not available. Install with: pip install cellpose")

        # Initialize model
        self._load_model(model_type, gpu)

    def _setup_logging(self):
        """Setup logging configuration"""
        logging.basicConfig(
            level=logging.INFO,
            format='[%(asctime)s] %(levelname)s: %(message)s',
            datefmt='%Y-%m-%d %H:%M:%S'
        )
        return logging.getLogger('CellposeServer')

    def _load_model(self, model_type, gpu):
        """Load Cellpose model"""
        try:
            self.logger.info(f"Loading Cellpose model: {model_type} (GPU={gpu})")
            self.model = models.Cellpose(model_type=model_type, gpu=gpu)
            self.logger.info(f"Model loaded successfully")
        except Exception as e:
            self.logger.error(f"Failed to load model: {e}")
            raise

    def segment_2d(self, image_bytes, width, height, params_json):
        """
        Segment 2D image

        Args:
            image_bytes: Image data as byte array (float32)
            width: Image width
            height: Image height
            params_json: JSON string with Cellpose parameters

        Returns:
            Label mask as byte array (int32)
        """
        try:
            self.logger.info(f"Segmenting 2D image: {width}x{height}")

            # Deserialize image
            image = self._bytes_to_array_2d(image_bytes, width, height)

            # Parse parameters
            params = json.loads(params_json)

            # Run Cellpose
            masks, flows, styles, diams = self.model.eval(
                image,
                diameter=params.get('diameter', 30),
                channels=params.get('channels', [0, 0]),
                flow_threshold=params.get('flow_threshold', 0.4),
                cellprob_threshold=params.get('cellprob_threshold', 0.0),
                do_3D=False,
                normalize=params.get('normalize', True),
                resample=params.get('resample', True),
                net_avg=params.get('net_avg', True)
            )

            self.logger.info(f"Found {masks.max()} objects")

            # Convert to bytes
            return self._array_to_bytes_2d(masks)

        except Exception as e:
            self.logger.error(f"2D segmentation failed: {e}")
            raise

    def segment_3d(self, image_bytes, width, height, depth, params_json):
        """
        Segment 3D stack

        Args:
            image_bytes: Image data as byte array (float32)
            width: Image width
            height: Image height
            depth: Image depth (number of slices)
            params_json: JSON string with Cellpose parameters

        Returns:
            Label mask as byte array (int32)
        """
        try:
            self.logger.info(f"Segmenting 3D volume: {width}x{height}x{depth}")

            # Deserialize image
            image = self._bytes_to_array_3d(image_bytes, width, height, depth)

            # Parse parameters
            params = json.loads(params_json)
            do_3d = params.get('do_3D', False)

            if do_3d:
                # True 3D segmentation
                masks, flows, styles = self.model.eval(
                    image,
                    diameter=params.get('diameter', 30),
                    channels=params.get('channels', [0, 0]),
                    flow_threshold=params.get('flow_threshold', 0.4),
                    cellprob_threshold=params.get('cellprob_threshold', 0.0),
                    do_3D=True,
                    normalize=params.get('normalize', True),
                    resample=params.get('resample', True),
                    anisotropy=params.get('anisotropy', 1.0)
                )
            else:
                # 2D + stitching (faster, often better)
                stitch_threshold = params.get('stitch_threshold', 0.0)
                if stitch_threshold >= 0:
                    # Stitch 2D masks
                    masks, flows, styles = self.model.eval(
                        image,
                        diameter=params.get('diameter', 30),
                        channels=params.get('channels', [0, 0]),
                        flow_threshold=params.get('flow_threshold', 0.4),
                        cellprob_threshold=params.get('cellprob_threshold', 0.0),
                        do_3D=False,
                        normalize=params.get('normalize', True),
                        resample=params.get('resample', True),
                        stitch_threshold=stitch_threshold
                    )
                else:
                    # Process slice-by-slice without stitching
                    masks = np.zeros((depth, height, width), dtype=np.int32)
                    max_label = 0
                    for z in range(depth):
                        slice_masks, _, _, _ = self.model.eval(
                            image[z],
                            diameter=params.get('diameter', 30),
                            channels=params.get('channels', [0, 0]),
                            flow_threshold=params.get('flow_threshold', 0.4),
                            cellprob_threshold=params.get('cellprob_threshold', 0.0),
                            do_3D=False,
                            normalize=params.get('normalize', True),
                            resample=params.get('resample', True)
                        )
                        # Offset labels to make unique across slices
                        slice_masks[slice_masks > 0] += max_label
                        max_label = slice_masks.max()
                        masks[z] = slice_masks

            self.logger.info(f"Found {masks.max()} objects")

            # Convert to bytes
            return self._array_to_bytes_3d(masks)

        except Exception as e:
            self.logger.error(f"3D segmentation failed: {e}")
            raise

    def _bytes_to_array_2d(self, image_bytes, width, height):
        """Convert byte array to 2D numpy array"""
        # Convert bytes to numpy array
        arr = np.frombuffer(image_bytes, dtype=np.float32)
        return arr.reshape((height, width))

    def _bytes_to_array_3d(self, image_bytes, width, height, depth):
        """Convert byte array to 3D numpy array"""
        arr = np.frombuffer(image_bytes, dtype=np.float32)
        return arr.reshape((depth, height, width))

    def _array_to_bytes_2d(self, array):
        """Convert 2D numpy array to bytes"""
        return array.astype(np.int32).tobytes()

    def _array_to_bytes_3d(self, array):
        """Convert 3D numpy array to bytes"""
        return array.astype(np.int32).tobytes()

    def health_check(self):
        """Simple health check"""
        return "OK"

    def get_version(self):
        """Get Cellpose version"""
        try:
            import cellpose
            return cellpose.__version__
        except:
            return "unknown"

    def get_python_version(self):
        """Get Python version"""
        return sys.version

    def shutdown(self):
        """Clean shutdown"""
        self.logger.info("Shutting down Cellpose server")


def start_server(port=25333, model='cyto2', gpu=True):
    """
    Start Py4J gateway server

    Args:
        port: Port for Py4J communication
        model: Cellpose model to load
        gpu: Use GPU if available
    """
    print(f"Starting Cellpose server on port {port}")
    print(f"Model: {model}, GPU: {gpu}")

    try:
        # Create Cellpose server
        server = CellposeServer(model_type=model, gpu=gpu)

        # Start Py4J gateway
        gateway = JavaGateway(
            gateway_parameters=GatewayParameters(port=port),
            callback_server_parameters=CallbackServerParameters(port=port),
            python_server_entry_point=server
        )

        print("Cellpose server started successfully")
        print("Press Ctrl+C to stop")

        # Keep server running
        try:
            import time
            while True:
                time.sleep(1)
        except KeyboardInterrupt:
            print("\nShutting down...")
            server.shutdown()
            gateway.shutdown()

    except Exception as e:
        print(f"ERROR: Failed to start server: {e}")
        sys.exit(1)


if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description='Cellpose server for VTEA 2.0')
    parser.add_argument('--port', type=int, default=25333, help='Py4J port (default: 25333)')
    parser.add_argument('--model', default='cyto2', help='Cellpose model (default: cyto2)')
    parser.add_argument('--gpu', action='store_true', default=True, help='Use GPU (default: True)')
    parser.add_argument('--cpu', action='store_false', dest='gpu', help='Force CPU mode')

    args = parser.parse_args()

    # Check Cellpose installation
    if not CELLPOSE_AVAILABLE:
        print("ERROR: Cellpose is not installed!")
        print("Install with: pip install cellpose")
        sys.exit(1)

    start_server(port=args.port, model=args.model, gpu=args.gpu)
