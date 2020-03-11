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
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ForkJoinPool;
import static java.util.concurrent.ForkJoinTask.invokeAll;
import java.util.concurrent.RecursiveAction;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import vtea.objects.layercake.microRegion;
import vtea.objects.layercake.microVolume;
import vtea.protocol.listeners.ChangeThresholdListener;
import vtea.protocol.setup.MicroThresholdAdjuster;
import vteaobjects.MicroObject;

/**
 *
 * @author winfrees
 */
//@Plugin(type = Segmentation.class)

public class Region2DSingleThreshold extends AbstractSegmentation {

    private int[] minConstants = new int[4]; // 0: minObjectSize, 1: maxObjectSize, 2: minOverlap, 3: minThreshold
    private ImagePlus imageOriginal;
    private ImagePlus imageResult;
    private ImageStack stackOriginal;
    protected ImageStack stackResult;

    private double[][] distance;

    private boolean watershedImageJ = true;

    private ArrayList<MicroObject> alVolumes = new ArrayList<MicroObject>();
    private List<microRegion> alRegions = Collections.synchronizedList(new ArrayList<microRegion>());
    private List<microRegion> alRegionsProcessed = Collections.synchronizedList(new ArrayList<microRegion>());

    int[] settings = {0, 20, 1000, 0};

    JTextFieldLinked f1 = new JTextFieldLinked(String.valueOf(settings[0]), 5);
    JTextField f2 = new JTextField(String.valueOf(settings[1]), 5);
    JTextField f3 = new JTextField(String.valueOf(settings[2]), 5);
    //JTextField f4 = new JTextField(String.valueOf(settings[3]), 5);

    MicroThresholdAdjuster mta;

    public Region2DSingleThreshold() {
        VERSION = "0.2";
        AUTHOR = "Seth Winfree";
        COMMENT = "Single threshold regions.";
        NAME = "Layer 2D";
        KEY = "Region2DSingleThreshold";

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

        protocol.add(new JLabel("Low Threshold"));
        protocol.add(f1);
        protocol.add(new JLabel("Min Vol (vox)"));
        protocol.add(f2);
        protocol.add(new JLabel("Max Vol (vox)"));
        protocol.add(f3);
        protocol.add(new JCheckBox("Watershed", true));

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
            //JTextField f4 = (JTextField) sComponents.get(7);
            JCheckBox watershed = new JCheckBox("Watershed", ((JCheckBox) (sComponents.get(6))).isSelected());

            dComponents.add(new JLabel("Low Threshold"));
            dComponents.add(f1);
            dComponents.add(new JLabel("Min Vol (vox)"));
            dComponents.add(f2);
            dComponents.add(new JLabel("Max Vol (vox)"));
            dComponents.add(f3);
            dComponents.add(watershed);

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
            //JTextField n4 = (JTextField)dComponents.get(7);
            JCheckBox n5 = (JCheckBox) dComponents.get(6);

            n1.setText((String) fields.get(0));
            n2.setText((String) fields.get(1));
            n3.setText((String) fields.get(2));
            //n4.setText((String)fields.get(3));
            n5.setSelected((boolean) fields.get(3));

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
            //JTextField f4 = (JTextField) sComponents.get(7);
            JCheckBox watershed = new JCheckBox("Watershed", ((JCheckBox) (sComponents.get(6))).isSelected());

            fields.add(f1.getText());
            fields.add(f2.getText());
            fields.add(f3.getText());
            //fields.add(f4.getText());
            fields.add(((JCheckBox) (sComponents.get(6))).isSelected());

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

        System.out.println("PROFILING: processing on Layer2D...");
        //System.out.println("PROFILING: Image width: " + is[0].getWidth() + ", height: " + is[0].getHeight());

        /**
         * segmentation and measurement protocol redefining. 0: title text, 1:
         * method (as String), 2: channel, 3: ArrayList of JComponents used for
         * analysis 3: ArrayList of Arraylist for morphology determination
         */
        // 0: minObjectSize, 1: maxObjectSize, 2: minOverlap, 3: minThreshold
        // 0: minObjectSize, 1: maxObjectSize, 2: minThreshold
        ArrayList al = (ArrayList) protocol.get(3);

        /**
         * PLugin JComponents starts at 1
         */
        minConstants[2] = Integer.parseInt(((JTextField) (al.get(1))).getText());
        minConstants[0] = Integer.parseInt(((JTextField) (al.get(3))).getText());
        minConstants[1] = Integer.parseInt(((JTextField) (al.get(5))).getText());
        watershedImageJ = ((JCheckBox) (al.get(6))).isSelected();

        int segmentationChannel = (int) protocol.get(2);

        ///System.out.println("PROFILING: segmentation channel: " + segmentationChannel + " for " + is.length + " channels.");
        stackOriginal = is[segmentationChannel];
        imageOriginal = new ImagePlus("Mask", stackOriginal);
        stackResult = stackOriginal.duplicate();

        // System.out.println("PROFILING: processing on Layer2D... settings collected. Channels: " + imageOriginal.getNChannels());
        //Segment and preprocess the image
        for (int x = 0; x < stackResult.getWidth(); x++) {
            for (int y = 0; y < stackResult.getHeight(); y++) {
                if (stackResult.getVoxel(x, y, 0) >= minConstants[2]) {
                    stackResult.setVoxel(x, y, 0, (Math.pow(2, stackResult.getBitDepth())) - 1);
                } else {
                    stackResult.setVoxel(x, y, 0, 0);
                }
            }
        }

        imageResult = new ImagePlus("Mask Result", stackResult);

        //}
        //imageResult = new ImagePlus("Mask Result", stackResult);
        IJ.run(imageResult, "8-bit", "");
        IJ.run(imageResult, "Invert", "");
        if (watershedImageJ) {
            IJ.run(imageResult, "Watershed", "");
        }
        IJ.run(imageResult, "Invert", "");

        //define the regions
        notifyProgressListeners("Finding regions...", 10.0);

        RegionForkPool rrf = new RegionForkPool(imageResult.getImageStack(), stackOriginal, 0, 0);
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(rrf);

        //sort the regions
        //build the volumes
        //int z = 0;
        int nVolumesLocal = 0;
        //microVolume volume = new microVolume();
        //double[] startRegion = new double[2];

        microRegion test = new microRegion();

        int db = 0;

        for (int i = 0; i < alRegions.size(); i++) {

            db = (100 * (i + 1)) / alRegions.size();

            notifyProgressListeners("Building volumes...", (double) db);

            test = alRegions.get(i);

            if (!test.isAMember()) {
                nVolumesLocal++;
                test.setMembership(nVolumesLocal);
                test.setAMember(true);
                //z = 0;
                alRegionsProcessed.add(test);
                microVolume mv = new microVolume();
                if ((test.getPixelsX()).length >= minConstants[0]
                        && (test.getPixelsX()).length <= minConstants[1]) {
                    mv.setPixelsX(test.getPixelsX());
                    mv.setPixelsY(test.getPixelsY());

                    int[] zPixels = new int[mv.getPixelsX().length];
                    for (int j = 0; j < mv.getPixelsX().length; j++) {
                        zPixels[j] = 0;
                    }
                    mv.setPixelsZ(zPixels);

                    alVolumes.add(mv);
                    mv.setCentroid();
                    mv.setSerialID(alVolumes.size() - 1);
                }
            }
        }

        System.out.println("PROFILING:  Found " + alVolumes.size() + " volumes.");

        return true;
    }

