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
package vtea.plot;

import java.util.ArrayList;
import java.util.ListIterator;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import vtea.exploration.listeners.AddFeaturesListener;
import vtea.gui.ComboboxToolTipRenderer;

/**
 *
 * @author sethwinfree
 */
public class PlotOutputFrame extends javax.swing.JFrame implements AddFeaturesListener {

    PlotOutput go;
    
    ArrayList measurements;
    ArrayList descriptions;
    String[] plotTypes = {"Violin Plot", "Heatmap"};
    ArrayList<String> descriptionsLabels = new ArrayList<String>();
    int position = 0;
    
    final static int DOWN_ARROW = 1;
    final static int UP_ARROW = 2;
    
    int featureCount = 0;
    String key = "";

    /**
     * Creates new form GraphOutputFrame
     */
    public PlotOutputFrame() {
        
    }
    
    public void process(String key, String title, ArrayList descriptions, ArrayList descriptionLabel) {
        
        this.key = key;
        this.setTitle(title);
        //this.measurements = (ArrayList) plotvalues;
        this.descriptions = descriptions;
        this.descriptionsLabels = descriptionLabel;
        initComponents();
        
        plotType.setModel(new DefaultComboBoxModel(vtea._vtea.PLOTMAKEROPTIONS));
        plotType.setSelectedItem("Select Method");
        plotFeature.setModel(new DefaultComboBoxModel(descriptions.toArray()));
        plotFeature.setSelectedIndex(0);
        groupType.setModel(new DefaultComboBoxModel(descriptions.toArray()));
        groupType.setSelectedIndex(0);
        
        ComboboxToolTipRenderer renderer = new ComboboxToolTipRenderer();
        renderer.setTooltips(descriptionsLabels);
        
        plotType.setRenderer(renderer);
        plotFeature.setRenderer(renderer);
        groupType.setRenderer(renderer);
        
        go = new PlotOutput();
        go.updatePlotOutput();
        if (go.getPlotCount() > 0) {
            setPlot();  
            this.Forward.setEnabled(true);
        }
        go.getPlot(WIDTH);
        
        graphName.setText(go.getPlotName(position));
        
        pack();
        setVisible(true);
    }
    
    @Override
    public void addFeatures(String name, ArrayList<ArrayList<Number>> results) {
        
        int xsel = plotType.getSelectedIndex();
        int ysel = plotFeature.getSelectedIndex();
        int zsel = groupType.getSelectedIndex();
        //int ssel = jComboBoxPointSize.getSelectedIndex();

        int hasColumn = hasColumn(name);
        
        if (hasColumn > -1) {
            
            ListIterator<ArrayList<Number>> itr = measurements.listIterator();
            
            while (itr.hasNext()) {
                ArrayList<Number> data = itr.next();
                data.remove(hasColumn);
            }
            
            descriptions.remove(hasColumn);
            descriptionsLabels.remove(hasColumn);
            
            featureCount--;
            
        }
        
        int newFeatures = results.size();
        int startSize = featureCount;
        
        String descr;

        //add desrciptions, grab for SQL column name additions
        //add results as columns,  need to check size.
        for (int i = startSize; i < newFeatures + startSize; i++) {
            descr = "";
            if (results.size() > 1) {
                descr = name + "_" + (i - startSize);
                descriptionsLabels.add(descr);
            } else {
                descr = name;
                descriptionsLabels.add(descr);
            }
            
            if (descr.length() > 10) {
                String truncated = name.substring(0, 8) + "__" + name.substring(name.length() - 5, name.length());
                descr = truncated + "_" + (i - startSize);
            }
            
            descriptions.add(descr);
            
            this.featureCount++;
            
        }
        for (int j = 0; j < results.size(); j++) {
            
            ArrayList<Number> features = results.get(j);   //the list of a single feature  

            for (int k = 0; k < features.size(); k++) {
                
                Number feature = (Number) features.get(k);
                
                ArrayList<Number> object = (ArrayList) measurements.get(k);
                object.add(feature);
            }
        }

        //AvailableDataHM = makeAvailableDataHM(descriptions);
        //ec.updateFeatureSpace(AvailableDataHM, measurements);
        //updatePlot = false;
        plotType.setModel(new DefaultComboBoxModel(this.descriptions.toArray()));
        plotFeature.setModel(new DefaultComboBoxModel(this.descriptions.toArray()));
        groupType.setModel(new DefaultComboBoxModel(this.descriptions.toArray()));
        
        ComboboxToolTipRenderer renderer = new ComboboxToolTipRenderer();
        renderer.setTooltips(descriptionsLabels);
        
        plotType.setRenderer(renderer);
        plotFeature.setRenderer(renderer);
        groupType.setRenderer(renderer);
        
        plotType.setSelectedIndex(xsel);
        plotFeature.setSelectedIndex(ysel);
        groupType.setSelectedIndex(zsel);
        
        initComponents();
        pack();
        
    }
    
