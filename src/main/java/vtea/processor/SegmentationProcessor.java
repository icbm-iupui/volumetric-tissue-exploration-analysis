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
package vtea.processor;

import ij.ImagePlus;
import ij.ImageStack;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ForkJoinPool;
import static java.util.concurrent.ForkJoinTask.invokeAll;
import java.util.concurrent.RecursiveAction;
import org.scijava.plugin.Plugin;
import static vtea._vtea.MORPHOLOGICALMAP;
import static vtea._vtea.SEGMENTATIONMAP;
import static vtea._vtea.getInterleavedStacks;
import vtea.objects.Segmentation.AbstractSegmentation;
import vtea.objects.Segmentation.Region2DSingleThreshold;
import vtea.objects.layercake.microRegion;
import vtea.objects.layercake.microVolume;
import vtea.objects.morphology.AbstractMorphology;
import vteaobjects.MicroObject;
import static java.util.concurrent.ForkJoinTask.invokeAll;
import javax.swing.JComponent;

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
    
    List<MicroObject> volumes = Collections.synchronizedList(new ArrayList<MicroObject>());

    public SegmentationProcessor() {

        VERSION = "0.0";
        AUTHOR = "Seth Winfree";
        COMMENT = "Processor for segmentation processing";
        NAME = "Segmentation Processor";
        KEY = "SegmentationProcessor";

    }

    public SegmentationProcessor(String str, ImagePlus imp, ArrayList p) {

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

        /**
         * segmentation and measurement protocol redefining. 0: title text, 1:
         * method (as String), 2: channel, 3: ArrayList of JComponents used for
         * analysis 4: ArrayList of Arraylist for morphology determination
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
            } catch (NullPointerException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                System.out.println("EXCEPTION: new instance decleration error... ");
            }

        } catch (NullPointerException | ClassNotFoundException ex) {
            System.out.println("EXCEPTION: new class decleration error... ");
        }

        ((AbstractSegmentation) iImp).addListener(this);

        try {
            if(impOriginal.getNSlices() > 1){
            ((AbstractSegmentation) iImp).process(getInterleavedStacks(impOriginal), protocol, false);
            }
            else {
            ((AbstractSegmentation) iImp).process(getInterleavedStacks(impOriginal), protocol, false); 
            }

            volumes = ((AbstractSegmentation) iImp).getObjects();

            
        } catch (Exception ex) {
            System.out.println("EXCEPTION: Error in object segmentation... ");

        }

        long start_time = System.currentTimeMillis();

        ProcessorForkPool pfp = new ProcessorForkPool(protocol, 0, volumes.size());
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(pfp);

        long end_time = System.currentTimeMillis();

        System.out.println("PROFILING: Segmentation time: " + (end_time - start_time) + " ms.");

        firePropertyChange("progress", 0, 100);
        firePropertyChange("segmentationDone", key, "Segmentation done...  ");
    }

    @Override
    public String getChange() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void done() {

    }

    public ArrayList getObjects() {
        return (ArrayList)volumes;
    }

    public ArrayList getProtocol() {
        return protocol;
    }

    @Override
    public void FireProgressChange(String str, double db) {

        //System.out.println("Building progress..." + db);
        firePropertyChange("progress", 0, (int) db);
        firePropertyChange("comment", key, str);
    }

    private class ProcessorForkPool extends RecursiveAction {

        private int start;
        private int stop;

        private ArrayList protocol;
        
        
        ProcessorForkPool(ArrayList p, int start, int stop) {

            this.start = start;
            this.stop = stop;
            this.protocol = p;

        }

        @Override
        protected void compute() {
            
            long processors = Runtime.getRuntime().availableProcessors();
            
            
          
            long length = volumes.size() / processors;

            if (volumes.size() < processors) {
                length = volumes.size();
            }
            if (stop - start > length) {

                invokeAll(new ProcessorForkPool(protocol, start, start + ((stop - start) / 2)),
                        new ProcessorForkPool(protocol, start + ((stop - start) / 2) + 1, stop));

            } else {
                doThis();
                //doThat();
            }
        }

        private void doThis() {

              /**
             * segmentation and measurement protocol redefining. 0: title text,
             * 1: method (as String), 2: channel, 3: ArrayList of JComponents
             * used for analysis 4: ArrayList of Arraylist for morphology
             * determination
             * 
             // ArrayList for morphology:  0: method(as String), 1: channel, 
             // 2: ArrayList of JComponents for method
             */
            //descriptors for derived volumes
            
            firePropertyChange("reset", 0, 0);
            
            int count = 1;

            double progress = 1;
            
            

            ArrayList<ArrayList> morphologies = (ArrayList) protocol.get(4);
            
             Iterator<ArrayList> itr = morphologies.iterator();

            Object iImp = new Object();
            
            

            while (itr.hasNext()) {

                ArrayList morphology = (ArrayList)itr.next();
                
                 

                int channel = (int)morphology.get(1);
                
                    
                
                //components for method approach
                
                
                ArrayList<JComponent> components = (ArrayList<JComponent>)morphology.get(2);
                
                //System.out.println("PROFILING: Morphological processing: " + (String) morphology.get(0) + ", on " + volumes.size() + " volumes.");
                
                

                try {

                    Class<?> c;
                    
                    //method name for class generation

                    String str = (String) morphology.get(0);
                    
                    

                    c = Class.forName(MORPHOLOGICALMAP.get(str));
                    Constructor<?> con;

                    con = c.getConstructor();
                    iImp = con.newInstance();

                    
                    ArrayList<ArrayList<Number>> result;
                    int morph = 0;

                    int volumecount = 1;

                    //List sub_volumes = volumes.subList(start, stop);
                    
                    
                    ListIterator<MicroObject> itr_vol = volumes.listIterator(start);
                    
                    

                    int i = start;
                    
                    int end = stop-start;
                    
                    
                    
                     
                    //this is where I grab the protocols.
                    
                    while (itr_vol.hasNext() && i <= stop){
                        MicroObject obj = new MicroObject();
                        obj = (MicroObject)itr_vol.next();

                        progress = 100 * ((double) count / (double) volumes.size());
                        firePropertyChange("progress", 0, ((int) progress));
                        firePropertyChange("comment", key, ("Performing morphological operations...  "));

                        count++;

                        /**
                         * result. This is an arraylist of arraylist of numbers.
                         *
                         */
                        
                         
                        
                        
                        //test for existing morphology
                        
                        String current_UID = ((AbstractMorphology)iImp).getUID(components);
                        
                        
                        //System.out.println("PROFILING: Current morphology: " + current_UID);
                        
                        current_UID.concat(str);
                        
                        int same_morphology = obj.checkMorphological(current_UID);
                        
                        //System.out.println("PROFILING: Checked for morphology: " + current_UID + ", result: " + same_morphology);
                        
                       
                        //grab the morphology results
                        
                        //System.out.println("PROFILING: Object size: " + (obj.getPixelsX()).length);
                        
                        //String Derived_UID = iImp.getClass().getName();
                        
                        if(same_morphology == -1){
                        
                        
                        result = ((AbstractMorphology) iImp).process(obj.getPixelsX(), obj.getPixelsY(), obj.getPixelsZ(), components, "", String.valueOf(channel));
                          
                        ArrayList<Number> xAr = result.get(0);
                        ArrayList<Number> yAr = result.get(1);
                        ArrayList<Number> zAr = result.get(2);
                        
                        int[] x = new int[xAr.size()];
                        int[] y = new int[xAr.size()];
                        int[] z = new int[xAr.size()];
                        
                        for(int j = 0; j < xAr.size(); j++){
                            x[j] = xAr.get(j).intValue();
                            y[j] = yAr.get(j).intValue();
                            z[j] = zAr.get(j).intValue();
                        }
                        
                        //System.out.println("PROFILING: Generating morphology");
                        
                        obj.setMorphological(current_UID, x, y, z);
                        
                        }else{
                            
                        //System.out.println("PROFILING: Skipping morphology and copying");
                           
                        obj.setMorphological(current_UID, obj.getMorphPixelsX(same_morphology), obj.getMorphPixelsY(same_morphology), obj.getMorphPixelsZ(same_morphology));   
                            
                        }
                        
                        volumecount++;
                        morph++;
                        i++;
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }

        }
    }
}
