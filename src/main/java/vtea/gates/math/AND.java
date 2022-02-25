/*
 * Copyright (C) 2021 SciJava
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
package vtea.gates.math;

import ij.gui.Roi;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import org.scijava.plugin.Plugin;
import vtea.exploration.plotgatetools.gates.Gate;
import vtea.exploration.plotgatetools.gates.PolygonGate;
import vtea.jdbc.H2DatabaseEngine;
import vtea.objects.measurements.Measurements;
import vteaobjects.MicroObject;

/**
 *
 * @author sethwinfree
 * @param <T>
 * @param <A>
 * @param <K>
 */
@Plugin(type = GateMath.class)
public class AND<T extends Gate, A extends Roi, K extends MicroObject> extends AbstractGateMath {

    ArrayList<MicroObject> objects;
    ArrayList<ArrayList<Number>> measurements;
    ArrayList<String> descriptions;
    String keySQLSafe;
    HashMap<Double, Integer> objPositions;

    public AND() {
        VERSION = "0.0";
        AUTHOR = "Seth Winfree";
        COMMENT = "Calculate AND of Gates or Rois";
        NAME = "AND";
        KEY = "AND";
        TYPE = "NA";
    }

    @Override
    public void settings(ArrayList objects, ArrayList measurements,
            ArrayList descriptions, String keySQLSafe) {
        this.objects = objects;
        this.measurements = measurements;
        this.descriptions = descriptions;
        this.keySQLSafe = keySQLSafe;
        objPositions = getSerialIDHashMap(this.objects);
    }

