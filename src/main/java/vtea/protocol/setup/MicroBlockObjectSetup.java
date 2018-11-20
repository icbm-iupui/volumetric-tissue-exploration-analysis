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
package vtea.protocol.setup;


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
import static java.lang.Thread.MAX_PRIORITY;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import static vtea._vtea.SEGMENTATIONMAP;
import vtea.objects.Segmentation.AbstractSegmentation;
import vtea.processor.ImageProcessingProcessor;
import vtea.processor.listeners.SegmentationListener;

/**
 *
 * @author vinfrais
 */
public final class MicroBlockObjectSetup extends MicroBlockSetup implements SegmentationListener, ImageListener, RoiListener {

    public static String getMethod(int i) {
        return null;
    }

    private DefaultCellEditor channelEditor = new DefaultCellEditor(new channelNumber());
    private DefaultCellEditor analysisEditor = new DefaultCellEditor(new analysisType());
    //private SpinnerEditor thresEditor = new SpinnerEditor();
    
    private Object[] columnTitles = {"Channel", "Method", "Distance(px)"};

    private boolean[] canEditColumns = new boolean[]{true, true, true, true};
    private TableColumn channelColumn;
    private TableColumn analysisColumn;
    
    private AbstractSegmentation Approach;

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
        
        //setup the image
        
        ThresholdOriginal = imp.duplicate();
        ThresholdPreview = getThresholdPreview();
        ThresholdPreview.addImageListener(this);

        IJ.run(ThresholdPreview, "Grays", "");
        
        //setup the method
        
        super.processComboBox = new DefaultComboBoxModel(vtea._vtea.SEGMENTATIONOPTIONS);
        
        ProcessSelectComboBox.setModel(processComboBox);
        ProcessSelectComboBox.setVisible(true);  
        
        ProcessVariables = new String[vtea._vtea.SEGMENTATIONOPTIONS.length][10];        

        //setup thresholder       

        makeProtocolPanel((String)ProcessSelectComboBox.getSelectedItem());

 
        setBounds(new java.awt.Rectangle(500, 160, 378, 282));

        TitleText.setText("Object_" + (step));
        TitleText.setEditable(true);
        PositionText.setText("Object_" + (step));
        PositionText.setVisible(false);
        ProcessText.setText("Object building method ");

        comments.remove(notesPane);
        comments.remove(tablePane);
        comments.add(new JButton("Morphology Settings"));
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

        tablePane.setVisible(true);
        MethodDetails.setVisible(false);
        MethodDetails.removeAll();
        makeProtocolPanel((String)ProcessSelectComboBox.getSelectedItem());
        MethodDetails.revalidate();
        MethodDetails.repaint();
        MethodDetails.setVisible(true);

