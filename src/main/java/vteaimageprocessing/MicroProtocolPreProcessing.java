/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vteaimageprocessing;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.ChannelSplitter;
import ij.plugin.Duplicator;
import ij.plugin.RGBStackMerge;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import static vtea._vtea.PROCESSINGMAP;

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
        
        impOriginal.setZ(impOriginal.getNSlices()/2);
        impOriginal.setRoi(new Roi(0,0,255,255));
        if(impOriginal.getWidth() < 255 || impOriginal.getHeight() < 255){
            impOriginal.setRoi(new Roi(0,0,impOriginal.getWidth(),impOriginal.getHeight()));
        }
        impPreview = new Duplicator().run(impOriginal);
        impPreview.hide();
        impOriginal.deleteRoi();
        
    }

    public ImagePlus ProcessImage() {
        impProcessed = new Duplicator().run(impOriginal);
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
        impPreview.resetDisplayRange();
        return impPreview;
    }
    
    private void ProcessManager(ArrayList protocol, ImagePlus imp) {

        Object iImp = new Object();

        try {
            Class<?> c;
            c = Class.forName(PROCESSINGMAP.get(protocol.get(0).toString()));
            Constructor<?> con;
            try {
                con = c.getConstructor();
                iImp = con.newInstance();  
                ((AbstractImageProcessing)iImp).getVersion();

            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(MicroProtocolPreProcessing.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MicroProtocolPreProcessing.class.getName()).log(Level.SEVERE, null, ex);
        }
        
       ChannelSplitter cs = new ChannelSplitter();
       RGBStackMerge rsm = new RGBStackMerge();
       
       ImagePlus temp_imp = new ImagePlus("Ch_" + (Integer)protocol.get(1) + "_modified", cs.getChannel(imp, (Integer)protocol.get(1)+1)); 
       ImagePlus[] merged = new ImagePlus[imp.getNChannels()];
       for(int i = 0; i < merged.length; i++){
           merged[i] = new ImagePlus("Ch_" + i, cs.getChannel(imp, (Integer)protocol.get(1)+1));
       }
       merged[(Integer)protocol.get(1)] = temp_imp;   
       
       ((AbstractImageProcessing)iImp).process(protocol, temp_imp);
       imp = rsm.mergeHyperstacks(merged, false);
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
