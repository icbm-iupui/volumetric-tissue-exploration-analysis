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
import javax.swing.JComponent;
import vtea.exploration.listeners.AddFeaturesListener;
import vteaexploration.ProgressTracker;

/**
 *
 * @author Seth
 */
public class ClassRandomization extends JComponent {
    
    ArrayList<ArrayList<Number>> classes;
    ArrayList<Number> randomizedClasses;
    
    ArrayList<AddFeaturesListener> addfeaturelisteners = new ArrayList<AddFeaturesListener>();
    
    public ClassRandomization(ArrayList<ArrayList<Number>> cl){
        classes = cl;
        randomizedClasses = new ArrayList<Number>();
    }
    
    public void process() {
       Random rand = new Random();
       //need to catch no "Assigned" feature here
        int nextRandom;
        int start = classes.size();
        ProgressTracker tracker = new ProgressTracker();
        double progress = 0;
        
         //System.out.println("PROFILING: Object classes to randomize:  " + classes.size());
         
        tracker.createandshowGUI("Randomize postions...", 0, 0);
        addPropertyChangeListener(tracker);
        
        while(!classes.isEmpty())
        {
            progress = 100 * ((double) classes.size()/ (double) start);
            firePropertyChange("method", "", "Randomizing positions");
            firePropertyChange("progress", "Randomizing...", (int) progress);
            
            nextRandom = rand.nextInt(classes.size());
            ArrayList<Number>  c = classes.get(nextRandom);
            randomizedClasses.add(c.get(0));
            classes.remove(nextRandom);
            //System.out.println("PROFILING: Reassigning object " + nextRandom);
        }  
        
        ArrayList result = new ArrayList();
        result.add(randomizedClasses);
        
        notifyAddFeatureListener("Assigned_Random_" + vtea._vtea.COUNTRANDOM, result);
         tracker.setVisible(false);
        vtea._vtea.COUNTRANDOM++;
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
