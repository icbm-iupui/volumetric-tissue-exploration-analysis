/* 
 * Copyright (C) 2020 Indiana University
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
package vtea.objects.layercake;

import ij.*;
import ij.gui.*;
import java.awt.*;
import java.math.*;

//new class for defining a region object-reference by volume class
public class microRegion extends Object implements Cloneable, java.io.Serializable {

    /**
     * Constants
     */
    public static final int INCLUDED = 1;
    public static final int EXCLUDED = 2;
//    public static final int GROW = 0;  //use subtype to determine how much
//    public static final int SHRINK = 1;

    /**
     * Variables
     */
    private int[] x;		//x coordinates
    private int[] y;		//y coordinates
    
    private int[] xPerimeter;
    private int[] yPerimeter;
    private int nPerimeter;

    private int n = 0;			//number of pixels
    private int z;
    
    private boolean volumeMember = false;
    
//stack position, (0 to a)

    private String name;		//based on Roi name
    private int volume;		//volume membership if defined
    private int include = INCLUDED;		//flag for including region in analysis

//calculated variables
    private float mean = 0;
    private int nThreshold = 0;
    private float thresholdedmean = 0;
    private double thresholdformean = 0.8;
    private float thresholdedid = 0;
    private float integrated_density = 0;
    private double min = 0;
    private double max = 0;
    private double[] deviation;
    private double[] FeretValues = new double[5];  //1, maximum caliper width; 1 , FeretAngle; 3, minimum caliper width; 4, FeretX; 5, FeretY. 
    private Rectangle boundingRectangle;
    
    private float centroid_x = 0;
    private float centroid_y = 0;

    private int centerBoundX = 0;
    private int centerBoundY = 0;
    
    private int[][] imageArray;
    private int[][] maskArray;

    private Object[][] analysisResultsRegion;
//[channel][0, count, 1, mean, 2, integrated density, 3, min, 4, max, 5 standard deviation, 6 Feret_AR, 7 Feret_Min, 8 Feret_Max]

//calculated regions, 0 = GROW_1, 1 = GROW_2 results
    /**
     * Constructors
     */
//microRegion for one pixel array with delimiters
//public microRegion(int[] pixels, int[] defDimensions, int countDimensions){}
//microRegion from two arrays
    //empty microRegion
    public microRegion() {
    }
    
    public microRegion(int[] x, int[] y, int n, int z) {
        this.x = x;
        this.y = y;
        this.n = n;
        this.z = z;
        this.name = "r_" + z + x + y;
        this.calculatePerimeter();
        this.calculateCenter();
    }

//microRegion from two arrays and stack to calculate measurements
    public microRegion(int[] x, int[] y, int n, int z, ImageStack stack) {
        this.x = x;
        this.y = y;
        this.n = n;
        this.z = z;
        this.calculatePerimeter();
        this.calculateCenter();
        //this.calculateMeasurements(stack);
        this.name = "r_" + z + x + y; 
        //this.makeImageArray(new ImagePlus(this.name,stack));
    }


    /**
     * Methods
     */
//calculate constants
//get analysis variables
//add individual pixels
    public void addPixels(int x, int y) {
        this.x[this.n] = x;
        this.y[this.n] = y;
        this.n++;
    }

//assign and retrieve volume membership
    public void setMembership(int volume) {
        this.volume = volume;
        this.volumeMember = true;
    }
    
    public void setAMember(boolean value){
        this.volumeMember = value;
    }
    
    public boolean isAMember(){
        return this.volumeMember;
    }

    public int getMembership() {
        return this.volume;
    }

    public void setInclude() {
        this.include = INCLUDED;
    }

    public void setExclude() {
        this.include = EXCLUDED;
    }
    
    private void makeImageArray(ImagePlus imp){

        PolygonRoi polygon = new PolygonRoi(x, y, n-1, Roi.FREEROI);
        Rectangle bounds = polygon.getBounds();
        ImagePlus result;  
        result = new ImagePlus(this.name, imp.getStack().getProcessor(z));
        result.setRoi(bounds);
        result.getProcessor().crop();
        result.show();
    }
