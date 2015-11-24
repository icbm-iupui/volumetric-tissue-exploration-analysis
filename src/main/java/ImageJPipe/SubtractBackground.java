/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageJPipe;

/**
 *
 * @author winfrees
 */
public class SubtractBackground extends Object {

    public double radius;
    public boolean LightBackground;
    public boolean SlidingParaboloid;
    public boolean DisableSmoothing;
    public String macroText;

    private String lbtext = "";
    private String sptext = "";
    private String dstext = "";

    public SubtractBackground(double r, boolean lb, boolean sp, boolean ds, String stacktext) {
        radius = r;
        LightBackground = lb;
        if (lb == true) {
            lbtext = "light ";
        }
        SlidingParaboloid = sp;
        if (sp == true) {
            lbtext = "sliding ";
        }
        DisableSmoothing = ds;
        if (ds == true) {
            lbtext = "disable ";
        }

        macroText = "run(\"Subtract Background...\", \"rolling=" + radius + " " + lbtext + sptext + dstext + stacktext;
    }

    public String getMacroText() {
        return macroText;
    }
}
