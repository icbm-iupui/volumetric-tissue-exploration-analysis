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
package vtea.protocol;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;

/**
 *
 * @author vinfrais
 */
public class UtilityMethods {

    static public ImagePlus makeThumbnail(ImagePlus imp) {

        if (imp.getNSlices() > 1) {
            imp.setPosition(imp.getStackSize() / 2);
        }
        try {
            CompositeImage compImp = new CompositeImage(imp, IJ.COMPOSITE);
            if (compImp.getNSlices() > 1) {
                compImp.setPosition(imp.getStackSize() / 2);
            }
            return compImp;

        } catch (IllegalArgumentException i) {
            System.out.println("ERROR.  Composite conversion failure.");
            return imp;
        }
    }

    static public long getImagePlusSize(ImagePlus imp) {

        long size = imp.getBitDepth() * imp.getWidth() * imp.getHeight() * imp.getNChannels() * imp.getNSlices();

        return size;
    }

    static public double lengthCart(double[] position, double[] reference_pt) {
        double distance;
        double part0 = position[0] - reference_pt[0];
        double part1 = position[1] - reference_pt[1];
        distance = Math.sqrt((part0 * part0) + (part1 * part1));
        return distance;
    }

    static public double[] getChannelDisplayRange(ImagePlus imp, int channel) {

        double[] range = new double[2];
        double max = 0;
        double min = Math.pow(2, imp.getBitDepth()) - 1;
        imp.setC(channel);

        if (imp.getNSlices() == 1) {
            range[0] = imp.getProcessor().getMin();
            range[1] = imp.getProcessor().getMax();
            System.out.println("PROFILING: range determination for channel " + channel + ": " + range[0] + " to " + range[1]);
            return range;
        } else {
            ImageStack is = imp.getImageStack();
            for (int i = 0; i < is.getSize(); i++) {
                for (int x = 0; x < is.getWidth(); x++) {
                    for (int y = 0; y < is.getHeight(); y++) {
                        if (is.getVoxel(x, y, i) > max) {
                            max = is.getVoxel(x, y, i);
                        } else if (is.getVoxel(x, y, i) < min && is.getVoxel(x, y, i) < max) {
                            min = is.getVoxel(x, y, i);
                        }
                    }
                }
            }
            range[0] = min;
            range[1] = max;
            System.out.println("PROFILING: range determination for channel " + channel + ": " + range[0] + " to " + range[1]);
            return range;

        }
    }
}
