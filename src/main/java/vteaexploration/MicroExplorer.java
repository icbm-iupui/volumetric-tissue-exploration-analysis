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
import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.RoiListener;
import ij.io.FileSaver;
import ij.measure.ResultsTable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FilenameUtils;
import vtea.OpenObxFormat;
import vtea._vtea;
import vtea.exploration.listeners.AddFeaturesListener;
import vtea.exploration.listeners.AxesChangeListener;
import vtea.exploration.listeners.FeatureMapListener;
import vtea.exploration.listeners.PlotUpdateListener;
import vtea.exploration.listeners.SubGateExplorerListener;
import vtea.exploration.listeners.UpdatePlotWindowListener;
import vtea.exploration.listeners.AxesSetupExplorerPlotUpdateListener;
import vtea.exploration.listeners.LinkedKeyListener;
import vtea.exploration.plotgatetools.gates.PolygonGate;
import vtea.exploration.plotgatetools.listeners.ChangePlotAxesListener;
import vtea.exploration.plotgatetools.listeners.MakeImageOverlayListener;
import vtea.exploration.plotgatetools.listeners.PopupMenuAxisLUTListener;
import vtea.exploration.plotgatetools.listeners.PopupMenuAxisListener;
import vtea.exploration.plotgatetools.listeners.PopupMenuLUTListener;
import vtea.exploration.plotgatetools.listeners.ResetSelectionListener;
import vtea.exploration.plottools.panels.AbstractExplorationPanel;
import vtea.exploration.plottools.panels.DefaultPlotPanels;
import vtea.exploration.plottools.panels.ExplorationCenter;
import vtea.exploration.plottools.panels.PlotAxesPanels;
import vtea.exploration.plottools.panels.VerticalLabelUI;
import vtea.exploration.plottools.panels.XYPanels;
import vtea.feature.FeatureFrame;
import vtea.measurement.MeasurementFrame;
import vtea.processor.ExplorerProcessor;
import vtea.protocol.setup.SegmentationPreviewer;
import vteaobjects.MicroObject;

/**
 *
 * @author vinfrais
 *
 */

