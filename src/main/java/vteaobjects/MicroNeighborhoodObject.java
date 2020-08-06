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

/**
 *
 * @author sethwinfree
 */
public class MicroNeighborhoodObject extends MicroObject {
    
    int[] classes;
    int neighborhoodSize;
    ArrayList<MicroObject> objects;
    double[] centroid;
    
    //calculated
    
    double[][] meanDistances;
    double stDevDistances;
    double rangeDistances;
    
    
    
    
public MicroNeighborhoodObject(ArrayList<MicroObject> o, double[] c){
    
    super();
    //make a combined x, y, z arrays
    //maintain sub arrays per object for reference
    //add the reference key for the dataset from which the objects originated
    //add an arraylist that references the original objectserial IDs
    
    
    
}
    

private void makeCombinedArrays(){
    

}

private void makeOutline(){
    
}

private void calculateNeighborhoodValues(){
    
}


}
