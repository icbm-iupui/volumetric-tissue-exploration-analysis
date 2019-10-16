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
package vteaexploration;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.colorchooser.*;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import java.util.HashMap;
import vtea.exploration.listeners.CustomLutListener;

/**
 *
 * @author sukhoc
 */
public class LutCustomColorChooser extends JPanel implements ChangeListener {
    
    protected JColorChooser tcc;
    protected JLabel banner;
    JPanel comboBoxPanel;
    JComboBox customLutClusterComboBox;
    ArrayList clusterInfo;
    int currentCluster;
    String selectedCluster;
    Color selectedColor;
    HashMap<String, Color> MapCustomLut2Clusters = new HashMap<String, Color>();
    JPanel okButtonPanel;
    JButton okButton;
    ArrayList<CustomLutListener> CustomLutListeners = new ArrayList<CustomLutListener>();
 
    public LutCustomColorChooser(ArrayList populateCustomLutComboBox) {
        
        super(new BorderLayout());
        this.clusterInfo = populateCustomLutComboBox;
        this.currentCluster = 0;
        selectedCluster = (String)populateCustomLutComboBox.get(0);
        selectedColor = new Color(0x000000);
 
        //Set up the banner at the top of the window
        banner = new JLabel("Welcome to the Tutorial Zone!",
                            JLabel.CENTER);
        banner.setForeground(Color.yellow);
        banner.setBackground(Color.blue);
        banner.setOpaque(true);
        banner.setFont(new Font("SansSerif", Font.BOLD, 24));
        banner.setPreferredSize(new Dimension(100, 65));
 
//        JPanel bannerPanel = new JPanel(new BorderLayout());
//        bannerPanel.add(banner, BorderLayout.CENTER);
//        bannerPanel.setBorder(BorderFactory.createTitledBorder("Banner"));

        comboBoxPanel = new JPanel(new BorderLayout());
        
        customLutClusterComboBox = new JComboBox();
        customLutClusterComboBox.setModel(new DefaultComboBoxModel(this.clusterInfo.toArray()));
        customLutClusterComboBox.setSelectedIndex(0);
        customLutClusterComboBox.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxCustomLutActionPerformed(evt);
            }
        });
        comboBoxPanel.add(customLutClusterComboBox, BorderLayout.CENTER);
 
        //Set up color chooser for setting text color
        tcc = new JColorChooser(banner.getForeground());
        tcc.getSelectionModel().addChangeListener(this);
        //tcc.setBorder(BorderFactory.createTitledBorder(
        //"Choose Text Color"));
        
 
        
        tcc.removeChooserPanel(tcc.getChooserPanels()[1]);
        tcc.removeChooserPanel(tcc.getChooserPanels()[1]);
        tcc.removeChooserPanel(tcc.getChooserPanels()[2]);
        
        okButtonPanel = new JPanel(new BorderLayout());
        okButton = new JButton();
        okButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/dialog-apply.png"))); // NOI18N
        okButton.setToolTipText("Accept changes");
        okButton.setMaximumSize(vtea._vtea.SMALLBUTTONSIZE);
        okButton.setMinimumSize(vtea._vtea.SMALLBUTTONSIZE);
        okButton.setPreferredSize(vtea._vtea.SMALLBUTTONSIZE);
        okButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OkButtonActionPerformed(evt);
            }
        });
        okButtonPanel.add(okButton, BorderLayout.EAST);
 
        //add(bannerPanel, BorderLayout.CENTER);
        add(comboBoxPanel, BorderLayout.NORTH);
        add(tcc, BorderLayout.CENTER);
        add(okButtonPanel, BorderLayout.PAGE_END);
    }
 
    @Override
    public void stateChanged(ChangeEvent e) {
        Color newColor = tcc.getColor();
        banner.setForeground(newColor);
        
        selectedColor = newColor;
        selectedCluster = (String)customLutClusterComboBox.getSelectedItem();
        MapCustomLut2Clusters.put(selectedCluster, selectedColor);
    }
 
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("CustomLUTSelection");
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Create and set up the content pane.
        JComponent newContentPane = this;
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    
//    public void setComboBoxDetails(ArrayList populateCustomLutComboBox){
//        this.clusterInfo = populateCustomLutComboBox;
//    }
    
    private void jComboBoxCustomLutActionPerformed(java.awt.event.ActionEvent evt){
        
        this.currentCluster = customLutClusterComboBox.getSelectedIndex();
        customLutClusterComboBox.setSelectedIndex(this.currentCluster);
    }
    
//    public void getCustomLutColors(HashMap<String,Color> customLutColors){
//        customLutColors = this.MapCustomLut2Clusters;
//    }
    
    public void invokeCustomLUTWindow() {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
//        javax.swing.SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                createAndShowGUI();
//            }
//        });
        createAndShowGUI();
    }
    
    public void registerAxesSetup(CustomLutListener listener){
        this.CustomLutListeners.add(listener);
    }

    private void OkButtonActionPerformed(ActionEvent evt) {
        this.onCustomLutSelection(MapCustomLut2Clusters);
        this.setVisible(false);
    }

    public void onCustomLutSelection(HashMap<String, Color> customLutColors) {
        for (CustomLutListener listener : CustomLutListeners) {
            listener.onCustomLutSelection(customLutColors);
        }
       // this.CustomLutListeners.get(0).onCustomLutSelection(customLutColors);
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
