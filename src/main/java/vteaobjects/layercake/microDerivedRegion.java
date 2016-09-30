package vteaobjects.layercake;

import vteaobjects.layercake.microRegion;
import ij.*;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.lang.Integer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class  microDerivedRegion extends microRegion {

    public static final int INCLUDED = 1;
    public static final int EXCLUDED = 2;
    public static final int GROW = 0;
    public static final int MASK = 2;
    public static final int ERODE = 1;

    /**
     * Variables
     */
    
    private ArrayList<int[]> sourcePixels;
    private ArrayList<int[]> pixels;
    private int[] xOriginal;
    private int[] yOriginal;
    private int nOriginal;
    private int[] x;			//x coordinates
    private int[] y;  
    //y coordinates
    private Set<int[]> points = Collections.synchronizedSet(new HashSet<int[]>());
    

    
    private int z;				//z position
    private int n;				//number of pixels
    private int type;			//what kind of analysis
    private int subtype;			//analysis modifier

    private String name;			//based on Roi name
    private int volume;			//volume membership if defined
    private int include = INCLUDED;		//flag for including region in analysis

    private float mean = 0;
    private int nThreshold = 0;
    private float thresholdedmean = 0;
    private double thresholdformean = 0.8;
    private float thresholdedid = 0;
    private float integrated_density = 0;
    private double[] deviation;
    private double min = 0;
    private double max = 0;
    private float stdev = 0;

    private double[] FeretValues = new double[5];  //1, maximum caliper width; 1 , FeretAngle; 3, minimum caliper width; 4, FeretX; 5, FeretY. 

    private float centroid_x = 0;
    private float centroid_y = 0;

    private int centerBoundX = 0;
    private int centerBoundY = 0;

    private Object[] analysisResultsRegion;
//[0, count, 1, mean, 2, integrated density, 3, min, 4, max, 5 standard deviation, 6 Feret_AR, 7 Feret_Min, 8 Feret_Max]

    /**
     * Constructors
     */
    //empty constructor
    public microDerivedRegion() {
    }

    //start with a microregion and derive per type
    public microDerivedRegion(microRegion Region, int type) {
    }

    //type is limited to GROW_1 and GROW_2 for number of pixels
    public microDerivedRegion(int[] x, int[] y, int n, int z, String name) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.n = n;
        this.z = z;
    }

    public microDerivedRegion(int[] x, int[] y, int n, int z, int type, String name) {

        this.x = x;
        this.y = y;
        this.n = n + 1;
        this.z = z;
        this.type = type;

        switch (type) {  //new functions may be added later
            default:
                this.name = name;
                break;
        }
    }

    public microDerivedRegion(int[] x, int[] y, int n, int z, int type, int subtype, String name) {

        
        this.x = x;
        this.y = y;
        this.n = n;
        
        this.nOriginal = n;
        this.xOriginal = new int[x.length];
        this.yOriginal = new int[y.length];
        
        System.arraycopy(this.x, 0, this.xOriginal, 0, this.n);
        System.arraycopy(this.y, 0, this.yOriginal, 0, this.n);
        
        int[] holder = new int[2];    
        for(int i = 0; i < n; i++){
            holder[0] = x[i];
            holder[1] = y[i];
            this.points.add(holder);
            holder = new int[2];
        }

        this.z = z;
        this.type = type;
        this.subtype = subtype;

        switch (type) {  //new functions may be added later
            case microVolume.GROW:
                //IJ.log("Grow region: " + subtype);
                this.growRegion2(subtype);
                this.name = name + "_GROW_" + subtype + "px";
                break;
            case microVolume.MASK:
                this.name = name + "_MASK_" + subtype + "px";
                break;
            default:
                this.name = name;
                break;
        }
        //System.out.println("microDerivedRegion::<init>  ...making region " + this.getName() + ", from " + n + " pixels");
    }

    private void setXYfromSet(Set<int[]> points){
        Iterator<int[]> itr = points.iterator();
        
        int[] xNew = new int[points.size()];
        int[] yNew = new int[points.size()];
        int[] holder = new int[2];
        int i = 0;
        
        while(itr.hasNext()){
            holder = itr.next();
            xNew[i] = holder[0];
            yNew[i] = holder[1];
            i++;
        }
        x = xNew;
        y = yNew;
        n = points.size();
    }

    private void growRegion2(int times) {
        ArrayList<Integer> xArr = new ArrayList<Integer>();
        ArrayList<Integer> yArr = new ArrayList<Integer>();
        for(int j = 1; j <= times; j++){
            for(int i = 0; i < n; i++){
                xArr.add(x[i]-1);
                yArr.add(y[i]);
                
                xArr.add(x[i]+1);
                yArr.add(y[i]);
                
                xArr.add(x[i]);
                yArr.add(y[i]-1);
                
                xArr.add(x[i]);
                yArr.add(y[i]+1);
                
                xArr.add(x[i]+1);
                yArr.add(y[i]+1);
                
                xArr.add(x[i]+1);
                yArr.add(y[i]-1);
                
                xArr.add(x[i]-1);
                yArr.add(y[i]+1);
                
                xArr.add(x[i]-1);
                yArr.add(y[i]-1);
            }
            
            int[] xVal = new int[xArr.size()];
            int[] yVal = new int[yArr.size()];
            
            for(int k = 0; k < xArr.size(); k++){
                    xVal[k] = xArr.get(k);
                    yVal[k] = yArr.get(k);
            }

            x = xVal;
            y = yVal;    
            n = xArr.size();
            
            this.removeDuplicates(x, y, n);  
        }
            removeOriginalRegionPixels();  
            //System.out.println("PROFILING-DETAILED: old size: " + xOriginal.length + ", new size: " + x.length);
    }
    
    private void growRegion(int growPixels) {

        //no neighbor as criteria with 8-connected
        //int[] x = this.x; int[] y = this.y; 
        int n_local = this.n;

        //could use ArrayList here instead of dynamically reseting array size...
        
        //for now lets use the circumfrence of a circle with 
        //area n_local as an estimate.

        //double primary = 2*Math.PI*(Math.sqrt(n_local/Math.PI));
        //double secondary = 2*Math.PI*(Math.sqrt(n_local/Math.PI) + growPixels);   
        
        int maxsize = 5000;
        
        //IJ.log("MaxSize for microRegion: " + maxsize);
        //IJ.log("Region size: " + this.n);
        
        int[] x_local = new int[this.n*100];
        int[] y_local = new int[this.n*100];
        
        //List<int[]> resultPixels = new ArrayList<int[]>();

        

        System.arraycopy(this.x, 0, x_local, 0, this.n);
        System.arraycopy(this.y, 0, y_local, 0, this.n);

        int[] xGrow = new int[maxsize];
        int[] yGrow = new int[maxsize];
        int nGrow = 0;

        int xCurrent = 0;
        int yCurrent = 0;
        int xTest = 0;
        int yTest = 0;
        boolean NW = false;
        boolean N = false;
        boolean NE = false;
        boolean E = false;
        boolean SE = false;
        boolean S = false;
        boolean SW = false;
        boolean W = false;

//loop for number of pixels to grow 
        for (int p = 1; p <= growPixels; p++) {

            xCurrent = 0;
            yCurrent = 0;
            xTest = 0;
            yTest = 0;
            NW = false;
            N = false;
            NE = false;
            E = false;
            SE = false;
            S = false;
            SW = false;
            W = false;

//cycle through the existing pixels
            for (int i = 0; i <= n_local-1; i++) {
                //parse regions pixels
                xCurrent = x_local[i];
                yCurrent = y_local[i];
                for (int m = 0; m <= n_local-1; m++) {
                    //check by 8-pt connectedness, if not in existing array, needs to be flipped.
                    xTest = x_local[m];
                    yTest = y_local[m];

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
                    //if any position contains a false then this is an edge pixel,  add pixels to result array
                    //NEED TO REMOVE OVERLAP....OR REMOVE COPYS FROM ARRAY...
                    //Could work off of a copy array.
                    //IJ.log("                              Found edge pixel...  " + xCurrent + " ," + yCurrent); 
                    if (xCurrent != 0 && yCurrent != 0){
                    
                    if (NW == true) {
                        xGrow[nGrow] = xCurrent - 1;
                        yGrow[nGrow] = yCurrent - 1;
                        nGrow++;
                    }
                    if (N == true) {
                        xGrow[nGrow] = xCurrent;
                        yGrow[nGrow] = yCurrent - 1;
                        nGrow++;
                    }
                    if (NE == true) {
                        xGrow[nGrow] = xCurrent + 1;
                        yGrow[nGrow] = yCurrent - 1;
                        nGrow++;
                    }
                    if (E == true) {
                        xGrow[nGrow] = xCurrent + 1;
                        yGrow[nGrow] = yCurrent;
                        nGrow++;
                    }
                    if (SE == true) {
                        xGrow[nGrow] = xCurrent + 1;
                        yGrow[nGrow] = yCurrent + 1;
                        nGrow++;
                    }
                    if (S == true) {
                        xGrow[nGrow] = xCurrent;
                        yGrow[nGrow] = yCurrent + 1;
                        nGrow++;
                    }
                    if (SW == true) {
                        xGrow[nGrow] = xCurrent - 1;
                        yGrow[nGrow] = yCurrent + 1;
                        nGrow++;
                    }
                    if (W == true) {
                        xGrow[nGrow] = xCurrent - 1;
                        yGrow[nGrow] = yCurrent;
                        nGrow++;
                    }
                    }
                    
                    //if this is a grow by x add to parent and result array and repeat x		
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
            //if looping multiple times for more than 1 pixel at 8 connected grow add grow to start region
            removeDuplicates(xGrow, yGrow, nGrow);
            x_local = x;
            y_local = y;
            n_local = n;
            //System.out.println("End_loop..." + p);
        }
//        removeOriginalRegionPixels();
//        for(int k = 0; k < n; k++){
//        System.out.println("Derived pixel (" + x[k] + "," + y[k] + ")");
//        }
//        for(int l = 0; l < nOriginal; l++){
//        System.out.println("Original pixel (" + xOriginal[l] + "," + yOriginal[l] + ")");
//        }

//        this.x = xGrow;
//        this.y = yGrow;
//        this.n = nGrow;
        //IJ.log("                               Start size...               " + (start_count));
        //IJ.log("                               Grow size...               " + (this.n));

    }

    private void dilateRegion(int numberTimes){       
    Polygon shape = new Polygon(x, y, n);
    Rectangle bounds = shape.getBounds();   
    bounds.grow(numberTimes+1, numberTimes+1); 
    
    }

    //calculated values-inherited from microRegion
    @Override
    public void calculateMeasurements(ImageStack stack) {
        
        int[] x_local = this.x;
        int[] y_local = this.y;
        int z_local = this.z;
        int n_local = this.n;
        
        //System.out.println("microDerivedRegion::calculateMeasurements      ...calculating for region " + this.getName() + ", from " + n + " pixels");
        
        long total = 0;
        double local_min = 0;
        double local_max = 0;
        
        if(n > 0){
        double[] deviation = new double[n];
        
        
        for (int i = 0; i <= n_local - 1; i++) {
            if(getVoxelBounds(stack,x_local[i], y_local[i], z_local) > -1){
            total = total + (long) stack.getVoxel(x_local[i], y_local[i], z_local);
            if (stack.getVoxel(x_local[i], y_local[i], z_local) < local_min) {
                local_min = stack.getVoxel(x_local[i], y_local[i], z_local);
            }
            if (stack.getVoxel(x_local[i], y_local[i], z_local) > local_max) {
                local_max = stack.getVoxel(x_local[i], y_local[i], z_local);
            deviation[i] = stack.getVoxel(x[i], y[i], z);
            }
        }
        this.deviation = deviation;
            //IJ.log("DerivedRegions max: " + local_max + "getVoxel: x " + x_local[i] + ", y " + y_local[i] + ", z" + z_local + " value: " + stack.getVoxel(x_local[i], y_local[i], z_local));
        }
        this.max = local_max;
        this.min = local_min;
        this.mean = total / n_local;
        this.integrated_density = total;      
        total = 0;
        int thresholdcount = 0;        
        for(int j = 0; j <= n_local - 1; j++) {
            if(getVoxelBounds(stack,x_local[j], y_local[j], z_local) > -1){
                if (stack.getVoxel(x_local[j], y_local[j], z_local) > local_max*this.thresholdformean){
                    total = total + (long) stack.getVoxel(x_local[j], y_local[j], z_local);
                    thresholdcount++;
            }  
            }
        }   
        this.thresholdedid = total;
        this.nThreshold = thresholdcount;
        if(thresholdcount > 0) {this.thresholdedmean = total/thresholdcount;}
        else {this.thresholdedmean = 0;}
        calculateCenter();
        } 
    }
    
        private double getVoxelBounds(ImageStack stack, int x, int y, int z){
        
        try{
            return stack.getVoxel(x, y, z);
        }catch(IndexOutOfBoundsException e){
            return -1;
        }
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
    }
    
    private void removeOriginalRegionPixels(){


            //IJ.log("Sizes derived, original:                                    (" + n + ", " + xOriginal.length + ")");

            ArrayList<Integer> xOrigArr = new ArrayList<Integer>();
            ArrayList<Integer> yOrigArr = new ArrayList<Integer>();
            
            ArrayList<Integer> xArr = new ArrayList<Integer>();
            ArrayList<Integer> yArr = new ArrayList<Integer>();
            
            for(int i = 0; i < nOriginal; i++){
                xOrigArr.add(xOriginal[i]);
                yOrigArr.add(yOriginal[i]);
            }
            

            
            for(int i = 0; i < n; i++){
                xArr.add(x[i]);
                yArr.add(y[i]);
            }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < nOriginal; j++) {            
                if ((xOrigArr.get(j) == xArr.get(i)) && (yOrigArr.get(j) == yArr.get(i))) {        
                    xArr.remove(i);
                    yArr.remove(i);
                    n--;       
                //IJ.log("Original pixel(" + xOrigArr.get(j) + ", " + yOrigArr.get(j) + ") == (" + xArr.get(i) + ", " + yArr.get(i) + ");  Pixels remaining: " + xArr.size() + ".");
                
                }
                else{
                 //IJ.log("Original pixel(" + xOrigArr.get(j) + ", " + yOrigArr.get(j) + ")  != (" + xArr.get(i) + ", " + yArr.get(i) + ");  Pixels remaining: " + xArr.size() + ".");   
                }
            }
        }

//        for (int k = 0; k <= n-1; k++) {
//            if ((x[k] + y[k]) != 0) {
//                resultX[counter] = x[k];
//                resultY[counter] = y[k];
//                IJ.log("Parsed pixels                                    (" + resultX[counter] + ", " + resultY[counter] + ")");
//                counter++;
//
//            }
//        }
        
            int[] resultX = new int[xArr.size()];
            int[] resultY = new int[yArr.size()];
            
            for(int i = 0; i < xArr.size(); i++ ){
                resultX[i] = xArr.get(i);
                resultY[i] = yArr.get(i);
            }
            
        
        
        this.x = resultX;
        this.y = resultY;
        this.n = resultX.length;
    }

    private void removeDuplicates(int[] x, int[] y, int n) {

        int counter = 0;
        int testX;
        int testY;
        int currentX;
        int currentY;
        int[] resultX = new int[n];
        int[] resultY = new int[n];

        for (int i = 0; i <= n - 1; i++) {
            currentX = x[i];
            currentY = y[i];
            //IJ.log("Current Pixel                                    (" + currentX + ", " + currentY + ")");
            for (int j = 0; j <= n - 1; j++) {
                testX = x[j];
                testY = y[j];
                //IJ.log("Test Pixel                                    (" + testX + ", " + testY + ")");
                if (currentX == testX) {
                    if (currentY == testY) {
                        if (i != j) {
                            x[j] = 0;
                            y[j] = 0;
                        }
                    }
                }
            }
        }

        for (int k = 0; k <= n-1; k++) {
            if ((x[k] + y[k]) != 0) {
                resultX[counter] = x[k];
                resultY[counter] = y[k];
                //IJ.log("Parsed pixels                                    (" + resultX[counter] + ", " + resultY[counter] + ")");
                counter++;

            }
        }
        this.x = resultX;
        this.y = resultY;
        this.n = counter;
    }

    private void setDerivedConstants() {

        Object[] result = new Object[9];

        for (int i = 0; i <= 3; i++) {

        }

//[0, count, 1, mean, 2, integrated density, 3, min, 4, max, 5 standard deviation, 6 Feret_AR, 7 Feret_Min, 8 Feret_Max]
        analysisResultsRegion = result;
    }

    //private retrival functions
    @Override
    public int isRegionIncluded() {
        return this.include;
    }

    @Override
    public int getPixelCount() {
        return this.n;
    }

    @Override
    public int[] getPixelsX() {
        return this.x;
    }

    @Override
    public int[] getPixelsY() {
        return this.y;
    }

    @Override
    public int getZPosition() {
        return this.z;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public double getMaxIntensity() {
        return this.max;
    }

    @Override
    public double getMinIntensity() {
        return this.min;
    }

    @Override
    public double getIntegratedIntensity() {
        return this.integrated_density;
    }

    @Override
    public double getMeanIntensity() {
        return this.mean;
    }

    public int getType() {
        return this.type;
    }

    public Object[] getDerivedConstants() {
        return this.analysisResultsRegion;
    }
    
    @Override
            public double[] getDeviations() {
        return this.deviation;
    }
            
    @Override
    public double getThresholdedIntegratedIntensity() {
        return this.thresholdedid;
    }
    
    @Override
    public double getThresholdedMeanIntensity() {
        return this.thresholdedmean;
    }
    @Override
    public void setThreshold(double threshold) {
        this.thresholdformean = threshold;
    }
    @Override
    public int getThresholdPixelCount() {
        return this.nThreshold;
    }

       


}
