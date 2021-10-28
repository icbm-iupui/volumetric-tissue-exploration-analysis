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

import ij.ImagePlus;
import ij.ImageStack;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.scijava.plugin.Plugin;
import vtea._vtea;
import vtea.objects.layercake.microRegion;
import vteaexploration.ProgressTracker;
import vteaobjects.MicroObject;

/**
 *
 * @author winfrees
 */
@Plugin(type = Segmentation.class)

public class Points extends AbstractSegmentation {

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

    JTextAreaFile1 file;
    JTextArea fiduciaryRadius; //radius of cell fiduciary
    JLabel identifiedVolumes;
    JComboBox fiduciarySize; //fiduciary size

    JTextArea xPosition = new JTextArea("2");
    JTextArea yPosition = new JTextArea("3");
    JTextArea zPosition = new JTextArea("4");

    String[] size = {"5", "10", "20"};

    private ImagePlus progress;

    public Points() {
        VERSION = "0.1";
        AUTHOR = "Seth Winfree";
        COMMENT = "Import existing points with x y and optionally z position";
        NAME = "Cells with Points";
        KEY = "CellsPoints";
        TYPE = "Import";

        protocol = new ArrayList();
        file = new JTextAreaFile1("Double click to load csv");
        file.setSize(new Dimension(280, 40));
        
        identifiedVolumes = new JLabel();
        identifiedVolumes.setText("");

        String[] options = {"Y", "N"};
        fiduciarySize = new JComboBox(size);
        protocol.add(file);
        protocol.add(fiduciarySize);
        protocol.add(xPosition);
        protocol.add(yPosition);
        protocol.add(zPosition);

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
        JPanel container = new JPanel();
        container.setBackground(vtea._vtea.BACKGROUND);
        container.setPreferredSize(new Dimension(280, 300));
        JPanel panel = new JPanel();
        panel.setBackground(vtea._vtea.BACKGROUND);
        panel.setPreferredSize(new Dimension(270, 100));
        panel.setLayout(new GridBagLayout());
        file.setPreferredSize(new Dimension(240, 30));
        file.setMinimumSize(new Dimension(240, 30));
        fiduciarySize.setPreferredSize(new Dimension(60, 30));
        fiduciarySize.setMinimumSize(new Dimension(60, 30));
        xPosition.setPreferredSize(new Dimension(60, 30));
        xPosition.setMinimumSize(new Dimension(60, 30));
        yPosition.setPreferredSize(new Dimension(60, 30));
        yPosition.setMinimumSize(new Dimension(60, 30));
        zPosition.setPreferredSize(new Dimension(60, 30));
        zPosition.setMinimumSize(new Dimension(60, 30));
        identifiedVolumes.setPreferredSize(new Dimension(200, 30));
        identifiedVolumes.setMinimumSize(new Dimension(200, 30));

        GridBagConstraints layoutConstraints = new GridBagConstraints();

        layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 0;
        layoutConstraints.weightx = 1;
        layoutConstraints.weighty = 1;
        layoutConstraints.gridwidth = 2;
        panel.add(file, layoutConstraints);

        layoutConstraints.fill = GridBagConstraints.CENTER;
        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 3;
        layoutConstraints.weightx = 1;
        layoutConstraints.weighty = 1;
        layoutConstraints.gridwidth = 1;
        panel.add(new JLabel("Fiduciary size:"), layoutConstraints);

        layoutConstraints.fill = GridBagConstraints.CENTER;
        layoutConstraints.gridx = 1;
        layoutConstraints.gridy = 3;
        layoutConstraints.weightx = 1;
        layoutConstraints.weighty = 1;
        layoutConstraints.gridwidth = 1;
        fiduciarySize.setSelectedIndex(1);
        panel.add(fiduciarySize, layoutConstraints);

        layoutConstraints.fill = GridBagConstraints.CENTER;
        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 4;
        layoutConstraints.weightx = 1;
        layoutConstraints.weighty = 1;
        layoutConstraints.gridwidth = 1;
        panel.add(new JLabel("Column X:"), layoutConstraints);

        layoutConstraints.fill = GridBagConstraints.CENTER;
        layoutConstraints.gridx = 1;
        layoutConstraints.gridy = 4;
        layoutConstraints.weightx = 1;
        layoutConstraints.weighty = 1;
        layoutConstraints.gridwidth = 1;
        panel.add(xPosition, layoutConstraints);

        layoutConstraints.fill = GridBagConstraints.CENTER;
        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 5;
        layoutConstraints.weightx = 1;
        layoutConstraints.weighty = 1;
        layoutConstraints.gridwidth = 1;
        panel.add(new JLabel("Column Y:"), layoutConstraints);

        layoutConstraints.fill = GridBagConstraints.CENTER;
        layoutConstraints.gridx = 1;
        layoutConstraints.gridy = 5;
        layoutConstraints.weightx = 1;
        layoutConstraints.weighty = 1;
        layoutConstraints.gridwidth = 1;
        panel.add(yPosition, layoutConstraints);

        layoutConstraints.fill = GridBagConstraints.CENTER;
        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 6;
        layoutConstraints.weightx = 1;
        layoutConstraints.weighty = 1;
        layoutConstraints.gridwidth = 1;
        panel.add(new JLabel("Column Z:"), layoutConstraints);

        layoutConstraints.fill = GridBagConstraints.CENTER;
        layoutConstraints.gridx = 1;
        layoutConstraints.gridy = 6;
        layoutConstraints.weightx = 1;
        layoutConstraints.weighty = 1;
        layoutConstraints.gridwidth = 1;
        panel.add(zPosition, layoutConstraints);
        
        layoutConstraints.fill = GridBagConstraints.CENTER;
        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 7;
        layoutConstraints.weightx = 1;
        layoutConstraints.weighty = 1;
        layoutConstraints.gridwidth = 1;
        panel.add(identifiedVolumes, layoutConstraints);


        container.add(panel);
        return container;
    }

