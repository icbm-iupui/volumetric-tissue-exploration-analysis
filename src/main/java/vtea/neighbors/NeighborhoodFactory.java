/*
 * Copyright (C) 2020 SciJava
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
package vtea.neighbors;

import ij.ImagePlus;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import vtea.processor.ExplorerProcessor;
import vtea.processor.NeighborhoodMeasurementsProcessor;
import vteaexploration.ProgressTracker;
import vteaobjects.MicroNeighborhoodObject;
import vteaobjects.MicroObject;

/**
 *
 * @author sethwinfree
 */
public class NeighborhoodFactory implements PropertyChangeListener {
    
private ArrayList<NeighborhoodMeasurementsProcessor> neighborhoodProcessors = new ArrayList();
String key;
String parentKey;
ArrayList<MicroNeighborhoodObject> objects;
ArrayList<Integer> classes;
HashMap<String, String> objectClasses;
ImagePlus image;
ProgressTracker progress;

    
public void makeNeighborhoodAnalysis(ImagePlus imp, String k, String parentk, 
        ArrayList<MicroNeighborhoodObject> obj, ArrayList<Integer> c, HashMap<String, String> v, ProgressTracker pt){
   
   image = imp;
   key = k;
   parentKey = parentk;
   objects = obj;
   classes = c;
   objectClasses = v;
   progress = pt;
   
   
   
   NeighborhoodMeasurementsProcessor nmp = new NeighborhoodMeasurementsProcessor(k,obj,c,v);
   neighborhoodProcessors.add(nmp);
   nmp.addPropertyChangeListener(this);
   nmp.execute();
}  

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals("neighborhoodMeasurementDone"))
        {
        NeighborhoodMeasurementsProcessor nmp = neighborhoodProcessors.get(neighborhoodProcessors.size()-1);
        //System.out.println("PROFILING: Found " + objects.size() + " neighborhoods, with " + nmp.getFeatures().size() + " measurements.");
   
           ExplorerProcessor ep = new ExplorerProcessor(key, parentKey, image, objects, nmp.getFeatures(), 
           nmp.getDescriptions(), nmp.getDescriptionLabels(), new ArrayList());
           ep.execute();
        }
        if (evt.getPropertyName().equals("progress")) {
            int p = (Integer) evt.getNewValue();
            progress.setPercentProgress(p);
             progress.setTextProgress(String.format(
                    "Completed %d%%...\n", p));

        }
    }
}
