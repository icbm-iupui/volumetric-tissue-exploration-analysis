package vtea.objects.measurements;




/**
 *
 * @author sethwinfree
 */
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



import java.util.ArrayList;
import org.scijava.plugin.Plugin;
import vtea.objects.measurements.AbstractMeasurement;
import vtea.objects.measurements.Measurements;
/**
 *
 * @author sethwinfree
 */
//@Plugin(type = Measurements.class)
public class TheAnswer extends AbstractMeasurement {

    Number get42(){
        return 42;
    }

    public TheAnswer() {
        VERSION = "1.0";
        AUTHOR = "Seth Winfree";
        COMMENT = "The answer to life the universe and everything";
        NAME = "TheAnswer";
        KEY = "TheAnswer";
        TYPE = "Intensity";
    }

    @Override
    public Number process(ArrayList al, ArrayList values) {

        return get42();
    }

}
