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
package vtea.dataset.volume;

import ij.ImageStack;
import vtea.partition.Chunk;

import java.util.Iterator;
import java.util.List;

/**
 * Interface for chunked/partitioned volume datasets.
 * Supports efficient processing of large volumes that don't fit in RAM.
 *
 * @author sethwinfree
 */
public interface ChunkedVolumeDataset extends VolumeDataset {

    /**
     * Get the number of chunks in each dimension
     * @return array [numChunksX, numChunksY, numChunksZ]
     */
    int[] getNumChunks();

    /**
     * Get total number of chunks
     * @return total chunk count
     */
    int getTotalChunkCount();

    /**
     * Get a specific chunk by index
     * @param chunkIndex chunk index
     * @param channel channel index
     * @return Chunk object containing data and metadata
     */
    Chunk getChunk(int chunkIndex, int channel);

    /**
     * Get chunk by 3D grid coordinates
     * @param chunkX X chunk coordinate
     * @param chunkY Y chunk coordinate
     * @param chunkZ Z chunk coordinate
     * @param channel channel index
     * @return Chunk object
     */
    Chunk getChunk(int chunkX, int chunkY, int chunkZ, int channel);

    /**
     * Get an iterator over all chunks
     * @param channel channel to iterate over
     * @return Iterator of Chunks
     */
    Iterator<Chunk> getChunkIterator(int channel);

    /**
     * Get an iterator over chunks with specific settings
     * @param channel channel index
     * @param overlapPercent percentage of overlap between chunks (0.0 to 0.5)
     * @return Iterator of overlapping Chunks
     */
    Iterator<Chunk> getChunkIterator(int channel, double overlapPercent);

    /**
     * Get all chunks that intersect with a given region
     * @param xStart start X coordinate
     * @param yStart start Y coordinate
     * @param zStart start Z coordinate
     * @param width region width
     * @param height region height
     * @param depth region depth
     * @param channel channel index
     * @return List of overlapping chunks
     */
    List<Chunk> getIntersectingChunks(long xStart, long yStart, long zStart,
                                     long width, long height, long depth, int channel);

    /**
     * Get chunk that contains the specified voxel
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param channel channel index
     * @return Chunk containing the voxel
     */
    Chunk getChunkContaining(long x, long y, long z, int channel);

    /**
     * Preload/cache specific chunks for faster access
     * @param chunkIndices indices of chunks to preload
     * @param channel channel index
     */
    void preloadChunks(int[] chunkIndices, int channel);

    /**
     * Clear cached chunks to free memory
     */
    void clearCache();

    /**
     * Get current cache size in bytes
     * @return cache size
     */
    long getCacheSize();

    /**
     * Set maximum cache size in bytes
     * @param maxSize maximum cache size
     */
    void setMaxCacheSize(long maxSize);

    /**
     * Get compression type used for chunks
     * @return compression name (e.g., "blosc", "gzip", "none")
     */
    String getCompressionType();

    /**
     * Get compression ratio
     * @return compression ratio (uncompressed/compressed)
     */
    double getCompressionRatio();

    /**
     * Check if chunk is currently in cache
     * @param chunkIndex chunk index
     * @param channel channel index
     * @return true if cached
     */
    boolean isChunkCached(int chunkIndex, int channel);

    /**
     * Get the overlap region size in pixels
     * @return overlap size [x, y, z]
     */
    int[] getOverlapSize();

    /**
     * Set the overlap region size for chunk retrieval
     * @param overlapX overlap in X dimension
     * @param overlapY overlap in Y dimension
     * @param overlapZ overlap in Z dimension
     */
    void setOverlapSize(int overlapX, int overlapY, int overlapZ);
}
