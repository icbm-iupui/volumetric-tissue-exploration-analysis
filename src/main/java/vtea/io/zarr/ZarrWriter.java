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

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import org.janelia.saalfeldlab.n5.Bzip2Compression;
import org.janelia.saalfeldlab.n5.Compression;
import org.janelia.saalfeldlab.n5.DataType;
import org.janelia.saalfeldlab.n5.GzipCompression;
import org.janelia.saalfeldlab.n5.Lz4Compression;
import org.janelia.saalfeldlab.n5.N5Writer;
import org.janelia.saalfeldlab.n5.RawCompression;
import org.janelia.saalfeldlab.n5.XzCompression;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Writer for Zarr format files with chunked output support.
 * Allows writing segmentation results, processed images, and analysis outputs to Zarr.
 *
 * @author sethwinfree
 */
public class ZarrWriter implements AutoCloseable {

    private final String zarrPath;
    private final N5Writer n5Writer;
    private Compression compression;
    private int[] defaultChunkSize;

    /**
     * Compression types supported by N5/Zarr
     */
    public enum CompressionType {
        NONE,
        GZIP,
        LZ4,
        BZIP2,
        XZ
    }

    /**
     * Constructor with default settings
     * @param zarrPath path to Zarr directory
     * @throws IOException if unable to create
     */
    public ZarrWriter(String zarrPath) throws IOException {
        this(zarrPath, CompressionType.GZIP, new int[]{512, 512, 32});
    }

    /**
     * Constructor with compression and chunk size
     * @param zarrPath path to Zarr directory
     * @param compressionType compression type
     * @param chunkSize default chunk size [x, y, z]
     * @throws IOException if unable to create
     */
    public ZarrWriter(String zarrPath, CompressionType compressionType, int[] chunkSize) throws IOException {
        this.zarrPath = zarrPath;
        this.defaultChunkSize = chunkSize.clone();
        this.n5Writer = new N5ZarrWriter(zarrPath);

        // Set compression
        switch (compressionType) {
            case GZIP:
                this.compression = new GzipCompression();
                break;
            case LZ4:
                // LZ4 compression with default block size (65536)
                this.compression = new Lz4Compression();
                break;
            case BZIP2:
                // Bzip2 compression with default block size (9 = best compression)
                this.compression = new Bzip2Compression();
                break;
            case XZ:
                // XZ compression with default preset (6 = balanced)
                this.compression = new XzCompression();
                break;
            case NONE:
            default:
                this.compression = new RawCompression();
                break;
        }
    }

    /**
     * Set compression type
     * @param compression Compression object
     */
    public void setCompression(Compression compression) {
        this.compression = compression;
    }

    /**
     * Set default chunk size
     * @param chunkSize chunk dimensions [x, y, z]
     */
    public void setDefaultChunkSize(int[] chunkSize) {
        this.defaultChunkSize = chunkSize.clone();
    }

    /**
     * Write ImagePlus to Zarr
     * @param imp ImagePlus to write
     * @param datasetName dataset name
     * @throws IOException if unable to write
     */
    public void writeImagePlus(ImagePlus imp, String datasetName) throws IOException {
        writeImagePlus(imp, datasetName, defaultChunkSize);
    }

    /**
     * Write ImagePlus to Zarr with custom chunk size
     * @param imp ImagePlus to write
     * @param datasetName dataset name
     * @param chunkSize chunk size [x, y, z]
     * @throws IOException if unable to write
     */
    public void writeImagePlus(ImagePlus imp, String datasetName, int[] chunkSize) throws IOException {
        // Convert ImagePlus to ImgLib2
        RandomAccessibleInterval<?> rai = ImageJFunctions.wrapReal(imp);

        // Determine data type
        DataType dataType;
        switch (imp.getBitDepth()) {
            case 8:
                dataType = DataType.UINT8;
                break;
            case 16:
                dataType = DataType.UINT16;
                break;
            case 32:
                dataType = DataType.FLOAT32;
                break;
            default:
                throw new IOException("Unsupported bit depth: " + imp.getBitDepth());
        }

        // Write using N5Utils
        N5Utils.save((RandomAccessibleInterval) rai, n5Writer, datasetName,
                    chunkSize, compression);

        // Write metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", imp.getTitle());
        metadata.put("dimensions", new long[]{imp.getWidth(), imp.getHeight(), imp.getNSlices()});
        metadata.put("channels", imp.getNChannels());
        metadata.put("frames", imp.getNFrames());
        n5Writer.setAttributes(datasetName, metadata);
    }

