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
package vteaexploration;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.Macro;
import ij.Prefs;
import ij.Undo;
import ij.WindowManager;
import ij.gui.ColorChooser;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.ImageRoi;
import ij.gui.MessageDialog;
import ij.gui.Overlay;
import ij.gui.Plot;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.ProfilePlot;
import ij.gui.Roi;
import ij.gui.RoiProperties;
import ij.gui.ShapeRoi;
import ij.gui.TextRoi;
import ij.gui.Toolbar;
import ij.gui.YesNoCancelDialog;
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.io.RoiDecoder;
import ij.io.RoiEncoder;
import ij.io.SaveDialog;
import ij.macro.Interpreter;
import ij.macro.MacroRunner;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.Colors;
import ij.plugin.OverlayLabels;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.Filler;
import ij.plugin.filter.ThresholdToSelection;
import ij.plugin.frame.Recorder;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.text.TextWindow;
import ij.util.StringSorter;
import ij.util.Tools;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.List;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author vinfrais
 */
public class microGateManager extends javax.swing.JFrame implements ActionListener, ItemListener, MouseListener, MouseWheelListener, ListSelectionListener {

    public static final String LOC_KEY = "manager.loc";
    static final int xOffset = 800, yOffset = 0;

    private static final int BUTTONS = 11;
    private static final int DRAW = 0, FILL = 1, LABEL = 2;
    private static final int SHOW_ALL = 0, SHOW_NONE = 1, LABELS = 2, NO_LABELS = 3;
    private static final int MENU = 0, COMMAND = 1;
    private static int rows = 15;
    private static int lastNonShiftClick = -1;
    private static boolean allowMultipleSelections = true;
    private static String moreButtonLabel = "More " + '\u00bb';
    private Panel panel;
    private static Frame instance;
    private static int colorIndex = 4;
    private JList list;
    private DefaultListModel listModel;
    private Hashtable rois = new Hashtable();
    private boolean canceled;
    private boolean macro;
    private boolean ignoreInterrupts;
    private PopupMenu pm;
    private Button moreButton, colorButton;
    private Checkbox showAllCheckbox = new Checkbox("Show All", false);
    private Checkbox labelsCheckbox = new Checkbox("Labels", false);

    private static Color backgroundWindow = ImageJ.backgroundColor;

    private static boolean measureAll = true;
    private static boolean onePerSlice = true;
    private static boolean restoreCentered;
    private int prevID;
    private boolean noUpdateMode;
    private int defaultLineWidth = 1;
    private Color defaultColor;
    private boolean firstTime = true;
    private int[] selectedIndexes;
    private boolean appendResults;
    private ResultsTable mmResults;
    private int imageID;
    private int activeimageID;

    private ArrayList<ActivePlotWindowListener> listenersPlotWindow = new ArrayList<ActivePlotWindowListener>();

//    public microGateManager_old(int height, int x, int y) {
//        super("Gate Manager", 
//              true, //resizable
//              false, //closable
//              true, //maximizable
//              false);//iconifiable
//
//
//	
//        //...Create the GUI and put it in the window...
//
//        //...Then set the window size or call pack...
//        //setSize(400,height);
//
//        //Set the window's location.
//        setLocation(x, y);
//        list = new JList();
//        showWindow();
//    }
//    
//    public microGateManager_old(boolean hideWindow) {
//        super("microFLOW Gate Manager");
//        list = new JList();
//        listModel = new DefaultListModel();
//        list.setModel(listModel);
//    
//    }
    /**
     * Creates new form microGateManager
     */
    public microGateManager() {
//        list = new JList();
        listModel = new DefaultListModel();
//        list.setModel(listModel);
        initComponents();
        GuiSetup();

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(32767, 10));
        jPanel1 = new javax.swing.JPanel();
        saveGateResult = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        listGates = new javax.swing.JList();
        addGate = new javax.swing.JButton();
        renameGate = new javax.swing.JButton();
        deleteGate = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("VTC-Gate Manager");
        setBackground(getBackgroundColor());
        setResizable(false);

        jPanel1.setBackground(vtea._vtea.BACKGROUND);

