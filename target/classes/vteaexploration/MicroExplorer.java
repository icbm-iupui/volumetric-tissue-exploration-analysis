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
package vteaexploration;

import vtea.exploration.plottools.panels.VerticalLabelUI;
import vtea.exploration.plotgatetools.gates.Gate;
import vtea.exploration.plotgatetools.listeners.ChangePlotAxesListener;
import vtea.exploration.plotgatetools.listeners.MakeImageOverlayListener;
import vtea.exploration.plotgatetools.listeners.PopupMenuAxisListener;
import vtea.exploration.plotgatetools.listeners.ResetSelectionListener;
import vtea.exploration.plotgatetools.listeners.PopupMenuAxisLUTListener;
import vtea.exploration.plotgatetools.listeners.PopupMenuLUTListener;
import vtea.exploration.plottools.panels.ExplorationCenter;
import vtea.exploration.plottools.panels.PlotAxesPanels;
import vtea.exploration.plottools.panels.XYPanels;
import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.RoiListener;
import ij.measure.ResultsTable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.NullPointerException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import vtea.exploration.listeners.AxesChangeListener;
import vtea.exploration.listeners.PlotUpdateListener;
import vtea.exploration.listeners.UpdatePlotWindowListener;
import vtea.exploration.plottools.panels.DefaultPlotPanels;
import vteaobjects.MicroObject;
import vteaobjects.MicroObjectModel;

/**
 *
 * @author vinfrais
 *
 */
public class MicroExplorer extends javax.swing.JFrame implements RoiListener, PlotUpdateListener, MakeImageOverlayListener, ChangePlotAxesListener, ImageListener, ResetSelectionListener, PopupMenuAxisListener, PopupMenuLUTListener, PopupMenuAxisLUTListener, UpdatePlotWindowListener, AxesChangeListener, Runnable {

    private XYPanels DefaultXYPanels;
    private static final Dimension MainPanelSize = new Dimension(630, 640);
    private static final int POLYGONGATE = 10;
    private static final int RECTANGLEGATE = 11;
    private static final int QUADRANTGATE = 12;

    private static final int XAXIS = 0;
    private static final int YAXIS = 1;
    private static final int LUTAXIS = 2;
    private static final int POINTAXIS = 3;

    public static final int XSTART = 0;
    public static final int YSTART = 1;
    public static final int LUTSTART = 1;

    public static final int POINTSIZE = 4;

    List plotvalues;
    ExplorationCenter ec;
    PlotAxesPanels pap;
    JPanel HeaderPanel;
    ImagePlus imp;
    ImagePlus impoverlay;
    int impMode;
    String title;
    ArrayList descriptions;
    ArrayList<MicroObject> ImageGatedObjects = new ArrayList<MicroObject>();

    private boolean all = false;

    boolean imageGate = false;
    boolean noLUT = false;

    ResultsTable rt = new ResultsTable();

    JLabel xLabel;
    JLabel yLabel;
    JLabel lLabel;

    GatePercentages ResultsWindow;
    
    HashMap<Integer, JToggleButton> GateButtonsHM = new HashMap<Integer, JToggleButton>();
    HashMap<Integer, String> AvailableDataHM = new HashMap<Integer, String>();

    ArrayList<ExplorationCenter> ExplorationPanels = new ArrayList<ExplorationCenter>();
    
    PlotAxesSetup AxesSetup = new PlotAxesSetup();

    String[] sizes = {"2", "4", "8", "10", "15", "20"};

    int[] sizes_val = {6, 2, 4, 8, 10, 15, 20};

    public JMenuExploration ProcessingMenu;
    public JMenuExploration ObjectMenu;
    public JMenuExploration WorkflowMenu;

    /**
     * Creates new form MicroExplorer
     */
    public MicroExplorer() {

        Roi.addRoiListener(this);

    }

