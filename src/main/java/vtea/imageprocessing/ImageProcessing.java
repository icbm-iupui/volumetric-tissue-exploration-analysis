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
package vtea.imageprocessing;

import ij.ImagePlus;
import java.awt.Component;
import java.util.ArrayList;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import vtea.VTEAModule;

/**
 *
 * @author vinfrais
 */
public interface ImageProcessing<T extends Component, A extends RealType> extends VTEAModule {

    public boolean setOptions(ArrayList<T> al);

    public ArrayList<T> getOptions();

    public boolean process(ArrayList al, ImagePlus imp);

    public boolean process(ArrayList al, Img<A> img);

    public Img<A> getResult();

    public ImagePlus getImpResult();

    public Img<A> getPreview();

    public String getImageJMacroCommand();

    public String runImageJMacroCommand(String str);

    public String getVersion();

    public String getAuthor();

    public String getComment();

    public void sendProgressComment();

    public String getProgressComment();

    public boolean copyComponentParameter(String version, ArrayList<T> dComponents, ArrayList<T> sComponents);

    public boolean loadComponentParameter(String version, ArrayList<T> dComponents, ArrayList fields);

    public boolean saveComponentParameter(String version, ArrayList<T> dComponents, ArrayList fields);

}
