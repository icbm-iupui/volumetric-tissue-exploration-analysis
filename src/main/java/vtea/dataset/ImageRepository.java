/* 
 * Copyright (C) 2020 Indiana University
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
package vtea.dataset;

import ij.ImagePlus;
import vtea.dataset.volume.VolumeDataset;
import vtea.dataset.volume.ImagePlusVolumeDataset;

/**
 * ImageRepository provides unified access to image data in VTEA.
 * Now supports both traditional ImagePlus and new VolumeDataset abstraction
 * for Zarr and chunked data support in VTEA 2.0.
 *
 * @author sethwinfree
 */
public abstract class ImageRepository implements Dataset {

    private static ImagePlus imp;
    private static VolumeDataset volumeDataset;

    /**
     * Get reference to legacy ImagePlus
     * @return ImagePlus reference
     * @deprecated Use getVolumeDataset() for better support of large datasets
     */
    @Deprecated
    public ImagePlus getReferenceToImage() {
        if (imp != null) {
            return imp;
        } else if (volumeDataset != null) {
            // Try to get ImagePlus from VolumeDataset
            return volumeDataset.getImagePlus();
        }
        return null;
    }

    /**
     * Set reference to ImagePlus (legacy method)
     * @param ip ImagePlus to set
     * @deprecated Use setVolumeDataset() for better support of large datasets
     */
    @Deprecated
    public void setReferenceToImage(ImagePlus ip) {
        imp = ip;
        if (ip != null) {
            volumeDataset = new ImagePlusVolumeDataset(ip);
        }
    }

    /**
     * Get a copy of the ImagePlus (legacy method)
     * Warning: May be memory intensive for large volumes
     * @return duplicated ImagePlus
     * @deprecated Use VolumeDataset methods for memory-efficient access
     */
    @Deprecated
    public ImagePlus getCopyOfImage() {
        if (imp != null) {
            return imp.duplicate();
        } else if (volumeDataset != null) {
            ImagePlus result = volumeDataset.getImagePlus();
            return result != null ? result.duplicate() : null;
        }
        return null;
    }

    /**
     * Get the VolumeDataset (preferred method in VTEA 2.0)
     * @return VolumeDataset instance
     */
    public VolumeDataset getVolumeDataset() {
        return volumeDataset;
    }

    /**
     * Set the VolumeDataset (preferred method in VTEA 2.0)
     * @param dataset VolumeDataset to set
     */
    public void setVolumeDataset(VolumeDataset dataset) {
        volumeDataset = dataset;
        // Update legacy ImagePlus reference if possible and memory permits
        if (dataset != null && dataset.fitsInMemory()) {
            imp = dataset.getImagePlus();
        } else {
            imp = null; // Too large for in-memory representation
        }
    }

    /**
     * Check if dataset is chunked/partitioned
     * @return true if chunked
     */
    public boolean isChunked() {
        return volumeDataset != null && volumeDataset.isChunked();
    }

    /**
     * Check if dataset fits in memory
     * @return true if fits in RAM
     */
    public boolean fitsInMemory() {
        if (volumeDataset != null) {
            return volumeDataset.fitsInMemory();
        }
        return imp != null; // If we have ImagePlus, it's already in memory
    }

    /**
     * Get dimensions [width, height, depth, channels, timepoints]
     * @return dimensions array
     */
    public long[] getDimensions() {
        if (volumeDataset != null) {
            return volumeDataset.getDimensions();
        } else if (imp != null) {
            return new long[]{
                imp.getWidth(),
                imp.getHeight(),
                imp.getNSlices(),
                imp.getNChannels(),
                imp.getNFrames()
            };
        }
        return null;
    }

    /**
     * Get the source path or identifier
     * @return source string
     */
    public String getSource() {
        if (volumeDataset != null) {
            return volumeDataset.getSource();
        } else if (imp != null) {
            return imp.getTitle();
        }
        return "Unknown";
    }

}
