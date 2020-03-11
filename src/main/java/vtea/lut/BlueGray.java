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

        ZEROPERCENT = new Color(0x999999);
        TENPERCENT = new Color(0x8a8aa3);
        TWENTYPERCENT = new Color(0x7b7bad);
        THIRTYPERCENT = new Color(0x6666bb);
        FORTYPERCENT = new Color(0x5050ca);
        FIFTYPERCENT = new Color(0x4c4ccc);
        SIXTYPERCENT = new Color(0x3d3dd6);
        SEVENTYPERCENT = new Color(0x2e2ee0);
        EIGHTYPERCENT = new Color(0x1f1feb);
        NINETYPERCENT = new Color(0x0f0ff5);
        ALLPERCENT = new Color(0x0000ff);

    }

}
