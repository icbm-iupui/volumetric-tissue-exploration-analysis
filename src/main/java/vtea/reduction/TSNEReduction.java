/*
 * Copyright (C) 2018 SciJava
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
package vtea.reduction;

import com.jujutsu.tsne.TSneConfiguration;
import com.jujutsu.tsne.barneshut.BHTSne;
import com.jujutsu.tsne.barneshut.BarnesHutTSne;
import com.jujutsu.utils.TSneUtils;
import ij.IJ;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.scijava.plugin.Plugin;
import vtea.featureprocessing.AbstractFeatureProcessing;
import vtea.featureprocessing.FeatureProcessing;

/**
 *
 * @author drewmcnutt
 */
@Plugin (type = FeatureProcessing.class)
public class TSNEReduction extends AbstractFeatureProcessing{
    public TSNEReduction(){
        VERSION = "0.1";
        AUTHOR = "Andrew McNutt";
        COMMENT = "Implements the plugin from Leif Jonsson";
        NAME = "tSNE";
        KEY = "tSNE";
        TYPE = "Reduction";
    }
    
    public TSNEReduction(int max){
        this();
        protocol = new ArrayList();
        
        protocol.add(new JLabel("New Dimension"));
        protocol.add(new JTextField("2",2));
        
        protocol.add( new JLabel("Iterations"));
        protocol.add( new JTextField("1000",4));
        
        //protocol.add(new JLabel("Learning Rate"));
        //protocol.add(new JTextField("200",200));
        
        protocol.add(new JLabel("Perplexity"));
        protocol.add(new JTextField("20",20));
        JCheckBox pca = new JCheckBox("PCA Preprocessing");
        JTextField jtf = new JTextField(Integer.toString(max),2);
        pca.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent ie){
                jtf.setEditable(true);
            }
        });
        protocol.add(pca);
        protocol.add(new JLabel("Input Dimensions"));
        jtf.setEditable(false);
        protocol.add(jtf);
    }
    
    @Override
    public boolean process(ArrayList al, double[][] feature){
        int outDim = Integer.parseInt(((JTextField)al.get(5)).getText());
        int itr = Integer.parseInt(((JTextField)al.get(7)).getText());
        int perpl = Integer.parseInt(((JTextField)al.get(9)).getText());
        boolean pca = ((JCheckBox)al.get(10)).isSelected();
        int inDim = Integer.parseInt(((JTextField)al.get(12)).getText());
        
        ArrayList selectData = (ArrayList)al.get(1);
        boolean znorm = (boolean)al.get(0);
        feature = selectColumns(feature, selectData);
        feature = normalizeColumns(feature, znorm);
        
        IJ.log("PROFILING: Training tSNE for " + itr + " iterations");
        long start = System.nanoTime();
        BarnesHutTSne tsne = new BHTSne();
        TSneConfiguration config = TSneUtils.buildConfig(feature,outDim,inDim,perpl,itr, pca, 0.5, false);
        double[][] Y = tsne.tsne(config);
        
        IJ.log("PROFILING: Extracting results");
        
        for(int i = 0; i < Y.length; i++){
            ArrayList obj = new ArrayList();
            for(int j = 0; j < Y[i].length; j++){
                obj.add(Y[i][j]);
            }
            dataResult.add(obj);
        }
        
        long end = System.nanoTime();
        IJ.log("PROFILING: tSNE completed in " + (end-start)/1000000 + " ms" );
        
        
        return true;
    }
    
    public static String getBlockComment(ArrayList comComponents){
        String comment = "<html>";
        comment = comment.concat(((JLabel)comComponents.get(4)).getText() + ": ");
        comment = comment.concat(((JTextField)comComponents.get(5)).getText() + ", ");
        comment = comment.concat(((JLabel)comComponents.get(6)).getText() + ": ");
        comment = comment.concat(((JTextField)comComponents.get(7)).getText() + ", ");
        comment = comment.concat(((JLabel)comComponents.get(8)).getText() + ": ");
        comment = comment.concat(((JTextField)comComponents.get(9)).getText() + ", ");
        comment = comment.concat(((JCheckBox)comComponents.get(10)).getText());
        boolean pca = ((JCheckBox)comComponents.get(10)).isSelected();
        comment = comment.concat(pca? ": Enabled" : ": Disabled");
        if(pca){
            comment = comment.concat(((JLabel)comComponents.get(11)).getText() + ": ");
            comment = comment.concat(((JTextField)comComponents.get(12)).getText());
        }
        comment = comment.concat("</html>");
        return comment;
    }
}