    public void process(ImagePlus imp, String title, ArrayList plotvalues, ExplorationCenter ec, PlotAxesPanels pap, ArrayList AvailableData){
        //Needs to be converted to a Factory metaphor.
        
        //Setup base dataseta
        //Available data is an arraylist of the available tags as they exist in microvolumes.
        //imp is the original image
        
        this.descriptions = AvailableData;
        
        
        
        this.imp = imp;
        this.impoverlay = imp.duplicate();
        this.impoverlay.addImageListener(this);
        
        this.impoverlay.setOpenAsHyperStack(true);
        this.impoverlay.setDisplayMode(IJ.COMPOSITE);
        this.impoverlay.setTitle(title);
        
        //Setup GUI and populate comboboxes
        
        initComponents();
        addMenuItems();

       this.title = title;

       AxesSetup.setDescriptor(this.getTitle());

        get3DProjection.setEnabled(false);

        makeOverlayImage(new ArrayList<Gate>(), ec.getSelectedObjects(), ec.getGatedObjects(impoverlay), MicroExplorer.XAXIS, MicroExplorer.YAXIS);
        AvailableDataHM = makeAvailableDataHM(descriptions);

        final SelectPlottingDataMenu PlottingPopupXaxis = new SelectPlottingDataMenu(descriptions, MicroExplorer.XAXIS);
        final SelectPlottingDataMenu PlottingPopupYaxis = new SelectPlottingDataMenu(descriptions, MicroExplorer.YAXIS);
        final SelectPlottingDataMenu PlottingPopupLUTaxis = new SelectPlottingDataMenu(descriptions, MicroExplorer.LUTAXIS);

        PlottingPopupXaxis.addPopupMenuAxisListener(this);
        PlottingPopupYaxis.addPopupMenuAxisListener(this);
        PlottingPopupLUTaxis.addPopupMenuAxisListener(this);

        GateButtonsHM.put(POLYGONGATE, this.addPolygonGate);
        GateButtonsHM.put(RECTANGLEGATE, this.addRectangularGate);
        GateButtonsHM.put(QUADRANTGATE, this.addQuadrantGate);

        xLabel = new JLabel("X_axis");
        xLabel.setFont(new Font("Lucidia Grande", Font.BOLD, 16));
        yLabel = new JLabel("Y_axis");

        yLabel.setUI(new VerticalLabelUI(false));
        yLabel.setFont(new Font("Lucidia Grande", Font.BOLD, 16));

        lLabel = new JLabel("No LUT");
        lLabel.setFont(new Font("Lucidia Grande", Font.BOLD, 16));

        xLabel.addMouseListener(new java.awt.event.MouseListener() {

            @Override
            public void mouseClicked(MouseEvent me) {
                if (SwingUtilities.isRightMouseButton(me)) {
                    PlottingPopupXaxis.show(me.getComponent(), me.getX(), me.getY());
                }
            }

            @Override
            public void mousePressed(MouseEvent me) {
            }

            @Override
            public void mouseReleased(MouseEvent me) {
            }

            @Override
            public void mouseEntered(MouseEvent me) {
            }

            @Override
            public void mouseExited(MouseEvent me) {
            }
        });
        yLabel.addMouseListener(new java.awt.event.MouseListener() {

            @Override
            public void mouseClicked(MouseEvent me) {
                if (SwingUtilities.isRightMouseButton(me)) {
                    PlottingPopupYaxis.show(me.getComponent(), me.getX(), me.getY());
                }
            }

            @Override
            public void mousePressed(MouseEvent me) {
            }

            @Override
            public void mouseReleased(MouseEvent me) {
            }

            @Override
            public void mouseEntered(MouseEvent me) {
            }

            @Override
            public void mouseExited(MouseEvent me) {
            }
        });
        lLabel.addMouseListener(new java.awt.event.MouseListener() {

            @Override
            public void mouseClicked(MouseEvent me) {
                if (SwingUtilities.isRightMouseButton(me)) {
                    PlottingPopupLUTaxis.show(me.getComponent(), me.getX(), me.getY());
                }
            }

            @Override
            public void mousePressed(MouseEvent me) {
            }

            @Override
            public void mouseReleased(MouseEvent me) {
            }

            @Override
            public void mouseEntered(MouseEvent me) {
            }

            @Override
            public void mouseExited(MouseEvent me) {
            }
        });



        DefaultXYPanels = new XYPanels(AvailableData);
        DefaultXYPanels.addChangePlotAxesListener(this);

        this.getContentPane().setBackground(new Color(255, 255, 255, 255));
        this.getContentPane().setPreferredSize(new Dimension(600, 600));

        Main.setBackground(new Color(255, 255, 255, 255));
        ec.addResetSelectionListener(this);
        ec.getXYChartPanel().addUpdatePlotWindowListener(this);
        ec.setGatedOverlay(impoverlay);
        ExplorationPanels.add(ec);

        //load default view
        setPanels(plotvalues, ExplorationPanels.get(0), pap);
        this.addAxesLabels(AvailableData.get(this.XSTART).toString(), AvailableData.get(this.YSTART).toString(), AvailableData.get(this.LUTSTART).toString());
        this.displayXYView();
        this.repaint();
        this.pack();
        this.setVisible(true);
        jComboBoxPointSize.setSelectedIndex(4);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jComboBox1 = new javax.swing.JComboBox();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        North = new javax.swing.JPanel();
        toolbarPlot = new javax.swing.JToolBar();
        jLabel15 = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        jLabel1 = new javax.swing.JLabel();
        jComboBoxXaxis = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jComboBoxYaxis = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        jComboBoxLUTPlot = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jComboBoxPointSize = new javax.swing.JComboBox();
        toolbarGate = new javax.swing.JToolBar();
        jLabel4 = new javax.swing.JLabel();
        addPolygonGate = new javax.swing.JToggleButton();
        addRectangularGate = new javax.swing.JToggleButton();
        addQuadrantGate = new javax.swing.JToggleButton();
        exportGates = new javax.swing.JButton();
        LoadGates = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jLabel3 = new javax.swing.JLabel();
        axesLabel = new javax.swing.JLabel();
        AutoScaleAxes = new javax.swing.JButton();
        AxesSettings = new javax.swing.JButton();
        SetGlobalToLocal = new javax.swing.JButton();
        UseGlobal = new javax.swing.JToggleButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        get3DProjection = new javax.swing.JButton();
        jSeparator7 = new javax.swing.JToolBar.Separator();
        BWLUT = new javax.swing.JToggleButton();
        jSeparator8 = new javax.swing.JToolBar.Separator();
        ExportGraph = new javax.swing.JButton();
        exportCSV = new javax.swing.JButton();
        importCSV = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        WestPanel = new javax.swing.JPanel();
        yTextPanel = new javax.swing.JPanel();
        FlipAxes = new javax.swing.JButton();
        SouthPanel = new javax.swing.JPanel();
        Main = new javax.swing.JPanel();
        jMenuBar = new javax.swing.JMenuBar();
        Settings = new javax.swing.JMenu();
        Edit = new javax.swing.JMenu();

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        setTitle(getTitle());
        setBackground(vtea._vtea.BACKGROUND);
        setBounds(new java.awt.Rectangle(892, 100, 0, 0));
        addContainerListener(new java.awt.event.ContainerAdapter() {
            public void componentAdded(java.awt.event.ContainerEvent evt) {
                formComponentAdded(evt);
            }
        });

        North.setMinimumSize(new java.awt.Dimension(600, 75));
        North.setPreferredSize(new java.awt.Dimension(600, 80));
        North.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 2, 5));

        toolbarPlot.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        toolbarPlot.setFloatable(false);
        toolbarPlot.setRollover(true);
        toolbarPlot.setMinimumSize(new java.awt.Dimension(600, 30));
        toolbarPlot.setPreferredSize(new java.awt.Dimension(600, 30));

        jLabel15.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel15.setText("Plot");
        jLabel15.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel15MouseClicked(evt);
            }
        });
        toolbarPlot.add(jLabel15);
        toolbarPlot.add(jSeparator4);

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        jLabel1.setLabelFor(jComboBoxXaxis);
        jLabel1.setText("X  ");
        toolbarPlot.add(jLabel1);

        jComboBoxXaxis.setModel(new DefaultComboBoxModel(this.descriptions.toArray()));
        jComboBoxXaxis.setSelectedIndex(this.XSTART);
        jComboBoxXaxis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxXaxisActionPerformed(evt);
            }
        });
        toolbarPlot.add(jComboBoxXaxis);

        jLabel2.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        jLabel2.setLabelFor(jComboBoxYaxis);
        jLabel2.setText(" Y ");
        toolbarPlot.add(jLabel2);

        jComboBoxYaxis.setModel(new DefaultComboBoxModel(this.descriptions.toArray()));
        jComboBoxYaxis.setSelectedIndex(this.YSTART);
        jComboBoxYaxis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxYaxisActionPerformed(evt);
            }
        });
        toolbarPlot.add(jComboBoxYaxis);

        jLabel5.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabel5.setLabelFor(jComboBoxLUTPlot);
        jLabel5.setText(" Color ");
        jLabel5.setToolTipText("Metric to be plotted as a \npoint's look-up table.");
        toolbarPlot.add(jLabel5);

        jComboBoxLUTPlot.setModel(new DefaultComboBoxModel(this.descriptions.toArray()));
        jComboBoxLUTPlot.setSelectedIndex(this.LUTSTART);
        jComboBoxLUTPlot.setToolTipText("Metric to be plotted as a \npoint's look-up table.");
        jComboBoxLUTPlot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxLUTPlotActionPerformed(evt);
            }
        });
        toolbarPlot.add(jComboBoxLUTPlot);

        jLabel6.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        jLabel6.setText(" Size ");
        toolbarPlot.add(jLabel6);

        jComboBoxPointSize.setModel(new DefaultComboBoxModel(this.sizes));
        jComboBoxPointSize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxPointSizeActionPerformed(evt);
            }
        });
        jComboBoxPointSize.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jComboBoxPointSizePropertyChange(evt);
            }
        });
        toolbarPlot.add(jComboBoxPointSize);

        North.add(toolbarPlot);

        toolbarGate.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        toolbarGate.setFloatable(false);
        toolbarGate.setRollover(true);
        toolbarGate.setPreferredSize(new java.awt.Dimension(600, 40));

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel4.setText("Gate  ");
        toolbarGate.add(jLabel4);

        addPolygonGate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/polygon-filled.png"))); // NOI18N
        addPolygonGate.setToolTipText("Add polygon gate");
        addPolygonGate.setFocusable(false);
        addPolygonGate.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addPolygonGate.setMaximumSize(new java.awt.Dimension(35, 40));
        addPolygonGate.setMinimumSize(new java.awt.Dimension(35, 40));
        addPolygonGate.setPreferredSize(new java.awt.Dimension(35, 40));
        addPolygonGate.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addPolygonGate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPolygonGateActionPerformed(evt);
            }
        });
        toolbarGate.add(addPolygonGate);

        addRectangularGate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/draw-rectangle.png"))); // NOI18N
        addRectangularGate.setToolTipText("Add rectangular gate");
        addRectangularGate.setFocusable(false);
        addRectangularGate.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addRectangularGate.setMaximumSize(new java.awt.Dimension(35, 40));
        addRectangularGate.setMinimumSize(new java.awt.Dimension(35, 40));
        addRectangularGate.setPreferredSize(new java.awt.Dimension(35, 40));
        addRectangularGate.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addRectangularGate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRectangularGateActionPerformed(evt);
            }
        });
        toolbarGate.add(addRectangularGate);
        addRectangularGate.getAccessibleContext().setAccessibleName("AddRectangleGate");

        addQuadrantGate.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        addQuadrantGate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/quad_sw.png"))); // NOI18N
        addQuadrantGate.setToolTipText("Add quadrant gate");
        addQuadrantGate.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        addQuadrantGate.setBorderPainted(false);
        addQuadrantGate.setFocusable(false);
        addQuadrantGate.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addQuadrantGate.setMaximumSize(new java.awt.Dimension(35, 40));
        addQuadrantGate.setMinimumSize(new java.awt.Dimension(35, 40));
        addQuadrantGate.setName(""); // NOI18N
        addQuadrantGate.setPreferredSize(new java.awt.Dimension(35, 40));
        addQuadrantGate.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addQuadrantGate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addQuadrantGateActionPerformed(evt);
            }
        });
        toolbarGate.add(addQuadrantGate);

        exportGates.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/document-save-2_24.png"))); // NOI18N
        exportGates.setToolTipText("Save gates...");
        exportGates.setFocusable(false);
        exportGates.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        exportGates.setMaximumSize(new java.awt.Dimension(35, 40));
        exportGates.setMinimumSize(new java.awt.Dimension(35, 40));
        exportGates.setName(""); // NOI18N
        exportGates.setPreferredSize(new java.awt.Dimension(35, 40));
        exportGates.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        exportGates.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportGatesActionPerformed(evt);
            }
        });
        toolbarGate.add(exportGates);

        LoadGates.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/document-open-folder_24.png"))); // NOI18N
        LoadGates.setToolTipText("Load gates...");
        LoadGates.setFocusable(false);
        LoadGates.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        LoadGates.setMaximumSize(new java.awt.Dimension(35, 40));
        LoadGates.setMinimumSize(new java.awt.Dimension(35, 40));
        LoadGates.setName(""); // NOI18N
        LoadGates.setPreferredSize(new java.awt.Dimension(35, 40));
        LoadGates.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        LoadGates.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LoadGatesActionPerformed(evt);
            }
        });
        toolbarGate.add(LoadGates);
        toolbarGate.add(jSeparator1);
        toolbarGate.add(jLabel3);

        axesLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        axesLabel.setText("Axes ");
        axesLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                axesLabelMouseClicked(evt);
            }
        });
        toolbarGate.add(axesLabel);

        AutoScaleAxes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit-undo.png"))); // NOI18N
        AutoScaleAxes.setToolTipText("Autoscale axes");
        AutoScaleAxes.setFocusable(false);
        AutoScaleAxes.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        AutoScaleAxes.setMaximumSize(new java.awt.Dimension(35, 40));
        AutoScaleAxes.setMinimumSize(new java.awt.Dimension(35, 40));
        AutoScaleAxes.setPreferredSize(new java.awt.Dimension(35, 40));
        AutoScaleAxes.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        AutoScaleAxes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AutoScaleAxesActionPerformed(evt);
            }
        });
        toolbarGate.add(AutoScaleAxes);

        AxesSettings.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/emblem-system.png"))); // NOI18N
        AxesSettings.setToolTipText("Axes settings");
        AxesSettings.setFocusable(false);
        AxesSettings.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        AxesSettings.setMaximumSize(new java.awt.Dimension(35, 40));
        AxesSettings.setMinimumSize(new java.awt.Dimension(35, 40));
        AxesSettings.setPreferredSize(new java.awt.Dimension(35, 40));
        AxesSettings.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        AxesSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AxesSettingsActionPerformed(evt);
            }
        });
        toolbarGate.add(AxesSettings);

        SetGlobalToLocal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/setGlobal copy.png"))); // NOI18N
        SetGlobalToLocal.setToolTipText("Set global axes");
        SetGlobalToLocal.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        SetGlobalToLocal.setMaximumSize(new java.awt.Dimension(35, 40));
        SetGlobalToLocal.setMinimumSize(new java.awt.Dimension(35, 40));
        SetGlobalToLocal.setPreferredSize(new java.awt.Dimension(35, 40));
        SetGlobalToLocal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SetGlobalToLocalActionPerformed(evt);
            }
        });
        toolbarGate.add(SetGlobalToLocal);

        UseGlobal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/global copy.png"))); // NOI18N
        UseGlobal.setToolTipText("Use global axes");
        UseGlobal.setMaximumSize(new java.awt.Dimension(35, 40));
        UseGlobal.setMinimumSize(new java.awt.Dimension(35, 40));
        UseGlobal.setPreferredSize(new java.awt.Dimension(35, 40));
        UseGlobal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UseGlobalActionPerformed(evt);
            }
        });
        toolbarGate.add(UseGlobal);
        toolbarGate.add(jSeparator5);

        get3DProjection.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/cube.png"))); // NOI18N
        get3DProjection.setToolTipText("Visualize in 3D");
        get3DProjection.setEnabled(false);
        get3DProjection.setMaximumSize(new java.awt.Dimension(35, 40));
        get3DProjection.setMinimumSize(new java.awt.Dimension(35, 40));
        get3DProjection.setName(""); // NOI18N
        get3DProjection.setPreferredSize(new java.awt.Dimension(35, 40));
        get3DProjection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                get3DProjectionActionPerformed(evt);
            }
        });
        toolbarGate.add(get3DProjection);
        toolbarGate.add(jSeparator7);

        BWLUT.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/removeLUT.png"))); // NOI18N
        BWLUT.setToolTipText("Remove LUT");
        BWLUT.setFocusable(false);
        BWLUT.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BWLUT.setMaximumSize(new java.awt.Dimension(35, 40));
        BWLUT.setMinimumSize(new java.awt.Dimension(35, 40));
        BWLUT.setPreferredSize(new java.awt.Dimension(35, 40));
        BWLUT.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        BWLUT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BWLUTActionPerformed(evt);
            }
        });
        toolbarGate.add(BWLUT);
        toolbarGate.add(jSeparator8);

        ExportGraph.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/insert-image-2 copy.png"))); // NOI18N
        ExportGraph.setToolTipText("Export PNG...");
        ExportGraph.setFocusable(false);
        ExportGraph.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ExportGraph.setMaximumSize(new java.awt.Dimension(35, 40));
        ExportGraph.setMinimumSize(new java.awt.Dimension(35, 40));
        ExportGraph.setName(""); // NOI18N
        ExportGraph.setPreferredSize(new java.awt.Dimension(35, 40));
        ExportGraph.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        ExportGraph.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExportGraphActionPerformed(evt);
            }
        });
        toolbarGate.add(ExportGraph);

        exportCSV.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/document-save-2_24.png"))); // NOI18N
        exportCSV.setToolTipText("Export objects...");
        exportCSV.setFocusable(false);
        exportCSV.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        exportCSV.setMaximumSize(new java.awt.Dimension(35, 40));
        exportCSV.setMinimumSize(new java.awt.Dimension(35, 40));
        exportCSV.setName(""); // NOI18N
        exportCSV.setPreferredSize(new java.awt.Dimension(35, 40));
        exportCSV.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        exportCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportCSVActionPerformed(evt);
            }
        });
        toolbarGate.add(exportCSV);

        importCSV.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/document-open-folder_24.png"))); // NOI18N
        importCSV.setToolTipText("Import...");
        importCSV.setEnabled(false);
        importCSV.setFocusable(false);
        importCSV.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        importCSV.setMaximumSize(new java.awt.Dimension(35, 40));
        importCSV.setMinimumSize(new java.awt.Dimension(35, 40));
        importCSV.setName(""); // NOI18N
        importCSV.setPreferredSize(new java.awt.Dimension(35, 40));
        importCSV.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        importCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importCSVActionPerformed(evt);
            }
        });
        toolbarGate.add(importCSV);

        jPanel1.setPreferredSize(new java.awt.Dimension(100, 100));
        toolbarGate.add(jPanel1);

        North.add(toolbarGate);

        getContentPane().add(North, java.awt.BorderLayout.NORTH);

        WestPanel.setMinimumSize(new java.awt.Dimension(44, 530));
        WestPanel.setPreferredSize(new java.awt.Dimension(44, 530));
        WestPanel.setLayout(new java.awt.BorderLayout());

        yTextPanel.setMaximumSize(new java.awt.Dimension(140, 40));
        yTextPanel.setLayout(new java.awt.BorderLayout());
        WestPanel.add(yTextPanel, java.awt.BorderLayout.CENTER);

        FlipAxes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/view-refresh-5.png"))); // NOI18N
        FlipAxes.setToolTipText("Flip X and Y axes");
        FlipAxes.setFocusable(false);
        FlipAxes.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        FlipAxes.setMaximumSize(new java.awt.Dimension(40, 40));
        FlipAxes.setMinimumSize(new java.awt.Dimension(40, 40));
        FlipAxes.setPreferredSize(new java.awt.Dimension(40, 40));
        FlipAxes.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        FlipAxes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FlipAxesActionPerformed(evt);
            }
        });
        WestPanel.add(FlipAxes, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(WestPanel, java.awt.BorderLayout.WEST);

        SouthPanel.setPreferredSize(new java.awt.Dimension(530, 30));
        getContentPane().add(SouthPanel, java.awt.BorderLayout.SOUTH);

        Main.setBackground(new java.awt.Color(255, 255, 255));
        Main.setMinimumSize(new java.awt.Dimension(600, 630));
        Main.setName(""); // NOI18N
        Main.setPreferredSize(new java.awt.Dimension(630, 600));
        Main.setLayout(new java.awt.BorderLayout());
        getContentPane().add(Main, java.awt.BorderLayout.CENTER);

        Settings.setText("Settings");
        jMenuBar.add(Settings);

        Edit.setText("Edit");
        jMenuBar.add(Edit);

        setJMenuBar(jMenuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentAdded(java.awt.event.ContainerEvent evt) {//GEN-FIRST:event_formComponentAdded
        this.setExtendedState(MAXIMIZED_BOTH);        // TODO add your handling code here:
    }//GEN-LAST:event_formComponentAdded

    private void jComboBoxXaxisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxXaxisActionPerformed
        //ec.setCustomRange(false);
        onPlotChangeRequest(jComboBoxXaxis.getSelectedIndex(), jComboBoxYaxis.getSelectedIndex(), jComboBoxLUTPlot.getSelectedIndex(), jComboBoxPointSize.getSelectedIndex(), imageGate);
    }//GEN-LAST:event_jComboBoxXaxisActionPerformed

    private void jComboBoxYaxisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxYaxisActionPerformed
        //ec.setCustomRange(false);
        onPlotChangeRequest(jComboBoxXaxis.getSelectedIndex(), jComboBoxYaxis.getSelectedIndex(), jComboBoxLUTPlot.getSelectedIndex(), jComboBoxPointSize.getSelectedIndex(), imageGate);
    }//GEN-LAST:event_jComboBoxYaxisActionPerformed

    private void addQuadrantGateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addQuadrantGateActionPerformed

        if (!(this.GateButtonsHM.get(POLYGONGATE).isEnabled()) && !(this.GateButtonsHM.get(POLYGONGATE).isEnabled())) {
            ec.stopGateSelection();
            this.activationGateTools(0);
        } else {

            this.activationGateTools(QUADRANTGATE);
            this.makeQuadrantGate();
        }
    }//GEN-LAST:event_addQuadrantGateActionPerformed

    private void addPolygonGateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPolygonGateActionPerformed

        if (!(this.GateButtonsHM.get(QUADRANTGATE).isEnabled()) && !(this.GateButtonsHM.get(QUADRANTGATE).isEnabled())) {
            ec.stopGateSelection();
            this.activationGateTools(0);
        } else {
            this.activationGateTools(POLYGONGATE);
            this.makePolygonGate();
        }
    }//GEN-LAST:event_addPolygonGateActionPerformed

    private void addRectangularGateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addRectangularGateActionPerformed
        if (!(this.GateButtonsHM.get(QUADRANTGATE).isEnabled()) && !(this.GateButtonsHM.get(QUADRANTGATE).isEnabled())) {
            ec.stopGateSelection();
            this.activationGateTools(0);
        } else {
            this.activationGateTools(RECTANGLEGATE);
            this.makeRectangleGate();
        }
    }//GEN-LAST:event_addRectangularGateActionPerformed

    private void FlipAxesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FlipAxesActionPerformed
        flipAxes();
    }//GEN-LAST:event_FlipAxesActionPerformed

    private void jComboBoxLUTPlotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxLUTPlotActionPerformed
        //ec.setCustomRange(false);
        onPlotChangeRequest(jComboBoxXaxis.getSelectedIndex(), jComboBoxYaxis.getSelectedIndex(), jComboBoxLUTPlot.getSelectedIndex(), jComboBoxPointSize.getSelectedIndex(), imageGate);
    }//GEN-LAST:event_jComboBoxLUTPlotActionPerformed

    private void jComboBoxPointSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxPointSizeActionPerformed
        //ec.setCustomRange(false);
        onPlotChangeRequest(jComboBoxXaxis.getSelectedIndex(), jComboBoxYaxis.getSelectedIndex(), jComboBoxLUTPlot.getSelectedIndex(), jComboBoxPointSize.getSelectedIndex(), imageGate);
    }//GEN-LAST:event_jComboBoxPointSizeActionPerformed

    private void jComboBoxPointSizePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jComboBoxPointSizePropertyChange

    }//GEN-LAST:event_jComboBoxPointSizePropertyChange

    private void get3DProjectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_get3DProjectionActionPerformed
        
        
    new Thread(new Runnable() {
      public void run() {
          
           ec.getZProjection();

          try {
            java.lang.Thread.sleep(100);
          }
          catch(Exception e) { }  
      }
    }).start();
        
