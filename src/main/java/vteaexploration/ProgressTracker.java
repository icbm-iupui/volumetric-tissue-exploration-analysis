/*
 * Copyright (C) 2020 SciJava
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
package vteaexploration;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import static vteaexploration.MicroExplorer.YAXIS;

/**
 *
 * @author sethwinfree
 */
class ProgressTracker extends JPanel {
       
    JProgressBar jpb = new JProgressBar();
    JFrame jf = new JFrame();
    
    public ProgressTracker(){
        super();
        jpb.setMaximum(100);
        jpb.setMinimum(100);
        this.add(jpb);
    }
    

    
    public void createandshowGUI(String str, int x, int y){
               
        
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.add(new JLabel(str));
        this.add(jpb);
        JComponent newContentPane = this;
        newContentPane.setOpaque(true); //content panes must be opaque
        jf.setContentPane(newContentPane);

        jf.setLocation(x, y);
        jf.pack();
        jf.setVisible(true);
    }
    
    public void setPercentProgress(int i){
        jpb.setValue(i);
    }
    
    public void setVisible(boolean b){
        jf.setVisible(b);
    }
    
   public void setPosition(int x, int y){
        jf.setLocation(x, y);
    }
    
    
}
