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
import ij.process.ImageProcessor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of InMemoryVolumeDataset that wraps ImagePlus for backward compatibility.
 * Provides the VolumeDataset interface for traditional ImageJ ImagePlus objects.
 *
 * @author sethwinfree
 */
public class ImagePlusVolumeDataset implements InMemoryVolumeDataset {

    private ImagePlus imp;
    private final Map<String, String> metadata;

    /**
     * Constructor with ImagePlus
     * @param imp ImagePlus to wrap
     */
    public ImagePlusVolumeDataset(ImagePlus imp) {
        if (imp == null) {
            throw new IllegalArgumentException("ImagePlus cannot be null");
        }
        this.imp = imp;
        this.metadata = new HashMap<>();
    }

    @Override
    public long[] getDimensions() {
        return new long[]{
            imp.getWidth(),
            imp.getHeight(),
            imp.getNSlices(),
            imp.getNChannels(),
            imp.getNFrames()
        };
    }

    @Override
    public long getWidth() {
        return imp.getWidth();
    }

    @Override
    public long getHeight() {
        return imp.getHeight();
    }

    @Override
    public long getDepth() {
        return imp.getNSlices();
    }

    @Override
    public int getNumChannels() {
        return imp.getNChannels();
    }

    @Override
    public int getNumTimepoints() {
        return imp.getNFrames();
    }

    @Override
    public int getBitDepth() {
        return imp.getBitDepth();
    }

    @Override
    public boolean isChunked() {
        return false;  // ImagePlus is always in-memory
    }

    @Override
    public int[] getChunkDimensions() {
        return null;  // Not chunked
    }

    @Override
    public double getVoxel(long x, long y, long z, int channel) {
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight() || z < 0 || z >= getDepth()) {
            return 0.0;
        }

        int oldChannel = imp.getChannel();
        int oldSlice = imp.getSlice();

        imp.setPositionWithoutUpdate(channel + 1, (int) z + 1, imp.getFrame());
        double value = imp.getStack().getVoxel((int) x, (int) y, (int) z);

        imp.setPositionWithoutUpdate(oldChannel, oldSlice, imp.getFrame());

