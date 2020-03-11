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
package vtea;

import ij.ImagePlus;
import ij.io.Opener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.filechooser.FileNameExtensionFilter;
import vtea.processor.ExplorerProcessor;
import vteaobjects.MicroObject;

/**
 *
 * @author sethwinfree
 */
public class OpenObxFormat {

    public OpenObxFormat() {
    }

    public void importObjects(JComponent parent) {

        JFileChooser jf = new JFileChooser(_vtea.LASTDIRECTORY);
        FileNameExtensionFilter filter
                = new FileNameExtensionFilter("VTEA object file.", ".obx", "obx");
        jf.addChoosableFileFilter(filter);
        jf.setFileFilter(filter);
        int returnVal = jf.showOpenDialog(parent);
        File file = jf.getSelectedFile();
        
        _vtea.LASTDIRECTORY = file.getAbsolutePath();

        ArrayList result = new ArrayList();

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                try {
                    FileInputStream fis = new FileInputStream(file);
                    ObjectInputStream ois = new ObjectInputStream(fis);

//                    ProgressMonitorInputStream pm
//                            = new ProgressMonitorInputStream(parent, "Reading" + file.getName(), fis);
//                    
//                    pm.getProgressMonitor().setMillisToPopup(10);
                    

                    result = (ArrayList) ois.readObject();
                    ois.close();
                } catch (IOException e) {
                    System.out.println("ERROR: Could not open the object file.");
                    
                }

                File image = new File(file.getParent(), ((String) result.get(0)) + ".tif");

                if (image.exists()) {

                    Opener op = new Opener();
                    ImagePlus imp = op.openImage(file.getParent(), ((String) result.get(0)) + ".tif");

                    executeExploring((file.getName()).replace(".obx", ""), result, imp);

                } else {

                    System.out.println("WARNING: Could not find the image file.");

                    JFrame frame = new JFrame();
                    frame.setBackground(vtea._vtea.BUTTONBACKGROUND);
                    Object[] options = {"Yes", "No"};
                    int n = JOptionPane.showOptionDialog(frame,
                            "ERROR: The image file associated with the obx file\n "
                            + " could not be found.  Manually open image?",
                            "Image not found...",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.ERROR_MESSAGE,
                            null,
                            options,
                            options[0]);
                    if (n == JOptionPane.NO_OPTION) {

                    } else {
                        JFileChooser jf2 = new JFileChooser(_vtea.LASTDIRECTORY);

                        FileNameExtensionFilter filter2
                                = new FileNameExtensionFilter("Tiff file.", ".tif", "tif");
                        jf2.addChoosableFileFilter(filter2);
                        jf2.setFileFilter(filter2);
                        int returnVal2 = jf2.showOpenDialog(parent);
                        File file2 = jf2.getSelectedFile();
                        Opener op = new Opener();
                        ImagePlus imp = op.openImage(file2.getParent(), file2.getName());
                        executeExploring((file.getName()).replace(".obx", ""), result, imp);
                    }
                }
            } catch (Exception e) {
                System.out.println("ERROR: Not Found.");

            }
        }
    }

    private void executeExploring(String name, ArrayList result, ImagePlus imp) {

        String k = (String) result.get(0);
        ArrayList<MicroObject> objects = (ArrayList<MicroObject>) result.get(1);
        ArrayList measures = (ArrayList) result.get(2);
        ArrayList descriptions = (ArrayList) result.get(3);
        ArrayList descriptionLabels = (ArrayList) result.get(4);

        ExplorerProcessor ep = new ExplorerProcessor(name, imp, objects, measures, descriptions, descriptionLabels);
        ep.execute();

    }

}
