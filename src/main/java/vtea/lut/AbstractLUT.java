/*
 * Copyright (C) 2018 SciJava
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
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author sethwinfree
 */
public class AbstractLUT implements LUT {
    
    protected String VERSION = "0.1";
    protected String AUTHOR = "VTEA Developer";
    protected String COMMENT = "Abstract LUT";
    protected String NAME = "ABSTRACT LUT";
    protected String KEY = "ABSTRACT LUT";
    
    static Color ZEROPERCENT = new Color(0, 0, 0);
    static Color TENPERCENT = new Color(0, 0, 82);
    static Color TWENTYPERCENT = new Color(61, 0, 178);
    static Color THIRTYPERCENT = new Color(122, 0, 227);
    static Color FORTYPERCENT = new Color(178, 0, 136);
    static Color FIFTYPERCENT = new Color(213, 27, 45);
    static Color SIXTYPERCENT = new Color(249, 95, 0);
    static Color SEVENTYPERCENT = new Color(255, 140, 0);
    static Color EIGHTYPERCENT = new Color(255, 175, 0);
    static Color NINETYPERCENT = new Color(255, 190, 0);
    static Color ALLPERCENT = new Color(255, 250, 50);

    @Override
    public HashMap getLUTMAP() {
        HashMap<String, Color> hm = new HashMap<String, Color>();
        
        hm.put("0", ZEROPERCENT);
        hm.put("10", TENPERCENT);
        hm.put("20", TWENTYPERCENT);
        hm.put("30", THIRTYPERCENT);
        hm.put("40", FORTYPERCENT);
        hm.put("50", FIFTYPERCENT);
        hm.put("60", SIXTYPERCENT);
        hm.put("70", SEVENTYPERCENT);
        hm.put("80", EIGHTYPERCENT);
        hm.put("90", NINETYPERCENT);
        hm.put("100", ALLPERCENT);
        
        return hm;
      }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getKey() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
  
    
    
    
}
