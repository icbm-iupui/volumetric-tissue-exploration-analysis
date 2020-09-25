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
import ij.io.Opener;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.scijava.plugin.Plugin;
import vtea.objects.layercake.microRegion;
import vteaobjects.MicroObject;

/**
 *
 * @author winfrees
 */
@Plugin(type = Segmentation.class)

public class PreLabelled extends AbstractSegmentation {

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

    JTextAreaFile file;
    JComboBox uniqueID; //do all objects have a unique ID
    JComboBoxCustom contiguousObject; //enabled only with unique ID, otherwise assumed yes.

    private ImagePlus progress;

    public PreLabelled() {
        VERSION = "0.1";
        AUTHOR = "Seth Winfree";
        COMMENT = "Import existing prelabelled image";
        NAME = "Prelabelled Nuclei";
        KEY = "Prelabelled";
        TYPE = "Import";

        protocol = new ArrayList();
        file = new JTextAreaFile("Double click to load labelled image");
        file.setSize(new Dimension(280, 40));
        String[] options = {"Y", "N"};
        uniqueID = new JComboBox(options);
        //startID = new JTextArea("0");
        uniqueID.setSelectedIndex(1);
        contiguousObject = new JComboBoxCustom(options);
        protocol.add(file);
        protocol.add(uniqueID);
        //protocol.add(contiguousObject);

    }

    @Override
    public void setImage(ImagePlus thresholdPreview) {
        imagePreview = thresholdPreview;
    }

    @Override
    public void updateImage(ImagePlus thresholdPreview) {
        imagePreview = thresholdPreview;

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
        panel.setPreferredSize(new Dimension(355, 150));
        panel.setLayout(new GridBagLayout());
        file.setPreferredSize(new Dimension(250, 30));
        file.setMinimumSize(new Dimension(250, 30));

        GridBagConstraints layoutConstraints = new GridBagConstraints();

        layoutConstraints.fill = GridBagConstraints.CENTER;
        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 0;
        layoutConstraints.weightx = 1;
        layoutConstraints.weighty = 1;
        //layoutConstraints.gridwidth = 1;
        panel.add(new JLabel("Labeled image: "), layoutConstraints);

        layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
        layoutConstraints.gridx = 2;
        layoutConstraints.gridy = 0;
        layoutConstraints.weightx = 1;
        layoutConstraints.weighty = 1;
        layoutConstraints.gridwidth = 3;
        panel.add(file, layoutConstraints);

        layoutConstraints.fill = GridBagConstraints.CENTER;
        layoutConstraints.gridx = 2;
        layoutConstraints.gridy = 1;
        layoutConstraints.weightx = 1;
        layoutConstraints.weighty = 1;
        layoutConstraints.gridwidth = 1;
        panel.add(new JLabel("Unique IDs:"), layoutConstraints);

        layoutConstraints.fill = GridBagConstraints.CENTER;
        layoutConstraints.gridx = 3;
        layoutConstraints.gridy = 1;
        layoutConstraints.weightx = 1;
        layoutConstraints.weighty = 1;
        layoutConstraints.gridwidth = 1;
        panel.add(uniqueID, layoutConstraints);

//        layoutConstraints.fill = GridBagConstraints.CENTER;
//        layoutConstraints.gridx = 2;
//        layoutConstraints.gridy = 2;
//        layoutConstraints.weightx = 1;
//        layoutConstraints.weighty = 1;
//        layoutConstraints.gridwidth = 1;
//        panel.add(new JLabel("Continguous:"), layoutConstraints);
//        
//        layoutConstraints.fill = GridBagConstraints.CENTER;
//        layoutConstraints.gridx = 3;
//        layoutConstraints.gridy =2;
//        layoutConstraints.weightx = 1;
//        layoutConstraints.weighty = 1;
//        layoutConstraints.gridwidth = 1;
//        panel.add(this.contiguousObject, layoutConstraints);
        return panel;
    }

