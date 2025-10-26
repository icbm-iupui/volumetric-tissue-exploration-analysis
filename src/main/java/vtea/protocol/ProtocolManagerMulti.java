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

import com.formdev.flatlaf.FlatLightLaf;
import ij.ImagePlus;
import ij.WindowManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static java.awt.event.InputEvent.BUTTON2_DOWN_MASK;
import java.awt.event.KeyEvent;
import static java.awt.event.MouseEvent.BUTTON3;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AbstractDocument;
import vtea.ImageSelectionListener;
import vtea.OpenImageWindow;
import vtea.OpenObxFormat;
import vtea._vtea;
import vtea.util.BackgroundTaskHelper;
import static vtea._vtea.PROCESSINGMAP;
import static vtea._vtea.SEGMENTATIONMAP;
import vtea.imageprocessing.AbstractImageProcessing;
import vtea.objects.Segmentation.AbstractSegmentation;
import vtea.processor.ImageProcessingProcessor;
import vtea.protocol.SingleImageProcessing.ObjectStepBlockGUI;
import vtea.protocol.blockstepgui.ProcessStepBlockGUI;
import vtea.protocol.listeners.BatchStateListener;
import vtea.protocol.listeners.CopyBlocksListener;
import vtea.protocol.listeners.FileOperationListener;
import vtea.protocol.listeners.RenameTabListener;
import vtea.protocol.listeners.RepaintTabListener;
import vtea.protocol.listeners.RequestImageListener;
import vtea.protocol.listeners.TransferProtocolStepsListener;
import vtea.protocol.menus.AvailableWorkflowsMenu;
import vtea.protocol.menus.JMenuProtocol;
import vtea.protocol.menus.LengthFilter;
import vtea.protocol.setup.MicroBlockObjectSetup;
import vteaexploration.GateManager;
import vteaexploration.MicroExplorer;

/**
 *
 * @author winfrees
 */
public class ProtocolManagerMulti extends javax.swing.JFrame implements FileOperationListener, ImageSelectionListener, RequestImageListener, RepaintTabListener, RenameTabListener, CopyBlocksListener, BatchStateListener, ActionListener {

    private static int WORKFLOW = 0;
    private static int PROCESSBLOCKS = 1;
    private static int OBJECTBLOCKS = 2;

    public static final int PROCESS = 100;
    public static final int OBJECT = 110;
    public static final int EXPLORATION = 120;

    public JList OpenImages;
    public OpenImageWindow openerWindow = new OpenImageWindow(OpenImages);

    protected JPanel thumbnail;
    protected GateManager gateManager;

    protected boolean batch = false;

    public AvailableWorkflowsMenu awf = new AvailableWorkflowsMenu();

    private ArrayList<TransferProtocolStepsListener> listeners = new ArrayList<>();

    public JMenuProtocol ProcessingMenu;
    public JMenuProtocol ObjectMenu;

    private JPopupMenu menu = new JPopupMenu();

    public JWindow thumb = new JWindow();

    public GridLayout PreProcessingLayout = new GridLayout(5, 1, 0, 0);
    public GridLayout ObjectLayout = new GridLayout(5, 1, 0, 0);
    public GridLayout ExploreLayout = new GridLayout(5, 1, 0, 0);

    public Color ButtonBackground = new java.awt.Color(102, 102, 102);
    public String[] Channels;

    private MicroExperiment me = new MicroExperiment();

    private ArrayList<JPanel> Tabs = new ArrayList<>();

