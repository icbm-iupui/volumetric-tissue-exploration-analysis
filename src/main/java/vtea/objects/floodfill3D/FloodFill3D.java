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
package vtea.objects.floodfill3D;


import vtea.objects.layercake.microVolume;
import ij.*;
import ij.process.*;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import static java.util.concurrent.ForkJoinTask.invokeAll;
import java.util.concurrent.RecursiveAction;
import vteaobjects.MicroObject;



public class FloodFill3D implements Cloneable, java.io.Serializable {

    /**
     * Constants
     */

    /**
     * Variables
     */
    private  ImagePlus imageOriginal;
    private  ImagePlus imageResult;
    private  ImageStack stackOriginal;
    private ImageStack[] stackComplete;
    protected  ImageStack stackResult;
    
    private boolean watershedImageJ = true;

    private List<MicroObject> alVolumes = Collections.synchronizedList(new ArrayList<MicroObject>());


    private int[] minConstants; // 0: minObjectSize, 1: maxObjectSize, 2: minOverlap, 3: minThreshold

    private microVolume[] Volumes;
    private int nVolumes;

//derivedRegionType[][], [Channel][0, type, 1, subtype];

    /**
     * Constructors
     */
//empty cosntructor
    public FloodFill3D() {}
    
     public FloodFill3D(ImageStack[] is, int maskStack, int[] min, boolean imageOptimize) {

        minConstants = min;
        stackOriginal = is[maskStack];
        stackComplete = is;
        stackResult = stackOriginal.duplicate();

        for (int n = 0; n < stackResult.getSize(); n++) {
            for(int x = 0; x < stackResult.getWidth(); x++){
                for(int y = 0; y < stackResult.getHeight(); y++){
                    if(stackResult.getVoxel(x, y, n) <= minConstants[3]){
                        stackResult.setVoxel(x, y, n, (Math.pow(2,stackOriginal.getBitDepth()))-1);   
                    }else{
                        stackResult.setVoxel(x, y, n, 0);
                    }  
                }
            }                        
        } 
        imageResult = new ImagePlus("Mask Result", stackResult);
        IJ.run(imageResult, "8-bit", ""); 
        if(watershedImageJ){
            IJ.run(imageResult, "Watershed", "stack");
        }
        
        IJ.run(imageResult, "Invert", "stack");     
         

     makeRegions(imageResult.getStack(), maskStack);
  
     imageResult.setTitle("3D Floodfill Result");
     }

    /**
     * Methods
     */
     
    

    private void makeRegions(ImageStack stack, int maskStack) {
       
        int width = stack.getWidth();
        int height = stack.getHeight();
        int depth = stack.getSize();
        int color = 1;
        int nVolumes = 0;
        int serialID = 0;
        
        ArrayList<int[]> pixels = new ArrayList<int[]>();
        
        for(int z = 0; z < depth; z++){
            for(int x= 0; x < width; x++){
                for(int y = 0; y < height; y++){
                    if(stack.getVoxel(x, y, z) == 255){
                        pixels = floodfill(stack,x,y,z,width,height,depth,color, pixels);
                        if(pixels.size() > this.minConstants[0]){
                            this.alVolumes.add(new MicroObject(pixels, maskStack, stackComplete, serialID) {});
                            serialID++;
                        }
                        pixels = new ArrayList<int[]>();
                        color++;
                        nVolumes++;
                        if(color == 255){color = 1;}
                    }   
                }
            }
        }
        //System.out.println("PROFILING: 3D Flood Filling volumes: " + nVolumes);
        System.out.println("PROFILING: 3D Flood Filling volumes: " + alVolumes.size());
        //IJ.log("PROFILING: 3D Flood Filling volumes: " + nVolumes);
        IJ.log("PROFILING: 3D Flood Filling volumes: " + alVolumes.size());
    }
    
    public void makeDerivedRegions(){
    
    
    }
    
    
    
