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

import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import java.awt.Component;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import org.scijava.plugin.Plugin;
import static vtea._vtea.GATEMATHMAP;
import vtea.exploration.listeners.AddFeaturesListener;
import vtea.exploration.listeners.GateMathObjectListener;
import vtea.exploration.plotgatetools.gates.PolygonGate;
import vtea.exploration.plottools.panels.XYExplorationPanel;
import vtea.gates.math.AbstractGateMath;
import vtea.jdbc.H2DatabaseEngine;
import vtea.plotprocessing.AbstractPlotMaker;
import vteaobjects.MicroObject;

/**
 *
 * @author sethwinfree
 */
@Plugin(type = Processor.class)
public class GateMathProcessor extends AbstractProcessor {

    private String keySQLSafe;

    private ArrayList<String> descriptions;
    private ArrayList<ArrayList<Number>> measurements;
    private ArrayList<MicroObject> objects;
    private ArrayList<PolygonGate> gates;

    private ArrayList<String> gatesString;
    private ArrayList<String> operatorsString;

    private ArrayList<MicroObject> gatedObjects;

    HashMap<Double, Integer> result = new HashMap();
    
    private HashMap<Double, Integer> objPositions;

    ArrayList<AddFeaturesListener> addfeaturelisteners = new ArrayList<AddFeaturesListener>();

    private int classAssigned;

    public GateMathProcessor() {

        VERSION = "0.0";
        AUTHOR = "Seth Winfree";
        COMMENT = "Processor for calcualting gating strategies.";
        NAME = "Gate Math Processor";
        KEY = "GateMathProcessor";

    }

    public <T extends String> GateMathProcessor(
            ArrayList<String> gatesString, ArrayList<String> operatorsString,
            ArrayList<PolygonGate> gates, ArrayList<MicroObject> objects,
            ArrayList<ArrayList<Number>> measurements,
            ArrayList<String> descriptions,
            String keySQLSafe, int classAssigned) {

        VERSION = "0.0";
        AUTHOR = "Seth Winfree";
        COMMENT = "Processor for calculating gating strategies.";
        NAME = "Gate Math Processor";
        KEY = "GateMathProcessor";

        this.objects = objects;
        this.gates = gates;
        this.objects = objects;
        this.measurements = measurements;
        this.gatesString = gatesString;
        this.operatorsString = operatorsString;
        this.descriptions = descriptions;
        this.keySQLSafe = keySQLSafe;
        this.classAssigned = classAssigned;
        this.objPositions = getSerialIDHashMap(this.objects);
    }

