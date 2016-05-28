/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 */
public interface MicroObjectModel {

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
}
