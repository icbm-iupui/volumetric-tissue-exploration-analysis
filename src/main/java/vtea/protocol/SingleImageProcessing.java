/* 
 * Copyright (C) 2020 Indiana University
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
package vtea.protocol;

import ij.ImagePlus;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JWindow;
import vtea.ImageSelectionListener;
import vtea.processor.ExplorerProcessor;
import vtea.processor.ImageProcessingProcessor;
import vtea.processor.MeasurementProcessor;
import vtea.processor.SegmentationProcessor;
import vtea.protocol.blockstepgui.ProcessStepBlockGUI;
import vtea.protocol.listeners.AnalysisStartListener;
import vtea.protocol.listeners.DeleteBlockListener;
import vtea.protocol.listeners.MicroBlockSetupListener;
import vtea.protocol.listeners.RebuildPanelListener;
import vtea.protocol.listeners.RepaintTabListener;
import vtea.protocol.listeners.RequestImageListener;
import vtea.protocol.listeners.TransferProtocolStepsListener;
import vtea.protocol.listeners.UpdateProgressListener;
import vtea.protocol.listeners.UpdateSegmentationListener;
import vtea.protocol.listeners.UpdatedImageListener;
import vtea.protocol.listeners.UpdatedProtocolListener;
import vtea.protocol.menus.MultipleFilesMenu;
import vtea.protocol.setup.MicroBlockObjectSetup;
import vteaobjects.MicroObject;

/**
 *
 * @author vinfrais
 */
public class SingleImageProcessing extends javax.swing.JPanel implements PropertyChangeListener, UpdateSegmentationListener, UpdateProgressListener, ImageSelectionListener, TransferProtocolStepsListener, RebuildPanelListener, DeleteBlockListener {

    public static final int WORKFLOW = 0;
    public static final int PROCESSBLOCKS = 1;
    public static final int OBJECTBLOCKS = 2;
    public static final int EXPLOREBLOCKS = 3;
    public static final int BATCH = 4;

    //public JList OpenImages;
    private ImagePlus OriginalImage;
    private ImagePlus ProcessedImage;
    private ImagePlus ThumbnailImage;
    protected JPanel thumbnail;

    protected String tabName;

    protected Color ImageBlockBackground = new java.awt.Color(135, 175, 215);

//batch support
    public MultipleFilesMenu mfm = new MultipleFilesMenu(false);
    protected boolean batch = false;
    protected JList batchImages;

    protected ArrayList<ProcessStepBlockGUI> ProcessingStepsList;
    protected ArrayList<ObjectStepBlockGUI> ObjectStepsList;

    public JWindow thumb = new JWindow();

    protected GridLayout PreProcessingLayout = new GridLayout(10, 1, 0, 0);
    protected GridLayout ObjectLayout = new GridLayout(4, 1, 0, 0);
    protected GridLayout ExploreLayout = new GridLayout(4, 1, 0, 0);

    protected ArrayList<String> Channels;

    private ArrayList<AnalysisStartListener> listeners = new ArrayList<AnalysisStartListener>();
    private ArrayList<RequestImageListener> RequestImageListeners = new ArrayList<RequestImageListener>();
    private ArrayList<RepaintTabListener> RepaintTabListeners = new ArrayList<RepaintTabListener>();

    private ArrayList<UpdatedImageListener> UpdatedImageListeners = new ArrayList<UpdatedImageListener>();
    private ArrayList<UpdatedProtocolListener> UpdatedProtocolListeners = new ArrayList<UpdatedProtocolListener>();

    private ArrayList<SegmentationProcessor> segmentationProcessors = new ArrayList();
    private ArrayList<ExplorerProcessor> explorerProcessors = new ArrayList();
    private ArrayList<MeasurementProcessor> measurementProcessors = new ArrayList();

    private int segmentationCount = 0;

    private int tab;
    private boolean selected = false;

    private int[] thread = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    private boolean imageLoaded = false;

