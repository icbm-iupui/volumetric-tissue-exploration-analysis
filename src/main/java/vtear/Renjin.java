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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.*;
import org.renjin.script.*;

// ... add additional imports here ...

public class Renjin {
 public Renjin(){
     try {
         // create a script engine manager:
         RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
         // create a Renjin engine:
         ScriptEngine engine = factory.getScriptEngine();
         
         // ... put your Java code here ...
         
         System.out.println("Running R environment...");
         engine.eval("df <- data.frame(x=1:10, y=(1:10)+rnorm(n=10))");
         engine.eval("print(df)");
         engine.eval("print(lm(y ~ x, df))");
         engine.eval("plot(df)");
         
//          engine.eval("library(devtools)");
//          engine.eval("devtools::install_github('nolanlab/Rclusterpp')");
//          engine.eval("source('http://bioconductor.org/biocLite.R')");
//          engine.eval("devtools::install_github('nolanlab/spade'')");
    
    //engine.eval("install.packages('Rtsne')");
 //   engine.eval("library('Rtsne')");

//    engine.eval("iris_unique <- unique(iris) # Remove duplicates");
 //   engine.eval("iris_matrix <- as.matrix(iris_unique[,1:4])");
 //   engine.eval("set.seed(42) # Set a seed if you want reproducible results");
//    engine.eval("tsne_out <- Rtsne(iris_matrix)"); 
//    # Show the objects in the 2D tsne representation
//    engine.eval("plot(tsne_out$Y,col=iris_unique$Species)");
//    # Using a dist object
//    engine.eval("tsne_out <- Rtsne(dist(iris_matrix))");
 //   engine.eval("plot(tsne_out$Y,col=iris_unique$Species)");
//    # Use a given initialization of the locations of the points
//   tsne_part1 <- Rtsne(iris_unique[,1:4], theta=0.0, pca=FALSE,max_iter=350)
//   tsne_part2 <- Rtsne(iris_unique[,1:4], theta=0.0, pca=FALSE, max_iter=150,Y_init=tsne_part1$Y)

 
     } catch (ScriptException ex) {
         Logger.getLogger(Renjin.class.getName()).log(Level.SEVERE, null, ex);
     }
    
  }
}
