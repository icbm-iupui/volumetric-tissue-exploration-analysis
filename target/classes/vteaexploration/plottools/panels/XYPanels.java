/* 
 * Copyright (C) 2016 Indiana University
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
package vteaexploration.plottools.panels;

import vteaexploration.plotgatetools.listeners.ChangePlotAxesListener;
import vteaobjects.layercake.microVolume;
import vtea._vtea;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.ListIterator;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIDefaults;

/**
 *
 * @author vinfrais
 */
public class XYPanels extends DefaultPlotPanels implements PlotAxesPanels {

    
    
    JTextField X_text = new JTextField("x_axis");
    JTextField Y_text = new JTextField("y_axis"){
        @Override
        public void paintComponent(Graphics grphcs) {       
        
        Graphics2D g2 = (Graphics2D) grphcs;
        g2.rotate(-Math.PI/2);
        g2.drawString(this.getText(),0,0);
        super.paintComponent(grphcs);
        }};
    JTextField X_units = new JTextField("units");
    JTextField Y_units = new JTextField("units"){
        @Override
        public void paintComponent(Graphics grphcs) {       
        
        Graphics2D g2 = (Graphics2D) grphcs;
        g2.rotate(-Math.PI/2);
        g2.drawString(this.getText(),0,0);
        super.paintComponent(grphcs);
        }};
   

    JComboBox X_channels;
    JComboBox X_values;
    JComboBox Y_values;
    JComboBox Z_values;
    


    ArrayList<ChangePlotAxesListener> listeners = new ArrayList<ChangePlotAxesListener>();

    public XYPanels(ArrayList AvailableData) {
        super();
        setAxesValues();
        HeaderPanel.setPreferredSize(new Dimension(512,50));
        RightPanel.setPreferredSize(new Dimension(512,50));
        RightPanel.setLayout(new FlowLayout());
        
        makeFooterPanel(AvailableData);
        makeLeftPanel(AvailableData);
    }

    private void makeFooterPanel(ArrayList AvailableData) {
        


        X_values = new JComboBox(AvailableData.toArray());
        //Y_values = new JComboBox(AvailableData.toArray());

        X_values.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                notifyChangePlotAxesListeners(X_values.getSelectedIndex(), Y_values.getSelectedIndex(), 0);
            }
        });

//        Y_values.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                notifyChangePlotAxesListeners(X_values.getSelectedIndex(), Y_values.getSelectedIndex());
//            }
//        });

        UIDefaults defaults = javax.swing.UIManager.getDefaults();

        X_units.setEditable(false);
        //Y_units.setEditable(false);
        X_units.setFocusable(false);
        //Y_units.setFocusable(false);
        X_text.setBackground(defaults.getColor("Panel.background"));
//        Y_text.setBackground(defaults.getColor("Panel.background"));
        X_units.setBackground(defaults.getColor("Panel.background"));
//        Y_units.setBackground(defaults.getColor("Panel.background"));
        X_text.setBackground(_vtea.BACKGROUND);
        //Y_text.setBackground(VTEAService.BACKGROUND);
        X_units.setBackground(_vtea.BACKGROUND);
        //Y_units.setBackground(VTEAService.BACKGROUND);
        X_units.setBorder(null);
        //Y_units.setBorder(null);
        X_text.setBorder(null);
        //Y_text.setBorder(null);

        FooterPanel.removeAll();

        javax.swing.GroupLayout BottomLayout = new javax.swing.GroupLayout(FooterPanel);
        FooterPanel.setLayout(BottomLayout);
        BottomLayout.setHorizontalGroup(
                BottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(BottomLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(X_text)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(X_values, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(X_units)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 80, Short.MAX_VALUE)
//                        .addComponent(Y_text)
//                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
//                        .addComponent(Y_values, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
//                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
//                        .addComponent(Y_units)
                       .addContainerGap())
        );
        BottomLayout.setVerticalGroup(
                BottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, BottomLayout.createSequentialGroup()
                        .addContainerGap(7, Short.MAX_VALUE)
                        .addGroup(BottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(X_values, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(X_text)
                                //.addComponent(Y_values, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(X_units))
                                //.addComponent(Y_units)
                                //.addComponent(Y_text))
                        .addContainerGap())
        );
    }
    
    private void makeLeftPanel(ArrayList AvailableData) {
        


        //X_values = new JComboBox(AvailableData.toArray());
        Y_values = new JComboBox(AvailableData.toArray()){
        @Override
        public void paintComponent(Graphics grphcs) {       
        
        Graphics2D g2 = (Graphics2D) grphcs;
        g2.rotate(-Math.PI/2);
        super.paintComponent(grphcs);
        }};
        
       

//        X_values.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                notifyChangePlotAxesListeners(X_values.getSelectedIndex(), Y_values.getSelectedIndex());
//            }
//        });

        Y_values.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                notifyChangePlotAxesListeners(X_values.getSelectedIndex(), Y_values.getSelectedIndex(), 0);
            }
        });

        UIDefaults defaults = javax.swing.UIManager.getDefaults();

//        X_units.setEditable(false);
        Y_units.setEditable(false);
//        X_units.setFocusable(false);
        Y_units.setFocusable(false);
//        X_text.setBackground(defaults.getColor("Panel.background"));
       Y_text.setBackground(defaults.getColor("Panel.background"));
//        X_units.setBackground(defaults.getColor("Panel.background"));
       Y_units.setBackground(defaults.getColor("Panel.background"));
//        X_text.setBackground(VTEAService.BACKGROUND);
        Y_text.setBackground(_vtea.BACKGROUND);
//        X_units.setBackground(VTEAService.BACKGROUND);
        Y_units.setBackground(_vtea.BACKGROUND);
//        X_units.setBorder(null);
        Y_units.setBorder(null);
//        X_text.setBorder(null);
        Y_text.setBorder(null);

        LeftPanel.removeAll();
        LeftPanel.setPreferredSize(new Dimension(50, 600));

        FlowLayout LeftLayout = new java.awt.FlowLayout(FlowLayout.CENTER, 5, 5);
        LeftPanel.setLayout(LeftLayout);
        //LeftPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        
        LeftPanel.add(Y_text);
        LeftPanel.add(Y_values);
        LeftPanel.add(Y_units);
        
       
    }

    private void setAxesValues() {

        X_values = new JComboBox(microVolume.Analytics);
        Y_values = new JComboBox(microVolume.Analytics);
    }

    public void addMeasures(ArrayList<String> als) {

        ListIterator<String> itr = als.listIterator();
        while (itr.hasNext()) {
            X_values.addItem((String) itr.next());
        }
    }

    public void addChangePlotAxesListener(ChangePlotAxesListener listener) {
        listeners.add(listener);
    }

    private void notifyChangePlotAxesListeners(int x, int y, int z) {
        for (ChangePlotAxesListener listener : listeners) {

            //listener.onChangeAxes(x, y, z, 0);

        }
    }

    @Override
    public JPanel getBorderPanelHeader() {
        return new JPanel();
    }

    @Override
    public JPanel getBorderPanelFooter() {

        return FooterPanel;
    }

    @Override
    public JPanel getBorderPanelLeft() {
        return LeftPanel;
    }

    @Override
    public JPanel getBorderPanelRight() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
   

}
