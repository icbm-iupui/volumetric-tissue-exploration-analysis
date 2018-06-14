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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.apache.commons.lang3.ArrayUtils;
import static vtea._vtea.FEATUREMAP;
import vtea.featureprocessing.AbstractFeatureProcessing;
/**
 *
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
    ArrayList FeatureComponents = new ArrayList();
    //ArrayList seldata = new ArrayList();
    
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
        comments.removeAll();
        PreviewButton.setVisible(false);

//        methodBuild.setMaximumSize(new java.awt.Dimension(400, 300));
//        methodBuild.setMinimumSize(new java.awt.Dimension(359, 300));
//        methodBuild.setPreferredSize(new java.awt.Dimension(359, 300));
//        GridBagConstraints gbc = new java.awt.GridBagConstraints();
//        gbc.gridx = 0;
//        gbc.gridy = 13;
//        getContentPane().add(methodBuild, gbc);
        
//        gbc = new GridBagConstraints();
//        gbc.gridx = 0;
//        gbc.gridy = 6;
//        getContentPane().remove(methodMorphology);
//        getContentPane().add(methodMorphology, gbc);
        
        pack();
        
        setupGroups();
        
        setupDataBox();
        
        setSpecificComboBox(ChannelComboBox.getSelectedIndex());
        
    }
    
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
    @Override
    protected void updateTitles(){
        TitleText.setText("Feature_" + step);
        this.repaint();
    }
    
    @Override
    protected void updateProtocolPanel(ActionEvent evt) {
        if(evt.getSource() == ProcessSelectComboBox){
            makeProtocolPanel(FEATUREOPTIONS[ProcessSelectComboBox.getSelectedIndex()]);
        } else if(evt.getSource() == this.ChannelComboBox){
            setSpecificComboBox(ChannelComboBox.getSelectedIndex());
        }
    }
    
    @Override
    protected JPanel makeProtocolPanel(String str){
        
        
        CurrentProcessItems.set(0, makeMethodComponentsArray(str, ProcessVariables));
        FeatureComponents = CurrentProcessItems.get(0);
        
        MethodDetails.setVisible(false);
        MethodDetails.removeAll();
        
        GridBagConstraints layoutConstraints = new GridBagConstraints();
        if (FeatureComponents.size() > 0) {
            layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.gridx = 0;
            layoutConstraints.gridy = 0;
            layoutConstraints.weightx = 1;
            layoutConstraints.weighty = 1;
            MethodDetails.add((Component) FeatureComponents.get(0), layoutConstraints);
        }

        if (FeatureComponents.size() > 1) {
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 1;
            layoutConstraints.gridy = 0;
            MethodDetails.add((Component) FeatureComponents.get(1), layoutConstraints);
        }

        if (FeatureComponents.size() > 2) {
            layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.gridx = 2;
            layoutConstraints.gridy = 0;
            MethodDetails.add((Component) FeatureComponents.get(2), layoutConstraints);
        }
        if (FeatureComponents.size() > 3) {
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 3;
            layoutConstraints.gridy = 0;
            MethodDetails.add((Component) FeatureComponents.get(3), layoutConstraints);
        }
        if (FeatureComponents.size() > 4) {
            layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.gridx = 0;
            layoutConstraints.gridy = 1;
            MethodDetails.add((Component) FeatureComponents.get(4), layoutConstraints);
        }
        if (FeatureComponents.size() > 5) {
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 1;
            layoutConstraints.gridy = 1;
            MethodDetails.add((Component) FeatureComponents.get(5), layoutConstraints);
        }
        if (FeatureComponents.size() > 6) {
            layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.gridx = 2;
            layoutConstraints.gridy = 1;
            MethodDetails.add((Component) FeatureComponents.get(6), layoutConstraints);
        }
        if (FeatureComponents.size() > 7) {
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 3;
            layoutConstraints.gridy = 1;
            MethodDetails.add((Component) FeatureComponents.get(7), layoutConstraints);
        }
        
        
        MethodDetails.setVisible(true);
        repaint();
        pack();
        
        updateProcessList();
        //CurrentProcessList.addAll(FeatureComponents);
        
        return MethodDetails;
    }
    
    public void setupPanels(){
        //jComboBoxData.setModel(new DefaultComboBoxModel(this.availabledata.toArray()));
        //add code for setting up parameters panel and data selection panel
    }
    
    public void setSpecificComboBox(int index){
        
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
    public void updateProtocol(){
        CurrentStepProtocol = CurrentProcessList;
        super.notifyMicroBlockSetupListeners(CurrentStepProtocol);
    }
    
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
        
        methodBuild.setLayout(new javax.swing.BoxLayout(methodBuild,BoxLayout.Y_AXIS));
        all.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        methodBuild.add(all);

        for(int i = 0; i < features.length; i++){
            JCheckBox cb = new JCheckBox(features[i].toString());
            cb.addItemListener(new ItemListener(){
                @Override
                public void itemStateChanged(ItemEvent evt) {
                        //System.out.println("Box for " + cb.getText());
                        updateProcessList();
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
    
    public void checkAllBoxes(){
        boolean set = all.isSelected();
        for(Component c: dataPanel.getComponents()){
                ((JCheckBox)c).setSelected(set);
        }
        repaint();
    }
    
    public ArrayList getSelectedData(){
        ArrayList selected = new ArrayList();
        for(Component c: dataPanel.getComponents()){
                selected.add(((JCheckBox)c).isSelected());
        }
//        for(Object cb: seldata){
//            selected.add(((JCheckBox)cb).isSelected());
//        }
        return selected;
    }
    
    private void updateProcessList(){
        CurrentProcessList.clear();
        CurrentProcessList.add(getSelectedData());
        CurrentProcessList.add(ProcessSelectComboBox.getSelectedItem());
        CurrentProcessList.add(ChannelComboBox.getSelectedItem());
        if(!FeatureComponents.isEmpty())
            CurrentProcessList.addAll(FeatureComponents);
    }
}
