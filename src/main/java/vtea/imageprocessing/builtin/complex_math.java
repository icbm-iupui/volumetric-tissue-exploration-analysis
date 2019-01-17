/*******************************************************************************
 * Copyright (c) 2013 Jay Unruh, Stowers Institute for Medical Research.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package vtea.imageprocessing.builtin;

public class complex_math{
	// here we calculate complex math functions
	public double absolute_square(double[] cin){
		return cin[0]*cin[0]+cin[1]*cin[1];
	}

	public float absolute_square(float[] cin){
		return cin[0]*cin[0]+cin[1]*cin[1];
	}

	public double[] complex_exp(double[] cin){
		// complex exponent function
		double[] temp=new double[2];
		temp[0]=Math.exp(cin[0])*Math.cos(cin[1]);
		temp[1]=Math.exp(cin[0])*Math.sin(cin[1]);
		return temp;
	}

	public float[] complex_exp(float[] cin){
		// complex exponent function
		float[] temp=new float[2];
		temp[0]=(float)(Math.exp(cin[0])*Math.cos(cin[1]));
		temp[1]=(float)(Math.exp(cin[0])*Math.sin(cin[1]));
		return temp;
	}

	public double[] complex_multiply(double[] cin1,double[] cin2){
		double[] temp=new double[2];
		temp[0]=cin1[0]*cin2[0]-cin1[1]*cin2[1];
		temp[1]=cin1[1]*cin2[0]+cin1[0]*cin2[1];
		return temp;
	}

	public float[] complex_multiply(float[] cin1,float[] cin2){
		float[] temp=new float[2];
		temp[0]=cin1[0]*cin2[0]-cin1[1]*cin2[1];
		temp[1]=cin1[1]*cin2[0]+cin1[0]*cin2[1];
		return temp;
	}

	public double[] complex_divide(double[] cinnum,double[] cinden){
		double[] temp=new double[2];
		double temp2=absolute_square(cinden);
		temp[0]=cinden[0];
		temp[1]=cinden[1];
		temp=complex_multiply(cinnum,temp);
		temp[0]/=temp2;
		temp[1]/=temp2;
		return temp;
	}

	public float[] complex_divide(float[] cinnum,float[] cinden){
		float[] temp=new float[2];
		float temp2=absolute_square(cinden);
		temp[0]=cinden[0];
		temp[1]=cinden[1];
		temp=complex_multiply(cinnum,temp);
		temp[0]/=temp2;
		temp[1]/=temp2;
		return temp;
	}

	public double[] complex_subtract(double[] cin1,double[] cin2){
		return new double[]{cin1[0]-cin2[0],cin1[1]-cin2[1]};
	}

	public float[] complex_subtract(float[] cin1,float[] cin2){
		return new float[]{cin1[0]-cin2[0],cin1[1]-cin2[1]};
	}

	public double[] complex_add(double[] cin1,double[] cin2){
		return new double[]{cin1[0]+cin2[0],cin1[1]+cin2[1]};
	}

	public float[] complex_add(float[] cin1,float[] cin2){
		return new float[]{cin1[0]+cin2[0],cin1[1]+cin2[1]};
	}
}
