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

import ij.ImagePlus;
import ij.ImageStack;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.morphology.MinimaAndMaxima3D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imagej.ops.OpService;
import net.imglib2.IterableInterval;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import inra.ijpb.watershed.Watershed;

/**
 *
 * @author Seth
 */

//@Plugin(type = Segmentation.class)
public class Imglib2ConnectedComponents extends AbstractSegmentation {
    private static RandomAccessibleInterval makeRai(IterableInterval ii, ImageJ ij) {
        RandomAccessibleInterval rai;
        if (ii instanceof RandomAccessibleInterval) {
            rai = (RandomAccessibleInterval)ii;
        } else {
            rai = ij.op().create().img(ii);
            ij.op().copy().iterableInterval((Img) rai, ii);
        }
        return rai;
    }

  


    
  public Imglib2ConnectedComponents (){   
     VERSION = "0.1";
        AUTHOR = "Seth Winfree";
        COMMENT = "Connected components object segmentation by imglib2.";
        NAME = "imglib2 Connect 3D";
        KEY = "imglib2ConnectedComponents";

        protocol = new ArrayList();

        protocol.add(new JLabel("Nothing to change!"));
        protocol.add(new JTextField(0));
//        protocol.add(new JLabel("Centroid Offset"));
//        protocol.add(new JLabel("Min Vol (vox)"));
//        protocol.add(new JLabel("Max Vol (vox)"));
//        protocol.add(new JCheckBox("Watershed", true));
       
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
    public boolean process(ImageStack[] is, List protocol, boolean count) {
        int segmentationChannel = (int) protocol.get(2);
        
        ImagePlus imp = new ImagePlus("Segmentation", is[segmentationChannel]);
            run(imp);
       
       return true;

    }
 
 
 
     @Override
    public boolean  process(ImagePlus imp, List details, boolean calculate) {
      
        //dataset = new DefaultDataset(vtea._vtea.context, new ImgPlus(ImagePlusAdapter.wrap(imp)));
        
       run(imp);
       
       return true;
      
    }
    
     private <T extends RealType<T>, I extends NumericType< I > & NativeType< I >> void run(ImagePlus imp) {
         
           OpService ops;
        boolean invert = false;
        
        ImageJ ij = new ImageJ();
        
        final Img<I> ImglibOriginal = ImagePlusAdapter.wrap( imp );
        final Img<I> ImglibSegmented = ImglibOriginal.copy();
        
        final ImgPlus<T> imgSegemented = new ImgPlus(ImglibSegmented); 
        
        Img segmentedImg = (Img) ij.op().threshold().otsu(imgSegemented);
        
      double tolerance = 5;
     int conn = 26;
     boolean dams = false;
     
    ImagePlus segmentedImp = ImageJFunctions.wrap((RandomAccessibleInterval<I>)segmentedImg, "Segmented");
     
    segmentedImp.show();
         
ImageStack image = segmentedImp.getImageStack();
// find regional minima on gradient image with dynamic value of 'tolerance' and 'conn'-connectivity
ImageStack regionalMinima = MinimaAndMaxima3D.extendedMinima(image, tolerance, conn );
// impose minima on gradient image
ImageStack imposedMinima = MinimaAndMaxima3D.imposeMinima(image, regionalMinima, conn );
// label minima using connected components (32-bit output)
ImageStack labeledMinima = BinaryImages.componentsLabeling( regionalMinima, conn, 32 );
// apply marker-based watershed using the labeled minima on the minima-imposed 
// gradient image (the last value indicates the use of dams in the output)
ImageStack resultStack = Watershed.computeWatershed( imposedMinima, labeledMinima, conn, dams );

ImagePlus watershedImp = new ImagePlus("Wateshed",resultStack);

watershedImp.show();
    
         
        final Img<I> Imglib = ImagePlusAdapter.wrap( watershedImp );
        
        final ImgPlus<T> img = new ImgPlus(Imglib); 
        
        ImageJFunctions.show(img);
        
      


//                
//                IterableInterval erodedInterval = ij.op().morphology().erode(img, new DiamondShape(1));
//                RandomAccessibleInterval erodedImg = makeRai(erodedInterval, ij);
                
                
                
ImgLabeling cca = ij.op().labeling().cca(makeRai(img, ij), ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

                
		//show result
		//ij.ui().show(cca.getIndexImg());
                
                ImageJFunctions.show(cca.getIndexImg());

		//get count of connected components
		LabelRegions<IntegerType> regions = new LabelRegions(cca);
		int cells = regions.getExistingLabels().size();

		//print result
		System.out.println("Counted " + cells + " cells.");
     }
                
               
     

    
      	
}
