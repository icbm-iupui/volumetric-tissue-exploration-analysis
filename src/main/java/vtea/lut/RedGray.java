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
public class RedGray extends AbstractLUT {

    public RedGray() {
        VERSION = "0.1";
        AUTHOR = "Seth Winfree";
        COMMENT = "Implements LUTs for VTEA";
        NAME = "RedGray";
        KEY = "RGLUT";

        ZEROPERCENT = new Color(0x9e9191);
        TENPERCENT = new Color(0xad7a7a);
        TWENTYPERCENT = new Color(0xbd6363);
        THIRTYPERCENT = new Color(0xc75454);
        FORTYPERCENT = new Color(0xcc4c4c);
        FIFTYPERCENT = new Color(0xd63d3d);
        SIXTYPERCENT = new Color(0xd14545);
        SEVENTYPERCENT = new Color(0xdb3636);
        EIGHTYPERCENT = new Color(0xeb1f1f);
        NINETYPERCENT = new Color(0xf50f0f);
        ALLPERCENT = new Color(0xff0000);

    }

}