//calculation methods
    public void calculateMeasurements(ImageStack stack) {
        int[] x = this.x;
        int[] y = this.y;
        z = this.z;
        int n = this.n;

        long total = 0;
        double min = 0;
        double max = 0;
        double[] deviation = new double[n];
        
        for (int i = 0; i <= n - 1; i++) {
            total = total + (long) stack.getVoxel(x[i], y[i], z);
            if (stack.getVoxel(x[i], y[i], z) < min) {
                min = stack.getVoxel(x[i], y[i], z);
            }
            if (stack.getVoxel(x[i], y[i], z) > max) {
                max = stack.getVoxel(x[i], y[i], z);
            }
            deviation[i] = stack.getVoxel(x[i], y[i], z);
        }

        this.deviation = deviation;
        this.max = max;
        this.min = min;
        this.mean = total / n;
        this.integrated_density = total;
        
        total = 0;
        int thresholdcount = 0;
        
        for(int j = 0; j <= n - 1; j++) {
            if (stack.getVoxel(x[j], y[j], z) > max*this.thresholdformean){
                total = total + (long) stack.getVoxel(x[j], y[j], z);
                thresholdcount++;
            }  
        }    
        this.thresholdedid = total;
        this.nThreshold = thresholdcount;
        this.thresholdedmean = total/thresholdcount;

    }

    private void calculateCenter() {

        int[] x = this.x;
        int[] y = this.y;
        int n = this.n;

        double[] FeretValues = new double[5]; //1, maximum caliper width; 1 , FeretAngle; 3, minimum caliper width; 4, FeretX; 5, FeretY. 

        PolygonRoi polygon = new PolygonRoi(x, y, n, Roi.FREEROI);

        Rectangle bounds = polygon.getBounds();

        int xCenter = (int) (bounds.getWidth()) / 2;
        int yCenter = (int) (bounds.getHeight()) / 2;

        FeretValues = polygon.getFeretValues();

        this.FeretValues = FeretValues;
        this.centerBoundX = xCenter + bounds.x;
        this.centerBoundY = yCenter + bounds.y;
        this.boundingRectangle = bounds;

    }
    
    private void calculatePerimeter() {

        //no neighbor as criteria with 8-connected
        int[] xRegion = this.x;
        int[] yRegion = this.y;
        int nPoints = this.n;

        //IJ.log("microRegion::calculatePerimeter                starting...");
        //IJ.log("microRegion::calculatePerimeter                "+ nPoints + " pixels to test");
        int xCurrent = 0;
        int yCurrent = 0;
        int xTest = 0;
        int yTest = 0;

        int[] xPerimeter = new int[nPoints];
        int[] yPerimeter = new int[nPoints];
        int nPerimeter = 1;

        boolean NW = false;
        boolean N = false;
        boolean NE = false;
        boolean E = false;
        boolean SE = false;
        boolean S = false;
        boolean SW = false;
        boolean W = false;

        for (int n = 0; n <= nPoints - 1; n++) {
            xCurrent = xRegion[n];
            yCurrent = yRegion[n];
            for (int m = 0; m <= nPoints - 1; m++) {

                xTest = xRegion[m];
                yTest = yRegion[m];
                //IJ.log("                           Testing... " + xTest + " ," + yTest + ", against " + xCurrent + " ," + yCurrent);	
                //true is when a point in the array is there
                //false is when a point is not in the array

                if (xCurrent - 1 == xTest) {
                    if (yCurrent - 1 == yTest) {
                        NW = (true || NW);
                    }
                }
                if (xCurrent == xTest) {
                    if (yCurrent - 1 == yTest) {
                        N = (true || N);
                    }
                }
                if (xCurrent + 1 == xTest) {
                    if (yCurrent - 1 == yTest) {
                        NE = (true || NE);
                    }
                }
                if (xCurrent + 1 == xTest) {
                    if (yCurrent == yTest) {
                        E = (true || E);
                    }
                }
                if (xCurrent + 1 == xTest) {
                    if (yCurrent + 1 == yTest) {
                        SE = (true || SE);
                    }
                }
                if (xCurrent == xTest) {
                    if (yCurrent + 1 == yTest) {
                        S = (true || S);
                    }
                }
                if (xCurrent - 1 == xTest) {
                    if (yCurrent + 1 == yTest) {
                        SW = (true || SW);
                    }
                }
                if (xCurrent - 1 == xTest) {
                    if (yCurrent == yTest) {
                        W = (true || W);
                    }
                }
            }

            NW = !(NW);
            N = !(N);
            NE = !(NE);
            E = !(E);
            SE = !(SE);
            S = !(S);
            SW = !(SW);
            W = !(W);
            //the Not effectively makes the combined positions for all checked pixels a NOR, or if any pixel is false, return true.

            if ((NW || N || NE || E || SE || S || SW || W)) {
                xPerimeter[nPerimeter - 1] = xCurrent;
                yPerimeter[nPerimeter - 1] = yCurrent;
                nPerimeter++;
                //IJ.log("                              Found edge pixel...  " + xCurrent + " ," + yCurrent); 		
            }
            NW = false;
            N = false;
            NE = false;
            E = false;
            SE = false;
            S = false;
            SW = false;
            W = false;
        }

        this.xPerimeter = xPerimeter;
        this.yPerimeter = yPerimeter;
        this.nPerimeter = nPerimeter - 1;

        //IJ.log("                               Total perimeter...  " + (nPerimeter-1)); 
    }

    private boolean containsPoint(int x1, int y1, int[] x, int[] y, int n) {

        for (int i = 0; i <= n; i++) {
            if (x1 == x[n]) {
                if (y1 == y[n]) {
                    return true;
                }
            }
        }

        return false;
    }

