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
import ij.gui.Roi;
import static ij.IJ.Roi;
import ij.ImagePlus;
import ij.ImageStack;
import static ij.Undo.ROI;
import ij.gui.Roi;
import ij.io.FileInfo;
import static ij.io.Opener.ROI;
import ij.plugin.ChannelSplitter;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import ij.plugin.RGBStackMerge;
import ij.plugin.ZProjector;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import io.scif.jj2000.j2k.roi.encoder.ROI;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;

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
    
    
    public void readCSV(Path2D path, int xAxis, int yAxis) throws IOException{
        //CNN implmentation
        info = image.getDimensions();
        //ArrayList<MicroObject> result = getGatedObjects(path, xAxis, yAxis);
        ArrayList<MicroObject> result = (ArrayList) objects;
        int selectd = result.size();

        Collections.sort(result, new ZComparator()); 

        ListIterator<MicroObject> vitr = result.listIterator();
        MicroObject checker = ((MicroObject) vitr.next());
        boolean allowMorph = checker.getMorphologicalCount() != 0;
        //Not sure if this line does as intended, bigger output images if higher quality images
        int quality = checker.getRange(0) > 32? 64: 32;
        vitr.previous();
        
        int maxSize = info[0] > info[1]?info[1]:info[0];
        CNNObjImgOptions options = new CNNObjImgOptions();
        options.showCNNDialog();
        ArrayList chosenOptions = options.getInformation();
        File csv = options.chooseCSVLocation();
        NucleiLabelReader nlr = new NucleiLabelReader(image, result, measurements, csv, chosenOptions);
        nlr.run();
    }
    public void saveImages(Path2D path, int xAxis, int yAxis) throws IOException{
        info = image.getDimensions();
        Roi originalROI = image.getRoi();
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
//            case "Dapi+actin+apq1":
//                methodName = "get2ChannelStack";
//                break;
        }
        try{
            getProperVolume = thisclass.getDeclaredMethod(methodName, new Class[]{MicroObject.class, Class.forName("[I")});
            getProperVolume.setAccessible(true);
        }catch(NoSuchMethodException | ClassNotFoundException e){
            Logger.getLogger(NucleiExportation.class.getName()).log(Level.SEVERE, null, e);
        }
        
        File file = options.chooseSaveLocation();
        final int sizeL = size;
        final int depthL = depth;
        final Method gpvL = getProperVolume;
        ArrayList<String> filenames = new ArrayList<>();
        //ArrayList<ArrayList<Integer>> all_images = new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<byte[]>> all_images_array = new ArrayList<ArrayList<byte[]>>();
        ArrayList<Object[]> img_pix_array = new ArrayList<Object[]>();
        ArrayList<Double> serialID_array = new ArrayList<Double>();
        
        if (file != null) {
            int count = 0;
            int[] counter = new int[selectd];
            System.out.print("Finding all MicroObjects ... ");
            long start = System.currentTimeMillis();
            
            
            //set paralellism parameters
            String paralellism = System.getProperty("java.util.concurrent.ForkJoinPool.common.parallelism");
            System.out.println(paralellism);
            long processors = Runtime.getRuntime().availableProcessors();
            int num_processors_use = (int) (processors / 2);
            System.out.println("");
            System.out.println("Using : " + String.valueOf(num_processors_use) +" processors");
            System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(num_processors_use));
            
            IntStream.range(0, selectd).parallel().forEach((int i) -> {
                //System.out.println(i);
                MicroObject vol = result.get(i);
                double serialID_export = vol.getSerialID();
                serialID_array.add(serialID_export);
                ImagePlus objImp = getObjectImage(vol, sizeL, depthL, gpvL);
                if(objImp == null) {
                    counter[i] = 0;
                    return;
                }
                
//                int n_channels = objImp.getNChannels();
//                ArrayList<ImageStack> ims = new ArrayList<ImageStack>();
//                for (int n =0; n < n_channels; n++){
//                    objImp.setC(n);
//                    int bitDepthOut = 8;
//                    ImageStack stNu = objImp.getStack();
//                
//                    //int height = stNu.getHeight();
//                    //int width = stNu.getWidth();
//                    int slices = stNu.getSize();
//                    //System.out.println(height + " " + width + " " + slices); //32x32x7
//                    ImagePlus temp= IJ.createImage("nuclei", size, size, slices, bitDepthOut);
//                    ImageStack tempStack = temp.getImageStack();
//                
//                    for (int z = 1; z <= depth; z++){
//                        ImageProcessor ip_s = stNu.getProcessor(z);
//                        //ip_s.multiply(Math.pow(2,16) / Math.pow(2, 12));
//                        tempStack.setProcessor(ip_s.convertToByteProcessor(), z);
//                    }
//                    ims.add(tempStack);
//                }
                
                int bitDepthOut = 8;
                ImageStack stNu = objImp.getStack();
//                System.out.println("Color model");
//                System.out.println(stNu.getColorModel());
                
                //int height = stNu.getHeight();
                //int width = stNu.getWidth();
                int slices = stNu.getSize();
                //System.out.println(height + " " + width + " " + slices); //32x32x7
                ImagePlus temp= IJ.createImage("nuclei", size, size, slices, bitDepthOut);
                ImageStack tempStack = temp.getImageStack();
                
                for (int z = 1; z <= depth; z++){
                    ImageProcessor ip_s = stNu.getProcessor(z);
                    //ip_s.multiply(Math.pow(2,16) / Math.pow(2, 12));
                    tempStack.setProcessor(ip_s.convertToByteProcessor(), z);
                }

//                for(int t = 0; t < size; t++) {
//                    for(int j = 0; j < size; j++) {
//                        for(int k = 0; k < depth; k++) {
//                            //System.out.println(i + " " + j + " " + k);
//                            tempStack.setVoxel(t,j,k, 
//                                    stNu.getVoxel(t,j,k) * Math.pow(2,8) / Math.pow(2, 12));
//                        }
//                    }  
//                }
                objImp = new ImagePlus("8bit", tempStack);

//                RGBStackMerge sm = new  RGBStackMerge();
//                System.out.println(ims.size());
//                ImageStack multichannel = sm.mergeStacks(size, size, objImp.getNSlices(), ims.get(1), ims.get(2), ims.get(3), false);
//                ImagePlus objImpNu = new ImagePlus("nuclei", multichannel);
                
                      
                //ZProjection as based on the choice made in options
                if(!projChoice.equals("No Z projection")){
                    objImp = ZProjector.run(objImp,projChoice);
                }
                
//                ImageStack is = objImp.getStack();
//                slices = is.getSize();
//                for(int z = 1; z <= slices; z++){
//                    ImageProcessor ipz = is.getProcessor(z);
//                    int[][] itz = ipz.getIntArray();
//                }
                ImageStack ips = objImp.getStack();
                Object[] pix = ips.getImageArray();
                ArrayList<byte[]> oneimgbyte = new ArrayList<byte[]>();
                for (int z = 0; z < pix.length; z++) {
                    byte[] onelayer = (byte[]) pix[z];
                    if (onelayer != null){
                        oneimgbyte.add(onelayer);
                        //System.out.println("=======");
                        //System.out.println(Arrays.toString(onelayer));
                    }
                }
                
                all_images_array.add(oneimgbyte);

//                ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
//                for (int s = 0; s < objImp.getNSlices(); s++){
//                    objImp.setSlice(s+1);
//                    BufferedImage bufImg = objImp.getBufferedImage();
//                    byte[] pixels = ((DataBufferByte) bufImg.getRaster().getDataBuffer()).getData();
//                    try {
//                        outputStream.write(pixels);
//                    } catch (IOException ex) {
//                        Logger.getLogger(NucleiExportation.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//                byte [] pixels_whole = outputStream.toByteArray();
//                all_images_array.add(pixels_whole);
                //File objfile = new File(file.getPath()+ File.separator + "nuclei" + count + "_ " + label + "_" + Math.round(vol.getCentroidZ()) + ".tiff");
                //File objfile2 = new File(file.getPath()+ File.separator + "nuclei" + count + "_ " + label + "_" + Math.round(vol.getCentroidZ()) + "_byte.tiff");
                filenames.add("nuclei" + count + "_ " + label + "_" + Math.round(vol.getCentroidZ()) + ".tiff");
                
                //ImageIO.write(bufImg, "gif", objfile);
                //IJ.saveAsTiff(objImp,objfile.getPath());
                //IJ.saveAsTiff(objImpOriginal,objfile2.getPath());
                counter[i] = 1;
            });
            
            //System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", paralellism);
            
            /*
            while(vitr.hasNext()){
                MicroObject vol = (MicroObject) vitr.next();
                
                ImagePlus objImp = getObjectImage(vol, size, depth, getProperVolume);
                if(objImp == null)
                    continue;
                
//                Duplicator dup = new Duplicator();
//                ImagePlus objImpOriginal = dup.run(objImp);
                
                
                
//                objImp.show();
//                objImp.hide();
                //objImp.setDefault16bitRange(12);
                //System.out.println(objImp.getDisplayRangeMax());
                //System.out.println(objImp.);
                
//                ImagePlus objImpTo8 = objImp;
//                // scale data and use the whole range effectively
//                ImageStack st = objImp.getImageStack();
//                ImageStack st_scaled = new ImageStack(size, size, depth);
//                for(int i = 0; i < depth; i++){
//                    ImageProcessor ip = st.getProcessor(i+1);
//                    //double scale = Math.pow(2, image.getBitDepth()) / Math.pow(2, 12); //set min max goes to 8bit, original is 12bit
//                    //ip.setMinAndMax(0, Math.pow(2, 12)); //only for display, doesn't change values
//                    //ip.multiply(scale);
//                    st_scaled.setProcessor(ip, i+1);
//                }
//                
//                
//                
//                ImagePlus objImpScaled = new ImagePlus("scaled", st_scaled);
//                objImp = objImpScaled;
                //objImp = objImpTo8;
                
                
                //Manually convert to 8 bit
                int bitDepthOut = 8;
                ImageStack stNu = objImp.getStack();
                int height = stNu.getHeight();
                int width = stNu.getWidth();
                int slices = stNu.getSize();
                //System.out.println(height + " " + width + " " + slices); //32x32x7
                ImagePlus temp= IJ.createImage("nuclei", size, size, slices, bitDepthOut);
                ImageStack tempStack = temp.getImageStack();

                for(int i = 0; i < size; i++) {
                    for(int j = 0; j < size; j++) {
                        for(int k = 0; k < depth; k++) {
                            //System.out.println(i + " " + j + " " + k);
                            tempStack.setVoxel(i,j,k, 
                                    stNu.getVoxel(i,j,k) * Math.pow(2,8) / Math.pow(2, 12));
                        }
                    }  
                }
                
                objImp = new ImagePlus("8bit", tempStack);
                      
                //ZProjection as based on the choice made in options
                if(!projChoice.equals("No Z projection")){
                    objImp = ZProjector.run(objImp,projChoice);
                }
                
                // write the image as a 1d pixel array
//                ImageStack finalStack = objImp.getStack();
//                int heightf = finalStack.getHeight();
//                int widthf = finalStack.getWidth();
//                int slicesf = finalStack.getSize();
//                ArrayList<Integer> one_image = new ArrayList<>();
//                for(int k = 1; k < slicesf; k++) {
//                    for(int j = 0; j < heightf; j++) {
//                        for(int i = 0; i < widthf; i++) {
//                            //System.out.println(i + " " + j + " " + k);
//                            one_image.add( (int) finalStack.getVoxel(i,j,k));
//                        }
//                    }  
//                }
//                //System.out.println(one_image.size());
//                all_images.add(one_image);
//                ImageConverter ic = new ImageConverter(objImp);
//                ic.convertToGray8();
                

                // switch to buffered images
                
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
                for (int i = 0; i < objImp.getNSlices(); i++){
                    objImp.setSlice(i+1);
                    BufferedImage bufImg = objImp.getBufferedImage();
                    byte[] pixels = ((DataBufferByte) bufImg.getRaster().getDataBuffer()).getData();
                    outputStream.write(pixels);
                }
                byte [] pixels_whole = outputStream.toByteArray();
                System.out.println("Size of one pixel array is ");
                System.out.println(pixels_whole.length);
                all_images_array.add(pixels_whole);
                //File objfile = new File(file.getPath()+ File.separator + "nuclei" + count + "_ " + label + "_" + Math.round(vol.getCentroidZ()) + ".tiff");
                //File objfile2 = new File(file.getPath()+ File.separator + "nuclei" + count + "_ " + label + "_" + Math.round(vol.getCentroidZ()) + "_16.tiff");
                filenames.add("nuclei" + count + "_ " + label + "_" + Math.round(vol.getCentroidZ()) + ".tiff");
                
                //ImageIO.write(bufImg, "gif", objfile);
                //IJ.saveAsTiff(objImp,objfile.getPath());
                //IJ.saveAsTiff(objImpOriginal,objfile2.getPath());
                count++;
            }
            */ 
            long end = System.currentTimeMillis();
            System.out.println((end - start) / 1000f + " seconds");
            //System.out.println(String.format("%d/%d nuclei could not be exported", selectd-count, selectd));
            System.out.println(String.format("%d/%d nuclei could not be exported", selectd - IntStream.of(counter).sum(), selectd));
            image.setRoi(originalROI);
        }
        
        //Create CSV Files
         try {
                try {
//                    System.out.print("Writing  label csv ... ");
//                    long start = System.currentTimeMillis();
//                    PrintWriter pw = new PrintWriter(file.getPath()+ File.separator + "Label_" + label + ".csv");
//                    StringBuilder sb = new StringBuilder();
//
//                    ListIterator itr = filenames.listIterator();
//
//                    sb.append("Filename");
//                    sb.append(',');
//                    sb.append("Label");
//                    sb.append('\n');
//
//                    while (itr.hasNext()) {
//                        sb.append((String) itr.next());
//                        if (itr.hasNext()) {
//                            sb.append(",");
//                            sb.append(label);
//                            sb.append("\n");
//                        }
//                    }
//
//                    sb.append(",");
//                    sb.append(label);
//
//                    pw.write(sb.toString());
//                    pw.close();
//                    long end = System.currentTimeMillis();
//                    System.out.println((end - start) / 1000f + " seconds");
                    
                    // faster thing hopefully
                    LocalTime time = java.time.LocalTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmmss");
                    String timef = formatter.format(time);
                    File img_csv = new File(file.getPath()+ File.separator + "images"  + "_ " + label + "_" + timef+".csv");
                    FileWriter writer = new FileWriter(img_csv);
                    int bufSize = (int) Math.pow(1024, 2);
                    BufferedWriter bufferedWriter = new BufferedWriter(writer, bufSize);

                    System.out.print("Writing buffered (buffer size: " + bufSize + ")... ");
                    long start = System.currentTimeMillis();
//                    for (byte[] img : all_images_array) {
//                        bufferedWriter.write(String.valueOf(label));
//                        bufferedWriter.write(",");
//                        for (byte pix : img){
//                            int pixInt = pix+128; // byte is [-128 128]
//                            bufferedWriter.write(String.valueOf(pixInt));
//                            bufferedWriter.write(",");
//                        }
//                        bufferedWriter.newLine();
//                    }
                    Iterator itID = serialID_array.iterator();
                    //TODO: add serial ID to csv output file for integration of classification into VTEA
                    for (ArrayList<byte[]> al : all_images_array){
                        bufferedWriter.write(String.valueOf(label));
                        bufferedWriter.write(",");
                        bufferedWriter.write(String.valueOf(itID.next()));
                        bufferedWriter.write(",");
                        for (byte[] ba : al){
                            for (byte b : ba){
                                bufferedWriter.write(String.valueOf(b));
                                bufferedWriter.write(",");
                            }
                        }
                        bufferedWriter.newLine();
                    }
                    
                    
                    
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    long end = System.currentTimeMillis();
                    System.out.println((end - start) / 1000f + " seconds");
                    
                    
//                    PrintWriter pw_image = new PrintWriter(file.getPath()+ File.separator + "Images_" + label + ".csv");
//                    StringBuilder sb_image = new StringBuilder();
//                    ListIterator itr_image = all_images.listIterator();
//                    
//
//                    sb_image.append("Label");
//                    sb_image.append(',');
//                    sb_image.append("Values");
//                    sb_image.append('\n');
//
//                    while (itr_image.hasNext()) {
//                        ArrayList<Integer> one = (ArrayList<Integer>) itr_image.next();
//                        ListIterator itr_one = (ListIterator) one.listIterator();
//                        sb_image.append(label);
//                        sb_image.append(",");
//                    
//                        while(itr_one.hasNext()){
//                            sb_image.append((int) itr_one.next());
//                            if (itr_one.hasNext()) {
//                                sb_image.append(",");
//                            
//                            }
//                            else
//                                sb_image.append("\n");
//                        }
//                        
//                    }
//
//
//
//                    pw_image.write(sb_image.toString());
//                    pw_image.close();
                    
                    

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
                    if (image.getRoi() != null){
                        System.out.println("======ROI recognized======");
                        Roi r = image.getRoi();
                        MicroObject o = objects.get(i);
                        if (r.containsPoint(o.getCentroidX(), o.getCentroidY()))
                            result.add((MicroObject) objects.get(i));
                    }
                    else{
                        result.add((MicroObject) objects.get(i));
                    }                    
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

        if(xStart < 0 || yStart < 0 || zStart < 0 || yStart+size > image.getHeight() || xStart+size > image.getWidth() ||  zRange < 3 ) //|| zRange + zStart > image.getNFrames(){
        {
            //System.out.println(xStart + " " + yStart + " " + zStart + " " + zRange + " " + size);
            return null;
        }

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
        
        //ImageStack cropMe = image.getImageStack();
        
        ChannelSplitter cs = new ChannelSplitter();
        
       // Roi r = new Roi(xStart, yStart, size, size);
       
       image.setRoi(new Rectangle(xStart, yStart, size, size));
        
       Duplicator dup = new Duplicator();
       
       ImagePlus objImp = dup.run(image);
       
       
       ImageStack stNu = cs.getChannel(objImp, 8); // 32x32x19
       
       int[] xPixels = vol.getPixelsX();
       int[] yPixels = vol.getPixelsY();
       int[] zPixels = vol.getPixelsZ(); 
//       System.out.println("===== andre stuff ======");
//       System.out.println(xPixels.length);
//       
//       System.out.println(stNu.getSize()); //should be 19
//       System.out.println(stNu.getWidth());
//       System.out.println(stNu.getHeight());
//       
       for(int i = 0; i < 10; i++){
//           System.out.println("==========");
//           System.out.println(xPixels[i]-xStart);
//           System.out.println(yPixels[i]-yStart);
//           System.out.println(zPixels[i]+1-zStart);
           //System.out.println();
           //System.out.println(stNu.getVoxel(xPixels[i], yPixels[i], zPixels[i]));
       }
       
       ImagePlus temp= IJ.createImage("nuclei", size, size, depth, image.getBitDepth());
//       temp.show();
//       temp.hide();
       ImageStack tempStack = temp.getImageStack();
       
       for(int i = 0; i < xPixels.length; i++) {
//           System.out.println("==========");
//           System.out.println(xPixels[i]-xStart);
//           System.out.println(yPixels[i]-yStart);
//           System.out.println(zPixels[i]);
//           System.out.println(zPixels[i]-zStart-1);
           tempStack.setVoxel(xPixels[i]-xStart, yPixels[i] - yStart, zPixels[i]-zStart-1, 
                   stNu.getVoxel(xPixels[i]-xStart, yPixels[i]-yStart, zPixels[i]));
           
           // add a pixel in a random direction
//           double randx = Math.random();
//           double randy = Math.random();
//           int randJitterx = (int) Math.round(randx)*2 - 1;
//           int randJittery = (int) Math.round(randy)*2 - 1;
//           tempStack.setVoxel(xPixels[i]-xStart+randJitterx, yPixels[i] - yStart + randJittery, zPixels[i]-zStart,
//                   stNu.getVoxel(xPixels[i]-xStart+randJitterx, yPixels[i]-yStart+ randJittery, zPixels[i]-zStart));
       }
       
       ImagePlus objImpNu = new ImagePlus("nuclei", tempStack);
       
       //objImpNu.setDefault16bitRange(12);
//       System.out.println("===MASK BIT DEPTH====");
//       System.out.println(objImpNu.getBitDepth());
//       System.out.println(image.getBitDepth());
       
        
//        ImageStack stSource = image.getImageStack();
        //ImagePlus objImp = IJ.createImage("nuclei", image.getBitDepth()+" black", size, size, depth);
        //ImagePlus objImp = IJ.createImage("nuclei", size, size, depth, image.getBitDepth());
//        ImageStack st = objImp.getImageStack();
//        
//        int[] xPixels = vol.getPixelsX();
//        int[] yPixels = vol.getPixelsY();
//        int[] zPixels = vol.getPixelsZ();
//        for(int i = 0; i < xPixels.length; i++){
//            st.setVoxel(xPixels[i]-xStart, yPixels[i]-yStart, zPixels[i]-zStart, stSource.getVoxel(xPixels[i], yPixels[i], zPixels[i]));        
//        }
//        
//        objImp.setStack(st);
//        
//        return objImp;


          return objImpNu;
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
    
    private ImagePlus get2ChannelStack(MicroObject vol, int[] starts){
        int xStart = starts[0];
        int yStart = starts[1];
        int zStart = starts[2];
        int finish = depth + zStart;
        //System.out.println("zstart " + zStart);
        //System.out.println("finish " + finish);
        
        ImageStack cropMe = image.getImageStack();
        
        ChannelSplitter cs = new ChannelSplitter();
        RGBStackMerge sm = new  RGBStackMerge();
        
       // Roi r = new Roi(xStart, yStart, size, size);
       
       image.setRoi(new Rectangle(xStart, yStart, size, size));
        
       Duplicator dup = new Duplicator();
       
       ImagePlus objImp = dup.run(image);
       
       //ImageStack stNu = cs.getChannel(objImp, 8); //TODO: nuclei channel is hardcoded 
       //ImageStack stActin = cs.getChannel(objImp, 1);
       //ImageStack stapq1 = cs.getChannel(objImp, 3);
       ImagePlus[] sts = cs.split(objImp);
       ImagePlus[] sts_select = new ImagePlus[3];
       sts_select[0] = sts[0];
       sts_select[1] = sts[2];
       sts_select[2] = sts[7];
       //ImageStack multichannel = sm.mergeStacks(size, size, objImp.getNSlices(), stNu, stActin, stapq1, false);
       ImagePlus objImpNu = new ImagePlus();
       HyperStackConverter hsc = new HyperStackConverter();
       //objImpNu = objImpNu.createHyperStack("multi", sts_select.length, objImp.getNSlices(), objImp.getNFrames(), objImp.getBitDepth()); 
       objImpNu = IJ.createHyperStack("multi", size, size, sts_select.length, objImp.getNSlices(), objImp.getNFrames(), objImp.getBitDepth());
//       for (int c = 1; c <= sts_select.length; c++){
//           objImpNu.setC(c);
//           objImpNu.setStack(sts_select[c-1].getStack());
//       }
       
       
       //ImagePlus objImpNu = new ImagePlus("nuclei", multichannel);
       ImagePlus objImpNu_crop = dup.run(objImpNu, zStart, zStart+depth-1); //TODO validate this is correct
       System.out.println("get2channelstack: number of channels");
       System.out.println(objImp.getNChannels());
       System.out.println(objImpNu.getNChannels());
       System.out.println(objImpNu_crop.getNChannels());
//       System.out.println(objImpNu_crop.getDisplayRangeMax()); //4095
//       System.out.println(objImpNu_crop.getDisplayRangeMin()); //0
//       System.out.println(objImpNu_crop.getDefault16bitRange()); //0 (aka automatically set)
//       System.out.println();
//        ImageStack objImgStack = cropMe.crop(xStart, yStart,zStart, size, size,(depth)*image.getNChannels());
//        ImagePlus objImp = IJ.createHyperStack("nuclei", size, size, info[2], info[3], info[4], image.getBitDepth());
//        
//        objImp.setStack(objImgStack);
        
        //return objImp;
        return objImpNu_crop;
    }
    
    private ImagePlus getBoxStack(MicroObject vol, int[] starts){
        int xStart = starts[0];
        int yStart = starts[1];
        int zStart = starts[2];
        int finish = depth + zStart;
        //System.out.println("zstart " + zStart);
        //System.out.println("finish " + finish);
        
        ImageStack cropMe = image.getImageStack();
        
        ChannelSplitter cs = new ChannelSplitter();
        
       // Roi r = new Roi(xStart, yStart, size, size);
       
       image.setRoi(new Rectangle(xStart, yStart, size, size));
        
       Duplicator dup = new Duplicator();
       
       ImagePlus objImp = dup.run(image);
       
       ImageStack stNu = cs.getChannel(objImp, 8); //TODO: nuclei channel is hardcoded 
       ImagePlus objImpNu = new ImagePlus("nuclei", stNu);
       ImagePlus objImpNu_crop = dup.run(objImpNu, zStart, zStart+depth-1); //TODO validate this is correct
       
//       System.out.println(objImpNu_crop.getDisplayRangeMax()); //4095
//       System.out.println(objImpNu_crop.getDisplayRangeMin()); //0
//       System.out.println(objImpNu_crop.getDefault16bitRange()); //0 (aka automatically set)
//       System.out.println();
//        ImageStack objImgStack = cropMe.crop(xStart, yStart,zStart, size, size,(depth)*image.getNChannels());
//        ImagePlus objImp = IJ.createHyperStack("nuclei", size, size, info[2], info[3], info[4], image.getBitDepth());
//        
//        objImp.setStack(objImgStack);
        
        //return objImp;
        return objImpNu_crop;
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
            String[] pixtypeList = {"Mask Volume", "Morphological Volume", "All Values in Box", "Dapi+actin+apq1"};
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
        public int showCNNDialog() {
            return JOptionPane.showOptionDialog(null, this, "Setup CNN",
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
        public File chooseCSVLocation(){
            JFileChooser objectimagejfc = new JFileChooser(vtea._vtea.LASTDIRECTORY);
            //objectimagejfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal= objectimagejfc.showOpenDialog(this);
            objectimagejfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            //int returnVal = objectimagejfc.showSaveDialog(this);
            File file = objectimagejfc.getSelectedFile();
            
            if(returnVal == JFileChooser.APPROVE_OPTION)
                return file;
            else
                return null;
        }
        
        public class Timer {
            // A simple "stopwatch" class with millisecond accuracy
            private long startTime, endTime;
            public void start()   {  startTime = System.currentTimeMillis();       }
            public void stop()    {  endTime   = System.currentTimeMillis();       }
            public long getTime() {  return endTime - startTime;                   }
            }
    }

class CNNObjImgOptions extends JPanel{
        JTextArea threshold;

        
        public CNNObjImgOptions(){
            ArrayList<JLabel> labels = new ArrayList();
            
            JLabel thresholdLabel = new JLabel("Select confidence threshold: ");
            labels.add(thresholdLabel);
            double recommended = 0.95;
            threshold = new JTextArea(String.valueOf(recommended));
  
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
            this.add(threshold, gbc);
            
        }
        
        public int showCNNDialog() {
            return JOptionPane.showOptionDialog(null, this, "Setup CNN",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
            null, null);
        }
        
        public ArrayList getInformation(){
            ArrayList info = new ArrayList(1);
            info.add(Double.parseDouble(threshold.getText()));
            return info;
        }
        
        public File chooseCSVLocation(){
            JFileChooser objectimagejfc = new JFileChooser(vtea._vtea.LASTDIRECTORY);
            //objectimagejfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal= objectimagejfc.showOpenDialog(this);
            objectimagejfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            //int returnVal = objectimagejfc.showSaveDialog(this);
            File file = objectimagejfc.getSelectedFile();
            
            if(returnVal == JFileChooser.APPROVE_OPTION)
                return file;
            else
                return null;
        }
        
}