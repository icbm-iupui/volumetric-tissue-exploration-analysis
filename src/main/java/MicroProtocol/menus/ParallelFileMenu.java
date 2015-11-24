/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package MicroProtocol.menus;

import MicroProtocol.listeners.BatchStateListener;
import MicroProtocol.listeners.RenameTabListener;
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
public class ParallelFileMenu extends JPopupMenu implements ActionListener, ItemListener {
                JRadioButtonMenuItem SingleFile;
                JMenuItem Rename;
                JMenuItem Item2;
                JMenuItem Item3;
                boolean batch;
                private ArrayList<RenameTabListener> listeners = new ArrayList<RenameTabListener>();
                
                public ParallelFileMenu(){
                    //batch = multiple;
                     Rename = new JMenuItem("Rename...");
                     //MultipleFiles.setSelected(multiple);
                     Rename.setActionCommand("Multiple");
                     Rename.addItemListener(this);
                     Rename.addActionListener(this);
                     //Item2 = new JMenuItem("Item2");
                     //Item3 = new JMenuItem("Item3");

                    add(Rename);
                    //addSeparator();
                    //add(Item2);
                    //add(Item3);
                } 

    @Override
    public void itemStateChanged(ItemEvent ie) {
        
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
 
        }
    
    

                }

