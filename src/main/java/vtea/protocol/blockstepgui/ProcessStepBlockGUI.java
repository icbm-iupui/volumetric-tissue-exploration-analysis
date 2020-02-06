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

import vtea.protocol.ProtocolManagerMulti;
import vtea.protocol.listeners.DeleteBlockListener;
import vtea.protocol.listeners.MicroBlockSetupListener;
import vtea.protocol.listeners.RebuildPanelListener;
import vtea.protocol.setup.MicroBlockProcessSetup;
import vtea.protocol.setup.MicroBlockSetup;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.Duplicator;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import static vtea._vtea.PROCESSINGMAP;
import vtea.imageprocessing.AbstractImageProcessing;
import vtea.processor.ImageProcessingProcessor;
import vtea.protocol.datastructure.ImageProcessingProtocol;
import vtea.workflow.ImageProcessingWorkflow;

/**
 *
 * @author vinfrais
 */
public class ProcessStepBlockGUI extends AbstractMicroBlockStepGUI implements Serializable, Cloneable, MicroBlockSetupListener {

    private static final long serialVersionUID = 8141562834104755012L;

//        protected JPanel step = new JPanel();
//    Font PositionFont = new Font("Arial", Font.PLAIN, 18);
//    Font ProcessFont = new Font("Arial", Font.BOLD, 12);
//    Font CommentFont = new Font("Arial", Font.ITALIC, 14);
//    JLabel Position = new JLabel();
//    public JLabel Comment = new JLabel("Block by Block");
    JLabel Process = new JLabel("First things first");
    String ProcessString;
    int processChannel;
    boolean ProcessTypeSet = false;
//    int position;
//    int type;
//    ArrayList<String> Channels;
    Color BlockColor;
    //boolean multiple;

    JWindow thumb = new JWindow();

    ImagePlus ThumbnailImage;
    ImagePlus OriginalImage;
    ImagePlus PreviewThumbnailImage;

    Boolean updatePreviewImage = true;

    public MicroBlockProcessSetup mbs;

    private ArrayList settings;

    private ArrayList ProtocolAll;

    
    public ArrayList<RebuildPanelListener> rebuildpanelisteners = new ArrayList<>();
    public ArrayList<DeleteBlockListener> deleteblocklisteners = new ArrayList<>();


    public ProcessStepBlockGUI() {
        
    }

    public ProcessStepBlockGUI(String ProcessText, String CommentText, Color BlockColor,
           boolean multiple, ImagePlus ThumbnailImage, ImagePlus OriginalImage, ArrayList<String> Channels, 
           int type, ArrayList<ProcessStepBlockGUI> protocol, int position) {
        
        BuildProcessStepBlock(ProcessText, CommentText, BlockColor, multiple, 
                ThumbnailImage, OriginalImage, Channels, type, protocol, position); 
    }
    ;
        
