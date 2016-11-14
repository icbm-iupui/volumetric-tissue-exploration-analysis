/* 
 * Copyright (C) 2016 Indiana University
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
package MicroDeprecated;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author vinfrais
 */
import ij.text.*;
import ij.io.*;

import java.awt.*;

@Deprecated

public class microLog extends java.lang.Object {

    private static TextPanel logPanel;
    private String title;

    microLog(String title) {
        this.title = title;
    }

    public static synchronized void log(String s) {
        if (s == null) {
            return;
        }
        if (logPanel == null) {
            TextWindow logWindow = new TextWindow("Log", "", 400, 250);
            logPanel = logWindow.getTextPanel();
            logPanel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        }
        if (logPanel != null) {
            if (s.startsWith("\\")) {
            } //handleLogCommand(s);
            else {
                logPanel.append(s);
            }
        } else {
            LogStream.redirectSystem(false);
            System.out.println(s);
        }
    }

    public void setLogWindowPosition(int x, int y) {
    }

}
