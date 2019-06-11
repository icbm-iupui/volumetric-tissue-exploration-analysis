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
package vtea.exploration.plottools.panels;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.ZProjector;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.geom.Path2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.DefaultComboBoxModel;
import vteaexploration.MicroExplorer;
import vteaobjects.MicroObject;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Facilitates exportation of segmented nuclei images
 * @author drewmcnutt
 */
public class NucleiExportation {
    ImagePlus image;
    protected ArrayList<MicroObject> objects = new ArrayList();
    protected ArrayList<ArrayList<Number>> measurements = new ArrayList();
    int size;
    int depth;
    int[] info;
    
    NucleiExportation(ImagePlus image, ArrayList objects , ArrayList measurements){
        this.image = image;
        this.objects = objects;
        this.measurements = measurements;
    }
    
    /**
     * Outputs selected segmented nuclei as individual tiff images into a folder
     * @param path path of the gate selecting the nuclei
     * @param xAxis current measurement on the x-axis of the explorer
     * @param yAxis current measurement on the x-axis of the explorer
     */
    public void saveImages(Path2D path, int xAxis, int yAxis){
        info = image.getDimensions();

        ArrayList<MicroObject> result = getGatedObjects(path, xAxis, yAxis);

        int selectd = result.size();

        Collections.sort(result, new ZComparator()); 

        ListIterator<MicroObject> vitr = result.listIterator();
        MicroObject checker = ((MicroObject) vitr.next());
        boolean allowMorph = checker.getMorphologicalCount() != 0;
        //Not sure if this line does as intended, bigger output images if higher quality images
        int quality = checker.getRange(0) > 32? 64: 32;
        vitr.previous();
        
        int maxSize = info[0] > info[1]?info[1]:info[0];
        ExportObjImgOptions options = new ExportObjImgOptions(info[3],maxSize, quality, allowMorph);
        options.showDialog();
        ArrayList chosenOptions = options.getInformation();
        this.size = Integer.parseInt(chosenOptions.get(0).toString());
        this.depth = Integer.parseInt(chosenOptions.get(1).toString());
        boolean dapi = Boolean.getBoolean(chosenOptions.get(2).toString());
        String label = chosenOptions.get(3).toString();
        String projChoice = chosenOptions.get(4).toString();
        String voltype = (chosenOptions.get(5).toString());
        
        Class thisclass = this.getClass();
        Method getProperVolume = null;
        String methodName = null;
        switch(voltype){
            case "Mask Volume":
                methodName = "getMaskStack";
                break;
            case "Morphological Volume":
                methodName = "getMorphStack";
                break;
            case "All Values in Box":
                methodName = "getBoxStack";
                break;
        }
        try{
            getProperVolume = thisclass.getDeclaredMethod(methodName, new Class[]{MicroObject.class, Class.forName("[I")});
            getProperVolume.setAccessible(true);
        }catch(NoSuchMethodException | ClassNotFoundException e){
            Logger.getLogger(NucleiExportation.class.getName()).log(Level.SEVERE, null, e);
        }
        
        File file = options.chooseSaveLocation();
        
        ArrayList<String> filenames = new ArrayList<>();

        if (file != null) {
            int count = 0;
            while(vitr.hasNext()){
                MicroObject vol = (MicroObject) vitr.next();
                
                ImagePlus objImp = getObjectImage(vol, size, depth, getProperVolume);
                if(objImp == null)
                    continue;
                
                //ZProjection as based on the choice made in options
                if(!projChoice.equals("No Z projection"))
                    objImp = ZProjector.run(objImp,projChoice);

                File objfile = new File(file.getPath()+ File.separator + "nuclei" + count + "_ " + label + "_" + Math.round(vol.getCentroidZ()) + ".tiff");
                
                filenames.add("nuclei" + count + "_ " + label + "_" + Math.round(vol.getCentroidZ()) + ".tiff");
                
                IJ.saveAsTiff(objImp,objfile.getPath());
                count++;
            }   
            System.out.println(String.format("%d/%d nuclei could not be exported", selectd-count, selectd));
        }
        
         try {
                try {
                    PrintWriter pw = new PrintWriter(file.getPath()+ File.separator + "Label_" + label + ".csv");
                    StringBuilder sb = new StringBuilder();

                    ListIterator itr = filenames.listIterator();

                    sb.append("Filename");
                    sb.append(',');
                    sb.append("Label");
                    sb.append('\n');

                    while (itr.hasNext()) {
                        sb.append((String) itr.next());
                        if (itr.hasNext()) {
                            sb.append(",");
                            sb.append(label);
                            sb.append("\n");
                        }
                    }

                    sb.append(",");
                    sb.append(label);

                    pw.write(sb.toString());
                    pw.close();

                } catch (FileNotFoundException e) {
                }

            } catch (NullPointerException ne) {
            }
        
        
        
    }
    
