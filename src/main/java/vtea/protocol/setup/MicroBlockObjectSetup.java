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
package vtea.protocol.setup;


import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.RoiListener;
import ij.plugin.ChannelSplitter;
import java.awt.Component;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static java.lang.Thread.MAX_PRIORITY;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.TableColumn;
import static vtea._vtea.PROCESSINGMAP;
import static vtea._vtea.SEGMENTATIONMAP;
import vtea.imageprocessing.AbstractImageProcessing;
import vtea.morphology.MorphologyFrame;
import vtea.objects.Segmentation.AbstractSegmentation;
import vtea.processor.ImageProcessingProcessor;
import vtea.processor.listeners.SegmentationListener;
import vtea.protocol.listeners.MorphologyFrameListener;

/**
 *
 * @author vinfrais
 */
public final class MicroBlockObjectSetup extends MicroBlockSetup implements ActionListener, SegmentationListener, MorphologyFrameListener, ImageListener, RoiListener {

    private Object[] columnTitles = {"Channel", "Method"};

    private boolean[] canEditColumns = new boolean[]{false, false};
    private TableColumn channelColumn;
    private TableColumn analysisColumn;
    
    private AbstractSegmentation Approach;
    
    private ArrayList<ArrayList> Morphology;
    
    private ArrayList CurrentProtocol;
    
    private MorphologyFrame MorphologyMenu;
    
    MicroThresholdAdjuster mta;

    ImagePlus ThresholdOriginal = new ImagePlus();
    ImagePlus ThresholdPreview = new ImagePlus();

    public MicroBlockObjectSetup(int step, ArrayList Channels, ImagePlus imp) {

        super(step, Channels);
        
        this.setTitle("Object_" + (step));
        
        MorphologyMenu = new MorphologyFrame(Channels);
        MorphologyMenu.addMorphologyListener(this);
        
   
        CurrentProtocol = new ArrayList();
        
        Morphology = new ArrayList<ArrayList>();
        
        //setup the image
        
        ThresholdOriginal = imp.duplicate();
        ThresholdPreview = getThresholdPreview();
        ThresholdPreview.addImageListener(this);

        IJ.run(ThresholdPreview, "Grays", "");
        
        //setup the method
        
        super.processComboBox = new DefaultComboBoxModel(vtea._vtea.SEGMENTATIONOPTIONS);
        
        ProcessSelectComboBox.setModel(processComboBox);
        ProcessSelectComboBox.setVisible(true);  
        
        //ProcessVariables = new String[vtea._vtea.SEGMENTATIONOPTIONS.length][10];        

        //setup thresholder       

        makeProtocolPanel((String)ProcessSelectComboBox.getSelectedItem());

 
        setBounds(new java.awt.Rectangle(500, 160, 378, 282));

        TitleText.setText("Object_" + (step));
        TitleText.setEditable(true);
        PositionText.setText("Object_" + (step));
        PositionText.setVisible(false);
        ProcessText.setText("Object building method ");

        comments.remove(notesPane);
        comments.remove(tablePane);
        
        JButton MorphologyButton = new JButton("Morphology Settings");
        MorphologyButton.setActionCommand("morphology");
        MorphologyButton.addActionListener(this);
        MorphologyButton.setToolTipText("Morphology settings for measurements");
        
        comments.add(MorphologyButton); 
        tablePane.setVisible(true);
        PreviewButton.setVisible(true);
        PreviewButton.setEnabled(true);
        
        repaint();
        pack();
    }
    
    public AbstractSegmentation getSegmentation(){
        return this.Approach;
    }
    
    public void setProcessedImage(ImagePlus imp) {
        this.ThresholdOriginal = imp;
        //ThresholdPreview = getThresholdPreview();
        //IJ.run(ThresholdPreview, "Grays", "");
    }

    public void updateNewImage() {

        Point p = new Point();
        try {
            p = ThresholdPreview.getWindow().getLocation();
        } catch (NullPointerException e) {
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            p = new Point(gd.getDisplayMode().getWidth() / 2, gd.getDisplayMode().getHeight() / 2);
        }
        ThresholdPreview.removeImageListener(this);
        
        ThresholdPreview.hide();
        ThresholdPreview.flush();
        ThresholdPreview = getThresholdPreview();
        IJ.run(ThresholdPreview, "Grays", "");
        ThresholdPreview.updateImage();
        //ThresholdPreview.show();
        //ThresholdPreview.getWindow().setLocation(p);
        ThresholdPreview.hide();
        
                
        if(this.isVisible()){
            ThresholdPreview.show();
            ThresholdPreview.getWindow().setLocation(p);
        }
        
        ThresholdPreview.addImageListener(this);

        tablePane.setVisible(true);
        MethodDetails.setVisible(false);
        MethodDetails.removeAll();
        makeProtocolPanel((String)ProcessSelectComboBox.getSelectedItem());
        MethodDetails.revalidate();
        MethodDetails.repaint();
        MethodDetails.setVisible(true);

        pack();

    }