    /**
     * Creates new form NewJPanel
     */
    public SingleImageProcessing() {
        this.ProcessingStepsList = new ArrayList<>();
        this.ObjectStepsList = new ArrayList<>();
        this.addPropertyChangeListener(this);
        initComponents();
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

        SingleImageProcessing = new javax.swing.JPanel();
        Preprocessing_Header = new javax.swing.JPanel();
        OpenImage = new javax.swing.JButton();
        PreProcessingLabel = new javax.swing.JLabel();
        AddStep_Preprocessing = new javax.swing.JButton();
        DeleteAllSteps_PreProcessing = new javax.swing.JButton();
        PreProcessing_Panel = new javax.swing.JPanel();
        PreProcessingGo = new javax.swing.JButton();
        PreProcessingStepsScrollPanel = new javax.swing.JScrollPane();
        PreProcessingStepsPanel = new javax.swing.JPanel();
        Object_Header = new javax.swing.JPanel();
        FindObjectText = new javax.swing.JLabel();
        ObjectsLabel = new javax.swing.JLabel();
        AddStep_Object = new javax.swing.JButton();
        DeleteAllSteps_Object = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        exploreText = new javax.swing.JLabel();
        Object_Panel = new javax.swing.JPanel();
        ObjectStepsPanel = new javax.swing.JPanel();
        ObjectGo = new javax.swing.JButton();
        ProgressPanel = new javax.swing.JPanel();
        ProgressComment = new javax.swing.JLabel();
        VTEAProgressBar = new javax.swing.JProgressBar();

        setMaximumSize(new java.awt.Dimension(761, 381));
        setMinimumSize(new java.awt.Dimension(761, 381));
        setPreferredSize(new java.awt.Dimension(761, 381));
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });
        java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 1, 1);
        flowLayout1.setAlignOnBaseline(true);
        setLayout(flowLayout1);

        SingleImageProcessing.setMaximumSize(new java.awt.Dimension(750, 353));
        SingleImageProcessing.setMinimumSize(new java.awt.Dimension(750, 353));
        SingleImageProcessing.setLayout(new java.awt.GridBagLayout());

        Preprocessing_Header.setBackground(new java.awt.Color(204, 204, 204));
        Preprocessing_Header.setForeground(new java.awt.Color(102, 102, 102));
        Preprocessing_Header.setAlignmentX(0.0F);
        Preprocessing_Header.setAlignmentY(0.0F);
        Preprocessing_Header.setMaximumSize(new java.awt.Dimension(400, 36));
        Preprocessing_Header.setMinimumSize(new java.awt.Dimension(300, 36));
        Preprocessing_Header.setPreferredSize(new java.awt.Dimension(300, 36));
        Preprocessing_Header.setLayout(new java.awt.GridBagLayout());

        OpenImage.setBackground(vtea._vtea.BUTTONBACKGROUND);
        OpenImage.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        OpenImage.setText("Load Image");
        OpenImage.setToolTipText("Load image for processing.");
        OpenImage.setBorder(null);
        OpenImage.setMargin(new java.awt.Insets(2, 5, 2, 5));
        OpenImage.setMaximumSize(new java.awt.Dimension(80, 33));
        OpenImage.setMinimumSize(new java.awt.Dimension(80, 23));
        OpenImage.setPreferredSize(new java.awt.Dimension(120, 34));
        OpenImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OpenImageActionPerformed(evt);
            }
        });
        Preprocessing_Header.add(OpenImage, new java.awt.GridBagConstraints());

        PreProcessingLabel.setBackground(new java.awt.Color(204, 204, 204));
        PreProcessingLabel.setFont(new java.awt.Font("Helvetica Neue", 0, 24)); // NOI18N
        PreProcessingLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        PreProcessingLabel.setText("Process");
        PreProcessingLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        PreProcessingLabel.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.ipadx = 11;
        Preprocessing_Header.add(PreProcessingLabel, gridBagConstraints);

        AddStep_Preprocessing.setBackground(vtea._vtea.BUTTONBACKGROUND);
        AddStep_Preprocessing.setForeground(new java.awt.Color(102, 102, 102));
        AddStep_Preprocessing.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/list-add-3 2.png"))); // NOI18N
        AddStep_Preprocessing.setToolTipText("Adds a processing step.");
        AddStep_Preprocessing.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        AddStep_Preprocessing.setEnabled(false);
        AddStep_Preprocessing.setMaximumSize(new java.awt.Dimension(32, 32));
        AddStep_Preprocessing.setMinimumSize(new java.awt.Dimension(32, 32));
        AddStep_Preprocessing.setName(""); // NOI18N
        AddStep_Preprocessing.setPreferredSize(new java.awt.Dimension(34, 34));
        AddStep_Preprocessing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddStep_PreprocessingActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        Preprocessing_Header.add(AddStep_Preprocessing, gridBagConstraints);

        DeleteAllSteps_PreProcessing.setBackground(vtea._vtea.BUTTONBACKGROUND);
        DeleteAllSteps_PreProcessing.setForeground(new java.awt.Color(102, 102, 102));
        DeleteAllSteps_PreProcessing.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit-clear-list_24.png"))); // NOI18N
        DeleteAllSteps_PreProcessing.setToolTipText("Delete all processing");
        DeleteAllSteps_PreProcessing.setEnabled(false);
        DeleteAllSteps_PreProcessing.setPreferredSize(new java.awt.Dimension(34, 34));
        DeleteAllSteps_PreProcessing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteAllSteps_PreProcessingActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        Preprocessing_Header.add(DeleteAllSteps_PreProcessing, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 7;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        SingleImageProcessing.add(Preprocessing_Header, gridBagConstraints);

        PreProcessing_Panel.setBackground(new java.awt.Color(204, 204, 204));
        PreProcessing_Panel.setForeground(new java.awt.Color(102, 102, 102));
        PreProcessing_Panel.setAlignmentX(0.0F);
        PreProcessing_Panel.setAlignmentY(0.0F);
        PreProcessing_Panel.setMaximumSize(new java.awt.Dimension(300, 300));
        PreProcessing_Panel.setMinimumSize(new java.awt.Dimension(300, 300));
        PreProcessing_Panel.setPreferredSize(new java.awt.Dimension(300, 300));

        PreProcessingGo.setBackground(vtea._vtea.BUTTONBACKGROUND);
        PreProcessingGo.setText("Process");
        PreProcessingGo.setToolTipText("Process the loaded image.");
        PreProcessingGo.setEnabled(false);
        PreProcessingGo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PreProcessingGoActionPerformed(evt);
            }
        });

        PreProcessingStepsScrollPanel.setBackground(vtea._vtea.ACTIONPANELBACKGROUND);

        PreProcessingStepsPanel.setBackground(vtea._vtea.ACTIONPANELBACKGROUND);
        PreProcessingStepsPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        PreProcessingStepsPanel.setPreferredSize(new java.awt.Dimension(240, 620));

        javax.swing.GroupLayout PreProcessingStepsPanelLayout = new javax.swing.GroupLayout(PreProcessingStepsPanel);
        PreProcessingStepsPanel.setLayout(PreProcessingStepsPanelLayout);
        PreProcessingStepsPanelLayout.setHorizontalGroup(
            PreProcessingStepsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 269, Short.MAX_VALUE)
        );
        PreProcessingStepsPanelLayout.setVerticalGroup(
            PreProcessingStepsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 620, Short.MAX_VALUE)
        );

        PreProcessingStepsScrollPanel.setViewportView(PreProcessingStepsPanel);

        javax.swing.GroupLayout PreProcessing_PanelLayout = new javax.swing.GroupLayout(PreProcessing_Panel);
        PreProcessing_Panel.setLayout(PreProcessing_PanelLayout);
        PreProcessing_PanelLayout.setHorizontalGroup(
            PreProcessing_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PreProcessing_PanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(PreProcessingGo, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(193, Short.MAX_VALUE))
            .addGroup(PreProcessing_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(PreProcessing_PanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(PreProcessingStepsScrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        PreProcessing_PanelLayout.setVerticalGroup(
            PreProcessing_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PreProcessing_PanelLayout.createSequentialGroup()
                .addGap(263, 263, 263)
                .addComponent(PreProcessingGo)
                .addContainerGap(8, Short.MAX_VALUE))
            .addGroup(PreProcessing_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(PreProcessing_PanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(PreProcessingStepsScrollPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 247, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(47, Short.MAX_VALUE)))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        SingleImageProcessing.add(PreProcessing_Panel, gridBagConstraints);

        Object_Header.setBackground(new java.awt.Color(204, 204, 204));
        Object_Header.setForeground(new java.awt.Color(102, 102, 102));
        Object_Header.setAlignmentX(0.0F);
        Object_Header.setAlignmentY(0.0F);
        Object_Header.setMaximumSize(new java.awt.Dimension(440, 36));
        Object_Header.setMinimumSize(new java.awt.Dimension(440, 36));
        Object_Header.setPreferredSize(new java.awt.Dimension(440, 36));
        Object_Header.setLayout(new java.awt.GridBagLayout());

        FindObjectText.setForeground(new java.awt.Color(153, 153, 153));
        FindObjectText.setText("Find Objects...");
        FindObjectText.setMaximumSize(new java.awt.Dimension(120, 16));
        FindObjectText.setMinimumSize(new java.awt.Dimension(100, 16));
        FindObjectText.setPreferredSize(new java.awt.Dimension(100, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        Object_Header.add(FindObjectText, gridBagConstraints);

        ObjectsLabel.setBackground(new java.awt.Color(0, 0, 0));
        ObjectsLabel.setFont(new java.awt.Font("Helvetica Neue", 0, 24)); // NOI18N
        ObjectsLabel.setText("Segment");
        ObjectsLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        ObjectsLabel.setMaximumSize(new java.awt.Dimension(150, 28));
        ObjectsLabel.setMinimumSize(new java.awt.Dimension(150, 28));
        ObjectsLabel.setPreferredSize(new java.awt.Dimension(150, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        Object_Header.add(ObjectsLabel, gridBagConstraints);

        AddStep_Object.setBackground(new java.awt.Color(204, 204, 204));
        AddStep_Object.setForeground(new java.awt.Color(102, 102, 102));
        AddStep_Object.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/list-add-3 2.png"))); // NOI18N
        AddStep_Object.setToolTipText("Add a segmentation method.");
        AddStep_Object.setEnabled(false);
        AddStep_Object.setMaximumSize(new java.awt.Dimension(34, 34));
        AddStep_Object.setMinimumSize(new java.awt.Dimension(34, 34));
        AddStep_Object.setPreferredSize(new java.awt.Dimension(34, 34));
        AddStep_Object.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddStep_ObjectActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        Object_Header.add(AddStep_Object, gridBagConstraints);

        DeleteAllSteps_Object.setBackground(new java.awt.Color(204, 204, 204));
        DeleteAllSteps_Object.setForeground(new java.awt.Color(102, 102, 102));
        DeleteAllSteps_Object.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit-clear-list_24.png"))); // NOI18N
        DeleteAllSteps_Object.setToolTipText("Delete all segmentation methods.");
        DeleteAllSteps_Object.setEnabled(false);
        DeleteAllSteps_Object.setMaximumSize(new java.awt.Dimension(34, 34));
        DeleteAllSteps_Object.setMinimumSize(new java.awt.Dimension(34, 34));
        DeleteAllSteps_Object.setPreferredSize(new java.awt.Dimension(34, 34));
        DeleteAllSteps_Object.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteAllSteps_ObjectActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        Object_Header.add(DeleteAllSteps_Object, gridBagConstraints);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        Object_Header.add(jPanel1, new java.awt.GridBagConstraints());

        exploreText.setFont(new java.awt.Font("Helvetica Neue", 0, 18)); // NOI18N
        exploreText.setForeground(new java.awt.Color(153, 153, 153));
        exploreText.setText("     ...explore");
        Object_Header.add(exploreText, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 7;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        SingleImageProcessing.add(Object_Header, gridBagConstraints);

        Object_Panel.setBackground(new java.awt.Color(204, 204, 204));
        Object_Panel.setForeground(new java.awt.Color(102, 102, 102));
        Object_Panel.setAlignmentX(0.0F);
        Object_Panel.setAlignmentY(0.0F);
        Object_Panel.setMaximumSize(new java.awt.Dimension(440, 360));
        Object_Panel.setMinimumSize(new java.awt.Dimension(440, 360));
        Object_Panel.setPreferredSize(new java.awt.Dimension(440, 300));
        Object_Panel.setRequestFocusEnabled(false);

        ObjectStepsPanel.setBackground(vtea._vtea.ACTIONPANELBACKGROUND);
        ObjectStepsPanel.setPreferredSize(new java.awt.Dimension(160, 245));

        javax.swing.GroupLayout ObjectStepsPanelLayout = new javax.swing.GroupLayout(ObjectStepsPanel);
        ObjectStepsPanel.setLayout(ObjectStepsPanelLayout);
        ObjectStepsPanelLayout.setHorizontalGroup(
            ObjectStepsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        ObjectStepsPanelLayout.setVerticalGroup(
            ObjectStepsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 245, Short.MAX_VALUE)
        );

        ObjectGo.setBackground(vtea._vtea.BUTTONBACKGROUND);
        ObjectGo.setText("Find Objects");
        ObjectGo.setToolTipText("Find segmented objects.");
        ObjectGo.setEnabled(false);
        ObjectGo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ObjectGoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout Object_PanelLayout = new javax.swing.GroupLayout(Object_Panel);
        Object_Panel.setLayout(Object_PanelLayout);
        Object_PanelLayout.setHorizontalGroup(
            Object_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Object_PanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(Object_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ObjectStepsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                    .addGroup(Object_PanelLayout.createSequentialGroup()
                        .addComponent(ObjectGo, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 316, Short.MAX_VALUE)))
                .addContainerGap())
        );
        Object_PanelLayout.setVerticalGroup(
            Object_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Object_PanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ObjectStepsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(ObjectGo)
                .addContainerGap(68, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        SingleImageProcessing.add(Object_Panel, gridBagConstraints);

        add(SingleImageProcessing);

        ProgressPanel.setMaximumSize(new java.awt.Dimension(700, 50));
        ProgressPanel.setMinimumSize(new java.awt.Dimension(700, 50));
        ProgressPanel.setPreferredSize(new java.awt.Dimension(700, 30));
        ProgressPanel.setRequestFocusEnabled(false);
        java.awt.FlowLayout flowLayout2 = new java.awt.FlowLayout(java.awt.FlowLayout.TRAILING, 1, 1);
        flowLayout2.setAlignOnBaseline(true);
        ProgressPanel.setLayout(flowLayout2);

        ProgressComment.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        ProgressComment.setText("Load an image to begin...");
        ProgressPanel.add(ProgressComment);

        VTEAProgressBar.setPreferredSize(new java.awt.Dimension(200, 20));
        ProgressPanel.add(VTEAProgressBar);

        add(ProgressPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void OpenImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OpenImageActionPerformed

        this.notifyRequestImageListeners(tab);
        this.notifyRepaintTabListeners();

    }//GEN-LAST:event_OpenImageActionPerformed

    private void AddStep_PreprocessingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddStep_PreprocessingActionPerformed

        addPreprocessingBlock();
    }//GEN-LAST:event_AddStep_PreprocessingActionPerformed

    private void DeleteAllSteps_PreProcessingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteAllSteps_PreProcessingActionPerformed
        ProcessingStepsList.clear();
        PreProcessingStepsPanel.removeAll();
        AddStep_Preprocessing.setEnabled(false);
        PreProcessingGo.setEnabled(false);
        this.OpenImage.setEnabled(true);
        PreProcessingStepsPanel.repaint();
        this.OriginalImage = new ImagePlus();
        System.gc();

    }//GEN-LAST:event_DeleteAllSteps_PreProcessingActionPerformed

    private void PreProcessingGoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PreProcessingGoActionPerformed

        this.firePropertyChange("comment", "", "Processing image data...");
        executeProcessing();
        VTEAProgressBar.setValue(0);
        FindObjectText.setForeground(vtea._vtea.ACTIVETEXT);
        AddStep_Object.setEnabled(true);
        if (ObjectStepsList.size() > 0) {
            ObjectGo.setEnabled(true);
        }
    }//GEN-LAST:event_PreProcessingGoActionPerformed

    private void AddStep_ObjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddStep_ObjectActionPerformed

        addObjectBlock();

    }//GEN-LAST:event_AddStep_ObjectActionPerformed

    private void DeleteAllSteps_ObjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteAllSteps_ObjectActionPerformed
        ObjectStepsList.clear();
        ObjectStepsPanel.removeAll();
        AddStep_Object.setEnabled(true);
        ObjectStepsPanel.repaint();
        //pack();
    }//GEN-LAST:event_DeleteAllSteps_ObjectActionPerformed

    private void ObjectGoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ObjectGoActionPerformed

        new Thread(new Runnable() {
            public void run() {
                VTEAProgressBar.setIndeterminate(true);
                ObjectGo.setEnabled(false);
                PreProcessingGo.setEnabled(false);
                firePropertyChange("comment", "", "Finding objects...");
                executeSegmentation();

                PreProcessingGo.setEnabled(true);
                VTEAProgressBar.setIndeterminate(false);

                try {
                    java.lang.Thread.sleep(100);
                } catch (Exception e) {
                }
            }
        }).start();
    }//GEN-LAST:event_ObjectGoActionPerformed

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed

    }//GEN-LAST:event_formKeyPressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AddStep_Object;
    public javax.swing.JButton AddStep_Preprocessing;
    private javax.swing.JButton DeleteAllSteps_Object;
    private javax.swing.JButton DeleteAllSteps_PreProcessing;
    private javax.swing.JLabel FindObjectText;
    public javax.swing.JButton ObjectGo;
    public javax.swing.JPanel ObjectStepsPanel;
    private javax.swing.JPanel Object_Header;
    private javax.swing.JPanel Object_Panel;
    private javax.swing.JLabel ObjectsLabel;
    private javax.swing.JButton OpenImage;
    private javax.swing.JButton PreProcessingGo;
    private javax.swing.JLabel PreProcessingLabel;
    public javax.swing.JPanel PreProcessingStepsPanel;
    private javax.swing.JScrollPane PreProcessingStepsScrollPanel;
    public javax.swing.JPanel PreProcessing_Panel;
    private javax.swing.JPanel Preprocessing_Header;
    public javax.swing.JLabel ProgressComment;
    private javax.swing.JPanel ProgressPanel;
    private javax.swing.JPanel SingleImageProcessing;
    public javax.swing.JProgressBar VTEAProgressBar;
    private javax.swing.JLabel exploreText;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

    //classes for batch processing
    public void setTabValue(int tab) {
        this.tab = tab;
    }

    public void setTabName(String st) {
        this.tabName = st;
    }

    @Override
    public void transferThese(int type, int tab, String arg) {
        if (this.tab == tab) {
        }
    }

    @Override
    public void rebuildPanel(int type) {
        switch (type) {
            case ProtocolManagerMulti.PROCESS:
                this.RebuildPanelProcessing();
                return;
            case ProtocolManagerMulti.OBJECT:
                this.RebuildPanelObject();
                return;
            default:
                return;
        }
    }

    @Override
    public void deleteBlock(int type, int position) {
        switch (type) {
            case ProtocolManagerMulti.PROCESS:
                this.deleteProcessStep(position);
                return;
            case ProtocolManagerMulti.OBJECT:
                this.deleteObjectStep(position);
                return;
            default:
                return;
        }
    }

    @Override
    public void changeProgress(String text, int min, int max, int position) {
        VTEAProgressBar.setMinimum(min);
        VTEAProgressBar.setMaximum(max);
        VTEAProgressBar.setValue(position);
        ProgressComment.setText(text);
    }

    public void addPreprocessingBlock() {
        if (ProcessingStepsList.size() > 0) {
            ProcessStepBlockGUI block = new ProcessStepBlockGUI("Process Step", "", Color.LIGHT_GRAY, false, ThumbnailImage, OriginalImage, Channels, ProtocolManagerMulti.PROCESS, ProcessingStepsList, ProcessingStepsList.size() + 1);
            block.addDeleteBlockListener(this);
            block.addRebuildPanelListener(this);
            this.notifyRepaintTabListeners();
            PreProcessingStepsPanel.setLayout(PreProcessingLayout);
            PreProcessingStepsPanel.add(block.getPanel());
            PreProcessingStepsPanel.repaint();
            ProcessingStepsList.add(block);
            if (ProcessingStepsList.size() <= 9) {
                AddStep_Preprocessing.setEnabled(true);
            }
            if (ProcessingStepsList.size() >= 10) {
                AddStep_Preprocessing.setEnabled(false);
            }
        }
        this.notifyRepaintTabListeners();
    }

    public void addObjectBlock() {

        new Thread(() -> {
            try {
                ProgressComment.setText("Processed image transferring...");
                VTEAProgressBar.setIndeterminate(true);

                ObjectStepBlockGUI block = new ObjectStepBlockGUI();
                addUpdatedImageListener(block);
                block.addUpdateSegmentationListener(this);
                ObjectStepsPanel.setLayout(ObjectLayout);
                ObjectStepsPanel.add(block.getPanel());
                ObjectStepsPanel.repaint();

                ObjectStepsList.add(block);
                if (ObjectStepsList.size() <= 1) {
                    AddStep_Object.setEnabled(true);
                }
                if (ObjectStepsList.size() >= 4) {
                    AddStep_Object.setEnabled(false);
                }
                this.notifyRepaintTabListeners();
                VTEAProgressBar.setIndeterminate(false);
                ProgressComment.setText("Processed image transferred...");
            } catch (Exception e) {
                VTEAProgressBar.setIndeterminate(false);
                ProgressComment.setText("Processed image transferred...");
                System.out.println("ERROR: " + e.getLocalizedMessage());
            }
        }).start();

    }

    @Override
    public void onUpdateSegmentation(int i) {
        ObjectGo.setEnabled(true);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        if (evt.getPropertyName().equals("progress")) {
            int progress = (Integer) evt.getNewValue();
            VTEAProgressBar.setValue(progress);
            ProgressComment.setText(String.format(
                    "Completed %d%%...\n", progress));
        }

        if (evt.getPropertyName().equals("reset")) {
            for (int i = 0; i < thread.length; i++) {
                thread[i] = 0;
            }
            VTEAProgressBar.setValue(0);
            ProgressComment.setText(String.format(
                    "", thread[0]));
        }
        if (evt.getPropertyName().equals("3")) {
            thread[3] = +((Integer) evt.getNewValue());
            VTEAProgressBar.setValue(thread[3]);
            ProgressComment.setText(String.format(
                    "Completed %d%%...\n", thread[3]));
        }
        if (evt.getPropertyName().equals("segmentationDone")) {
            ProgressComment.setText((String) evt.getNewValue());
            String key = (String) evt.getOldValue();
            ListIterator itr = segmentationProcessors.listIterator();

            while (itr.hasNext()) {
                SegmentationProcessor sp = (SegmentationProcessor) itr.next();
                if (sp.getUIDKey().equals(key)) {
                    executeMeasuring(key, sp.getObjects(), sp.getProtocol());
                }
            }
        }
        if (evt.getPropertyName().equals("measurementDone")) {
            ProgressComment.setText((String) evt.getNewValue());
            String key = (String) evt.getOldValue();

            ListIterator itr = measurementProcessors.listIterator();

            while (itr.hasNext()) {
                MeasurementProcessor mp = (MeasurementProcessor) itr.next();
                if (mp.getUIDKey().equals(key)) {
                    executeExploring(key, mp.getObjects(), mp.getFeatures(),
                            mp.getDescriptions(), mp.getDescriptionLabels());
                }
            }
        }
        if (evt.getPropertyName().equals("comment")) {
            ProgressComment.setText((String) evt.getNewValue());
        }
        if (evt.getPropertyName().equals("escape")
                && (Boolean) evt.getNewValue()) {

            ImagePlus ProcessedShow = new ImagePlus("Processed");
            ProcessedShow = UtilityMethods.makeThumbnail(ProcessedImage);
            ProcessedShow.setTitle(this.getName() + "_Processed");

            ProgressComment.setText("Processing complete...");
            ProcessedShow.show();

            if (ObjectStepsList.size() > 0) {
                notifyUpdatedImageListeners(ProcessedImage);
            }
        }
        if (evt.getPropertyName().equals("escape") && !(Boolean) evt.getNewValue()) {

            System.out.println("PROFILING: Error in processing, thread terminated early...");

        }
    }

//classes for step blocks
    public final class ObjectStepBlockGUI extends Object implements
            MicroBlockSetupListener, UpdatedImageListener, UpdatedProtocolListener {

        JPanel step = new JPanel();
        Font PositionFont = new Font("Arial", Font.PLAIN, 14);
        Font ObjectFont = new Font("Arial", Font.BOLD, 12);
        Font CommentFont = new Font("Arial", Font.ITALIC, 10);
        JLabel Position = new JLabel();
        JLabel Comment = new JLabel("Block by Block");
        JLabel Object = new JLabel("First things first");
        boolean ProcessTypeSet = false;
        boolean thresholdPreviewRoi = false;
        int position;
        String key = UUID.randomUUID().toString();

        JButton DeleteButton;
        JButton EditButton;
        JButton MeasureButton;
        JButton UpdateExplorer;

        MicroBlockObjectSetup mbs;

        private ArrayList settings;

        private ArrayList<UpdateSegmentationListener> UpdateSegmentationListeners = new ArrayList<UpdateSegmentationListener>();

        public ObjectStepBlockGUI() {
            BuildStepBlock("Setup segmentation...", "", Color.LIGHT_GRAY);
        }

        public ObjectStepBlockGUI(String ProcessText,
                String CommentText, Color BlockColor) {

            BuildStepBlock(ProcessText, CommentText, Color.GREEN);
        }

        private void BuildStepBlock(String ProcessText,
                String CommentText, Color BlockColor) {

            if (ObjectStepsList.isEmpty()) {
                position = 1;
            } else {
                position = ObjectStepsList.size() + 1;
            }

            Object.setText(ProcessText);

            Comment.setText(CommentText);
            step.setBackground(BlockColor);

            //need max size set here
            Position.setText(position + ".");
            Position.setFont(PositionFont);

            if (Object.getText().length() < 12) {
                ObjectFont = new Font("Arial", Font.BOLD, 10);
            }
            if (Comment.getText().length() > 12) {
                CommentFont = new Font("Arial", Font.BOLD, 8);
            }

            Object.setFont(ObjectFont);
            Comment.setFont(CommentFont);

            mbs = new MicroBlockObjectSetup(position, Channels, ProcessedImage);

            mbs.setVisible(false);
            mbs.addMicroBlockSetupListener(this);

            DeleteButton = new JButton();
            DeleteButton.setPreferredSize(new Dimension(20, 20));
            DeleteButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    deleteObjectStep(position);
                    mbs.setVisible(false);
                }
            });

            EditButton = new JButton();
            EditButton.setPreferredSize(new Dimension(20, 20));
            EditButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    mbs.setVisible(true);

                }
            });

            UpdateExplorer = new JButton();
            UpdateExplorer.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    notifyUpdateSegmentationListeners(position);
                }
            });
            UpdateExplorer.setEnabled(false);

            DeleteButton.setSize(20, 20);
            DeleteButton.setBackground(vtea._vtea.BUTTONBACKGROUND);
            DeleteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit-delete-6_16.png")));
            DeleteButton.setToolTipText("Delete protocol...");

            EditButton.setSize(20, 20);
            EditButton.setBackground(vtea._vtea.BUTTONBACKGROUND);
            EditButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit-4.png")));
            EditButton.setToolTipText("Edit segmentation protocol...");

            JPanel fill = new JPanel();
            fill.setPreferredSize(new Dimension(20, 20));

            step.setSize(205, 20);
            step.setBorder(javax.swing.BorderFactory.createEtchedBorder());

            step.setLayout(new GridBagLayout());
            GridBagConstraints layoutConstraints = new GridBagConstraints();

            layoutConstraints.fill = GridBagConstraints.BOTH;
            layoutConstraints.gridx = 1;
            layoutConstraints.gridy = 0;
            layoutConstraints.weightx = 10;
            layoutConstraints.weighty = 10;
            step.add(Object, layoutConstraints);

            layoutConstraints.fill = GridBagConstraints.BOTH;
            layoutConstraints.gridx = 1;
            layoutConstraints.gridy = 1;
            layoutConstraints.weightx = 10;
            layoutConstraints.weighty = 10;
            step.add(Comment, layoutConstraints);

            layoutConstraints.fill = GridBagConstraints.BOTH;
            layoutConstraints.gridx = 2;
            layoutConstraints.gridy = 0;
            layoutConstraints.weightx = -1;
            layoutConstraints.weighty = -1;
            layoutConstraints.ipadx = 0;
            layoutConstraints.ipady = 20;
            step.add(DeleteButton, layoutConstraints);

            layoutConstraints.fill = GridBagConstraints.BOTH;
            layoutConstraints.gridx = 2;
            layoutConstraints.gridy = 1;
            layoutConstraints.weightx = -1;
            layoutConstraints.weighty = -1;
            layoutConstraints.ipadx = 0;
            layoutConstraints.ipady = 20;
            step.add(EditButton, layoutConstraints);

            step.addMouseListener(new java.awt.event.MouseListener() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                }

                ;
            @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    //thumb.setVisible(false);
                }

                ;
            @Override
                public void mouseReleased(java.awt.event.MouseEvent evt) {
                    thumb.setVisible(false);
                }

                ;
            @Override
                public void mousePressed(java.awt.event.MouseEvent evt) {

                }

                ;
            @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                }
            ;
        }

        );
        }
        
    public void addUpdateSegmentationListener(UpdateSegmentationListener listener) {
            UpdateSegmentationListeners.add(listener);
        }

        private void notifyUpdateSegmentationListeners(int tab) {
            for (UpdateSegmentationListener listener : UpdateSegmentationListeners) {
                listener.onUpdateSegmentation(tab);
            }
        }

        public void setPosition(int n) {
            position = n;
            Position.setText(position + ".");
        }

        public JPanel getPanel() {
            return step;
        }

        public int getPosition() {
            return position;
        }

        public String getUID() {
            return key;
        }

        public ArrayList getVariables() {
            return settings;
        }

        @Override
        public void onChangeSetup(ArrayList al2) {

            String MethodText = new String();

            ArrayList<ArrayList> morphologies = (ArrayList<ArrayList>) al2.get(4);

            MethodText = "Approach: " + (String) (al2.get(1)) + " on channel " + ((int) al2.get(2) + 1) + ".  Measuring in " + morphologies.size() + " morphologies.";

            Object.setText(al2.get(0) + ": " + (String) (al2.get(1)) + ", by channel: " + ((int) al2.get(2) + 1));
            Comment.setText(MethodText);

            RebuildPanelObject();

            settings = new ArrayList();

            if (settings.size() > 0) {
                settings.clear();
            }
            settings.addAll(al2);

            notifyUpdateSegmentationListeners(1);
        }

        @Override
        public void onUpdateImage(ImagePlus imp) {
            this.mbs.setProcessedImage(imp);
            this.mbs.updateNewImage();
        }

        @Override
        public void protocolUpdated(ArrayList<ArrayList> al) {
        }

    }

    //GUI table manipulation
    private void deleteProcessStep(int position) {
        ProcessingStepsList.remove(position - 1);
        UpdatePositionProcessing(position);
        ProcessingStepsList.trimToSize();
        PreProcessingStepsPanel.removeAll();
        PreProcessingStepsPanel.setLayout(PreProcessingLayout);

        if (ProcessingStepsList.size() < 0) {
        } else {
            RebuildPanelProcessing();
        }

        if (ProcessingStepsList.size() < 5) {
            AddStep_Preprocessing.setEnabled(true);
        }

        PreProcessingStepsPanel.repaint();
        this.notifyRepaintTabListeners();
    }

    public void rebuildProcessingGUI() {
        PreProcessingStepsPanel.removeAll();
        PreProcessingStepsPanel.setLayout(PreProcessingLayout);

        if (ProcessingStepsList.size() < 0) {
        } else {
            RebuildPanelProcessing();
        }

        if (ProcessingStepsList.size() < 5) {
            AddStep_Preprocessing.setEnabled(true);
        }

        PreProcessingStepsPanel.repaint();
        this.notifyRepaintTabListeners();
    }

    private void deleteObjectStep(int position) {
        ObjectStepsList.remove(position - 1);
        UpdatePositionObject(position);
        ObjectStepsList.trimToSize();
        ObjectStepsPanel.removeAll();
        ObjectStepsPanel.setLayout(ObjectLayout);

        if (ObjectStepsList.size() < 0) {
        } else {
            RebuildPanelObject();
        }
        if (5 >= ObjectStepsList.size()) {
            AddStep_Object.setEnabled(true);
        }

        ObjectStepsPanel.repaint();
        this.notifyRepaintTabListeners();
    }

    public ArrayList<ProcessStepBlockGUI> getProcessStepsList() {
        return this.ProcessingStepsList;
    }

    public void setProcessStepsList(ArrayList<ProcessStepBlockGUI> ProcessingStepsList) {
        if (this.ProcessingStepsList.size() > 1) {
            ArrayList<ProcessStepBlockGUI> temp = new ArrayList<ProcessStepBlockGUI>();
            temp.add(this.ProcessingStepsList.get(0));
            this.ProcessingStepsList.clear();
            this.ProcessingStepsList.addAll(temp);
            this.ProcessingStepsList.addAll(ProcessingStepsList);
            UpdatePositionProcessing(0);
            this.RebuildPanelProcessing();
        } else {

        }
    }

    public ArrayList<ProcessStepBlockGUI> cloneProcessStepsList() {
        ArrayList<ProcessStepBlockGUI> export = new ArrayList<ProcessStepBlockGUI>();
        for (int i = 1; i < ProcessingStepsList.size(); i++) {
            try {
                ProcessStepBlockGUI psbg = (ProcessStepBlockGUI) ProcessingStepsList.get(i).clone();
                export.add(psbg);
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(SingleImageProcessing.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return export;
    }

    public ArrayList<ObjectStepBlockGUI> getObjectStepsList() {
        return this.ObjectStepsList;
    }

    public ArrayList getProProcessingProtocol() {
        return extractSteps(ProcessingStepsList, PROCESSBLOCKS);
    }

    public void addProcessSteps(ArrayList ProcessingStepsList) {
        ProcessingStepsList.trimToSize();
        this.ProcessingStepsList.addAll(ProcessingStepsList);
        UpdatePositionProcessing(0);
        this.RebuildPanelProcessing();
    }

    public void setObjectSteps(ArrayList ObjectStepList) {
        this.ObjectStepsList = ObjectStepList;
        this.RebuildPanelObject();
    }

    public ImagePlus getOriginalImage() {
        return this.OriginalImage;
    }

    public ImagePlus getThumbnailImage() {
        return this.ThumbnailImage;
    }

    public void RebuildPanelProcessing() {
        ProcessStepBlockGUI sb;
        ListIterator litr = ProcessingStepsList.listIterator();
        while (litr.hasNext()) {
            sb = (ProcessStepBlockGUI) litr.next();
            sb.setPosition(ProcessingStepsList.indexOf(sb) + 1);
            PreProcessingStepsPanel.add(sb.getPanel());
        }
    }

    public void RebuildPanelObject() {
        ObjectStepBlockGUI sb;
        ListIterator litr = ObjectStepsList.listIterator();
        while (litr.hasNext()) {
            sb = (ObjectStepBlockGUI) litr.next();
            sb.setPosition(ObjectStepsList.indexOf(sb) + 1);
            ObjectStepsPanel.add(sb.getPanel());
        }
    }

    public void UpdatePositionProcessing(int position) {
        ProcessStepBlockGUI sb;
        for (int i = 1; i < ProcessingStepsList.size(); i++) {
            sb = (ProcessStepBlockGUI) ProcessingStepsList.get(i);
            sb.setPosition(i);
            ProcessingStepsList.set(i, sb);
        }
    }

    public void UpdatePositionObject(int position) {
        ObjectStepBlockGUI sb;
        for (int i = 0; i < ObjectStepsList.size(); i++) {
            sb = (ObjectStepBlockGUI) ObjectStepsList.get(i);
            sb.setPosition(i);
            ObjectStepsList.set(i, sb);
        }
    }

    private void executeProcessing() {

        ProgressComment.setText("Processing image data...");

        ArrayList<ArrayList> protocol = new ArrayList<>();

        //get the arraylist, decide the nubmer of steps, by .steps to do and whether this is a preview or final by .type
        protocol = extractSteps(ProcessingStepsList, PROCESSBLOCKS);

        if (protocol.size() > 0) {

            ProcessedImage = OriginalImage.duplicate();
            ImageProcessingProcessor ipp = new ImageProcessingProcessor(ProcessedImage, protocol);
            ipp.addPropertyChangeListener(this);
            ipp.execute();

        } else {

            OriginalImage.deleteRoi();
            ProcessedImage = OriginalImage.duplicate();
            OriginalImage.restoreRoi();

            ImagePlus ProcessedShow = new ImagePlus("Processed");
            ProcessedShow = UtilityMethods.makeThumbnail(ProcessedImage);
            ProcessedShow.setTitle(this.getName() + "_Processed");

            ProgressComment.setText("Processing complete...");
            ProcessedShow.show();
        }

        if (ObjectStepsList.size() > 0) {
            notifyUpdatedImageListeners(ProcessedImage);
        }
    }

    private synchronized void executeSegmentation() {

        ArrayList<ArrayList> protocol = new ArrayList();
        protocol = extractSteps(ObjectStepsList, OBJECTBLOCKS);
        segmentationCount = 0;

        for (int i = 0; i < ObjectStepsList.size(); i++) {
            String key = ((ObjectStepBlockGUI) ObjectStepsList.get(i)).getUID() + "_" + System.currentTimeMillis();

            System.out.println("PROFILING: Segmentation on dataset: " + key);

            SegmentationProcessor sp = new SegmentationProcessor(key, ProcessedImage, (ArrayList) protocol.get(i));
            sp.addPropertyChangeListener(this);
            segmentationProcessors.add(sp);
            sp.execute();
        }
    }

    private void executeMeasuring(String key, ArrayList<MicroObject> vols, ArrayList protocol) {

        System.out.println("PROFILING: Measuring on dataset: " + key);

        MeasurementProcessor mp = new MeasurementProcessor(key, ProcessedImage, vols, protocol);
        mp.addPropertyChangeListener(this);
        measurementProcessors.add(mp);

        mp.execute();
    }

    private void executeExploring(String key, ArrayList<MicroObject> vols, ArrayList measurements, ArrayList headers, ArrayList headerLabels) {

        //ij.ImageStack[] test = vols.get(0).exportObjImage(); for debugging
        ExplorerProcessor ep = new ExplorerProcessor(key, key, ProcessedImage, vols, measurements, headers, headerLabels);
        ep.addPropertyChangeListener(this);
        explorerProcessors.add(ep);
        ep.execute();
    }

//       private DefaultXYZDataset createXYZDataset(ArrayList alVolumes, int x, int y, int l) {
//        
//        DefaultXYZDataset result = new DefaultXYZDataset();
//        int counter = 0;
//
//        double[] xCorrected = new double[alVolumes.size()];
//        double[] yCorrected = new double[alVolumes.size()];
//        double[] lCorrected = new double[alVolumes.size()];
//
//        ListIterator litr = alVolumes.listIterator();
//
//        while (litr.hasNext()) {
//            MicroObjectModel volume = (MicroObjectModel) litr.next();
//            xCorrected[counter] = processPosition(x, volume).doubleValue();
//            yCorrected[counter] = processPosition(y, volume).doubleValue();
//            if(l > 0){
//            lCorrected[counter] = processPosition(l, volume).doubleValue();
//            }else{
//                lCorrected[counter] = 0;
//            }
//
//            counter++;
//
//        }
//
//        double[][] series = new double[][]{xCorrected, yCorrected, lCorrected};
//        result.addSeries("first", series);
//
//        return result;
//    }
//    private Number processPosition(int a, MicroObjectModel volume) {
////        ArrayList ResultsPointer = volume.getResultPointer();
////        int size = ResultsPointer.size();
//        if (a <= 10) {
//            //System.out.println("PROFILING: Object " + volume.getSerialID() + ", value:" + (Number) volume.getAnalysisMaskVolume()[a]);
//            return (Number) volume.getAnalysisMaskVolume()[a];
//        } else {
//            int row = ((a) / 11) - 1;
//            int column = a % 11;
//            return (Number) volume.getAnalysisResultsVolume()[row][column];
//        }
//    }
    static public ArrayList extractSteps(ArrayList sb_al, int blocktype) {

        ArrayList<ArrayList> Result = new ArrayList<ArrayList>();

        if (blocktype == PROCESSBLOCKS) {

            ProcessStepBlockGUI ppsb;

            ListIterator<Object> litr = sb_al.listIterator();
            while (litr.hasNext()) {
                ppsb = (ProcessStepBlockGUI) litr.next();
                if (!(ppsb.Comment.getText()).equals("New Image")) {
                    Result.add(ppsb.getVariables());
                }
            }
        }

        if (blocktype == OBJECTBLOCKS) {

            ObjectStepBlockGUI osb;
            ListIterator<Object> litr = sb_al.listIterator();
            while (litr.hasNext()) {
                osb = (ObjectStepBlockGUI) litr.next();

                Result.add(osb.getVariables());
            }
        }

        return Result;
    }

    public void setImage(ImagePlus imp, int tab) {
        if ((tab + 1) == this.tab && (ProcessingStepsList.size() == 0)) {

            OriginalImage = imp.duplicate();
            OriginalImage.setOpenAsHyperStack(true);

            this.ThumbnailImage = UtilityMethods.makeThumbnail(imp);

            Channels = new ArrayList<String>();
            for (int i = 0; i <= OriginalImage.getNChannels() - 1; i++) {
                Channels.add("Channel_" + i);
            }
            AddStep_Preprocessing.setEnabled(true);

            ProcessStepBlockGUI block = new ProcessStepBlockGUI(imp.getTitle(), "New Image", ImageBlockBackground, batch, ThumbnailImage, OriginalImage, this.Channels, ProtocolManagerMulti.PROCESS, ProcessingStepsList, getBlockPosition());

            PreProcessingStepsPanel.setLayout(PreProcessingLayout);
            PreProcessingStepsPanel.add(block.getPanel());
            PreProcessingStepsPanel.repaint();
            //pack();

            ProcessingStepsList.add(block);

            if (ProcessingStepsList.size() <= 4) {

                AddStep_Preprocessing.setEnabled(true);
            }
            if (ProcessingStepsList.size() >= 5) {

                AddStep_Preprocessing.setEnabled(false);
            }
        }
    }

    private int getBlockPosition() {
        int position;
        if (ProcessingStepsList.isEmpty()) {
            position = 1;
        } else {
            position = ProcessingStepsList.size() + 1;
        }
        return position;
    }

    public void setSelectedTab(boolean b) {
        this.selected = b;
    }

    public void addListener(AnalysisStartListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners(int i) {
        for (AnalysisStartListener listener : listeners) {
            listener.onStartButton(i);
        }
    }

    public void addUpdatedProtocolListener(UpdatedProtocolListener listener) {
        UpdatedProtocolListeners.add(listener);
    }

    private void notifyUpdatedProtcolListeners(ArrayList<ArrayList> al) {
        for (UpdatedProtocolListener listener : UpdatedProtocolListeners) {
            listener.protocolUpdated(al);
        }
    }

    public void addRequestImageListener(RequestImageListener listener) {
        RequestImageListeners.add(listener);
    }

    private void notifyRequestImageListeners(int tab) {
        for (RequestImageListener listener : RequestImageListeners) {
            listener.onRequest(tab);
        }
    }

    public void addRepaintTabListener(RepaintTabListener listener) {
        RepaintTabListeners.add(listener);
    }

    private void notifyRepaintTabListeners() {
        for (RepaintTabListener listener : RepaintTabListeners) {
            listener.repaintTab();
        }
    }

    public void addUpdatedImageListener(UpdatedImageListener listener) {
        UpdatedImageListeners.add(listener);
    }

    private void notifyUpdatedImageListeners(ImagePlus imp) {
        for (UpdatedImageListener listener : UpdatedImageListeners) {
            listener.onUpdateImage(imp);
        }
    }

    @Override
    public void onSelect(ImagePlus imp, int tab) {

        if (tab == tab && ProcessingStepsList.size() == 0) {
            imp.deleteRoi();
            this.OriginalImage = imp;
            imp.restoreRoi();

            ProgressComment.setText("Image loading...");
            VTEAProgressBar.setIndeterminate(true);
            //ThumbnailImage = imp.duplicate();
            ThumbnailImage = new ImagePlus();

            //System.out.println("PROFILING: made it to SIP line 1489");
            ThumbnailImage = UtilityMethods.makeThumbnail(imp);

            Channels = new ArrayList<String>();
            for (int i = 0; i <= OriginalImage.getNChannels() - 1; i++) {
                Channels.add("Channel_" + (i + 1));
            }

            AddStep_Preprocessing.setEnabled(true);
            imageLoaded = true;

            //System.out.println("PROFILING: made it to SIP line 1501");
            ProcessStepBlockGUI block = new ProcessStepBlockGUI(imp.getTitle(), "New Image", ImageBlockBackground, this.batch, this.ThumbnailImage, this.OriginalImage, this.Channels, ProtocolManagerMulti.PROCESS, ProcessingStepsList, getBlockPosition());
            block.addRebuildPanelListener(this);
            PreProcessingStepsPanel.setLayout(PreProcessingLayout);
            PreProcessingStepsPanel.add(block.getPanel());
            PreProcessingStepsPanel.repaint();
            //pack();

            ProcessingStepsList.add(block);

            //System.out.println("PROFILING: made it to SIP line 1512");
            if (ProcessingStepsList.size() <= 4) {

                AddStep_Preprocessing.setEnabled(true);
            }
            if (ProcessingStepsList.size() >= 5) {

                AddStep_Preprocessing.setEnabled(false);
            }
        }
        notifyRepaintTabListeners();
        OpenImage.setEnabled(false);
        DeleteAllSteps_PreProcessing.setEnabled(true);
        AddStep_Preprocessing.setEnabled(true);
        PreProcessingGo.setEnabled(true);
        VTEAProgressBar.setIndeterminate(false);
        ProgressComment.setText("Image loaded...");
        //System.out.println("PROFILING: made it to SIP line 1526");
    }

};
