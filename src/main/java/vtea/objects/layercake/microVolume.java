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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;
import vteaobjects.MicroObject;
import vteaobjects.MicroObjectModel;

//new class for defining a cell object for microFLOW analysis
public class microVolume extends MicroObject implements MicroObjectModel, Cloneable, java.io.Serializable {

    public static final int INCLUDED = 1;
    public static final int EXCLUDED = 2;

    public static final int X_VALUES = 1;
    public static final int Y_VALUES = 2;

    public static final int MASK = 0;
    public static final int GROW = 1;  //use subtype to determine how much
    public static final int FILL = 2;
    static public String[] Analytics = {"#pixels", "mean", "sum", "min", "max", "SD", "AR", "F_min", "F_max", "mean_th", "mean_sq"};
    static int serialCounter = 0;

    private int x_centroid;    //center x_centroid
    private int y_centroid;    //center y_centroid
    private int z_centroid;    //center z_centroid
    private int n;    //total pixels
    private String name;
    private int nChannels;
    private int nRegions = 0;		//number of member regions
    private int nDerivedRegions = 0;	//number of member derived regions by array location

//regions for the mask volume
    private microRegion[] Regions;
    private List<microRegion> alRegions = new CopyOnWriteArrayList<microRegion>();

//derived regions for all dependent channels,  
    private microDerivedRegion[][] DerivedRegions;

    private float mean = 0;
    private float integrated_density = 0;
    private double min = 0;
    private double max = 0;
    private float stdev = 0;
    private double FeretMaxCaliperMax = 0;
    private double FeretMinCaliperMax = 0;
    private double FeretAspectRatio = 0;
    private int[][] derivedConstants;
    private Object[][] analysisResultsVolume = new Object[6][11];
    private Object[] analysisMaskVolume = new Object[11];

    private Color[][] Colorized = new Color[4][9];
    private ArrayList ResultsPointer;

    microVolume Original;

    int serialID = 0;

    private boolean gated = false;

    //   Volume measurements; 297, 1663.5555, 558321, 0.0, 4095.0
//[channel][0, count, 1, mean, 2, integrated density, 3, min, 4, max, 5 standard deviation, 6 Feret_AR, 7 Feret_Min, 8 Feret_Max]
//derived regions for regions, 2D array, [Regions][Derived Regions],  or each region may have different derived regions.
//use GROW_1 and GROW_2 for intial lookup of Derived Regions-add analyses as required.  nDerived is the counter
    /**
     * Default constructor
     */
    public microVolume() {
        super();
    }


    //Extract pixels array for MicroObject requirements
    public void makePixelArrays() {

        ArrayList<Integer> xAl = new ArrayList();
        ArrayList<Integer> yAl = new ArrayList();
        ArrayList<Integer> zAl = new ArrayList();

        ListIterator<microRegion> itr = alRegions.listIterator();

        while (itr.hasNext()) {
            microRegion region = itr.next();
            int[] xR = region.getPixelsX();
            int[] yR = region.getPixelsY();
            int[] zR = new int[xR.length];

            int z_position = region.getZPosition();

            for (int i = 0; i < xR.length; i++) {
                zAl.add(z_position);
                xAl.add(xR[i]);
                yAl.add(yR[i]);
            }

        }

        z = new int[xAl.size()];
        y = new int[xAl.size()];
        x = new int[xAl.size()];

        for (int j = 0; j < xAl.size(); j++) {
            z[j] = zAl.get(j);
            x[j] = xAl.get(j);
            y[j] = yAl.get(j);
        }
    }

//region manipulation
    public void addRegion(int[] x, int[] y, int n, int z) {
        this.Regions[this.nRegions + 1] = new microRegion(x, y, n, z);
        this.nRegions++;
    }

    public void addRegion(microRegion Region) {
        this.alRegions.add(Region);
        this.nRegions++;
    }

    public void addRegions(List<microRegion> regions) {
        alRegions.addAll(regions);
    }

    public void setRegion(microRegion Region, int nRegions) {
        this.Regions[nRegions] = Region;
    }

    public List getRegions() {
        return this.alRegions;
    }

    public microRegion[] getRegionsAsArray() {
        List<microRegion> ls = getRegions();
        return ls.toArray(Regions);
    }

    public ArrayList getDRegions(int channel) {
        ArrayList<microDerivedRegion> mral = new ArrayList<microDerivedRegion>();

        for (int i = 0; i <= nDerivedRegions - 1; i++) {
            mral.add(DerivedRegions[channel][i]);
        }
        return mral;
    }

    public microDerivedRegion[][] getDRegions() {
        return this.DerivedRegions;
    }

    public void setDerivedRegions(microDerivedRegion[][] mdr) {
        this.DerivedRegions = mdr;
    }