    //static ResultsTable rt;
    /**
     * Creates new form protocolManager
     */
    public ProtocolManagerMulti() {

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(MicroExplorer.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Initialize all components before any layout/pack operations
        openerWindow.addImageSelectionListener(this);
        GuiSetup();
        initComponents();
        addGateManager();
        addNewTabTab();
        addMenuItems();
        addSingleImagePanel();

        this.ImageTabs.setTabPlacement(JTabbedPane.TOP);
        this.ImageTabs.setSelectedIndex(ImageTabs.getTabCount() - 1);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                _vtea.setLastDirectory(_vtea.getLastDirectory());
                _vtea.clearVTEADirectory();
            }
        });

        // Apply LAF and layout ONCE at the end after all components added
        SwingUtilities.updateComponentTreeUI(this);
        this.pack();
        //IJ.log("Starting things up!");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        PreProcessing_Contextual = new javax.swing.JPopupMenu();
        Object_Contextual = new javax.swing.JPopupMenu();
        Explore_Contextual = new javax.swing.JPopupMenu();
        jPopUpAddParallelAnalysis = new javax.swing.JPopupMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jPopUpAvailableProtocols = new javax.swing.JPopupMenu();
        ImageTabs = new javax.swing.JTabbedPane();
        MenuBar = new javax.swing.JMenuBar();
        Settings = new javax.swing.JMenu();
        Default_Edit = new javax.swing.JMenu();

        jPopUpAddParallelAnalysis.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPopUpAddParallelAnalysisMouseClicked(evt);
            }
        });

        jMenuItem1.setText("jMenuItem1");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jPopUpAddParallelAnalysis.add(jMenuItem1);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("VTEA Protocols v." + vtea._vtea.VERSION);
        setAutoRequestFocus(false);
        setBackground(new java.awt.Color(204, 204, 204));
        setBounds(new java.awt.Rectangle(0, 100, 890, 400));
        // Allow window resizing for better flexibility and high-DPI support
        setMinimumSize(new java.awt.Dimension(767, 472));
        setPreferredSize(new java.awt.Dimension(767, 472));
        setName("ProcessingFrame"); // NOI18N
        setResizable(true); // Changed from false to allow user resizing
        setSize(new java.awt.Dimension(767, 472));
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                formMousePressed(evt);
            }
        });
        getContentPane().setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 1, 1));

        ImageTabs.setBackground(vtea._vtea.ACTIONPANELBACKGROUND);
        ImageTabs.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        ImageTabs.setToolTipText("");
        ImageTabs.setMaximumSize(new java.awt.Dimension(780, 460));
        ImageTabs.setMinimumSize(new java.awt.Dimension(760, 440));
        ImageTabs.setPreferredSize(new java.awt.Dimension(760, 440));
        ImageTabs.setRequestFocusEnabled(false);
        ImageTabs.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                ImageTabsMouseReleased(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ImageTabsMouseClicked(evt);
            }
        });
        ImageTabs.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                ImageTabsComponentResized(evt);
            }
        });
        ImageTabs.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ImageTabsKeyPressed(evt);
            }
        });
        getContentPane().add(ImageTabs);

        MenuBar.setPreferredSize(new java.awt.Dimension(910, 22));

        Settings.setText("Settings");
        MenuBar.add(Settings);

        Default_Edit.setText("Edit");
        MenuBar.add(Default_Edit);

        setJMenuBar(MenuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ImageTabsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ImageTabsMouseClicked
        if (evt.getModifiersEx() == KeyEvent.VK_DELETE) {
            if (this.ImageTabs.getSelectedIndex() > 1) {
                JFrame frame = new JFrame();
                frame.setBackground(vtea._vtea.BUTTONBACKGROUND);
                String[] options = {"Delete", "Cancel"};

                int result = JOptionPane.showOptionDialog(null, frame,
                        "Are you sure you want to delete tab, " + ImageTabs.getTitleAt(ImageTabs.getSelectedIndex()) + "?",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        options,
                        null);

                if (result == JOptionPane.OK_OPTION) {
                    this.ImageTabs.remove(this.ImageTabs.getSelectedIndex());
                    this.ImageTabs.setSelectedIndex(1);
                } else {
                    this.ImageTabs.setSelectedIndex(this.ImageTabs.getSelectedIndex());
                }
            } else {
                this.ImageTabs.setSelectedIndex(this.ImageTabs.getSelectedIndex());
            }
        }// TODO add your handling code here:        
    }//GEN-LAST:event_ImageTabsMouseClicked

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jPopUpAddParallelAnalysisMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPopUpAddParallelAnalysisMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jPopUpAddParallelAnalysisMouseClicked

    private void ImageTabsComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_ImageTabsComponentResized
        this.repaint();        // TODO add your handling code here:
    }//GEN-LAST:event_ImageTabsComponentResized

    private void ImageTabsMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ImageTabsMouseReleased
        JTabbedPane jtp = (JTabbedPane) evt.getComponent();
        String tabtitle = jtp.getTitleAt(jtp.getSelectedIndex());
        int onmask = BUTTON2_DOWN_MASK;

        if (menu.getComponentCount() < 3) {
            createPopUpMenu();
        }

        //if(SwingUtilities.isRightMouseButton(evt)){ParallelFileMenu pfm = new ParallelFileMenu(); pfm.show(evt.getComponent(), evt.getX(), evt.getY());}
        if (evt.getClickCount() > 1 && tabtitle.equals("Add Workflow")) {
            addSingleImagePanel();
            refreshMenuItems();
            this.ImageTabs.setSelectedIndex(ImageTabs.getTabCount() - 1);
            evt.consume();

        } else if (evt.getClickCount() == 1 && tabtitle.equals("Add Workflow")) {
            addSingleImagePanel();
            refreshMenuItems();
            this.ImageTabs.setSelectedIndex(ImageTabs.getTabCount() - 1);
            evt.consume();

        } else if (evt.getClickCount() == 1 && (evt.getButton() == BUTTON3)) {

            if (this.ImageTabs.getTabCount() <= 2) {
                menu.getComponent(3).setEnabled(false);
            } else {
                menu.getComponent(3).setEnabled(true);
            }
            menu.show(evt.getComponent(), evt.getX(), evt.getY());
            refreshMenuItems();
        }
        if (jtp.getTabCount() == 19) {
            jtp.setEnabledAt(0, false);
        }

        if (!rebuildPanels()) {

            this.ImageTabs.setSelectedIndex(ImageTabs.getTabCount() - 1);
        }
    }//GEN-LAST:event_ImageTabsMouseReleased

    private void ImageTabsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ImageTabsKeyPressed

        if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
            if (this.ImageTabs.getSelectedIndex() > 1 || this.ImageTabs.getTabCount() > 2) {

                JPanel panel = new JPanel();
                JLabel text = new JLabel("Are you sure you want to delete tab, " + ImageTabs.getTitleAt(ImageTabs.getSelectedIndex()) + "?");
                //panel.setBackground(VTC.VTEAService.BUTTONBACKGROUND);  
                panel.add(text);
                String[] options = {"Delete", "Cancel"};

                int result = JOptionPane.showOptionDialog(null, panel,
                        "Delete " + ImageTabs.getTitleAt(ImageTabs.getSelectedIndex()),
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        options,
                        null);

                if (result == JOptionPane.OK_OPTION) {
                    Tabs.remove(this.ImageTabs.getSelectedIndex() - 1);
                    ImageTabs.remove(this.ImageTabs.getSelectedIndex());

                    for (int i = 1; i < Tabs.size(); i++) {
                        SingleImageProcessing sip = (SingleImageProcessing) Tabs.get(i);
                        sip.setTabValue(i);
                    }

                    this.ImageTabs.setSelectedIndex(1);
                } else {
                    this.ImageTabs.setSelectedIndex(this.ImageTabs.getSelectedIndex());
                }
            } else {
                this.ImageTabs.setSelectedIndex(this.ImageTabs.getSelectedIndex());
            }
        }// TODO add your handling code here:        
    }//GEN-LAST:event_ImageTabsKeyPressed

    private void formMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMousePressed
        // TODO add your handling code here:
    }//GEN-LAST:event_formMousePressed

    /**
     * @param args the command line arguments
     */
    private void GuiSetup() {

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                //new pipelineManager().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu Default_Edit;
    private javax.swing.JPopupMenu Explore_Contextual;
    public javax.swing.JTabbedPane ImageTabs;
    protected javax.swing.JMenuBar MenuBar;
    private javax.swing.JPopupMenu Object_Contextual;
    private javax.swing.JPopupMenu PreProcessing_Contextual;
    private javax.swing.JMenu Settings;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPopupMenu jPopUpAddParallelAnalysis;
    private javax.swing.JPopupMenu jPopUpAvailableProtocols;
    // End of variables declaration//GEN-END:variables

    //
    private void addGateManager() {
        GateManager gateManager = new GateManager();
    }

    public void addSingleImagePanel() {

        SingleImageProcessing NewPanel = new SingleImageProcessing();

        openerWindow.addImageSelectionListener(NewPanel);
        NewPanel.addRequestImageListener(this);
        NewPanel.addRepaintTabListener(this);

        Tabs.add(NewPanel);

        addTransferProtocolStepsListener(NewPanel);

        ImageTabs.addTab("Image_" + this.Tabs.indexOf(NewPanel), NewPanel);
        NewPanel.setName("Image_" + this.Tabs.indexOf(NewPanel));

        if (ImageTabs.getTabCount() > 2) {
            refreshMenuItems();
        } else {
            addMenuItems();
        }
        //createPopUpMenu();

    }

    private void createPopUpMenu() {
        this.menu = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("Rename...");

        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu.add(new JSeparator());

        menuItem = new JMenuItem("Duplicate");

        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Delete");

        menuItem.addActionListener(this);
        menu.add(menuItem);

        //Add listener to the text area so the popup menu can come up.
//        MouseListener popupListener = new PopupListener(menu);
//        layer.addMouseListener(popupListener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
//        System.out.println(e.getActionCommand());
        if (e.getActionCommand().equals("Rename...")) {
            JFrame frame = new JFrame();
            frame.setBackground(vtea._vtea.BUTTONBACKGROUND);
            String name = JOptionPane.showInputDialog(null, "Rename to:",
                    ImageTabs.getTitleAt(this.ImageTabs.getSelectedIndex()));

            if (!name.equalsIgnoreCase(ImageTabs.getTitleAt(this.ImageTabs.getSelectedIndex()))) {
                ImageTabs.setTitleAt(ImageTabs.getSelectedIndex(), name);
                SingleImageProcessing sip = (SingleImageProcessing) ImageTabs.getComponentAt(this.ImageTabs.getSelectedIndex());
                sip.setName(name);
                addMenuItems();

            } else {
                ImageTabs.setSelectedIndex(this.ImageTabs.getSelectedIndex());
            }

        } else if (e.getActionCommand().equals("Delete")) {
            JPanel panel = new JPanel();
            JLabel text = new JLabel("Are you sure you want to delete tab, " + ImageTabs.getTitleAt(ImageTabs.getSelectedIndex()) + "?");
            //panel.setBackground(VTC.VTEAService.BUTTONBACKGROUND);  
            panel.add(text);
            String[] options = {"Delete", "Cancel"};

            int result = JOptionPane.showOptionDialog(null, panel,
                    "Delete " + ImageTabs.getTitleAt(ImageTabs.getSelectedIndex()),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    options,
                    null);

            if (result == JOptionPane.OK_OPTION) {
                Tabs.remove(this.ImageTabs.getSelectedIndex() - 1);
                ImageTabs.remove(this.ImageTabs.getSelectedIndex());

                for (int i = 1; i < Tabs.size(); i++) {
                    SingleImageProcessing sip = (SingleImageProcessing) Tabs.get(i);
                    sip.setTabValue(i);
                }

                this.ImageTabs.setSelectedIndex(1);
                refreshMenuItems();
            } else {
                this.ImageTabs.setSelectedIndex(this.ImageTabs.getSelectedIndex());
            }

        } else if (e.getActionCommand().equals("Duplicate")) {

        }

    }

    public void addBatchPanel(String selected, ArrayList tabs) {

        ArrayList<String> al = getAllTabNames();

        BatchImageProcessing NewPanel = new BatchImageProcessing(getAllTabNames(), al.indexOf(selected));
        this.Tabs.add(NewPanel);
        this.ImageTabs.addTab("Batch" + this.Tabs.indexOf(NewPanel), NewPanel);
        JTextField label = new JTextField("Batch");
        label.setEditable(true);
        label.setMaximumSize(new Dimension(30, 50));
        label.setMargin(new Insets(3, 0, 0, 0));
        label.setBackground(new Color(0, 0, 0, 0));
        label.setBorder(BorderFactory.createEmptyBorder());
        ((AbstractDocument) label.getDocument()).setDocumentFilter(new LengthFilter(7));

        this.ImageTabs.setTabComponentAt(this.Tabs.indexOf(NewPanel) + 1, label);
        NewPanel.setTabValue(this.Tabs.size() - 1);

        SingleImageProcessing sip = (SingleImageProcessing) (this.ImageTabs.getComponentAt(tabs.indexOf(selected)));

        NewPanel.setPreProcessingProtocols(sip.getProProcessingProtocol());
        NewPanel.setObjectProcotols(sip.getObjectStepsList());
        NewPanel.setProtocolSynopsis(sip.getProProcessingProtocol(), sip.getObjectStepsList());

    }

    public void addMenuItems() {
        //this.WorkflowMenu = new JMenuProtocol("Workflow", this.getTabNames(), SingleImageProcessing.WORKFLOW);
        this.ProcessingMenu = new JMenuProtocol("Start", this.getTabNames(), SingleImageProcessing.PROCESSBLOCKS);
        this.ObjectMenu = new JMenuProtocol("Explorer", this.getTabNames(), SingleImageProcessing.OBJECTBLOCKS);

        ProcessingMenu.addStepCopierListener(this);
        ObjectMenu.addStepCopierListener(this);

        ProcessingMenu.addFileOperationListener(this);
        ObjectMenu.addFileOperationListener(this);

//        ListIterator<JPanel> itr = Tabs.listIterator();
//        
//        //while(itr.hasNext()){            
//            SingleImageProcessing sip = (SingleImageProcessing)itr.next();    
//            
//            //           
//        //}
        this.MenuBar.removeAll();
        //this.MenuBar.add(WorkflowMenu);
        this.MenuBar.add(ProcessingMenu);
        //this.MenuBar.add(ObjectMenu);
    }

    public void refreshMenuItems() {

        this.MenuBar.removeAll();
        this.MenuBar.add(this.Settings);
        this.MenuBar.add(this.Default_Edit);

        addMenuItems();
    }

    public boolean rebuildPanels() {
        try {
            SingleImageProcessing sip;
            BatchImageProcessing bip;

            //if (!((JTextField) ImageTabs.getTabComponentAt(ImageTabs.getSelectedIndex())).getText().equals("Batch")) {
            sip = (SingleImageProcessing) (ImageTabs.getComponentAt(ImageTabs.getSelectedIndex()));

            sip.RebuildPanelProcessing();
            sip.RebuildPanelObject();
            //}
        } catch (NullPointerException npe) {
            return false;
        }
        return true;

    }

    public void addNewTabTab() {
        this.ImageTabs.addTab("Add Workflow", new JPanel());
    }

    public ArrayList getTabNames() {

        ArrayList<String> titles = new ArrayList<String>();

        int currenttab = ImageTabs.getSelectedIndex();

        for (int i = 1; i < ImageTabs.getTabCount(); i++) {
            if (i != currenttab) {
                titles.add(ImageTabs.getTitleAt(i));
            }
        }
        return titles;

    }

    public ArrayList getAllTabNames() {
        ArrayList<String> titles = new ArrayList<String>();
        for (int i = 1; i <= ImageTabs.getTabCount() - 1; i++) {
            //titles.add(ImageTabs.getTitleAt(i)); 
            if (!((JTextField) ImageTabs.getTabComponentAt(i)).getText().equals("Batch")) {
                titles.add(((JTextField) ImageTabs.getTabComponentAt(i)).getText());
            }
        }
        return titles;
    }

    public void UpdateImageList() {
        openerWindow.updateImages();
        if(WindowManager.getImageCount()==0){
            setEnabledLoadButtons(false);
        } else {
            setEnabledLoadButtons(true);
        }
    }
    
    private void setEnabledLoadButtons(boolean b) {  
      for(int i = 0; i < Tabs.size(); i++){
        SingleImageProcessing p = (SingleImageProcessing)Tabs.get(i);
        p.LoadImage.setEnabled(b);
      }
    }

    public void openImage(int tab) {
        this.openerWindow.getImageFile(tab);
    }

    public void addTransferProtocolStepsListener(TransferProtocolStepsListener listener) {
        listeners.add(listener);
    }

    private void notifyTransferProtocolStepsListeners(int type, int tab, String arg) {
        for (TransferProtocolStepsListener listener : listeners) {
            listener.transferThese(type, tab, arg);
        }
    }

    @Override
    public void onSelect(ImagePlus imp, int tab) {

        ListIterator itr = Tabs.listIterator();

        while (itr.hasNext()) {
            SingleImageProcessing s = (SingleImageProcessing) itr.next();
            s.setSelectedTab(false);
        }

        SingleImageProcessing sip = (SingleImageProcessing) Tabs.get(tab);
        sip.setImage(imp, tab);
        sip.setSelectedTab(true);
        pack();
    }

    @Override
    public void onRequest(int tab) {
        openImage(tab);
    }

    @Override
    public void repaintTab() {
        this.repaint();
    }

    @Override
    public void renameTab(int tab) {
        JTextField label = new JTextField("Image_" + tab);
        label.setEditable(true);
        this.ImageTabs.setTabComponentAt(tab, label);
    }

    @Override
    public void onCopy(String source, int StepsList) {

        //this is used in copying  
        SingleImageProcessing SourceSIP;
        SingleImageProcessing DestinationSIP;

//        System.out.println("__________________________________________________");
//        System.out.println("PROFILING: Copying image processing from " + source);
        DestinationSIP = (SingleImageProcessing) (ImageTabs.getComponentAt(ImageTabs.getSelectedIndex()));

        for (int i = 1; i <= ImageTabs.getTabCount() - 1; i++) {

            if (ImageTabs.getTitleAt(i).equals(source)) {

                SourceSIP = (SingleImageProcessing) (ImageTabs.getComponentAt(i));

                switch (StepsList) {
                    case SingleImageProcessing.PROCESSBLOCKS:

                        ArrayList<ProcessStepBlockGUI> destinationProtocol
                                = DestinationSIP.getProcessStepsList();
                        ArrayList<ProcessStepBlockGUI> sourceProtocol
                                = SourceSIP.getProcessStepsList();

//                        //System.out.println("PROFILING: Processing " 
//                                + sourceProtocol.size() + 
//                                " image processing blocks.");
                        for (int j = 1; j < sourceProtocol.size(); j++) {
//                            System.out.println("PROFILING: "
//                                    + "Adding processing step " + j);
                            DestinationSIP.addPreprocessingBlock();
                            DestinationSIP.UpdatePositionProcessing(1);
                            DestinationSIP.RebuildPanelProcessing();

                            ProcessStepBlockGUI destinationStep
                                    = DestinationSIP.getProcessStepsList().get(j);
                            ProcessStepBlockGUI sourceStep
                                    = SourceSIP.getProcessStepsList().get(j);

                            destinationStep.mbs.setChannel(sourceStep.mbs.getChannel());
                            destinationStep.mbs.setMethod(sourceStep.mbs.getMethod());
                            destinationStep.mbs.setSetup(
                                    makeProcessingMethodsArray("version", sourceStep.mbs.getMethod(),
                                            sourceStep.mbs.getProcessList()));
                            destinationStep.mbs.blockSetupOKAction();
                        }

                        break;

                    case SingleImageProcessing.OBJECTBLOCKS:
                        ArrayList<SingleImageProcessing.ObjectStepBlockGUI> destinationObjectProtocol
                                = DestinationSIP.getObjectStepsList();
                        ArrayList<SingleImageProcessing.ObjectStepBlockGUI> sourceObjectProtocol
                                = SourceSIP.getObjectStepsList();
//                        //System.out.println("PROFILING: Object " 
//                                + sourceObjectProtocol.size() + 
//                                " image processing blocks.");

                        for (int j = 1; j < sourceObjectProtocol.size(); j++) {
//                            System.out.println("PROFILING: "
//                                    + "Adding object step " + j);
                            DestinationSIP.addObjectBlock();
                            //DestinationSIP.UpdatePositionProcessing(1);
                            DestinationSIP.RebuildPanelObject();

                            SingleImageProcessing.ObjectStepBlockGUI destinationStep
                                    = DestinationSIP.getObjectStepsList().get(j);
                            SingleImageProcessing.ObjectStepBlockGUI sourceStep
                                    = SourceSIP.getObjectStepsList().get(j);

                            /**
                             * This all needs help to implement copy and paste
                             * of segmentation methods and saving of image
                             * processing
                             */
//                           destinationStep.mbs.setChannel(sourceStep.mbs.g);
//                           destinationStep.mbs.setMethod(sourceStep.mbs.getMethod());
//                           destinationStep.mbs.setSetup(
//                                   makeMethodsArray("version",sourceStep.mbs.getMethod(), 
//                                           sourceStep.mbs.getProcessList()));
//                           destinationStep.mbs.blockSetupOKAction();
                        }

                        //need to add the populating of the Setup window here and keep it flexible for 
                        //importing settings.
                        break;

                    default:
                        break;
                }
            }
        }
    }

    private ArrayList makeProcessingMethodsArray(String version, String method, ArrayList sourceSettings) {
        Object iImp = new Object();

        ArrayList result = new ArrayList();

        try {
            Class<?> c;
            c = Class.forName(PROCESSINGMAP.get(method));
            Constructor<?> con;
            try {
                con = c.getConstructor();
                iImp = con.newInstance();

                ((AbstractImageProcessing) iImp).copyComponentParameter(version, result, sourceSettings);

            } catch (NullPointerException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(ImageProcessingProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (NullPointerException | ClassNotFoundException ex) {
            Logger.getLogger(ImageProcessingProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private ArrayList saveObjectMethodsArray(String version, String method, ArrayList sourceSettings) {
        Object iImp = new Object();

        ArrayList result = new ArrayList();

        try {
            Class<?> c;
            c = Class.forName(SEGMENTATIONMAP.get(method));
            Constructor<?> con;
            try {
                con = c.getConstructor();
                iImp = con.newInstance();

                ((AbstractSegmentation) iImp).saveComponentParameter(version, result, sourceSettings);

            } catch (NullPointerException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(ImageProcessingProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (NullPointerException | ClassNotFoundException ex) {
            Logger.getLogger(ImageProcessingProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private void makeObjectMethodsArray(String version, String method,
            ArrayList fields, ArrayList dComponents) {
        Object iImp = new Object();

        //ArrayList result = new ArrayList();
//        for(int i = 0; i < fields.size(); i++){
//            //System.out.println("PROFILING:  Importing field value: " + fields.get(i));
//        }
//         //System.out.println("PROFILING:  To import to: " + dComponents.size() + " fields.");
//        
//        
        try {
            Class<?> c;
            c = Class.forName(SEGMENTATIONMAP.get(method));
            Constructor<?> con;
            try {
                con = c.getConstructor();
                iImp = con.newInstance();

                ((AbstractSegmentation) iImp).loadComponentParameter(version, dComponents, fields);

            } catch (NullPointerException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(ImageProcessingProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (NullPointerException | ClassNotFoundException ex) {
            Logger.getLogger(ImageProcessingProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void batchStateAdd(String selected, ArrayList tabs) {
        this.addBatchPanel(selected, tabs);
    }

    @Override
    public int onProccessingFileOpen() throws Exception {

        SingleImageProcessing DestinationSIP;
        DestinationSIP = (SingleImageProcessing) (ImageTabs.getComponentAt(ImageTabs.getSelectedIndex()));

        JFileChooser chooser = new JFileChooser(_vtea.LASTDIRECTORY);
        FileNameExtensionFilter filter
                = new FileNameExtensionFilter("VTEA processing file.", ".prc", "prc");
        chooser.addChoosableFileFilter(filter);
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {

            File file = chooser.getSelectedFile();
            _vtea.LASTDIRECTORY = file.getAbsolutePath();
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object temp = ois.readObject();

            ArrayList<ArrayList> sourceProtocol = (ArrayList) temp;

            ArrayList<ProcessStepBlockGUI> protocolExisting = DestinationSIP.getProcessStepsList();

            ProcessStepBlockGUI image = protocolExisting.get(0);

            DestinationSIP.ProcessingStepsList.clear();

            DestinationSIP.ProcessingStepsList.add(image);

            DestinationSIP.rebuildProcessingGUI();

            for (int j = 0; j < sourceProtocol.size(); j++) {

                ArrayList sourceStep = sourceProtocol.get(j);

                DestinationSIP.addPreprocessingBlock();
                DestinationSIP.UpdatePositionProcessing(1);
                DestinationSIP.RebuildPanelProcessing();

                ProcessStepBlockGUI destinationStep
                        = DestinationSIP.getProcessStepsList().get(j + 1);

                destinationStep.mbs.setChannel((int) sourceStep.get(0));
                destinationStep.mbs.setMethod((String) sourceStep.get(1));
                destinationStep.mbs.setSetup(makeProcessingMethodsArray("version",
                        (String) sourceStep.get(1),
                        (ArrayList) sourceStep.get(2)));
                destinationStep.mbs.blockSetupOKAction();

            }

            repaint();
            ois.close();
            _vtea.LASTDIRECTORY = file.getAbsolutePath();
            return 1;
        } else {
            repaint();

            return -1;
        }
    }

    @Override
    public void onProcessingFileSave() throws Exception {
        File file;
        int choice = JOptionPane.OK_OPTION;
        do {
            JFileChooser chooser = new JFileChooser(_vtea.LASTDIRECTORY);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("VTEA processing file.", ".prc", "prc");
            chooser.setFileFilter(filter);
            chooser.showSaveDialog(this);
            file = chooser.getSelectedFile();

            _vtea.LASTDIRECTORY = file.getAbsolutePath();

            String filename = file.getName();
            if (!filename.endsWith(".prc")) {
                String path = file.getPath();
                path += ".prc";
                file = new File(path);
            }

            if (file.exists()) {
                String message = String.format("%s already exists\nOverwrite it?", file.getName());
                choice = JOptionPane.showConfirmDialog(null, message, "Overwrite File", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            }
        } while (choice != JOptionPane.OK_OPTION);

        SingleImageProcessing SourceSIP;
        SourceSIP = (SingleImageProcessing) (ImageTabs.getComponentAt(ImageTabs.getSelectedIndex()));

        ArrayList<ProcessStepBlockGUI> sourceProtocol = SourceSIP.getProcessStepsList();
        ArrayList<ArrayList> destinationProtocol = new ArrayList<ArrayList>();

        ArrayList<ArrayList> protocol = new ArrayList<ArrayList>();

        for (int i = 1; i < sourceProtocol.size(); i++) {

            ArrayList stepResult = new ArrayList();

            ProcessStepBlockGUI stepSource = sourceProtocol.get(i);

            stepResult.add(stepSource.mbs.getChannel());
            stepResult.add(stepSource.mbs.getMethod());
            stepResult.add(stepSource.mbs.getProcessList());

            destinationProtocol.add(stepResult);

        }

        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(destinationProtocol);
        oos.close();
        repaint();
    }

    @Override
    public void onFileExport() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onSegmentationFileOpen() throws Exception {
        SingleImageProcessing DestinationSIP;

        DestinationSIP = (SingleImageProcessing) (ImageTabs.getComponentAt(ImageTabs.getSelectedIndex()));
        int startingCount = DestinationSIP.getObjectStepsList().size();
        JFileChooser chooser = new JFileChooser(_vtea.LASTDIRECTORY);
        FileNameExtensionFilter filter
                = new FileNameExtensionFilter("VTEA segementation file.", ".seg", "seg");
        chooser.addChoosableFileFilter(filter);
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            _vtea.LASTDIRECTORY = file.getAbsolutePath();
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object temp = ois.readObject();

            ArrayList<ArrayList> sourceProtocol = (ArrayList) temp;

            for (int j = 0; j < sourceProtocol.size(); j++) {

                //System.out.println("PROFILING:  Found " + sourceProtocol.size() + " protocols to open.");
                ArrayList sourceStep = sourceProtocol.get(j);

                DestinationSIP.addObjectBlock();
                DestinationSIP.UpdatePositionObject(1);
                DestinationSIP.RebuildPanelObject();

                ObjectStepBlockGUI destinationStep
                        = DestinationSIP.getObjectStepsList().get(j + startingCount);

                /**
                 * segmentation and measurement protocol redefining. 0: title
                 * text, 1: method (as String), 2: channel, 3: ArrayList of
                 * JComponents used for analysis 4: ArrayList of Arraylist for
                 * morphology determination
                 */
                destinationStep.mbs.setTitle((String) sourceStep.get(0));
                destinationStep.mbs.setMethod((String) sourceStep.get(1));
                destinationStep.mbs.setChannel((int) sourceStep.get(2));

                //System.out.println("PROFILING:  Found " +  ((ArrayList)sourceStep.get(3)).size() + " steps.");
                makeObjectMethodsArray("version", (String) sourceStep.get(1),
                        (ArrayList) sourceStep.get(3),
                        (ArrayList) destinationStep.mbs.getSegmentation().getOptions());
//                           
                //need to add the morphology ArrayList here.

                destinationStep.mbs.blockSetupOKAction();

            }

            repaint();
            ois.close();

        } else {
            repaint();

        }
    }
    
    

    @Override
    public void onSegmentationFileSave() throws Exception {
        int choice = JOptionPane.OK_OPTION;
        File file;
        do {
            JFileChooser chooser = new JFileChooser(_vtea.LASTDIRECTORY);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("VTEA segementation file.", ".seg", "seg");
            chooser.setFileFilter(filter);
            chooser.showSaveDialog(this);
            file = chooser.getSelectedFile();

            _vtea.LASTDIRECTORY = file.getAbsolutePath();

            String filename = file.getName();
            if (!filename.endsWith(".seg")) {
                String path = file.getPath();
                path += ".seg";
                file = new File(path);
            }

            if (file.exists()) {
                String message = String.format("%s already exists\nOverwrite it?", file.getName());
                choice = JOptionPane.showConfirmDialog(null, message, "Overwrite File", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            }
        } while (choice != JOptionPane.OK_OPTION);

        SingleImageProcessing SourceSIP;
        SourceSIP = (SingleImageProcessing) (ImageTabs.getComponentAt(ImageTabs.getSelectedIndex()));

        ArrayList<ObjectStepBlockGUI> sourceProtocol = SourceSIP.getObjectStepsList();

        System.out.println("PROFILING:  Found " + sourceProtocol.size() + " protocols to save.");
        ArrayList<ArrayList> destinationProtocol = new ArrayList<ArrayList>();

        for (int i = 0; i < sourceProtocol.size(); i++) {

            ArrayList stepResult = new ArrayList();

            ObjectStepBlockGUI stepSource = sourceProtocol.get(i);

            //destinationProtocol.add(((MicroBlockObjectSetup)(stepSource.mbs)).getSettings());
            /**
             * segmentation and measurement protocol redefining. 0: title text,
             * 1: method (as String), 2: channel, 3: ArrayList of JComponents
             * used for analysis 4: ArrayList of Arraylist for morphology
             * determination
             */
            //System.out.println("PROFILING:  Adding " + ((MicroBlockObjectSetup)(stepSource.mbs)).getTitle());
            //System.out.println("PROFILING:  Adding " + ((MicroBlockObjectSetup)(stepSource.mbs)).getMethod());
            //System.out.println("PROFILING:  Adding " + ((MicroBlockObjectSetup)(stepSource.mbs)).getChannel());
            stepResult.add(((MicroBlockObjectSetup) (stepSource.mbs)).getTitle());
            stepResult.add(((MicroBlockObjectSetup) (stepSource.mbs)).getMethod());
            stepResult.add(((MicroBlockObjectSetup) (stepSource.mbs)).getChannel());

            stepResult.add(saveObjectMethodsArray("version",
                    ((MicroBlockObjectSetup) (stepSource.mbs)).getMethod(),
                    ((MicroBlockObjectSetup) (stepSource.mbs)).getProcessList()));

            destinationProtocol.add(stepResult);
        }

        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(destinationProtocol);
        oos.close();
        repaint();
    }

    @Override
    public void onLoadDatasets() throws Exception {
        BackgroundTaskHelper.execute(
            () -> {
                OpenObxFormat io = new OpenObxFormat();
                io.importObjects(ImageTabs);
            },
            null, // No success callback needed
            (e) -> {
                // Error callback runs on EDT
                System.err.println("ERROR loading datasets: " + e.getLocalizedMessage());
            }
        );
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
//            int returnVal = jf.showOpenDialog(ImageTabs);
//            File file = jf.getSelectedFile();
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
//                        new ProgressMonitorInputStream(ImageTabs,"Reading" + file.getName() ,fis);
//                        
//                        result = (ArrayList) ois.readObject();
//                        ois.close(); 
//                        } catch (IOException e) {
//                        System.out.println("ERROR: Could not open the object file.");
//                        } 
//                    
//                        File image = new File(file.getParent(), ((String)result.get(0))+".tif");
//                      
//                        if(image.exists()){
//                            
//                            Opener op = new Opener();
//                            ImagePlus imp = op.openImage(file.getParent(), ((String)result.get(0))+".tif");
//                            
//                            executeExploring((file.getName()).replace(".obx", ""), result, imp);
//
//                        }else{
//                            
//                             System.out.println("WARNING: Could not find the image file.");
//                             
//                             JFileChooser jf2 = new JFileChooser(_vtea.LASTDIRECTORY);
//                            
//                             FileNameExtensionFilter filter2 = 
//                             new FileNameExtensionFilter("Tiff file.", ".tif", "tif");
//                             jf2.addChoosableFileFilter(filter2);
//                             jf2.setFileFilter(filter2);
//                             int returnVal2 = jf2.showOpenDialog(ImageTabs);
//                             File file2 = jf2.getSelectedFile();
//                             //System.out.println("PROFILING: Getting image file: " + file2.getName());
//                             Opener op = new Opener();
//                             ImagePlus imp = op.openImage(file2.getParent(), file2.getName());
//                             //imp.setTitle(file.getName());
//                             executeExploring((file.getName()).replace(".obx", ""), result, imp);                          
//                        }
//                        }catch (Exception e) {
//                    System.out.println("ERROR: Not Found.");
// 
//                    }
//            
//            }
//        }
//
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
}
