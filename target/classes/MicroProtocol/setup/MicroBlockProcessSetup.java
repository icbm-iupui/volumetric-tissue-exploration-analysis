/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MicroProtocol.setup;

import MicroProtocol.SingleImageProcessing;
import static MicroProtocol.SingleImageProcessing.OBJECTBLOCKS;
import static MicroProtocol.SingleImageProcessing.PROCESSBLOCKS;
import MicroProtocol.blockstepGUI.ProcessStepBlockGUI;
import ij.ImagePlus;
import ij.LookUpTable;
import ij.plugin.ChannelSplitter;
import ij.process.LUT;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ListIterator;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import vteapreprocessing.MicroProtocolPreProcessing;

/**
 *
 * @author vinfrais
 */
public class MicroBlockProcessSetup extends MicroBlockSetup implements ChangeListener, ActionListener {
    
    ImagePlus ThresholdPreview = new ImagePlus();
    ImagePlus OriginalImage;
    ImagePlus ProcessPreview;
    int currentSlice;
    
   boolean preview = true;
   boolean includePrevious = true;
   
   JSlider sliceSlider; 
   JToggleButton previewControl;
   JToggleButton previousInclude;
   
   ArrayList<ProcessStepBlockGUI> ProtocolAll;
           
    public MicroBlockProcessSetup(int step, ArrayList Channels, ArrayList<ProcessStepBlockGUI> Protocols, ImagePlus imp) {

        
        super(step, Channels);
        
        ProtocolAll = Protocols;
       
        OriginalImage = imp;
        
        currentSlice = OriginalImage.getNSlices()/2;
        //subclass specific settings
        TitleText.setText("Processing, Step " + step);
        TitleText.setEditable(false);
        String[] ProcessOptions = {"Select Method", "Background Subtraction", "Enhance Contrast", "Reduce Noise"};
        cbm = new DefaultComboBoxModel(ProcessOptions);
        cbm.setSelectedItem("Select Method");
        MethodDetails.repaint();
        jTextPane1.setText("");
        ProcessText.setText("Method: ");
        ChannelSelection.setText("Channel: ");
        ProcessSelectComboBox.setModel(cbm);
        tablePane.setVisible(false);
        secondaryTable.setVisible(false);
        ProcessSelectComboBox.setVisible(true);
        
        
        JPanel imagePanel = new JPanel(){
        
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            BufferedImage image = imp.getBufferedImage(); // get your buffered image.
            Graphics2D g2 = (Graphics2D) g;
            g2.drawImage(image, null, 10, 10);

        }
    };  
        //imagePanel.setBackground(Color.red);
        imagePanel.setPreferredSize(new Dimension(256,256));
        imagePanel.setMinimumSize(new Dimension(256,256));
        imagePanel.setBackground(VTC._VTC.BACKGROUND);
        
        methodBuild.setLayout(new GridBagLayout());
        
        super.ChannelComboBox.addActionListener(this);
        super.ProcessSelectComboBox.addActionListener(this);
        
        GridBagConstraints layoutConstraints = new GridBagConstraints();

        //Image
            //layoutConstraints.anchor = GridBagConstraints.CENTER;
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
            
            sliceSlider =new JSlider(SwingConstants.VERTICAL, 1, OriginalImage.getNSlices(), currentSlice); 

            sliceSlider.addChangeListener(this);
            
            methodBuild.add(sliceSlider);
            
            JPanel previewControlPanel = new JPanel();
            
            previewControlPanel.setMinimumSize(new Dimension(50, 280));
            previewControlPanel.setBackground(VTC._VTC.BACKGROUND);
            
            GridBagLayout gb = new GridBagLayout();
            
            previewControlPanel.setLayout(gb);
           
            
            previewControl = new JToggleButton();
            previewControl.setText("Pre");
            previewControl.setEnabled(false);
            previewControl.setMaximumSize(new Dimension(45,45));
            previewControl.addChangeListener(this);
            
