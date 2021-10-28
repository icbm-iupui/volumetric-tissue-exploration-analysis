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

import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Random;
import vtea.exploration.listeners.AddFeaturesListener;
import vteaobjects.MicroObject;

/**
 *
 * @author Seth
 */
public class PositionRandomization {
  

    ArrayList<Number> randomizedX;
    ArrayList<Number> randomizedY;
    ArrayList<Number> randomizedZ;
    
        int maxX;
        int maxY;
        int maxZ;
        
        int count;
    
    Roi pr;
    boolean roi = false;
    
    ArrayList<AddFeaturesListener> addfeaturelisteners = new ArrayList<AddFeaturesListener>();  
    
    public PositionRandomization(int c, ImagePlus imp){
        
        count = c;
        
        if(imp.getRoi() != null){
            roi = true;
            pr = imp.getRoi();
        }
        
        maxX = imp.getWidth();
        maxY = imp.getHeight();
        maxZ = imp.getNSlices();
        
        process();
    }
    

    
    private void process(){
        
        
        Random randX = new Random();
        Random randY = new Random();
        Random randZ = new Random();
        
        int nextRandomX = 0;
        int nextRandomY = 0;
        int nextRandomZ = 0;
                

        
        for(int i = 0; i < count; i++){
            
            nextRandomX = randX.nextInt(maxX);
            nextRandomY = randY.nextInt(maxY);
            nextRandomZ = randZ.nextInt(maxZ);
            
            //while()
      
            randomizedX.add(nextRandomX);
            randomizedY.add(nextRandomY);
            randomizedZ.add(nextRandomZ);
        }
        ArrayList<ArrayList<Number>> result = new ArrayList<>();
        
        result.add(randomizedX);
        notifyAddFeatureListener("PosX_Random", result);
        
        result = new ArrayList<>();
        result.add(randomizedY);
        notifyAddFeatureListener("PosY_Random", result);
        
        result = new ArrayList<>();
        result.add(randomizedZ);
        notifyAddFeatureListener("PosX_Random", result);
    }
            
    public void addFeatureListener(AddFeaturesListener listener) {
        addfeaturelisteners.add(listener);
    }

    public void notifyAddFeatureListener(String name,
            ArrayList<ArrayList<Number>> feature) {
        for (AddFeaturesListener listener : addfeaturelisteners) {
            listener.addFeatures(name, "Randomized Positions" , feature);
        }
    }
    
    
}
