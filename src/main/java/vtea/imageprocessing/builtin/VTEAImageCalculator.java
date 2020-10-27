/*
 * Copyright (C) 2020 SciJava
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
package vtea.imageprocessing.builtin;

import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.plugin.ChannelSplitter;
import ij.plugin.ImageCalculator;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import net.imglib2.img.Img;
import org.scijava.plugin.Plugin;
import vtea.imageprocessing.AbstractImageProcessing;
import vtea.imageprocessing.ImageProcessing;

/**
 *
 * @author Seth
 */
@Plugin(type = ImageProcessing.class)
public class VTEAImageCalculator extends AbstractImageProcessing {
    
    
    static String[] operations = {"Add", "Subtract", "OR", "AND"};
    String[] channelsStrings = {"NA"};
    
    JComboBox channels = new JComboBox(channelsStrings);
    ListeningJComboBox images = new ListeningJComboBox();
    
     public VTEAImageCalculator() {
         
        VERSION = "0.1";

        AUTHOR = "Seth Winfree";

        COMMENT = "Implements basic image math from ImageJ";

        NAME = "Image Calculator";

        KEY = "ImageCalculator";
        
       
        
        images = new ListeningJComboBox();
        
        ImagePlus.addImageListener(images);
        
        channels = new JComboBox(){
            
            @Override
             public void actionPerformed(ActionEvent e) {
                ImagePlus imp = WindowManager.getImage((String)images.getSelectedItem());
                String[] channels = new String[imp.getNChannels()];
                for(int i = 0; i < imp.getNChannels(); i++){
                    channels[i] = "" + (i+1);
                }
                this.setModel(new DefaultComboBoxModel(channels));
                this.updateUI();
        }
            
            
            
        };
        
        images.addActionListener(channels);

        protocol = new ArrayList();
        
        protocol.add(new JLabel("Operation: "));
        
        protocol.add(new JComboBox(operations));
        
        protocol.add(new JLabel("Image: "));

        protocol.add(images);
        
        protocol.add(new JLabel("Channel: "));

        protocol.add(channels);
        
       //need to subclass JComboBox for automatic lookup of channel numbers...

    }

    @Override
    public boolean process(ArrayList al, ImagePlus imp) {

        JComboBox operation = (JComboBox) al.get(1);
        String operator = (String)operation.getSelectedItem();
        ListeningJComboBox images = (ListeningJComboBox) al.get(3);
        String image = (String)images.getSelectedItem();
        JComboBox channels = (JComboBox) al.get(5);
        int channel = channels.getSelectedIndex();
        
        String stack = "";
        
        if(imp.getZ() > 1){
            stack = "stack";
        }
        
        String icString = operator + " " + stack;
        System.out.println("PROFILING: Image Calculator output: " + icString);
        
        ChannelSplitter splitter = new ChannelSplitter();
    
        ImageCalculator ic = new ImageCalculator();
        ImagePlus imp1 = WindowManager.getImage(image);
        ImagePlus imp2 = new ImagePlus("Ch_" + (Integer) protocol.get(1) + "_modified", splitter.getChannel(imp1, channel + 1));
        

        imp = ic.run(icString, imp, imp2);

        return true;
    }

    @Override
    public boolean process(ArrayList al, Img img) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public boolean getCrossChannel() {
        return true;
    }
    
       

    @Override
    public Img getPreview() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getImageJMacroCommand() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String runImageJMacroCommand(String str) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void sendProgressComment() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getProgressComment() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean copyComponentParameter(String version, ArrayList dComponents, ArrayList sComponents) {

        try {
            dComponents.clear();
            
            
             protocol.add(new JLabel("Operation: "));
        
        protocol.add(new JComboBox(operations));
        
        protocol.add(new JLabel("Image: "));

        protocol.add(images);
        
        protocol.add(new JLabel("Channel: "));

        protocol.add(channels);
            
            
            

            JComboBox operations = (JComboBox) sComponents.get(1);
            JComboBox doperations = new JComboBox(operations.getModel());
            doperations.setSelectedIndex(operations.getSelectedIndex());

            dComponents.add(new JLabel("Operation: "));
            dComponents.add(doperations);
            
            ListeningJComboBox images = (ListeningJComboBox) sComponents.get(3);
            ListeningJComboBox dimages = new ListeningJComboBox(images.getModel());
            doperations.setSelectedIndex(images.getSelectedIndex());

            dComponents.add(new JLabel("Image: "));
            dComponents.add(dimages);
            
            ListeningJComboBox channels = (ListeningJComboBox) sComponents.get(5);
            ListeningJComboBox dchannels = new ListeningJComboBox(channels.getModel());
            doperations.setSelectedIndex(channels.getSelectedIndex());

            dComponents.add(new JLabel("Channel: "));
            dComponents.add(dchannels);
  
            return true;
        } catch (Exception e) {
            System.out.println("ERROR: Could not copy parameter(s) for " + NAME);
            return false;
        }
    }

    @Override
    public boolean loadComponentParameter(String version, ArrayList dComponents, ArrayList fields) {
        try {
            dComponents.clear();

            String operation = (String) fields.get(0);
            String image = (String) fields.get(1);
            String channel = (String) fields.get(2);

            dComponents.add(new JLabel("Radius (pixels):"));
            //dComponents.add((new JTextField(sRadius, 3)));

            return true;

        } catch (Exception e) {

            System.out.println("ERROR: Could not copy parameter(s) for " + NAME);

            return false;
        }
    }

    @Override
    public boolean saveComponentParameter(String version, ArrayList dComponents, ArrayList fields) {
        try {
            fields.clear();
            fields.add(((JTextField) (dComponents.get(1))).getText());
            return true;

        } catch (Exception e) {

            System.out.println("ERROR: Could not copy parameter(s) for " + NAME);

            return false;
        }
    }


     
    class ListeningJComboBox extends JComboBox implements ImageListener{
        
        ListeningJComboBox(){
            super();
            this.setModel(new DefaultComboBoxModel(populateImages()));
        }
        
        ListeningJComboBox(ComboBoxModel m){
            super(m);
        }

        @Override
        public void imageOpened(ImagePlus ip) {
            this.setModel(new DefaultComboBoxModel(populateImages()));
        }

        @Override
        public void imageClosed(ImagePlus ip) {
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void imageUpdated(ImagePlus ip) {
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
         protected String[] populateImages() {
        int[] windowList = WindowManager.getIDList();

        if (windowList == null) {
            String[] titles = new String[1];
            titles[0] = "NO OPEN IMAGES";
            //IJ.log("NO IMAGES...  :(");
            return titles;
        }

        String[] titles = new String[windowList.length];

        for (int i = 0; i < windowList.length; i++) {
            ImagePlus imp_temp = WindowManager.getImage(windowList[i]);
            if (!imp_temp.getTitle().contains("Plot") && imp_temp.getT()<2) {
                titles[i] = imp_temp != null ? imp_temp.getTitle() : "";
            }
        }
        return titles;
    }
        
    }
}
