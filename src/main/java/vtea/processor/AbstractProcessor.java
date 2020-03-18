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
package vtea.processor;

import javax.swing.SwingWorker;
import vtea.processor.listeners.ProgressListener;

/**
 *
 * @author sethwinfree
 */
public abstract class AbstractProcessor extends SwingWorker<Void, Void> implements Processor, ProgressListener {

    protected String VERSION = "0.0";
    protected String AUTHOR = "VTEA Developer";
    protected String COMMENT = "New functionality";
    protected String NAME = "ABSTRACTPROCESSOR";
    protected String KEY = "ABSTRACTPROCESSOR";

    protected String key;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    public String getUIDKey() {
        return key;
    }

    @Override
    public void FireProgressChange(String str, double db) {
        firePropertyChange("progress", 0, (int) db);
        firePropertyChange("comment", key, str);
    }

}
