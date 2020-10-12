/*
 * Copyright (C) 2019 SciJava
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
import org.scijava.plugin.Plugin;
import java.util.ArrayList;
import java.util.HashMap;
import static vtea.lut.AbstractLUT.ZEROPERCENT;

import vteaexploration.LutCustomColorChooser;

/**
 *
 * @author sukhoc
 */
@Plugin (type = LUT.class)
public class CustomLUT extends AbstractLUT {

    public CustomLUT(){
        VERSION = "0.1";
        AUTHOR = "Suraj Khochare";
        COMMENT = "Implements LUTs for VTEA";
        NAME = "Custom LUT";
        KEY = "CUSTOMLUT";
        
        //Temporary------------------------
        ZEROPERCENT = new Color(0x000000);
        TENPERCENT = new Color(0x000000);
        TWENTYPERCENT = new Color(0x000000);
        THIRTYPERCENT = new Color(0x000000);
        FORTYPERCENT = new Color(0x000000);
        FIFTYPERCENT = new Color(0x000000);
        SIXTYPERCENT = new Color(0x000000);
        SEVENTYPERCENT = new Color(0x000000);
        EIGHTYPERCENT = new Color(0x000000);
        NINETYPERCENT = new Color(0x000000);
        ALLPERCENT = new Color(0x000000);
        //----------------------------------
        
    }
    
}
