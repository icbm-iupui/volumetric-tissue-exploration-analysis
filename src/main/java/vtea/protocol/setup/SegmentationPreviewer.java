/* 
 * Copyright (C) 2016-2018 Indiana University
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
package vtea.protocol.setup;

import vtea.protocol.MicroExperiment;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.Roi;
import ij.plugin.Duplicator;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.ListIterator;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import vteaobjects.MicroObject;
import vtea.objects.layercake.microVolume;
import vtea.processor.SegmentationProcessor;
import vtea.processor.listeners.ProgressListener;

/**
 *
 * @author sethwinfree
 */

public class SegmentationPreviewer implements Runnable, PropertyChangeListener{
   
    
ArrayList protocol;
ImagePlus Image;
ImagePlus segmentationPreview;

SegmentationProcessor sp;

boolean Preview = false; //or "Done"

    
SegmentationPreviewer(ImagePlus imp, ArrayList al){   
        Image = imp;     
        protocol = al;    
}

public void SegmentationPreviewFactory(){
        if(checkImage(Image)){   
        Duplicator dup = new Duplicator();
        segmentationPreview = dup.run(Image); 
        sp = new SegmentationProcessor("Preview", segmentationPreview, protocol);
        sp.addPropertyChangeListener(this);
        sp.execute(); 
    }
}

public static void SegmentationFactory(ImagePlus imp, ArrayList<MicroObject> objects){
    makeImage(imp, objects);  
}

static private boolean checkImage(ImagePlus imp){
    try{
        Roi r = imp.getRoi();
       ImagePlus test = r.getImage().duplicate();
        }catch(NullPointerException e){
            JFrame frame = new JFrame();
            frame.setBackground(vtea._vtea.BUTTONBACKGROUND);
            JOptionPane.showMessageDialog(frame,
            "Please select a region of the threshold preview image \nto preview the segmentation.",
            "Roi required.",
            JOptionPane.WARNING_MESSAGE);
            return false;
        }
    return true; 
}

static private void makeImage(ImagePlus imp, ArrayList<MicroObject> objects){

    ImagePlus resultImage = IJ.createImage("Segmentation", "8-bit black", imp.getWidth(), imp.getHeight(), imp.getNSlices());    
    ImageStack resultStack = resultImage.getStack();   
    int value = 1;   
    ListIterator<MicroObject> citr = objects.listIterator();
   
    while(citr.hasNext()){
                            MicroObject vol = (MicroObject) citr.next();
                            vol.setColor(value);
                            value++;
                            if(value > 255){value = 1;}
    }

        for (int i = 0; i <= imp.getNSlices(); i++) {
                ListIterator<MicroObject> itr = objects.listIterator();
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

    @Override
    public void run() {
        Preview = false;
        SegmentationPreviewFactory();
    }


    @Override
    public void propertyChange(PropertyChangeEvent evt) { 
       if (evt.getPropertyName().equals("segmentationDone")) {
           makeImage(segmentationPreview, sp.getObjects());
        } 
    }
    
}
    