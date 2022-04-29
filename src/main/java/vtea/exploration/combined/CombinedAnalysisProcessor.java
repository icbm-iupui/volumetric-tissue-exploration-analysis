/*
 * Copyright (C) 2022 SciJava
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
package vtea.exploration.combined;

import java.util.ArrayList;
import org.scijava.plugin.Plugin;
import vtea.processor.AbstractProcessor;
import vtea.processor.Processor;

/**
 *
 * @author sethwinfree
 */
@Plugin(type = Processor.class)
public class CombinedAnalysisProcessor extends AbstractProcessor {
    
    public CombinedAnalysisProcessor() {
        VERSION = "0.1";
        AUTHOR = "Seth Winfree";
        COMMENT = "Processor for combining datasets";
        NAME = "Combined Analysis Processor";
        KEY = "CombinedAnalysisProcessor";
    }

    @Override
    protected Void doInBackground() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int process(ArrayList al, String... str) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getChange() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
