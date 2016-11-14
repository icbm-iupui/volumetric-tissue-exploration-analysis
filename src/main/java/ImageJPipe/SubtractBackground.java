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
package ImageJPipe;

/**
 *
 * @author winfrees
 */
public class SubtractBackground extends Object {

    public double radius;
    public boolean LightBackground;
    public boolean SlidingParaboloid;
    public boolean DisableSmoothing;
    public String macroText;

    private String lbtext = "";
    private String sptext = "";
    private String dstext = "";

    public SubtractBackground(double r, boolean lb, boolean sp, boolean ds, String stacktext) {
        radius = r;
        LightBackground = lb;
        if (lb == true) {
            lbtext = "light ";
        }
        SlidingParaboloid = sp;
        if (sp == true) {
            lbtext = "sliding ";
        }
        DisableSmoothing = ds;
        if (ds == true) {
            lbtext = "disable ";
        }

        macroText = "run(\"Subtract Background...\", \"rolling=" + radius + " " + lbtext + sptext + dstext + stacktext;
    }

    public String getMacroText() {
        return macroText;
    }
}
