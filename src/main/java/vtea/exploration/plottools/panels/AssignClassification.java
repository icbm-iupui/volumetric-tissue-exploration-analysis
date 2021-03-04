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
package vtea.exploration.plottools.panels;

import ij.ImagePlus;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Random;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import vtea.exploration.listeners.AddFeaturesListener;
import vtea.exploration.listeners.UpdateFeaturesListener;
import vtea.jdbc.H2DatabaseEngine;
import vteaobjects.MicroObject;

/**
 *
 * @author Seth
 */
public class AssignClassification {
    
    protected ArrayList<MicroObject> objects = new ArrayList();
    protected ArrayList<MicroObject> gatedobjects = new ArrayList();
    protected ArrayList<ArrayList<Number>> measurements = new ArrayList();
     protected ArrayList<String> descriptions = new ArrayList();
    HashMap<Double, Integer> result = new HashMap();
    String key;
    int nClasses = 1;
   
    ArrayList<AddFeaturesListener> addfeaturelisteners = new ArrayList<AddFeaturesListener>();
    
    public AssignClassification(ArrayList<MicroObject> gatedObjects, ArrayList<MicroObject> objects, String keySQLSafe, ArrayList<String> descriptions) {
        this.objects = objects;
        this.descriptions = descriptions;
        this.gatedobjects = gatedObjects;
        this.key = keySQLSafe; //SQLSafe required
    }
    
    public void process(){
        SetupAssignClassification smc = new SetupAssignClassification();
        
                int result = smc.showDialog();
                int[] settings = new int[2];

        if (result == JOptionPane.OK_OPTION) {
            settings = smc.getInformation();
            nClasses = settings[0];
            //nCells = settings[1];
        }
        
        if (hasColumn()){

        ArrayList<ArrayList<Number>> features
                    = H2DatabaseEngine.getColumn(vtea._vtea.H2_MEASUREMENTS_TABLE + "_"
                            + key, "Assigned");

            double maxClass = XYExplorationPanel.getMaximumOfData(features, 0);
            double minClass = XYExplorationPanel.getMinimumOfData(features, 0);
            
         //remove  column
        H2DatabaseEngine.dropColumn(vtea._vtea.H2_MEASUREMENTS_TABLE + "_"
                            + key, "Assigned");
        
       for(int i = 0; i < features.size(); i++){
           ArrayList<Number> c = features.get(i);
           this.result.put((double)i,c.get(0).intValue());
       }
        
        } 
        //cycle through arraylist and replace per position...
        

        for(int i = 0; i < gatedobjects.size(); i++){
            
            MicroObject obj = gatedobjects.get(i);
            this.result.put(obj.getSerialID(), nClasses);
            
        }

        addFeature();
 
    }
    
        protected void addFeature(){

        ArrayList<ArrayList<Number>> paddedTable = new ArrayList();
        ArrayList<Number> r = new ArrayList();
        
        for(int i = 0; i < objects.size(); i++){
            
            
            MicroObject m = objects.get(i);
       
           
           result.putIfAbsent(m.getSerialID(), -1);
           r.add(result.get(m.getSerialID()));
          
            }

        paddedTable.add(r);
        notifyAddFeatureListener("Assigned", paddedTable);  
    }
        
    private boolean hasColumn(){
        
        
        
        ListIterator<String> itr = descriptions.listIterator();
        
        while(itr.hasNext()){
            String str = itr.next();
            if(str.equalsIgnoreCase("Assigned")){return true;}
        }
        return false;
    }
    
    public void addFeatureListener(AddFeaturesListener listener) {
        addfeaturelisteners.add(listener);
    }

    public void notifyAddFeatureListener(String name,
            ArrayList<ArrayList<Number>> feature) {
        for (AddFeaturesListener listener : addfeaturelisteners) {
            listener.addFeatures(name, feature);
        }
    }
    
    class SetupAssignClassification extends JPanel implements ItemListener {

        JSpinner nClasses;
        JSpinner nCells;
        JCheckBox allCells;

        int nCellsValue = 100;
        ArrayList<JLabel> labels = new ArrayList<JLabel>();

        public SetupAssignClassification() {
            JLabel classLabel = new JLabel("Number of class: ");
            labels.add(classLabel);
            nClasses = new JSpinner(new SpinnerNumberModel(0, -1, 30, 1));


            ListIterator<JLabel> labiter = labels.listIterator();
            setupPanel(labiter);
        }

        private void setupPanel(ListIterator<JLabel> labiter) {
            JLabel curlabel;
            this.removeAll();
            this.setLayout(new GridBagLayout());

            curlabel = labiter.next();
            GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0.2, 1.0,
                    GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
            this.add(curlabel, gbc);
            gbc = new GridBagConstraints(1, 0, 1, 1, 1, 1.0, GridBagConstraints.EAST,
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
            this.add(nClasses, gbc);

        }

        public int showDialog() {
            return JOptionPane.showOptionDialog(null, this, "Set assigned class",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                    null, null);
        }

        public int[] getInformation() {
            int[] settings = new int[2];

            settings[0] = (int) this.nClasses.getValue();
            settings[1] = objects.size();
            
            return settings;
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (((JCheckBox) e.getItem()).isSelected()) {
                nCellsValue = (int) nCells.getValue();
                this.nCells.setEnabled(false);
            } else {
                nCells = new JSpinner(new SpinnerNumberModel(nCellsValue, 20, 1000, 20));
                this.nCells.setEnabled(true);
            }
        }

    }
}


