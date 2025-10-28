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
 * Chunked version of SingleThreshold segmentation.
 * Creates a single large object containing all pixels above threshold.
 *
 * This is the simplest segmentation method - useful as a baseline
 * or for very large regions where connected components are not needed.
 *
 * @author sethwinfree
 */
@Plugin(type = Segmentation.class)
public class ChunkedSingleThreshold extends AbstractChunkedSegmentation<Component, Object> {

    private int lowThreshold = 127;
    private int channel = 0;

    /**
     * Constructor
     */
    public ChunkedSingleThreshold() {
        super();
        this.NAME = "Chunked Single Threshold 3D";
        this.KEY = "CHUNKEDSINGLETHRESHOLD3D";
        this.TYPE = "Segmentation";
        this.VERSION = "0.1";
        this.AUTHOR = "Seth Winfree";
        this.COMMENT = "Simple threshold for large regions by intensity (chunked processing).";
    }

    @Override
    protected List<MicroObject> processChunk(Chunk chunk, List details, boolean calculate) {
        try {
            // Extract parameters
            extractParameters(details);

            // Get chunk data
            ImageStack chunkStack = chunk.getData();
            if (chunkStack == null) {
                System.err.println("Chunk " + chunk.getChunkId() + " has no data");
                return new ArrayList<>();
            }

            // Collect all pixels above threshold
            ArrayList<Integer> xPixels = new ArrayList<>();
            ArrayList<Integer> yPixels = new ArrayList<>();
            ArrayList<Integer> zPixels = new ArrayList<>();

            int width = chunkStack.getWidth();
            int height = chunkStack.getHeight();
            int depth = chunkStack.getSize();

            // Scan all pixels in chunk
            for (int z = 0; z < depth; z++) {
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        double voxelValue = chunkStack.getVoxel(x, y, z);
                        if (voxelValue >= lowThreshold) {
                            xPixels.add(x);
                            yPixels.add(y);
                            zPixels.add(z);
                        }
                    }
                }
            }

            // Create MicroObject from pixels
            List<MicroObject> chunkObjects = new ArrayList<>();

            if (!xPixels.isEmpty()) {
                int[] xArray = new int[xPixels.size()];
                int[] yArray = new int[yPixels.size()];
                int[] zArray = new int[zPixels.size()];

                for (int i = 0; i < xPixels.size(); i++) {
                    xArray[i] = xPixels.get(i);
                    yArray[i] = yPixels.get(i);
                    zArray[i] = zPixels.get(i);
                }

                // Create MicroObject (one object per chunk containing all threshold pixels)
                MicroObject obj = new MicroObject(
                    xArray, yArray, zArray,
                    channel,
                    new ImageStack[]{chunkStack},
                    chunk.getChunkId()
                );

                chunkObjects.add(obj);

                // Transform to global coordinates
                transformToGlobalCoordinates(chunkObjects, chunk);

                // Tag as boundary object (single threshold objects always merge)
                tagBoundaryObjects(chunkObjects, chunk);
            }

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
            // Extract from protocol structure
            if (details.size() > 2) {
                channel = (Integer) details.get(2);
            }

