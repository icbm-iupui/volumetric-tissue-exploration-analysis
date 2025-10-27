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

import ij.ImagePlus;
import ij.ImageStack;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * Base interface for all volume data sources in VTEA 2.0.
 * Provides abstraction over different storage backends (in-memory, Zarr, etc.)
 *
 * @author sethwinfree
 */
public interface VolumeDataset {

    /**
     * Get the dimensions of the volume [x, y, z, channels, timepoints]
     * @return long array with dimensions
     */
    long[] getDimensions();

    /**
     * Get width (X dimension)
     * @return width in pixels
     */
    long getWidth();

    /**
     * Get height (Y dimension)
     * @return height in pixels
     */
    long getHeight();

    /**
     * Get depth (Z dimension)
     * @return depth in slices
     */
    long getDepth();

    /**
     * Get number of channels
     * @return number of channels
     */
    int getNumChannels();

    /**
     * Get number of timepoints
     * @return number of timepoints
     */
    int getNumTimepoints();

    /**
     * Get the bit depth of the volume
     * @return bit depth (8, 16, 32, etc.)
     */
    int getBitDepth();

    /**
     * Check if this dataset is chunked/partitioned
     * @return true if chunked, false if in-memory
     */
    boolean isChunked();

    /**
     * Get the chunk dimensions if chunked
     * @return chunk dimensions [x, y, z] or null if not chunked
     */
    int[] getChunkDimensions();

    /**
     * Get pixel value at specific coordinates
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param channel channel index
     * @return pixel value as double
     */
    double getVoxel(long x, long y, long z, int channel);

    /**
     * Get a sub-volume as ImageStack for processing
     * @param xStart starting X coordinate
     * @param yStart starting Y coordinate
     * @param zStart starting Z coordinate
     * @param width width of region
     * @param height height of region
     * @param depth depth of region
     * @param channel channel to extract
     * @return ImageStack containing the sub-volume
     */
    ImageStack getSubVolume(long xStart, long yStart, long zStart,
                           long width, long height, long depth, int channel);

    /**
     * Get entire volume as ImagePlus (may be memory intensive for large volumes)
     * @return ImagePlus containing full volume
     */
    ImagePlus getImagePlus();

    /**
     * Get entire volume for a specific channel as ImagePlus
     * @param channel channel index
     * @return ImagePlus for specified channel
     */
    ImagePlus getImagePlus(int channel);

    /**
     * Get ImgLib2 RandomAccessibleInterval for advanced processing
     * @param <T> pixel type
     * @return RandomAccessibleInterval view of the data
     */
    <T extends RealType<T> & NativeType<T>> RandomAccessibleInterval<T> getImgLib2();

    /**
     * Get ImgLib2 RandomAccessibleInterval for specific channel
     * @param <T> pixel type
     * @param channel channel index
     * @return RandomAccessibleInterval view of the channel
     */
    <T extends RealType<T> & NativeType<T>> RandomAccessibleInterval<T> getImgLib2(int channel);

    /**
     * Get the source path or identifier for this dataset
     * @return path string or identifier
     */
    String getSource();

    /**
     * Get estimated memory footprint in bytes
     * @return memory size in bytes
     */
    long getEstimatedMemorySize();

    /**
     * Check if the entire dataset fits in available memory
     * @return true if can fit in RAM
     */
    boolean fitsInMemory();

    /**
     * Get metadata as key-value pairs
     * @param key metadata key
     * @return metadata value or null if not found
     */
    String getMetadata(String key);

    /**
     * Set metadata
     * @param key metadata key
     * @param value metadata value
     */
    void setMetadata(String key, String value);

    /**
     * Get the data type name
     * @return type name (e.g., "uint8", "uint16", "float32")
     */
    String getDataType();

    /**
     * Close and release resources
     */
    void close();
}
