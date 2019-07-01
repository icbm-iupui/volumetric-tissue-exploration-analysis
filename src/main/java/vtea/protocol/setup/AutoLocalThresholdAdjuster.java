/*
 * Copyright (C) 2019 SciJava
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

import ij.*;
import fiji.threshold.Auto_Local_Threshold;
import ij.gui.ImageRoi;
import ij.gui.Overlay;
import ij.plugin.Duplicator;
import java.awt.*;
import java.awt.event.*;
import ij.process.*;
import ij.plugin.Thresholder;
import java.awt.image.BufferedImage;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

/** Adjusts the lower and upper threshold levels of the active image. This
    class is multi-threaded to provide a more responsive user interface. */
public class AutoLocalThresholdAdjuster  implements ItemListener, ImageListener{

    static final String[] thresholdAlgorithms = {"Micro Threshold Adjuster","Auto-Local Threshold Adjuster"};
    static final int DEFAULT = 0;
    static AutoLocalThresholdAdjuster instanceAlta;
    Auto_Local_Threshold alt = new Auto_Local_Threshold();
    static final int MICRO_THRESHOLD_ADJUSTER=0, AUTOLOCAL_THRESHOLD_ADJUSTER=1;
    static int defaultThreshAlgorithm = MICRO_THRESHOLD_ADJUSTER;
    static int currentThresholdAlgorithm = MICRO_THRESHOLD_ADJUSTER;
    static String[] autoLocalThreshMethodNames = {"Bernsen","Contrast","Mean","Median","MidGrey","Niblack","Otsu","Phansalkar","Sauvola"};
    static String defaultAutoLocalThreshMethod = autoLocalThreshMethodNames[DEFAULT];
    static String currentAutoLocalThreshMethod = autoLocalThreshMethodNames[DEFAULT];
    
    int[] defaultSettings = {15, 0, 0};
    
    JPanel panel;
    ImageJ ij;
    JComboBox autoLocalThreshAlgChoice;
    JCheckBox blackBackground, processStack;
    JTextField radius, param1, param2;
    