        saveGateResult.setBackground(vtea._vtea.BUTTONBACKGROUND);
        saveGateResult.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/document-save-2_24.png"))); // NOI18N
        saveGateResult.setToolTipText("Save selected gates");
        saveGateResult.setPreferredSize(vtea._vtea.SMALLBUTTONSIZE);
        saveGateResult.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveGateResultActionPerformed(evt);
            }
        });

        listGates.setModel(listModel);
        listGates.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listGatesMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                listGatesMousePressed(evt);
            }
        });
        jScrollPane3.setViewportView(listGates);

        addGate.setBackground(vtea._vtea.BUTTONBACKGROUND);
        addGate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/list-add-3 2.png"))); // NOI18N
        addGate.setToolTipText("Add gate");
        addGate.setPreferredSize(vtea._vtea.SMALLBUTTONSIZE);
        addGate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addGateActionPerformed(evt);
            }
        });

        renameGate.setBackground(vtea._vtea.BUTTONBACKGROUND);
        renameGate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit-rename.png"))); // NOI18N
        renameGate.setToolTipText("Rename selected gate");
        renameGate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renameGateActionPerformed(evt);
            }
        });

        deleteGate.setBackground(vtea._vtea.BUTTONBACKGROUND);
        deleteGate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit-delete-6_24.png"))); // NOI18N
        deleteGate.setToolTipText("Delete selected gate");
        deleteGate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteGateActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(saveGateResult, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, addGate, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, deleteGate, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, renameGate, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .add(jSeparator1)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 427, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, Short.MAX_VALUE))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(addGate, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 33, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(renameGate, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(deleteGate, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(saveGateResult, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void renameGateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renameGateActionPerformed
        rename(null);
    }//GEN-LAST:event_renameGateActionPerformed

    private void saveGateResultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveGateResultActionPerformed
        save();
    }//GEN-LAST:event_saveGateResultActionPerformed

    private void addGateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addGateActionPerformed
        runCommand("add");
    }//GEN-LAST:event_addGateActionPerformed

    private void listGatesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listGatesMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_listGatesMouseClicked

    private void deleteGateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteGateActionPerformed
        delete(false);
    }//GEN-LAST:event_deleteGateActionPerformed

    private void listGatesMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listGatesMousePressed
        //give select the image and the index of the gate
        select(getImage(), listGates.getSelectedIndex());

        //select();        // TODO add your handling code here:
    }//GEN-LAST:event_listGatesMousePressed

    /**
     * @param args the command line arguments
     */
    private void GuiSetup() {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(microGateManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(microGateManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(microGateManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(microGateManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                //microGateManager().setVisible(true);
            }
        });
    }

    void add(boolean shiftKeyDown, boolean altKeyDown) {
        if (shiftKeyDown) {
            addAndDraw(altKeyDown);
        } else if (altKeyDown) {
            addRoi(true);
        } else {
            addRoi(false);
        }
    }

    /**
     * Adds the specified ROI.
     */
    public void addRoi(Roi roi) {
        addRoi(roi, false, null, -1);
    }

    boolean addRoi(boolean promptForName) {
        return addRoi(null, promptForName, null, -1);
    }

    boolean addRoi(Roi roi, boolean promptForName, Color color, int lineWidth) {

        ImagePlus imp = roi == null ? getImage() : WindowManager.getCurrentImage();

        if (!imp.getTitle().contains("Plot")) {
           // IJ.log("***NOT a PLOT image***");
            return false;
        }

        IJ.log("title: " + imp.getTitle());

        if (roi == null) {
            if (imp == null) {
                return false;
            }
            roi = imp.getRoi();
            if (roi == null) {
                error("The active plot does not have a selection.");
                return false;
            }
        }
        if ((roi instanceof PolygonRoi) && ((PolygonRoi) roi).getNCoordinates() == 0) {
            return false;
        }
        if (color == null && roi.getStrokeColor() != null) {
            color = roi.getStrokeColor();
        } else if (color == null && defaultColor != null) {
            color = defaultColor;
        }
        if (lineWidth < 0) {
            int sw = (int) roi.getStrokeWidth();
            lineWidth = sw > 1 ? sw : defaultLineWidth;
        }
        if (lineWidth > 100) {
            lineWidth = 1;
        }
        int n = getCount();
        if (n > 0 && !IJ.isMacro() && imp != null) {
            // check for duplicate
            String label = (String) listModel.getElementAt(n - 1);
            Roi roi2 = (Roi) rois.get(label);
            if (roi2 != null) {
                int slice2 = getSliceNumber(roi2, label);
                if (roi.equals(roi2) && (slice2 == -1 || slice2 == imp.getCurrentSlice()) && imp.getID() == prevID && !Interpreter.isBatchMode()) {
                    return false;
                }
            }
        }
        prevID = imp != null ? imp.getID() : 0;
        String name = roi.getName();
        if (isStandardName(name)) {
            name = null;
        }
        String label = name != null ? name : getLabel(imp, roi, -1);
        if (promptForName) {
            label = promptForName(label);
        }
        if (label == null) {
            return false;
        }
        label = getUniqueName(label);
        listModel.addElement(label);
        roi.setName(label);
        Roi roiCopy = (Roi) roi.clone();
        if (lineWidth > 1) {
            roiCopy.setStrokeWidth(lineWidth);
        }
        if (color != null) {
            roiCopy.setStrokeColor(color);
        }
        rois.put(label, roiCopy);
        updateShowAll();
        if (record()) {
            recordAdd(defaultColor, defaultLineWidth);
        }
        return true;
    }

    void recordAdd(Color color, int lineWidth) {
        if (Recorder.scriptMode()) {
            Recorder.recordCall("rm.addRoi(imp.getRoi());");
        } else if (color != null && lineWidth == 1) {
            Recorder.recordString("microGateManager(\"Add\", \"" + getHex(color) + "\");\n");
        } else if (lineWidth > 1) {
            Recorder.recordString("microGateManager(\"Add\", \"" + getHex(color) + "\", " + lineWidth + ");\n");
        } else {
            Recorder.record("microGateManager", "Add");
        }
    }

    String getHex(Color color) {
        if (color == null) {
            color = ImageCanvas.getShowAllColor();
        }
        String hex = Integer.toHexString(color.getRGB());
        if (hex.length() == 8) {
            hex = hex.substring(2);
        }
        return hex;
    }

    /**
     * Adds the specified ROI to the list. The third argument ('n') will be used
     * to form the first part of the ROI label if it is >= 0.
     */
    public void add(ImagePlus imp, Roi roi, int n) {
        if (roi == null) {
            return;
        }
        String label = roi.getName();
        String label2 = label;
        if (label == null) {
            label = getLabel(imp, roi, n);
        } else {
            label = label + "-" + n;
        }
        if (label == null) {
            return;
        }
        listModel.addElement(label);
        if (label2 != null) {
            roi.setName(label2);
        } else {
            roi.setName(label);
        }
        rois.put(label, (Roi) roi.clone());
    }

    boolean isStandardName(String name) {
        if (name == null) {
            return false;
        }
        boolean isStandard = false;
        int len = name.length();
        if (len >= 14 && name.charAt(4) == '-' && name.charAt(9) == '-') {
            isStandard = true;
        } else if (len >= 17 && name.charAt(5) == '-' && name.charAt(11) == '-') {
            isStandard = true;
        } else if (len >= 9 && name.charAt(4) == '-') {
            isStandard = true;
        } else if (len >= 11 && name.charAt(5) == '-') {
            isStandard = true;
        }
        return isStandard;
    }

    String getLabel(ImagePlus imp, Roi roi, int n) {
        Rectangle r = roi.getBounds();
        int xc = r.x + r.width / 2;
        int yc = r.y + r.height / 2;
        if (n >= 0) {
            xc = yc;
            yc = n;
        }
        if (xc < 0) {
            xc = 0;
        }
        if (yc < 0) {
            yc = 0;
        }
        int digits = 4;
        String xs = "" + xc;
        if (xs.length() > digits) {
            digits = xs.length();
        }
        String ys = "" + yc;
        if (ys.length() > digits) {
            digits = ys.length();
        }
        if (digits == 4 && imp != null && imp.getStackSize() >= 10000) {
            digits = 5;
        }
        xs = "000000" + xc;
        ys = "000000" + yc;
        String label = ys.substring(ys.length() - digits) + "-" + xs.substring(xs.length() - digits);
        if (imp != null && imp.getStackSize() > 1) {
            boolean hasPosition = false;
            boolean hyperstack = imp != null && imp.isHyperStack();
            int slice = roi.getPosition();
            if (slice == 0) {
                slice = imp.getCurrentSlice();
            } else {
                hasPosition = true;
            }
            if (!hasPosition) {
                hasPosition = hyperstack && (roi.getZPosition() > 0 || roi.getTPosition() > 0);
            }
            String zs = "000000" + slice;
            label = zs.substring(zs.length() - digits) + "-" + label;
            if (!hasPosition) {
                if (hyperstack) {
                    if (imp.getNSlices() > 1) {
                        roi.setPosition(0, imp.getSlice(), 0);
                    } else if (imp.getNFrames() > 1) {
                        roi.setPosition(0, 0, imp.getFrame());
                    }
                } else {
                    if (imp.getStackSize() == 1) {
                        roi.setPosition(0);
                    } else {
                        roi.setPosition(slice);
                    }
                }
            }
        }
        return label;
    }

    void addAndDraw(boolean altKeyDown) {
        if (altKeyDown) {
            if (!addRoi(true)) {
                return;
            }
        } else if (!addRoi(false)) {
            return;
        }
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp != null) {
            Undo.setup(Undo.COMPOUND_FILTER, imp);
            IJ.run(imp, "Draw", "slice");
            Undo.setup(Undo.COMPOUND_FILTER_DONE, imp);
        }
        if (record()) {
            Recorder.record("microGateManager", "Add & Draw");
        }
    }

    boolean delete(boolean replacing) {
        IJ.log("Deleting...");
        int count = getCount();
        if (count == 0) {
            return error("The list is empty.");
        }
        int index[] = getSelectedIndexes();
        if (index.length == 0 || (replacing && count > 1)) {
            String msg = "Delete all items on the list?";
            if (replacing) {
                msg = "Replace items on the list?";
            }
            canceled = false;
            if (!IJ.isMacro() && !macro) {
                YesNoCancelDialog d = new YesNoCancelDialog(instance, "Gate Manager", msg);
                if (d.cancelPressed()) {
                    canceled = true;
                    return false;
                }
                if (!d.yesPressed()) {
                    return false;
                }
            }
            index = getAllIndexes();
        }
        if (count == index.length && !replacing) {
            rois.clear();
            listModel.removeAllElements();
        } else {
            for (int i = count - 1; i >= 0; i--) {
                boolean delete = false;
                for (int j = 0; j < index.length; j++) {
                    if (index[j] == i) {
                        delete = true;
                    }
                }
                if (delete) {
                    rois.remove((String) listModel.getElementAt(i));
                    listModel.remove(i);
                }
            }
        }

        ImagePlus imp = WindowManager.getCurrentImage();
        if (count > 1 && index.length == 1 && imp != null) {
            imp.deleteRoi();
        }
        updateShowAll();
        if (record()) {
            Recorder.record("microGateManager", "Delete");
        }
        return true;
    }

    boolean update(boolean clone) {
        ImagePlus imp = getImage();
        if (imp == null) {
            return false;
        }
        ImageCanvas ic = imp.getCanvas();
        boolean showingAll = ic != null && ic.getShowAllROIs();
        Roi roi = imp.getRoi();
        if (roi == null) {
            error("The active image does not have a selection.");
            return false;
        }
        int index = listGates.getSelectedIndex();
        if (index < 0 && !showingAll) {
            return error("Exactly one item in the list must be selected.");
        }
        if (index >= 0) {
            String name = (String) listModel.getElementAt(index);
            rois.remove(name);
            if (clone) {
                Roi roi2 = (Roi) roi.clone();
                if (imp.getStackSize() > 1 && Prefs.showAllSliceOnly) {
                    roi2.setPosition(imp.getCurrentSlice());
                }
                roi.setName(name);
                roi2.setName(name);
                rois.put(name, roi2);
            } else {
                rois.put(name, roi);
            }
        }
        if (record()) {
            Recorder.record("microGateManager", "Update");
        }
        updateShowAll();
        return true;
    }

    boolean rename(String name2) {
        int index = listGates.getSelectedIndex();
        if (index < 0) {
            return error("Exactly one item in the list must be selected.");
        }
        String name = (String) listModel.getElementAt(index);
        if (name2 == null) {
            name2 = promptForName(name);
        }
        if (name2 == null) {
            return false;
        }
        if (name2.equals(name)) {
            return false;
        }
        name2 = getUniqueName(name2);
        Roi roi = (Roi) rois.get(name);
        if (roi == null) {
            return false;
        }
        rois.remove(name);
        roi.setName(name2);
        int position = getSliceNumber(name2);
        if (position > 0 && roi.getCPosition() == 0 && roi.getZPosition() == 0 && roi.getTPosition() == 0) {
            roi.setPosition(position);
        }
        rois.put(name2, roi);
        listModel.setElementAt(name2, index);
        listGates.setSelectedIndex(index);
        if (Prefs.useNamesAsLabels && labelsCheckbox.getState()) {
            ImagePlus imp = WindowManager.getCurrentImage();
            if (imp != null) {
                imp.draw();
            }
        }
        if (record()) {
            Recorder.record("microGateManager", "Rename", name2);
        }
        return true;
    }

    String promptForName(String name) {
        GenericDialog gd = new GenericDialog("Gate Manager");
        gd.addStringField("Rename As:", name, 20);
        gd.showDialog();
        if (gd.wasCanceled()) {
            return null;
        } else {
            return gd.getNextString();
        }
    }

    boolean restore(ImagePlus imp, int index, boolean setSlice) {
        String label = (String) listModel.getElementAt(index);
        Roi roi = (Roi) rois.get(label);
        if (imp == null || roi == null) {
            return false;
        }
        if (setSlice) {
            int c = roi.getCPosition();
            int z = roi.getZPosition();
            int t = roi.getTPosition();
            boolean hyperstack = imp.isHyperStack();
            //IJ.log("restore: "+hyperstack+" "+c+" "+z+" "+t);
            if (hyperstack && (c > 0 || z > 0 || t > 0)) {
                imp.setPosition(c, z, t);
            } else {
                int n = getSliceNumber(roi, label);
                if (n >= 1 && n <= imp.getStackSize()) {
                    if (hyperstack) {
                        if (imp.getNSlices() > 1 && n < imp.getNSlices()) {
                            imp.setPosition(imp.getC(), n, imp.getT());
                        } else if (imp.getNFrames() > 1 && n < imp.getNFrames()) {
                            imp.setPosition(imp.getC(), imp.getZ(), n);
                        } else {
                            imp.setPosition(n);
                        }
                    } else {
                        imp.setSlice(n);
                    }
                }
            }
        }
        if (showAllCheckbox.getState() && !restoreCentered && !noUpdateMode) {
            roi.setImage(null);
            imp.setRoi(roi);
            return true;
        }
        Roi roi2 = (Roi) roi.clone();
        Rectangle r = roi2.getBounds();
        int width = imp.getWidth(), height = imp.getHeight();
        if (restoreCentered) {
            ImageCanvas ic = imp.getCanvas();
            if (ic != null) {
                Rectangle r1 = ic.getSrcRect();
                Rectangle r2 = roi2.getBounds();
                roi2.setLocation(r1.x + r1.width / 2 - r2.width / 2, r1.y + r1.height / 2 - r2.height / 2);
            }
        }
        if (r.x >= width || r.y >= height || (r.x + r.width) < 0 || (r.y + r.height) < 0) {
            roi2.setLocation((width - r.width) / 2, (height - r.height) / 2);
        }
        if (noUpdateMode) {
            imp.setRoi(roi2, false);
            noUpdateMode = false;
        } else {
            imp.setRoi(roi2, true);
        }
        return true;
    }

    boolean restoreWithoutUpdate(int index) {
        noUpdateMode = true;
        return restore(getImage(), index, false);
    }

    /**
     * Returns the slice number associated with the specified name, or -1 if the
     * name does not include a slice number.
     */
    public int getSliceNumber(String label) {
        int slice = -1;
        if (label.length() >= 14 && label.charAt(4) == '-' && label.charAt(9) == '-') {
            slice = (int) Tools.parseDouble(label.substring(0, 4), -1);
        } else if (label.length() >= 17 && label.charAt(5) == '-' && label.charAt(11) == '-') {
            slice = (int) Tools.parseDouble(label.substring(0, 5), -1);
        } else if (label.length() >= 20 && label.charAt(6) == '-' && label.charAt(13) == '-') {
            slice = (int) Tools.parseDouble(label.substring(0, 6), -1);
        }
        return slice;
    }

    /**
     * Returns the slice number associated with the specified ROI or name, or -1
     * if the ROI or name does not include a slice number.
     */
    int getSliceNumber(Roi roi, String label) {
        int slice = roi != null ? roi.getPosition() : -1;
        if (slice == 0) {
            slice = -1;
        }
        if (slice == -1) {
            slice = getSliceNumber(label);
        }
        return slice;
    }

    void open(String path) {
        Macro.setOptions(null);
        String name = null;
        if (path == null || path.equals("")) {
            OpenDialog od = new OpenDialog("Open Selection(s)...", "");
            String directory = od.getDirectory();
            name = od.getFileName();
            if (name == null) {
                return;
            }
            path = directory + name;
        }
        if (Recorder.record && !Recorder.scriptMode()) {
            Recorder.record("microGateManager", "Open", path);
        }
        if (path.endsWith(".zip")) {
            openZip(path);
            return;
        }
        Opener o = new Opener();
        if (name == null) {
            name = o.getName(path);
        }
        Roi roi = o.openRoi(path);
        if (roi != null) {
            if (name.endsWith(".roi")) {
                name = name.substring(0, name.length() - 4);
            }
            name = getUniqueName(name);
            listModel.addElement(name);
            rois.put(name, roi);
        }
        updateShowAll();
    }

    // Modified on 2005/11/15 by Ulrik Stervbo to only read .roi files and to not empty the current list
    void openZip(String path) {
        ZipInputStream in = null;
        ByteArrayOutputStream out;
        int nRois = 0;
        try {
            in = new ZipInputStream(new FileInputStream(path));
            byte[] buf = new byte[1024];
            int len;
            ZipEntry entry = in.getNextEntry();
            while (entry != null) {
                String name = entry.getName();
                if (name.endsWith(".roi")) {
                    out = new ByteArrayOutputStream();
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.close();
                    byte[] bytes = out.toByteArray();
                    RoiDecoder rd = new RoiDecoder(bytes, name);
                    Roi roi = rd.getRoi();
                    if (roi != null) {
                        name = name.substring(0, name.length() - 4);
                        name = getUniqueName(name);
                        listModel.addElement(name);
                        rois.put(name, roi);
                        nRois++;
                    }
                }
                entry = in.getNextEntry();
            }
            in.close();
        } catch (IOException e) {
            error(e.toString());
        }
        if (nRois == 0) {
            error("This ZIP archive does not appear to contain \".roi\" files");
        }
        updateShowAll();
    }