    @Override
    public void doUpdateOfTool() {
        //f1.setText(String.valueOf(mta.getMin()));
        //mta.doUpdate();
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
            JTextAreaFile f1 = (JTextAreaFile) sComponents.get(0);
            JComboBox f2 = (JComboBox) sComponents.get(1);
            dComponents.add(f1);
            dComponents.add(f2);

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

            dComponents.clear();
            JTextAreaFile n1 = (JTextAreaFile) fields.get(0);
            JComboBox n2 = (JComboBox) fields.get(1);
            dComponents.add(n1);
            dComponents.add(n2);

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

            fields.add(((JTextAreaFile) sComponents.get(0)));
            fields.add(((JComboBox) sComponents.get(1)));

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
        JTextAreaFile man = (JTextAreaFile) al.get(0);
        JComboBox unique = (JComboBox) al.get(1);

        //JComboBoxCustom contiguous = (JComboBoxCustom)al.get(2);
        File image = new File(man.getText());
        ImagePlus imp = new ImagePlus();

        if (image.exists()) {

            Opener op = new Opener();
            imp = op.openImage(image.getParent(), image.getName());

            if (imp.getType() == ImagePlus.COLOR_RGB || imp.getType() == ImagePlus.COLOR_256) {
                System.out.println("WARNING: RGB images not supported, convert to 8, 16, or 32 bit.");

                JFrame frame = new JFrame();
                frame.setBackground(vtea._vtea.BUTTONBACKGROUND);
                Object[] options = {"Cancel"};
                int n = JOptionPane.showOptionDialog(frame,
                        "ERROR: RGB image files can \n "
                        + " not be openned.  Convert to grayscale.",
                        "Image format error...",
                        JOptionPane.CANCEL_OPTION,
                        JOptionPane.ERROR_MESSAGE,
                        null,
                        options,
                        options[0]);
                return false;
            }

        } else {

            System.out.println("WARNING: Could not find the image file.");

            JFrame frame = new JFrame();
            frame.setBackground(vtea._vtea.BUTTONBACKGROUND);
            Object[] options = {"Cancel"};
            int n = JOptionPane.showOptionDialog(frame,
                    "ERROR: The image file selected \n "
                    + " could not be openned.",
                    "Image not found...",
                    JOptionPane.CANCEL_OPTION,
                    JOptionPane.ERROR_MESSAGE,
                    null,
                    options,
                    options[0]);
            return false;
        }

        if (((String) unique.getSelectedItem()).equals("Y")) {

            findUnique(imp);

        } else {

            findNonUnique(imp);
        }

        System.out.println("PROFILING:  Found " + alVolumes.size() + " volumes.");
        return true;
    }

