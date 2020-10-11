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
import ij.gui.Roi;
import ij.io.Opener;
import ij.plugin.frame.RoiManager;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
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
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.scijava.plugin.Plugin;
import vtea.objects.layercake.microRegion;
import vtea.objects.layercake.microVolume;
import vtea.protocol.listeners.ChangeThresholdListener;
import vtea.protocol.setup.MicroThresholdAdjuster;
import vteaobjects.MicroObject;
import smile.neighbor.KDTree;
import smile.neighbor.Neighbor;
import vtea.protocol.setup.IJRoiManagerClone;

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
        protocol.add(file);

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
        panel.setPreferredSize(new Dimension(355, 300));
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
            dComponents.add(f1);

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
            dComponents.add(n1);

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

        File image = new File(man.getText());
        ImagePlus imp = new ImagePlus();

        if (image.exists()) {

            Opener op = new Opener();
            imp = op.openImage(image.getParent(), image.getName());

            //executeExploring((file.getName()).replace(".obx", ""), result, imp);
        } else {

            System.out.println("WARNING: Could not find the image file.");

            JFrame frame = new JFrame();
            frame.setBackground(vtea._vtea.BUTTONBACKGROUND);
            Object[] options = {"Yes", "No"};
            int n = JOptionPane.showOptionDialog(frame,
                    "ERROR: The image file selected could \n "
                    + " could not be openned.",
                    "Image not found...",
                    JOptionPane.CANCEL_OPTION,
                    JOptionPane.ERROR_MESSAGE,
                    null,
                    options,
                    options[0]);
            return false;
        }

        ImageStack stack = imp.getImageStack();

        double object = 0;
        double objectCount = 0;

        for (int n = 0; n < stack.getSize(); n++) {
            for (int x = 0; x < stack.getWidth(); x++) {
                for (int y = 0; y < stack.getHeight(); y++) {
                    if (stack.getVoxel(x, y, n) != object && stack.getVoxel(x, y, n) > 0) {
                        double value = stack.getVoxel(x, y, n);
                        ArrayList<int[]> pixels = new ArrayList<int[]>();
                        pixels = floodfill_6C_3D(imp.getImageStack(), x, y, n, imp.getWidth(), imp.getHeight(), imp.getNSlices(), pixels, value, 0);
                        MicroObject obj = new MicroObject();
                        
                        int[] xPos = new int[pixels.size()];
                         int[] yPos = new int[pixels.size()];
                          int[] zPos = new int[pixels.size()];
                          
                          for(int c = 0; c < pixels.size(); c++){
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

        System.out.println("PROFILING:  Found " + alVolumes.size() + " volumes.");
        return true;
    }

    private ArrayList<int[]> floodfill_6C_3D(ImageStack is, int x, int y, int z, int width, int height, int size, ArrayList<int[]> pixels, double color, int depth) {

        if (x < 0 || y < 0 || z < 0 || x >= width || y >= height || z >= size) {
            return pixels;
        } else if (is.getVoxel(x, y, z) == color) {
            int[] pixel = new int[3];
 

            depth++;
            //System.out.println("PROFILING:  Recursion depth " + depth);
            is.setVoxel(x, y, z, 0);

            pixel[0] = x;
            pixel[1] = y;
            pixel[2] = z;

            pixels.add(pixel);

            pixels = floodfill_6C_3D(is, x + 1, y, z, width, height, size, pixels, color, depth);
            pixels = floodfill_6C_3D(is, x, y + 1, z, width, height, size, pixels, color, depth);
            pixels = floodfill_6C_3D(is, x + 1, y + 1, z, width, height, size, pixels, color, depth);
            pixels = floodfill_6C_3D(is, x - 1, y, z, width, height, size, pixels, color, depth);
            pixels = floodfill_6C_3D(is, x, y - 1, z, width, height, size, pixels, color, depth);
            pixels = floodfill_6C_3D(is, x - 1, y - 1, z, width, height, size, pixels, color, depth);
            pixels = floodfill_6C_3D(is, x - 1, y + 1, z, width, height, size, pixels, color, depth);
            pixels = floodfill_6C_3D(is, x + 1, y - 1, z, width, height, size, pixels, color, depth);
            pixels = floodfill_6C_3D(is, x, y, z + 1, width, height, size, pixels, color, depth);
            pixels = floodfill_6C_3D(is, x, y, z - 1, width, height, size, pixels, color, depth);
            return pixels;
        } else {
            return pixels;
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
