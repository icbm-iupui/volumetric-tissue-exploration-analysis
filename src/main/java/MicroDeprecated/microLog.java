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