    @Override
    protected Void doInBackground() throws Exception {

        String currentGate1 = "";
        String currentGate2 = "";
        String currentOperator = "";

        ArrayList<ArrayList> gatedResults = new ArrayList<>();

        try {
//            firePropertyChange("progress", 0, 5);
//            firePropertyChange("comment", "", "Processing gating strategy");

            currentGate1 = this.gatesString.get(0);
            currentGate2 = this.gatesString.get(1);
            currentOperator = this.operatorsString.get(0);
            
            System.out.println("PROFILING: GateMath for 'Assigned' -> " 
                    + classAssigned + ", " + currentGate1
                    + " " + currentOperator + " " + currentGate2);

            Class<?> c;
            c = Class.forName(GATEMATHMAP.get(operatorsString.get(0)));
            Constructor<?> con;

            Object iImp = new Object();

            con = c.getConstructor();
            iImp = con.newInstance();

            ((AbstractGateMath) iImp).settings(objects, measurements, descriptions, keySQLSafe);

            if (currentGate1.contains("GATE-")) {
                if (currentGate2.contains("GATE-")) {
                    gatedResults = ((AbstractGateMath) iImp).process(
                            getGate(currentGate1), getGate(currentGate2));
                } else if (currentGate2.contains("ROI-")) {
                    gatedResults = ((AbstractGateMath) iImp).process(
                            getGate(currentGate1), getRoi(currentGate2));
                }
            } else if (currentGate1.contains("ROI-")) {
                if (currentGate2.contains("GATE-")) {
                    gatedResults = ((AbstractGateMath) iImp).process(
                            getRoi(currentGate1), getGate(currentGate2));
                } else if (currentGate2.contains("ROI-")) {
                    gatedResults = ((AbstractGateMath) iImp).process(
                            getRoi(currentGate1), getRoi(currentGate2));
                }
            }

//            System.out.println("PROFILING: gated: " + gatedResults.get(0).size());
            if (operatorsString.size() > 1) {

                for (int row = 1; row < operatorsString.size(); row++) {
                    Class<?> c1;
                    c1 = Class.forName(GATEMATHMAP.get(operatorsString.get(row)));
                    Constructor<?> con1;

                    Object iImp1 = new Object();

                    con1 = c1.getConstructor();
                    iImp1 = con1.newInstance();

                    currentGate1 = this.gatesString.get(row + 1);

                    if (currentGate1.contains("GATE-")) {
                        gatedResults = ((AbstractGateMath) iImp).process(
                                gatedResults, getGate(currentGate1));
                    } else if (currentGate1.contains("ROI-")) {
                        gatedResults = ((AbstractGateMath) iImp).process(
                                gatedResults, getRoi(currentGate1));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        gatedObjects = gatedResults.get(0);

        //System.out.println("PROFILING: gated: " + gatedObjects.size() + " added to class " + classAssigned);
        ListIterator<MicroObject> itr = gatedObjects.listIterator();

        if (hasColumn()) {

            ArrayList<ArrayList<Number>> features
                    = H2DatabaseEngine.getColumn(vtea._vtea.H2_MEASUREMENTS_TABLE + "_"
                            + keySQLSafe, "Assigned");

            //remove  column
            H2DatabaseEngine.dropColumn(vtea._vtea.H2_MEASUREMENTS_TABLE + "_"
                    + keySQLSafe, "Assigned");

            for (int i = 0; i < features.size(); i++) {
                ArrayList<Number> c = features.get(i);
                this.result.put((double) i, c.get(0).intValue());
            }

        }

        while (itr.hasNext()) {

            MicroObject obj = itr.next();
            result.put((double)objPositions.get(obj.getSerialID()), classAssigned);

        }

        ArrayList<ArrayList<Number>> paddedTable = new ArrayList();
        ArrayList<Number> r = new ArrayList();

        for (int i = 0; i < objects.size(); i++) {
            
            

            MicroObject m = objects.get(i);

            result.putIfAbsent((double)objPositions.get(m.getSerialID()), -1);
            r.add(result.get((double)objPositions.get(m.getSerialID())));

        }

        paddedTable.add(r);
        notifyAddFeatureListener("Assigned", paddedTable);

        return null;
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

    @Override
    public int process(ArrayList al, String... str) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getChange() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Roi getRoi(String name) {
        RoiManager rm = RoiManager.getInstance();
        Roi[] rois = rm.getRoisAsArray();
        for (Roi r : rois) {
            if (r.getName().equals(name.replace("ROI-", ""))) {
                return r;
            }
        }
        return null;
    }

    private PolygonGate getGate(String name) {

        ListIterator<PolygonGate> itr = gates.listIterator();

        while (itr.hasNext()) {
            PolygonGate g = itr.next();
            if (g.getName().equals(name.replace("GATE-", ""))) {
                return g;
            }
        }
        return null;
    }

    public ArrayList<MicroObject> getGatedObjects() {
        return gatedObjects;
    }

    public void addFeatureListener(AddFeaturesListener listener) {
        addfeaturelisteners.add(listener);
    }

    public void notifyAddFeatureListener(String name,
            ArrayList<ArrayList<Number>> feature) {
        for (AddFeaturesListener listener : addfeaturelisteners) {
            listener.addFeatures(name, "",feature);
        }
    }

    private boolean hasColumn() {

        ListIterator<String> itr = descriptions.listIterator();

        while (itr.hasNext()) {
            String str = itr.next();
            if (str.equalsIgnoreCase("Assigned")) {
                return true;
            }
        }
        return false;
    }

}
