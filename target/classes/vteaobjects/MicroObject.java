/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package vteaobjects;

import ij.ImageStack;
import java.awt.Color;
import java.awt.Rectangle;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import vteaobjects.layercake.microVolume;


/**
 *
 * @author vinfrais
 */
public class MicroObject implements MicroObjectModel {
    
    int[] x;
    int[] y;
    int[] z;
    
    int[][] derivedX;
    int[][] derivedY;
    int[][] derivedZ;
    
    
    
    //calculated variables
    private float mean = 0;
    private int nThreshold = 0;
    private float thresholdedmean = 0;
    private double thresholdformean = 0.8;
    private float thresholdedid = 0;
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
    //static public String[] Analytics = {"#pixels", "mean", "sum", "min", "max", "SD", "AR", "F_min", "F_max", "mean_th", "mean_sq"};
    private Color[][] Colorized = new Color[4][9];
    private ArrayList ResultsPointer;
    
    //calculated variables

    private double[] deviation;
    private double[] FeretValues = new double[5];  //1, maximum caliper width; 1 , FeretAngle; 3, minimum caliper width; 4, FeretX; 5, FeretY. 
    private Rectangle boundingRectangle;
    
    private float centroid_x = 0;
    private float centroid_y = 0;

    private int centerBoundX = 0;
    private int centerBoundY = 0;
    
    private int serialID;
    private int nChannels;
    
    private int xMin;
    private int xMax;
    private int yMin;
    private int yMax;
    private int zMin;
    private int zMax;
    
    private int xOffset;
    private int yOffset;
    private int zOffset;
    
    private int xLimit;
    private int yLimit;
    private int zLimit;
    
    private int padding = 3;
    
    private int[][][] theBox;
    
    public MicroObject(){}
    


    public MicroObject(ArrayList<int[]> pixels, int maskChannel, ImageStack[] is, int serialID){
        
       this.serialID = serialID;
       xLimit = is[0].getWidth();
       yLimit = is[0].getHeight();
       zLimit = is[0].getSize();
       
       

       setPixels(pixels);
       
       
       
       calculateMeasurements(is, maskChannel);
       //System.out.println("PROFILING: Object " + this.getSerialID() + " from channel " + maskChannel + " created of size: " + analysisMaskVolume[0] + " with mean intensities of: " + analysisMaskVolume[1]);
       //System.out.println("PROFILING: " + analysisMaskVolume[0] + ", " + analysisMaskVolume[1]);
       
    }
    
    @Override
   public void makeDerivedRegions(int[][] derivedRegionType, int channels, ImageStack[] Stacks, ArrayList ResultsPointers){
       
       //System.out.println("Deriving regions for object# " + this.serialID + ",for " + this.x.length + " pixels.");
        derivedConstants = derivedRegionType;
        this.nChannels = channels;
        //DerivedRegions = new microDerivedRegion[nChannels][alRegions.size()];
        //derivedX = new int[channels][]
        this.ResultsPointer = ResultsPointers;
        
        
        for (int i = 0; i < channels; i++) {
            
            switch (derivedRegionType[i][0]) {
                case microVolume.GROW:
                    calculateGrow(i, derivedRegionType[i][1], Stacks[i]);
                    calculateDerivedObjectMeasurements(i, Stacks[i]);
                    break;
                case microVolume.MASK:
                    calculateMask(i, Stacks[i]);
                    calculateDerivedObjectMeasurements(i, Stacks[i]);
                    break;
                case microVolume.FILL:
                    //calculateFill();
                    break;
                default:
                    break;
            }
        }
   }
   
