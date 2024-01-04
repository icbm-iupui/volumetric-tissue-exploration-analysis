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
package vtea.objects.Segmentation;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ImageRoi;
import ij.gui.Overlay;
import ij.plugin.Duplicator;
import ij.plugin.StackCombiner;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import static java.util.concurrent.ForkJoinTask.invokeAll;
import java.util.concurrent.RecursiveAction;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.scijava.plugin.Plugin;
import vtea.objects.layercake.microRegion;
import vtea.objects.layercake.microVolume;
import vtea.protocol.listeners.ChangeThresholdListener;
import vtea.protocol.setup.MicroThresholdAdjuster;
import vteaobjects.MicroObject;
import smile.neighbor.KDTree;
import smile.neighbor.Neighbor;

/**
 *
 * @author winfrees
 */
@Plugin(type = Segmentation.class)

public class LayerCake3DSingleThresholdkDTree extends AbstractSegmentation {

    private int[] minConstants = new int[4]; // 0: minObjectSize, 1: maxObjectSize, 2: minOverlap, 3: minThreshold
    private ImagePlus imageOriginal;
    private ImagePlus imageResult;
    private ImageStack stackOriginal;
    protected ImageStack stackResult;

    private double[][] distance;

    private boolean watershedImageJ = true;
    private boolean enforce2_5D = true;

    private List<MicroObject> alVolumes = Collections.synchronizedList(new ArrayList<MicroObject>());
    private List<microRegion> alRegions = Collections.synchronizedList(new ArrayList<microRegion>());
    private List<microRegion> alRegionsProcessed = Collections.synchronizedList(new ArrayList<microRegion>());

    int[] settings = {0, 5, 20, 1000};

    JTextFieldLinked f1 = new JTextFieldLinked(String.valueOf(settings[0]), 5);
    JTextField f2 = new JTextField(String.valueOf(settings[1]), 5);
    JTextField f3 = new JTextField(String.valueOf(settings[2]), 5);
    JTextField f4 = new JTextField(String.valueOf(settings[3]), 5);


    MicroThresholdAdjuster mta;

    public LayerCake3DSingleThresholdkDTree() {
        VERSION = "0.2";
        AUTHOR = "Seth Winfree";
        COMMENT = "Connected components object segmentation with kDTree datatype.";
        NAME = "Connect 2D/3D with kDTree";
        KEY = "Connect2D3DSingleThresholdkDTree";
        TYPE = "Calculated";

        protocol = new ArrayList();

        f1.setPreferredSize(new Dimension(20, 30));
        f1.setMaximumSize(f1.getPreferredSize());
        f1.setMinimumSize(f1.getPreferredSize());

        f2.setPreferredSize(new Dimension(20, 30));
        f2.setMaximumSize(f2.getPreferredSize());
        f2.setMinimumSize(f2.getPreferredSize());

        f3.setPreferredSize(new Dimension(20, 30));
        f3.setMaximumSize(f3.getPreferredSize());
        f3.setMinimumSize(f3.getPreferredSize());

        f4.setPreferredSize(new Dimension(20, 30));
        f4.setMaximumSize(f4.getPreferredSize());
        f4.setMinimumSize(f4.getPreferredSize());


        protocol.add(new JLabel("Low Threshold"));
        protocol.add(f1);
        protocol.add(new JLabel("Centroid Offset"));
        protocol.add(f2);
        protocol.add(new JLabel("Min Vol (vox)"));
        protocol.add(f3);
        protocol.add(new JLabel("Max Vol (vox)"));
        protocol.add(f4);
        protocol.add(new JCheckBox("Watershed", true));
        protocol.add(new JCheckBox("2.5D", true));

    }

    @Override
    public void setImage(ImagePlus thresholdPreview) {
        imagePreview = thresholdPreview;
    }

    @Override
    public void updateImage(ImagePlus thresholdPreview) {
        imagePreview = thresholdPreview;
        mta = new MicroThresholdAdjuster(imagePreview);
    }

    @Override
    public ArrayList<MicroObject> getObjects() {
        return new ArrayList(alVolumes);
    }

    @Override
    public ImagePlus getSegmentation() {
        return this.imageResult;
    }

    @Override
    public JPanel getSegmentationTool() {
        JPanel panel = new JPanel();
        panel.setBackground(vtea._vtea.BACKGROUND);
        mta = new MicroThresholdAdjuster(imagePreview);
        panel.add(mta.getPanel());
        mta.addChangeThresholdListener(f1);
        mta.notifyChangeThresholdListeners(mta.getMin(), mta.getMax());
        return panel;
    }