        public void makeDerivedRegionsPool(int[][] localDerivedRegionTypes, int channels, ImageStack[] Stack, ArrayList ResultsPointers) {
        
        FloodFill3D.DerivedRegionForkPool drf = new FloodFill3D.DerivedRegionForkPool(localDerivedRegionTypes, channels, Stack, ResultsPointers, 0, alVolumes.size());
 
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(drf);

    }
    
    
    private ArrayList<int[]> floodfill(ImageStack stack, int x, int y, int z, int width, int height, int depth, int color, ArrayList<int[]> pixels){
        if(x < 0 || y < 0 || z < 0 || x >= width || y >= height || z >= depth || stack.getVoxel(x, y, z) == 0 || stack.getVoxel(x, y, z) == color){
            return pixels;
        }
        //|| stack.getVoxel(x, y, z) == color
        //System.out.println("PROFILING: Adding point to object: " + color );
        stack.setVoxel(x, y, z, color);
        
        int[] pixel = new int[3];
        pixel[0] = x;
        pixel[1] = y;
        pixel[2] = z;
        
        pixels.add(pixel);
        
        pixels = floodfill(stack, x, y, z+1, width, height, depth, color, pixels);
        pixels = floodfill(stack, x+1, y, z+1, width, height, depth, color, pixels);
        pixels = floodfill(stack, x, y+1, z+1, width, height, depth, color, pixels);
        pixels = floodfill(stack, x+1, y+1, z+1, width, height, depth, color, pixels);
        pixels = floodfill(stack, x-1, y, z+1, width, height, depth, color, pixels);
        pixels = floodfill(stack, x, y-1, z+1, width, height, depth, color, pixels);
        pixels = floodfill(stack, x-1, y-1, z+1, width, height, depth, color, pixels);
        pixels = floodfill(stack, x-1, y+1, z+1, width, height, depth, color, pixels);
        pixels = floodfill(stack, x+1, y-1, z+1, width, height, depth, color, pixels);
//        pixels = floodfill(stack, x, y, z-1, width, height, depth, color, pixels);
//        pixels = floodfill(stack, x+1, y, z-1, width, height, depth, color, pixels);
//        pixels = floodfill(stack, x, y+1, z-1, width, height, depth, color, pixels);
//        pixels = floodfill(stack, x+1, y+1, z-1, width, height, depth, color, pixels);
//        pixels = floodfill(stack, x-1, y, z-1, width, height, depth, color, pixels);
//        pixels = floodfill(stack, x, y-1, z-1, width, height, depth, color, pixels);
//        pixels = floodfill(stack, x-1, y-1, z-1, width, height, depth, color, pixels);
//        pixels = floodfill(stack, x-1, y+1, z-1, width, height, depth, color, pixels);
//        pixels = floodfill(stack, x+1, y-1, z-1, width, height, depth, color, pixels);
        pixels = floodfill(stack, x+1, y, z, width, height, depth, color, pixels);
        pixels = floodfill(stack, x, y+1, z, width, height, depth, color, pixels);
        pixels = floodfill(stack, x+1, y+1, z, width, height, depth, color, pixels);
        pixels = floodfill(stack, x-1, y, z, width, height, depth, color, pixels);
        pixels = floodfill(stack, x, y-1, z, width, height, depth, color, pixels);
        pixels = floodfill(stack, x-1, y-1, z, width, height, depth, color, pixels);
        pixels = floodfill(stack, x-1, y+1, z, width, height, depth, color, pixels);
        pixels = floodfill(stack, x+1, y-1, z, width, height, depth, color, pixels);

        return pixels;

    }
    
    @Deprecated
    
