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
    int channelOfInterest;
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
        long start0 = System.currentTimeMillis();
        ArrayList<MicroObject> result = getGatedObjects(path, xAxis, yAxis);
        long end0 = System.currentTimeMillis();
        //System.out.println("Time to gather objects: " +(end0 - start0) / 1000f + " seconds");
        
        int selectd = result.size();

        Collections.sort(result, new ZComparator()); 

        ListIterator<MicroObject> vitr = result.listIterator();
        MicroObject checker = ((MicroObject) vitr.next());
        boolean allowMorph = checker.getMorphologicalCount() != 0;
        //Not sure if this line does as intended, bigger output images if higher quality images
        int quality = 32; //checker.getRange(0) > 32? 64: 32;
        vitr.previous();
        
        
        
        int maxSize = info[0] > info[1]?info[1]:info[0];
        ExportObjImgOptions options = new ExportObjImgOptions(info[3],maxSize, quality, allowMorph, image.getNChannels());
        options.showDialog();
        ArrayList chosenOptions = options.getInformation();
        this.channelOfInterest = Integer.parseInt(chosenOptions.get(6).toString());
        this.size = Integer.parseInt(chosenOptions.get(0).toString());
        this.depth = Integer.parseInt(chosenOptions.get(1).toString());
        boolean dapi = Boolean.getBoolean(chosenOptions.get(2).toString());
        String label = chosenOptions.get(3).toString();
        String projChoice = chosenOptions.get(4).toString();
        String voltype = (chosenOptions.get(5).toString());
        //String bitdepth = (chosenOptions.get(7).toString());
        
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
        
        
        if (file != null) {
            int count = 0;
            
            System.out.print("Finding all MicroObjects ... ");
            
            
            
            //set paralellism parameters
            String paralellism = System.getProperty("java.util.concurrent.ForkJoinPool.common.parallelism");
            System.out.println(paralellism);
            long processors = Runtime.getRuntime().availableProcessors();
            int num_processors_use = (int) (processors / 2);
            System.out.println("");
            System.out.println("Using : " + String.valueOf(num_processors_use) +" processors");
            System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(num_processors_use));
            
            ArrayList<ArrayList<MicroObject>> res_split = new ArrayList<ArrayList<MicroObject>>();
            //int batch_size = 500;
//            if (selectd > 500) {
//                System.out.println("You are exporting many volumes, splitting jobs");
//                for (int b = 0; b < result.size(); b += batch_size){
//                    ArrayList<MicroObject> batch = new ArrayList<MicroObject>();
//                    for (int it = 0; it < batch_size; it++){
//                        MicroObject to_add = null;
//                        if (it+b < result.size()){to_add = result.get(it + b);}
//                        if (to_add != null) {batch.add(to_add);}
//                    }
//                    res_split.add(batch);
//                }
//                System.out.println("Made " + res_split.size() + " batches");
//            }
//            else {
                res_split.add(result);