//    new Thread(new Runnable() {
//      public void run() {
//          
//          try {
//              new ClearVolumeRenderer();
//          } catch (InterruptedException ex) {
//              Logger.getLogger(MicroExplorer.class.getName()).log(Level.SEVERE, null, ex);
//          }
//
//          try {
//            java.lang.Thread.sleep(100);
//          }
//          catch(Exception e) { }  
//      }
//    }).start();
       
    }//GEN-LAST:event_get3DProjectionActionPerformed

    private void jLabel15MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MouseClicked

    }//GEN-LAST:event_jLabel15MouseClicked

    private void SetGlobalToLocalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SetGlobalToLocalActionPerformed
        ec.setAxesToCurrent();
    }//GEN-LAST:event_SetGlobalToLocalActionPerformed

    private void UseGlobalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UseGlobalActionPerformed
        ec.setGlobalAxes(!ec.getGlobalAxes());
        updatePlotByPopUpMenu(this.jComboBoxXaxis.getSelectedIndex(), this.jComboBoxYaxis.getSelectedIndex(), this.jComboBoxLUTPlot.getSelectedIndex(), jComboBoxPointSize.getSelectedIndex());

    }//GEN-LAST:event_UseGlobalActionPerformed

    private void BWLUTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BWLUTActionPerformed
     if (!noLUT) {
            onPlotChangeRequest(jComboBoxXaxis.getSelectedIndex(), jComboBoxYaxis.getSelectedIndex(), jComboBoxLUTPlot.getSelectedIndex(), jComboBoxPointSize.getSelectedIndex(), imageGate);
        } else {
            onRemoveLUTChangeRequest(jComboBoxXaxis.getSelectedIndex(), jComboBoxYaxis.getSelectedIndex(), -1, jComboBoxPointSize.getSelectedIndex(), imageGate);
        }
    }//GEN-LAST:event_BWLUTActionPerformed

    private void exportCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportCSVActionPerformed
       ExportCSV ex = new ExportCSV();
       ex.export(this.descriptions, this.plotvalues);
    }//GEN-LAST:event_exportCSVActionPerformed

    private void importCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importCSVActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_importCSVActionPerformed

    private void AxesSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AxesSettingsActionPerformed
       AxesSetup.setVisible(true);
       AxesSetup.setContent(ec.getSettingsContent());
       AxesSetup.addAxesChangeListener(this);
    }//GEN-LAST:event_AxesSettingsActionPerformed

    private void axesLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_axesLabelMouseClicked
        
    }//GEN-LAST:event_axesLabelMouseClicked

    private void AutoScaleAxesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AutoScaleAxesActionPerformed
        ec.setCustomRange(false);
        updatePlotByPopUpMenu(this.jComboBoxXaxis.getSelectedIndex(), this.jComboBoxYaxis.getSelectedIndex(), this.jComboBoxLUTPlot.getSelectedIndex(), jComboBoxPointSize.getSelectedIndex());
    }//GEN-LAST:event_AutoScaleAxesActionPerformed

    private void exportGatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportGatesActionPerformed
        ec.exportGates();
    }//GEN-LAST:event_exportGatesActionPerformed

    private void LoadGatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LoadGatesActionPerformed
        ec.importGates();
    }//GEN-LAST:event_LoadGatesActionPerformed

    private void ExportGraphActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExportGraphActionPerformed
        ec.getBufferedImage();
    }//GEN-LAST:event_ExportGraphActionPerformed

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
//            java.util.logging.Logger.getLogger(MicroExplorer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(MicroExplorer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(MicroExplorer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(MicroExplorer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        //</editor-fold>
//
//        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new MicroExplorer().setVisible(true);
//            }
//        });
//    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AutoScaleAxes;
    private javax.swing.JButton AxesSettings;
    private javax.swing.JToggleButton BWLUT;
    private javax.swing.JMenu Edit;
    private javax.swing.JButton ExportGraph;
    private javax.swing.JButton FlipAxes;
    private javax.swing.JButton LoadGates;
    protected javax.swing.JPanel Main;
    private javax.swing.JPanel North;
    private javax.swing.JButton SetGlobalToLocal;
    private javax.swing.JMenu Settings;
    private javax.swing.JPanel SouthPanel;
    private javax.swing.JToggleButton UseGlobal;
    private javax.swing.JPanel WestPanel;
    protected javax.swing.JToggleButton addPolygonGate;
    protected javax.swing.JToggleButton addQuadrantGate;
    protected javax.swing.JToggleButton addRectangularGate;
    private javax.swing.JLabel axesLabel;
    private javax.swing.JButton exportCSV;
    private javax.swing.JButton exportGates;
    private javax.swing.JButton get3DProjection;
    private javax.swing.JButton importCSV;
    private javax.swing.JComboBox jComboBox1;
    protected javax.swing.JComboBox jComboBoxLUTPlot;
    private javax.swing.JComboBox jComboBoxPointSize;
    protected javax.swing.JComboBox jComboBoxXaxis;
    protected javax.swing.JComboBox jComboBoxYaxis;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator7;
    private javax.swing.JToolBar.Separator jSeparator8;
    private javax.swing.JToolBar toolbarGate;
    private javax.swing.JToolBar toolbarPlot;
    private javax.swing.JPanel yTextPanel;
    // End of variables declaration//GEN-END:variables

    private void setPanels(List plotvalues, ExplorationCenter ec, PlotAxesPanels pap) {

        this.plotvalues = plotvalues;
        this.ec = ec;
        this.ec.addMakeImageOverlayListener(this);
        DefaultPlotPanels DPP = new DefaultPlotPanels();
        this.pap = DPP;
        Main.removeAll();
        Main.add(ec.getPanel());
        updateBorderPanels(pap);

        pack();
    }

    public void updateCenterPanel(List plotvalues, ExplorationCenter ec) {
        Main.removeAll();
        Main.add(ec.getPanel());
        pack();
    }

    public void updateBorderPanels(PlotAxesPanels pap) {
    }

    public void displayXYView() {
        updateBorderPanels(DefaultXYPanels);
        //this.repaint();
        this.pack();
    }

    private void redrawCenterPanel() {
        updateCenterPanel(this.plotvalues, this.ec);
    }

    static public Dimension getMainDimension() {
        return MicroExplorer.MainPanelSize;
    }

    private void makePolygonGate() {
        activationGateTools(POLYGONGATE);
        ec.addPolygonToPlot();
        pack();
    }

    private void makeRectangleGate() {
        activationGateTools(RECTANGLEGATE);
        ec.addRectangleToPlot();
        pack();
    }

    private void makeQuadrantGate() {
        activationGateTools(QUADRANTGATE);
        ec.addQuadrantToPlot();
        pack();
    }

    private HashMap makeAvailableDataHM(ArrayList al) {
        HashMap<Integer, String> hm = new HashMap<Integer, String>();
        ListIterator<String> itr = al.listIterator();
        while (itr.hasNext()) {
            hm.put(itr.nextIndex(), itr.next());
        }
        return hm;
    }

    private void flipAxes() {
        updatePlotByPopUpMenu(this.jComboBoxYaxis.getSelectedIndex(), this.jComboBoxXaxis.getSelectedIndex(), this.jComboBoxLUTPlot.getSelectedIndex(), jComboBoxPointSize.getSelectedIndex());
    }

    private void activationGateTools(int activeGate) {
        if (activeGate > 5) {
            for (int i = 10; i <= 12; i++) {
                if (i != activeGate) {
                    this.GateButtonsHM.get(i).setEnabled(false);
                }
            }
        } else if (activeGate == 0) {
            for (int i = 10; i <= 12; i++) {
                this.GateButtonsHM.get(i).setEnabled(true);
                this.GateButtonsHM.get(i).setSelected(false);
            }
        } else {
            for (int i = 10; i <= 12; i++) {
                this.GateButtonsHM.get(i).setEnabled(true);
                this.GateButtonsHM.get(i).setSelected(false);
            }
        }
    }

    public void onPlotChangeRequest(int x, int y, int z, int size, boolean imagegate) {
        if (BWLUT.isSelected()) {
            onRemoveLUTChangeRequest(x, y, z, size, imagegate);
        } else {
            Main.removeAll();
            ec.updatePlot(x, y, z, size);
            Main.add(ec.getPanel());
            updateBorderPanels(DefaultXYPanels);
            updateAxesLabels(jComboBoxXaxis.getSelectedItem().toString(), jComboBoxYaxis.getSelectedItem().toString(), jComboBoxLUTPlot.getSelectedItem().toString());
            pack();
        }
    }

    public void onRemoveLUTChangeRequest(int x, int y, int z, int size, boolean imagegate) {

        Main.removeAll();
        ec.updatePlot(x, y, -1, size);
        Main.add(ec.getPanel());
        updateBorderPanels(DefaultXYPanels);
        updateAxesLabels(jComboBoxXaxis.getSelectedItem().toString(), jComboBoxYaxis.getSelectedItem().toString(), "");
        pack();
    }

    private void addAxesLabels(String xText, String yText, String lText) {

        yTextPanel.setPreferredSize(new Dimension(40, 40));
        yLabel.setText(yText);
        yTextPanel.add(yLabel, BorderLayout.CENTER);
        JPanel yTextBuffer = new JPanel(new FlowLayout());
        yTextBuffer.setPreferredSize(new Dimension(40, 160));
        yTextPanel.add(yTextBuffer, BorderLayout.SOUTH);

        SouthPanel.setLayout(new FlowLayout());
        xLabel.setText(xText);
        lLabel.setText("Color: " + lText + "           ");
        SouthPanel.add(lLabel);
        SouthPanel.add(xLabel);
        pack();
    }

    private void updateAxesLabels(String xText, String yText, String lText) {
        yLabel.setText(yText);
        xLabel.setText(xText);
        lLabel.setText("Color: " + lText + "           ");
        yTextPanel.removeAll();
        SouthPanel.removeAll();
        addAxesLabels(xText, yText, lText);
    }

    private void updatePlotByPopUpMenu(int x, int y, int l, int size) {
        Main.removeAll();
        ec.updatePlot(x, y, l, size);
        Main.add(ec.getPanel());
        jComboBoxXaxis.setSelectedIndex(x);
        jComboBoxYaxis.setSelectedIndex(y);
        jComboBoxLUTPlot.setSelectedIndex(l);
        updateBorderPanels(DefaultXYPanels);
        pack();
    }

    private void updatePlotPointSize(int size) {
        ec.updatePlotPointSize(size);
    }

    private Number processPosition(int a, MicroObject volume) {
        if (a <= 10) {
            return (Number) volume.getAnalysisMaskVolume()[a];
        } else {
            int row = ((a) / 11) - 1;
            int column = a % 11;
            return (Number) volume.getAnalysisResultsVolume()[row][column];
        }
    }

    public void addMenuItems() {

        this.jMenuBar.removeAll();

    }

    @Override
    @SuppressWarnings("empty-statementget3DProjection.setEnabled(true);")
    public void makeOverlayImage(ArrayList gates, int x, int y, int xAxis, int yAxis) {
        get3DProjection.setEnabled(true);
    }

    @Override
    public void onChangeAxes(int x, int y, int l, int s, boolean imagegate) {
        updatePlotByPopUpMenu(x, y, l, s);
    }

    @Override
    public void onChangePointSize(int size, boolean imagegate) {

    }

    @Override
    public void changeLUT(int x) {
        //updatePlotByPopUpMenu(x, y);
    }

    @Override
    public void changeAxisLUT(String str) {

    }

    @Override
    public void imageOpened(ImagePlus ip) {
 
    }

    @Override
    public void imageClosed(ImagePlus ip) {
        
    if(ip.getID() == impoverlay.getID()){
   
    JFrame frame = new JFrame();
    frame.setBackground(vtea._vtea.BUTTONBACKGROUND);
    Object[] options = {"Yes", "No"};
        int n = JOptionPane.showOptionDialog(frame,
        "The overlay image has been closed.  Reload?",
        "Image closed.",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        options,
        options[0]);
        
        if (n == JOptionPane.YES_OPTION) {
            this.impoverlay = imp.duplicate();
            //this.impoverlay.addImageListener(this);
            ec.setGatedOverlay(impoverlay);
           impoverlay.setTitle(title);
            impoverlay.show();
        } else {}

        }
    }

    @Override
    public void imageUpdated(ImagePlus ip) {
   
        
    }

    @Override
    public void resetGateSelection() {
        activationGateTools(0);
    }

    @Override
    public void changeAxes(int axis, String position) {
        if (axis == MicroExplorer.XAXIS) {
            updatePlotByPopUpMenu(this.descriptions.indexOf(position), this.jComboBoxYaxis.getSelectedIndex(), this.jComboBoxLUTPlot.getSelectedIndex(), this.jComboBoxPointSize.getSelectedIndex());
        } else if (axis == MicroExplorer.YAXIS) {
            updatePlotByPopUpMenu(this.jComboBoxXaxis.getSelectedIndex(), this.descriptions.indexOf(position), this.jComboBoxLUTPlot.getSelectedIndex(), this.jComboBoxPointSize.getSelectedIndex());
        } else if (axis == MicroExplorer.LUTAXIS) {
            updatePlotByPopUpMenu(this.jComboBoxXaxis.getSelectedIndex(), this.jComboBoxYaxis.getSelectedIndex(), this.descriptions.indexOf(position), this.jComboBoxPointSize.getSelectedIndex());
        } else if (axis == MicroExplorer.POINTAXIS) {
            updatePlotByPopUpMenu(this.jComboBoxXaxis.getSelectedIndex(), this.jComboBoxYaxis.getSelectedIndex(), this.jComboBoxLUTPlot.getSelectedIndex(), this.descriptions.indexOf(position));
        }
    }

    @Override
    public void run() {
    }

    @Override
    public void onUpdatePlotWindow() {
        validate();
        repaint();
    }

    @Override
    public void onPlotUpdateListener() {
    }

    @Override
    public void roiModified(ImagePlus ip, int i) {
        try {
            if (ip.getID() == impoverlay.getID()) {

                if (i == RoiListener.COMPLETED || i == RoiListener.MOVED) {
                    ImageGatedObjects.clear();
                    ArrayList<MicroObject> volumes = (ArrayList) plotvalues.get(1);
                    ListIterator<MicroObject> itr = volumes.listIterator();
                    while (itr.hasNext()) {
                        MicroObject m = itr.next();
                        int[] c = new int[2];
                        c = m.getBoundsCenter();

                        if (ip.getRoi().contains(c[0], c[1])) {
                            ImageGatedObjects.add(m);
                        }
                    }            
                    validate();
                    repaint();
                }
            }
        } catch (NullPointerException e) {
        }
    }


    @Override
    public void onAxesSetting(ArrayList al) {
        
        
        ArrayList<Double> limits = new ArrayList();
        
        limits.add(Double.valueOf(((JTextField)(al.get(1))).getText()));
        limits.add(Double.valueOf(((JTextField)(al.get(3))).getText()));
        limits.add(Double.valueOf(((JTextField)(al.get(6))).getText()));
        limits.add(Double.valueOf(((JTextField)(al.get(8))).getText()));
        
        boolean xLinear = true;
        boolean yLinear = true;
        
        if(((JComboBox)al.get(4)).getSelectedIndex() == 1){
            xLinear = false;
        }
        if(((JComboBox)al.get(9)).getSelectedIndex() == 1){
            yLinear = false;
        }

        ec.setAxesTo(limits, xLinear, yLinear);

        updatePlotByPopUpMenu(this.jComboBoxXaxis.getSelectedIndex(), this.jComboBoxYaxis.getSelectedIndex(), this.jComboBoxLUTPlot.getSelectedIndex(), jComboBoxPointSize.getSelectedIndex());
    }

    class SelectPlottingDataMenu extends JPopupMenu implements ActionListener {

        HashMap<Integer, String> hm_position;
        HashMap<String, Integer> hm_string;
        int Axis;
        String CurrentSelection;
        private ArrayList<PopupMenuAxisListener> listeners = new ArrayList<PopupMenuAxisListener>();

        public SelectPlottingDataMenu(ArrayList<String> AvailableData, int Axis) {

            this.Axis = Axis;

            String tempString;
            Integer tempInteger = 0;

            ListIterator<String> itr = AvailableData.listIterator();
            HashMap<String, Integer> hm_string = new HashMap<String, Integer>();
            HashMap<Integer, String> hm_position = new HashMap<Integer, String>();

            while (itr.hasNext()) {
                tempString = itr.next();
                tempInteger = itr.nextIndex();
                this.add(new JMenuItem(tempString)).addActionListener(this);
                hm_string.put(tempString, tempInteger);

            }
            if (this.Axis == 2) {
                hm_string.put("None", tempInteger++);
            }
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            impoverlay.setOpenAsHyperStack(true);
            notifyPopupMenuAxisListeners(Axis, ae.getActionCommand());
        }

        public void addPopupMenuAxisListener(PopupMenuAxisListener listener) {
            listeners.add(listener);
        }

        public void notifyPopupMenuAxisListeners(int axis, String position) {
            for (PopupMenuAxisListener listener : listeners) {
                listener.changeAxes(Axis, position);
            }
        }
    }
    
    class ExportCSV {

        public ExportCSV() {}

        
        protected void export(ArrayList header, List attributes) {
           

            JFileChooser jf = new JFileChooser(new File("untitled.csv"));
            
            int returnVal = jf.showSaveDialog(Main);
            
            File file = jf.getSelectedFile(); 
            
            //int returnVal = chooser.showOpenDialog(parent);
            
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                
                try{
                    
                    
               
 
            
            //plotvalues contains data,  get(1) -> array          
            //availabledata contains the column data.          
            
//                private Number processPosition(int a, MicroObjectModel volume) {
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
            try{
                
            PrintWriter pw = new PrintWriter(file);
            StringBuilder sb = new StringBuilder();
            
            ListIterator itr = descriptions.listIterator();
            
            sb.append("Object");
            sb.append(',');
            sb.append("PosX");
            sb.append(',');
            sb.append("PosY");
            sb.append(',');
            sb.append("PosZ");
            sb.append(',');
            while(itr.hasNext()){
            sb.append((String)itr.next());
                if(itr.hasNext()){
                    sb.append(',');
                }
            }

            sb.append('\n');

            ArrayList<MicroObject> objects = ec.getObjects();
            ArrayList<ArrayList<Number>> measurements = ec.getMeasurments();
            
                
            for(int i = 0; i < objects.size(); i++){
                
                MicroObject volume = objects.get(i);
                
                
                
                sb.append(volume.getSerialID());
                sb.append(',');
                sb.append(volume.getCentroidX());
                sb.append(',');
                sb.append(volume.getCentroidY());
                sb.append(',');
                sb.append(volume.getCentroidZ());
                  
                
                ArrayList<Number> measured = measurements.get(i);
                
                ListIterator<Number> measured_itr = measured.listIterator();
                
                while(measured_itr.hasNext()){
                
                sb.append(','); 
                sb.append(measured_itr.next());
                
                
            }
                
                sb.append('\n');
            }
           
            
            
            pw.write(sb.toString());
            pw.close();

        
            }catch(FileNotFoundException e){}
        
        }catch(NullPointerException ne){}
                
    } else {}
    
        } 
        
    }

}
