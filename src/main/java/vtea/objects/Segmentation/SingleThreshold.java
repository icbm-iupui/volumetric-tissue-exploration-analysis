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
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.scijava.plugin.Plugin;
import vtea.protocol.listeners.ChangeThresholdListener;
import vtea.protocol.setup.MicroThresholdAdjuster;
import vteaobjects.MicroObject;

/**
 *
 * @author winfrees
 */
@Plugin(type = Segmentation.class)

public class SingleThreshold extends AbstractSegmentation {

    private ImagePlus imageOriginal;
    private ImagePlus imageResult;
    private ImageStack stackOriginal;
    protected ImageStack stackResult;

    private ArrayList<MicroObject> SingleThresholdObject = new ArrayList<MicroObject>();

    JTextFieldLinked f1 = new JTextFieldLinked("0", 5);

    MicroThresholdAdjuster mta;

    public SingleThreshold() {
        VERSION = "0.1";
        AUTHOR = "Seth Winfree";
        COMMENT = "Simple threshold for large regions by intensity.";
        NAME = "Single Threshold 3D";
        KEY = "SingleThreshold3D";
        TYPE = "Calculated";

        protocol = new ArrayList();

        f1.setPreferredSize(new Dimension(20, 30));
        f1.setMaximumSize(f1.getPreferredSize());
        f1.setMinimumSize(f1.getPreferredSize());

        protocol.add(new JLabel("Low Threshold"));
        protocol.add(f1);

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
        return SingleThresholdObject;
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

        System.out.println("PROFILING: processing on single Threshold...");

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
        int lowIntensity = Integer.parseInt(((JTextField) (al.get(1))).getText());

        int segmentationChannel = (int) protocol.get(2);

        stackOriginal = is[segmentationChannel];
        imageOriginal = new ImagePlus("Mask", stackOriginal);
        stackResult = stackOriginal.duplicate();

        //Segment and preprocess the image
        for (int n = 0; n < stackResult.getSize(); n++) {
            for (int x = 0; x < stackResult.getWidth(); x++) {
                for (int y = 0; y < stackResult.getHeight(); y++) {
                    if (stackResult.getVoxel(x, y, n) >= lowIntensity) {
                        stackResult.setVoxel(x, y, n, (Math.pow(2, stackResult.getBitDepth())) - 1);
                    } else {
                        stackResult.setVoxel(x, y, n, 0);
                    }
                }
            }
        }
        imageResult = new ImagePlus("Mask Result", stackResult);

        IJ.run(imageResult, "8-bit", "");
        //IJ.run(imageResult, "Invert", "stack");

        //define the regions
        notifyProgressListeners("Finding object...", 10.0);

        //loop through all pixels
        ArrayList<Number> xPixels = new ArrayList<Number>();
        ArrayList<Number> yPixels = new ArrayList<Number>();
        ArrayList<Number> zPixels = new ArrayList<Number>();

        ImageStack imageResultStack = imageResult.getImageStack();

        for (int x = 0; x < imageResult.getWidth(); x++) {
            for (int y = 0; y < imageResult.getHeight(); y++) {
                for (int z = 0; z < imageResult.getStackSize(); z++) {
                    if (imageResultStack.getVoxel(x, y, z) == 255) {
                        xPixels.add(x);
                        yPixels.add(y);
                        zPixels.add(z);
                    }
                }
            }
        }

        int[] xArray = new int[xPixels.size()];
        int[] yArray = new int[xPixels.size()];
        int[] zArray = new int[xPixels.size()];

        for (int i = 0; i < xPixels.size(); i++) {
            xArray[i] = (int) xPixels.get(i);
            yArray[i] = (int) yPixels.get(i);
            zArray[i] = (int) zPixels.get(i);
        }

        ArrayList<int[]> pixels = new ArrayList<int[]>();

        pixels.add(xArray);
        pixels.add(yArray);
        pixels.add(zArray);

        System.out.println("PROFILING: object size..." + xArray.length);

        SingleThresholdObject.add(new MicroObject(pixels, segmentationChannel, is, 0));

        return true;
    }

    public class JTextFieldLinked extends JTextField implements ChangeThresholdListener {

        JTextFieldLinked(String str, int i) {
            super(str, i);
        }

        @Override
        public void thresholdChanged(double min, double max) {
            double ipmin = imagePreview.getProcessor().getMin();
            double ipmax = imagePreview.getProcessor().getMax();

            min = ipmin + (min / 255.0) * (ipmax - ipmin);
            max = ipmin + (max / 255.0) * (ipmax - ipmin);

            //System.out.println("PROFILING: threshold minimum changes to: " + String.valueOf(Math.round(min)));
            f1.setText("" + String.valueOf(Math.round(min)));

        }
    }
}
