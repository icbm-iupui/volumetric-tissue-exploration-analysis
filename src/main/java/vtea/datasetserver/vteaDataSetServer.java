package vtea.datasetserver;

import java.awt.image.BufferedImage;
import java.net.URI;
import qupath.lib.images.servers.ImageServer;

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
/**
 *
 * @author sethwinfree
 */
abstract public class vteaDataSetServer implements DataSetServer {

    URI location;

    public vteaDataSetServer() {

    }

    public void loadImageReference() {

    }

    public URI getURI() {
        return location;
    }

    public String getURIString() {
        return location.getPath();
    }

    public void getChunk(Object obj, String... arg) {

    }
}
