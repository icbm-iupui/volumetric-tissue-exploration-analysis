/*
 * Copyright (C) 2020 Indiana University and 2023 University of Nebraska
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
package vtea.objects.Segmentation;

import ij.ImagePlus;
import ij.ImageStack;
import org.scijava.plugin.Plugin;
import vtea.dataset.volume.ChunkedVolumeDataset;
import vtea.partition.Chunk;
import vtea.partition.ObjectStitcher;
import vteaobjects.MicroObject;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Chunked version of LayerCake3DSingleThreshold segmentation.
 * Processes large volumes in chunks using simple distance-based region connection.
 *
 * This adapter delegates to LayerCake3DSingleThreshold for chunk processing,
 * then transforms coordinates to global space and stitches boundary objects.
 *
 * @author sethwinfree
 */
@Plugin(type = Segmentation.class)
public class ChunkedLayerCake3D extends AbstractChunkedSegmentation<Component, Object> {

    private final LayerCake3DSingleThreshold traditional;

    // Processing parameters (extracted from details list)
    private int channel = 0;
    private int threshold = 127;
    private int minSize = 10;
    private int maxSize = Integer.MAX_VALUE;
    private boolean is2D = false;

    /**
     * Constructor
     */
    public ChunkedLayerCake3D() {
        super();
        this.traditional = new LayerCake3DSingleThreshold();
        this.NAME = "Chunked LayerCake 3D";
        this.KEY = "CHUNKEDLAYERCAKE3D";
        this.TYPE = "Segmentation";
    }

