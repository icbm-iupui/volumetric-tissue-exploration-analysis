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

/**
 * Interface for in-memory volume datasets.
 * Represents traditional ImagePlus-based datasets that fit entirely in RAM.
 *
 * @author sethwinfree
 */
public interface InMemoryVolumeDataset extends VolumeDataset {

    /**
     * Get the underlying ImagePlus object
     * @return ImagePlus instance
     */
    ImagePlus getImagePlusReference();

    /**
     * Set the underlying ImagePlus object
     * @param imp ImagePlus to set
     */
    void setImagePlus(ImagePlus imp);

    /**
     * Get a duplicate of the ImagePlus
     * @return duplicated ImagePlus
     */
    ImagePlus duplicateImagePlus();

    /**
     * Get the ImageStack for a specific channel
     * @param channel channel index (1-based)
     * @return ImageStack for the channel
     */
    ImageStack getStack(int channel);

    /**
     * Check if the dataset has multiple channels
     * @return true if multi-channel
     */
    boolean isMultiChannel();

    /**
     * Check if the dataset has multiple timepoints
     * @return true if time series
     */
    boolean isTimeSeries();

    /**
     * Get the current channel
     * @return current channel index (1-based)
     */
    int getCurrentChannel();

    /**
     * Set the current channel
     * @param channel channel to set (1-based)
     */
    void setCurrentChannel(int channel);

    /**
     * Get the current slice (Z position)
     * @return current slice index (1-based)
     */
    int getCurrentSlice();

    /**
     * Set the current slice
     * @param slice slice to set (1-based)
     */
    void setCurrentSlice(int slice);

    /**
     * Get the current timepoint
     * @return current frame index (1-based)
     */
    int getCurrentFrame();

    /**
     * Set the current timepoint
     * @param frame frame to set (1-based)
     */
    void setCurrentFrame(int frame);
}
