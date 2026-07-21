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
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.janelia.saalfeldlab.n5.DataType;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrReader;
import vtea.partition.Chunk;
import vtea.partition.VolumePartitioner;

import java.io.IOException;
import java.util.*;

/**
 * Implementation of ChunkedVolumeDataset for Zarr-backed volumes.
 * Provides lazy loading and caching for efficient processing of large datasets.
 *
 * @author sethwinfree
 */
public class ZarrVolumeDataset implements ChunkedVolumeDataset {

    private final String zarrPath;
    private final String datasetName;
    private N5Reader n5Reader;
    private RandomAccessibleInterval<?> imgLib2Data;

    private long[] dimensions;  // [x, y, z, c, t]
    private int[] chunkDimensions;
    private int numChannels;
    private int numTimepoints;
    private DataType dataType;
    private int bitDepth;

    private final Map<String, String> metadata;
    private final Map<String, Chunk> chunkCache;
    private long maxCacheSize;
    private long currentCacheSize;

    private VolumePartitioner partitioner;
    private int[] overlapSize;

    private static final long DEFAULT_MAX_CACHE_SIZE = 2L * 1024 * 1024 * 1024; // 2GB

    /**
     * Constructor for Zarr dataset
     * @param zarrPath path to Zarr directory or file
     * @param datasetName name of dataset within Zarr (use "" for root)
     * @throws IOException if unable to open Zarr
     */
    public ZarrVolumeDataset(String zarrPath, String datasetName) throws IOException {
        this.zarrPath = zarrPath;
        this.datasetName = datasetName != null ? datasetName : "";
        this.metadata = new HashMap<>();
        this.chunkCache = new LinkedHashMap<>(16, 0.75f, true); // LRU cache
        this.maxCacheSize = DEFAULT_MAX_CACHE_SIZE;
        this.currentCacheSize = 0;
        this.overlapSize = new int[]{0, 0, 0};

        openZarr();
        initializePartitioner();
    }

    /**
     * Open and read Zarr metadata
     * @throws IOException if unable to read
     */
    private void openZarr() throws IOException {
        n5Reader = new N5ZarrReader(zarrPath);

        if (!n5Reader.exists(datasetName)) {
            throw new IOException("Dataset '" + datasetName + "' not found in Zarr: " + zarrPath);
        }

        // Get dataset attributes
        long[] dims = n5Reader.getDatasetAttributes(datasetName).getDimensions();
        chunkDimensions = n5Reader.getDatasetAttributes(datasetName).getBlockSize();
        dataType = n5Reader.getDatasetAttributes(datasetName).getDataType();

        // Handle different dimension orders (assume XYZCT or XYZ)
        if (dims.length == 5) {
            dimensions = new long[]{dims[0], dims[1], dims[2], dims[3], dims[4]};
            numChannels = (int) dims[3];
            numTimepoints = (int) dims[4];
        } else if (dims.length == 4) {
            dimensions = new long[]{dims[0], dims[1], dims[2], dims[3], 1};
            numChannels = (int) dims[3];
            numTimepoints = 1;
        } else if (dims.length == 3) {
            dimensions = new long[]{dims[0], dims[1], dims[2], 1, 1};
            numChannels = 1;
            numTimepoints = 1;
        } else {
            throw new IOException("Unsupported number of dimensions: " + dims.length);
        }

        // Determine bit depth
        switch (dataType) {
            case UINT8:
            case INT8:
                bitDepth = 8;
                break;
            case UINT16:
            case INT16:
                bitDepth = 16;
                break;
            case UINT32:
            case INT32:
            case FLOAT32:
                bitDepth = 32;
                break;
            case FLOAT64:
                bitDepth = 64;
                break;
            default:
                bitDepth = 32;
        }

        // Load metadata
        loadMetadata();
    }

    /**
     * Load Zarr attributes as metadata
     */
    private void loadMetadata() {
        try {
            // Get all attribute keys and read each one
            Map<String, Class<?>> attributeList = n5Reader.listAttributes(datasetName);
            if (attributeList != null) {
                for (String key : attributeList.keySet()) {
                    try {
                        Object value = n5Reader.getAttribute(datasetName, key, Object.class);
                        if (value != null) {
                            metadata.put(key, value.toString());
                        }
                    } catch (Exception ex) {
                        // Skip individual attributes that can't be read
                    }
                }
            }
        } catch (Exception e) {
            // Metadata not critical
            System.err.println("Warning: Could not load Zarr metadata: " + e.getMessage());
        }
    }