    ImagePlus impThreshold; 
    ImagePlus impBackup;
    JPanel gui = new JPanel();
    int previewImgId;
    boolean imageWasChanged = false;
    
    
    public AutoLocalThresholdAdjuster(ImagePlus cimp) {
       
        impThreshold = cimp;
        previewImgId = impThreshold.getID();
        impBackup = impThreshold.duplicate();

        ij = IJ.getInstance();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        gui.setLayout(gridbag);
        gui.setPreferredSize(new Dimension(280,300));
        gui.setBackground(vtea._vtea.BACKGROUND);
        
        // Checkbox1 to check for background color choice
        int y = 0;
        panel = new JPanel();
        panel.setBackground(vtea._vtea.BACKGROUND);
        blackBackground = new JCheckBox("White objects on black background");
        blackBackground.setSelected(true);
        blackBackground.addItemListener(this);
        panel.add(blackBackground);
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 1;
        c.insets = new Insets(2, 0, 0, 5);
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.fill = GridBagConstraints.NONE;
        gui.add(panel, c);
        
        // Checkbox2 to check for stack processing
//        panel = new JPanel();
//        panel.setBackground(vtea._vtea.BACKGROUND);
//        processStack = new JCheckBox("Process on a stack");
//        processStack.setSelected(false);
//        processStack.addItemListener(this);
//        panel.add(processStack);
//        c.gridx = 0;
//        c.gridy = y++;
//        c.gridwidth = 1;
//        c.insets = new Insets(3, 0, 0, 5);
//        c.anchor = GridBagConstraints.FIRST_LINE_START;
//        c.fill = GridBagConstraints.NONE;
//        gui.add(panel, c);
                
        // ComboBox for Auto-local threshold algorithm selection
        panel = new JPanel();
        panel.setBackground(vtea._vtea.BACKGROUND);
        panel.add(new JLabel("Method"));
        autoLocalThreshAlgChoice = new JComboBox();
        for (int i=0; i<autoLocalThreshMethodNames.length; i++){
            autoLocalThreshAlgChoice.addItem(autoLocalThreshMethodNames[i]);
        }
        autoLocalThreshAlgChoice.setSelectedItem(defaultAutoLocalThreshMethod);
        autoLocalThreshAlgChoice.addItemListener(this);
        panel.add(autoLocalThreshAlgChoice);
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 1;
        c.insets = new Insets(4, 0, 0, 5);
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.fill = GridBagConstraints.NONE;
        gui.add(panel, c);
        
        // Text-edit box for radius (default value = 15)
        panel = new JPanel();
        panel.setBackground(vtea._vtea.BACKGROUND);
        panel.add(new JLabel("Radius"));
        radius = new JTextField(String.valueOf(defaultSettings[0]), 5);
        panel.add(radius);
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 1;
        c.insets = new Insets(5, 0, 0, 5);
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.fill = GridBagConstraints.NONE;
        gui.add(panel, c);
        
        // Text-edit box for parameter1 (default value = 0)
        panel = new JPanel();
        panel.setBackground(vtea._vtea.BACKGROUND);
        panel.add(new JLabel("Parameter 1"));
        param1 = new JTextField(String.valueOf(defaultSettings[1]), 5);
        panel.add(param1);
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 1;
        c.insets = new Insets(6, 0, 0, 5);
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.fill = GridBagConstraints.NONE;
        gui.add(panel, c);
        
        // Text-edit box for parameter2 (default value = 0)
        panel = new JPanel();
        panel.setBackground(vtea._vtea.BACKGROUND);
        panel.add(new JLabel("Parameter 2"));
        param2 = new JTextField(String.valueOf(defaultSettings[2]), 5);
        panel.add(param2);
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 1;
        c.insets = new Insets(7, 0, 0, 15);
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.fill = GridBagConstraints.NONE;
        gui.add(panel, c);
        
        ImagePlus.addImageListener(this);
    }

    
    @Override
    public synchronized void itemStateChanged(ItemEvent e) {
        Object source = e.getSource();
 
        
        if (source==autoLocalThreshAlgChoice) {
            // Acknowledge change in selection for auto-local threshold algorithm
            currentAutoLocalThreshMethod = (String)autoLocalThreshAlgChoice.getSelectedItem();
            Thresholder.setMethod(currentAutoLocalThreshMethod);
            //autoLocalThreshAlgChoice.setSelectedItem(currentAutoLocalThreshMethod);
            //autoLocalThreshAlgChoice.addItemListener(this);

            this.UpdateImgPreviewWithAutoLocalThreshold();
        } else{

        }
    }

    /** Returns the current thresholding method ("Default", "Huang", etc). */
    public static String getMethod() {
        return currentAutoLocalThreshMethod;
    }
    
    /** Sets the thresholding method ("Default", "Huang", etc). */
    public static void setMethod(String thresholdingMethod) {
        boolean valid = false;
        for (int i=0; i<autoLocalThreshMethodNames.length; i++) {
            if (autoLocalThreshMethodNames[i].equals(thresholdingMethod)) {
                valid = true;
                break;
            }
        }
        if (valid) {
            currentAutoLocalThreshMethod = thresholdingMethod;
            if (instanceAlta!=null)
                instanceAlta.autoLocalThreshAlgChoice.setSelectedItem(currentAutoLocalThreshMethod);
        }
    }
    
    public static String getThresholdAlgorithm(){
        return thresholdAlgorithms[currentThresholdAlgorithm];
    }
    
    public static void setThresholdAlgorithm(String setThreshAlgorithm){
        if (instanceAlta!=null) synchronized (instanceAlta) {
            AutoLocalThresholdAdjuster ata = ((AutoLocalThresholdAdjuster)instanceAlta);
            if (thresholdAlgorithms[0].equals(setThreshAlgorithm))
                currentThresholdAlgorithm = 0;
            else if (thresholdAlgorithms[1].equals(setThreshAlgorithm))
                currentThresholdAlgorithm = 1;
        }
    }

    // Returns GUI panel for auto-local threshold adjuster
    public JPanel getPanel(){
        return this.gui;
    }
    
