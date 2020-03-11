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

import vtea.protocol.listeners.MicroBlockSetupListener;
import vtea.protocol.setup.MicroBlockObjectSetup;
import vtea.protocol.setup.MicroBlockSetup;
import ij.ImagePlus;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import vtea.protocol.listeners.DeleteBlockListener;
import vtea.protocol.listeners.RebuildPanelListener;

/**
 *
 * @author vinfrais
 */
    public final class ObjectStepBlockGUI extends AbstractMicroBlockStepGUI implements MicroBlockSetupListener {

        JPanel step = new JPanel();
        Font PositionFont = new Font("Arial", Font.PLAIN, 16);
        Font ObjectFont = new Font("Arial", Font.BOLD, 12);
        Font CommentFont = new Font("Arial", Font.ITALIC, 10);
        JLabel Position = new JLabel();
        JLabel Comment = new JLabel("Block by Block");
        JLabel Object = new JLabel("First things first");
        boolean ProcessTypeSet = false;
        int position;
        
        JButton DeleteButton;
        JButton EditButton;
        JButton PreviewButton;

        MicroBlockSetup mbs;

        private ArrayList settings;
        
        public ArrayList<RebuildPanelListener> rebuildpanelisteners = new ArrayList<>();
        public ArrayList<DeleteBlockListener> deleteblocklisteners = new ArrayList<>();

        public ObjectStepBlockGUI() {
            
        }

        public ObjectStepBlockGUI(String ProcessText, String CommentText, Color BlockColor, int position, ImagePlus Image) {
            BuildStepBlock(ProcessText, CommentText, Color.GREEN, position, Image);
        }

        private void BuildStepBlock(String ProcessText, String CommentText, Color BlockColor, final int position, ImagePlus Image) {

           
            
            
            Object.setText(ProcessText);

            Comment.setText(CommentText);
            step.setBackground(BlockColor);

            //need max size set here
            Position.setText("Object classifier " + position);
            Position.setFont(PositionFont);

            if (Object.getText().length() < 12) {
                ObjectFont = new Font("Arial", Font.BOLD, 8);
            }
            if (Comment.getText().length() > 12) {
                CommentFont = new Font("Arial", Font.BOLD, 5);
            }

            Object.setFont(ObjectFont);
            Comment.setFont(CommentFont);

            mbs = new MicroBlockObjectSetup(position, Channels, Image);

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
            
            PreviewButton = new JButton();
            PreviewButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    
                    
                }
            });

            DeleteButton.setPreferredSize(new Dimension(20, 20));
            DeleteButton.setBackground(vtea._vtea.BUTTONBACKGROUND);
            DeleteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit-delete-6_16.png")));

            EditButton.setPreferredSize(new Dimension(20, 20));
            EditButton.setBackground(vtea._vtea.BUTTONBACKGROUND);
            EditButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit-4.png")));
            
            PreviewButton.setSize(20, 20);
            PreviewButton.setBackground(vtea._vtea.BUTTONBACKGROUND);
            PreviewButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/eye.png")));
            
            if(Comment.getText().isEmpty()){
                PreviewButton.setEnabled(false);
            }else{
                PreviewButton.setEnabled(true);
            }

            step.setSize(205, 40);
            step.setBorder(javax.swing.BorderFactory.createEtchedBorder());

            step.setLayout(new GridBagLayout());
            GridBagConstraints layoutConstraints = new GridBagConstraints();

            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 0;
            layoutConstraints.gridy = 0;
            layoutConstraints.weightx = 1;
            layoutConstraints.weighty = 1;
            step.add(Position, layoutConstraints);

            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 1;
            layoutConstraints.gridy = 0;
            layoutConstraints.weightx = 20;
            layoutConstraints.weighty = 20;
            step.add(Object, layoutConstraints);

            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 1;
            layoutConstraints.gridy = 1;
            layoutConstraints.weightx = 20;
            layoutConstraints.weighty = 20;
            step.add(Comment, layoutConstraints);

            layoutConstraints.fill = GridBagConstraints.BOTH;
            layoutConstraints.gridx = 2;
            layoutConstraints.gridy = 0;
            layoutConstraints.weightx = -1;
            layoutConstraints.weighty = -1;
            layoutConstraints.ipadx = -1;
            layoutConstraints.ipady = -1;
            step.add(DeleteButton, layoutConstraints);

            layoutConstraints.fill = GridBagConstraints.BOTH;
            layoutConstraints.gridx = 3;
            layoutConstraints.gridy = 2;
            layoutConstraints.weightx = -1;
            layoutConstraints.weighty = -1;
            layoutConstraints.ipadx = -1;
            layoutConstraints.ipady = -1;
            step.add(EditButton, layoutConstraints);
            
            layoutConstraints.fill = GridBagConstraints.BOTH;
            layoutConstraints.gridx = 3;
            layoutConstraints.gridy = 1;
            layoutConstraints.weightx = -1;
            layoutConstraints.weighty = -1;
            layoutConstraints.ipadx = -1;
            layoutConstraints.ipady = -1;
            step.add(PreviewButton, layoutConstraints);

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
                    thumb.setVisible(false);
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
        
        @Override
        protected void deleteStep(int type) {
            this.notifyDeleteBlockListeners(type, position);
            this.notifyRebuildPanelListeners(type);

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
