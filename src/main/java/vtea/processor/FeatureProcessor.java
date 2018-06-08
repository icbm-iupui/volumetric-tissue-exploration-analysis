/*
 * Copyright (C) 2018 SciJava
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
import javax.swing.filechooser.FileNameExtensionFilter;
import static vtea._vtea.FEATUREMAP;
import vtea.featureprocessing.AbstractFeatureProcessing;

/**
 *
 * @author drewmcnutt
 */
@Plugin(type = Processor.class)
public class FeatureProcessor extends AbstractProcessor{
    
    ArrayList protocol;
    double[][] features;
    static int step;
    ArrayList result;
    
    public FeatureProcessor(){
        VERSION = "0.1";
        AUTHOR = "Andrew McNutt";
        COMMENT = "Converting to SciJava plugin architecture";
        NAME = "Feature Processor";
        KEY = "FeatureProcessor";
    }
    
    public FeatureProcessor(double[][] features, ArrayList protocol) {

        this.features = features;
        this.protocol = protocol;
    }
    
    public double[][] process() {
        double[][] featdupl = features;
        ListIterator<Object> litr = protocol.listIterator();
        while (litr.hasNext()) {
            ProcessManager((ArrayList) litr.next(), featdupl);
        }
        return featdupl;
    }
    
    private void ProcessManager(ArrayList protocol, double[][] features) {

        Object iFeatp = new Object();
        
        try {
            Class<?> c;
            c = Class.forName(FEATUREMAP.get(protocol.get(0).toString()));
            Constructor<?> con;
            try {
                con = c.getConstructor();
                iFeatp = con.newInstance();  
                ((AbstractFeatureProcessing)iFeatp).getVersion();

            } catch ( NullPointerException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(FeatureProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (NullPointerException | ClassNotFoundException ex) {
            Logger.getLogger(FeatureProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        ((AbstractFeatureProcessing)iFeatp).process(protocol,features);
        result = ((AbstractFeatureProcessing)iFeatp).getResult();
       
    }
    
    @Override
    protected Void doInBackground() throws Exception{
        setProgress(0);
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
            System.out.println(e);
            throw e;
        }
        return null;
    }
    
    @Override
    public int process(ArrayList al, String... str) {
        return 0;
    }
    
    @Override
    public String getChange() {
        return "";
    }
    
    public static int getStep(){
        return step;
    }
    
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
                        sb.append("Object,Membership\n");
                        for(int i = 0; i < result.size(); i++){
                            sb.append(features[i][0]);
                            sb.append(',');
                            sb.append(result.get(i));
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
