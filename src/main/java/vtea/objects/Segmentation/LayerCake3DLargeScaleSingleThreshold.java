/* 
 * Copyright (C) 2016-2018 Indiana University
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

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.RGBStackMerge;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ForkJoinPool;
import static java.util.concurrent.ForkJoinTask.invokeAll;
import java.util.concurrent.RecursiveAction;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.scijava.plugin.Plugin;
import vteaobjects.MicroObject;
import vtea.objects.layercake.microRegion;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import static vtea._vtea.getInterleavedStacks;

import vtea.protocol.listeners.ChangeThresholdListener;
import vtea.protocol.setup.MicroThresholdAdjuster;
import static java.util.concurrent.ForkJoinTask.invokeAll;
import static java.util.concurrent.ForkJoinTask.invokeAll;
import static java.util.concurrent.ForkJoinTask.invokeAll;
import javax.swing.JFileChooser;
import org.apache.commons.io.FilenameUtils;
import vtea.processor.listeners.ProgressListener;
import vteaexploration.MicroExplorer;

/**
 *
 * @author winfrees
 */
@Plugin(type = Segmentation.class)

public class LayerCake3DLargeScaleSingleThreshold extends AbstractSegmentation implements ProgressListener {

    private int[] minConstants = new int[6]; // 0: minObjectSize, 1: maxObjectSize, 2: minOverlap, 3: minThreshold
    private ImagePlus imageOriginal;
    private ImagePlus imageResult;
    private ImageStack stackOriginal;
    protected ImageStack stackResult;

    private double[][] distance;

    private boolean watershedImageJ = true;
    
    private boolean extraLargeScale = false;

    private ArrayList xPositions = new ArrayList();
    private ArrayList yPositions = new ArrayList();

    private ArrayList xStart = new ArrayList();
    private ArrayList yStart = new ArrayList();

    private ArrayList<MicroObject> alVolumes = new ArrayList<MicroObject>();
    private List<MicroObject> Volumes = Collections.synchronizedList(new ArrayList<MicroObject>());
    private List<microRegion> alRegions = Collections.synchronizedList(new ArrayList<microRegion>());
    private List<microRegion> alRegionsProcessed = Collections.synchronizedList(new ArrayList<microRegion>());

    int[] settings = {0, 5, 20, 1000, 512, 5};

    JTextFieldLinked f1 = new JTextFieldLinked(String.valueOf(settings[0]), 5);
    JTextField f2 = new JTextField(String.valueOf(settings[1]), 5);
    JTextField f3 = new JTextField(String.valueOf(settings[2]), 5);
    JTextField f4 = new JTextField(String.valueOf(settings[3]), 5);
    JTextField f5 = new JTextField(String.valueOf(settings[4]), 5);
    JTextField f6 = new JTextField(String.valueOf(settings[5]), 5);

    MicroThresholdAdjuster mta;

    public LayerCake3DLargeScaleSingleThreshold() {
        VERSION = "0.1";
        AUTHOR = "Seth Winfree";
        COMMENT = "Connected components object segmentation for large-scale images.";
        NAME = "LS Connect 3D";
        KEY = "Connect3DSingleThresholdLS";

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

        f5.setPreferredSize(new Dimension(20, 30));
        f5.setMaximumSize(f5.getPreferredSize());
        f5.setMinimumSize(f5.getPreferredSize());

        f6.setPreferredSize(new Dimension(20, 30));
        f6.setMaximumSize(f6.getPreferredSize());
        f6.setMinimumSize(f6.getPreferredSize());

        protocol.add(new JLabel("Low Threshold"));
        protocol.add(f1);
        protocol.add(new JLabel("Centroid Offset"));
        protocol.add(f2);
        protocol.add(new JLabel("Min Vol (vox)"));
        protocol.add(f3);
        protocol.add(new JLabel("Max Vol (vox)"));
        protocol.add(f4);
        protocol.add(new JCheckBox("Watershed", true));
        protocol.add(new JLabel("XY division"));
        protocol.add(f5);
        protocol.add(new JCheckBox("ELS", false));
        //protocol.add(new JLabel("Overlap"));
        //protocol.add(f6);

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
        return alVolumes;
    }

    @Override
    public ImagePlus getSegmentation() {
        return this.imageResult;
    }

