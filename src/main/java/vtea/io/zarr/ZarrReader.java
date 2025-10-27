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
package vtea.io.zarr;

import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.janelia.saalfeldlab.n5.DataType;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrReader;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Reader for Zarr format files with efficient chunk-based access.
 * Provides methods to read entire volumes, sub-volumes, and individual chunks.
 *
 * @author sethwinfree
 */
public class ZarrReader implements AutoCloseable {

    private final String zarrPath;
    private final N5Reader n5Reader;
    private final Map<String, DatasetInfo> datasetCache;

    /**
     * Dataset metadata container
     */
    public static class DatasetInfo {
        public final String name;
        public final long[] dimensions;
        public final int[] chunkSize;
        public final DataType dataType;
        public final Map<String, Object> attributes;

        public DatasetInfo(String name, long[] dimensions, int[] chunkSize,
                          DataType dataType, Map<String, Object> attributes) {
            this.name = name;
            this.dimensions = dimensions.clone();
            this.chunkSize = chunkSize.clone();
            this.dataType = dataType;
            this.attributes = new HashMap<>(attributes != null ? attributes : new HashMap<>());
        }

        public int getBitDepth() {
            switch (dataType) {
                case UINT8:
                case INT8:
                    return 8;
                case UINT16:
                case INT16:
                    return 16;
                case UINT32:
                case INT32:
                case FLOAT32:
                    return 32;
                case FLOAT64:
                    return 64;
                default:
                    return 32;
            }
        }

        @Override
        public String toString() {
            return String.format("Dataset[%s, dims=%s, chunks=%s, type=%s]",
                    name, Arrays.toString(dimensions), Arrays.toString(chunkSize), dataType);
        }
    }

    /**
     * Constructor
     * @param zarrPath path to Zarr directory
     * @throws IOException if unable to open
     */
    public ZarrReader(String zarrPath) throws IOException {
        this.zarrPath = zarrPath;
        this.datasetCache = new HashMap<>();

        // Verify path exists
        File zarrFile = new File(zarrPath);
        if (!zarrFile.exists()) {
            throw new IOException("Zarr path does not exist: " + zarrPath);
        }

        // Open N5 Zarr reader
        this.n5Reader = new N5ZarrReader(zarrPath);
    }

    /**
     * Get list of datasets in Zarr
     * @return array of dataset names
     * @throws IOException if unable to list
     */
    public String[] listDatasets() throws IOException {
        return n5Reader.list("/");
    }

    /**
     * Check if dataset exists
     * @param datasetName dataset name
     * @return true if exists
     */
    public boolean datasetExists(String datasetName) {
        return n5Reader.exists(datasetName);
    }

    /**
     * Get dataset metadata
     * @param datasetName dataset name
     * @return DatasetInfo object
     * @throws IOException if unable to read
     */
    public DatasetInfo getDatasetInfo(String datasetName) throws IOException {
        // Check cache
        if (datasetCache.containsKey(datasetName)) {
            return datasetCache.get(datasetName);
        }

        if (!n5Reader.exists(datasetName)) {
            throw new IOException("Dataset not found: " + datasetName);
        }

        DatasetAttributes attrs = n5Reader.getDatasetAttributes(datasetName);
        Map<String, Object> attributes = new HashMap<>();

        try {
            Map<String, ?> zarrAttrs = n5Reader.getAttributes(datasetName);
            if (zarrAttrs != null) {
                attributes.putAll(zarrAttrs);
            }
        } catch (Exception e) {
            // Attributes not critical
        }

        DatasetInfo info = new DatasetInfo(
                datasetName,
                attrs.getDimensions(),
                attrs.getBlockSize(),
                attrs.getDataType(),
                attributes
        );

        datasetCache.put(datasetName, info);
        return info;
    }

    /**
     * Read entire dataset as ImgLib2 RandomAccessibleInterval
     * @param <T> pixel type
     * @param datasetName dataset name
     * @return RandomAccessibleInterval
     * @throws IOException if unable to read
     */
    public <T> RandomAccessibleInterval<T> readDataset(String datasetName) throws IOException {
        return (RandomAccessibleInterval<T>) N5Utils.open(n5Reader, datasetName);
    }

    /**
     * Read sub-volume as ImageStack
     * @param datasetName dataset name
     * @param xStart start X
     * @param yStart start Y
     * @param zStart start Z
     * @param width width
     * @param height height
     * @param depth depth
     * @return ImageStack
     * @throws IOException if unable to read
     */
    public ImageStack readSubVolume(String datasetName, long xStart, long yStart, long zStart,
                                    int width, int height, int depth) throws IOException {
        DatasetInfo info = getDatasetInfo(datasetName);
        RandomAccessibleInterval<?> rai = readDataset(datasetName);

        // Clip to bounds
        long xEnd = Math.min(xStart + width, info.dimensions[0]);
        long yEnd = Math.min(yStart + height, info.dimensions[1]);
        long zEnd = Math.min(zStart + depth, info.dimensions.length > 2 ? info.dimensions[2] : 1);

        // Create interval
        RandomAccessibleInterval<?> cropped = Views.interval(rai,
                new long[]{xStart, yStart, zStart},
                new long[]{xEnd - 1, yEnd - 1, zEnd - 1});

        // Convert to ImageStack
        return convertToImageStack(cropped, info.getBitDepth(),
                (int) (xEnd - xStart), (int) (yEnd - yStart), (int) (zEnd - zStart));
    }

