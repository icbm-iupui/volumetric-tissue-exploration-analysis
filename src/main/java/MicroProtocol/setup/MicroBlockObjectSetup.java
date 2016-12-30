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
package MicroProtocol.setup;

import MicroProtocol.listeners.ChangeThresholdListener;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.RoiListener;
import ij.plugin.ChannelSplitter;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 *
 * @author vinfrais
 */
public final class MicroBlockObjectSetup extends MicroBlockSetup implements ChangeThresholdListener, RoiListener {
    public static String getMethod(int i) {
        return VTC._VTC.PROCESSOPTIONS[i];
    }

    //static public String[] ProcessOptions = {"Select Method", "LayerCake 3D", "FloodFill 3D", "Assisted Detection 3D", "Auto Detection 3D"};



    private DefaultCellEditor channelEditor = new DefaultCellEditor(new channelNumber());
    private DefaultCellEditor analysisEditor = new DefaultCellEditor(new analysisType());
    //private SpinnerEditor thresEditor = new SpinnerEditor();

    private Object[] columnTitles = {"Channel", "Method", "Distance(px)"};
    
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
        {null, null, null}};
    
    MicroThresholdAdjuster mta;
    
    ImagePlus ThresholdOriginal = new ImagePlus();
    ImagePlus ThresholdPreview = new ImagePlus();
    
    
    
    
    
    //ImagePlus ImagePreview = new ImagePlus()
    
    public MicroBlockObjectSetup(int step, ArrayList Channels, ImagePlus imp) {
        super(step, Channels);
 
        ThresholdOriginal = imp.duplicate();
        ThresholdPreview = getThresholdPreview();
        
        IJ.run(ThresholdPreview, "Grays", "");

        mta = new MicroThresholdAdjuster(ThresholdPreview); 
        mta.addChangeThresholdListener(this);
        
        makeProtocolPanel(step);

        super.cbm = new DefaultComboBoxModel(VTC._VTC.PROCESSOPTIONS);
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

        ProcessSelectComboBox.setModel(cbm);
        ProcessSelectComboBox.setVisible(true);
        
        this.makeProtocolPanel(ProcessSelectComboBox.getSelectedIndex());
        
        PreviewButton.setVisible(true);
        PreviewButton.setEnabled(true);
        
        repaint();
        pack();
    }
    
    private Object[][] makeDerivedRegionTable(){
        Object[][] CellValues = new Object[this.Channels.size()][4];
        
        for(int i = 0; i < this.Channels.size(); i++){
            CellValues[i][0] = Channels.get(i);
            CellValues[i][1] = "Grow";
            CellValues[i][2] = 2;
        }
        return CellValues;
    }
    
    @Override
    protected void getSegmentationPreview(){
        
        CurrentStepProtocol = CurrentProcessList;  
        
        updateProcessVariables();
        
        ThresholdOriginal.setRoi(ThresholdPreview.getRoi());
        
        this.PreviewProgress.setText("Getting Preview...");

        SegmentationPreviewer.SegmentationPreviewFactory(ThresholdOriginal, getSettings());
        
        this.PreviewProgress.setText("");
        
    }

    
    @Override
    public void setVisible(boolean b) {    
        super.setVisible(b); 
        if(b){
            ThresholdPreview.show(); 
            ThresholdPreview.setTitle(this.TitleText.getText());
        }
        else {ThresholdPreview.hide();}
    }
    
    

    @Override
    protected void updateTitles() {
         ThresholdPreview.setTitle(TitleText.getText()); 
    }
    
   
    @Override
    protected void updateProtocolPanel(java.awt.event.ActionEvent evt) {
   
        if(evt.getSource() == ChannelComboBox){
            
            //mta = new MicroThresholdAdjuster(ThresholdPreview); 
            //mta.addChangeThresholdListener(this);
            mta.removeChangeThresholdListeners();
            //mta.doUpdate();
            
            Point p = new Point();
            p = ThresholdPreview.getWindow().getLocation();
            ThresholdPreview.hide();
            IJ.run(ThresholdPreview, "Grays", "");
            ThresholdPreview = getThresholdPreview();
            
            ThresholdPreview.updateImage();
            ThresholdPreview.show();
            ThresholdPreview.getWindow().setLocation(p);
            mta.setImagePlus(ThresholdPreview);
            

        }
       tablePane.setVisible(true);
        MethodDetails.setVisible(false);
        MethodDetails.removeAll();
        makeProtocolPanel(ProcessSelectComboBox.getSelectedIndex());
        MethodDetails.revalidate();
        MethodDetails.repaint();
        MethodDetails.setVisible(true);
        //mta.doUpdate();
        pack();
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
        pack();
        MethodDetails.setVisible(true);    
        CurrentProcessList.clear();
        CurrentProcessList.add(cbm.getSelectedItem());
        CurrentProcessList.addAll(ProcessComponents);
        return MethodDetails;
    }
    
    private ImagePlus getThresholdPreview(){
        ChannelSplitter cs = new ChannelSplitter();
        ImagePlus imp = new ImagePlus("Threshold "+super.TitleText.getText(),cs.getChannel(ThresholdOriginal,this.ChannelComboBox.getSelectedIndex()+1).duplicate());
        return imp;
    }
    
    private int getChannelIndex(String text) {
        return Channels.indexOf(text);
    }


    @Override
    protected ArrayList makeMethodComponentsArray(int position, String[][] values) {

        ArrayList result = new ArrayList();

        if (position == 0) {
            result.add(new JLabel("Low Threshold"));
            result.add(new JTextField(values[0][0]));
            result.add(new JLabel("Centroid Offset"));
            result.add(new JTextField(values[0][1]));
            result.add(new JLabel("Min Vol (vox)"));
            result.add(new JTextField(values[0][2]));
            result.add(new JLabel("Max Vol (vox)"));
            result.add(new JTextField(values[0][3]));
        }
        if (position == 1) {
            result.add(new JLabel("Low Threshold"));
            result.add(new JTextField(values[1][0]));
            result.add(new JLabel("High Threshold"));
            result.add(new JTextField(values[1][1]));
            result.add(new JLabel("Min Vol (vox)"));
            result.add(new JTextField(values[1][2]));
            result.add(new JLabel("Max Vol (vox)"));
            result.add(new JTextField(values[1][3]));
        }
        if (position == 2) {
            result.add(new JLabel("Solution not supported"));
            result.add(new JTextField("5"));
        }
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
    
    public void setProcessedImage(ImagePlus imp){
        this.ThresholdOriginal = imp;
    }
    
 
    private void updateProcessVariables()
    {
        ProcessVariables[ProcessSelectComboBox.getSelectedIndex()][0] = ((JTextField)CurrentStepProtocol.get(2)).getText();
        ProcessVariables[ProcessSelectComboBox.getSelectedIndex()][1] = ((JTextField)CurrentStepProtocol.get(4)).getText();
        ProcessVariables[ProcessSelectComboBox.getSelectedIndex()][2] = ((JTextField)CurrentStepProtocol.get(6)).getText();
        ProcessVariables[ProcessSelectComboBox.getSelectedIndex()][3] = ((JTextField)CurrentStepProtocol.get(8)).getText();
    }

    @Override
    protected void blockSetupOKAction() {
        
        CurrentStepProtocol = CurrentProcessList;  
        
        updateProcessVariables();

        makeProtocolPanel(ProcessSelectComboBox.getSelectedIndex());

        super.notifyMicroBlockSetupListeners(getSettings());
        
        this.setVisible(false);
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

        ArrayList repeated = new ArrayList();
        ArrayList key = new ArrayList();
        ArrayList<ArrayList> result = new ArrayList<ArrayList>();


        JTextField placeholder;

        repeated.add(ChannelComboBox.getSelectedIndex());
        repeated.add(ProcessSelectComboBox.getSelectedIndex());
        key.addAll(Arrays.asList("minObjectSize", "maxObjectSize", "minOverlap", "minThreshold"));
        repeated.add(key);

        if (ProcessSelectComboBox.getSelectedIndex() == 0) {
            //build primary volume variables
            placeholder = (JTextField) CurrentStepProtocol.get(6);
            repeated.add(placeholder.getText());
            //System.out.println("PROFILING: " + placeholder.getText());
            placeholder = (JTextField) CurrentStepProtocol.get(8);
            repeated.add(placeholder.getText());
            //System.out.println("PROFILING: " + placeholder.getText());
            //repeated.add("1000");
            placeholder = (JTextField) CurrentStepProtocol.get(4);
            repeated.add(placeholder.getText());
            //System.out.println("PROFILING: " + placeholder.getText());
            placeholder = (JTextField) CurrentStepProtocol.get(2);
            repeated.add(placeholder.getText());
            //System.out.println("PROFILING: " + placeholder.getText());

            //add primary as first item in list
            result.add(repeated);
             
            for(int i = 0; i < secondaryTable.getRowCount(); i++){
                ArrayList alDerived = new ArrayList();
                alDerived.add(getChannelIndex(secondaryTable.getValueAt(i, 0).toString()));
                alDerived.add(getAnalysisTypeInt(i, secondaryTable.getModel()));
                alDerived.add(secondaryTable.getValueAt(i, 2).toString());
                result.add(alDerived);
            }
        }
        if (ProcessSelectComboBox.getSelectedIndex() == 1) {
            //build primary volume variables
            placeholder = (JTextField) CurrentStepProtocol.get(6);
            repeated.add(placeholder.getText());
            placeholder = (JTextField) CurrentStepProtocol.get(8);
            repeated.add(placeholder.getText());
            placeholder = (JTextField) CurrentStepProtocol.get(4);
            repeated.add(placeholder.getText());
            placeholder = (JTextField) CurrentStepProtocol.get(2);
            repeated.add(placeholder.getText());
            result.add(repeated);
             
            for(int i = 0; i < secondaryTable.getRowCount(); i++){
                ArrayList alDerived = new ArrayList();
                alDerived.add(getChannelIndex(secondaryTable.getValueAt(i, 0).toString()));
                alDerived.add(getAnalysisTypeInt(i, secondaryTable.getModel()));
                alDerived.add(secondaryTable.getValueAt(i, 2).toString());
                result.add(alDerived);
            }
        }
        return result;
    }

    private int getAnalysisTypeInt(int row, TableModel ChannelTableValues) {

        if (ChannelTableValues.getValueAt(row, 1) == null) {
            return 2;
        }

        String comp = ChannelTableValues.getValueAt(row, 1).toString();

        if (comp == null) {
            return 2;
        }
        if (comp.equals("Mask")) {
            return 0;
        }
        if (comp.equals("Grow")) {
            return 1;
        }
        if (comp.equals("Fill")) {
            return 2;
        } else {
            return 0;
        }
    }

    @Override
    public void thresholdChanged(double min, double max) {
        
        max = (this.ThresholdPreview.getProcessor().getMax()/255)*max;
        min = (this.ThresholdPreview.getProcessor().getMax()/255)*min;

        String[][] str = {{String.valueOf(Math.round(min)), "5", "20", "1000"},{String.valueOf(Math.round(min)), String.valueOf(Math.round(max)), "20", "1000"}};

        tablePane.setVisible(true);
        MethodDetails.setVisible(false);
        MethodDetails.removeAll();
        
        ProcessVariables = str;
        
        makeProtocolPanel(ProcessSelectComboBox.getSelectedIndex());
        MethodDetails.revalidate();
        MethodDetails.repaint();
        MethodDetails.setVisible(true);
        pack();
        
 
    }

    @Override
    public void roiModified(ImagePlus ip, int i) {
        if(ip.getID() == ThresholdPreview.getID()){
            try{
                Roi r = ThresholdPreview.getRoi();
            }catch(NullPointerException e){
                
            }
            
        }
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
