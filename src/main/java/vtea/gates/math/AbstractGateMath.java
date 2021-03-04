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
package vtea.gates.math;

import ij.gui.Roi;
import java.util.ArrayList;
import vtea.exploration.plotgatetools.gates.Gate;
import vteaobjects.MicroObject;
import vteaobjects.MicroObjectModel;

/**
 *
 * @author sethwinfree
 */
public abstract class AbstractGateMath<T extends Gate, A extends Roi, K extends MicroObjectModel>
        implements GateMath {
    
        String VERSION = "0.0";
        String AUTHOR = "Seth Winfree";
        String COMMENT = "Abstract";
        String NAME = "abstract";
        String KEY = "ABS";
        String TYPE = "ABS";
    
        

        
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    public String getVersion() {
        return VERSION;
    }

    public String getAuthor() {
        return AUTHOR;
    }

    public String getComment() {
        return COMMENT;
    }

    public String getType() {
        return TYPE;
    }
    
}
