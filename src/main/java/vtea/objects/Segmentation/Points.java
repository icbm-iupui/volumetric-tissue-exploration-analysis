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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
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
import vtea._vtea;
import vtea.objects.layercake.microRegion;
import vteaexploration.ProgressTracker;
import vteaobjects.MicroObject;

/**
 *
 * @author winfrees
 */
//@Plugin(type = Segmentation.class)

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
    JComboBox containsMeasures; //does the csv contain features
    JComboBox fiduciarySize; //fiduciary size
    

    private ImagePlus progress;

    public Points() {
        VERSION = "0.1";
        AUTHOR = "Seth Winfree";
        COMMENT = "Import existing points with x y and optionally z position";
        NAME = "Cells as points";
        KEY = "CellsPoints";
        TYPE = "Import";

        protocol = new ArrayList();
        file = new JTextAreaFile1("Double click to load csv");
        file.setSize(new Dimension(280, 40));
        
        fiduciaryRadius = new JTextArea("5");
        String[] options = {"Y", "N"};
        String[] size = {"5","10","20"};
        containsMeasures = new JComboBox(options);
        fiduciarySize = new JComboBox(size);
        //startID = new JTextArea("0");
        //uniqueID.setSelectedIndex(1);
        //contiguousObject = new JComboBoxCustom(options);
        protocol.add(file);
        protocol.add(containsMeasures);
        
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
        JPanel container = new JPanel();
        container.setBackground(vtea._vtea.BACKGROUND);
        container.setPreferredSize(new Dimension(280, 300));
        JPanel panel = new JPanel();
        panel.setBackground(vtea._vtea.BACKGROUND);
        panel.setPreferredSize(new Dimension(270, 100));
        panel.setLayout(new GridBagLayout());
        file.setPreferredSize(new Dimension(240, 30));
        file.setMinimumSize(new Dimension(240, 30));

        GridBagConstraints layoutConstraints = new GridBagConstraints();

        layoutConstraints.fill = GridBagConstraints.CENTER;
        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 0;
        layoutConstraints.weightx = 1;
        layoutConstraints.weighty = 1;
        layoutConstraints.gridwidth = 2;
        panel.add(new JLabel("CSV file: "), layoutConstraints);

        layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 1;
        layoutConstraints.weightx = 1;
        layoutConstraints.weighty = 1;
        layoutConstraints.gridwidth = 2;
        panel.add(file, layoutConstraints);

//        layoutConstraints.fill = GridBagConstraints.CENTER;
//        layoutConstraints.gridx = 0;
//        layoutConstraints.gridy = 2;
//        layoutConstraints.weightx = 1;
//        layoutConstraints.weighty = 1;
//        layoutConstraints.gridwidth = 1;
//        panel.add(new JLabel("Contains features:"), layoutConstraints);
//        
//        layoutConstraints.fill = GridBagConstraints.CENTER;
//        layoutConstraints.gridx = 1;
//        layoutConstraints.gridy = 2;
//        layoutConstraints.weightx = 1;
//        layoutConstraints.weighty = 1;
//        layoutConstraints.gridwidth = 1;
//        containsMeasures.setSelectedIndex(1);
//        panel.add(containsMeasures, layoutConstraints);
        
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

        container.add(panel);
        return container;
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
        JComboBox unique = (JComboBox) al.get(1);

        //JComboBoxCustom contiguous = (JComboBoxCustom)al.get(2);
        File csv = new File(man.getText());
        ImagePlus imp = new ImagePlus();

        buildObjects(imp, csv);

        System.out.println("PROFILING:  Found " + alVolumes.size() + " volumes.");
        return true;
    }

    private void buildObjects(ImagePlus imp, File csv) {
        ImageStack stack = imp.getImageStack();

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
    
     
    private ArrayList<ArrayList<Number>> getPixels(float x, float y, float z, float radius){
        ArrayList<ArrayList<Number>> pixels = new ArrayList<ArrayList<Number>>();
        
         
        
        return pixels;
    }
    
    class VTEASphere {
        
    int[] x;
    int[] y;
    int[] z;
        
        
        
        
    }


    class JComboBoxCustom extends JComboBox implements ItemListener {

        String[] ActionStrings;

        public JComboBoxCustom() {
            super();

        }

        public JComboBoxCustom​(String[] actions) {
            super(actions);
            ActionStrings = actions;
            containsMeasures.addItemListener(this);
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

        ArrayList<ArrayList<Number>> csvData = new ArrayList();

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
                
            } catch (IOException ioe) {
                System.out.println("ERROR: Could not open the file.");
            }

            //csvData is an ArrayList of ArrayList<Number> where each 
            //ArrayList<Number> is a segmented nucleus and columns are features
            //Features can be added easily into VTEA by column 
            //imported CSVs may not have values for all objects, enter a -1 for 
            //now
            //Old implementation parsed each feature of each object, On2 atleast
            //parse the csvData once and build each column at the same time.
            //generate empty ArrayList for objects without data
//            ArrayList<ArrayList<Number>> FeatureColumns = new ArrayList<>();
//
//            for (int k = 0; k < dataColumns; k++) {
//                blank.add(-1);
//                FeatureColumns.add(new ArrayList<Number>());
//            }

            //build data by column
            
//            for (int c = 0; c < this.objects.size(); c++) {
//                
//                String positionText = objects.get(c).getCentroidX() + "_" +
//                        objects.get(c).getCentroidY() + "_" +
//                        objects.get(c).getCentroidZ();      
//                
////                ArrayList<Number> data = this.getObjectDataAll(zeroOrder, positionImport, csvData, blank, c, positionText);
//                for (int b = 0; b < dataColumns; b++) {
//                    ArrayList<Number> al = FeatureColumns.get(b);
//                    al.add(data.get(b));
//                }
//                progress = 100 * ((double) c / (double) objects.size());
//
//                firePropertyChange("method", "", "Importing CSV");
//                firePropertyChange("progress", "Building table", (int) progress);
//            }

//            String[] columnTitles = header.split(",");
//
//            for (int m = 0; m < FeatureColumns.size(); m++) {
//
//                ArrayList<ArrayList<Number>> result = new ArrayList<ArrayList<Number>>();
//
//                progress = 100 * ((double) m / (double) FeatureColumns.size());
//
//                firePropertyChange("method", "", "Importing CSV");
//                firePropertyChange("progress", "Importing table...", (int) progress);
//
//                result.add(FeatureColumns.get(m));
                //this.notifyAddFeatureListener((columnTitles[m].replace(".", "_")).replace("/", "_"),(columnTitles[m].replace(".", "_")).replace("/", "_"), result);

            }
        }
           
    
           
    }
    

};
