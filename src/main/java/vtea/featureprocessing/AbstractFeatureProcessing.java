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
package vtea.featureprocessing;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import static java.lang.Math.sqrt;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import net.imglib2.type.numeric.RealType;

/**
 * Abstract of all Feature Processing classes. All features must extend this.
 * All Feature Processing Classes must have a static method as follows: public
 * static String getBlockComment(ArrayList comComponents) this method takes in
 * all of the parameter components provided in the Setup box and returns a
 * string for the comment text of the block GUI.
 *
 * @author drewmcnutt
 *
 * @param <T>
 * @param <A>
 *
 */
public abstract class AbstractFeatureProcessing<T extends Component, A extends RealType> implements FeatureProcessing {

    protected String VERSION = "0.0";
    protected String AUTHOR = "VTEA Developer";
    protected String COMMENT = "New functionality";
    protected String NAME = "ABSTRACTFEATUREPROCESSING";
    protected String KEY = "ABSTRACTFEATUREPROCESSING";
    protected String TYPE = "";

    protected ArrayList<T> protocol = new ArrayList();

    protected ArrayList dataResult = new ArrayList();
    protected int progress;

    /**
     * Sets parameters for the feature method.
     *
     * @param al List of parameters
     * @return true
     */
    @Override
    public boolean setOptions(ArrayList al) {
        protocol = al;
        return true;
    }

    /**
     * Provides the parameters for the feature method.
     *
     * @return the parameters
     */
    @Override
    public ArrayList getOptions() {
        return protocol;
    }

    /**
     * Provides the newly calculated feature.
     *
     * @return value of the new feature for each object
     */
    @Override
    public ArrayList getResult() {

        return dataResult;

    }

