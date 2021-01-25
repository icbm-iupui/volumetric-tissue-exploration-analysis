/*
 * Copyright (C) 2021 SciJava
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
package vtea.renjin;

/**
 *
 * @author sethwinfree
 */
public abstract class AbstractRenjin {
    
    public final static String VTEACOLORS = "plot_colors <- c( hex(RGB(255/255, 0, 0)), " +
"        hex(RGB(0, 255/255, 0))," +
"        hex(RGB(0, 0, 255/255))," +
"        hex(RGB(255/255, 153/255, 51/255))," +
"        hex(RGB(153/255, 255/255, 51/255))," +
"        hex(RGB(51/255, 255/255, 153/255))," +
"        hex(RGB(51/255, 255/255, 255/255)), " +
"        hex(RGB(102/255, 178/255, 255/255))," +
"        hex(RGB(178/255, 102/255, 255/255))," +
"        hex(RGB(255/255, 102/255, 255/255)), " +
"        hex(RGB(255/255, 102/255, 178/255))," +
"        hex(RGB((255*0.5)/255, (0*0.5)/255, (0*0.5)/255)), " +
"        hex(RGB((0*0.5)/255, (255*0.5)/255, (0*0.5)/255))," +
"        hex(RGB((0*0.5)/255, (0*0.5)/255, (255*0.5)/255))," +
"        hex(RGB((255*0.5)/255, (153*0.5)/255, (51*0.5)/255))," +
"        hex(RGB((153*0.5)/255, (255*0.5)/255, (51*0.5)/255))," +
"        hex(RGB((51*0.5)/255, (255*0.5)/255, (153*0.5)/255))," +
"        hex(RGB((51*0.5)/255, (255*0.5)/255, (255*0.5)/255)), " +
"        hex(RGB((102*0.5)/255, (178*0.5)/255, (255*0.5)/255))," +
"        hex(RGB((178*0.5)/255, (102*0.5)/255, (255*0.5)/255))," +
"        hex(RGB((255*0.5)/255, (102*0.5)/255, (255*0.5)/255)), " +
"        hex(RGB((255*0.5)/255, (102*0.5)/255, (178*0.5)/255)))";
    
    
}
