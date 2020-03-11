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
package vtea;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.ListIterator;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.apache.commons.io.FilenameUtils;
import vteaobjects.MicroObject;

/**
 *
 * @author sethwinfree
 */
    public class ExportCSV {
        
        JComponent Main;

        public ExportCSV(JComponent Main) {
        }

        public void export(ArrayList<MicroObject> objects, 
                ArrayList<ArrayList<Number>> measurements, 
                ArrayList<String> descriptions) {
            File file;
            int returnVal = JFileChooser.CANCEL_OPTION;
            int choice = JOptionPane.OK_OPTION;
            do{
                JFileChooser jf = new JFileChooser(_vtea.LASTDIRECTORY);

                returnVal = jf.showSaveDialog(Main);

                file = jf.getSelectedFile();

                file = jf.getSelectedFile();
                if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("csv")) {

                } else {
                    file = new File(file.toString() + ".csv");
                }
                
                if(file.exists()){
                    String message = String.format("%s already exists\nOverwrite it?", file.getName());
                    choice = JOptionPane.showConfirmDialog(null, message ,"Overwrite File", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                }
            }while(choice != JOptionPane.OK_OPTION);
            if (returnVal == JFileChooser.APPROVE_OPTION) {

                try {

                    try {

                        PrintWriter pw = new PrintWriter(file);
                        StringBuilder sb = new StringBuilder();

                        ListIterator itr = descriptions.listIterator();

                        sb.append("Object");
                        sb.append(',');
                        sb.append("PosX,PosY,PosZ,");
                        while (itr.hasNext()) {
                            sb.append((String) itr.next());
                            if (itr.hasNext()) {
                                sb.append(',');
                            }
                        }

                        sb.append('\n');

                        for (int i = 0; i < objects.size(); i++) {

                            MicroObject volume = objects.get(i);
                            ArrayList<Number> measured = measurements.get(i);

                            sb.append(volume.getSerialID());
                            sb.append(',');
                            sb.append(volume.getCentroidX());
                            sb.append(',');
                            sb.append(volume.getCentroidY());
                            sb.append(',');
                            sb.append(volume.getCentroidZ());

                            ListIterator<Number> itr_mes = measured.listIterator();

                            while (itr_mes.hasNext()) {

                                sb.append(",");
                                sb.append(itr_mes.next());
                            }
                            sb.append('\n');
                        }

                        pw.write(sb.toString());
                        pw.close();

                    } catch (FileNotFoundException e) {
                    }

                } catch (NullPointerException ne) {
                }
                _vtea.LASTDIRECTORY =  file.getPath();
            } else {
            }

        }

    }