    @Override
    public void doUpdateOfTool() {
        f1.setText(String.valueOf(mta.getMin()));
        mta.doUpdate();
    }

    /**
     * Copies components between an source and destination arraylist
     *
     * @param version
     * @param dComponents
     * @param sComponents
     * @return
     */
    @Override
    public boolean copyComponentParameter(String version, ArrayList dComponents, ArrayList sComponents) {
        try {
            dComponents.clear();

            JTextFieldLinked f1 = (JTextFieldLinked) sComponents.get(1);
            JTextField f2 = (JTextField) sComponents.get(3);
            JTextField f3 = (JTextField) sComponents.get(5);
            JTextField f4 = (JTextField) sComponents.get(7);
            JCheckBox watershed = new JCheckBox("Watershed", ((JCheckBox) (sComponents.get(8))).isSelected());
            JCheckBox dimensionality = new JCheckBox("2.5D", ((JCheckBox) (sComponents.get(9))).isSelected());

            dComponents.add(new JLabel("Low Threshold"));
            dComponents.add(f1);
            dComponents.add(new JLabel("Centroid Offset"));
            dComponents.add(f2);
            dComponents.add(new JLabel("Min Vol (vox)"));
            dComponents.add(f3);
            dComponents.add(new JLabel("Max Vol (vox)"));
            dComponents.add(f4);
            dComponents.add(watershed);
            dComponents.add(dimensionality);

            return true;
        } catch (Exception e) {
            System.out.println("ERROR: Could not copy parameter(s) for " + NAME);
            return false;
        }
    }

    /**
     * Takes a set of values from 'fields' and populates the components , as
     * defined herein
     *
     * @param version
     * @param dComponents
     * @param fields
     * @return
     */

    @Override
    public boolean loadComponentParameter(String version, ArrayList dComponents, ArrayList fields) {
        try {

            //dComponents.clear();
            JTextFieldLinked n1 = (JTextFieldLinked) dComponents.get(1);
            JTextField n2 = (JTextField) dComponents.get(3);
            JTextField n3 = (JTextField) dComponents.get(5);
            JTextField n4 = (JTextField) dComponents.get(7);
            JCheckBox n5 = (JCheckBox) dComponents.get(8);
            JCheckBox n6 = (JCheckBox) dComponents.get(9);

            n1.setText((String) fields.get(0));
            n2.setText((String) fields.get(1));
            n3.setText((String) fields.get(2));
            n4.setText((String) fields.get(3));
            n5.setSelected((boolean) fields.get(4));
            n6.setSelected((boolean) fields.get(5));

            return true;
        } catch (Exception e) {
            System.out.println("ERROR: Could not copy parameter(s) for " + NAME);
            return false;
        }
    }

