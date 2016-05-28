/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MicroProtocol.setup;

import ij.IJ;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
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
public class MicroBlockObjectSetup extends MicroBlockSetup {

    //static public String[] ProcessOptions = {"Select Method", "LayerCake 3D", "FloodFill 3D", "Assisted Detection 3D", "Auto Detection 3D"};

    private int getChannelIndex(String text) {
//
//        int result = 0;
//        for (int c = 0; c <= Channels.length - 1; c++) {
//            System.out.println("Translating channels: " + Channels[c] + " test: " + text);
//            if (Channels[c].equals(text)) {
//                return c;
//            }
//        }
//        return result;
        return Channels.indexOf(text);
    }

    public static String getMethod(int i) {
        return VTC._VTC.PROCESSOPTIONS[i];
    }

    private DefaultCellEditor channelEditor = new DefaultCellEditor(new channelNumber());
    private DefaultCellEditor analysisEditor = new DefaultCellEditor(new analysisType());
    //private SpinnerEditor thresEditor = new SpinnerEditor();

    private Object[] columnTitles = {"Channel", "Method", "Distance", "MOD2"};
    
    private boolean[] canEditColumns = new boolean[]{true, true, true, true, true};
    private TableColumn channelColumn;
    private TableColumn analysisColumn;
    private TableColumn lowThreshold;
    private TableColumn highThreshold;
    private JScrollPane jsp;
    private Object[][] CellValues = {
        {"Channel_1", "Grow", 2, null},
        {"Channel_2", "Grow", 2, null},
        {"Channel_3", "Grow", 2, null},
        {null, null, null, null},
        {null, null, null, null}};

    public MicroBlockObjectSetup(int step, ArrayList Channels) {

        super(step, Channels);
        super.cbm = new DefaultComboBoxModel(VTC._VTC.PROCESSOPTIONS);
        super.cbm.setSelectedItem("Select Method");
        
        setBounds(new java.awt.Rectangle(500, 160, 378, 282));

        TitleText.setText("Object_" + (step));
        TitleText.setEditable(true);
        PositionText.setText("Object_" + (step));
        PositionText.setVisible(false);
        ProcessText.setText("Primary object classifier");
        MenuTypeText.setText("method");

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

        //revalidate();
        repaint();
        pack();

    }
    
    private Object[][] makeDerivedRegionTable(){
        Object[][] CellValues = new Object[this.Channels.size()][4];
        
        for(int i = 0; i < this.Channels.size(); i++){
            CellValues[i][0] = Channels.get(i);
            CellValues[i][1] = "Grow";
            CellValues[i][2] = 2;
            CellValues[i][3] = null;
        }
        return CellValues;
    }

    @Override
    protected JPanel makeProtocolPanel(int position) {

        ArrayList ProcessComponents;
            
                if (CurrentProcessItems.get(position) == null) {
            ProcessComponents = CurrentProcessItems.set(position, makeComponentsArray(position));
            ProcessComponents = CurrentProcessItems.get(position);
        } else {
            ProcessComponents = CurrentProcessItems.get(position);
        }

        MethodDetails.setVisible(false);
        MethodDetails.removeAll();

        GridBagConstraints layoutConstraints = new GridBagConstraints();

        //MethodDetail
        if (ProcessComponents.size() > 0) {
            layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.gridx = 0;
            layoutConstraints.gridy = 0;
            layoutConstraints.weightx = 1;
            layoutConstraints.weighty = 1;
            MethodDetails.add((Component) ProcessComponents.get(0), layoutConstraints);
        }

        if (ProcessComponents.size() > 1) {
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 1;
            layoutConstraints.gridy = 0;
            //layoutConstraints.weightx = 1;
            //layoutConstraints.weighty = 1;
            MethodDetails.add((Component) ProcessComponents.get(1), layoutConstraints);
        }

        if (ProcessComponents.size() > 2) {
            layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.gridx = 2;
            layoutConstraints.gridy = 0;
            //layoutConstraints.weightx = 1;
            //layoutConstraints.weighty = 1;
            MethodDetails.add((Component) ProcessComponents.get(2), layoutConstraints);
        }
        if (ProcessComponents.size() > 3) {
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 3;
            layoutConstraints.gridy = 0;
            //layoutConstraints.weightx = 1;
            //layoutConstraints.weighty = 1;
            MethodDetails.add((Component) ProcessComponents.get(3), layoutConstraints);
        }
        if (ProcessComponents.size() > 4) {
            layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.gridx = 0;
            layoutConstraints.gridy = 1;
            //layoutConstraints.weightx = 1;
            //layoutConstraints.weighty = 1;
            MethodDetails.add((Component) ProcessComponents.get(4), layoutConstraints);
        }
        if (ProcessComponents.size() > 5) {
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 1;
            layoutConstraints.gridy = 1;
            //layoutConstraints.weightx = 1;
            //layoutConstraints.weighty = 1;
            MethodDetails.add((Component) ProcessComponents.get(5), layoutConstraints);
        }
        if (ProcessComponents.size() > 6) {
            layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.gridx = 2;
            layoutConstraints.gridy = 1;
            //layoutConstraints.weightx = 1;
            //layoutConstraints.weighty = 1;
            MethodDetails.add((Component) ProcessComponents.get(6), layoutConstraints);
        }
        if (ProcessComponents.size() > 7) {
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 3;
            layoutConstraints.gridy = 1;
            //layoutConstraints.weightx = 1;
            //layoutConstraints.weighty = 1;
            MethodDetails.add((Component) ProcessComponents.get(7), layoutConstraints);
        }

        pack();
        MethodDetails.setVisible(true);

        if (!(null == this.CurrentProcessList)) {
            this.CurrentProcessList.clear();
        }
        CurrentProcessList.add(cbm.getSelectedItem());
        //CurrentProcessList.add(jTextPane1);
        CurrentProcessList.addAll(ProcessComponents);

        return MethodDetails;
    }

    @Override
    protected ArrayList makeComponentsArray(int position) {

        ArrayList result = new ArrayList();

        if (position == 1) {
            result.add(new JLabel("Low Threshold"));
            result.add(new JTextField("750"));
            result.add(new JLabel("Region Offset"));
            result.add(new JTextField("5"));
            result.add(new JLabel("Minimum Size (px)"));
            result.add(new JTextField("20"));
            result.add(new JLabel("Maximum Size (px)"));
            result.add(new JTextField("100"));
        }
        if (position == 2) {
            result.add(new JLabel("Low Threshold"));
            result.add(new JTextField("750"));
            result.add(new JLabel("High Threshold"));
            result.add(new JTextField("4095"));
            result.add(new JLabel("Minimum Size (px)"));
            result.add(new JTextField("20"));
            result.add(new JLabel("Maximum Size (px)"));
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
    protected void blockSetupOKAction() {

        //Object definition does not hold note value
        //ArrayList cps = (ArrayList)CurrentProcessList.get(2);
        makeProtocolPanel(ProcessSelectComboBox.getSelectedIndex());
        //CurrentProcessList.set(1, "");

        CurrentStepProtocol = CurrentProcessList;

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