    private class RegionForkPool extends RecursiveAction {

        private int maxsize = 1;
        private final ArrayList<microRegion> alResult = new ArrayList<>();

        int n_positions = 0;

        int count = 0;
        private ImageStack result;
        private ImageStack original;
        private int start;
        private int stop;

        RegionForkPool(ImageStack st, ImageStack orig, int start, int stop) {

            result = st;
            original = orig;

            this.start = start;
            this.stop = stop;
            maxsize = result.getWidth() * result.getHeight();

        }

        private void defineRegions() {

            int color = 1;
            int region = 0;
            ArrayList<int[]> pixels = new ArrayList<int[]>();

            for (int p = 0; p < result.getWidth(); p++) {
                for (int q = 0; q < result.getHeight(); q++) {

                    if (getPixelBounds(result, p, q) >= 255) {
                        //System.out.println("PROFILING: pixel value: " + getPixelBounds(result, p, q));
                        pixels = floodfill(result, p, q, result.getWidth(), result.getHeight(), color, pixels);

                        System.out.println("PROFILING: region size: " + pixels.size());

                        int[] pixel = new int[2];
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

                        alResult.add(new microRegion(xPixels, yPixels, xPixels.length, 0, original));
                        //System.out.println("PROFILING: Adding region:" + alResult.size() +" of size " + xPixels.length);

                        if (color < 253) {
                            //System.out.println("PROFILING: Color change" + color);
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
            //}
            //System.out.println("PROFILING: ...Regions found in thread:  " + alResult.size());

        }

        private ArrayList<int[]> floodfill(ImageStack stack, int x, int y, int width, int height, int color, ArrayList<int[]> pixels) {

            if (x < 0 || y < 0 || x >= width || y >= height || stack.getVoxel(x, y, 0) < 255) {
                //System.out.println("PROFILING: ...Nothing at: " + x + "," + y);
                return pixels;

            } else {
                //System.out.println("PROFILING: ...Floodfilling at: " + x + "," + y + " for object " + color);
                stack.setVoxel(x, y, 0, color);

                int[] pixel = new int[2];
                pixel[0] = x;
                pixel[1] = y;

                pixels.add(pixel);

                pixels = floodfill(stack, x + 1, y, width, height, color, pixels);
                pixels = floodfill(stack, x, y + 1, width, height, color, pixels);
                pixels = floodfill(stack, x + 1, y + 1, width, height, color, pixels);
                pixels = floodfill(stack, x - 1, y, width, height, color, pixels);
                pixels = floodfill(stack, x, y - 1, width, height, color, pixels);
                pixels = floodfill(stack, x - 1, y - 1, width, height, color, pixels);
                pixels = floodfill(stack, x - 1, y + 1, width, height, color, pixels);
                pixels = floodfill(stack, x + 1, y - 1, width, height, color, pixels);
            }
            return pixels;

        }

        private double getPixelBounds(ImageStack stack, int x, int y) {

            try {
                //System.out.println("PROFILING: ...Regions found in thread:  " + stack.getVoxel(x, y, 0));
                return stack.getVoxel(x, y, 0);
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

            //long length = result.getSize() / processors;
            long length = 1;

//            if (result.getSize() < processors) {
//                length = result.getSize();
//            }
            if (stop - start > length) {

                invokeAll(new RegionForkPool(result, original, start, start + ((stop - start) / 2)),
                        new RegionForkPool(result, original, start + ((stop - start) / 2) + 1, stop));

            } else {
                defineRegions();
                setRegions();
            }
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
