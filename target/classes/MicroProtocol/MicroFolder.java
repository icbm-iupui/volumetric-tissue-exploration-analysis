 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MicroProtocol;

import ij.IJ;
import vteaobjects.layercake.SingleThresholdDataModel;
import vteaobjects.layercake.microVolume;
import ij.ImagePlus;
import ij.ImageStack;
import java.util.ArrayList;
import vteaobjects.MicroObject;

/**
 *
 * @author vinfrais
 */
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
    
    private Thread t;
    private String threadName = "microFolder_" + System.nanoTime();

    // 0: minObjectSize, 1: maxObjectSize, 2: minOverlap, 3: minThreshold
    MicroFolder(ImagePlus imp, ArrayList details) {
        protocol = details;
        imagedata = getInterleavedStacks(imp);
       
        
    }
    
    

    public void process() {

        ArrayList mask;
        mask =  (ArrayList)protocol.get(0);

        switch ((Integer) mask.get(1)) {
            case 1:
                stdm = new SingleThresholdDataModel(imagedata, protocol);
                volumes = stdm.getObjects();
                volumes3D = stdm.getObjects3D();
                System.out.println("PROFILING: Getting " + volumes3D.size() + " 3D flood fill volumes.");
                break;
            default: ;
                break;
        }
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

        //microVolume.Analytics;
        protocol.size();
        for (int i = 0; i < protocol.size(); i++) {
            for (int c = 0; c < MicroObject.Analytics.length; c++) {
                String derived = new String();
                String text = new String();
                if (i == 0) {
                    derived = " ";
                } else {
                    derived = "_d" + ((ArrayList) protocol.get(i)).get(1) + " ";
                }
                text = "Ch" + ((Integer)((ArrayList) protocol.get(i)).get(0)+1) + derived + MicroObject.Analytics[c];
                al.add(text);
            }
        }
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