            previousInclude = new JToggleButton();
            previousInclude.setText("Inc");
            previousInclude.setMaximumSize(new Dimension(45,45));
            previousInclude.addChangeListener(this);
            previousInclude.setEnabled(false);
            if(step > 2){previousInclude.setEnabled(true);}
            
            //layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.gridx = 0;
            layoutConstraints.gridy = 1;
            layoutConstraints.ipadx = 5;
            layoutConstraints.weightx = 1;
            layoutConstraints.weighty = 1;
            
            gb.setConstraints(previewControl, layoutConstraints);
            previewControlPanel.add(previewControl);
            
            //layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.fill = 
            layoutConstraints.gridx = 0;
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
    
    private void doPreviewInset(){}
    
    private void doPreview(int slice){
        ChannelSplitter cs = new ChannelSplitter();
        final ImagePlus imp = new ImagePlus("preview", cs.getChannel(OriginalImage.duplicate(), ChannelComboBox.getSelectedIndex()+1));
        
        ProcessPreview = new ImagePlus("ProcessPreview", imp.getStack().getProcessor(slice));
        
        
        if(this.previewControl.isSelected()){
            String method = (String)ProcessSelectComboBox.getItemAt(ProcessSelectComboBox.getSelectedIndex());
            ArrayList<Component> comps = new ArrayList<Component>(Arrays.asList(this.MethodDetails.getComponents()));
            ArrayList variables = new ArrayList();
            variables.add(method);
            variables.add(0);
            variables.addAll(comps);
            ArrayList<ArrayList> protocol = new ArrayList<ArrayList>();
            protocol.add(variables);
            MicroProtocolPreProcessing mpp = new MicroProtocolPreProcessing(ProcessPreview,protocol);          
            ProcessPreview = mpp.ProcessImage();
            ProcessPreview.resetDisplayRange();       
        }
        if(this.previousInclude.isSelected()){
            //make the new protocol list up to this step
            
           makeProtocolPanel(ProcessSelectComboBox.getSelectedIndex());
           CurrentStepProtocol = CurrentProcessList;
           super.notifyMicroBlockSetupListeners(CurrentStepProtocol);
           
           
           
            ArrayList<ProcessStepBlockGUI> protocol = new ArrayList<ProcessStepBlockGUI>();
         
            //System.out.println("PROFILING: Protocol has " +  ProtocolAll.size() + " steps.");
            
                for(int i = 0; i < step; i++){
                    if(ProtocolAll.get(i).getChannel() == ChannelComboBox.getSelectedIndex()){
                    protocol.add(ProtocolAll.get(i));
                    }
                }
            
            
            protocol = ExtractSteps(protocol);
            //Extract protocol with Extract Steps
            //make new preprocessor
            
            MicroProtocolPreProcessing mpp = new MicroProtocolPreProcessing(ProcessPreview,protocol);          
            ProcessPreview = mpp.ProcessImage();
            ProcessPreview.resetDisplayRange();  
        }
        imp.setPosition(slice);
        methodBuild.remove(0);
        //methodBuild.setPreferredSize(new Dimension(359,300));
        JPanel imagePanel = new JPanel(){
        
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            BufferedImage image = ProcessPreview.getBufferedImage(); // get your buffered image.
            ProcessPreview.setLut(new LUT((IndexColorModel)LookUpTable.createGrayscaleColorModel(false), 0.0 , Math.pow(2, ProcessPreview.getBitDepth())-1));
            Graphics2D g2 = (Graphics2D) g;
            g2.drawImage(image, null, 10, 10);

        }
    };  
        //imagePanel.setBackground(Color.red);
        imagePanel.setPreferredSize(new Dimension(256,256));
        imagePanel.setMinimumSize(new Dimension(256,256));
        imagePanel.setBackground(VTC._VTC.BACKGROUND);

        
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
    
