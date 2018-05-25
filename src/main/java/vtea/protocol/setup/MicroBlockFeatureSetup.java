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
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
/**
 *
 * @author drewmcnutt
 */
public class MicroBlockFeatureSetup extends MicroBlockSetup {
    ArrayList availabledata = new ArrayList();
    String FEATUREGROUPS[] = {"Cluster", "Dimensionality Reduction", "Other"};
    JComboBox jComboBoxData = new JComboBox();
    
    
    public MicroBlockFeatureSetup(int step, ArrayList AvailableData, List plotvalues){
        super(step);
        this.availabledata = AvailableData;
        this.setLocation(400, 0);
        this.setResizable(false);
        
        TitleText.setText("Feature_" + step);
        TitleText.setEditable(true);
        
        ccbm = new DefaultComboBoxModel(FEATUREGROUPS);
        ChannelSelection.setText("Type of feature");
        ProcessText.setText("");
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
        
    }
    @Override
    protected void updateTitles(){
        TitleText.setText("Feature_" + step);
        this.repaint();
    }
    
    @Override
    protected void updateProtocolPanel(java.awt.event.ActionEvent evt) {
        if(evt.getSource() == ChannelComboBox){
            setSpecificComboBox();
        }else{
            setupPanels();
        }
    }
    
    public void setupPanels(){
        //jComboBoxData.setModel(new DefaultComboBoxModel(this.availabledata.toArray()));
        //add code for setting up parameters panel and data selection panel
    }
    
    public void setSpecificComboBox(){
        
    }
}
