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
package vtea.plotprocessing;

import java.util.ArrayList;

/**
 *
 * @author sethwinfree
 */
abstract public class AbstractPlotMaker implements PlotMaker {

    protected String VERSION = "0.0";
    protected String AUTHOR = "VTEA developer";
    protected String COMMENT = "Classes for generating plots.";
    protected String NAME = "Abstract PlotMaker";
    protected String KEY = "AbstractPlotMaker";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public String getAuthor() {
        return AUTHOR;
    }

    @Override
    public String getComment() {
        return COMMENT;
    }

    @Override
    public String makePlot(String location, String key, ArrayList<ArrayList<Double>> al,
            ArrayList<String> featureNames, String group) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public boolean doesMultiples(){
        return false;
    }

}