    private ArrayList ExtractSteps(ArrayList sb_al) {

        ArrayList<ArrayList> Result = new ArrayList<ArrayList>();

            ProcessStepBlockGUI ppsb;

            ListIterator<Object> litr = sb_al.listIterator();
            while (litr.hasNext()) {
                ppsb = (ProcessStepBlockGUI) litr.next();
                if (!(ppsb.Comment.getText()).equals("New Image")) {
                    Result.add(ppsb.getVariables());
                    //System.out.println("PROFILING: preprocessing constants" + ppsb.getVariables());
                }
            }
        return Result;
    }

    @Override
    protected void updateProtocolPanel(ActionEvent evt) {
        if(evt.getSource() == this.ProcessSelectComboBox){
        super.updateProtocolPanel(evt);
            if(ProcessSelectComboBox.getSelectedIndex() > 0){
                previewControl.setEnabled(true);
            } else {
                previewControl.setEnabled(false);
                previousInclude.setEnabled(false);
            }
        }
        else if (!checkChannels()) {
            includePrevious = false;
            previousInclude.setEnabled(false);
        }
        else {
            previousInclude.setEnabled(true);
            doPreview(currentSlice);
        }
        
    }
    
    private boolean checkChannels(){
        
        
        for(int i = 0; i < ProtocolAll.size(); i++){
            if(ProtocolAll.get(i).getChannel() == this.ChannelComboBox.getSelectedIndex()){
                return true;
            }
        }
        
        return false;
    }
    
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
       MicroBlockProcessSetup Copy = new MicroBlockProcessSetup(this.step, this.Channels, this.ProtocolAll, this.OriginalImage);
       //Copy process items position by position
       
       ArrayList<Component> ComponentSource;
       ArrayList<Component> ComponentDestination = new ArrayList<Component>();
       
       Iterator<ArrayList> itr = this.CurrentProcessItems.iterator();
       