    /**
     * Takes the a set of components, as defined herein and populates the fields
     * ArrayList for serialization.
     *
     * @param version in case multiple versions need support
     * @param sComponents
     * @param fields
     * @return
     */
    @Override
    public boolean saveComponentParameter(String version, ArrayList fields, ArrayList sComponents) {

        try {

            JTextFieldLinked f1 = (JTextFieldLinked) sComponents.get(1);
            JTextField f2 = (JTextField) sComponents.get(3);
            JTextField f3 = (JTextField) sComponents.get(5);
            JTextField f4 = (JTextField) sComponents.get(7);
            JCheckBox watershed = new JCheckBox("Watershed", ((JCheckBox) (sComponents.get(8))).isSelected());
            JCheckBox dimensionality = new JCheckBox("2.5D", ((JCheckBox) (sComponents.get(9))).isSelected());

            fields.add(f1.getText());
            fields.add(f2.getText());
            fields.add(f3.getText());
            fields.add(f4.getText());
            fields.add(((JCheckBox) (sComponents.get(8))).isSelected());
            fields.add(((JCheckBox) (sComponents.get(9))).isSelected());

            return true;
        } catch (Exception e) {
            System.out.println("ERROR: Could not save parameter(s) for " + NAME + "\n" + e.getLocalizedMessage());
            return false;
        }
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

        System.out.println("PROFILING: processing with connected components useing kD tree...");
        //System.out.println("PROFILING: Image width: " + is[0].getWidth() + ", height: " + is[0].getHeight());

        /**
         * segmentation and measurement protocol redefining. 0: title text, 1:
         * method (as String), 2: channel, 3: ArrayList of JComponents used for
         * analysis 3: ArrayList of Arraylist for morphology determination
         */
        // 0: minObjectSize, 1: maxObjectSize, 2: minOverlap, 3: minThreshold
        ArrayList al = (ArrayList) protocol.get(3);

        /**
         * PLugin JComponents starts at 1
         */
        minConstants[3] = Integer.parseInt(((JTextField) (al.get(1))).getText());
        minConstants[2] = Integer.parseInt(((JTextField) (al.get(3))).getText());
        minConstants[0] = Integer.parseInt(((JTextField) (al.get(5))).getText());
        minConstants[1] = Integer.parseInt(((JTextField) (al.get(7))).getText());
        watershedImageJ = ((JCheckBox) (al.get(8))).isSelected();
        enforce2_5D = ((JCheckBox) (al.get(9))).isSelected();

        int segmentationChannel = (int) protocol.get(2);

        //System.out.println("PROFILING: segmentation channel: " + segmentationChannel + " for " + is.length + " channels.");
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
            
            if(imageResult.getHeight() > 30000 && imageResult.getWidth() > 30000){
                
                imageResult.setRoi(0,0,imageResult.getWidth(),imageResult.getHeight()/2);
                
                Duplicator dup = new Duplicator();
                
                ImagePlus imageResult_top = dup.crop(imageResult);
                IJ.run(imageResult_top, "Watershed", "stack");
                
                //imageResult_top.show();
                
                imageResult.setRoi(0,imageResult.getHeight()/2,imageResult.getWidth(),imageResult.getHeight());
                ImagePlus imageResult_bottom = dup.crop(imageResult);
                IJ.run(imageResult_bottom, "Watershed", "stack");
                
                //imageResult_bottom.show();
                
                StackCombiner combiner = new StackCombiner();
                ImageStack result = combiner.combineVertically(imageResult_top.getImageStack(), 
                        imageResult_bottom.getImageStack());
                
                imageResult = new ImagePlus(imageResult.getTitle(), result);
                
            }else{
            IJ.run(imageResult, "Watershed", "stack");
            }
        }
        IJ.run(imageResult, "Invert", "stack");

        //imageResult.show();
        //define the regions
        notifyProgressListeners("Finding regions...", 10.0);

        RegionForkPool rrf = new RegionForkPool(imageResult.getStack(), stackOriginal, 0, stackOriginal.getSize(), 0, stackOriginal.getHeight());
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(rrf);

        //sort the regions
        Collections.sort(alRegions, new ZComparator());
        Collections.sort(alRegions, new XComparator());
        Collections.sort(alRegions, new YComparator());

        //build the volumes
        notifyProgressListeners("Building kD tree...", 0.0);
        
        ListIterator<microRegion> regionCentroids = alRegions.listIterator();
        
        int i = 0;
        int db = 0;
        
        double[][] data = new double[alRegions.size()][3];
        double[][] key = new double[alRegions.size()][1];
        //System.out.println("Building kD tree...");
        
        while(regionCentroids.hasNext()){
            double[] d = new double[3];
            
            db = (100 * (i + 1)) / alRegions.size();
            notifyProgressListeners("Building kD tree...", (double)db);
            
            microRegion mr = regionCentroids.next();
           
            d[0] = mr.getBoundCenterX();
            d[1] = mr.getBoundCenterY();
            d[2] = mr.getZPosition();
       
            data[i] = d;
            
            //System.out.println("PROFILING: Adding: " + mr.getBoundCenterX() + ", " + mr.getBoundCenterY() + ", " + mr.getZPosition() + " at " + i);
            
            double[] k = new double[1];
            k[0] = i;         
            key[i] = k;
            
            i++; 
        }
        
        KDTree tree = new KDTree(data, key);

        int nVolumesLocal = 0;
        db = 0;
        //int z = 0;
        
        if(!enforce2_5D){
        
        for (int j = 0; j < alRegions.size(); j++) {
            
            db = (100 * (j + 1)) / alRegions.size();
            notifyProgressListeners("Building volumes...", (double) db);
                
            //test is the starting point
            
            microRegion test = alRegions.get(j);

            if (!test.isAMember()) {
                nVolumesLocal++;
                test.setMembership(nVolumesLocal);
                test.setAMember(true);
                alRegionsProcessed.add(test);
               double[] query = new double[3];
               
               query[0] = test.getBoundCenterX();
               query[1] = test.getBoundCenterY();
               query[2] = test.getZPosition();
               
               ArrayList<Neighbor> neighbors = new ArrayList<Neighbor>();
               tree.range(query, minConstants[2], neighbors);  
               ListIterator<Neighbor> neighborItr = neighbors.listIterator();
               //if(!enforce2_5D){
               findConnectedRegions(nVolumesLocal, query, tree, 3);
               //} 
            } 
        } 
        }else{
            alRegionsProcessed.addAll(alRegions);
        }
        