    private ArrayList getGatedObjects(Path2D path, int xAxis, int yAxis){
        ArrayList<MicroObject> volumes = (ArrayList) objects;

        ArrayList<MicroObject> result = new ArrayList<>();
        int total = volumes.size();
        double xValue;
        double yValue;

        try {
            //Outputs the nuclei that are in the given gate to result
            for (int i = 0; i < total; i++) {
                ArrayList<Number> measured = measurements.get(i);

                xValue = measured.get(xAxis).floatValue();
                yValue = measured.get(yAxis).floatValue();

                if (path.contains(xValue, yValue)) {
                    result.add((MicroObject) objects.get(i));
                }
            }

        } catch (NullPointerException e) {
        }
        
        return result;
    }
    
    private ImagePlus getObjectImage(MicroObject vol, int size, int depth, Method getProperVolume){
        vol.setMaxZ();  //not already set for most MicroVolumes
        vol.setMinZ();  //same as above

        int xStart = Math.round(vol.getCentroidX()) - size/2;
        int yStart = Math.round(vol.getCentroidY()) - size/2;

        int zRange = vol.getMaxZ() - vol.getMinZ();
        int zStart = zRange > depth? (int)vol.getCentroidZ()-depth/2: vol.getMinZ()-(depth-zRange)/2;
        int[] starts = {xStart, yStart, zStart};

        //Throw out volumes that are on the edge or are improperly segmented(Zrange too big)
//                if(yStart*xStart < 0 || zStart < 0 || yStart+size > image.getHeight() || xStart+size > image.getWidth() || zRange > 5)
//                    continue;

        if(xStart < 0 || yStart < 0 || zStart < 0 || yStart+size > image.getHeight() || xStart+size > image.getWidth())
            return null;

        //ImageStack objImgStack = cropMe.crop(xStart, yStart,zStart, size, size,(depth)*image.getNChannels());
        //ImagePlus objImp = IJ.createHyperStack("nuclei", size, size, info[2], info[3], info[4], image.getBitDepth());


        ImagePlus objImp = null;
        Object[] params = new Object[2];
        params[0] = vol;
        params[1] = starts;
        try{
            objImp = (ImagePlus) getProperVolume.invoke(this, vol, starts);
            if(objImp == null)
                return null;
        }catch(IllegalAccessException | InvocationTargetException e){
            Logger.getLogger(NucleiExportation.class.getName()).log(Level.SEVERE, null, e);
        }

        
        
        return objImp;
    }
    
