/*
 * Copyright (C) 2018 SciJava
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
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import vtea.protocol.listeners.DeleteBlockListener;
import vtea.protocol.listeners.MicroBlockSetupListener;
import vtea.protocol.listeners.RebuildPanelListener;
import vtea.protocol.setup.MicroBlockFeatureSetup;
import vtea.protocol.setup.MicroBlockSetup;

/**
 *
 * @author drewmcnutt
 */
public class FeatureStepBlockGUI extends AbstractMicroBlockStepGUI implements MicroBlockSetupListener{
    
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

    MicroBlockSetup mbs;

    private ArrayList settings;

    public ArrayList<RebuildPanelListener> rebuildpanelisteners = new ArrayList<>();
    public ArrayList<DeleteBlockListener> deleteblocklisteners = new ArrayList<>();

    public FeatureStepBlockGUI() {
    }
    
    public FeatureStepBlockGUI(String FeatureText, String CommentText, Color BlockColor, int position, ArrayList AvailableData, int nvol) {
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

        mbs = new MicroBlockFeatureSetup(position, AvailableData, nvol);

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
                }

                ;
            @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    //thumb.setVisible(false);
                }

                ;
            @Override
                public void mouseReleased(java.awt.event.MouseEvent evt) {
                    //thumb.setVisible(false);
                }

                ;
            @Override
                public void mousePressed(java.awt.event.MouseEvent evt) {

                }

                ;
            @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                }
            ;
        }

        );
        }    
    @Override
    public void setPosition(int n) {
        position = n;
        Position.setText(position + ".");
    }

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
    
    public void repaintWindow(){
        mbs.repaint();
    }
    
    public MicroBlockSetup getSetup(){
        return mbs;
    }
    
    public void updateSetup(){
        ((MicroBlockFeatureSetup)mbs).updateProtocol();
    }
    
    @Override
    public void onChangeSetup(ArrayList al){
        int len = al.size();
//        System.out.printf("The length is %d", len);
//        for(Object o: al)
//            System.out.println(o.toString());
        Feature.setText(al.get(1).toString());
        
        if(len == 2){
            Comment.setText("");
        }else if(al.get(1).toString().contains("Clustering")){
            JSpinner nclust = (JSpinner)al.get(4);
            JLabel lab = (JLabel)al.get(3);
            Comment.setText(lab.getText() + ((Integer)nclust.getValue()).toString());
        }else if(len == 11){
            JLabel lab1 = (JLabel)al.get(3);
            JLabel lab2 = (JLabel)al.get(5);
            JLabel lab3 = (JLabel)al.get(7);
            JLabel lab4 = (JLabel)al.get(9);
            JTextField val1 = (JTextField)al.get(4);
            JTextField val2 = (JTextField)al.get(6);
            JTextField val3 = (JTextField)al.get(8);
            JCheckBox val4 = (JCheckBox)al.get(10);
            Comment.setText("<html>" + lab1.getText() + ": " + (Integer.parseInt(val1.getText())) + ", " +  lab2.getText() + ": " + (Integer.parseInt(val2.getText())) + ", " +  lab3.getText() + ": " + (Integer.parseInt(val3.getText())) + ", " +  lab4.getText() + ": " + val4.isSelected() + "</html>");
        }
        
        notifyRebuildPanelListeners(4);
        
        this.settings = al;
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
    
}
