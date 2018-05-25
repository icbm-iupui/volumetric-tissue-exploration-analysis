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
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
    //JButton PreviewButton;

    MicroBlockSetup mbs;

    private ArrayList settings;

    public ArrayList<RebuildPanelListener> rebuildpanelisteners = new ArrayList<>();
    public ArrayList<DeleteBlockListener> deleteblocklisteners = new ArrayList<>();

    public FeatureStepBlockGUI() {
    }
    
    public FeatureStepBlockGUI(String FeatureText, String CommentText, Color BlockColor, int position, ArrayList AvailableData, List plotvalues) {
            BuildStepBlock(FeatureText, CommentText, Color.GRAY, position, AvailableData, plotvalues);
    }
    
    private void BuildStepBlock(String ProcessText, String CommentText, Color BlockColor, final int position, ArrayList AvailableData, List plotvalues) {
        this.position = position;
        Feature.setText(ProcessText);

        //Comment.setText(CommentText);
        step.setBackground(BlockColor);

        //need max size set here
        Position.setText(position + ".");
        Position.setFont(PositionFont);
        
        Comment.setText("");

        if (Feature.getText().length() < 12) {
            FeatureFont = new Font("Arial", Font.BOLD, 8);
        }
        if (Comment.getText().length() > 12) {
            CommentFont = new Font("Arial", Font.BOLD, 5);
        }
        Feature.setFont(FeatureFont);
        Comment.setFont(CommentFont);

        mbs = new MicroBlockFeatureSetup(position, AvailableData, plotvalues);

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
        step.add(new JLabel(""), layoutConstraints);

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