public class MicroExplorer extends javax.swing.JFrame implements 
        FeatureMapListener, SubGateExplorerListener, AddFeaturesListener, 
        RoiListener, LinkedKeyListener, PlotUpdateListener, MakeImageOverlayListener, 
        ChangePlotAxesListener, ImageListener, ResetSelectionListener, 
        PopupMenuAxisListener, PopupMenuLUTListener, PopupMenuAxisLUTListener, 
        UpdatePlotWindowListener, AxesChangeListener, AxesSetupExplorerPlotUpdateListener, Runnable {

    private XYPanels DefaultXYPanels;
    private static final Dimension MAINPANELSIZE = new Dimension(630, 640);
    private static final int POLYGONGATE = 10;
    private static final int RECTANGLEGATE = 11;
    private static final int QUADRANTGATE = 12;

    public static final int XAXIS = 0;
    public static final int YAXIS = 1;
    private static final int LUTAXIS = 2;
    private static final int POINTAXIS = 3;

    public static final int XSTART = 0;
    public static final int YSTART = 1;
    public static final int LUTSTART = 1;

    public static final int POINTSIZE = 4;

    int featureCount = 0;

    ArrayList measurements;
    ExplorationCenter ec;
    PlotAxesPanels pap;
    JPanel HeaderPanel;
    ImagePlus imp;
    ImagePlus impoverlay;
    int impMode;
    String title;
    String key;
    ArrayList<String> childKeys = new ArrayList<String>();
    String parentKey;
    ArrayList descriptions;
    ArrayList<String> descriptionsLabels = new ArrayList<String>();
    ArrayList<MicroObject> Objects = new ArrayList<MicroObject>();
    ArrayList<MicroObject> ImageGatedObjects = new ArrayList<MicroObject>();

    private boolean all = false;

    boolean imageGate = false;
    boolean noLUT = false;
    boolean updatePlot = true;

    ResultsTable rt = new ResultsTable();

    JLabel xLabel;
    JLabel yLabel;
    JLabel lLabel;

    TableWindow ResultsWindow;

    double[][] ObjectIDs;

    HashMap<Integer, JToggleButton> GateButtonsHM = new HashMap<Integer, JToggleButton>();
    HashMap<Integer, String> AvailableDataHM = new HashMap<Integer, String>();

    ArrayList<AddFeaturesListener> FeatureListeners = new ArrayList<AddFeaturesListener>();

    ArrayList<SubGateExplorerListener> SubGateListeners = new ArrayList<SubGateExplorerListener>();

    int subgateSerial = 0;

    FeatureFrame ff;
    boolean ffchecked = false;

    MeasurementFrame mf;
    boolean mfchecked = false;

    String[] sizes = {"4", "6", "8", "10", "15", "20"};

    public JMenuExploration ProcessingMenu;
    public JMenuExploration ObjectMenu;
    public JMenuExploration WorkflowMenu;

    /**
     * Creates new form MicroExplorer
     */
    public MicroExplorer() {

        Roi.addRoiListener(this);

    }

    public void process(String key, ImagePlus imp, String title, ArrayList plotvalues, AbstractExplorationPanel aep, PlotAxesPanels pap, ArrayList AvailableData, ArrayList descriptionLabel) {
        //Needs to be converted to a Factory metaphor.

        //Setup base dataseta
        //Available data is an arraylist of the available tags as they exist in microvolumes.
        //imp is the original image
        this.key = key;

        this.descriptions = AvailableData;
        this.descriptionsLabels = descriptionLabel;
        //as taken from stackoverflow

        ComboboxToolTipRenderer renderer = new ComboboxToolTipRenderer();
        renderer.setTooltips(descriptionsLabels);

        this.imp = imp;
        this.impoverlay = imp.duplicate();
        this.impoverlay.addImageListener(this);

        this.impoverlay.setOpenAsHyperStack(true);
        this.impoverlay.setDisplayMode(IJ.COMPOSITE);
        impoverlay.setTitle("Mapping: " + title);

        //Setup GUI and populate comboboxes
        initComponents();

        this.jComboBoxXaxis.setRenderer(renderer);
        this.jComboBoxYaxis.setRenderer(renderer);
        this.jComboBoxLUTPlot.setRenderer(renderer);

        addMenuItems();

        this.title = title;
        get3DProjection.setEnabled(false);

        makeOverlayImage(new ArrayList<PolygonGate>(), aep.getSelectedObjects(), aep.getGatedObjects(impoverlay), MicroExplorer.XAXIS, MicroExplorer.YAXIS);

        AvailableDataHM = makeAvailableDataHM(descriptions);

        GateButtonsHM.put(POLYGONGATE, this.addPolygonGate);
        GateButtonsHM.put(RECTANGLEGATE, this.addRectangularGate);
        GateButtonsHM.put(QUADRANTGATE, this.addQuadrantGate);

        final SelectPlottingDataMenu PlottingPopupXaxis = new SelectPlottingDataMenu(descriptions, MicroExplorer.XAXIS);
        final SelectPlottingDataMenu PlottingPopupYaxis = new SelectPlottingDataMenu(descriptions, MicroExplorer.YAXIS);
        final SelectPlottingDataMenu PlottingPopupLUTaxis = new SelectPlottingDataMenu(descriptions, MicroExplorer.LUTAXIS);

        PlottingPopupXaxis.addPopupMenuAxisListener(this);
        PlottingPopupYaxis.addPopupMenuAxisListener(this);
        PlottingPopupLUTaxis.addPopupMenuAxisListener(this);

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

        aep.addResetSelectionListener(this);
        aep.addSubgateListener(this);
        aep.addLinkedKeyListener(this);
        aep.getXYChartPanel().addUpdatePlotWindowListener(this);
        aep.addFeatureListener(this);
        aep.setGatedOverlay(impoverlay);
        aep.addAxesSetpExplorerPlotUpdateListener(this);

        //load default view
        setPanels(plotvalues, aep, pap);
        this.addAxesLabels(AvailableData.get(this.XSTART).toString(), AvailableData.get(this.YSTART).toString(), AvailableData.get(this.LUTSTART).toString());
        this.displayXYView();
        this.repaint();
        this.pack();
        this.setVisible(true);
        jComboBoxPointSize.setSelectedIndex(4);

        makeDataTable();

        ff = new FeatureFrame(descriptions, ObjectIDs, imp);
        ff.addListener(this);

        mf = new MeasurementFrame(descriptions, aep.getObjects(), imp);
        mf.addListener(this);

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
        addPolygonGate = new javax.swing.JToggleButton();
        addRectangularGate = new javax.swing.JToggleButton();
        addQuadrantGate = new javax.swing.JToggleButton();
        exportGates = new javax.swing.JButton();
        LoadGates = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jLabel3 = new javax.swing.JLabel();
        AutoScaleAxes = new javax.swing.JButton();
        AxesSettings = new javax.swing.JButton();
        SetGlobalToLocal = new javax.swing.JButton();
        UseGlobal = new javax.swing.JToggleButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        get3DProjection = new javax.swing.JButton();
        getMask = new javax.swing.JButton();
        getSegmentation = new javax.swing.JButton();
        jSeparator7 = new javax.swing.JToolBar.Separator();
        jButtonDistance = new javax.swing.JButton();
        jButtonMeas = new javax.swing.JButton();
        jButtonFeature = new javax.swing.JButton();
        jSeparator8 = new javax.swing.JToolBar.Separator();
        ExportGraph = new javax.swing.JButton();
        exportCSV = new javax.swing.JButton();
        jSeparator9 = new javax.swing.JToolBar.Separator();
        exportOBJ = new javax.swing.JButton();
        importOBJ = new javax.swing.JButton();
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
        setMaximumSize(getPreferredSize());
        setMinimumSize(getPreferredSize());
        setPreferredSize(new java.awt.Dimension(725, 650));
        setSize(new java.awt.Dimension(725, 650));
        addContainerListener(new java.awt.event.ContainerAdapter() {
            public void componentAdded(java.awt.event.ContainerEvent evt) {
                formComponentAdded(evt);
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                formComponentMoved(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        North.setMinimumSize(new java.awt.Dimension(638, 75));
        North.setPreferredSize(new java.awt.Dimension(710, 80));
        North.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                NorthMouseClicked(evt);
            }
        });
        North.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 2, 5));

        toolbarPlot.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        toolbarPlot.setFloatable(false);
        toolbarPlot.setForeground(new java.awt.Color(255, 255, 255));
        toolbarPlot.setRollover(true);
        toolbarPlot.setMaximumSize(new java.awt.Dimension(715, 30));
        toolbarPlot.setMinimumSize(new java.awt.Dimension(715, 30));
        toolbarPlot.setPreferredSize(new java.awt.Dimension(715, 30));
        toolbarPlot.add(jSeparator4);

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        jLabel1.setLabelFor(jComboBoxXaxis);
        jLabel1.setText("X");
        toolbarPlot.add(jLabel1);

        jComboBoxXaxis.setModel(new DefaultComboBoxModel(descriptions.toArray()));
        jComboBoxXaxis.setSelectedIndex(this.XSTART);
        jComboBoxXaxis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxXaxisActionPerformed(evt);
            }
        });
        toolbarPlot.add(jComboBoxXaxis);

        jLabel2.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        jLabel2.setLabelFor(jComboBoxYaxis);
        jLabel2.setText(" Y");
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
        jLabel5.setText(" Color");
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
        toolbarGate.setMaximumSize(new java.awt.Dimension(725, 41));
        toolbarGate.setMinimumSize(new java.awt.Dimension(725, 41));
        toolbarGate.setPreferredSize(new java.awt.Dimension(725, 41));

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

        getMask.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/masks.png"))); // NOI18N
        getMask.setToolTipText("Visualize gated masks");
        getMask.setFocusable(false);
        getMask.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        getMask.setMaximumSize(new java.awt.Dimension(35, 40));
        getMask.setMinimumSize(new java.awt.Dimension(35, 40));
        getMask.setName(""); // NOI18N
        getMask.setPreferredSize(new java.awt.Dimension(35, 40));
        getMask.setVerifyInputWhenFocusTarget(false);
        getMask.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        getMask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getMaskActionPerformed(evt);
            }
        });
        toolbarGate.add(getMask);

        getSegmentation.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/Segmentation.png"))); // NOI18N
        getSegmentation.setToolTipText("Visualize segmentation");
        getSegmentation.setFocusable(false);
        getSegmentation.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        getSegmentation.setMaximumSize(new java.awt.Dimension(35, 40));
        getSegmentation.setMinimumSize(new java.awt.Dimension(35, 40));
        getSegmentation.setName(""); // NOI18N
        getSegmentation.setPreferredSize(new java.awt.Dimension(35, 40));
        getSegmentation.setVerifyInputWhenFocusTarget(false);
        getSegmentation.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        getSegmentation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getSegmentationActionPerformed(evt);
            }
        });
        toolbarGate.add(getSegmentation);
        toolbarGate.add(jSeparator7);

        jButtonDistance.setBackground(new java.awt.Color(102, 255, 102));
        jButtonDistance.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/ruler.png"))); // NOI18N
        jButtonDistance.setToolTipText("Add distance measure");
        jButtonDistance.setEnabled(false);
        jButtonDistance.setFocusable(false);
        jButtonDistance.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonDistance.setMaximumSize(new java.awt.Dimension(35, 40));
        jButtonDistance.setMinimumSize(new java.awt.Dimension(35, 40));
        jButtonDistance.setPreferredSize(new java.awt.Dimension(35, 40));
        jButtonDistance.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonDistance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDistanceActionPerformed(evt);
            }
        });
        toolbarGate.add(jButtonDistance);

        jButtonMeas.setBackground(new java.awt.Color(102, 255, 102));
        jButtonMeas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/FeaturesAdd_2.png"))); // NOI18N
        jButtonMeas.setToolTipText("Import features from CSV...");
        jButtonMeas.setFocusable(false);
        jButtonMeas.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonMeas.setMaximumSize(new java.awt.Dimension(35, 40));
        jButtonMeas.setMinimumSize(new java.awt.Dimension(35, 40));
        jButtonMeas.setPreferredSize(new java.awt.Dimension(35, 40));
        jButtonMeas.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonMeas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonMeasActionPerformed(evt);
            }
        });
        toolbarGate.add(jButtonMeas);

        jButtonFeature.setBackground(new java.awt.Color(102, 255, 102));
        jButtonFeature.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/Features.png"))); // NOI18N
        jButtonFeature.setToolTipText("Add features...");
        jButtonFeature.setFocusable(false);
        jButtonFeature.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonFeature.setMaximumSize(new java.awt.Dimension(35, 40));
        jButtonFeature.setMinimumSize(new java.awt.Dimension(35, 40));
        jButtonFeature.setPreferredSize(new java.awt.Dimension(35, 40));
        jButtonFeature.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonFeature.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFeatureActionPerformed(evt);
            }
        });
        toolbarGate.add(jButtonFeature);
        toolbarGate.add(jSeparator8);

        ExportGraph.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/insert-image-2 copy.png"))); // NOI18N
        ExportGraph.setToolTipText("Export graph as PNG...");
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

        exportCSV.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/document-save_csv-2_32.png"))); // NOI18N
        exportCSV.setToolTipText("Export objects as csv...");
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
        toolbarGate.add(jSeparator9);

        exportOBJ.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/document-save_obj-2_32.png"))); // NOI18N
        exportOBJ.setToolTipText("Export objects...");
        exportOBJ.setFocusable(false);
        exportOBJ.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        exportOBJ.setMaximumSize(new java.awt.Dimension(35, 40));
        exportOBJ.setMinimumSize(new java.awt.Dimension(35, 40));
        exportOBJ.setName(""); // NOI18N
        exportOBJ.setPreferredSize(new java.awt.Dimension(35, 40));
        exportOBJ.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        exportOBJ.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportOBJActionPerformed(evt);
            }
        });
        toolbarGate.add(exportOBJ);

        importOBJ.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/document-open-folder-obj_32.png"))); // NOI18N
        importOBJ.setToolTipText("Import objects...");
        importOBJ.setFocusable(false);
        importOBJ.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        importOBJ.setMaximumSize(new java.awt.Dimension(35, 40));
        importOBJ.setMinimumSize(new java.awt.Dimension(35, 40));
        importOBJ.setName(""); // NOI18N
        importOBJ.setPreferredSize(new java.awt.Dimension(35, 40));
        importOBJ.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        importOBJ.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importOBJActionPerformed(evt);
            }
        });
        toolbarGate.add(importOBJ);

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
        Main.setToolTipText("");
        Main.setMaximumSize(new java.awt.Dimension(600, 600));
        Main.setMinimumSize(new java.awt.Dimension(600, 600));
        Main.setName(""); // NOI18N
        Main.setPreferredSize(new java.awt.Dimension(600, 600));
        Main.setLayout(new java.awt.BorderLayout());
        getContentPane().add(Main, java.awt.BorderLayout.CENTER);

        jMenuBar.setBackground(new java.awt.Color(238, 238, 238));
        jMenuBar.setMinimumSize(new java.awt.Dimension(52, 1));
        jMenuBar.setPreferredSize(new java.awt.Dimension(52, 22));

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
        if(updatePlot){
        ec.setCustomRange(this.XAXIS, false);
        onPlotChangeRequest(jComboBoxXaxis.getSelectedIndex(), jComboBoxYaxis.getSelectedIndex(), jComboBoxLUTPlot.getSelectedIndex(), jComboBoxPointSize.getSelectedIndex(), imageGate);}
    }//GEN-LAST:event_jComboBoxXaxisActionPerformed

    private void jComboBoxYaxisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxYaxisActionPerformed
        if(updatePlot){
        ec.setCustomRange(this.YAXIS,false);
        onPlotChangeRequest(jComboBoxXaxis.getSelectedIndex(), jComboBoxYaxis.getSelectedIndex(), jComboBoxLUTPlot.getSelectedIndex(), jComboBoxPointSize.getSelectedIndex(), imageGate);}
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
    
        onPlotChangeRequest(jComboBoxXaxis.getSelectedIndex(), jComboBoxYaxis.getSelectedIndex(), jComboBoxLUTPlot.getSelectedIndex(), jComboBoxPointSize.getSelectedIndex(), imageGate);
    }//GEN-LAST:event_jComboBoxLUTPlotActionPerformed

    private void jComboBoxPointSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxPointSizeActionPerformed
      
        onPlotChangeRequest(jComboBoxXaxis.getSelectedIndex(), jComboBoxYaxis.getSelectedIndex(), jComboBoxLUTPlot.getSelectedIndex(), jComboBoxPointSize.getSelectedIndex(), imageGate);
    }//GEN-LAST:event_jComboBoxPointSizeActionPerformed

    private void jComboBoxPointSizePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jComboBoxPointSizePropertyChange

    }//GEN-LAST:event_jComboBoxPointSizePropertyChange

    private void get3DProjectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_get3DProjectionActionPerformed

        new Thread(() -> {
            try {
                ec.getZProjection();
                java.lang.Thread.sleep(100);
                String image = "Gates_" + impoverlay.getTitle();
                IJ.run("Open in ClearVolume", image);
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getLocalizedMessage());
            }
        }).start();
        
      
    }//GEN-LAST:event_get3DProjectionActionPerformed

    private void SetGlobalToLocalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SetGlobalToLocalActionPerformed
        ec.setAxesToCurrent();
    }//GEN-LAST:event_SetGlobalToLocalActionPerformed

    private void UseGlobalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UseGlobalActionPerformed
        ec.setGlobalAxes(!ec.getGlobalAxes());
        
       // System.out.println("PROFILING: set global axes, " + ec.getGlobalAxes());
        
        updatePlotByPopUpMenu(this.jComboBoxXaxis.getSelectedIndex(), 
                this.jComboBoxYaxis.getSelectedIndex(), 
                this.jComboBoxLUTPlot.getSelectedIndex(), 
                jComboBoxPointSize.getSelectedIndex());

    }//GEN-LAST:event_UseGlobalActionPerformed

    private void exportOBJActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportOBJActionPerformed
        new Thread(() -> {
            try {

                ExportOBJ ex = new ExportOBJ();
                ex.export(key, imp, ec.getObjects(),
                        measurements, descriptions,
                        descriptionsLabels);
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getLocalizedMessage());
            }
        }).start();
    }//GEN-LAST:event_exportOBJActionPerformed

    private void importOBJActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importOBJActionPerformed
        new Thread(() -> {
            try {
                OpenObxFormat io = new OpenObxFormat();
                io.importObjects(Main);

            } catch (Exception e) {
                System.out.println("ERROR: " + e.getLocalizedMessage());
            }
        }).start();
    }//GEN-LAST:event_importOBJActionPerformed

    private void AxesSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AxesSettingsActionPerformed
        ec.invokeAxesSettingsDialog(this.getX(), this.getY()+this.getHeight());
        //System.out.println("PROFILING: Axes setup at: " + this.getX() + " and " + (this.getY()+this.getHeight()));
    }//GEN-LAST:event_AxesSettingsActionPerformed

    private void AutoScaleAxesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AutoScaleAxesActionPerformed
        ec.setCustomRange(this.XAXIS, false);
        ec.setCustomRange(this.YAXIS, false);
        updatePlotByPopUpMenu(this.jComboBoxXaxis.getSelectedIndex(), this.jComboBoxYaxis.getSelectedIndex(), this.jComboBoxLUTPlot.getSelectedIndex(), jComboBoxPointSize.getSelectedIndex());
    }//GEN-LAST:event_AutoScaleAxesActionPerformed

    private void exportGatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportGatesActionPerformed
        new Thread(() -> {
            try {
                ec.exportGates();
            } catch (Exception e) {

            }
        }).start();

    }//GEN-LAST:event_exportGatesActionPerformed

    private void LoadGatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LoadGatesActionPerformed
        ec.importGates();
    }//GEN-LAST:event_LoadGatesActionPerformed

    private void ExportGraphActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExportGraphActionPerformed

        new Thread(() -> {
            try {
                ec.getBufferedImage();

            } catch (Exception e) {

            }
        }).start();

    }//GEN-LAST:event_ExportGraphActionPerformed

    private void jButtonFeatureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFeatureActionPerformed
        ff.setVisible(true);
        if (!ffchecked) {
            ffchecked = true;
        }

    }//GEN-LAST:event_jButtonFeatureActionPerformed

    private void getSegmentationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getSegmentationActionPerformed
        SegmentationPreviewer.SegmentationFactory(imp, ec.getObjects());
    }//GEN-LAST:event_getSegmentationActionPerformed

    private void jButtonDistanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDistanceActionPerformed

    }//GEN-LAST:event_jButtonDistanceActionPerformed

    private void exportCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportCSVActionPerformed

        new Thread(() -> {
            try {

                ExportCSV ex = new ExportCSV();
                ex.export(this.descriptions, this.measurements);
            } catch (Exception e) {
                //System.out.println("ERROR: " + e.getLocalizedMessage());
            }
        }).start();

    }//GEN-LAST:event_exportCSVActionPerformed

    private void jButtonMeasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonMeasActionPerformed

        ec.addFromCSV("new");

