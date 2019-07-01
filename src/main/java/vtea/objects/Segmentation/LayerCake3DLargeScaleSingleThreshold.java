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

import fiji.threshold.Auto_Local_Threshold;
//import fiji.threshold.Auto_Threshold;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.plugin.Duplicator;
import ij.plugin.RGBStackMerge;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ForkJoinPool;
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
import vtea.protocol.setup.AutoLocalThresholdAdjuster;
import vtea.protocol.setup.ThresholdApproach;
import static java.util.concurrent.ForkJoinTask.invokeAll;
import vtea.processor.listeners.ProgressListener;

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
    AutoLocalThresholdAdjuster alta;
    ThresholdApproach ta;

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
        ta = new ThresholdApproach(imagePreview);
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
        
        Auto_Local_Threshold alt = new Auto_Local_Threshold();
        //alt.exec(imagePreview, AUTHOR, 0, 0, 0, watershedImageJ);
//        alta = new AutoLocalThresholdAdjuster(imagePreview);
//        panel.add(alta.getPanel());
        
        ta = new ThresholdApproach(imagePreview);
        panel.add(ta.getPanel());
//        mta = new MicroThresholdAdjuster(imagePreview);
//        panel.add(mta.getPanel());
//        mta.addChangeThresholdListener(f1);
//        mta.notifyChangeThresholdListeners(mta.getMin(), mta.getMax()); 
        return panel;
    }
    
    @Override
    public void doUpdateOfTool() {
        f1.setText(String.valueOf(mta.getMin()));
        mta.doUpdate();
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
                
//        if(extraLargeScale){
//            
//            if(width > height){
//                xyDim = width/3;
//            }else{
//                xyDim = height/3;
//            }
//        }   

         int segmentationChannel = (int)protocol.get(2);
         
                  
        stackOriginal = is[segmentationChannel];
        imageOriginal = new ImagePlus("Mask", stackOriginal);
        stackResult = stackOriginal.duplicate();

        ArrayList<ImagePlus> volumes = new ArrayList<ImagePlus>();

        boolean processing = true;

        int round = 1;
        
        

        for (int x = 0; x < width; x = x + xyDim) {
            xPositions.add(x);
        }

        for (int y = 0; y < height; y = y + xyDim) {
            yPositions.add(y);
        }

        //get grid
        ListIterator itr = xPositions.listIterator();

        while (itr.hasNext()) {

            int x = (int) itr.next();

            for (int p = 0; p < yPositions.size(); p++) {

                int y = (int) yPositions.get(p);
                
                Duplicator dup1 = new Duplicator();
                imageOriginal.setRoi(new Rectangle(x, y, xyDim, xyDim));
                volumes.add(dup1.run(imageOriginal));
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
                 Duplicator dup2 = new Duplicator();
                imageOriginal.setRoi(new Rectangle(x, y, xRemain, xyDim));
                volumes.add(dup2.run(imageOriginal));
                imageOriginal.deleteRoi();

                xStart.add(x);
                yStart.add(y);
          }
        }

        //get y remain for all x
        if (((int) yPositions.get(yPositions.size() - 1) + xyDim) < height) {

            int y = (int) xPositions.get(xPositions.size() - 1) + xyDim;
            yRemain = height - y;

            for (int p = 0; p < yPositions.size(); p++) {

                int x = (int) xPositions.get(p);
                 Duplicator dup3 = new Duplicator();
                imageOriginal.setRoi(new Rectangle(x, y, xyDim, yRemain));
                volumes.add(dup3.run(imageOriginal));
                imageOriginal.deleteRoi();

                xStart.add(x);
                yStart.add(y);
            }
        }

        // get y and x remain.
        if (xRemain > 0 && yRemain > 0) {

            int xLast = ((int) xPositions.get(xPositions.size() - 1) + xyDim);
            int yLast = ((int) yPositions.get(yPositions.size() - 1) + xyDim);
             Duplicator dup4 = new Duplicator();
            imageOriginal.setRoi(new Rectangle(xLast, yLast, xRemain, yRemain));
            volumes.add(dup4.run(imageOriginal));
            imageOriginal.deleteRoi();

            xStart.add(xLast);
            yStart.add(yLast);
        }

        notifyProgressListeners("Made " + volumes.size() + " total sub-images.", 15.0);

        SegmentationForkPool sfp = new SegmentationForkPool(volumes, 1, volumes.size());
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(sfp);

        notifyProgressListeners("LargeScale 3D Connect found " + alVolumes.size() + " total volumes.", 100.0);

        System.out.println("PROFILING: LargeScale 3D Connect found " + alVolumes.size() + " total volumes.");

        return true;
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
                
                protocol.set(2,(int)0);

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
    /** Copies components between an source and destination arraylist
     * 
     * @param version
     * @param dComponents
     * @param sComponents
     * @return 
     */
        @Override
    public boolean copyComponentParameter(String version, ArrayList dComponents, ArrayList sComponents) {
      try{
            dComponents.clear();

            JTextFieldLinked f1 = (JTextFieldLinked) sComponents.get(1);
            JTextField f2 = (JTextField) sComponents.get(3);
            JTextField f3 = (JTextField) sComponents.get(5);
            JTextField f4 = (JTextField) sComponents.get(7);
            JCheckBox watershed = new JCheckBox("Watershed", ((JCheckBox)(sComponents.get(8))).isSelected());
            JTextField f5 = (JTextField) sComponents.get(10);
            
   
        dComponents.add(new JLabel("Low Threshold"));
        dComponents.add(f1);
        dComponents.add(new JLabel("Centroid Offset"));
        dComponents.add(f2);
        dComponents.add(new JLabel("Min Vol (vox)"));
        dComponents.add(f3);
        dComponents.add(new JLabel("Max Vol (vox)"));
        dComponents.add(f4);
        dComponents.add(watershed);
        dComponents.add(new JLabel("XY division"));
        dComponents.add(f5);
        dComponents.add(new JCheckBox("ELS", false));

        return true;
        } catch(Exception e){
            System.out.println("ERROR: Could not copy parameter(s) for " + NAME);
            return false;
        }
    }
    /**Takes  a set of values from 'fields' and populates the components , 
     * as defined herein 
     * 
     * @param version
     * @param dComponents
     * @param fields
     * @return 
     */
   
    
    @Override
    public boolean loadComponentParameter(String version, ArrayList dComponents, ArrayList fields) {
             try{
            
        //dComponents.clear();
        
        
        JTextFieldLinked n1 = (JTextFieldLinked)dComponents.get(1);
        JTextField n2 = (JTextField)dComponents.get(3);
        JTextField n3 = (JTextField)dComponents.get(5);
        JTextField n4 = (JTextField)dComponents.get(7);
        JCheckBox n5 = (JCheckBox)dComponents.get(8);
        JTextField n6 = (JTextField)dComponents.get(10);
        
        n1.setText((String)fields.get(0));
        n2.setText((String)fields.get(1));
        n3.setText((String)fields.get(2));
        n4.setText((String)fields.get(3));
        n5.setSelected((boolean)fields.get(4));
        n6.setText((String)fields.get(5));
        
        return true;
        } catch(Exception e){
            System.out.println("ERROR: Could not copy parameter(s) for " + NAME);
            return false;
        }
    }
    /**
     * Takes the a set of components, as defined herein and populates the fields
     * ArrayList for serialization.
     * @param version in case multiple versions need support 
     * @param sComponents
     * @param fields
     * @return 
     */
    @Override
    public boolean saveComponentParameter(String version, ArrayList fields, ArrayList sComponents) {
            
        
        try{
            
            JTextFieldLinked f1 = (JTextFieldLinked) sComponents.get(1);
            JTextField f2 = (JTextField) sComponents.get(3);
            JTextField f3 = (JTextField) sComponents.get(5);
            JTextField f4 = (JTextField) sComponents.get(7);
            JCheckBox watershed = new JCheckBox("Watershed", ((JCheckBox)(sComponents.get(8))).isSelected());
            JTextField f5 = (JTextField) sComponents.get(10);

            fields.add(f1.getText());
            fields.add(f2.getText());
            fields.add(f3.getText());
            fields.add(f4.getText());
            fields.add(((JCheckBox)(sComponents.get(8))).isSelected()); 
            fields.add(f5.getText());
            fields.add(false);

            return true;
        } catch(Exception e){
            System.out.println("ERROR: Could not copy parameter(s) for " + NAME + "\n" + e.getLocalizedMessage());
            return false;
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