    @Override
    public void doUpdateOfTool() {

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
            JTextAreaFile1 f1 = (JTextAreaFile1) sComponents.get(0);
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
            JTextAreaFile1 n1 = (JTextAreaFile1) fields.get(0);
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
        // 0: label, 1: file location, 2: with or without features
        ArrayList al = (ArrayList) protocol.get(3);

        /**
         * PLugin JComponents starts at 1
         */
        JTextAreaFile1 man = (JTextAreaFile1) al.get(0);
        JComboBox fiduciarySize = (JComboBox) al.get(1);
        int xCol = Integer.parseInt(((JTextArea) al.get(2)).getText());
        int yCol = Integer.parseInt(((JTextArea) al.get(3)).getText());
        int zCol = Integer.parseInt(((JTextArea) al.get(4)).getText());

        ArrayList<ArrayList<Number>> csvData = man.getCSVData();

        ImagePlus imp = new ImagePlus("image", is[0]);

        int R = Integer.parseInt(size[fiduciarySize.getSelectedIndex()]);

        alVolumes = buildObjects(imp, csvData, xCol, yCol, zCol, R);

        System.out.println("PROFILING:  Found " + alVolumes.size() + " volumes.");
        return true;
    }

    private ArrayList<MicroObject> buildObjects(ImagePlus imp,
            ArrayList<ArrayList<Number>> csvData, int xCol, int yCol, int zCol,
            int R) {

        ArrayList<MicroObject> objects = new ArrayList<MicroObject>();
        
        System.out.println("PROFILING:  Adding fiduciaries of r = " + R );

        ListIterator<ArrayList<Number>> itr = csvData.listIterator();

        while (itr.hasNext()) {

            ArrayList<Number> al = itr.next();

            ArrayList<ArrayList<Integer>> sphere = getPixels((float) al.get(xCol-1),
                    (float) al.get(yCol-1), (float) al.get(zCol-1), R, imp);

            MicroObject obj = new MicroObject();
            obj.setSerialID(Math.round((float)al.get(0)));

            ArrayList<Integer> xArray = new ArrayList<>();
            xArray = sphere.get(0);
            
            
            //System.out.println("PROFILING:  Adding object of size = " + xArray.size() );
            
            int[] xPixels = xArray.stream().mapToInt(i -> i).toArray();

            ArrayList<Integer> yArray = new ArrayList<>();
            yArray = sphere.get(1);
            int[] yPixels = yArray.stream().mapToInt(i -> i).toArray();

            ArrayList<Integer> zArray = new ArrayList<>();
            zArray = sphere.get(2);
            int[] zPixels = zArray.stream().mapToInt(i -> i).toArray();

            obj.setPixelsX(xPixels);
            obj.setPixelsY(yPixels);
            obj.setPixelsZ(zPixels);
            obj.setCentroid();

            objects.add(obj);
        }
        return objects;
    }

