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

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import org.scijava.plugin.Plugin;
import vtea.dataset.volume.ChunkedVolumeDataset;
import vtea.dataset.volume.VolumeDataset;
import vtea.objects.layercake.microRegion;
import vtea.objects.layercake.microVolume;
import vtea.partition.Chunk;
import vtea.partition.ObjectStitcher;
import vteaobjects.MicroObject;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Chunked version of LayerCake3DSingleThresholdkDTree for processing large volumes.
 * Extends AbstractChunkedSegmentation to support volume partitioning with Zarr backend.
 *
 * Uses kDTree spatial indexing for efficient boundary object merging across chunks.
 * Supports both 2.5D and 3D segmentation modes with optional watershed.
 *
 * @author sethwinfree
 */
@Plugin(type = Segmentation.class)
public class ChunkedLayerCake3DkDTree extends AbstractChunkedSegmentation {

    // Reuse the original implementation for actual segmentation
    private final LayerCake3DSingleThresholdkDTree traditional;

    private int[] minConstants = new int[4]; // 0: minObjectSize, 1: maxObjectSize, 2: minOverlap, 3: minThreshold
    private boolean watershedImageJ = true;
    private boolean enforce2_5D = true;

    // UI components (shared with traditional implementation)
    int[] settings = {0, 5, 20, 1000};

    JTextField f1 = new JTextField(String.valueOf(settings[0]), 5);
    JTextField f2 = new JTextField(String.valueOf(settings[1]), 5);
    JTextField f3 = new JTextField(String.valueOf(settings[2]), 5);
    JTextField f4 = new JTextField(String.valueOf(settings[3]), 5);

    public ChunkedLayerCake3DkDTree() {
        super();
        VERSION = "2.0";
        AUTHOR = "Seth Winfree";
        COMMENT = "Chunked connected components with kDTree for large volumes (VTEA 2.0)";
        NAME = "Chunked Connect 2D/3D with kDTree";
        KEY = "ChunkedConnect2D3DkDTree";
        TYPE = "Calculated";
        COMPATIBILITY = "3D";

        // Create traditional implementation for delegation
        traditional = new LayerCake3DSingleThresholdkDTree();

        // Setup UI components
        f1.setPreferredSize(new Dimension(20, 30));
        f1.setMaximumSize(f1.getPreferredSize());
        f1.setMinimumSize(f1.getPreferredSize());

        f2.setPreferredSize(new Dimension(20, 30));
        f2.setMaximumSize(f2.getPreferredSize());
        f2.setMinimumSize(f2.getPreferredSize());

        f3.setPreferredSize(new Dimension(20, 30));
        f3.setMaximumSize(f3.getPreferredSize());
        f3.setMinimumSize(f3.getPreferredSize());

        f4.setPreferredSize(new Dimension(20, 30));
        f4.setMaximumSize(f4.getPreferredSize());
        f4.setMinimumSize(f4.getPreferredSize());

        protocol = new ArrayList();
        protocol.add(new JLabel("Low Threshold"));
        protocol.add(f1);
        protocol.add(new JLabel("Centroid Offset"));
        protocol.add(f2);
        protocol.add(new JLabel("Min Vol (vox)"));
        protocol.add(f3);
        protocol.add(new JLabel("Max Vol (vox)"));
        protocol.add(f4);
        protocol.add(new JCheckBox("Watershed", true));
        protocol.add(new JCheckBox("2.5D", true));
        protocol.add(new JCheckBox("Use Chunking", true));
    }

