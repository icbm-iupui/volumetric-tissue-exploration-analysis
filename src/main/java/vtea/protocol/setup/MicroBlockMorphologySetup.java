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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import static vtea._vtea.MORPHOLOGICALMAP;
import vtea.objects.morphology.AbstractMorphology;
import vtea.processor.ImageProcessingProcessor;

/**
 *
 * @author vinfrais
 */
public final class MicroBlockMorphologySetup extends MicroBlockSetup {

    public static String getMethod(int i) {
        return null;
    }

    private AbstractMorphology Approach;

    public MicroBlockMorphologySetup(int step, ArrayList Channels) {

        super(step, Channels);
        
        setTitle("Add Morphology"); // NOI18N

        ChannelSelection.setText("Morphology on: ");
        ChannelComboBox.addItem("All");

        //setup the method
        super.processComboBox = new DefaultComboBoxModel(vtea._vtea.MORPHOLOGICALOPTIONS);

        ProcessSelectComboBox.setModel(processComboBox);
        ProcessSelectComboBox.setVisible(true);

        ProcessVariables = new String[vtea._vtea.MORPHOLOGICALOPTIONS.length][10];

        //setup thresholder       
        makeProtocolPanel((String) ProcessSelectComboBox.getSelectedItem());

        setBounds(new java.awt.Rectangle(500, 160, 378, 282));

        TitleText.setText("Morphology_" + (step));
        TitleText.setEditable(true);
        PositionText.setText("Morphology_" + (step));
        PositionText.setVisible(false);
        ProcessText.setText("Morphology method ");

        comments.remove(notesPane);

        repaint();
        pack();
    }

    @Override
    protected void updateTitles() {

    }

    @Override
    protected void updateProtocolPanel(java.awt.event.ActionEvent evt) {

        //Rewrite to handoff to a new instance of the segmentation method
        if (evt.getSource() == ChannelComboBox) {

        }

        MethodDetails.setVisible(false);
        MethodDetails.removeAll();
        makeProtocolPanel((String) ProcessSelectComboBox.getSelectedItem());
        MethodDetails.revalidate();
        MethodDetails.repaint();
        MethodDetails.setVisible(true);

        pack();

    }

    @Override
    protected JPanel makeProtocolPanel(String str) {

        ArrayList ProcessComponents;

        JPanel ProcessVisualization;

        Approach = getMorphologicalApproach(str);

        ProcessVisualization = Approach.getMorphologicalTool();
        ProcessComponents = Approach.getOptions();

        CurrentProcessList = ProcessComponents;

        MethodDetails.setVisible(false);
        MethodDetails.removeAll();

        GridBagConstraints layoutConstraints = new GridBagConstraints();

        methodBuild.removeAll();
        methodBuild.add(ProcessVisualization);

        double width = 4;
        int rows = (int) Math.ceil(ProcessComponents.size() / width);

        int count = 0;

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < width; x++) {
                layoutConstraints.weightx = 1;
                layoutConstraints.weighty = 1;
                layoutConstraints.gridx = x;
                layoutConstraints.gridy = y;
                if (count < ProcessComponents.size()) {
                    MethodDetails.add((Component) ProcessComponents.get(count), layoutConstraints);
                    count++;
                }
            }
        }

        pack();
        MethodDetails.setVisible(true);

        return MethodDetails;
    }

    @Override
    protected ArrayList makeMethodComponentsArray(String method, String[][] str) {
//        Object iImp = new Object();
//
//        try {
//            Class<?> c;
//            c = Class.forName(SEGMENTATIONMAP.get(method));
//            Constructor<?> con;
//            try {
//                con = c.getConstructor();
//                iImp = con.newInstance();
//                ((AbstractSegmentation) iImp).setImage(ThresholdPreview);
//                return ((AbstractSegmentation) iImp).getOptions();
//                
//            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//                Logger.getLogger(ImageProcessingProcessor.class.getName()).log(Level.SEVERE, null, ex);
//            }
//
//        } catch (ClassNotFoundException ex) {
//            Logger.getLogger(ImageProcessingProcessor.class.getName()).log(Level.SEVERE, null, ex);
//        }

        ArrayList result = new ArrayList();
        return result;
    }

    @Override
    protected void blockSetupOKAction() {

        CurrentStepProtocol = CurrentProcessList;

        //updateProcessVariables();
        notifyMicroBlockSetupListeners(getSettings());

        setVisible(false);

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
    private ArrayList<ArrayList> getSettings() {

        /**
         * segmentation and measurement protocol redefining. -: title text, 0:
         * method (as String), 1: channel, 2: ArrayList of JComponents used for
         * analysis 3: ArrayList of Arraylist for morphology determination
         */
        ArrayList<ArrayList> resultList = new ArrayList<ArrayList>();

        ArrayList result = new ArrayList();

        //result.add(TitleText.getText());
        ArrayList<JComponent> Comps = new ArrayList();

        if ((ChannelComboBox.getSelectedItem().toString()).equals("All")) {
            for (int i = 0; i < this.Channels.size(); i++) {
                result = new ArrayList();
                result.add((String) (ProcessSelectComboBox.getItemAt(ProcessSelectComboBox.getSelectedIndex())));
                result.add(i + 1);
                Comps = new ArrayList();
                Comps.addAll(CurrentStepProtocol);
                result.add(Comps);
                resultList.add(result);
            }
        } else {
            result = new ArrayList();
            result.add((String) (ProcessSelectComboBox.getItemAt(ProcessSelectComboBox.getSelectedIndex())));
            result.add(ChannelComboBox.getSelectedIndex() + 1);
            Comps = new ArrayList();
            Comps.addAll(CurrentStepProtocol);
            result.add(Comps);
            resultList.add(result);
        }
        return resultList;
    }

    private AbstractMorphology getMorphologicalApproach(String method) {
        Object iImp = new Object();

        try {
            Class<?> c;
            c = Class.forName(MORPHOLOGICALMAP.get(method));
            Constructor<?> con;
            try {
                con = c.getConstructor();
                iImp = con.newInstance();

                //((AbstractSegmentation) iImp).setImage(ThresholdPreview);
                return (AbstractMorphology) iImp;

            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(ImageProcessingProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ImageProcessingProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new AbstractMorphology();

    }

//    private class channelNumber extends javax.swing.JComboBox {
//
//        public channelNumber() {
//            this.setModel(new javax.swing.DefaultComboBoxModel(Channels.toArray()));
//        }
//    ;
//
//    };
//    private class analysisType extends JComboBox {
//
//        public analysisType() {
//            this.setModel(new javax.swing.DefaultComboBoxModel(vtea._vtea.MORPHOLOGICALOPTIONS));
//        }
//    ;
//    };
}
