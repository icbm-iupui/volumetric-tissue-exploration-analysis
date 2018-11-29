/* 
 * Copyright (C) 2016-2018 Indiana University
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


import vtea.protocol.blockstepgui.ProcessStepBlockGUI;
import vtea.protocol.listeners.BatchStateListener;
import vtea.protocol.listeners.CopyBlocksListener;
import vtea.protocol.listeners.RenameTabListener;
import vtea.protocol.listeners.RepaintTabListener;
import vtea.protocol.listeners.RequestImageListener;
import vtea.protocol.listeners.TransferProtocolStepsListener;
import vtea.protocol.menus.AvailableWorkflowsMenu;
import vtea.protocol.menus.JMenuBatch;
import vtea.protocol.menus.JMenuProtocol;
import vtea.protocol.menus.LengthFilter;
import vtea.ImageSelectionListener;
import vtea.OpenImageWindow;
import ij.ImagePlus;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JWindow;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static java.awt.event.InputEvent.BUTTON1_DOWN_MASK;
import static java.awt.event.InputEvent.BUTTON2_DOWN_MASK;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import static java.awt.event.MouseEvent.BUTTON3;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.text.AbstractDocument;
import org.jdesktop.jxlayer.JXLayer;
import static vtea._vtea.PROCESSINGMAP;
import vteaexploration.GateManager;
import vtea.exploration.plotgatetools.gates.Gate;
import static vtea.exploration.plotgatetools.gates.GateLayer.clipboardGate;
import static vtea.exploration.plotgatetools.gates.GateLayer.gateInClipboard;
import vtea.imageprocessing.AbstractImageProcessing;
import vtea.protocol.setup.MicroBlockProcessSetup;

/**
 *
 * @author winfrees
 */
public class ProtocolManagerMulti extends javax.swing.JFrame implements ImageSelectionListener, RequestImageListener, RepaintTabListener, RenameTabListener, CopyBlocksListener, BatchStateListener, ActionListener {

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

    private ArrayList<TransferProtocolStepsListener> listeners = new ArrayList<TransferProtocolStepsListener>();

    public JMenuProtocol ProcessingMenu;
    public JMenuProtocol ObjectMenu;
    public JMenuProtocol WorkflowMenu;
    public JMenuBatch BatchMenu;
    
    private JPopupMenu menu = new JPopupMenu();

    public JWindow thumb = new JWindow();

    public GridLayout PreProcessingLayout = new GridLayout(5, 1, 0, 0);
    public GridLayout ObjectLayout = new GridLayout(5, 1, 0, 0);
    public GridLayout ExploreLayout = new GridLayout(5, 1, 0, 0);

    public Color ButtonBackground = new java.awt.Color(102, 102, 102);
    public String[] Channels;

    private MicroExperiment me = new MicroExperiment();

    private ArrayList<JPanel> Tabs = new ArrayList<JPanel>();

