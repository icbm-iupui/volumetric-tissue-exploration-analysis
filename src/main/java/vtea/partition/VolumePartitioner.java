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
package vtea.partition;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages volume partitioning strategies for chunked processing.
 * Calculates optimal chunk sizes and creates chunk metadata.
 *
 * @author sethwinfree
 */
public class VolumePartitioner {

    private final long[] volumeDimensions;  // [width, height, depth]
    private int[] chunkDimensions;          // [chunkWidth, chunkHeight, chunkDepth]
    private int[] overlapSize;              // [overlapX, overlapY, overlapZ]
    private PartitioningStrategy strategy;

    // Default chunk size
    private static final int DEFAULT_CHUNK_XY = 512;
    private static final int DEFAULT_CHUNK_Z = 32;
    private static final double DEFAULT_OVERLAP_PERCENT = 0.1;  // 10% overlap

    /**
     * Constructor with volume dimensions
     * @param volumeDimensions dimensions [width, height, depth]
     */
    public VolumePartitioner(long[] volumeDimensions) {
        this.volumeDimensions = volumeDimensions.clone();
        this.chunkDimensions = new int[]{DEFAULT_CHUNK_XY, DEFAULT_CHUNK_XY, DEFAULT_CHUNK_Z};
        this.strategy = PartitioningStrategy.FIXED_SIZE;
        calculateOverlapSize(DEFAULT_OVERLAP_PERCENT);
    }

    /**
     * Constructor with custom chunk dimensions
     * @param volumeDimensions volume dimensions [width, height, depth]
     * @param chunkDimensions chunk dimensions [width, height, depth]
     */
    public VolumePartitioner(long[] volumeDimensions, int[] chunkDimensions) {
        this.volumeDimensions = volumeDimensions.clone();
        this.chunkDimensions = chunkDimensions.clone();
        this.strategy = PartitioningStrategy.FIXED_SIZE;
        calculateOverlapSize(DEFAULT_OVERLAP_PERCENT);
    }

    /**
     * Constructor with custom chunk dimensions and overlap
     * @param volumeDimensions volume dimensions [width, height, depth]
     * @param chunkDimensions chunk dimensions [width, height, depth]
     * @param overlapPercent overlap percentage (0.0 to 0.5)
     */
    public VolumePartitioner(long[] volumeDimensions, int[] chunkDimensions, double overlapPercent) {
        this.volumeDimensions = volumeDimensions.clone();
        this.chunkDimensions = chunkDimensions.clone();
        this.strategy = PartitioningStrategy.FIXED_SIZE;
        calculateOverlapSize(overlapPercent);
    }

    /**
     * Calculate overlap size from percentage
     * @param overlapPercent percentage of chunk size (0.0 to 0.5)
     */
    private void calculateOverlapSize(double overlapPercent) {
        overlapPercent = Math.max(0.0, Math.min(0.5, overlapPercent));
        this.overlapSize = new int[3];
        for (int i = 0; i < 3; i++) {
            this.overlapSize[i] = (int) (chunkDimensions[i] * overlapPercent);
        }
    }

    /**
     * Set the partitioning strategy
     * @param strategy partitioning strategy
     */
    public void setStrategy(PartitioningStrategy strategy) {
        this.strategy = strategy;
        if (strategy == PartitioningStrategy.MEMORY_BASED) {
            calculateOptimalChunkSize();
        }
    }

    /**
     * Calculate optimal chunk size based on available memory
     */
    private void calculateOptimalChunkSize() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long availableMemory = maxMemory - usedMemory;

        // Use 25% of available memory per chunk (conservative)
        long targetChunkMemory = availableMemory / 4;

        // Assume 2 bytes per pixel (16-bit), with overhead factor of 2x
        int bytesPerPixel = 2;
        int overheadFactor = 2;
        long targetVoxels = targetChunkMemory / (bytesPerPixel * overheadFactor);

        // Calculate cube root for balanced dimensions
        int targetDim = (int) Math.cbrt(targetVoxels);

        // Set XY dimensions (typically larger)
        chunkDimensions[0] = Math.min(targetDim * 2, (int) volumeDimensions[0]);
        chunkDimensions[1] = Math.min(targetDim * 2, (int) volumeDimensions[1]);

        // Z dimension typically smaller
        chunkDimensions[2] = Math.min(targetDim / 2, (int) volumeDimensions[2]);

