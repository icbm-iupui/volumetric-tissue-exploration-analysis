/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package MicroProtocol.menus;

import MicroProtocol.listeners.BatchStateListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

/**
 *
 * @author vinfrais
 */
public class AvailableWorkflowsMenu extends JPopupMenu implements ActionListener, ItemListener {
                JRadioButtonMenuItem SingleFile;
                JRadioButtonMenuItem MultipleFiles;
                JMenuItem Item2;
                JMenuItem Item3;
                boolean batch;
                private ArrayList<BatchStateListener> listeners = new ArrayList<BatchStateListener>();
                
                public AvailableWorkflowsMenu(){
                    //batch = multiple;
                     MultipleFiles = new JRadioButtonMenuItem("Multiple Files");
                     //MultipleFiles.setSelected(multiple);
                     MultipleFiles.setActionCommand("Multiple");
                     MultipleFiles.addItemListener(this);
                     MultipleFiles.addActionListener(this);
                     Item2 = new JMenuItem("Item2");
                     Item3 = new JMenuItem("Item3");

                    add(MultipleFiles);
                    addSeparator();
                    add(Item2);
                    add(Item3);
                } 

    @Override
    public void itemStateChanged(ItemEvent ie) {
        
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
 
        }
    
    

                }

