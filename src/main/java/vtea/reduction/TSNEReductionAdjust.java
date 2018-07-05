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


import ij.IJ;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.scijava.plugin.Plugin;
import com.jujutsu.tsne.barneshut.BHTSne;
import com.jujutsu.tsne.barneshut.BarnesHutTSne;
import com.jujutsu.utils.TSneUtils;
import com.jujutsu.tsne.PrincipalComponentAnalysis;
import com.jujutsu.tsne.TSneConfiguration;
import com.jujutsu.tsne.barneshut.DataPoint;
import com.jujutsu.tsne.barneshut.Distance;
import com.jujutsu.tsne.barneshut.EuclideanDistance;
import com.jujutsu.tsne.barneshut.VpTree;
import com.jujutsu.utils.MatrixOps;
import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;
import java.util.List;
import java.util.Random;
import javax.swing.JCheckBox;
import vtea.featureprocessing.AbstractFeatureProcessing;
import vtea.featureprocessing.FeatureProcessing;

/**
 *
 * @author drewmcnutt
 */
@Plugin (type = FeatureProcessing.class)
public class TSNEReductionAdjust extends AbstractFeatureProcessing{
    protected final Distance distance = new EuclideanDistance();
    protected volatile boolean abort = false;
    
    public TSNEReductionAdjust(){
        VERSION = "0.1";
        AUTHOR = "Andrew McNutt";
        COMMENT = "Implements the plugin from Leif Jonsson";
        NAME = "tSNE Adjustable";
        KEY = "tSNE";
        TYPE = "Reduction";
    }
    
