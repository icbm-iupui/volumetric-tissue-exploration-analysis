/*
 * Copyright (C) 2020 SciJava
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
package vteaexploration;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.WEST;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.ListIterator;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import org.apache.commons.io.FilenameUtils;
import vtea._vtea;
import vtea.exploration.listeners.SubGateExplorerListener;
import vtea.exploration.listeners.UpdatePlotSettingsListener;
import vtea.exploration.plotgatetools.gates.Gate;
import vtea.exploration.plotgatetools.gates.PolygonGate;
import vtea.exploration.plotgatetools.listeners.MakeObjectMasksListener;
import vtea.exploration.plotgatetools.listeners.ResetSelectionListener;
import vtea.exploration.plottools.panels.XYExplorationPanel;
import vtea.gui.ComboboxToolTipRenderer;
import vtea.jdbc.H2DatabaseEngine;
import vtea.plot.PlotExportSetup;
import vtea.plot.PlotOutput;
import vtea.processor.PlotProcessor;
import vteaexploration.PlotAxesSetup;

/**
 *
 * @author sethwinfree
 */
public class ObjectTypeMapsOutputFrame extends javax.swing.JFrame implements UpdatePlotSettingsListener, ItemListener {

    PlotOutput go;
    
    ArrayList<MakeObjectMasksListener> makeobjectmaskslisteners = new ArrayList();

    ArrayList measurements;
    String[] mapTypes = {"Gate", "Feature"};
    ArrayList<String> descriptions;
    ArrayList<String> valuesFeature;
    ArrayList<Double> valuesFeatureDouble;

    ArrayList<JCheckBox> boxes;
    
    ArrayList<PolygonGate> gates;
    ArrayList<String> gatesLabels;

    ArrayList<String> descriptionsLabels;
    int position = 0;

    JPanel multipleFeatures;

    boolean[] multipleSelections;
    
    //PlotExportSetup pes;

    final static int DOWN_ARROW = 1;
    final static int UP_ARROW = 2;

    ArrayList<Boolean> selected;

    int featureCount = 0;
    String keySQLSafe = "";
    
    public static Dimension plotSize = new Dimension(500,500);

    /**
     * Creates new form GraphOutputFrame
     */
    public ObjectTypeMapsOutputFrame() {
        
    }

    public void process(String keySQLSafe, String title, ArrayList<PolygonGate> gates,ArrayList descriptions, ArrayList descriptionLabel) {

        this.keySQLSafe = keySQLSafe;
        this.setTitle(title);

        this.descriptions = new ArrayList<String>();
        this.descriptionsLabels = new ArrayList<String>();

        this.descriptions.addAll(descriptions);
        this.descriptionsLabels.addAll(descriptionLabel);
        
        this.valuesFeature = new ArrayList<String>();
        
        this.gates = gates;
        this.gatesLabels = new ArrayList<String>();
        
        if(gates.size() > 0){
            ListIterator<PolygonGate> gatesList = gates.listIterator();
            while(gatesList.hasNext()){
            gatesLabels.add(gatesList.next().getName());
                    }
        }

        initComponents();

        multipleFeatures = new JPanel();
        multipleFeatures.setPreferredSize(new Dimension(630, 300));
        multipleFeatures.setMinimumSize(new Dimension(630, 300));
        multipleFeatures.setMaximumSize(new Dimension(630, 300));

        PlotPanel.add(multipleFeatures);
        multipleFeatures.repaint();

        plotType.setModel(new DefaultComboBoxModel(vtea._vtea.MAPMAKEROPTIONS));
        plotType.setSelectedIndex(0);
        plotFeature.setModel(new DefaultComboBoxModel(this.descriptions.toArray()));
        plotFeature.setSelectedIndex(0);
        progressLabel.setText("               ");
        
        multipleFeatures.removeAll();   
        String columnHeader = this.descriptions.get(plotFeature.getSelectedIndex());      
        validateFeature(columnHeader);
        multipleSelections = new boolean[valuesFeature.size()];
        pack();
        setVisible(true);
    }

