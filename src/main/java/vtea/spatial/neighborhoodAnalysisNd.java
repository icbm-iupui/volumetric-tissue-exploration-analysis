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
package vtea.spatial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import smile.neighbor.Neighbor;
import vteaobjects.MicroObject;

/**
 *
 * @author sethwinfree
 */
public class neighborhoodAnalysisNd {
    
    ArrayList<ArrayList<Neighbor>> neighborhoods;
    ArrayList<ArrayList> clonedObjects = new ArrayList<ArrayList>();
    ArrayList<ArrayList<String>> descriptions = new ArrayList<ArrayList<String>>();
    
    
    
    public neighborhoodAnalysisNd(ArrayList<ArrayList<Neighbor>> n, ArrayList<ArrayList> cloned, ArrayList<ArrayList<String>> d){
        
        neighborhoods = n;
        clonedObjects = cloned;
        descriptions = d;

    }
    
    public void makeNeighborhoodObjects(){
        
        ArrayList<MicroObject> objects = clonedObjects.get(0);
        ArrayList<ArrayList<Number>> measurements = clonedObjects.get(1);
        ArrayList<String> labels = descriptions.get(0);
        ArrayList<String> labelsExtended = descriptions.get(1);
        
        Hashtable ObjectIDMap = new Hashtable();
        
        for(int i = 0; i < objects.size(); i++){            
            MicroObject obj = objects.get(i);
            ObjectIDMap.put(i, obj.getSerialID());           
        }
        
        for(int j = 0; j < neighborhoods.size(); j++){
            
            ArrayList<Neighbor> neighborhood = neighborhoods.get(j);
            
            for (int k = 0; k < neighborhood.size(); k++){               
                Neighbor n = neighborhood.get(k);
                objects.get((int)ObjectIDMap.get((int)n.key));        
            } 
        }
    } 
}
