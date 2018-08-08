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
package vtea.processor;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.scijava.plugin.Plugin;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import static vtea._vtea.FEATUREMAP;
import vtea.featureprocessing.AbstractFeatureProcessing;

/**
 * Feature Processor. Using the analysis methods and settings provided 
 * it computes the new features for the selected data.
 * @see AbstractFeatureProcessing
 * @author drewmcnutt
 */
@Plugin(type = Processor.class)
public class FeatureProcessor extends AbstractProcessor{
    /**
     * Features to be analyzed with their settings
     */
    ArrayList protocol;
    /**
     * 2D array of objects and features
     */
    double[][] features;
    /**
     * the size of one feature in comparison to total progress
     */
    static int step;
    /**
     * The newly computed features
     */
    ArrayList result;
    
    /**
     * Constructor.
     * Provides version, author, etc.
     */
    public FeatureProcessor(){
        VERSION = "0.1";
        AUTHOR = "Andrew McNutt";
        COMMENT = "Converting to SciJava plugin architecture";
        NAME = "Feature Processor";
        KEY = "FeatureProcessor";
    }
    
    /**
     * Constructor.
     * Sets up the processor to begin computing the new features
     * @param features the 2D array with each row denoting a new object and each
     * column a different feature. The first column is the unique serialID for 
     * each object.
     * @param protocol the list of all of the feature processing requested by 
     * the user with the user specified options
     */
    public FeatureProcessor(double[][] features, ArrayList protocol) {

        this.features = features;
        this.protocol = protocol;
        result = new ArrayList(protocol.size());
    }
       
    /**
     * Method.
     * Using the feature name, processes the feature with the given settings and
     * adds the results to result
     * @param protocol the settings of the specific feature that is being analyzed
     * @param features 2D array of objects and features
     */
    private void ProcessManager(ArrayList protocol, double[][] features) {

        Object iFeatp = new Object();
        
        try {
            Class<?> c;
            c = Class.forName(FEATUREMAP.get(protocol.get(2).toString()));
            Constructor<?> con;
            con = c.getConstructor();
            iFeatp = con.newInstance();
            boolean validate = (boolean)protocol.get(3);
            ((AbstractFeatureProcessing)iFeatp).process(protocol,features,validate);
        } catch (NullPointerException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException ex) {
            Logger.getLogger(FeatureProcessor.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "The feature(s) & parameters attempted have caused an error,\n reconfigure and try again", "Feature Computation Error", JOptionPane.ERROR_MESSAGE);
        }
        result.add((ArrayList)((AbstractFeatureProcessing)iFeatp).getResult());
       
    }
    /**
     * Method.
     * Process that runs outside of the Swing event thread to compute all of the
     * new features and update the progress bar.
     * @return
     * @throws Exception Prints out the exception that was thrown
     */
    @Override
    protected Void doInBackground() throws Exception{
        setProgress(0);
        ArrayList ID = new ArrayList(features.length);
        for(double[] feature : features) {
            ID.add(feature[0]);
        }
        result.add(ID);
        try{       
            firePropertyChange("comment", "", "Starting feature Analysis...");
            firePropertyChange("progress", 0, 5);
            ListIterator<Object> litr = this.protocol.listIterator();
            
            step = 100/protocol.size();
                    
            while (litr.hasNext()) {
                ProcessManager((ArrayList) litr.next(), features);
                setProgress(getProgress() + step);
            }
            outputResults();
            setProgress(100);
            firePropertyChange("comment", "", "Done.");
        }catch(Exception e){
            System.out.println(e + " in doInBackground");
            e.printStackTrace();
        }
        return null;
    }
    /**
     * Method.
     * Currently useless
     * @param al
     * @param str
     * @return 
     */
    @Override
    public int process(ArrayList al, String... str) {
        return 0;
    }
    
    /**
     * Method.
     * Currently useless
     * @return empty string
     */
    @Override
    public String getChange() {
        return "";
    }
    
    /**
     * Method.
     * Retrieves the step size
     * @return the size of one feature analysis in terms of total progress
     */
    public static int getStep(){
        return step;
    }
    
    /**
     * Method.
     * Uses a file chooser to output the newly computed features next to the 
     * unique serial ID
     */
    public void outputResults(){
        JFileChooser jf = new JFileChooser(new File("untitled.csv"));
        jf.addChoosableFileFilter(new FileNameExtensionFilter("Comma Separated Values","csv"));
            
        int returnVal = jf.showSaveDialog(null);
            
        File file = jf.getSelectedFile(); 

        if(returnVal == JFileChooser.APPROVE_OPTION) {
//            if(file.getName().length() < 5 | file.getName().length() >= 5 && (file.getName().substring(file.getName().length()-3)).equals(".csv"))
//                file.renameTo(file + ".csv");
            try{

                        PrintWriter pw = new PrintWriter(file);
                        StringBuilder sb = new StringBuilder();
                        
                        //Header
                        sb.append("Object,");
                        for(Object methods: protocol){
                            String header = ((ArrayList)methods).get(2).toString();
                            header = header.replaceFirst(" Hierarchical", "");
                            header = header.replaceFirst(" Clustering", "");
                            header = header.replaceFirst(" Reduction", "");
                            sb.append(header);
                            sb.append(',');
                        }
                        sb.append("\n");
                        
                        //Data
                        for(int i = 0; i < features.length; i++){
                            //Each object in result is a different analysis
                            for(Object data: result){
                                ArrayList al = (ArrayList)data;
                                sb.append(al.get(i));
                                sb.append(',');
                            }
                            sb.append('\n');
                        }
                        
                        pw.write(sb.toString());
                        pw.close();
                        
            }catch(Exception e){
                
            }
        }else{
            
        }
    }
            
}
