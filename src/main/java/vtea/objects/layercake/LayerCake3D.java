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

import com.opencsv.CSVWriter;
import ij.*;
import ij.process.*;
import java.util.*;
import ij.ImagePlus;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;


public class LayerCake3D implements Cloneable, java.io.Serializable {

    /**
     * Constants
     */
    /**
     * Variables
     */
    private ImagePlus imageOriginal;
    private ImagePlus imageResult;
    private ImageStack stackOriginal;
    //protected ImageStack stackResult;

    private boolean watershedImageJ = true;

    private List<microRegion> alRegions = Collections.synchronizedList(new ArrayList<microRegion>());
    private List<microVolume> alVolumes = Collections.synchronizedList(new ArrayList<microVolume>());

    private List<microRegion> alRegionsProcessed = Collections.synchronizedList(new ArrayList<microRegion>());
    private List<microRegion> alRegionsParsing = Collections.synchronizedList(new ArrayList<microRegion>());

    private int[] minConstants; // 0: minObjectSize, 1: maxObjectSize, 2: minOverlap, 3: minThreshold

    private microVolume[] Volumes;
    private int nVolumes;

//derivedRegionType[][], [Channel][0, type, 1, subtype];
    /**
     * Constructors
     */
//empty cosntructor
    public LayerCake3D() {

    }
//constructor for volume building

    public LayerCake3D(List<microRegion> Regions, int[] minConstants, ImageStack orig) {

        this.stackOriginal = orig;
        this.minConstants = minConstants;
        this.alRegions = Regions;
        this.nVolumes = 0;
        Collections.sort(alRegions, new ZComparator());
        VolumeForkPool vf = new VolumeForkPool(alRegions, minConstants, 0, alRegions.size() - 1);
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(vf);
    }

//constructor for region building
    public LayerCake3D(ImageStack stack, int[] min, boolean imageOptimize) {

        minConstants = min;
        stackOriginal = stack;
        imageOriginal = new ImagePlus("Mask", stack);

        ImageStack stackResult = stack.duplicate();

        //System.out.println("PROFILING: parsing stack of dimensions: z, " + stackResult.getSize() + " for a threshold of " + minConstants[3]);

        for (int n = 0; n < stackResult.getSize(); n++) {
            for (int x = 0; x < stackResult.getWidth(); x++) {
                for (int y = 0; y < stackResult.getHeight(); y++) {
                    if (stackResult.getVoxel(x, y, n) >= minConstants[3]) {
                        stackResult.setVoxel(x, y, n, (Math.pow(2, stackResult.getBitDepth())) - 1);
                    } else {
                        stackResult.setVoxel(x, y, n, 0);
                    }
                }
            }
        }

        imageResult = new ImagePlus("Mask Result", stackResult);
        IJ.run(imageResult, "8-bit", "");
        if (watershedImageJ) {
            IJ.run(imageResult, "Watershed", "stack");
        }
        IJ.run(imageResult, "Invert", "stack");

        //imageResult.show();
        makeRegionsPool(imageResult.getStack(), stackOriginal);
    }