//simplify into gates 1-etc
    String getUniqueName(String name) {
        String name2 = name;
        int n = 1;
        Roi roi2 = (Roi) rois.get(name2);
        while (roi2 != null) {
            roi2 = (Roi) rois.get(name2);
            if (roi2 != null) {
                int lastDash = name2.lastIndexOf("-");
                if (lastDash != -1 && name2.length() - lastDash < 5) {
                    name2 = name2.substring(0, lastDash);
                }
                name2 = name2 + "-" + n;
                n++;
            }
            roi2 = (Roi) rois.get(name2);
        }
        return name2;
    }

    boolean save() {
        if (getCount() == 0) {
            return error("The selection list is empty.");
        }
        int[] indexes = getSelectedIndexes();
        if (indexes.length == 0) {
            indexes = getAllIndexes();
        }
        if (indexes.length > 1) {
            return saveMultiple(indexes, null);
        }
        String name = (String) listModel.getElementAt(indexes[0]);
        Macro.setOptions(null);
        SaveDialog sd = new SaveDialog("Save Selection...", name, ".roi");
        String name2 = sd.getFileName();
        if (name2 == null) {
            return false;
        }
        String dir = sd.getDirectory();
        Roi roi = (Roi) rois.get(name);
        rois.remove(name);
        if (!name2.endsWith(".roi")) {
            name2 = name2 + ".roi";
        }
        String newName = name2.substring(0, name2.length() - 4);
        rois.put(newName, roi);
        roi.setName(newName);
        listModel.setElementAt(newName, indexes[0]);
        RoiEncoder re = new RoiEncoder(dir + name2);
        try {
            re.write(roi);
        } catch (IOException e) {
            IJ.error("microGateManager", e.getMessage());
        }
        return true;
    }

    boolean saveMultiple(int[] indexes, String path) {
        Macro.setOptions(null);
        if (path == null) {
            SaveDialog sd = new SaveDialog("Save ROIs...", "RoiSet", ".zip");
            String name = sd.getFileName();
            if (name == null) {
                return false;
            }
            if (!(name.endsWith(".zip") || name.endsWith(".ZIP"))) {
                name = name + ".zip";
            }
            String dir = sd.getDirectory();
            path = dir + name;
        }
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(path));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(zos));
            RoiEncoder re = new RoiEncoder(out);
            for (int i = 0; i < indexes.length; i++) {
                String label = (String) listModel.getElementAt(indexes[i]);
                Roi roi = (Roi) rois.get(label);
                if (IJ.debugMode) {
                    //IJ.log("saveMultiple: " + i + "  " + label + "  " + roi);
                }
                if (roi == null) {
                    continue;
                }
                if (!label.endsWith(".roi")) {
                    label += ".roi";
                }
                zos.putNextEntry(new ZipEntry(label));
                re.write(roi);
                out.flush();
            }
            out.close();
        } catch (IOException e) {
            error("" + e);
            return false;
        }
        if (record()) {
            Recorder.record("microGateManager", "Save", path);
        }
        return true;
    }

    boolean measure(int mode) {
        ImagePlus imp = getImage();
        if (imp == null) {
            return false;
        }
        int[] indexes = getSelectedIndexes();
        if (indexes.length == 0) {
            indexes = getAllIndexes();
        }
        if (indexes.length == 0) {
            return false;
        }
        boolean allSliceOne = true;
        for (int i = 0; i < indexes.length; i++) {
            String label = (String) listModel.getElementAt(indexes[i]);
            Roi roi = (Roi) rois.get(label);
            if (getSliceNumber(roi, label) > 1) {
                allSliceOne = false;
            }
        }
        int measurements = Analyzer.getMeasurements();
        if (imp.getStackSize() > 1) {
            Analyzer.setMeasurements(measurements | Measurements.SLICE);
        }
        int currentSlice = imp.getCurrentSlice();
        for (int i = 0; i < indexes.length; i++) {
            if (restore(getImage(), indexes[i], !allSliceOne)) {
                IJ.run("Measure");
            } else {
                break;
            }
        }
        imp.setSlice(currentSlice);
        Analyzer.setMeasurements(measurements);
        if (indexes.length > 1) {
            IJ.run("Select None");
        }
        if (record()) {
            Recorder.record("microGateManager", "Measure");
        }
        return true;
    }

    /*
     void showIndexes(int[] indexes) {
     for (int i=0; i<indexes.length; i++) {
     String label = (String) listModel.getElementAt(indexes[i]);
     Roi roi = (Roi)rois.get(label);
     IJ.log(i+" "+roi.getName());
     }
     }
     */

    /* This method performs measurements for several ROI's in a stack
     and arranges the results with one line per slice.  By constast, the 
     measure() method produces several lines per slice.  The results 
     from multiMeasure() may be easier to import into a spreadsheet 
     program for plotting or additional analysis. Based on the multi() 
     method in Bob Dougherty's Multi_Measure plugin
     (http://www.optinav.com/Multi-Measure.htm).
     */
    boolean multiMeasure(String cmd) {
        ImagePlus imp = getImage();
        if (imp == null) {
            return false;
        }
        int[] indexes = getSelectedIndexes();
        if (indexes.length == 0) {
            indexes = getAllIndexes();
        }
        if (indexes.length == 0) {
            return false;
        }
        int measurements = Analyzer.getMeasurements();

        int nSlices = imp.getStackSize();
        if (cmd != null) {
            appendResults = cmd.contains("append") ? true : false;
        }
        if (IJ.isMacro()) {
            if (nSlices > 1) {
                measureAll = true;
            }
            onePerSlice = true;
        } else {
            GenericDialog gd = new GenericDialog("Multi Measure");
            if (nSlices > 1) {
                gd.addCheckbox("Measure all " + nSlices + " slices", measureAll);
            }
            gd.addCheckbox("One Row Per Slice", onePerSlice);
            gd.addCheckbox("Append results", appendResults);
            int columns = getColumnCount(imp, measurements) * indexes.length;
            String str = nSlices == 1 ? "this option" : "both options";
            gd.setInsets(10, 25, 0);
            gd.addMessage(
                    "Enabling " + str + " will result\n"
                    + "in a table with " + columns + " columns."
            );
            gd.showDialog();
            if (gd.wasCanceled()) {
                return false;
            }
            if (nSlices > 1) {
                measureAll = gd.getNextBoolean();
            }
            onePerSlice = gd.getNextBoolean();
            appendResults = gd.getNextBoolean();
        }
        if (!measureAll) {
            nSlices = 1;
        }
        int currentSlice = imp.getCurrentSlice();

        if (!onePerSlice) {
            int measurements2 = nSlices > 1 ? measurements | Measurements.SLICE : measurements;
            ResultsTable rt = new ResultsTable();
            Analyzer analyzer = new Analyzer(imp, measurements2, rt);
            for (int slice = 1; slice <= nSlices; slice++) {
                if (nSlices > 1) {
                    imp.setSliceWithoutUpdate(slice);
                }
                for (int i = 0; i < indexes.length; i++) {
                    if (restoreWithoutUpdate(indexes[i])) {
                        analyzer.measure();
                    } else {
                        break;
                    }
                }
            }
            rt.show("Results");
            if (nSlices > 1) {
                imp.setSlice(currentSlice);
            }
            return true;
        }

        Analyzer aSys = new Analyzer(imp); // System Analyzer
        ResultsTable rtSys = Analyzer.getResultsTable();
        ResultsTable rtMulti = new ResultsTable();
        if (appendResults && mmResults != null) {
            rtMulti = mmResults;
        }
        rtSys.reset();
        //Analyzer aMulti = new Analyzer(imp, measurements, rtMulti); //Private Analyzer

        for (int slice = 1; slice <= nSlices; slice++) {
            int sliceUse = slice;
            if (nSlices == 1) {
                sliceUse = currentSlice;
            }
            imp.setSliceWithoutUpdate(sliceUse);
            rtMulti.incrementCounter();
            if ((Analyzer.getMeasurements() & Measurements.LABELS) != 0) {
                rtMulti.addLabel("Label", imp.getTitle());
            }
            int roiIndex = 0;
            for (int i = 0; i < indexes.length; i++) {
                if (restoreWithoutUpdate(indexes[i])) {
                    roiIndex++;
                    aSys.measure();
                    for (int j = 0; j <= rtSys.getLastColumn(); j++) {
                        float[] col = rtSys.getColumn(j);
                        String head = rtSys.getColumnHeading(j);
                        String suffix = "" + roiIndex;
                        Roi roi = imp.getRoi();
                        if (roi != null) {
                            String name = roi.getName();
                            if (name != null && name.length() > 0 && (name.length() < 9 || !Character.isDigit(name.charAt(0)))) {
                                suffix = "(" + name + ")";
                            }
                        }
                        if (head != null && col != null && !head.equals("Slice")) {
                            rtMulti.addValue(head + suffix, rtSys.getValue(j, rtSys.getCounter() - 1));
                        }
                    }
                } else {
                    break;
                }
            }
        }
        mmResults = (ResultsTable) rtMulti.clone();
        rtMulti.show("Results");

        imp.setSlice(currentSlice);
        if (indexes.length > 1) {
            IJ.run("Select None");
        }
        if (record()) {
            String arg = appendResults ? " append" : "";
            Recorder.record("microGateManager", "Multi Measure" + arg);
        }
        return true;
    }

    int getColumnCount(ImagePlus imp, int measurements) {
        ImageStatistics stats = imp.getStatistics(measurements);
        ResultsTable rt = new ResultsTable();
        Analyzer analyzer = new Analyzer(imp, measurements, rt);
        analyzer.saveResults(stats, null);
        int count = 0;
        for (int i = 0; i <= rt.getLastColumn(); i++) {
            float[] col = rt.getColumn(i);
            String head = rt.getColumnHeading(i);
            if (head != null && col != null) {
                count++;
            }
        }
        return count;
    }

    void multiPlot() {
        ImagePlus imp = getImage();
        if (imp == null) {
            return;
        }
        int[] indexes = getSelectedIndexes();
        if (indexes.length == 0) {
            indexes = getAllIndexes();
        }
        int n = indexes.length;
        if (n == 0) {
            return;
        }
        Color[] colors = {Color.blue, Color.green, Color.magenta, Color.red, Color.cyan, Color.yellow};
        if (n > colors.length) {
            colors = new Color[n];
            double c = 0;
            double inc = 150.0 / n;
            for (int i = 0; i < n; i++) {
                colors[i] = new Color((int) c, (int) c, (int) c);
                c += inc;
            }
        }
        int currentSlice = imp.getCurrentSlice();
        double[][] x = new double[n][];
        double[][] y = new double[n][];
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double fixedMin = ProfilePlot.getFixedMin();
        double fixedMax = ProfilePlot.getFixedMax();
        boolean freeYScale = fixedMin == 0.0 && fixedMax == 0.0;
        if (!freeYScale) {
            minY = fixedMin;
            maxY = fixedMax;
        }
        int maxX = 0;
        Calibration cal = imp.getCalibration();
        double xinc = cal.pixelWidth;
        for (int i = 0; i < indexes.length; i++) {
            if (!restore(getImage(), indexes[i], true)) {
                break;
            }
            Roi roi = imp.getRoi();
            if (roi == null) {
                break;
            }
            if (roi.isArea() && roi.getType() != Roi.RECTANGLE) {
                IJ.run(imp, "Area to Line", "");
            }
            ProfilePlot pp = new ProfilePlot(imp, Prefs.verticalProfile || IJ.altKeyDown());
            y[i] = pp.getProfile();
            if (y[i] == null) {
                break;
            }
            if (y[i].length > maxX) {
                maxX = y[i].length;
            }
            if (freeYScale) {
                double[] a = Tools.getMinMax(y[i]);
                if (a[0] < minY) {
                    minY = a[0];
                }
                if (a[1] > maxY) {
                    maxY = a[1];
                }
            }
            double[] xx = new double[y[i].length];
            for (int j = 0; j < xx.length; j++) {
                xx[j] = j * xinc;
            }
            x[i] = xx;
        }
        String xlabel = "Distance (" + cal.getUnits() + ")";
        Plot plot = new Plot("Profiles", xlabel, "Value", x[0], y[0]);
        plot.setLimits(0, maxX * xinc, minY, maxY);
        for (int i = 1; i < indexes.length; i++) {
            plot.setColor(colors[i]);
            if (x[i] != null) {
                plot.addPoints(x[i], y[i], Plot.LINE);
            }
        }
        plot.setColor(colors[0]);
        if (x[0] != null) {
            plot.show();
        }
        imp.setSlice(currentSlice);
        if (indexes.length > 1) {
            IJ.run("Select None");
        }
        if (record()) {
            Recorder.record("microGateManager", "Multi Plot");
        }
    }

    boolean drawOrFill(int mode) {
        int[] indexes = getSelectedIndexes();
        if (indexes.length == 0) {
            indexes = getAllIndexes();
        }
        ImagePlus imp = WindowManager.getCurrentImage();
        imp.deleteRoi();
        ImageProcessor ip = imp.getProcessor();
        ip.setColor(Toolbar.getForegroundColor());
        ip.snapshot();
        Undo.setup(Undo.FILTER, imp);
        Filler filler = mode == LABEL ? new Filler() : null;
        int slice = imp.getCurrentSlice();
        for (int i = 0; i < indexes.length; i++) {
            String name = (String) listModel.getElementAt(indexes[i]);
            Roi roi = (Roi) rois.get(name);
            int type = roi.getType();
            if (roi == null) {
                continue;
            }
            if (mode == FILL && (type == Roi.POLYLINE || type == Roi.FREELINE || type == Roi.ANGLE)) {
                mode = DRAW;
            }
            int slice2 = getSliceNumber(roi, name);
            if (slice2 >= 1 && slice2 <= imp.getStackSize()) {
                imp.setSlice(slice2);
                ip = imp.getProcessor();
                ip.setColor(Toolbar.getForegroundColor());
                if (slice2 != slice) {
                    Undo.reset();
                }
            }
            switch (mode) {
                case DRAW:
                    roi.drawPixels(ip);
                    break;
                case FILL:
                    ip.fill(roi);
                    break;
                case LABEL:
                    roi.drawPixels(ip);
                    filler.drawLabel(imp, ip, i + 1, roi.getBounds());
                    break;
            }
        }
        if (record() && (mode == DRAW || mode == FILL)) {
            Recorder.record("microGateManager", mode == DRAW ? "Draw" : "Fill");
        }
        if (showAllCheckbox.getState()) {
            runCommand("show none");
        }
        imp.updateAndDraw();
        return true;
    }

    void setProperties(Color color, int lineWidth, Color fillColor) {
        boolean showDialog = color == null && lineWidth == -1 && fillColor == null;
        int[] indexes = getSelectedIndexes();
        if (indexes.length == 0) {
            indexes = getAllIndexes();
        }
        int n = indexes.length;
        if (n == 0) {
            return;
        }
        Roi rpRoi = null;
        String rpName = null;
        Font font = null;
        int justification = TextRoi.LEFT;
        double opacity = -1;
        if (showDialog) {
            String label = (String) listModel.getElementAt(indexes[0]);
            rpRoi = (Roi) rois.get(label);
            if (n == 1) {
                fillColor = rpRoi.getFillColor();
                rpName = rpRoi.getName();
            }
            if (rpRoi.getStrokeColor() == null) {
                rpRoi.setStrokeColor(Roi.getColor());
            }
            rpRoi = (Roi) rpRoi.clone();
            if (n > 1) {
                rpRoi.setName("range: " + (indexes[0] + 1) + "-" + (indexes[n - 1] + 1));
            }
            rpRoi.setFillColor(fillColor);
            RoiProperties rp = new RoiProperties("Properties", rpRoi);
            if (!rp.showDialog()) {
                return;
            }
            lineWidth = (int) rpRoi.getStrokeWidth();
            defaultLineWidth = lineWidth;
            color = rpRoi.getStrokeColor();
            fillColor = rpRoi.getFillColor();
            defaultColor = color;
            if (rpRoi instanceof TextRoi) {
                font = ((TextRoi) rpRoi).getCurrentFont();
                justification = ((TextRoi) rpRoi).getJustification();
            }
            if (rpRoi instanceof ImageRoi) {
                opacity = ((ImageRoi) rpRoi).getOpacity();
            }
        }
        ImagePlus imp = WindowManager.getCurrentImage();
        if (n == getCount() && n > 1 && !IJ.isMacro()) {
            GenericDialog gd = new GenericDialog("Gate Manager");
            gd.addMessage("Apply changes to all " + n + " selections?");
            gd.showDialog();
            if (gd.wasCanceled()) {
                return;
            }
        }
        for (int i = 0; i < n; i++) {
            String label = (String) listModel.getElementAt(indexes[i]);
            Roi roi = (Roi) rois.get(label);
            //IJ.log("set "+color+"  "+lineWidth+"  "+fillColor);
            if (color != null) {
                roi.setStrokeColor(color);
            }
            if (lineWidth >= 0) {
                roi.setStrokeWidth(lineWidth);
            }
            roi.setFillColor(fillColor);
            if (roi != null && (roi instanceof TextRoi)) {
                roi.setImage(imp);
                if (font != null) {
                    ((TextRoi) roi).setCurrentFont(font);
                }
                ((TextRoi) roi).setJustification(justification);
                roi.setImage(null);
            }
            if (roi != null && (roi instanceof ImageRoi) && opacity != -1) {
                ((ImageRoi) roi).setOpacity(opacity);
            }
        }
        if (rpRoi != null && rpName != null && !rpRoi.getName().equals(rpName)) {
            rename(rpRoi.getName());
        }
        ImageCanvas ic = imp != null ? imp.getCanvas() : null;
        Roi roi = imp != null ? imp.getRoi() : null;
        boolean showingAll = ic != null && ic.getShowAllROIs();
        if (roi != null && (n == 1 || !showingAll)) {
            if (lineWidth >= 0) {
                roi.setStrokeWidth(lineWidth);
            }
            if (color != null) {
                roi.setStrokeColor(color);
            }
            if (fillColor != null) {
                roi.setFillColor(fillColor);
            }
            if (roi != null && (roi instanceof TextRoi)) {
                ((TextRoi) roi).setCurrentFont(font);
                ((TextRoi) roi).setJustification(justification);
            }
            if (roi != null && (roi instanceof ImageRoi) && opacity != -1) {
                ((ImageRoi) roi).setOpacity(opacity);
            }
        }
        if (lineWidth > 1 && !showingAll && roi == null) {
            showAll(SHOW_ALL);
            showingAll = true;
        }
        if (imp != null) {
            imp.draw();
        }
        if (record()) {
            if (fillColor != null) {
                Recorder.record("microGateManager", "Set Fill Color", Colors.colorToString(fillColor));
            } else {
                Recorder.record("microGateManager", "Set Color", Colors.colorToString(color != null ? color : Color.red));
                Recorder.record("microGateManager", "Set Line Width", lineWidth);
            }
        }
    }

    void flatten() {
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }
        ImageCanvas ic = imp.getCanvas();
        if ((ic != null && ic.getShowAllList() == null) && imp.getOverlay() == null && imp.getRoi() == null) {
            error("Image does not have an overlay or ROI");
        } else {
            IJ.doCommand("Flatten"); // run Image>Flatten in separate thread
        }
    }

    public boolean getDrawLabels() {
        return labelsCheckbox.getState();
    }

    void combine() {
        ImagePlus imp = getImage();
        if (imp == null) {
            return;
        }
        int[] indexes = getSelectedIndexes();
        if (indexes.length == 1) {
            error("More than one item must be selected, or none");
            return;
        }
        if (indexes.length == 0) {
            indexes = getAllIndexes();
        }
        int nPointRois = 0;
        for (int i = 0; i < indexes.length; i++) {
            Roi roi = (Roi) rois.get((String) listModel.getElementAt(indexes[i]));
            if (roi.getType() == Roi.POINT) {
                nPointRois++;
            } else {
                break;
            }
        }
        if (nPointRois == indexes.length) {
            combinePoints(imp, indexes);
        } else {
            combineRois(imp, indexes);
        }
        if (record()) {
            Recorder.record("microGateManager", "Combine");
        }
    }

    void combineRois(ImagePlus imp, int[] indexes) {
        ShapeRoi s1 = null, s2 = null;
        ImageProcessor ip = null;
        for (int i = 0; i < indexes.length; i++) {
            Roi roi = (Roi) rois.get((String) listModel.getElementAt(indexes[i]));
            if (!roi.isArea()) {
                if (ip == null) {
                    ip = new ByteProcessor(imp.getWidth(), imp.getHeight());
                }
                roi = convertLineToPolygon(roi, ip);
            }
            if (s1 == null) {
                if (roi instanceof ShapeRoi) {
                    s1 = (ShapeRoi) roi;
                } else {
                    s1 = new ShapeRoi(roi);
                }
                if (s1 == null) {
                    return;
                }
            } else {
                if (roi instanceof ShapeRoi) {
                    s2 = (ShapeRoi) roi;
                } else {
                    s2 = new ShapeRoi(roi);
                }
                if (s2 == null) {
                    continue;
                }
                s1.or(s2);
            }
        }
        if (s1 != null) {
            imp.setRoi(s1);
        }
    }

    Roi convertLineToPolygon(Roi roi, ImageProcessor ip) {
        if (roi == null) {
            return null;
        }
        ip.resetRoi();
        ip.setColor(0);
        ip.fill();
        ip.setColor(255);
        if (roi.getType() == Roi.LINE && roi.getStrokeWidth() > 1) {
            ip.fillPolygon(roi.getPolygon());
        } else {
            roi.drawPixels(ip);
        }
        //new ImagePlus("ip", ip.duplicate()).show();
        ip.setThreshold(255, 255, ImageProcessor.NO_LUT_UPDATE);
        ThresholdToSelection tts = new ThresholdToSelection();
        return tts.convert(ip);
    }

    void combinePoints(ImagePlus imp, int[] indexes) {
        int n = indexes.length;
        Polygon[] p = new Polygon[n];
        int points = 0;
        for (int i = 0; i < n; i++) {
            Roi roi = (Roi) rois.get((String) listModel.getElementAt(indexes[i]));
            p[i] = roi.getPolygon();
            points += p[i].npoints;
        }
        if (points == 0) {
            return;
        }
        int[] xpoints = new int[points];
        int[] ypoints = new int[points];
        int index = 0;
        for (int i = 0; i < p.length; i++) {
            for (int j = 0; j < p[i].npoints; j++) {
                xpoints[index] = p[i].xpoints[j];
                ypoints[index] = p[i].ypoints[j];
                index++;
            }
        }
        imp.setRoi(new PointRoi(xpoints, ypoints, xpoints.length));
    }

    void and() {
        ImagePlus imp = getImage();
        if (imp == null) {
            return;
        }
        int[] indexes = getSelectedIndexes();
        if (indexes.length == 1) {
            error("More than one item must be selected, or none");
            return;
        }
        if (indexes.length == 0) {
            indexes = getAllIndexes();
        }
        ShapeRoi s1 = null, s2 = null;
        for (int i = 0; i < indexes.length; i++) {
            Roi roi = (Roi) rois.get((String) listModel.getElementAt(indexes[i]));
            if (roi == null || !roi.isArea()) {
                continue;
            }
            if (s1 == null) {
                if (roi instanceof ShapeRoi) {
                    s1 = (ShapeRoi) roi.clone();
                } else {
                    s1 = new ShapeRoi(roi);
                }
                if (s1 == null) {
                    return;
                }
            } else {
                if (roi instanceof ShapeRoi) {
                    s2 = (ShapeRoi) roi.clone();
                } else {
                    s2 = new ShapeRoi(roi);
                }
                if (s2 == null) {
                    continue;
                }
                s1.and(s2);
            }
        }
        if (s1 != null) {
            imp.setRoi(s1);
        }
        if (record()) {
            Recorder.record("microGateManager", "AND");
        }
    }

    void xor() {
        ImagePlus imp = getImage();
        if (imp == null) {
            return;
        }
        int[] indexes = getSelectedIndexes();
        if (indexes.length == 1) {
            error("More than one item must be selected, or none");
            return;
        }
        if (indexes.length == 0) {
            indexes = getAllIndexes();
        }
        ShapeRoi s1 = null, s2 = null;
        for (int i = 0; i < indexes.length; i++) {
            Roi roi = (Roi) rois.get((String) listModel.getElementAt(indexes[i]));
            if (!roi.isArea()) {
                continue;
            }
            if (s1 == null) {
                if (roi instanceof ShapeRoi) {
                    s1 = (ShapeRoi) roi.clone();
                } else {
                    s1 = new ShapeRoi(roi);
                }
                if (s1 == null) {
                    return;
                }
            } else {
                if (roi instanceof ShapeRoi) {
                    s2 = (ShapeRoi) roi.clone();
                } else {
                    s2 = new ShapeRoi(roi);
                }
                if (s2 == null) {
                    continue;
                }
                s1.xor(s2);
            }
        }
        if (s1 != null) {
            imp.setRoi(s1);
        }
        if (record()) {
            Recorder.record("microGateManager", "XOR");
        }
    }

    void addParticles() {
        String err = IJ.runMacroFile("ij.jar:AddParticles", null);
        if (err != null && err.length() > 0) {
            error(err);
        }
    }

    void sort() {
        int n = rois.size();
        if (n == 0) {
            return;
        }
        String[] labels = new String[n];
        int index = 0;
        for (Enumeration en = rois.keys(); en.hasMoreElements();) {
            labels[index++] = (String) en.nextElement();
        }
        listModel.clear();
        StringSorter.sort(labels);
        for (int i = 0; i < labels.length; i++) {
            listModel.addElement(labels[i]);
        }
        if (record()) {
            Recorder.record("microGateManager", "Sort");
        }
    }

    void specify() {
        try {
            IJ.run("Specify...");
        } catch (Exception e) {
            return;
        }
        runCommand("add");
    }

    void removeSliceInfo() {
        int[] indexes = getSelectedIndexes();
        if (indexes.length == 0) {
            indexes = getAllIndexes();
        }
        for (int i = 0; i < indexes.length; i++) {
            int index = indexes[i];
            String name = (String) listModel.getElementAt(index);
            int n = getSliceNumber(name);
            if (n == -1) {
                continue;
            }
            String name2 = name.substring(5, name.length());
            name2 = getUniqueName(name2);
            Roi roi = (Roi) rois.get(name);
            rois.remove(name);
            roi.setName(name2);
            roi.setPosition(0);
            rois.put(name2, roi);
            listModel.setElementAt(name2, index);
        }
        if (record()) {
            Recorder.record("microGateManager", "Remove Slice Info");
        }
    }

    private void help() {
        String macro = "run('URL...', 'url=" + IJ.URL + "/docs/menus/analyze.html#manager');";
        new MacroRunner(macro);
    }

    private void labels() {
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp != null) {
            showAllCheckbox.setState(true);
            labelsCheckbox.setState(true);
            showAll(LABELS);
        }
        //IJ.doCommand("Labels...");
    }

    private void options() {
        Color c = ImageCanvas.getShowAllColor();
        GenericDialog gd = new GenericDialog("Options");
        //gd.addPanel(makeButtonPanel(gd), GridBagConstraints.CENTER, new Insets(5, 0, 0, 0));
        gd.addCheckbox("Associate \"Show All\" ROIs with Slices", Prefs.showAllSliceOnly);
        gd.addCheckbox("Restore ROIs centered", restoreCentered);
        gd.addCheckbox("Use ROI names as labels", Prefs.useNamesAsLabels);
        gd.showDialog();
        if (gd.wasCanceled()) {
            if (c != ImageCanvas.getShowAllColor()) {
                ImageCanvas.setShowAllColor(c);
            }
            return;
        }
        Prefs.showAllSliceOnly = gd.getNextBoolean();
        restoreCentered = gd.getNextBoolean();
        Prefs.useNamesAsLabels = gd.getNextBoolean();
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp != null) {
            Overlay overlay = imp.getOverlay();
            if (overlay != null) {
                overlay.drawNames(Prefs.useNamesAsLabels);
            }
            imp.draw();
        }
        if (record()) {
            Recorder.record("microGateManager", "Associate", Prefs.showAllSliceOnly ? "true" : "false");
            Recorder.record("microGateManager", "Centered", restoreCentered ? "true" : "false");
            Recorder.record("microGateManager", "UseNames", Prefs.useNamesAsLabels ? "true" : "false");
        }
    }

    Panel makeButtonPanel(GenericDialog gd) {
        Panel panel = new Panel();
        //buttons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        colorButton = new Button("\"Show All\" Color...");
        colorButton.addActionListener(this);
        panel.add(colorButton);
        return panel;
    }

    void setShowAllColor() {
        ColorChooser cc = new ColorChooser("\"Show All\" Color", ImageCanvas.getShowAllColor(), false);
        ImageCanvas.setShowAllColor(cc.getColor());
    }

    void split() {
        ImagePlus imp = getImage();
        if (imp == null) {
            return;
        }
        Roi roi = imp.getRoi();
        if (roi == null || roi.getType() != Roi.COMPOSITE) {
            error("Image with composite selection required");
            return;
        }
        boolean record = Recorder.record;
        Recorder.record = false;
        Roi[] rois = ((ShapeRoi) roi).getRois();
        for (int i = 0; i < rois.length; i++) {
            imp.setRoi(rois[i]);
            addRoi(false);
        }
        Recorder.record = record;
        if (record()) {
            Recorder.record("microGateManager", "Split");
        }
    }

    void showAll(int mode) {
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null) {
            return;
        }
        boolean showAll = mode == SHOW_ALL;
        if (showAll) {
            imageID = imp.getID();
        }
        if (mode == LABELS) {
            showAll = true;
            if (record()) {
                Recorder.record("microGateManager", "Show All with labels");
            }
        } else if (mode == NO_LABELS) {
            showAll = true;
            if (record()) {
                Recorder.record("microGateManager", "Show All without labels");
            }
        }
        if (showAll) {
            imp.deleteRoi();
        }
        Roi[] rois = getRoisAsArray();
        if (mode == SHOW_NONE) {
            removeOverlay(imp);
            imageID = 0;
        } else if (rois.length > 0) {
            Overlay overlay = newOverlay();
            for (int i = 0; i < rois.length; i++) {
                overlay.add(rois[i]);
            }
            setOverlay(imp, overlay);
        }
        if (record()) {
            Recorder.record("microGateManager", showAll ? "Show All" : "Show None");
        }
    }

    void updateShowAll() {
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null) {
            return;
        }
        if (showAllCheckbox.getState()) {
            Roi[] rois = getRoisAsArray();
            if (rois.length > 0) {
                Overlay overlay = newOverlay();
                for (int i = 0; i < rois.length; i++) {
                    overlay.add(rois[i]);
                }
                setOverlay(imp, overlay);
            } else {
                removeOverlay(imp);
            }
        } else {
            removeOverlay(imp);
        }
    }

    int[] getAllIndexes() {
        int count = getCount();
        int[] indexes = new int[count];
        for (int i = 0; i < count; i++) {
            indexes[i] = i;
        }
        return indexes;
    }

    ImagePlus getImage() {
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null) {
            error("There are no images open.");
            return null;
        } else {
            return imp;
        }
    }

    boolean error(String msg) {
        new MessageDialog(instance, "Gate Manager", msg);
        Macro.abort();
        return false;
    }

