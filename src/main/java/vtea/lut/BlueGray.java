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
import org.scijava.plugin.Plugin;

/**
 *
 * @author sethwinfree
 */
@Plugin(type = LUT.class)
public class BlueGray extends AbstractLUT {

    public BlueGray() {
        VERSION = "0.1";
        AUTHOR = "Seth Winfree";
        COMMENT = "Implements LUTs for VTEA";
        NAME = "BlueGray";
        KEY = "BGLUT";
        TRANSPARENCY = 50;
        
        

        ZEROPERCENT = new Color(0x96999999, true);
        TENPERCENT = new Color(0x968a8aa3, true);
        TWENTYPERCENT = new Color(0x967b7bad, true);
        THIRTYPERCENT = new Color(0x966666bb, true);
        FORTYPERCENT = new Color(0x965050ca, true);
        FIFTYPERCENT = new Color(0x964c4ccc, true);
        SIXTYPERCENT = new Color(0x963d3dd6, true);
        SEVENTYPERCENT = new Color(0x962e2ee0, true);
        EIGHTYPERCENT = new Color(0x961f1feb, true);
        NINETYPERCENT = new Color(0x960f0ff5, true);
        ALLPERCENT = new Color(0x960000ff, true);

    }

}
