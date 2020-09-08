/*
 * Copyright (C) 2020 SciJava
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
package vtea.objects.Segmentation;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.data.image.Images3D;
import inra.ijpb.morphology.MinimaAndMaxima3D;
import inra.ijpb.watershed.Watershed;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Seth
 */
//@Plugin(type = Segmentation.class)
public class MorphoLibJConnectedComponents extends AbstractSegmentation {
    
    int[] settings = {1, 1};
    
    String[] choices = {"6", "26"};

    JTextField f1 = new JTextField(String.valueOf(settings[1]), 5);
    JComboBox f2 = new JComboBox(choices);

public MorphoLibJConnectedComponents() {
    
  VERSION = "0.1";
        AUTHOR = "Seth Winfree";
        COMMENT = "Connected components object segmentation after 3D watershed.";
        NAME = "MorphoLibJ Connect 3D";
        KEY = "MorphoLibJConnectedComponents";

protocol = new ArrayList();

        f1.setPreferredSize(new Dimension(20, 30));
        f1.setMaximumSize(f1.getPreferredSize());
        f1.setMinimumSize(f1.getPreferredSize());

        f2.setPreferredSize(new Dimension(60, 30));
        f2.setMaximumSize(f2.getPreferredSize());
        f2.setMinimumSize(f2.getPreferredSize());

        protocol.add(new JLabel("Tolerance"));
        protocol.add(f1);
        protocol.add(new JLabel("Connectivity"));
        protocol.add(f2);
        protocol.add(new JLabel("WARNING EXPERIMENTAL! this approach does not work well."));
       
 }


    @Override
    public JPanel getSegmentationTool() {
        JPanel panel = new JPanel();
        panel.setBackground(vtea._vtea.BACKGROUND);

        return panel;
    }
    
     @Override
    public ArrayList getSegmentationToolOptions() {
        return protocol;
     }
    
    public JPanel getVisualization() {
                JPanel panel = new JPanel();
        panel.setBackground(vtea._vtea.BACKGROUND);

        return panel;
    }

    @Override
    public boolean process(ImageStack[] is, List details, boolean count) {
        int segmentationChannel = (int) details.get(2);
        
        ImagePlus imp = new ImagePlus("Segmentation", is[segmentationChannel]);

         ArrayList al = (ArrayList) details.get(3);

        int tolerance = Integer.parseInt(((JTextField) (al.get(1))).getText());
        int connectivity = Integer.parseInt(choices[((JComboBox) (al.get(3))).getSelectedIndex()]);
        
        IJ.run(imp, "Invert", "stack");
        
        ImageStack image = imp.getImageStack();
        ImageStack regionalMinima = MinimaAndMaxima3D.extendedMinima(image, tolerance, connectivity );
        ImageStack imposedMinima = MinimaAndMaxima3D.imposeMinima(image, regionalMinima, connectivity );
        ImageStack labeledMinima = BinaryImages.componentsLabeling( regionalMinima, connectivity, 32 );
        ImageStack resultStack = Watershed.computeWatershed(imposedMinima, labeledMinima, connectivity, false );
        
        //ImageStack resultStack = Watershed.com
        

ImagePlus watershedImp = new ImagePlus("Wateshed",resultStack);

Images3D.optimizeDisplayRange( watershedImp );

watershedImp.show();
       
       return true;
      
    }   
    
    
    
    
}

