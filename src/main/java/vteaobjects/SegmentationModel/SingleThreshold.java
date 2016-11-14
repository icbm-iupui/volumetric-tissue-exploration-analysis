/* 
 * Copyright (C) 2016 Indiana University
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
package vteaobjects.SegmentationModel;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;

/**
 *
 * @author winfrees
 */
public class SingleThreshold implements Segmentation {
    
    private int[] minConstants; // 0: minObjectSize, 1: maxObjectSize, 2: minOverlap, 3: minThreshold
    private  ImagePlus imageOriginal;
    private  ImagePlus imageResult;
    private  ImageStack stackOriginal;
    protected  ImageStack stackResult;
    private boolean watershedImageJ = true;

    
public SingleThreshold(ImageStack stack, int[] min, boolean imageOptimize) {
     
         
        minConstants = min;
        stackOriginal = stack;
        imageOriginal = new ImagePlus("Mask", stack);
        stackResult = stackOriginal.duplicate();

        for (int n = 0; n <= stackResult.getSize(); n++) {
            for(int x = 0; x < stackResult.getWidth(); x++){
                for(int y = 0; y < stackResult.getHeight(); y++){
                    if(stackResult.getVoxel(x, y, n) <= minConstants[3]){
                        stackResult.setVoxel(x, y, n, (Math.pow(2,stack.getBitDepth()))-1);   
                    }else{
                        stackResult.setVoxel(x, y, n, 0);
                    }  
                }
            }                        
        } 
        imageResult = new ImagePlus("Mask Result", stackResult);
        IJ.run(imageResult, "8-bit", ""); 
        if(watershedImageJ){IJ.run(imageResult, "Watershed", "stack");}
        IJ.run(imageResult, "Invert", "stack");
        
         
         imageResult.show();
         
         
     
     imageResult.setTitle("3D Floodfill Result");
     }

    @Override
    public ImagePlus getSegmentation() {
        return this.imageResult;
    }
}
