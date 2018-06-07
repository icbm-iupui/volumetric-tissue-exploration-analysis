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

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
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
    
    
    public MicroBlockFeatureSetup(int step, ArrayList AvailableData){
        super(step);
        this.availabledata = AvailableData;
        this.setLocation(400, 0);
        this.setResizable(false);
        
        TitleText.setText("Feature_" + step);
        TitleText.setEditable(true);
        
        ccbm = new DefaultComboBoxModel(FEATUREGROUPS);
        ChannelSelection.setText("Type of feature");
        ProcessText.setText("");
        ProcessSelectComboBox.setVisible(false);
        comments.removeAll();
        methodMorphology.removeAll();
        PreviewButton.setVisible(false);
                
        methodBuild.setMaximumSize(new java.awt.Dimension(400, 300));
        methodBuild.setMinimumSize(new java.awt.Dimension(359, 300));
        methodBuild.setPreferredSize(new java.awt.Dimension(359, 300));
        GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 13;
        getContentPane().remove(methodBuild);
        getContentPane().add(methodBuild, gbc);
        
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        getContentPane().remove(methodMorphology);
        getContentPane().add(methodMorphology, gbc);
        
        ChannelComboBox.setModel(ccbm);
        
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
            //makeProtocolPanel(FEATUREOPTIONS[ProcessSelectComboBox.getSelectedIndex()]);
        } else if(evt.getSource() == this.ChannelComboBox){
            setSpecificComboBox(ChannelComboBox.getSelectedIndex());
        }
    }
    
    @Override
    protected JPanel makeProtocolPanel(String str){
        return new JPanel();
    }
    
    public void setupPanels(){
        //jComboBoxData.setModel(new DefaultComboBoxModel(this.availabledata.toArray()));
        //add code for setting up parameters panel and data selection panel
    }
    
    public void setSpecificComboBox(int index){
        CurrentProcessList.clear();
        CurrentProcessList.add(ChannelComboBox.getSelectedItem());
        
        ProcessSelectComboBox.removeAllItems();
        cbm.removeAllElements();
        
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
        
        CurrentProcessList.add(ProcessSelectComboBox.getSelectedItem());
        
        this.revalidate();
        this.repaint();
        this.pack();
    }
    
    public void updateProtocol(){
        CurrentStepProtocol = CurrentProcessList;
        super.notifyMicroBlockSetupListeners(CurrentStepProtocol);
    }
}
