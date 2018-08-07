/*
 * Copyright (C) 2018 SciJava
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.apache.commons.lang3.ArrayUtils;
import static vtea._vtea.FEATUREMAP;
import vtea.featureprocessing.AbstractFeatureProcessing;
/**
 * Setup Window for the blocks in Feature Frame. Allows selection of specific
 * feature types and specific parameters.
 * @author drewmcnutt
 */
public class MicroBlockFeatureSetup extends MicroBlockSetup implements ActionListener{
    ArrayList availabledata = new ArrayList();
    String[] FEATUREOPTIONS = vtea._vtea.FEATUREOPTIONS;
    String[] FEATUREGROUPS = vtea._vtea.FEATURETYPE;
    ArrayList CLUSTER = new ArrayList();
    ArrayList REDUCTION = new ArrayList();
    ArrayList OTHER = new ArrayList();
    JComboBox jComboBoxData = new JComboBox();
    int nvol;
    JPanel  dataPanel;
    JScrollPane dataScroll;
    JCheckBox all;
    JCheckBox normalize;
    ArrayList FeatureComponents = new ArrayList();

    /**
     * Constructor.
     * Sets up the MicroBlockFeatureSetup GUI and initializes all of the proper 
     * variables to allow the proper functioning of the window
     * @param step value of what step it is in the list
     * @param AvailableData contains the names of all of the computed features
     * @param nvol the total number of volumes segmented from the image
     */
    public MicroBlockFeatureSetup(int step, ArrayList AvailableData, int nvol){
        super(step);
        this.availabledata = AvailableData;
        this.nvol = nvol;
        this.setLocation(400, 0);
        this.setResizable(false);
        
        TitleText.setText("Feature_" + step);
        TitleText.setEditable(true);
        
        ChannelSelection.setText("Type of feature");
        ccbm = new DefaultComboBoxModel(FEATUREGROUPS);
        ChannelComboBox.setModel(ccbm);
        getContentPane().remove(comments);
        getContentPane().remove(this.methodSelection);
        buttonPanel.remove(PreviewButton);
        
        methodMorphology.setMaximumSize(new Dimension(359, 500));
        methodMorphology.setPreferredSize(new Dimension(359,150));
        this.MethodDetails.setPreferredSize(new Dimension(340,125));
        repaint();
        pack();
        
        setupGroups();
        
        setupDataBox();
        
        setSpecificComboBox(ChannelComboBox.getSelectedIndex());
        
    }
    /**
     * Updates the panel based on what combo box was altered
     * @param e 
     */
    @Override
    public void actionPerformed(ActionEvent e){
        if(e.getSource() == ChannelComboBox){
            int ind = ChannelComboBox.getSelectedIndex();
            setSpecificComboBox(ind);
            this.BlockSetupOK.setEnabled(false);
        }else if(e.getSource() == ProcessSelectComboBox){
            updateProtocolPanel(e);
        }
    }
    
    /**
     * Updates the label at the top of the Window
     */
    @Override
    protected void updateTitles(){
        TitleText.setText("Feature_" + step);
        this.repaint();
    }
    /**
     * Updates the proper panel based on which combo box was altered
     * @param evt 
     */
    @Override
    protected void updateProtocolPanel(ActionEvent evt) {
        if(evt.getSource() == ProcessSelectComboBox){
            makeProtocolPanel(ProcessSelectComboBox.getSelectedItem().toString());
        } else if(evt.getSource() == this.ChannelComboBox){
            setSpecificComboBox(ChannelComboBox.getSelectedIndex());
        }
    }
    /**
     * Changes the components in MethodDetails so that they pertain to the 
     * selected method
     * @param str passed to makeMethodComponentsArray to get the components for the method
     * @return JPanel
     */
    @Override
    protected JPanel makeProtocolPanel(String str){
        CurrentProcessItems.set(0, makeMethodComponentsArray(str, ProcessVariables));
        FeatureComponents = CurrentProcessItems.get(0);
        
        MethodDetails.setVisible(false);
        MethodDetails.removeAll();
        
        GridBagConstraints layoutConstraints = new GridBagConstraints();
        layoutConstraints.weightx = 1;
        layoutConstraints.weighty = 1;

        for(int i = 0; i < FeatureComponents.size(); i++){
            layoutConstraints.fill = 10 / (1 + 4 * (i % 2));
            layoutConstraints.gridx = i % 4;
            layoutConstraints.gridy = i / 4;

            MethodDetails.add((Component) FeatureComponents.get(i), layoutConstraints);
        }
        
        MethodDetails.repaint();
        pack();
        MethodDetails.setVisible(true);

        updateProcessList();

        
        return MethodDetails;
    }
    
    /**
     * 
     * @param index 
     */
    private void setSpecificComboBox(int index){
        
        switch (index){
            case 0:
                ProcessText.setText("Clustering Method");
                cbm = new DefaultComboBoxModel(CLUSTER.toArray());
                //ProcessSelectComboBox.setModel(new DefaultComboBoxModel(CLUSTER.toArray()));
                break;
            case 1:
                ProcessText.setText("Reduction Method");
                cbm = new DefaultComboBoxModel(REDUCTION.toArray());
                //ProcessSelectComboBox.setModel(new DefaultComboBoxModel(REDUCTION.toArray()));
                break;
            default:
                ProcessText.setText("Other Method");
                cbm = new DefaultComboBoxModel(OTHER.toArray());
                //ProcessSelectComboBox.setModel(new DefaultComboBoxModel(OTHER.toArray()));
        }
        
        ProcessSelectComboBox.setModel(cbm);
        ProcessSelectComboBox.setVisible(true);
        
        updateProcessList();
        
        this.revalidate();
        this.repaint();
        this.pack();
        if(ProcessSelectComboBox.getSelectedItem() != null)
            makeProtocolPanel((String)ProcessSelectComboBox.getSelectedItem());
        else
            makeProtocolPanel("");
    }
    