    private ImagePlus getMaskStack(MicroObject vol, int[] starts ){
        int xStart = starts[0];
        int yStart = starts[1];
        int zStart = starts[2];
        ImageStack stSource = image.getImageStack();
        ImagePlus objImp = IJ.createImage("nuclei", image.getBitDepth()+" black", size, size, depth);
        ImageStack st = objImp.getImageStack();
        
        int[] xPixels = vol.getPixelsX();
        int[] yPixels = vol.getPixelsY();
        int[] zPixels = vol.getPixelsZ();
        for(int i = 0; i < xPixels.length; i++){
            st.setVoxel(xPixels[i]-xStart, yPixels[i]-yStart, zPixels[i]-zStart, stSource.getVoxel(xPixels[i], yPixels[i], zPixels[i]));        
        }
        
        objImp.setStack(st);
        
        return objImp;
    }
    
    private ImagePlus getMorphStack(MicroObject vol, int[] starts){
        int xStart = starts[0];
        int yStart = starts[1];
        int zStart = starts[2];
        ImageStack stSource = image.getImageStack();
        ImagePlus objImp = IJ.createImage("nuclei", image.getBitDepth()+" black", size, size, depth);
        ImageStack st = objImp.getImageStack();
        
        int numMorph = vol.getMorphologicalCount();
        int morphindex = 0;
        if(numMorph < 1)
            return null;
        else if(numMorph > 1){
            ArrayList morphoptions = new ArrayList();
            for(int i = 0; i<numMorph; i++)
                morphoptions.add(String.valueOf(i));
            String[] morphopt = (String[]) morphoptions.toArray();
            morphindex =Integer.parseInt(JOptionPane.showInputDialog(null, "Which morphology to use?", "Morphology Choice", JOptionPane.QUESTION_MESSAGE, null, morphopt, morphopt[0]).toString());
        }
        int[] xPixels = vol.getMorphPixelsX(morphindex);
        int[] yPixels = vol.getMorphPixelsY(morphindex);
        int[] zPixels = vol.getMorphPixelsZ(morphindex);
        for(int i = 0; i < xPixels.length; i++){
            st.setVoxel(xPixels[i]-xStart, yPixels[i]-yStart, zPixels[i]-zStart, stSource.getVoxel(xPixels[i], yPixels[i], zPixels[i]));
        }
        
        objImp.setStack(st);
        
        return objImp;
    }
    
    private ImagePlus getBoxStack(MicroObject vol, int[] starts){
        int xStart = starts[0];
        int yStart = starts[1];
        int zStart = starts[2];
        
        ImageStack cropMe = image.getImageStack();
        ImageStack objImgStack = cropMe.crop(xStart, yStart,zStart, size, size,(depth)*image.getNChannels());
        ImagePlus objImp = IJ.createHyperStack("nuclei", size, size, info[2], info[3], info[4], image.getBitDepth());
        
        objImp.setStack(objImgStack);
        
        return objImp;
    }
}
class ExportObjImgOptions extends JPanel{
        JTextArea size;
        JTextArea label;
        JSpinner depth;
        JCheckBox dapi;
        JComboBox zproject;
        JComboBox pixeltype;
        
        public ExportObjImgOptions(int maxDepth, int maxSize, int recSize, boolean allowMorph){
            ArrayList<JLabel> labels = new ArrayList();
            
            JLabel labelLabel = new JLabel("Class label: ");
            labels.add(labelLabel);
            label = new JTextArea("1");
            
            JLabel sizeLabel = new JLabel("Select Size: ");
            labels.add(sizeLabel);
            size = new JTextArea(String.valueOf(recSize),1,3);
            size.addFocusListener(new java.awt.event.FocusListener(){
                @Override
                public void focusLost(java.awt.event.FocusEvent evt) {
                    if(Integer.parseInt(size.getText())>maxSize)
                        size.setText(String.valueOf(maxSize));
                }
                @Override
                public void focusGained(java.awt.event.FocusEvent evt){
                }
            });
            
            JLabel depthLabel = new JLabel("Select Depth: ");
            labels.add(depthLabel);
            depth = new JSpinner(new SpinnerNumberModel(7,1,maxDepth,1));
            
            dapi = new JCheckBox("Only use DAPI channel in output ", false);
            dapi.setEnabled(false);
            
            JLabel pixtypeLabel = new JLabel("Select volume to export: ");
            labels.add(pixtypeLabel);
            String[] pixtypeList = {"Mask Volume", "Morphological Volume", "All Values in Box"};
            DefaultComboBoxModel<String> pixtypecbm = new DefaultComboBoxModel(pixtypeList);
            if(!allowMorph)
                pixtypecbm.removeElement("Morphological Volume");
            pixeltype = new JComboBox(pixtypecbm);
            
            JLabel zprojLabel = new JLabel("Select Z-projection: ");
            labels.add(zprojLabel);
            String [] zprojList = {"No Z projection", "avg", "min", "max", "sum", "sd", "median"};
            zproject = new JComboBox(zprojList);
            
            ListIterator<JLabel> labiter = labels.listIterator();
            setupPanel(labiter); 
        }
        
