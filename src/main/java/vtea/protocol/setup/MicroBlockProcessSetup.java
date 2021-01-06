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
package vtea.protocol.setup;

import ij.ImagePlus;
import ij.LookUpTable;
import ij.plugin.ChannelSplitter;
import ij.plugin.Duplicator;
import ij.process.LUT;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import static vtea._vtea.PROCESSINGMAP;
import static vtea._vtea.PROCESSINGOPTIONS;
import vtea.imageprocessing.AbstractImageProcessing;
import vtea.processor.ImageProcessingProcessor;
import vtea.protocol.blockstepgui.ProcessStepBlockGUI;
import vtea.protocol.datastructure.ImageProcessingProtocol;

/**
 *
 * @author vinfrais
 */
public class MicroBlockProcessSetup extends MicroBlockSetup implements ChangeListener, ActionListener {

    ImagePlus OriginalImage;
    ImagePlus ProcessPreview;
    int currentSlice;

    boolean preview = true;
    boolean includePrevious = true;

    JSlider sliceSlider;
    JToggleButton previewControl;
    JToggleButton previousInclude;

    ArrayList<ProcessStepBlockGUI> ProtocolAll;

    public MicroBlockProcessSetup() {
    }

    public MicroBlockProcessSetup(int step, ArrayList Channels, ArrayList<ProcessStepBlockGUI> Protocols, ImagePlus imp) {

        super(step, Channels);

        ProtocolAll = Protocols;
        //OriginalImage = new Duplicator().run(imp);
        OriginalImage = imp;
        imp.deleteRoi();

        if (OriginalImage.getNSlices() > 1) {
            currentSlice = OriginalImage.getNSlices() / 2;
        }
        //subclass specific settings
        TitleText.setText("Processing, Step " + step);
        TitleText.setEditable(false);
        processComboBox = new DefaultComboBoxModel(vtea._vtea.PROCESSINGOPTIONS);
        processComboBox.setSelectedItem("Select Method");
        MethodDetails.repaint();
        jTextPane1.setText("");
        ProcessText.setText("Method: ");
        ChannelSelection.setText("Channel: ");
        ProcessSelectComboBox.setModel(processComboBox);
        ProcessSelectComboBox.setVisible(true);

        JPanel imagePanel = new JPanel() {

            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                BufferedImage image = imp.getBufferedImage(); // get your buffered image.
                Graphics2D g2 = (Graphics2D) g;
                g2.drawImage(image, null, 10, 10);

            }
        };
        imagePanel.setPreferredSize(new Dimension(256, 256));
        imagePanel.setMinimumSize(new Dimension(256, 256));
        imagePanel.setLayout(new FlowLayout());
        imagePanel.setBackground(new Color(238,238,238));
        //imagePanel.setForeground(new Color(238,238,238));

        methodBuild.setLayout(new GridBagLayout());

        super.ChannelComboBox.addActionListener(this);
        super.ProcessSelectComboBox.addActionListener(this);

        GridBagConstraints layoutConstraints = new GridBagConstraints();

        layoutConstraints.fill = GridBagConstraints.CENTER;
        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 0;
        layoutConstraints.weightx = 1;
        layoutConstraints.weighty = 1;

        methodBuild.add(imagePanel, 0);

        layoutConstraints.fill = GridBagConstraints.CENTER;
        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 1;
        layoutConstraints.ipadx = 5;
        layoutConstraints.weightx = 1;
        layoutConstraints.weighty = 1;

        if (OriginalImage.getNSlices() > 1) {
            sliceSlider = new JSlider(SwingConstants.VERTICAL, 1, OriginalImage.getNSlices(), currentSlice);
            //sliceSlider.setPaintTrack(false);
            sliceSlider.addChangeListener(this);
        } else {
            sliceSlider = new JSlider(SwingConstants.VERTICAL, 1, 10, 1);
            sliceSlider.setEnabled(false);
        }

