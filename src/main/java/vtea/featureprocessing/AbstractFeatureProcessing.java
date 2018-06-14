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
package vtea.featureprocessing;

import java.awt.Component;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.imglib2.type.numeric.RealType;
import org.apache.commons.lang.ArrayUtils;

/**
 *
 * @author drewmcnutt
 * 
 * @param <T>
 * @param <A>
 * 
 */
public abstract class AbstractFeatureProcessing<T extends Component, A extends RealType> implements FeatureProcessing{
    
    protected String VERSION = "0.0";
    protected String AUTHOR = "VTEA Developer";
    protected String COMMENT = "New functionality";
    protected String NAME = "ABSTRACTFEATUREPROCESSING";
    protected String KEY = "ABSTRACTFEATUREPROCESSING";
    protected String TYPE = "";

    protected ArrayList<T> protocol= new ArrayList();
    
    protected ArrayList dataResult = new ArrayList();
    protected int progress;
    
    @Override
    public boolean setOptions(ArrayList al, double[][] proximity) {
        protocol = al;
        return true;
    }

    @Override
    public ArrayList getOptions() {
        return protocol;
    }

    @Override
    public ArrayList getResult() {
        
       return dataResult; 
        
    }

//    @Override
//    public double[] getPreview() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
    
//    @Override
//    public String getImageJMacroCommand(){
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public String runImageJMacroCommand(String str) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }

    @Override
    public void sendProgressComment() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getProgressComment() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return String.valueOf(progress);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public String getAuthor() {
        return AUTHOR;
    }

    @Override
    public String getComment() {
        return COMMENT;
    }
    
    @Override
    public String getType(){
        return TYPE;
    }
    
    public double[][] selectColumns(double[][] feature, ArrayList al){
        ArrayList delcol = new ArrayList();
        for(int i = 0; i < al.size(); i++){
            if(((boolean)al.get(i)) == false)
                delcol.add(i+1);
        }
        delcol.add(feature[0].length);
        for(Object c: delcol)
            System.out.println(c.toString());
        
        double[][] newfeature = new double[feature.length][feature[0].length-delcol.size() + 1];
        
        int count = 0;
        int j = 0;
        int curcol = 0;
        if(delcol.isEmpty())
            newfeature = feature;
        else{
            /*newfeature is filled with all of the elements of feature that were
            selected by the user*/
            for(Object col: delcol){
                int c = (int)col;
                for(int i = 0; i < feature.length; i++){
                    j = curcol;
                    for(;j < c;j++){
                        newfeature[i][j-count] = feature[i][j];
                    }
                }
                if(j==c){
                    count++;
                    j++;
                    curcol = c + 1;
                }
            }
        }
        /*Take the bstuff below out, just for debugging */
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
                        sb.append("\n");
                        
                        //Data
                        for(int i = 0; i < newfeature.length; i++){
                            for(int k = 0; k < newfeature[i].length; k++){
                                sb.append(newfeature[i][k]);
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
        return newfeature;
    }
    
    @Override
    public boolean process(ArrayList al, double[][] feature) {
        return false;
    }

//    @Override
//    public boolean process(ArrayList al, Img img) {
//        return false;
//    }

    @Override
    public boolean copyComponentParameter(int index, ArrayList dComponents, ArrayList sComponents) {
    
        try{
            
        return true;
        
        } catch(Exception e){
            
            System.out.println("ERROR: Could not copy parameter(s) for " + NAME);
            
            return false;
        }
    }
}