    @Override
    public ArrayList<ArrayList> process(Roi g1, PolygonGate g2) {

        ArrayList<ArrayList> result = new ArrayList<>();

        Path2D.Double path = g2.createPath2DInChartSpace();

        ArrayList<MicroObject> objectsTemp = new ArrayList<MicroObject>();
        ArrayList<MicroObject> objectsGated = new ArrayList<MicroObject>();
        ArrayList<MicroObject> objectsFinal = new ArrayList<MicroObject>();

        ArrayList<ArrayList<Number>> sortTemp
                = new ArrayList<ArrayList<Number>>();

        ArrayList<ArrayList<Number>> measurementsTemp
                = new ArrayList<ArrayList<Number>>();
        ArrayList<ArrayList<Number>> measurementsFinal
                = new ArrayList<ArrayList<Number>>();
        ArrayList<ArrayList<Number>> measurementsGated
                = new ArrayList<ArrayList<Number>>();

        //ArrayList<String> description = new ArrayList<>();

        double xValue = 0;
        double yValue = 0;

        //System.out.println("PROFILING: Measurements length, " + measurements.size());
        //this is where we need to add logic for polygons...  this is tripping up things
        ArrayList<ArrayList> resultKey
                = H2DatabaseEngine.getObjectsInRange2D(path,
                        vtea._vtea.H2_MEASUREMENTS_TABLE + "_" + keySQLSafe,
                        g2.getXAxis(), path.getBounds2D().getX(),
                        path.getBounds2D().getX() + path.getBounds2D().getWidth(),
                        g2.getYAxis(), path.getBounds2D().getY(),
                        path.getBounds2D().getY() + path.getBounds2D().getHeight(),
                        g2.getXAxis());

        ListIterator<ArrayList> itr = resultKey.listIterator();

        while (itr.hasNext()) {
            ArrayList al = itr.next();
            double object = (Double)al.get(0);
            objectsTemp.add(objects.get(objPositions.get(object)));
            measurementsTemp.add(this.measurements.get(objPositions.get(object)));
            sortTemp.add(al);
        }

        measurementsGated = measurementsTemp;
        objectsGated = objectsTemp;

        try {
            int position = 0;
            for (int i = 0; i < objectsGated.size(); i++) {

                MicroObject object = ((MicroObject) objectsGated.get(i));

                ArrayList<Number> sorted = (ArrayList<Number>) sortTemp.get(i);

                xValue = sorted.get(1).doubleValue();
                yValue = sorted.get(2).doubleValue();

                if (path.contains(xValue, yValue)) {

                    objectsFinal.add(object);

                    measurementsFinal.add(cloneMeasurements(measurementsGated.get(i)));
                    position++;
                }
            }

            ListIterator<MicroObject> itr1 = objectsFinal.listIterator();

            objectsGated = new ArrayList<MicroObject>();

            while (itr1.hasNext()) {
                MicroObject o = itr1.next();
                if (g1.contains((int) o.getCentroidX(), (int) o.getCentroidY())) {
                    objectsGated.add(o);
                }
            }

            objectsFinal = objectsGated;

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        result.add(objectsFinal);
        result.add(measurementsFinal);
        return result;
    }

    @Override
    public ArrayList<ArrayList> process(PolygonGate g1, PolygonGate g2) {
       
        ArrayList<ArrayList> result = new ArrayList<>();

        Path2D.Double path = g2.createPath2DInChartSpace();

        ArrayList<MicroObject> objectsTemp = new ArrayList<MicroObject>();
        ArrayList<MicroObject> objectsGated = new ArrayList<MicroObject>();
        ArrayList<MicroObject> objectsFinal = new ArrayList<MicroObject>();

        ArrayList<ArrayList<Number>> sortTemp
                = new ArrayList<ArrayList<Number>>();

        ArrayList<ArrayList<Number>> measurementsTemp
                = new ArrayList<ArrayList<Number>>();
        ArrayList<ArrayList<Number>> measurementsFinal
                = new ArrayList<ArrayList<Number>>();
        ArrayList<ArrayList<Number>> measurementsGated
                = new ArrayList<ArrayList<Number>>();

        double xValue = 0;
        double yValue = 0;

        ArrayList<ArrayList> resultKey
                = H2DatabaseEngine.getObjectsInRange2D(path,
                        vtea._vtea.H2_MEASUREMENTS_TABLE + "_" + keySQLSafe,
                        g2.getXAxis(), path.getBounds2D().getX(),
                        path.getBounds2D().getX() + path.getBounds2D().getWidth(),
                        g2.getYAxis(), path.getBounds2D().getY(),
                        path.getBounds2D().getY() + path.getBounds2D().getHeight(),
                        g2.getXAxis());

        ListIterator<ArrayList> itr = resultKey.listIterator();
        
        //System.out.println("PROFILING: First gate: " + resultKey.get(0).size());
        
      
        while (itr.hasNext()) {
            ArrayList al = itr.next();
            double object = (Double)al.get(0);
            objectsTemp.add(objects.get(objPositions.get(object)));
            measurementsTemp.add(this.measurements.get(objPositions.get(object)));
            sortTemp.add(al);
        }

        measurementsGated = measurementsTemp;
        objectsGated = objectsTemp;

        try {
            //int position = 0;
            for (int i = 0; i < objectsGated.size(); i++) {

                MicroObject object = ((MicroObject) objectsGated.get(i));

                ArrayList<Number> sorted = (ArrayList<Number>) sortTemp.get(i);

                xValue = sorted.get(1).doubleValue();
                yValue = sorted.get(2).doubleValue();

                if (path.contains(xValue, yValue)) {

                    objectsFinal.add(object);

                    measurementsFinal.add(cloneMeasurements(measurementsGated.get(i)));
                    //position++;
                }
            }

            ListIterator<MicroObject> itr1 = objectsFinal.listIterator();

            objectsGated = new ArrayList<MicroObject>();
            
            Path2D.Double path2 = g1.createPath2DInChartSpace();
            
            int counter = 0;
            
            //System.out.println("PROFILING: First gate: " + objectsFinal.size());

            while (itr1.hasNext()) {
                MicroObject o = itr1.next();
                if (path2.contains(
                        (float) (measurementsFinal.get(counter)).get(descriptions.indexOf(g1.getXAxis())), 
                        (float) (measurementsFinal.get(counter)).get(descriptions.indexOf(g1.getYAxis())))) {
                    objectsGated.add(o);
                }
                counter++;
            }

            objectsFinal = objectsGated;
            
           // System.out.println("PROFILING: Second gate: " + objectsFinal.size());

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        result.add(objectsFinal);
        result.add(measurementsFinal);
        return result;
    }

    @Override
    public ArrayList<ArrayList> process(Roi g1, Roi g2) {

        ListIterator<MicroObject> itr = objects.listIterator();

        ArrayList<MicroObject> objectsGated = new ArrayList<>();
        ArrayList<ArrayList<Number>> measurementsGated = new ArrayList<>();

        ArrayList<ArrayList> result = new ArrayList<>();

        int measurement = 0;

        while (itr.hasNext()) {
            MicroObject o = itr.next();
            if (g1.contains((int) o.getCentroidX(), (int) o.getCentroidY())
                    && g2.contains((int) o.getCentroidX(), (int) o.getCentroidY())) {
                objectsGated.add(objects.get(objPositions.get(o.getSerialID())));
                measurementsGated.add(measurements.get(objPositions.get(o.getSerialID())));
                measurement++;
            }
        }
        result.add(objectsGated);
        result.add(measurementsGated);

        return result;
    }


    private ArrayList<Number> cloneMeasurements(ArrayList<Number> al) {
        ArrayList<Number> result = new ArrayList<Number>();

        for (int i = 0; i < al.size(); i++) {
            float f = al.get(i).floatValue();
            result.add(Float.valueOf(f));
        }

        return result;
    }

    @Override
    public ArrayList process(PolygonGate g1, Roi g2) {
        return process(g2,g1);
    }

    @Override
    public ArrayList process(ArrayList gatedResults, PolygonGate g1) {
        
        ArrayList<MicroObject> objectsGated = (ArrayList<MicroObject>)gatedResults.get(0);
        ArrayList<ArrayList<Number>> measurementsGated = (ArrayList<ArrayList<Number>>)gatedResults.get(1);
        
        ArrayList<MicroObject> objectsFinal = new ArrayList<>();
        ArrayList<ArrayList<Number>> measurementsFinal = new ArrayList<>();
        
        ArrayList<ArrayList> result = new ArrayList<>();
        
        ListIterator<MicroObject> itr1 = objectsGated.listIterator();

            objectsGated = new ArrayList<MicroObject>();
            
            Path2D.Double path2 = g1.createPath2DInChartSpace();
            
            int counter = 0;

            while (itr1.hasNext()) {
                MicroObject o = itr1.next();
                if (path2.contains(
                        (float) (measurementsGated.get(counter)).get(descriptions.indexOf(g1.getXAxis())), 
                        (float) (measurementsGated.get(counter)).get(descriptions.indexOf(g1.getYAxis())))) {
                    objectsFinal.add(objects.get(objPositions.get(o.getSerialID())));
                    measurementsFinal.add(measurements.get(objPositions.get(o.getSerialID())));
                }
                counter++;
            }

        result.add(objectsFinal);
         result.add(measurementsFinal);
         
         return result;
    }
    
    
    @Override
    public ArrayList process(ArrayList gatedResults, Roi g1) {
        ArrayList<MicroObject> objectsGated = (ArrayList<MicroObject>)gatedResults.get(0);
        ArrayList<ArrayList<Number>> measurementsGated = (ArrayList<ArrayList<Number>>)gatedResults.get(1);
        
        ArrayList<MicroObject> objectsFinal = new ArrayList<>();
        ArrayList<ArrayList<Number>> measurementsFinal = new ArrayList<>();
        
        ArrayList<ArrayList> result = new ArrayList<>();
        
        ListIterator<MicroObject> itr1 = objectsGated.listIterator();

            objectsGated = new ArrayList<MicroObject>();
            
           
            
            int counter = 0;

            while (itr1.hasNext()) {
                MicroObject o = itr1.next();
                if(g1.contains((int)o.getCentroidX(),(int)o.getCentroidY())) {
                    objectsFinal.add(objects.get(objPositions.get(o.getSerialID())));
                    measurementsFinal.add(measurements.get(objPositions.get(o.getSerialID())));
                }
                counter++;
            }

        result.add(objectsFinal);
         result.add(measurementsFinal);
         
         return result;
    }
    
           private HashMap<Double, Integer> getSerialIDHashMap(ArrayList<MicroObject> objs) {

        HashMap<Double, Integer> lookup = new HashMap();

        int position = 0;

        ListIterator<MicroObject> itr = objs.listIterator();

        while (itr.hasNext()) {
            MicroObject obj = itr.next();
            lookup.put(obj.getSerialID(), position);
            position++;
        }

        return lookup;

    }

}
