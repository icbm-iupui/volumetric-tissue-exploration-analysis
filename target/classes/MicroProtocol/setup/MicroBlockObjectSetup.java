/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MicroProtocol.setup;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.plugin.ChannelSplitter;
import ij.plugin.frame.ThresholdAdjuster;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
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
public class MicroBlockObjectSetup extends MicroBlockSetup {

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

    
        mta = new MicroThresholdAdjuster(ThresholdPreview); 
        
        makeProtocolPanel(step);

        super.cbm = new DefaultComboBoxModel(VTC._VTC.PROCESSOPTIONS);
        super.cbm.setSelectedItem("Select Method");
        setBounds(new java.awt.Rectangle(500, 160, 378, 282));

        TitleText.setText("Object_" + (step));
        TitleText.setEditable(true);
        PositionText.setText("Object_" + (step));
        PositionText.setVisible(false);
        ProcessText.setText("Object building method ");
        //MenuTypeText.setText("method");

        comments.remove(notesPane);
        tablePane.setVisible(false);
        
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
    public void setVisible(boolean b) {    
        super.setVisible(b); 
        if(b){
            ThresholdPreview.show(); 
            ThresholdPreview.setTitle(this.TitleText.getText());
            //ThresholdPreview.getWindow().setAlwaysOnTop(true);
        }
        else {ThresholdPreview.hide();}
    }
    
    

    @Override
    protected void updateTitles() {
         ThresholdPreview.setTitle(TitleText.getText()); 
    }
    
   
    @Override
    protected void updateProtocolPanel(java.awt.event.ActionEvent evt) {
        if(ProcessSelectComboBox.getSelectedIndex() != 0){
        if(evt.getSource() == ChannelComboBox){
            
            Point p = new Point();
            p = ThresholdPreview.getWindow().getLocation();
            ThresholdPreview.hide();
            ThresholdPreview = getThresholdPreview();
            ThresholdPreview.updateImage();
            ThresholdPreview.show();
            ThresholdPreview.getWindow().setLocation(p);
            
            //System.out.println("PROFILING: UpdateProtocol, ChannelComboBox");
            mta = new MicroThresholdAdjuster(ThresholdPreview); 
            mta.doUpdate();
            
        }
       tablePane.setVisible(true);
        MethodDetails.setVisible(false);
        MethodDetails.removeAll();
        makeProtocolPanel(ProcessSelectComboBox.getSelectedIndex());
        MethodDetails.revalidate();
        MethodDetails.repaint();
        MethodDetails.setVisible(true);
        } else {
        methodBuild.removeAll();
        MethodDetails.removeAll();
        MethodDetails.repaint();
        MethodDetails.setVisible(true);
        //ThresholdPreview.hide();
        }
        pack();
    }

