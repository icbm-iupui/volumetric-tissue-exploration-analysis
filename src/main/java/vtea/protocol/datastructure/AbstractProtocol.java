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
package vtea.protocol.datastructure;

import java.util.ArrayList;

/**
 *
 * @author sethwinfree
 */
public abstract class AbstractProtocol extends ArrayList implements Protocol  {
    
       String NAME = "Abstract Protocol";
       String KEY = "AbstractProtocol";
       String DESCRIPTION = "Abstract class for protocols.  Protocols string "
               + "processes together or hold segmentation approaches.";
       
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getKey() {
        return KEY;
    }
    
    @Override
    public String getDescription() {
        return DESCRIPTION;
    }
    
    AbstractProtocol(){
    }
    
    AbstractProtocol(String name){
        NAME = name;
    }
    
    AbstractProtocol(String name, ArrayList al){
        NAME = name;
        addAll(al);
    }
    
    AbstractProtocol(String name, String description, ArrayList al){
        NAME = name;
        DESCRIPTION = description;
        addAll(al);
    }  
}