    @Deprecated
    public synchronized void cleanupVolumes() {

        //loop through all volumes
        List<microVolume> alVolumesTrim = Collections.synchronizedList(new ArrayList<microVolume>());

//        for(int i = 0; i < alVolumes.size(); i++){
//            try{
//                if(alVolumes.get(i).getCentroidZ()<0){
//                    alVolumes.remove(i);
//                }
//                }catch(NullPointerException e){alVolumes.remove(i);}
//        }
//
        Collections.sort(alVolumes, new ZObjectComparator());

        microVolume[] mvs = new microVolume[alVolumes.size()];

        mvs = alVolumes.toArray(mvs);
        microVolume testVolume;
        List<microRegion> testRegions;
        microRegion testRegion;
        microVolume referenceVolume;
        List<microRegion> referenceRegions;
        microRegion referenceRegion;
        int referenceZ;
        int[] assigned = new int[alVolumes.size()];

        for (int i = 0; i < assigned.length; i++) {
            assigned[i] = 0;
        }
        microVolume resultVolume = new microVolume();
        double testCentroid[] = new double[2];
        double referenceCentroid[] = new double[2];

        for (int i = 0; i < mvs.length; i++) {
            if (assigned[i] == 0) {
                referenceVolume = mvs[i];

                referenceRegions = referenceVolume.getRegions();
                referenceRegion = referenceRegions.get(referenceRegions.size() - 1);
                referenceCentroid[0] = referenceRegion.getBoundCenterX();
                referenceCentroid[1] = referenceRegion.getBoundCenterY();
                referenceZ = referenceRegion.getZPosition();

                //System.out.println("PROFILING-volume cleanup, on: " + mvs[i].getName() + " at: " + referenceZ + " and " + mvs[i].getNRegions() + " regions.");
                //System.out.println("PROFILING-checking for neighboring volumes: " + i);
                for (int j = 0; j < mvs.length; j++) {

                    testVolume = mvs[j];
                    testRegions = testVolume.getRegions();
                    //Collections.sort(testRegions, new ZComparator());
                    testRegion = testRegions.get(0);
                    testCentroid[0] = testRegion.getBoundCenterX();
                    testCentroid[1] = testRegion.getBoundCenterY();
                    if (i != j && assigned[j] != 1 && lengthCart(testCentroid, referenceCentroid) < minConstants[2] && testRegion.getZPosition() - referenceZ == 1) {
                        ListIterator<microRegion> testItr = testRegions.listIterator();
                        while (testItr.hasNext()) {
                            microRegion reg = testItr.next();
                            resultVolume.addRegion(new microRegion(reg.getPixelsX(), reg.getPixelsY(), reg.getPixelCount(), reg.getZPosition(), stackOriginal));
                        }
                        resultVolume.addRegions(referenceRegions);
                        resultVolume.addRegions(testRegions);
                        resultVolume.setName(referenceVolume.getName() + "_" + testVolume.getName());
                        assigned[i] = 1;
                        assigned[j] = 1;

                        //System.out.println("PROFILING-found a partner: " + mvs[j].getName() + " at z: " + testRegion.getZPosition() + " at, " + lengthCart(testCentroid, referenceCentroid) + " pixels.");
                    }
                    testVolume = new microVolume();
                }

                if (assigned[i] == 1) {
                    resultVolume.calculateVolumeMeasurements();
                    //System.out.println("PROFILING-calculated volume measures: " + resultVolume.getName() + ". Giving derived: " + resultVolume.getAnalysisResultsVolume()[0][2] + " for "+ resultVolume.getNRegions() + " regions.");
                    //System.out.println("PROFILING-calculated volume measures: " + resultVolume.getName() + ".  Giving region: " + resultVolume.getAnalysisMaskVolume()[2] + " for "+ resultVolume.getNRegions() + " regions.");
                    alVolumesTrim.add(resultVolume);
                    //System.out.println("PROFILING: Adding to list: " + resultVolume.getName());
                    resultVolume = new microVolume();
                    resultVolume.setName("");
                    referenceVolume = new microVolume();
                }
            }

        }

        for (int k = 0; k < mvs.length; k++) {
            if (assigned[k] == 0) {
                microVolume mv = new microVolume();
                mv = mvs[k];
                mv.calculateVolumeMeasurements();
                alVolumesTrim.add(mv);
                //System.out.println("PROFILING: Adding to list: " + mv.getName());
            }
        }

        alVolumes.clear();
        System.out.println("PROFILING: Volumes found: " + alVolumesTrim.size());
        alVolumes.addAll(alVolumesTrim);
    }

    @Deprecated
    private void defineVolumes() {
        int z;
        microVolume volume = new microVolume();
        double[] startRegion = new double[2];

        int nVolumesLocal = 0;

        microRegion test = new microRegion();

        //ArrayList<microRegion> regions = new ArrayList<microRegion>();
        alRegionsParsing.addAll(alRegions);

        for (int i = 0; i < alRegionsParsing.size(); i++) {
            test = alRegionsParsing.get(i);
            if (!test.isAMember()) {
                nVolumesLocal++;
                startRegion[0] = test.getBoundCenterX();
                startRegion[1] = test.getBoundCenterY();
                test.setMembership(nVolumesLocal);
                test.setAMember(true);
                z = test.getZPosition();
                alRegionsProcessed.add(test);
                findConnectedRegions(nVolumesLocal, startRegion, z);
            }
        }

        for (int j = 1; j <= nVolumesLocal; j++) {
            volume = new microVolume();
            volume.setName("vol_" + j);
            Iterator<microRegion> vol = alRegionsProcessed.listIterator();
            microRegion region = new microRegion();
            while (vol.hasNext()) {
                region = vol.next();
                if (j == region.getMembership()) {
                    volume.addRegion(region);
                }
            }
            if (volume.getNRegions() > 0) {
                volume.calculateVolumeMeasurements();
                if (volume.getPixelCount() >= minConstants[0]) {
                    alVolumes.add(volume);
                }
            }
        }
    }

    private void findConnectedRegions(int volumeNumber, double[] startRegion, int z) {

        double[] testRegion = new double[2];

        microRegion test = new microRegion();
        for (int i = 0; i < alRegionsParsing.size(); i++) {
            test = alRegionsParsing.get(i);
            testRegion[0] = test.getBoundCenterX();
            testRegion[1] = test.getBoundCenterY();
            double comparator = lengthCart(startRegion, testRegion);
            if (!test.isAMember()) {
                if (comparator <= minConstants[2] && ((test.getZPosition() - z) < 3)) {
                    test.setMembership(volumeNumber);
                    test.setAMember(true);
                    z = test.getZPosition();
                    testRegion[0] = (testRegion[0] + startRegion[0]) / 2;
                    testRegion[1] = (testRegion[1] + startRegion[1]) / 2;
                    alRegionsProcessed.add(test);
                    alRegionsParsing.remove(i);
                    findConnectedRegions(volumeNumber, testRegion, z);
                    //System.out.println("PROFILING: Adding regions: " + i);
                }
            }

        }
    }