       while(itr.hasNext())
       {
           try {
               ComponentSource = itr.next();
               Iterator<Component> itr2 = ComponentSource.iterator();
               while(itr2.hasNext())
               {
                   ComponentDestination.add((Component)itr2.next());
               }
               
               Copy.CurrentProcessItems.add(ComponentDestination);
           }
           catch (NullPointerException npe) {Copy.CurrentProcessItems.add(null);}
       }
      Copy.ProcessSelectComboBox.setSelectedIndex(this.ProcessSelectComboBox.getSelectedIndex());
      Copy.updateProtocolPanel(new ActionEvent(new JMenuItem(),0,"menubar"));
      Copy.pack();
      return Copy;
    }

    @Override
    protected JPanel makeProtocolPanel(int position) {

        JPanel BuiltPanel = new JPanel();
        ArrayList ProcessComponents;

        notesPane.setVisible(true);
        tablePane.setVisible(false);
        
        

        
        if (CurrentProcessItems.get(position) == null) {
            ProcessComponents = CurrentProcessItems.set(position, makeMethodComponentsArray(position));
            ProcessComponents = CurrentProcessItems.get(position);
        } else {
            ProcessComponents = CurrentProcessItems.get(position);
        }

        MethodDetails.setVisible(false);
        MethodDetails.removeAll();

        BuiltPanel.setLayout(new GridBagLayout());
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
            //layoutConstraints.weightx = 1;
            //layoutConstraints.weighty = 1;
            MethodDetails.add((Component) ProcessComponents.get(1), layoutConstraints);
        }

        if (ProcessComponents.size() > 2) {
            layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.gridx = 2;
            layoutConstraints.gridy = 0;
            //layoutConstraints.weightx = 1;
            //layoutConstraints.weighty = 1;
            MethodDetails.add((Component) ProcessComponents.get(2), layoutConstraints);
        }
        if (ProcessComponents.size() > 3) {
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 3;
            layoutConstraints.gridy = 0;
            //layoutConstraints.weightx = 1;
            //layoutConstraints.weighty = 1;
            MethodDetails.add((Component) ProcessComponents.get(3), layoutConstraints);
        }
        if (ProcessComponents.size() > 4) {
            layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.gridx = 0;
            layoutConstraints.gridy = 1;
            //layoutConstraints.weightx = 1;
            //layoutConstraints.weighty = 1;
            MethodDetails.add((Component) ProcessComponents.get(4), layoutConstraints);
        }
        if (ProcessComponents.size() > 5) {
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 1;
            layoutConstraints.gridy = 1;
            //layoutConstraints.weightx = 1;
            //layoutConstraints.weighty = 1;
            MethodDetails.add((Component) ProcessComponents.get(5), layoutConstraints);
        }
        if (ProcessComponents.size() > 6) {
            layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.gridx = 2;
            layoutConstraints.gridy = 1;
            //layoutConstraints.weightx = 1;
            //layoutConstraints.weighty = 1;
            MethodDetails.add((Component) ProcessComponents.get(6), layoutConstraints);
        }
        if (ProcessComponents.size() > 7) {
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 3;
            layoutConstraints.gridy = 1;
            //layoutConstraints.weightx = 1;
            //layoutConstraints.weighty = 1;
            MethodDetails.add((Component) ProcessComponents.get(7), layoutConstraints);
        }

        pack();
        MethodDetails.setVisible(true);

        if (!(null == this.CurrentProcessList)) {
            this.CurrentProcessList.clear();
        }
        CurrentProcessList.add(cbm.getSelectedItem());
        CurrentProcessList.add(ccbm.getIndexOf(ccbm.getSelectedItem()));
        CurrentProcessList.addAll(ProcessComponents);

        return MethodDetails;
    }

    @Override
    protected ArrayList makeMethodComponentsArray(int position) {

        ArrayList result = new ArrayList();

        if (position == 1) {
            result.add(new JLabel("Minimum dimension of object (pixels):"));
            result.add(new JTextField("5", 5));
            //result.add(new JRadioButton("paraboloid", false));
            //result.add(new JRadioButton("stack", false));
        }
        if (position == 2) {
            result.add(new JLabel("saturation (%)"));
            result.add(new JTextField("0.1", 5));
            result.add(new JRadioButton("normalize", true));
            result.add(new JRadioButton("equalize", false));
            result.add(new JRadioButton("process_all", true));
            result.add(new JRadioButton("use", false));
        }
        if (position == 3) {
            result.add(new JLabel("Radius (pixels):"));
            result.add(new JTextField("1", 5));
        }
        return result;
    }

    @Override
    protected void blockSetupOKAction() {

        //Object definition does not hold note value
        //CurrentProcessList.set(1, "");
        makeProtocolPanel(ProcessSelectComboBox.getSelectedIndex());

        CurrentStepProtocol = CurrentProcessList;
        super.notifyMicroBlockSetupListeners(this.CurrentStepProtocol);

        this.setVisible(false);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if(e.getSource() == sliceSlider){
        currentSlice = ((JSlider)e.getSource()).getValue();
        doPreview(currentSlice);
        }else if(e.getSource() == this.previewControl){
         currentSlice = sliceSlider.getValue();
            doPreview(currentSlice);   
        }else if(e.getSource() == this.previousInclude){
          currentSlice = sliceSlider.getValue();
            doPreview(currentSlice);  
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
       if(e.getSource() == this.ChannelComboBox && this.ChannelComboBox.getSelectedIndex() > 0){
          previewControl.setSelected(false);
          previousInclude.setSelected(false);
          currentSlice = sliceSlider.getValue();
            doPreview(currentSlice);  
        }else if(e.getSource() == this.ProcessSelectComboBox && this.ProcessSelectComboBox.getSelectedIndex() > 0){
          currentSlice = sliceSlider.getValue();
          updateProtocolPanel(e);
            doPreview(currentSlice);  
        }
    }

}
