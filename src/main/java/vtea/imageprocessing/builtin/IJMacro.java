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

import java.awt.Dimension;
import java.util.ArrayList;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import org.scijava.plugin.Plugin;
import vtea.imageprocessing.AbstractImageProcessing;
import vtea.imageprocessing.ImageProcessing;

/**
 *
 * @author sethwinfree
 */
@Plugin(type = ImageProcessing.class)
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

        //protocol.add(new JLabel("Paste macro here:"));
        protocol.add(paneScrollPane);

        //final ImageJ ij = new ImageJ();
////        ij.op().labeling().cca(thresholded, StructuringElement.FOUR_CONNECTED);
    }
}
