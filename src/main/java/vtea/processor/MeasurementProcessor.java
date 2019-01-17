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
package vtea.processor;

import ij.ImagePlus;
import ij.ImageStack;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.RecursiveAction;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imglib2.RealPoint;
import static vtea._vtea.MORPHOLOGICALMAP;
import static vtea._vtea.OBJECTMEASUREMENTMAP;
import static vtea._vtea.getInterleavedStacks;
import vtea.objects.measurements.AbstractMeasurement;
import vtea.objects.morphology.AbstractMorphology;
import vteaobjects.MicroObject;
import static java.util.concurrent.ForkJoinTask.invokeAll;
import static java.util.concurrent.ForkJoinTask.invokeAll;
import static java.util.concurrent.ForkJoinTask.invokeAll;
import static java.util.concurrent.ForkJoinTask.invokeAll;

/**
 *
 * @author sethwinfree
 */
public class MeasurementProcessor extends AbstractProcessor {

    private ArrayList<MicroObject> objects;

    //first entry in ArrayList<Number> is the object UID.
    private ArrayList<ArrayList<Number>> measurements;

    private Collection features;
    private ArrayList protocol;
    private ImagePlus impOriginal;
    private ArrayList<String> description;
    private ArrayList<String> descriptionLabels;

    private ArrayList objectFeatures;
    
    public MeasurementProcessor(){
        VERSION = "0.0";
        AUTHOR = "Seth Winfree";
        COMMENT = "Processor for measurement processing";
        NAME = "Measurment Processor";
        KEY = "MeasurementProcessor";
    }
    

