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
package vtea.exploration.util;

import java.util.ArrayList;
import java.util.Random;
import vtea.exploration.listeners.AddFeaturesListener;

/**
 *
 * @author Seth
 */
public class ClassRandomization {
    
    ArrayList<Number> classes;
    ArrayList<Number> randomizedClasses;
    
    ArrayList<AddFeaturesListener> addfeaturelisteners = new ArrayList<AddFeaturesListener>();
    
    public ClassRandomization(ArrayList<Number> cl){
        classes = cl;
        process();
    }
    
    private void process() {
       Random rand = new Random();
       //need to catch no "Assigned" feature here
        int nextRandom = rand.nextInt(classes.size());
        while(!classes.isEmpty())
        {
            randomizedClasses.add(classes.get(nextRandom));
            classes.remove(nextRandom);
        }
        
        ArrayList<ArrayList<Number>> result = new ArrayList<>();
        
        result.add(randomizedClasses);
        
        notifyAddFeatureListener("Assigned_Random", result);
    }
    
    public void addFeatureListener(AddFeaturesListener listener) {
        addfeaturelisteners.add(listener);
    }

    public void notifyAddFeatureListener(String name,
            ArrayList<ArrayList<Number>> feature) {
        for (AddFeaturesListener listener : addfeaturelisteners) {
            listener.addFeatures(name, "Randomized Class" , feature);
        }
    }
}