   private void calculateGrow(int Channel, int amountGrow, ImageStack is){
       setMinMaxOffsetValues();
       
       
       
   }
   
   
    private void calculateGrowOld(int Channel, int amountGrow, ImageStack is) {

        ArrayList<Integer> xderived = new ArrayList<Integer>();
        ArrayList<Integer> yderived = new ArrayList<Integer>();
        ArrayList<Integer> zderived = new ArrayList<Integer>();   
        
        ArrayList<Integer> xObj = new ArrayList<Integer>();
        ArrayList<Integer> yObj = new ArrayList<Integer>();
        ArrayList<Integer> zObj = new ArrayList<Integer>();  
        
        //populate arraylists
        
        for(int j = 0; j < x.length; j++){

                        xObj.add(x[j]);
                        yObj.add(y[j]);
                        zObj.add(z[j]);
 
        } 
        
        ArrayList<ArrayList> al = new ArrayList<ArrayList>();
        
        al.add(xderived);
        al.add(yderived);
        al.add(zderived);

        int width = is.getWidth();
        int height = is.getHeight();
        int size = is.getSize();

        //System.out.println("PROFILING: Deriving by grow of " + amountGrow + " of Object# " + this.getSerialID() + " for " + this.x.length + " voxels in channel " + Channel);
        
        //for(int i = 0; i < amountGrow; i++){
            for(int j = 0; j < xObj.size(); j++){
               // for(int k = 0; k < yObj.size(); k++){
                    //for(int l = 0; l < zObj.size(); l++){
                    
                    //System.out.println("Check 1");
                        //al = dilatefill3D(al.get(0), al.get(1), al.get(2), xObj.get(j), yObj.get(k), zObj.get(l), width, height, size);
                        //System.out.println("Dilate fill 3D kernel (" + xObj.get(j) + ", " + yObj.get(j)+ ", " + zObj.get(j)+")");
                        for(int k = -1; k <= 1; k++){
                            for(int  l = -1; l <=1 ; l++){
                                for(int m = -1; m <= 1; m++){
                                    
                                    al = dilatefill3D(al, xObj.get(j)+k, yObj.get(j)+l, zObj.get(j)+m, width, height, size);
                                }
                            }   
                        }
//                        al = dilatefill3D(al.get(0), al.get(1), al.get(2), xObj.get(j)+1, yObj.get(j), zObj.get(j), width, height, size);
//                        al = dilatefill3D(al.get(0), al.get(1), al.get(2), xObj.get(j), yObj.get(j)+1, zObj.get(j), width, height, size);
//                        al = dilatefill3D(al.get(0), al.get(1), al.get(2), xObj.get(j)+1, yObj.get(j)+1, zObj.get(j), width, height, size);
//                        al = dilatefill3D(al.get(0), al.get(1), al.get(2), xObj.get(j)-1, yObj.get(j), zObj.get(j), width, height, size);
//                        al = dilatefill3D(al.get(0), al.get(1), al.get(2), xObj.get(j), yObj.get(j)-1, zObj.get(j), width, height, size);
//                        al = dilatefill3D(al.get(0), al.get(1), al.get(2), xObj.get(j)-1, yObj.get(j)-1, zObj.get(j), width, height, size);
//                        al = dilatefill3D(al.get(0), al.get(1), al.get(2), xObj.get(j)-1, yObj.get(j)+1, zObj.get(j), width, height, size);
//                        al = dilatefill3D(al.get(0), al.get(1), al.get(2), xObj.get(j)+1, yObj.get(j)-1, zObj.get(j), width, height, size);
//                        
//                        al = dilatefill3D(al.get(0), al.get(1), al.get(2), xObj.get(j), yObj.get(j), zObj.get(j)-1, width, height, size);
//                        al = dilatefill3D(al.get(0), al.get(1), al.get(2), xObj.get(j)+1, yObj.get(j), zObj.get(j)-1, width, height, size);
//                        al = dilatefill3D(al.get(0), al.get(1), al.get(2), xObj.get(j), yObj.get(j)+1, zObj.get(j)-1, width, height, size);
//                        al = dilatefill3D(al.get(0), al.get(1), al.get(2), xObj.get(j)+1, yObj.get(j)+1, zObj.get(j)-1, width, height, size);
//                        al = dilatefill3D(al.get(0), al.get(1), al.get(2), xObj.get(j)-1, yObj.get(j), zObj.get(j)-1, width, height, size);
//                        al = dilatefill3D(al.get(0), al.get(1), al.get(2), xObj.get(j), yObj.get(j)-1, zObj.get(j)-1, width, height, size);
//                        al = dilatefill3D(al.get(0), al.get(1), al.get(2), xObj.get(j)-1, yObj.get(j)-1, zObj.get(j)-1, width, height, size);
//                        al = dilatefill3D(al.get(0), al.get(1), al.get(2), xObj.get(j)-1, yObj.get(j)+1, zObj.get(j)-1, width, height, size);
//                        al = dilatefill3D(al.get(0), al.get(1), al.get(2), xObj.get(j)+1, yObj.get(j)-1, zObj.get(j)-1, width, height, size);
//                        
//                        
//                        
//                        al = dilatefill3D(al.get(0), al.get(1), al.get(2), xObj.get(j), yObj.get(j), zObj.get(j)+1, width, height, size);
//                        al = dilatefill3D(al.get(0), al.get(1), al.get(2), xObj.get(j)+1, yObj.get(j), zObj.get(j)+1, width, height, size);
//                        al = dilatefill3D(al.get(0), al.get(1), al.get(2), xObj.get(j), yObj.get(j)+1, zObj.get(j)+1, width, height, size);
//                        al = dilatefill3D(al.get(0), al.get(1), al.get(2), xObj.get(j)+1, yObj.get(j)+1, zObj.get(j)+1, width, height, size);
//                        al = dilatefill3D(al.get(0), al.get(1), al.get(2), xObj.get(j)-1, yObj.get(j), zObj.get(j)+1, width, height, size);
//                        al = dilatefill3D(al.get(0), al.get(1), al.get(2), xObj.get(j), yObj.get(j)-1, zObj.get(j)+1, width, height, size);
//                        al = dilatefill3D(al.get(0), al.get(1), al.get(2), xObj.get(j)-1, yObj.get(j)-1, zObj.get(j)+1, width, height, size);
//                        al = dilatefill3D(al.get(0), al.get(1), al.get(2), xObj.get(j)-1, yObj.get(j)+1, zObj.get(j)+1, width, height, size);
//                        al = dilatefill3D(al.get(0), al.get(1), al.get(2), xObj.get(j)+1, yObj.get(j)-1, zObj.get(j)+1, width, height, size);
                   // }
               // }
            }
        //}  
        int c = 0;
        for(int i = 0; i < al.get(0).size(); i++){
            this.derivedX[Channel][c] = (Integer)al.get(0).get(i);
            this.derivedY[Channel][c] = (Integer)al.get(1).get(i);
            this.derivedZ[Channel][c] = (Integer)al.get(2).get(i);
        }
    } 
     