    @Override
    protected JPanel makeProtocolPanel(int position) {

        ArrayList ProcessComponents;
            
        if (CurrentProcessItems.get(position) == null) {
            ProcessComponents = CurrentProcessItems.set(position, makeMethodComponentsArray(position));
            ProcessComponents = CurrentProcessItems.get(position);
        } else {
            ProcessComponents = CurrentProcessItems.get(position);
        }
        

        MethodDetails.setVisible(false);
        MethodDetails.removeAll();

        GridBagConstraints layoutConstraints = new GridBagConstraints();
   //     GridBagConstraints secondaryLayoutConstraints = new GridBagConstraints();
        
//        secondaryFilter.removeAll();
//        
//        ArrayList SecondaryComponents;
//        
//        SecondaryComponents = this.makeSecondaryComponentsArray(position);
//        
//        //Secondary-size filter
//        if (SecondaryComponents.size() > 0) {
//            secondaryLayoutConstraints.fill = GridBagConstraints.CENTER;
//            secondaryLayoutConstraints.gridx = 1;
//            secondaryLayoutConstraints.gridy = 0;
//            secondaryLayoutConstraints.weightx = 1;
//            secondaryLayoutConstraints.weighty = 1;
//            secondaryFilter.add((Component) SecondaryComponents.get(0), secondaryLayoutConstraints);
//        }
//
//        if (SecondaryComponents.size() > 1) {
//            secondaryLayoutConstraints.fill = GridBagConstraints.HORIZONTAL;
//            secondaryLayoutConstraints.gridx = 2;
//            secondaryLayoutConstraints.gridy = 0;
//            //layoutConstraints.weightx = 1;
//            //layoutConstraints.weighty = 1;
//            secondaryFilter.add((Component) SecondaryComponents.get(1), secondaryLayoutConstraints);
//        } 
//        if (SecondaryComponents.size() > 2) {
//            secondaryLayoutConstraints.fill = GridBagConstraints.CENTER;
//            secondaryLayoutConstraints.gridx = 3;
//            secondaryLayoutConstraints.gridy = 0;
//            //layoutConstraints.weightx = 1;
//            //layoutConstraints.weighty = 1;
//            secondaryFilter.add((Component) SecondaryComponents.get(2), secondaryLayoutConstraints);
//        }
//        if (SecondaryComponents.size() > 3) {
//            secondaryLayoutConstraints.fill = GridBagConstraints.HORIZONTAL;
//            secondaryLayoutConstraints.gridx = 4;
//            secondaryLayoutConstraints.gridy = 0;
//            //layoutConstraints.weightx = 1;
//            //layoutConstraints.weighty = 1;
//            secondaryFilter.add((Component) SecondaryComponents.get(3), secondaryLayoutConstraints);
//        }
 

            methodBuild.removeAll();
        
        if(this.ProcessSelectComboBox.getSelectedIndex() != 0){
            methodBuild.add(mta.getPanel());
        
            layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.gridx = 0;
            layoutConstraints.gridy = 0;
            layoutConstraints.weightx = 1;
            layoutConstraints.weighty = 1;
        //MethodDetails.add(mta.getPanel());
        
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
            //layoutConstraints.weightx = 1;
            //layoutConstraints.weighty = 1;
            MethodDetails.add((Component) ProcessComponents.get(1), layoutConstraints);
        }

        if (ProcessComponents.size() > 2) {
            layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.gridx = 3;
            layoutConstraints.gridy = 0;
            //layoutConstraints.weightx = 1;
            //layoutConstraints.weighty = 1;
            MethodDetails.add((Component) ProcessComponents.get(2), layoutConstraints);
        }
        if (ProcessComponents.size() > 3) {
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 4;
            layoutConstraints.gridy = 0;
            //layoutConstraints.weightx = 1;
            //layoutConstraints.weighty = 1;
            MethodDetails.add((Component) ProcessComponents.get(3), layoutConstraints);
        }
        if (ProcessComponents.size() > 4) {
            layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.gridx = 1;
            layoutConstraints.gridy = 1;
            //layoutConstraints.weightx = 1;
            //layoutConstraints.weighty = 1;
            MethodDetails.add((Component) ProcessComponents.get(4), layoutConstraints);
        }
        if (ProcessComponents.size() > 5) {
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 2;
            layoutConstraints.gridy = 1;
            //layoutConstraints.weightx = 1;
            //layoutConstraints.weighty = 1;
            MethodDetails.add((Component) ProcessComponents.get(5), layoutConstraints);
        }
        if (ProcessComponents.size() > 6) {
            layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.gridx = 3;
            layoutConstraints.gridy = 1;
            //layoutConstraints.weightx = 1;
            //layoutConstraints.weighty = 1;
            MethodDetails.add((Component) ProcessComponents.get(6), layoutConstraints);
        }
        if (ProcessComponents.size() > 7) {
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 4;
            layoutConstraints.gridy = 1;
            //layoutConstraints.weightx = 1;
            //layoutConstraints.weighty = 1;
            MethodDetails.add((Component) ProcessComponents.get(7), layoutConstraints);
        }
        }else{
            MethodDetails.removeAll();
            MethodDetails.setVisible(false);
        }

        pack();
        MethodDetails.setVisible(true);
       
