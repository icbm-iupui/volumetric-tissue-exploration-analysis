/* 
 * Copyright (C) 2016 Indiana University
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

import ij.ImageStack;
import java.awt.Rectangle;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author vinfrais
 * @param <T>
 */
public interface MicroObjectModel <T extends Number> {
    
    //arraylist of points, ordered, not limit on dimensionality
    
    //are points just different non-native types?
    
    //way to access this table
    
    //
    
    public void makeDerivedRegions(int[][] derivedRegionType, int channels, ImageStack[] Stacks, ArrayList ResultsPointers);
    
    public void calculateDerivedObjectMeasurements(int channel, ImageStack is);
    
    public void calculateObjectMeasurments();
    
    public int[][] getDerivedObjectConstants();
    
    public ArrayList getObjectPixels();
    
    //calculated values
    public float getMean();

    public double[] getFeretValues();

    public int getPixelCount();
    
    public int getThresholdPixelCount();

    public int[] getPixelsX();

    public int[] getPixelsY();
    
    public int[] getPixelsZ();

    public float getCentroidX();

    public float getCentroidY();
    
    public float getCentroidZ();

    public int getBoundCenterX();

    public int getBoundCenterY();
    
    public int[] getBoundsCenter();

    public double getMaxIntensity();

    public double getMinIntensity();

    public double getIntegratedIntensity();

    public double getMeanIntensity();
    
    public double[] getDeviations();
    
    public double getThresholdedIntegratedIntensity();
    
    public double getThresholdedMeanIntensity();
    
    public void setThreshold(double threshold);
  
    public Rectangle getBoundingRectangle();
    
    public ArrayList getResultPointer();
    
    public Object[] getAnalysisMaskVolume();
    
    public Object[][] getAnalysisResultsVolume();
    
    public int getSerialID();
    
    public List getRegions();
    
    public int[] getXPixelsInRegion(int i);
    
    public int[] getYPixelsInRegion(int i);
    
    public void setGated(boolean b);
    
    public boolean getGated();
    
    public void setColor(int c);
    
    public int getColor();
    
    public void setMorphological(String method_UID, ArrayList<Integer> x, ArrayList<Integer> y, ArrayList<Integer> z); 
    

   
   
}