    /**
     * Initialize the volume partitioner
     */
    private void initializePartitioner() {
        long[] volDims = new long[]{dimensions[0], dimensions[1], dimensions[2]};
        int[] chunkDims = new int[]{
            Math.min(chunkDimensions[0], (int) dimensions[0]),
            Math.min(chunkDimensions[1], (int) dimensions[1]),
            chunkDimensions.length > 2 ? Math.min(chunkDimensions[2], (int) dimensions[2]) : (int) dimensions[2]
        };
        partitioner = new VolumePartitioner(volDims, chunkDims);
    }

    @Override
    public long[] getDimensions() {
        return dimensions.clone();
    }

    @Override
    public long getWidth() {
        return dimensions[0];
    }

    @Override
    public long getHeight() {
        return dimensions[1];
    }

    @Override
    public long getDepth() {
        return dimensions[2];
    }

    @Override
    public int getNumChannels() {
        return numChannels;
    }

    @Override
    public int getNumTimepoints() {
        return numTimepoints;
    }

    @Override
    public int getBitDepth() {
        return bitDepth;
    }

    @Override
    public boolean isChunked() {
        return true;
    }

    @Override
    public int[] getChunkDimensions() {
        return chunkDimensions.clone();
    }

    @Override
    public double getVoxel(long x, long y, long z, int channel) {
        try {
            RandomAccessibleInterval<?> rai = getImgLib2RAI(channel);
            if (rai == null) return 0.0;

            // Access voxel through ImgLib2
            if (bitDepth == 8) {
                net.imglib2.RandomAccess<UnsignedByteType> ra = ((RandomAccessibleInterval<UnsignedByteType>) rai).randomAccess();
                ra.setPosition(new long[]{x, y, z});
                return ra.get().getRealDouble();
            } else if (bitDepth == 16) {
                net.imglib2.RandomAccess<UnsignedShortType> ra = ((RandomAccessibleInterval<UnsignedShortType>) rai).randomAccess();
                ra.setPosition(new long[]{x, y, z});
                return ra.get().getRealDouble();
            } else {
                net.imglib2.RandomAccess<FloatType> ra = ((RandomAccessibleInterval<FloatType>) rai).randomAccess();
                ra.setPosition(new long[]{x, y, z});
                return ra.get().getRealDouble();
            }
        } catch (Exception e) {
            System.err.println("Error reading voxel: " + e.getMessage());
            return 0.0;
        }
    }

    @Override
    public ImageStack getSubVolume(long xStart, long yStart, long zStart,
                                   long width, long height, long depth, int channel) {
        try {
            RandomAccessibleInterval<?> rai = getImgLib2RAI(channel);
            if (rai == null) return null;

            // Crop to region
            RandomAccessibleInterval<?> cropped = Views.interval(rai,
                new long[]{xStart, yStart, zStart},
                new long[]{xStart + width - 1, yStart + height - 1, zStart + depth - 1});

            // Convert to ImageStack
            return raiToImageStack(cropped, (int) width, (int) height, (int) depth);

        } catch (Exception e) {
            System.err.println("Error reading sub-volume: " + e.getMessage());
            return null;
        }
    }

    /**
     * Convert RandomAccessibleInterval to ImageStack
     */
    private ImageStack raiToImageStack(RandomAccessibleInterval<?> rai, int width, int height, int depth) {
        ImageStack stack = new ImageStack(width, height);

        for (int z = 0; z < depth; z++) {
            ImageProcessor ip;

            if (bitDepth == 8) {
                ip = new ByteProcessor(width, height);
                RandomAccessibleInterval<UnsignedByteType> slice =
                    Views.hyperSlice((RandomAccessibleInterval<UnsignedByteType>) rai, 2, z);
                copyToProcessor(slice, ip, width, height);
            } else if (bitDepth == 16) {
                ip = new ShortProcessor(width, height);
                RandomAccessibleInterval<UnsignedShortType> slice =
                    Views.hyperSlice((RandomAccessibleInterval<UnsignedShortType>) rai, 2, z);
                copyToProcessor(slice, ip, width, height);
            } else {
                ip = new FloatProcessor(width, height);
                RandomAccessibleInterval<FloatType> slice =
                    Views.hyperSlice((RandomAccessibleInterval<FloatType>) rai, 2, z);
                copyToProcessor(slice, ip, width, height);
            }

            stack.addSlice(ip);
        }

        return stack;
    }

