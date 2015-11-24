/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package MicroProtocol.blockstepGUI;

import MicroProtocol.listeners.MicroBlockSetupListener;
import MicroProtocol.setup.MicroBlockObjectSetup;
import MicroProtocol.setup.MicroBlockSetup;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author vinfrais
 */
    public final class ObjectStepBlockGUI extends MicroBlockStepGUI implements MicroBlockSetupListener {

        JPanel step = new JPanel();
        Font PositionFont = new Font("Arial", Font.PLAIN, 16);
        Font ObjectFont = new Font("Arial", Font.BOLD, 12);
        Font CommentFont = new Font("Arial", Font.ITALIC, 10);
        JLabel Position = new JLabel();
        JLabel Comment = new JLabel("Block by Block");
        JLabel Object = new JLabel("First things first");
        boolean ProcessTypeSet = false;
        int position;

        MicroBlockSetup mbs;

        private ArrayList settings;

        public ObjectStepBlockGUI() {
            
        }

        public ObjectStepBlockGUI(String ProcessText, String CommentText, Color BlockColor, int position) {
            BuildStepBlock(ProcessText, CommentText, Color.GREEN, position);
        }

        private void BuildStepBlock(String ProcessText, String CommentText, Color BlockColor, final int position) {

//            if (ObjectStepsList.isEmpty()) {
//                position = 1;
//            } else {
//                position = ObjectStepsList.size() + 1;
//            }

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

            mbs = new MicroBlockObjectSetup(position, Channels);

            mbs.setVisible(false);
            mbs.addMicroBlockSetupListener(this);

            JButton DeleteButton = new JButton();
            DeleteButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    deleteStep(type, position);
                }
            });

            JButton EditButton = new JButton();
            EditButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    mbs.setVisible(true);
                    //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            });

            DeleteButton.setSize(20, 20);
            DeleteButton.setBackground(VTC._VTC.BUTTONBACKGROUND);
            DeleteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit-delete-6_16.png")));

            EditButton.setSize(20, 20);
            EditButton.setBackground(VTC._VTC.BUTTONBACKGROUND);
            EditButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit-4.png")));

            step.setSize(205, 20);
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

    }