        //System.out.println("PROFILING... Regions: " + alRegionsProcessed.size());
        
        //this needs to be threaded
        
        VolumeForkPool vfp = new VolumeForkPool(stackOriginal, nVolumesLocal, 0, nVolumesLocal);
        pool = new ForkJoinPool();
        pool.invoke(vfp);

     /**   for (int k = 1; k <= nVolumesLocal; k++) {
             db = (100 * (k + 1)) / nVolumesLocal;
            notifyProgressListeners("Parsing volumes...", (double) db);
            microVolume volume = new microVolume();
            ListIterator<microRegion> vol = alRegionsProcessed.listIterator();
            microRegion region = new microRegion();
            while (vol.hasNext()) {
                region = vol.next();
                if (k == region.getMembership()) {
                    volume.addRegion(region);
                }
            }
            if (volume.getNRegions() > 1 || imageResult.getNSlices() == 1) {
                volume.makePixelArrays();
                volume.setCentroid();
                volume.setSerialID(alVolumes.size());
                if ((volume.getPixelsX()).length >= minConstants[0] && (volume.getPixelsX()).length <= minConstants[1]) {
                    alVolumes.add(volume);
                }
            }
        }
        * 
        * 
        **/
      
       
        System.out.println("PROFILING: Renumbering objects from threads...");
        
        for(int k = 0; k < alVolumes.size(); k++){ 
    
            db = (100 * (k + 1)) / alVolumes.size();
            notifyProgressListeners("Renumbering objects...", (double) db);
            
            MicroObject o = alVolumes.get(k);
            o.setSerialID(k);
        }
        
        System.out.println("PROFILING:  Found " + alVolumes.size() + " volumes.");
        
