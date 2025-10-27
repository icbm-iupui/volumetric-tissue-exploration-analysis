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

import ij.ImageStack;
import java.io.Serializable;

/**
 * Represents a 3D chunk/partition of a volume dataset.
 * Contains both the data and metadata about the chunk's position and overlap regions.
 *
 * @author sethwinfree
 */
public class Chunk implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int chunkId;
    private final int[] gridPosition;  // [chunkX, chunkY, chunkZ]
    private final long[] globalStart;  // [x, y, z] in global coordinates
    private final long[] globalEnd;    // [x, y, z] in global coordinates (exclusive)
    private final long[] dimensions;   // [width, height, depth] of this chunk
    private final int channel;

    // Overlap regions with neighboring chunks
    private final int[] overlapStart;  // [x, y, z] overlap at start
    private final int[] overlapEnd;    // [x, y, z] overlap at end

    // The actual image data
    private ImageStack data;

    // Processing status
    private boolean processed;
    private boolean boundaryChunk;  // true if chunk is at volume boundary

    /**
     * Constructor for a volume chunk
     * @param chunkId unique chunk identifier
     * @param gridPosition position in chunk grid [chunkX, chunkY, chunkZ]
     * @param globalStart start coordinates in global volume [x, y, z]
     * @param globalEnd end coordinates in global volume [x, y, z] (exclusive)
     * @param overlapStart overlap at start boundary [x, y, z]
     * @param overlapEnd overlap at end boundary [x, y, z]
     * @param channel channel index
     */
    public Chunk(int chunkId, int[] gridPosition, long[] globalStart, long[] globalEnd,
                 int[] overlapStart, int[] overlapEnd, int channel) {
        this.chunkId = chunkId;
        this.gridPosition = gridPosition.clone();
        this.globalStart = globalStart.clone();
        this.globalEnd = globalEnd.clone();
        this.overlapStart = overlapStart.clone();
        this.overlapEnd = overlapEnd.clone();
        this.channel = channel;

        this.dimensions = new long[3];
        for (int i = 0; i < 3; i++) {
            this.dimensions[i] = globalEnd[i] - globalStart[i];
        }

        this.processed = false;
        this.boundaryChunk = false;
    }

    /**
     * Get the chunk ID
     * @return chunk identifier
     */
    public int getChunkId() {
        return chunkId;
    }

    /**
     * Get the grid position of this chunk
     * @return array [chunkX, chunkY, chunkZ]
     */
    public int[] getGridPosition() {
        return gridPosition.clone();
    }

    /**
     * Get the global start coordinates
     * @return array [x, y, z]
     */
    public long[] getGlobalStart() {
        return globalStart.clone();
    }

    /**
     * Get the global end coordinates (exclusive)
     * @return array [x, y, z]
     */
    public long[] getGlobalEnd() {
        return globalEnd.clone();
    }

    /**
     * Get the dimensions of this chunk
     * @return array [width, height, depth]
     */
    public long[] getDimensions() {
        return dimensions.clone();
    }

    /**
     * Get the width of this chunk
     * @return width in pixels
     */
    public long getWidth() {
        return dimensions[0];
    }

    /**
     * Get the height of this chunk
     * @return height in pixels
     */
    public long getHeight() {
        return dimensions[1];
    }

    /**
     * Get the depth of this chunk
     * @return depth in slices
     */
    public long getDepth() {
        return dimensions[2];
    }

    /**
     * Get the channel index
     * @return channel
     */
    public int getChannel() {
        return channel;
    }

    /**
     * Get the overlap at the start boundary
     * @return array [x, y, z] overlap in pixels
     */
    public int[] getOverlapStart() {
        return overlapStart.clone();
    }

    /**
     * Get the overlap at the end boundary
     * @return array [x, y, z] overlap in pixels
     */
    public int[] getOverlapEnd() {
        return overlapEnd.clone();
    }

    /**
     * Get the core region (excluding overlap) start coordinates relative to chunk
     * @return array [x, y, z]
     */
    public int[] getCoreStart() {
        return overlapStart.clone();
    }

    /**
     * Get the core region (excluding overlap) end coordinates relative to chunk
     * @return array [x, y, z]
     */
    public int[] getCoreEnd() {
        int[] coreEnd = new int[3];
        for (int i = 0; i < 3; i++) {
            coreEnd[i] = (int) dimensions[i] - overlapEnd[i];
        }
        return coreEnd;
    }

    /**
     * Get the core region dimensions (excluding overlap)
     * @return array [width, height, depth]
     */
    public int[] getCoreDimensions() {
        int[] coreDims = new int[3];
        for (int i = 0; i < 3; i++) {
            coreDims[i] = (int) dimensions[i] - overlapStart[i] - overlapEnd[i];
        }
        return coreDims;
    }

    /**
     * Set the image data for this chunk
     * @param data ImageStack containing the chunk data
     */
    public void setData(ImageStack data) {
        this.data = data;
    }

    /**
     * Get the image data
     * @return ImageStack with chunk data
     */
    public ImageStack getData() {
        return data;
    }

    /**
     * Check if this chunk has data loaded
     * @return true if data is loaded
     */
    public boolean hasData() {
        return data != null;
    }

    /**
     * Clear the data to free memory
     */
    public void clearData() {
        this.data = null;
    }

    /**
     * Mark this chunk as processed
     * @param processed processing status
     */
    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    /**
     * Check if this chunk has been processed
     * @return true if processed
     */
    public boolean isProcessed() {
        return processed;
    }

    /**
     * Mark this as a boundary chunk
     * @param boundaryChunk true if at volume boundary
     */
    public void setBoundaryChunk(boolean boundaryChunk) {
        this.boundaryChunk = boundaryChunk;
    }

    /**
     * Check if this is a boundary chunk
     * @return true if at volume boundary
     */
    public boolean isBoundaryChunk() {
        return boundaryChunk;
    }

    /**
     * Check if a global coordinate is within this chunk (including overlap)
     * @param x global X coordinate
     * @param y global Y coordinate
     * @param z global Z coordinate
     * @return true if coordinate is in chunk
     */
    public boolean contains(long x, long y, long z) {
        return x >= globalStart[0] && x < globalEnd[0] &&
               y >= globalStart[1] && y < globalEnd[1] &&
               z >= globalStart[2] && z < globalEnd[2];
    }

    /**
     * Check if a global coordinate is within the core region (excluding overlap)
     * @param x global X coordinate
     * @param y global Y coordinate
     * @param z global Z coordinate
     * @return true if coordinate is in core region
     */
    public boolean containsInCore(long x, long y, long z) {
        long coreStartX = globalStart[0] + overlapStart[0];
        long coreStartY = globalStart[1] + overlapStart[1];
        long coreStartZ = globalStart[2] + overlapStart[2];
        long coreEndX = globalEnd[0] - overlapEnd[0];
        long coreEndY = globalEnd[1] - overlapEnd[1];
        long coreEndZ = globalEnd[2] - overlapEnd[2];

        return x >= coreStartX && x < coreEndX &&
               y >= coreStartY && y < coreEndY &&
               z >= coreStartZ && z < coreEndZ;
    }

    /**
     * Convert global coordinates to chunk-local coordinates
     * @param globalCoords global [x, y, z]
     * @return local [x, y, z] coordinates within this chunk
     */
    public int[] toLocalCoordinates(long[] globalCoords) {
        int[] local = new int[3];
        for (int i = 0; i < 3; i++) {
            local[i] = (int) (globalCoords[i] - globalStart[i]);
        }
        return local;
    }

    /**
     * Convert chunk-local coordinates to global coordinates
     * @param localCoords local [x, y, z] within chunk
     * @return global [x, y, z] coordinates
     */
    public long[] toGlobalCoordinates(int[] localCoords) {
        long[] global = new long[3];
        for (int i = 0; i < 3; i++) {
            global[i] = localCoords[i] + globalStart[i];
        }
        return global;
    }

    /**
     * Get estimated memory size of this chunk in bytes
     * @return memory size
     */
    public long getEstimatedMemorySize() {
        if (data == null) {
            return 0;
        }
        long pixelCount = dimensions[0] * dimensions[1] * dimensions[2];
        int bytesPerPixel = data.getBitDepth() / 8;
        return pixelCount * bytesPerPixel;
    }

    @Override
    public String toString() {
        return String.format("Chunk[id=%d, grid=%s, start=[%d,%d,%d], dim=[%d,%d,%d], ch=%d, processed=%b]",
                chunkId,
                java.util.Arrays.toString(gridPosition),
                globalStart[0], globalStart[1], globalStart[2],
                dimensions[0], dimensions[1], dimensions[2],
                channel, processed);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Chunk other = (Chunk) obj;
        return chunkId == other.chunkId && channel == other.channel;
    }

    @Override
    public int hashCode() {
        return 31 * chunkId + channel;
    }
}
