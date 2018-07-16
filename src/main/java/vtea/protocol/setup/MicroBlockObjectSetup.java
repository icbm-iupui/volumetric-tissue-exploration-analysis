/* 
 * Copyright (C) 2016 Indiana University
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
package vtea.protocol.setup;

import vtea.protocol.listeners.ChangeThresholdListener;
import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.RoiListener;
import ij.plugin.ChannelSplitter;
import java.awt.Component;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import static vtea._vtea.SEGMENTATIONMAP;
import vtea.objects.Segmentation.AbstractSegmentation;
import vtea.processor.ImageProcessingProcessor;

/**
 *
 * @author vinfrais
 */
public final class MicroBlockObjectSetup extends MicroBlockSetup implements ChangeThresholdListener, ImageListener, RoiListener {

    public static String getMethod(int i) {
        return vtea._vtea.PROCESSOPTIONS[i];
    }

    private DefaultCellEditor channelEditor = new DefaultCellEditor(new channelNumber());
    private DefaultCellEditor analysisEditor = new DefaultCellEditor(new analysisType());
    //private SpinnerEditor thresEditor = new SpinnerEditor();

    private Object[] columnTitles = {"Channel", "Method", "Distance(px)"};
    
    

    private boolean imageClosed = false;

    private boolean[] canEditColumns = new boolean[]{true, true, true, true};
    private TableColumn channelColumn;
    private TableColumn analysisColumn;
    private TableColumn lowThreshold;
    private TableColumn highThreshold;
    private JScrollPane jsp;
    private Object[][] CellValues = {
        {"Channel_1", "Grow", 2},
        {"Channel_2", "Grow", 2},
        {"Channel_3", "Grow", 2},
        {null, null, null},
        {null, null, null},
        {null, null, null},
        {null, null, null},
        {null, null, null},
        {null, null, null}};

    MicroThresholdAdjuster mta;

    ImagePlus ThresholdOriginal = new ImagePlus();
    ImagePlus ThresholdPreview = new ImagePlus();