//calculated values
    public float getMean() {
        return this.mean;
    }

    public double[] getFeretValues() {
        return this.FeretValues;
    }

    private int nCombinations(int n, int r) {

        BigInteger top = BigInteger.ONE;
        BigInteger bottom;
        BigInteger bottom1 = BigInteger.ONE;
        BigInteger bottom2 = BigInteger.ONE;
        BigInteger result;

        for (int i = 1; i <= n; i++) {
            top = top.multiply(BigInteger.valueOf(i));
        }

        for (int i = 1; i <= n - r; i++) {
            bottom1 = bottom1.multiply(BigInteger.valueOf(i));
        }

        for (int i = 1; i <= r; i++) {
            bottom2 = bottom2.multiply(BigInteger.valueOf(i));
        }

        bottom = bottom1.multiply(bottom2);

        result = top.divide(bottom);

        return result.intValue();

    }

    private int factorial(int n) {

        BigInteger result = BigInteger.ONE;
        for (int i = 1; i <= n; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }
        return result.intValue();
    }

//field retrival methods
    public void setName(String str){
        name = str;
    }
    
    public String setName(){
        return name;
    }
    
    
    public int isRegionIncluded() {
        return this.include;
    }

    public int getPixelCount() {
        return this.n;
    }
    
    public int getThresholdPixelCount() {
        return this.nThreshold;
    }

    public int[] getPixelsX() {
        return this.x;
    }

    public int[] getPixelsY() {
        return this.y;
    }

    public int getZPosition() {
        return this.z;
    }

    public String getName() {
        return this.name;
    }

    public float getCentroidX() {
        return this.centroid_x;
    }

    public float getCentroidY() {
        return this.centroid_y;
    }

    public int getBoundCenterX() {
        return this.centerBoundX;
    }

    public int getBoundCenterY() {
        return this.centerBoundY;
    }

    public double getMaxIntensity() {
        return this.max;
    }

    public double getMinIntensity() {
        return this.min;
    }

    public double getIntegratedIntensity() {
        return this.integrated_density;
    }

    public double getMeanIntensity() {
        return this.mean;
    }
    
    public double[] getDeviations() {
        return this.deviation;
    }
    
    public double getThresholdedIntegratedIntensity() {
        return this.thresholdedid;
    }
    
    public double getThresholdedMeanIntensity() {
        return this.thresholdedmean;
    }
    public void setThreshold(double threshold) {
        this.thresholdformean = threshold;
    }
  
    public Rectangle getBoundingRectangle() {return this.boundingRectangle;}
    
}