    private JScrollPane makeCheckPanel(ArrayList<String> labels) {
        boxes = new ArrayList<JCheckBox>();
  
        JPanel panel = new JPanel();
        JScrollPane jsp = new JScrollPane();
        GridBagConstraints layoutConstraints = new GridBagConstraints();

        jsp.setPreferredSize(new Dimension(600, 150));
        panel.setBackground(new Color(238, 238, 238));
        panel.setLayout(new GridBagLayout());

        int count = 0;
        
        //System.out.println("Number of labels make CheckPanel: " + labels.size());

        for (int i = 0; i < labels.size(); i++) {

            layoutConstraints.weightx = 1;
            layoutConstraints.weighty = 1;
            layoutConstraints.fill = 10 / (1 + 4 * (i % 2));
            layoutConstraints.gridx = i % 4;
            layoutConstraints.gridy = i / 4;
            layoutConstraints.anchor = WEST;
            layoutConstraints.fill = CENTER;
            if (count < labels.size()) {
                JCheckBox box = new JCheckBox(labels.get(i));
                box.addItemListener(this);
                boxes.add(box);
                panel.add(box, layoutConstraints);
                count++;
            }

            jsp.setViewportView(panel);
            jsp.validate();
            jsp.repaint();
            
        }
        return jsp;
    }