        CurrentProcessList.clear();
        CurrentProcessList.add(cbm.getSelectedItem());
        CurrentProcessList.addAll(ProcessComponents);
        //CurrentProcessList.addAll(SecondaryComponents);

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

    public static String getMethod(int i) {
        return VTC._VTC.PROCESSOPTIONS[i];
    }

    @Override
    protected ArrayList makeMethodComponentsArray(int position) {

        ArrayList result = new ArrayList();

        if (position == 1) {
            result.add(new JLabel("Low Threshold"));
            result.add(new JTextField("750"));
            result.add(new JLabel("Region Offset"));
            result.add(new JTextField("5"));
            result.add(new JLabel("Min Vol (vox)"));
            result.add(new JTextField("20"));
            result.add(new JLabel("Max Vol (vox)"));
            result.add(new JTextField("100"));
        }
        if (position == 2) {
            result.add(new JLabel("Low Threshold"));
            result.add(new JTextField("750"));
            result.add(new JLabel("High Threshold"));
            result.add(new JTextField("4095"));
            result.add(new JLabel("Min Vol (vox)"));
            result.add(new JTextField("20"));
            result.add(new JLabel("Max Vol (vox)"));
            result.add(new JTextField("100"));
        }
        if (position == 3) {
            result.add(new JLabel("Solution not supported"));
            result.add(new JTextField("5"));
            //result.add(new JRadioButton("normalize", false));
            //result.add(new JRadioButton("secondary", false));
        }
        return result;
    }
    
    

    @Override
    protected ArrayList makeSecondaryComponentsArray(int position) {
       
        ArrayList result = new ArrayList();

        //if (position == 1) {
            result.add(new JLabel("Min. Size (vox)"));
            result.add(new JTextField("20"));
            result.add(new JLabel("Max. Size (vox)"));
            result.add(new JTextField("100"));
//        }
//        if (position == 2) {
//
//            result.add(new JLabel("Minimum Size (px)"));
//            result.add(new JTextField("20"));
//            result.add(new JLabel("Maximum Size (px)"));
//            result.add(new JTextField("100"));
//        }
//        if (position == 3) {
//            result.add(new JLabel("Solution not supported"));
//           
//            //result.add(new JRadioButton("normalize", false));
//            //result.add(new JRadioButton("secondary", false));
//        }
        return result;
    }
    
    public void setProcessedImage(ImagePlus imp){
        this.ThresholdOriginal = imp;
    }

    @Override
    protected void blockSetupOKAction() {

        //Object definition does not hold note value
        //ArrayList cps = (ArrayList)CurrentProcessList.get(2);
        makeProtocolPanel(ProcessSelectComboBox.getSelectedIndex());
        //CurrentProcessList.set(1, "");

        CurrentStepProtocol = CurrentProcessList;

//        System.out.println("PROFILING: CurrentProcessList: " + ((JTextField)(CurrentProcessList.get(2))).getText());
//        System.out.println("PROFILING: CurrentProcessList: " + ((JTextField)(CurrentProcessList.get(4))).getText());
//        System.out.println("PROFILING: CurrentProcessList: " + ((JTextField)(CurrentProcessList.get(6))).getText());
//        System.out.println("PROFILING: CurrentProcessList: " + ((JTextField)(CurrentProcessList.get(8))).getText());

        super.notifyMicroBlockSetupListeners(getSettings());

        //IJ.log("MicroBlockSetupObject result: " + getSettings());

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

        if (ProcessSelectComboBox.getSelectedIndex() == 1) {
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
        if (ProcessSelectComboBox.getSelectedIndex() == 2) {
            //build primary volume variables
            placeholder = (JTextField) CurrentStepProtocol.get(6);
            repeated.add(placeholder.getText());
            placeholder = (JTextField) CurrentStepProtocol.get(8);
            repeated.add(placeholder.getText());
            //repeated.add("1000");
            placeholder = (JTextField) CurrentStepProtocol.get(4);
            repeated.add(placeholder.getText());
            placeholder = (JTextField) CurrentStepProtocol.get(2);
            repeated.add(placeholder.getText());

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
