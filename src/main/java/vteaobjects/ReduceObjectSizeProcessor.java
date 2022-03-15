/*
 * Copyright (C) 2022 SciJava
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
package vteaobjects;

import ij.ImagePlus;
import ij.io.FileSaver;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Set;
import vtea._vtea;
import vtea.processor.AbstractProcessor;
import vteaexploration.ProgressTracker;

/**
 *
 * @author sethwinfree
 */
public class ReduceObjectSizeProcessor extends AbstractProcessor {

    private String key;
    private ImagePlus image;
    private ArrayList<MicroObject> objects;
    private ArrayList measurements;
    private ArrayList headers;
    private ArrayList headerLabels;

    private File file;

    private ArrayList<MicroObject> objectsProcessed;

    public ReduceObjectSizeProcessor(String k, ImagePlus imp, ArrayList<MicroObject> obj,
            ArrayList meas, ArrayList head, ArrayList headLab, File f) {
        objects = obj;
        image = imp;
        measurements = meas;
        headers = head;
        headerLabels = headLab;
        file = f;

    }

    @Override
    protected Void doInBackground() throws Exception {

        System.out.println("PROFILING: " + "Checking for segmentation redundancies...");

        ArrayList<MicroObject> newObjects = new ArrayList<MicroObject>();
        ArrayList<Integer> morphology = getUniqueMorphology();

        ListIterator<MicroObject> itr = objects.listIterator();

        objectsProcessed = new ArrayList<MicroObject>();

        while (itr.hasNext()) {

            MicroObject obj = itr.next();
            Set<String> keys = obj.morphologicalLookup.keySet();
            String[] keysArr = new String[keys.size()];
            keysArr = keys.toArray(keysArr);
            
            MicroObject newObject = new MicroObject();

            newObject.setSerialID((int) obj.getSerialID());
            newObject.setPixelsX(obj.getPixelsX());
            newObject.setPixelsY(obj.getPixelsY());
            newObject.setPixelsZ(obj.getPixelsZ());
            newObject.setCentroid();
            newObject.setGated(obj.getGated());
           
            for (int i = 0; i < morphology.size(); i++) {
                newObject.setMorphological(keysArr[morphology.get(i)], obj.getMorphPixelsX(morphology.get(i)),
                        obj.getMorphPixelsY(morphology.get(i)), obj.getMorphPixelsZ(morphology.get(i)));
            }
            newObjects.add(newObject);
        }
        objectsProcessed.addAll(newObjects);

        exportObx();

        return null;
    }

    private void exportObx() {
        try {
            ArrayList output = new ArrayList();
            output.add(key);
            output.add(objectsProcessed);
            output.add(measurements);
            output.add(headers);
            output.add(headerLabels);

            try {
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(output);
                oos.close();

                FileSaver fs = new FileSaver(image);
                fs.saveAsTiffStack(file.getParent() + "/" + key + ".tif");

            } catch (IOException e) {
                System.out.println("ERROR: Could not save the file" + e);
            }
        } catch (NullPointerException ne) {
            System.out.println("ERROR: NPE in reduced object export");
        }

        _vtea.LASTDIRECTORY = file.getAbsolutePath();
    }

    private ArrayList<Integer> getUniqueMorphology() {

        ArrayList<Integer> result = new ArrayList<Integer>();

        MicroObject obj = objects.get(0);

        int morphCount = obj.getMorphologicalCount();

        //System.out.println("PROFILING: Total morphologies: "
        //       + morphCount);
        ArrayList<int[]> morphs = obj.getMorphological(0);
        int[] pixels_x = morphs.get(0);
        int[] pixels_y = morphs.get(1);
        int[] pixels_z = morphs.get(2);

        int morphSize_x = pixels_x.length;
        int morphSize_y = pixels_y.length;
        int morphSize_z = pixels_z.length;

        result.add(0);

        for (int j = 1; j < morphCount; j++) {
            ArrayList<int[]> testmorphs = obj.getMorphological(j);
            int[] x = testmorphs.get(0);
            int[] y = testmorphs.get(1);
            int[] z = testmorphs.get(2);
            if (morphSize_x != x.length | morphSize_y != y.length
                    | morphSize_z != z.length) {
                morphSize_x = x.length;
                morphSize_y = y.length;
                morphSize_z = z.length;
                result.add(j);
            }
        }

        System.out.println("PROFILING: Reduced obx size to unique morphologies... ");
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

    @Override
    public int process(ArrayList al, String... str) {
        System.out.println("PROFILING: " + "Checking for segmentation redundancies...");

        ArrayList<MicroObject> newObjects = new ArrayList<MicroObject>();
        ArrayList<Integer> morphology = getUniqueMorphology();

        ListIterator<MicroObject> itr = objects.listIterator();

        objectsProcessed = new ArrayList<MicroObject>();

        while (itr.hasNext()) {

            MicroObject obj = itr.next();
            Set<String> keys = obj.morphologicalLookup.keySet();
            String[] keysArr = new String[keys.size()];
            keysArr = keys.toArray(keysArr);

            //System.out.println("PROFILING: morphologies to add: " + keysArr.length);
            //System.out.println("PROFILING: keys to add: " + morphology.size());
            MicroObject newObject = new MicroObject();

            newObject.setSerialID((int) obj.getSerialID());
            newObject.setPixelsX(obj.getPixelsX());
            newObject.setPixelsY(obj.getPixelsY());
            newObject.setPixelsZ(obj.getPixelsZ());
            newObject.setCentroid();
            newObject.setGated(obj.getGated());
            //newObject.setThreshold(obj.getThresholdedMeanIntensity());
            for (int i = 0; i < morphology.size(); i++) {
                newObject.setMorphological(keysArr[morphology.get(i)], obj.getMorphPixelsX(morphology.get(i)),
                        obj.getMorphPixelsY(morphology.get(i)), obj.getMorphPixelsZ(morphology.get(i)));
            }
            newObjects.add(newObject);
        }
        objectsProcessed.addAll(newObjects);
        return 0;
    }

    @Override
    public String getChange() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public ArrayList<MicroObject> getObjects() {
        return objectsProcessed;
    }

}
