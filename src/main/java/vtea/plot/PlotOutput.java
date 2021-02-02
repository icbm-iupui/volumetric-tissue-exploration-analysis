/*
 * Copyright (C) 2021 SciJava
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
package vtea.plot;

import ij.io.FileSaver;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.apache.commons.io.FilenameUtils;
import vtea._vtea;

/**
 *
 * @author sethwinfree
 */
public class PlotOutput {

    ArrayList<String> graphs;
    ArrayList<String> imagetype;

    final static int PNG = 0;
    final static int TIFF = 1;

    public void PlotOutput() {
        graphs = new ArrayList<String>();
    }

    public String getPlotName(int i) {
        if (graphs.size() > 0) {
            return graphs.get(i);
        } else {
            return "No graphs available...";
        }
    }

    //pdf  view pdf 
    public ImageIcon getPlot(int i) {
        ImageIcon imageIcon = new ImageIcon();
        try {
            String filename = vtea._vtea.PLOT_DIRECTORY
                    + System.getProperty("file.separator") + graphs.get(i);
            BufferedImage img = ImageIO.read(new File(filename));
            imageIcon = new ImageIcon(img);

        } catch (IOException ex ) {
              ex.printStackTrace();
        }
        return imageIcon;
    }

    public ImageIcon getPlot(String name) {
        ImageIcon imageIcon = new ImageIcon();
        try {
            String filename = vtea._vtea.PLOT_DIRECTORY
                    + System.getProperty("file.separator") + name;
            BufferedImage img = ImageIO.read(new File(filename));
            imageIcon = new ImageIcon(img);

        } catch (IOException ex) {
             ex.printStackTrace();
        }
        return imageIcon;
    }
    
        
    public void deletePlot(int i) {
        
        graphs.remove(i);
        
    }
    
            //pdf  view pdf 
    public String savePlot(int i) {
        
        int returnVal;
        String filename = vtea._vtea.PLOT_DIRECTORY
                    + System.getProperty("file.separator") + graphs.get(i);
        return filename;
        
        
        
    }

    public int getPlotCount() {
        return graphs.size();
    }

    public void updatePlotOutput() {
        //get directory contents, rebuild.
        File f = new File(vtea._vtea.PLOT_DIRECTORY);
        File[] ar = f.listFiles();
        graphs = new ArrayList<String>();
        for (int i = 0; i < ar.length; i++) {
            if(ar[i].getName().contains(".png") || ar[i].getName().contains(".tif")){
            graphs.add(ar[i].getName());}
        }

        //return graphs;
    }

    public ArrayList<String> getPlots() {
        return graphs;
    }

}
