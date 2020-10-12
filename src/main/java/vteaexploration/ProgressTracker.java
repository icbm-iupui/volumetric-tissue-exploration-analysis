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

import ij.ImagePlus;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.ListIterator;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import vtea.processor.MeasurementProcessor;
import vtea.processor.SegmentationProcessor;
import vtea.protocol.UtilityMethods;
import static vteaexploration.MicroExplorer.YAXIS;

/**
 *
 * @author sethwinfree
 */
public class ProgressTracker extends JPanel implements PropertyChangeListener {
       
    JProgressBar jpb = new JProgressBar();
    JFrame jf = new JFrame();
    JLabel label = new JLabel();
    JLabel method = new JLabel();
    
    public ProgressTracker(){
        super();

        
    }
    

    
    public void createandshowGUI(String str, int x, int y){
        method.setText("Method: ");
        label.setText(str);
        jf.setTitle("Progress of " + str);
        setPreferredSize(new Dimension((int)(725*0.8), 60));
        jf.setPreferredSize(new Dimension((int)(725*0.8), 60));
        jf.setResizable(false);
        jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        
 
        jpb.setMaximum(100);
        jpb.setMinimum(0);
        jpb.setValue(1);
        

        
         GridBagConstraints layoutConstraints = new GridBagConstraints();
        layoutConstraints.weightx = 1;
        layoutConstraints.weighty = 1;
            layoutConstraints.fill = GridBagConstraints.BOTH ;
            layoutConstraints.gridx = 0;
            layoutConstraints.gridy = 0;
            layoutConstraints.anchor = GridBagConstraints.WEST;

            this.add((Component) method, layoutConstraints);
        
        layoutConstraints = new GridBagConstraints();
        layoutConstraints.weightx = 1;
        layoutConstraints.weighty = 1;
            layoutConstraints.fill = GridBagConstraints.BOTH ;
            layoutConstraints.gridx = 0;
            layoutConstraints.gridy = 1;
            layoutConstraints.anchor = GridBagConstraints.CENTER;

            this.add((Component) label, layoutConstraints);
            
                    layoutConstraints = new GridBagConstraints();
        layoutConstraints.weightx = 1;
        layoutConstraints.weighty = 1;
            layoutConstraints.fill = GridBagConstraints.BOTH ;
            layoutConstraints.gridx = 0;
            layoutConstraints.gridy = 1;
            layoutConstraints.anchor = GridBagConstraints.EAST;

            this.add((Component) jpb, layoutConstraints);
      
        
        JComponent newContentPane = this;
        newContentPane.setOpaque(true); //content panes must be opaque
        jf.setContentPane(newContentPane);

        jf.setLocation(x, y+96);
        jf.pack();
        jf.repaint();
        jf.setVisible(true);
    }
    
    public void setPercentProgress(int i){
        jpb.setValue(i);
    }
    
    public void setIndeterminant(boolean b){
        jpb.setIndeterminate(b);
    }
    
    public void setTextProgress(String str){
        label.setText(str);
    }
    
    public void setTextMethod(String str){
        label.setText(str);

    }
    
    public void setVisible(boolean b){
        jf.setVisible(b);
    }
    
   public void setPosition(int x, int y){
        jf.setLocation(x, y);
    }

  

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
           if (evt.getPropertyName().equals("progress")) {
            int progress = (Integer) evt.getNewValue();
            String step = (String) evt.getOldValue();
            jpb.setIndeterminate(false);
            jpb.setValue(progress);
            label.setText(step + "... " + String.format(
                    "Completed %d%%...\n", progress));
        }

        if (evt.getPropertyName().equals("reset")) {    
            jpb.setValue(0);
            label.setText(String.format(
                    "", 0));
        }
        
        
        if (evt.getPropertyName().equals("comment")) {
            label.setText((String) evt.getNewValue());
        }
        
        if (evt.getPropertyName().equals("indeterminant")) {
            jpb.setIndeterminate(true);
            label.setText((String) evt.getNewValue());
            
        }
        
           if (evt.getPropertyName().equals("method")) {
            method.setText((String) evt.getNewValue());
        } 
           
        if (evt.getPropertyName().equals("escape")
                && (Boolean) evt.getNewValue()) {
            label.setText("Processing complete...");            
        }
        if (evt.getPropertyName().equals("escape") && !(Boolean) evt.getNewValue()) {

            System.out.println("PROFILING: Error in processing, thread terminated early...");

        }
    }
}
 