        methodBuild.add(sliceSlider);

        JPanel previewControlPanel = new JPanel();

        previewControlPanel.setMinimumSize(new Dimension(50, 280));
        previewControlPanel.setBackground(new Color(238,238,238));

        GridBagLayout gb = new GridBagLayout();

        previewControlPanel.setLayout(gb);

        previewControl = new JToggleButton();
        previewControl.setIcon((new javax.swing.ImageIcon(getClass().getResource("/icons/eye.png"))));
        previewControl.setToolTipText("Preview image processing.");
        previewControl.setEnabled(false);
        previewControl.setMaximumSize(new Dimension(45, 45));
        previewControl.addChangeListener(this);

        previousInclude = new JToggleButton();
        previousInclude.setIcon((new javax.swing.ImageIcon(getClass().getResource("/icons/eye_include.png"))));
        previousInclude.setMaximumSize(new Dimension(45, 45));
        previousInclude.setToolTipText("Preview all image processing.");
        previousInclude.addChangeListener(this);
        previousInclude.setEnabled(false);
        if (step > 2) {
            previousInclude.setEnabled(true);
        }

        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 1;
        layoutConstraints.ipadx = 5;
        layoutConstraints.weightx = 1;
        layoutConstraints.weighty = 1;

        gb.setConstraints(previewControl, layoutConstraints);
        previewControlPanel.add(previewControl);

        layoutConstraints.fill = layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 2;
        layoutConstraints.ipadx = 5;
        layoutConstraints.weightx = 1;
        layoutConstraints.weighty = 1;
        gb.setConstraints(previousInclude, layoutConstraints);
        previewControlPanel.add(previousInclude);

        //layoutConstraints.fill = GridBagConstraints.NON;
        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 2;
        layoutConstraints.ipadx = 5;
        layoutConstraints.weightx = 1;
        layoutConstraints.weighty = 1;
        gb.setConstraints(methodBuild, layoutConstraints);
        methodBuild.add(previewControlPanel);

        doPreview(currentSlice);
        repaint();
        pack();

    }

    private void doPreview(int slice) {
        ChannelSplitter cs = new ChannelSplitter();
        final ImagePlus imp = new ImagePlus("preview", cs.getChannel(new Duplicator().run(OriginalImage), ChannelComboBox.getSelectedIndex() + 1));
        if (imp.getNSlices() > 1) {
            ProcessPreview = new ImagePlus("ProcessPreview", imp.getStack().getProcessor(slice));
            ProcessPreview.resetStack();
        } else {
            ProcessPreview = new ImagePlus("ProcessPreview", imp.getProcessor());
        }

        if (this.previewControl.isSelected()) {
            String method = (String) ProcessSelectComboBox.getItemAt(ProcessSelectComboBox.getSelectedIndex());
            ArrayList<Component> comps = new ArrayList<Component>(Arrays.asList(this.MethodDetails.getComponents()));
            ArrayList variables = new ArrayList();
            variables.add(method);
            variables.add(0);
            variables.addAll(comps);
            ImageProcessingProtocol protocol = new ImageProcessingProtocol();
            protocol.add(variables);
            ImageProcessingProcessor previewEngine = new ImageProcessingProcessor(ProcessPreview, protocol);
            ProcessPreview = previewEngine.processPreview();
        }
        if (this.previousInclude.isSelected()) {

            CurrentStepProtocol = CurrentProcessList;
            super.notifyMicroBlockSetupListeners(CurrentStepProtocol);

            ImageProcessingProtocol protocol = new ImageProcessingProtocol();

            for (int i = 0; i < step; i++) {
                if (ProtocolAll.get(i).getChannel() == ChannelComboBox.getSelectedIndex()) {
                    protocol.add(ProtocolAll.get(i));
                }
            }

            protocol = ExtractSteps(protocol);
            //Extract protocol with Extract Steps
            //make new preprocessor

            ImageProcessingProcessor previewEngine = new ImageProcessingProcessor(ProcessPreview, protocol);
            //previewEngine.execute();
            ProcessPreview = previewEngine.processPreview();
        }
        imp.setPosition(slice);
        methodBuild.remove(0);
        //methodBuild.setPreferredSize(new Dimension(359,300));
        JPanel imagePanel = new JPanel() {

            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                BufferedImage image = ProcessPreview.getBufferedImage(); // get your buffered image.
                ProcessPreview.setLut(new LUT((IndexColorModel) LookUpTable.createGrayscaleColorModel(false), 0.0, Math.pow(2, ProcessPreview.getBitDepth()) - 1));
                Graphics2D g2 = (Graphics2D) g;
                g2.drawImage(image, null, 10, 10);

            }
        };
        //imagePanel.setBackground(Color.red);
        imagePanel.setPreferredSize(new Dimension(256, 256));
        imagePanel.setMinimumSize(new Dimension(256, 256));
        imagePanel.setBackground(new Color(238,238,238));

        GridBagConstraints layoutConstraints = new GridBagConstraints();

        //Image
        //layoutConstraints.anchor = GridBagConstraints.CENTER;
        layoutConstraints.fill = GridBagConstraints.CENTER;
        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 0;
        layoutConstraints.weightx = 1;
        layoutConstraints.weighty = 1;

        methodBuild.add(imagePanel, 0);

        //layoutConstraints.anchor = GridBagConstraints.CENTER;
        pack();
    }

