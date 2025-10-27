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
package vtea.utilities.conversion;

import ij.IJ;
import ij.ImagePlus;
import loci.plugins.BF;
import loci.plugins.in.ImporterOptions;
import vtea.io.zarr.ZarrWriter;
import vtea.io.zarr.ZarrWriter.CompressionType;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for converting TIFF/OME-TIFF files to Zarr format.
 * Supports progress tracking and various compression options.
 *
 * @author sethwinfree
 */
public class TiffToZarrConverter {

    private CompressionType compressionType = CompressionType.GZIP;
    private int[] chunkSize = new int[]{512, 512, 32};
    private boolean preserveMetadata = true;
    private ProgressCallback progressCallback;

    /**
     * Progress callback interface
     */
    public interface ProgressCallback {
        void onProgress(String message, double progress);
    }

    /**
     * Default constructor
     */
    public TiffToZarrConverter() {
    }

    /**
     * Set compression type
     * @param type compression type
     */
    public void setCompressionType(CompressionType type) {
        this.compressionType = type;
    }

    /**
     * Set chunk size
     * @param chunkSize chunk dimensions [x, y, z]
     */
    public void setChunkSize(int[] chunkSize) {
        if (chunkSize.length >= 3) {
            this.chunkSize = chunkSize.clone();
        }
    }

    /**
     * Set whether to preserve metadata
     * @param preserve true to preserve
     */
    public void setPreserveMetadata(boolean preserve) {
        this.preserveMetadata = preserve;
    }

    /**
     * Set progress callback
     * @param callback callback to invoke
     */
    public void setProgressCallback(ProgressCallback callback) {
        this.progressCallback = callback;
    }

    /**
     * Convert a single TIFF file to Zarr
     * @param tiffPath path to TIFF file
     * @param zarrPath output Zarr directory path
     * @return true if successful
     */
    public boolean convert(String tiffPath, String zarrPath) {
        return convert(tiffPath, zarrPath, "data");
    }