    public MeasurementProcessor(String k, ImagePlus imp, ArrayList<MicroObject> obj, ArrayList p) {

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

    @Override
    protected Void doInBackground() throws Exception {
        //get Array of methods
        firePropertyChange("progress", 0, 1);
        firePropertyChange("comment", key, "Starting measurements...  ");

        ImageStack[] is = getInterleavedStacks(impOriginal);

        Iterator<MicroObject> itr_vol = objects.iterator();
        //loop through volumes

        ArrayList<ArrayList<Number>> mask_values = new ArrayList();

        //ArrayList<ArrayList<Number>> measurements = new ArrayList();
        int size = objects.size();

        int count = 1;
        
        int channel_text = 1;

        double progress = 1;

        Object[] arr = features.toArray(); //available features

        String[] featurenames = new String[arr.length];

        for (int a = 0; a < arr.length; a++) {
            featurenames[a] = (String) arr[a];
        }

        //make descriptor array for Explorer Gui mask volume
        //mask descriptors
        for (int k = 0; k < featurenames.length; k++) {
            for (int j = 0; j < impOriginal.getNChannels(); j++) {

                try {
                    Class<?> c;

                    String str = featurenames[k];

                    c = Class.forName(str);
                    Constructor<?> con;

                    Object iImp = new Object();

                    try {

                        con = c.getConstructor();
                        iImp = con.newInstance();
                        
                        channel_text = j+1;
                        
                        description.add("Ch_" + channel_text + "_" + ((AbstractMeasurement) iImp).getKey());
                        descriptionLabels.add("Channel: " + channel_text + ", Measurement:" + ((AbstractMeasurement) iImp).getName());
                        
                        //System.out.println("PROFILING: Adding measurement: " + "Ch_" + channel_text + "_" + ((AbstractMeasurement) iImp).getName());


                    } catch (NullPointerException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        System.out.println("EXCEPTION: new instance decleration error... NPE etc.");
                    }
                } catch (NullPointerException | ClassNotFoundException ex) {
                    System.out.println("EXCEPTION: new class decleration error... Class not found.");
                }
            }
        }

        /**
         * segmentation and measurement protocol redefining. 0: title text, 1:
         * method (as String), 2: channel, 3: ArrayList of JComponents used for
         * analysis 4: ArrayList of Arraylist for morphology determination
         */
        
        /** morphological protocol in morphologies.
         * morphological determinants 0:Channel 1:Operation 2:Value
         * 
         * 
         */
        
         // ArrayList for morphology:  0: method(as String), 1: channel, 
         // 2: ArrayList of JComponents for method
        
        
        
        //descriptors for derived volumes  this needs to be:
        
        //ArrayList 0: channel  1: derivedRegion position for  derived region 
        //ArrayList in the MicroObjects.
        
       
        ArrayList morphologies = (ArrayList) protocol.get(4);

        for (int l = 0; l < morphologies.size(); l++) {
            ArrayList morphology = (ArrayList) morphologies.get(l);
            for (int k = 0; k < featurenames.length; k++) {

                try {
                    Class<?> c;

                    String str = featurenames[k];

                    //System.out.println("PROFILING: feature name, " + str);

                    c = Class.forName(str);
                    Constructor<?> con;

                    Object iImp = new Object();

                    try {

                        con = c.getConstructor();
                        iImp = con.newInstance();
                        
                        
                        String descr = "Ch_" + morphology.get(1) + "_" + morphology.get(0) + "_" + ((AbstractMeasurement) iImp).getName();
                        
              if (descr.length() > 10) {
                    descr = descr.substring(0, 8) + "..." + descr.substring(descr.length() - 5, descr.length());
             }
                       
                        description.add(descr);
                        descriptionLabels.add("Channel: " + morphology.get(1) + ", Morphology: " +  morphology.get(0) + ", Measurement:" + ((AbstractMeasurement) iImp).getName());
  
                    } catch (NullPointerException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        System.out.println("EXCEPTION: new instance decleration error... NPE etc.");
                    }
                } catch (NullPointerException | ClassNotFoundException ex) {
                    System.out.println("EXCEPTION: new class decleration error... Class not found.");
                }
            }
        }
        
        while (itr_vol.hasNext()) {

            MicroObject obj = itr_vol.next();

            progress = 100 * ((double) count / (double) size);
            firePropertyChange("progress", 0, ((int) progress));

            count++;

            mask_values = new ArrayList();

            //mask volumes measurements
            
            for (int i = 0; i < impOriginal.getNChannels(); i++) {

                int[] x = obj.getPixelsX();
                int[] y = obj.getPixelsY();
                int[] z = obj.getPixelsZ();

                ArrayList<Number> channel = new ArrayList();

                for (int j = 0; j < x.length; j++) {
                    if (x[j] >= 0 && x[j] < is[i].getWidth()
                            && y[j] >= 0 && y[j] < is[i].getHeight()
                            && z[j] >= 0 && z[j] < is[i].getSize()) {
                        channel.add(is[i].getVoxel(x[j], y[j], z[j]));
                    }
                }
                mask_values.add(channel);
            }

            ArrayList results = new ArrayList();
            Iterator<String> itr_features = features.iterator();
            
            //loop through measurements
            
            while (itr_features.hasNext()) {
                try {
                    Class<?> c;

                    String str = (String) itr_features.next();

                    c = Class.forName(str);
                    Constructor<?> con;

                    Object iImp = new Object();

                    try {
                        con = c.getConstructor();
                        iImp = con.newInstance();

                        for (int k = 0; k < mask_values.size(); k++) {
                            
                            ArrayList<int[]> al = new ArrayList<int[]>();
                            
                            al.add(obj.getPixelsX());
                            al.add(obj.getPixelsY());
                            al.add(obj.getPixelsZ());

                            results.add(((AbstractMeasurement) iImp).process(al, (ArrayList<Number>) mask_values.get(k)));

                        }

                    } catch (NullPointerException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        System.out.println("EXCEPTION: new instance decleration error... NPE etc.");
                    }
                } catch (NullPointerException | ClassNotFoundException ex) {
                    System.out.println("EXCEPTION: new class decleration error... Class not found.");
                }

            }
            measurements.add(results);
            
            //
            //
            //loop through derived morphology settings
            
         /** morphological protocol in morphologies.
         * morphological determinants 0:Channel 1:Operation 2:Value
         */   

            for (int l = 0; l < morphologies.size(); l++) {

                ArrayList morphology = (ArrayList) morphologies.get(l);

              //get derived_values by referencing morphologies and "is" 
                
                int channel = ((int)morphology.get(1))-1;

                int[] x = obj.getMorphPixelsX(l);
                int[] y = obj.getMorphPixelsY(l);
                int[] z = obj.getMorphPixelsZ(l);
                
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

        }

        firePropertyChange("progress", 0, 100);
        firePropertyChange("measurementDone", key, "Segmentation and measurement done...    ");

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