    private void BuildProcessStepBlock(String ProcessText, String CommentText, Color BlockColor, 
            boolean multiple, ImagePlus ThumbnailImage, ImagePlus imp, ArrayList<String> Channels, 
            final int type, ArrayList<ProcessStepBlockGUI> protocol, final int position) {

        
        int impWidth = imp.getWidth();
        int impHeight = imp.getHeight();
        
        int previewHeight = 256;
        int previewWidth = 256;
        int previewStartX = impWidth/4;
        int previewStartY = impHeight/4;
        
        
        if(imp.getWidth() < 256) {
            previewWidth = imp.getWidth();
            previewStartX = 0;
        }
        if(imp.getHeight() < 256) {
            previewHeight = imp.getHeight();
            previewStartY = 0;
        }
        

            
        imp.setRoi(new Roi(previewStartX, previewStartY, previewWidth, previewHeight));
        OriginalImage = new Duplicator().run(imp);
        imp.deleteRoi();
        
        this.OriginalImage = OriginalImage;
        
        this.Channels = (ArrayList) Channels.clone();
        this.Channels.add("All");
        this.position = position;
        this.type = type;
        this.BlockColor = BlockColor;

        this.ProcessString = ProcessText;

        this.ProtocolAll = protocol;

        if (ProcessString.length() > 30) {
            Process.setText(ProcessString.substring(0, 30).replace("DUP_", "") + "...");
            
        } else {
            Process.setText(ProcessText.replace("DUP_", ""));
        }

        Comment.setText(CommentText);

        step.setBackground(BlockColor);

        //need max size set here
        Position.setText(position + ".");

        Position.setFont(PositionFont);

        if (Process.getText().length() > 12) {
            ProcessFont = new Font("Arial", Font.BOLD, 10);
        }
        if (Comment.getText().length() < 12) {
            CommentFont = new Font("Arial", Font.BOLD, 12);
        }

        Process.setFont(ProcessFont);
        Comment.setFont(CommentFont);

        mbs = new vtea.protocol.setup.MicroBlockProcessSetup(position, Channels, protocol, OriginalImage);
        
       
        
        
        mbs.setVisible(false);
        mbs.addMicroBlockSetupListener(this);

       
        JButton DeleteButton = new JButton();
        DeleteButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                deleteStep(type);
                mbs.setVisible(false);
            }
        });

        JButton EditButton = new JButton();
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
        step.add(Process, layoutConstraints);

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
            
            mbs.setVisible(true);
        }

        step.addMouseListener(new java.awt.event.MouseListener() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
            };
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
            };
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                thumb.setVisible(false);};
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (!SwingUtilities.isRightMouseButton(evt)) {
                    showThumbnail(evt.getXOnScreen(), evt.getYOnScreen());
                }};
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) { };
            });

        }

    @Override
        protected void showThumbnail(int x, int y) {  
            
        int previewSize = 256;  

        int previewWidth = previewSize;
        int previewHeight = previewSize;
        

        if(OriginalImage.getWidth() < 256){previewWidth = OriginalImage.getWidth();}
        if(OriginalImage.getHeight() < 256){previewHeight = OriginalImage.getHeight();}
        
        thumb = new JWindow(); 
        thumb.setSize(new Dimension(previewWidth, previewHeight));

      
        if(OriginalImage.getWidth() > previewSize && OriginalImage.getHeight() > previewSize){   
            OriginalImage.setRoi(((OriginalImage.getWidth()-previewSize)/2), ((OriginalImage.getHeight()-previewSize)/2), previewWidth, previewHeight);
             
            ThumbnailImage = new Duplicator().run(OriginalImage); 
            OriginalImage.deleteRoi();
        }else if(OriginalImage.getWidth() > previewSize){
            OriginalImage.setRoi(((OriginalImage.getWidth()-previewSize)/2), 0, previewWidth, previewHeight);
             ThumbnailImage = new Duplicator().run(OriginalImage);

            OriginalImage.deleteRoi();
        }else if(OriginalImage.getHeight() > previewSize){
            OriginalImage.setRoi(0, ((OriginalImage.getHeight()-previewSize)/2), previewWidth, previewHeight);
            
             
            ThumbnailImage = new Duplicator().run(OriginalImage);
            OriginalImage.deleteRoi();

        }else{
            ThumbnailImage = new Duplicator().run(OriginalImage);
        }      
        

        if (position > 1 && updatePreviewImage) {
            ThumbnailImage = new Duplicator().run(OriginalImage);
            ThumbnailImage = previewThumbnail(ThumbnailImage);
            if(ThumbnailImage.getNSlices() > 1)
                ThumbnailImage.setZ(ThumbnailImage.getNSlices()/2);  
            thumb.add(new PreviewImagePanel(ThumbnailImage.getImage()));
            
           
        } else {
            ThumbnailImage = new Duplicator().run(OriginalImage);
            if(ThumbnailImage.getNSlices() > 1)
                ThumbnailImage.setZ(ThumbnailImage.getNSlices()/2);
            thumb.add(new PreviewImagePanel(ThumbnailImage.getImage()));
        }
        thumb.setLocation(x, y);
        thumb.setVisible(true);
    }

    private ImagePlus previewThumbnail(ImagePlus imp) {

        ImageProcessingProtocol options = new ImageProcessingProtocol((String)settings.get(0));

        if(Process.getText().equals("Process Step")){
            return imp; 
        }else{
            options.add(settings);
            ImageProcessingProcessor previewEngine = new ImageProcessingProcessor(imp, options);
            return previewEngine.processPreview();
        }
    }
    
    public void setImages(ImagePlus original, ImagePlus thumbnail){
        OriginalImage = original;
        ThumbnailImage = thumbnail;
      
        ((MicroBlockProcessSetup)mbs).setImage(OriginalImage);
        ((MicroBlockProcessSetup)mbs).resetSliderRange();
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
    
    public void setVariables(ArrayList al){
        settings = al;
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

    public int getChannel() {
        return processChannel;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        
        super.clone();
        
        System.out.println("PROFILING: Copying image processing: " + Process.getText());
        System.out.println("PROFILING: with parameter count: " + this.mbs.getProcessList().size());
        
        ProcessStepBlockGUI Copy = new ProcessStepBlockGUI(Process.getText(), Comment.getText(), this.BlockColor, false, this.ThumbnailImage.duplicate(), OriginalImage.duplicate(), this.Channels, this.type, this.ProtocolAll, this.position);

        Copy.updatePreviewImage = true;
        
        
        Copy.mbs = new vtea.protocol.setup.MicroBlockProcessSetup(position, Channels, ProtocolAll, OriginalImage);
        Copy.mbs.setVisible(false);
        Copy.mbs.addMicroBlockSetupListener(Copy);
        Copy.mbs.cloneProcessList(Process.getText(), this.mbs.getProcessList());
        
        

        return Copy;
    }
    
    public ArrayList getSetup(){
        //Arraylist for making a new Setup window
        
        //0: Channel
        //1: Method
        //2: Arraylist of components for method detail
        
        ArrayList setup = new ArrayList();
        
        setup.add(mbs.getChannel());
        setup.add(mbs.getMethod());
        setup.add(mbs.getProcessList());
        
        return setup;
        
    }
    
        
    
    public void updateSetup(){
        ((MicroBlockProcessSetup)mbs).updateProtocol();
    }

    @Override
    public void onChangeSetup(ArrayList al) {
        
        Process.setText(al.get(0).toString());
        processChannel = (Integer) al.get(1);
        Comment.setText("On channel: " + ((Integer) al.get(1) + 1));

        notifyRebuildPanelListeners(ProtocolManagerMulti.PROCESS);

        this.settings = al;
        updatePreviewImage = true;
    }
    
    @Override
    protected void deleteStep(int type) {
        this.notifyDeleteBlockListeners(type, this.position);
        this.notifyRebuildPanelListeners(type);

    }
}