//    public void setSetup(ArrayList ProcessComponents){
//        try{
//            
//        
//            
//        MethodDetails.setVisible(false);
//        MethodDetails.removeAll();
//
//        //BuiltPanel.setLayout(new GridBagLayout());
//        GridBagConstraints layoutConstraints = new GridBagConstraints();
//
//        //MethodDetail
//        if (ProcessComponents.size() > 0) {
//            layoutConstraints.fill = GridBagConstraints.CENTER;
//            layoutConstraints.gridx = 0;
//            layoutConstraints.gridy = 0;
//            layoutConstraints.weightx = 1;
//            layoutConstraints.weighty = 1;
//            MethodDetails.add((Component) ProcessComponents.get(0), layoutConstraints);
//        }
//
//        if (ProcessComponents.size() > 1) {
//            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
//            layoutConstraints.gridx = 1;
//            layoutConstraints.gridy = 0;
//            MethodDetails.add((Component) ProcessComponents.get(1), layoutConstraints);
//        }
//
//        if (ProcessComponents.size() > 2) {
//            layoutConstraints.fill = GridBagConstraints.CENTER;
//            layoutConstraints.gridx = 2;
//            layoutConstraints.gridy = 0;
//            MethodDetails.add((Component) ProcessComponents.get(2), layoutConstraints);
//        }
//        if (ProcessComponents.size() > 3) {
//            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
//            layoutConstraints.gridx = 3;
//            layoutConstraints.gridy = 0;
//            MethodDetails.add((Component) ProcessComponents.get(3), layoutConstraints);
//        }
//        if (ProcessComponents.size() > 4) {
//            layoutConstraints.fill = GridBagConstraints.CENTER;
//            layoutConstraints.gridx = 0;
//            layoutConstraints.gridy = 1;
//            MethodDetails.add((Component) ProcessComponents.get(4), layoutConstraints);
//        }
//        if (ProcessComponents.size() > 5) {
//            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
//            layoutConstraints.gridx = 1;
//            layoutConstraints.gridy = 1;
//            MethodDetails.add((Component) ProcessComponents.get(5), layoutConstraints);
//        }
//        if (ProcessComponents.size() > 6) {
//            layoutConstraints.fill = GridBagConstraints.CENTER;
//            layoutConstraints.gridx = 2;
//            layoutConstraints.gridy = 1;
//            MethodDetails.add((Component) ProcessComponents.get(6), layoutConstraints);
//        }
//        if (ProcessComponents.size() > 7) {
//            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
//            layoutConstraints.gridx = 3;
//            layoutConstraints.gridy = 1;
//            MethodDetails.add((Component) ProcessComponents.get(7), layoutConstraints);
//        }
//
//        pack();
//        MethodDetails.setVisible(true);
//        
//        CurrentProcessList.clear();
//
//        CurrentProcessList.add(processComboBox.getSelectedItem());
//        CurrentProcessList.add(channelsComboBox.getIndexOf(channelsComboBox.getSelectedItem()));
//        CurrentProcessList.addAll(ProcessComponents);
//            
//           
//            
//        }catch (Exception e){}
//        
//        
//    }
    public void updatePreviewImage(ImagePlus imp) {
        OriginalImage = imp;
        doPreview(OriginalImage.getNSlices() / 2);
        repaint();
        pack();
    }

    public void resetSliderRange() {
        sliceSlider.removeChangeListener(this);
        sliceSlider.setMinimum(1);
        sliceSlider.setMaximum(OriginalImage.getNSlices());
        sliceSlider.addChangeListener(this);
    }

    public void updateProtocol() {
        CurrentStepProtocol = CurrentProcessList;
        notifyMicroBlockSetupListeners(CurrentStepProtocol);
    }

    private ImageProcessingProtocol ExtractSteps(ArrayList sb_al) {

        ImageProcessingProtocol Result = new ImageProcessingProtocol();

        ProcessStepBlockGUI ppsb;

        ListIterator<Object> litr = sb_al.listIterator();
        while (litr.hasNext()) {
            ppsb = (ProcessStepBlockGUI) litr.next();
            if (!(ppsb.Comment.getText()).equals("New Image")) {
                Result.add(ppsb.getVariables());
            }
        }
        return Result;
    }

    @Override
    protected void updateProtocolPanel(ActionEvent evt) {
        if (evt.getSource() == this.ProcessSelectComboBox) {
            makeProtocolPanel(PROCESSINGOPTIONS[ProcessSelectComboBox.getSelectedIndex()]);
            previewControl.setEnabled(true);
        } else if (!checkChannels()) {
            includePrevious = false;
            previousInclude.setEnabled(false);
        } else {
            previousInclude.setEnabled(true);
            doPreview(currentSlice);
        }
    }

    private boolean checkChannels() {
        for (int i = 0; i < ProtocolAll.size(); i++) {
            if (ProtocolAll.get(i).getChannel() == this.ChannelComboBox.getSelectedIndex()) {
                return true;
            }
        }
        return false;
    }

