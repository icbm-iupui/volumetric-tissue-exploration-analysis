/* 
 * Copyright (C) 2016 Indiana University
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
package MicroProtocol;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.LUT;
import java.awt.Color;

/**
 *
 * @author vinfrais
 */
public class UtilityMethods {
    

    static public ImagePlus makeThumbnail(ImagePlus imp){   
       
        imp.setPosition(imp.getStackSize()/2);
        
//        if(imp.isHyperStack()){
//            IJ.run("Hyperstack to Stack", "");
//        }
 
        //imp.show();
        
        CompositeImage compImp = new CompositeImage(imp, IJ.COMPOSITE); 
        
//        for(int i = 1; i <= imp.getNChannels(); i++){
//            
//            switch(i){
//                case 1:
//                        //imp.setC(i); IJ.run(imp, "Cyan", "");break;
//                        //imp.setC(i); imp.setLut(LUT.createLutFromColor(Color.CYAN));  break;
//                        compImp.setChannelLut(LUT.createLutFromColor(Color.CYAN),1); break; 
//                case 2:
//                        //imp.setC(i); imp.setLut(LUT.createLutFromColor(Color.CYAN));  break;
//                        //imp.setC(i); imp.setLut(LUT.createLutFromColor(Color.RED));  break;
//                        compImp.setChannelLut(LUT.createLutFromColor(Color.YELLOW),2);break;
//                case 3:
//                        //imp.setC(i); imp.setLut(LUT.createLutFromColor(Color.CYAN));  break;
//                        //imp.setC(i); imp.setLut(LUT.createLutFromColor(Color.GREEN));  break;
//                        compImp.setChannelLut(LUT.createLutFromColor(Color.GREEN),3);break;
//                case 4:
//                    //imp.setC(i); imp.setLut(LUT.createLutFromColor(Color.CYAN));  break;
//                        //imp.setC(i); imp.setLut(LUT.createLutFromColor(Color.MAGENTA)); break;
//                        compImp.setChannelLut(LUT.createLutFromColor(Color.MAGENTA),4);break;        
//                case 5:
//                        //imp.setC(i); imp.setLut(LUT.createLutFromColor(Color.YELLOW));  break;
//                        compImp.setChannelLut(LUT.createLutFromColor(Color.BLUE),5);break;
//                case 6:
//                        //imp.setC(i); imp.setLut(LUT.createLutFromColor(Color.BLUE)); break;
//                        compImp.setChannelLut(LUT.createLutFromColor(Color.RED),6);break;
//                case 7:
//                        //imp.setC(i); imp.setLut(LUT.createLutFromColor(Color.WHITE)); break;
//                        compImp.setChannelLut(LUT.createLutFromColor(Color.WHITE),7);break;
//                default:
//                        break;
//            }
         
 
            
         
          
//        }

        compImp.setPosition(imp.getStackSize()/2);
        return compImp;
    }
    
    static private double[] getChannelDisplayRange(ImagePlus imp, int channel){
        
        double[] range = new double[2];
        double max = 0;
        double min = Math.pow(2,imp.getBitDepth())-1;
        imp.setC(channel);
        ImageStack is = imp.getImageStack();
        
        
        if(is.getSize() == 1){
            range[0] = imp.getProcessor().getMin();
            range[1] = imp.getProcessor().getMax();
            System.out.println("PROFILING: range determination for channel " + channel + ": " + range[0] + " to " + range[1]);
            return range;
        } else {
            for(int i = 0; i < is.getSize(); i++){
                for(int x = 0; x < is.getWidth(); x++){
                    for(int y = 0; y < is.getHeight(); y++){
                        if(is.getVoxel(x, y, i)> max){
                            max = is.getVoxel(x, y, i);
                        }else if(is.getVoxel(x, y, i) < min && is.getVoxel(x, y, i) < max){
                            min = is.getVoxel(x, y, i);
                        }
                    }
                }
            }
           range[0] = min;
           range[1] = max;
           System.out.println("PROFILING: range determination for channel " + channel + ": " + range[0] + " to " + range[1]);
           return range;
           
        }
    }
}