//    public void processWindowEvent(WindowEvent e) {
//        super.processWindowEvent(e);
//        if (e.getID()==WindowEvent.WINDOW_CLOSING) {
//            instance = null;    
//        }
//        if (!IJ.isMacro())
//            ignoreInterrupts = false;
//    }
    /**
     * Returns a reference to the microGateManager_old or null if it is not
     * open.
     */
//    public static microGateManager_old getInstance() {
//        return (microGateManager_old)instance;
//    }
    /**
     * Returns a reference to the microGateManager_old window or to the macro
     * batch mode microGateManager_old, or null if neither exists.
     */
//        remove for microGateManager_old?
//public static microGateManager_old getInstance2() {
// microGateManager_old rm = getInstance();
////        if (rm==null && IJ.isMacro())
////            rm = Interpreter.getBatchModeRoiManager();
//        return rm;
//    }
    /**
     * Returns the ROI Hashtable.
     *
     * @see #getCount
     * @see #getRoisAsArray
     */
    public Hashtable getROIs() {
        return rois;
    }

    /**
     * Obsolete
     *
     * @deprecated
     * @see #getCount
     * @see #getRoisAsArray
     * @see #getSelectedIndex
     */
    public List getList() {
        List awtList = new List();
        for (int i = 0; i < getCount(); i++) {
            awtList.add((String) listModel.getElementAt(i));
        }
        int index = getSelectedIndex();
        if (index >= 0) {
            awtList.select(index);
        }
        return awtList;
    }

    /**
     * Returns the ROI count.
     */
    public int getCount() {
        return listModel.getSize();
    }

    /**
     * Returns the index of the specified Roi, or -1 if it is not found.
     */
    public int getRoiIndex(Roi roi) {
        int n = getCount();
        for (int i = 0; i < n; i++) {
            String label = (String) listModel.getElementAt(i);
            Roi roi2 = (Roi) rois.get(label);
            if (roi == roi2) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the index of the first selected ROI or -1 if no ROI is selected.
     */
    public int getSelectedIndex() {
        return listGates.getSelectedIndex();
    }

    /**
     * Returns the ROIs as an array.
     */
    public Roi[] getRoisAsArray() {
        int n = getCount();
        Roi[] array = new Roi[n];
        for (int i = 0; i < n; i++) {
            String label = (String) listModel.getElementAt(i);
            array[i] = (Roi) rois.get(label);
        }
        return array;
    }

    public Roi getRoi() {
        int index = listGates.getSelectedIndex();
        String label = (String) listModel.getElementAt(index);

        Roi result = (Roi) rois.get(label);
        return result;
    }

    /**
     * Returns the selected ROIs as an array, or all the ROIs if none are
     * selected.
     */
    public Roi[] getSelectedRoisAsArray() {
        int[] indexes = getSelectedIndexes();
        if (indexes.length == 0) {
            indexes = getAllIndexes();
        }
        int n = indexes.length;
        Roi[] array = new Roi[n];
        for (int i = 0; i < n; i++) {
            String label = (String) listModel.getElementAt(indexes[i]);
            array[i] = (Roi) rois.get(label);
        }
        return array;
    }

    /**
     * Returns the name of the ROI with the specified index, or null if the
     * index is out of range.
     */
    public String getName(int index) {
        if (index >= 0 && index < getCount()) {
            return (String) listModel.getElementAt(index);
        } else {
            return null;
        }
    }

    /**
     * Returns the name of the ROI with the specified index. Can be called from
     * a macro using
     * <pre>call("ij.plugin.frame.RoiManager.getName", index)</pre> Returns
     * "null" if the Gate Manager is not open or index is out of range.
     */
//    public static String getName(String index) {
//        int i = (int)Tools.parseDouble(index, -1);
//        microGateManager_old instance = getInstance2();
//        if (instance!=null && i>=0 && i<instance.getCount())
//            return  (String) instance.listModel.getElementAt(i);
//        else
//            return "null";
//    }
    /**
     * Executes the Gate Manager "Add", "Add & Draw", "Update", "Delete",
     * "Measure", "Draw", "Show All", "Show None", "Fill", "Deselect", "Select
     * All", "Combine", "AND", "XOR", "Split", "Sort" or "Multi Measure"
     * command. Returns false if <code>cmd</code> is not one of these strings.
     */
    public boolean runCommand(String cmd) {
        cmd = cmd.toLowerCase();
        macro = true;
        boolean ok = true;
        if (cmd.equals("add")) {
            boolean shift = IJ.shiftKeyDown();
            boolean alt = IJ.altKeyDown();
            if (Interpreter.isBatchMode()) {
                shift = false;
                alt = false;
            }
            setRoiPosition();
            add(shift, alt);
        } else if (cmd.equals("add & draw")) {
            addAndDraw(false);
        } else if (cmd.equals("update")) {
            update(true);
        } else if (cmd.equals("update2")) {
            update(false);
        } else if (cmd.equals("delete")) {
            delete(false);
        } else if (cmd.equals("measure")) {
            measure(COMMAND);
        } else if (cmd.equals("draw")) {
            drawOrFill(DRAW);
        } else if (cmd.equals("fill")) {
            drawOrFill(FILL);
        } else if (cmd.equals("label")) {
            drawOrFill(LABEL);
        } else if (cmd.equals("and")) {
            and();
        } else if (cmd.equals("or") || cmd.equals("combine")) {
            combine();
        } else if (cmd.equals("xor")) {
            xor();
        } else if (cmd.equals("split")) {
            split();
        } else if (cmd.equals("sort")) {
            sort();
        } else if (cmd.startsWith("multi measure")) {
            multiMeasure(cmd);
        } else if (cmd.equals("multi plot")) {
            multiPlot();
        } else if (cmd.equals("show all")) {
            if (WindowManager.getCurrentImage() != null) {
                showAll(SHOW_ALL);
                showAllCheckbox.setState(true);
            }
        } else if (cmd.equals("show none")) {
            if (WindowManager.getCurrentImage() != null) {
                showAll(SHOW_NONE);
                showAllCheckbox.setState(false);
            }
        } else if (cmd.equals("show all with labels")) {
            labelsCheckbox.setState(true);
            showAll(LABELS);
            if (Interpreter.isBatchMode()) {
                IJ.wait(250);
            }
        } else if (cmd.equals("show all without labels")) {
            labelsCheckbox.setState(false);
            showAll(NO_LABELS);
            if (Interpreter.isBatchMode()) {
                IJ.wait(250);
            }
        } else if (cmd.equals("deselect") || cmd.indexOf("all") != -1) {
            if (IJ.isMacOSX()) {
                ignoreInterrupts = true;
            }
            select(-1);
            IJ.wait(50);
        } else if (cmd.equals("reset")) {
            if (IJ.isMacOSX() && IJ.isMacro()) {
                ignoreInterrupts = true;
            }
            listModel.clear();
            rois.clear();
            updateShowAll();
        } else if (cmd.equals("debug")) {
            //IJ.log("Debug: "+debugCount);
            //for (int i=0; i<debugCount; i++)
            //  IJ.log(debug[i]);
        } else if (cmd.equals("enable interrupts")) {
            ignoreInterrupts = false;
        } else if (cmd.equals("remove slice info")) {
            removeSliceInfo();
        } else {
            ok = false;
        }
        macro = false;
        return ok;
    }

    /**
     * Executes the Gate Manager "Open", "Save" or "Rename" command. Returns
     * false if <code>cmd</code> is not "Open", "Save" or "Rename", or if an
     * error occurs.
     */
    public boolean runCommand(String cmd, String name) {
        cmd = cmd.toLowerCase();
        macro = true;
        if (cmd.equals("open")) {
            open(name);
            macro = false;
            return true;
        } else if (cmd.equals("save")) {
            save(name, false);
        } else if (cmd.equals("save selected")) {
            save(name, true);
        } else if (cmd.equals("rename")) {
            rename(name);
            macro = false;
            return true;
        } else if (cmd.equals("set color")) {
            Color color = Colors.decode(name, Color.cyan);
            setProperties(color, -1, null);
            macro = false;
            return true;
        } else if (cmd.equals("set fill color")) {
            Color fillColor = Colors.decode(name, Color.cyan);
            setProperties(null, -1, fillColor);
            macro = false;
            return true;
        } else if (cmd.equals("set line width")) {
            int lineWidth = (int) Tools.parseDouble(name, 0);
            if (lineWidth >= 0) {
                setProperties(null, lineWidth, null);
            }
            macro = false;
            return true;
        } else if (cmd.equals("associate")) {
            Prefs.showAllSliceOnly = name.equals("true") ? true : false;
            macro = false;
            return true;
        } else if (cmd.equals("centered")) {
            restoreCentered = name.equals("true") ? true : false;
            macro = false;
            return true;
        } else if (cmd.equals("usenames")) {
            Prefs.useNamesAsLabels = name.equals("true") ? true : false;
            macro = false;
            if (labelsCheckbox.getState()) {
                ImagePlus imp = WindowManager.getCurrentImage();
                if (imp != null) {
                    imp.draw();
                }
            }
            return true;
        }
        return false;
    }

    private boolean save(String name, boolean saveSelected) {
        if (!name.endsWith(".zip") && !name.equals("")) {
            return error("Name must end with '.zip'");
        }
        if (getCount() == 0) {
            return error("The selection list is empty.");
        }
        int[] indexes = null;
        if (saveSelected) {
            indexes = getSelectedIndexes();
            if (indexes.length == 0) {
                indexes = getAllIndexes();
            }
        } else {
            indexes = getAllIndexes();
        }
        boolean ok = false;
        if (name.equals("")) {
            ok = saveMultiple(indexes, null);
        } else {
            ok = saveMultiple(indexes, name);
        }
        macro = false;
        return ok;
    }

    /**
     * Adds the current selection to the Gate Manager, using the specified color
     * (a 6 digit hex string) and line width.
     */
    public boolean runCommand(String cmd, String hexColor, double lineWidth) {
        setRoiPosition();
        if (hexColor == null && lineWidth == 1.0 && (IJ.altKeyDown() && !Interpreter.isBatchMode())) {
            addRoi(true);
        } else {
            Color color = hexColor != null ? Colors.decode(hexColor, Color.cyan) : null;
            addRoi(null, false, color, (int) Math.round(lineWidth));
        }
        return true;
    }

    private void setRoiPosition() {
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null) {
            return;
        }
        Roi roi = imp.getRoi();
        if (roi == null) {
            return;
        }
        if (imp.isHyperStack()) {
            roi.setPosition(imp.getC(), imp.getZ(), imp.getT());
        } else {
            roi.setPosition(imp.getCurrentSlice());
        }
    }

    /**
     * Assigns the ROI at the specified index to the current image.
     */
    public void select(int index) {
        select(null, index);
    }

    /**
     * Assigns the ROI at the specified index to 'imp'.
     */
    public void select(ImagePlus imp, int index) {
        selectedIndexes = null;
        int n = getCount();
        if (index < 0) {
            for (int i = 0; i < n; i++) {
                listGates.clearSelection();
            }
            if (record()) {
                Recorder.record("microGateManager", "Deselect");
            }
            return;
        }
        if (index >= n) {
            return;
        }
        boolean mm = listGates.getSelectionMode() == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
        if (mm) {
            listGates.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
        int delay = 1;
        long start = System.currentTimeMillis();
        while (true) {
            if (listGates.isSelectedIndex(index)) {
                break;
            }
            listGates.clearSelection();
            listGates.setSelectedIndex(index);
        }
        if (imp == null) {
            imp = WindowManager.getCurrentImage();
        }
        if (imp != null) {
            restore(imp, index, true);
        }
        if (mm) {
            listGates.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        }
    }

    public void select(int index, boolean shiftKeyDown, boolean altKeyDown) {
        if (!(shiftKeyDown || altKeyDown)) {
            select(index);
        }
        ImagePlus imp = IJ.getImage();
        if (imp == null) {
            return;
        }
        Roi previousRoi = imp.getRoi();
        if (previousRoi == null) {
            select(index);
            return;
        }
        Roi.previousRoi = (Roi) previousRoi.clone();
        String label = (String) listModel.getElementAt(index);
        Roi roi = (Roi) rois.get(label);
        if (roi != null) {
            roi.setImage(imp);
            roi.update(shiftKeyDown, altKeyDown);
        }
    }

    public void setEditMode(ImagePlus imp, boolean editMode) {
        showAllCheckbox.setState(editMode);
        labelsCheckbox.setState(editMode);
        showAll(editMode ? LABELS : SHOW_NONE);
    }

    /*
     void selectAll() {
     boolean allSelected = true;
     int count = getCount();
     for (int i=0; i<count; i++) {
     if (!listGates.isIndexSelected(i))
     allSelected = false;
     }
     if (allSelected)
     select(-1);
     else {
     for (int i=0; i<count; i++)
     if (!listGates.isSelected(i)) list.select(i);
     }
     }
     */
//    /** Overrides PlugInFrame.close(). */
//    public void close() {
//        super.close();
//        instance = null;
//        Prefs.saveLocation(LOC_KEY, getLocation());
//        if (!showAllCheckbox.getState() || IJ.macroRunning())
//            return;
//        int n = getCount();
//        ImagePlus imp = WindowManager.getCurrentImage();
//        if (imp==null)
//            return;
//        if (n>0) {
//            GenericDialog gd = new GenericDialog("microGateManager_old");
//            gd.addMessage("Save the "+n+" displayed Gates as an overlay?");
//            gd.setOKLabel("Discard");
//            gd.setCancelLabel("Save as Overlay");
//            gd.showDialog();
//            if (gd.wasCanceled())
//                moveRoisToOverlay(imp);
//            else
//                removeOverlay(imp);
//        } else
//            imp.draw();
//    }
//    
    /**
     * Moves all the ROIs to the specified image's overlay.
     */
    public void moveRoisToOverlay(ImagePlus imp) {
        if (imp == null) {
            return;
        }
        Roi[] rois = getRoisAsArray();
        int n = rois.length;
        Overlay overlay = imp.getOverlay();
        if (overlay == null) {
            overlay = newOverlay();
        }
        //ImageCanvas ic = imp.getCanvas();
        //Color color = ic!=null?ic.getShowAllColor():null;
        for (int i = 0; i < n; i++) {
            Roi roi = (Roi) rois[i].clone();
            if (!Prefs.showAllSliceOnly) {
                roi.setPosition(0);
            }
            //if (color!=null && roi.getStrokeColor()==null)
            //  roi.setStrokeColor(color);
            if (roi.getStrokeWidth() == 1) {
                roi.setStrokeWidth(0);
            }
            overlay.add(roi);
        }
        imp.setOverlay(overlay);
        setOverlay(imp, null);
    }

    /**
     * Selects multiple ROIs, where 'indexes' is an array of integers, each
     * greater than or equal to 0 and less than the value returned by
     * getCount().
     */
    /**
     * Selects multiple ROIs, where 'indexes' is an array of integers, each
     * greater than or equal to 0 and less than the value returned by
     * getCount().
     *
     * @see #getSelectedIndexes
     * @see #getSelectedRoisAsArray
     * @see #getCount
     */
    public void setSelectedIndexes(int[] indexes) {
        int count = getCount();
        if (count == 0) {
            return;
        }
        for (int i = 0; i < indexes.length; i++) {
            if (indexes[i] < 0) {
                indexes[i] = 0;
            }
            if (indexes[i] >= count) {
                indexes[i] = count - 1;
            }
        }
        selectedIndexes = indexes;
        listGates.setSelectedIndices(indexes);
    }

    /**
     * Returns an array of all of the selected indexes.
     */
    public int[] getSelectedIndexes() {
        //IJ.log("Getting Selected Indexes    getSelectedIndexes");
        if (selectedIndexes != null) {
            int[] indexes = selectedIndexes;
            selectedIndexes = null;
            return indexes;
        } else {
            return listGates.getSelectedIndices();
        }
    }

    private Overlay newOverlay() {
        Overlay overlay = OverlayLabels.createOverlay();
        overlay.drawLabels(labelsCheckbox.getState());
        if (overlay.getLabelFont() == null && overlay.getLabelColor() == null) {
            overlay.setLabelColor(Color.white);
            overlay.drawBackgrounds(true);
        }
        overlay.drawNames(Prefs.useNamesAsLabels);
        return overlay;
    }

    private void removeOverlay(ImagePlus imp) {
        if (imp != null) {
            setOverlay(imp, null);
        }
    }

    private void setOverlay(ImagePlus imp, Overlay overlay) {
        if (imp == null) {
            return;
        }
        ImageCanvas ic = imp.getCanvas();
        if (ic == null) {
            return;
        }
        ic.setShowAllList(overlay);
        imp.draw();
    }

    private Color getBackgroundColor() {

        return ImageJ.backgroundColor;
    }

    private void listRois() {
        StringBuffer sb = new StringBuffer();
        Roi[] rois = getRoisAsArray();
        for (int i = 0; i < rois.length; i++) {
            Rectangle r = rois[i].getBounds();
            String color = Colors.colorToString(rois[i].getStrokeColor());
            sb.append(i + "\t" + rois[i].getName() + "\t" + rois[i].getTypeAsString() + "\t" + (r.x + r.width / 2) + "\t" + (r.y + r.height / 2) + "\t" + color + "\n");
        }
        String headings = "Index\tName\tType\tX\tY\tColor";
        new TextWindow("ROI List", headings, sb.toString(), 400, 400);
    }

    private boolean record() {
        return Recorder.record && !IJ.isMacro();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int x = e.getX(), y = e.getY();
        if (e.isPopupTrigger() || e.isMetaDown()) {
            pm.show(e.getComponent(), x, y);
        }
    }

    /**
     *
     * @param event
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent event) {
        synchronized (this) {
            int index = listGates.getSelectedIndex();
            int rot = event.getWheelRotation();
            if (rot < -1) {
                rot = -1;
            }
            if (rot > 1) {
                rot = 1;
            }
            index += rot;
            if (index < 0) {
                index = 0;
            }
            if (index >= getCount()) {
                index = getCount();
            }
            //IJ.log(index+"  "+rot);
            select(index);
            if (IJ.isWindows()) {
                listGates.requestFocusInWindow();
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String label = e.getActionCommand();
        if (label == null) {
            return;
        }
        String command = label;
        if (command.equals("Add [t]")) {
            runCommand("add");
        } else if (command.equals("Update")) {
            update(true);
        } else if (command.equals("Delete")) {
            delete(false);
        } else if (command.equals("Rename...")) {
            rename(null);
        } else if (command.equals("Properties...")) {
            setProperties(null, -1, null);
        } else if (command.equals("Flatten [F]")) {
            flatten();
        } else if (command.equals("Measure")) {
            measure(MENU);
        } else if (command.equals("Open...")) {
            open(null);
        } else if (command.equals("Save...")) {
            save();
        } else if (command.equals("Fill")) {
            drawOrFill(FILL);
        } else if (command.equals("Draw")) {
            drawOrFill(DRAW);
        } else if (command.equals("Deselect")) {
            select(-1);
        } else if (command.equals(moreButtonLabel)) {
            Point ploc = panel.getLocation();
            Point bloc = moreButton.getLocation();
            pm.show(this, ploc.x, bloc.y);
        } else if (command.equals("OR (Combine)")) {
            combine();
        } else if (command.equals("Split")) {
            split();
        } else if (command.equals("AND")) {
            and();
        } else if (command.equals("XOR")) {
            xor();
        } else if (command.equals("Add Particles")) {
            addParticles();
        } else if (command.equals("Multi Measure")) {
            multiMeasure(null);
        } else if (command.equals("Multi Plot")) {
            multiPlot();
        } else if (command.equals("Sort")) {
            sort();
        } else if (command.equals("Specify...")) {
            specify();
        } else if (command.equals("Remove Slice Info")) {
            removeSliceInfo();
        } else if (command.equals("Labels...")) {
            labels();
        } else if (command.equals("List")) {
            listRois();
        } else if (command.equals("Help")) {
            help();
        } else if (command.equals("Options...")) {
            options();
        } else if (command.equals("\"Show All\" Color...")) {
            setShowAllColor();
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Object source = e.getSource();
        if (source == showAllCheckbox) {
            if (firstTime) {
                labelsCheckbox.setState(true);
            }
            showAll(showAllCheckbox.getState() ? SHOW_ALL : SHOW_NONE);
            firstTime = false;
            return;
        }
        if (source == labelsCheckbox) {
            if (firstTime) {
                showAllCheckbox.setState(true);
            }
            boolean editState = labelsCheckbox.getState();
            boolean showAllState = showAllCheckbox.getState();
            if (!showAllState && !editState) {
                showAll(SHOW_NONE);
            } else {
                showAll(editState ? LABELS : NO_LABELS);
                if (editState) {
                    showAllCheckbox.setState(true);
                }
            }
            firstTime = false;
            //return;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }
        if (getCount() == 0) {
            if (record()) {
                Recorder.record("microGateManager", "Deselect");
            }
            return;
        }
        int[] selected = listGates.getSelectedIndices();
        if (selected.length == 0) {
            return;
        }
        if (WindowManager.getCurrentImage() != null) {
            if (selected.length == 1) {
                restore(getImage(), selected[0], true);
            }
            if (record()) {
                String arg = Arrays.toString(selected);
                if (!arg.startsWith("[") || !arg.endsWith("]")) {
                    return;
                }
                arg = arg.substring(1, arg.length() - 1);
                arg = arg.replace(" ", "");
                if (Recorder.scriptMode()) {
                    if (selected.length == 1) {
                        Recorder.recordCall("rm.select(" + arg + ");");
                    } else {
                        Recorder.recordCall("rm.setSelectedIndexes([" + arg + "]);");
                    }
                } else {
                    if (selected.length == 1) {
                        Recorder.recordString("microGateManager(\"Select\", " + arg + ");\n");
                    } else {
                        Recorder.recordString("microGateManager(\"Select\", newArray(" + arg + "));\n");
                    }
                }
            }
        }
    }

//    public void windowActivated(WindowEvent e) {
//        super.windowActivated(e);
//        ImagePlus imp = WindowManager.getCurrentImage();
//        if (imp!=null) {
//            if (imageID!=0 && imp.getID()!=imageID) {
//                showAll(SHOW_NONE);
//                showAllCheckbox.setState(false);
//            }
//        }
//    }
//  public void onActivatePlotWindow(int sourceImageID){
//  //notifyActivatePlotWindowListeners();
//  
//  }
//    
//  
//   
//  public void addActivatePlotWindowListener(ActivePlotWindowListener listener) {
//                //listenersPlotWindow.add(listener);
//         }
//
//  private void notifyActivatePlotWindowListeners() {
//        for (ActivePlotWindowListener listener : listenersPlotWindow) {
//            IJ.log("Updating analysis table...");
//            
//            
//            //int sourceImageID = getCurrentWindowID();
//            //listener.onActivatePlotWindow(sourceImageID);
//            
//            }}
//   
//  //private int getCurrentWindowID() {return 0;}
//    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addGate;
    private javax.swing.JButton deleteGate;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JList listGates;
    private javax.swing.JButton renameGate;
    private javax.swing.JButton saveGateResult;
    // End of variables declaration//GEN-END:variables

}