    /**
     * Read single Z-slice as ImageProcessor
     * @param datasetName dataset name
     * @param z slice index
     * @return ImageProcessor
     * @throws IOException if unable to read
     */
    public ImageProcessor readSlice(String datasetName, int z) throws IOException {
        DatasetInfo info = getDatasetInfo(datasetName);
        RandomAccessibleInterval<?> rai = readDataset(datasetName);

        if (rai.numDimensions() < 3) {
            throw new IOException("Dataset must have at least 3 dimensions");
        }

        RandomAccessibleInterval<?> slice = Views.hyperSlice(rai, 2, z);
        return convertToProcessor(slice, info.getBitDepth(),
                (int) info.dimensions[0], (int) info.dimensions[1]);
    }

    /**
     * Read voxel value
     * @param datasetName dataset name
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return pixel value
     * @throws IOException if unable to read
     */
    public double readVoxel(String datasetName, long x, long y, long z) throws IOException {
        DatasetInfo info = getDatasetInfo(datasetName);
        RandomAccessibleInterval<?> rai = readDataset(datasetName);

        // Bounds check
        if (x < 0 || x >= info.dimensions[0] ||
            y < 0 || y >= info.dimensions[1] ||
            (info.dimensions.length > 2 && (z < 0 || z >= info.dimensions[2]))) {
            return 0.0;
        }

        switch (info.dataType) {
            case UINT8:
            case INT8:
                return ((RandomAccessibleInterval<UnsignedByteType>) rai)
                        .randomAccess().setPositionAndGet(x, y, z).getRealDouble();
            case UINT16:
            case INT16:
                return ((RandomAccessibleInterval<UnsignedShortType>) rai)
                        .randomAccess().setPositionAndGet(x, y, z).getRealDouble();
            case UINT32:
            case INT32:
            case FLOAT32:
                return ((RandomAccessibleInterval<FloatType>) rai)
                        .randomAccess().setPositionAndGet(x, y, z).getRealDouble();
            default:
                return 0.0;
        }
    }

    /**
     * Convert ImgLib2 RAI to ImageStack
     */
    private ImageStack convertToImageStack(RandomAccessibleInterval<?> rai, int bitDepth,
                                          int width, int height, int depth) {
        ImageStack stack = new ImageStack(width, height);

        for (int z = 0; z < depth; z++) {
            RandomAccessibleInterval<?> slice = Views.hyperSlice(rai, 2, z);
            ImageProcessor ip = convertToProcessor(slice, bitDepth, width, height);
            stack.addSlice(ip);
        }

        return stack;
    }

    /**
     * Convert ImgLib2 2D slice to ImageProcessor
     */
    private ImageProcessor convertToProcessor(RandomAccessibleInterval<?> slice,
                                             int bitDepth, int width, int height) {
        ImageProcessor ip;

        if (bitDepth == 8) {
            ip = new ByteProcessor(width, height);
            copyToProcessor((RandomAccessibleInterval<UnsignedByteType>) slice, ip);
        } else if (bitDepth == 16) {
            ip = new ShortProcessor(width, height);
            copyToProcessor((RandomAccessibleInterval<UnsignedShortType>) slice, ip);
        } else {
            ip = new FloatProcessor(width, height);
            copyToProcessor((RandomAccessibleInterval<FloatType>) slice, ip);
        }

        return ip;
    }

    /**
     * Copy ImgLib2 data to ImageProcessor
     */
    private <T extends net.imglib2.type.numeric.RealType<T>> void copyToProcessor(
            RandomAccessibleInterval<T> rai, ImageProcessor ip) {
        var cursor = Views.flatIterable(rai).cursor();
        int i = 0;
        while (cursor.hasNext()) {
            ip.setf(i++, cursor.next().getRealFloat());
        }
    }

    /**
     * Get the Zarr path
     * @return path string
     */
    public String getZarrPath() {
        return zarrPath;
    }

    /**
     * Get underlying N5 reader
     * @return N5Reader instance
     */
    public N5Reader getN5Reader() {
        return n5Reader;
    }

    @Override
    public void close() {
        datasetCache.clear();
        // N5Reader doesn't require explicit closing
    }

    @Override
    public String toString() {
        return String.format("ZarrReader[%s, datasets=%d]", zarrPath, datasetCache.size());
    }
}
