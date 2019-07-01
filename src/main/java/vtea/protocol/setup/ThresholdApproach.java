/*
 * Copyright (C) 2019 SciJava
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

import ij.*;
import ij.gui.*;
import ij.measure.*;
import ij.plugin.ChannelSplitter;
import ij.plugin.Thresholder;
import ij.plugin.filter.*;
import ij.plugin.frame.Recorder;
import ij.process.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import vtea.protocol.setup.MicroThresholdAdjuster;
import vtea.protocol.setup.AutoLocalThresholdAdjuster;

/**
 *
 * @author sukhoc
 */
public class ThresholdApproach implements ChangeListener, ItemListener{
    
    static final int MICRO_THRESHOLD_ADJUSTER=0, AUTOLOCAL_THRESHOLD_ADJUSTER=1;
    static final String[] thresholdAlgorithmOptions = {"Micro Threshold Adjuster","Auto-Local Threshold Adjuster"};
    static int defaultThreshAlgorithm = MICRO_THRESHOLD_ADJUSTER;
    static int currentThreshAlgorithm;
    JComboBox threshAlgorithmComboBox;
    ImageJ ij;
    ImagePlus impThreshold; 
    ImagePlus impBackup;
    JPanel panel;
    JPanel gui = new JPanel();
    
    ThresholdApproach instanceTA;
    MicroThresholdAdjuster mta;
    AutoLocalThresholdAdjuster alta;
    
    public ThresholdApproach(ImagePlus cimp) {
        
        if (cimp!=null){
            impThreshold = cimp;        
            impBackup = impThreshold.duplicate();

            ij = IJ.getInstance();
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            gui.setLayout(gridbag);
            gui.setPreferredSize(new Dimension(280,270));
            gui.setBackground(vtea._vtea.BACKGROUND);

            // Add combo-box for threshold algorithm choice for MicroThresholdAdjuster/AutoLocalThresholdAdjuster
            int y = 0;
            c.gridx = 0;
            c.gridy = y++;
            c.gridwidth = 1;
            c.insets = new Insets(0, 0, 0, 5); //top left bottom right
            c.anchor = GridBagConstraints.PAGE_START;
            c.fill = GridBagConstraints.NONE;
            gui.add(getThreshAlgChoiceComboBox(defaultThreshAlgorithm),c);

            // Load MicroThresholdAdjuster Settings by default
            mta = new MicroThresholdAdjuster(impThreshold);

            c.gridx = 0;
            c.gridy = y++;
            c.gridwidth = 1;
            c.insets = new Insets(0, 0, 0, 5); //top left bottom right
            c.anchor = GridBagConstraints.FIRST_LINE_START;
            c.fill = GridBagConstraints.NONE;
            gui.add(mta.getPanel(), c);
            gui.repaint();
        }
    }
    
    @Override
    public synchronized void itemStateChanged(ItemEvent e) {
        Object source = e.getSource();
        GridBagConstraints c = new GridBagConstraints();
        int y = 0;
 
        if (source==threshAlgorithmComboBox) {
            currentThreshAlgorithm = threshAlgorithmComboBox.getSelectedIndex();
            
            switch(currentThreshAlgorithm){
                case MICRO_THRESHOLD_ADJUSTER:
                    gui.remove(1);
                    gui.repaint();
                    
                    mta = new MicroThresholdAdjuster(impThreshold);
        
                    c.gridx = 0;
                    c.gridy = 1;
                    c.gridwidth = 1;
                    c.insets = new Insets(1, 0, 0, 5); //top left bottom right
                    c.anchor = GridBagConstraints.FIRST_LINE_START;
                    c.fill = GridBagConstraints.NONE;
                    gui.add(mta.getPanel(), c);
                    gui.repaint();
                    break;
                case AUTOLOCAL_THRESHOLD_ADJUSTER:
                    gui.remove(1);
                    gui.repaint();
                    
                    alta = new AutoLocalThresholdAdjuster(impThreshold);
        
                    c.gridx = 0;
                    c.gridy = 1;
                    c.gridwidth = 1;
                    c.insets = new Insets(1, 0, 0, 5); //top left bottom right
                    c.anchor = GridBagConstraints.FIRST_LINE_START;
                    c.fill = GridBagConstraints.NONE;
                    gui.add(alta.getPanel(), c);
                    gui.repaint();
                    break;
            }
        } else{
            
        }
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        Object source = e.getSource();
        
        if (source==threshAlgorithmComboBox) {
            currentThreshAlgorithm = (int)threshAlgorithmComboBox.getSelectedItem();
            
            switch(currentThreshAlgorithm){
                case MICRO_THRESHOLD_ADJUSTER:
                    mta = new MicroThresholdAdjuster(impThreshold);
                    this.gui.add(mta.getPanel());
                    break;
                case AUTOLOCAL_THRESHOLD_ADJUSTER:
                    alta = new AutoLocalThresholdAdjuster(impThreshold);
                    this.gui.add(alta.getPanel());
                    break;
            }
        } else{
            
        }
    }
    
    public JPanel getPanel(){
        return this.gui;
    }
    
    protected JPanel getThreshAlgChoiceComboBox(int currentThreshAlgorithm){
        // Add combo-box for threshold algorithm choice for MicroThresholdAdjuster/AutoLocalThresholdAdjuster
        JPanel panel = new JPanel();
        panel.setBackground(vtea._vtea.BACKGROUND);
        
        threshAlgorithmComboBox = new JComboBox();
        for (int i=0; i<thresholdAlgorithmOptions.length; i++)
            threshAlgorithmComboBox.addItem(thresholdAlgorithmOptions[i]);
        threshAlgorithmComboBox.setSelectedItem(currentThreshAlgorithm);
        threshAlgorithmComboBox.addItemListener(this);
        panel.add(threshAlgorithmComboBox);
        
        return panel;
    }
    
    public boolean processEntireVolume(JPanel visualizer, ImageStack stackResult){
        int count = visualizer.getComponentCount();
        Component c = visualizer.getComponent(count);
        
        if (c instanceof JComboBox){
            if((int)((JComboBox) c).getSelectedItem() == AUTOLOCAL_THRESHOLD_ADJUSTER){
                return true;
            }
            else{
                return false;
            }
        }
        return true;
    }
    

    
}
