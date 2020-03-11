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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import vtea.protocol.listeners.RenameTabListener;

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