    /**
     * Copy ImgLib2 data to ImageProcessor
     */
    private <T extends RealType<T>> void copyToProcessor(RandomAccessibleInterval<T> rai,
                                                          ImageProcessor ip, int width, int height) {
        net.imglib2.Cursor<T> cursor = Views.flatIterable(rai).cursor();
        int i = 0;
        while (cursor.hasNext()) {
            ip.setf(i++, cursor.next().getRealFloat());
        }
    }

    @Override
    public ImagePlus getImagePlus() {
        return getImagePlus(0);
    }

    @Override
    public ImagePlus getImagePlus(int channel) {
        try {
            RandomAccessibleInterval<?> rai = getImgLib2RAI(channel);
            if (rai == null) return null;

            return ImageJFunctions.wrap((RandomAccessibleInterval) rai, datasetName + "_ch" + channel);
        } catch (Exception e) {
            System.err.println("Error creating ImagePlus: " + e.getMessage());
            return null;
        }
    }

    @Override
    public <T extends RealType<T> & NativeType<T>> RandomAccessibleInterval<T> getImgLib2() {
        return getImgLib2(0);
    }

    @Override
    public <T extends RealType<T> & NativeType<T>> RandomAccessibleInterval<T> getImgLib2(int channel) {
        return (RandomAccessibleInterval<T>) getImgLib2RAI(channel);
    }