    public MicroBlockObjectSetup(int step, ArrayList Channels, ImagePlus imp) {
        
        super(step, Channels);
        
        //setup the images
        
        ThresholdOriginal = imp.duplicate();
        ThresholdPreview = getThresholdPreview();

        ThresholdPreview.addImageListener(this);

        IJ.run(ThresholdPreview, "Grays", "");
        
        //setup the method
        
        super.cbm = new DefaultComboBoxModel(vtea._vtea.SEGMENTATIONOPTIONS);
        
        ProcessSelectComboBox.setModel(cbm);
        ProcessSelectComboBox.setVisible(true);  
        
        ProcessVariables = new String[vtea._vtea.SEGMENTATIONOPTIONS.length][10];
        
        
        
        //setup thresholder
        
        
        mta = new MicroThresholdAdjuster(ThresholdPreview);
        mta.addChangeThresholdListener(this);
        mta.notifyChangeThresholdListeners(mta.minThreshold, mta.maxThreshold);
        
        ProcessSelectComboBox.setSelectedIndex(0);

        makeProtocolPanel((String)ProcessSelectComboBox.getSelectedItem());

 
        setBounds(new java.awt.Rectangle(500, 160, 378, 282));

        TitleText.setText("Object_" + (step));
        TitleText.setEditable(true);
        PositionText.setText("Object_" + (step));
        PositionText.setVisible(false);
        ProcessText.setText("Object building method ");

        comments.remove(notesPane);
        tablePane.setVisible(true);

        CellValues = makeDerivedRegionTable();

        secondaryTable.setModel(new javax.swing.table.DefaultTableModel(
                CellValues,
                columnTitles
        ) {
            boolean[] canEdit = canEditColumns;

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        channelColumn = secondaryTable.getColumnModel().getColumn(0);
        analysisColumn = secondaryTable.getColumnModel().getColumn(1);

        channelColumn.setCellEditor(channelEditor);
        analysisColumn.setCellEditor(analysisEditor);

        PreviewButton.setVisible(true);
        PreviewButton.setEnabled(true);

        repaint();
        pack();
    }

    public void updateNewImage() {

        Point p = new Point();
        try {
            p = ThresholdPreview.getWindow().getLocation();
            ThresholdPreview.close();
        } catch (NullPointerException e) {
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            p = new Point(gd.getDisplayMode().getWidth() / 2, gd.getDisplayMode().getHeight() / 2);
        }
        ThresholdPreview.close();
        ThresholdPreview = getThresholdPreview();
        IJ.run(ThresholdPreview, "Grays", "");
        ThresholdPreview.updateImage();
        ThresholdPreview.show();
        ThresholdPreview.getWindow().setLocation(p);

        mta = new MicroThresholdAdjuster(ThresholdPreview);
        mta.addChangeThresholdListener(this);

        tablePane.setVisible(true);
        MethodDetails.setVisible(false);
        MethodDetails.removeAll();
        makeProtocolPanel((String)ProcessSelectComboBox.getSelectedItem());
        MethodDetails.revalidate();
        MethodDetails.repaint();
        MethodDetails.setVisible(true);
        mta.doUpdate();
        mta.notifyChangeThresholdListeners(mta.minThreshold, mta.maxThreshold);
        pack();

    }

    private Object[][] makeDerivedRegionTable() {
        Object[][] CellValues = new Object[this.Channels.size()][4];

        System.out.println("PROFILING: Number of channels: " + this.Channels.size());
        //System.out.println("PROFILING: Number of channels: " + this.Channels.size());

        for (int i = 0; i < this.Channels.size(); i++) {
            CellValues[i][0] = Channels.get(i);
            CellValues[i][1] = "Grow";
            CellValues[i][2] = 2;
        }
        return CellValues;
    }

    @Override
    protected void getSegmentationPreview() {
        CurrentStepProtocol = CurrentProcessList;

        updateProcessVariables();

        ThresholdOriginal.setRoi(ThresholdPreview.getRoi());

        PreviewProgress.setText("Getting Preview...");
        PreviewButton.setEnabled(false);

        SegmentationPreviewer p = new SegmentationPreviewer(ThresholdOriginal, getSettings());

        new Thread(p).start();

        PreviewButton.setEnabled(true);
        PreviewProgress.setText("");
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b && !(ThresholdPreview.isVisible())) {
            ThresholdPreview.show();
            ThresholdPreview.setTitle(this.TitleText.getText());
        } else {
            ThresholdPreview.hide();
        }
    }

    @Override
    protected void updateTitles() {
        ThresholdPreview.setTitle(TitleText.getText());
    }

    @Override
    protected void updateProtocolPanel(java.awt.event.ActionEvent evt) {

        if (evt.getSource() == ChannelComboBox) {

            Point p = new Point();
            try {
                p = ThresholdPreview.getWindow().getLocation();
                ThresholdPreview.hide();
            } catch (NullPointerException e) {
                GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
                p = new Point(gd.getDisplayMode().getWidth() / 2, gd.getDisplayMode().getHeight() / 2);
            }

            ThresholdPreview.hide();
            ThresholdPreview = getThresholdPreview();
            IJ.run(ThresholdPreview, "Grays", "");

            ThresholdPreview.updateImage();
            ThresholdPreview.show();
            ThresholdPreview.getWindow().setLocation(p);
            mta = new MicroThresholdAdjuster(ThresholdPreview);
            mta.addChangeThresholdListener(this);
        }

        tablePane.setVisible(true);
        MethodDetails.setVisible(false);
        MethodDetails.removeAll();
        makeProtocolPanel((String) ProcessSelectComboBox.getSelectedItem());
        MethodDetails.revalidate();
        MethodDetails.repaint();
        MethodDetails.setVisible(true);
        mta.doUpdate();
        mta.notifyChangeThresholdListeners(mta.minThreshold, mta.maxThreshold);
        pack();
    }

    @Override
    protected JPanel makeProtocolPanel(String str) {
        
        ArrayList ProcessComponents;
     
        //CurrentProcessItems.set(0, makeMethodComponentsArray(str, ProcessVariables));
        //ProcessComponents = CurrentProcessItems.get(0);
 
        ProcessComponents = makeMethodComponentsArray(str, ProcessVariables); 

        
        

        MethodDetails.setVisible(false);
        MethodDetails.removeAll();

        GridBagConstraints layoutConstraints = new GridBagConstraints();

        methodBuild.removeAll();
        methodBuild.add(mta.getPanel());
        mta.removeChangeThresholdListeners();
        mta.addChangeThresholdListener(this);

        layoutConstraints.fill = GridBagConstraints.CENTER;
        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 0;
        layoutConstraints.weightx = 1;
        layoutConstraints.weighty = 1;

        //MethodDetail
        if (ProcessComponents.size() > 0) {
            layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.gridx = 1;
            layoutConstraints.gridy = 0;
            layoutConstraints.weightx = 1;
            layoutConstraints.weighty = 1;
            MethodDetails.add((Component) ProcessComponents.get(0), layoutConstraints);
        }

        if (ProcessComponents.size() > 1) {
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 2;
            layoutConstraints.gridy = 0;

            MethodDetails.add((Component) ProcessComponents.get(1), layoutConstraints);
        }

        if (ProcessComponents.size() > 2) {
            layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.gridx = 3;
            layoutConstraints.gridy = 0;

            MethodDetails.add((Component) ProcessComponents.get(2), layoutConstraints);
        }
        if (ProcessComponents.size() > 3) {
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 4;
            layoutConstraints.gridy = 0;

            MethodDetails.add((Component) ProcessComponents.get(3), layoutConstraints);
        }
        if (ProcessComponents.size() > 4) {
            layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.gridx = 1;
            layoutConstraints.gridy = 1;

            MethodDetails.add((Component) ProcessComponents.get(4), layoutConstraints);
        }
        if (ProcessComponents.size() > 5) {
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 2;
            layoutConstraints.gridy = 1;

            MethodDetails.add((Component) ProcessComponents.get(5), layoutConstraints);
        }
        if (ProcessComponents.size() > 6) {
            layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.gridx = 3;
            layoutConstraints.gridy = 1;

            MethodDetails.add((Component) ProcessComponents.get(6), layoutConstraints);
        }
        if (ProcessComponents.size() > 7) {
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 4;
            layoutConstraints.gridy = 1;

            MethodDetails.add((Component) ProcessComponents.get(7), layoutConstraints);
        }
        mta.doUpdate();
        pack();
        MethodDetails.setVisible(true);
        
        //Current Process list is handed to Arraylit for SIP processing

        CurrentProcessList = ProcessComponents;

        
        
        return MethodDetails;
    }

    @Override
    protected JPanel makeProtocolPanel(int position) {

        ArrayList ProcessComponents;

        ProcessComponents = CurrentProcessItems.set(position, makeMethodComponentsArray(position, ProcessVariables));
        ProcessComponents = CurrentProcessItems.get(position);

        MethodDetails.setVisible(false);
        MethodDetails.removeAll();

        GridBagConstraints layoutConstraints = new GridBagConstraints();

        methodBuild.removeAll();
        methodBuild.add(mta.getPanel());
        mta.removeChangeThresholdListeners();
        mta.addChangeThresholdListener(this);

        layoutConstraints.fill = GridBagConstraints.CENTER;
        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 0;
        layoutConstraints.weightx = 1;
        layoutConstraints.weighty = 1;

        //MethodDetail
        if (ProcessComponents.size() > 0) {
            layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.gridx = 1;
            layoutConstraints.gridy = 0;
            layoutConstraints.weightx = 1;
            layoutConstraints.weighty = 1;
            MethodDetails.add((Component) ProcessComponents.get(0), layoutConstraints);
        }

        if (ProcessComponents.size() > 1) {
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 2;
            layoutConstraints.gridy = 0;

            MethodDetails.add((Component) ProcessComponents.get(1), layoutConstraints);
        }

        if (ProcessComponents.size() > 2) {
            layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.gridx = 3;
            layoutConstraints.gridy = 0;

            MethodDetails.add((Component) ProcessComponents.get(2), layoutConstraints);
        }
        if (ProcessComponents.size() > 3) {
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 4;
            layoutConstraints.gridy = 0;

            MethodDetails.add((Component) ProcessComponents.get(3), layoutConstraints);
        }
        if (ProcessComponents.size() > 4) {
            layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.gridx = 1;
            layoutConstraints.gridy = 1;

            MethodDetails.add((Component) ProcessComponents.get(4), layoutConstraints);
        }
        if (ProcessComponents.size() > 5) {
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 2;
            layoutConstraints.gridy = 1;

            MethodDetails.add((Component) ProcessComponents.get(5), layoutConstraints);
        }
        if (ProcessComponents.size() > 6) {
            layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.gridx = 3;
            layoutConstraints.gridy = 1;

            MethodDetails.add((Component) ProcessComponents.get(6), layoutConstraints);
        }
        if (ProcessComponents.size() > 7) {
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 4;
            layoutConstraints.gridy = 1;

            MethodDetails.add((Component) ProcessComponents.get(7), layoutConstraints);
        }
        mta.doUpdate();
        pack();
        MethodDetails.setVisible(true);
        CurrentProcessList.clear();
        CurrentProcessList.add(cbm.getSelectedItem());
        CurrentProcessList.addAll(ProcessComponents);
        return MethodDetails;
    }

    private ImagePlus getThresholdPreview() {
        ChannelSplitter cs = new ChannelSplitter();
        ImagePlus imp = new ImagePlus("Threshold " + super.TitleText.getText(),
                cs.getChannel(ThresholdOriginal,
                        this.ChannelComboBox.getSelectedIndex() + 1).duplicate()) {
        };
        imp.hide();
        return imp;
    }

    private int getChannelIndex(String text) {
        return Channels.indexOf(text);
    }

    @Override
    protected ArrayList makeMethodComponentsArray(String method, String[][] str) {
        Object iImp = new Object();

        try {
            Class<?> c;
            c = Class.forName(SEGMENTATIONMAP.get(method));
            Constructor<?> con;
            try {
                con = c.getConstructor();
                iImp = con.newInstance();
                return ((AbstractSegmentation) iImp).getOptions();
                
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(ImageProcessingProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ImageProcessingProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }

        ArrayList result = new ArrayList();
        return result;
    }

    @Override
    protected ArrayList makeMethodComponentsArray(int position, String[][] values) {

        ArrayList result = new ArrayList();
        
        
        

//        if (this.ProcessSelectComboBox.getItemAt(position).equals("LayerCake 3D")) {
//            result.add(new JLabel("Low Threshold"));
//            result.add(new JTextField(values[position][0]));
//            result.add(new JLabel("Centroid Offset"));
//            result.add(new JTextField(values[position][1]));
//            result.add(new JLabel("Min Vol (vox)"));
//            result.add(new JTextField(values[position][2]));
//            result.add(new JLabel("Max Vol (vox)"));
//            result.add(new JTextField(values[position][3]));
//        }
//        if (this.ProcessSelectComboBox.getItemAt(position).equals("FloodFill 3D")) {
//            result.add(new JLabel("Low Threshold"));
//            result.add(new JTextField(values[position][0]));
//            result.add(new JLabel("High Threshold"));
//            result.add(new JTextField(values[position][1]));
//            result.add(new JLabel("Min Vol (vox)"));
//            result.add(new JTextField(values[position][2]));
//            result.add(new JLabel("Max Vol (vox)"));
//            result.add(new JTextField(values[position][3]));
//        }
//        if (position == 2) {
//            result.add(new JLabel("Solution not supported"));
//            result.add(new JTextField("5"));
//        }
        return result;
    }

    @Override
    protected ArrayList makeSecondaryComponentsArray(int position) {

        ArrayList result = new ArrayList();

        result.add(new JLabel("Min. Size (vox)"));
        result.add(new JTextField("20"));
        result.add(new JLabel("Max. Size (vox)"));
        result.add(new JTextField("100"));

        return result;
    }

    public void setProcessedImage(ImagePlus imp) {
        this.ThresholdOriginal = imp;
        ThresholdPreview = getThresholdPreview();
        IJ.run(ThresholdPreview, "Grays", "");

        ThresholdPreview.updateImage();

        mta.setImagePlus(ThresholdPreview);
        mta.doUpdate();
    }

    private void updateProcessVariables() {
        ProcessVariables[ProcessSelectComboBox.getSelectedIndex()][0] = ((JTextField) CurrentStepProtocol.get(1)).getText();
        ProcessVariables[ProcessSelectComboBox.getSelectedIndex()][1] = ((JTextField) CurrentStepProtocol.get(3)).getText();
        ProcessVariables[ProcessSelectComboBox.getSelectedIndex()][2] = ((JTextField) CurrentStepProtocol.get(5)).getText();
        ProcessVariables[ProcessSelectComboBox.getSelectedIndex()][3] = ((JTextField) CurrentStepProtocol.get(7)).getText();
    }

    @Override
    protected void blockSetupOKAction() {

        CurrentStepProtocol = CurrentProcessList;

        updateProcessVariables();
        makeProtocolPanel((String)ProcessSelectComboBox.getSelectedItem());
        notifyMicroBlockSetupListeners(getSettings());
        
         this.setVisible(false);
    }

    @Override
    protected void blockSetupCancelAction() {

    }

    /**
     * Details content breakdown
     *
     * [0] primary channel; method array list for primary channel segmentation
     * [1] secondary channel; method array list [2] secondary channel; method
     * array list [3] etc...
     *
     * method arraylist for primary [0] channel [1] key for method [2] field key
     * [3] field1 [4] field2 [5] etc...
     *
     * The primary is the first item in the arraylist
     *
     * add secondary volumes as addition lines with the same logic
     *
     * @param settings
     */
    // 0: minObjectSize, 1: maxObjectSize, 2: minOverlap, 3: minThreshold
    //field key 0: minObjectSize, 1: maxObjectSize, 2: minOverlap, 3: minThreshold
    private ArrayList getSettings() {
        
         /**segmentation and measurement protocol redefining.
         * 0: title text, 1: method (as String), 2: channel, 3: ArrayList of JComponents used 
         * for analysis 3: ArrayList of Arraylist for morphology determination
         */
        

        //ArrayList repeated = new ArrayList();
        //ArrayList key = new ArrayList();
        ArrayList result = new ArrayList();

        result.add(TitleText.getText());

        result.add((String)(ProcessSelectComboBox.getItemAt(ProcessSelectComboBox.getSelectedIndex())));
        
        result.add(ChannelComboBox.getSelectedIndex());
        
        ArrayList<JComponent> Comps = new ArrayList();
        
        Comps.addAll(CurrentStepProtocol);
     
        result.add(Comps);
        
        ArrayList<ArrayList> Measures = new ArrayList();

            for (int i = 0; i < secondaryTable.getRowCount(); i++) {
                ArrayList alDerived = new ArrayList();
                alDerived.add(getChannelIndex(secondaryTable.getValueAt(i, 0).toString()));
                alDerived.add(getAnalysisTypeInt(i, secondaryTable.getModel()));
                alDerived.add(secondaryTable.getValueAt(i, 2).toString());
                Measures.add(alDerived);
            }
        
        result.add(Measures);
        
        
        //repeated.add(ProcessSelectComboBox.getItemAt(ProcessSelectComboBox.getSelectedIndex()));
        //key.addAll(Arrays.asList("minObjectSize", "maxObjectSize", "minOverlap", "minThreshold"));
        //repeated.add(key);
        


//        if (ProcessSelectComboBox.getItemAt(ProcessSelectComboBox.getSelectedIndex()).equals("LayerCake 3D")) {
//            //build primary volume variables
//            placeholder = (JTextField) CurrentStepProtocol.get(6);
//            repeated.add(placeholder.getText());
//            //System.out.println("PROFILING: " + placeholder.getText());
//            placeholder = (JTextField) CurrentStepProtocol.get(8);
//            repeated.add(placeholder.getText());
//            //System.out.println("PROFILING: " + placeholder.getText());
//            //repeated.add("1000");
//            placeholder = (JTextField) CurrentStepProtocol.get(4);
//            repeated.add(placeholder.getText());
//            //System.out.println("PROFILING: " + placeholder.getText());
//            placeholder = (JTextField) CurrentStepProtocol.get(2);
//            repeated.add(placeholder.getText());
//            //System.out.println("PROFILING: " + placeholder.getText());
//
//            //add primary as first item in list
//            result.add(repeated);

     
        
//        if (ProcessSelectComboBox.getItemAt(ProcessSelectComboBox.getSelectedIndex()).equals("FloodFill 3D")) {
//            //build primary volume variables
//            placeholder = (JTextField) CurrentStepProtocol.get(6);
//            repeated.add(placeholder.getText());
//            placeholder = (JTextField) CurrentStepProtocol.get(8);
//            repeated.add(placeholder.getText());
//            placeholder = (JTextField) CurrentStepProtocol.get(4);
//            repeated.add(placeholder.getText());
//            placeholder = (JTextField) CurrentStepProtocol.get(2);
//            repeated.add(placeholder.getText());
//            result.add(repeated);
//
//            for (int i = 0; i < secondaryTable.getRowCount(); i++) {
//                ArrayList alDerived = new ArrayList();
//                alDerived.add(getChannelIndex(secondaryTable.getValueAt(i, 0).toString()));
//                alDerived.add(getAnalysisTypeInt(i, secondaryTable.getModel()));
//                alDerived.add(secondaryTable.getValueAt(i, 2).toString());
//                result.add(alDerived);
//            }
//        }
        return result;
    }

    private String getAnalysisTypeInt(int row, TableModel ChannelTableValues) {

//        if (ChannelTableValues.getValueAt(row, 1) == null) {
//            return 2;
//        }

        String comp = ChannelTableValues.getValueAt(row, 1).toString();
//
//        if (comp == null) {
//            return 2;
//        }
//        if (comp.equals("Mask")) {
//            return 0;
//        }
//        if (comp.equals("Grow")) {
//            return 1;
//        }
//        if (comp.equals("Fill")) {
//            return 2;
//        } else {
//            return 0;
//        }
    return comp;
    }

    @Override
    public void thresholdChanged(double min, double max) {

        double ipmin = ThresholdPreview.getProcessor().getMin();
        double ipmax = ThresholdPreview.getProcessor().getMax();

        min = ipmin + (min / 255.0) * (ipmax - ipmin);
        max = ipmin + (max / 255.0) * (ipmax - ipmin);

        tablePane.setVisible(true);
        MethodDetails.setVisible(false);
        MethodDetails.removeAll();
 
        makeProtocolPanel((String)ProcessSelectComboBox.getSelectedItem());
        
        MethodDetails.revalidate();
        MethodDetails.repaint();
        MethodDetails.setVisible(true);
        pack();

    }

    @Override
    public void roiModified(ImagePlus ip, int i) {
        if (ip.getID() == ThresholdPreview.getID()) {
            try {
                Roi r = ThresholdPreview.getRoi();
            } catch (NullPointerException e) {

            }

        }
    }

    @Override
    public void imageOpened(ImagePlus ip) {

    }

    @Override
    public void imageClosed(ImagePlus ip) {

        if (this.isVisible() && ip.getID() == ThresholdPreview.getID()) {

            JFrame frame = new JFrame();
            frame.setBackground(vtea._vtea.BUTTONBACKGROUND);
            Object[] options = {"Yes", "No"};
            int n = JOptionPane.showOptionDialog(frame,
                    "The threshold preview has been closed.  Reopen?",
                    "Preview closed.",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (n == JOptionPane.YES_OPTION) {
                ThresholdPreview = getThresholdPreview();
                mta.setImagePlus(ThresholdPreview);
                mta.doUpdate();
                ThresholdPreview.setTitle(TitleText.toString());
                ThresholdPreview.show();
            } else {
            }

        }
    }

    @Override
    public void imageUpdated(ImagePlus ip) {

    }

    private class channelNumber extends javax.swing.JComboBox {

        public channelNumber() {
            this.setModel(new javax.swing.DefaultComboBoxModel(Channels.toArray()));
        }
    ;

    };
    private class analysisType extends JComboBox {

        public analysisType() {
            this.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Mask", "Grow"}));
        }
    ;
};

}