    public TSNEReductionAdjust(int n){
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
        
        protocol.add(new JLabel("PCA Preprocessing"));
        JCheckBox pca = new JCheckBox();
        JTextField jtf = new JTextField(null,2);
        JLabel inputD = new JLabel("Input Dimensions");
        pca.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent ie){
                if(jtf.isEditable()){
                    jtf.setForeground(Color.LIGHT_GRAY);
                    inputD.setForeground(Color.LIGHT_GRAY);
                    jtf.setEditable(false);
                }else{
                    jtf.setForeground(Color.BLACK);
                    inputD.setForeground(Color.BLACK);
                    jtf.setEditable(true);
                }
                
            }
        });
        protocol.add(pca);
        inputD.setForeground(Color.LIGHT_GRAY);
        protocol.add(inputD);
        jtf.setForeground(Color.LIGHT_GRAY);
        protocol.add(jtf);
    }
    
    @Override
    public boolean process(ArrayList al, double[][] feature){
        
        int outDim = Integer.parseInt(((JTextField)al.get(5)).getText());
        int itr = Integer.parseInt(((JTextField)al.get(7)).getText());
        int perpl = Integer.parseInt(((JTextField)al.get(9)).getText());
        boolean pca = ((JCheckBox)al.get(11)).isSelected();
        String inD = ((JTextField)al.get(13)).getText();
        int inDim = pca? (inD.equals("") ? feature[0].length : Integer.parseInt(inD)) : feature[0].length;
        
        ArrayList selectData = (ArrayList)al.get(1);
        boolean znorm = (boolean)al.get(0);
        feature = selectColumns(feature, selectData);
        feature = normalizeColumns(feature, znorm);
        
        IJ.log("PROFILING: Training tSNE for " + itr + " iterations");
        long start = System.nanoTime();
        TSneConfiguration config = TSneUtils.buildConfig(feature,outDim,inDim,perpl,itr, pca, 0.5, false);
        double[][] Y = run(config, 5);
        
        
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
        comment = comment.concat(((JLabel)comComponents.get(10)).getText());
        boolean pca = ((JCheckBox)comComponents.get(11)).isSelected();
        comment = comment.concat(pca? ": Enabled" : ": Disabled");
        if(pca){
            comment = comment.concat(((JLabel)comComponents.get(12)).getText() + ": ");
            comment = comment.concat(((JTextField)comComponents.get(13)).getText());
        }
        comment = comment.concat("</html>");
        return comment;
    }
    
    private double[][] run(TSneConfiguration parameterObject, int randSeed){
        int D = parameterObject.getXStartDim();
        double[][] Xin = parameterObject.getXin();
        boolean exact = (parameterObject.getTheta() == .0);

        if(exact) throw new IllegalArgumentException("The Barnes Hut implementation does not support exact inference yet (theta==0.0), if you want exact t-SNE please use one of the standard t-SNE implementations (FastTSne for instance)");

        if(parameterObject.usePca() && D > parameterObject.getInitialDims() && parameterObject.getInitialDims() > 0) {
                PrincipalComponentAnalysis pca = new PrincipalComponentAnalysis();
                Xin = pca.pca(Xin, parameterObject.getInitialDims());
                D = parameterObject.getInitialDims();
                IJ.log("X:Shape after PCA is = " + Xin.length + " x " + Xin[0].length);
        }

        double [] X = flatten(Xin);	
        int N = parameterObject.getNrRows();
        int no_dims = parameterObject.getOutputDims();

        double [] Y = new double[N*no_dims];
        IJ.log("X:Shape is = " + N + " x " + D);
        // Determine whether we are using an exact algorithm
        double perplexity = parameterObject.getPerplexity();
        if(N - 1 < 3 * perplexity) { throw new IllegalArgumentException("Perplexity too large for the number of data points!\n"); }
        IJ.log("Using no_dims = " + no_dims + ", perplexity = " + perplexity + ", and theta = " + parameterObject.getTheta());

        // Set learning parameters
        double total_time = 0;
        int stop_lying_iter = 250, mom_switch_iter = 250;
        double momentum = .5, final_momentum = .8;
        double eta = 200.0;

        // Allocate some memory
        double [] dY    = new double[N * no_dims];
        double [] uY    = new double[N * no_dims];
        double [] gains = new double[N * no_dims];
        for(int i = 0; i < N * no_dims; i++) gains[i] = 1.0;

        // Normalize input data (to prevent numerical problems)
        IJ.log("Computing input similarities...");
        long start = System.currentTimeMillis();
        //zeroMean(X, N, D);
        double max_X = .0;
        for(int i = 0; i < N * D; i++) {
                if(X[i] > max_X) max_X = X[i];
        }

        for(int i = 0; i < N * D; i++) X[i] /= max_X;

        double [] P = null;
        int K  = (int) (3 * perplexity);
        int [] row_P = new int[N+1];
        int [] col_P = new int[N*K];
        double [] val_P = new double[N*K];
        /**_row_P = (int*)    malloc((N + 1) * sizeof(int));
         *_col_P = (int*)    calloc(N * K, sizeof(int));
         *_val_P = (double*) calloc(N * K, sizeof(double));*/
        // Compute input similarities for exact t-SNE
        if(exact) {

                // Compute similarities
                P = new double[N * N];
                computeGaussianPerplexity(X, N, D, P, perplexity);

                // Symmetrize input similarities
                IJ.log("Symmetrizing...");
                int nN = 0;
                for(int n = 0; n < N; n++) {
                        int mN = 0;
                        for(int m = n + 1; m < N; m++) {
                                P[nN + m] += P[mN + n];
                                P[mN + n]  = P[nN + m];
                                mN += N;
                        }
                        nN += N;
                }
                double sum_P = .0;
                for(int i = 0; i < N * N; i++) sum_P += P[i];
                for(int i = 0; i < N * N; i++) P[i] /= sum_P;
        }

        // Compute input similarities for approximate t-SNE
        else {

                // Compute asymmetric pairwise input similarities
                computeGaussianPerplexity(X, N, D, row_P, col_P, val_P, perplexity, K);

                // Verified that val_P,col_P,row_P is the same at this point

                // Symmetrize input similarities
                SymResult res = symmetrizeMatrix(row_P, col_P, val_P, N);
                row_P = res.sym_row_P;
                col_P = res.sym_col_P;
                val_P = res.sym_val_P;

                double sum_P = .0;
                for(int i = 0; i < row_P[N]; i++) sum_P += val_P[i];
                for(int i = 0; i < row_P[N]; i++) val_P[i] /= sum_P;
        }
        long end = System.currentTimeMillis();

        // Lie about the P-values
        if(exact) { for(int i = 0; i < N * N; i++)        P[i] *= 12.0; }
        else {      for(int i = 0; i < row_P[N]; i++) val_P[i] *= 12.0; }

        Random randGen = new Random(randSeed);
        // Initialize solution (randomly)
        for(int i = 0; i < N * no_dims; i++) Y[i] = randGen.nextDouble() * 0.0001;

        // Perform main training loop
        if(exact) IJ.log("Done in " + IJ.d2s((end - start) / 1000.0, 2) +  " seconds!\nLearning embedding...\n");
        else IJ.log("Done in " + IJ.d2s((end - start) / 1000.0, 2) +  " seconds (sparsity = " + IJ.d2s((double) row_P[N] / ((double) N * (double) N) , 2) +")!\nLearning embedding...\n");
        start = System.currentTimeMillis();
        for(int iter = 0; iter < parameterObject.getMaxIter() && !abort; iter++) {

                /*if(exact) computeExactGradient(P, Y, N, no_dims, dY);
                // Compute (approximate) gradient
                else*/ computeGradient(P, row_P, col_P, val_P, Y, N, no_dims, dY, parameterObject.getTheta());

                updateGradient(N, no_dims, Y, momentum, eta, dY, uY, gains);

                // Make solution zero-mean
                zeroMean(Y, N, no_dims);

                // Stop lying about the P-values after a while, and switch momentum
                if(iter == stop_lying_iter) {
                        if(exact) { for(int i = 0; i < N * N; i++)        P[i] /= 12.0; }
                        else      { for(int i = 0; i < row_P[N]; i++) val_P[i] /= 12.0; }
                }
                if(iter == mom_switch_iter) momentum = final_momentum;

                // Print out progress
                if(((iter > 0 && iter % 50 == 0) || iter == parameterObject.getMaxIter() - 1) && !parameterObject.silent() ) {
                        end = System.currentTimeMillis();
                        String err_string = "not_calculated";
                        if(parameterObject.printError()) {
                                double C = .0;
                                /*if(exact) C = evaluateError(P, Y, N, no_dims);
                                else */     C = evaluateError(row_P, col_P, val_P, Y, N, no_dims, parameterObject.getTheta());  // doing approximate computation here!
                                err_string = "" + C;
                        }
                        if(iter == 0)
                                IJ.log("Iteration " + (iter + 1) + ": error is " + err_string);
                        else {
                                total_time += (end - start) / 1000.0;
                                IJ.log("Iteration " + iter + ": error is " + err_string + " (50 iterations in " + IJ.d2s((end - start) / 1000.0, 2) + " seconds)");
                        }
                        start = System.currentTimeMillis();
                }
        }
        end = System.currentTimeMillis(); total_time += (end - start) / 1000.0;

        IJ.log("Fitting performed in " + IJ.d2s(total_time) + " seconds");
        return expand(Y,N,no_dims);
    }
    
    private double[] flatten(double[][] x){
        int noCols = x[0].length;
            double [] flat = new double[x.length*x[0].length];
            for (int i = 0; i < x.length; i++) {
                    for (int j = 0; j < x[i].length; j++) {
                            flat[i*noCols+j] = x[i][j];
                    }
            }
        return flat;
    }

    private void computeGaussianPerplexity(double [] X, int N, int D, double [] P, double perplexity){
        // Compute the squared Euclidean distance matrix
        double [] DD = new double[N * N];
        computeSquaredEuclideanDistance(X, N, D, DD);

        // Compute the Gaussian kernel row by row
        int nN = 0;
        for(int n = 0; n < N; n++) {

                // Initialize some variables
                boolean found = false;
                double beta = 1.0;
                double min_beta = -Double.MAX_VALUE;
                double max_beta =  Double.MAX_VALUE;
                double tol = 1e-5;
                double sum_P = Double.MIN_VALUE;

                // Iterate until we found a good perplexity
                int iter = 0;
                while(!found && iter < 200) {

                        // Compute Gaussian kernel row
                        for(int m = 0; m < N; m++) P[nN + m] = exp(-beta * DD[nN + m]);
                        P[nN + n] = Double.MIN_VALUE;

                        // Compute entropy of current row
                        sum_P = Double.MIN_VALUE;
                        for(int m = 0; m < N; m++) sum_P += P[nN + m];
                        double H = 0.0;
                        for(int m = 0; m < N; m++) H += beta * (DD[nN + m] * P[nN + m]);
                        H = (H / sum_P) + log(sum_P);

                        // Evaluate whether the entropy is within the tolerance level
                        double Hdiff = H - log(perplexity);
                        if(Hdiff < tol && -Hdiff < tol) {
                                found = true;
                        }
                        else {
                                if(Hdiff > 0) {
                                        min_beta = beta;
                                        if(max_beta == Double.MAX_VALUE || max_beta == -Double.MAX_VALUE)
                                                beta *= 2.0;
                                        else
                                                beta = (beta + max_beta) / 2.0;
                                }
                                else {
                                        max_beta = beta;
                                        if(min_beta == -Double.MAX_VALUE || min_beta == Double.MAX_VALUE)
                                                beta /= 2.0;
                                        else
                                                beta = (beta + min_beta) / 2.0;
                                }
                        }

                        // Update iteration counter
                        iter++;
                }

                // Row normalize P
                for(int m = 0; m < N; m++) P[nN + m] /= sum_P;
                nN += N;
        }
    }
    
    private void computeGaussianPerplexity(double [] X, int N, int D, int [] _row_P, int [] _col_P, double [] _val_P, double perplexity, int K){
        if(perplexity > K) IJ.log("Perplexity should be lower than K!");

            // Allocate the memory we need
            /**_row_P = (int*)    malloc((N + 1) * sizeof(int));
             *_col_P = (int*)    calloc(N * K, sizeof(int));
             *_val_P = (double*) calloc(N * K, sizeof(double));
                if(*_row_P == null || *_col_P == null || *_val_P == null) { Rcpp::stop("Memory allocation failed!\n"); }*/
            int [] row_P = _row_P;
            int [] col_P = _col_P;
            double [] val_P = _val_P;
            double [] cur_P = new double[N - 1];

            row_P[0] = 0;
            for(int n = 0; n < N; n++) row_P[n + 1] = row_P[n] + K;    

            // Build ball tree on data set
            VpTree<DataPoint> tree = new VpTree<DataPoint>(distance);
            final DataPoint [] obj_X = new DataPoint [N];
            for(int n = 0; n < N; n++) {
                    double [] row = MatrixOps.extractRowFromFlatMatrix(X,n,D);
                    obj_X[n] = new DataPoint(D, n, row);
            }
            tree.create(obj_X);

            // VERIFIED THAT TREES LOOK THE SAME
            //System.out.println("Created Tree is: ");
            //			AdditionalInfoProvider pp = new AdditionalInfoProvider() {			
            //				@Override
            //				public String provideInfo(Node node) {
            //					return "" + obj_X[node.index].index();
            //				}
            //			};
            //			TreePrinter printer = new TreePrinter(pp);
            //			printer.printTreeHorizontal(tree.getRoot());

            // Loop over all points to find nearest neighbors
            IJ.log("Building tree...");
            List<DataPoint> indices = new ArrayList<>();
            List<Double> distances = new ArrayList<>();
            for(int n = 0; n < N; n++) {

                    if(n % 10000 == 0) IJ.log(" - point " + n + " of " + N);

                    // Find nearest neighbors
                    indices.clear();
                    distances.clear();
                    //System.out.println("Looking at: " + obj_X.get(n).index());
                    tree.search(obj_X[n], K + 1, indices, distances);

                    // Initialize some variables for binary search
                    boolean found = false;
                    double beta = 1.0;
                    double min_beta = -Double.MAX_VALUE;
                    double max_beta =  Double.MAX_VALUE;
                    double tol = 1e-5;

                    // Iterate until we found a good perplexity
                    int iter = 0; 
                    double sum_P = 0.;
                    while(!found && iter < 200) {

                            // Compute Gaussian kernel row and entropy of current row
                            sum_P = Double.MIN_VALUE;
                            double H = .0;
                            for(int m = 0; m < K; m++) {
                                    cur_P[m] = exp(-beta * distances.get(m + 1));
                                    sum_P += cur_P[m];
                                    H += beta * (distances.get(m + 1) * cur_P[m]);
                            }
                            H = (H / sum_P) + log(sum_P);

                            // Evaluate whether the entropy is within the tolerance level
                            double Hdiff = H - log(perplexity);
                            if(Hdiff < tol && -Hdiff < tol) {
                                    found = true;
                            }
                            else {
                                    if(Hdiff > 0) {
                                            min_beta = beta;
                                            if(max_beta == Double.MAX_VALUE || max_beta == -Double.MAX_VALUE)
                                                    beta *= 2.0;
                                            else
                                                    beta = (beta + max_beta) / 2.0;
                                    }
                                    else {
                                            max_beta = beta;
                                            if(min_beta == -Double.MAX_VALUE || min_beta == Double.MAX_VALUE)
                                                    beta /= 2.0;
                                            else
                                                    beta = (beta + min_beta) / 2.0;
                                    }
                            }

                            // Update iteration counter
                            iter++;
                    }

                    // Row-normalize current row of P and store in matrix 
                    for(int m = 0; m < K; m++) {
                            cur_P[m] /= sum_P;
                            col_P[row_P[n] + m] = indices.get(m + 1).index();
                            val_P[row_P[n] + m] = cur_P[m];
                    }
            }
    }
    
    private void computeSquaredEuclideanDistance(double [] X, int N, int D, double [] DD){
        double [] dataSums = new double[N];
            for(int n = 0; n < N; n++) {
                    for(int d = 0; d < D; d++) {
                            dataSums[n] += (X[n * D + d] * X[n * D + d]);
                    }
            }
            for(int n = 0; n < N; n++) {
                    for(int m = 0; m < N; m++) {
                            DD[n * N + m] = dataSums[n] + dataSums[m];
                    }
            }
    }
    
    private double [][] expand(double[]x, int N, int D) {
		double [][] expanded = new double[N][D];
		for (int row = 0; row < N; row++) {
			for (int col = 0; col < D; col++) {
				expanded[row][col] = x[row*D+col];
			}
		}
		return expanded;
    }
    
    class SymResult{
        int []    sym_row_P;
        int []    sym_col_P;
        double [] sym_val_P;

        public SymResult(int[] sym_row_P, int[] sym_col_P, double[] sym_val_P) {
                super();
                this.sym_row_P = sym_row_P;
                this.sym_col_P = sym_col_P;
                this.sym_val_P = sym_val_P;
        }
    }
    
    SymResult symmetrizeMatrix(int [] _row_P, int [] _col_P, double [] _val_P, int N) {

        // Get sparse matrix
        int [] row_P = _row_P;
        int [] col_P = _col_P;
        double [] val_P = _val_P;

        // Count number of elements and row counts of symmetric matrix
        int [] row_counts = new int[N];
        for(int n = 0; n < N; n++) {
                for(int i = row_P[n]; i < row_P[n + 1]; i++) {

                        // Check whether element (col_P[i], n) is present
                        boolean present = false;
                        for(int m = row_P[col_P[i]]; m < row_P[col_P[i] + 1]; m++) {
                                if(col_P[m] == n) present = true;
                        }
                        if(present) row_counts[n]++;
                        else {
                                row_counts[n]++;
                                row_counts[col_P[i]]++;
                        }
                }
        }
        int no_elem = 0;
        for(int n = 0; n < N; n++) no_elem += row_counts[n];

        // Allocate memory for symmetrized matrix
        int []    sym_row_P = new int[N + 1];
        int []    sym_col_P = new int[no_elem];
        double [] sym_val_P = new double[no_elem];

        // Construct new row indices for symmetric matrix
        sym_row_P[0] = 0;
        for(int n = 0; n < N; n++) sym_row_P[n + 1] = sym_row_P[n] + row_counts[n];

        // Fill the result matrix
        int [] offset = new int[N];
        for(int n = 0; n < N; n++) {
                for(int i = row_P[n]; i < row_P[n + 1]; i++) {                                  // considering element(n, col_P[i])

                        // Check whether element (col_P[i], n) is present
                        boolean present = false;
                        for(int m = row_P[col_P[i]]; m < row_P[col_P[i] + 1]; m++) {
                                if(col_P[m] == n) {
                                        present = true;
                                        if(n <= col_P[i]) {                                                 // make sure we do not add elements twice
                                                sym_col_P[sym_row_P[n]        + offset[n]]        = col_P[i];
                                                sym_col_P[sym_row_P[col_P[i]] + offset[col_P[i]]] = n;
                                                sym_val_P[sym_row_P[n]        + offset[n]]        = val_P[i] + val_P[m];
                                                sym_val_P[sym_row_P[col_P[i]] + offset[col_P[i]]] = val_P[i] + val_P[m];
                                        }
                                }
                        }

                        // If (col_P[i], n) is not present, there is no addition involved
                        if(!present) {
                                sym_col_P[sym_row_P[n]        + offset[n]]        = col_P[i];
                                sym_col_P[sym_row_P[col_P[i]] + offset[col_P[i]]] = n;
                                sym_val_P[sym_row_P[n]        + offset[n]]        = val_P[i];
                                sym_val_P[sym_row_P[col_P[i]] + offset[col_P[i]]] = val_P[i];
                        }

                        // Update offsets
                        if(!present || (present && n <= col_P[i])) {
                                offset[n]++;
                                if(col_P[i] != n) offset[col_P[i]]++;               
                        }
                }
        }

        // Divide the result by two
        for(int i = 0; i < no_elem; i++) sym_val_P[i] /= 2.0;

        return new SymResult(sym_row_P, sym_col_P, sym_val_P);
    }

    private void computeGradient(double [] P, int [] inp_row_P, int [] inp_col_P, double [] inp_val_P, double [] Y, int N, int D, double [] dC, double theta){
        // Construct space-partitioning tree on current map
        SPTree tree = new SPTree(D, Y, N);

        // Compute all terms required for t-SNE gradient
        double [] sum_Q = new double[1];
        double [] pos_f = new double[N * D];
        double [][] neg_f = new double[N][D];

        tree.computeEdgeForces(inp_row_P, inp_col_P, inp_val_P, N, pos_f);
        for(int n = 0; n < N; n++) tree.computeNonEdgeForces(n, theta, neg_f[n], sum_Q);

        // Compute final t-SNE gradient
        for(int n = 0; n < N; n++) {
                for(int d = 0; d < D; d++) {
                        dC[n*D+d] = pos_f[n*D+d] - (neg_f[n][d] / sum_Q[0]);
                }
        }
    }

    private void updateGradient(int N, int no_dims, double[] Y, double momentum, double eta, double[] dY, double[] uY,
double[] gains){
        for(int i = 0; i < N * no_dims; i++)  {
            // Update gains
            gains[i] = (sign_tsne(dY[i]) != sign_tsne(uY[i])) ? (gains[i] + .2) : (gains[i] * .8);
            if(gains[i] < .01) gains[i] = .01;

            // Perform gradient update (with momentum and gains)
            Y[i] = Y[i] + uY[i];
            uY[i] = momentum * uY[i] - eta * gains[i] * dY[i];
        }
    }
    
    static double sign_tsne(double x) { return (x == .0 ? .0 : (x < .0 ? -1.0 : 1.0)); }
    
    private void zeroMean(double [] X, int N, int D){
        // Compute data mean
        double [] mean = new double[D];
        for(int n = 0; n < N; n++) {
                for(int d = 0; d < D; d++) {
                        mean[d] += X[n * D + d];
                }
        }
        for(int d = 0; d < D; d++) {
                mean[d] /= (double) N;
        }

        // Subtract data mean
        for(int n = 0; n < N; n++) {
                for(int d = 0; d < D; d++) {
                        X[n * D + d] -= mean[d];
                }
        }
    }
    
    private double evaluateError(int [] row_P, int [] col_P, double [] val_P, double [] Y, int N, int D, double theta){
        // Get estimate of normalization term
        SPTree tree = new SPTree(D, Y, N);
        double [] buff = new double[D];
        double [] sum_Q = new double[1];
        for(int n = 0; n < N; n++) tree.computeNonEdgeForces(n, theta, buff, sum_Q);

        // Loop over all edges to compute t-SNE error
        int ind1, ind2;
        double C = .0, Q;
        for(int n = 0; n < N; n++) {
                ind1 = n * D;
                for(int i = row_P[n]; i < row_P[n + 1]; i++) {
                        Q = .0;
                        ind2 = col_P[i] * D;
                        for(int d = 0; d < D; d++) buff[d]  = Y[ind1 + d];
                        for(int d = 0; d < D; d++) buff[d] -= Y[ind2 + d];
                        for(int d = 0; d < D; d++) Q += buff[d] * buff[d];
                        Q = (1.0 / (1.0 + Q)) / sum_Q[0];
                        C += val_P[i] * log((val_P[i] + Double.MIN_VALUE) / (Q + Double.MIN_VALUE));
                }
        }

        return C;
    }
    
    class SPTree{
        final static int QT_NODE_CAPACITY = 1;
        
	protected SPTree parent;
	protected int dimension;
	protected boolean is_leaf;
	protected int size;
	protected int cum_size;
	
	 // Axis-aligned bounding box stored as a center with half-dimensions to represent the boundaries of this quad tree
        Cell boundary;
    
    // Indices in this space-partitioning tree node, corresponding center-of-mass, and list of all children
        double[] data;
        double[] center_of_mass;
        int [] index = new int[QT_NODE_CAPACITY];
    
    // Children
        SPTree [] children;
        int no_children;

        public SPTree(int D, double[] inp_data, int N) {
		// Compute mean, width, and height of current map (boundaries of SPTree)
		int nD = 0;
		double [] mean_Y = new double [D];
		double []  min_Y = new double [D]; 
		double []  max_Y = new double [D]; 
		for(int d = 0; d < D; d++)  {
			min_Y[d] = Double.POSITIVE_INFINITY;
			max_Y[d] = Double.NEGATIVE_INFINITY;
		}
		for( int n = 0; n < N; n++) {
			for( int d = 0; d < D; d++) {
				mean_Y[d] += inp_data[n * D + d];
				if(inp_data[nD + d] < min_Y[d]) min_Y[d] = inp_data[nD + d];
				if(inp_data[nD + d] > max_Y[d]) max_Y[d] = inp_data[nD + d];
			}
			nD += D;
		}
		for(int d = 0; d < D; d++) mean_Y[d] /= (double) N;

		// Construct SPTree
		double [] width = new double [D];
		for(int d = 0; d < D; d++) width[d] = max(max_Y[d] - mean_Y[d], mean_Y[d] - min_Y[d]) + 1e-5;
		init(null, D, inp_data, mean_Y, width);
		fill(N);
	}
        
        SPTree(SPTree inp_parent, int D, double [] inp_data, double [] inp_corner, double [] inp_width) {
		init(inp_parent, D, inp_data, inp_corner, inp_width);
        }
        
        void init(SPTree inp_parent, int D, double [] inp_data, double [] inp_corner, double [] inp_width)
	{
		parent = inp_parent;
		dimension = D;
		no_children = 2;
		for(int d = 1; d < D; d++) no_children *= 2;
		data = inp_data;
		is_leaf = true;
		size = 0;
		cum_size = 0;

		center_of_mass = new double[D];
		boundary = new Cell(dimension);
		for(int d = 0; d < D; d++) {
			boundary.setCorner(d, inp_corner[d]);
			boundary.setWidth( d, inp_width[d]);
			center_of_mass[d] = .0;
		}

		children = getTreeArray(no_children);
		for(int i = 0; i < no_children; i++) children[i] = null;
	}
        
        void fill(int N)
	{
		for(int i = 0; i < N; i++) insert(i);
        }
        
        boolean insert(int new_index)
	{
		// Ignore objects which do not belong in this quad tree
		double [] point = MatrixOps.extractRowFromFlatMatrix(data,new_index,dimension);

		if(!boundary.containsPoint(point))
			return false;

		// Online update of cumulative size and center-of-mass
		cum_size++;
		double mult1 = (double) (cum_size - 1) / (double) cum_size;
		double mult2 = 1.0 / (double) cum_size;
		for(int d = 0; d < dimension; d++) {
			center_of_mass[d] *= mult1;
			center_of_mass[d] += mult2 * point[d];
		}

		// If there is space in this quad tree and it is a leaf, add the object here
		if(is_leaf && size < QT_NODE_CAPACITY) {
			index[size] = new_index;
			size++;
			return true;
		}

		// Don't add duplicates for now (this is not very nice)
		boolean any_duplicate = false;
		for(int n = 0; n < size; n++) {
			boolean duplicate = true;
			for(int d = 0; d < dimension; d++) {
				if(point[d] != data[index[n] * dimension + d]) { duplicate = false; break; }
			}
			any_duplicate = any_duplicate || duplicate;
		}
		if(any_duplicate) return true;

		// Otherwise, we need to subdivide the current cell
		if(is_leaf) subdivide();

		// Find out where the point can be inserted
		for(int i = 0; i < no_children; i++) {
			if(children[i].insert(new_index)) return true;
		}

		// Otherwise, the point cannot be inserted (this should never happen)
		assert false;
		return false;
        }
        
        void subdivide() {

		// Create new children
		double [] new_corner = new double[dimension];
		double [] new_width  = new double[dimension];
		for(int i = 0; i < no_children; i++) {
			int div = 1;
			for(int d = 0; d < dimension; d++) {
				new_width[d] = .5 * boundary.getWidth(d);
				if((i / div) % 2 == 1) new_corner[d] = boundary.getCorner(d) - .5 * boundary.getWidth(d);
				else                   new_corner[d] = boundary.getCorner(d) + .5 * boundary.getWidth(d);
				div *= 2;
			}
			children[i] = getNewTree(this, new_corner, new_width);
		}

		// Move existing points to correct children
		for(int i = 0; i < size; i++) {
			boolean success = false;
			for(int j = 0; j < no_children; j++) {
				if(!success) success = children[j].insert(index[i]);
			}
			index[i] = -1;
		}

		// Empty parent node
		size = 0;
		is_leaf = false;
        }
        
        SPTree getNewTree(SPTree root, double[] new_corner, double[] new_width) {
		return new SPTree(root, dimension, data, new_corner, new_width);
        }
        
        SPTree[] getTreeArray(int no_children) {
		return new SPTree[no_children];
	}
        
        double computeNonEdgeForces(int point_index, double theta, double [] neg_f, Object accumulator){
            double [] sum_Q = (double []) accumulator;
            double [] buff = new double[dimension];
            // Make sure that we spend no time on empty nodes or self-interactions
            if(cum_size == 0 || (is_leaf && size == 1 && index[0] == point_index)) return 0.0;

            // Compute distance between point and center-of-mass
            double D = .0;
            int ind = point_index * dimension;
            // Check whether we can use this node as a "summary"
            double max_width = 0.0;
            double cur_width;
            for(int d = 0; d < dimension; d++) {
                    buff[d] = data[ind + d] - center_of_mass[d];
                    D += buff[d] * buff[d];
                    cur_width = boundary.getWidth(d);
                    max_width = (max_width > cur_width) ? max_width : cur_width;
            } 

            if(is_leaf || max_width / sqrt(D) < theta) {
                    // Compute and add t-SNE force between point and current node
                    D = 1.0 / (1.0 + D);
                    double mult = cum_size * D;
                    sum_Q[0] += mult;
                    mult *= D;
                    for(int d = 0; d < dimension; d++) neg_f[d] += mult * buff[d];
            }
            else {

                    // Recursively apply Barnes-Hut to children
                    for(int i = 0; i < no_children; i++) children[i].computeNonEdgeForces(point_index, theta, neg_f, sum_Q);
            }
            return sum_Q[0];
        }
        
        // Computes edge forces
	void computeEdgeForces(int [] row_P, int [] col_P, double [] val_P, int N, double [] pos_f){
            // Loop over all edges in the graph
            double [] buff = new double[dimension];
            int ind1 = 0;
            int ind2 = 0;
            double D;
            for(int n = 0; n < N; n++) {
                    for(int i = row_P[n]; i < row_P[n + 1]; i++) {

                            // Compute pairwise distance and Q-value
                            D = 1.0;
                            ind2 = col_P[i] * dimension;
                            for(int d = 0; d < dimension; d++) { 
                                    buff[d] = data[ind1 + d] - data[ind2 + d];
                                    D += buff[d] * buff[d];
                            } 
                            D = val_P[i] / D;

                            // Sum positive force
                            for(int d = 0; d < dimension; d++) pos_f[ind1 + d] += D * buff[d];
                    }
                    ind1 += dimension;
            }
        }
        
        class Cell {		
            int dimension;
            double [] corner;
            double [] width;

            // Constructs cell
            Cell(int inp_dimension) {
                    dimension = inp_dimension;
                    corner = new double[dimension];
                    width  = new double[dimension];
            }

            Cell(int inp_dimension, double [] inp_corner, double [] inp_width) {
                    dimension = inp_dimension;
                    corner = new double[dimension];
                    width  = new double[dimension];
                    for(int d = 0; d < dimension; d++) setCorner(d, inp_corner[d]);
                    for(int d = 0; d < dimension; d++) setWidth( d,  inp_width[d]);
            }

            double getCorner(int d) {
                    return corner[d];
            }

            double getWidth(int d) {
                    return width[d];
            }

            void setCorner(int d, double val) {
                    corner[d] = val;
            }

            void setWidth(int d, double val) {
                    width[d] = val;
            }

            // Checks whether a point lies in a cell
            boolean containsPoint(double point[])
            {
                    for(int d = 0; d < dimension; d++) {
                            if(corner[d] - width[d] > point[d]) return false;
                            if(corner[d] + width[d] < point[d]) return false;
                    }
                    return true;
            }
	}

        
    }
    
    
    
    

}