    @Override
    public JPanel getSegmentationTool() {
        JPanel panel = new JPanel();
        panel.setBackground(vtea._vtea.BACKGROUND);
        MicroThresholdAdjuster mta = new MicroThresholdAdjuster(imagePreview);
        mta.addChangeThresholdListener(f1);
        f1.setText(String.valueOf(mta.getMin()));

        panel.add(mta.getPanel());
        return panel;
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
     * @param splitToFile
     * @return
     */
    @Override
    public boolean process(ImageStack[] is, List protocol, boolean splitToFile) {

        System.out.println("PROFILING: processing on Connect3D LargeScale...");

        notifyProgressListeners("Processing on large scale 3D Connect...", 10.0);
        /**
         * segmentation and measurement protocol redefining. 0: title text, 1:
         * method (as String), 2: channel, 3: ArrayList of JComponents used for
         * analysis 3: ArrayList of Arraylist for morphology determination
         */

        // 0: minObjectSize, 1: maxObjectSize, 2: minOverlap, 3: minThreshold
        this.protocol = (ArrayList) protocol;

        ArrayList al = (ArrayList) protocol.get(3);

        minConstants[3] = Integer.parseInt(((JTextField) (al.get(1))).getText());
        minConstants[2] = Integer.parseInt(((JTextField) (al.get(3))).getText());
        minConstants[0] = Integer.parseInt(((JTextField) (al.get(5))).getText());
        minConstants[1] = Integer.parseInt(((JTextField) (al.get(7))).getText());
        minConstants[4] = Integer.parseInt(((JTextField) (al.get(10))).getText());
        watershedImageJ = ((JCheckBox) (al.get(8))).isSelected();
        extraLargeScale = ((JCheckBox) (al.get(11))).isSelected();

        int width = is[0].getWidth();
        int height = is[0].getHeight();
        
                //build the volumes
        int xyDim = minConstants[4];
        
        int lsxyDim = width;
        
                
        if(extraLargeScale){
            
            if(width > height){
                xyDim = width/3;
            }else{
                xyDim = height/3;
            }
        }
        



        //int overlap = minConstants[5];
        RGBStackMerge rsm = new RGBStackMerge();

        ImagePlus[] merged = new ImagePlus[is.length];

        for (int i = 0; i < merged.length; i++) {
            merged[i] = new ImagePlus("Ch_" + i, is[i]);
        }

        imageOriginal = rsm.mergeHyperstacks(merged, false);

        ArrayList<ImagePlus> volumes = new ArrayList<ImagePlus>();

        boolean processing = true;

        int round = 1;

        for (int x = 0; x < width; x = x + xyDim) {

            xPositions.add(x);

            //System.out.println("PROFILING: x start position: " + x );
        }

        for (int y = 0; y < height; y = y + xyDim) {

            yPositions.add(y);

            //System.out.println("PROFILING: y start position: " + y );
        }

        //get grid
        ListIterator itr = xPositions.listIterator();

        while (itr.hasNext()) {

            int x = (int) itr.next();

            for (int p = 0; p < yPositions.size(); p++) {

                int y = (int) yPositions.get(p);

                imageOriginal.setRoi(new Rectangle(x, y, xyDim, xyDim));
                volumes.add(imageOriginal.duplicate());
                imageOriginal.deleteRoi();

                xStart.add(x);
                yStart.add(y);
            }
        }

        int xRemain = 0;
        int yRemain = 0;
 
        if (((int) xPositions.get(xPositions.size() - 1) + xyDim) < width) {

            int x = (int) xPositions.get(xPositions.size() - 1) + xyDim;
            xRemain = width - x;

            for (int p = 0; p < yPositions.size(); p++) {

                int y = (int) yPositions.get(p);

                imageOriginal.setRoi(new Rectangle(x, y, xRemain, xyDim));
                volumes.add(imageOriginal.duplicate());
                imageOriginal.deleteRoi();

                xStart.add(x);
                yStart.add(y);

                //System.out.println("PROFILING: Added new image at, " + x + ", " + y + " with dimensions " + xRemain + " by "+ xyDim);
            }
        }

        //get y remain for all x
        if (((int) yPositions.get(yPositions.size() - 1) + xyDim) < height) {

            int y = (int) xPositions.get(xPositions.size() - 1) + xyDim;
            yRemain = height - y;

            for (int p = 0; p < yPositions.size(); p++) {

                int x = (int) xPositions.get(p);

                imageOriginal.setRoi(new Rectangle(x, y, xyDim, yRemain));
                volumes.add(imageOriginal.duplicate());
                imageOriginal.deleteRoi();

                xStart.add(x);
                yStart.add(y);

                //System.out.println("PROFILING: Added new image at, " + x + ", " + y + " with dimensions " + xyDim + " by " + yRemain);
            }
        }

        // get y and x remain.
        if (xRemain > 0 && yRemain > 0) {

            int xLast = ((int) xPositions.get(xPositions.size() - 1) + xyDim);
            int yLast = ((int) yPositions.get(yPositions.size() - 1) + xyDim);

            imageOriginal.setRoi(new Rectangle(xLast, yLast, xRemain, yRemain));
            volumes.add(imageOriginal.duplicate());
            imageOriginal.deleteRoi();

            xStart.add(xLast);
            yStart.add(yLast);

            //System.out.println("PROFILING: Added new image at, " + xLast + ", " + yLast + " with dimensions " + xRemain + " by " + yRemain);
        }

        notifyProgressListeners("Made " + volumes.size() + " total sub-images.", 15.0);

      //  System.out.println("PROFILING:  Made " + volumes.size() + " total sub-images.");

        SegmentationForkPool sfp = new SegmentationForkPool(volumes, 1, volumes.size());
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(sfp);

        notifyProgressListeners("LargeScale 3D Connect found " + alVolumes.size() + " total volumes.", 100.0);

        System.out.println("PROFILING: LargeScale 3D Connect found " + alVolumes.size() + " total volumes.");

        return true;
    }
    
    private void divideAndConquer() {
        
    }

    @Override
    public void FireProgressChange(String str, double db) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private class SegmentationForkPool extends RecursiveAction implements ProgressListener {

        private ArrayList<ImagePlus> imps = new ArrayList<ImagePlus>();
        private ArrayList<MicroObject> volumes = new ArrayList<MicroObject>();

        private ImageStack[] stack;
        private ImageStack original;
        private int start;
        private int stop;

        SegmentationForkPool(ArrayList<ImagePlus> imps, int start, int stop) {

            this.imps = imps;

            this.start = start;
            this.stop = stop;

        }

        @Override
        protected void compute() {

            long length = 0;

            if (stop - start > length) {

                invokeAll(new SegmentationForkPool(imps, start, start + ((stop - start) / 2)),
                        new SegmentationForkPool(imps, start + ((stop - start) / 2) + 1, stop));

            } else {
                LayerCake3DSingleThreshold lc3dst1 = new LayerCake3DSingleThreshold();
                
                lc3dst1.addListener(this);

                lc3dst1.process(getInterleavedStacks(imps.get(start - 1)), protocol, watershedImageJ);

                if (((ArrayList) (lc3dst1.getObjects())).size() > 0) {

                    //System.out.println("PROFILING: Adding offset of " + ((int) xStart.get(start - 1)) + " and " + ((int) yStart.get(start - 1)));

                    ArrayList<MicroObject> al = lc3dst1.getObjects();

                    ListIterator<MicroObject> itr = al.listIterator();

                    //add offset to for mapping back to image and meaurements
                    while (itr.hasNext()) {
                        MicroObject object = itr.next();
                        object.setPixelsX(addOffset((int) xStart.get(start - 1), object.getPixelsX()));
                        object.setPixelsY(addOffset((int) yStart.get(start - 1), object.getPixelsY()));
                        object.setCentroid();
                    }

                    alVolumes.addAll(al);
                }

            }
            //reset serial id
            ListIterator<MicroObject> allObjects = alVolumes.listIterator();
            int serial = 0;
            while (allObjects.hasNext()) {
                MicroObject object = allObjects.next();
                object.setSerialID(serial);
                serial++;
            }
        }

        private int[] addOffset(int d, int[] pixels) {

            int[] result = new int[pixels.length];

            for (int i = 0; i < pixels.length; i++) {
                result[i] = pixels[i] + d;
            }
            return result;
        }

        @Override
        public void FireProgressChange(String str, double db) {
              notifyProgressListeners(str, db); 
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

            f1.setText("" + String.valueOf(Math.round(min)));
        }
    }
}
