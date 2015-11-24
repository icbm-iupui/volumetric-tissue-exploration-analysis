/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package MicroProtocol.menus;

import MicroProtocol.listeners.BatchStateListener;
import MicroProtocol.listeners.CopyBlocksListener;
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
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 *
 * @author vinfrais
 */
public class JMenuBatch extends JMenu implements ActionListener, ItemListener {
                JMenuItem LoadSteps;
                JMenuItem SaveSteps;
                JMenuItem NewSteps;
                JMenu CopySteps;
                boolean batch;              
                int ProtocolType;
                
                String ItemString1 = "Load...";
                String ItemString2 = "Save...";
                String ItemString3 = "Copy from...";
                
                private ArrayList<BatchStateListener> listeners = new ArrayList<BatchStateListener>();
                private ArrayList<CopyBlocksListener> CopyListeners = new ArrayList<CopyBlocksListener>();
                
                public JMenuBatch(final String text, final ArrayList<String> tabs, int type, boolean batch){
                    
                    super(text);
                    
                    this.batch = batch;
                    
                    this.ProtocolType = type;
                    
                     NewSteps = new JMenuItem("Add files...");
                     NewSteps.setActionCommand("Files");
                     NewSteps.addActionListener(this);
                     //NewSteps.setEnabled(false);
                    
                     LoadSteps = new JMenuItem("Load...");
                     LoadSteps.setActionCommand("Load");
                     LoadSteps.addActionListener(this);
                     
                     SaveSteps = new JMenuItem("Save...");
                     SaveSteps.setActionCommand("Save");
                     SaveSteps.addActionListener(this);
                     
                     CopySteps = new JMenu("Add batch...");
                     CopySteps.setActionCommand("Batch");
                     CopySteps.addActionListener(this);
                     
                     ListIterator<String> itr = tabs.listIterator();
                     
                     JMenuItem OpenTab = new JMenuItem(); 
                     
                     while(itr.hasNext())
                     {  
                     OpenTab = new JMenuItem(itr.next());
                     
                     OpenTab.setActionCommand(this.getName());
                     
                     OpenTab.addActionListener(new ActionListener(){
                         @Override
                         public void actionPerformed(ActionEvent ae) {   
                            //notifyStepCopierListener(((JMenuItem)ae.getSource()).getText(), ProtocolType);
                            notifyBatchListeners(ae.getActionCommand(), tabs);
                            //System.out.println(ae.getActionCommand());
                         }
                     });
                     
                     OpenTab.addItemListener(new ItemListener(){

                         @Override
                         public void itemStateChanged(ItemEvent ie) {
                             //notifyBatchListeners(((JMenuItem)ie.getItem())., tabs);
                         }
                     });
                     CopySteps.add(OpenTab);
                     
                     }
                    add(NewSteps);
                    add(LoadSteps);
                    add(SaveSteps);
                    add(CopySteps);                  
                } 
                
    @Override
    public void itemStateChanged(ItemEvent ie) {
        
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        
    }
    
        
    
    public void addBatchListener(BatchStateListener listener) {
        listeners.add(listener);
    }

    public void notifyBatchListeners(String selected, ArrayList tab) {
        
        
        
        for (BatchStateListener listener : listeners) {
            listener.batchStateAdd(selected, tab);
            this.NewSteps.setEnabled(true);
            this.CopySteps.setEnabled(false);
        }
    }
    
    public void addStepCopierListener(CopyBlocksListener listener){
         CopyListeners.add(listener);   
    }
    
    public void notifyStepCopierListener(String source, int type){
        for (CopyBlocksListener listener : CopyListeners){
     //IJ.log(type + ", Event: " + source);
    listener.onCopy(source, type);
}
    }
}