/*
 * Copyright (C) 2017 SciJava
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
package vtea.processor;

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
import org.scijava.plugin.Plugin;
import static vtea._vtea.PROCESSINGMAP;
import vtea.imageprocessing.AbstractImageProcessing;


/**
 *
 * @author sethwinfree
 */
@Plugin(type = Processor.class)
public class ImageProcessingProcessor extends AbstractProcessor {

    ImagePlus impOriginal;
    ImagePlus impPreview;
    ArrayList protocol;
    int channelProcess; //-1. 0, 1 etc.  -1 for all.
    
    /*ImageProcessing steps are kept as fields in an ArrayList 
    These fields are arraylists that include:
    0:Name 1: Channel to operate on 2... Components
   */
    public ImageProcessingProcessor(){
    
    VERSION = "0.1";
    AUTHOR = "Seth Winfree";
    COMMENT = "Converting to SciJava plugin architecture";
    NAME = "Image Processing Processor";
    KEY = "ImageProcessingProcessor";
    
    }

    public ImageProcessingProcessor(ImagePlus imp, ArrayList protocol) {

    VERSION = "0.1";
    AUTHOR = "Seth Winfree";
    COMMENT = "Converting to SciJava plugin architecture";
    NAME = "Image Processing Processor";
    KEY = "ImageProcessingProcessor";   
    
        impOriginal = imp;
        this.protocol = protocol;
        //channelProcess = channel;
    
    }
    
    public ImagePlus processPreview() {
        makePreviewImage();
        ListIterator<Object> litr = this.protocol.listIterator();
        while (litr.hasNext()) {
            ProcessManager((ArrayList) litr.next(), impPreview);
        }
        impPreview.resetDisplayRange();
        return impPreview;
    }
    
    public ImagePlus process() {
        impPreview = new Duplicator().run(impOriginal);
        ListIterator<Object> litr = protocol.listIterator();
        while (litr.hasNext()) {
            ProcessManager((ArrayList) litr.next(), impPreview);
        }
        return impPreview;
    }
    
    public ImagePlus getResult() {
        return this.impOriginal;
    }

    public ImagePlus getPreview() {
        return this.impPreview;
    }
    
    public ArrayList getSteps() {
        return this.protocol;
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

            } catch ( NullPointerException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(ImageProcessingProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (NullPointerException | ClassNotFoundException ex) {
            Logger.getLogger(ImageProcessingProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
       if(imp.getNChannels()>1){
        
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
       
       } else {
           ((AbstractImageProcessing)iImp).process(protocol, imp);
           imp.resetDisplayRange();
       }
    }
    
    private void makePreviewImage() {
        
        impOriginal.setZ(impOriginal.getNSlices()/2);
        impOriginal.setRoi(new Roi(0,0,255,255));
        if(impOriginal.getWidth() < 255 || impOriginal.getHeight() < 255){
            impOriginal.setRoi(new Roi(0,0,impOriginal.getWidth(),impOriginal.getHeight()));
        }
        impPreview = new Duplicator().run(impOriginal); //with ROI duplicator only copies ROI
        impPreview.hide();
        impOriginal.deleteRoi(); 
    }   

    @Override
    protected Void doInBackground() throws Exception {
      
        int progress = 0;
   
        try{       
            firePropertyChange("comment", "", "Starting image processing...");
            firePropertyChange("progress", 0, 5);
            ListIterator<Object> litr = this.protocol.listIterator();
            
            int step = 100/protocol.size();
                    
        while (litr.hasNext()) {
            setProgress(progress);
            ProcessManager((ArrayList) litr.next(), impOriginal);
            progress += step;
        }
        setProgress(100);
        firePropertyChange("comment", "", "Done.");
        }catch(Exception e){
        throw e;
        }

      return null;  
    }
    
    @Override
    public void done() {
        try{
              
            firePropertyChange("escape", false, true);
        
        } catch (Exception ex) {
          ex.printStackTrace();         
        }
    }

    @Override
    public int process(ArrayList al, String... str) {
        return 0;
    }

    @Override
    public String getChange() {
        return "";
    }
}
