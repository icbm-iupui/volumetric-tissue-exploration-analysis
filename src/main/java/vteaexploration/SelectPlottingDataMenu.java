/*
 * Copyright (C) 2021 SciJava
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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import vtea.exploration.plotgatetools.listeners.PopupMenuAxisListener;

/**
 *
 * @author sethwinfree
 */
    public class SelectPlottingDataMenu extends JPopupMenu implements ActionListener {

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

