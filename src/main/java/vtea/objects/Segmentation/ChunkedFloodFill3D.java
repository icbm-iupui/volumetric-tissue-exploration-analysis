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
import vtea.objects.floodfill3D.FloodFill3D;
import vtea.partition.Chunk;
import vtea.partition.ObjectStitcher;
import vteaobjects.MicroObject;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Chunked version of FloodFill3D segmentation.
 * Processes large volumes in chunks using 3D flood fill algorithm.
 *
 * This adapter works directly with FloodFill3D class for chunk processing,
 * then transforms coordinates to global space and stitches boundary objects.
 *
 * Note: This works directly with FloodFill3D rather than FloodFill3DSingleThreshold
 * because the wrapper class has an incomplete getObjects() implementation.
 *
 * @author sethwinfree
 */
@Plugin(type = Segmentation.class)
public class ChunkedFloodFill3D extends AbstractChunkedSegmentation<Component, Object> {

    // Processing parameters
    private int channel = 0;
    private int minThreshold = 127;
    private int minObjectSize = 10;
    private int maxObjectSize = Integer.MAX_VALUE;
    private boolean useWatershed = true;

    /**
     * Constructor
     */
    public ChunkedFloodFill3D() {
        super();
        this.NAME = "Chunked FloodFill 3D";
        this.KEY = "CHUNKEDFLOODFILL3D";
        this.TYPE = "Segmentation";
        this.VERSION = "0.1";
        this.AUTHOR = "Seth Winfree";
        this.COMMENT = "Chunked 3D flood fill algorithm for large volumes.";
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

            // Create ImageStack array for FloodFill3D
            ImageStack[] stacks = new ImageStack[1];
            stacks[0] = chunkStack;

            // Create minConstants array for FloodFill3D
            // 0: minObjectSize, 1: maxObjectSize, 2: minOverlap, 3: minThreshold
            int[] minConstants = new int[4];
            minConstants[0] = minObjectSize;
            minConstants[1] = maxObjectSize;
            minConstants[2] = 0; // minOverlap not used in basic floodfill
            minConstants[3] = minThreshold;

            // Create FloodFill3D processor for this chunk
            FloodFill3D floodFill = new FloodFill3D(stacks, 0, minConstants, false);
            floodFill.setWaterShedImageJ(useWatershed);

            // Get results as ArrayList of MicroObjects
            ArrayList<MicroObject> chunkObjects = floodFill.getVolumesAsList();

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
                minThreshold = (Integer) details.get(index++);
            }
            if (details.size() > index && details.get(index) instanceof Integer) {
                minObjectSize = (Integer) details.get(index++);
            }
            if (details.size() > index && details.get(index) instanceof Integer) {
                maxObjectSize = (Integer) details.get(index++);
            }
            if (details.size() > index && details.get(index) instanceof Boolean) {
                useWatershed = (Boolean) details.get(index++);
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

        // Configure stitcher for FloodFill3D characteristics
        ObjectStitcher stitcher = new ObjectStitcher();

        // FloodFill uses 26-connectivity, so use slightly larger distance threshold
        stitcher.setDistanceThreshold(8.0); // Allow for diagonal connections

        // Require moderate overlap to merge (flood fill creates dense regions)
        stitcher.setOverlapThreshold(0.15); // 15% overlap minimum

        // Don't use intensity correlation (threshold-based method)
        stitcher.setUseIntensityCorrelation(false);

        List<MicroObject> stitched = stitcher.stitchObjects(chunkObjects);

        // Log stitching statistics
        String stats = stitcher.getStitchingStats(chunkObjects.size(), stitched.size());
        System.out.println(stats);

        return stitched;
    }

    @Override
    protected boolean processTraditional(ImagePlus imp, List details, boolean calculate) {
        // For non-chunked data, use FloodFill3D directly
        try {
            extractParameters(details);

            ImageStack stack = imp.getStack();
            ImageStack[] stacks = new ImageStack[1];
            stacks[0] = stack;

            int[] minConstants = new int[4];
            minConstants[0] = minObjectSize;
            minConstants[1] = maxObjectSize;
            minConstants[2] = 0;
            minConstants[3] = minThreshold;

            FloodFill3D floodFill = new FloodFill3D(stacks, 0, minConstants, false);
            floodFill.setWaterShedImageJ(useWatershed);

            // Store results
            chunkResults.clear();
            chunkResults.addAll(floodFill.getVolumesAsList());

            return true;
        } catch (Exception e) {
            System.err.println("Error in traditional processing: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected boolean processTraditional(ImageStack[] stacks, List details, boolean calculate) {
        // For non-chunked data, use FloodFill3D directly
        try {
            extractParameters(details);

            int[] minConstants = new int[4];
            minConstants[0] = minObjectSize;
            minConstants[1] = maxObjectSize;
            minConstants[2] = 0;
            minConstants[3] = minThreshold;

            FloodFill3D floodFill = new FloodFill3D(stacks, channel, minConstants, false);
            floodFill.setWaterShedImageJ(useWatershed);

            // Store results
            chunkResults.clear();
            chunkResults.addAll(floodFill.getVolumesAsList());

            return true;
        } catch (Exception e) {
            System.err.println("Error in traditional processing: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public ArrayList<MicroObject> getObjects() {
        if (useChunkedProcessing) {
            return super.getObjects(); // Returns stitched results
        } else {
            return new ArrayList<>(chunkResults);
        }
    }

    @Override
    public Component getUI() {
        // Create UI panel for parameters
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Threshold
        JPanel thresholdPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        thresholdPanel.add(new JLabel("Min Threshold:"));
        JTextField thresholdField = new JTextField(String.valueOf(minThreshold), 10);
        thresholdPanel.add(thresholdField);
        panel.add(thresholdPanel);

        // Min size
        JPanel minSizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        minSizePanel.add(new JLabel("Min Size (voxels):"));
        JTextField minSizeField = new JTextField(String.valueOf(minObjectSize), 10);
        minSizePanel.add(minSizeField);
        panel.add(minSizePanel);

        // Max size
        JPanel maxSizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        maxSizePanel.add(new JLabel("Max Size (voxels):"));
        JTextField maxSizeField = new JTextField(String.valueOf(maxObjectSize), 10);
        maxSizePanel.add(maxSizeField);
        panel.add(maxSizePanel);

        // Watershed option
        JPanel watershedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JCheckBox watershedCheckbox = new JCheckBox("Use Watershed", useWatershed);
        watershedPanel.add(watershedCheckbox);
        panel.add(watershedPanel);

        // Info label
        JLabel infoLabel = new JLabel("<html><i>Chunked 3D FloodFill with 26-connectivity</i></html>");
        panel.add(infoLabel);

        return panel;
    }

    @Override
    public String getProgressDetail() {
        if (useChunkedProcessing) {
            return getChunkProcessingStats();
        } else {
            return String.format("FloodFill3D: %d objects found", chunkResults.size());
        }
    }

    @Override
    public void setParameters(List<Object> parameters) {
        extractParameters(parameters);
    }

    @Override
    public String toString() {
        return NAME + " [" + (useChunkedProcessing ? "Chunked Mode" : "Traditional Mode") + "]";
    }
}