    private void setPlot() {
        ImageIcon imageIcon = go.getPlot(position);
        JLabel imgLabel = new JLabel(imageIcon);
        imgLabel.setSize(PlotPanel.getSize());
        PlotPanel.removeAll();
        PlotPanel.add(imgLabel);
        PlotPanel.validate();
        PlotPanel.repaint();
        pack();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        NorthPanel = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        jLabel1 = new javax.swing.JLabel();
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        plotType = new javax.swing.JComboBox<>();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        jLabel2 = new javax.swing.JLabel();
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        plotFeature = new javax.swing.JComboBox<>();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        SavePlot = new javax.swing.JButton();
        filler7 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        filler8 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        jToolBar2 = new javax.swing.JToolBar();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        filler9 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        Labels = new javax.swing.JTextField();
        progressLabel = new javax.swing.JLabel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        MaskProgress = new javax.swing.JProgressBar();
        PlotPanel = new javax.swing.JPanel();

        setTitle("Export Object Masks");
        setAlwaysOnTop(true);
        setMinimumSize(new java.awt.Dimension(610, 265));
        setName("Plots"); // NOI18N
        setPreferredSize(new java.awt.Dimension(610, 265));
        setResizable(false);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                formComponentMoved(evt);
            }
        });

        NorthPanel.setMaximumSize(new java.awt.Dimension(610, 70));
        NorthPanel.setMinimumSize(new java.awt.Dimension(610, 70));
        NorthPanel.setPreferredSize(new java.awt.Dimension(610, 70));
        NorthPanel.setRequestFocusEnabled(false);

        jToolBar1.setRollover(true);
        jToolBar1.setMaximumSize(new java.awt.Dimension(600, 31));
        jToolBar1.setMinimumSize(new java.awt.Dimension(600, 31));
        jToolBar1.setPreferredSize(new java.awt.Dimension(600, 31));

        jLabel1.setFont(new java.awt.Font("Helvetica", 1, 13)); // NOI18N
        jLabel1.setText("Generate Map From:");
        jToolBar1.add(jLabel1);
        jToolBar1.add(filler5);

        plotType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        plotType.setMaximumSize(new java.awt.Dimension(150, 27));
        plotType.setMinimumSize(new java.awt.Dimension(150, 27));
        plotType.setPreferredSize(new java.awt.Dimension(200, 27));
        plotType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plotTypeActionPerformed(evt);
            }
        });
        jToolBar1.add(plotType);
        jToolBar1.add(filler3);

        jLabel2.setFont(new java.awt.Font("Helvetica", 1, 13)); // NOI18N
        jLabel2.setText("Feature:");
        jToolBar1.add(jLabel2);
        jToolBar1.add(filler6);

        plotFeature.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        plotFeature.setMaximumSize(new java.awt.Dimension(150, 27));
        plotFeature.setMinimumSize(new java.awt.Dimension(150, 27));
        plotFeature.setPreferredSize(new java.awt.Dimension(150, 27));
        plotFeature.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plotFeatureActionPerformed(evt);
            }
        });
        jToolBar1.add(plotFeature);
        jToolBar1.add(filler4);

        SavePlot.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/document-save-2_24.png"))); // NOI18N
        SavePlot.setText("Build");
        SavePlot.setToolTipText("Save plot as png...");
        SavePlot.setFocusable(false);
        SavePlot.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        SavePlot.setMaximumSize(new java.awt.Dimension(90, 30));
        SavePlot.setMinimumSize(new java.awt.Dimension(90, 30));
        SavePlot.setPreferredSize(new java.awt.Dimension(90, 30));
        SavePlot.setRequestFocusEnabled(false);
        SavePlot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SavePlotActionPerformed(evt);
            }
        });
        jToolBar1.add(SavePlot);
        jToolBar1.add(filler7);
        jToolBar1.add(filler8);

        NorthPanel.add(jToolBar1);

        jToolBar2.setRollover(true);
        jToolBar2.setMaximumSize(new java.awt.Dimension(600, 31));
        jToolBar2.setMinimumSize(new java.awt.Dimension(600, 31));
        jToolBar2.setPreferredSize(new java.awt.Dimension(600, 31));

        jPanel4.setToolTipText("");
        jPanel4.setMaximumSize(new java.awt.Dimension(15, 30));
        jPanel4.setMinimumSize(new java.awt.Dimension(15, 30));
        jPanel4.setRequestFocusEnabled(false);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );

        jToolBar2.add(jPanel4);

        jLabel3.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel3.setText("Labels");
        jLabel3.setEnabled(false);
        jToolBar2.add(jLabel3);
        jToolBar2.add(filler9);

        Labels.setEnabled(false);
        Labels.setMaximumSize(new java.awt.Dimension(300, 20));
        Labels.setMinimumSize(new java.awt.Dimension(300, 20));
        Labels.setPreferredSize(new java.awt.Dimension(300, 20));
        Labels.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                LabelsFocusLost(evt);
            }
        });
        Labels.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LabelsActionPerformed(evt);
            }
        });
        jToolBar2.add(Labels);

        progressLabel.setFont(new java.awt.Font("Helvetica", 1, 13)); // NOI18N
        progressLabel.setText("Progress ");
        jToolBar2.add(progressLabel);
        jToolBar2.add(filler1);
        jToolBar2.add(filler2);

        MaskProgress.setMaximumSize(new java.awt.Dimension(180, 20));
        MaskProgress.setMinimumSize(new java.awt.Dimension(180, 20));
        MaskProgress.setPreferredSize(new java.awt.Dimension(180, 20));
        jToolBar2.add(MaskProgress);

        NorthPanel.add(jToolBar2);

        getContentPane().add(NorthPanel, java.awt.BorderLayout.NORTH);

        PlotPanel.setBackground(new java.awt.Color(255, 255, 255));
        PlotPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        PlotPanel.setMaximumSize(new java.awt.Dimension(610, 150));
        PlotPanel.setMinimumSize(new java.awt.Dimension(610, 150));
        PlotPanel.setPreferredSize(new java.awt.Dimension(610, 150));
        PlotPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                PlotPanelComponentResized(evt);
            }
        });
        getContentPane().add(PlotPanel, java.awt.BorderLayout.CENTER);
        PlotPanel.getAccessibleContext().setAccessibleName("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void plotTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plotTypeActionPerformed
       // 
       // 
            multipleFeatures.removeAll();
            multipleFeatures.validate();
            multipleFeatures.repaint();
        
        if(plotType.getSelectedIndex() == 1){    
            multipleSelections = new boolean[gatesLabels.size()];
            plotFeature.setEnabled(false);
            //System.out.println("Number of gate labels: " + gatesLabels.size());
            if(!gatesLabels.isEmpty()){
                multipleFeatures.removeAll();
                multipleFeatures.add(makeCheckPanel(gatesLabels));
                multipleFeatures.validate();
                PlotPanel.removeAll();
                PlotPanel.add(multipleFeatures);
                PlotPanel.repaint();
                multipleFeatures.repaint();
            }
        } 
        if(plotType.getSelectedIndex() == 0){
            multipleSelections = new boolean[valuesFeature.size()];
            plotFeature.setEnabled(true);
            //System.out.println("Number of feature labels: " + valuesFeature.size());
            plotFeature.setModel(new DefaultComboBoxModel(descriptions.toArray()));
            plotFeature.setSelectedIndex(0);
            plotFeature.validate();
        }
    }//GEN-LAST:event_plotTypeActionPerformed

    private void PlotPanelComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_PlotPanelComponentResized
        // TODO add your handling code here:
    }//GEN-LAST:event_PlotPanelComponentResized

    
    private void SavePlotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SavePlotActionPerformed
        

        notifyMakeObjectMasksListeners();
        


    }//GEN-LAST:event_SavePlotActionPerformed

    private void formComponentMoved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentMoved

    }//GEN-LAST:event_formComponentMoved

    private void plotFeatureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plotFeatureActionPerformed

        //get range of column
        //if < 50
        
            multipleFeatures.removeAll();
            multipleFeatures.validate();
            multipleFeatures.repaint();
        
        JComboBox menu = (JComboBox) evt.getSource();
        
        String columnHeader = descriptions.get(menu.getSelectedIndex());

        validateFeature(columnHeader);
    }//GEN-LAST:event_plotFeatureActionPerformed

    private void LabelsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_LabelsFocusLost
    validateLabels();
    }//GEN-LAST:event_LabelsFocusLost

    private void LabelsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LabelsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_LabelsActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField Labels;
    private javax.swing.JProgressBar MaskProgress;
    private javax.swing.JPanel NorthPanel;
    private javax.swing.JPanel PlotPanel;
    private javax.swing.JButton SavePlot;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.Box.Filler filler7;
    private javax.swing.Box.Filler filler8;
    private javax.swing.Box.Filler filler9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JComboBox<String> plotFeature;
    private javax.swing.JComboBox<String> plotType;
    private javax.swing.JLabel progressLabel;
    // End of variables declaration//GEN-END:variables

    
    public double getMaximum(ArrayList<Double> column){
        
        Double num = 0.0;
        
        for(int i = 0; i < column.size();i++){            
            Double test = column.get(i);       
            if(test.intValue() > num.intValue()){
                num = test;
            }
        }
        return num;   
    }
    
    
    
    @Override
    public void updateDescriptions(ArrayList<String> descriptions, ArrayList<String> descriptionLabel) {
        //System.out.println("PROFILING: updating descriptions");

        this.descriptions.clear();
        this.descriptionsLabels.clear();

        this.descriptions.addAll(descriptions);
        this.descriptionsLabels.addAll(descriptionLabel);

        int ysel = plotFeature.getSelectedIndex();
        plotFeature.setModel(new DefaultComboBoxModel(this.descriptions.toArray()));
        ComboboxToolTipRenderer renderer = new ComboboxToolTipRenderer();
        renderer.setTooltips(descriptionsLabels);
        plotFeature.setRenderer(renderer);
        plotFeature.setSelectedIndex(ysel);
        this.plotFeature.validate();
        this.plotFeature.repaint();
        pack();
    }

    @Override
    public void itemStateChanged(ItemEvent e) { 
        
        ArrayList<String> text = new ArrayList<String>();
        
        if(plotType.getSelectedIndex()==0){
            text = valuesFeature;
        } else {
            text = gatesLabels;
        }        
            
         String str = ((JCheckBox)(e.getSource())).getText();
         
         //System.out.println("Added feature: " + str);
         
         if(((JCheckBox)(e.getSource())).isSelected()){
         multipleSelections[text.indexOf(str)] = true ;
         } else {
         multipleSelections[text.indexOf(str)] = false ;  
         }
         
         //System.out.println("Boolean: at feature: " + multipleSelections);
         
         //validateLabels();
    }
    
    private void validateLabels(){
        
//       if(!Labels.getText().isEmpty()){
//        int labelCount = checkedLabels();
//        ArrayList<String> labels = delimitedValues(',');
//          Labels.setBackground(Color.WHITE);
//           SavePlot.setEnabled(true);
//        if(labels.size() > labelCount){
//            Labels.setToolTipText("Too many labels.");
//            Labels.setBackground(Color.RED);
//            SavePlot.setEnabled(false);
//        }
//        else if(labels.size() < labelCount){
//            Labels.setToolTipText("Too few labels.");
//            Labels.setBackground(Color.RED);
//            SavePlot.setEnabled(false);
//        }
//        else if(labels.size() == labelCount){
//            Labels.setToolTipText("Labels look good.");
//            Labels.setBackground(Color.WHITE);
//            SavePlot.setEnabled(true);
//        }
//       } 
    }
    
    private int checkedLabels(){
        int count = 0;
        for(int i = 0; i < multipleSelections.length; i++){
            if(multipleSelections[i]){count++;}
        }
        return count;
    }
    
    private ArrayList<String> delimitedValues(char delimitedBy){
        
        char[] delimitedString = Labels.getText().toCharArray();
        ArrayList<String> labels = new ArrayList();
        
        
        for(int i = 0; i < delimitedString.length; i++){
            char[] textblock = new char[Labels.getText().length()];
                while(i < delimitedString.length){
                    if(delimitedString[i] != delimitedBy){
                        textblock[i] = delimitedString[i]; 
                        
                    } else if(delimitedString[i] == delimitedBy){
                        String textblockString = new String(textblock);
                        labels.add(textblockString.trim());  
                    }
                    i++;
                }
        }
        return labels;
    }
    
    private void validateFeature(String columnHeader){
        ArrayList<ArrayList<Double>> result = H2DatabaseEngine.getColumn(
                   vtea._vtea.H2_MEASUREMENTS_TABLE + "_" + keySQLSafe, 
                   columnHeader);
           
           double max = XYExplorationPanel.getMaximumOfData(result, 0);

           //System.out.println("Total values found:" + result.size() + ", max value: " + max);

        if(max <= 50){  
            valuesFeature = new ArrayList();
            valuesFeatureDouble = new ArrayList();
            double i = 0;
            while(i <= max){
                valuesFeature.add(Double.toString(i));
                valuesFeatureDouble.add(i);
                i++;
            }
            multipleFeatures.add(makeCheckPanel(valuesFeature));
            multipleFeatures.validate();
            PlotPanel.add(multipleFeatures);
            multipleFeatures.repaint();
           }
    }
    
  
    public void notifyMakeObjectMasksListeners() {

        for (MakeObjectMasksListener listener : makeobjectmaskslisteners) {
            if(plotType.getSelectedItem().equals("Gate")){
                listener.onMakeObjectMasksFromGate(multipleSelections,delimitedValues(','));
            } else if(plotType.getSelectedItem().equals("Feature")){
                listener.onMakeObjectMasksFromFeature(multipleSelections,
                        plotFeature.getSelectedItem().toString(),  
                        valuesFeatureDouble, delimitedValues(','));
            }
        }
    }

    public void addMakeObjectMasksListener(MakeObjectMasksListener listener) {
        makeobjectmaskslisteners.add(listener);
    }
}