    /**
     * 
     * @param method
     * @param str
     * @return 
     */
    @Override
    protected ArrayList makeMethodComponentsArray(String method, String[][] str){
        Object iFeatp = new Object();
        
        try{
            Class<?> c;
            c = Class.forName(FEATUREMAP.get(method));
            Constructor<?> con;
            con = c.getDeclaredConstructor(int.class);
            iFeatp = con.newInstance(new Object[]{this.nvol});
            return ((AbstractFeatureProcessing)iFeatp).getOptions();        
        }catch(Exception e){
            System.out.println(e);
        }
        return new ArrayList();
    }
    
    /**
     * 
     */
    private void setupGroups(){
        for(String feature : FEATUREOPTIONS){
            try{
                Class<?> c;
                c = Class.forName(vtea._vtea.FEATUREMAP.get(feature));
                Constructor<?> con;
                con = c.getConstructor();
                Object temp = new Object();
                temp = con.newInstance();
                String type = ((AbstractFeatureProcessing)temp).getType();
                if(type.equals(FEATUREGROUPS[0])){
                    CLUSTER.add(feature);
                }else if(type.equals(FEATUREGROUPS[1])){
                    REDUCTION.add(feature);
                }else{
                    OTHER.add(feature);
                }
            }catch (NullPointerException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException ex) {
                Logger.getLogger(MicroBlockFeatureSetup.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        
        if(CLUSTER.isEmpty())
            this.FEATUREGROUPS = ArrayUtils.removeElement(this.FEATUREGROUPS,"Cluster");
        if(REDUCTION.isEmpty())
            this.FEATUREGROUPS = ArrayUtils.removeElement(this.FEATUREGROUPS, "Reduction");
        if(OTHER.isEmpty())
            this.FEATUREGROUPS = ArrayUtils.removeElement(this.FEATUREGROUPS, "Other");
        
        ccbm = new DefaultComboBoxModel(this.FEATUREGROUPS);
        ChannelComboBox.setModel(ccbm);
    }
    
    /**
     * Updates the protocol to fit that selected in the Setup
     */
    public void updateProtocol(){
        CurrentStepProtocol = CurrentProcessList;
        super.notifyMicroBlockSetupListeners(CurrentStepProtocol);
    }
    
    /**
     * 
     */
    private void setupDataBox(){
        Object[] features = (this.availabledata.toArray());
        //int colsize = features.length / 2;
        
        dataPanel = new JPanel();
        dataPanel.setPreferredSize(new Dimension(350,13 * features.length));
        dataPanel.setMinimumSize(new Dimension(350,700));
        //System.out.printf("Vgap = %d and HGap = %d", gl.getVgap(), gl.getHgap());
        dataPanel.setLayout(new java.awt.GridLayout(0, 2, 0, 1));
        dataPanel.setAlignmentY(JComponent.LEFT_ALIGNMENT);
        dataScroll = new JScrollPane();
        dataScroll.setViewportView(dataPanel);
        dataScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        methodBuild.setVisible(false);
        methodBuild.removeAll();
        
        all = new JCheckBox("Select All Data");
        all.addActionListener(new java.awt.event.ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent evt){
                checkAllBoxes();
            }
        });
        all.setSelected(true);
        
        normalize = new JCheckBox("Z-scale all data");
        normalize.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateProcessList();
            }
        });
        
        methodBuild.setLayout(new javax.swing.BoxLayout(methodBuild,BoxLayout.Y_AXIS));
        all.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        methodBuild.add(all);
        normalize.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        methodBuild.add(normalize);

        for(int i = 0; i < features.length; i++){
            JCheckBox cb = new JCheckBox(features[i].toString());
            cb.setSelected(true);
            cb.addItemListener(new ItemListener(){
                @Override
                public void itemStateChanged(ItemEvent evt) {
                        //System.out.println("Box for " + cb.getText());
                        updateProcessList();
                        checkSelected();
                }
            });
            //this.seldata.add(cb);
            dataPanel.add(cb);
        }
        methodBuild.add(dataScroll);
        methodBuild.setVisible(true);
        methodBuild.repaint();
        repaint();
        pack();
    }
    
    /**
     * 
     */
    private void checkAllBoxes(){
        boolean set = all.isSelected();
        for(Component c: dataPanel.getComponents()){
                ((JCheckBox)c).setSelected(set);
        }
        repaint();
    }
    
    /**
     * 
     * @return 
     */
    private ArrayList getSelectedData(){
        ArrayList selected = new ArrayList();
        for(Component c: dataPanel.getComponents()){
                selected.add(((JCheckBox)c).isSelected());
        }
        
        return selected;
    }
    
    private void checkSelected(){
        int true_count = 0;
        for(Component c: dataPanel.getComponents()){
            true_count += (((JCheckBox)c).isSelected()) ? 1 : 0;
        }
        if(true_count == 0)
            BlockSetupOK.setEnabled(false);
        else
            BlockSetupOK.setEnabled(true);
    }
    /**
     * 
     */
    private void updateProcessList(){
        CurrentProcessList.clear();
        CurrentProcessList.add(normalize.isSelected());
        CurrentProcessList.add(getSelectedData());
        CurrentProcessList.add(ProcessSelectComboBox.getSelectedItem());
        CurrentProcessList.add(ChannelComboBox.getSelectedItem());
        if(!FeatureComponents.isEmpty())
            CurrentProcessList.addAll(FeatureComponents);
    }
    
}