        private void setupPanel(ListIterator<JLabel> labiter){
            JLabel curlabel;
            this.setLayout(new GridBagLayout());
            
            curlabel = labiter.next();
            GridBagConstraints gbc = new GridBagConstraints(0,0,1,1,0.2,1.0,
                    GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
            this.add(curlabel, gbc);
            gbc = new GridBagConstraints(1,0,1,1,1,1.0,GridBagConstraints.EAST,
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
            this.add(label, gbc);
            
            curlabel = labiter.next();
            gbc = new GridBagConstraints(0,1,1,1,0.2,1.0,GridBagConstraints.WEST,
                    GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
            this.add(curlabel,gbc);
            gbc = new GridBagConstraints(1,1,1,1,1,1.0,GridBagConstraints.EAST,
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
            this.add(size,gbc);
            
            //Added label label
            curlabel = labiter.next();
            gbc = new GridBagConstraints(0,2,1,1,0.2,1.0,GridBagConstraints.WEST,
                    GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
            this.add(curlabel,gbc);
            gbc = new GridBagConstraints(1,2,1,1,1,1.0,GridBagConstraints.EAST,
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
            this.add(depth,gbc);
            
            curlabel = labiter.next();
            gbc = new GridBagConstraints(0,3,1,1,0.2,1.0,GridBagConstraints.WEST,
                    GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
            this.add(curlabel,gbc);
            gbc = new GridBagConstraints(1,3,1,1,1,1.0,GridBagConstraints.EAST,
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
            this.add(pixeltype,gbc);
            
            curlabel = labiter.next();
            gbc = new GridBagConstraints(0,4,1,1,0.2,1.0,GridBagConstraints.WEST,
                    GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
            this.add(curlabel,gbc);
            gbc = new GridBagConstraints(1,4,1,1,1,1.0,GridBagConstraints.EAST,
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
            this.add(zproject,gbc);
            
            gbc = new GridBagConstraints(0,5,1,1,1,1.0,GridBagConstraints.EAST,
                    GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
//            this.add(dapi,gbc);
        }
        
        public int showDialog() {
            return JOptionPane.showOptionDialog(null, this, "Setup Output Images",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
            null, null);
        }
        
        public ArrayList getInformation(){
            ArrayList info = new ArrayList(3);
            info.add(Integer.parseInt(size.getText()));
            info.add(Integer.parseInt(depth.getValue().toString()));
            info.add(dapi.isEnabled());
            info.add(label.getText());
            info.add(zproject.getSelectedItem().toString());
            info.add(pixeltype.getSelectedItem().toString());
            return info;
        }
        
        public File chooseSaveLocation(){
            JFileChooser objectimagejfc = new JFileChooser(vtea._vtea.LASTDIRECTORY);
            objectimagejfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int returnVal = objectimagejfc.showSaveDialog(this);
            File file = objectimagejfc.getSelectedFile();
            
            if(returnVal == JFileChooser.APPROVE_OPTION)
                return file;
            else
                return null;
        }
    }