        return value;
    }

    @Override
    public ImageStack getSubVolume(long xStart, long yStart, long zStart,
                                   long width, long height, long depth, int channel) {
        int oldChannel = imp.getChannel();
        imp.setC(channel + 1);

        ImageStack subStack = new ImageStack((int) width, (int) height);

        for (int z = 0; z < depth; z++) {
            int sliceNum = (int) (zStart + z + 1);
            if (sliceNum > 0 && sliceNum <= imp.getNSlices()) {
                imp.setSliceWithoutUpdate(sliceNum);
                ImageProcessor ip = imp.getProcessor().duplicate();
                ip.setRoi((int) xStart, (int) yStart, (int) width, (int) height);
                ImageProcessor cropped = ip.crop();
                subStack.addSlice(cropped);
            }
        }

        imp.setC(oldChannel);
        return subStack;
    }

    @Override
    public ImagePlus getImagePlus() {
        return imp;
    }

    @Override
    public ImagePlus getImagePlus(int channel) {
        if (imp.getNChannels() == 1) {
            return imp.duplicate();
        }

        ImagePlus channelImp = new ImagePlus();
        ImageStack channelStack = new ImageStack(imp.getWidth(), imp.getHeight());

        int oldChannel = imp.getChannel();
        imp.setC(channel + 1);

        for (int z = 1; z <= imp.getNSlices(); z++) {
            imp.setSliceWithoutUpdate(z);
            ImageProcessor ip = imp.getProcessor().duplicate();
            channelStack.addSlice(ip);
        }

        imp.setC(oldChannel);

        channelImp.setStack(channelStack);
        channelImp.setDimensions(1, imp.getNSlices(), imp.getNFrames());
        return channelImp;
    }

    @Override
    public <T extends RealType<T> & NativeType<T>> RandomAccessibleInterval<T> getImgLib2() {
        return ImageJFunctions.wrapReal(imp);
    }

    @Override
    public <T extends RealType<T> & NativeType<T>> RandomAccessibleInterval<T> getImgLib2(int channel) {
        ImagePlus channelImp = getImagePlus(channel);
        return ImageJFunctions.wrapReal(channelImp);
    }

    @Override
    public String getSource() {
        String path = imp.getOriginalFileInfo() != null ?
                     imp.getOriginalFileInfo().directory + imp.getOriginalFileInfo().fileName :
                     imp.getTitle();
        return path != null ? path : "Unknown";
    }

    @Override
    public long getEstimatedMemorySize() {
        long pixelCount = (long) imp.getWidth() * imp.getHeight() *
                         imp.getNSlices() * imp.getNChannels() * imp.getNFrames();
        int bytesPerPixel = imp.getBitDepth() / 8;
        return pixelCount * bytesPerPixel;
    }

    @Override
    public boolean fitsInMemory() {
        // Already in memory
        return true;
    }

    @Override
    public String getMetadata(String key) {
        // First check custom metadata
        if (metadata.containsKey(key)) {
            return metadata.get(key);
        }

        // Try ImagePlus properties
        Object prop = imp.getProperty(key);
        return prop != null ? prop.toString() : null;
    }

    @Override
    public void setMetadata(String key, String value) {
        metadata.put(key, value);
        imp.setProperty(key, value);
    }

    @Override
    public String getDataType() {
        switch (imp.getBitDepth()) {
            case 8:
                return "uint8";
            case 16:
                return "uint16";
            case 32:
                return "float32";
            default:
                return "unknown";
        }
    }

    @Override
    public void close() {
        // ImagePlus doesn't require explicit closing
        // But we can clear large data structures
        if (imp != null) {
            imp.flush();
        }
    }

    // InMemoryVolumeDataset specific methods

    @Override
    public ImagePlus getImagePlusReference() {
        return imp;
    }

    @Override
    public void setImagePlus(ImagePlus imp) {
        if (imp == null) {
            throw new IllegalArgumentException("ImagePlus cannot be null");
        }
        this.imp = imp;
    }

    @Override
    public ImagePlus duplicateImagePlus() {
        return imp.duplicate();
    }

    @Override
    public ImageStack getStack(int channel) {
        if (imp.getNChannels() == 1) {
            return imp.getStack();
        }

        ImageStack channelStack = new ImageStack(imp.getWidth(), imp.getHeight());
        int oldChannel = imp.getChannel();
        imp.setC(channel);

        for (int z = 1; z <= imp.getNSlices(); z++) {
            imp.setSliceWithoutUpdate(z);
            ImageProcessor ip = imp.getProcessor().duplicate();
            channelStack.addSlice(ip);
        }

        imp.setC(oldChannel);
        return channelStack;
    }

    @Override
    public boolean isMultiChannel() {
        return imp.getNChannels() > 1;
    }

    @Override
    public boolean isTimeSeries() {
        return imp.getNFrames() > 1;
    }

    @Override
    public int getCurrentChannel() {
        return imp.getChannel();
    }

    @Override
    public void setCurrentChannel(int channel) {
        if (channel >= 1 && channel <= imp.getNChannels()) {
            imp.setC(channel);
        }
    }

    @Override
    public int getCurrentSlice() {
        return imp.getSlice();
    }

    @Override
    public void setCurrentSlice(int slice) {
        if (slice >= 1 && slice <= imp.getNSlices()) {
            imp.setSlice(slice);
        }
    }

    @Override
    public int getCurrentFrame() {
        return imp.getFrame();
    }

    @Override
    public void setCurrentFrame(int frame) {
        if (frame >= 1 && frame <= imp.getNFrames()) {
            imp.setT(frame);
        }
    }

    @Override
    public String toString() {
        return String.format("ImagePlusVolumeDataset[%s, %dx%dx%d, %d channels, %d frames, %d-bit]",
                imp.getTitle(),
                imp.getWidth(), imp.getHeight(), imp.getNSlices(),
                imp.getNChannels(), imp.getNFrames(), imp.getBitDepth());
    }
}
