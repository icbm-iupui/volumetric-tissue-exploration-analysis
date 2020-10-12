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

import ij.IJ;
import java.awt.Component;
import java.awt.Dimension;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.jfree.chart.renderer.LookupPaintScale;
import vtea.exploration.listeners.AxesChangeListener;
import vtea.exploration.listeners.PlotAxesPreviewButtonListener;
import vtea.exploration.plottools.panels.ExplorationCenter;

/**
 *
 * @author sukhoc
 */
public class PlotAxesManager implements AxesChangeListener{
    
    ExplorationCenter ec;
    String keySQLSafe = "";
    Connection connection;
    
    PlotAxesSetup AxesSetup;// = new PlotAxesSetup();
    ArrayList<PlotAxesPreviewButtonListener> PlotAxesPreviewButtonListeners = new ArrayList();
    
    public PlotAxesManager(String key, Connection connection, String title,
            HashMap<Integer, String> hm){
        
        this.connection = connection;
        keySQLSafe = key.replace("-", "_");
        AxesSetup = new PlotAxesSetup();
        AxesSetup.setDescriptor(title);
        AxesSetup.shareConnection(connection, keySQLSafe, hm);
        
    }
    
    public void createAxesSettingsDialog(ArrayList<Component> settingsContent, int xPos, int yPos){
        
        AxesSetup.setLocation(xPos, yPos);
        AxesSetup.setVisible(true); 
        AxesSetup.setContent(settingsContent);
        
        ArrayList<Component> al = new ArrayList<Component>();
        
        al.add(new JLabel("Graph LUT:"));
        JButton editCustomLut = new JButton();
        editCustomLut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit-4_small.png")));
        al.add(editCustomLut);
        al.add(new JComboBox(vtea._vtea.LUTOPTIONS));

        AxesSetup.setLUT(al);
        AxesSetup.addAxesChangeListener(this);
        
    }
    
    public LookupPaintScale getLookupPaintScale(int l){
        LookupPaintScale lps = AxesSetup.getPaintScale(l);
        
        return lps;
    }
    
    public void addPlotAxesPreviewButtonListener(PlotAxesPreviewButtonListener listener){
        this.PlotAxesPreviewButtonListeners.add(listener);
    }
    
    public void notifyPlotAxesPreviewBtnListeners(ArrayList limits, 
            boolean xLinear, boolean yLinear, int lutTableSelectedIndex) {
        for (PlotAxesPreviewButtonListener listener : PlotAxesPreviewButtonListeners) {
            
                  new Thread(() -> {
            try {
                  listener.setAxesTo(limits, xLinear, yLinear, lutTableSelectedIndex);
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getLocalizedMessage());
            }
        }).start();
            
          
        }
    }

    @Override
    public void onAxesSetting(ArrayList Content, ArrayList LUT) {
        
        
        
        ArrayList<Double> limits = new ArrayList();

        limits.add(Double.valueOf(((JTextField) (Content.get(1))).getText()));
        limits.add(Double.valueOf(((JTextField) (Content.get(3))).getText()));
        limits.add(Double.valueOf(((JTextField) (Content.get(6))).getText()));
        limits.add(Double.valueOf(((JTextField) (Content.get(8))).getText()));

        boolean xLinear = true;
        boolean yLinear = true;

        if (((JComboBox) Content.get(4)).getSelectedIndex() == 1) {
            xLinear = false;
        }
        if (((JComboBox) Content.get(9)).getSelectedIndex() == 1) {
            yLinear = false;
        }
        
        JComboBox lutTable = (JComboBox)LUT.get(2);

        notifyPlotAxesPreviewBtnListeners(limits, xLinear, yLinear, lutTable.getSelectedIndex());

    }
    
    public void shareExplorerLutSelectedIndex(int explorerLutSelectedIndex){
        AxesSetup.shareExplorerLutSelectedIndex(explorerLutSelectedIndex);
    }
    
    public void setAxesSetupAxisLimits(ArrayList<Component> settingsContent){
        AxesSetup.setContent(settingsContent);
    }
    
    public void updateFeatureSpace(HashMap<Integer, String> hm){
        AxesSetup.notifyAddFeatures(hm);
    }
    
    public void updateMenuPosition(int xPos, int yPos){  
        AxesSetup.setLocation(xPos, yPos);
        AxesSetup.pack();
    }
    
    public void close(){
        AxesSetup.setVisible(false);
    }
    
}