    //static ResultsTable rt;
    /**
     * Creates new form protocolManager
     */
    public ProtocolManagerMulti() {

        openerWindow.addImageSelectionListener(this);
        GuiSetup();
        initComponents();
        addGateManager();
        addNewTabTab();
        addMenuItems();
        addSingleImagePanel();
        
 
        this.ImageTabs.setTabPlacement(JTabbedPane.TOP);
        this.ImageTabs.setSelectedIndex(ImageTabs.getTabCount() - 1);
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
        setBounds(new java.awt.Rectangle(30, 100, 890, 400));
        setMaximumSize(new java.awt.Dimension(760, 480));
        setMinimumSize(new java.awt.Dimension(760, 480));
        setName("ProcessingFrame"); // NOI18N
        setPreferredSize(new java.awt.Dimension(760, 480));
        setSize(new java.awt.Dimension(782, 482));
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
                String[] options = {"Delete","Cancel"}; 
            
                int result = JOptionPane.showOptionDialog(null,frame,
                "Are you sure you want to delete tab, " + ImageTabs.getTitleAt(ImageTabs.getSelectedIndex()) + "?",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                null);
            
                if(result == JOptionPane.OK_OPTION){
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
        
        if(menu.getComponentCount() < 3){
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
    
        } else if (evt.getClickCount() == 1 && (evt.getButton() == BUTTON3)){
            
            if(this.ImageTabs.getTabCount() <= 2){menu.getComponent(3).setEnabled(false);}else{
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
        
        
        
        if (evt.getKeyCode() ==  KeyEvent.VK_DELETE) {
            if (this.ImageTabs.getSelectedIndex() > 1 || this.ImageTabs.getTabCount() > 2) {
                
                JPanel panel = new JPanel();
                JLabel text = new JLabel("Are you sure you want to delete tab, " + ImageTabs.getTitleAt(ImageTabs.getSelectedIndex()) + "?");
                //panel.setBackground(VTC.VTEAService.BUTTONBACKGROUND);  
                panel.add(text);
                String[] options = {"Delete","Cancel"}; 
                
                
                int result = JOptionPane.showOptionDialog(null,panel,
                "Delete " + ImageTabs.getTitleAt(ImageTabs.getSelectedIndex()),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                null);
            
                if(result == JOptionPane.OK_OPTION){
                    Tabs.remove(this.ImageTabs.getSelectedIndex()-1);
                    ImageTabs.remove(this.ImageTabs.getSelectedIndex());
                    
                    for(int i = 1; i < Tabs.size(); i++){
                        SingleImageProcessing sip = (SingleImageProcessing)Tabs.get(i);    
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
        
        ProcessingMenu.addFileOperationListener(NewPanel);
        
        Tabs.add(NewPanel);

        addTransferProtocolStepsListener(NewPanel);
        
        ImageTabs.addTab("Image_" + this.Tabs.indexOf(NewPanel), NewPanel);
        NewPanel.setName("Image_" + this.Tabs.indexOf(NewPanel));
        

        if(ImageTabs.getTabCount() > 2) {refreshMenuItems();}
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
        System.out.println(e.getActionCommand());
        if(e.getActionCommand().equals("Rename...")){
            JFrame frame = new JFrame();
            frame.setBackground(vtea._vtea.BUTTONBACKGROUND);
            String name = JOptionPane.showInputDialog(null,"Rename to:",
            ImageTabs.getTitleAt(this.ImageTabs.getSelectedIndex()));
            
            if(!name.equalsIgnoreCase(ImageTabs.getTitleAt(this.ImageTabs.getSelectedIndex()))){                
                ImageTabs.setTitleAt(ImageTabs.getSelectedIndex(), name); 
                SingleImageProcessing sip = (SingleImageProcessing)ImageTabs.getComponentAt(this.ImageTabs.getSelectedIndex());
                sip.setName(name);
                addMenuItems();
                
                } else {
                    ImageTabs.setSelectedIndex(this.ImageTabs.getSelectedIndex());
                }
            
            
            
            
        }else if(e.getActionCommand().equals("Delete")){
                JPanel panel = new JPanel();
                JLabel text = new JLabel("Are you sure you want to delete tab, " + ImageTabs.getTitleAt(ImageTabs.getSelectedIndex()) + "?");
                //panel.setBackground(VTC.VTEAService.BUTTONBACKGROUND);  
                panel.add(text);
                String[] options = {"Delete","Cancel"}; 
                
                
                int result = JOptionPane.showOptionDialog(null,panel,
                "Delete " + ImageTabs.getTitleAt(ImageTabs.getSelectedIndex()),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                null);
            
                if(result == JOptionPane.OK_OPTION){
                    Tabs.remove(this.ImageTabs.getSelectedIndex()-1);
                    ImageTabs.remove(this.ImageTabs.getSelectedIndex());
                    
                    for(int i = 1; i < Tabs.size(); i++){
                        SingleImageProcessing sip = (SingleImageProcessing)Tabs.get(i);    
                        sip.setTabValue(i);
                    }
                    
                    this.ImageTabs.setSelectedIndex(1);
                    refreshMenuItems();
                } else {
                    this.ImageTabs.setSelectedIndex(this.ImageTabs.getSelectedIndex());
                }
    
        }else if(e.getActionCommand().equals("Duplicate")){
          
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
        NewPanel.setObjectProcotols(sip.getObjectSteps());
        NewPanel.setProtocolSynopsis(sip.getProProcessingProtocol(), sip.getObjectSteps());

    }

    public void addMenuItems() {
        this.WorkflowMenu = new JMenuProtocol("Workflow", this.getTabNames(), SingleImageProcessing.WORKFLOW);
        this.ProcessingMenu = new JMenuProtocol("Processing", this.getTabNames(), SingleImageProcessing.PROCESSBLOCKS);
        this.ObjectMenu = new JMenuProtocol("Objects", this.getTabNames(), SingleImageProcessing.OBJECTBLOCKS);

        ProcessingMenu.addStepCopierListener(this);  
        ObjectMenu.addStepCopierListener(this);
        
        ListIterator<JPanel> itr = Tabs.listIterator();
        
        while(itr.hasNext()){            
            SingleImageProcessing sip = (SingleImageProcessing)itr.next();    
            ProcessingMenu.addFileOperationListener(sip);
            ObjectMenu.addFileOperationListener(sip);           
        }
 
        this.MenuBar.removeAll();
        this.MenuBar.add(WorkflowMenu);
        this.MenuBar.add(ProcessingMenu);
        this.MenuBar.add(ObjectMenu);
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
    }

    public void openImage(int tab) {
        //this.openerWindow.setVisible(true);
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
        
        while(itr.hasNext()){
            SingleImageProcessing s = (SingleImageProcessing)itr.next();
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

        //System.out.println("PROFILING: copy blocks to " +  ((JTextField)ImageTabs.getTabComponentAt(ImageTabs.getSelectedIndex())).getText()  +" from " + source + " of type " + StepsList);
        SingleImageProcessing SourceSIP;
        SingleImageProcessing DestinationSIP;

        

        DestinationSIP = (SingleImageProcessing) (ImageTabs.getComponentAt(ImageTabs.getSelectedIndex()));

        for (int i = 1; i <= ImageTabs.getTabCount() - 1; i++) {

            if (ImageTabs.getTitleAt(i).equals(source)) {
                
                SourceSIP = (SingleImageProcessing) (ImageTabs.getComponentAt(i));
                
                switch (StepsList) {
                    case SingleImageProcessing.PROCESSBLOCKS:
                        DestinationSIP.setProcessSteps((ArrayList) SourceSIP.getProcessSteps());
                        ListIterator<ProcessStepBlockGUI> itr = DestinationSIP.ProcessingStepsList.listIterator();
                        while (itr.hasNext()) {
                            ProcessStepBlockGUI psbgSource = (ProcessStepBlockGUI) DestinationSIP.getProcessingProtocolList().get(itr.nextIndex());
                            ProcessStepBlockGUI psbg = (ProcessStepBlockGUI) itr.next();
                            
                            MicroBlockProcessSetup mbpsSource = (MicroBlockProcessSetup)psbgSource.getSetup();
                            MicroBlockProcessSetup mbpsDestination = (MicroBlockProcessSetup)psbgSource.getSetup();

                            
                            psbg.setImages(DestinationSIP.getOriginalImage(), DestinationSIP.getThumbnailImage());
                            psbg.deleteblocklisteners.clear();
                            psbg.rebuildpanelisteners.clear();
                            psbg.addRebuildPanelListener(DestinationSIP);
                            psbg.addDeleteBlockListener(DestinationSIP); 
                            
                            
                            //psbg.getSetup().
                        }   
                        
//                        ListIterator<ProcessStepBlockGUI> itr2 = DestinationSIP.ProcessingStepsList.listIterator();
//                        while (itr2.hasNext()) {
//                            psbg = (ProcessStepBlockGUI) itr2.next();  
//                            psbg.updateSetup();
//                        }  
                        
                        DestinationSIP.UpdatePositionProcessing(1);
                        DestinationSIP.RebuildPanelProcessing();
                        SourceSIP.RebuildPanelProcessing();
                        break;
                    case SingleImageProcessing.OBJECTBLOCKS:
                        DestinationSIP.setObjectSteps((ArrayList) SourceSIP.getObjectSteps().clone());
                        DestinationSIP.UpdatePositionObject(1);
                        DestinationSIP.RebuildPanelObject();
                        SourceSIP.RebuildPanelObject();

                        break;

                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void batchStateAdd(String selected, ArrayList tabs) {
        this.addBatchPanel(selected, tabs);
    }


}