        return true;
    }
    
    private ImagePlus splitImageWatershed(ImagePlus imp){
        
        
        
        
        
        return imp;
    }

    private double lengthCart(double[] position, double[] reference_pt) {
        double distance;
        double part0 = position[0] - reference_pt[0];
        double part1 = position[1] - reference_pt[1];
        distance = Math.sqrt((part0 * part0) + (part1 * part1));
        return distance;
    }

    private void findConnectedRegions(int volumeNumber, double[] query, KDTree tree, int nD) {

        ArrayList<Neighbor> neighbors = new ArrayList<Neighbor>();
               tree.range(query, minConstants[2], neighbors);  
               ListIterator<Neighbor> neighborItr = neighbors.listIterator();
              
               while(neighborItr.hasNext()){
                   Neighbor n = neighborItr.next();
                   double[] neighborkey = (double[])n.value;
                   microRegion addRegion = alRegions.get((int)neighborkey[0]);
                   
                if (!addRegion.isAMember()) {
                   if (nD == 3 && n.distance <= (minConstants[2])
                           && Math.abs(addRegion.getZPosition() - query[2]) == 1
                           ) {
                       
                           addRegion.setMembership(volumeNumber);
                           addRegion.setAMember(true);
                           alRegionsProcessed.add(addRegion);

                           query[0] = addRegion.getBoundCenterX();
                           query[1] = addRegion.getBoundCenterY();
                           query[2] = addRegion.getZPosition();
                           
                           findConnectedRegions(volumeNumber, query, tree, nD);
                       } 

//                 if(nD == 2 && n.distance <= (minConstants[2])) {
//                        addRegion.setMembership(volumeNumber);
//                           addRegion.setAMember(true);
//                           alRegionsProcessed.add(addRegion);
//
//                           query[0] = addRegion.getBoundCenterX();
//                           query[1] = addRegion.getBoundCenterY();
//                           query[2] = addRegion.getZPosition();
//                           
//                           findConnectedRegions(volumeNumber, query, tree, nD);
//                }
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
            if (o1.getCentroidX() == o2.getCentroidX()) {
                return 0;
            } else if (o1.getCentroidX() > o2.getCentroidX()) {
                if (o1.getZPosition() != o2.getZPosition()) {
                    return 1;
                } else {
                    return 0;
                }
            } else if (o1.getCentroidX() < o2.getCentroidX()) {
                if (o1.getZPosition() != o2.getZPosition()) {
                    return -1;
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        }

    }

    private class YComparator implements Comparator<microRegion> {

        @Override
        public int compare(microRegion o1, microRegion o2) {
            if (o1.getCentroidY() == o2.getCentroidY()) {
                return 0;
            } else if (o1.getCentroidY() > o2.getCentroidY()) {
                if (o1.getZPosition() != o2.getZPosition()) {
                    return 1;
                } else {
                    return 0;
                }
            } else if (o1.getCentroidY() < o2.getCentroidY()) {
                if (o1.getZPosition() != o2.getZPosition()) {
                    return -1;
                } else {
                    return 0;
                }
            } else {
                return 0;
            }

        }
    }

    private class RegionForkPool extends RecursiveAction {

        private int maxsize = 1;
        private ArrayList<microRegion> alResult = new ArrayList<>();

        int n_positions = 0;

        int count = 0;
        private ImageStack stack;
        private ImageStack original;
        private int start;
        private int stop;
        private int startHeight;
        private int stopHeight;
        

        RegionForkPool(ImageStack st, ImageStack orig, int start, int stop, int startHeight, int stopHeight) {

            stack = st;
            original = orig;
            this.start = start;
            this.stop = stop;           
            this.startHeight = startHeight;
            this.stopHeight = stopHeight; 
            
            maxsize = stack.getSize() * stack.getWidth() * stack.getHeight();
        }

        private boolean defineRegions() {

            int color = 1;
            int region = 0;
            ArrayList<int[]> pixels = new ArrayList<int[]>();

            
            if(stopHeight > stack.getHeight()){stopHeight = stack.getHeight();}

            for (int n = this.start; n <= this.stop; n++) {
                for (int p = 0; p < stack.getWidth(); p++) {
                  for (int q = startHeight; q < stopHeight; q++) {
                    
                        if (getVoxelBounds(stack, p, q, n) == 255) {
                            
                            pixels = floodfill(stack, p, q, n, stack.getWidth(), startHeight, stopHeight, stack.getSize(), color, pixels);
                            
                            if(!pixels.isEmpty()){
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
            }
            if(!alResult.isEmpty()){System.out.println("PROFILING: ...Regions found in thread:  " + alResult.size());
            return true;}
            else {System.out.println("PROFILING: ...Regions found in thread:  0"); return false;}
        }

        private ArrayList<int[]> floodfill(ImageStack stack, int x, int y, int z, int width,int start_height, int height, int depth, int color, ArrayList<int[]> pixels) {

            //System.out.println("Section HeightStop: " + height); 
            //System.out.println("Stack Height: " + stack.getHeight()); 
            
            if (x < 0 || y < 0 || z < 0) {
                return pixels;
            }
            
            
            if (y >= height || x >= width || z >= depth ) {
                return pixels;
            }
          
            if (stack.getVoxel(x, y, z) < 255) {
                return pixels;
            }
     
            if(pixels.size() < minConstants[1]) {

                stack.setVoxel(x, y, z, color);

                int[] pixel = new int[3];
                pixel[0] = x;
                pixel[1] = y;
                pixel[2] = z;

                pixels.add(pixel);
                
                pixels = floodfill(stack, x, y - 1, z, width, start_height,height, depth, color, pixels);
                pixels = floodfill(stack, x - 1, y - 1, z, width, start_height,height, depth, color, pixels);
                pixels = floodfill(stack, x + 1, y - 1, z, width, start_height,height, depth, color, pixels);
                pixels = floodfill(stack, x - 1, y, z, width,start_height, height, depth, color, pixels);
                pixels = floodfill(stack, x - 1, y + 1, z, width,start_height, height, depth, color, pixels);
                pixels = floodfill(stack, x, y + 1, z, width, start_height,height, depth, color, pixels);
                pixels = floodfill(stack, x + 1, y + 1, z, width, start_height,height, depth, color, pixels);
                pixels = floodfill(stack, x + 1, y, z, width, start_height,height, depth, color, pixels);
          
            } else { 
                return new ArrayList<int[]>();
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
            if(!alResult.isEmpty()){alRegions.addAll(alResult);}
        }

        public ArrayList<microRegion> getRegions() {
            return this.alResult;
        }

        public int[] convertPixelArrayList(List<Integer> integers) {
            int[] ret = new int[integers.size()];
            Iterator<Integer> iterator = integers.iterator();
            for (int i = 0; i < ret.length; i++) {
                ret[i] = iterator.next();
            }
            return ret;
        }

        @Override
        protected void compute() {

            long processors = Runtime.getRuntime().availableProcessors(); 

            long depth = (stack.getSize() / (processors-1));
            long height = (stack.getHeight() / 8);
            
            if(height < 1024) {height=1024;}

            if(stopHeight > stack.getHeight()){stopHeight = stack.getHeight();}

            if(stack.getSize() == 1){   
                depth = 1;
                if(stopHeight - startHeight > height){
                        invokeAll(new RegionForkPool(stack, original, start, stop, 
                        startHeight, startHeight + ((stopHeight - startHeight) / 2)),
                        new RegionForkPool(stack, original,start, stop, 
                        startHeight + ((stopHeight - startHeight) / 2) + 1, stopHeight));
                } else  {
                    if(defineRegions()){
                    setRegions();}
                    //System.out.println("PROFILING: ...Total regions: " + alRegions.size());
                }
            } else {
                startHeight = 0;
                stopHeight = stack.getHeight();
                if (stop - start > depth) {
                    int stop1 = start + ((stop - start) / 2);
                        invokeAll(new RegionForkPool(stack, original, start, stop1, 
                        0, 0),
                        new RegionForkPool(stack, original, stop1 + 1, stop, 
                        0, 0));
                } else  {
                    if(defineRegions()){
                    setRegions();}
                    //System.out.println("PROFILING: ...Total regions: " + alRegions.size());
                }
            }
        }      
    }
    
    private class VolumeForkPool extends RecursiveAction {

        private int nVolumes;
        private int start;
        private int stop;
        private ImageStack stack;
        

        VolumeForkPool(ImageStack stack, int nVolumes, int start, int stop) {

            this.stack = stack;
            this.nVolumes = nVolumes;
            this.start = start;
            this.stop = stop;                       
        }

        @Override
        protected void compute() {

         if(!enforce2_5D){   
            
            long processors = Runtime.getRuntime().availableProcessors();          
            long length = alRegionsProcessed.size()/ (processors-1);
                //System.out.println("PROFILING: ...Total processed regions: " + alRegionsProcessed.size());
                if(stop - start > length){
                        //System.out.println("PROFILING: ...Splitting image volume...");
                        invokeAll(new VolumeForkPool(stackOriginal,nVolumes, start, start + ((stop - start) / 2)),
                        new VolumeForkPool(stackOriginal,nVolumes, start + ((stop - start) / 2) + 1, stop));
                } else  {
                parseVolumes();
                }
         } else {   
             alVolumes.clear();
             
            ListIterator<microRegion> itr = alRegionsProcessed.listIterator(); 
             
            while(itr.hasNext()){  
             microRegion region = itr.next();      
             microVolume volume = new microVolume();
             volume.addRegion(region);
             volume.makePixelArrays();
             volume.setCentroid();
             volume.setSerialID(alVolumes.size());
             if ((volume.getPixelsX()).length >= minConstants[0] && (volume.getPixelsX()).length <= minConstants[1]) {
                    alVolumes.add(volume);
                }
             


             
             
            }
             System.out.println("PROFILING:  Fixing split nuclei on: " + alVolumes.size() + " volumes.");
             if(stack.getHeight() > 1024){
             fix2DCollisions();
             }
         }

        }  
        
        private boolean parseVolumes() {
               for (int k = start; k < stop; k++) {
             double db = (100 * (k + 1)) / (stop-start);
            notifyProgressListeners("Parsing volumes...", (double) db);
            microVolume volume = new microVolume();
            ListIterator<microRegion> vol = alRegionsProcessed.listIterator();
            microRegion region = new microRegion();
            while (vol.hasNext()) {
                region = vol.next();
                if (k == region.getMembership()) {
                    volume.addRegion(region);
                }
            }
            
            //trap 2.5D
            
            int minRegions = 0;


            if (volume.getNRegions() > minRegions || imageResult.getNSlices() == 1) {
                volume.makePixelArrays();
                volume.setCentroid();
                volume.setSerialID(alVolumes.size());
                if ((volume.getPixelsX()).length >= minConstants[0] && (volume.getPixelsX()).length <= minConstants[1]) {
                    alVolumes.add(volume);
                }
            }
        }
        return true;
        }
    
    private void fix2DCollisions(){
        
        List<MicroObject> updated_alVolumes = Collections.synchronizedList(new ArrayList<MicroObject>());
        List<MicroObject> boundary_alVolumes = Collections.synchronizedList(new ArrayList<MicroObject>());
        
        boundary_alVolumes.addAll(getBoundaryObjects(128));
        
        
        boolean[] processed = new boolean[boundary_alVolumes.size()];
        boolean[] notmerged = new boolean[boundary_alVolumes.size()];
        

        
        
        
        //int merge_counter = 0;
        
        for(int i = 0; i < boundary_alVolumes.size(); i++){
            processed[i] = false;
            notmerged[i] = true;
        }
        
        
        
        for(int i = 0; i < boundary_alVolumes.size(); i++){

            MicroObject obj1 = boundary_alVolumes.get(i);
            Polygon pg = getPerimeter(obj1);
            
            processed[i] = true;
            
        
            //System.out.println("PROFILING:  Screening object: " + obj1.getSerialID());
            
            label:for(int j = 0; j < boundary_alVolumes.size(); j++){

                if(!processed[j]){
                
                 MicroObject obj2 = boundary_alVolumes.get(j);
                 
                    //System.out.println("     ...against object: " + obj2.getSerialID());
                 
                 int[] obj2_x = obj2.getPixelsX();
                 int[] obj2_y = obj2.getPixelsY();

                    if(detectSplit(pg, obj2_x, obj2_y)){
                        //System.out.println("PROFILING:  Merging connected nuclei: " + obj1.getSerialID() + " and " + obj2.getSerialID());
                        updated_alVolumes.add(mergeObjects(obj1, obj2));
                        processed[j] = true;
                        notmerged[j] = false;
                        notmerged[i] = false;
                        break label;
                    }
                }
            }

            

        } 
        
        for(int i = 0; i < notmerged.length; i++){
            
            if(notmerged[i]){
                updated_alVolumes.add(boundary_alVolumes.get(i));
            }
            
            
        }
   

        //System.out.println("PROFILING:  Merged volumes: " + 
          //      merge_counter + " volumes.");
        
        
        //alVolumes.clear();
        alVolumes.addAll(updated_alVolumes);
        
    }
    
    private ArrayList<Integer> calculateBoundaries(){
        
            int processors = Runtime.getRuntime().availableProcessors();          
            int height = stack.getHeight()/8;
            
            if(height < 1024) {height=1024;}
            
            int start = 0;
            int stop = stack.getHeight();
            
            ArrayList<Integer> boundaries = new ArrayList<>();
            
           
            
            int nBoundary = 0;
            
        while(stop - start > height){         
            nBoundary = (stop - start)/2;       
            stop = nBoundary;
        } 
        
        stop = stack.getHeight();
        
        while(start < stop){
            
            boundaries.add(start);
            start = start+nBoundary;
            
        }
        

       System.out.println("PROFILING:  Fixing " + boundaries.size() + " boundaries."); 
        return boundaries;
    }
    
    private ArrayList<MicroObject> getBoundaryObjects(int range){
        
        ArrayList<MicroObject> boundaryVolumes = new ArrayList<>();
        
        ArrayList<Integer> boundaries = calculateBoundaries();
        
        for(int i = 0; i < boundaries.size(); i++){
            
            ListIterator<MicroObject> itr = alVolumes.listIterator();
            
            int min = boundaries.get(i)-(range/2);
            int max = boundaries.get(i)+(range/2);
            
            if(min < 0){min = 0;}
            if(max > stack.getHeight()){max = stack.getHeight();}
            
            while(itr.hasNext()){    
                MicroObject test = itr.next();
                if(test.getCentroidY() > min && test.getCentroidY() < max){
                    boundaryVolumes.add(test);
                    itr.remove();  
                }
                
            }
        } 
        System.out.println("PROFILING:  Found " + boundaryVolumes.size() + " boundary object(s)."); 
        return boundaryVolumes;
        }
    
    private boolean detectSplit(Polygon pg, int[] obj2_x, int[] obj2_y){
    
    for(int i = 0; i < obj2_x.length; i++){
                     if(pg.contains(obj2_x[i], obj2_y[i])){
                         return true;
                     }
    }
    return false;
    }
    
    
    private microVolume mergeObjects(MicroObject obj1, MicroObject obj2){
        
        int[] obj1_x = obj1.getPixelsX();
        int[] obj1_y = obj1.getPixelsY();
                                
        int[] obj2_x = obj2.getPixelsX();
        int[] obj2_y = obj2.getPixelsY();
        
        //System.out.println("PROFILING:  previous size: " + obj1_x.length + "," + obj2_x.length);
        
        int[] merge_x = new int[obj1_x.length+obj2_x.length];
        int[] merge_y = new int[obj1_x.length+obj2_x.length];  
        
      
        
        for(int i = 0; i < obj1_x.length; i++){
            merge_x[i] = obj1_x[i];
            merge_y[i] = obj1_y[i];
        }
        
        //System.out.println("PROFILING:  added object 1: " + obj1_x.length);
        
       
        
        for(int i = 0; i < obj2_x.length; i++){
            merge_x[i+obj1_x.length] = obj2_x[i];
            merge_y[i+obj1_x.length] = obj2_y[i];
        }
        
        
        //System.out.println("PROFILING:  combined size: " + merge_x.length);
        
        microVolume merged = new microVolume();
        microRegion region = new microRegion(merge_x, merge_y, merge_y.length, 0);
 
        merged.addRegion(region);

        merged.makePixelArrays();
        merged.setCentroid();
        merged.setSerialID(alVolumes.size());
         
        
        return merged;
    }
        
    private Polygon getPerimeter(MicroObject test){
        Polygon boundary = new Polygon();
        int[] pixels_x = test.getPixelsX();
        int[] pixels_y = test.getPixelsY();
        
        Polygon pg = new Polygon(pixels_x,pixels_y, pixels_y.length);
        
        //for each pixel, check 8C for inside p, if not, boundary
        
        for(int i = 0; i < pixels_x.length; i++){
            
            Point p = new Point(pixels_x[i], pixels_y[i]);
            
            p = new Point(pixels_x[i], pixels_y[i]-1);
            if(!(pg.contains(p))){boundary.addPoint(p.x,p.y);}
            p = new Point(pixels_x[i], pixels_y[i]+1);
            if(!(pg.contains(p))){boundary.addPoint(p.x,p.y);}
            p = new Point(pixels_x[i]+1, pixels_y[i]);
            if(!(pg.contains(p))){boundary.addPoint(p.x,p.y);}
            p = new Point(pixels_x[i]-1, pixels_y[i]);
            if(!(pg.contains(p))){boundary.addPoint(p.x,p.y);}
            p = new Point(pixels_x[i]-1, pixels_y[i]-1);
            if(!(pg.contains(p))){boundary.addPoint(p.x,p.y);}
            p = new Point(pixels_x[i]+1, pixels_y[i]+1);
            if(!(pg.contains(p))){boundary.addPoint(p.x,p.y);}
            p = new Point(pixels_x[i]+1, pixels_y[i]-1);
            if(!(pg.contains(p))){boundary.addPoint(p.x,p.y);}
            p = new Point(pixels_x[i]-1, pixels_y[i]+1);
            if(!(pg.contains(p))){boundary.addPoint(p.x,p.y);}  
            
        }
        
//        ByteProcessor ip = new ByteProcessor(200,200);
//        BufferedImage selections = new BufferedImage(200,
//                    200, BufferedImage.TYPE_INT_ARGB);
//
//
//            Overlay overlay = new Overlay();
//        
//        
//        
//        
//        Graphics2D g2 = selections.createGraphics();
//        
//        
//        int[] x_pixels = boundary.xpoints;
//        int[] y_pixels = boundary.ypoints;
//        for (int c = 0; c < x_pixels.length; c++) {
//                                g2.setColor(Color.green);
//                                g2.drawRect(x_pixels[c], y_pixels[c], 1, 1);
//        }
//        
//        x_pixels = pg.xpoints;
//        y_pixels = pg.ypoints;
//        for (int c = 0; c < x_pixels.length; c++) {
//                                g2.setColor(Color.red);
//                                g2.drawRect(x_pixels[c], y_pixels[c], 1, 1);
//        }
//        
//        ImageRoi ir = new ImageRoi(0, 0, selections);
//        overlay.selectable(false);
//        overlay.add(ir);
//
//
//        ImagePlus imp = new ImagePlus("Boundary of object: " + test.getSerialID(), ip);
//        
//        //imp.setOverlay(pg, Color.green, new BasicStroke(1.5f));
//        
//        imp.setOverlay(overlay);
//       
//        imp.draw();
//        imp.show();
        
        return boundary;
    }

        
    }

    public class JTextFieldLinked extends JTextField implements ChangeThresholdListener {

        JTextFieldLinked(String str, int i) {
            super(str, i);
            setBackground(new Color(255, 152, 152));
        }

        @Override
        public void thresholdChanged(double min, double max) {
            double ipmin = imagePreview.getProcessor().getMin();
            double ipmax = imagePreview.getProcessor().getMax();

            min = ipmin + (min / 255.0) * (ipmax - ipmin);
            max = ipmin + (max / 255.0) * (ipmax - ipmin);

            if (min > 0) {
                this.setBackground(Color.WHITE);
            } else {
                this.setBackground(new Color(255, 152, 152));
            }

            //System.out.println("PROFILING: threshold minimum changes to: " + String.valueOf(Math.round(min)));
            f1.setText("" + String.valueOf(Math.round(min)));

        }
    }
}