    public int getNDRegions() {
        return nDerivedRegions;
    }

//Analysis functions
    public int getNRegions() {
        return alRegions.size();
    }

//    public float getMean() {
//        return this.mean;
//    }
//
//    public float getIntDen() {
//        return this.integrated_density;
//    }
//
//    public double getFeretMax() {
//        return this.FeretMaxCaliperMax;
//    }
//
//    public double getFeretMin() {
//        return this.FeretMinCaliperMax;
//    }
//
//    public double getFeretAR() {
//        return this.FeretAspectRatio;
//    }
//
//    public int[][] getderivedConstants() {
//        return this.derivedConstants;
//    }
//
//    public double getMax() {
//        return (Double)analysisMaskVolume[4];
//    }
//    public int getPixelCount() {
//        return this.n;
//    }
//    public int getNChannels() {
//        return this.nChannels;
//    }
//
//    public double getMin() {
//        return this.min;
//    }
//    public ArrayList getVolumePixels(int dim) {
//        int countRegion = this.nRegions;
//        int countPixel;
//        //microRegion[] localRegions = this.Regions;
//        microRegion[] localRegions = getRegionsAsArray();
//        //int[] pixels = new int[1];  
//        int[] pixels;
//        ArrayList Dpixels = new ArrayList();
//
//        switch (dim) {
//
//            case X_VALUES:
//
//                for (int c = 0; c <= countRegion; c++) {
//                    countPixel = localRegions[c].getPixelCount();
//                    pixels = localRegions[c].getPixelsX();
//
//                    for (int m = 0; m <= countPixel; m++) {
//                        Dpixels.add(pixels[m]);
//                    }
//                }
//                return Dpixels;
//
//            case Y_VALUES:
//
//                for (int d = 0; d <= countRegion; d++) {
//                    countPixel = localRegions[d].getPixelCount();
//                    pixels = localRegions[d].getPixelsY();
//
//                    for (int m = 0; m <= countPixel; m++) {
//                        Dpixels.add(pixels[m]);
//                    }
//                }
//                return Dpixels;
//
//            default:
//                return Dpixels;
//        }
//    }
//    public int getParticleCount(microRegion Region) {
//        return Region.getPixelCount();
//    }
//    @Override
//    public Object[][] getAnalysisResultsVolume() {
//        return this.analysisResultsVolume;
//    }
//
//    @Override
//    public Object[] getAnalysisMaskVolume() {
//        return this.analysisMaskVolume;
//    }
//
//    public ArrayList getResultPointer() {
//        return this.ResultsPointer;
//    }
//    public Color getAnalyticColor(int channel, int analytic) {
//        try{return Colorized[channel][analytic];}
//        catch(NullPointerException np){}
//        return Color.BLACK;
//    }
//    
//    public void setAnalyticColor(Color clr,int channel, int analytic){
//        this.Colorized[channel][analytic] = clr;
//    }
//    public void setName(String str){
//        name = str;
//    }
//    
//    public String getName(){
//        return name;
//    }
//    public int[] getBoundsCenter(){
//        int[] center = new int[2];
//        center[0] = (Integer)this.x_centroid;
//        center[1] = (Integer)this.y_centroid;
//        return center;
//    }
//
//    @Override
//    public void calculateDerivedObjectMeasurements(int channel, ImageStack is) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public void calculateObjectMeasurments() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public int[][] getDerivedObjectConstants() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public ArrayList getObjectPixels() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public double[] getFeretValues() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public int getThresholdPixelCount() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public int[] getPixelsX() {
//        return x;
//    }
//
//    @Override
//    public int[] getPixelsY() {
//        return y;
//     }
//    
//        @Override
//    public int[] getPixelsZ() {
//        return z;
//     }
//
//    @Override
//    public float getCentroidX() {
//         try{
//        return x_centroid;
//        } catch(NullPointerException e){return -1;}
//    }
//    
//    @Override
//    public float getCentroidY() {
//           try{
//        return y_centroid;
//        } catch(NullPointerException e){return -1;}
//    }
//    
//    @Override
//    public float getCentroidZ() {
//        try{
//        return z_centroid;
//        } catch(NullPointerException e){return -1;}
//    }
//
//    @Override
//    public int getBoundCenterX() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public int getBoundCenterY() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public double getMaxIntensity() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public double getMinIntensity() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public double getIntegratedIntensity() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public double getMeanIntensity() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public double[] getDeviations() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public double getThresholdedIntegratedIntensity() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public double getThresholdedMeanIntensity() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public void setThreshold(double threshold) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public Rectangle getBoundingRectangle() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public double getSerialID() {
//       return serialID;
//    }
//     @Override
//    public int[] getXPixelsInRegion(int i) {
//        List regions = getRegions();
//                
//                //have microobject return the slice specific arrays of pixels
//            
//                ListIterator<microRegion> ritr = regions.listIterator(); 
//                while (ritr.hasNext()) {
//                    microRegion region = ritr.next();
//
//                    if (region.getZPosition() == i) {
//                          return region.getPixelsX();
//                    }
//                }
//            return null;    
//    }
//
//    @Override
//    public int[] getYPixelsInRegion(int i) {
//        List regions = getRegions();
//                
//                //have microobject return the slice specific arrays of pixels
//            
//                ListIterator<microRegion> ritr = regions.listIterator(); 
//                while (ritr.hasNext()) {
//                    microRegion region = ritr.next();
//
//                    if (region.getZPosition() == i) {
//                          return region.getPixelsY();
//                    }
//                }
//                return null;
//    }
};
