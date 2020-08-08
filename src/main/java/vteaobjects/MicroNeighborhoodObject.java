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
package vteaobjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;

/**
 *
 * @author sethwinfree
 */
public class MicroNeighborhoodObject extends MicroObject {
    
    double[] classes;
    ArrayList<MicroObject> objects;
    double[] centroids;
    int neighborhoodSize;
    
    String key;
    
    //calculated
    
    double[] meanDistances;
    double stDevDistances;
    double rangeDistances;
    
    
    
    
    
    
public MicroNeighborhoodObject(ArrayList<MicroObject> o, double[] c, String k){
    
    super();
    //make a combined x, y, z arrays
    //maintain sub arrays per object for reference
    //add the reference key for the dataset from which the objects originated
    //add an arraylist that references the original objectserial IDs
    key = k;
    objects = o;
    centroids = c;
    
    neighborhoodSize = objects.size();
    
    

}

public double[] getClasses(){
    return classes;
    }

public ArrayList<MicroObject> getObjects(){
    return objects;
    }

public double[] getCentroids(){
    return centroids;
    }

public int getNeighborhoodSize(){
    return neighborhoodSize;
}


//method to get outline

//method to get all pixels


private void makeCombinedArrays(){
    
    ListIterator<MicroObject> itrObj = objects.listIterator();
    

    
    while(itrObj.hasNext()){
       
        MicroObject obj = itrObj.next();       
        obj.getPixelsX();
        obj.getPixelsY();
        obj.getPixelsZ();
    }
    
//    x = new int[xCombined.size()];
//    y = new int[xCombined.size()];
//    z = new int[xCombined.size()];
//   
//    for(int i = 0; i < xCombined.size(); i++){
//        x[i] = xCombined.get(i);
//    }
    
}

//public static void arraycopy(Object source, int source_position,   
//Object destination, int destination_position, int length)  

private int[] concatenateArray(int[] start, int[] end){
    
    int length = start.length + end.length;
    
    int[] result = new int[length];
    
    System.arraycopy(start,0,result,0,start.length);
    System.arraycopy(end,0,result,start.length,end.length);
    
    return result;
}

private void makeOutline(){
    
}

private void calculateNeighborhoodValues(){
    
}


}
