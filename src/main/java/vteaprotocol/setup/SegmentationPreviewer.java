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
package vteaprotocol.setup;

import vteaprotocol.MicroExperiment;
import vteaprotocol.SingleImageProcessing;
import static vteaprotocol.SingleImageProcessing.OBJECTBLOCKS;
import static vteaprotocol.SingleImageProcessing.ProgressComment;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ImageRoi;
import ij.gui.Roi;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import vteaobjects.MicroObject;
import vteaobjects.layercake.microVolume;

/**
 *
 * @author sethwinfree
 */

public class SegmentationPreviewer  {
    
    
SegmentationPreviewer(){   

}

static public void SegmentationPreviewFactory(ImagePlus imp, ArrayList al){
        if(checkImage(imp)){
        Roi r = imp.getRoi();
        MicroExperiment me = new MicroExperiment(); 
        ImagePlus segmentationPreview = r.getImage().duplicate(); 
        imp.deleteRoi();
        
        ArrayList<ArrayList> protocols = new ArrayList();
        
        protocols.add(al);
 
        me.start(segmentationPreview, protocols, false);
        
        makeImage(segmentationPreview, me);
    }
}

static private boolean checkImage(ImagePlus imp){
    try{
        Roi r = imp.getRoi();
       ImagePlus segmentationPreview = r.getImage().duplicate();
        }catch(NullPointerException e){
            JFrame frame = new JFrame();
            frame.setBackground(vtea.VTEA.BUTTONBACKGROUND);
            JOptionPane.showMessageDialog(frame,
            "Please select a region of the threshold preview image \nto preview the segmentation.",
            "Roi required.",
            JOptionPane.WARNING_MESSAGE);
            return false;
        }
    return true; 
}

static private void makeImage(ImagePlus imp, MicroExperiment me){
    
    
    ImagePlus resultImage = IJ.createImage("Segmentation Preview", "8-bit black", imp.getWidth(), imp.getHeight(), imp.getNSlices()); 
    
    ImageStack resultStack = resultImage.getStack();
    
    ArrayList<microVolume> volumes = me.getFolderVolumes(0);
    
    ListIterator<microVolume> citr = volumes.listIterator();
    
    int value = 1;
    while(citr.hasNext()){
                            MicroObject vol = (MicroObject) citr.next();
                            vol.setColor(value);
                            value++;
                            if(value > 255){value = 1;}
    }

        for (int i = 0; i <= imp.getNSlices(); i++) {
                 //System.out.println("PROFILING: generating segmentation image, slice "+ i);
                 ListIterator<microVolume> itr = volumes.listIterator();
                while(itr.hasNext()){
                        try {
                            MicroObject vol = (MicroObject) itr.next();
                            
                          

                            int[] x_pixels = vol.getXPixelsInRegion(i);
                            int[] y_pixels = vol.getYPixelsInRegion(i);

                            for (int c = 0; c < x_pixels.length; c++) {

                                resultStack.setVoxel(x_pixels[c], y_pixels[c], i, vol.getColor());
                                
                            }
 

                        } catch (NullPointerException e) {
                        }
                }
        }
    IJ.run(resultImage, "3-3-2 RGB", "");
    resultImage.show();
    
  }
    
    
}
    