    /**
     * Methods
     */
    private void makeRegionsPool(ImageStack stack, ImageStack original) {
        RegionForkPool rrf = new RegionForkPool(stack, original, 0, stack.getSize());
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(rrf);
    }

    private double[] check8Neighbors(ImageStack stack, int[] point, double counter) {
        double[] result = new double[2];
        int x = point[0];
        int y = point[1];
        int z = point[2];
        double[] neighbors = new double[10];

        //N
        try {
            neighbors[0] = stack.getVoxel(x, y + 1, z);
        } catch (NullPointerException e) {
            neighbors[0] = 0;
        }
        //NE
        try {
            neighbors[1] = stack.getVoxel(x + 1, y + 1, z);
        } catch (NullPointerException e) {
            neighbors[1] = 0;
        }
        //E
        try {
            neighbors[2] = stack.getVoxel(x, y + 1, z);
        } catch (NullPointerException e) {
            neighbors[2] = 0;
        }
        //SE
        try {
            neighbors[3] = stack.getVoxel(x - 1, y + 1, z);
        } catch (NullPointerException e) {
            neighbors[3] = 0;
        }
        //S
        try {
            neighbors[4] = stack.getVoxel(x, y - 1, z);
        } catch (NullPointerException e) {
            neighbors[4] = 0;
        }
        //SW
        try {
            neighbors[5] = stack.getVoxel(x - 1, y - 1, z);
        } catch (NullPointerException e) {
            neighbors[5] = 0;
        }
        //W
        try {
            neighbors[6] = stack.getVoxel(x - 1, y, z);
        } catch (NullPointerException e) {
            neighbors[6] = 0;
        }
        //NW
        try {
            neighbors[7] = stack.getVoxel(x - 1, y + 1, z);
        } catch (NullPointerException e) {
            neighbors[7] = 0;
        }
        //up
        try {
            neighbors[8] = stack.getVoxel(x, y, z + 1);
        } catch (NullPointerException e) {
            neighbors[7] = 0;
        }
        //down
        try {
            neighbors[9] = stack.getVoxel(x, y, z - 1);
        } catch (NullPointerException e) {
            neighbors[7] = 0;
        }

        //parse neighbors array
        double tag = counter;
        for (int i = 0; i <= 10; i++) {
            if (neighbors[i] > 255) {
                tag = neighbors[i];
            }
            if (neighbors[i] == 255) {
                tag = counter++;
            }
        }

        result[0] = tag;
        result[1] = counter;

        return result;
    }

    public void makeDerivedRegions(int[][] localDerivedRegionTypes, int channels, ImageStack[] stack, ArrayList ResultsPointers) {
        ListIterator<microVolume> itr = alVolumes.listIterator();
        while (itr.hasNext()) {
            microVolume mv = new microVolume();
            mv = itr.next();
            mv.makeDerivedRegions(localDerivedRegionTypes, channels, stack, ResultsPointers);
        }
    }

    public void makeDerivedRegionsThreading(int[][] localDerivedRegionTypes, int channels, ImageStack[] Stack, ArrayList ResultsPointers) {

        int processors = Runtime.getRuntime().availableProcessors();
        int length = alVolumes.size() / processors;
        int remainder = alVolumes.size() % processors;

        int start = 0;
        int stop = start + length - 1;

        CopyOnWriteArrayList<DerivedRegionWorker> rw = new CopyOnWriteArrayList<DerivedRegionWorker>();

        for (int i = 0; i < processors; i++) {
            ArrayList<microVolume> volume = new ArrayList<microVolume>();
            if (i == processors - 1) {
                synchronized (alVolumes) {
                    //ListIterator<microVolume> itr = alVolumes.listIterator(start);
                    //DerivedRegionWorker region = new DerivedRegionWorker(localDerivedRegionTypes, channels, Stack, ResultsPointers, itr, stop);
                    ArrayList<microVolume> process = new ArrayList<microVolume>();
                    process.addAll(alVolumes.subList(start, stop));

                    DerivedRegionWorker region = new DerivedRegionWorker(localDerivedRegionTypes, channels, Stack, ResultsPointers, process, stop);
                    rw.add(region);
                }
                //IJ.log("RegionFactory::makeDerivedRegion Created thread #"+i +" for volumes: " + start + " to " + stop + ", " + volume.size() + " total.");

                start = stop + 1;
                stop = stop + length + remainder;
            } else {
                synchronized (alVolumes) {
                    //ListIterator<microVolume> itr = alVolumes.listIterator(start);
                    //DerivedRegionWorker region = new DerivedRegionWorker(localDerivedRegionTypes, channels, Stack, ResultsPointers, itr, stop);
                    ArrayList<microVolume> process = new ArrayList<microVolume>();
                    process.addAll(alVolumes.subList(start, stop));
                    DerivedRegionWorker region = new DerivedRegionWorker(localDerivedRegionTypes, channels, Stack, ResultsPointers, process, stop);
                    rw.add(region);
                }
                //IJ.log("RegionFactory::makeDerivedRegion Created thread #"+i +" for volumes: " + start + " to " + stop + ", " + volume.size() + " total.");

                start = stop + 1;
                stop = start + length;
            }
        }
        ListIterator<DerivedRegionWorker> itr = rw.listIterator();
        while (itr.hasNext()) {
            itr.next().start();
        }
    }