    private int dilatefill3D(ImageStack stack, int x, int y, int z, int width, int height, int depth, double color){
        
        
         if(x < 0 || y < 0 || z < 0 || x >= width || y >= height || z >= depth || stack.getVoxel(x, y, z) == 0 || stack.getVoxel(x, y, z) == color){
            return 0;
        }
        
        stack.setVoxel(x, y, z, color);
        
        dilatefill3D(stack, x, y, z+1, width, height, depth, color);
        dilatefill3D(stack, x+1, y, z+1, width, height, depth, color);
        dilatefill3D(stack, x, y+1, z+1, width, height, depth, color);
        dilatefill3D(stack, x+1, y+1, z+1, width, height, depth, color);
        dilatefill3D(stack, x-1, y, z+1, width, height, depth, color);
        dilatefill3D(stack, x, y-1, z+1, width, height, depth, color);
        dilatefill3D(stack, x-1, y-1, z+1, width, height, depth, color);
        dilatefill3D(stack, x-1, y+1, z+1, width, height, depth, color);
        dilatefill3D(stack, x+1, y-1, z+1, width, height, depth, color);
//        floodfill3D(stack, x, y, z-1, width, height, depth, color);
//        floodfill3D(stack, x+1, y, z-1, width, height, depth, color);
//        floodfill3D(stack, x, y+1, z-1, width, height, depth, color);
//        floodfill3D(stack, x+1, y+1, z-1, width, height, depth, color);
//        floodfill3D(stack, x-1, y, z-1, width, height, depth, color);
//        floodfill3D(stack, x, y-1, z-1, width, height, depth, color);
//        floodfill3D(stack, x-1, y-1, z-1, width, height, depth, color);
//        floodfill3D(stack, x-1, y+1, z-1, width, height, depth, color);
//        floodfill3D(stack, x+1, y-1, z-1, width, height, depth, color);
        dilatefill3D(stack, x+1, y, z, width, height, depth, color);
        dilatefill3D(stack, x, y+1, z, width, height, depth, color);
        dilatefill3D(stack, x+1, y+1, z, width, height, depth, color);
        dilatefill3D(stack, x-1, y, z, width, height, depth, color);
        dilatefill3D(stack, x, y-1, z, width, height, depth, color);
        dilatefill3D(stack, x-1, y-1, z, width, height, depth, color);
        dilatefill3D(stack, x-1, y+1, z, width, height, depth, color);
        dilatefill3D(stack, x+1, y-1, z, width, height, depth, color);

        return 1;
    }
    
 
    private float maxPixel(ImageStack stack) {
        float max = this.minConstants[0];	
        for (int n = 0; n <= stack.getSize() - 1; n++) {
            ImageProcessor ipStack = stack.getProcessor(n + 1);
            if (ipStack.getMax() > max) {
                max = (float) ipStack.getMax();
            }
        }
        return max;
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



    private int[] calculateCartesian(int pixel, int width, int slice) {
        int[] result = new int[3];
        result[1] = (int) Math.ceil(pixel / width);
        result[0] = pixel - (result[1] * width);
        result[2] = slice - 1;
        return result;
    }

    private int calculateLinear(int x, int y, int width) {
        int result = (width * y) - (width - x);
        return result;
    }

    private double lengthCart(double[] position, double[] reference_pt) {
        double distance;
        double part0 = position[0] - reference_pt[0];
        double part1 = position[1] - reference_pt[1];
        distance = Math.sqrt((part0 * part0) + (part1 * part1));
        return distance;
    }
    
    public void setWaterShedImageJ(boolean b) {
        this.watershedImageJ = b;
    }

    public microVolume[] getVolumes() {
        return this.Volumes;
    }

    public int getVolumesCount() {
        return this.alVolumes.size();
    }

    public ImagePlus getMaskImage() {
        return this.imageResult;
    }

    public ImagePlus getOriginalImage() {
        return this.imageOriginal;
    }

    public ArrayList getVolumesAsList() {
        return new ArrayList(alVolumes);
    }

   

    private class DerivedRegionForkPool extends RecursiveAction {
        //class splits it self into new classes...  start with largest start and stop and subdivided recursively until start-stop is the number for the number of cores or remaineder.
        private int[][] derivedRegionType;
        int channels;
        ImageStack[] stack;
        ArrayList ResultsPointers;
        int stop;
        int start;
        List<microVolume> volumes = Collections.synchronizedList(new ArrayList<microVolume>());
        
        //Thread t;
        //private String threadName = "derivedregionmaker_" + System.nanoTime();
        
        DerivedRegionForkPool(int[][] ldrt, int c, ImageStack[] st, ArrayList rp, int start, int stop) {
            derivedRegionType = ldrt;
            channels = c;
            stack = st;
            ResultsPointers = rp;
            this.stop = stop;
            this.start = start;
            
            //System.out.println("PROFILING-DETAILS: ForkJoin Start and Stop points:" + start + ", " + stop);
            //volumes = alVolumes.subList(start, stop);
        }
        
        
        
        
        private void defineDerivedRegions(){
            ListIterator<MicroObject> itr = alVolumes.listIterator(start);
            int i = start;
            while(itr.hasNext() && i<=stop){
                MicroObject mv = new MicroObject() {};
                mv = itr.next();
                
                mv.makeDerivedRegions(derivedRegionType, channels, stack, ResultsPointers);
                //System.out.println("PROFILING: making derived regions.  " + mv.getName() + ", getting " + mv.getNDRegions() + " derived regions and " + mv.getderivedConstants()[1][0]+ "  Giving: " + mv.getAnalysisResultsVolume()[0][2]);
                i++;
            }
        }
        
        @Override
        protected void compute() {
            
            int processors = Runtime.getRuntime().availableProcessors();
            
            //int processors = 1;
            
            int length = alVolumes.size()/processors;
            
            if(alVolumes.size() < processors){
                length = alVolumes.size();
            }
            
            
            // System.out.println("PROFILING-DETAILS: Derived Regions Making ForkJoin Start and Stop points:" + start + ", " + stop + " for length: " + (stop-start) + " and target length: " + length);
            
            if(stop-start > length){
                invokeAll(new DerivedRegionForkPool(derivedRegionType, channels, stack, ResultsPointers, start, start+((stop-start)/2)),
                        new DerivedRegionForkPool(derivedRegionType, channels, stack, ResultsPointers, start+((stop-start)/2)+1, stop));
                //System.out.println("PROFILING-DETAILS: ForkJoin Splitting...");
            }
            else{
                // System.out.println("PROFILING-DETAILS: ForkJoin Computing...");
                defineDerivedRegions();
            }
        }
    }

}
