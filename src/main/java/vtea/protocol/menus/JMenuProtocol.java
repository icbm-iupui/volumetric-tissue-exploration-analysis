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
package vtea.protocol.menus;

import vtea.protocol.listeners.BatchStateListener;
import vtea.protocol.listeners.CopyBlocksListener;
import vtea.protocol.listeners.FileOperationListener;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.ListIterator;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 *
 * @author vinfrais
 */
public class JMenuProtocol extends JMenu implements ActionListener, ItemListener {

    JMenuItem LoadProcessSteps;
    JMenuItem SaveProcessSteps;

    JMenuItem LoadObjectSteps;
    JMenuItem SaveObjectSteps;
    
    JMenuItem SaveAllSteps;
    
    JMenuItem LoadExplorer;
    
    JMenu CopySteps;
    //JMenuItem Item3;
    boolean batch;
    int ProtocolType;

    private ArrayList<BatchStateListener> listeners = new ArrayList<BatchStateListener>();
    private ArrayList<CopyBlocksListener> CopyListeners = new ArrayList<CopyBlocksListener>();
    private ArrayList<FileOperationListener> fileOperationListeners = new ArrayList<FileOperationListener>();

    public JMenuProtocol(final String text, ArrayList<String> tabs, int type) {

        super(text);

        this.ProtocolType = type;

        LoadProcessSteps = new JMenuItem("Load Processing...");
        LoadProcessSteps.setActionCommand("LoadProcess");
        LoadProcessSteps.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                notifyFileOperationListener(ae);
            }
        });

        SaveProcessSteps = new JMenuItem("Save Processing...");
        SaveProcessSteps.setActionCommand("SaveProcess");
        SaveProcessSteps.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                notifyFileOperationListener(ae);
            }
        });

        LoadObjectSteps = new JMenuItem("Load Segmentation...");
        LoadObjectSteps.setActionCommand("LoadObject");
        LoadObjectSteps.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                notifyFileOperationListener(ae);
            }
        });

        SaveObjectSteps = new JMenuItem("Save Segmentation...");
        SaveObjectSteps.setActionCommand("SaveObject");
        SaveObjectSteps.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                notifyFileOperationListener(ae);
            }
        });

        CopySteps = new JMenu("Copy from...");
        CopySteps.setActionCommand("Copy");
        CopySteps.addActionListener(this);

        ListIterator<String> itr = tabs.listIterator();

        JMenuItem OpenTab = new JMenuItem();
 
        while (itr.hasNext()) {
            OpenTab = new JMenuItem(itr.next());
            OpenTab.setActionCommand(this.getName());
            OpenTab.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    notifyStepCopierListener(((JMenuItem) ae.getSource()).getText(), ProtocolType);
                }
            });
            CopySteps.add(OpenTab);
        }
        
        SaveAllSteps = new JMenuItem("Save All...");
        SaveAllSteps.setActionCommand("SaveAll");
        SaveAllSteps.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                notifyFileOperationListener(ae);
            }
        });
        
        LoadExplorer = new JMenuItem("Load Dataset...");
        LoadExplorer.setActionCommand("LoadDataset");
        LoadExplorer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                notifyFileOperationListener(ae);
            }
        });
        
        
        add(LoadProcessSteps);
        add(SaveProcessSteps);
        add(CopySteps);
        addSeparator();
        add(LoadObjectSteps);
        add(SaveObjectSteps);
        addSeparator();
        add(SaveAllSteps);
        addSeparator();
        add(LoadExplorer);

    }

    //private void copySteps           
    @Override
    public void itemStateChanged(ItemEvent ie) {

    }

    @Override
    public void actionPerformed(ActionEvent ae) {

    }

    public void addStepCopierListener(CopyBlocksListener listener) {
        CopyListeners.add(listener);

    }

    public void notifyStepCopierListener(String source, int type) {
        for (CopyBlocksListener listener : CopyListeners) {
            listener.onCopy(source, type);
        }
    }

    public void addFileOperationListener(FileOperationListener listener) {
        fileOperationListeners.add(listener);
    }

    private void notifyFileOperationListener(ActionEvent ae) {
        JMenuItem temp = (JMenuItem) (ae.getSource());
        for (FileOperationListener listener : fileOperationListeners) {

            if (temp.getText().equals("Load Processing...")) {
                try {
                    //System.out.println("PROFILING: loading.");
                    int returnVal = listener.onProccessingFileOpen();

                    if (returnVal == 1) {

                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this.getParent(),
                            "File could not be opened...\n" +
                                e.getMessage(),
                            vtea._vtea.VERSION,
                            JOptionPane.WARNING_MESSAGE);
                    System.out.println("ERROR: "+ e.getLocalizedMessage());
                }

            } else if (temp.getText().equals("Save Processing...")) {
                try {
                    listener.onProcessingFileSave();
                } catch (Exception e) {
//                    JOptionPane.showMessageDialog(this.getParent(),
//                            "File could not be saved...\n" +
//                                e.getMessage(),
//                            vtea._vtea.VERSION,
//                            JOptionPane.WARNING_MESSAGE);
//                    System.out.println("ERROR: "+ e.getLocalizedMessage());
                }
            } else if (temp.getText().equals("Save Segmentation...")) {
                try {
                    listener.onSegmentationFileSave();
                } catch (Exception e) {
//                    JOptionPane.showMessageDialog(this.getParent(),
//                            "File could not be saved...\n" +
//                                e.getMessage(),
//                            vtea._vtea.VERSION,
//                            JOptionPane.WARNING_MESSAGE);
//                    System.out.println("ERROR: "+ e.getLocalizedMessage());
                }
            } else if (temp.getText().equals("Save All...")) {
                try {
                    listener.onProcessingFileSave();
                    listener.onSegmentationFileSave();
                } catch (Exception e) {
//                    JOptionPane.showMessageDialog(this.getParent(),
//                            "Could not save settings...\n" +
//                                e.getMessage(),
//                            vtea._vtea.VERSION,
//                            JOptionPane.WARNING_MESSAGE);
//                    System.out.println("ERROR: "+ e.getLocalizedMessage());
                }
            } else if (temp.getText().equals("Load Dataset...")) {
                try {
                    listener.onLoadDatasets();                  
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this.getParent(),
                            "Could not load data set...\n" +
                                e.getMessage(),
                            vtea._vtea.VERSION,
                            JOptionPane.WARNING_MESSAGE);
                    System.out.println("ERROR: "+ e.getLocalizedMessage());
                }    
             
            } else if (temp.getText().equals("Load Segmentation...")) {
                try {
                    listener.onSegmentationFileOpen();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this.getParent(),
                            "File could not be opened...\n" +
                                e.getMessage(),
                            vtea._vtea.VERSION,
                            JOptionPane.WARNING_MESSAGE);
                    System.out.println("ERROR: "+ e.getLocalizedMessage());
                }
            } else if (temp.getText().equals("Export")) {
                try {
                    listener.onFileExport();
                } catch (Exception e) {
//                    JOptionPane.showMessageDialog(this.getParent(),
//                            "File could not be exported...",
//                            vtea._vtea.VERSION,
//                            JOptionPane.WARNING_MESSAGE);
//                    //System.out.println("PROFILING: "+ e.getMessage());
                }
            }
        }

    }

}