    /**
     * Write ImageStack to Zarr
     * @param stack ImageStack to write
     * @param datasetName dataset name
     * @param bitDepth bit depth (8, 16, or 32)
     * @throws IOException if unable to write
     */
    public void writeImageStack(ImageStack stack, String datasetName, int bitDepth) throws IOException {
        writeImageStack(stack, datasetName, bitDepth, defaultChunkSize);
    }

    /**
     * Write ImageStack to Zarr with custom chunk size
     * @param stack ImageStack to write
     * @param datasetName dataset name
     * @param bitDepth bit depth (8, 16, or 32)
     * @param chunkSize chunk size [x, y, z]
     * @throws IOException if unable to write
     */
    public void writeImageStack(ImageStack stack, String datasetName, int bitDepth, int[] chunkSize) throws IOException {
        int width = stack.getWidth();
        int height = stack.getHeight();
        int depth = stack.getSize();

        // Create ImgLib2 image
        Img<?> img;
        DataType dataType;

        if (bitDepth == 8) {
            img = ArrayImgs.unsignedBytes(width, height, depth);
            dataType = DataType.UINT8;
            copyStackToImg(stack, (Img<UnsignedByteType>) img);
        } else if (bitDepth == 16) {
            img = ArrayImgs.unsignedShorts(width, height, depth);
            dataType = DataType.UINT16;
            copyStackToImg(stack, (Img<UnsignedShortType>) img);
        } else if (bitDepth == 32) {
            img = ArrayImgs.floats(width, height, depth);
            dataType = DataType.FLOAT32;
            copyStackToImg(stack, (Img<FloatType>) img);
        } else {
            throw new IOException("Unsupported bit depth: " + bitDepth);
        }

        // Write to Zarr
        N5Utils.save(img, n5Writer, datasetName, chunkSize, compression);
    }

    /**
     * Copy ImageStack data to ImgLib2 Img
     */
    private <T extends net.imglib2.type.numeric.RealType<T>> void copyStackToImg(
            ImageStack stack, Img<T> img) {
        net.imglib2.Cursor<T> cursor = img.cursor();
        int z = 0;
        int sliceSize = stack.getWidth() * stack.getHeight();

        while (cursor.hasNext()) {
            cursor.fwd();
            long[] pos = new long[3];
            cursor.localize(pos);

            ImageProcessor ip = stack.getProcessor((int) pos[2] + 1);
            int index = (int) (pos[1] * stack.getWidth() + pos[0]);
            cursor.get().setReal(ip.getf(index));
        }
    }

    /**
     * Write 3D array to Zarr (for label maps, segmentation results)
     * @param data 3D array [z][y][x]
     * @param datasetName dataset name
     * @param dataType data type
     * @throws IOException if unable to write
     */
    public void write3DArray(Object data, String datasetName, DataType dataType) throws IOException {
        write3DArray(data, datasetName, dataType, defaultChunkSize);
    }