    @Override
    public void sendProgressComment() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getProgressComment() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return String.valueOf(progress);
    }

    /**
     * Provides name of the feature.
     *
     * @return the name of the feature
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Provides key of the feature.
     *
     * @return the key of the feature
     */
    @Override
    public String getKey() {
        return KEY;
    }

    /**
     * The version of the feature.
     *
     * @return the version of the feature
     */
    @Override
    public String getVersion() {
        return VERSION;
    }

    /**
     * The author of the feature.
     *
     * @return the author of the feature
     */
    @Override
    public String getAuthor() {
        return AUTHOR;
    }

    /**
     * Provides any comments about the feature.
     *
     * @return the comment about the feature
     */
    @Override
    public String getComment() {
        return COMMENT;
    }

    /**
     * Provides the feature's type.
     *
     * @return the feature's type(Cluster,Dimensionality Reduction, etc.)
     */
    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getDataDescription(ArrayList param) {
        return KEY;
    }

    /**
     * Makes a two dimensional integer array from a file. Rows are delimited by
     * newlines and columns are delimited by ','
     *
     * @param location the location of the file(just the file name if it is in
     * the current path)
     * @return 2D integer array
     */
    public double[][] getDoubleList(String location) {
        ArrayList temp = new ArrayList();
        double[][] table;
        String line = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(location));

            while ((line = br.readLine()) != null) {
                String vals[] = line.split(",");
                ArrayList row = new ArrayList(vals.length);
                for (String val : vals) {
                    if (!val.equals("null")) {
                        row.add(Double.parseDouble(val));
                    } else {
                        row.add(0.0);
                    }
                }

                temp.add(row);
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }

        table = new double[temp.size()][((ArrayList) temp.get(0)).size()];
        for (int i = 0; i < temp.size(); i++) {
            for (int j = 0; j < ((ArrayList) temp.get(0)).size(); j++) {
                table[i][j] = (double) ((ArrayList) temp.get(i)).get(j);
            }
        }

        return table;
    }

    /**
     * Makes a one dimensional integer array from a file. Values are delimited
     * by newlines
     *
     * @param location the location of the file(just the file name if it is in
     * the current path)
     * @return 1D integer array
     */
    public int[] getIntList(String location) {
        ArrayList temp = new ArrayList();
        int[] table;
        String line;

        try {
            BufferedReader br = new BufferedReader(new FileReader(location));

            while ((line = br.readLine()) != null) {
                if (!line.equals("null")) {
                    temp.add(Integer.parseInt(line));
                } else {
                    temp.add(0);
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        table = new int[temp.size()];
        for (int i = 0; i < temp.size(); i++) {
            table[i] = (int) (temp.get(i));
        }

        return table;
    }

    /**
     * Deletes columns from feature array. Deletes the ObjectID column as well
     * as selected columns
     *
     * @param feature the original 2D feature array with columns denoting
     * features and rows denoting objects
     * @param keep corresponds to the columns of the array(true means keep and
     * false means delete)
     * @return the new 2D feature array with the selected columns deleted.
     */
    public double[][] selectColumns(double[][] feature, ArrayList keep) {
        ArrayList delcol = new ArrayList();
        delcol.add(0);
        for (int i = 0; i < keep.size(); i++) {
            if (((boolean) keep.get(i)) == false) {
                delcol.add(i + 1);
            }
        }

        double[][] newfeature = new double[feature.length][feature[0].length - delcol.size()];

        int count = 0;
        int j = 0;
        int curcol = 0;
        if (delcol.isEmpty()) {
            newfeature = feature;
        } else {
            /*newfeature is filled with all of the elements of feature that were
            selected by the user*/
            delcol.add(feature[0].length);
            for (Object col : delcol) {
                int c = (int) col;
                for (int i = 0; i < feature.length; i++) {
                    j = curcol;
                    for (; j < c; j++) {
                        newfeature[i][j - count] = feature[i][j];
                    }
                }
                if (j == c) {
                    count++;
                    j++;
                    curcol = c + 1;
                }
            }
        }
        return newfeature;
    }

    /**
     * Z-Normalizes the data such that all measurements have a mean of 0 and
     * variance of 1. Z-normalization is carried out independently for all
     * columns.
     *
     * @param feature the original 2D array of data to be normalized
     * @param normalize true if data is to be normalized
     * @return 2D array of z-normalized data
     */
    public double[][] normalizeColumns(double[][] feature, boolean normalize) {
        double[][] normalized = new double[feature.length][feature[0].length];
        if (normalize) {
            for (int j = 0; j < feature[0].length; j++) {
                double mu = 0;
                double var = 0;
                for (double[] feat : feature) {
                    mu += feat[j];
                }
                mu /= feature.length;
                for (double[] feat : feature) {
                    var += (feat[j] - mu) * (feat[j] - mu);
                }
                var /= feature.length;
                for (int i = 0; i < feature.length; i++) {
                    normalized[i][j] = (var == 0 ? 1 : (feature[i][j] - mu) / sqrt(var));
                }
            }
//            /*Take the stuff below out, just for debugging */
//        JFileChooser jf = new JFileChooser(new File("untitled.csv"));
//        jf.addChoosableFileFilter(new FileNameExtensionFilter("Comma Separated Values","csv"));
//            
//        int returnVal = jf.showSaveDialog(null);
//            
//        File file = jf.getSelectedFile(); 
//
//        if(returnVal == JFileChooser.APPROVE_OPTION) {
////            if(file.getName().length() < 5 | file.getName().length() >= 5 && (file.getName().substring(file.getName().length()-3)).equals(".csv"))
////                file.renameTo(file + ".csv");
//            try{
//
//                        PrintWriter pw = new PrintWriter(file);
//                        StringBuilder sb = new StringBuilder();
//                        
//                        //Header
//                        sb.append("Object,");
//                        sb.append("\n");
//                        
//                        //Data
//                        for(int i = 0; i < normalized.length; i++){
//                            for(int k = 0; k < normalized[i].length; k++){
//                                sb.append(normalized[i][k]);
//                                sb.append(',');
//                            }
//                            sb.append('\n');
//                        }
//                        
//                        pw.write(sb.toString());
//                        pw.close();
//                        
//            }catch(Exception e){
//                
//            }
//        }else{
//            
//        }
            return normalized;

        } else {
            normalized = feature;
            return normalized;
        }
    }

    public int[][] getList(String location) {
        ArrayList temp = new ArrayList();
        int[][] table;
        String line = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(location));

            while ((line = br.readLine()) != null) {
                String vals[] = line.split(",");
                ArrayList row = new ArrayList(vals.length);
                for (String val : vals) {
                    if (!val.equals("null")) {
                        row.add(Integer.parseInt(val));
                    } else {
                        row.add(0.0);
                    }
                }

                temp.add(row);
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }

        table = new int[temp.size()][((ArrayList) temp.get(0)).size()];
        for (int i = 0; i < temp.size(); i++) {
            for (int j = 0; j < ((ArrayList) temp.get(0)).size(); j++) {
                table[i][j] = (int) ((ArrayList) temp.get(i)).get(j);
            }
        }

        return table;
    }

    /**
     * Determines if the operating system is Windows or not
     *
     * @return True if Windows, False otherwise
     */
    public boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().startsWith("windows");
    }

    /**
     * Retrieves the current working directory
     *
     * @return path of the current working directory
     */
    public String getCWD() {
        return Paths.get("").toAbsolutePath().toString();
    }

    /**
     * Provides the call name for executing a python script
     *
     * @return the name of python on the computer
     */
    public String getPython() {
        Process p;
        String s = new String();
        try {
            if (isWindows()) {
                p = Runtime.getRuntime().exec("cmd.exe /c where python");
                p.waitFor();
            } else {
                p = Runtime.getRuntime().exec("which python");
                p.waitFor();
            }

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            s = stdInput.readLine();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return s;
    }

    /**
     * Deletes the files with the given names in the current path
     *
     * @param files String array holding the names of the files to be deleted
     */
    public void deleteFiles(String[] files) {
        try {
            for (String file : files) {
                File deleted = new File(file);
                boolean isFileDeleted = deleted.delete();
//                System.out.println(isFileDeleted);
            }
        } catch (java.lang.SecurityException ie) {
            ie.printStackTrace();
        }

    }

    /**
     * Creates a new file composed of the data provided. Writes a file
     * 'matrix_for_python.csv' using the data given The file does not have a
     * header row or header column.
     *
     * @param feature data to put into a file
     */
    public void makeMatrixCSVFile(double[][] feature) {
        try {

            PrintWriter pw = new PrintWriter("matrix_for_python.csv");
            StringBuilder sb = new StringBuilder();

            for (double[] row : feature) {
                for (int i = 0; i < row.length; i++) {
                    sb.append(row[i]);
                    sb.append(',');
                }
                sb.replace(sb.lastIndexOf(","), sb.length(), "\n");
                //sb.append("\n");
            }

            pw.write(sb.toString());
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method is overwritten by each analysis method.
     *
     * @param al parameters of analysis method
     * @param feature 2D feature array of the objects
     * @param validate whether or not to perform validation
     * @return always false
     */
    @Override
    public boolean process(ArrayList al, double[][] feature, boolean validate) {
        return false;
    }

    @Override
    public boolean copyComponentParameter(int index, ArrayList dComponents, ArrayList sComponents) {

        try {

            return true;

        } catch (Exception e) {

            System.out.println("ERROR: Could not copy parameter(s) for " + NAME);

            return false;
        }
    }

    /**
     * Provides the current time
     *
     * @return a string of the current time in h:mm:s format
     */
    @Override
    public String getCurrentTime() {
        DateTimeFormatter time = DateTimeFormatter.ofPattern("h'_'mm'_'s");
        return LocalTime.now().format(time);
    }
}