    private ArrayList<ArrayList<Integer>> getPixels(float X, float Y, float Z, int R, ImagePlus imp) {

        //System.out.println("PROFILING:  Adding object at = " + X + ", " + Y+ ", " + Z );
        
        ArrayList<ArrayList<Integer>> pixels = new ArrayList<ArrayList<Integer>>();

        ArrayList<Integer> xPixels = new ArrayList<Integer>();
        ArrayList<Integer> yPixels = new ArrayList<Integer>();
        ArrayList<Integer> zPixels = new ArrayList<Integer>();

//        try {

            int x0 = Math.round(X);
            int y0 = Math.round(Y);
            int z0 = Math.round(Z);
            
            //System.out.println("PROFILING:  Adding object at = " + x0 + ", " + y0+ ", " + z0 );

            int xStart = x0 - R - 1;
            int xStop = x0 + R + 1;

            xStart = lowBounds(xStart, 0);
            xStop = highBounds(xStop, imp.getWidth());

            int yStart = y0 - R - 1;
            int yStop = y0 + R + 1;

            yStart = lowBounds(yStart, 0);
            yStop = highBounds(yStop, imp.getHeight());

            int zStart = z0 - R - 1;
            int zStop = z0 + R + 1;

            zStart = lowBounds(zStart, 0);
            zStop = highBounds(zStop, imp.getNSlices());
            
            //System.out.println("PROFILING:  bounds " + xStart +","+ xStop +","+
            //        yStart +","+ yStop +","+ zStart +","+ zStop);

            for (int x = xStart; x < xStop; x++) {
                for (int y = yStart; y < yStop; y++) {
                    for (int z = zStart; z < zStop; z++) {
                        if (Math.pow(x - x0, 2) + Math.pow(y - y0, 2) + Math.pow(z - z0, 2) <= R) {
                            xPixels.add(x);
                            yPixels.add(y);
                            zPixels.add(z);
                        }
                    }
                }
            }

//        } catch (NullPointerException e) {
//            System.out.println("ERROR:  Adding object at = " + X + ", " + Y+ ", " + Z );
//        }

        pixels.add(xPixels);
        pixels.add(yPixels);
        pixels.add(zPixels);
        
        //System.out.println("PROFILING:  Built sphere of size = " + xPixels.size() );

        return pixels;
    }

    private int lowBounds(int value, int min) {

        if (value < min) {
            return min;
        } else {
            return value;
        }

    }

    private int highBounds(int value, int max) {

        if (value > max) {
            return max;
        } else {
            return value;
        }
    }


    class JComboBoxCustom extends JComboBox implements ItemListener {

        String[] ActionStrings;

        public JComboBoxCustom() {
            super();

        }

        public JComboBoxCustom​(String[] actions) {
            super(actions);
            ActionStrings = actions;
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

interface ChangeTextListener1 {

    public void textChanged(String[] channels);

}

class JTextAreaFile1 extends JTextArea {

    private File location;
    ImagePlus image;
    ArrayList<ArrayList<Number>> csvData = new ArrayList();

    ArrayList<ChangeTextListener> ChangeTextListeners = new ArrayList<ChangeTextListener>();

    public JTextAreaFile1(String s) {
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

    public ArrayList<ArrayList<Number>> getCSVData() {
        return csvData;
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        if (e.getClickCount() == 2) {

            int dataColumns = 0;

            JFileChooser jf = new JFileChooser(_vtea.LASTDIRECTORY);

            FileNameExtensionFilter filter2
                    = new FileNameExtensionFilter("CSV file.", ".csv", "csv");
            jf.addChoosableFileFilter(filter2);
            jf.setFileFilter(filter2);

            jf.setDialogTitle("Select CSV file");

            JPanel panel1 = (JPanel) jf.getComponent(3);
            JPanel panel2 = (JPanel) panel1.getComponent(3);

            int returnVal = jf.showOpenDialog(this.getParent());
            File file = jf.getSelectedFile();

            ArrayList<Number> blank = new ArrayList<Number>();

            String header = "";

            ProgressTracker tracker = new ProgressTracker();
            double progress;
            this.addPropertyChangeListener(tracker);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    BufferedReader csvReader = new BufferedReader(new FileReader(file));
                    String row;
                    boolean firstRow = true;

                    tracker.createandshowGUI("Import CSV features...", 0, 0);
                    //addPropertyChangeListener(tracker);

                    while ((row = csvReader.readLine()) != null) {

                        firePropertyChange("method", "", "Importing CSV");
                        firePropertyChange("indeterminant", "Parsing CSV", "");

                        if (firstRow) {
                            header = row;
                            firstRow = false;
                        } else {
                            String[] data = row.split(",");

                            dataColumns = data.length;

                            ArrayList<Number> dataList = new ArrayList<Number>();

                            for (int j = 0; j < data.length; j++) {
                                dataList.add(Float.parseFloat(data[j]));

                                progress = 100 * ((double) j / (double) data.length);

                                firePropertyChange("method", "", "Importing CSV");
                                firePropertyChange("progress", "Building table", (int) progress);

                            }

                            csvData.add(dataList);

                        }
                    }
                    csvReader.close();

                    setText(file.getName());
                    tracker.setVisible(false);
                    System.out.println("PROFILING:  Identified " + csvData.size() + " volumes.");

                } catch (IOException ioe) {
                    System.out.println("ERROR: Could not open the file.");
                }
            }
        }

    }

};
