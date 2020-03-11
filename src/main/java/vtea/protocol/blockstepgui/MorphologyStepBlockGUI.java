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
package vtea.protocol.blockstepgui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import static vtea._vtea.MORPHOLOGICALMAP;
import vtea.protocol.listeners.DeleteBlockListener;
import vtea.protocol.listeners.MicroBlockSetupListener;
import vtea.protocol.listeners.RebuildPanelListener;
import vtea.protocol.setup.MicroBlockFeatureSetup;
import vtea.protocol.setup.MicroBlockMorphologySetup;
import vtea.protocol.setup.MicroBlockSetup;

/**
 * Block for Morphology Steps in MorphologyFrame.
 * @author drewmcnutt
 */
public class MorphologyStepBlockGUI extends AbstractMicroBlockStepGUI implements MicroBlockSetupListener{
    
    JPanel step = new JPanel();
    Font PositionFont = new Font("Arial", Font.PLAIN, 16);
    Font FeatureFont = new Font("Arial", Font.BOLD, 12);
    Font CommentFont = new Font("Arial", Font.ITALIC, 10);
    JLabel Position = new JLabel();
    JLabel Comment = new JLabel("Block by Block");
    JLabel Feature = new JLabel("First things first");
    boolean ProcessTypeSet = false;
    int position;


    JButton DeleteButton;
    JButton EditButton;
    
    JPopupMenu Popup;
    JCheckBoxMenuItem ValidationSelector;
    
    boolean performValidation;

    MicroBlockSetup mbs;

    private ArrayList<ArrayList> settings;
    ArrayList ad;
    
    public ArrayList<RebuildPanelListener> rebuildpanelisteners = new ArrayList<>();
    public ArrayList<DeleteBlockListener> deleteblocklisteners = new ArrayList<>();
    
    /**
     * Constructor. Empty
     */
    public MorphologyStepBlockGUI() {
    }
    
    /**
     * Constructor. Creates block with specified features.
     * @param FeatureText text for title of block
     * @param CommentText text for subtitle of block
     * @param BlockColor color of the block
     * @param position position of the block in order
     * @param AvailableData all of the current feature names
     * @param nvol number of volumes
     */
    public MorphologyStepBlockGUI(String FeatureText, String CommentText, Color BlockColor, int position, ArrayList Channels) {
            BuildStepBlock(FeatureText, CommentText, Color.GRAY, position, Channels);
    }
    
