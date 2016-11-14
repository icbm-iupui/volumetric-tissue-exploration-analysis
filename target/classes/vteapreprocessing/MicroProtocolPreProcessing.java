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
package vteapreprocessing;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.plugin.ChannelSplitter;
import ij.plugin.Duplicator;
import ij.plugin.RGBStackMerge;
import ij.plugin.filter.BackgroundSubtracter;
import java.util.ArrayList;
import java.util.ListIterator;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 *
 * @author vinfrais
 */
public class MicroProtocolPreProcessing extends java.lang.Object {

    ImagePlus impOriginal;
    ImagePlus impProcessed;
    ImagePlus impPreview;
    ArrayList protocol;
    int channelProcess; //-1. 0, 1 etc.  -1 for all.
    
    /*ImageProcessing steps are kept as fields in an ArrayList 
    These fields are arraylists that include:
    0:Name 1: Channel to operate on 2... Components
   */

    public MicroProtocolPreProcessing(ImagePlus imp, ArrayList protocol) {

        
        
        impOriginal = imp;
        this.protocol = protocol;
        //channelProcess = channel;
    
    }
    
    private void makePreviewImage() {
        
        impOriginal.resetStack();
        impOriginal.setRoi(new Roi(0,0,255,255));
        if(impOriginal.getWidth() < 255 || impOriginal.getHeight() < 255){
            impOriginal.setRoi(new Roi(0,0,impOriginal.getWidth(),impOriginal.getHeight()));
        }
        impPreview = new Duplicator().run(impOriginal);
        impPreview.hide();
        impOriginal.deleteRoi();
        
    }

    public ImagePlus ProcessImage() {
        impProcessed = impOriginal.duplicate();
        ListIterator<Object> litr = this.protocol.listIterator();
        while (litr.hasNext()) {
            ProcessManager((ArrayList) litr.next(), impProcessed);
        }
        return impProcessed;
    }
    
    public ImagePlus ProcessPreviewImage() {
        makePreviewImage();
        ListIterator<Object> litr = this.protocol.listIterator();
        while (litr.hasNext()) {
            ProcessManager((ArrayList) litr.next(), impPreview);
        }
       
        return impPreview;
    }
    
    private void ProcessManager(ArrayList protocol, ImagePlus imp) {
        
        int testcase = 0;
        
        //System.out.println(steps);
        
        //in extensibility model, TC would hold load and populate an array of processing approaches
        
        if(protocol.get(0).toString().equals("Background Subtraction")) {testcase = 0;};
        if(protocol.get(0).toString().equals("Enhance Contrast")) {testcase = 1;};
        if(protocol.get(0).toString().equals("Reduce Noise")) {testcase = 2;};  
        
       ChannelSplitter cs = new ChannelSplitter();
       RGBStackMerge rsm = new RGBStackMerge();
       
       ImagePlus temp_imp = new ImagePlus("Ch_" + (Integer)protocol.get(1) + "_modified", cs.getChannel(imp, (Integer)protocol.get(1)+1)); 
       ImagePlus[] merged = new ImagePlus[imp.getNChannels()];
       for(int i = 0; i < merged.length; i++){
           merged[i] = new ImagePlus("Ch_" + i, cs.getChannel(imp, (Integer)protocol.get(1)+1));
       }
       merged[(Integer)protocol.get(1)] = temp_imp;
       
        switch (testcase) {
            case 0:
                SubtractBackground(temp_imp,protocol);
                //System.out.println("PROFILING: Running Background Subtraction...");
                imp = rsm.mergeHyperstacks(merged, false);
                break;
            case 1:
                EnhanceContrast(temp_imp,protocol);
               //System.out.println("PROFILING: Running Enhance Contrast...");
               imp = rsm.mergeHyperstacks(merged, false);
                break;
            case 2:
            //System.out.println("PROFILING: Denoising with median filter...");
                DeNoise(temp_imp,protocol);
                break;
            default: ;
                break;
        }
    }

    private void SubtractBackground(ImagePlus imp, ArrayList variables) {

        JTextField radius;
        int channel;
        JRadioButton slidingparabaloid, stack;

        channel = (Integer)variables.get(1);
        radius = (JTextField) variables.get(3);
        
        BackgroundSubtracter rbb = new BackgroundSubtracter();
         ChannelSplitter cs = new ChannelSplitter();
        
        ImageStack is;
        
        is = cs.getChannel(imp, channel+1);
        
        for(int n = 1; n <= is.getSize(); n++){
        rbb.rollingBallBackground(is.getProcessor(n), Integer.parseInt(radius.getText()), false, false, false, true, true);
        //System.out.println("PROFILING: Running Background Subtraction... slice " + n + ", radius: " + radius.getText());
        }
        //ImagePlus result = new ImagePlus("processed", is);
        //result.show();
    }
    
        private void EnhanceContrast(ImagePlus imp, ArrayList variables) {

        JTextField fractionsaturated;
        JRadioButton normalize, stack, equalize, stackhistogram;

        fractionsaturated = (JTextField) variables.get(3);
        normalize = (JRadioButton) variables.get(4);
        equalize = (JRadioButton) variables.get(5);
        stack = (JRadioButton) variables.get(6);
        stackhistogram = (JRadioButton) variables.get(7);

        String norm, equal, stackall, stackhisto;

        if (normalize.isSelected()) {
            norm = "normalize";
        } else {
            norm = "";
        }
        if (equalize.isSelected()) {
            equal = "equalize";
        } else {
            equal = "";
        }
        if (stack.isSelected()) {
            stackall = "process_all";
        } else {
            stackall = "";
        }
        if (stackhistogram.isSelected()) {
            stackhisto = "use";
        } else {
            stackhisto = "";
        }
       // System.out.println("PROFILING: Enhance Contrast" + "saturated=" + fractionsaturated.getText() + " " + norm + " " + equal + " " + stackall + " " + stackhisto);
        //IJ.run(imp, "Enhance Contrast...", "saturated=" + fractionsaturated.getText() + " " + norm + " " + equal + " " + stackall + " " + stackhisto);
    }

    private void DeNoise(ImagePlus imp, ArrayList variables) {
        int channel = (Integer)variables.get(1);
        JTextField radius = (JTextField) variables.get(3);
        //radius = (JTextField)variables.get(2);
        
        ChannelSplitter cs = new ChannelSplitter();
        
        ImageStack is;
        
        is = cs.getChannel(imp, channel+1);
        
        for(int n = 1; n <= is.getSize(); n++){
            IJ.run(imp, "Median...", "radius="+radius.getText()+" stack");
        }
        //System.out.println("PROFILING: DeNoise, Median..." + "radius=" + radius.getText());
        //IJ.run(imp, "Median...", "radius="+radius+" stack");
    }

    public ImagePlus getResult() {
        return this.impProcessed;
    }

    public ImagePlus getPreview() {
        return this.impOriginal;
    }
    
    public ArrayList getSteps() {
        return this.protocol;
    }

}