    /**
     * Internal method to get ImgLib2 RAI for a channel
     */
    private RandomAccessibleInterval<?> getImgLib2RAI(int channel) {
        try {
            RandomAccessibleInterval<?> fullRAI = N5Utils.open(n5Reader, datasetName);

            // Extract channel if multi-channel
            if (numChannels > 1 && fullRAI.numDimensions() >= 4) {
                return Views.hyperSlice(fullRAI, 3, channel);
            }

            return fullRAI;
        } catch (IOException e) {
            System.err.println("Error opening Zarr with N5: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String getSource() {
        return zarrPath + "/" + datasetName;
    }

    @Override
    public long getEstimatedMemorySize() {
        long pixelCount = dimensions[0] * dimensions[1] * dimensions[2] * numChannels * numTimepoints;
        return pixelCount * (bitDepth / 8);
    }

    @Override
    public boolean fitsInMemory() {
        long estimatedSize = getEstimatedMemorySize();
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        return estimatedSize < (maxMemory * 0.5);
    }

    @Override
    public String getMetadata(String key) {
        return metadata.get(key);
    }

    @Override
    public void setMetadata(String key, String value) {
        metadata.put(key, value);
    }

    @Override
    public String getDataType() {
        return dataType != null ? dataType.toString().toLowerCase() : "unknown";
    }

    @Override
    public void close() {
        clearCache();
        if (n5Reader != null) {
            // N5Reader doesn't require explicit closing
            n5Reader = null;
        }
    }

    // ChunkedVolumeDataset methods

    @Override
    public int[] getNumChunks() {
        return partitioner.getNumChunks();
    }

    @Override
    public int getTotalChunkCount() {
        return partitioner.getTotalChunkCount();
    }

    @Override
    public Chunk getChunk(int chunkIndex, int channel) {
        int[] numChunks = getNumChunks();
        int chunksPerSlice = numChunks[0] * numChunks[1];
        int chunkZ = chunkIndex / chunksPerSlice;
        int remainder = chunkIndex % chunksPerSlice;
        int chunkY = remainder / numChunks[0];
        int chunkX = remainder % numChunks[0];

        return getChunk(chunkX, chunkY, chunkZ, channel);
    }

    @Override
    public Chunk getChunk(int chunkX, int chunkY, int chunkZ, int channel) {
        String cacheKey = chunkX + "_" + chunkY + "_" + chunkZ + "_" + channel;

        // Check cache
        if (chunkCache.containsKey(cacheKey)) {
            return chunkCache.get(cacheKey);
        }

        // Generate chunk metadata
        int chunkId = chunkX + chunkY * getNumChunks()[0] +
                     chunkZ * getNumChunks()[0] * getNumChunks()[1];
        Chunk chunk = partitioner.generateChunk(chunkX, chunkY, chunkZ, channel, chunkId);

        // Load data
        ImageStack data = getSubVolume(
            chunk.getGlobalStart()[0], chunk.getGlobalStart()[1], chunk.getGlobalStart()[2],
            chunk.getWidth(), chunk.getHeight(), chunk.getDepth(), channel);

        chunk.setData(data);

        // Add to cache
        addToCache(cacheKey, chunk);

        return chunk;
    }

    @Override
    public Iterator<Chunk> getChunkIterator(int channel) {
        return getChunkIterator(channel, 0.0);
    }

    @Override
    public Iterator<Chunk> getChunkIterator(int channel, double overlapPercent) {
        List<Chunk> chunks = partitioner.generateChunks(channel);
        return chunks.iterator();
    }

    @Override
    public List<Chunk> getIntersectingChunks(long xStart, long yStart, long zStart,
                                            long width, long height, long depth, int channel) {
        return partitioner.getIntersectingChunks(xStart, yStart, zStart, width, height, depth, channel);
    }

    @Override
    public Chunk getChunkContaining(long x, long y, long z, int channel) {
        int[] numChunks = getNumChunks();
        int[] coreSize = new int[3];
        for (int i = 0; i < 3; i++) {
            coreSize[i] = chunkDimensions[i] - 2 * overlapSize[i];
        }

        int chunkX = (int) (x / coreSize[0]);
        int chunkY = (int) (y / coreSize[1]);
        int chunkZ = (int) (z / coreSize[2]);

        chunkX = Math.min(chunkX, numChunks[0] - 1);
        chunkY = Math.min(chunkY, numChunks[1] - 1);
        chunkZ = Math.min(chunkZ, numChunks[2] - 1);

        return getChunk(chunkX, chunkY, chunkZ, channel);
    }

    @Override
    public void preloadChunks(int[] chunkIndices, int channel) {
        for (int index : chunkIndices) {
            getChunk(index, channel);
        }
    }

    @Override
    public void clearCache() {
        chunkCache.clear();
        currentCacheSize = 0;
    }

    @Override
    public long getCacheSize() {
        return currentCacheSize;
    }

    @Override
    public void setMaxCacheSize(long maxSize) {
        this.maxCacheSize = maxSize;
        evictCacheIfNeeded();
    }

    /**
     * Add chunk to cache with LRU eviction
     */
    private void addToCache(String key, Chunk chunk) {
        long chunkSize = chunk.getEstimatedMemorySize();
        currentCacheSize += chunkSize;
        chunkCache.put(key, chunk);
        evictCacheIfNeeded();
    }

    /**
     * Evict oldest chunks if cache exceeds max size
     */
    private void evictCacheIfNeeded() {
        while (currentCacheSize > maxCacheSize && !chunkCache.isEmpty()) {
            Iterator<Map.Entry<String, Chunk>> it = chunkCache.entrySet().iterator();
            if (it.hasNext()) {
                Map.Entry<String, Chunk> entry = it.next();
                currentCacheSize -= entry.getValue().getEstimatedMemorySize();
                entry.getValue().clearData();
                it.remove();
            }
        }
    }

    @Override
    public String getCompressionType() {
        String compression = metadata.get("compressor");
        return compression != null ? compression : "unknown";
    }

    @Override
    public double getCompressionRatio() {
        // Would need to track actual vs compressed size
        return 1.0;
    }

    @Override
    public boolean isChunkCached(int chunkIndex, int channel) {
        int[] numChunks = getNumChunks();
        int chunksPerSlice = numChunks[0] * numChunks[1];
        int chunkZ = chunkIndex / chunksPerSlice;
        int remainder = chunkIndex % chunksPerSlice;
        int chunkY = remainder / numChunks[0];
        int chunkX = remainder % numChunks[0];

        String cacheKey = chunkX + "_" + chunkY + "_" + chunkZ + "_" + channel;
        return chunkCache.containsKey(cacheKey);
    }

    @Override
    public int[] getOverlapSize() {
        return overlapSize.clone();
    }

    @Override
    public void setOverlapSize(int overlapX, int overlapY, int overlapZ) {
        this.overlapSize = new int[]{overlapX, overlapY, overlapZ};
        partitioner.setOverlapSize(overlapSize);
    }

    @Override
    public String toString() {
        return String.format("ZarrVolumeDataset[%s, %dx%dx%d, %d channels, %s, chunked=%dx%dx%d]",
                zarrPath, dimensions[0], dimensions[1], dimensions[2],
                numChannels, getDataType(),
                chunkDimensions[0], chunkDimensions[1],
                chunkDimensions.length > 2 ? chunkDimensions[2] : 1);
    }
}
