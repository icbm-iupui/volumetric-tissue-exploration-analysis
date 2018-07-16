/*
 * Copyright (C) 2018 SciJava
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imglib2.RealPoint;
import static vtea._vtea.MORPHOLOGICALMAP;
import static vtea._vtea.OBJECTMEASUREMENTMAP;
import static vtea._vtea.OBJECTMEASUREMENTOPTIONS;
import static vtea._vtea.getInterleavedStacks;
import vtea.objects.layercake.microVolume;
import vtea.objects.measurements.AbstractMeasurement;
import vteaobjects.MicroObject;

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
    private String key;
    private ImagePlus impOriginal;
    private ArrayList<String> description;
    
    
    
    private ArrayList objectFeatures;
    
    public MeasurementProcessor(String k, ImagePlus imp, ArrayList<MicroObject> obj, ArrayList prt){
        
        objects = obj;
        protocol = prt;
        key = k;
        impOriginal = imp;
        
        description = new ArrayList<>(); 
        measurements = new ArrayList<>();
        features =  OBJECTMEASUREMENTMAP.values();
        
        
        //protocol what does it look like.
        
//        for(int i = 0; i < vtea._vtea.OBJECTMEASUREMENTOPTIONS.length; i++){
//            System.out.println("PROFILING:                       Measurment added: " + vtea._vtea.OBJECTMEASUREMENTOPTIONS[i] + ".");
//            features.add(vtea._vtea.OBJECTMEASUREMENTOPTIONS[i]); 
//  
//        }
        
        
        
        
    }

    @Override
    protected Void doInBackground() throws Exception {
        //get Array of methods
        firePropertyChange("progress", 0, 1);
        firePropertyChange("comment", key, "Starting measurements...  ");
        
        ImageStack[] is = getInterleavedStacks(impOriginal);
        
        Iterator<MicroObject> itr_vol = objects.iterator();
        //loop through volumes
        
        ArrayList values = new ArrayList();
        
        int size = objects.size();
        
        int count = 1;
        
        double progress = 1;
        
        Object[] arr = features.toArray();
        String[] text = new String[arr.length];
        
        for(int a = 0; a < arr.length; a++){     
            text[a] = (String)arr[a];           
        }
        
        for(int j = 0; j < impOriginal.getNChannels(); j++){ 
            for(int k = 0; k < text.length; k++){
                
                try {
                    Class<?> c;

                    String str = text[k];

                    c = Class.forName(str);
                    Constructor<?> con;

                    Object iImp = new Object();

                    try {
                        
                        con = c.getConstructor();
                        iImp = con.newInstance();
                         description.add("Ch_" + j + "_"+ ((AbstractMeasurement)iImp).getName());
  
                     
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
            
            //System.out.println("PROFILING: Measuring object " + count + ",  of " + size + " or " );
            
            progress = 100*((double)count/(double)size);
            firePropertyChange("progress", 0, ((int)progress));
           
            //firePropertyChange("comment", key, ("Measuring object " + obj.getSerialID() + "...  "));
            
            count++;
            
            values = new ArrayList();
            
            ArrayList results = new ArrayList();

            for (int i = 0; i < impOriginal.getNChannels(); i++) {

                int[] x = obj.getPixelsX();
                int[] y = obj.getPixelsY();
                int[] z = obj.getPixelsZ();
                
                

                ArrayList<Number> channel = new ArrayList();
                
                //System.out.println("PROFILING: Pixel length:" + x.length + " for channel " +  i);
                  
                for (int j = 0; j < x.length; j++) {
                    
                //System.out.println("PROFILING: Checking pixel #" +j+ " " + x[j] + ", " + y[j] + ", " + z[j] + ".");
    

                    if (x[j] >= 0 && x[j] < is[i].getWidth()
                            && y[j] >= 0 && y[j] < is[i].getHeight()
                            && z[j] >= 0 && z[j] < is[i].getSize()) {
                        channel.add(is[i].getVoxel(x[j], y[j], z[j]));
                          //System.out.println("PROFILING: Adding:" + x[j] + ", " + y[j] + ", " + z[j] + ".");
    
                    }
                }
                values.add(channel);
                
               //System.out.println("PROFILING: " + values.size() + " channel data added.");
     
            }

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

                        //System.out.println("PROFILING: Instance of " + str + ", created.");
                        
                        for(int k = 0; k < values.size(); k++){
                            
                            //System.out.println("PROFILING: description: " + "Ch_" + k + "_"+ ((AbstractMeasurement)iImp).getKey());
        
                            
                            results.add(((AbstractMeasurement)iImp).process(new ArrayList(), (ArrayList<Number>)values.get(k)));
                            
                        }  
                    } catch (NullPointerException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        System.out.println("EXCEPTION: new instance decleration error... NPE etc.");
                    }
                } catch (NullPointerException | ClassNotFoundException ex) {
                    System.out.println("EXCEPTION: new class decleration error... Class not found.");
                }
                measurements.add(results);
            }
        }
        
        //System.out.println("PROFILING: calculated " + measurements.get(0).size() + " measurements for " + measurements.size() + " total measures.");
        
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
    
    private ArrayList<RealPoint> calculatePoints(MicroObject object){
        
        int[] z = object.getPixelsZ();
        int[] x = object.getPixelsX();
        int[] y = object.getPixelsY();
        
        ArrayList<RealPoint> points = new ArrayList();
        
        
        
        for(int i = 0; i < z.length; i++){
            points.add(new RealPoint(x[i], y[i], z[i]));
        }
        return points;
    }
    
    //@Param positions are the points of the object to be measured
    //values is the arraylist for storing measured values.
    
    private Number calculateMeasurement(ArrayList<RealPoint> positions, ArrayList<ArrayList<Number>> values, String operation){
        
        Object iImp = new Object();
        
        try {
            Class<?> c;
            c = Class.forName(operation);
            Constructor<?> con;
            try {
                con = c.getConstructor();
                iImp = con.newInstance(); 
                return ((AbstractMeasurement)iImp).process(positions, values);

            } catch ( NullPointerException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(ImageProcessingProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (NullPointerException | ClassNotFoundException ex) {
            Logger.getLogger(ImageProcessingProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }
    
    public ArrayList getDescriptions() {
//        ArrayList al = new ArrayList();
//
//        //microVolume.Analytics;
//        protocol.size();
//        for (int i = 0; i < protocol.size(); i++) {
//            for (int c = 0; c < microVolume.Analytics.length; c++) {
//                String derived = new String();
//                String text = new String();
//                if (i == 0) {
//                    derived = " ";
//                } else {
//                    derived = "_d" + ((ArrayList) protocol.get(i)).get(1) + " ";
//                }
//                text = "Ch" + ((int)((ArrayList) protocol.get(i)).get(0)+1) + derived + microVolume.Analytics[c];
//                al.add(text);
//            }
//        }
//        return al;
        return description;
    }
    
    public ArrayList getFeatures(){
        return measurements;
    }
    
    public ArrayList<MicroObject> getObjects(){
        return objects;
    }
    
}
