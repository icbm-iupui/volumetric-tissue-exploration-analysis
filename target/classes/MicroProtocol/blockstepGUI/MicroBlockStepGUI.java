/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MicroProtocol.blockstepGUI;

import MicroProtocol.ProtocolManagerMulti;
import MicroProtocol.listeners.DeleteBlockListener;
import MicroProtocol.listeners.MicroBlockSetupListener;
import MicroProtocol.listeners.RebuildPanelListener;
import MicroProtocol.setup.MicroBlockSetup;
import ij.IJ;
import ij.ImagePlus;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

/**
 *
 * @author vinfrais
 */
public class MicroBlockStepGUI implements MicroBlockSetupListener {

    JPanel step = new JPanel();
    Font PositionFont = new Font("Arial", Font.PLAIN, 18);
    Font ProcessFont = new Font("Arial", Font.BOLD, 12);
    Font CommentFont = new Font("Arial", Font.ITALIC, 10);
    JLabel Position = new JLabel();
    public JLabel Comment = new JLabel("Block by Block");
    JLabel Headline = new JLabel("First things first");
    
    int position;
    int type;
    ArrayList<String> Channels;
        //boolean multiple;

    JWindow thumb = new JWindow();
    ImagePlus ThumbnailImage;
    ImagePlus PreviewThumbnailImage;

    MicroBlockSetup mbs;

    private ArrayList settings;
    private ArrayList<RebuildPanelListener> rebuildpanelisteners = new ArrayList<RebuildPanelListener>();
    private ArrayList<DeleteBlockListener> deleteblocklisteners = new ArrayList<DeleteBlockListener>();

    MicroBlockStepGUI() {
    }

    protected void BuildStepBlock(String ProcessText, String CommentText, Color BlockColor, boolean multiple, ImagePlus ThumbnailImage, ArrayList<String> Channels, final int type, final int position) {

        this.ThumbnailImage = ThumbnailImage;
        this.PreviewThumbnailImage = ThumbnailImage.duplicate();
        this.Channels = Channels;
        this.position = position;
        this.type = type;
        //IJ.log("BLOCK TYPE: " + type);

        Headline.setText(ProcessText);

        Comment.setText(CommentText);
        step.setBackground(BlockColor);

        //need max size set here
        Position.setText(position + ".");

        Position.setFont(PositionFont);

        if (Headline.getText().length() > 12) {
            ProcessFont = new Font("Arial", Font.BOLD, 8);
        }
        if (Comment.getText().length() > 12) {
            CommentFont = new Font("Arial", Font.BOLD, 6);
        }

        Headline.setFont(ProcessFont);
        Comment.setFont(CommentFont);

        mbs = new MicroProtocol.setup.MicroBlockProcessSetup(position, Channels);

        mbs.setVisible(false);
        mbs.addMicroBlockSetupListener(this);

        JButton DeleteButton = new JButton();
        DeleteButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {

                //deleteProcessStep(position);
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
        step.add(Headline, layoutConstraints);

        layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
        layoutConstraints.gridx = 1;
        layoutConstraints.gridy = 1;
        layoutConstraints.weightx = 20;
        layoutConstraints.weighty = 20;
        step.add(Comment, layoutConstraints);

        if (position > 1) {

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

        }

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
                if (!SwingUtilities.isRightMouseButton(evt)) {
                    showThumbnail(evt.getXOnScreen(), evt.getYOnScreen());
                }
            }

            ;
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
//                    if(SwingUtilities.isRightMouseButton(evt) && position == 1) { 
//                        IJ.log("Rightclick detected on step: " + position);       
//                        mfm.show(evt.getComponent(), evt.getX(), evt.getY());
//                          }
            }
        ;

    }

    );

        //step.
            


        }
        
    protected void deleteStep(int type, int position) {
        this.notifyDeleteBlockListeners(type, position);
        this.notifyRebuildPanelListeners(type);

    }

    protected void showThumbnail(int x, int y) {
        thumb.setSize(300, 300);
                        //ExtractSteps
        //new micro preproccessing use imp returned.

        thumb.add(new ImagePanel(ThumbnailImage.getImage()));
        thumb.setLocation(x, y);
        thumb.setVisible(true);
    }

    private void ProcessTypeManager() {
    }

    public void setPosition(int n) {
        position = n;
        Position.setText(position + ".");
    }

    public JPanel getPanel() {
        return step;
    }

    public int getPosition() {
        return position;
    }

    public ArrayList getVariables() {
        return settings;
    }

    public void addRebuildPanelListener(RebuildPanelListener listener) {
        rebuildpanelisteners.add(listener);
    }

    protected void notifyRebuildPanelListeners(int type) {
        for (RebuildPanelListener listener : rebuildpanelisteners) {
            listener.rebuildPanel(type);
        }
    }

    public void addDeleteBlockListener(DeleteBlockListener listener) {
        deleteblocklisteners.add(listener);
    }

    protected void notifyDeleteBlockListeners(int type, int position) {
        for (DeleteBlockListener listener : deleteblocklisteners) {
            listener.deleteBlock(type, position);
        }
    }

    @Override
    public void onChangeSetup(ArrayList al) {

//        Headline.setText(al.get(0).toString());
//        Comment.setText(al.get(1).toString());
//
//        notifyRebuildPanelListeners(ProtocolManagerMulti.PROCESS);
//
//        this.settings = al;
    }

}
