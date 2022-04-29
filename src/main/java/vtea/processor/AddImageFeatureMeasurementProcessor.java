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
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imglib2.RealPoint;
import org.scijava.plugin.Plugin;
import static vtea._vtea.OBJECTMEASUREMENTMAP;
import static vtea._vtea.getInterleavedStacks;
import vtea.objects.measurements.AbstractMeasurement;
import vteaobjects.MicroObject;

/**
 *
 * @author sethwinfree
 */
@Plugin(type = Processor.class)
public class AddImageFeatureMeasurementProcessor extends AbstractProcessor {

    private ArrayList<MicroObject> objects;

    //first entry in ArrayList<Number> is the object UID.
    private ArrayList<ArrayList<Number>> measurements;
    


    private Collection features;
    private ArrayList protocol;
    private ImagePlus impOriginal;
    private ArrayList<String> description;
    private ArrayList<String> descriptionLabels;

    private ArrayList objectFeatures;

    public AddImageFeatureMeasurementProcessor() {
        VERSION = "0.0";
        AUTHOR = "Seth Winfree";
        COMMENT = "Processor for measurement processing";
        NAME = "Measurment Processor";
        KEY = "MeasurementProcessor";
    }

    public AddImageFeatureMeasurementProcessor(String k, ImagePlus imp, ArrayList<MicroObject> obj, ArrayList p) {

        VERSION = "0.0";
        AUTHOR = "Seth Winfree";
        COMMENT = "Processor for measurement processing";
        NAME = "Measurment Processor";
        KEY = "MeasurementProcessor";

        objects = obj;
        protocol = p;
        key = k;
        impOriginal = imp;
        
       
        
        

        description = new ArrayList<>();
        descriptionLabels = new ArrayList<>();
        measurements = new ArrayList<>();
        features = OBJECTMEASUREMENTMAP.values();

    }

    public void setupAdHocMeasurementProcessor(Collection newFeatures) {
        features = newFeatures;

        //set morphology arraylist
    }

