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
package vtea.imageprocessing.builtin;

public class matrixsolve {

    public static float[][] mat_mult(float[][] a, float[][] b) {
        float[][] out = new float[a.length][a[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                for (int k = 0; k < a[0].length; k++) {
                    out[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return out;
    }

    public static float[] vec_mult(float[][] a, float[] b) {
        float[] out = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b.length; j++) {
                out[i] += a[i][j] * b[j];
            }
        }
        return out;
    }

    public static double[] vec_mult(double[][] a, double[] b) {
        double[] out = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b.length; j++) {
                out[i] += a[i][j] * b[j];
            }
        }
        return out;
    }

    public static float[][] transpose(float[][] a) {
        float[][] out = new float[a[0].length][a.length];
        for (int i = 0; i < a[0].length; i++) {
            for (int j = 0; j < a.length; j++) {
                out[i][j] = a[j][i];
            }
        }
        return out;
    }

    public static double[][] transpose(double[][] a) {
        double[][] out = new double[a[0].length][a.length];
        for (int i = 0; i < a[0].length; i++) {
            for (int j = 0; j < a.length; j++) {
                out[i][j] = a[j][i];
            }
        }
        return out;
    }

    public static float[][] conj_mult(float[][] a, float[][] b) {
        float[][] at = transpose(a);
        return mat_mult(at, b);
    }

    public static float trace(float[][] a) {
        int length = Math.min(a.length, a[0].length);
        float tr = 0.0f;
        for (int i = 0; i < length; i++) {
            tr += a[i][i];
        }
        return tr;
    }

    /*
        * This class contains matrix solving methods Copyright Jay Unruh Stowers
        * Institute for Medical Research 4/25/08
     */

    public void gjsolve(double[][] A, double[] b, double[] x, int size) {
        // here is a gauss jordan matrix equation solver
        // solves Ax=b for x
        double[][] gj = new double[size][size + 1];
        double[][] gj2 = new double[size][size + 1];
        double dumdouble;
        int pivrow, i, j, k;
        if (size == 1) { //heres the trivial 1 x 1 solution
            x[0] = b[0] / A[0][0];
            return;
        }
        if (size == 2) { //and the 2 x 2 solution
            double a1 = A[0][0], b1 = A[1][0], c1 = A[0][1], d1 = A[1][1];
            double factor = 1.0 / (a1 * d1 - b1 * c1);
            x[0] = factor * (d1 * b[0] - b1 * b[1]);
            x[1] = factor * (a1 * b[1] - c1 * b[0]);
            return;
        }
        if (size == 3) { //and the 3 x 3 solution
            double factor = A[0][0] * A[1][1] * A[2][2] - A[0][0] * A[1][2] * A[2][1] - A[0][1] * A[1][0] * A[2][2]
                    + A[0][1] * A[1][2] * A[2][0] + A[0][2] * A[1][0] * A[2][1] - A[0][2] * A[1][1] * A[2][0];
            factor = 1.0 / factor;
            x[0] = factor * (b[0] * (A[1][1] * A[2][2] - A[2][1] * A[1][2]) + b[1] * (A[0][2] * A[2][1] - A[2][2] * A[0][1]) + b[2] * (A[0][1] * A[1][2] - A[1][1] * A[0][2]));
            x[1] = factor * (b[0] * (A[1][2] * A[2][0] - A[2][2] * A[1][0]) + b[1] * (A[0][0] * A[2][2] - A[2][0] * A[0][2]) + b[2] * (A[0][2] * A[1][0] - A[1][2] * A[0][0]));
            x[2] = factor * (b[0] * (A[1][0] * A[2][1] - A[2][0] * A[1][1]) + b[1] * (A[0][1] * A[2][0] - A[2][1] * A[0][0]) + b[2] * (A[0][0] * A[1][1] - A[1][0] * A[0][1]));
            return;
        }
        // copy the A matrix to the left side of the gj matrix
        for (i = 0; i < size; i++) {
            for (j = 0; j < size; j++) {
                gj[i][j] = A[i][j];
            }
        }
        // copy the b array to the right side
        for (i = 0; i < size; i++) {
            gj[i][size] = b[i];
        }
        // now start the solve loop
        // the general inversion simply requires normalization of the pivot row
        // by the pivot element
        // and subsequent subtraction of the pivot row multiplied by a factor
        // from all other rows to
        // produce the identity matrix on the left side
        // in each step switch the pivot row with the row below it having the
        // highest absolute value
        // in the pivot column
        for (k = 0; k < size; k++) {
            // find the pivot row (look only below the former pivot row)
            dumdouble = gj[k][k];
            pivrow = k;
            for (i = k + 1; i < size; i++) {
                if (gj[i][k] > Math.abs(dumdouble)) {
                    dumdouble = gj[i][k];
                    pivrow = i;
                }
            }
            // now perform the inversion step into matrix gj2
            // first clear the gj2 matrix
            for (i = 0; i < size; i++) {
                for (j = 0; j < size + 1; j++) {
                    gj2[i][j] = 0.0;
                }
            }
            // normalize the pivot row by its element in the pivot column
            // note: have to check if the pivot element is zero
            for (i = 0; i < size + 1; i++) {
                if (gj[pivrow][k] == 0.0) {
                    if (gj[pivrow][i] <= 0.0) {
                        gj2[k][i] = -1.0;
                    } else {
                        gj[k][i] = 1.0;
                    }
                } else {
                    gj2[k][i] = gj[pivrow][i] / gj[pivrow][k];
                }
            }
            // now subtract the pivot row from the other rows
            // multiply it by pivot column in the subsequent rows to reproduce
            // the identity matrix
            // need to replace the pivot row with the row it replaced so do it
            // first (no need if the pivot row is k)
            // then loop over the other rows, skipping the replaced row and the
            // pivot row
            if (pivrow != k) {
                for (i = 0; i < size + 1; i++) {
                    gj2[pivrow][i] = gj[k][i] - gj2[k][i] * gj[k][k];
                }
            }
            for (i = 0; i < size; i++) {
                for (j = 0; j < size + 1; j++) {
                    if (i != k && i != pivrow) {
                        gj2[i][j] = gj[i][j] - gj2[k][j] * gj[i][k];
                    }
                }
            }
            // now replace the gj matrix with the updated gj2 matrix and repeat
            // for other columns
            for (i = 0; i < size; i++) {
                for (j = 0; j < size + 1; j++) {
                    gj[i][j] = gj2[i][j];
                }
            }
        }
        // finally fill the x vector with the rightmost column of the gj matrix
        for (i = 0; i < size; i++) {
            x[i] = gj[i][size];
        }
    }