    private void findNonUnique(ImagePlus imp) {
        ImageStack stack = imp.getImageStack();

        //this.progress = IJ.createImage("Segmentation", "32-bit black", imp.getWidth(), imp.getHeight(), imp.getNSlices());
        //ImageStack progressStack = progress.getImageStack();
        //progress.show();
        //double object = 0;
        //double objectCount = 0;
         int width = stack.getWidth();
        int height = stack.getHeight();
        int size = stack.getSize();
        double max = width * height * size;


        for (int z = 0; z < stack.getSize(); z++) {
            for (int x = 0; x < stack.getWidth(); x++) {
                for (int y = 0; y < stack.getHeight(); y++) {
                    double v = (z + x + y);
                    double db = 100 * (v / max);
                    double color = stack.getVoxel(x, y, z);
                    notifyProgressListeners("Parsing pixels...", (double) db);
                    if (color > 0) {

                        System.out.println("PROFILING: next object: " + stack.getVoxel(x, y, z));

                        ArrayList<int[]> pixels = new ArrayList<int[]>();

                        int[] pixel = new int[3];

                        pixel[0] = x;
                        pixel[1] = y;
                        pixel[2] = z;

                        pixels.add(pixel);

                        stack.setVoxel(x, y, z, 0);

                        if (!(x + 1 < 0 || x + 1 >= width)) {
                            if (stack.getVoxel(x + 1, y, z) == color) {
                                pixels = floodfill_6C_3D(stack, x + 1, y, z, width, height, size, pixels, color, 0);
                                //System.out.println("PROFILING: x+1, depth: " + depth+ ", object: " + color);
                            }
                        }
                        if (!(x - 1 < 0 || x - 1 >= width)) {
                            if (stack.getVoxel(x - 1, y, z) == color) {
                                pixels = floodfill_6C_3D(stack, x - 1, y, z, width, height, size, pixels, color, 0);
                                //System.out.println("PROFILING: x-1, depth: " + depth+ ", object: " + color);
                            }
                        }
                        if (!(y + 1 < 0 || y + 1 >= height)) {
                            if (stack.getVoxel(x, y + 1, z) == color) {
                                pixels = floodfill_6C_3D(stack, x, y + 1, z, width, height, size, pixels, color, 0);
                                //System.out.println("PROFILING: y+1, depth: " + depth+ ", object: " + color);
                            }
                        }
                        if (!(y - 1 < 0 || y - 1 >= height)) {
                            if (stack.getVoxel(x, y - 1, z) == color) {
                                pixels = floodfill_6C_3D(stack, x, y - 1, z, width, height, size, pixels, color, 0);
                                //System.out.println("PROFILING: y-1, depth: " + depth+ ", object: " + color);
                            }
                        }
                        if (!(z + 1 < 0 || z + 1 >= size)) {
                            if (stack.getVoxel(x, y, z + 1) == color) {
                                pixels = floodfill_6C_3D(stack, x, y, z + 1, width, height, size, pixels, color, 0);
                                //System.out.println("PROFILING: z+1, depth: " + depth+ ", object: " + color);
                            }
                        }
                        if (!(z - 1 < 0 || z - 1 >= size)) {
                            if (stack.getVoxel(x, y, z - 1) == color) {
                                pixels = floodfill_6C_3D(stack, x, y, z - 1, width, height, size, pixels, color, 0);
                                //System.out.println("PROFILING: z-1, depth: " + depth + ", object: " + color);
                            }
                        }

                        pixels = floodfill_6C_3D(imp.getImageStack(), x, y, z, imp.getWidth(), imp.getHeight(), imp.getNSlices(), pixels, color, 0);

                        color = 0;

                        MicroObject obj = new MicroObject();
                        int[] xPos = new int[pixels.size()];
                        int[] yPos = new int[pixels.size()];
                        int[] zPos = new int[pixels.size()];

                        for (int c = 0; c < pixels.size(); c++) {
                            int p[] = new int[3];
                            p = pixels.get(c);
                            xPos[c] = p[0];
                            yPos[c] = p[1];
                            zPos[c] = p[2];
                        }

                        obj.setPixelsX(xPos);
                        obj.setPixelsY(yPos);
                        obj.setPixelsZ(zPos);
                        obj.setCentroid();
                        obj.setSerialID(alVolumes.size());
                        alVolumes.add(obj);
                    } 
                }
            }
        }
    }

