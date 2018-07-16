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
package vtea.objects.layercake;

import ij.*;
import ij.gui.ImageRoi;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.lang.Integer;
import vteaobjects.MicroObject;
import vteaobjects.MicroObjectModel;
//

//new class for defining a cell object for microFLOW analysis
public class microVolume extends MicroObject implements MicroObjectModel, Cloneable, java.io.Serializable {

    public static final int INCLUDED = 1;
    public static final int EXCLUDED = 2;

    public static final int X_VALUES = 1;
    public static final int Y_VALUES = 2;
    
    public static final int MASK = 0;
    public static final int GROW = 1;  //use subtype to determine how much
    public static final int FILL = 2;

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

    static public String[] Analytics = {"#pixels", "mean", "sum", "min", "max", "SD", "AR", "F_min", "F_max", "mean_th", "mean_sq"};
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

    public void makeDerivedRegions(int[][] derivedRegionType, int channels, ImageStack[] Stacks, ArrayList ResultsPointers) {
        derivedConstants = derivedRegionType;
        this.nChannels = channels;
        
        analysisResultsVolume = new Object[nChannels][11];
        
        DerivedRegions = new microDerivedRegion[nChannels][alRegions.size()];
        this.ResultsPointer = ResultsPointers;
        
        for (int i = 0; i < nChannels; i++) {
            
            switch (derivedRegionType[i][0]) {
                case microVolume.GROW:
                    calculateGrow(i, derivedRegionType[i][1], Stacks[i]);
                    calculateDerivedVolumeMeasurements(i);
                    break;
                case microVolume.MASK:
                    calculateMask(i, Stacks[i]);
                    calculateDerivedVolumeMeasurements(i);
                    break;
                case microVolume.FILL:
                    calculateFill();
                    break;
                default:
                    break;
            }
        }
    }
    
    public void setObjectID(int id){
        serialID = id;
    }
  
    public void calculateAllDerivedVolumeMeasurements(){   
        for(int i = 0; i < this.nChannels; i++){
            calculateDerivedVolumeMeasurements(i);
        } 
    }
    