            if (details.size() > 3) {
                ArrayList componentList = (ArrayList) details.get(3);
                if (componentList != null && componentList.size() > 1) {
                    Object component = componentList.get(1);
                    if (component instanceof JTextField) {
                        lowThreshold = Integer.parseInt(((JTextField) component).getText());
                    } else if (component instanceof String) {
                        lowThreshold = Integer.parseInt((String) component);
                    } else if (component instanceof Integer) {
                        lowThreshold = (Integer) component;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting parameters, using defaults: " + e.getMessage());
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
     * Tag objects as boundary objects (for SingleThreshold, all chunks merge into one object)
     */
    private void tagBoundaryObjects(List<MicroObject> objects, Chunk chunk) {
        // For SingleThreshold, all chunk objects should be merged
        // Mark all objects as boundary objects
        for (MicroObject obj : objects) {
            obj.setProperty("chunkBoundary", true);

            // Store bounding box
            double[] bbox = calculateBoundingBox(obj);
            if (bbox != null) {
                obj.setProperty("boundingBox", bbox);
            }
        }
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

        // For SingleThreshold, we want to merge ALL chunk objects into ONE large object
        // This is different from other methods that preserve individual objects

        // Use ObjectStitcher with very permissive parameters to merge everything
        ObjectStitcher stitcher = new ObjectStitcher();

        // Large distance threshold - we want to merge all chunks
        stitcher.setDistanceThreshold(Double.MAX_VALUE);

        // Zero overlap requirement - just merge everything marked as boundary
        stitcher.setOverlapThreshold(0.0);

        // No intensity correlation needed
        stitcher.setUseIntensityCorrelation(false);

        List<MicroObject> stitched = stitcher.stitchObjects(chunkObjects);

        // Log statistics
        int totalPixels = 0;
        for (MicroObject obj : chunkObjects) {
            totalPixels += obj.getVoxelCount();
        }

        System.out.println("SingleThreshold: Merged " + chunkObjects.size() +
                          " chunk objects into " + stitched.size() +
                          " object(s) with " + totalPixels + " total pixels");

        return stitched;
    }

    @Override
    protected boolean processTraditional(ImagePlus imp, List details, boolean calculate) {
        // For non-chunked data, process entire image like original SingleThreshold
        try {
            extractParameters(details);

            ImageStack stack = imp.getStack();

            // Collect all pixels above threshold
            ArrayList<Integer> xPixels = new ArrayList<>();
            ArrayList<Integer> yPixels = new ArrayList<>();
            ArrayList<Integer> zPixels = new ArrayList<>();

            for (int z = 0; z < stack.getSize(); z++) {
                for (int y = 0; y < stack.getHeight(); y++) {
                    for (int x = 0; x < stack.getWidth(); x++) {
                        if (stack.getVoxel(x, y, z) >= lowThreshold) {
                            xPixels.add(x);
                            yPixels.add(y);
                            zPixels.add(z);
                        }
                    }
                }
            }

            // Create MicroObject
            chunkResults.clear();
            if (!xPixels.isEmpty()) {
                int[] xArray = new int[xPixels.size()];
                int[] yArray = new int[yPixels.size()];
                int[] zArray = new int[zPixels.size()];

                for (int i = 0; i < xPixels.size(); i++) {
                    xArray[i] = xPixels.get(i);
                    yArray[i] = yPixels.get(i);
                    zArray[i] = zPixels.get(i);
                }

                MicroObject obj = new MicroObject(xArray, yArray, zArray, channel,
                                                 new ImageStack[]{stack}, 0);
                chunkResults.add(obj);

                System.out.println("SingleThreshold: Found " + xArray.length + " pixels above threshold");
            }

            return true;
        } catch (Exception e) {
            System.err.println("SingleThreshold processing failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected boolean processTraditional(ImageStack[] stacks, List details, boolean calculate) {
        if (stacks == null || stacks.length == 0) {
            return false;
        }

        ImagePlus imp = new ImagePlus("temp", stacks[channel]);
        return processTraditional(imp, details, calculate);
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
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Threshold input
        JPanel thresholdPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        thresholdPanel.add(new JLabel("Low Threshold:"));
        JTextField thresholdField = new JTextField(String.valueOf(lowThreshold), 10);
        thresholdPanel.add(thresholdField);
        panel.add(thresholdPanel);

        // Info label
        JLabel infoLabel = new JLabel("<html><i>Simple threshold - creates one large object with all pixels above threshold</i></html>");
        panel.add(infoLabel);

        // Warning label
        JLabel warningLabel = new JLabel("<html><b>Note:</b> Does not separate individual objects (use FloodFill or LayerCake for that)</html>");
        panel.add(warningLabel);

        return panel;
    }

    @Override
    public String getProgressDetail() {
        if (useChunkedProcessing) {
            return getChunkProcessingStats();
        } else {
            if (chunkResults.isEmpty()) {
                return "SingleThreshold: No pixels above threshold";
            } else {
                return String.format("SingleThreshold: %d pixels above threshold",
                                   chunkResults.get(0).getVoxelCount());
            }
        }
    }

    @Override
    public void setParameters(List<Object> parameters) {
        extractParameters(parameters);
    }

    @Override
    public String toString() {
        return NAME + " [" + (useChunkedProcessing ? "Chunked Mode" : "Traditional Mode") +
               ", threshold=" + lowThreshold + "]";
    }
}
