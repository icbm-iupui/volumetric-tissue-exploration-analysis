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
import ij.process.ImageProcessor;
import org.scijava.plugin.Plugin;
import vtea.dataset.volume.ChunkedVolumeDataset;
import vtea.deeplearning.CellposeInterface;
import vtea.deeplearning.CellposeModel;
import vtea.deeplearning.CellposeParams;
import vtea.deeplearning.DeepLearningException;
import vtea.partition.Chunk;
import vtea.partition.ObjectStitcher;
import vteaobjects.MicroObject;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Chunked Cellpose segmentation using deep learning.
 * Processes large volumes in chunks using Cellpose models via Python bridge.
 *
 * @author sethwinfree
 */
@Plugin(type = Segmentation.class)
public class ChunkedCellposeSegmentation extends AbstractChunkedSegmentation<Component, Object> {

    private CellposeInterface cellposeInterface;
    private CellposeParams params;

    // UI components for parameter configuration
    private JComboBox<CellposeModel> modelComboBox;
    private JTextField diameterField;
    private JSlider cellprobSlider;
    private JSlider flowSlider;
    private JCheckBox use3DCheckbox;
    private JCheckBox useGPUCheckbox;
    private JButton testConnectionButton;
    private JLabel statusLabel;

    /**
     * Constructor
     */
    public ChunkedCellposeSegmentation() {
        super();
        this.NAME = "Chunked Cellpose (Deep Learning)";
        this.KEY = "CHUNKEDCELLPOSE";
        this.TYPE = "Segmentation";
        this.VERSION = "1.0";
        this.AUTHOR = "Seth Winfree";
        this.COMMENT = "Deep learning segmentation using Cellpose with chunked processing.";

        // Initialize with default parameters
        this.params = CellposeParams.createCytoDefault();

        // Initialize Cellpose interface
        this.cellposeInterface = new CellposeInterface();
    }

    @Override
    protected List<MicroObject> processChunk(Chunk chunk, List details, boolean calculate) {
        try {
            // Extract parameters from details if provided
            if (details != null && !details.isEmpty()) {
                extractParameters(details);
            }

            // Ensure Cellpose server is running
            if (!cellposeInterface.isServerRunning()) {
                try {
                    cellposeInterface.startServer();
                } catch (DeepLearningException e) {
                    System.err.println("Failed to start Cellpose server: " + e.getUserMessage());
                    return new ArrayList<>();
                }
            }

            // Get chunk data
            ImageStack chunkStack = chunk.getData();
            if (chunkStack == null) {
                System.err.println("Chunk " + chunk.getChunkId() + " has no data");
                return new ArrayList<>();
            }

            // Run Cellpose segmentation
            int[][][] labelMask;
            if (params.isDo3D() && chunkStack.getSize() > 1) {
                // True 3D segmentation
                labelMask = cellposeInterface.segment3D(chunkStack, params);
            } else {
                // 2D slice-by-slice segmentation
                labelMask = segment2DStack(chunkStack);
            }

            // Convert label mask to MicroObjects
            List<MicroObject> chunkObjects = labelsToObjects(labelMask, chunkStack);

            // Transform from chunk-local to global coordinates
            transformToGlobalCoordinates(chunkObjects, chunk);

            // Tag objects near chunk boundaries for stitching
            tagBoundaryObjects(chunkObjects, chunk);

            return chunkObjects;

        } catch (DeepLearningException e) {
            System.err.println("Cellpose segmentation failed for chunk " + chunk.getChunkId() + ": " + e.getUserMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error processing chunk " + chunk.getChunkId() + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Segment 2D stack slice-by-slice
     */
    private int[][][] segment2DStack(ImageStack stack) throws DeepLearningException {
        int width = stack.getWidth();
        int height = stack.getHeight();
        int depth = stack.getSize();
        int[][][] labelMask = new int[depth][height][width];

        int maxLabel = 0;
        for (int z = 0; z < depth; z++) {
            ImageProcessor ip = stack.getProcessor(z + 1);
            int[][] sliceLabels = cellposeInterface.segment2D(ip, params);

            // Offset labels to make unique across slices
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (sliceLabels[y][x] > 0) {
                        labelMask[z][y][x] = sliceLabels[y][x] + maxLabel;
                    }
                }
            }

            // Update max label
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    maxLabel = Math.max(maxLabel, labelMask[z][y][x]);
                }
            }
        }

