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
package vtea.protocol;

import ij.IJ;
import vtea.objects.layercake.SingleThresholdDataModel;
import vtea.objects.layercake.microVolume;
import ij.ImagePlus;
import ij.ImageStack;
import java.util.ArrayList;
import vteaobjects.MicroObject;

/**
 *
 * @author vinfrais
 */

@Deprecated
public class MicroFolder extends java.lang.Object implements Runnable {

    //class to organize the microbuilder classes
    /**
     * Details content breakdown
     *
     * [0] primary channel; method array list for primary channel segmentation
     * [1] secondary channel; method array list [2] secondary channel; method
     * array list [3] etc...
     *
     * method arraylist [0] channel [1] key for method [2] field key [3] field1
     * [4] field2 [5] etc...
     *
     * @param details
     */
    private ArrayList<ArrayList> protocol;
    private ArrayList volumes;
    private ArrayList volumes3D;
    private ImageStack[] imagedata;
    private SingleThresholdDataModel stdm;
    private boolean calculate;
    
    private boolean imageChanged = false;
    private boolean protocolChanged = false;
    
    private Thread t;
    private String threadName = "microFolder_" + System.nanoTime();

    // 0: minObjectSize, 1: maxObjectSize, 2: minOverlap, 3: minThreshold
    MicroFolder(ImagePlus imp, ArrayList al, boolean calculate) {
        protocol = al;
        imagedata = getInterleavedStacks(imp); 
        this.calculate = calculate;
        imageChanged = true;
        protocolChanged = true;
    }
    
    public void setProcessedFlags(boolean b){
        imageChanged = false;
        protocolChanged = false;
    }
    
    
    
    //rewrite this to decided on class by mask(1)

    public void process() {

        ArrayList mask;
        mask =  (ArrayList)protocol.get(0);

        if(mask.get(1).equals("LayerCake 3D")){
                stdm = new SingleThresholdDataModel();
                stdm.processDataLayerCake(imagedata, protocol, calculate);
                volumes = stdm.getObjects();
//                System.out.println("PROFILING: Getting " + volumes.size() + " 3D layercake volumes.");
                setProcessedFlags(false);
        }else if(mask.get(1).equals("FloodFill 3D")){
                stdm = new SingleThresholdDataModel();
                stdm.processData3DFloodFill(imagedata, protocol, calculate);
                volumes = stdm.getObjects();
//                System.out.println("PROFILING: Getting " + volumes.size() + " 3D flood fill volumes.");
                setProcessedFlags(false);
        }
        
    }  
    
    public void setNewImageData(ImagePlus imp) {
       imagedata = getInterleavedStacks(imp); 
       imageChanged = true;
    }
    
    public void setNewProtocol(ArrayList al) {
        protocol.clear();
        protocol.addAll(al);
        protocolChanged = true;
    }
    
    public boolean getProtocolUpdate(){
        return protocolChanged;
    }
    
    public boolean getImageUpdate(){
        return imageChanged;
    }

    public ArrayList getVolumes() {
        return volumes;
    }   
    
    public ArrayList getVolumes3D() {
        return volumes3D;
    }

    public ArrayList getAvailableData() {

        ArrayList al = new ArrayList();

        //microVolume.Analytics;
        protocol.size();
        for (int i = 0; i < protocol.size(); i++) {
            for (int c = 0; c < microVolume.Analytics.length; c++) {
                String derived = new String();
                String text = new String();
                if (i == 0) {
                    derived = " ";
                } else {
                    derived = "_d" + ((ArrayList) protocol.get(i)).get(1) + " ";
                }
                text = "Ch" + ((Integer)((ArrayList) protocol.get(i)).get(0)+1) + derived + microVolume.Analytics[c];
                al.add(text);
            }
        }
        return al;
    }
    
        public ArrayList getAvailableData3D() {

        ArrayList al = new ArrayList();

//        //microVolume.Analytics;
//        protocol.size();
//        for (int i = 0; i < protocol.size(); i++) {
//            for (int c = 0; c < MicroObject.Analytics.length; c++) {
//                String derived = new String();
//                String text = new String();
//                if (i == 0) {
//                    derived = " ";
//                } else {
//                    derived = "_d" + ((ArrayList) protocol.get(i)).get(1) + " ";
//                }
//                text = "Ch" + ((Integer)((ArrayList) protocol.get(i)).get(0)+1) + derived + MicroObject.Analytics[c];
//                al.add(text);
//            }
//        }
        return al;
    }

    public static ImageStack[] getInterleavedStacks(ImagePlus imp) {
        ImageStack[] stacks = new ImageStack[imp.getNChannels()];
        ImageStack stack = imp.getImageStack();
        for (int m = 0; m <= imp.getNChannels() - 1; m++) {
            stacks[m] = new ImageStack(imp.getWidth(), imp.getHeight());
            for (int n = m; n <= imp.getStackSize() - 1; n += imp.getNChannels()) {
                stacks[m].addSlice(stack.getProcessor(n + 1));
            }
        }	
        return stacks;
    }

    @Override
    public void run() {
        process();
    }
    
    public void start(){
 

                     t = new Thread (this, threadName);
                     t.start ();
            try {
                t.join();
            } catch (InterruptedException ex) {     
            }
            System.out.println("PROFILING: Exiting MicroExperiment thread: " + threadName); 
            IJ.log("PROFILING: Exiting MicroExperiment thread: " + threadName); 
        }
}
