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
public class Fire extends AbstractLUT {

    public Fire() {
        VERSION = "0.1";
        AUTHOR = "Seth Winfree";
        COMMENT = "Implements LUTs for VTEA";
        NAME = "Fire";
        KEY = "FIRELUT";

        ZEROPERCENT = new Color(0, 0, 0, 100);
        TENPERCENT = new Color(0, 0, 82,100);
        TWENTYPERCENT = new Color(61, 0, 178, 100);
        THIRTYPERCENT = new Color(122, 0, 227,100);
        FORTYPERCENT = new Color(178, 0, 136,100);
        FIFTYPERCENT = new Color(213, 27, 45,100);
        SIXTYPERCENT = new Color(249, 95, 0, 100);
        SEVENTYPERCENT = new Color(255, 140, 0, 100);
        EIGHTYPERCENT = new Color(255, 175, 0, 100);
        NINETYPERCENT = new Color(255, 190, 0, 100);
        ALLPERCENT = new Color(255, 250, 50, 100);
    }

}
