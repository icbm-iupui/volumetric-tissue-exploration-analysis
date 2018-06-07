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

import org.scijava.plugin.Plugin;
import java.util.ArrayList;
import java.util.ListIterator;

/**
 *
 * @author drewmcnutt
 */
@Plugin(type = Processor.class)
public class FeatureProcessor extends AbstractProcessor{
    
    ArrayList protocol;
    double[][] features;
    
    public FeatureProcessor(){
        VERSION = "0.1";
        AUTHOR = "Drew McNutt";
        COMMENT = "Converting to SciJava plugin architecture";
        NAME = "Feature Processor";
        KEY = "FeatureProcessor";
    }
    
    public FeatureProcessor(double[][] features, ArrayList protocol) {

        this.features = features;
        this.protocol = protocol;
    }
    
    public double[][] process() {
        
        ListIterator<Object> litr = protocol.listIterator();
        while (litr.hasNext()) {
            ProcessManager((ArrayList) litr.next(), features);
        }
        return (new double[4][]);
    }
    
    private void ProcessManager(ArrayList protocol, double[][] features) {

        //Object iImp = new Object();
        /*
        try {
            Class<?> c;
            c = Class.forName(PROCESSINGMAP.get(protocol.get(0).toString()));
            Constructor<?> con;
            try {
                con = c.getConstructor();
                iImp = con.newInstance();  
                ((AbstractImageProcessing)iImp).getVersion();

            } catch ( NullPointerException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(ImageProcessingProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (NullPointerException | ClassNotFoundException ex) {
            Logger.getLogger(ImageProcessingProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        */
       
    }
    
    @Override
    protected Void doInBackground() throws Exception{
        int progress = 0;
   
        try{       
            firePropertyChange("comment", "", "Starting image processing...");
            firePropertyChange("progress", 0, 5);
            ListIterator<Object> litr = this.protocol.listIterator();
            
            int step = 100/protocol.size();
                    
        while (litr.hasNext()) {
            setProgress(progress);
            ProcessManager((ArrayList) litr.next(), features);
            progress += step;
        }
        setProgress(100);
        firePropertyChange("comment", "", "Done.");
        }catch(Exception e){
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
}