    /**
     * Write 3D array to Zarr with custom chunk size
     * @param data 3D array [z][y][x]
     * @param datasetName dataset name
     * @param dataType data type
     * @param chunkSize chunk size [x, y, z]
     * @throws IOException if unable to write
     */
    public void write3DArray(Object data, String datasetName, DataType dataType, int[] chunkSize) throws IOException {
        if (data instanceof byte[][][]) {
            byte[][][] byteData = (byte[][][]) data;
            int depth = byteData.length;
            int height = byteData[0].length;
            int width = byteData[0][0].length;

            Img<UnsignedByteType> img = ArrayImgs.unsignedBytes(width, height, depth);
            copyArrayToImg(byteData, img);
            N5Utils.save(img, n5Writer, datasetName, chunkSize, compression);

        } else if (data instanceof short[][][]) {
            short[][][] shortData = (short[][][]) data;
            int depth = shortData.length;
            int height = shortData[0].length;
            int width = shortData[0][0].length;

            Img<UnsignedShortType> img = ArrayImgs.unsignedShorts(width, height, depth);
            copyArrayToImg(shortData, img);
            N5Utils.save(img, n5Writer, datasetName, chunkSize, compression);

        } else if (data instanceof float[][][]) {
            float[][][] floatData = (float[][][]) data;
            int depth = floatData.length;
            int height = floatData[0].length;
            int width = floatData[0][0].length;

            Img<FloatType> img = ArrayImgs.floats(width, height, depth);
            copyArrayToImg(floatData, img);
            N5Utils.save(img, n5Writer, datasetName, chunkSize, compression);

        } else {
            throw new IOException("Unsupported array type");
        }
    }

    /**
     * Copy byte array to Img
     */
    private void copyArrayToImg(byte[][][] data, Img<UnsignedByteType> img) {
        net.imglib2.Cursor<UnsignedByteType> cursor = img.cursor();
        while (cursor.hasNext()) {
            cursor.fwd();
            long[] pos = new long[3];
            cursor.localize(pos);
            cursor.get().set(data[(int) pos[2]][(int) pos[1]][(int) pos[0]] & 0xFF);
        }
    }

    /**
     * Copy short array to Img
     */
    private void copyArrayToImg(short[][][] data, Img<UnsignedShortType> img) {
        net.imglib2.Cursor<UnsignedShortType> cursor = img.cursor();
        while (cursor.hasNext()) {
            cursor.fwd();
            long[] pos = new long[3];
            cursor.localize(pos);
            cursor.get().set(data[(int) pos[2]][(int) pos[1]][(int) pos[0]] & 0xFFFF);
        }
    }

    /**
     * Copy float array to Img
     */
    private void copyArrayToImg(float[][][] data, Img<FloatType> img) {
        net.imglib2.Cursor<FloatType> cursor = img.cursor();
        while (cursor.hasNext()) {
            cursor.fwd();
            long[] pos = new long[3];
            cursor.localize(pos);
            cursor.get().set(data[(int) pos[2]][(int) pos[1]][(int) pos[0]]);
        }
    }

    /**
     * Set dataset attributes/metadata
     * @param datasetName dataset name
     * @param attributes attribute map
     * @throws IOException if unable to write
     */
    public void setAttributes(String datasetName, Map<String, Object> attributes) throws IOException {
        n5Writer.setAttributes(datasetName, attributes);
    }

    /**
     * Create empty dataset
     * @param datasetName dataset name
     * @param dimensions dimensions [x, y, z, ...]
     * @param chunkSize chunk size
     * @param dataType data type
     * @throws IOException if unable to create
     */
    public void createDataset(String datasetName, long[] dimensions, int[] chunkSize,
                             DataType dataType) throws IOException {
        n5Writer.createDataset(datasetName, dimensions, chunkSize, dataType, compression);
    }

    /**
     * Remove a dataset
     * @param datasetName dataset name
     * @throws IOException if unable to remove
     */
    public void removeDataset(String datasetName) throws IOException {
        n5Writer.remove(datasetName);
    }

    /**
     * Check if dataset exists
     * @param datasetName dataset name
     * @return true if exists
     */
    public boolean datasetExists(String datasetName) {
        return n5Writer.exists(datasetName);
    }

    /**
     * Get the Zarr path
     * @return path string
     */
    public String getZarrPath() {
        return zarrPath;
    }

    /**
     * Get underlying N5 writer
     * @return N5Writer instance
     */
    public N5Writer getN5Writer() {
        return n5Writer;
    }

    @Override
    public void close() throws IOException {
        // N5Writer doesn't require explicit closing, but we can ensure any pending writes are complete
    }

    @Override
    public String toString() {
        return String.format("ZarrWriter[%s, compression=%s]",
                zarrPath, compression != null ? compression.getClass().getSimpleName() : "none");
    }
}
