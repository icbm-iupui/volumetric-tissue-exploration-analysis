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
package vtea.renjin;

import java.io.File;
import javax.script.*;
import org.math.R.RenjinSession;
import org.math.R.Rsession;
import org.math.R.*;

import org.renjin.script.*;

// ... add additional imports here ...

public class TestRenjin {
  public boolean process() throws Exception {
    // create a script engine manager:
    RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
//    // create a Renjin engine:
    ScriptEngine engine = factory.getScriptEngine();
////    
engine.eval("library(vioplot)");

engine.eval("mu<-2");
engine.eval("si<-0.6");
engine.eval("bimodal<-c(rnorm(1000,-mu,si),rnorm(1000,mu,si))");
engine.eval("uniform<-runif(2000,-4,4)");
engine.eval("normal<-rnorm(2000,0,3)");
engine.eval("pdf('violin.pdf')");
engine.eval("vioplot(bimodal,uniform,normal)");
engine.eval("dev.off()");
////engine.eval("boxplot(bimodal,uniform,normal)");
//




//engine.eval("df <- data.frame(x = 1:10, y = rnorm(n = 10))");
//engine.eval("print(lm(y ~ x, df))");
//    // ... put your Java code here ...
engine.eval("library(ggplot2)");
engine.eval("df <- data.frame(x=1:10, y=(1:10)+rnorm(n=10))");
engine.eval("print(df)");
engine.eval("print(lm(y ~ x, df))");
engine.eval("p <- ggplot(df, aes(y,x)) + geom_violin()");
engine.eval("pdf('test.pdf')");
engine.eval("print(p)");
//engine.eval("png('violin.png')");
//engine.eval("print(p)");
engine.eval("dev.off()");



Rsession r = new RenjinSession(System.out, null);

        //double[] rand = (double[]) r.eval("rnorm(10)"); //create java variable from R command

        //...
        r.set("c", Math.random()); //create R variable from java one

        r.save(new File("save.Rdata"), "c"); //save variables in .Rdata
        //r.rm("c"); //delete variable in R environment
        //r.load(new File("save.Rdata")); //load R variable from .Rdata

        //...
        //r.set("df", new double[][]{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}, {10, 11, 12}}, "x1", "x2", "x3"); //create data frame from given vectors
        //double value = (double) (r.eval("df$x1[3]")); //access one value in data frame
         File file = new File("plot.png");
         file.createNewFile();
        //...
        r.toPNG(file, 400, 400, "plot(rnorm(10))"); //create jpeg file from R graphical command (like plot)

        //String html = r.asHTML("summary(rnorm(100))"); //format in html using R2HTML
        //System.out.println(html);

        //String txt = r.asString("summary(rnorm(100))"); //format in text
        //System.out.println(txt);

        //...
        //System.out.println(r.installPackage("sensitivity", true)); //install and load R package
        //System.out.println(r.installPackage("DiceKriging", true));

        r.end();
        
        return true;
  }
}
