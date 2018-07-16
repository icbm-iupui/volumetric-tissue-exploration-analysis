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
import ij.ImageStack;
import ij.plugin.ChannelSplitter;
import ij.plugin.RGBStackMerge;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.renjin.sexp.BuiltinFunction;
import org.scijava.plugin.Plugin;
import static vtea._vtea.MORPHOLOGICALMAP;
import static vtea._vtea.OBJECTMEASUREMENTMAP;
import static vtea._vtea.SEGMENTATIONMAP;
import static vtea._vtea.getInterleavedStacks;
import vtea.imageprocessing.AbstractImageProcessing;
import vtea.objects.Segmentation.AbstractSegmentation;
import vtea.objects.layercake.SingleThresholdDataModel;
import vtea.objects.measurements.AbstractMeasurement;
import vtea.objects.morphology.AbstractMorphology;
import vteaobjects.MicroObject;



/**
 *
 * @author sethwinfree
 * 
 * 
 */
@Plugin(type = Processor.class)
public class SegmentationProcessor extends AbstractProcessor {
    
    ImagePlus impOriginal;
    ImagePlus impPreview;
    ArrayList protocol;
    int channelProcess;
    
    private ImageStack[] imageStackArray;
    private ArrayList volumes;
    
    
    public SegmentationProcessor(){
        
    VERSION = "0.0";
    AUTHOR = "Seth Winfree";
    COMMENT = "Processor for segmentation processing";
    NAME = "Segmentation Processor";
    KEY = "SegmentationProcessor";
    
    }
    
    public SegmentationProcessor(String str, ImagePlus imp, ArrayList p){
        
    VERSION = "0.0";
    AUTHOR = "Seth Winfree";
    COMMENT = "Processor for segmentation processing";
    NAME = "Segmentation Processor";
    KEY = "SegmentationProcessor";
    
    impOriginal = imp;
    protocol = p;
    key = str;
    
    }

    @Override
    protected Void doInBackground() throws Exception {
         
        ProcessManager(protocol);
      return null;  
      }

    @Override
    public int process(ArrayList al, String... str) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    private void ProcessManager(ArrayList protocol) {
        
        firePropertyChange("progress", 0, 1);
        firePropertyChange("comment", key, ("Starting segmentation...  "));
          
         /**segmentation and measurement protocol redefining.
         * 0: title text, 1: method (as String), 2: channel, 3: ArrayList of JComponents used 
         * for analysis 4: ArrayList of Arraylist for morphology determination
         */
        
        //get class
        //pass protocol and imageplus to segmentation
        
        

            Object iImp = new Object();

            try {
                Class<?> c;
                
                c = Class.forName(SEGMENTATIONMAP.get(protocol.get(1)));
                Constructor<?> con;
                try {
                    con = c.getConstructor();
                    iImp = con.newInstance();  
                    
                             //System.out.println("PROFILING: Instance of " + SEGMENTATIONMAP.get(protocol.get(1)) + ", created.");
     

                } catch ( NullPointerException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    System.out.println("EXCEPTION: new instance decleration error... ");
                }

            } catch (NullPointerException | ClassNotFoundException ex) {
                System.out.println("EXCEPTION: new class decleration error... ");
            }
            
            
         
           try{   
           ((AbstractSegmentation)iImp).process(getInterleavedStacks(impOriginal), protocol, false);
           volumes = ((AbstractSegmentation)iImp).getObjects();
                        } catch (Exception ex) {
                      
            }
           
           //morphology processor
           
           //System.out.println("PROFILING: Processing " + MORPHOLOGICALMAP.size() +" morphological filters.");
     
    
           Collection values =  MORPHOLOGICALMAP.values();
           
           //System.out.println("PROFILING: processing " + MORPHOLOGICALMAP.size() +" morphological filters.");
           
           Iterator<String> itr = values.iterator();
           
            iImp = new Object();
            
            while(itr.hasNext()){

            try {
                Class<?> c;
                
                String str = (String)itr.next();
                
                c = Class.forName(str);
                Constructor<?> con;
                try {
                    con = c.getConstructor();
                    iImp = con.newInstance();  
                    
                    //System.out.println("PROFILING: Instance of " + str + ", created.");
              
                } catch ( NullPointerException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    System.out.println("EXCEPTION: new instance decleration error... ");
                }

            } catch (NullPointerException | ClassNotFoundException ex) {
                System.out.println("EXCEPTION: new class decleration error... ");
            }
           try{  
               //LOOP THROUGH ALL MICROOBJECTS with MorphologyProcessor.
               //this is hardwired for mask only and GUI is NOT considered
               ListIterator<MicroObject> itr_vol = volumes.listIterator();
               
               int size = volumes.size();
               int count = 1;
               
               double progress = 1;
               
               
               MicroObject obj;
               ArrayList<ArrayList<Number>> result;
               int morph = 0;
               
            while(itr_vol.hasNext()){
                obj = itr_vol.next();
                
            progress = 100*((double)count/(double)size);
            firePropertyChange("progress", 0, ((int)progress));
            firePropertyChange("comment", key, ("Performing morphological operations...  "));
       
            count++;
                
                result = ((AbstractMorphology)iImp).process(obj.getPixelsX(), obj.getPixelsY(), obj.getPixelsZ(), "", "1");
                
                ListIterator<ArrayList<Number>> itr_der = result.listIterator();
                
                int voxel = 0;
                int[] x = new int[result.size()];
                int[] y = new int[result.size()];
                int[] z = new int[result.size()];
                
                ArrayList<Integer> xAr = new ArrayList();
                ArrayList<Integer> yAr = new ArrayList();
                ArrayList<Integer> zAr = new ArrayList();
            
                while(itr_der.hasNext()){
                    ArrayList<Number> positions = itr_der.next();
                    xAr.add((Integer)positions.get(0));
                    yAr.add((Integer)positions.get(1));
                    zAr.add((Integer)positions.get(2));
                }
                
                obj.setMorphological(String.valueOf(morph), xAr, yAr, zAr);
                
                morph++;
                
                //System.out.println("PROFILING: morphology operation added... ");
            }
            
            } catch (Exception ex) {
                 ex.printStackTrace();         
            }
           

            }
           
            

            firePropertyChange("progress", 0, 100);
            firePropertyChange("segmentationDone", key, "Segmentation done...  ");
            

           
           
           //System.out.println("PROFILING: Done processing method.");
          
    }
    

    @Override
    public String getChange() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void done() {
        
        
        
        
    }

    
    public ArrayList getObjects() {
        return volumes;
    }
    
    public ArrayList getProtocol() {
        return protocol;
    }
    
    
    
    
}