    /**
     * Convert TIFF to Zarr with custom dataset name
     * @param tiffPath path to TIFF file
     * @param zarrPath output Zarr directory path
     * @param datasetName name for dataset within Zarr
     * @return true if successful
     */
    public boolean convert(String tiffPath, String zarrPath, String datasetName) {
        try {
            notifyProgress("Reading TIFF file: " + tiffPath, 0.0);

            // Load TIFF using Bio-Formats or ImageJ
            ImagePlus imp = loadTiff(tiffPath);

            if (imp == null) {
                System.err.println("Failed to load TIFF: " + tiffPath);
                return false;
            }

            notifyProgress(String.format("Loaded: %dx%dx%d, %d channels, %d-bit",
                          imp.getWidth(), imp.getHeight(), imp.getNSlices(),
                          imp.getNChannels(), imp.getBitDepth()), 0.2);

            // Create Zarr writer
            notifyProgress("Creating Zarr file: " + zarrPath, 0.3);
            ZarrWriter writer = new ZarrWriter(zarrPath, compressionType, chunkSize);

            // Convert channels separately if multi-channel
            if (imp.getNChannels() > 1) {
                for (int c = 1; c <= imp.getNChannels(); c++) {
                    notifyProgress("Converting channel " + c + "/" + imp.getNChannels(),
                                 0.3 + (0.6 * c / imp.getNChannels()));

                    ImagePlus channelImp = extractChannel(imp, c);
                    String channelDataset = datasetName + "/c" + c;
                    writer.writeImagePlus(channelImp, channelDataset, chunkSize);

                    if (preserveMetadata) {
                        writeChannelMetadata(writer, channelDataset, imp, c);
                    }
                }
            } else {
                notifyProgress("Converting image data", 0.4);
                writer.writeImagePlus(imp, datasetName, chunkSize);

                if (preserveMetadata) {
                    writeImageMetadata(writer, datasetName, imp);
                }
            }

            writer.close();
            imp.close();

            notifyProgress("Conversion complete", 1.0);
            return true;

        } catch (Exception e) {
            System.err.println("Error converting TIFF to Zarr: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Load TIFF file using Bio-Formats or ImageJ
     * @param tiffPath path to TIFF
     * @return ImagePlus
     */
    private ImagePlus loadTiff(String tiffPath) {
        try {
            // Try Bio-Formats first for better format support
            ImporterOptions options = new ImporterOptions();
            options.setId(tiffPath);
            options.setVirtual(false);
            options.setGroupFiles(true);

            ImagePlus[] imps = BF.openImagePlus(options);
            if (imps != null && imps.length > 0) {
                return imps[0];
            }
        } catch (Exception e) {
            System.out.println("Bio-Formats failed, trying ImageJ: " + e.getMessage());
        }

        // Fallback to ImageJ
        return IJ.openImage(tiffPath);
    }

    /**
     * Extract a single channel from multi-channel ImagePlus
     * @param imp ImagePlus
     * @param channel channel number (1-based)
     * @return single-channel ImagePlus
     */
    private ImagePlus extractChannel(ImagePlus imp, int channel) {
        ImagePlus dup = imp.duplicate();
        dup.setC(channel);
        return new ImagePlus("Channel_" + channel, dup.getImageStack());
    }

    /**
     * Write image metadata to Zarr
     * @param writer ZarrWriter
     * @param datasetName dataset name
     * @param imp ImagePlus
     * @throws IOException if unable to write
     */
    private void writeImageMetadata(ZarrWriter writer, String datasetName, ImagePlus imp) throws IOException {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source_file", imp.getOriginalFileInfo() != null ?
                   imp.getOriginalFileInfo().fileName : imp.getTitle());
        metadata.put("dimensions", new long[]{imp.getWidth(), imp.getHeight(), imp.getNSlices()});
        metadata.put("channels", imp.getNChannels());
        metadata.put("frames", imp.getNFrames());
        metadata.put("bit_depth", imp.getBitDepth());
        metadata.put("voxel_width", imp.getCalibration().pixelWidth);
        metadata.put("voxel_height", imp.getCalibration().pixelHeight);
        metadata.put("voxel_depth", imp.getCalibration().pixelDepth);
        metadata.put("unit", imp.getCalibration().getUnit());

        writer.setAttributes(datasetName, metadata);
    }

    /**
     * Write channel-specific metadata to Zarr
     * @param writer ZarrWriter
     * @param datasetName dataset name
     * @param imp ImagePlus
     * @param channel channel number
     * @throws IOException if unable to write
     */
    private void writeChannelMetadata(ZarrWriter writer, String datasetName,
                                     ImagePlus imp, int channel) throws IOException {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("channel_number", channel);
        metadata.put("channel_name", "Channel_" + channel);
        metadata.put("dimensions", new long[]{imp.getWidth(), imp.getHeight(), imp.getNSlices()});
        metadata.put("bit_depth", imp.getBitDepth());

        writer.setAttributes(datasetName, metadata);
    }

    /**
     * Notify progress callback
     * @param message progress message
     * @param progress progress value (0.0 to 1.0)
     */
    private void notifyProgress(String message, double progress) {
        if (progressCallback != null) {
            progressCallback.onProgress(message, progress);
        } else {
            System.out.println(String.format("[%3.0f%%] %s", progress * 100, message));
        }
    }

    /**
     * Convert multiple TIFF files to a single multi-channel Zarr
     * @param tiffPaths array of TIFF file paths (one per channel)
     * @param zarrPath output Zarr directory path
     * @param datasetName dataset name
     * @return true if successful
     */
    public boolean convertMultiFile(String[] tiffPaths, String zarrPath, String datasetName) {
        try {
            notifyProgress("Converting " + tiffPaths.length + " files to Zarr", 0.0);

            ZarrWriter writer = new ZarrWriter(zarrPath, compressionType, chunkSize);

            for (int i = 0; i < tiffPaths.length; i++) {
                notifyProgress("Processing file " + (i + 1) + "/" + tiffPaths.length,
                             (double) i / tiffPaths.length);

                ImagePlus imp = loadTiff(tiffPaths[i]);
                if (imp == null) {
                    System.err.println("Failed to load: " + tiffPaths[i]);
                    continue;
                }

                String channelDataset = datasetName + "/c" + (i + 1);
                writer.writeImagePlus(imp, channelDataset, chunkSize);

                if (preserveMetadata) {
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("source_file", new File(tiffPaths[i]).getName());
                    metadata.put("channel_number", i + 1);
                    writer.setAttributes(channelDataset, metadata);
                }

                imp.close();
            }

            writer.close();
            notifyProgress("Multi-file conversion complete", 1.0);
            return true;

        } catch (Exception e) {
            System.err.println("Error in multi-file conversion: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get recommended chunk size based on image dimensions
     * @param width image width
     * @param height image height
     * @param depth image depth
     * @param bitDepth bit depth
     * @param targetChunkMB target chunk size in MB
     * @return recommended chunk size [x, y, z]
     */
    public static int[] recommendChunkSize(int width, int height, int depth,
                                          int bitDepth, int targetChunkMB) {
        int bytesPerPixel = bitDepth / 8;
        long targetBytes = targetChunkMB * 1024L * 1024L;
        long targetVoxels = targetBytes / bytesPerPixel;

        // Start with square XY and adjust
        int chunkXY = (int) Math.sqrt(targetVoxels / 32); // Assume Z=32
        chunkXY = Math.max(256, Math.min(2048, chunkXY));
        chunkXY = Math.min(chunkXY, Math.min(width, height));

        int chunkZ = (int) (targetVoxels / (chunkXY * chunkXY));
        chunkZ = Math.max(16, Math.min(128, chunkZ));
        chunkZ = Math.min(chunkZ, depth);

        return new int[]{chunkXY, chunkXY, chunkZ};
    }

    /**
     * Estimate output Zarr size
     * @param imp ImagePlus
     * @param compressionRatio estimated compression ratio
     * @return estimated size in bytes
     */
    public static long estimateZarrSize(ImagePlus imp, double compressionRatio) {
        long voxels = (long) imp.getWidth() * imp.getHeight() * imp.getNSlices() *
                     imp.getNChannels() * imp.getNFrames();
        int bytesPerPixel = imp.getBitDepth() / 8;
        long uncompressedSize = voxels * bytesPerPixel;
        return (long) (uncompressedSize / compressionRatio);
    }

    /**
     * Main method for command-line usage
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: TiffToZarrConverter <input.tif> <output.zarr> [dataset_name]");
            System.out.println("   or: TiffToZarrConverter <input1.tif,input2.tif,...> <output.zarr> [dataset_name]");
            return;
        }

        String input = args[0];
        String output = args[1];
        String datasetName = args.length > 2 ? args[2] : "data";

        TiffToZarrConverter converter = new TiffToZarrConverter();
        converter.setCompressionType(CompressionType.GZIP);

        boolean success;
        if (input.contains(",")) {
            // Multiple files
            String[] files = input.split(",");
            success = converter.convertMultiFile(files, output, datasetName);
        } else {
            // Single file
            success = converter.convert(input, output, datasetName);
        }

        if (success) {
            System.out.println("Conversion successful: " + output);
        } else {
            System.err.println("Conversion failed");
            System.exit(1);
        }
    }
}
