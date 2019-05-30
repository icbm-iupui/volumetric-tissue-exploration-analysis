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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import static vtea._vtea.FEATUREMAP;
import static vtea._vtea.OBJECTMEASUREMENTMAP;
import vtea.protocol.listeners.DeleteBlockListener;
import vtea.protocol.listeners.MicroBlockSetupListener;
import vtea.protocol.listeners.RebuildPanelListener;
import vtea.protocol.setup.MicroBlockFeatureSetup;
import vtea.protocol.setup.MicroBlockMeasurementSetup;
import vtea.protocol.setup.MicroBlockSetup;

/**
 * Block for Analysis Steps in FeatureFrame.
 * @author drewmcnutt
 */
public class MeasurementStepBlockGUI extends AbstractMicroBlockStepGUI implements MicroBlockSetupListener{
    
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

    private ArrayList settings;
    ArrayList ad;
    
    public ArrayList<RebuildPanelListener> rebuildpanelisteners = new ArrayList<>();
    public ArrayList<DeleteBlockListener> deleteblocklisteners = new ArrayList<>();
    
    /**
     * Constructor. Empty
     */
    public MeasurementStepBlockGUI() {
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
    public MeasurementStepBlockGUI(String FeatureText, String CommentText, Color BlockColor, int position, ArrayList AvailableData, int nvol) {
            BuildStepBlock(FeatureText, CommentText, Color.GRAY, position, AvailableData, nvol);
    }
    
    private void BuildStepBlock(String ProcessText, String CommentText, Color BlockColor, final int position, ArrayList AvailableData, final int nvol) {
        this.position = position;
        Feature.setText("");

        //Comment.setText(CommentText);
        step.setBackground(BlockColor);

        //need max size set here
        Position.setText(position + ".");
        Position.setFont(PositionFont);
        
        Comment.setText("");

        FeatureFont = new Font("Arial", Font.BOLD, 14);
        CommentFont = new Font("Arial", Font.ITALIC, 10);
        
        Feature.setFont(FeatureFont);
        Comment.setFont(CommentFont);
        
        ad = AvailableData;
        mbs = new MicroBlockMeasurementSetup(position, AvailableData, nvol);

        mbs.setVisible(true);
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
        
//        Popup = new JPopupMenu();
//        ValidationSelector = new JCheckBoxMenuItem("Perform Validation on this method");
//        ValidationSelector.addItemListener(new java.awt.event.ItemListener() {
//           @Override
//           public void itemStateChanged(ItemEvent ae){
//                setupForValidation(ValidationSelector.isSelected());
//           }
//        });
//        ValidationSelector.setEnabled(false);
//        Popup.add(ValidationSelector);
        
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
        int len = al.size();
        
        Feature.setText(al.get(2).toString());
        
        String text;
        boolean validate;
        try{
            Class<?> c;
            c = Class.forName(OBJECTMEASUREMENTMAP.get((al.get(2)).toString()));
            Method getBlockComment = c.getMethod("getBlockComment", ArrayList.class);
            text = getBlockComment.invoke(null, (Object)al).toString();
            Field f = c.getField("validate");
            validate = f.getBoolean(null);
        }catch(Exception e){
            e.printStackTrace();
            text = "Text not available...";
            validate = false;
        }
        
        Comment.setText(text);
        
        notifyRebuildPanelListeners(4);
        

        this.settings = al;
        this.settings.set(3, false);
        addToolTip();
        
        if(validate){
            ValidationSelector.setEnabled(true);
        }else{
            ValidationSelector.setEnabled(false);
            ValidationSelector.setSelected(false);
        }
        setupForValidation(ValidationSelector.isSelected());
    }
    
    @Override
    protected void deleteStep(int type) {
        this.notifyDeleteBlockListeners(type, this.position);
        this.notifyRebuildPanelListeners(type);
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
        String tt = "<html>";
        ArrayList data = (ArrayList)this.settings.get(1);
        String curline = "";
        
        for(int i = 0; i < data.size(); i++){
            if((boolean)data.get(i)){
                String f = ad.get(i).toString();
                int flen = f.length();
                if(curline.length() + flen > 67){
                    curline = curline.concat("<br>");
                    tt = tt.concat(curline);
                    curline = "";
                }
                curline = curline.concat(f);
                if(curline.length() > 0){
                    curline = curline.concat(", ");
                }
            }
        }
        tt = tt.concat(curline);
        
        tt = tt.substring(0, tt.lastIndexOf(",")); //removes trailing ','
        tt = tt.concat("</html>");
        
        step.setToolTipText(tt);
    }
    
    private void setupForValidation(boolean selected){
            this.settings.set(3, selected);
    }
}