    public void calculateDerivedVolumeMeasurements(int Channel) {
        int countPixels = 0;
        long total = 0;
        long totalThreshold = 0;
        int nThreshold = 0; 
        double minLocal = 0;//why doubles?
        double maxLocal = 0;//why doubles?
        double standardDeviation = 0;
        double[] FeretValues = new double[5];
        double meanFeretAR = 0;
        double meanFeretMaxCaliperLocal = 0;
        double meanFeretMinCaliperLocal = 0;
        
        ArrayList<microDerivedRegion> regions = new ArrayList<microDerivedRegion>();

        for(int i = 0; i < nRegions; i++){
            regions.add(DerivedRegions[Channel][i]);
        }
        
        ListIterator<microDerivedRegion> itr = regions.listIterator();

        while(itr.hasNext()){   
            
            microRegion region = new microDerivedRegion();
            
            region = itr.next();
            
            //region.calculateMeasurements(RegionFactory.stackResult);
            
            try{

                FeretValues = region.getFeretValues();


                double[] deviations = region.getDeviations();

                meanFeretAR = meanFeretAR + (FeretValues[0]) / (FeretValues[2]);
                meanFeretMaxCaliperLocal = meanFeretMaxCaliperLocal + FeretValues[0];
                meanFeretMinCaliperLocal = meanFeretMinCaliperLocal + FeretValues[2];
                countPixels = countPixels + region.getPixelCount();
                total = total + (long) region.getIntegratedIntensity();
                mean = mean + (long) region.getMeanIntensity();

                if (region.getMinIntensity() < minLocal) {
                    minLocal = region.getMinIntensity();
                }
                if (region.getMaxIntensity() > maxLocal) {
                    maxLocal = region.getMaxIntensity();
                }
                for(int j = 0; j <= region.getPixelCount()-1; j++){
                    standardDeviation = standardDeviation + Math.pow(deviations[j]-(total/countPixels), 2);
                }
                region.setThreshold(0.90*maxLocal);
                totalThreshold = totalThreshold + (long) region.getThresholdedIntegratedIntensity();
                nThreshold = nThreshold + region.getThresholdPixelCount();


                this.n = countPixels;
            } catch(NullPointerException e){ System.out.println("Yikes, where did the derived region values go? " + e);
            }
        }
        
        analysisResultsVolume[Channel][0] = countPixels;
        
        if(countPixels == 0){analysisResultsVolume[Channel][1] = 0;}
        else{
        analysisResultsVolume[Channel][1] = total/countPixels; //changed from averaging regions to all pixels
        }
        
        analysisResultsVolume[Channel][2] = total;
        analysisResultsVolume[Channel][3] = minLocal;
        analysisResultsVolume[Channel][4] = maxLocal;
        analysisResultsVolume[Channel][5] = Math.sqrt(standardDeviation/countPixels);
        analysisResultsVolume[Channel][8] = meanFeretMaxCaliperLocal / (nRegions);
        analysisResultsVolume[Channel][7] = meanFeretMinCaliperLocal / (nRegions);
        //analysisResultsVolume[Channel][9] = (total/countPixels)*(total/countPixels);
        if(nThreshold > 0){
            analysisResultsVolume[Channel][9] = totalThreshold/nThreshold;
        } else {
            analysisResultsVolume[Channel][9] = totalThreshold;   
        }
        if(countPixels == 0){analysisResultsVolume[Channel][10] = 0;
        }else{
                    analysisResultsVolume[Channel][10] = (total/countPixels)*(total/countPixels);
        }
       
        

        if (meanFeretMinCaliperLocal / (nRegions) != 0) {
            analysisMaskVolume[6] = (meanFeretMaxCaliperLocal / (nRegions)) / (meanFeretMinCaliperLocal / (nRegions));
        }

        //IJ.log("microVolume Derived Volume measurements: " + analysisResultsVolume[Channel][1] + ", " + analysisResultsVolume[Channel][2] + ", " + analysisResultsVolume[Channel][3] + ", " + analysisResultsVolume[Channel][4] + ", " + analysisResultsVolume[Channel][5] + ", " + analysisResultsVolume[Channel][6]);
        //System.out.println("Analyzing volume: " + this.getName() + " ID " + analysisResultsVolume[Channel][2]);
    }
    
    public void calculateVolumeMeasurements() {
        
        int nRegionsLocal = this.alRegions.size();
        //microRegion[] RegionsLocal = this.alRegions.toArray(Regions);

        int countPixels = 0;
        int countPixelsThreshold = 0;

        long total = 0;
        long totalThreshold = 0;
        double minLocal = 0;//why doubles?
        double maxLocal = 0;//why doubles?
        double standardDeviation = 0;
        double[] FeretValues = new double[5];
        double meanFeretAR = 0;
        double meanFeretMaxCaliperLocal = 0;
        double meanFeretMinCaliperLocal = 0;
        
        //System.out.pr("microVolume::calculateVolumeMeasurements                Regions to analyze: " + this.alRegions.size());
        
        ListIterator<microRegion> itr = alRegions.listIterator();
        
        
        
        while(itr.hasNext()){
            microRegion region = new microRegion();
            region = itr.next();

            FeretValues = region.getFeretValues();
            meanFeretAR = meanFeretAR + (FeretValues[0]) / (FeretValues[2]);
            meanFeretMaxCaliperLocal = meanFeretMaxCaliperLocal + FeretValues[0];
            meanFeretMinCaliperLocal = meanFeretMinCaliperLocal + FeretValues[2];
            double[] deviations = region.getDeviations();
            countPixels = countPixels + region.getPixelCount();
            total = total + (long) region.getIntegratedIntensity();
            totalThreshold = totalThreshold + (long) region.getThresholdedIntegratedIntensity();
            countPixelsThreshold = countPixelsThreshold + region.getThresholdPixelCount();
            mean = mean + (long) region.getMeanIntensity();
            x_centroid = (x_centroid + region.getBoundCenterX())/2;
            y_centroid = (y_centroid + region.getBoundCenterY())/2;
            z_centroid = (z_centroid + region.getZPosition())/2;
                       
            if (region.getMinIntensity() < minLocal) {
                minLocal = region.getMinIntensity();
            }
            if (region.getMaxIntensity() > maxLocal) {
                maxLocal = region.getMaxIntensity();
            }
            for(int j = 0; j <= region.getPixelCount()-1; j++){
                standardDeviation = standardDeviation + Math.pow(deviations[j]-(total/countPixels), 2);
            }
                
        }
        this.n = countPixels;

        
        analysisMaskVolume[0] = countPixels;
        analysisMaskVolume[1] = total/countPixels; 
        analysisMaskVolume[2] = total;
        analysisMaskVolume[3] = minLocal; 
        analysisMaskVolume[4] = maxLocal;
        analysisMaskVolume[5] = Math.sqrt(standardDeviation/countPixels);
        analysisMaskVolume[8] = meanFeretMaxCaliperLocal / (nRegionsLocal);
        analysisMaskVolume[7] = meanFeretMinCaliperLocal / (nRegionsLocal);
        analysisMaskVolume[9] = totalThreshold/countPixelsThreshold;
        analysisMaskVolume[10] = Math.pow(totalThreshold/countPixelsThreshold,2);

        if (meanFeretMinCaliperLocal / (nRegionsLocal) != 0) {
            analysisMaskVolume[6] = (meanFeretMaxCaliperLocal / (nRegionsLocal)) / (meanFeretMinCaliperLocal / (nRegionsLocal));
        }
        

        //System.out.println("New Object average z_centroid: " + z_centroid);

        //IJ.log("microVolume::calculateVolumeMeasurements Mask Volume measurements: " + analysisMaskVolume[0] + ", " + analysisMaskVolume[1] + ", " + analysisMaskVolume[2] + ", " + analysisMaskVolume[3] + ", " + analysisMaskVolume[4]);

    }

//redefine derived region --> may upgrade to multiple derived states, depends upon utility
//public void rederiveRegion(int n, int type){DerivedRegions[n] = new derivedRegion(Regions[n], type);}
//private methods for making derived regions
    