    public double[][] gjinv2(double[][] A, int size) {
        // here is a gauss jordan matrix inverter
        double[][] gj = new double[size][size + size];
        double[][] gj2 = new double[size][size + size];
        double dumdouble;
        int pivrow, i, j, k;
        double[][] inv = new double[size][size];
        if (size == 1) { //heres the trivial 1 x 1 solution
            inv[0][0] = 1.0 / A[0][0];
            return inv;
        }
        if (size == 2) { //and the 2 x 2 solution
            double a1 = A[0][0], b1 = A[1][0], c1 = A[0][1], d1 = A[1][1];
            double factor = 1.0 / (a1 * d1 - b1 * c1);
            inv[0][0] = factor * d1;
            inv[0][1] = -factor * b1;
            inv[1][0] = -factor * c1;
            inv[1][1] = factor * a1;
            return inv;
        }
        if (size == 3) { //and the 3 x 3 solution
            double factor = A[0][0] * A[1][1] * A[2][2] - A[0][0] * A[1][2] * A[2][1] - A[0][1] * A[1][0] * A[2][2]
                    + A[0][1] * A[1][2] * A[2][0] + A[0][2] * A[1][0] * A[2][1] - A[0][2] * A[1][1] * A[2][0];
            factor = 1.0 / factor;
            inv[0][0] = factor * (A[1][1] * A[2][2] - A[2][1] * A[1][2]);
            inv[0][1] = factor * (A[0][2] * A[2][1] - A[2][2] * A[0][1]);
            inv[0][2] = factor * (A[0][1] * A[1][2] - A[1][1] * A[0][2]);
            inv[1][0] = factor * (A[1][2] * A[2][0] - A[2][2] * A[1][0]);
            inv[1][1] = factor * (A[0][0] * A[2][2] - A[2][0] * A[0][2]);
            inv[1][2] = factor * (A[0][2] * A[1][0] - A[1][2] * A[0][0]);
            inv[2][0] = factor * (A[1][0] * A[2][1] - A[2][0] * A[1][1]);
            inv[2][1] = factor * (A[0][1] * A[2][0] - A[2][1] * A[0][0]);
            inv[2][2] = factor * (A[0][0] * A[1][1] - A[1][0] * A[0][1]);
            return inv;
        }
        // copy the A matrix to the left side of the gj matrix
        for (i = 0; i < size; i++) {
            for (j = 0; j < size; j++) {
                gj[i][j] = A[i][j];
            }
        }
        // copy the b array to the right side
        for (i = 0; i < size; i++) {
            for (j = 0; j < size; j++) {
                if (j == i) {
                    gj[i][j + size] = 1.0;
                }
            }
        }
        // now start the solve loop
        // the general inversion simply requires normalization of the pivot row
        // by the pivot element
        // and subsequent subtraction of the pivot row multiplied by a factor
        // from all other rows to
        // produce the identity matrix on the left side
        // in each step switch the pivot row with the row below it having the
        // highest absolute value
        // in the pivot column
        for (k = 0; k < size; k++) {
            // find the pivot row (look only below the former pivot row)
            dumdouble = gj[k][k];
            pivrow = k;
            for (i = k + 1; i < size; i++) {
                if (gj[i][k] > Math.abs(dumdouble)) {
                    dumdouble = gj[i][k];
                    pivrow = i;
                }
            }
            // now perform the inversion step into matrix gj2
            // first clear the gj2 matrix
            for (i = 0; i < size; i++) {
                for (j = 0; j < size + size; j++) {
                    gj2[i][j] = 0.0;
                }
            }
            // normalize the pivot row by its element in the pivot column
            // note: have to check if the pivot element is zero
            for (i = 0; i < size + size; i++) {
                if (gj[pivrow][k] == 0.0) {
                    if (gj[pivrow][i] <= 0.0) {
                        gj2[k][i] = -1.0;
                    } else {
                        gj[k][i] = 1.0;
                    }
                } else {
                    gj2[k][i] = gj[pivrow][i] / gj[pivrow][k];
                }
            }
            // now subtract the pivot row from the other rows
            // multiply it by pivot column in the subsequent rows to reproduce
            // the identity matrix
            // need to replace the pivot row with the row it replaced so do it
            // first (no need if the pivot row is k)
            // then loop over the other rows, skipping the replaced row and the
            // pivot row
            if (pivrow != k) {
                for (i = 0; i < size + size; i++) {
                    gj2[pivrow][i] = gj[k][i] - gj2[k][i] * gj[k][k];
                }
            }
            for (i = 0; i < size; i++) {
                for (j = 0; j < size + size; j++) {
                    if (i != k && i != pivrow) {
                        gj2[i][j] = gj[i][j] - gj2[k][j] * gj[i][k];
                    }
                }
            }
            // now replace the gj matrix with the updated gj2 matrix and repeat
            // for other columns
            for (i = 0; i < size; i++) {
                for (j = 0; j < size + size; j++) {
                    gj[i][j] = gj2[i][j];
                }
            }
        }
        // finally fill the inv matrix with the rightmost columns of the gj matrix
        for (i = 0; i < size; i++) {
            for (j = 0; j < size; j++) {
                inv[i][j] = gj[i][j + size];
            }
        }
        return inv;
    }