    private void findUnique(ImagePlus imp) {
        ImageStack stack = imp.getImageStack();

        double object = 0;
        double objectCount = 0;

        //Integer is ArrayList position, Double is annotated value)
        HashMap<Integer, Double> objectLocation = new HashMap<Integer, Double>();
        HashMap<Double, Integer> objectLabel = new HashMap<Double, Integer>();
        ArrayList<ArrayList<ArrayList<Integer>>> voxels = new ArrayList<ArrayList<ArrayList<Integer>>>();
        //Objects->Dimensions->Pixels

        double max = stack.getSize() * stack.getWidth() * stack.getHeight();

        for (int n = 0; n < stack.getSize(); n++) {
            for (int x = 0; x < stack.getWidth(); x++) {
                for (int y = 0; y < stack.getHeight(); y++) {

                    double v = (n + x + y);
                    double db = 100 * (v / max);
                    notifyProgressListeners("Parsing pixels...", (double) db);

                    if (stack.getVoxel(x, y, n) != 0) {
                        if (objectLocation.containsValue(stack.getVoxel(x, y, n))) {
                            ArrayList<ArrayList<Integer>> voxs = voxels.get(objectLabel.get(stack.getVoxel(x, y, n)));
                            ArrayList<Integer> xList = voxs.get(0);
                            ArrayList<Integer> yList = voxs.get(1);
                            ArrayList<Integer> zList = voxs.get(2);
                            xList.add(x);
                            yList.add(y);
                            zList.add(n);
                        } else {
                            ArrayList<Integer> xList = new ArrayList<Integer>();
                            ArrayList<Integer> yList = new ArrayList<Integer>();
                            ArrayList<Integer> zList = new ArrayList<Integer>();
                            xList.add(x);
                            yList.add(y);
                            zList.add(n);
                            ArrayList<ArrayList<Integer>> voxs = new ArrayList<ArrayList<Integer>>();
                            voxs.add(xList);
                            voxs.add(yList);
                            voxs.add(zList);
                            voxels.add(voxs);
                            objectLocation.put(voxels.size() - 1, stack.getVoxel(x, y, n));
                            objectLabel.put(stack.getVoxel(x, y, n), voxels.size() - 1);
                        }
                    }
                }
            }
        }

        ListIterator<ArrayList<ArrayList<Integer>>> itr = voxels.listIterator();

        max = voxels.size();
        double count = 0;

        while (itr.hasNext()) {
            count++;

            double db = 100 * (count / max);
            notifyProgressListeners("Building objects...", (double) db);

            ArrayList<ArrayList<Integer>> objPix = itr.next();

            ArrayList<Integer> xList = objPix.get(0);
            ArrayList<Integer> yList = objPix.get(1);
            ArrayList<Integer> zList = objPix.get(2);

            int[] xPos = new int[xList.size()];
            int[] yPos = new int[xList.size()];
            int[] zPos = new int[xList.size()];

            for (int i = 0; i < xList.size(); i++) {
                xPos[i] = xList.get(i);
                yPos[i] = yList.get(i);
                zPos[i] = zList.get(i);
            }

            MicroObject obj = new MicroObject();
            obj.setPixelsX(xPos);
            obj.setPixelsY(yPos);
            obj.setPixelsZ(zPos);
            obj.setCentroid();
            obj.setSerialID(alVolumes.size());
            alVolumes.add(obj);
        }

    }

    private ArrayList<int[]> floodfill_6C_3D(ImageStack is, int x, int y, int z, int width, int height, int size, ArrayList<int[]> pixels, double color, int depth) {

        if (x < 0 || y < 0 || z < 0 || x >= width || y >= height || z >= size) {
            return pixels;
        }

        if (is.getVoxel(x, y, z) == color) {
            int[] pixel = new int[3];

            depth++;

            pixel[0] = x;
            pixel[1] = y;
            pixel[2] = z;

            pixels.add(pixel);

            is.setVoxel(x, y, z, 0);

            if (!(x + 1 < 0 || x + 1 >= width)) {
                if (is.getVoxel(x + 1, y, z) == color) {
                    pixels = floodfill_6C_3D(is, x + 1, y, z, width, height, size, pixels, color, depth);
                    //System.out.println("PROFILING: x+1, depth: " + depth+ ", object: " + color);
                }
            }
            if (!(x - 1 < 0 || x - 1 >= width)) {
                if (is.getVoxel(x - 1, y, z) == color) {
                    pixels = floodfill_6C_3D(is, x - 1, y, z, width, height, size, pixels, color, depth);
                    //System.out.println("PROFILING: x-1, depth: " + depth+ ", object: " + color);
                }
            }
            if (!(y + 1 < 0 || y + 1 >= height)) {
                if (is.getVoxel(x, y + 1, z) == color) {
                    pixels = floodfill_6C_3D(is, x, y + 1, z, width, height, size, pixels, color, depth);
                    //System.out.println("PROFILING: y+1, depth: " + depth+ ", object: " + color);
                }
            }
            if (!(y - 1 < 0 || y - 1 >= height)) {
                if (is.getVoxel(x, y - 1, z) == color) {
                    pixels = floodfill_6C_3D(is, x, y - 1, z, width, height, size, pixels, color, depth);
                    //System.out.println("PROFILING: y-1, depth: " + depth+ ", object: " + color);
                }
            }
            if (!(z + 1 < 0 || z + 1 >= size)) {
                if (is.getVoxel(x, y, z + 1) == color) {
                    pixels = floodfill_6C_3D(is, x, y, z + 1, width, height, size, pixels, color, depth);
                    //System.out.println("PROFILING: z+1, depth: " + depth+ ", object: " + color);
                }
            }
            if (!(z - 1 < 0 || z - 1 >= size)) {
                if (is.getVoxel(x, y, z - 1) == color) {
                    pixels = floodfill_6C_3D(is, x, y, z - 1, width, height, size, pixels, color, depth);
                    //System.out.println("PROFILING: z-1, depth: " + depth + ", object: " + color);
                }
            }
        }
        return pixels;
    }