    @Override
    protected List<MicroObject> processChunk(Chunk chunk, List details, boolean calculate) {
        try {
            // Extract parameters from details list
            extractParameters(details);

            // Get chunk data
            ImageStack chunkStack = chunk.getData();
            if (chunkStack == null) {
                System.err.println("Chunk " + chunk.getChunkId() + " has no data");
                return new ArrayList<>();
            }

            // Create ImageStack array for the traditional processor
            ImageStack[] stacks = new ImageStack[1];
            stacks[0] = chunkStack;

            // Create details list for chunk processing (in chunk-local coordinates)
            List<Object> chunkDetails = new ArrayList<>();
            chunkDetails.add(0); // channel index (always 0 since we have single channel stack)
            chunkDetails.add(threshold);
            chunkDetails.add(minSize);
            chunkDetails.add(maxSize);
            chunkDetails.add(is2D);

            // Create temporary processor for this chunk
            LayerCake3DSingleThreshold tempProcessor = new LayerCake3DSingleThreshold();

            // Process chunk with traditional algorithm
            boolean success = tempProcessor.process(stacks, chunkDetails, calculate);

            if (!success) {
                System.err.println("Failed to process chunk " + chunk.getChunkId());
                return new ArrayList<>();
            }

            // Get results from processor
            ArrayList<MicroObject> chunkObjects = tempProcessor.getObjects();

            if (chunkObjects == null || chunkObjects.isEmpty()) {
                return new ArrayList<>();
            }

            // Transform from chunk-local to global coordinates
            transformToGlobalCoordinates(chunkObjects, chunk);

            // Tag objects near chunk boundaries for stitching
            tagBoundaryObjects(chunkObjects, chunk);

            return chunkObjects;

        } catch (Exception e) {
            System.err.println("Error processing chunk " + chunk.getChunkId() + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Extract processing parameters from details list
     */
    private void extractParameters(List details) {
        if (details == null || details.isEmpty()) {
            return;
        }

        try {
            int index = 0;
            if (details.size() > index && details.get(index) instanceof Integer) {
                channel = (Integer) details.get(index++);
            }
            if (details.size() > index && details.get(index) instanceof Integer) {
                threshold = (Integer) details.get(index++);
            }
            if (details.size() > index && details.get(index) instanceof Integer) {
                minSize = (Integer) details.get(index++);
            }
            if (details.size() > index && details.get(index) instanceof Integer) {
                maxSize = (Integer) details.get(index++);
            }
            if (details.size() > index && details.get(index) instanceof Boolean) {
                is2D = (Boolean) details.get(index++);
            }
        } catch (Exception e) {
            System.err.println("Error extracting parameters: " + e.getMessage());
        }
    }

    /**
     * Transform object coordinates from chunk-local to global volume space
     */
    private void transformToGlobalCoordinates(List<MicroObject> objects, Chunk chunk) {
        long[] globalStart = chunk.getGlobalStart();

        for (MicroObject obj : objects) {
            // Transform pixel coordinates
            int[] pixelsX = obj.getPixelsX();
            int[] pixelsY = obj.getPixelsY();
            int[] pixelsZ = obj.getPixelsZ();

            if (pixelsX != null && pixelsY != null && pixelsZ != null) {
                for (int i = 0; i < pixelsX.length; i++) {
                    pixelsX[i] += (int) globalStart[0];
                    pixelsY[i] += (int) globalStart[1];
                    pixelsZ[i] += (int) globalStart[2];
                }
            }

            // Transform centroid
            double[] centroid = obj.getCentroidXYZ_AsDbl();
            if (centroid != null && centroid.length >= 3) {
                centroid[0] += globalStart[0];
                centroid[1] += globalStart[1];
                centroid[2] += globalStart[2];
                obj.setCentroidXYZ(centroid);
            }

            // Store chunk information
            obj.setProperty("chunkId", chunk.getChunkId());
        }
    }

    /**
     * Tag objects that are near chunk boundaries for stitching
     */
    private void tagBoundaryObjects(List<MicroObject> objects, Chunk chunk) {
        for (MicroObject obj : objects) {
            boolean isNearBoundary = isNearChunkBoundary(obj, chunk);
            obj.setProperty("chunkBoundary", isNearBoundary);

            // Store bounding box for efficient stitching
            double[] bbox = calculateBoundingBox(obj);
            if (bbox != null) {
                obj.setProperty("boundingBox", bbox);
            }
        }
    }

    /**
     * Check if object is near chunk boundary
     */
    private boolean isNearChunkBoundary(MicroObject obj, Chunk chunk) {
        double[] centroid = obj.getCentroidXYZ_AsDbl();
        if (centroid == null || centroid.length < 3) {
            return false;
        }

        long[] globalStart = chunk.getGlobalStart();
        long[] globalEnd = chunk.getGlobalEnd();

        // Use a margin based on object size
        double margin = Math.cbrt(obj.getVoxelCount()) * 2; // 2x radius

        // Check if near any boundary
        boolean nearXMin = centroid[0] - globalStart[0] < margin;
        boolean nearXMax = globalEnd[0] - centroid[0] < margin;
        boolean nearYMin = centroid[1] - globalStart[1] < margin;
        boolean nearYMax = globalEnd[1] - centroid[1] < margin;
        boolean nearZMin = centroid[2] - globalStart[2] < margin;
        boolean nearZMax = globalEnd[2] - centroid[2] < margin;

        return nearXMin || nearXMax || nearYMin || nearYMax || nearZMin || nearZMax;
    }

    /**
     * Calculate bounding box for object [xmin, ymin, zmin, xmax, ymax, zmax]
     */
    private double[] calculateBoundingBox(MicroObject obj) {
        int[] pixelsX = obj.getPixelsX();
        int[] pixelsY = obj.getPixelsY();
        int[] pixelsZ = obj.getPixelsZ();

        if (pixelsX == null || pixelsY == null || pixelsZ == null || pixelsX.length == 0) {
            return null;
        }

        double xMin = Double.MAX_VALUE, xMax = Double.MIN_VALUE;
        double yMin = Double.MAX_VALUE, yMax = Double.MIN_VALUE;
        double zMin = Double.MAX_VALUE, zMax = Double.MIN_VALUE;

        for (int i = 0; i < pixelsX.length; i++) {
            xMin = Math.min(xMin, pixelsX[i]);
            xMax = Math.max(xMax, pixelsX[i]);
            yMin = Math.min(yMin, pixelsY[i]);
            yMax = Math.max(yMax, pixelsY[i]);
            zMin = Math.min(zMin, pixelsZ[i]);
            zMax = Math.max(zMax, pixelsZ[i]);
        }

        return new double[]{xMin, yMin, zMin, xMax, yMax, zMax};
    }

    @Override
    protected List<MicroObject> stitchChunks(List<MicroObject> chunkObjects,
                                            ChunkedVolumeDataset dataset) {
        if (chunkObjects == null || chunkObjects.isEmpty()) {
            return new ArrayList<>();
        }

        // Configure stitcher for LayerCake3D characteristics
        ObjectStitcher stitcher = new ObjectStitcher();

        // LayerCake3D uses simpler distance-based connection
        // Use smaller distance threshold than kDTree version
        stitcher.setDistanceThreshold(5.0); // Conservative distance for simple method

        // Require significant overlap to merge
        stitcher.setOverlapThreshold(0.1); // 10% overlap minimum

        // Don't use intensity correlation (LayerCake3D is threshold-based)
        stitcher.setUseIntensityCorrelation(false);

        List<MicroObject> stitched = stitcher.stitchObjects(chunkObjects);

        // Log stitching statistics
        String stats = stitcher.getStitchingStats(chunkObjects.size(), stitched.size());
        System.out.println(stats);

        return stitched;
    }

    @Override
    protected boolean processTraditional(ImagePlus imp, List details, boolean calculate) {
        // For non-chunked data, delegate directly to traditional processor
        return traditional.process(imp, details, calculate);
    }

    @Override
    protected boolean processTraditional(ImageStack[] stacks, List details, boolean calculate) {
        // For non-chunked data, delegate directly to traditional processor
        return traditional.process(stacks, details, calculate);
    }

    @Override
    public ArrayList<MicroObject> getObjects() {
        if (useChunkedProcessing) {
            return super.getObjects(); // Returns stitched results
        } else {
            return traditional.getObjects();
        }
    }

    @Override
    public Component getUI() {
        // Create UI panel for parameters
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Threshold
        JPanel thresholdPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        thresholdPanel.add(new JLabel("Threshold:"));
        JTextField thresholdField = new JTextField(String.valueOf(threshold), 10);
        thresholdPanel.add(thresholdField);
        panel.add(thresholdPanel);

        // Min size
        JPanel minSizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        minSizePanel.add(new JLabel("Min Size (voxels):"));
        JTextField minSizeField = new JTextField(String.valueOf(minSize), 10);
        minSizePanel.add(minSizeField);
        panel.add(minSizePanel);

        // Max size
        JPanel maxSizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        maxSizePanel.add(new JLabel("Max Size (voxels):"));
        JTextField maxSizeField = new JTextField(String.valueOf(maxSize), 10);
        maxSizePanel.add(maxSizeField);
        panel.add(maxSizePanel);

        // 2D mode
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JCheckBox mode2D = new JCheckBox("2D Mode (process slices independently)", is2D);
        modePanel.add(mode2D);
        panel.add(modePanel);

        // Info label
        JLabel infoLabel = new JLabel("<html><i>Chunked LayerCake 3D with simple distance-based connection</i></html>");
        panel.add(infoLabel);

        return panel;
    }

    @Override
    public String getProgressDetail() {
        if (useChunkedProcessing) {
            return getChunkProcessingStats();
        } else {
            return traditional.getProgressDetail();
        }
    }

    @Override
    public void setParameters(List<Object> parameters) {
        extractParameters(parameters);
        if (!useChunkedProcessing) {
            traditional.setParameters(parameters);
        }
    }

    @Override
    public String toString() {
        return NAME + " [" + (useChunkedProcessing ? "Chunked Mode" : "Traditional Mode") + "]";
    }
}
