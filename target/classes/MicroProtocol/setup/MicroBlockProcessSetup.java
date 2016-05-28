/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MicroProtocol.setup;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 *
 * @author vinfrais
 */
public class MicroBlockProcessSetup extends MicroBlockSetup {

    public MicroBlockProcessSetup(int step, ArrayList Channels) {

        super(step, Channels);

        //subclass specific settings
        TitleText.setEditable(false);
        String[] ProcessOptions = {"Select Method", "Background Subtraction", "Enhance Contrast", "Reduce Noise"};
        cbm = new DefaultComboBoxModel(ProcessOptions);
        cbm.setSelectedItem("Select Method");
        MethodDetails.repaint();
        jTextPane1.setText("");
        ProcessText.setText("Processing on: ");
        ProcessSelectComboBox.setModel(cbm);
        secondaryTable.setVisible(false);
        ProcessSelectComboBox.setVisible(true);

        repaint();
        pack();

    }
    

    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
       MicroBlockProcessSetup Copy = new MicroBlockProcessSetup(this.step, this.Channels);
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
      Copy.updateProtocolPanel();
      Copy.pack();
       Copy.updateProtocolPanel();
      return Copy;
    }

    @Override
    protected JPanel makeProtocolPanel(int position) {

        JPanel BuiltPanel = new JPanel();
        ArrayList ProcessComponents;

        notesPane.setVisible(true);
        tablePane.setVisible(false);

//        if (null == CurrentProcessItems[position]) {
//            ProcessComponents = CurrentProcessItems[position] = makeComponentsArray(position);
//        } else {
//            ProcessComponents = CurrentProcessItems[position];
//        }
        
        if (CurrentProcessItems.get(position) == null) {
            ProcessComponents = CurrentProcessItems.set(position, makeComponentsArray(position));
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
    protected ArrayList makeComponentsArray(int position) {

        ArrayList result = new ArrayList();

        if (position == 1) {
            result.add(new JLabel("Minimum dimension of object (pixels):"));
            result.add(new JTextField(5));
            //result.add(new JRadioButton("paraboloid", false));
            //result.add(new JRadioButton("stack", false));
        }
        if (position == 2) {
            result.add(new JLabel("saturation"));
            result.add(new JTextField(5));
            result.add(new JRadioButton("normalize", false));
            result.add(new JRadioButton("equalize", false));
            result.add(new JRadioButton("process_all", false));
            result.add(new JRadioButton("use", false));
        }
        if (position == 3) {

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

}
