/*
 * Copyright (C) 2017 SciJava
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
package vtear;

/**
 *
 * @author sethwinfree
 */
import java.util.Arrays;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
 
public class RServe {
 
    public RServe() throws RserveException,
            REXPMismatchException {
        RConnection engine = new RConnection();
        REXP x = engine.eval("R.version.string");
        System.out.println("Launching R with Rserve.");
        System.out.println(x.asString());
        
                 System.out.println("Running Rserve environment...");
    engine.eval("df <- data.frame(x=1:10, y=(1:10)+rnorm(n=10))");
    engine.eval("df");
    engine.eval("lm(y ~ x, df)");
   
    engine.eval("library('Rtsne')");
    System.out.println("Calculating Rtsne...");
    engine.eval("iris_unique <- unique(iris) # Remove duplicates");
    engine.eval("iris_matrix <- as.matrix(iris_unique[,1:4])");
    engine.eval("set.seed(42) # Set a seed if you want reproducible results");
    engine.eval("tsne_out <- Rtsne(iris_matrix)"); 

    System.out.println("Plotting...");
    //engine.eval("plot(tsne_out$Y,col=iris_unique$Species)");
    //REXP iris = engine.eval("iris_unique$Species");
    //System.out.println("Raw data: " + iris);
    //engine.eval("dev.off()");
    
    //double[][] test = iris.asDoubleMatrix();
    //System.out.println(test);
    //System.out.println(Arrays.asList(test));
    engine.eval("library(spade)");
    
    engine.eval("markers <- c(\"Cd(110,111,112,114)\",\"Cell_length\",\"Dy(163.929)-Dual\",\"Er(165.930)-Dual\",\"Er(166.932)-Dual\",\"Er(167.932)-Dual\",\"Er(169.935)-Dual\",\"Eu(150.919)-Dual\",\"Eu(152.921)-Dual\",\"Gd(155.922)-Dual\",\"Gd(157.924)-Dual\",\"Gd(159.927)-Dual\",\"Ho(164.930)-Dual\",\"In(114.903)-Dual\",\"Ir(190.960)-Dual\",\"La(138.906)-Dual\",\"Lu(174.940)-Dual\",\"Nd(141.907)-Dual\",\"Nd(143.910)-Dual\",\"Nd(144.912)-Dual\",\"Nd(145.913)-Dual\",\"Nd(147.916)-Dual\",\"Nd(149.920)-Dual\",\"Pr(140.907)-Dual\",\"Sm(146.914)-Dual\",\"Sm(151.919)-Dual\",\"Sm(153.922)-Dual\",\"Tb(158.925)-Dual\",\"Tm(168.934)-Dual\",\"Yb(170.936)-Dual\",\"Yb(171.936)-Dual\",\"Yb(173.938)-Dual\",\"Yb(175.942)-Dual\")");
    engine.eval("PANELS <- list(list(panel_files=c(\"Bendall_et_al_Science_2011_Marrow_1_SurfacePanel_Live_CD44pos_Singlets.fcs\"), median_cols=NULL,reference_files=c(\"Bendall_et_al_Science_2011_Marrow_1_SurfacePanel_Live_CD44pos_Singlets.fcs\"),fold_cols=c()))");
    engine.eval("SPADE.driver(\"Bendall_et_al_Science_2011_Marrow_1_SurfacePanel_Live_CD44pos_Singlets.fcs\", out_dir=\"output\", cluster_cols=markers, panels=PANELS, transforms=flowCore::arcsinhTransform(a=0, b=0.2), layout=SPADE.layout.arch, downsampling_target_percent=0.1, downsampling_target_number=NULL, downsampling_target_pctile=NULL, downsampling_exclude_pctile=0.01, k=200, clustering_samples=50000)");
    
    }
}
