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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.WEST;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.ListIterator;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import org.apache.commons.io.FilenameUtils;
import vtea._vtea;
import vtea.exploration.listeners.UpdatePlotSettingsListener;
import vtea.gui.ComboboxToolTipRenderer;
import vtea.processor.PlotProcessor;

/**
 *
 * @author sethwinfree
 */
public class PlotOutputFrame extends javax.swing.JFrame implements UpdatePlotSettingsListener, ItemListener {

    PlotOutput go;

    ArrayList measurements;
    String[] plotTypes = {"Violin Plot", "Heatmap"};
    ArrayList<String> descriptions;

    ArrayList<JCheckBox> boxes;

    ArrayList<String> descriptionsLabels;
    int position = 0;

    JFrame multipleFeatures;

    boolean[] multipleSelections;

    final static int DOWN_ARROW = 1;
    final static int UP_ARROW = 2;

    ArrayList<Boolean> selected;

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

        this.descriptions = new ArrayList<String>();
        this.descriptionsLabels = new ArrayList<String>();

        this.descriptions.addAll(descriptions);
        this.descriptionsLabels.addAll(descriptionLabel);

        this.descriptions.add("Multiple...");
        this.descriptionsLabels.add("For selecting multiple features...");

        multipleSelections = new boolean[descriptions.size()];
        initComponents();

        multipleFeatures = new JFrame("Multiple feature selection");
        multipleFeatures.setAlwaysOnTop(true);
        multipleFeatures.setPreferredSize(new Dimension(630, 300));
        multipleFeatures.setMinimumSize(new Dimension(630, 300));
        multipleFeatures.setMaximumSize(new Dimension(630, 300));
        multipleFeatures.setType(Type.UTILITY);
        multipleFeatures.add(makeCheckPanel());
        multipleFeatures.validate();
        multipleFeatures.repaint();