    private void calculateGrow(int Channel, int amountGrow, ImageStack is) {
        ListIterator<microRegion> itr = alRegions.listIterator();
        int i = 0;
        while(itr.hasNext()){
            microRegion region = new microRegion();
            region = itr.next();
            DerivedRegions[Channel][i] = new microDerivedRegion(region.getPixelsX(), region.getPixelsY(), region.getPixelCount(), region.getZPosition(), microVolume.GROW, amountGrow, region.getName());
            DerivedRegions[Channel][i].calculateMeasurements(is);
            //System.out.println("Calculated ID:" + DerivedRegions[Channel][i].getIntegratedIntensity());
            if(DerivedRegions[Channel][i].getPixelCount()>0){
            i++;
            }
        }
        this.nDerivedRegions = i;
    }

    private void calculateFill() {
    }

    private void calculateMask(int Channel, ImageStack is) {
            ListIterator<microRegion> itr = alRegions.listIterator();
        int i = 0;        
        while(itr.hasNext()){           
            microRegion region = new microRegion();
            region = itr.next();
            DerivedRegions[Channel][i] = new microDerivedRegion(region.getPixelsX(), region.getPixelsY(), region.getPixelCount(), region.getZPosition(), microVolume.MASK, 0, region.getName());
            DerivedRegions[Channel][i].calculateMeasurements(is);          
            i++;
        }
};
    
    //Extract pixels array for MicroObject requirements
    