    private void BuildStepBlock(String ProcessText, String CommentText, Color BlockColor, final int position, ArrayList Channels) {
        this.position = position;
        Feature.setText("");
        
        Comment.setText("New morphological settings...");
        step.setBackground(BlockColor);

        //need max size set here
        Position.setText(position + ".");
        Position.setFont(PositionFont);
        
        //Comment.setText("");

        FeatureFont = new Font("Arial", Font.BOLD, 14);
        CommentFont = new Font("Arial", Font.ITALIC, 10);
        
        Feature.setFont(FeatureFont);
        Comment.setFont(CommentFont);
        
        mbs = new MicroBlockMorphologySetup(position, Channels);

        mbs.setVisible(false);
        mbs.addMicroBlockSetupListener(this);

        DeleteButton = new JButton();
        DeleteButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                deleteStep(type);
            }
        });

        EditButton = new JButton();
        EditButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                mbs.setVisible(true);
            }
        });
        
        Popup = new JPopupMenu();
        ValidationSelector = new JCheckBoxMenuItem("Perform Validation on this method");
        ValidationSelector.addItemListener(new java.awt.event.ItemListener() {
           @Override
           public void itemStateChanged(ItemEvent ae){
                
           }
        });
        ValidationSelector.setEnabled(false);
        Popup.add(ValidationSelector);
        
        performValidation = false;

        DeleteButton.setSize(20, 20);
        DeleteButton.setBackground(vtea._vtea.BUTTONBACKGROUND);
        DeleteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit-delete-6_16.png")));

        EditButton.setSize(20, 20);
        EditButton.setBackground(vtea._vtea.BUTTONBACKGROUND);
        EditButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit-4.png")));
        
        step.setSize(205, 20);
        step.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        step.setLayout(new GridBagLayout());
        GridBagConstraints layoutConstraints = new GridBagConstraints();

        layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
        layoutConstraints.anchor = GridBagConstraints.NORTHWEST;
        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 0;
        layoutConstraints.weightx = 1;
        layoutConstraints.weighty = 1;
        layoutConstraints.ipadx = 10;

        step.add(Position, layoutConstraints);

        layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
        layoutConstraints.anchor = GridBagConstraints.CENTER;
        layoutConstraints.gridx = 1;
        layoutConstraints.gridy = 0;
        layoutConstraints.weightx = 20;
        layoutConstraints.weighty = 20;
        step.add(Feature, layoutConstraints);

        layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
        layoutConstraints.gridx = 1;
        layoutConstraints.gridy = 1;
        layoutConstraints.weightx = 20;
        layoutConstraints.weighty = 20;
        step.add(Comment, layoutConstraints);

        layoutConstraints.fill = GridBagConstraints.BOTH;
        layoutConstraints.anchor = GridBagConstraints.EAST;
        layoutConstraints.gridx = 2;
        layoutConstraints.gridy = 0;
        layoutConstraints.weightx = -1;
        layoutConstraints.weighty = -1;
        layoutConstraints.ipadx = -1;
        layoutConstraints.ipady = -1;
        step.add(DeleteButton, layoutConstraints);
        
        layoutConstraints.fill = GridBagConstraints.BOTH;
        layoutConstraints.anchor = GridBagConstraints.EAST;
        layoutConstraints.gridx = 2;
        layoutConstraints.gridy = 1;
        layoutConstraints.weightx = -1;
        layoutConstraints.weighty = -1;
        layoutConstraints.ipadx = -1;
        layoutConstraints.ipady = -1;
        step.add(EditButton, layoutConstraints);
        
        step.addMouseListener(new java.awt.event.MouseListener() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
            };
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                //thumb.setVisible(false);
            };
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                showPopup(evt);
            };
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                showPopup(evt);
            };
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                
            };
            private void showPopup(java.awt.event.MouseEvent evt){
                if (evt.isPopupTrigger()) {
                    Popup.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            };
        });
    }    
    
    /**
     * Sets position of block.
     * @param n new position for the block
     */
    @Override
    public void setPosition(int n) {
        position = n;
        Position.setText(position + ".");
    }

    /**
     * Retrieves GUI of the block.
     * @return 
     */
    @Override
    public JPanel getPanel() {
        return step;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public ArrayList getVariables() {
        return settings;
    }
    
    /**
     * Retrieves the Setup class for the block
     * @return Setup for block
     */
    public MicroBlockSetup getSetup(){
        return mbs;
    }
    
    /**
     * Updates the protocol to be the parameters selected in the Setup class.
     */
    public void updateSetup(){
        ((MicroBlockFeatureSetup)mbs).updateProtocol();
    }
    
    /**
     * Rebuilds the block text when the parameters are changed in the Setup.
     * @param al the selected parameters
     */
    @Override
    public void onChangeSetup(ArrayList al){
        if(al.size() > 1){     
           ArrayList morphology = (ArrayList)al.get(0);
            Feature.setText(morphology.get(0).toString()); 
            Comment.setText(Feature.getText() + ", " + " on all channels ");        
    } else {        
        ArrayList morphology = (ArrayList)al.get(0);
        Feature.setText(morphology.get(0).toString()); 
        Comment.setText(Feature.getText() + ", " + " on channel " + morphology.get(1));  
    }   
        settings = al;
    }
    
    @Override
    protected void deleteStep(int type) {
        notifyDeleteBlockListeners(type, this.position);
        mbs.setVisible(false);
    }
    
    @Override
    public void addRebuildPanelListener(RebuildPanelListener listener) {
        rebuildpanelisteners.add(listener);
    }

    @Override
    protected void notifyRebuildPanelListeners(int type) {
        for (RebuildPanelListener listener : rebuildpanelisteners) {
            listener.rebuildPanel(type);
        }
    }

    @Override
    public void addDeleteBlockListener(DeleteBlockListener listener) {
        deleteblocklisteners.add(listener);
    }

    @Override
    protected void notifyDeleteBlockListeners(int type, int position) {
        for (DeleteBlockListener listener : deleteblocklisteners) {
            listener.deleteBlock(type, position);
        }
    }
    
    private void addToolTip(){
//        String tt = "<html>";
//        ArrayList data = (ArrayList)this.settings.get(1);
//        String curline = "";
//        
//        for(int i = 0; i < data.size(); i++){
//            if((boolean)data.get(i)){
//                String f = ad.get(i).toString();
//                int flen = f.length();
//                if(curline.length() + flen > 67){
//                    curline = curline.concat("<br>");
//                    tt = tt.concat(curline);
//                    curline = "";
//                }
//                curline = curline.concat(f);
//                if(curline.length() > 0){
//                    curline = curline.concat(", ");
//                }
//            }
//        }
//        tt = tt.concat(curline);
//        
//        tt = tt.substring(0, tt.lastIndexOf(",")); //removes trailing ','
//        tt = tt.concat("</html>");
//        
//        step.setToolTipText(tt);
    }

}
