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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import net.imglib2.RealPoint;
import static vtea._vtea.NEIGHBORHOODMEASUREMENTMAP;
import vtea.objects.neighborhoodmeasurements.AbstractNeighborhoodMeasurement;
import vteaobjects.MicroNeighborhoodObject;
import vteaobjects.MicroObject;

/**
 *
 * @author sethwinfree
 */
public class NeighborhoodMeasurementsProcessor extends AbstractProcessor {

    private ArrayList<MicroNeighborhoodObject> objects;
    private ArrayList<Integer> classes;
    private HashMap<String, String> objectClasses;

    //first entry in ArrayList<Number> is the object UID.
    private ArrayList<ArrayList<Number>> measurements;

    private Collection features;
    private ArrayList protocol;
    private ImagePlus impOriginal;
    private ArrayList<String> description;
    private ArrayList<String> descriptionLabels;

    private ArrayList objectFeatures;

    public NeighborhoodMeasurementsProcessor() {
        VERSION = "0.0";
        AUTHOR = "Seth Winfree";
        COMMENT = "Processor for measurement processing on neighborhoods";
        NAME = "Neighborhood Measurment Processor";
        KEY = "NeighborhoodMeasurementProcessor";
    }

    public NeighborhoodMeasurementsProcessor(String k, ArrayList<MicroNeighborhoodObject> obj, ArrayList<Integer> c, HashMap<String, String> v) {

        VERSION = "0.0";
        AUTHOR = "Seth Winfree";
        COMMENT = "Processor for measurement processing";
        NAME = "Measurment Processor";
        KEY = "MeasurementProcessor";

        objects = obj;
        key = k;
        objectClasses = v;
        classes = c;

        description = new ArrayList<>();
        descriptionLabels = new ArrayList<>();
        measurements = new ArrayList<>();
        features = NEIGHBORHOODMEASUREMENTMAP.values();

    }



    @Override
    protected Void doInBackground() throws Exception {
        //get Array of methods
        firePropertyChange("progress", 0, 1);
        firePropertyChange("comment", key, "Starting measurements...  ");

        //ImageStack[] is = getInterleavedStacks(impOriginal);

        Iterator<MicroNeighborhoodObject> itr_vol = objects.iterator();
        //loop through volumes

    
        int size = objects.size();

        int count = 1;
        double progress = 1;

        Object[] arr = features.toArray(); //available features

        String[] featurenames = new String[arr.length];

        for (int a = 0; a < arr.length; a++) {
            featurenames[a] = (String) arr[a];
        }

        //make descriptor array for Explorer Gui mask volume
        //mask descriptors
        for (int k = 0; k < featurenames.length; k++) {
            for (int j = 0; j < classes.size(); j++) {

                try {
                    Class<?> c;

                    String str = featurenames[k];

                    c = Class.forName(str);
                    Constructor<?> con;

                    Object iImp = new Object();

                    try {

                        con = c.getConstructor();
                        iImp = con.newInstance();

                        //class_text = j + 1;

                       description.add("Class_" + classes.get(j) + "_" + ((AbstractNeighborhoodMeasurement) iImp).getKey());
                       descriptionLabels.add("Class: " + classes.get(j) + ", Measurement:" + ((AbstractNeighborhoodMeasurement) iImp).getName());

                        System.out.println("PROFILING: Adding measurement: " + "Class_" + classes.get(j) + "_" + ((AbstractNeighborhoodMeasurement) iImp).getKey());
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

        while (itr_vol.hasNext()) {

            MicroNeighborhoodObject obj = itr_vol.next();
            
            ArrayList<MicroObject> objMeasure = obj.getObjects();

            progress = 100 * ((double) count / (double) size);
            firePropertyChange("progress", 0, ((int) progress));

            count++;

            ArrayList<Number> results = new ArrayList<>();
            Iterator<String> itr_features = features.iterator();
            
            //System.out.println("PROFILING: Measurements to calculate: " + features.size() + " on " +objMeasure.size() + " objects.");

            //loop through measurements
            while (itr_features.hasNext()) {
                try {
                    Class<?> c;

                    String str = (String) itr_features.next();
                    //System.out.println("PROFILING: Calculating measurements, " + str);

                    c = Class.forName(str);
                    Constructor<?> con;

                    Object iImp = new Object();

                    try {
                        con = c.getConstructor();
                        iImp = con.newInstance();

                        ((AbstractNeighborhoodMeasurement) iImp).process(objMeasure, classes, objectClasses);
                        results.addAll(((AbstractNeighborhoodMeasurement) iImp).process(objMeasure, classes, objectClasses));
                        //System.out.println("PROFILING: Adding measurements, " + results.size());

                    } catch (NullPointerException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        System.out.println("EXCEPTION: new instance decleration error... NPE etc.");
                        ex.printStackTrace();
                    }
                } catch (NullPointerException | ClassNotFoundException ex) {
                    System.out.println("EXCEPTION: new class decleration error... Class not found.");
                    ex.printStackTrace();
                }

            }
            measurements.add(results);
        }

        firePropertyChange("progress", 0, 100);
        firePropertyChange("neighborhoodMeasurementDone", key, "Neighborhood analysis done...    ");

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

    public ArrayList getDescriptions() {
        return description;
    }

    public ArrayList getDescriptionLabels() {
        return descriptionLabels;
    }

    public ArrayList getFeatures() {
        return measurements;
    }

    public ArrayList<MicroNeighborhoodObject> getObjects() {
        return objects;
    }

}