    @Override
    protected Void doInBackground() throws Exception {
        //get Array of methods
        
        description = new ArrayList<>();
        descriptionLabels = new ArrayList<>();
        measurements = new ArrayList<>();
        features = OBJECTMEASUREMENTMAP.values();
        
        firePropertyChange("progress", 0, 1);
        firePropertyChange("comment", key, "Starting measurements...  ");

        ImageStack[] is = getInterleavedStacks(impOriginal);
        
        //loop through volumes

        //ArrayList<ArrayList<Number>> measurements = new ArrayList();
        int size = objects.size();

        double progress = 1;

        Object[] arr = features.toArray(); //available features
        
      // System.out.println("PROFILING: setting up for: " + features.size() + " features.");

        String[] featurenames = new String[arr.length];

        for (int a = 0; a < arr.length; a++) {
            featurenames[a] = (String) arr[a];
        }
         
        MicroObject obj_test = objects.get(0);
            
        int morphcount = obj_test.getMorphologicalCount();

        /**
         * segmentation and measurement protocol redefining. 0: title text, 1:
         * method (as String), 2: channel, 3: ArrayList of JComponents used for
         * analysis 4: ArrayList of Arraylist for morphology determination
         */
        
        /**
         * morphological protocol in morphologies. morphological determinants
         * 0:Channel 1:Operation 2:Value
         *
         *
         */
        
        // ArrayList for morphology:  0: method(as String), 1: channel, 
        // 2: ArrayList of JComponents for method
        //descriptors for derived volumes  this needs to be:
        //ArrayList 0: channel  1: derivedRegion position for  derived region 
        //ArrayList in the MicroObjects.
        
        ArrayList morphologies = (ArrayList) protocol;
        
        //ArrayList morphologies = (ArrayList) protocol.get(4);
      
        
        for (int l = 0; l < morphologies.size(); l++) {
            ArrayList morphology = (ArrayList) morphologies.get(l);
            morphcount++;
            for (int k = 0; k < featurenames.length; k++) {

                try {
                    Class<?> c;

                    String str = featurenames[k];

                    
                    c = Class.forName(str);
                    Constructor<?> con;

                    Object iImp = new Object();

                    try {
                        con = c.getConstructor();
                        iImp = con.newInstance();
                        String descr = "Img_Ch_" + morphology.get(1) + "_" + 
                                morphology.get(0) + "_" + 
                                ((AbstractMeasurement) iImp).getName() + "_" + 
                                morphcount;

                        if (descr.length() > 10) {
                            descr = descr.substring(0, 8) + "_" + 
                                    descr.substring(descr.length() - 5, descr.length());
                        }

                        description.add(descr);
                        descriptionLabels.add("Added image feature. Row: " + 
                                l + ", Channel: " + morphology.get(1) + 
                                ", Morphology: " + morphology.get(0) + 
                                ", Measurement: " + 
                                ((AbstractMeasurement) iImp).getName());

                    } catch (NullPointerException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        System.out.println("EXCEPTION: new instance decleration error... NPE etc.");
                    }
                } catch (NullPointerException | ClassNotFoundException ex) {
                    System.out.println("EXCEPTION: new class decleration error... Class not found.");
                }
            }
        }
        
        //System.out.println("PROFILING: features to calculate: " + description);  
        //System.out.println("PROFILING: calcualting on " + objects.size() + " cells for : "+ features.size() + " features.");
        //System.out.println("PROFILING: total morphologies: " + obj_test.getMorphologicalCount() + ", new morphologies:" + protocol.size() + "," + morphologies.size());

        Iterator<MicroObject> itr_vol = objects.iterator();
        int count = 1;

        
        while (itr_vol.hasNext()) {
            MicroObject obj = itr_vol.next();
            progress = 100 * ((double) count / (double) size);
            firePropertyChange("progress", 0, ((int) progress));

            count++;

            ArrayList results = new ArrayList();


            //loop through derived morphology settings
            /**
             * morphological protocol in morphologies. morphological
             * determinants 0:Channel 1:Operation 2:Value 3:UID
             */

            int start = obj.getMorphologicalCount()-morphologies.size();
            int morphcounter = 0;
            
            for (int l = start; l < obj.getMorphologicalCount(); l++) {
                
                ArrayList morphology = (ArrayList) morphologies.get(morphcounter);
                morphcounter++;
                //get derived_values by referencing morphologies and "is" 
                int channel = ((int) morphology.get(1)) - 1;

                String UID = (String)morphology.get(3);
                
                //System.out.println("PROFILING: UID: " + UID);
                
                int[] x = obj.getMorphPixelsX(obj.checkMorphological(UID));
                int[] y = obj.getMorphPixelsY(obj.checkMorphological(UID));
                int[] z = obj.getMorphPixelsZ(obj.checkMorphological(UID));

//                int[] x = obj.getMorphPixelsX(l);
//                int[] y = obj.getMorphPixelsY(l);
//                int[] z = obj.getMorphPixelsZ(l);

                //System.out.println("PROFILING: morphology measure on channel: "+ channel + " for length: " + x.length );
                ArrayList<Number> values = new ArrayList();

                //results = new ArrayList();
//
                for (int j = 0; j < x.length; j++) {
                    if (x[j] >= 0 && x[j] < is[channel].getWidth()
                            && y[j] >= 0 && y[j] < is[channel].getHeight()
                            && z[j] >= 0 && z[j] < is[channel].getSize()) {
                        values.add(is[channel].getVoxel(x[j], y[j], z[j]));
                    }
                }
//                
                Iterator itr = features.iterator();
                
                while (itr.hasNext()) {
                    try {
                        Class<?> c;
                        String str = (String) itr.next();
                        c = Class.forName(str);
                        Constructor<?> con;
                        Object iImp = new Object();
                        try {
                            con = c.getConstructor();
                            iImp = con.newInstance();
                            results.add(((AbstractMeasurement) iImp).process(new ArrayList(), (ArrayList<Number>) values));

                        } catch (NullPointerException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            System.out.println("EXCEPTION: new instance decleration error... NPE etc.");
                        }
                    } catch (NullPointerException | ClassNotFoundException ex) {
                        System.out.println("EXCEPTION: new class decleration error... Class not found.");
                    }
                } 
            }
                    measurements.add(results);
        }

        firePropertyChange("progress", 0, 100);
        firePropertyChange("measurementDone", key, "Done...    ");

        return null;
    }

    @Override
    public int process(ArrayList al, String... str) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getChange() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private ArrayList<RealPoint> calculatePoints(MicroObject object) {

        int[] z = object.getPixelsZ();
        int[] x = object.getPixelsX();
        int[] y = object.getPixelsY();

        ArrayList<RealPoint> points = new ArrayList();

        for (int i = 0; i < z.length; i++) {
            points.add(new RealPoint(x[i], y[i], z[i]));
        }
        return points;
    }

    //@Param positions are the points of the object to be measured
    //values is the arraylist for storing measured measurements.
    private Number calculateMeasurement(ArrayList<RealPoint> positions, ArrayList<ArrayList<Number>> values, String operation) {

        Object iImp = new Object();

        try {
            Class<?> c;
            c = Class.forName(operation);
            Constructor<?> con;
            try {
                con = c.getConstructor();
                iImp = con.newInstance();
                return ((AbstractMeasurement) iImp).process(positions, values);

            } catch (NullPointerException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(ImageProcessingProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (NullPointerException | ClassNotFoundException ex) {
            Logger.getLogger(ImageProcessingProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    public ArrayList getDescriptions() {
        return description;
    }

    public ArrayList getDescriptionLabels() {
        return descriptionLabels;
    }

    public ArrayList getFeatures() {
        return measurements;
    }

    public ArrayList<MicroObject> getObjects() {
        return objects;
    }

}