    public void UpdateImgPreviewWithAutoLocalThreshold(){
        ImagePlus tempImgPlusRef;
        ImageProcessor tempImgProcRef;
        // Check check-box for "White objects on black background"
        boolean isBlackBackground = blackBackground!=null && blackBackground.isSelected();
        
        // This method only updates the current image and does not change to a different image
        this.imageWasChanged = false;
        
        String selectedAutoLocalMethod = this.currentAutoLocalThreshMethod;
        int inputRadius = Integer.parseInt(this.radius.getText());
        double inputParam1 = Double.parseDouble(this.param1.getText());
        double inputParam2 = Double.parseDouble(this.param2.getText());
        
        // Create temporary ImagePlus reference and reset threshold using ImageProcessor
        ImagePlus originalImgPlus = this.impThreshold;
        
        //impThreshold.setOverlay(new Overlay());
        
        Duplicator dup = new Duplicator();
        
        
        tempImgPlusRef = dup.run(impThreshold);
        
        tempImgProcRef = tempImgPlusRef.getProcessor();
        tempImgProcRef.resetThreshold();
        tempImgProcRef.resetMinAndMax();
        tempImgPlusRef.repaintWindow();
        
        // Convert image to 8-bit as necessary for ImageJ Auto-Local Threshold method
        IJ.run(tempImgPlusRef, "8-bit", "");
        
        int Zposition = impThreshold.getZ();
        
        tempImgPlusRef.setZ(Zposition);
        
        // Invoke ImageJ Auto-Local Threshold algorithm as selected by user in combo-box
        alt.exec(tempImgPlusRef, selectedAutoLocalMethod, inputRadius, inputParam1, inputParam2, isBlackBackground);
        
        // Update and draw preview image
        //tempImgPlusRef.repaintWindow();
        //tempImgPlusRef.updateAndDraw();
        
        ImageProcessor ip = tempImgPlusRef.getProcessor();
        
        //overlay logic probably a new method
        
        Overlay overlay = new Overlay();

                int count = 0;

                BufferedImage placeholder = new BufferedImage(impThreshold.getWidth(), impThreshold.getHeight(), BufferedImage.TYPE_INT_ARGB);

                ImageStack gateOverlay = new ImageStack(impThreshold.getWidth(), impThreshold.getHeight());

               
                for (int i = 0; i <= impThreshold.getNSlices(); i++) {

                    BufferedImage selections = new BufferedImage(impThreshold.getWidth(), impThreshold.getHeight(), BufferedImage.TYPE_INT_ARGB);

                    Graphics2D g2 = selections.createGraphics();

                    ImageRoi ir = new ImageRoi(0, 0, placeholder);


                        try {


                            for (int x = 0; x < ip.getWidth(); x++) {
                                 for (int y = 0; y < ip.getHeight(); y++) {
                                if(ip.getPixel(x, y) > 0){
                                g2.setColor(Color.RED);
                                g2.drawRect(x, y, 1, 1);
                                }
                            }
                            }
                            ir = new ImageRoi(0, 0, selections);
                            count++;

                        } catch (NullPointerException e) {
                        }
                    
                    ir.setPosition(0, Zposition, 0);

                    //old setPosition not functional as of imageJ 1.5m
                    ir.setOpacity(0.1);
                    overlay.selectable(false);
                    overlay.add(ir);

            }
        
        //
//        Overlay over = new Overlay();
//        
//        originalImgPlus.setOverlay();
        
        // Reset flag
        
        impThreshold.setOverlay(overlay);
        
        this.imageWasChanged = true;
    }
    
    @Override
    public void imageUpdated(ImagePlus impUpdated){
        //if (processStack!=null && processStack.isSelected()){
            if ((impUpdated.getID() == this.previewImgId) && this.imageWasChanged){
                this.UpdateImgPreviewWithAutoLocalThreshold();
            }
        //}
    }
    
    @Override
    public void imageClosed(ImagePlus impUpdated){}
    
    @Override
    public void imageOpened(ImagePlus impUpdated){}
    
}