    public void makeDerivedRegionsPool(int[][] localDerivedRegionTypes, int channels, ImageStack[] Stack, ArrayList ResultsPointers) {

        DerivedRegionForkPool drf = new DerivedRegionForkPool(localDerivedRegionTypes, channels, Stack, ResultsPointers, 0, alVolumes.size());
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(drf);

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

    @Deprecated
    private ImageStack optimizeMask(ImageStack inputStack) {
        int localwidth = inputStack.getWidth();
        int localheight = inputStack.getHeight();

        ImagePlus localImp = new ImagePlus(null, inputStack);

        IJ.run(localImp, "Subtract Background...", "rolling=50 stack");

        StackProcessor sp = new StackProcessor(localImp.getStack());
        ImageStack s1 = sp.resize(localwidth * 2, localheight * 2, true);
        localImp.setStack(null, s1);

        IJ.run(localImp, "Median...", "radius=2 stack");

        sp = new StackProcessor(localImp.getStack());
        ImageStack s2 = sp.resize(localwidth, localheight, true);

        localImp.setStack("Mask Channel-Optimized", s2);
        localImp.show();

        return s2;
    }

    @Deprecated
    private int[] calculateCartesian(int pixel, int width, int slice) {
        int[] result = new int[3];
        result[1] = (int) Math.ceil(pixel / width);
        result[0] = pixel - (result[1] * width);
        result[2] = slice - 1;
        return result;
    }

    @Deprecated
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

    public List<microRegion> getRegions() {
        return alRegions;
    }

    public int getRegionsCount() {
        return this.alRegions.size();
    }

    public int getProcessedRegionsCount() {
        return this.alRegionsProcessed.size();
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

    //private class VolumeSwingWorker extends SwingWorker {}
    private class DerivedRegionWorker implements Runnable {

        private int[][] derivedRegionType;
        int channels;
        ImageStack[] stack;
        ArrayList ResultsPointers;
        ArrayList<microVolume> Volumes;
        int stop;
        ListIterator<microVolume> itr;
        Thread t;
        private String threadName = "derivedregionmaker_" + System.nanoTime();

        DerivedRegionWorker(int[][] ldrt, int c, ImageStack[] st, ArrayList rp, ListIterator<microVolume> litr, int s) {
            this.derivedRegionType = ldrt;
            channels = c;
            stack = st;
            ResultsPointers = rp;
            stop = s;
            itr = litr;
        }

        DerivedRegionWorker(int[][] ldrt, int c, ImageStack[] st, ArrayList rp, ArrayList<microVolume> vols, int s) {
            this.derivedRegionType = ldrt;
            channels = c;
            stack = st;
            ResultsPointers = rp;
            Volumes = vols;
            stop = s;
            itr = vols.listIterator();
        }

        @Override
        public void run() {
            long start = System.nanoTime();
            defineDerivedRegions();
            long end = System.nanoTime();
            System.out.println("PROFILING: Thread: " + threadName + " runtime: " + ((end - start) / 1000000) + " ms.");
        }

        public void start() {
            t = new Thread(this, threadName);
            t.setPriority(Thread.MAX_PRIORITY);
            t.start();
            try {
                t.join();
            } catch (Exception e) {
                System.out.println("PROFILING: Thread " + threadName + " interrupted.");
            }

        }

        private void defineDerivedRegions() {
            while (itr.hasNext()) {
                microVolume mv = new microVolume();
                mv = itr.next();
                mv.makeDerivedRegions(derivedRegionType, channels, stack, ResultsPointers);
            }
        }
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

        private void defineDerivedRegions() {
            ListIterator<microVolume> itr = alVolumes.listIterator(start);
            int i = start;
            while (itr.hasNext() && i <= stop) {
                microVolume mv = new microVolume();
                mv = itr.next();

                mv.makeDerivedRegions(derivedRegionType, channels, stack, ResultsPointers);
                //System.out.println("PROFILING: making derived regions.  " + mv.getName() + ", getting " + mv.getNDRegions() + " derived regions and " + mv.getderivedConstants()[1][0]+ "  Giving: " + mv.getAnalysisResultsVolume()[0][2]);
                i++;
            }
        }

        @Override
        protected void compute() {

            //int processors = Runtime.getRuntime().availableProcessors();
            
            
            double stackSize = stack.length*(stack[0].size() * stack[0].getHeight() * stack[0].getWidth() * stack[0].getBitDepth());
            
            //long processors = vtea._vtea.getPossibleThreads(stackSize);
            
            long processors = Runtime.getRuntime().availableProcessors();
            
            
            long length = alVolumes.size() / processors;

            if (alVolumes.size() < processors) {
                length = alVolumes.size();
            }
            
            if (length > processors){
                length = processors;
            }
            
            //System.out.println("PROFILING: Derived region calculation on " + length + " threads.");

            //System.out.println("PROFILING-DETAILS: Derived Regions Making ForkJoin Start and Stop points:" + start + ", " + stop + " for length: " + (stop-start) + " and target length: " + length);
            if (stop - start > length) {
                invokeAll(new DerivedRegionForkPool(derivedRegionType, channels, stack, ResultsPointers, start, start + ((stop - start) / 2)),
                        new DerivedRegionForkPool(derivedRegionType, channels, stack, ResultsPointers, start + ((stop - start) / 2) + 1, stop));
                //System.out.println("PROFILING-DETAILS: ForkJoin Splitting...");
            } else {
                //System.out.println("PROFILING-DETAILS: ForkJoin Computing...");
                defineDerivedRegions();
            }
        }
    }

    private class RegionForkPool extends RecursiveAction {

        private int maxsize = 1;
        private ArrayList<microRegion> alResult = new ArrayList<microRegion>();
        private ArrayList<microRegion> alResultCopy = new ArrayList<microRegion>();
        private int[] start_pixel = new int[3];
        int x, y, z;

        ArrayList<Integer> x_positions = new ArrayList<Integer>();
        ArrayList<Integer> y_positions = new ArrayList<Integer>();

        int n_positions = 0;
        int[] BOL = new int[5000];  //start of line position
        int[] EOL = new int[5000];  //end of line position
        int[] row = new int[5000];  //line position

        int count = 0;
        private ImageStack stack;
        private ImageStack original;
        private int start;
        private int stop;

        private Thread t;
        private String threadName = "regionfinder_" + System.nanoTime();

        RegionForkPool(ImageStack st, ImageStack orig, int start, int stop) {

            stack = st;
            original = orig;

            this.start = start;
            this.stop = stop;
            maxsize = stack.getSize() * stack.getWidth() * stack.getHeight();
        }

        private void defineRegions() {

            int color = 1;
            int region = 0;
            ArrayList<int[]> pixels = new ArrayList<int[]>();

            for (int n = this.start; n <= this.stop; n++) {
                for (int p = 0; p < stack.getWidth(); p++) {
                    for (int q = 0; q < stack.getHeight(); q++) {
                        //start pixel selected if 255, new region

                        if (getVoxelBounds(stack, p, q, n) == 255) {
                            //System.out.println("PROFILING: region start: " + region);
                            pixels = floodfill(stack, p, q, n, stack.getWidth(), stack.getHeight(), stack.getSize(), color, pixels);

                            //System.out.println("PROFILING: region size: " + pixels.size());
                            //microRegion(int[] x, int[] y, int n, int z, ImageStack stack)
                            int[] pixel = new int[3];
                            int[] xPixels = new int[pixels.size()];
                            int[] yPixels = new int[pixels.size()];
                            int j = 0;

                            ListIterator<int[]> itr = pixels.listIterator();
                            //unpack the arraylist
                            while (itr.hasNext()) {
                                pixel = itr.next();
                                xPixels[j] = pixel[0];
                                yPixels[j] = pixel[1];
                                j++;
                            }

                            // 0: minObjectSize, 1: maxObjectSize, 2: minOverlap, 3: minThreshold
                            //if (xPixels.length > (int) minConstants[0] && xPixels.length < (int) minConstants[1]) {
                            alResult.add(new microRegion(xPixels, yPixels, xPixels.length, n, original));
                            //alResultCopy.add(new microRegion(xPixels, yPixels, xPixels.length, n, original));
                            //}

                            if (color < 253) {
                                color++;
                            } else {
                                color = 1;
                            }

                            n_positions = 0;
                            count = 0;
                            region++;
                            pixels.clear();
                        }
                    }
                }
            }
            System.out.println("PROFILING: ...Regions found in thread:  " + alResult.size());

        }

        private ArrayList<int[]> floodfill(ImageStack stack, int x, int y, int z, int width, int height, int depth, int color, ArrayList<int[]> pixels) {

            if (x < 0 || y < 0 || z < 0 || x >= width || y >= height || z >= depth || stack.getVoxel(x, y, z) < 255) {
                return pixels;
            } else {

                stack.setVoxel(x, y, z, color);

                int[] pixel = new int[3];
                pixel[0] = x;
                pixel[1] = y;
                pixel[2] = z;

                pixels.add(pixel);

                pixels = floodfill(stack, x + 1, y, z, width, height, depth, color, pixels);
                pixels = floodfill(stack, x, y + 1, z, width, height, depth, color, pixels);
                pixels = floodfill(stack, x + 1, y + 1, z, width, height, depth, color, pixels);
                pixels = floodfill(stack, x - 1, y, z, width, height, depth, color, pixels);
                pixels = floodfill(stack, x, y - 1, z, width, height, depth, color, pixels);
                pixels = floodfill(stack, x - 1, y - 1, z, width, height, depth, color, pixels);
                pixels = floodfill(stack, x - 1, y + 1, z, width, height, depth, color, pixels);
                pixels = floodfill(stack, x + 1, y - 1, z, width, height, depth, color, pixels);
            }
            return pixels;

        }

        private double getVoxelBounds(ImageStack stack, int x, int y, int z) {

            try {
                return stack.getVoxel(x, y, z);
            } catch (IndexOutOfBoundsException e) {
                return -1;
            }
        }

        private void setRegions() {
            alRegions.addAll(alResult);
        }

        public ArrayList<microRegion> getRegions() {
            return this.alResult;
        }

        public int[] convertPixelArrayList(List<Integer> integers) {
            int[] ret = new int[integers.size()];
            Iterator<Integer> iterator = integers.iterator();
            for (int i = 0; i < ret.length; i++) {
                ret[i] = iterator.next().intValue();
            }
            return ret;
        }

        @Override
        protected void compute() {

            long processors = Runtime.getRuntime().availableProcessors();
                                      
            long length = stack.getSize() / processors;

            if (stack.getSize() < processors) {
                length = stack.getSize();
            }
            if (stop - start > length) {

                invokeAll(new RegionForkPool(stack, original, start, start + ((stop - start) / 2)),
                        new RegionForkPool(stack, original, start + ((stop - start) / 2) + 1, stop));

            } else {
                defineRegions();
                setRegions();
            }
        }
    }

    private class VolumeForkPool extends RecursiveAction {

        private int start;
        private int stop;

        private List<microRegion> alRegionsLocal = Collections.synchronizedList(new ArrayList<microRegion>());
        private List<microRegion> alRegionsProcessedLocal = Collections.synchronizedList(new ArrayList<microRegion>());
        
        
        private short[][] linkProbability;
        
        
        //private List<microRegion> alRegionsLocal = Collections.synchronizedList(new ArrayList<microRegion>());

        private int[] minConstantsLocal;

        private int nVolumesLocal;

        public VolumeForkPool(List<microRegion> Regions, int[] minConstants, int start, int stop) {
            this.minConstantsLocal = minConstants;
            this.alRegionsLocal = Regions;
            this.nVolumesLocal = 0;
            this.start = start;
            this.stop = stop;
            
//SKETCH for adding probabilistic volume building
            
//            linkProbability = new short[alRegionsLocal.size()][alRegionsLocal.size()];
//            
//            
//            calculateProbabilities((float)2);
//            
//            try {
//                CSVWriter writer = new CSVWriter(new FileWriter("yourfile.csv"));
//                
//            
//            String[] b = new String[linkProbability.length];
//            for(int j = 0; j < alRegionsLocal.size(); j++){
//                for ( int i = 0; i < alRegionsLocal.size(); i++) {
//                    b[i] = String.valueOf(linkProbability[j][i]); 
//                }
//                writer.writeNext(b);
//            }
//            
//
//            
//            
//            
//            writer.close();
//            } catch (IOException ex) {
//                Logger.getLogger(LayerCake3D.class.getName()).log(Level.SEVERE, null, ex);
//            }
//           
     
            
        }
        
        private synchronized void calculateProbabilities(double sigma) {
            
        long start = System.currentTimeMillis();
        
        for(int i = 0; i < alRegionsLocal.size(); i++){
                int[] testStartRegion = new int[3]; 
                microRegion test;
                test = alRegions.get(i);
                testStartRegion[0] = test.getBoundCenterX();
                testStartRegion[1] = test.getBoundCenterY();
                testStartRegion[2] = test.getZPosition();
                float sumCost = 0;
                
            for(int j = 0; j < alRegionsLocal.size(); j++){ 
                if(j != i){
                int[] compareStartRegion = new int[3];
                microRegion compare;
                compare = alRegions.get(j);
                compareStartRegion[0] = compare.getBoundCenterX();
                compareStartRegion[1] = compare.getBoundCenterY();
                compareStartRegion[2] = compare.getZPosition();
                short cost = costCalculation(testStartRegion[0], compareStartRegion[0],1, testStartRegion[1], 
                    compareStartRegion[1], 1, testStartRegion[2], compareStartRegion[2],  0.8, sigma);
                linkProbability[i][j] = cost;
                sumCost = sumCost + cost;
                }else{
                    
                }
            }
            for(int k = 0; k < alRegionsLocal.size(); k++){ 
                linkProbability[i][k] = (short)(100*(linkProbability[i][k]/sumCost));
            }
           
        }
        
        
{
        short probablity = 0;
        for(int i=0; i<(linkProbability.length/2 + 1); i++){
            for(int j=i; j<(linkProbability[0].length); j++){
                probablity = linkProbability[i][j];
                linkProbability[i][j] = linkProbability[j][i];
                linkProbability[j][i] = probablity;
            }
        }

    }

        long stop = System.currentTimeMillis();
         //linkProbability = MatrixUtils.createRealMatrix(linkProbability).transpose().getData();   
         System.out.println("PROFILING: Probability matrix time: " + (stop-start) + " ms for a " + linkProbability.length + " matrix.");
        }
 
        private synchronized short costCalculation(int x1, int x2, double xw, int y1, int y2, double yw, int z1, int z2, double zw, double sigma) {
            //Gaussian with sigma
            //return (Math.exp(-(xw*(Math.pow(x1-x2, 2))+yw*(Math.pow(y1-y2, 2))+zw*(Math.pow(z1-z2, 2))))/Math.pow(2*sigma,2));
            //t-distribution 1 DOF
            return (short)(100*(float)(1/(1+(((Math.pow(x1-x2, 2))+yw*(Math.pow(y1-y2, 2))+zw*(Math.pow(z1-z2, 2)))))));
        }

        private synchronized void volumeBuild(){
            
            microVolume volume = new microVolume();
            
            for(int i = 0; i < alRegionsLocal.size(); i++){
                if(!(alRegionsLocal.get(i).isAMember())){
                    alRegionsLocal.get(i).setMembership(i);
                    volume = parseProbabilities(i,5);
                    if (volume.getNRegions() > 1) {
                        volume.calculateVolumeMeasurements();
                        if (volume.getPixelCount() >= minConstantsLocal[0] && volume.getPixelCount() <= minConstantsLocal[1]) {
                            alVolumes.add(volume);
                        }
                    }
                }
            }
        }
        
        private synchronized microVolume parseProbabilities(int parent,double cutoff){           
            microVolume volume = new microVolume();   
            for(int i = 0; i < alRegionsLocal.size(); i++){
                if (linkProbability[i][parent] > cutoff && !(alRegionsLocal.get(i).isAMember())){                  
                    volume.addRegion(alRegionsLocal.get(i));   
                    alRegionsLocal.get(i).setMembership(parent);
                    System.out.println("PROFILING: Scanning Graph, Node: " + parent + ", position: " + i);
                    parseRecursive(volume, parent, i, cutoff);
                } 
            }
            return volume;
        }
        
        private synchronized void parseRecursive(microVolume volume, int parent, int child, double cutoff){            
            for(int i = 0; i < alRegionsLocal.size(); i++){
                if (linkProbability[i][child] > cutoff && !(alRegionsLocal.get(i).isAMember())){                  
                    volume.addRegion(alRegionsLocal.get(i));   
                    alRegionsLocal.get(i).setMembership(parent);
                    System.out.println("PROFILING: Scanning Graph, Node: " + parent + ", position: " + i);
                    parseRecursive(volume, parent, i,cutoff);
                } 
            }
        }
        
        private void resetMembership(int position, int newposition){
            for(int i = 0; i < alRegionsLocal.size(); i++){
                if(alRegionsLocal.get(i).getMembership() == position){
                    alRegionsLocal.get(i).setMembership(newposition);
                }
            }
        }

        @Override
        protected void compute() {

            //multi-threading was leading to over segmentation errors.  One thread slows things down but is more accurate.
            //long processors = Runtime.getRuntime().availableProcessors();
            long processors = 1;
            
            long length = alRegions.size() / processors;

            if (alRegions.size() < processors) {
                length = alRegions.size();
            }

            //System.out.println("PROFILING-DETAILS: Volume Making ForkJoin Start and Stop points:" + start + ", " + stop + " for length: " + (stop-start) + " and target length: " + length);
            if (stop - start > length) {
               
                invokeAll(new VolumeForkPool(alRegions, minConstantsLocal, start, start + ((stop - start) / 2)),
                        new VolumeForkPool(alRegions, minConstantsLocal, start + ((stop - start) / 2) + 1, stop));
               
            } else {
                defineVolumes();
                //volumeBuild();
            }
        }
        
        
        private synchronized void defineVolumes() {
            int z;
            microVolume volume = new microVolume();
            double[] startRegion = new double[2];

            microRegion test = new microRegion();

            int i = start;

            while (i < stop) {
                test = alRegions.get(i);
                if (!test.isAMember()) {
                    nVolumesLocal++;
                    startRegion[0] = test.getBoundCenterX();
                    startRegion[1] = test.getBoundCenterY();
                    test.setMembership(nVolumesLocal);
                    test.setAMember(true);
                    z = test.getZPosition();
                    alRegionsProcessedLocal.add(test);
                    findConnectedRegions(nVolumesLocal, startRegion, z);
                }
                i++;
            }

            for (int j = 1; j <= this.nVolumesLocal; j++) {
                volume = new microVolume();
                volume.setName("vol_" + j);
                Iterator<microRegion> vol = alRegionsProcessedLocal.listIterator();
                microRegion region = new microRegion();
                while (vol.hasNext()) {
                    region = vol.next();
                    if (j == region.getMembership()) {
                        volume.addRegion(region);
                    }
                }
                if (volume.getNRegions() > 0) {
                    volume.calculateVolumeMeasurements();
                    if (volume.getPixelCount() >= minConstantsLocal[0] && volume.getPixelCount() <= minConstantsLocal[1]) {
                        alVolumes.add(volume);
                    }
                }
            }
        }

//        @Override
//        protected void compute() {
//
//            //multi-threading was leading to over segmentation errors.  One thread slows things down but is more accurate.
//            int processors = 1;
//            int length = alRegions.size() / processors;
//
//            if (alRegions.size() < processors) {
//                length = alRegions.size();
//            }
//
//            //System.out.println("PROFILING-DETAILS: Volume Making ForkJoin Start and Stop points:" + start + ", " + stop + " for length: " + (stop-start) + " and target length: " + length);
//            if (stop - start > length) {
//               
//                invokeAll(new VolumeForkPool(alRegions, minConstantsLocal, start, start + ((stop - start) / 2)),
//                        new VolumeForkPool(alRegions, minConstantsLocal, start + ((stop - start) / 2) + 1, stop));
//               
//            } else {
//                defineVolumes();
//            }
//        }

        private synchronized void findConnectedRegions(int volumeNumber, double[] startRegion, int z) {

            double[] testRegion = new double[2];
            

            
            int i = start;
            while (i < stop - 1) {
                
                                             
                microRegion test = new microRegion();
                test = alRegionsLocal.get(i);
                testRegion[0] = test.getBoundCenterX();
                testRegion[1] = test.getBoundCenterY();
                double comparator = lengthCart(startRegion, testRegion);

                if (!test.isAMember()) {
                    if (comparator <= minConstants[2] && ((test.getZPosition() - z) == 1)) {
                        
                        test.setMembership(volumeNumber);
                        test.setAMember(true);
                        z = test.getZPosition();
                        testRegion[0] = (testRegion[0] + startRegion[0]) / 2;
                        testRegion[1] = (testRegion[1] + startRegion[1]) / 2;
                        alRegionsProcessedLocal.add(test);
                        
                        //spped it up
                        alRegionsLocal.remove(i);
                        stop--;
                        //speed it up
                        //System.out.println("PROFILING: Regions left: " + alRegionsLocal.size() + ", tested " + i + " regions.");
                        
                        findConnectedRegions(volumeNumber, testRegion, z);
                        
                    }
                    
                    
                }
                i++;
            }
        }
    }

    private class ZComparator implements Comparator<microRegion> {

        @Override
        public int compare(microRegion o1, microRegion o2) {
            if (o1.getZPosition() == o2.getZPosition()) {
                return 0;
            } else if (o1.getZPosition() > o2.getZPosition()) {
                return 1;
            } else if (o1.getZPosition() < o2.getZPosition()) {
                return -1;
            } else {
                return 0;
            }
        }

    }

    private class XComparator implements Comparator<microRegion> {

        @Override
        public int compare(microRegion o1, microRegion o2) {
            if (o1.getCentroidX() > o2.getCentroidX()) {
                return 1;
            } else {
                return -1;
            }
        }

    }

    private class YComparator implements Comparator<microRegion> {

        @Override
        public int compare(microRegion o1, microRegion o2) {
            if (o1.getCentroidY() > o2.getCentroidY()) {
                return 1;
            } else {
                return -1;
            }
        }

    }

    private class ZObjectComparator implements Comparator<microVolume> {

        @Override
        public int compare(microVolume o1, microVolume o2) {
            if (o1.getCentroidZ() > o2.getCentroidZ()) {
                return 1;
            } else if (o1.getCentroidZ() < o2.getCentroidZ()) {
                return -1;
            } else if (o2.getCentroidZ() > o1.getCentroidZ()) {
                return -1;
            } else if (o2.getCentroidZ() < o2.getCentroidZ()) {
                return 1;
            } else if (o1.getCentroidZ() == o2.getCentroidZ()) {
                return 0;
            } else {
                return -1;
            }
        }

    }

}