    private void calculateMask(int Channel, ImageStack is) {
//            ListIterator<microRegion> itr = alRegions.listIterator();
//        int i = 0;        
//        while(itr.hasNext()){           
//            microRegion region = new microRegion();
//            region = itr.next();
//            DerivedRegions[Channel][i] = new microDerivedRegion(region.getPixelsX(), region.getPixelsY(), region.getPixelCount(), region.getZPosition(), microVolume.MASK, 0, region.getName());
//            DerivedRegions[Channel][i].calculateMeasurements(is);          
//            i++;
//        }
}
    
    private void setMinMaxOffsetValues(){
        //System.out.println("PROFILING: Object ID# " + serialID + " created of size: " + x.length);
        xMax = 0;
        xMin = xLimit;
        yMax = 0;
        yMin = yLimit;
        zMax = 0;
        zMin = zLimit;
        
        for(int i = 0; i < x.length; i++){
            if(xMin > x[i]){xMin = x[i];}
            if(xMax < x[i]){xMax = x[i];}
            if(yMin > y[i]){yMin = y[i];}
            if(yMax < y[i]){yMax = y[i];}
            if(zMin > z[i]){zMin = z[i];}
            if(zMax < z[i]){zMax = z[i];}
        }
        
        //change offset dynamically-gui input
        
       // System.out.println("PROFILING: Object ID# " + serialID + " xMin: " + xMin + ", xMax: " + xMax);
        
       // System.out.println("PROFILING: Object ID# " + serialID + " yMin " + yMin + ", yMax: " + yMax);
        
       // System.out.println("PROFILING: Object ID# " + serialID + " zMin " + zMin + ", zMax: " + zMax);
        
        //Padding
        
        xOffset = xMin + padding;
        yOffset = yMin + padding;
        zOffset = zMin + padding;
        
        //theBoxX = xReal-Padding+1
        theBox = new int[xMax-xMin+2*(padding)+1][yMax-yMin+2*(padding)+1][zMax-zMin+2*(padding)+1];
        
        for(int i = 0; i < (xMax-xMin+2*(padding)); i++){
            for(int j = 0; j < (yMax-yMin+2*(padding)); j++){
                for(int k = 0; k < (zMax-zMin+2*(padding)); k++){
                    theBox[i][j][k] = -1;
                    
                }
            }
               
        }
        
        for(int i = 0; i < x.length; i++){
            theBox[padding + (x[i]-xMin)][padding + (y[i]-yMin)][padding + (z[i]-zMin)] = 0;
        }
        
        int ring = 1;
        
        while(ring < 5){
        
        for(int i = 1; i < (xMax-xMin+2*(padding)); i++){
            for(int j = 1; j < (yMax-yMin+2*(padding)); j++){
                for(int k = 1; k < (zMax-zMin+2*(padding)); k++){
                    
                    try{
                        if(theBox[i+1][j][k] == ring-1){theBox[i][j][k] = ring; break;} 
                        if(theBox[i-1][j-1][k] == ring-1){theBox[i][j][k] = ring;  break;}
                        if(theBox[i+1][j+1][k] == ring-1){theBox[i][j][k] = ring; break;}
                        if(theBox[i+1][j-1][k] == ring-1){theBox[i][j][k] = ring;  break;}
                        if(theBox[i-1][j+1][k] == ring-1){theBox[i][j][k] = ring; break;}
                        if(theBox[i-1][j][k] == ring-1){theBox[i][j][k] = ring; break;}
                        if(theBox[i][j-1][k] == ring-1){theBox[i][j][k] = ring; break;}
                        
                        if(theBox[i+1][j][k+1] == ring-1){theBox[i][j][k] = ring; break;}
                        if(theBox[i-1][j-1][k+1] == ring-1){theBox[i][j][k] = ring;break;}
                        if(theBox[i+1][j+1][k+1] == ring-1){theBox[i][j][k] = ring;break;}
                        if(theBox[i+1][j-1][k+1] == ring-1){theBox[i][j][k] = ring; break;}
                        if(theBox[i-1][j+1][k+1] == ring-1){theBox[i][j][k] = ring; break;}
                        if(theBox[i-1][j][k+1] == ring-1){theBox[i][j][k] = ring; break;}
                        if(theBox[i][j-1][k+1] == ring-1){theBox[i][j][k] = ring; break;}
                        
                        if(theBox[i+1][j][k-1] == ring-1){theBox[i][j][k] = ring;break;}
                        if(theBox[i-1][j-1][k-1] == ring-1){theBox[i][j][k] = ring;break;}
                        if(theBox[i+1][j+1][k-1] == ring-1){theBox[i][j][k] = ring;break;}
                        if(theBox[i+1][j-1][k-1] == ring-1){theBox[i][j][k] = ring; break;}
                        if(theBox[i-1][j+1][k-1] == ring-1){theBox[i][j][k] = ring;break;}
                        if(theBox[i-1][j][k-1] == ring-1){theBox[i][j][k] = ring;break;}
                        if(theBox[i][j-1][k-1] == ring-1){theBox[i][j][k] = ring; break;}
                        
                    }catch(NullPointerException e){}
                }
            }
            
        }
        ring++;
        }
        
        int[] ringPixelCount = new int[ring];
        int objectPixelCount = 0;
        
        for(int r = 0; r < ring; r++){
            for(int i = 1; i < xMax-xMin+2*(padding); i++){
                for(int j = 1; j < yMax-yMin+2*(padding); j++){
                    for(int k = 1; k < zMax-zMin+2*(padding); k++){
                        if(theBox[i][j][k] == r) {ringPixelCount[r]++;}
                        //if(theBox[i][j][k] == 0) {objectPixelCount++;}
                    }
                }
            }
            
        }
       //System.out.println("Object has " + objectPixelCount + " pixels");
        for (int i = 0; i < ring; i++){
           System.out.println("Ring #" + i + " has " + ringPixelCount[i] + " pixels");
        }
        
        
    }
    