    @Override
    protected List<MicroObject> processChunk(Chunk chunk, List details, boolean calculate) {
        try {
            // Extract parameters from details
            ArrayList al = (ArrayList) details.get(3);

            minConstants[3] = Integer.parseInt(((JTextField) (al.get(1))).getText());
            minConstants[2] = Integer.parseInt(((JTextField) (al.get(3))).getText());
            minConstants[0] = Integer.parseInt(((JTextField) (al.get(5))).getText());
            minConstants[1] = Integer.parseInt(((JTextField) (al.get(7))).getText());
            watershedImageJ = ((JCheckBox) (al.get(8))).isSelected();
            enforce2_5D = ((JCheckBox) (al.get(9))).isSelected();

            notifyProgressListeners("Processing chunk " + chunk.getChunkId(), 0.0);

            // Get chunk data
            ImageStack chunkStack = chunk.getData();
            if (chunkStack == null) {
                System.err.println("Chunk " + chunk.getChunkId() + " has no data");
                return new ArrayList<>();
            }

            // Create a temporary ImagePlus for processing
            ImagePlus chunkImage = new ImagePlus("Chunk_" + chunk.getChunkId(), chunkStack);

            // Process using traditional LayerCake algorithm on this chunk
            // Create ImageStack array (single channel for now)
            ImageStack[] stacks = new ImageStack[1];
            stacks[0] = chunkStack;

            // Create a temporary protocol list for this chunk
            ArrayList chunkDetails = new ArrayList();
            chunkDetails.add(details.get(0)); // title
            chunkDetails.add(details.get(1)); // method
            chunkDetails.add(0); // channel 0
            chunkDetails.add(al); // parameters

            // Use the traditional implementation's process method
            LayerCake3DSingleThresholdkDTree tempProcessor = new LayerCake3DSingleThresholdkDTree();
            tempProcessor.process(stacks, chunkDetails, calculate);

            // Get the objects from processing
            List<MicroObject> chunkObjects = tempProcessor.getObjects();

            // Transform object coordinates to global space
            long[] globalStart = chunk.getGlobalStart();
            for (MicroObject obj : chunkObjects) {
                // Adjust coordinates
                int[] pixelsX = obj.getPixelsX();
                int[] pixelsY = obj.getPixelsY();
                int[] pixelsZ = obj.getPixelsZ();

                for (int i = 0; i < pixelsX.length; i++) {
                    pixelsX[i] += (int) globalStart[0];
                    pixelsY[i] += (int) globalStart[1];
                }
                for (int i = 0; i < pixelsZ.length; i++) {
                    pixelsZ[i] += (int) globalStart[2];
                }

                // Update centroid
                double[] centroid = obj.getCentroidXYZ_AsDbl();
                if (centroid != null && centroid.length >= 3) {
                    centroid[0] += globalStart[0];
                    centroid[1] += globalStart[1];
                    centroid[2] += globalStart[2];
                    obj.setCentroidXYZ(centroid);
                }

                // Tag objects near chunk boundaries for stitching
                boolean nearBoundary = isNearChunkBoundary(obj, chunk);
                obj.setProperty("chunkBoundary", nearBoundary);
                obj.setProperty("chunkId", chunk.getChunkId());
            }

            notifyProgressListeners("Chunk " + chunk.getChunkId() + " complete: " +
                                  chunkObjects.size() + " objects", 1.0);

            return chunkObjects;

        } catch (Exception e) {
            System.err.println("Error processing chunk " + chunk.getChunkId() + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Check if object is near chunk boundary (within overlap region)
     */
    private boolean isNearChunkBoundary(MicroObject obj, Chunk chunk) {
        double[] centroid = obj.getCentroidXYZ_AsDbl();
        if (centroid == null || centroid.length < 3) {
            return false;
        }

        long[] globalStart = chunk.getGlobalStart();
        long[] globalEnd = chunk.getGlobalEnd();
        int[] overlapStart = chunk.getOverlapStart();
        int[] overlapEnd = chunk.getOverlapEnd();

        // Check if centroid is in overlap regions
        for (int dim = 0; dim < 3; dim++) {
            double coord = centroid[dim];

            // Near start boundary?
            if (coord < globalStart[dim] + overlapStart[dim]) {
                return true;
            }

            // Near end boundary?
            if (coord > globalEnd[dim] - overlapEnd[dim]) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected List<MicroObject> stitchChunks(List<MicroObject> chunkObjects,
                                            ChunkedVolumeDataset dataset) {
        notifyProgressListeners("Stitching boundary objects...", 0.95);

        // Use ObjectStitcher with kDTree spatial indexing
        ObjectStitcher stitcher = new ObjectStitcher();

        // Configure stitching parameters based on algorithm settings
        // Use centroid offset as distance threshold
        stitcher.setDistanceThreshold(minConstants[2]);

        // Overlap threshold - objects must have some overlap to merge
        stitcher.setOverlapThreshold(0.05); // 5% minimum overlap

        // Use intensity correlation for better matching
        stitcher.setUseIntensityCorrelation(true);

        List<MicroObject> stitched = stitcher.stitchObjects(chunkObjects);

        notifyProgressListeners("Stitching complete: " + chunkObjects.size() +
                              " → " + stitched.size() + " objects", 1.0);

        System.out.println(stitcher.getStitchingStats(chunkObjects.size(), stitched.size()));

        return stitched;
    }

    @Override
    protected boolean processTraditional(ImagePlus imp, List details, boolean calculate) {
        // Delegate to the original LayerCake3DSingleThresholdkDTree implementation
        ImageStack[] stacks = new ImageStack[imp.getNChannels()];

        for (int c = 0; c < imp.getNChannels(); c++) {
            imp.setC(c + 1);
            stacks[c] = imp.getImageStack();
        }

        return processTraditional(stacks, details, calculate);
    }

    @Override
    protected boolean processTraditional(ImageStack[] stacks, List details, boolean calculate) {
        // Delegate to the original implementation
        boolean success = traditional.process(stacks, details, calculate);

        if (success) {
            // Copy results to our lists
            chunkResults.clear();
            chunkResults.addAll(traditional.getObjects());
            stitchedResults.clear();
            stitchedResults.addAll(traditional.getObjects());
        }

        return success;
    }

    @Override
    public ImagePlus getSegmentation() {
        if (useChunkedProcessing) {
            // For chunked processing, would need to reconstruct segmentation image
            // For now, return null (segmentation image not applicable for chunked mode)
            return null;
        } else {
            return traditional.getSegmentation();
        }
    }

    @Override
    public JPanel getSegmentationTool() {
        return traditional.getSegmentationTool();
    }

    @Override
    public void setImage(ImagePlus thresholdPreview) {
        super.setImage(thresholdPreview);
        traditional.setImage(thresholdPreview);
    }

    @Override
    public void updateImage(ImagePlus thresholdPreview) {
        super.updateImage(thresholdPreview);
        traditional.updateImage(thresholdPreview);
    }

    @Override
    public void doUpdateOfTool() {
        traditional.doUpdateOfTool();
    }

    @Override
    public boolean copyComponentParameter(String version, ArrayList dComponents, ArrayList sComponents) {
        return traditional.copyComponentParameter(version, dComponents, sComponents);
    }

    @Override
    public boolean loadComponentParameter(String version, ArrayList dComponents, ArrayList fields) {
        return traditional.loadComponentParameter(version, dComponents, fields);
    }

    @Override
    public boolean saveComponentParameter(String version, ArrayList fields, ArrayList sComponents) {
        return traditional.saveComponentParameter(version, fields, sComponents);
    }

    @Override
    public String runImageJMacroCommand(String str) {
        return traditional.runImageJMacroCommand(str);
    }

    @Override
    public void sendProgressComment() {
        traditional.sendProgressComment();
    }

    @Override
    public String getProgressComment() {
        return traditional.getProgressComment();
    }

    @Override
    public JPanel getOptionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Add chunking options
        JLabel chunkingLabel = new JLabel("Volume Partitioning:");
        chunkingLabel.setFont(chunkingLabel.getFont().deriveFont(Font.BOLD));
        panel.add(chunkingLabel);

        JCheckBox useChunkingCheckbox = new JCheckBox("Enable chunking for large volumes", true);
        panel.add(useChunkingCheckbox);

        JLabel overlapLabel = new JLabel("Chunk overlap: 10%");
        panel.add(overlapLabel);

        panel.add(Box.createVerticalStrut(10));

        // Add traditional options
        JLabel tradLabel = new JLabel("Segmentation Parameters:");
        tradLabel.setFont(tradLabel.getFont().deriveFont(Font.BOLD));
        panel.add(tradLabel);

        for (int i = 0; i < protocol.size(); i += 2) {
            if (i + 1 < protocol.size()) {
                JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                rowPanel.add((Component) protocol.get(i));
                rowPanel.add((Component) protocol.get(i + 1));
                panel.add(rowPanel);
            }
        }

        return panel;
    }

    /**
     * Get statistics about chunked processing
     */
    public String getProcessingStats() {
        if (!useChunkedProcessing) {
            return "Traditional processing used (data fits in memory)";
        }

        return getChunkProcessingStats();
    }
}
