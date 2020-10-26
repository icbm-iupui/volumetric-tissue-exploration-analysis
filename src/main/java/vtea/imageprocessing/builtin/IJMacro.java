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
package vtea.imageprocessing.builtin;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import ij.process.StackProcessor;
import java.awt.Dimension;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import org.scijava.plugin.Plugin;
import vtea.imageprocessing.AbstractImageProcessing;
import vtea.imageprocessing.ImageProcessing;

/**
 *
 * @author sethwinfree
 */
//@Plugin(type = ImageProcessing.class)  still needs some work, fix load .ijm
public class IJMacro extends AbstractImageProcessing {

    public IJMacro() {
        VERSION = "0.1";
        AUTHOR = "Seth Winfree";
        COMMENT = "Implements macro code";
        NAME = "ImageJ Macro";
        KEY = "IJMacro";

        protocol = new ArrayList();

        JTextPane scriptText = new JTextPane();

        JScrollPane paneScrollPane = new JScrollPane(scriptText);
        paneScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        paneScrollPane.setPreferredSize(new Dimension(250, 155));
        paneScrollPane.setMinimumSize(new Dimension(250, 155));
        paneScrollPane.setToolTipText("Add IJ macro text here");

        protocol.add(new JLabel("Write macro\nhere:"));
        protocol.add(paneScrollPane);
        protocol.add(scriptText);
        

        //final ImageJ ij = new ImageJ();
////        ij.op().labeling().cca(thresholded, StructuringElement.FOUR_CONNECTED);


    }
    
    @Override
    public boolean process(ArrayList al, ImagePlus imp) {
        JScrollPane pane = (JScrollPane) al.get(3);
        JTextPane scriptText = (JTextPane)pane.getComponent(0);

        String macro = scriptText.getText();
        imp.show();
        IJ.runMacro(macro);
        imp.hide();

        return true;
    }
    
     @Override
    public boolean copyComponentParameter(String version, ArrayList dComponents, ArrayList sComponents) {

        try {
            dComponents.clear();

            JTextPane script = (JTextPane) sComponents.get(1);
            dComponents.add(new JLabel("Write Macro here:"));
            dComponents.add(new JScrollPane(script));

            return true;
        } catch (Exception e) {
            System.out.println("ERROR: Could not copy parameter(s) for " + NAME);
            return false;
        }
    }

    @Override
    public boolean loadComponentParameter(String version, ArrayList dComponents, ArrayList fields) {
        try {
            dComponents.clear();

            String script = (String) fields.get(0);

            dComponents.add(new JLabel("Write Macro here:"));
            //dComponents.add((new JScrollPane(new JTextPane(script))));

            return true;

        } catch (Exception e) {

            System.out.println("ERROR: Could not copy parameter(s) for " + NAME);

            return false;
        }
    }

    @Override
    public boolean saveComponentParameter(String version, ArrayList dComponents, ArrayList fields) {
        try {
            fields.clear();
            fields.add(((JTextField) (dComponents.get(1))).getText());
            return true;

        } catch (Exception e) {

            System.out.println("ERROR: Could not copy parameter(s) for " + NAME);

            return false;
        }
    }
}