//    public void setChannel(int position){
//        this.ChannelComboBox.setSelectedIndex(position);
//    }
//    
//    public void setMethod(String str){
//        this.ProcessSelectComboBox.setSelectedItem(str);
//
//    }
//   public String getMethod(){
//        return (String)ProcessSelectComboBox.getSelectedItem();
//    }
//   
//    public int getChannel(){
//        return this.ChannelComboBox.getSelectedIndex();
//    }
//    
    @Override
    public void setImage(ImagePlus imp) {
        OriginalImage = imp;
    }

    @Override
    public void cloneProcessList(String str, ArrayList al) {

    }

    @Override
    public ArrayList getProcessList() {
        return (ArrayList) CurrentProcessItems.get(0).clone();
    }

    @Override
    protected JPanel makeProtocolPanel(String str) {

        //JPanel BuiltPanel = new JPanel();
        ArrayList ProcessComponents;

        notesPane.setVisible(true);

        CurrentProcessItems.set(0, makeMethodComponentsArray(str, ProcessVariables));
        ProcessComponents = CurrentProcessItems.get(0);

        MethodDetails.setVisible(false);
        MethodDetails.removeAll();

        //BuiltPanel.setLayout(new GridBagLayout());
        GridBagConstraints layoutConstraints = new GridBagConstraints();

        //MethodDetail
        if (ProcessComponents.size() > 0) {
            layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.gridx = 0;
            layoutConstraints.gridy = 0;
            layoutConstraints.weightx = 1;
            layoutConstraints.weighty = 1;
            MethodDetails.add((Component) ProcessComponents.get(0), layoutConstraints);
        }

        if (ProcessComponents.size() > 1) {
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 1;
            layoutConstraints.gridy = 0;
            MethodDetails.add((Component) ProcessComponents.get(1), layoutConstraints);
        }

        if (ProcessComponents.size() > 2) {
            layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.gridx = 2;
            layoutConstraints.gridy = 0;
            MethodDetails.add((Component) ProcessComponents.get(2), layoutConstraints);
        }
        if (ProcessComponents.size() > 3) {
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 3;
            layoutConstraints.gridy = 0;
            MethodDetails.add((Component) ProcessComponents.get(3), layoutConstraints);
        }
        if (ProcessComponents.size() > 4) {
            layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.gridx = 0;
            layoutConstraints.gridy = 1;
            MethodDetails.add((Component) ProcessComponents.get(4), layoutConstraints);
        }
        if (ProcessComponents.size() > 5) {
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 1;
            layoutConstraints.gridy = 1;
            MethodDetails.add((Component) ProcessComponents.get(5), layoutConstraints);
        }
        if (ProcessComponents.size() > 6) {
            layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.gridx = 2;
            layoutConstraints.gridy = 1;
            MethodDetails.add((Component) ProcessComponents.get(6), layoutConstraints);
        }
        if (ProcessComponents.size() > 7) {
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 3;
            layoutConstraints.gridy = 1;
            MethodDetails.add((Component) ProcessComponents.get(7), layoutConstraints);
        }

        pack();
        MethodDetails.setVisible(true);

        CurrentProcessList.clear();

        CurrentProcessList.add(processComboBox.getSelectedItem());
        CurrentProcessList.add(channelsComboBox.getIndexOf(channelsComboBox.getSelectedItem()));
        CurrentProcessList.addAll(ProcessComponents);

        return MethodDetails;
    }

    @Override
    protected ArrayList makeMethodComponentsArray(String method, String[][] str) {

        Object iImp = new Object();

        try {
            Class<?> c;
            c = Class.forName(PROCESSINGMAP.get(method));
            Constructor<?> con;
            try {
                con = c.getConstructor();
                iImp = con.newInstance();
                return ((AbstractImageProcessing) iImp).getOptions();

            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(ImageProcessingProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ImageProcessingProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }

        ArrayList result = new ArrayList();

        return result;
    }

    @Override
    public void blockSetupOKAction() {
        CurrentStepProtocol = CurrentProcessList;
        super.notifyMicroBlockSetupListeners(CurrentStepProtocol);
        this.setVisible(false);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == sliceSlider) {
            currentSlice = ((JSlider) e.getSource()).getValue();
            doPreview(currentSlice);
        } else if (e.getSource() == this.previewControl) {

            //push an updated protocol for global consideration
            currentSlice = sliceSlider.getValue();
            doPreview(currentSlice);
        } else if (e.getSource() == this.previousInclude) {

            //need to get the updated global protocol
            currentSlice = sliceSlider.getValue();
            doPreview(currentSlice);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.ChannelComboBox) {
            previewControl.setSelected(false);
            previousInclude.setSelected(false);
            currentSlice = sliceSlider.getValue();

            if (CurrentProcessList.size() > 2) {
                CurrentProcessList.set(1, channelsComboBox.getIndexOf(channelsComboBox.getSelectedItem()));
            }

            doPreview(currentSlice);
        } else if (e.getSource() == ProcessSelectComboBox) {
            //System.out.println("PROFILING: Process position: " + this.ProcessSelectComboBox.getSelectedIndex());
            currentSlice = sliceSlider.getValue();
            updateProtocolPanel(e);
            doPreview(currentSlice);
        }
    }
}