    public double[][] gjinv(double[][] A, int size) {
        //this is rather inefficient (the above gjinv2 routine is better)
        double[][] inv = new double[size][size];
        for (int i = 0; i < size; i++) {
            double[] b = new double[size];
            b[i] = 1.0f;
            double[] x = new double[size];
            gjsolve(A, b, x, size);
            for (int j = 0; j < size; j++) {
                inv[j][i] = x[j];
            }
        }
        return inv;
    }

    //inverse of a complex matrix
    //create the following {re(a),im(a)},{-im(a),re(a)}
    //then the inverse gives {re(a^-1),im(a^-1)},{-im(a^-1),re(a^-1)}
    //the inverse is returned in the passed arrays
    public void compinv(double[][] Ar, double[][] Ai, int size) {
        double[][] temp = new double[size + size][size + size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                temp[i][j] = Ar[i][j];
                temp[i][j + size] = Ai[i][j];
                temp[i + size][j] = -Ai[i][j];
                temp[i + size][j + size] = Ar[i][j];
            }
        }
        double[][] inv = gjinv2(temp, size + size);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Ar[i][j] = inv[i][j];
                Ai[i][j] = inv[i][j + size];
            }
        }
    }

    //kind of like above for the inverse
    //solve (A+iC)(x+iy)=(b+id) for (x+iy)
    //convert to {{Ax-Cy},{Cx+Ay}}={b,d}
    //this becomes {{A,-C},{C,A}}*{x,y}={b,d}
    public void compsolve(double[][] Ar, double[][] Ai, double[] br, double[] bi, double[] xr, double[] xi, int size) {
        double[][] tempA = new double[size + size][size + size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                tempA[i][j] = Ar[i][j];
                tempA[i][j + size] = Ai[i][j];
                tempA[i + size][j] = -Ai[i][j];
                tempA[i + size][j + size] = Ar[i][j];
            }
        }
        double[] tempb = new double[size + size];
        for (int i = 0; i < size; i++) {
            tempb[i] = br[i];
            tempb[i + size] = bi[i];
        }
        double[] tempx = new double[size + size];
        gjsolve(tempA, tempb, tempx, size + size);
        for (int i = 0; i < size; i++) {
            xr[i] = tempx[i];
            xi[i] = tempx[i + size];
        }
    }

    public double[][][] compmat_mult(double[][] Ar, double[][] Ai, double[][] Br, double[][] Bi) {
        double[][] outr = new double[Ar.length][Ar[0].length];
        double[][] outi = new double[Ar.length][Ar[0].length];
        complex_math cm = new complex_math();
        for (int i = 0; i < Ar.length; i++) {
            for (int j = 0; j < Ar[0].length; j++) {
                for (int k = 0; k < Ar[0].length; k++) {
                    double[] temp = cm.complex_multiply(new double[]{Ar[i][k], Ai[i][k]}, new double[]{Br[k][j], Bi[k][j]});
                    outr[i][j] += temp[0];
                    outi[i][j] += temp[1];
                }
            }
        }
        return new double[][][]{outr, outi};
    }

}