//            }
            // start processing of image
            for (int b = 0; b < res_split.size(); b++){
                ArrayList<String> filenames = new ArrayList<>();
                //ArrayList<ArrayList<Integer>> all_images = new ArrayList<ArrayList<Integer>>();
                ArrayList<ArrayList<byte[]>> all_images_array = new ArrayList<ArrayList<byte[]>>();
                ArrayList<Object[]> img_pix_array = new ArrayList<Object[]>();
                ArrayList<Double> serialID_array = new ArrayList<Double>();
               // System.out.println(String.format("======== Batch: %d/%d ======", b+1, res_split.size()));
                long start = System.currentTimeMillis();
                ArrayList<MicroObject> result_batch = res_split.get(b);
                int items_in_batch = result_batch.size();
                //System.out.println("Items in batch: " + items_in_batch);
                int[] counter = new int[items_in_batch];
                
                IntStream.range(0, items_in_batch).parallel().forEach((int i) -> {
                //IntStream.range(0, selectd).parallel().forEach((int i) -> {
                    //System.out.println(i);
                    MicroObject vol = result_batch.get(i);
                    //MicroObject vol = result.get(i);
                    double serialID_export = vol.getSerialID();
                    
                    ImagePlus objImp = getObjectImage(vol, sizeL, depthL, gpvL);
                    if(objImp == null) {
                        counter[i] = 0;
                        return; //stop if unable to return
                    }


//                    int bitDepthOut = 8;
//                    ImageStack stNu = objImp.getStack();
//                    int slices = stNu.getSize();
//                    //System.out.println(height + " " + width + " " + slices); //32x32x7
//                    ImagePlus temp= IJ.createImage("nuclei", size, size, slices, bitDepthOut);
//                    ImageStack tempStack = temp.getImageStack();
                    
                    objImp.setDisplayRange(0, 4095);
                    IJ.run(objImp, "8-bit", "");

//                    for (int z = 1; z <= depth; z++){
//                        //ImageProcessor ip_s = stNu.getProcessor(z);
//                        //ip_s.multiply(Math.pow(2,16) / Math.pow(2, 12));
//                        //tempStack.setProcessor(ip_s.convertToByteProcessor(), z);
//                        tempStack.getProcessor(z).setMinAndMax(0, 4095);
//                        tempStack.getProcessor(z).convertToByteProcessor(true);
//                    }
                    //objImp = new ImagePlus("8bit", tempStack);

                    //ZProjection as based on the choice made in options
                    if(!projChoice.equals("No Z projection")){
                        objImp = ZProjector.run(objImp,projChoice);
                    }
                    
                    File objfile = new File(file.getPath()+ File.separator + "nuclei" + count + "_ " + label + "_" + Math.round(vol.getCentroidZ()) + ".tiff");
                    IJ.saveAsTiff(objImp,objfile.getPath());


                    ImageStack ips = objImp.getStack();
                    Object[] pix = ips.getImageArray();
                    ArrayList<byte[]> oneimgbyte = new ArrayList<byte[]>();
                    for (int z = 0; z < pix.length; z++) {
                        byte[] onelayer = (byte[]) pix[z];
                        if (onelayer != null){
                            oneimgbyte.add(onelayer);
                        }
                    }

                    all_images_array.add(oneimgbyte);
                    serialID_array.add(serialID_export);

                    //File objfile2 = new File(file.getPath()+ File.separator + "nuclei" + count + "_ " + label + "_" + Math.round(vol.getCentroidZ()) + "_byte.tiff");
                    //filenames.add("nuclei" + count + "_ " + label + "_" + Math.round(vol.getCentroidZ()) + ".tiff");

                    //ImageIO.write(bufImg, "gif", objfile);
                    
                    //IJ.saveAsTiff(objImpOriginal,objfile2.getPath());
                    counter[i] = 1;
                });
                //System.out.println("Number of images in batch: " + all_images_array.size());
                long end = System.currentTimeMillis();
                System.out.println("Time to process nuclei: " +(end - start) / 1000f + " seconds");
                //System.out.println(String.format("%d/%d nuclei could not be exported", selectd-count, selectd));
                System.out.println(String.format("%d/%d nuclei could not be exported", items_in_batch - IntStream.of(counter).sum(), items_in_batch));
                image.setRoi(originalROI);
        
        
        //Create CSV Files
         try {
                try {                  
                    
                    // faster thing hopefully
                    LocalTime time = java.time.LocalTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmmss");
                    String timef = formatter.format(time);
                    File img_csv = new File(file.getPath()+ File.separator + "images"  + "_ " + label + "_" + timef+"_"+ b +".csv");
                    FileWriter writer = new FileWriter(img_csv);
                    int bufSize = (int) Math.pow(1024, 2);
                    BufferedWriter bufferedWriter = new BufferedWriter(writer, bufSize);

                    System.out.print("Writing buffered (buffer size: " + bufSize + ")... ");
                    long start1 = System.currentTimeMillis();
//                    
                    Iterator itID = serialID_array.iterator();
                    //TODO: add serial ID to csv output file for integration of classification into VTEA
                    for (ArrayList<byte[]> al : all_images_array){
                        bufferedWriter.write(String.valueOf(label));
                        bufferedWriter.write(",");
                        bufferedWriter.write(String.valueOf(itID.next()));
                        bufferedWriter.write(",");
                        for (byte[] ba : al){
                            for (byte by : ba){
                                bufferedWriter.write(String.valueOf(by));
                                bufferedWriter.write(",");
                            }
                        }
                        bufferedWriter.newLine();
                    }
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    long end1 = System.currentTimeMillis();
                    System.out.println((end1 - start1) / 1000f + " seconds");
                } catch (FileNotFoundException e) {
                }

            } catch (NullPointerException ne) {
            }
        
        
        }}
    }
    
    public void processVolume(){
        
    }
    public ArrayList splitImage(){
                // split for large images
            int xyDim = 512;
            //ImagePlus stackResult = image.duplicate();
            int width = image.getWidth();
            int height = image.getHeight();
            ArrayList xPositions = new ArrayList();
            ArrayList yPositions = new ArrayList();
            ArrayList xStart = new ArrayList();
            ArrayList yStart = new ArrayList();
            for (int x = 0; x < width; x = x + xyDim) {
                xPositions.add(x);
            }
            for (int y = 0; y < height; y = y + xyDim) {
                yPositions.add(y);
            }
            
            ArrayList<ImagePlus> grid = new ArrayList<ImagePlus>();
            ListIterator itr = xPositions.listIterator();

            while (itr.hasNext()) {

                int x = (int) itr.next();

                for (int p = 0; p < yPositions.size(); p++) {

                    int y = (int) yPositions.get(p);

                    Duplicator dup1 = new Duplicator();
                    image.setRoi(new Rectangle(x, y, xyDim, xyDim));
                    grid.add(dup1.run(image));
                    image.deleteRoi();

                    xStart.add(x);
                    yStart.add(y);
                }
            }
            int xRemain = 0;
            int yRemain = 0;
            //get y remain for all x
            if (((int) yPositions.get(yPositions.size() - 1) + xyDim) < height) {

                int y = (int) xPositions.get(xPositions.size() - 1) + xyDim;
                yRemain = height - y;

                for (int p = 0; p < yPositions.size(); p++) {

                    int x = (int) xPositions.get(p);
                     Duplicator dup3 = new Duplicator();
                    image.setRoi(new Rectangle(x, y, xyDim, yRemain));
                    grid.add(dup3.run(image));
                    image.deleteRoi();

                    xStart.add(x);
                    yStart.add(y);
                }
            }

            // get y and x remain.
            if (xRemain > 0 && yRemain > 0) {

                int xLast = ((int) xPositions.get(xPositions.size() - 1) + xyDim);
                int yLast = ((int) yPositions.get(yPositions.size() - 1) + xyDim);
                 Duplicator dup4 = new Duplicator();
                image.setRoi(new Rectangle(xLast, yLast, xRemain, yRemain));
                grid.add(dup4.run(image));
                image.deleteRoi();

                xStart.add(xLast);
                yStart.add(yLast);
            }

            System.out.println("Made " + grid.size() + " total sub-images.");
            return grid;
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
                        //System.out.println("======ROI recognized======");
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
       
       
       
       
       
       
       ImageStack stNu = cs.getChannel(objImp, channelOfInterest); // 32x32x19
       
       int[] xPixels = vol.getPixelsX();
       int[] yPixels = vol.getPixelsY();
       int[] zPixels = vol.getPixelsZ(); 
       

       
      ImagePlus temp= IJ.createImage("nuclei", size, size, depth, objImp.getBitDepth());
      
      //temp.show();

       
       ImageStack tempStack = temp.getImageStack();
       
       for(int i = 0; i < xPixels.length; i++) {
                    tempStack.setVoxel(xPixels[i]-xStart, yPixels[i] - yStart, zPixels[i]-zStart-1, 
                    stNu.getVoxel(xPixels[i]-xStart, yPixels[i]-yStart, zPixels[i]));
       }

       
       for(int j = 1; j <= tempStack.size(); j++){
           tempStack.getProcessor(j).convertToByte(false);
       }
       
       

         return temp;
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
        
        //ImageStack cropMe = image.getImageStack();
        
        ChannelSplitter cs = new ChannelSplitter();
        
       // Roi r = new Roi(xStart, yStart, size, size);
       
       image.setRoi(new Rectangle(xStart, yStart, size, size));
        
       Duplicator dup = new Duplicator();
       
       ImagePlus objImp = dup.run(image);
       
       ImageStack stNu = cs.getChannel(objImp, this.channelOfInterest); //TODO: nuclei channel is hardcoded 
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
        JComboBox channelchoice;
        JSpinner depth;
        JCheckBox dapi;
        JComboBox zproject;
        JComboBox pixeltype;
        
        public ExportObjImgOptions(int maxDepth, int maxSize, int recSize, boolean allowMorph, int nChannels){
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
            
            JLabel channel_label = new JLabel("Select nuclei channel: ");
            labels.add(channel_label);
            String[] channels = new String[nChannels];
            
            for(int i = 1; i <= nChannels; i++){
                
               channels[i-1] = "Channel " + i;
            }
              channelchoice = new JComboBox(channels);
            
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
            this.add(channelchoice, gbc);
            
            curlabel = labiter.next();
            gbc = new GridBagConstraints(0,1,1,1,0.2,1.0,
                    GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
            this.add(curlabel, gbc);
            gbc = new GridBagConstraints(1,1,1,1,1,1.0,GridBagConstraints.EAST,
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
            this.add(label, gbc);
            
            curlabel = labiter.next();
            gbc = new GridBagConstraints(0,2,1,1,0.2,1.0,GridBagConstraints.WEST,
                    GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
            this.add(curlabel,gbc);
            gbc = new GridBagConstraints(1,2,1,1,1,1.0,GridBagConstraints.EAST,
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
            this.add(size,gbc);
            
            //Added label label
            curlabel = labiter.next();
            gbc = new GridBagConstraints(0,3,1,1,0.2,1.0,GridBagConstraints.WEST,
                    GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
            this.add(curlabel,gbc);
            gbc = new GridBagConstraints(1,3,1,1,1,1.0,GridBagConstraints.EAST,
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
            this.add(depth,gbc);
            
            curlabel = labiter.next();
            gbc = new GridBagConstraints(0,4,1,1,0.2,1.0,GridBagConstraints.WEST,
                    GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
            this.add(curlabel,gbc);
            gbc = new GridBagConstraints(1,4,1,1,1,1.0,GridBagConstraints.EAST,
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
            this.add(pixeltype,gbc);
            
            curlabel = labiter.next();
            gbc = new GridBagConstraints(0,5,1,1,0.2,1.0,GridBagConstraints.WEST,
                    GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0);
            this.add(curlabel,gbc);
            gbc = new GridBagConstraints(1,5,1,1,1,1.0,GridBagConstraints.EAST,
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);
            this.add(zproject,gbc);
            
            gbc = new GridBagConstraints(0,6,1,1,1,1.0,GridBagConstraints.EAST,
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
            info.add(channelchoice.getSelectedIndex()+1);
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