/* 
 * Copyright (C) 2016-2018 Indiana University
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
package vtea.workflow;

import ij.ImagePlus;
import java.util.ArrayList;
import vtea.protocol.datastructure.AbstractProtocol;

/**
 *
 * @author sethwinfree
 * @param <T>
 * @param <S>
 */
public abstract class AbstractWorkflow<T extends Object, S extends ArrayList> implements Workflow {
 
    protected String VERSION = "0.0";
    protected String AUTHOR = "VTEA Developer";
    protected String COMMENT = "New functionality";
    protected String NAME = "ABSTRACTWORKFLOWPROCESSING";
    protected String KEY = "ABSTRACTWORKFLOWPROCESSING";

    AbstractProtocol protocol;
    
    Object original;
    Object preview;
    Object result;

    
    @Override
    public T getResult() {
        return (T) new Object();
    }

    @Override
    public T getPreview() {
        return (T) preview;
    }
    
    @Override
    public AbstractProtocol getSteps() {
        return protocol;
    }
    
   @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getKey() {
        return KEY;
    }
}
