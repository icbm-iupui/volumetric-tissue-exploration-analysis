package MicroDeprecated;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.measure.ResultsTable;
import ij.plugin.frame.Recorder;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Arrays;
import javax.swing.DefaultListModel;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

@Deprecated

public class microProcessManager extends JInternalFrame implements ActionListener, ItemListener, MouseListener, MouseWheelListener, ListSelectionListener {

    public static final String LOC_KEY = "manager.loc";
    private static final int BUTTONS = 11;
    private static int rows = 15;
    private Panel panel;
    static final int xOffset = 0, yOffset = 180;

    private Panel panelFile;
    private Panel panelResult;
    private static Frame instance;
    private static int colorIndex = 4;
    //private JLabel label1, label2, label3;

    private PopupMenu pm;

    private JList listResult;

    private DefaultListModel listModelResult;
    //private Button moreButton, colorButton;
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
    private int[] imageIDList;
    private String[] windowTitles;

    public microProcessManager() {

        super("Process Manager",
                true, //resizable
                true, //closable
                true, //maximizable
                true);//iconifiable

        //...Create the GUI and put it in the window...
        //...Then set the window size or call pack...
        setSize(300, 300);

        //Set the window's location.
        setLocation(xOffset, yOffset);
        listResult = new JList();
        showWindow();
    }

    void showWindow() {
        ImageJ ij = IJ.getInstance();
        addKeyListener(ij);
        addMouseListener(this);
        addMouseWheelListener(this);
        //WindowManager.addWindow(this);
        //setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
        setLayout(new BorderLayout());

        listModelResult = new DefaultListModel();
        listModelResult.addElement("EXP:1 MASK:2 on Test Image.tif  ");
        listResult = new JList(listModelResult);
        listResult.setPrototypeCellValue("ImageName_DDMMYY_ext_result       ");
        listResult.addListSelectionListener(this);
        listResult.addKeyListener(ij);
        listResult.addMouseListener(this);
        listResult.addMouseWheelListener(this);
        if (IJ.isLinux()) {
            listResult.setBackground(Color.white);
        }
        //JScrollPane scrollPane = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        //JScrollPane scrollPaneFiles = new JScrollPane(listFile, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        JScrollPane scrollPaneResults = new JScrollPane(listResult, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        panel = new Panel();
        int nButtons = BUTTONS;

        panel.setLayout(new GridLayout(nButtons, 1, 5, 1));
        panel.setBackground(ImageJ.backgroundColor);

        addCanvas();

        add("North", scrollPaneResults);
        add("South", panel);

        //addPopupMenu();
        pack();
        Dimension size = getSize();
        //if (size.width>270)
        //setSize(size.width-40, size.height);
        //list.remove(0);
//        Point loc = Prefs.getLocation(LOC_KEY);
//        if (loc!=null)
//            setLocation(loc);
//        else
//            GUI.center(this);
        show();
    }

    void updateWindow() {

    }

    //microGateManager specific
    void addCanvas() {

        Canvas c = new Canvas();
        c.addKeyListener(IJ.getInstance());
        c.addMouseListener(this);
        panel.add(c);
    }

    void addButton(String label) {
        Button b = new Button(label);
        b.addActionListener(this);
        b.addKeyListener(IJ.getInstance());
        b.addMouseListener(this);
        // if (label.equals(moreButtonLabel)) moreButton = b;
        panel.add(b);
    }

    void addPopupMenu() {
        pm = new PopupMenu();
        //addPopupItem("Select All");
        addPopupItem("Details...");
        addPopupItem("Delete");
        add(pm);
    }

    void addPopupItem(String s) {
        MenuItem mi = new MenuItem(s);
        mi.addActionListener(this);
        pm.add(mi);
    }

    public void getDetails() {

    }

    public String[] getOpenImages() {

        int[] windowList = WindowManager.getIDList();

        String[] titles;

        if (windowList == null || windowList.length < 1) {
            //error();
            IJ.showMessage("Caution", "No open images");
            titles = new String[1];
            titles[0] = "";
            return titles;
        }
        titles = new String[windowList.length];

        for (int i = 0; i < windowList.length; i++) {
            ImagePlus imp_temp = WindowManager.getImage(windowList[i]);
            titles[i] = imp_temp != null ? imp_temp.getTitle() : "";
        }
        return titles;
    }

    public void actionPerformed(ActionEvent e) {
        String label = e.getActionCommand();
        if (label == null) {
            return;
        }
        String command = label;

    }

    public void itemStateChanged(ItemEvent e) {
        Object source = e.getSource();
    }

    void add(boolean shiftKeyDown, boolean altKeyDown) {

    }

    boolean error(String msg) {
//        new MessageDialog(this, "ROI Manager", msg);
//        Macro.abort();
        return false;
    }

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
        listResult.setSelectedIndices(indexes);
    }

    /**
     * Returns an array of all of the selected indexes.
     */
    public int[] getSelectedIndexes() {
        if (selectedIndexes != null) {
            int[] indexes = selectedIndexes;
            selectedIndexes = null;
            return indexes;
        } else {
            return listResult.getSelectedIndices();
        }
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

    /**
     * Returns the ROI count.
     */
    public int getCount() {
        return listModelResult.getSize();
    }

    private boolean record() {
        return Recorder.record && !IJ.isMacro();
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

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
        int[] selected = listResult.getSelectedIndices();
        if (selected.length == 0) {
            return;
        }
        if (WindowManager.getCurrentImage() != null) {
            if (selected.length == 1) // restore(getImage(), selected[0], true);
            {
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
    }

//public void windowActivated(WindowEvent e) {
//        super.windowActivated(e);
//        //IJ.showMessage("Debug", "Activated!");
//        ImagePlus imp = WindowManager.getCurrentImage();
//        if (imp!=null) {
//            if (imageID!=0 && imp.getID()!=imageID) {
//                //showAll(SHOW_NONE);
//                //showAllCheckbox.setState(false);
//            }
//        }
//    }
    public void mousePressed(MouseEvent e) {
        int x = e.getX(), y = e.getY();
        if (e.isPopupTrigger() || e.isMetaDown()) {
        }
        //pm.show(e.getComponent(),x,y);
    }

    public void mouseWheelMoved(MouseWheelEvent event) {
        synchronized (this) {
            int index = listResult.getSelectedIndex();
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
            //select(index);
            if (IJ.isWindows()) {
                listResult.requestFocusInWindow();
            }
        }
    }

}

//class AnalysisListener implements ActionListener{
//	public void actionPerformed(ActionEvent e) {
//		int index = listFile.getSelectedIndex();
//		
//	}
//}