        plotType.setModel(new DefaultComboBoxModel(vtea._vtea.PLOTMAKEROPTIONS));
        plotType.setSelectedItem("Select Method");
        plotFeature.setModel(new DefaultComboBoxModel(this.descriptions.toArray()));
        plotFeature.setSelectedIndex(0);
        plotFeature.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (((String) plotFeature.getSelectedItem()).equals("Multiple...")) {
                    multipleFeatures.pack();
                    multipleFeatures.setVisible(true);
                }
            }
        });
        groupType.setModel(new DefaultComboBoxModel(this.descriptions.toArray()));
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
            graphName.setText(go.getPlotName(position));
        }
        pack();
        setVisible(true);
    }

    private JScrollPane makeCheckPanel() {
        boxes = new ArrayList<JCheckBox>();
  
        JPanel panel = new JPanel();

        JScrollPane jsp = new JScrollPane();

        GridBagConstraints layoutConstraints = new GridBagConstraints();

        jsp.setPreferredSize(new Dimension(600, 200));
        panel.setBackground(new Color(238, 238, 238));
        panel.setLayout(new GridBagLayout());

        int count = 0;

        for (int i = 0; i < descriptions.size() - 1; i++) {

            layoutConstraints.weightx = 1;
            layoutConstraints.weighty = 1;
            layoutConstraints.fill = 10 / (1 + 4 * (i % 2));
            layoutConstraints.gridx = i % 4;
            layoutConstraints.gridy = i / 4;
            layoutConstraints.anchor = WEST;
            layoutConstraints.fill = CENTER;
            if (count < descriptions.size() - 1) {
                JCheckBox box = new JCheckBox(descriptions.get(i));
                box.addItemListener(this);
                boxes.add(box);
                panel.add(box, layoutConstraints);
            }

            jsp.setViewportView(panel);

            jsp.validate();
            jsp.repaint();

            
        }
        return jsp;
    }


    private void setPlot() {
        ImageIcon imageIcon = go.getPlot(position);
        this.graphName.setText(go.getPlotName(position));
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

        NorthPanel = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        jLabel1 = new javax.swing.JLabel();
        plotType = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        plotFeature = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        groupType = new javax.swing.JComboBox<>();
        jToolBar2 = new javax.swing.JToolBar();
        Backward = new javax.swing.JButton();
        graphName = new javax.swing.JLabel();
        Forward = new javax.swing.JButton();
        ClosePlot = new javax.swing.JButton();
        SavePlot = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        Generate = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        GraphProgress = new javax.swing.JProgressBar();
        jPanel4 = new javax.swing.JPanel();
        PlotPanel = new javax.swing.JPanel();

        setTitle("Plots");
        setAlwaysOnTop(true);
        setMaximumSize(new java.awt.Dimension(600, 600));
        setMinimumSize(new java.awt.Dimension(600, 600));
        setName("Plots"); // NOI18N
        setPreferredSize(new java.awt.Dimension(600, 600));
        setResizable(false);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        NorthPanel.setMaximumSize(new java.awt.Dimension(600, 70));
        NorthPanel.setMinimumSize(new java.awt.Dimension(600, 70));
        NorthPanel.setPreferredSize(new java.awt.Dimension(600, 70));
        NorthPanel.setRequestFocusEnabled(false);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setMaximumSize(new java.awt.Dimension(600, 31));
        jToolBar1.setMinimumSize(new java.awt.Dimension(600, 31));
        jToolBar1.setPreferredSize(new java.awt.Dimension(600, 31));

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
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

        jLabel2.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabel2.setText("Feature");
        jToolBar1.add(jLabel2);

        plotFeature.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        plotFeature.setMaximumSize(new java.awt.Dimension(150, 27));
        plotFeature.setMinimumSize(new java.awt.Dimension(150, 27));
        plotFeature.setPreferredSize(new java.awt.Dimension(150, 27));
        jToolBar1.add(plotFeature);

        jLabel3.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabel3.setText("Groups");
        jToolBar1.add(jLabel3);

        groupType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        groupType.setMaximumSize(new java.awt.Dimension(150, 27));
        groupType.setMinimumSize(new java.awt.Dimension(150, 27));
        groupType.setPreferredSize(new java.awt.Dimension(150, 27));
        jToolBar1.add(groupType);

        NorthPanel.add(jToolBar1);

        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);
        jToolBar2.setMaximumSize(new java.awt.Dimension(600, 31));
        jToolBar2.setMinimumSize(new java.awt.Dimension(600, 31));
        jToolBar2.setPreferredSize(new java.awt.Dimension(600, 31));

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

        graphName.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        graphName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        graphName.setText("Plot");
        graphName.setToolTipText(getTitle());
        graphName.setMaximumSize(new java.awt.Dimension(200, 16));
        graphName.setMinimumSize(new java.awt.Dimension(200, 16));
        graphName.setPreferredSize(new java.awt.Dimension(200, 16));
        graphName.setRequestFocusEnabled(false);
        jToolBar2.add(graphName);

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

        ClosePlot.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        ClosePlot.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit-delete-6_24.png"))); // NOI18N
        ClosePlot.setToolTipText("Delete current plot...");
        ClosePlot.setEnabled(false);
        ClosePlot.setFocusable(false);
        ClosePlot.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ClosePlot.setMaximumSize(new java.awt.Dimension(30, 30));
        ClosePlot.setMinimumSize(new java.awt.Dimension(30, 30));
        ClosePlot.setPreferredSize(new java.awt.Dimension(30, 30));
        ClosePlot.setRequestFocusEnabled(false);
        ClosePlot.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        ClosePlot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClosePlotActionPerformed(evt);
            }
        });
        jToolBar2.add(ClosePlot);

        SavePlot.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        SavePlot.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/document-save-2_24.png"))); // NOI18N
        SavePlot.setToolTipText("Save plot as png...");
        SavePlot.setFocusable(false);
        SavePlot.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        SavePlot.setMaximumSize(new java.awt.Dimension(30, 30));
        SavePlot.setMinimumSize(new java.awt.Dimension(30, 30));
        SavePlot.setPreferredSize(new java.awt.Dimension(30, 30));
        SavePlot.setRequestFocusEnabled(false);
        SavePlot.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        SavePlot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SavePlotActionPerformed(evt);
            }
        });
        jToolBar2.add(SavePlot);

        jPanel3.setMaximumSize(new java.awt.Dimension(20, 30));
        jPanel3.setMinimumSize(new java.awt.Dimension(20, 30));
        jPanel3.setPreferredSize(new java.awt.Dimension(20, 30));
        jPanel3.setRequestFocusEnabled(false);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );

        jToolBar2.add(jPanel3);

        Generate.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        Generate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/dialog-apply.png"))); // NOI18N
        Generate.setText("Graph");
        Generate.setToolTipText("Makes a new graph with the listed settings.");
        Generate.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        Generate.setFocusable(false);
        Generate.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        Generate.setMaximumSize(new java.awt.Dimension(90, 30));
        Generate.setMinimumSize(new java.awt.Dimension(90, 30));
        Generate.setPreferredSize(new java.awt.Dimension(90, 30));
        Generate.setRequestFocusEnabled(false);
        Generate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GenerateActionPerformed(evt);
            }
        });
        jToolBar2.add(Generate);

        jPanel2.setToolTipText("");
        jPanel2.setMaximumSize(new java.awt.Dimension(10, 30));
        jPanel2.setMinimumSize(new java.awt.Dimension(10, 30));
        jPanel2.setPreferredSize(new java.awt.Dimension(10, 30));
        jPanel2.setRequestFocusEnabled(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );

        jToolBar2.add(jPanel2);

        GraphProgress.setMaximumSize(new java.awt.Dimension(150, 30));
        GraphProgress.setMinimumSize(new java.awt.Dimension(150, 30));
        GraphProgress.setPreferredSize(new java.awt.Dimension(150, 30));
        jToolBar2.add(GraphProgress);

        jPanel4.setToolTipText("");
        jPanel4.setMaximumSize(new java.awt.Dimension(15, 30));
        jPanel4.setMinimumSize(new java.awt.Dimension(15, 30));
        jPanel4.setRequestFocusEnabled(false);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );

        jToolBar2.add(jPanel4);

        NorthPanel.add(jToolBar2);

        getContentPane().add(NorthPanel, java.awt.BorderLayout.NORTH);

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
            .addGap(0, 611, Short.MAX_VALUE)
        );
        PlotPanelLayout.setVerticalGroup(
            PlotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 500, Short.MAX_VALUE)
        );

        getContentPane().add(PlotPanel, java.awt.BorderLayout.CENTER);
        PlotPanel.getAccessibleContext().setAccessibleName("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ForwardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ForwardActionPerformed
        position++;
        if (position < go.getPlotCount()) {          
        } else {
            position = go.getPlotCount() - 1;
        }
        if (position == go.getPlotCount() - 1) {
            this.Forward.setEnabled(false);
        }
        if (position > -1) {
            Backward.setEnabled(true);
        }
        setPlot();
    }//GEN-LAST:event_ForwardActionPerformed

    private void plotTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plotTypeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_plotTypeActionPerformed

    private void BackwardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BackwardActionPerformed
        position--;
        if (position > -1) {   
        } else {
            position = 0;
        }

        if (position == 0) {
            Backward.setEnabled(false);
        }
        if (position < go.getPlotCount()) {
            Forward.setEnabled(true);
        }
        setPlot();
    }//GEN-LAST:event_BackwardActionPerformed

    private void GenerateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GenerateActionPerformed

        new Thread(() -> {
            try {
                this.Generate.setEnabled(false);
                this.GraphProgress.setIndeterminate(true);
                ArrayList<String> settings = new ArrayList<>();

                settings.add((String) this.plotType.getSelectedItem());
                settings.add((String) this.groupType.getSelectedItem());

                ArrayList<String> features = new ArrayList<>();

                String featureTest = (String) plotFeature.getSelectedItem();

                if (featureTest.equals("Multiple...")) {
                    for (int j = 0; j < multipleSelections.length; j++) {
                        if (multipleSelections[j]) {
                            features.add(descriptions.get(j));
                        }
                    }
                } else {
                    features.add((String) plotFeature.getSelectedItem());
                }

                PlotProcessor pp = new PlotProcessor(key, settings, features);
                pp.run();

            } catch (Exception e) {
                System.out.println("ERROR in making 'features' list: " + e.getMessage());
            }
            go = new PlotOutput();
            go.updatePlotOutput();
            resetPosition(0);
            this.GraphProgress.setIndeterminate(false);
            this.Generate.setEnabled(true);
            repaint();
            validate();
            pack();
        }).start();


    }//GEN-LAST:event_GenerateActionPerformed

    private void PlotPanelComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_PlotPanelComponentResized
        // TODO add your handling code here:
    }//GEN-LAST:event_PlotPanelComponentResized

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        // TODO add your handling code here:
    }//GEN-LAST:event_formComponentResized

    private void ClosePlotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClosePlotActionPerformed
        go.deletePlot(position);
        go.updatePlotOutput();
        resetPosition(0);
        this.GraphProgress.setIndeterminate(false);
        this.Generate.setEnabled(true);
        repaint();
        validate();
        pack();
    }//GEN-LAST:event_ClosePlotActionPerformed

    private void SavePlotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SavePlotActionPerformed
        String filename = go.savePlot(position);
        int returnVal = JFileChooser.CANCEL_OPTION;
        File file;
        int choice = JOptionPane.OK_OPTION;
        do {
            JFileChooser jf = new JFileChooser(_vtea.LASTDIRECTORY);
            jf.setDialogTitle("Save plot...");

            returnVal = jf.showSaveDialog(this);

            file = jf.getSelectedFile();
            if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("png")) {

            } else {
                file = new File(file.toString() + ".png");
            }
            if (file.exists()) {
                String message = String.format("%s already exists\nOverwrite it?", file.getName());
                choice = JOptionPane.showConfirmDialog(null, message, "Overwrite File", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            }
        } while (choice != JOptionPane.OK_OPTION);

        if (returnVal == JFileChooser.APPROVE_OPTION) {

            try {
                File f = new File(filename);

                Files.copy(f.toPath(), file.toPath());

            } catch (NullPointerException | IOException ex) {
                System.out.println("ERROR: NPE or IO in image save.");
            }
            _vtea.LASTDIRECTORY = file.getAbsolutePath();
        }


    }//GEN-LAST:event_SavePlotActionPerformed

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
    private javax.swing.JButton ClosePlot;
    private javax.swing.JButton Forward;
    private javax.swing.JButton Generate;
    private javax.swing.JProgressBar GraphProgress;
    private javax.swing.JPanel NorthPanel;
    private javax.swing.JPanel PlotPanel;
    private javax.swing.JButton SavePlot;
    private javax.swing.JLabel graphName;
    private javax.swing.JComboBox<String> groupType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JComboBox<String> plotFeature;
    private javax.swing.JComboBox<String> plotType;
    // End of variables declaration//GEN-END:variables

    @Override
    public void updateDescriptions(ArrayList<String> descriptions, ArrayList<String> descriptionLabel) {
        System.out.println("PROFILING: updating descriptions");

        if (descriptions.contains("Multiple..."));
        {
            descriptions.remove("Multiple...");
            descriptionLabel.remove("For selecting multiple features...");
        }

        this.descriptions.clear();
        this.descriptionsLabels.clear();

        this.descriptions.addAll(descriptions);
        this.descriptionsLabels.addAll(descriptionLabel);

        this.descriptions.add("Multiple...");
        this.descriptionsLabels.add("For selecting multiple features...");

        int ysel = plotFeature.getSelectedIndex();
        int zsel = groupType.getSelectedIndex();

        plotFeature.setModel(new DefaultComboBoxModel(this.descriptions.toArray()));
        groupType.setModel(new DefaultComboBoxModel(this.descriptions.toArray()));

        ComboboxToolTipRenderer renderer = new ComboboxToolTipRenderer();
        renderer.setTooltips(descriptionsLabels);

        plotFeature.setRenderer(renderer);
        groupType.setRenderer(renderer);

        plotFeature.setSelectedIndex(ysel);
        groupType.setSelectedIndex(zsel);

        this.plotFeature.validate();
        this.groupType.validate();

        this.plotFeature.repaint();
        this.groupType.repaint();
        pack();
    }

    private void resetPosition(int pos) {
        position = go.getPlotCount() - 1;
        Backward.setEnabled(true);
        Forward.setEnabled(true);
        if (position < 0 || position == 0) {
            position = 0;
            Backward.setEnabled(false);
        }
        if (position == (go.getPlotCount() - 1)) {
            Forward.setEnabled(false);
        }

        Forward.validate();
        Backward.validate();
        graphName.validate();
        PlotPanel.validate();

        Forward.repaint();
        Backward.repaint();
        graphName.repaint();
        PlotPanel.repaint();

        setPlot();
    }

    @Override
    public void itemStateChanged(ItemEvent e) { 
         String str = ((JCheckBox)(e.getSource())).getText();
         if(((JCheckBox)(e.getSource())).isSelected()){
         multipleSelections[descriptions.indexOf(str)] = true ;
         } else {
         multipleSelections[descriptions.indexOf(str)] = false ;  
         }
    }
}