    @Override
    protected void getSegmentationPreview() {
        CurrentStepProtocol = CurrentProcessList;
        ThresholdOriginal.setRoi(ThresholdPreview.getRoi());

        PreviewProgress.setText("Getting Preview...");
        PreviewButton.setEnabled(false);

        SegmentationPreviewer p = new SegmentationPreviewer(ThresholdOriginal, getSettings());
        
        new Thread(p).start();
        
        Thread preview = new Thread(p);
        preview.setPriority(MAX_PRIORITY);

        PreviewButton.setEnabled(true);
        PreviewProgress.setText("");
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b && !(ThresholdPreview.isVisible())) {
            ThresholdPreview.show();
            ThresholdPreview.setTitle(this.TitleText.getText());
        } else {
            ThresholdPreview.hide();
        }
    }

    @Override
    protected void updateTitles() {
        ThresholdPreview.setTitle(TitleText.getText());
    }

    @Override
    protected void updateProtocolPanel(java.awt.event.ActionEvent evt) {

        //Rewrite to handoff to a new instance of the segmentation method
        
        ThresholdPreview.removeImageListener(this);
        
        if (evt.getSource() == ChannelComboBox) {

            Point p = new Point();
            try {
                p = ThresholdPreview.getWindow().getLocation();
                ThresholdPreview.hide();
            } catch (NullPointerException e) {
                GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
                p = new Point(gd.getDisplayMode().getWidth() / 2, gd.getDisplayMode().getHeight() / 2);
            }
            
            ThresholdPreview.hide();
            ThresholdPreview = getThresholdPreview();
            IJ.run(ThresholdPreview, "Grays", "");

            ThresholdPreview.updateImage();
            ThresholdPreview.show();
            ThresholdPreview.getWindow().setLocation(p);
            
        }

        tablePane.setVisible(true);
        MethodDetails.setVisible(false);
        MethodDetails.removeAll();
        makeProtocolPanel((String) ProcessSelectComboBox.getSelectedItem());
        MethodDetails.revalidate();
        MethodDetails.repaint();
        MethodDetails.setVisible(true);

        pack();
        ThresholdPreview.addImageListener(this);
    }

    @Override
    protected JPanel makeProtocolPanel(String str) {
        
        ArrayList ProcessComponents;
        
        JPanel ProcessVisualization;

       Approach = getSegmentationApproach(str);
       Approach.setImage(ThresholdPreview);
        
       ProcessComponents = Approach.getOptions();
        
        
        
        //process components have description and swing object
        //Current Process list is handed to Arraylist for SIP processing

        CurrentProcessList = (ArrayList)ProcessComponents.clone();

        MethodDetails.setVisible(false);
        MethodDetails.removeAll();

        GridBagConstraints layoutConstraints = new GridBagConstraints();

        methodBuild.removeAll();
        

        double width = 4;
        int rows = (int)Math.ceil(ProcessComponents.size()/width);
        
        int count = 0; 
            
            for(int y = 0; y < rows; y++){      
                for(int x = 0; x < width; x++){
                    layoutConstraints.weightx = 1;
                    layoutConstraints.weighty = 1;
                    layoutConstraints.gridx = x;
                    layoutConstraints.gridy = y;
                    if(count < ProcessComponents.size()){
                    MethodDetails.add((Component) ProcessComponents.get(count), layoutConstraints);
                    count++;
                    }
                }
            }  
        ProcessVisualization = Approach.getSegmentationTool();
        methodBuild.add(ProcessVisualization);
        pack();
        MethodDetails.setVisible(true);
     
        return MethodDetails;
    }
    
    public ArrayList getCurrentProtocol(){
        return CurrentProtocol;
    }

    @Override
    protected JPanel makeProtocolPanel(int position) {

        return MethodDetails;
    }

    private ImagePlus getThresholdPreview() {
        
        ChannelSplitter cs = new ChannelSplitter();
        ImagePlus imp = new ImagePlus("Threshold " + super.TitleText.getText(),
                cs.getChannel(ThresholdOriginal,
                        this.ChannelComboBox.getSelectedIndex() + 1).duplicate()) {
        };
        imp.hide();
        return imp;
    }

    @Override
    protected ArrayList makeMethodComponentsArray(String method, String[][] str) {

        ArrayList result = new ArrayList();

        return result;
    }


    @Override
    public void blockSetupOKAction() {
        ThresholdPreview.removeImageListener(this);
        CurrentStepProtocol = CurrentProcessList;
        

        notifyMicroBlockSetupListeners(getSettings());
        
        setVisible(false);
        ThresholdPreview.addImageListener(this);
    }

    @Override
    protected void blockSetupCancelAction() {

    }

    /**
     * Details content breakdown
     *
     * [0] primary channel; method array list for primary channel segmentation
     * [1] secondary channel; method array list [2] secondary channel; method
     * array list [3] etc...
     *
     * method arraylist for primary [0] channel [1] key for method [2] field key
     * [3] field1 [4] field2 [5] etc...
     *
     * The primary is the first item in the arraylist
     *
     * add secondary volumes as addition lines with the same logic
     *
     * @param settings
     */
    // 0: minObjectSize, 1: maxObjectSize, 2: minOverlap, 3: minThreshold
    //field key 0: minObjectSize, 1: maxObjectSize, 2: minOverlap, 3: minThreshold
    public ArrayList getSettings() {
        
         /**segmentation and measurement protocol redefining.
         * 0: title text, 1: method (as String), 2: channel, 3: ArrayList of JComponents used 
         * for analysis 3: ArrayList of Arraylist for morphology determination
         */

        ArrayList result = new ArrayList();

        result.add(TitleText.getText());

        result.add((String)(ProcessSelectComboBox.getItemAt(ProcessSelectComboBox.getSelectedIndex())));
        
        result.add(ChannelComboBox.getSelectedIndex());
        
        ArrayList<JComponent> Comps = new ArrayList();
        
        Comps.addAll(CurrentStepProtocol);
     
        result.add(Comps);
        
        ArrayList<ArrayList> Measures = new ArrayList();
        
        //MORPHOLOGY GETS IN HERE
        //This is where we need to hand off the morphology settings.  this will
        //follow all the way through to the Segemntation Processor
        
        // ArrayList for morphology:  0: method(as String), 1: channel, 
        // 2: ArrayList of JComponents for method

            for (int i = 0; i < Morphology.size(); i++) {
                ArrayList Morphological = new ArrayList();
                Morphological = Morphology.get(i);
                Measures.add(Morphological);
            }
        
        result.add(Measures);
        return result;
    }
    
    @Override
    public ArrayList getProcessList(){
        return this.CurrentProcessList;
    }

    @Override
    public void roiModified(ImagePlus ip, int i) {
        if (ip.getID() == ThresholdPreview.getID()) {
            try {
                Roi r = ThresholdPreview.getRoi();
            } catch (NullPointerException e) {

            }

        }
    }
    
    @Override
    public void imageClosed(ImagePlus ip) {
        if(this.isVisible()){
        if(!ThresholdPreview.isVisible()){
            JFrame frame = new JFrame();
            frame.setBackground(vtea._vtea.BUTTONBACKGROUND);
            Object[] options = {"Yes", "No"};
            int n = JOptionPane.showOptionDialog(frame,
                    "The threshold preview has been closed.  Reopen?",
                    "Preview closed.",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (n == JOptionPane.YES_OPTION) {
                ThresholdPreview = getThresholdPreview();
                ThresholdPreview.setTitle(TitleText.toString());
                IJ.run(ThresholdPreview, "Grays", "");
                
                    MethodDetails.setVisible(false);
                    MethodDetails.removeAll();
                    makeProtocolPanel((String) ProcessSelectComboBox.getSelectedItem());
                    MethodDetails.revalidate();
                    MethodDetails.repaint();
                    MethodDetails.setVisible(true);

                    ThresholdPreview.show();
                
            } else {
            }

        }
        }
    }

    @Override
    public void imageOpened(ImagePlus ip) {
    }

    @Override
    public void imageUpdated(ImagePlus ip) {

     }
    
    private AbstractSegmentation getSegmentationApproach(String method){
        Object iImp = new Object();

           try {
               Class<?> c;
               c = Class.forName(SEGMENTATIONMAP.get(method));
               Constructor<?> con;
               try {
                   con = c.getConstructor();
                   iImp = con.newInstance();
                   

                   return (AbstractSegmentation)iImp;

               } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                   Logger.getLogger(ImageProcessingProcessor.class.getName()).log(Level.SEVERE, null, ex);
               }

           } catch (ClassNotFoundException ex) {
               Logger.getLogger(ImageProcessingProcessor.class.getName()).log(Level.SEVERE, null, ex);
           }
           
           return new AbstractSegmentation();
        
    }


    @Override
    public void updateGui(String str, Double dbl) {
        tablePane.setVisible(true);
        MethodDetails.setVisible(false);
        MethodDetails.revalidate();
        MethodDetails.repaint();
        MethodDetails.setVisible(true);
        pack();
     }

    @Override
    public void actionPerformed(ActionEvent e) {       
        if(e.getActionCommand().equals("morphology")){ 
            this.MorphologyMenu.setVisible(true);
        }      
    }

    @Override
    public void addMorphology(ArrayList<ArrayList> morphologies) {
       Morphology.clear();
       ListIterator<ArrayList> itr = morphologies.listIterator();
       while(itr.hasNext()){
           ArrayList morphology = itr.next(); 
           Morphology.add(morphology);
       }
    }

    private class channelNumber extends javax.swing.JComboBox {

        public channelNumber() {
            this.setModel(new javax.swing.DefaultComboBoxModel(Channels.toArray()));
        }
    ;

    };
    private class analysisType extends JComboBox {

        public analysisType() {
            this.setModel(new javax.swing.DefaultComboBoxModel(vtea._vtea.MORPHOLOGICALOPTIONS));
        }
    ;
};

}