    private boolean containsPixel(int[] xVal, int[] yVal, int [] zVal, int x, int y, int z){
        //System.out.println("Checking object voxels!");
        for(int i = 0; i < xVal.length; i++){
            if(xVal[i] == x){
                for(int j = 0; j < yVal.length; j++){
                    if(yVal[i] == y){
                        for(int k = 0; k < zVal.length; k++){
                            if(xVal[i] == x && yVal[j] == y && zVal[k] == z){
                            return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    private boolean containsPixel(ArrayList<Integer> xVal, ArrayList<Integer> yVal, ArrayList<Integer> zVal, int x, int y, int z){
            //System.out.println("Checking Derived voxels!");
            if(xVal.size() > 0){
                for(int i = 0; i < xVal.size(); i++){
                    if(xVal.get(i)==x){
                        for(int j = 0; j < yVal.size(); j++){
                            if(xVal.get(j)==y){
                                for(int k = 0; k < zVal.size(); k++){
                                    if(xVal.get(i) == x && yVal.get(j) == y && zVal.get(k) == z){
                                     return true;
                                    }
                                 }
                            }
                        }
                    }
                    }
            }
        return false;
    }
    
    private ArrayList<ArrayList> dilatefill3D(ArrayList<ArrayList> al, int x, int y, int z, int width, int height, int size){
        
        //System.out.println("Checking: (" + x + ", " + y + ", " + z +")");
        
        //ArrayList<ArrayList> al = new ArrayList<ArrayList>();
        //System.out.println("What the what!");

         //System.out.println("In range!");
         if((containsPixel(this.x, this.y, this.z,x,y,z)) ){  
                       //System.out.println("Voxel already in object or derived!");
            return al;
         }
          
            al.get(0).add(x);
            al.get(1).add(y);
            al.get(2).add(z);
            return al;
         
 
    }
   
    private void setPixels(ArrayList<int[]> pixels){
        x = new int[pixels.size()];
        y = new int[pixels.size()];
        z = new int[pixels.size()];
        
        
        int[] pixel = new int[3];
        
        int counter = 0;
        
        ListIterator<int[]> itr = pixels.listIterator();
        
        while(itr.hasNext()){
            pixel = itr.next();
            x[counter] = pixel[0];
            y[counter] = pixel[1];
            z[counter] = pixel[2];
            counter++;
        }
         
    }
    
 public void calculateMeasurements(ImageStack[] stack, int maskChannel) {
        int[] x = this.x;
        int[] y = this.y;
        int[] z = this.z;
        int n = x.length;


        long total = 0;
        double min = 0;
        double max = 0;
        double[] deviation = new double[n];
        

        //for( int ch = 0; ch < stack.length; ch++){
            for (int i = 0; i < n; i++) {
                total = total + (long) stack[maskChannel].getVoxel(x[i], y[i], z[i]);
                if (stack[maskChannel].getVoxel(x[i], y[i], z[i]) < min) {
                    min = stack[maskChannel].getVoxel(x[i], y[i], z[i]);
                }
                if (stack[maskChannel].getVoxel(x[i], y[i], z[i]) > max) {
                    max = stack[maskChannel].getVoxel(x[i], y[i], z[i]);
                }
                deviation[i] = stack[maskChannel].getVoxel(x[i], y[i], z[i]);
            }
        

        this.deviation = deviation;
        this.max = max;
        this.min = min;
        this.mean = total / n;
        this.integrated_density = total;
        
        
        total = 0;
        int thresholdcount = 0;
        
        for(int j = 0; j < n; j++) {
                if (stack[maskChannel].getVoxel(x[j], y[j], z[j]) > max*this.thresholdformean){
                    total = total + (long) stack[maskChannel].getVoxel(x[j], y[j], z[j]);
                    thresholdcount++;
                }  
        }    
        this.thresholdedid = total;
        this.nThreshold = thresholdcount;
        if(thresholdcount > 0){
        this.thresholdedmean = total/thresholdcount;
        }else {this.thresholdedmean = 0;}
        analysisMaskVolume[0] = n;
        analysisMaskVolume[1] = this.mean; 
        analysisMaskVolume[2] = this.integrated_density;
        analysisMaskVolume[3] = this.min; 
        analysisMaskVolume[4] = this.max;
        analysisMaskVolume[5] = 0;
        analysisMaskVolume[8] = 0;
        analysisMaskVolume[7] = 0;

        if(nThreshold > 0){
            analysisMaskVolume[9] = this.integrated_density/nThreshold;
            analysisMaskVolume[10] = Math.pow(this.integrated_density/nThreshold,2);
        }
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


    public int getPixelCount() {
        return x.length;
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
    
    private void setDerivedObjectsArrays(int channel, int steps) {
            int count = 0;
        for(int i = 0; i < (xMax-xMin+2*(padding)); i++){
            for(int j = 0; j < (yMax-yMin+2*(padding)); j++){
                for(int k = 0; k < (zMax-zMin+2*(padding)); k++){
                    if(theBox[i][j][k] < steps && !(theBox[i][j][k]==0)){
                        count++;
                    }
                }
            }   
        }

        for(int i = 0; i < (xMax-xMin+2*(padding)); i++){
            for(int j = 0; j < (yMax-yMin+2*(padding)); j++){
                for(int k = 0; k < (zMax-zMin+2*(padding)); k++){
                    if(theBox[i][j][k] < steps && !(theBox[i][j][k]==0)){
                       // derivedX[]
                    }
                }
            }   
        }
        
        
    }
    
    
    @Override
    public void calculateDerivedObjectMeasurements(int Channel, ImageStack is) {
        
       int countPixels = 0;
        double total = 0;
        long totalThreshold = 0;
        int nThreshold = 0; 
        double minLocal = Math.pow(2,is.getBitDepth());//why doubles?
        double maxLocal = 0;//why do2ubles?
        double standardDeviation = 1;
        double[] FeretValues = new double[5];
        double meanFeretAR = 0;
        double meanFeretMaxCaliperLocal = 0;
        double meanFeretMinCaliperLocal = 0;
        
        try{
        for(int i = 0; i < derivedX.length; i++){
            for(int j = 0; j < derivedY.length; j++){
                for(int k = 0; k < derivedZ.length; k++){
                    total = total + is.getVoxel(i, j, k);
                    if(is.getVoxel(i, j, k) > maxLocal){
                        maxLocal = is.getVoxel(i, j, k);
                    }
                    if(is.getVoxel(i, j, k) < minLocal){
                        minLocal = is.getVoxel(i, j, k);
                    }
                }
            }
        }
        
        
        analysisResultsVolume[Channel][0] = derivedX.length;
        
        if(countPixels == 0){analysisResultsVolume[Channel][1] = 0;}
        else{
        analysisResultsVolume[Channel][1] = total/derivedX.length; //changed from averaging regions to all pixels
        }
        
        analysisResultsVolume[Channel][2] = total;
        analysisResultsVolume[Channel][3] = minLocal;
        analysisResultsVolume[Channel][4] = maxLocal;
        analysisResultsVolume[Channel][5] = Math.sqrt(standardDeviation/countPixels);
        analysisResultsVolume[Channel][8] = 0;
        analysisResultsVolume[Channel][7] = 0;
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
     

        //IJ.log("microVolume Derived Volume measurements: " + analysisResultsVolume[Channel][1] + ", " + analysisResultsVolume[Channel][2] + ", " + analysisResultsVolume[Channel][3] + ", " + analysisResultsVolume[Channel][4] + ", " + analysisResultsVolume[Channel][5] + ", " + analysisResultsVolume[Channel][6]);
        //System.out.println("Analyzing volume: " + this.getName() + " ID " + analysisResultsVolume[Channel][2]);
        
        System.out.println("PROFILING: Calculated derived measures for object: " + this.getSerialID() + ", giving a mean of: " + analysisResultsVolume[Channel][1]);
        }catch(NullPointerException e){//System.out.println("Uh Oh, where did the object go?");
        }
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
    public ArrayList getResultPointer() {
       return this.ResultsPointer;
    }

    @Override
    public Object[] getAnalysisMaskVolume() {
        return this.analysisMaskVolume;
    }

    @Override
    public Object[][] getAnalysisResultsVolume() {
       return this.analysisResultsVolume;
    }

    @Override
    public int getSerialID() {
       return this.serialID;
    }

    @Override
    public List getRegions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public int[] getXPixelsInRegion(int i) {
        int[] al_x = new int[z.length];
        int counter = 0;
         for(int j = 0; j < z.length; j++){
            if(z[j] == i){
                al_x[counter] = x[j];
                counter++;
            }
        }
        int[] export_x = new int[counter];
        for (int k = 0; k < counter; k++){
            export_x[k] = al_x[k];
        }
       // System.out.println("X Maximum size: " + z.length);
        //System.out.println("X Region counter: " + counter + " size: " + export_x.length);
        return export_x;
    }

    @Override
    public int[] getYPixelsInRegion(int i) {
                int[] al_y = new int[z.length];
        int counter = 0;
        for(int j = 0; j < z.length; j++){
            if(z[j] == i){
                al_y[counter] = y[j];
                counter++;
            }
        }
        int[] export_y = new int[counter];
        for (int k = 0; k < counter; k++){
            export_y[k] = al_y[k];
        }
        //System.out.println("Y Maximum size: " + z.length);
        //System.out.println("Y Region counter: " + counter + " size: " + export_y.length);
        return export_y;
    }

    @Override
    public int[] getBoundsCenter() {
        int boundCenter[] = new int[2];
        boundCenter[0] = this.centerBoundX;
        boundCenter[1] = this.centerBoundY;
        return boundCenter;
    }

    @Override
    public float getCentroidZ() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    }