    public void makePixelArrays(){
        
        ArrayList<Integer> xAl = new ArrayList();
        ArrayList<Integer> yAl = new ArrayList();
        ArrayList<Integer> zAl = new ArrayList();
        
        ListIterator<microRegion> itr = alRegions.listIterator();
        
        while(itr.hasNext()){          
           microRegion region = itr.next();
           int[] xR = region.getPixelsX();
           int[] yR = region.getPixelsY();
           int[] zR = new int[xR.length];
           
           int z_position = region.getZPosition();
           
           for(int i = 0; i < xR.length; i++){
                zAl.add(z_position);
                xAl.add(xR[i]);
                yAl.add(yR[i]);
           }

        }
        
        z = new int[xAl.size()];
        y = new int[xAl.size()];
        x = new int[xAl.size()];

        for(int j = 0; j < xAl.size(); j++){
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

public void addRegions(List<microRegion> regions){
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
    
    public void setDerivedRegions(microDerivedRegion[][] mdr){
        this.DerivedRegions = mdr;
    }
    
    public int getNDRegions(){
        return nDerivedRegions;
    }

//Analysis functions
    public int getNRegions() {
        return alRegions.size();
    }

    public float getMean() {
        return this.mean;
    }

    public float getIntDen() {
        return this.integrated_density;
    }

    public double getFeretMax() {
        return this.FeretMaxCaliperMax;
    }

    public double getFeretMin() {
        return this.FeretMinCaliperMax;
    }

    public double getFeretAR() {
        return this.FeretAspectRatio;
    }

    public int[][] getderivedConstants() {
        return this.derivedConstants;
    }

    public double getMax() {
        return (Double)analysisMaskVolume[4];
    }

    public int getPixelCount() {
        return this.n;
    }
    
    public int getNChannels() {
        return this.nChannels;
    }

    public double getMin() {
        return this.min;
    }

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

    @Override
    public Object[][] getAnalysisResultsVolume() {
        return this.analysisResultsVolume;
    }

    @Override
    public Object[] getAnalysisMaskVolume() {
        return this.analysisMaskVolume;
    }

    public ArrayList getResultPointer() {
        return this.ResultsPointer;
    }
    
//    public Color getAnalyticColor(int channel, int analytic) {
//        try{return Colorized[channel][analytic];}
//        catch(NullPointerException np){}
//        return Color.BLACK;
//    }
//    
//    public void setAnalyticColor(Color clr,int channel, int analytic){
//        this.Colorized[channel][analytic] = clr;
//    }
    
    public void setName(String str){
        name = str;
    }
    
    public String getName(){
        return name;
    }
    
    public int[] getBoundsCenter(){
        int[] center = new int[2];
        center[0] = (Integer)this.x_centroid;
        center[1] = (Integer)this.y_centroid;
        return center;
    }

    @Override
    public void calculateDerivedObjectMeasurements(int channel, ImageStack is) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void calculateObjectMeasurments() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int[][] getDerivedObjectConstants() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArrayList getObjectPixels() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double[] getFeretValues() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getThresholdPixelCount() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int[] getPixelsX() {
        return x;
    }

    @Override
    public int[] getPixelsY() {
        return y;
     }
    
        @Override
    public int[] getPixelsZ() {
        return z;
     }

    @Override
    public float getCentroidX() {
         try{
        return x_centroid;
        } catch(NullPointerException e){return -1;}
    }
    
    @Override
    public float getCentroidY() {
           try{
        return y_centroid;
        } catch(NullPointerException e){return -1;}
    }
    
    @Override
    public float getCentroidZ() {
        try{
        return z_centroid;
        } catch(NullPointerException e){return -1;}
    }

    @Override
    public int getBoundCenterX() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getBoundCenterY() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getMaxIntensity() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getMinIntensity() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getIntegratedIntensity() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getMeanIntensity() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double[] getDeviations() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getThresholdedIntegratedIntensity() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getThresholdedMeanIntensity() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setThreshold(double threshold) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Rectangle getBoundingRectangle() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getSerialID() {
       return serialID;
    }

     @Override
    public int[] getXPixelsInRegion(int i) {
        List regions = getRegions();
                
                //have microobject return the slice specific arrays of pixels
            
                ListIterator<microRegion> ritr = regions.listIterator(); 
                while (ritr.hasNext()) {
                    microRegion region = ritr.next();

                    if (region.getZPosition() == i) {
                          return region.getPixelsX();
                    }
                }
            return null;    
    }

    @Override
    public int[] getYPixelsInRegion(int i) {
        List regions = getRegions();
                
                //have microobject return the slice specific arrays of pixels
            
                ListIterator<microRegion> ritr = regions.listIterator(); 
                while (ritr.hasNext()) {
                    microRegion region = ritr.next();

                    if (region.getZPosition() == i) {
                          return region.getPixelsY();
                    }
                }
                return null;
    }

    @Override
    public void setMorphological(String method_UID, ArrayList x, ArrayList y, ArrayList z) {
 
    }



    };