//        mf.setVisible(true);
//        if (!mfchecked) {
//            mfchecked = true;
//        }
    }//GEN-LAST:event_jButtonMeasActionPerformed

    private void NorthMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_NorthMouseClicked

    }//GEN-LAST:event_NorthMouseClicked

    private void getMaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getMaskActionPerformed
new Thread(() -> {
            try {
                ec.getZProjection();
                java.lang.Thread.sleep(100);
                String image = "Gates_" + impoverlay.getTitle();
               
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getLocalizedMessage());
            }
        }).start();        // TODO add your handling code here:
    }//GEN-LAST:event_getMaskActionPerformed

    private void formComponentMoved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentMoved
        if(this.isVisible()){
            Point position = evt.getComponent().getLocationOnScreen();
            ec.updateMenuPositions(position.x, position.y + this.getHeight());
        }
    }//GEN-LAST:event_formComponentMoved

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
       ec.closeMenu();
       
    }//GEN-LAST:event_formWindowClosing

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        ec.closeMenu();       
    }//GEN-LAST:event_formWindowClosed

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
    private javax.swing.JButton exportCSV;
    private javax.swing.JButton exportGates;
    private javax.swing.JButton exportOBJ;
    private javax.swing.JButton get3DProjection;
    private javax.swing.JButton getMask;
    private javax.swing.JButton getSegmentation;
    private javax.swing.JButton importOBJ;
    private javax.swing.JButton jButtonDistance;
    private javax.swing.JButton jButtonFeature;
    private javax.swing.JButton jButtonMeas;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBoxLUTPlot;
    private javax.swing.JComboBox jComboBoxPointSize;
    private javax.swing.JComboBox jComboBoxXaxis;
    private javax.swing.JComboBox jComboBoxYaxis;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator7;
    private javax.swing.JToolBar.Separator jSeparator8;
    private javax.swing.JToolBar.Separator jSeparator9;
    private javax.swing.JToolBar toolbarGate;
    private javax.swing.JToolBar toolbarPlot;
    private javax.swing.JPanel yTextPanel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void makeSubGateExplorer(ArrayList<MicroObject> objects, ArrayList<ArrayList<Number>> measurements) {
        new Thread(() -> {
            try {

                ArrayList<String> newDescriptions = new ArrayList<String>();
                ArrayList<String> newDescriptionsLabels = new ArrayList<String>();

                ListIterator<String> itr = descriptions.listIterator();
                while (itr.hasNext()) {
                    String str = itr.next().toString();
                    newDescriptions.add(str);
                }

                ListIterator<String> itr1 = descriptionsLabels.listIterator();
                while (itr1.hasNext()) {
                    String str1 = itr1.next().toString();
                    newDescriptionsLabels.add(str1);
                }

                ExplorerProcessor ep = new ExplorerProcessor("Subgate_" + subgateSerial + "_" + this.key, this.key, imp.duplicate(), objects,
                        measurements, newDescriptions, newDescriptionsLabels);
                ep.execute();

                subgateSerial++;

            } catch (Exception e) {
                System.out.println("ERROR: " + e.getLocalizedMessage());
            }
        }).start();

    }
    
    public ArrayList<ArrayList<String>> getFeatureDescriptions(){
                        ArrayList<String> newDescriptions = new ArrayList<String>();
                ArrayList<String> newDescriptionsLabels = new ArrayList<String>();
                
                ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();

                ListIterator<String> itr = descriptions.listIterator();
                while (itr.hasNext()) {
                    String str = itr.next().toString();
                    newDescriptions.add(str);
                }

                ListIterator<String> itr1 = descriptionsLabels.listIterator();
                while (itr1.hasNext()) {
                    String str1 = itr1.next().toString();
                    newDescriptionsLabels.add(str1);
                }
                result.add(newDescriptions);
                result.add(newDescriptionsLabels);
                
                return result;
    }
    
    public void setParentKey(String str){
        parentKey = str;
    }

    private void setPanels(List plotvalues, ExplorationCenter ec, PlotAxesPanels pap) {

        this.measurements = (ArrayList) plotvalues;
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
        updateCenterPanel(this.measurements, this.ec);
    }

    static public Dimension getMainDimension() {
        return MicroExplorer.MAINPANELSIZE;
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
        new Thread(() -> {
            try {
            Main.removeAll();
            ec.updatePlot(x, y, z, size);
            Main.add(ec.getPanel());
            updateBorderPanels(DefaultXYPanels);
            updateAxesLabels(jComboBoxXaxis.getSelectedItem().toString(), jComboBoxYaxis.getSelectedItem().toString(), jComboBoxLUTPlot.getSelectedItem().toString());
            pack();
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getLocalizedMessage());
            }
        }).start();
    }
    
    public void ExplorerSetupPlotChangerequest(int x, int y, int z, int size){
         new Thread(() -> {
            try {
        Main.removeAll();
        ec.updatePlot(this.jComboBoxXaxis.getSelectedIndex(), this.jComboBoxYaxis.getSelectedIndex(), 
                this.jComboBoxLUTPlot.getSelectedIndex(), this.jComboBoxPointSize.getSelectedIndex());
        Main.add(ec.getPanel());
        updateBorderPanels(DefaultXYPanels);
        updateAxesLabels(jComboBoxXaxis.getSelectedItem().toString(), jComboBoxYaxis.getSelectedItem().toString(), jComboBoxLUTPlot.getSelectedItem().toString());
        pack();
                    } catch (Exception e) {
                System.out.println("ERROR: " + e.getLocalizedMessage());
            }
        }).start();
    }

    public void onRemoveLUTChangeRequest(int x, int y, int z, int size, boolean imagegate) {
    new Thread(() -> {
            try {
        Main.removeAll();
        ec.updatePlot(x, y, -1, size);
        Main.add(ec.getPanel());
        updateBorderPanels(DefaultXYPanels);
        updateAxesLabels(jComboBoxXaxis.getSelectedItem().toString(), jComboBoxYaxis.getSelectedItem().toString(), "");
        pack();
                      } catch (Exception e) {
                System.out.println("ERROR: " + e.getLocalizedMessage());
            }
        }).start();
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
             
        new Thread(() -> {
            try {
        Main.removeAll();
        ec.updatePlot(x, y, l, size);
        Main.add(ec.getPanel());
        
        updatePlot = false;
        
        jComboBoxXaxis.setSelectedIndex(x);
        jComboBoxYaxis.setSelectedIndex(y);
        jComboBoxLUTPlot.setSelectedIndex(l);
        jComboBoxPointSize.setSelectedIndex(size);
        
        updatePlot = true;

        updateBorderPanels(DefaultXYPanels);
        updateAxesLabels(jComboBoxXaxis.getSelectedItem().toString(), jComboBoxYaxis.getSelectedItem().toString(), jComboBoxLUTPlot.getSelectedItem().toString());

        pack();
                              } catch (Exception e) {
                System.out.println("ERROR: " + e.getLocalizedMessage());
            }
        }).start();

    }



    public void addMenuItems() {
        this.jMenuBar.removeAll();
        this.jMenuBar.setVisible(false);
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
    }

    @Override
    public void changeAxisLUT(String str) {

    }

    @Override
    public void imageOpened(ImagePlus ip) {

    }

    @Override
    public void imageClosed(ImagePlus ip) {
        if(this.isVisible()){
        if (ip.getID() == impoverlay.getID()) {
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
                ec.setGatedOverlay(impoverlay);
                impoverlay.setTitle("Mapping: " + title);
                impoverlay.show();
            } else {
            }

        }
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

                    ListIterator<MicroObject> itr = this.Objects.listIterator();
                    while (itr.hasNext()) {
                        MicroObject m = itr.next();
                        int x = (int) m.getCentroidX();
                        int y = (int) m.getCentroidY();
                        if (ip.getRoi().contains(x, y)) {
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

        //ec.setAxesTo(limits, xLinear, yLinear,lutTable.getSelectedIndex());

        updatePlotByPopUpMenu(this.jComboBoxXaxis.getSelectedIndex(), this.jComboBoxYaxis.getSelectedIndex(), this.jComboBoxLUTPlot.getSelectedIndex(), jComboBoxPointSize.getSelectedIndex());
    }

    private void checkMeasurements() {
        ListIterator<ArrayList<Number>> itr_measurements = measurements.listIterator();
////        System.out.println("Checking measurement arraylist");
        while (itr_measurements.hasNext()) {
            ArrayList<Number> measure = itr_measurements.next();
            ListIterator<Number> itr_values = measure.listIterator();

            while (itr_values.hasNext()) {

                //System.out.println("PROFILING: " + itr_values.next());
            }
        }

    }

//    private void checkMeasurementsArray() {
//
//        //System.out.println("Checking Feature Array" );
//        for (int i = 0; i < ObjectIDs.length; i++) {
//            for (int j = 0; j < descriptions.size() + 4; j++) {
//                //System.out.println("PROFILING: " + ObjectIDs[i][j]);
//            }
//        }
//    }

    /**
     * Creates 2D feature array. The first column is the unique SerialID and the
     * subsequent columns are different computed features.
     */
    private void makeDataTable() {

        ArrayList<MicroObject> objects = ec.getObjects();
        ListIterator a_itr = objects.listIterator();

        this.ObjectIDs = new double[objects.size()][descriptions.size() + 4];

        try {
            int i = 0;
            while (a_itr.hasNext()) {

                MicroObject volume = (MicroObject) a_itr.next();

                this.ObjectIDs[i][0] = (double) volume.getSerialID();
                this.ObjectIDs[i][1] = (double) volume.getCentroidX();
                this.ObjectIDs[i][2] = (double) volume.getCentroidY();
                this.ObjectIDs[i][3] = (double) volume.getCentroidZ();

                ArrayList measure = (ArrayList) measurements.get(i);

                ListIterator<Number> itr = measure.listIterator();

                int j = 4;
                while (itr.hasNext()) {
                    this.ObjectIDs[i][j] = ((Number) itr.next()).doubleValue();
                    //System.out.println("PROFILING feature array: " + ObjectIDs[i][j]);
                    j++;
                }
                i++;
            }

        } catch (NullPointerException ex) {
            System.out.println(ex);
        }
    }
    
    private int hasColumn(String test){

        int c = 0;
        ListIterator<String> itr = descriptions.listIterator();
        
        while(itr.hasNext()){        
            String str = itr.next();
            if(str.equalsIgnoreCase(test)){return c;}
            c++;
        }
        return -1;
    }

    @Override
    public void addFeatures(String name, ArrayList<ArrayList<Number>> results) {

        int xsel = jComboBoxXaxis.getSelectedIndex();
        int ysel = jComboBoxYaxis.getSelectedIndex();
        int zsel = jComboBoxLUTPlot.getSelectedIndex();
        int ssel = jComboBoxPointSize.getSelectedIndex();
        
        int hasColumn = hasColumn(name);
        
        if(hasColumn > -1){
            
            ListIterator<ArrayList<Number>> itr = measurements.listIterator();
            
            while(itr.hasNext()){
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

        AvailableDataHM = makeAvailableDataHM(descriptions);
        ec.updateFeatureSpace(AvailableDataHM, measurements);

        jComboBoxXaxis.setModel(new DefaultComboBoxModel(this.descriptions.toArray()));
        jComboBoxYaxis.setModel(new DefaultComboBoxModel(this.descriptions.toArray()));
        jComboBoxLUTPlot.setModel(new DefaultComboBoxModel(this.descriptions.toArray()));

        ComboboxToolTipRenderer renderer = new ComboboxToolTipRenderer();
        renderer.setTooltips(descriptionsLabels);

        jComboBoxXaxis.setRenderer(renderer);
        jComboBoxYaxis.setRenderer(renderer);
        jComboBoxLUTPlot.setRenderer(renderer);

        jComboBoxXaxis.setSelectedIndex(xsel);
        jComboBoxYaxis.setSelectedIndex(ysel);
        jComboBoxLUTPlot.setSelectedIndex(zsel);
        jComboBoxPointSize.setSelectedIndex(ssel);

        //x, y, l Labels
        final SelectPlottingDataMenu PlottingPopupXaxis = new SelectPlottingDataMenu(descriptions, MicroExplorer.XAXIS);
        final SelectPlottingDataMenu PlottingPopupYaxis = new SelectPlottingDataMenu(descriptions, MicroExplorer.YAXIS);
        final SelectPlottingDataMenu PlottingPopupLUTaxis = new SelectPlottingDataMenu(descriptions, MicroExplorer.LUTAXIS);

        PlottingPopupXaxis.addPopupMenuAxisListener(this);
        PlottingPopupYaxis.addPopupMenuAxisListener(this);
        PlottingPopupLUTaxis.addPopupMenuAxisListener(this);

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
         

        //rebuild FeatureFrame columns
        makeDataTable();
        ff.updateColumns(ObjectIDs, descriptions);
        ff.pack();
        
        pack();
        onPlotChangeRequest(jComboBoxXaxis.getSelectedIndex(), jComboBoxYaxis.getSelectedIndex(), jComboBoxLUTPlot.getSelectedIndex(), jComboBoxPointSize.getSelectedIndex(), imageGate);
       
        
      
    }

    @Override
    public void addFeatureMap(String name, ArrayList<ArrayList<Number>> al) {
        System.out.println("PROFILING: Adding distance map measurements");
        addFeatures(name, al);
    }

    @Override
    public void axesSetupExplorerPlotUpdate(int x, int y, int l, int pointsize) {
        ExplorerSetupPlotChangerequest(x, y, l, pointsize);
    }

    @Override
    public void addLinkedKey(String linkedKey) {
        this.childKeys.add(linkedKey);
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

    //as borrowed from https://stackoverflow.com/questions/480261/java-swing-mouseover-text-on-jcombobox-items
    public class ComboboxToolTipRenderer extends DefaultListCellRenderer {

        List<String> tooltips;

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {

            JComponent comp = (JComponent) super.getListCellRendererComponent(list,
                    value, index, isSelected, cellHasFocus);

            if (-1 < index && null != value && null != tooltips) {
                list.setToolTipText(tooltips.get(index));
            }
            return comp;
        }

        public void setTooltips(List<String> tooltips) {
            this.tooltips = tooltips;
        }
    }
    //this needs to be runnable

    public class ExportOBJ {

        public ExportOBJ() {
        }

        public void export(String k, ImagePlus imp, ArrayList<MicroObject> objects,
                ArrayList measurements, ArrayList headers,
                ArrayList headerLabels) {

            //Arraylist to save to file
            //key; Objects; Measurements; headers; headerLabels
            //string; ImagePlus; ArrayList; ArrayList; ArrayList; ArrayList
            ArrayList output = new ArrayList();

            output.add(k);
            output.add(objects);
            output.add(measurements);
            output.add(headers);
            output.add(headerLabels);

            int returnVal = JFileChooser.CANCEL_OPTION;
            File file;
            int choice = JOptionPane.OK_OPTION;
            do {
                JFileChooser jf = new JFileChooser(_vtea.LASTDIRECTORY);
                jf.setDialogTitle("Export VTEA objects...");

                returnVal = jf.showSaveDialog(Main);

                file = jf.getSelectedFile();
                if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("obx")) {

                } else {
                    file = new File(file.toString() + ".obx");
                }
                if (file.exists()) {
                    String message = String.format("%s already exists\nOverwrite it?", file.getName());
                    choice = JOptionPane.showConfirmDialog(null, message, "Overwrite File", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                }
            } while (choice != JOptionPane.OK_OPTION);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    try {
                        FileOutputStream fos = new FileOutputStream(file);
                        ObjectOutputStream oos = new ObjectOutputStream(fos);
                        oos.writeObject(output);
                        oos.close();

                        FileSaver fs = new FileSaver(imp);
                        fs.saveAsTiffStack(file.getParent() + "/" + key + ".tif");

                    } catch (IOException e) {
                        System.out.println("ERROR: Could not save the file" + e);
                    }
                } catch (NullPointerException ne) {
                    System.out.println("ERROR: NPE in object export");
                }
                _vtea.LASTDIRECTORY = file.getAbsolutePath();
            } else {
            }
        }

    }

//    public class ImportOBJ {
//
//        public ImportOBJ() {
//        }
//        
//        protected void importObjects() {
//
//            JFileChooser jf = new JFileChooser(_vtea.LASTDIRECTORY);
//            FileNameExtensionFilter filter = 
//            new FileNameExtensionFilter("VTEA object file.", ".obx", "obx");
//            jf.addChoosableFileFilter(filter);
//            jf.setFileFilter(filter);
//            int returnVal = jf.showOpenDialog(Main);
//            File file = jf.getSelectedFile();
//            
//            
//
//            ArrayList result = new ArrayList();
//
//            if (returnVal == JFileChooser.APPROVE_OPTION) {
//                try {
//                    try {
//                        FileInputStream fis = new FileInputStream(file);
//                        ObjectInputStream ois = new ObjectInputStream(fis);
//                        
//                        ProgressMonitorInputStream pm = 
//                        new ProgressMonitorInputStream(Main,"Reading" + file.getName() ,fis);
//                        
//                        result = (ArrayList) ois.readObject();
//                        ois.close(); 
//                        } catch (IOException e) {
//                        System.out.println("ERROR: Could not open the file.");
//                    }
//                    try{
//                        Opener op = new Opener();
//                        ImagePlus imp = op.openImage(file.getParent(), ((String)result.get(0))+".tif");
//                        executeExploring((file.getName()).replace(".obx", ""), result, imp);
//                    } catch (NullPointerException e) {
//                        System.out.println("WARNING: Could not find the tiff file.");
//                        JFileChooser jf2 = new JFileChooser(_vtea.LASTDIRECTORY);
//                        FileNameExtensionFilter filter2 = 
//                        new FileNameExtensionFilter("Tiff file.", ".tif", "tif");
//                        jf.addChoosableFileFilter(filter2);
//                        jf.setFileFilter(filter2);
//                        int returnVal2 = jf2.showOpenDialog(Main);
//                        File image = jf2.getSelectedFile();
//                        
//                        Opener op = new Opener();
//                        ImagePlus imp = op.openImage(file.getParent(), image.getName());
//                        executeExploring((file.getName()).replace(".obx", ""), result, imp);
//                    }
//                    
//                        
//    
//                    
//                } catch (ClassNotFoundException ne) {
//                    System.out.println("ERROR: Not Found.");
//                }
//            } else {
//            }
//            
//        }
//        
//        private void executeExploring(String name, ArrayList result, ImagePlus imp){
//
//        String k = (String)result.get(0);
//        ArrayList<MicroObject> objects = (ArrayList<MicroObject>)result.get(1);
//        ArrayList measures = (ArrayList)result.get(2);
//        ArrayList descriptions = (ArrayList)result.get(3);
//        ArrayList descriptionLabels = (ArrayList)result.get(4);
//            
//        ExplorerProcessor ep = new ExplorerProcessor(name, imp, objects, measures, descriptions, descriptionLabels);
//        ep.execute();
//
//        }
//
//    }
//

//    
    class ExportCSV {

        public ExportCSV() {
        }

        protected void export(ArrayList header, ArrayList al) {
            File file;
            int returnVal = JFileChooser.CANCEL_OPTION;
            int choice = JOptionPane.OK_OPTION;
            do {
                JFileChooser jf = new JFileChooser(_vtea.LASTDIRECTORY);

                returnVal = jf.showSaveDialog(Main);

                file = jf.getSelectedFile();

                file = jf.getSelectedFile();
                if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("csv")) {

                } else {
                    file = new File(file.toString() + ".csv");
                }

                if (file.exists()) {
                    String message = String.format("%s already exists\nOverwrite it?", file.getName());
                    choice = JOptionPane.showConfirmDialog(null, message, "Overwrite File", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                }
            } while (choice != JOptionPane.OK_OPTION);
            if (returnVal == JFileChooser.APPROVE_OPTION) {

                try {

                    try {

                        PrintWriter pw = new PrintWriter(file);
                        StringBuilder sb = new StringBuilder();

                        ListIterator itr = descriptions.listIterator();

                        sb.append("Object");
                        sb.append(',');
                        sb.append("PosX,PosY,PosZ,");
                        while (itr.hasNext()) {
                            sb.append((String) itr.next());
                            if (itr.hasNext()) {
                                sb.append(',');
                            }
                        }

                        sb.append('\n');

                        ArrayList<MicroObject> objects = ec.getObjects();
                        ArrayList<ArrayList<Number>> measurements = ec.getMeasurments();

                        for (int i = 0; i < objects.size(); i++) {

                            MicroObject volume = objects.get(i);
                            ArrayList<Number> measured = measurements.get(i);

                            sb.append(volume.getSerialID());
                            sb.append(',');
                            sb.append(volume.getCentroidX());
                            sb.append(',');
                            sb.append(volume.getCentroidY());
                            sb.append(',');
                            sb.append(volume.getCentroidZ());

                            ListIterator<Number> itr_mes = measured.listIterator();

                            while (itr_mes.hasNext()) {

                                sb.append(",");
                                sb.append(itr_mes.next());
                            }
                            sb.append('\n');
                        }

                        pw.write(sb.toString());
                        pw.close();

                    } catch (FileNotFoundException e) {
                    }

                } catch (NullPointerException ne) {
                }
                _vtea.LASTDIRECTORY = file.getPath();
            } else {
            }

        }

    }

}