        return labelMask;
    }

    /**
     * Convert Cellpose label mask to MicroObjects
     */
    private List<MicroObject> labelsToObjects(int[][][] labelMask, ImageStack originalStack) {
        List<MicroObject> objects = new ArrayList<>();

        // Find unique labels
        Map<Integer, List<int[]>> labelPixels = new HashMap<>();

        int depth = labelMask.length;
        int height = labelMask[0].length;
        int width = labelMask[0][0].length;

        for (int z = 0; z < depth; z++) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int label = labelMask[z][y][x];
                    if (label > 0) {
                        labelPixels.computeIfAbsent(label, k -> new ArrayList<>()).add(new int[]{x, y, z});
                    }
                }
            }
        }

        // Create MicroObject for each label
        int serialID = 0;
        for (Map.Entry<Integer, List<int[]>> entry : labelPixels.entrySet()) {
            List<int[]> pixels = entry.getValue();

            // Convert to MicroObject format
            int[] pixelsX = new int[pixels.size()];
            int[] pixelsY = new int[pixels.size()];
            int[] pixelsZ = new int[pixels.size()];

            for (int i = 0; i < pixels.size(); i++) {
                pixelsX[i] = pixels.get(i)[0];
                pixelsY[i] = pixels.get(i)[1];
                pixelsZ[i] = pixels.get(i)[2];
            }

            // Create MicroObject
            MicroObject obj = new MicroObject(pixelsX, pixelsY, pixelsZ, 0, new ImageStack[]{originalStack}, serialID);
            obj.setProperty("cellposeLabel", entry.getKey());
            objects.add(obj);
            serialID++;
        }

        return objects;
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

        // Use a margin based on object size (Cellpose can segment touching cells well)
        double margin = Math.cbrt(obj.getVoxelCount()) * 1.5;

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

        // Configure stitcher for Cellpose characteristics
        ObjectStitcher stitcher = new ObjectStitcher();

        // Cellpose produces consistent, high-quality segmentations
        // Use moderate distance threshold
        stitcher.setDistanceThreshold(10.0); // Slightly larger for DL precision

        // Require significant overlap to merge (Cellpose doesn't usually split cells)
        stitcher.setOverlapThreshold(0.2); // 20% overlap minimum

        // Enable intensity correlation (Cellpose provides good consistency)
        stitcher.setUseIntensityCorrelation(true);

        List<MicroObject> stitched = stitcher.stitchObjects(chunkObjects);

        // Log stitching statistics
        String stats = stitcher.getStitchingStats(chunkObjects.size(), stitched.size());
        System.out.println(stats);

        return stitched;
    }

    @Override
    protected boolean processTraditional(ImagePlus imp, List details, boolean calculate) {
        // For non-chunked data, process entire image
        try {
            extractParameters(details);

            if (!cellposeInterface.isServerRunning()) {
                cellposeInterface.startServer();
            }

            ImageStack stack = imp.getStack();
            int[][][] labelMask;

            if (params.isDo3D() && stack.getSize() > 1) {
                labelMask = cellposeInterface.segment3D(stack, params);
            } else {
                labelMask = segment2DStack(stack);
            }

            // Convert to MicroObjects
            chunkResults.clear();
            chunkResults.addAll(labelsToObjects(labelMask, stack));

            return true;
        } catch (Exception e) {
            System.err.println("Cellpose segmentation failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected boolean processTraditional(ImageStack[] stacks, List details, boolean calculate) {
        // Process first channel
        if (stacks == null || stacks.length == 0) {
            return false;
        }

        ImagePlus imp = new ImagePlus("temp", stacks[0]);
        return processTraditional(imp, details, calculate);
    }

    /**
     * Extract processing parameters from details list
     */
    private void extractParameters(List details) {
        // Parameters would be extracted from UI or provided list
        // For now, use current params member variable
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

        // Model selection
        JPanel modelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modelPanel.add(new JLabel("Model:"));
        modelComboBox = new JComboBox<>(CellposeModel.values());
        modelComboBox.setSelectedItem(CellposeModel.CYTO2);
        modelComboBox.addActionListener(e -> params.setModel((CellposeModel) modelComboBox.getSelectedItem()));
        modelPanel.add(modelComboBox);
        panel.add(modelPanel);

        // Diameter
        JPanel diameterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        diameterPanel.add(new JLabel("Diameter (pixels, 0=auto):"));
        diameterField = new JTextField(String.valueOf(params.getDiameter()), 5);
        diameterPanel.add(diameterField);
        panel.add(diameterPanel);

        // Cell probability threshold
        JPanel cellprobPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cellprobPanel.add(new JLabel("Cell Probability Threshold:"));
        cellprobSlider = new JSlider(-60, 60, (int) (params.getCellprobThreshold() * 10));
        cellprobSlider.setMajorTickSpacing(20);
        cellprobSlider.setPaintTicks(true);
        cellprobPanel.add(cellprobSlider);
        JLabel cellprobValue = new JLabel(String.format("%.1f", params.getCellprobThreshold()));
        cellprobSlider.addChangeListener(e -> cellprobValue.setText(String.format("%.1f", cellprobSlider.getValue() / 10.0)));
        cellprobPanel.add(cellprobValue);
        panel.add(cellprobPanel);

        // Flow threshold
        JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        flowPanel.add(new JLabel("Flow Threshold:"));
        flowSlider = new JSlider(0, 100, (int) (params.getFlowThreshold() * 100));
        flowSlider.setMajorTickSpacing(20);
        flowSlider.setPaintTicks(true);
        flowPanel.add(flowSlider);
        JLabel flowValue = new JLabel(String.format("%.2f", params.getFlowThreshold()));
        flowSlider.addChangeListener(e -> flowValue.setText(String.format("%.2f", flowSlider.getValue() / 100.0)));
        flowPanel.add(flowValue);
        panel.add(flowPanel);

        // 3D mode
        JPanel mode3DPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        use3DCheckbox = new JCheckBox("Use 3D segmentation (slower but more accurate)", params.isDo3D());
        mode3DPanel.add(use3DCheckbox);
        panel.add(mode3DPanel);

        // GPU mode
        JPanel gpuPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        useGPUCheckbox = new JCheckBox("Use GPU acceleration", params.isUseGPU());
        gpuPanel.add(useGPUCheckbox);
        panel.add(gpuPanel);

        // Status and test connection
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Status: Not connected");
        statusPanel.add(statusLabel);
        testConnectionButton = new JButton("Test Connection");
        testConnectionButton.addActionListener(e -> testConnection());
        statusPanel.add(testConnectionButton);
        panel.add(statusPanel);

        // Info
        JLabel infoLabel = new JLabel("<html><i>Deep learning segmentation using Cellpose</i></html>");
        panel.add(infoLabel);

        return panel;
    }

    /**
     * Test connection to Cellpose server
     */
    private void testConnection() {
        try {
            if (!cellposeInterface.isServerRunning()) {
                statusLabel.setText("Starting server...");
                cellposeInterface.startServer();
            }

            if (cellposeInterface.testConnection()) {
                String version = cellposeInterface.getCellposeVersion();
                statusLabel.setText("Status: Connected (Cellpose " + version + ")");
            } else {
                statusLabel.setText("Status: Connection failed");
            }
        } catch (Exception e) {
            statusLabel.setText("Status: Error - " + e.getMessage());
        }
    }

    @Override
    public String getProgressDetail() {
        if (useChunkedProcessing) {
            return getChunkProcessingStats();
        } else {
            return String.format("Cellpose: %d objects found", chunkResults.size());
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

    /**
     * Clean up resources
     */
    @Override
    protected void finalize() throws Throwable {
        if (cellposeInterface != null) {
            cellposeInterface.close();
        }
        super.finalize();
    }
}