        pack();

    }

    private Object[][] makeDerivedRegionTable() {
        Object[][] CellValues = new Object[this.Channels.size()][4];

        for (int i = 0; i < this.Channels.size(); i++) {
            CellValues[i][0] = Channels.get(i);
            CellValues[i][1] = "Grow";
            CellValues[i][2] = 1;
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
        
        Thread preview = new Thread(p);
        preview.setPriority(MAX_PRIORITY);

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

        //Rewrite to handoff to a new instance of the segmentation method
        
        ThresholdPreview.removeImageListener(this);
        
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
            
        }

        tablePane.setVisible(true);
        MethodDetails.setVisible(false);
        MethodDetails.removeAll();
        makeProtocolPanel((String) ProcessSelectComboBox.getSelectedItem());
        MethodDetails.revalidate();
        MethodDetails.repaint();
        MethodDetails.setVisible(true);

        pack();
        ThresholdPreview.addImageListener(this);
    }

    @Override
    protected JPanel makeProtocolPanel(String str) {
        
        ArrayList ProcessComponents;
        
        JPanel ProcessVisualization;

       Approach = getSegmentationApproach(str);
       Approach.setImage(ThresholdPreview);

        ProcessVisualization = Approach.getSegmentationTool();
        ProcessComponents = Approach.getOptions();
        
        //process components have description and swing object
        //Current Process list is handed to Arraylist for SIP processing

        CurrentProcessList = ProcessComponents;

        MethodDetails.setVisible(false);
        MethodDetails.removeAll();

        GridBagConstraints layoutConstraints = new GridBagConstraints();

        methodBuild.removeAll();
        methodBuild.add(ProcessVisualization);

        double width = 4;
        int rows = (int)Math.ceil(ProcessComponents.size()/width);
        
        int count = 0; 
            
            for(int y = 0; y < rows; y++){      
                for(int x = 0; x < width; x++){
                    layoutConstraints.weightx = 1;
                    layoutConstraints.weighty = 1;
                    layoutConstraints.gridx = x;
                    layoutConstraints.gridy = y;
                    if(count < ProcessComponents.size()){
                    MethodDetails.add((Component) ProcessComponents.get(count), layoutConstraints);
                    count++;
                    }
                }
            }    

        pack();
        MethodDetails.setVisible(true);

        return MethodDetails;
    }

    @Override
    protected JPanel makeProtocolPanel(int position) {

        return MethodDetails;
    }

    private ImagePlus getThresholdPreview() {
        
        ChannelSplitter cs = new ChannelSplitter();
        
        
        
        //System.out.println("PROFILING:  Channel selected: " + this.ChannelComboBox.getSelectedItem());
 
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
//        Object iImp = new Object();
//
//        try {
//            Class<?> c;
//            c = Class.forName(SEGMENTATIONMAP.get(method));
//            Constructor<?> con;
//            try {
//                con = c.getConstructor();
//                iImp = con.newInstance();
//                ((AbstractSegmentation) iImp).setImage(ThresholdPreview);
//                return ((AbstractSegmentation) iImp).getOptions();
//                
//            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//                Logger.getLogger(ImageProcessingProcessor.class.getName()).log(Level.SEVERE, null, ex);
//            }
//
//        } catch (ClassNotFoundException ex) {
//            Logger.getLogger(ImageProcessingProcessor.class.getName()).log(Level.SEVERE, null, ex);
//        }

        ArrayList result = new ArrayList();
        return result;
    }

    public void setProcessedImage(ImagePlus imp) {
        this.ThresholdOriginal = imp;
        ThresholdPreview = getThresholdPreview();
        IJ.run(ThresholdPreview, "Grays", "");

        //ThresholdPreview.updateImage();

        //mta.setImagePlus(ThresholdPreview);
        //mta.doUpdate();
    }

    private void updateProcessVariables() {
        ProcessVariables[ProcessSelectComboBox.getSelectedIndex()][0] = ((JTextField) CurrentStepProtocol.get(1)).getText();
        ProcessVariables[ProcessSelectComboBox.getSelectedIndex()][1] = ((JTextField) CurrentStepProtocol.get(3)).getText();
        ProcessVariables[ProcessSelectComboBox.getSelectedIndex()][2] = ((JTextField) CurrentStepProtocol.get(5)).getText();
        ProcessVariables[ProcessSelectComboBox.getSelectedIndex()][3] = ((JTextField) CurrentStepProtocol.get(7)).getText();
    }

    @Override
    protected void blockSetupOKAction() {
        ThresholdPreview.removeImageListener(this);
        CurrentStepProtocol = CurrentProcessList;
        
        //updateProcessVariables();
        notifyMicroBlockSetupListeners(getSettings());
        
         setVisible(false);
         ThresholdPreview.addImageListener(this);
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
        return result;
    }

    private String getAnalysisTypeInt(int row, TableModel ChannelTableValues) {

//        if (ChannelTableValues.getValueAt(row, 1) == null) {
//            return 2;
//        }

        String comp = ChannelTableValues.getValueAt(row, 1).toString();

    return comp;
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
    public void imageClosed(ImagePlus ip) {
        if(this.isVisible()){
        if(!ThresholdPreview.isVisible()){
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
                ThresholdPreview.setTitle(TitleText.toString());
                IJ.run(ThresholdPreview, "Grays", "");
                
                    MethodDetails.setVisible(false);
                    MethodDetails.removeAll();
                    makeProtocolPanel((String) ProcessSelectComboBox.getSelectedItem());
                    MethodDetails.revalidate();
                    MethodDetails.repaint();
                    MethodDetails.setVisible(true);

                    ThresholdPreview.show();
                
            } else {
            }

        }
        }
    }

    @Override
    public void imageOpened(ImagePlus ip) {
    }

    @Override
    public void imageUpdated(ImagePlus ip) {

     }
    
    private AbstractSegmentation getSegmentationApproach(String method){
        Object iImp = new Object();

           try {
               Class<?> c;
               c = Class.forName(SEGMENTATIONMAP.get(method));
               Constructor<?> con;
               try {
                   con = c.getConstructor();
                   iImp = con.newInstance();

                   //((AbstractSegmentation) iImp).setImage(ThresholdPreview);
                   return (AbstractSegmentation)iImp;

               } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                   Logger.getLogger(ImageProcessingProcessor.class.getName()).log(Level.SEVERE, null, ex);
               }

           } catch (ClassNotFoundException ex) {
               Logger.getLogger(ImageProcessingProcessor.class.getName()).log(Level.SEVERE, null, ex);
           }
           
           return new AbstractSegmentation();
        
    }


    @Override
    public void updateGui(String str, Double dbl) {
        tablePane.setVisible(true);
        MethodDetails.setVisible(false);
        //MethodDetails.removeAll();
 
        //makeProtocolPanel((String)ProcessSelectComboBox.getSelectedItem());
        
        //add reflection?
        
        MethodDetails.revalidate();
        MethodDetails.repaint();
        MethodDetails.setVisible(true);
        pack();
     }

    private class channelNumber extends javax.swing.JComboBox {

        public channelNumber() {
            this.setModel(new javax.swing.DefaultComboBoxModel(Channels.toArray()));
        }
    ;

    };
    private class analysisType extends JComboBox {

        public analysisType() {
            this.setModel(new javax.swing.DefaultComboBoxModel(vtea._vtea.MORPHOLOGICALOPTIONS));
        }
    ;
};

}
