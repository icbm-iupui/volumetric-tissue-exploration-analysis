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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import vtea.protocol.listeners.BatchStateListener;
import vtea.protocol.listeners.CopyBlocksListener;

/**
 *
 * @author vinfrais
 */
public class JMenuExploration extends JMenu implements ActionListener, ItemListener {

    JMenuItem LoadSteps;
    JMenuItem SaveSteps;
    JMenu CopySteps;
    //JMenuItem Item3;
    boolean batch;
    int ProtocolType;

    String ItemString1 = "Load...";
    String ItemString2 = "Save...";
    String ItemString3 = "Copy from...";

    private ArrayList<BatchStateListener> listeners = new ArrayList<BatchStateListener>();
    private ArrayList<CopyBlocksListener> CopyListeners = new ArrayList<CopyBlocksListener>();

    public JMenuExploration(final String text, int type) {

        super(text);

        this.ProtocolType = type;

        LoadSteps = new JMenuItem("Load...");
        LoadSteps.setActionCommand("Load");
        LoadSteps.addActionListener(this);

        SaveSteps = new JMenuItem("Save...");
        SaveSteps.setActionCommand("Load");
        SaveSteps.addActionListener(this);

        CopySteps = new JMenu("Copy from...");
        CopySteps.setActionCommand("Copy");
        CopySteps.addActionListener(this);

        //ListIterator<String> itr = tabs.listIterator();
//                     JMenuItem OpenTab = new JMenuItem(); 
//                     
//                     while(itr.hasNext())
//                     {  
//                     OpenTab = new JMenuItem(itr.next());
//                     OpenTab.setActionCommand(this.getName());
//                     OpenTab.addActionListener(new ActionListener(){
//                         @Override
//                         public void actionPerformed(ActionEvent ae) {   
//                            notifyStepCopierListener(((JMenuItem)ae.getSource()).getText(), ProtocolType);
//                         }
//                     });
//                     CopySteps.add(OpenTab);
//                     }
//                    add(LoadSteps);
//                    add(SaveSteps);
//                    add(CopySteps);
    }

    //private void copySteps           
    @Override
    public void itemStateChanged(ItemEvent ie) {

    }

    @Override
    public void actionPerformed(ActionEvent ae) {

    }

//    public void addBatchListener(BatchStateListener listener) {
//        listeners.add(listener);
//    }
//
//    public void notifyBatchListeners(boolean batch) {
//        for (BatchStateListener listener : listeners) {
//            listener.batchStateAdd(batch);
//        }
//    }
    public void addStepCopierListener(CopyBlocksListener listener) {
        CopyListeners.add(listener);

    }

    public void notifyStepCopierListener(String source, int type) {
        for (CopyBlocksListener listener : CopyListeners) {
            //IJ.log(type + ", Event: " + source);
            listener.onCopy(source, type);
        }
    }
}
