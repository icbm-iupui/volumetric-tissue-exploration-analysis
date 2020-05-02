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
package vtea.lut;

import java.awt.Color;
import java.util.HashMap;
import org.jfree.chart.renderer.LookupPaintScale;
import org.scijava.plugin.Plugin;
import static vtea.lut.AbstractLUT.ZEROPERCENT;

/**
 *
 * @author sethwinfree
 */
@Plugin(type = LUT.class)
public class VTEA_LUT extends AbstractLUT {
    
    

    Color[] colorsRGB =  new Color[1];
    
    public VTEA_LUT() {
        VERSION = "0.1";
        AUTHOR = "Seth Winfree";
        COMMENT = "Implements Cluster LUTs for VTEA overlapping with gating "
                + "colors";
        NAME = "VTEA Classes";
        KEY = "VTEACLASS";

        ZEROPERCENT = new Color(0x80000000, true);
        TENPERCENT = new Color(0x80000000, true);
        TWENTYPERCENT = new Color(0x80000000, true);
        THIRTYPERCENT = new Color(0x80000000, true);
        FORTYPERCENT = new Color(0x80000000, true);
        FIFTYPERCENT = new Color(0x80000000, true);
        SIXTYPERCENT = new Color(0x80000000, true);
        SEVENTYPERCENT = new Color(0x80000000, true);
        EIGHTYPERCENT = new Color(0x80000000, true);
        NINETYPERCENT = new Color(0x80000000, true);
        ALLPERCENT = new Color(0x80000000, true);

        this.setRGBColor(150);

    }
    
    private void setRGBColor(int transperancy){
        this.setTransparency(transperancy);
        
        this.colorsRGB =  new Color[]{
        new Color(255, 0, 0, TRANSPARENCY), 
        new Color(0, 255, 0, TRANSPARENCY), 
        new Color(0, 0, 255, TRANSPARENCY),
        //new Color(255, 255, 0, TRANSPARENCY), 
        new Color(255, 153, 51, TRANSPARENCY),
        new Color(153, 255, 51, TRANSPARENCY), 
        new Color(51, 255, 153, TRANSPARENCY),
        new Color(51, 255, 255, TRANSPARENCY), 
        new Color(102, 178, 255, TRANSPARENCY),
        //new Color(102, 102, 255, TRANSPARENCY), 
        new Color(178, 102, 255, TRANSPARENCY),
        new Color(255, 102, 255, TRANSPARENCY), 
        new Color(255, 102, 178, TRANSPARENCY),
        new Color((int)(255*0.5), (int)(0*0.5), (int)(0*0.5), TRANSPARENCY), 
        new Color((int)(0*0.5), (int)(255*0.5), (int)(0*0.5), TRANSPARENCY), 
        new Color((int)(0*0.5), (int)(0*0.5), (int)(255*0.5), TRANSPARENCY),
        //new Color((int)(255*0.5), (int)(255*0.5), (int)(0*0.5), TRANSPARENCY), 
        new Color((int)(255*0.5), (int)(153*0.5), (int)(51*0.5), TRANSPARENCY),
        new Color((int)(153*0.5), (int)(255*0.5), (int)(51*0.5), TRANSPARENCY), 
        new Color((int)(51*0.5), (int)(255*0.5), (int)(153*0.5), TRANSPARENCY),
        new Color((int)(51*0.5), (int)(255*0.5), (int)(255*0.5), TRANSPARENCY), 
        new Color((int)(102*0.5), (int)(178*0.5), (int)(255*0.5), TRANSPARENCY),
        //new Color((int)(102*0.5), (int)(102*0.5), (int)(255*0.5), TRANSPARENCY), 
        new Color((int)(178*0.5), (int)(102*0.5), (int)(255*0.5), TRANSPARENCY),
        new Color((int)(255*0.5), (int)(102*0.5), (int)(255*0.5), TRANSPARENCY), 
        new Color((int)(255*0.5), (int)(102*0.5), (int)(178*0.5), TRANSPARENCY),
    };
    }
    
    @Override
     public LookupPaintScale getPaintScale(double min, double max){
         
         ps = new LookupPaintScale(min, max+1, new Color(0x999999));
         
         ps.add(-2, new Color(210, 210, 210, 60));
         ps.add(-1, new Color(210, 210, 210, 60));
         
         for(int i = 0; i < colorsRGB.length; i++){      
            ps.add(i, colorsRGB[i]);      
         }
         
         return ps;
         
     }
     
         @Override
    public HashMap getLUTMAP() {
        HashMap<String, Color> hm = new HashMap<String, Color>();
        
        for(int i = 0; i < colorsRGB.length; i++){  
        hm.put(Integer.toString(i), colorsRGB[i]);
        }
        return hm;
      }


}
