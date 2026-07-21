package vtea.exploration.gallery;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.ZProjector;
import ij.process.ImageProcessor;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import vtea.deeplearning.data.CellRegionExtractor;
import vteaobjects.MicroObject;

/**
 * Processes MicroObject 3D regions into 2D maximum projection thumbnails
 * for display in gallery view.
 */
public class GalleryImageProcessor {

    /**
     * Extract and process a single cell region into a thumbnail.
     * @param cell The MicroObject to extract
     * @param imageStacks Multi-channel image data
     * @param regionSize Size of region to extract [width, height, depth]
     * @param channels Channels to include in composite (null for all)
     * @param thumbnailSize Output thumbnail dimension (square)
     * @return BufferedImage of maximum projection, or null if error
     */
    public static BufferedImage createThumbnail(
            MicroObject cell,
            ImageStack[] imageStacks,
            int[] regionSize,
            int[] channels,
            int thumbnailSize) {

        try {
            // Validate inputs
            if (cell == null || imageStacks == null || imageStacks.length == 0) {
                return null;
            }

            // Use all channels if not specified
            if (channels == null) {
                channels = new int[Math.min(3, imageStacks.length)];
                for (int i = 0; i < channels.length; i++) {
                    channels[i] = i;
                }
            }

            // Set default region size if not specified
            if (regionSize == null) {
                int depth = imageStacks[0].getSize();
                regionSize = new int[]{64, 64, depth};
            }

            // Extract 3D region for each channel
            ImageStack[] extractedRegions = CellRegionExtractor.extractRegion(
                    cell,
                    imageStacks,
                    regionSize,
                    channels,
                    CellRegionExtractor.PaddingType.REPLICATE
            );

            if (extractedRegions == null || extractedRegions.length == 0) {
                return null;
            }

            // Create composite if multiple channels, otherwise use single channel
            ImagePlus imp;
            if (extractedRegions.length == 1) {
                // Single channel - create grayscale
                imp = new ImagePlus("cell_" + cell.getSerialID(), extractedRegions[0]);
            } else {
                // Multiple channels - create RGB composite
                imp = createRGBComposite(extractedRegions, cell.getSerialID());
            }

            // Create maximum projection
            ImagePlus maxProj = makeMaxProjection(imp);

            if (maxProj == null) {
                return null;
            }

            // Convert to BufferedImage and scale
            BufferedImage thumbnail = toBufferedImage(maxProj, thumbnailSize);

            return thumbnail;

        } catch (Exception e) {
            System.err.println("Error creating thumbnail for cell " +
                    cell.getSerialID() + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create RGB composite from multiple channel stacks.
     * @param stacks Array of ImageStacks (up to 3 for RGB)
     * @param cellId Cell ID for title
     * @return ImagePlus with RGB composite
     */
    private static ImagePlus createRGBComposite(ImageStack[] stacks, int cellId) {
        if (stacks.length == 0) {
            return null;
        }

        int width = stacks[0].getWidth();
        int height = stacks[0].getHeight();
        int depth = stacks[0].getSize();

        // Create RGB stack
        ImageStack rgbStack = new ImageStack(width, height);

        for (int z = 1; z <= depth; z++) {
            // Create RGB image for this slice
            BufferedImage rgb = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int r = 0, g = 0, b = 0;

                    // Channel 0 -> Red
                    if (stacks.length > 0) {
                        r = (int) stacks[0].getProcessor(z).getPixelValue(x, y);
                    }

                    // Channel 1 -> Green
                    if (stacks.length > 1) {
                        g = (int) stacks[1].getProcessor(z).getPixelValue(x, y);
                    }

                    // Channel 2 -> Blue
                    if (stacks.length > 2) {
                        b = (int) stacks[2].getProcessor(z).getPixelValue(x, y);
                    }

                    // Clamp values to 0-255
                    r = Math.min(255, Math.max(0, r));
                    g = Math.min(255, Math.max(0, g));
                    b = Math.min(255, Math.max(0, b));

                    int pixel = (r << 16) | (g << 8) | b;
                    rgb.setRGB(x, y, pixel);
                }
            }

            // Convert BufferedImage to ImageProcessor
            ImagePlus tempImp = new ImagePlus("", rgb);
            rgbStack.addSlice(tempImp.getProcessor());
        }

        return new ImagePlus("cell_" + cellId, rgbStack);
    }

    /**
     * Create maximum projection from 3D stack.
     * @param stack 3D ImageStack
     * @return 2D maximum projection as ImagePlus
     */
    private static ImagePlus makeMaxProjection(ImagePlus imp) {
        if (imp == null || imp.getStackSize() <= 1) {
            return imp; // Already 2D or invalid
        }

        try {
            ZProjector projector = new ZProjector(imp);
            projector.setMethod(ZProjector.MAX_METHOD);
            projector.setStartSlice(1);
            projector.setStopSlice(imp.getStackSize());
            projector.doProjection();

            return projector.getProjection();
        } catch (Exception e) {
            System.err.println("Error creating max projection: " + e.getMessage());
            return null;
        }
    }

    /**
     * Convert ImagePlus to BufferedImage for Swing display.
     * @param imp ImagePlus to convert
     * @param targetSize Target thumbnail dimension (square)
     * @return BufferedImage scaled to targetSize x targetSize
     */
    private static BufferedImage toBufferedImage(ImagePlus imp, int targetSize) {
        if (imp == null) {
            return null;
        }

        try {
            ImageProcessor ip = imp.getProcessor();

            // Get original image
            BufferedImage original = ip.getBufferedImage();

            // Calculate scaling to fit in square while maintaining aspect ratio
            int width = original.getWidth();
            int height = original.getHeight();
            int maxDim = Math.max(width, height);

            double scale = (double) targetSize / maxDim;
            int scaledWidth = (int) (width * scale);
            int scaledHeight = (int) (height * scale);

            // Create scaled image
            BufferedImage scaled = new BufferedImage(
                    targetSize, targetSize, BufferedImage.TYPE_INT_RGB);

            Graphics2D g2 = scaled.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // Fill background with black
            g2.setColor(java.awt.Color.BLACK);
            g2.fillRect(0, 0, targetSize, targetSize);

            // Center the image
            int x = (targetSize - scaledWidth) / 2;
            int y = (targetSize - scaledHeight) / 2;

            // Draw scaled image
            Image scaledImage = original.getScaledInstance(
                    scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
            g2.drawImage(scaledImage, x, y, null);
            g2.dispose();

            return scaled;

        } catch (Exception e) {
            System.err.println("Error converting to BufferedImage: " + e.getMessage());
            return null;
        }
    }

    /**
     * Batch process multiple cells.
     * @param cells List of cells to process
     * @param imageStacks Image data
     * @param regionSize Region extraction size
     * @param channels Channels to composite
     * @param thumbnailSize Output thumbnail size
     * @return Map of cell -> thumbnail image
     */
    public static Map<MicroObject, BufferedImage> createThumbnailBatch(
            List<MicroObject> cells,
            ImageStack[] imageStacks,
            int[] regionSize,
            int[] channels,
            int thumbnailSize) {

        Map<MicroObject, BufferedImage> thumbnails = new HashMap<>();

        for (MicroObject cell : cells) {
            BufferedImage thumbnail = createThumbnail(
                    cell, imageStacks, regionSize, channels, thumbnailSize);

            if (thumbnail != null) {
                thumbnails.put(cell, thumbnail);
            }
        }

        return thumbnails;
    }
}
