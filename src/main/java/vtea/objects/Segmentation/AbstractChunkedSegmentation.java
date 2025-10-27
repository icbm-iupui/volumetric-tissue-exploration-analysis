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
import vtea.dataset.volume.ChunkedVolumeDataset;
import vtea.dataset.volume.VolumeDataset;
import vtea.partition.Chunk;
import vteaobjects.MicroObject;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Abstract base class for chunked segmentation algorithms.
 * Extends AbstractSegmentation to support volume partitioning for
 * processing large datasets that don't fit in memory.
 *
 * @author sethwinfree
 * @param <T> Component type
 * @param <K> Object type
 */
public abstract class AbstractChunkedSegmentation<T extends Component, K extends Object>
        extends AbstractSegmentation<T, K> {

    protected VolumeDataset volumeDataset;
    protected boolean useChunkedProcessing;
    protected double overlapPercent = 0.1; // 10% default overlap

    // Results from chunked processing
    protected List<MicroObject> chunkResults;
    protected List<MicroObject> stitchedResults;

    /**
     * Constructor
     */
    public AbstractChunkedSegmentation() {
        super();
        this.useChunkedProcessing = false;
        this.chunkResults = new CopyOnWriteArrayList<>();
        this.stitchedResults = new ArrayList<>();
    }

    /**
     * Set the volume dataset
     * @param dataset VolumeDataset to process
     */
    public void setVolumeDataset(VolumeDataset dataset) {
        this.volumeDataset = dataset;
        // Automatically enable chunked processing for chunked datasets
        if (dataset != null && dataset.isChunked()) {
            this.useChunkedProcessing = true;
        }
    }

    /**
     * Check if using chunked processing
     * @return true if chunked
     */
    public boolean isUsingChunkedProcessing() {
        return useChunkedProcessing;
    }

    /**
     * Set whether to use chunked processing
     * @param useChunked true to enable chunked processing
     */
    public void setUseChunkedProcessing(boolean useChunked) {
        this.useChunkedProcessing = useChunked;
    }

    /**
     * Set overlap percentage for chunks
     * @param overlapPercent overlap as percentage (0.0 to 0.5)
     */
    public void setOverlapPercent(double overlapPercent) {
        this.overlapPercent = Math.max(0.0, Math.min(0.5, overlapPercent));
    }

    /**
     * Get overlap percentage
     * @return overlap percentage
     */
    public double getOverlapPercent() {
        return overlapPercent;
    }

    /**
     * Process with automatic mode selection (chunked or traditional)
     * @param imp ImagePlus to process
     * @param details processing details
     * @param calculate whether to calculate measurements
     * @return true if successful
     */
    @Override
    public boolean process(ImagePlus imp, List details, boolean calculate) {
        if (volumeDataset != null && volumeDataset.isChunked()) {
            return processChunked((ChunkedVolumeDataset) volumeDataset, details, calculate);
        } else {
            return processTraditional(imp, details, calculate);
        }
    }

    /**
     * Process ImageStack with automatic mode selection
     * @param stacks ImageStack array
     * @param details processing details
     * @param calculate whether to calculate measurements
     * @return true if successful
     */
    @Override
    public boolean process(ImageStack[] stacks, List details, boolean calculate) {
        if (volumeDataset != null && volumeDataset.isChunked()) {
            return processChunked((ChunkedVolumeDataset) volumeDataset, details, calculate);
        } else {
            return processTraditional(stacks, details, calculate);
        }
    }

    /**
     * Process chunked dataset
     * @param dataset ChunkedVolumeDataset
     * @param details processing details
     * @param calculate whether to calculate measurements
     * @return true if successful
     */
    protected boolean processChunked(ChunkedVolumeDataset dataset, List details, boolean calculate) {
        try {
            chunkResults.clear();
            stitchedResults.clear();

            // Get channel from details (typically first element)
            int channel = 0;
            if (details != null && !details.isEmpty() && details.get(0) instanceof Integer) {
                channel = (Integer) details.get(0);
            }

            // Set overlap for dataset
            int[] chunkDims = dataset.getChunkDimensions();
            int overlapX = (int) (chunkDims[0] * overlapPercent);
            int overlapY = (int) (chunkDims[1] * overlapPercent);
            int overlapZ = chunkDims.length > 2 ? (int) (chunkDims[2] * overlapPercent) : 0;
            dataset.setOverlapSize(overlapX, overlapY, overlapZ);

            // Get total chunks for progress tracking
            int totalChunks = dataset.getTotalChunkCount();
            int processedChunks = 0;

            notifyProgressListeners("Starting chunked segmentation...", 0.0);

            // Iterate through chunks
            var chunkIterator = dataset.getChunkIterator(channel);
            while (chunkIterator.hasNext()) {
                Chunk chunk = chunkIterator.next();

                // Notify progress
                double progress = (double) processedChunks / totalChunks;
                notifyProgressListeners("Processing chunk " + processedChunks + "/" + totalChunks,
                                      progress);

                // Process single chunk
                List<MicroObject> chunkObjects = processChunk(chunk, details, calculate);

                if (chunkObjects != null) {
                    // Tag objects with chunk ID for stitching
                    for (MicroObject obj : chunkObjects) {
                        obj.setProperty("chunkId", chunk.getChunkId());
                        obj.setProperty("chunkBoundary", chunk.isBoundaryChunk());
                    }
                    chunkResults.addAll(chunkObjects);
                }

                // Clear chunk data to free memory
                chunk.clearData();
                processedChunks++;
            }

            notifyProgressListeners("Stitching chunks...", 0.95);

            // Stitch boundary objects
            stitchedResults = stitchChunks(chunkResults, dataset);

            notifyProgressListeners("Chunked segmentation complete", 1.0);

            return true;

        } catch (Exception e) {
            System.err.println("Error in chunked processing: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Process a single chunk - must be implemented by subclasses
     * @param chunk Chunk to process
     * @param details processing details
     * @param calculate whether to calculate measurements
     * @return list of MicroObjects found in this chunk
     */
    protected abstract List<MicroObject> processChunk(Chunk chunk, List details, boolean calculate);

    /**
     * Stitch objects from multiple chunks
     * Default implementation returns all objects (no stitching)
     * Override for sophisticated boundary object merging
     *
     * @param chunkObjects all objects from all chunks
     * @param dataset the chunked dataset
     * @return stitched list of objects
     */
    protected List<MicroObject> stitchChunks(List<MicroObject> chunkObjects,
                                            ChunkedVolumeDataset dataset) {
        // Default: return all objects without stitching
        // Subclasses should override for boundary object merging
        return new ArrayList<>(chunkObjects);
    }

    /**
     * Traditional processing for non-chunked data - must be implemented by subclasses
     * @param imp ImagePlus
     * @param details processing details
     * @param calculate whether to calculate measurements
     * @return true if successful
     */
    protected abstract boolean processTraditional(ImagePlus imp, List details, boolean calculate);

    /**
     * Traditional processing for ImageStack array
     * @param stacks ImageStack array
     * @param details processing details
     * @param calculate whether to calculate measurements
     * @return true if successful
     */
    protected abstract boolean processTraditional(ImageStack[] stacks, List details, boolean calculate);

    /**
     * Get segmentation results
     * @return list of MicroObjects
     */
    @Override
    public ArrayList<MicroObject> getObjects() {
        if (useChunkedProcessing && !stitchedResults.isEmpty()) {
            return new ArrayList<>(stitchedResults);
        } else if (!chunkResults.isEmpty()) {
            return new ArrayList<>(chunkResults);
        }
        return new ArrayList<>();
    }

    /**
     * Get chunk results before stitching (for debugging/analysis)
     * @return list of all objects from chunks
     */
    public List<MicroObject> getChunkResults() {
        return Collections.unmodifiableList(chunkResults);
    }

    /**
     * Get stitched results
     * @return list of stitched objects
     */
    public List<MicroObject> getStitchedResults() {
        return Collections.unmodifiableList(stitchedResults);
    }

    /**
     * Clear results to free memory
     */
    public void clearResults() {
        chunkResults.clear();
        stitchedResults.clear();
    }

    /**
     * Get statistics about chunked processing
     * @return stats string
     */
    public String getChunkProcessingStats() {
        if (!useChunkedProcessing) {
            return "Not using chunked processing";
        }

        return String.format("Chunked Processing Stats:\n" +
                           "  Chunk objects: %d\n" +
                           "  Stitched objects: %d\n" +
                           "  Objects merged: %d\n" +
                           "  Overlap: %.1f%%",
                           chunkResults.size(),
                           stitchedResults.size(),
                           chunkResults.size() - stitchedResults.size(),
                           overlapPercent * 100);
    }
}