    class JComboBoxCustom extends JComboBox implements ItemListener {

        String[] ActionStrings;

        public JComboBoxCustom() {
            super();

        }

        public JComboBoxCustom​(String[] actions) {
            super(actions);
            ActionStrings = actions;
            uniqueID.addItemListener(this);
            this.setEnabled(false);
        }

        @Override

        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String item = (String) e.getItem();
                if (item.equals(ActionStrings[0])) {
                    this.setEnabled(true);
                }
                if (item.equals(ActionStrings[1])) {
                    this.setSelectedIndex(0);
                    this.setEnabled(false);
                }
            }
        }

    }

    }

interface ChangeTextListener {

    public void textChanged(String[] channels);

}

class JTextAreaFile extends JTextArea {

    private File location;
    ImagePlus image;

    ArrayList<ChangeTextListener> ChangeTextListeners = new ArrayList<ChangeTextListener>();

    public JTextAreaFile(String s) {
        super(s);
    }

    @Override
    public void setSize(Dimension d) {
        setSize(d.width, d.height); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(100, 30); //To change body of generated methods, choose Tools | Templates.
    }

    public ImagePlus getRedirectImage() {
        return image;
    }

    public File getRedirectSource() {
        return location;
    }

    public void addChangeTextListener(ChangeTextListener listener) {
        ChangeTextListeners.add(listener);
    }

    private void notifyChangeTextListeners(String[] channels) {
        for (ChangeTextListener listener : ChangeTextListeners) {
            listener.textChanged(channels);
        }
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        if (e.getClickCount() == 2) {
            JFileChooser objectimagejfc = new JFileChooser(vtea._vtea.LASTDIRECTORY);
            FileNameExtensionFilter filter2
                    = new FileNameExtensionFilter("TIFF image file.", ".tif", "tif");
            objectimagejfc.addChoosableFileFilter(filter2);
            objectimagejfc.setFileFilter(filter2);

            int returnVal = objectimagejfc.showOpenDialog(this);
            location = objectimagejfc.getSelectedFile();

            if (returnVal == JFileChooser.APPROVE_OPTION) {

                new Thread(() -> {
                    Opener op = new Opener();
                    //setText("Loading image...");
                    this.setFocusable(false);
                    image = op.openImage(location.getParent(), location.getName());
                    String[] channels = new String[image.getNChannels()];
                    for (int i = 1; i <= image.getNChannels(); i++) {
                        channels[i - 1] = "Channel " + i;
                        notifyChangeTextListeners(channels);
                    }
                }).start();
                this.setFocusable(true);
                int size = 15;
                if (location.getName().length() < 15) {
                    size = location.getName().length();
                }

                setText(location.getPath());
            } else {

                setText("Double click to load labelled image");
            }
        }
    }

};
