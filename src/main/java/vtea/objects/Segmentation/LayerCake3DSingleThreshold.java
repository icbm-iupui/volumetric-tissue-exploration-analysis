/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vtea.objects.Segmentation;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ForkJoinPool;
import static java.util.concurrent.ForkJoinTask.invokeAll;
import java.util.concurrent.RecursiveAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import org.scijava.plugin.Plugin;
import vteaobjects.MicroObject;
import vtea.objects.layercake.LayerCake3D;
import vtea.objects.layercake.microRegion;
import static java.util.concurrent.ForkJoinTask.invokeAll;
import javax.swing.JPanel;
import vtea.objects.layercake.microVolume;

/**
 *
 * @author winfrees
 */

@Plugin (type = Segmentation.class)

public class LayerCake3DSingleThreshold extends AbstractSegmentation {
    
    
    private int[] minConstants = new int[4]; // 0: minObjectSize, 1: maxObjectSize, 2: minOverlap, 3: minThreshold
    private  ImagePlus imageOriginal;
    private  ImagePlus imageResult;
    private  ImageStack stackOriginal;
    protected  ImageStack stackResult;
    private boolean watershedImageJ = true;
    
    private LayerCake3D builderRegions;
    private LayerCake3D builderVolumes;
    
    private ArrayList<MicroObject> alVolumes = new ArrayList<MicroObject>();
    private List<microRegion> alRegions = Collections.synchronizedList(new ArrayList<microRegion>());
    private List<microRegion> alRegionsProcessed = Collections.synchronizedList(new ArrayList<microRegion>());
    
public LayerCake3DSingleThreshold(){
    VERSION = "0.1";
    AUTHOR = "Seth Winfree";
    COMMENT = "Blob slice algorithm for building objects.";
    NAME = "LayerCake 3D";
    KEY = "LayerCake3DSingleThreshold";
    
    protocol = new ArrayList();
    
            protocol.add(new JLabel("Low Threshold"));
            protocol.add(new JTextField("0"));
            protocol.add(new JLabel("Centroid Offset"));
            protocol.add(new JTextField("5"));
            protocol.add(new JLabel("Min Vol (vox)"));
            protocol.add(new JTextField("20"));
            protocol.add(new JLabel("Max Vol (vox)"));
            protocol.add(new JTextField("1000"));
    
}    

//    @Override
//    public JPanel getOptionsPanel() {
//        
//    }

    @Override
    public ArrayList<MicroObject> getObjects() {
        return alVolumes;
    }

    @Override
    public ImagePlus getSegmentation() {
        return this.imageResult;
    }

    @Override
    public String runImageJMacroCommand(String str) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void sendProgressComment() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getProgressComment() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     *
     * @param is
     * @param protocol
     * @param count
     * @return 
     */
    @Override
    public boolean process(ImageStack[] is, List protocol, boolean count) {

        System.out.println("PROFILING: processing on LayerCake3D...");

        
         /**segmentation and measurement protocol redefining.
         * 0: title text, 1: method (as String), 2: channel, 3: ArrayList of JComponents used 
         * for analysis 3: ArrayList of Arraylist for morphology determination
         */
             
            // 0: minObjectSize, 1: maxObjectSize, 2: minOverlap, 3: minThreshold
            
        ArrayList al = (ArrayList)protocol.get(3);

        /**PLugin JComponents starts at 1*/
  
        minConstants[3] = Integer.parseInt(((JTextField)(al.get(1))).getText());
        minConstants[2] = Integer.parseInt(((JTextField)(al.get(3))).getText());
        minConstants[0] = Integer.parseInt(((JTextField)(al.get(5))).getText());
        minConstants[1] = Integer.parseInt(((JTextField)(al.get(7))).getText());
        
        int segmentationChannel = (int)protocol.get(2);
                 
        stackOriginal = is[segmentationChannel];
        imageOriginal = new ImagePlus("Mask", stackOriginal);
        stackResult = stackOriginal.duplicate();

        //Segment and preprocess the image
        
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
        IJ.run(imageResult, "Invert", "stack");
        if (watershedImageJ) {
            IJ.run(imageResult, "Watershed", "stack");
        }
         IJ.run(imageResult, "Invert", "stack");
      
       //define the regions
        
        RegionForkPool rrf = new RegionForkPool(imageResult.getStack(), stackOriginal, 0, stackOriginal.getSize());       
        ForkJoinPool pool = new ForkJoinPool();       
        pool.invoke(rrf);

        //build the volumes
        

            int z;
            int nVolumesLocal = 0;
            microVolume volume = new microVolume();
            double[] startRegion = new double[2];

            microRegion test = new microRegion();

            for(int i = 0; i < alRegions.size(); i++){
                test = alRegions.get(i);
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
                    volume.makePixelArrays();
                    volume.calculateVolumeMeasurements();
                    volume.setObjectID(alVolumes.size());
                    if (volume.getPixelCount() >= minConstants[0] && volume.getPixelCount() <= minConstants[1]) {
                        alVolumes.add(volume);
                    }
                }
            }
        
        
            
        System.out.println("PROFILING:  Found " + alVolumes.size() + " volumes.");
        
        
        return true;
    }
    
    private double lengthCart(double[] position, double[] reference_pt) {
        double distance;
        double part0 = position[0] - reference_pt[0];
        double part1 = position[1] - reference_pt[1];
        distance = Math.sqrt((part0 * part0) + (part1 * part1));
        return distance;
    }
    
    private void findConnectedRegions(int volumeNumber, double[] startRegion, int z) {

            double[] testRegion = new double[2];
            
            
            
            int i = 0;
            
            
            while (i < alRegions.size()) {
                
                                             
                microRegion test = new microRegion();
                test = alRegions.get(i);
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
                        alRegionsProcessed.add(test);
                        
                        //spped it up
                        alRegions.remove(i);
                        

                        findConnectedRegions(volumeNumber, testRegion, z);
                        
                    }
                    
                    
                }
                i++;
            }
        }
    
    class ZComparator implements Comparator<microRegion> {

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
    
    class RegionForkPool extends RecursiveAction {

        private int maxsize = 1;
        private final ArrayList<microRegion> alResult = new ArrayList<>();
     
        int n_positions = 0;

        int count = 0;
        private ImageStack stack;
        private ImageStack original;
        private int start;
        private int stop;

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
                        if (getVoxelBounds(stack, p, q, n) == 255) {
                            pixels = floodfill(stack, p, q, n, stack.getWidth(), stack.getHeight(), stack.getSize(), color, pixels);

                            //System.out.println("PROFILING: region size: " + pixels.size());

                            int[] pixel = new int[3];
                            int[] xPixels = new int[pixels.size()];
                            int[] yPixels = new int[pixels.size()];
                            int j = 0;

                            ListIterator<int[]> itr = pixels.listIterator();
                            while (itr.hasNext()) {
                                pixel = itr.next();
                                xPixels[j] = pixel[0];
                                yPixels[j] = pixel[1];
                                j++;
                            }

                            alResult.add(new microRegion(xPixels, yPixels, xPixels.length, n, original));


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
}
