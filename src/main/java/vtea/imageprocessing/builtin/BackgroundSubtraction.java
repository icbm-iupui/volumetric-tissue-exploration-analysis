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

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.ChannelSplitter;
import ij.plugin.filter.BackgroundSubtracter;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import net.imglib2.img.Img;
import org.scijava.plugin.Plugin;
import vtea.imageprocessing.AbstractImageProcessing;
import vtea.imageprocessing.ImageProcessing;

/**
 *
 * @author sethwinfree
 */
@Plugin(type = ImageProcessing.class)
public class BackgroundSubtraction extends AbstractImageProcessing {

    public BackgroundSubtraction() {
        VERSION = "0.1";
        AUTHOR = "Seth Winfree";
        COMMENT = "Implements the plugin from ImageJ";
        NAME = "Background Subtraction";
        KEY = "BackgroundSubtraction";

        protocol = new ArrayList();

        protocol.add(new JLabel("Minimum dimension of object (pixels):"));

        protocol.add(new JTextField("5", 5));
    }

    //protcol key:  
    @Override
    public Img getResult() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Img getPreview() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getImageJMacroCommand() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String runImageJMacroCommand(String str) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void sendProgressComment() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getProgressComment() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean process(ArrayList al, ImagePlus imp) {

        JTextField radius;
        int channel;
        JRadioButton slidingparabaloid, stack;

        channel = (Integer) al.get(1);
        radius = (JTextField) al.get(3);

        BackgroundSubtracter rbb = new BackgroundSubtracter();
        ChannelSplitter cs = new ChannelSplitter();

        ImageStack is;

        //is = cs.getChannel(imp, channel+1);
        is = imp.getImageStack();

        for (int n = 1; n <= is.getSize(); n++) {
            rbb.rollingBallBackground(is.getProcessor(n), Float.parseFloat(radius.getText()), false, false, false, true, true);
        }
        //imgResult = ImageJFunctions.wrapReal(imp);
        return true;
    }

    @Override
    public boolean process(ArrayList al, Img img) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean copyComponentParameter(String version, ArrayList dComponents, ArrayList sComponents) {

        try {
            dComponents.clear();

            JTextField sRadius = (JTextField) sComponents.get(1);

            dComponents.add(new JLabel("Minimum dimension of object (pixels):"));
            dComponents.add((new JTextField(sRadius.getText(), 3)));

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

            String sRadius = (String) fields.get(0);

            dComponents.add(new JLabel("Minimum dimension of object (pixels):"));
            dComponents.add((new JTextField(sRadius, 3)));

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