    private int hasColumn(String test) {
        
        int c = 0;
        ListIterator<String> itr = descriptions.listIterator();
        
        while (itr.hasNext()) {
            String str = itr.next();
            if (str.equalsIgnoreCase(test)) {
                return c;
            }
            c++;
        }
        return -1;
    }
    
    private void updatePosition(int source) {
        
        if (source == UP_ARROW) {
            
        } else {
            
        }
        
    }
    
    private void setPlot() {
        ImageIcon imageIcon = go.getPlot(position);
        this.graphName.setText(go.getPlotName(position));
        System.out.println("PROFILING: getting plot at: " + go.getPlotName(position));
        JLabel imgLabel = new JLabel(imageIcon);
        imgLabel.setSize(PlotPanel.getSize());
        PlotPanel.removeAll();
        PlotPanel.add(imgLabel);
        PlotPanel.validate();
        PlotPanel.repaint();
        pack();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jToolBar1 = new javax.swing.JToolBar();
        jLabel1 = new javax.swing.JLabel();
        plotType = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        plotFeature = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        groupType = new javax.swing.JComboBox<>();
        jToolBar2 = new javax.swing.JToolBar();
        Backward = new javax.swing.JButton();
        Forward = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        graphName = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        Generate = new javax.swing.JButton();
        PlotPanel = new javax.swing.JPanel();

        setTitle("VTEA Plots");
        setAlwaysOnTop(true);
        setMinimumSize(new java.awt.Dimension(595, 600));
        setName("Plots"); // NOI18N
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setMaximumSize(new java.awt.Dimension(600, 31));
        jToolBar1.setMinimumSize(new java.awt.Dimension(600, 31));
        jToolBar1.setPreferredSize(new java.awt.Dimension(600, 31));

        jLabel1.setText("Plot:");
        jToolBar1.add(jLabel1);

        plotType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        plotType.setMaximumSize(new java.awt.Dimension(150, 27));
        plotType.setMinimumSize(new java.awt.Dimension(150, 27));
        plotType.setPreferredSize(new java.awt.Dimension(200, 27));
        plotType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plotTypeActionPerformed(evt);
            }
        });
        jToolBar1.add(plotType);

        jLabel2.setText("Feature");
        jToolBar1.add(jLabel2);

        plotFeature.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        plotFeature.setMaximumSize(new java.awt.Dimension(150, 27));
        plotFeature.setMinimumSize(new java.awt.Dimension(150, 27));
        plotFeature.setPreferredSize(new java.awt.Dimension(150, 27));
        jToolBar1.add(plotFeature);

        jLabel3.setText("Groups");
        jToolBar1.add(jLabel3);

        groupType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        groupType.setMaximumSize(new java.awt.Dimension(150, 27));
        groupType.setMinimumSize(new java.awt.Dimension(150, 27));
        groupType.setPreferredSize(new java.awt.Dimension(150, 27));
        jToolBar1.add(groupType);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(jToolBar1, gridBagConstraints);

        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);
        jToolBar2.setMaximumSize(new java.awt.Dimension(550, 31));
        jToolBar2.setMinimumSize(new java.awt.Dimension(550, 31));
        jToolBar2.setPreferredSize(new java.awt.Dimension(550, 31));

        Backward.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/go-back-7.png"))); // NOI18N
        Backward.setEnabled(false);
        Backward.setFocusable(false);
        Backward.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        Backward.setMaximumSize(new java.awt.Dimension(30, 30));
        Backward.setMinimumSize(new java.awt.Dimension(30, 30));
        Backward.setPreferredSize(new java.awt.Dimension(30, 30));
        Backward.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        Backward.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BackwardActionPerformed(evt);
            }
        });
        jToolBar2.add(Backward);

        Forward.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/go-forward-7.png"))); // NOI18N
        Forward.setEnabled(false);
        Forward.setFocusable(false);
        Forward.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        Forward.setMaximumSize(new java.awt.Dimension(30, 30));
        Forward.setMinimumSize(new java.awt.Dimension(30, 30));
        Forward.setPreferredSize(new java.awt.Dimension(30, 30));
        Forward.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        Forward.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ForwardActionPerformed(evt);
            }
        });
        jToolBar2.add(Forward);

        jPanel3.setMaximumSize(new java.awt.Dimension(30, 30));
        jPanel3.setMinimumSize(new java.awt.Dimension(30, 30));
        jPanel3.setPreferredSize(new java.awt.Dimension(30, 30));
        jPanel3.setRequestFocusEnabled(false);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );

        jToolBar2.add(jPanel3);

        graphName.setText("jLabel4");
        jToolBar2.add(graphName);

        jPanel2.setMaximumSize(new java.awt.Dimension(250, 30));
        jPanel2.setMinimumSize(new java.awt.Dimension(250, 30));
        jPanel2.setPreferredSize(new java.awt.Dimension(380, 30));
        jPanel2.setRequestFocusEnabled(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 287, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );

        jToolBar2.add(jPanel2);

        Generate.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        Generate.setText("Generate Graph");
        Generate.setToolTipText("Makes a new graph with the listed settings.");
        Generate.setFocusable(false);
        Generate.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        Generate.setMaximumSize(new java.awt.Dimension(120, 30));
        Generate.setMinimumSize(new java.awt.Dimension(120, 30));
        Generate.setPreferredSize(new java.awt.Dimension(120, 30));
        Generate.setRequestFocusEnabled(false);
        Generate.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        Generate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GenerateActionPerformed(evt);
            }
        });
        jToolBar2.add(Generate);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        getContentPane().add(jToolBar2, gridBagConstraints);

        PlotPanel.setBackground(new java.awt.Color(255, 255, 255));
        PlotPanel.setMaximumSize(new java.awt.Dimension(595, 500));
        PlotPanel.setMinimumSize(new java.awt.Dimension(595, 500));
        PlotPanel.setPreferredSize(new java.awt.Dimension(595, 500));
        PlotPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                PlotPanelComponentResized(evt);
            }
        });

        javax.swing.GroupLayout PlotPanelLayout = new javax.swing.GroupLayout(PlotPanel);
        PlotPanel.setLayout(PlotPanelLayout);
        PlotPanelLayout.setHorizontalGroup(
            PlotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 595, Short.MAX_VALUE)
        );
        PlotPanelLayout.setVerticalGroup(
            PlotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 500, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.ipady = 20;
        getContentPane().add(PlotPanel, gridBagConstraints);
        PlotPanel.getAccessibleContext().setAccessibleName("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ForwardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ForwardActionPerformed
        position++;
        if(position < go.getPlotCount()){
            setPlot();           
        }
        if(position == go.getPlotCount()-1){
            this.Forward.setEnabled(false);
         }
        if(position > -1){Backward.setEnabled(true);} 
    }//GEN-LAST:event_ForwardActionPerformed

    private void plotTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plotTypeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_plotTypeActionPerformed

    private void BackwardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BackwardActionPerformed
        position--;
        if(position > -1){
            setPlot();     
        }
        if(position == 0){
                Backward.setEnabled(false);
            }
        if(position < go.getPlotCount()){
            Forward.setEnabled(true);
        }
    }//GEN-LAST:event_BackwardActionPerformed

    private void GenerateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GenerateActionPerformed
        //a hack to get things working
        
    }//GEN-LAST:event_GenerateActionPerformed

    private void PlotPanelComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_PlotPanelComponentResized
        // TODO add your handling code here:
    }//GEN-LAST:event_PlotPanelComponentResized

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        // TODO add your handling code here:
    }//GEN-LAST:event_formComponentResized

    /**
     * @param args the command line arguments
     */
//    public static void main(String args[]) {
//        /* Set the Nimbus look and feel */
//        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
//        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
//         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
//         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(GraphOutputFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(GraphOutputFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(GraphOutputFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(GraphOutputFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        //</editor-fold>
//
//        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new GraphOutputFrame().setVisible(true);
//            }
//        });
//    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Backward;
    private javax.swing.JButton Forward;
    private javax.swing.JButton Generate;
    private javax.swing.JPanel PlotPanel;
    private javax.swing.JLabel graphName;
    private javax.swing.JComboBox<String> groupType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JComboBox<String> plotFeature;
    private javax.swing.JComboBox<String> plotType;
    // End of variables declaration//GEN-END:variables
}