        // Ensure minimum sizes
        chunkDimensions[0] = Math.max(256, chunkDimensions[0]);
        chunkDimensions[1] = Math.max(256, chunkDimensions[1]);
        chunkDimensions[2] = Math.max(16, chunkDimensions[2]);

        calculateOverlapSize(DEFAULT_OVERLAP_PERCENT);
    }

    /**
     * Set custom chunk dimensions
     * @param chunkDimensions dimensions [width, height, depth]
     */
    public void setChunkDimensions(int[] chunkDimensions) {
        this.chunkDimensions = chunkDimensions.clone();
        calculateOverlapSize(DEFAULT_OVERLAP_PERCENT);
    }

    /**
     * Set overlap size in pixels
     * @param overlapSize overlap [x, y, z] in pixels
     */
    public void setOverlapSize(int[] overlapSize) {
        this.overlapSize = overlapSize.clone();
    }

    /**
     * Set overlap as percentage of chunk size
     * @param overlapPercent overlap percentage (0.0 to 0.5)
     */
    public void setOverlapPercent(double overlapPercent) {
        calculateOverlapSize(overlapPercent);
    }

    /**
     * Get chunk dimensions
     * @return chunk dimensions [width, height, depth]
     */
    public int[] getChunkDimensions() {
        return chunkDimensions.clone();
    }

    /**
     * Get overlap size
     * @return overlap [x, y, z] in pixels
     */
    public int[] getOverlapSize() {
        return overlapSize.clone();
    }

    /**
     * Calculate the number of chunks in each dimension
     * @return number of chunks [numX, numY, numZ]
     */
    public int[] getNumChunks() {
        int[] numChunks = new int[3];
        for (int i = 0; i < 3; i++) {
            // Core chunk size (without overlap)
            int coreSize = chunkDimensions[i] - 2 * overlapSize[i];
            numChunks[i] = (int) Math.ceil((double) volumeDimensions[i] / coreSize);
        }
        return numChunks;
    }

    /**
     * Get total number of chunks
     * @return total chunk count
     */
    public int getTotalChunkCount() {
        int[] numChunks = getNumChunks();
        return numChunks[0] * numChunks[1] * numChunks[2];
    }

    /**
     * Generate all chunk metadata for a channel
     * @param channel channel index
     * @return list of Chunk objects
     */
    public List<Chunk> generateChunks(int channel) {
        List<Chunk> chunks = new ArrayList<>();
        int[] numChunks = getNumChunks();
        int chunkId = 0;

        for (int cz = 0; cz < numChunks[2]; cz++) {
            for (int cy = 0; cy < numChunks[1]; cy++) {
                for (int cx = 0; cx < numChunks[0]; cx++) {
                    Chunk chunk = generateChunk(cx, cy, cz, channel, chunkId++);
                    chunks.add(chunk);
                }
            }
        }

        return chunks;
    }

    /**
     * Generate a single chunk
     * @param chunkX X chunk coordinate
     * @param chunkY Y chunk coordinate
     * @param chunkZ Z chunk coordinate
     * @param channel channel index
     * @param chunkId unique chunk ID
     * @return Chunk object
     */
    public Chunk generateChunk(int chunkX, int chunkY, int chunkZ, int channel, int chunkId) {
        int[] numChunks = getNumChunks();

        // Core chunk size (without overlap)
        int[] coreSize = new int[3];
        for (int i = 0; i < 3; i++) {
            coreSize[i] = chunkDimensions[i] - 2 * overlapSize[i];
        }

        // Calculate global start position of core region
        long[] coreStart = new long[3];
        coreStart[0] = (long) chunkX * coreSize[0];
        coreStart[1] = (long) chunkY * coreSize[1];
        coreStart[2] = (long) chunkZ * coreSize[2];

        // Calculate overlap at start and end
        int[] overlapStart = new int[3];
        int[] overlapEnd = new int[3];

        for (int i = 0; i < 3; i++) {
            // Start overlap (0 if first chunk in dimension)
            overlapStart[i] = (chunkX == 0 && i == 0) || (chunkY == 0 && i == 1) || (chunkZ == 0 && i == 2)
                            ? 0 : overlapSize[i];

            // End overlap (0 if last chunk in dimension)
            boolean isLast = (i == 0 && chunkX == numChunks[0] - 1) ||
                           (i == 1 && chunkY == numChunks[1] - 1) ||
                           (i == 2 && chunkZ == numChunks[2] - 1);
            overlapEnd[i] = isLast ? 0 : overlapSize[i];
        }

        // Calculate actual global start (including overlap)
        long[] globalStart = new long[3];
        for (int i = 0; i < 3; i++) {
            globalStart[i] = Math.max(0, coreStart[i] - overlapStart[i]);
        }

        // Calculate global end
        long[] globalEnd = new long[3];
        for (int i = 0; i < 3; i++) {
            globalEnd[i] = Math.min(volumeDimensions[i], coreStart[i] + coreSize[i] + overlapEnd[i]);
        }

        int[] gridPosition = new int[]{chunkX, chunkY, chunkZ};

        Chunk chunk = new Chunk(chunkId, gridPosition, globalStart, globalEnd,
                               overlapStart, overlapEnd, channel);

        // Mark boundary chunks
        boolean isBoundary = chunkX == 0 || chunkY == 0 || chunkZ == 0 ||
                           chunkX == numChunks[0] - 1 ||
                           chunkY == numChunks[1] - 1 ||
                           chunkZ == numChunks[2] - 1;
        chunk.setBoundaryChunk(isBoundary);

        return chunk;
    }

    /**
     * Find chunks that intersect with a region
     * @param xStart start X coordinate
     * @param yStart start Y coordinate
     * @param zStart start Z coordinate
     * @param width region width
     * @param height region height
     * @param depth region depth
     * @param channel channel index
     * @return list of intersecting chunks
     */
    public List<Chunk> getIntersectingChunks(long xStart, long yStart, long zStart,
                                            long width, long height, long depth, int channel) {
        List<Chunk> intersecting = new ArrayList<>();
        int[] numChunks = getNumChunks();
        int[] coreSize = new int[3];
        for (int i = 0; i < 3; i++) {
            coreSize[i] = chunkDimensions[i] - 2 * overlapSize[i];
        }

        // Calculate chunk range
        int startCX = Math.max(0, (int) (xStart / coreSize[0]));
        int startCY = Math.max(0, (int) (yStart / coreSize[1]));
        int startCZ = Math.max(0, (int) (zStart / coreSize[2]));

        int endCX = Math.min(numChunks[0] - 1, (int) ((xStart + width - 1) / coreSize[0]));
        int endCY = Math.min(numChunks[1] - 1, (int) ((yStart + height - 1) / coreSize[1]));
        int endCZ = Math.min(numChunks[2] - 1, (int) ((zStart + depth - 1) / coreSize[2]));

        int chunkId = 0;
        for (int cz = startCZ; cz <= endCZ; cz++) {
            for (int cy = startCY; cy <= endCY; cy++) {
                for (int cx = startCX; cx <= endCX; cx++) {
                    chunkId = cx + cy * numChunks[0] + cz * numChunks[0] * numChunks[1];
                    Chunk chunk = generateChunk(cx, cy, cz, channel, chunkId);
                    intersecting.add(chunk);
                }
            }
        }

        return intersecting;
    }

    /**
     * Get estimated memory per chunk in bytes
     * @param bytesPerPixel bytes per pixel (1, 2, or 4)
     * @return memory size in bytes
     */
    public long getEstimatedChunkMemory(int bytesPerPixel) {
        long voxels = (long) chunkDimensions[0] * chunkDimensions[1] * chunkDimensions[2];
        return voxels * bytesPerPixel;
    }

    /**
     * Check if chunking is necessary
     * @param bytesPerPixel bytes per pixel
     * @return true if volume should be chunked
     */
    public boolean shouldChunk(int bytesPerPixel) {
        long volumeSize = volumeDimensions[0] * volumeDimensions[1] * volumeDimensions[2] * bytesPerPixel;
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();

        // Chunk if volume > 50% of max heap
        return volumeSize > (maxMemory * 0.5);
    }

    @Override
    public String toString() {
        int[] numChunks = getNumChunks();
        return String.format("VolumePartitioner[volume=%dx%dx%d, chunks=%dx%dx%d, chunkSize=%dx%dx%d, overlap=%dx%dx%d, total=%d]",
                volumeDimensions[0], volumeDimensions[1], volumeDimensions[2],
                numChunks[0], numChunks[1], numChunks[2],
                chunkDimensions[0], chunkDimensions[1], chunkDimensions[2],
                overlapSize[0], overlapSize[1], overlapSize[2],
                getTotalChunkCount());
    }
}
